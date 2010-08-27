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

package com.affymetrix.genometryImpl.das2;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.MutableSeqSpan;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.SimpleSymWithProps;
import com.affymetrix.genometryImpl.TypedSym;
import com.affymetrix.genometryImpl.general.FeatureRequestSym;
import com.affymetrix.genometryImpl.span.SimpleMutableSeqSpan;

/**
 *  Encapsulates a <b>constrained</b> DAS2 feature query and the features returned from the query.
 *  The expectation is that if IGB user wants multiple regions on multiple seqs with multiple types,
 *      these queries will get broken down by IGB DAS2 query optimizer into multiple Das2FeatureRequestSyms
 *
 *  Constraints:
 *    Query for features on a single seq
 *    One overlap span on seq
 *    Zero or one inside span on seq
 *    One feature type
 *
 *  OR, do we want a more generic Das2FeatureRequestSym with a list of feature filters
 *  OR, break down into two classes -- this one and a more generic filter-based one...
 *
 *  sym.getSpanCount() = 1, and this SeqSpan is the encompass_span
 *       (bounds of all returned spans [or union of overlap_span and bounds of all returned spans?]
 *
 */
public final class Das2FeatureRequestSym extends FeatureRequestSym implements TypedSym  {  // or should extend TypeContainerAnnot?s

  private final Das2Region das2_region;
  private final Das2Type das2_type;

  private final BioSeq aseq;
  private MutableSeqSpan sum_child_spans;
  private String format;

  //  for now trying to do without container info in constructor
  public Das2FeatureRequestSym(Das2Type type, Das2Region region, SeqSpan overlap) {
	super(overlap);
    das2_type = type;
    das2_region = region;
    aseq = overlap.getBioSeq();

    this.setProperty(SimpleSymWithProps.CONTAINER_PROP, Boolean.TRUE);
  }

  public String getType() { return das2_type.getID(); }

  public Das2Region getRegion() { return das2_region; }
  public Das2Type getDas2Type() { return das2_type; }
  String getFormat() { return format; }
  public void setFormat(String format) { this.format = format; }


  /**
      overriding MutableSeqSymmetry addSpan(), reomoveSpan(), setSpan(), removeSpans(), clear() methods,
      since span is determined by children
  */
  public void addSpan(SeqSpan span) {
    throw new RuntimeException("Can't add span to Das2FeatureRequestSym directly!");
  }
  public void removeSpan(SeqSpan span) {
    throw new RuntimeException("Can't remove span from Das2FeatureRequestSym directly!");
  }
  /*public void setSpan(int index, SeqSpan span) {
    throw new RuntimeException("Can't set span for Das2FeatureRequestSym directly!");
  }*/
  public void removeSpans() {
    throw new RuntimeException("Can't remove spans from Das2FeatureRequestSym directly!");
  }
  public void clear() {
    throw new RuntimeException("Can't call clear() for Das2FeatureRequestSym directly!");
  }

  // for now just consider overlap_span to be the actual span...
  public SeqSpan getSpan(int index) {
    if (index == 0) { return sum_child_spans; }
    else { return null; }
  }

  public SeqSpan getSpan(BioSeq seq) {
    if (seq == aseq) { return sum_child_spans; }
    else { return null; }
  }

  public boolean getSpan(int index, MutableSeqSpan span) {
    SeqSpan vspan = getSpan(index);
    if (vspan == null) { return false; }
    span.set(vspan.getStart(), vspan.getEnd(), aseq);
    return true;
  }

  public boolean getSpan(BioSeq seq, MutableSeqSpan span) {
    SeqSpan vspan = getSpan(seq);
    if (vspan == null) { return false; }
    span.set(vspan.getStart(), vspan.getEnd(), aseq);
    return true;
  }

  public int getSpanCount() { return ((sum_child_spans == null) ? 0 : 1); }

  public void addChild(SeqSymmetry child) {
    SeqSpan cspan = child.getSpan(aseq);
    if (cspan != null) {
      if (sum_child_spans == null) {
	sum_child_spans = new SimpleMutableSeqSpan(cspan);
      }
      else {
	sum_child_spans.set(Math.min(sum_child_spans.getMin(), cspan.getMin()),
			    Math.max(sum_child_spans.getMax(), cspan.getMax()),
			    aseq);
      }
    }
    super.addChild(child);
  }
}
