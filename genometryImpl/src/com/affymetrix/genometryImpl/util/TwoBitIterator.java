package com.affymetrix.genometryImpl.util;

import com.affymetrix.genometryImpl.MutableSeqSymmetry;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.symmetry.SimpleMutableSeqSymmetry;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 *
 * @author hiralv
 * @version $Id$
 */
public final class TwoBitIterator implements SearchableCharIterator {

	/** Use a 4KB buffer, as that is the block size of most filesystems */
	private static final int BUFFER_SIZE = 4096;
	
	/** Number of residues in each byte */
	private static final int RESIDUES_PER_BYTE = 4;

	/** Byte mask for translating unsigned bytes into Java integers */
    private static final int BYTE_MASK = 0xff;

	/** Character mask for translating binary into Java chars */
	private static final int CHAR_MASK = 0x03;

	private static final char[] BASES = { 'T', 'C', 'A', 'G', 't', 'c', 'a', 'g'};

	private final File file;
	private final long length, offset;
	private final MutableSeqSymmetry nBlocks, maskBlocks;
	private final ByteOrder byteOrder;

	public TwoBitIterator(File file, long length, long offset, ByteOrder byteOrder, MutableSeqSymmetry nBlocks, MutableSeqSymmetry maskBlocks) {
		this.file       = file;
		this.length     = length;
		this.offset     = offset;
		this.nBlocks    = nBlocks;
		this.maskBlocks = maskBlocks;
		this.byteOrder  = byteOrder;

		if (this.length > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("IGB can not handle sequences larger than " + Integer.MAX_VALUE + ".  Offending sequence length: " + length);
		}

	}

	private void loadBuffer(FileChannel channel, ByteBuffer buffer) throws IOException {
		buffer.rewind();
		channel.read(buffer);
		buffer.rewind();
	}

	public String substring(int start, int end) {
		FileChannel channel = null;

		//Sanity Check
		start = Math.max(0, start);
		end = Math.max(end, start);
		end = Math.min(end, getLength());

		int requiredLength = end - start;
		char[] residues = new char[requiredLength];
		byte[] valueBuffer = new byte[BUFFER_SIZE];
		long residuePosition = start;
		int residueCounter = 0;
		long startOffset = start / RESIDUES_PER_BYTE;
		long bytesToRead = calculateBytesToRead(start, end);
		int beginLength = Math.min(RESIDUES_PER_BYTE - start % 4,requiredLength);
		int endLength = Math.min(end % RESIDUES_PER_BYTE,requiredLength);
		if (bytesToRead == 1) {
			if (start % RESIDUES_PER_BYTE == 0) {
				beginLength = 0;
			} else {
				endLength = 0;
			}
		}

		MutableSeqSymmetry tempNBlocks = GetBlocks(start, nBlocks);
		MutableSeqSymmetry tempMaskBlocks = GetBlocks(start, maskBlocks);
		
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		buffer.order(this.byteOrder);
		try {

			channel = new RandomAccessFile(file, "r").getChannel();
			channel.position(this.offset + startOffset);
			loadBuffer(channel, buffer);
			
			//packedDNA
			SeqSpan nBlock = null;
			SeqSpan maskBlock = null;
			char[] temp = null;

			for (int i = 0; i < bytesToRead; i += BUFFER_SIZE) {
				buffer.get(valueBuffer);
				for (int k = 0; k < BUFFER_SIZE && residueCounter < requiredLength; k++) {

					if(bytesToRead == 1){
						temp = parseByte(valueBuffer[k],start%RESIDUES_PER_BYTE,requiredLength);
					}else if (k == 0 && beginLength != 0) {
						temp = parseByte(valueBuffer[k], beginLength, true);
					} else if (k == bytesToRead - 1 && endLength != 0) {
						temp = parseByte(valueBuffer[k], endLength, false);
					} else {
						temp = parseByte(valueBuffer[k]);
					}
					for (int j = 0; j < temp.length; j++) {
						nBlock = processResidue(residuePosition, temp, j, nBlock, tempNBlocks, false);
						maskBlock = processResidue(residuePosition, temp, j, maskBlock, tempMaskBlocks, true);
						residues[residueCounter++] = temp[j];
						residuePosition++;
					}		
				}
				channel.position(channel.position() + BUFFER_SIZE);
				loadBuffer(channel, buffer);
			}
				
		} catch (Exception ex) {
			ex.printStackTrace();
		}finally{
			GeneralUtils.safeClose(channel);
			valueBuffer = null;
			buffer = null;
			tempNBlocks = null;
			tempMaskBlocks = null;
		}
		return new String(residues);
	}

	private static long calculateBytesToRead(long start, long end) {

		if(start/RESIDUES_PER_BYTE == end/RESIDUES_PER_BYTE)
			return 1;

		int endExtra = end % RESIDUES_PER_BYTE == 0 ? 0 : 1;
		long bytesToRead = (end/RESIDUES_PER_BYTE) - (start/RESIDUES_PER_BYTE) + endExtra;

		return bytesToRead;
	}

	private static MutableSeqSymmetry GetBlocks(long start, MutableSeqSymmetry blocks){

		MutableSeqSymmetry tempBlocks =  new SimpleMutableSeqSymmetry();

		for(int i=0; i<blocks.getSpanCount(); i++){
			SeqSpan span = blocks.getSpan(i);
			if(start > span.getStart() && start >= span.getEnd()){
				continue;
			}
			tempBlocks.addSpan(span);

		}

		return tempBlocks;
	}
	private static SeqSpan processResidue(long residuePosition, char temp[], int pos, SeqSpan block, MutableSeqSymmetry blocks, boolean isMask){
		if (block == null) {
			block = GetNextBlock(blocks);
		}

		if (block != null) {
			if (residuePosition == block.getEnd()) {
				blocks.removeSpan( block);
				block = null;
			} else if (residuePosition >= block.getStart()) {
				if(isMask)
					temp[pos] = Character.toLowerCase(temp[pos]);
				else
					temp[pos] = 'N';
			}
		}
		return block;
	}

	private static SeqSpan GetNextBlock(MutableSeqSymmetry Blocks){
		if(Blocks.getSpanCount() > 0) {
			return Blocks.getSpan(0);
		}
		return null;
	}

	private static char[] parseByte(byte valueBuffer, int size, boolean isFirst){
		char temp[] = parseByte(valueBuffer);
		char newTemp[] = new char[size];

		if(isFirst){
			int skip = temp.length - size;
			for(int i=0; i<size; i++){
				newTemp[i] = temp[skip+i];
			}
		}else{
			for(int i=0; i<size; i++){
				newTemp[i] = temp[i];
			}
		}
		return newTemp;
	}

	private static char[] parseByte(byte valueBuffer, int position, int length) {
		char temp[] = parseByte(valueBuffer);
		char newTemp[] = new char[length];

		for(int i=0; i<length; i++){
			newTemp[i] = temp[position+i];
		}

		return newTemp;
	}
	
	private static char[] parseByte(byte valueBuffer){
		char temp[] = new char[RESIDUES_PER_BYTE];
		int dna, value = valueBuffer & BYTE_MASK;

		for (int j = RESIDUES_PER_BYTE; j > 0; j--) {
			dna = value & CHAR_MASK;
			value = value >> 2;
			temp[j-1] = BASES[dna];
		}

		return temp;
	}
	
	public int indexOf(String needle, int offset) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getLength() {
		return (int) length;
	}
}
