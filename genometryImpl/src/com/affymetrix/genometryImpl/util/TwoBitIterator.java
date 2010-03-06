package com.affymetrix.genometryImpl.util;

import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sgblanch
 * @version $Id$
 */
public final class TwoBitIterator implements SearchableCharIterator {
	private static final int BASES_PER_BYTE = 4;
	private static final int BUFFER_SIZE = 4096;

	private static final char[] BASES = { 'T', 'C', 'A', 'G', 't', 'c', 'a', 'g'};

	private final File file;
	private final long length, offset;
	private final SeqSymmetry nBlocks, maskBlocks;
	private final List<Segment> segments = new ArrayList<Segment>();
	private final ByteOrder byteOrder;

	public TwoBitIterator(File file, long length, long offset, ByteOrder byteOrder, SeqSymmetry nBlocks, SeqSymmetry maskBlocks) {
		this.file       = file;
		this.length     = length;
		this.offset     = offset;
		this.nBlocks    = nBlocks;
		this.maskBlocks = maskBlocks;
		this.byteOrder  = byteOrder;

		if (this.length > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("IGB can not handle sequences larger than " + Integer.MAX_VALUE + ".  Offending sequence length: " + length);
		}

		int  current_start = 0;
		long current_offset = offset;
		SeqSpan block;
		for (int i=0; i < nBlocks.getSpanCount(); i++) {
			block = nBlocks.getSpan(i);
			if (block.getMin() > current_start) {
				segments.add(new Segment(current_start, block.getMin(), current_offset));
				current_offset += block.getMin() - current_start;
				current_start   = block.getMax();
			}
		}
	}

	private void loadBuffer(FileChannel channel, ByteBuffer buffer) throws IOException {
		buffer.rewind();
		channel.read(buffer);
		buffer.rewind();
	}

	public String substring(int offset, int length) {
		char bases[] = new char[length];

		for (Segment segment : segments) {
			if (offset < segment.start) {
				continue;
			}

			if (offset + length < segment.end) {
				break;
			}
		}

		throw new UnsupportedOperationException("Not supported yet.");

		//return new String(bases);
	}

	public int indexOf(String needle, int offset) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getLength() {
		return (int) length;
	}

	private static final class Segment {
		protected final int start, end;
		protected final long offset;

		protected Segment(int start, int end, long offset) {
			this.start  = start;
			this.end    = end;
			this.offset = offset;
		}
	}
}
