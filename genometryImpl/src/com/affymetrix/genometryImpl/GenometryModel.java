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
package com.affymetrix.genometryImpl;

import com.affymetrix.genometry.AnnotatedBioSeq;
import com.affymetrix.genometry.MutableAnnotatedBioSeq;
import com.affymetrix.genometry.SeqSymmetry;
import com.affymetrix.genometryImpl.event.GroupSelectionEvent;
import com.affymetrix.genometryImpl.event.GroupSelectionListener;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.event.SymSelectionEvent;
import com.affymetrix.genometryImpl.event.SymSelectionListener;
import java.util.*;

public class GenometryModel {

  /** Creates a new instance of GenometryModel */
  public GenometryModel() {
  }

  static public boolean DEBUG = false;

  Map<String,AnnotatedSeqGroup> seq_groups = new LinkedHashMap<String,AnnotatedSeqGroup>();
  // LinkedHashMap preserves the order things were added in, which is nice for QuickLoad

  // maps sequences to lists of selected symmetries
  Map<AnnotatedBioSeq,List<SeqSymmetry>> seq2selectedSymsHash = new HashMap<AnnotatedBioSeq,List<SeqSymmetry>>();

  List<SeqSelectionListener> seq_selection_listeners = new ArrayList<SeqSelectionListener>();
  List<GroupSelectionListener> group_selection_listeners = new ArrayList<GroupSelectionListener>();
  List<SymSelectionListener> sym_selection_listeners = new ArrayList<SymSelectionListener>();
  //List model_change_listeners = new ArrayList();

  AnnotatedSeqGroup selected_group = null;
  MutableAnnotatedBioSeq selected_seq = null;

  /** Returns a Map of String names to AnnotatedSeqGroup objects. */
  public Map<String,AnnotatedSeqGroup> getSeqGroups() {
    return seq_groups;
  }

  public List<String> getSeqGroupNames() {
    List<String> list = new ArrayList<String>(getSeqGroups().keySet());
    Collections.sort(list);
    return Collections.unmodifiableList(list);
  }

  public AnnotatedSeqGroup getSeqGroup(String group_syn) {
    if (group_syn == null) { return null; }
    AnnotatedSeqGroup group = seq_groups.get(group_syn);
    if (group == null) {
      // try and find a synonym
      for (AnnotatedSeqGroup curgroup : seq_groups.values()) {
        if (curgroup.isSynonymous(group_syn)) {
          group = curgroup;
          break;
        }
      }
    }
    return group;
  }

  /**
   *  Returns the seq group with the given id, creating a new one if there
   *  isn't an existing one.
   *  @return a non-null AnnotatedSeqGroup
   */
  public AnnotatedSeqGroup addSeqGroup(String group_id) {
    //    System.out.println("%%%%%%%%% GenometryModel.addSeqGroup() called, id = " + group_id);
    // if AnnotatedSeqGroup with same or synonymous id already exists, then return it
    AnnotatedSeqGroup group = getSeqGroup(group_id);
    // otherwise create a new AnnotatedSeqGroup
    if (group == null) {
      //      System.out.println("  adding new seq group: " + group_id);
      group = new AnnotatedSeqGroup(group_id);
      seq_groups.put(group.getID(), group);
      //fireModelChangeEvent(GenometryModelChangeEvent.SEQ_GROUP_ADDED, group);
    }
    else {
      //      System.out.println("  already have seq group: " + group_id + ", actual id = " + group.getID());
    }
    return group;
  }

  public void addSeqGroup(AnnotatedSeqGroup group) {
    seq_groups.put(group.getID(), group);
    //fireModelChangeEvent(GenometryModelChangeEvent.SEQ_GROUP_ADDED, group);
  }

  public void removeSeqGroup(AnnotatedSeqGroup group) {
    seq_groups.remove(group.getID());
    //fireModelChangeEvent(GenometryModelChangeEvent.SEQ_GROUP_REMOVED, group);
  }

  public void removeAllSeqGroups() {
    this.setSelectedSeq(null);
    this.setSelectedSeqGroup(null);
    seq_groups.clear();
    //fireModelChangeEvent(GenometryModelChangeEvent.SEQ_GROUP_REMOVED, group);
  }

  public AnnotatedSeqGroup getSelectedSeqGroup() {
    return selected_group;
  }

  // TODO: modify so that fireGroupSelectionEvent() is only called if
  //     gropu arg is different than previous selected_group
  public void setSelectedSeqGroup(AnnotatedSeqGroup group) {
    if (DEBUG)  {
      System.out.println("GenometryModel.setSelectedSeqGroup() called, ");
      System.out.println("    group = " + (group == null ? null : group.getID()));
    }

    selected_group = group;
    selected_seq = null;
    ArrayList<AnnotatedSeqGroup> glist = new ArrayList<AnnotatedSeqGroup>();
    glist.add(selected_group);
    fireGroupSelectionEvent(this, glist);
  }

  public void addGroupSelectionListener(GroupSelectionListener listener) {
    group_selection_listeners.add(listener);
  }

  public void removeGroupSelectionListener(SeqSelectionListener listener) {
    group_selection_listeners.remove(listener);
  }

  public List getGroupSelectionListeners() {
    return group_selection_listeners;
  }

  void fireGroupSelectionEvent(Object src, List<AnnotatedSeqGroup> glist) {
    GroupSelectionEvent evt = new GroupSelectionEvent(src, glist);
    for (int i=group_selection_listeners.size()-1; i>=0; i--) {
      GroupSelectionListener listener = group_selection_listeners.get(i);
      listener.groupSelectionChanged(evt);
    }
  }

  public MutableAnnotatedBioSeq getSelectedSeq() {
    return selected_seq;
  }

  public void setSelectedSeq(MutableAnnotatedBioSeq seq) {
    setSelectedSeq(seq, this);
  }

  public void setSelectedSeq(MutableAnnotatedBioSeq seq, Object src) {
    if (DEBUG)  {
      System.out.println("GenometryModel.setSelectedSeq() called, ");
      System.out.println("    seq = " + (seq == null ? null : seq.getID()));
    }

    selected_seq = seq;
    ArrayList<AnnotatedBioSeq> slist = new ArrayList<AnnotatedBioSeq>();
    slist.add(selected_seq);
    fireSeqSelectionEvent(src, slist);
  }

  public void addSeqSelectionListener(SeqSelectionListener listener) {
    seq_selection_listeners.add(listener);
  }

  public void removeSeqSelectionListener(SeqSelectionListener listener) {
    seq_selection_listeners.remove(listener);
  }

  public List getSeqSelectionListeners() {
    return seq_selection_listeners;
  }

  /**
   *  SeqSelectionListeners are notified in the order they were added as listeners
   *    (order is important when one listener relies on another's state --
   *       for example in IGB some listeners assume main SeqMapView has been
   *       notified first)
   */
  void fireSeqSelectionEvent(Object src, List<AnnotatedBioSeq> slist) {
    SeqSelectionEvent evt = new SeqSelectionEvent(src, slist);
    Iterator iter = seq_selection_listeners.iterator();
    while (iter.hasNext())  {
      SeqSelectionListener listener = (SeqSelectionListener)iter.next();
      listener.seqSelectionChanged(evt);
    }
  }

  public void addSymSelectionListener(SymSelectionListener listener) {
    sym_selection_listeners.add(listener);
  }

  public void removeSymSelectionListener(SymSelectionListener listener) {
    sym_selection_listeners.remove(listener);
  }

  public List getSymSelectionListeners() {
    return sym_selection_listeners;
  }

  void fireSymSelectionEvent(Object src, List syms) {
    if (DEBUG) {
      System.out.println("Firing event: " + syms.size());
    }
    SymSelectionEvent sevt = new SymSelectionEvent(src, syms);
    for (int i=sym_selection_listeners.size()-1; i>=0; i--) {
      sym_selection_listeners.get(i).symSelectionChanged(sevt);
    }
  }

  /** Get a list of all BioSeq's that have selected SeqSymmetries on them.
   *  This may be different from the currently selected BioSeq's, because
   *  selection of sequence(s) and symmetries are independent.
   */
  public List<AnnotatedBioSeq> getSequencesWithSelections() {
    Set<AnnotatedBioSeq> sequences = new HashSet<AnnotatedBioSeq>();
    for (AnnotatedBioSeq seq : seq2selectedSymsHash.keySet()) {
      List list = (List) seq2selectedSymsHash.get(seq);
      if (! list.isEmpty()) {
        sequences.add(seq);
      }
    }
    return new ArrayList<AnnotatedBioSeq>(sequences);
  }

  /**
   *  Sets the selected symmetries.
   *  The symmetries can be on multiple sequences, and selecting
   *  them will not automatically change the selected sequence.
   *  All the SymSelectionListener's will be notified.
   *  @param syms A List of SeqSymmetry objects to select.
   *  @param src The object responsible for selecting the sequences.
   */
  public void setSelectedSymmetries(List<SeqSymmetry> syms, Object src)  {
    List<MutableAnnotatedBioSeq> seqs_with_selections = setSelectedSymmetries(syms);
    fireSymSelectionEvent(src, syms); // Note this is the complete list of selections
  }

  /**
   *  Sets the selected symmetries AND selects one of the sequences that they lie on.
   *  The symmetries can be on multiple sequences.
   *  If the current bio seq contains one of the symmetries, the seq will not
   *  be changed.  Otherwise the seq will be changed to one of the ones which
   *  has a selected symmetry.
   *  The SeqSelectionListener's will be notified first (only if the seq changes),
   *  and then the SymSelectionListener's will be notified.
   *  @param syms A List of SeqSymmetry objects to select.
   *  @param src The object responsible for selecting the sequences.
   */
  public void setSelectedSymmetriesAndSeq(List<SeqSymmetry> syms, Object src) {
    List seqs_with_selections = setSelectedSymmetries(syms);
    if (! seqs_with_selections.contains(getSelectedSeq())) {
      if (getSelectedSymmetries(getSelectedSeq()).isEmpty()) {
        MutableAnnotatedBioSeq seq = null;
        if (! seqs_with_selections.isEmpty()) {
          seq = (MutableAnnotatedBioSeq) seqs_with_selections.get(0);
        }
        setSelectedSeq(seq, src);
      }
    }
    fireSymSelectionEvent(src, syms); // Note this is the complete list of selections
  }

  /**
   *  Sets the selected symmetries.
   *  The symmetries can be on multiple sequences, and selecting
   *  them will not automatically change the selected sequence.
   *  This non-public version does NOT send a SymSelectionEvent.
   *  @param syms A List of SeqSymmetry objects to select.
   *  @param src The object responsible for selecting the sequences.
   *  @return The List of sequences with selections on them after this operation.
   */
  List<MutableAnnotatedBioSeq> setSelectedSymmetries(List<SeqSymmetry> syms) {

    if (DEBUG) {
      System.out.println("SetSelectedSymmetries called, number of syms: " + syms.size());
    }

    HashMap<MutableAnnotatedBioSeq,List<SeqSymmetry>> seq2SymsHash = new HashMap<MutableAnnotatedBioSeq,List<SeqSymmetry>>();

    // for each ID found in the ID2sym hash, add it to the owning sequences
    //  list of selected symmetries
    Iterator<SeqSymmetry> syms_iter = syms.iterator();
    HashSet<MutableAnnotatedBioSeq> all_seqs = new HashSet<MutableAnnotatedBioSeq>(); // remember all seqs found
    while (syms_iter.hasNext()) {
      SeqSymmetry sym = syms_iter.next();
      if (sym != null) {
        MutableAnnotatedBioSeq seq = getSelectedSeqGroup().getSeq(sym);
        if (seq != null) {
          // prepare the list to add the sym to based on the seq ID
          List<SeqSymmetry> symlist = seq2SymsHash.get(seq);
          if (symlist == null) {
            symlist = new ArrayList<SeqSymmetry>();
            seq2SymsHash.put(seq, symlist);
          }
          // add the sym to the list for the correct seq ID
          symlist.add(sym);
          all_seqs.add(seq);
        }
      }
    }

    // clear all the existing selections first
    clearSelectedSymmetries(); // do not send an event yet

    // now perform the selections for each sequence that was matched
    for(  MutableAnnotatedBioSeq seq : seq2SymsHash.keySet()) {
      List<SeqSymmetry> symslist = seq2SymsHash.get(seq);
      if (DEBUG) {
        System.out.println("Syms " + symslist.size() + " on seq " + seq.getID());
      }
      setSelectedSymmetries(symslist, seq); // do not send an event yet
    }

    return new ArrayList<MutableAnnotatedBioSeq>(all_seqs);
  }

  /**
   *  Selects a List of SeqSymmetry objects for a particular MutableAnnotatedBioSeq object.
   *  All SymmetrySelectionListeners will be notified regardless of whether this
   *  is the currently-selected sequence.
   *  @param syms A List of SeqSymmetry objects to select.
   *  @param src The object responsible for selecting the sequences.
   *  @param seq The MutableAnnotatedBioSeq to select the symmetries for.
   */
  void setSelectedSymmetries(List<SeqSymmetry> syms, Object src, MutableAnnotatedBioSeq seq ) {
    setSelectedSymmetries(syms, seq);
    fireSymSelectionEvent(src, syms);
  }

  // Selects a List of SeqSymmetry objects for a particular BioSeq.
  // Does not send a selection event.
  void setSelectedSymmetries(List<SeqSymmetry> syms, MutableAnnotatedBioSeq seq) {
    if (seq == null) { return; }
    // Should it complain if any of the syms are not on the specified seq?
    // (This is not an issue since this is not called from outside of this class.)

    if (DEBUG)  {
      System.out.println("GenometryModel.setSelectedSymmetries() called, ");
      System.out.println("    syms = " + syms);
    }
    // set the selected syms for the sequence
    seq2selectedSymsHash.put(seq, syms);
  }


  /**
   *  Get the list of selected symmetries on the currently selected sequence.
   *  @return A List of the selected SeqSymmetry objects, can be empty, but not null
   */
  public List getSelectedSymmetriesOnCurrentSeq() {
    return getSelectedSymmetries(selected_seq);
  }

  /**
   *  Get the list of selected symmetries on the currently selected sequence.
   *  @return A List of the selected SeqSymmetry objects, can be empty, but not null
   */
  public List<SeqSymmetry> getSelectedSymmetriesOnAllSeqs() {
    List<SeqSymmetry> result = new ArrayList<SeqSymmetry>();
    for (List<SeqSymmetry> list : seq2selectedSymsHash.values()) {
      result.addAll(list);
    }

    return result;
  }

  /**
   *  Get the list of selected symmetries on the specified sequence.
   *  @return A List of the selected SeqSymmetry objects, can be empty, but not null
   */
  public List<SeqSymmetry> getSelectedSymmetries(AnnotatedBioSeq seq) {
    AnnotatedBioSeq mseq = (AnnotatedBioSeq) seq; //TODO: change the method signature?
    List<SeqSymmetry> selections = seq2selectedSymsHash.get(seq);
    if (selections == null) {
      selections = new ArrayList<SeqSymmetry>();
      seq2selectedSymsHash.put(mseq, selections);
    }
    return selections;
  }

  /**
   *  Clears the symmetry selection for every sequence. Notifies all the selection listeners.
   *  @param src The object to use as the event source for the SymSelectionEvent.
   */
  public void clearSelectedSymmetries(Object src) {
    clearSelectedSymmetries();
    fireSymSelectionEvent(src, Collections.EMPTY_LIST);
  }

  /**
   *  Clears the symmetry selection for every sequence.
   *  Does not notifies the selection listeners.
   */
  void clearSelectedSymmetries() {
    for (List<SeqSymmetry> list : seq2selectedSymsHash.values()) {
      list.clear();
    }
  }

//  public void addModelChangeListener(GenometryModelChangeListener listener) {
//    model_change_listeners.add(listener);
//  }
//
//  public void removeModelChangeListener(GenometryModelChangeListener listener) {
//    model_change_listeners.remove(listener);
//  }
//
//  public List getModelChangeListeners() {
//    return model_change_listeners;
//  }
//
//  void fireModelChangeEvent(GenometryModelChangeEvent.EventType type, AnnotatedSeqGroup group) {
//    GenometryModelChangeEvent event = new GenometryModelChangeEvent(this, type, group);
//    for (int i=model_change_listeners.size()-1; i>=0; i--) {
//      GenometryModelChangeListener listener = (GenometryModelChangeListener) model_change_listeners.get(i);
//      listener.genometryModelChanged(event);
//    }
//  }
}
