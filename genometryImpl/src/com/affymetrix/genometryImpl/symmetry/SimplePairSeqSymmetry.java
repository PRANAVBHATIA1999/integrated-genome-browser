/**
 *   Copyright (c) 2001-2007 Affymetrix, Inc.
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

package com.affymetrix.genometryImpl.symmetry;

import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.MutableSeqSpan;
import java.util.List;

public final class SimplePairSeqSymmetry implements SeqSymmetry {

	private List<SeqSymmetry> children = null;
	private static int count = 2;
	private SeqSpan spanA;
	private SeqSpan spanB;

	public SimplePairSeqSymmetry(SeqSpan spanA, SeqSpan spanB) {
		this.spanA = spanA;
		this.spanB = spanB;
	}

	public SeqSpan getSpan(BioSeq seq) {
		if (spanA.getBioSeq() == seq) { return spanA; }
		else if (spanB.getBioSeq() == seq) { return spanB; }
		else  { return null; }
	}

	public int getSpanCount() {
		return count;
	}

	public SeqSpan getSpan(int i) {
		if (i == 0) { return spanA; }
		else if (i == 1) { return spanB; }
		else { return null; }
	}

	public BioSeq getSpanSeq(int i) {
		if (i == 0) { return spanA.getBioSeq(); }
		else if (i == 1) { return spanB.getBioSeq(); }
		else { return null; }
	}

	public boolean getSpan(BioSeq seq, MutableSeqSpan span) {
		if (seq == spanA.getBioSeq()) {
			span.setStart(spanA.getStart());
			span.setEnd(spanA.getEnd());
			span.setBioSeq(spanA.getBioSeq());
			return true;
		}
		else if (seq == spanB.getBioSeq()) {
			span.setStart(spanB.getStart());
			span.setEnd(spanB.getEnd());
			span.setBioSeq(spanB.getBioSeq());
			return true;
		}
		return false;
	}

	public boolean getSpan(int index, MutableSeqSpan span) {
		if (index == 0) {
			span.setStart(spanA.getStart());
			span.setEnd(spanA.getEnd());
			span.setBioSeq(spanA.getBioSeq());
			return true;
		}
		else if (index == 1) {
			span.setStart(spanB.getStart());
			span.setEnd(spanB.getEnd());
			span.setBioSeq(spanB.getBioSeq());
			return true;
		}
		return false;
	}

	public SeqSymmetry getChild(int index) {
		if ((children == null) || (index >= children.size())) { return null; }
		else { return children.get(index); }
	}

	public int getChildCount() {
		if (children == null) { return 0; }
		else { return children.size(); }
	}

	public String getID() { return null; }
}
