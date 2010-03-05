package com.affymetrix.genometryImpl.parsers;

import com.affymetrix.genometryImpl.MutableSeqSymmetry;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.symmetry.SimpleMutableSeqSymmetry;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 *
 * @author sgblanch
 * @version $Id$
 */
public final class TwoBitParser {
	/** Magic Number of 2bit files */
    private static final int MAGIC_NUMBER = 0x1A412743;

	/** Size of integer, in bytes */
	private static final int INT_SIZE = 4;

	/** Use a 4KB buffer, as that is the block size of most filesystems */
	private static final int BUFFER_SIZE = 4096;

    /** Byte mask for translating unsigned bytes into Java integers */
    private static final int BYTE_MASK = 0xff;

    /** Byte mask for translating unsigned ints into Java longs */
    private static final long INT_MASK = 0xffffffff;

	/** Character set used to decode strings.  Currently ASCII */
    private static final Charset charset = Charset.forName("ASCII");

	private File file;

	private static final char[] BASES = { 'T', 'C', 'A', 'G', 't', 'c', 'a', 'g'};

    public void open(File file, AnnotatedSeqGroup seq_group) throws FileNotFoundException, IOException {
		this.file = file;
        FileChannel channel = new RandomAccessFile(file, "r").getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		loadBuffer(channel, buffer);
        int seq_count = readFileHeader(buffer);
        readSequenceIndex(channel, buffer, seq_count, seq_group);
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
	private void loadBuffer(FileChannel channel, ByteBuffer buffer) throws IOException {
		buffer.rewind();
		channel.read(buffer);
		//buffer.order(byteOrder);
		buffer.rewind();
	}

    private int readFileHeader(ByteBuffer buffer) throws IOException {
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

    private void readBlocks(FileChannel channel, ByteBuffer buffer, BioSeq seq, MutableSeqSymmetry sym) throws IOException {
		//xBlockCount, where x = n OR mask
		int block_count = buffer.getInt();
		System.out.println("I want " + block_count + " blocks");
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


    private void readSequenceIndex(FileChannel channel, ByteBuffer buffer, int seq_count, AnnotatedSeqGroup seq_group) throws IOException {
        String name;
        int name_length;
		long offset, position;

		position = channel.position();
		for (int i = 0; i < seq_count; i++) {
			if (buffer.remaining() < INT_SIZE) {
				position = updateBuffer(channel, buffer, position);
			}

			name_length = buffer.get() & BYTE_MASK;

			if (buffer.remaining() < name_length + INT_SIZE) {
				position = updateBuffer(channel, buffer, position);
			}

			name   = getString(buffer, name_length);
			offset = buffer.getInt() & INT_MASK;

			System.out.println("Sequence '" + name + "', offset " + offset);
			readSequenceHeader(channel, buffer.order(), offset, seq_group, name);
        }
    }

    private void readSequenceHeader(FileChannel channel, ByteOrder order, long offset, AnnotatedSeqGroup seq_group, String name) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		buffer.order(order);
		MutableSeqSymmetry nBlocks    = new SimpleMutableSeqSymmetry();
		MutableSeqSymmetry maskBlocks = new SimpleMutableSeqSymmetry();

		long oldPosition = channel.position();
        channel.position(offset);
        loadBuffer(channel, buffer);

		//dnaSize
        long size = buffer.getInt() & INT_MASK;
		System.out.println("size is " + size + " bases");

		if (size > Integer.MAX_VALUE) {
			throw new IOException("IGB can not handle sequences larger than " + Integer.MAX_VALUE + ".  Offending sequence length: " + size);
		}

		BioSeq seq = seq_group.addSeq(name, (int) size);

		//nBlockCount, nBlockStart, nBlockSize
        readBlocks(channel, buffer, seq, nBlocks);

		//maskBlockCount, maskBlockStart, maskBlockSize
		readBlocks(channel, buffer, seq, maskBlocks);

		//reserved
        if (buffer.getInt() != 0) {
            throw new IOException("Unknown 2bit format: sequence's reserved field is non zero");
        }

		//packedDNA
		long length = size/4 + size%4;
		int value, dna;
		StringBuffer residues = new StringBuffer();
		StringBuffer temp;
		for(int i=0; i<length; i++){
			temp = new StringBuffer();
			value = buffer.get() & 0xff;

			for(int j=0; j<4; j++){
				dna = value & 0x03;
				value = value >> 2;
				temp.append(BASES[dna]);
			}
			temp.reverse();
			residues.append(temp);
		}
		residues = residues.delete((int)size, residues.length());

		for(int i=0; i<maskBlocks.getSpanCount(); i++){
			SeqSpan block = maskBlocks.getSpan(i);
			String subString = residues.substring(block.getStart(), block.getEnd()).toLowerCase();
			residues.replace(block.getStart(), block.getEnd(), subString);
		}

		for(int i=0; i<nBlocks.getSpanCount(); i++){
			SeqSpan block = nBlocks.getSpan(i);
			char subString[] = new char[block.getEnd() - block.getStart()];
			Arrays.fill(subString, 'N');
			residues.replace(block.getStart(), block.getEnd(), new String(subString));
		}
		
		System.out.println("residues :"+residues);
		//seq.setResiduesProvider(new TwoBitIterator(new Byte[4], nBlocks, maskBlocks, offset));
		//channel.position(oldPosition);
		
    }

	private long updateBuffer(FileChannel channel, ByteBuffer buffer, long position) throws IOException {
		channel.position(position - buffer.remaining());
		loadBuffer(channel, buffer);
		return channel.position();
	}

	public static void main(String[] args){
		File f = new File("/Users/aloraine/Downloads/tests/output/genbank.2bit");
		TwoBitParser instance = new TwoBitParser();
		try {
			instance.open(f, new AnnotatedSeqGroup("foo"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
