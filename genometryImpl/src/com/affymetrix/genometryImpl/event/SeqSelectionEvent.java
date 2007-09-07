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

package com.affymetrix.genometryImpl.event;

import java.util.*;
import com.affymetrix.genometry.AnnotatedBioSeq;

public class SeqSelectionEvent extends EventObject {
  List<AnnotatedBioSeq> selected_seqs;
  AnnotatedBioSeq primary_selection = null;
  static final long serialVersionUID = 1L;

  /**
   *  Constructor.
   *  @param seqs a List of AnnotatedBioSeq's that have been selected.
   *   (If null, will default to {@link Collections#EMPTY_LIST}.)
   */
  public SeqSelectionEvent(Object src, List<AnnotatedBioSeq> seqs) {
    super(src);
    this.selected_seqs = seqs;
    if (selected_seqs == null) { selected_seqs = Collections.<AnnotatedBioSeq>emptyList(); }
    if (selected_seqs.size() > 0) {
      primary_selection = selected_seqs.get(0);
    }
  }

  public SeqSelectionEvent(Object src, AnnotatedBioSeq seq) {
    super(src);
    selected_seqs = new ArrayList<AnnotatedBioSeq>(1);
    if (seq != null) {
      primary_selection = seq;
      selected_seqs.add(seq);
    }
  }

  /**
   *  @return a List of AnnotatedBioSeq's that have been selected.
   *   The List can be empty, but will not be null.
   */
  public List getSelectedSeqs() {
    return selected_seqs;
  }

  /** Gets the first entry in the list {@link #getSelectedSeqs()}.
   *  @return an AnnotatedBioSeq or null.
   */
  public AnnotatedBioSeq getSelectedSeq() {
    return primary_selection;
  }

  public String toString() {
    return "SeqSelectionEvent: seq count: " + selected_seqs.size() +
        " first seq: '" + (primary_selection == null ? "null" : primary_selection.getID()) +
        "' source: " + this.getSource();
  }
}
