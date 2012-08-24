package com.affymetrix.genometryImpl.symloader;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.genometryImpl.util.SeekableBufferedStream;
import com.affymetrix.genometryImpl.util.Timer;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class is a parser of UCSC Genome Browser file format .2bit used to store
 * nucleotide sequence information. This class extends InputStream and can be
 * used as it after choosing one of names of containing sequences. This parser
 * can be used to do some work like UCSC tool named twoBitToFa. For it just run
 * this class with input file path as single parameter and set stdout stream
 * into output file. If you have any problems or ideas don't hesitate to contact
 * me through email: rsutormin[at]gmail.com.
 * 
 * Ref : storage.bioinf.fbb.msu.ru/~roman/TwoBitParser.java
 * @author Roman Sutormin (storage.bioinf.fbb.msu.ru/~roman)
 */
public class TwoBitNew extends SymLoader {

    public static boolean DEBUG = false;
    public int DEFAULT_BUFFER_SIZE = 10000;
    //
    private SeekableBufferedStream raf;
    private boolean reverse = false;
    private HashMap<String, Long> seq2pos = new HashMap<String, Long>();
    private String cur_seq_name;
    private long[][] cur_nn_blocks;
    private long[][] cur_mask_blocks;
    private long cur_seq_pos;
    private long cur_dna_size;
    private int cur_nn_block_num;
    private int cur_mask_block_num;
    private int[] cur_bits;
    private byte[] buffer;
    private long buffer_size;
    private long buffer_pos;
    private long start_file_pos;
    private long file_pos;
    //
    private static final char[] bit_chars = {
        'T', 'C', 'A', 'G'
    };

	private final Map<BioSeq, String> chrMap = new HashMap<BioSeq, String>();
	
    public TwoBitNew(URI uri, String featureName, AnnotatedSeqGroup group){
		super(uri, featureName, group);  
    }

	@Override
	public void init() throws Exception{
		if (this.isInitialized) {
			return;
		}
		raf = new SeekableBufferedStream(LocalUrlCacher.getSeekableStream(uri));
		long sign = readFourBytes();
        if (sign == 0x1A412743) {
            if (DEBUG) {
                System.err.println("2bit: Normal number architecture");
            }
        } else if (sign == 0x4327411A) {
            reverse = true;
            if (DEBUG) {
                System.err.println("2bit: Reverse number architecture");
            }
        } else {
            throw new Exception("Wrong start signature in 2BIT format");
        }
        readFourBytes();
        int seq_qnt = (int) readFourBytes();
        readFourBytes();
		String seq_name;
        BioSeq seq;
        for (int i = 0; i < seq_qnt; i++) {
            int name_len = raf.read();
            char[] chars = new char[name_len];
            for (int j = 0; j < name_len; j++) {
                chars[j] = (char) raf.read();
            }
            seq_name = new String(chars);
			seq = group.getSeq(seq_name);
			if(seq == null)
				continue;
			
			chrMap.put(seq, seq_name);
            long pos = readFourBytes();
            seq2pos.put(seq_name, pos);
            if (DEBUG) {
                System.err.println("2bit: Sequence name=[" + seq_name + "], "
                        + "pos=" + pos);
            }
        }
		super.init();
	}
	
	@Override
	public List<BioSeq> getChromosomeList() throws Exception  {
		init();
		return new ArrayList<BioSeq>(chrMap.keySet());
	}

	@Override
	public String getRegionResidues(SeqSpan span) throws Exception  {
		init();
		BioSeq seq = span.getBioSeq();
		if(chrMap.containsKey(seq)){
			this.setCurrentSequence(chrMap.get(seq));
			return getResidueString(span.getMin(), span.getMax() - span.getMin());
		}
		
		Logger.getLogger(TwoBit.class.getName()).log(Level.WARNING,"Seq {0} not present {1}",new Object[]{seq.getID(), uri.toString()});
		return "";
	}
	
	private String getResidueString(int start, int len) throws IOException{
		if (cur_seq_name == null) {
            throw new RuntimeException("Sequence is not set");
        }
        System.out.println(">" + cur_seq_name + " pos=" + cur_seq_pos + ", len=" + len);
        
		char[] residues = new char[len];
		int ch;
		
		setCurrentSequencePosition(start);
        for (int qnt = 0; (qnt < residues.length); qnt++) {
			ch = read();
			if (ch < 0) {
				break;
			}
			residues[qnt] = (char) ch;
		}
		close();
		
		return new String(residues);
	}
	
    private long readFourBytes() throws Exception {
        long ret = 0;
        if (!reverse) {
            ret = raf.read();
            ret += raf.read() * 0x100;
            ret += raf.read() * 0x10000;
            ret += raf.read() * 0x1000000;
        } else {
            ret = raf.read() * 0x1000000;
            ret += raf.read() * 0x10000;
            ret += raf.read() * 0x100;
            ret += raf.read();
        }
        return ret;
    }

    /**
     * Method open nucleotide stream for sequence with given name.
     *
     * @param seq_name name of sequence (one of returned by getSequenceNames()).
     * @throws Exception
     */
    private void setCurrentSequence(String seq_name) throws Exception {
        if (cur_seq_name != null) {
            throw new Exception("Sequence [" + cur_seq_name + "] was not closed");
        }
        if (seq2pos.get(seq_name) == null) {
            throw new Exception("Sequence [" + seq_name + "] was not found in 2bit file");
        }
        cur_seq_name = seq_name;
        long pos = seq2pos.get(seq_name);
        raf.seek(pos);
        long dna_size = readFourBytes();
        if (DEBUG) {
            System.err.println("2bit: Sequence name=[" + cur_seq_name + "], dna_size=" + dna_size);
        }
        cur_dna_size = dna_size;
        int nn_block_qnt = (int) readFourBytes();
        cur_nn_blocks = new long[nn_block_qnt][2];
        for (int i = 0; i < nn_block_qnt; i++) {
            cur_nn_blocks[i][0] = readFourBytes();
        }
        for (int i = 0; i < nn_block_qnt; i++) {
            cur_nn_blocks[i][1] = readFourBytes();
        }
        if (DEBUG) {
            System.err.print("NN-blocks: ");
            for (int i = 0; i < nn_block_qnt; i++) {
                System.err.print("[" + cur_nn_blocks[i][0] + "," + cur_nn_blocks[i][1] + "] ");
            }
            System.err.println();
        }
        int mask_block_qnt = (int) readFourBytes();
        cur_mask_blocks = new long[mask_block_qnt][2];
        for (int i = 0; i < mask_block_qnt; i++) {
            cur_mask_blocks[i][0] = readFourBytes();
        }
        for (int i = 0; i < mask_block_qnt; i++) {
            cur_mask_blocks[i][1] = readFourBytes();
        }
        if (DEBUG) {
            System.err.print("Mask-blocks: ");
            for (int i = 0; i < mask_block_qnt; i++) {
                System.err.print("[" + cur_mask_blocks[i][0] + "," + cur_mask_blocks[i][1] + "] ");
            }
            System.err.println();
        }
        readFourBytes();
        start_file_pos = raf.position();
        reset();
    }

    /**
     * Method resets current position to the begining of sequence stream.
     */
    private synchronized void reset() throws IOException {
        cur_seq_pos = 0;
        cur_nn_block_num = (cur_nn_blocks.length > 0) ? 0 : -1;
        cur_mask_block_num = (cur_mask_blocks.length > 0) ? 0 : -1;
        cur_bits = new int[4];
        file_pos = start_file_pos;
        buffer_size = 0;
        buffer_pos = -1;
    }

    /**
     * @return number (starting from 0) of next readable nucleotide in sequence
     * stream.
     */
    private long getCurrentSequencePosition() {
        if (cur_seq_name == null) {
            throw new RuntimeException("Sequence is not set");
        }
        return cur_seq_pos;
    }

    private void setCurrentSequencePosition(long pos) throws IOException {
        if (cur_seq_name == null) {
            throw new RuntimeException("Sequence is not set");
        }
        if (pos > cur_dna_size) {
            throw new RuntimeException(
                    "Postion is too high (more than " + cur_dna_size + ")");
        }
        if (cur_seq_pos > pos) {
            reset();
        }
        skip(pos - cur_seq_pos);
    }

    private void loadBits() throws IOException {
        if ((buffer == null) || (buffer_pos < 0) || (file_pos < buffer_pos)
                || (file_pos >= buffer_pos + buffer_size)) {
            if ((buffer == null) || (buffer.length != DEFAULT_BUFFER_SIZE)) {
                buffer = new byte[DEFAULT_BUFFER_SIZE];
            }
            buffer_pos = file_pos;
            buffer_size = raf.read(buffer);
        }
        int cur_byte = buffer[(int) (file_pos - buffer_pos)] & 0xff;
        for (int i = 0; i < 4; i++) {
            cur_bits[3 - i] = cur_byte % 4;
            cur_byte /= 4;
        }
    }

    /**
     * Method reads 1 nucleotide from sequence stream. You should set current
     * sequence before use it.
     */
    private int read() throws IOException {
        if (cur_seq_name == null) {
            throw new IOException("Sequence is not set");
        }
        if (cur_seq_pos == cur_dna_size) {
            if (DEBUG) {
                System.err.println("End of sequence (file position:" + raf.position() + " )");
            }
            return -1;
        }
        int bit_num = (int) cur_seq_pos % 4;
        if (bit_num == 0) {
            loadBits();
        } else if (bit_num == 3) {
            file_pos++;
        }
        char ret = 'N';
        if ((cur_nn_block_num >= 0)
                && (cur_nn_blocks[cur_nn_block_num][0] <= cur_seq_pos)) {
            if (cur_bits[bit_num] != 0) {
                throw new IOException("Wrong data in NN-block (" + cur_bits[bit_num] + ") "
                        + "at position " + cur_seq_pos);
            }
            if (cur_nn_blocks[cur_nn_block_num][0] + cur_nn_blocks[cur_nn_block_num][1] == cur_seq_pos + 1) {
                cur_nn_block_num++;
                if (cur_nn_block_num >= cur_nn_blocks.length) {
                    cur_nn_block_num = -1;
                }
            }
            ret = 'N';
        } else {
            ret = bit_chars[cur_bits[bit_num]];
        }
        if ((cur_mask_block_num >= 0)
                && (cur_mask_blocks[cur_mask_block_num][0] <= cur_seq_pos)) {
            ret = Character.toLowerCase(ret);
            if (cur_mask_blocks[cur_mask_block_num][0] + cur_mask_blocks[cur_mask_block_num][1] == cur_seq_pos + 1) {
                cur_mask_block_num++;
                if (cur_mask_block_num >= cur_mask_blocks.length) {
                    cur_mask_block_num = -1;
                }
            }
        }
        cur_seq_pos++;
        return (int) ret;
    }

    /**
     * Method skips n nucleotides in sequence stream. You should set current
     * sequence before use it.
     */
    private synchronized long skip(long n) throws IOException {
        if (cur_seq_name == null) {
            throw new IOException("Sequence is not set");
        }
        if (n < 4) {
            int ret = 0;
            while ((ret < n) && (read() >= 0)) {
                ret++;
            }
            return ret;
        }
        if (n > cur_dna_size - cur_seq_pos) {
            n = cur_dna_size - cur_seq_pos;
        }
        cur_seq_pos += n;
        file_pos = start_file_pos + (cur_seq_pos / 4);
        raf.seek(file_pos);
        if ((cur_seq_pos % 4) != 0) {
            loadBits();
        }
        while ((cur_nn_block_num >= 0)
                && (cur_nn_blocks[cur_nn_block_num][0] + cur_nn_blocks[cur_nn_block_num][1] <= cur_seq_pos)) {
            cur_nn_block_num++;
            if (cur_nn_block_num >= cur_nn_blocks.length) {
                cur_nn_block_num = -1;
            }
        }
        while ((cur_mask_block_num >= 0)
                && (cur_mask_blocks[cur_mask_block_num][0] + cur_mask_blocks[cur_mask_block_num][1] <= cur_seq_pos)) {
            cur_mask_block_num++;
            if (cur_mask_block_num >= cur_mask_blocks.length) {
                cur_mask_block_num = -1;
            }
        }
        return n;
    }

    /**
     * Method closes current sequence and it's necessary to invoke it before
     * setting new current sequence.
     */
    private void close() throws IOException {
        cur_seq_name = null;
        cur_nn_blocks = null;
        cur_mask_blocks = null;
        cur_seq_pos = -1;
        cur_dna_size = -1;
        cur_nn_block_num = -1;
        cur_mask_block_num = -1;
        cur_bits = null;
        buffer_size = 0;
        buffer_pos = -1;
        file_pos = -1;
        start_file_pos = -1;
    }

    private int available() throws IOException {
        if (cur_seq_name == null) {
            throw new IOException("Sequence is not set");
        }
        return (int) (cur_dna_size - cur_seq_pos);
    }

    /**
     * Method closes random access file descriptor. You can't use any reading
     * methods after it.
     *
     * @throws Exception
     */
    private void closeParser() throws Exception {
        raf.close();
    }

    private String loadFragment(long seq_pos, int len) throws IOException {
        if (cur_seq_name == null) {
            throw new IOException("Sequence is not set");
        }
        setCurrentSequencePosition(seq_pos);
        char[] ret = new char[len];
        int i = 0;
        for (; i < len; i++) {
            int ch = read();
            if (ch < 0) {
                break;
            }
            ret[i] = (char) ch;
        }
        return new String(ret, 0, i);
    }

    private void printFastaSequence() throws IOException {
        if (cur_seq_name == null) {
            throw new RuntimeException("Sequence is not set");
        }
        printFastaSequence(cur_dna_size - cur_seq_pos);
    }

    private void printFastaSequence(long len) throws IOException {
        if (cur_seq_name == null) {
            throw new RuntimeException("Sequence is not set");
        }
        System.out.println(">" + cur_seq_name + " pos=" + cur_seq_pos + ", len=" + len);
        char[] line = new char[60];
        boolean end = false;
        long qnt_all = 0;
        while (!end) {
            int qnt = 0;
            for (; (qnt < line.length) && (qnt_all < len); qnt++, qnt_all++) {
                int ch = read();
                if (ch < 0) {
                    end = true;
                    break;
                }
                line[qnt] = (char) ch;
            }
            if (qnt > 0) {
                System.out.println(new String(line, 0, qnt));
            }
            if (qnt_all >= len) {
                end = true;
            }
        }
    }

	
    public static void main(String[] args) throws Exception {
//        if (args.length == 0) {
//            System.out.println("Usage: <program> <input.2bit> [<seq_name> [<start> [<length>]]]");
//            System.out.println("Resulting fasta data will be written in stdout.");
//            return;
//        }
        TwoBitNew p = new TwoBitNew(new URI("http://igbquickload.org/quickload/H_sapiens_Feb_2009/H_sapiens_Feb_2009.2bit"), null, null);
		//p.setCurrentSequence(p.getSequenceNames()[0]);
		Timer timer = new Timer();
		timer.start();
		p.printFastaSequence(100000);
		timer.print();
		p.close();
//        if (args.length == 1) {
//            String[] names = p.getSequenceNames();
//            for (int i = 0; i < names.length; i++) {
//                p.setCurrentSequence(names[i]);
//                p.printFastaSequence();
//                p.close();
//            }
//        } else {
//            String name = args[1];
//            p.setCurrentSequence(name);
//            if (args.length > 2) {
//                long start = Long.parseLong(args[2]);
//                p.skip(start);
//            }
//            if (args.length > 3) {
//                long len = Long.parseLong(args[3]);
//                p.printFastaSequence(len);
//            } else {
//                p.printFastaSequence();
//            }
//            p.close();
//        }
        p.closeParser();
    }

}
