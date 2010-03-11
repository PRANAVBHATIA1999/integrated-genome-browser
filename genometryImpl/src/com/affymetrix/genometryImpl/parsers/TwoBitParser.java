package com.affymetrix.genometryImpl.parsers;

import com.affymetrix.genometryImpl.MutableSeqSymmetry;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.symmetry.SimpleMutableSeqSymmetry;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.TwoBitIterator;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * @author sgblanch
 * @author hiralv
 * @version $Id$
 */
public final class TwoBitParser {
	/** Magic Number of 2bit files */
    private static final int MAGIC_NUMBER = 0x1A412743;

	/** Size of integer, in bytes */
	private static final int INT_SIZE = 4;

	/** Use a 4KB buffer, as that is the block size of most filesystems */
	private static  int BUFFER_SIZE = 4096;

    /** Byte mask for translating unsigned bytes into Java integers */
    private static final int BYTE_MASK = 0xff;

    /** Byte mask for translating unsigned ints into Java longs */
    private static final long INT_MASK = 0xffffffff;

	/** Character set used to decode strings.  Currently ASCII */
    private static final Charset charset = Charset.forName("ASCII");

	/** buffer for outputting */
	private static int BUFSIZE = 65536;

	private static final boolean DEBUG = false;
	
    public static BioSeq parse(File file, AnnotatedSeqGroup seq_group) throws FileNotFoundException, IOException {
        FileChannel channel = new RandomAccessFile(file, "r").getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		loadBuffer(channel, buffer);
        int seq_count = readFileHeader(buffer);
        BioSeq seq = readSequenceIndex(file, channel, buffer, seq_count, seq_group);
		GeneralUtils.safeClose(channel);
		return seq;
    }

	public static BioSeq parse(File file) throws FileNotFoundException, IOException {
		return parse(file,new AnnotatedSeqGroup("No_Data"));
	}

	public static boolean parse(File file, OutputStream out) throws FileNotFoundException, IOException {
		BioSeq seq = parse(file);
		return writeAnnotations(seq,0,seq.getLength(),out);
	}

	public static boolean parse(File file, int start, int end, OutputStream out) throws FileNotFoundException, IOException {
		BioSeq seq = parse(file);
		return writeAnnotations(seq,start,end,out);
	}

	public static boolean parse(File file, AnnotatedSeqGroup seq_group, OutputStream out) throws FileNotFoundException, IOException {
		BioSeq seq = parse(file,seq_group);
		return writeAnnotations(seq,0,seq.getLength(),out);
	}

	public static boolean parse(File file, AnnotatedSeqGroup seq_group, int start, int end, OutputStream out) throws FileNotFoundException, IOException {
		BioSeq seq = parse(file,seq_group);
		return writeAnnotations(seq,start,end,out);
	}
	
    private static String getString(ByteBuffer buffer, int length) {
        byte[] string = new byte[length];
        buffer.get(string);
        return new String(string, charset);
    }

	/**
	 * Load data from the channel into the buffer.  This convenience method is
	 * used to ensure that the buffer has the correct endian and is rewound.
	 */
	private static void loadBuffer(FileChannel channel, ByteBuffer buffer) throws IOException {
		buffer.rewind();
		channel.read(buffer);
		//buffer.order(byteOrder);
		buffer.rewind();
	}

    private static int readFileHeader(ByteBuffer buffer) throws IOException {
        /* Java is big endian so try that first */
        int magic = buffer.getInt();

        /* Try little endian if big endian did not work */
        if (magic != MAGIC_NUMBER) {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.rewind();
			magic = buffer.getInt();
        }

        /* Fail if we have no magic */
        if (magic != MAGIC_NUMBER) {
            throw new IOException("File is not in 2bit format:  Bad magic (0x" + Integer.toHexString(magic) + " actual, 0x" + Integer.toHexString(MAGIC_NUMBER) + " expected)");
        }

        /* Grab the rest of the header fields */
        int version = buffer.getInt();
        int seq_count = buffer.getInt();
        int reserved = buffer.getInt();

        /* Currently version and 'reserved' should be zero */
        if (version != 0 || reserved != 0) {
            throw new IOException("Unsupported 2bit format: version(" + version + ") and reserved(" + reserved + ") must equal 0");
        }

        return seq_count;
    }

    private static void readBlocks(ByteBuffer buffer, BioSeq seq, MutableSeqSymmetry sym) throws IOException {
		//xBlockCount, where x = n OR mask
		int block_count = buffer.getInt();

		if(DEBUG){
			System.out.println("I want " + block_count + " blocks");
		}

        int[] blockStarts = new int[block_count];
        //ByteBuffer buffer = ByteBuffer.allocate(2 * block_count * INT_SIZE + INT_SIZE);
        for (int i = 0; i < block_count; i++) {
			//xBlockStart, where x = n OR mask
            blockStarts[i] = buffer.getInt();
        }

        for (int i = 0; i < block_count; i++) {
			//xBlockSize, where x = n OR mask
			sym.addSpan(new SimpleSeqSpan(blockStarts[i], blockStarts[i] + buffer.getInt(), seq));
        }

    }

    private static BioSeq readSequenceIndex(File file, FileChannel channel, ByteBuffer buffer, int seq_count, AnnotatedSeqGroup seq_group) throws IOException {
        String name;
        int name_length;
		long offset, position;

		position = channel.position();
		//for (int i = 0; i < seq_count; i++) {
		if (buffer.remaining() < INT_SIZE) {
			position = updateBuffer(channel, buffer, position);
		}

		name_length = buffer.get() & BYTE_MASK;

		if (buffer.remaining() < name_length + INT_SIZE) {
			position = updateBuffer(channel, buffer, position);
		}

		name = getString(buffer, name_length);
		offset = buffer.getInt() & INT_MASK;

		if(DEBUG){
			System.out.println("Sequence '" + name + "', offset " + offset);
		}
		
		return readSequenceHeader(file, channel, buffer.order(), offset, seq_group, name);
		//}
    }

    private static BioSeq readSequenceHeader(File file, FileChannel channel, ByteOrder order, long offset, AnnotatedSeqGroup seq_group, String name) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		buffer.order(order);
		MutableSeqSymmetry nBlocks    = new SimpleMutableSeqSymmetry();
		MutableSeqSymmetry maskBlocks = new SimpleMutableSeqSymmetry();
		long residueOffset = offset;

        channel.position(offset);
        loadBuffer(channel, buffer);

		//dnaSize
        long size = buffer.getInt() & INT_MASK;

		if(DEBUG){
			System.out.println("size is " + size + " bases");
		}
		
		residueOffset += INT_SIZE;

		if (size > Integer.MAX_VALUE) {
			throw new IOException("IGB can not handle sequences larger than " + Integer.MAX_VALUE + ".  Offending sequence length: " + size);
		}

		BioSeq seq = seq_group.addSeq(name, (int) size);

		//nBlockCount, nBlockStart, nBlockSize
        readBlocks(buffer, seq, nBlocks);
		residueOffset += INT_SIZE + nBlocks.getSpanCount() * INT_SIZE * 2;

		//maskBlockCount, maskBlockStart, maskBlockSize
		readBlocks(buffer, seq, maskBlocks);
		residueOffset += INT_SIZE + maskBlocks.getSpanCount() * INT_SIZE * 2;

		//reserved
        if (buffer.getInt() != 0) {
            throw new IOException("Unknown 2bit format: sequence's reserved field is non zero");
        }
		residueOffset += INT_SIZE;

		seq.setResiduesProvider(new TwoBitIterator(file,size,residueOffset,buffer.order(),nBlocks,maskBlocks));

		return seq;
    }

	private static long updateBuffer(FileChannel channel, ByteBuffer buffer, long position) throws IOException {
		channel.position(position - buffer.remaining());
		loadBuffer(channel, buffer);
		return channel.position();
	}

	public static String getMimeType() {
		return "binary/2bit";
	}

	private static boolean writeAnnotations(BioSeq seq, int start, int end, OutputStream outstream)
	{
		if (seq.getResiduesProvider() == null) {
			return false;
		}
		// sanity checks
		start = Math.max(0, start);
		end = Math.max(end, start);
		end = Math.min(end, start+seq.getResiduesProvider().getLength());

		DataOutputStream dos = null;
		try
		{
			dos = new DataOutputStream(new BufferedOutputStream(outstream));

			// Only keep BUFSIZE characters in memory at one time
			for (int i=start;i<end;i+=BUFSIZE) {
				String outString = seq.getResidues(i, Math.min(i+BUFSIZE, end));
				dos.writeBytes(outString);
			}
			dos.flush();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public static void main(String[] args){
		String input_string = "ACTGGGTCTCAGTACTAGGAATTCCGTCATAGCTAAA";
		File f = new File("genometryImpl/test/data/2bit/noblocks.2bit");
		//File f = new File("genometryImpl/test/data/2bit/at.2bit");
		try {
			BioSeq seq = TwoBitParser.parse(f);
			int start = 7;
			int end = 8;
			System.out.println("Expected :"+input_string.substring(start, end));
			System.out.println("Result   :"+seq.getResidues(start,end));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
