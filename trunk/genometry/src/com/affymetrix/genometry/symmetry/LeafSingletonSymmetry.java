/**
 *   Copyright (c) 2001-2005 Affymetrix, Inc.
 *
 *   Licensed under the Common Public License, Version 1.0 (the "License").
 *   A copy of the license must be included with any distribution of
 *   this source code.
 *   Distributions from Affymetrix, Inc., place this in the
 *   IGB_LICENSE.html file.
 *
 *   The license is also available at
 *   http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.genometry.symmetry;

import com.affymetrix.genometry.SeqSymmetry;
import com.affymetrix.genometry.MutableAnnotatedBioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.MutableSeqSpan;
import com.affymetrix.genometry.span.SimpleMutableSeqSpan;

/**
 * A symmetry containing only a single span, and no children
 * In other words, it has a "breadth" of one (span) and a "depth" of zero (children)
 */
public class LeafSingletonSymmetry extends SimpleMutableSeqSpan implements SeqSymmetry {

	protected static int count = 1;

	public LeafSingletonSymmetry(SeqSpan span) {
		start = span.getStart();
		end = span.getEnd();
		seq = span.getBioSeq();
	}

	public LeafSingletonSymmetry(int start, int end, MutableAnnotatedBioSeq seq) {
		super(start, end, seq);
	}

	public SeqSpan getSpan(MutableAnnotatedBioSeq seq) {
		if (this.getBioSeq() == seq) { return this; }
		else { return null; }
	}

	public int getSpanCount() {
		return count;
	}

	public SeqSpan getSpan(int i) {
		if (i == 0) { return this; }
		else { return null; }
	}

	public MutableAnnotatedBioSeq getSpanSeq(int i) {
		if (i == 0) { return seq; }
		else { return null; }
	}

	public boolean getSpan(MutableAnnotatedBioSeq seq, MutableSeqSpan span) {
		if (this.getBioSeq() == seq) {
			span.setStart(this.getStart());
			span.setEnd(this.getEnd());
			span.setBioSeq(this.getBioSeq());
			return true;
		}
		return false;
	}

	public boolean getSpan(int index, MutableSeqSpan span) {
		if (index == 0) {
			span.setStart(this.getStart());
			span.setEnd(this.getEnd());
			span.setBioSeq(this.getBioSeq());
			return true;
		}
		return false;
	}

	public int getChildCount() {  return 0; }
	public SeqSymmetry getChild(int index) { return null; }
	public String getID() { return null; }

}
