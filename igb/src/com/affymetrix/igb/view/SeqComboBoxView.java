package com.affymetrix.igb.view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import com.affymetrix.genometry.*;
import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.event.*;

public class SeqComboBoxView extends JComponent
  implements ItemListener, GroupSelectionListener, SeqSelectionListener {

  static boolean DEBUG_EVENTS = false;
  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
  AnnotatedBioSeq selected_seq = null;
  JLabel seqL;
  JComboBox seqCB;
  static final String SELECT_A_SEQUENC = "Select a sequence";
  static final String NO_SEQUENCES = "No seqs to select";
  static final String NO_SEQ_SELECTED = "No sequence selected";

  public SeqComboBoxView() {
    // need to set maximum x size of seqL and seqCB (or entire SeqComboBoxView), so doesn't grab space needed by other parts of GUI
    seqL = new JLabel("Sequence: ");
    seqCB = new JComboBox();
    seqCB.addItem(NO_SEQUENCES);
    this.setLayout(new BorderLayout());
    this.add("West", seqL);
    this.add("Center", seqCB);
    seqCB.addItemListener(this);
    gmodel.addGroupSelectionListener(this);
    gmodel.addSeqSelectionListener(this);
  }


  public void groupSelectionChanged(GroupSelectionEvent evt) {
    //    AnnotatedSeqGroup group = (AnnotatedSeqGroup)evt.getSelectedGroups().get(0);
    AnnotatedSeqGroup group = gmodel.getSelectedSeqGroup();
    if (this.DEBUG_EVENTS)  {
      System.out.println("SeqComboBoxView received groupSelectionChanged() event");
      if (group == null) {
        System.out.println("  group: " + null);
      } else {
        System.out.println("  group: " + group.getID());
        System.out.println("  seq count: " + group.getSeqCount());
      }
    }
    selected_seq = null;
    seqCB.removeAllItems();
    if (group == null || group.getSeqCount() == 0) {
      seqCB.addItem(NO_SEQUENCES);
    }
    else {
      seqCB.addItem(NO_SEQ_SELECTED);
      Iterator iter = group.getSeqList().iterator();
      while (iter.hasNext()) {
        AnnotatedBioSeq aseq = (AnnotatedBioSeq)iter.next();
        String seqid = aseq.getID();
        seqCB.addItem(seqid);
      }
    }
  }

  public void seqSelectionChanged(SeqSelectionEvent evt) {
    AnnotatedBioSeq aseq = evt.getSelectedSeq();

    if (this.DEBUG_EVENTS)  { 
      System.out.println("SeqComboBoxView received seqSelectionChanged() event: " + (aseq == null ? "null" : aseq.getID())); 
    }

    if (aseq == null)  {
      if (seqCB.getSelectedItem() != NO_SEQ_SELECTED) {
        seqCB.setSelectedItem(NO_SEQ_SELECTED);
        // It is possible that the NO_SEQ_SELECTED item doesn't exist, because
        // the NO_SEQUENCES item is there instead.
        // This takes care of that case, though it isn't important:
        if (seqCB.getSelectedItem() != NO_SEQ_SELECTED) {
          seqCB.setSelectedItem(NO_SEQUENCES);
        }
      }
    }
    else  {
      String seqid = aseq.getID();
      seqCB.setSelectedItem(seqid);
    }
  }


  public void itemStateChanged(ItemEvent evt) {
    Object src = evt.getSource();
    if (DEBUG_EVENTS)  { System.out.println("SeqComboBoxView received itemStateChanged event: " + evt); }
    if ((src == seqCB) && (evt.getStateChange() == ItemEvent.SELECTED)) {
      String seqid = (String)evt.getItem();
      if (seqid == NO_SEQ_SELECTED)   {
	if (gmodel.getSelectedSeq() != null) {
	  gmodel.setSelectedSeq(null);
	}
      }
      else {
	AnnotatedSeqGroup group = gmodel.getSelectedSeqGroup();
	MutableAnnotatedBioSeq aseq = null;
        if (group != null) { aseq = group.getSeq(seqid); }
	if (gmodel.getSelectedSeq() != aseq) {  // to catch bounces from seqSelectionChanged() setting of selected item
	  gmodel.setSelectedSeq(aseq);
	}
      }
    }
  }

}
