/**
*   Copyright (c) 2001-2004 Affymetrix, Inc.
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

package com.affymetrix.igb.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import com.affymetrix.igb.IGB;
import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.igb.das2.*;
import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.event.*;
import com.affymetrix.swing.threads.SwingWorker;

import com.affymetrix.igb.util.GenometryViewer;  // temporary visualization till hooked into IGB

public class Das2LoadView extends JComponent
  implements ItemListener, ActionListener,
             // ComponentListener,  turned off pending change mechanism for now
	     SeqSelectionListener, GroupSelectionListener  {

  static String[] type_columns = { "ID", "ontology", "derivation", "status" };
  static Das2TypesTableModel empty_table_model = new Das2TypesTableModel(type_columns, new ArrayList());

  boolean THREAD_FEATURE_REQUESTS = false;
  boolean USE_SIMPLE_VIEW = false;
  SeqMapView gviewer = null;
  GenometryViewer simple_viewer = null;

  JComboBox das_serverCB;
  JComboBox das_sourceCB;
  JComboBox das_versionCB;
  //  JComboBox das_regionCB;
  //  JTextField minTF;
  //  JTextField maxTF;
  JButton load_featuresB;
  JPanel types_panel;
  JTable types_table;
  JScrollPane table_scroller;
  Map das_servers;
  Map version2typestates = new HashMap();
  LinkedHashMap checkbox2type;

  Das2LoadView myself = null;
  Das2ServerInfo current_server;
  Das2Source current_source;
  Das2VersionedSource current_version;
  Das2Region current_region;

  SingletonGenometryModel gmodel = IGB.getGenometryModel();
  AnnotatedSeqGroup current_group = null;
  AnnotatedBioSeq current_seq = null;

  String server_filler = "Choose a DAS2 server";
  String source_filler = "Choose a DAS2 source";
  String version_filler = "Choose a DAS2 version";
  String region_filler = "Choose a DAS2 seq";

  // turned off pending change mechanism for now
  //  boolean pending_group_change = false;
  //  boolean pending_seq_change = false;

  public Das2LoadView() {
    this(false);
  }

  /**
   *  choices for DAS2 annot loading range:
   *    whole genome
   *    whole chromosome
   *    specified range on current chromosome
   *    gviewer's view bounds on current chromosome
   */
  public Das2LoadView(boolean simple_view) {
    myself = this;
    USE_SIMPLE_VIEW = simple_view;
    if (!USE_SIMPLE_VIEW) {
      gviewer = IGB.getSingletonIGB().getMapView();
    }

    das_serverCB = new JComboBox();
    das_sourceCB = new JComboBox();
    das_versionCB = new JComboBox();
    //    das_regionCB = new JComboBox();
    //    minTF = new JTextField(10);
    //    maxTF = new JTextField(10);
    load_featuresB = new JButton("Load Features");
    types_panel = new JPanel();
    types_panel.setLayout(new BoxLayout(types_panel, BoxLayout.Y_AXIS));
    JScrollPane scrollpane = new JScrollPane(types_panel);

    types_table = new JTable();
    types_table.setModel(empty_table_model);
    table_scroller = new JScrollPane(types_table);

    checkbox2type = new LinkedHashMap();
    das_serverCB.addItem(server_filler);

    das_servers = Das2Discovery.getDas2Servers();
    Iterator iter = das_servers.keySet().iterator();
    while (iter.hasNext()) {
      String server_name = (String)iter.next();
      das_serverCB.addItem(server_name);
    }

    this.setLayout(new BorderLayout());
    JPanel panA = new JPanel();
    panA.setLayout(new GridLayout(3, 2));

    panA.add(new JLabel("DAS2 Server: "));
    panA.add(das_serverCB);
    panA.add(new JLabel("DAS2 Source: " ));
    panA.add(das_sourceCB);
    panA.add(new JLabel("DAS2 Version: "));
    panA.add(das_versionCB);
    //    panA.add(new JLabel("DAS2 Region: "));
    //    panA.add(das_regionCB);
    //    panA.add(new JLabel("min base: "));
    //    panA.add(minTF);
    //    panA.add(new JLabel("max base: "));
    //    panA.add(maxTF);

    JPanel middle_panel = new JPanel(new GridLayout(2, 1));
    middle_panel.add(scrollpane);
    middle_panel.add(table_scroller);

    this.add("North", panA);
    this.add("Center", middle_panel);
    this.add("South", load_featuresB);

    das_serverCB.addItemListener(this);
    das_sourceCB.addItemListener(this);
    das_versionCB.addItemListener(this);
    //    das_regionCB.addItemListener(this);
    load_featuresB.addActionListener(this);

    //    this.addComponentListener(this);  turned off pending change mechanism for now
    gmodel.addSeqSelectionListener(this);
    gmodel.addGroupSelectionListener(this);
  }

  public void itemStateChanged(ItemEvent evt) {
    //    System.out.println("Das2LoadView received ItemEvent: " + evt);
    Object src = evt.getSource();

    // selection of DAS server
    if ((src == das_serverCB) && (evt.getStateChange() == ItemEvent.SELECTED)) {
      System.out.println("Das2LoadView received SELECTED ItemEvent on server combobox");
      String server_name = (String)evt.getItem();
      if (server_name == server_filler) {
	// clear source and version choice boxes, and types panel
      }
      else {
	System.out.println("DAS server selected: " + server_name);
	current_server = (Das2ServerInfo)das_servers.get(server_name);
	System.out.println(current_server);
	setSources();
      }
    }

    // selection of DAS source
    else if ((src == das_sourceCB) && (evt.getStateChange() == ItemEvent.SELECTED)) {
      System.out.println("Das2LoadView received SELECTED ItemEvent on source combobox");
      String source_name = (String)evt.getItem();
      if (source_name == source_filler) {
	// clear version choice box and types panel
      }
      else  {
	System.out.println("source name: " + source_name);
	Map sources = current_server.getSources();
	current_source = (Das2Source)sources.get(source_name);
	System.out.println(current_source);
	//	System.out.println("  genome id: " + current_source.getGenome().getID());
	//	setRegionsAndTypes();
	setVersions();
      }
    }

    else if ((src == das_versionCB) && (evt.getStateChange() == ItemEvent.SELECTED)) {
      System.out.println("Das2LoadView received SELECTED ItemEvent on version combobox");
      String version_name = (String)evt.getItem();
      if (version_name == version_filler) {
	// clear types panel
      }
      if (version_name != version_filler) {
	System.out.println("version name: " + version_name);
	Map versions = current_source.getVersions();
	current_version = (Das2VersionedSource)versions.get(version_name);
	System.out.println(current_version);
	System.out.println("  version id: " + current_version.getGenome().getID());
	setRegionsAndTypes();
      }
    }

    // selection of DAS region point
    /*
    else if ((src == das_regionCB) && (evt.getStateChange() == ItemEvent.SELECTED)) {
      System.out.println("Das2LoadView received SELECTED ItemEvent on region combobox");
      String region_name = (String)evt.getItem();
      if (region_name != region_filler) {
	System.out.println("region seq: " + region_name);
	Map regions = current_version.getRegions();
	current_region = (Das2Region)regions.get(region_name);
      }
    }
    */
  }

  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if (src == load_featuresB) {
      System.out.println("Das2LoadView received ActionEvent on load features button");
      loadFeaturesInView();
    }
  }

  public void setVersions() {
    das_versionCB.removeItemListener(this);
    das_versionCB.removeAllItems();
    //    das_regionCB.removeAllItems();
    checkbox2type.clear();
    types_panel.removeAll();
    types_panel.validate();
    types_panel.repaint();

    types_table.setModel(empty_table_model);
    types_table.validate();
    types_table.repaint();

    das_serverCB.setEnabled(false);
    das_sourceCB.setEnabled(false);
    das_versionCB.setEnabled(false);
    //    das_regionCB.setEnabled(false);

    // don't need to put getVersions() call on separate thread, since a source's versions
    //    will have already been initialized in a previous server.getSources() call
    Map versions = current_source.getVersions();
    das_versionCB.addItem(version_filler);
    Iterator iter = versions.values().iterator();
    while (iter.hasNext()) {
      Das2VersionedSource version = (Das2VersionedSource)iter.next();
      das_versionCB.addItem(version.getID());
    }

    das_versionCB.addItemListener(this);
    das_serverCB.setEnabled(true);
    das_sourceCB.setEnabled(true);
    das_versionCB.setEnabled(true);
    //	  das_regionCB.setEnabled(true);

  }

  public void setSources() {
    das_sourceCB.removeItemListener(this);
    das_versionCB.removeItemListener(this);
    das_sourceCB.removeAllItems();
    das_versionCB.removeAllItems();
    //    das_regionCB.removeAllItems();
    checkbox2type.clear();
    types_panel.removeAll();
    types_panel.validate();
    types_panel.repaint();

    types_table.setModel(empty_table_model);
    types_table.validate();
    types_table.repaint();

    //    types_panel.setEnabled(false);
    das_serverCB.setEnabled(false);
    das_sourceCB.setEnabled(false);
    das_versionCB.setEnabled(false);
    //    das_regionCB.setEnabled(false);

    final SwingWorker worker = new SwingWorker() {
	Map sources = null;
	public Object construct() {
	  sources = current_server.getSources();
	  if (current_group == null) {
	    current_version = null;
	  }
	  else {
	    current_version = current_server.getVersionedSource(current_group);
	  }
	  if (current_version == null)  {
	    current_source = null;
	  }
	  else {
	    current_source = current_version.getSource();
	  }
	  return null;  // or could have it return types, shouldn't matter...
	}
	public void finished() {
	  das_sourceCB.addItem(source_filler);
	  Iterator iter = sources.values().iterator();
	  while (iter.hasNext()) {
	    Das2Source source = (Das2Source)iter.next();
	    //      das_sourceCB.addItem(source.getName() + "(" + source.getID() + ")");
	    das_sourceCB.addItem(source.getID());
	  }
	  if (current_source != null) {
	    das_sourceCB.setSelectedItem(current_source.getID());
	    das_versionCB.addItem(version_filler);
	    Iterator versions = current_source.getVersions().values().iterator();
	    while (versions.hasNext()) {
	      Das2VersionedSource version = (Das2VersionedSource)versions.next();
	      das_versionCB.addItem(version.getID());
	    }
	  }
	  if (current_version != null) {
	    das_versionCB.setSelectedItem(current_version.getID());
	  }
	  //	  das_sourceCB.addItemListener(listener);
	  das_sourceCB.addItemListener(myself);
	  das_versionCB.addItemListener(myself);
	  das_serverCB.setEnabled(true);
	  das_sourceCB.setEnabled(true);
	  das_versionCB.setEnabled(true);
	  //	  das_regionCB.setEnabled(true);
	  
	  if (current_version != null) {
	    setRegionsAndTypes();
	  }
	}
      };
    worker.start();
  }


  public void setRegionsAndTypes() {
    types_panel.setEnabled(false);
    das_serverCB.setEnabled(false);
    das_sourceCB.setEnabled(false);
    das_versionCB.setEnabled(false);
    //    das_regionCB.setEnabled(false);
    load_featuresB.setEnabled(false);

    //    das_regionCB.removeAllItems();
    checkbox2type.clear();
    types_panel.removeAll();
    types_panel.validate();
    types_panel.repaint();

    types_table.setModel(empty_table_model);
    types_table.validate();
    types_table.repaint();
    
    // alternative would be to use a QueuedExecutor (from ~.concurrent package)
    //    and two runnables, one for entries and one for types...
    final SwingWorker worker = new SwingWorker() {
	Map seqs = null;
	Map types = null; 
	public Object construct() {
	  seqs = current_version.getRegions();
	  types = current_version.getTypes();
	  return null;
	}
	public void finished() {
	  java.util.List type_states = new ArrayList();
	  Iterator iter = types.values().iterator();
	  while (iter.hasNext()) {
	    Das2Type dtype = (Das2Type)iter.next();
	    Das2TypeState tstate = new Das2TypeState(dtype, Das2TypeState.OFF);
	    type_states.add(tstate);
	    String typeid = dtype.getID();
	    //		  String typesource = dtype.getDerivation();
	    JCheckBox box = new JCheckBox(typeid);
	    types_panel.add(box);
	    checkbox2type.put(box, dtype);
	  }
	  types_panel.validate();
	  types_panel.repaint();
	  version2typestates.put(current_version, type_states);
	  Das2TypesTableModel new_table_model = new Das2TypesTableModel(type_columns, type_states);
	  types_table.setModel(new_table_model);
	  types_table.validate();
	  types_table.repaint();
	  //		this.setEnabled(true);
	  //		setEnabled(true);
	  types_panel.setEnabled(true);
	  das_serverCB.setEnabled(true);
	  das_sourceCB.setEnabled(true);
	  das_versionCB.setEnabled(true);
	  load_featuresB.setEnabled(true);
	}
      };
    worker.start();
  }

  public void loadFeaturesInView() {

    MutableAnnotatedBioSeq selected_seq = gmodel.getSelectedSeq();
    final SeqSpan overlap = gviewer.getVisibleSpan();
    final MutableAnnotatedBioSeq visible_seq = (MutableAnnotatedBioSeq)overlap.getBioSeq();
    //    MutableAnnotatedBioSeq current_seq = current_region.getAnnotatedSeq();
    if (current_version != null) {
      if (current_seq != null) {
	current_region = current_version.getRegion(current_seq);
      }
      else {
	current_region = current_version.getRegion(visible_seq);
      }
    }

    if (visible_seq != selected_seq) { System.out.println("WARNING, VISIBLE SPAN DOES NOT MATCH GMODEL'S SELECTED SEQ!!!"); }
    //    if (visible_seq != current_seq) { System.out.println("WARNING, VISIBLE SPAN DOES NOT MATCH CURRENT REGION!!!"); }

    //    System.out.println("region = " + current_region.getID() + ", seq = " + visible_seq.getID() +
    System.out.println("seq = " + visible_seq.getID() +
		       ", min = " + overlap.getMin() + ", max = " + overlap.getMax());

    // instead of iterating through checkboxes, go through Das2TypeStates
    //    if off, ignore
    //    if range-in-view && !fully_loaded, do range in view annotation request
    //    if per-seq && !fully_loaded, do full seq annotation request

    // could probably add finer resolution of threading here,
    //  so every request (one per type) launches on its own thread
    //  But for now putting them all on same (non-event) thread controlled by SwingWorker
    if (THREAD_FEATURE_REQUESTS) {
      final SwingWorker worker = new SwingWorker() {
	  int selected_type_count = 0;
	  public Object construct() {
	    Iterator iter = checkbox2type.entrySet().iterator();
	    while (iter.hasNext()) {
	      Map.Entry keyval = (Map.Entry)iter.next();
	      JCheckBox box = (JCheckBox)keyval.getKey();
	      Das2Type dtype = (Das2Type)keyval.getValue();
	      boolean selected = box.isSelected();
	      if (selected) {
		selected_type_count++;
		String typeid = dtype.getID();
		System.out.println("selected type: " + typeid);
		Das2FeatureRequestSym request_sym =
		  new Das2FeatureRequestSym(dtype, current_region, overlap, null);
		Iterator formats = request_sym.getDas2Type().getFormats().keySet().iterator();
		while (formats.hasNext()) {
		  String format = (String)formats.next();
		  String mime_type = (String)request_sym.getDas2Type().getFormats().get(format);
		  System.out.println("   format = " + format + ", mime_type = " + mime_type);
		}
		// load optmizer is responsible for adding any request syms as annotations to the annotated seqs?
		java.util.List optimized_requests = Das2ClientOptimizer.loadFeatures(request_sym);
		//		current_region.getFeatures(request_sym);
		// probably want to synchronize on aseq, since don't want to add annotations to aseq
		// on one thread when might be rendering based on aseq in event thread...
		//
		// or maybe should make addAnnotation() a synchronized method
		//		synchronized (visible_seq) {
		//		  visible_seq.addAnnotation(request_sym);
		//		}
	      }
	    }
	    return null;
	  }

	  public void finished() {
	    if (selected_type_count > 0) {
	      if (USE_SIMPLE_VIEW) {
		if (simple_viewer == null) { simple_viewer = GenometryViewer.displaySeq(visible_seq, false); }
		simple_viewer.setAnnotatedSeq(visible_seq);
	      }
	      else if (gviewer != null) {
		gviewer.setAnnotatedSeq(visible_seq, true, true);
	      }
	    }
	  }
	};
      worker.start();
    }

    else {  // non-threaded feature request
      int selected_type_count = 0;
      Iterator iter = checkbox2type.entrySet().iterator();
      while (iter.hasNext()) {
	Map.Entry keyval = (Map.Entry)iter.next();
	JCheckBox box = (JCheckBox)keyval.getKey();
	Das2Type dtype = (Das2Type)keyval.getValue();
	boolean selected = box.isSelected();
	if (selected) {
	  selected_type_count++;
	  String typeid = dtype.getID();
	  System.out.println("selected type: " + typeid);
	  Das2FeatureRequestSym request_sym =
	    new Das2FeatureRequestSym(dtype, current_region, overlap, null);
	  Iterator formats = request_sym.getDas2Type().getFormats().keySet().iterator();
	  while (formats.hasNext()) {
	    String format = (String)formats.next();
	    String mime_type = (String)request_sym.getDas2Type().getFormats().get(format);
	    System.out.println("   format = " + format + ", mime_type = " + mime_type);
	  }
	  java.util.List optimized_requests = Das2ClientOptimizer.loadFeatures(request_sym);
	  //	  current_region.getFeatures(request_sym);
	  // probably want to synchronize on aseq, since don't want to add annotations to aseq
	  // on one thread when might be rendering based on aseq in event thread...
	  //
	  // or maybe should make addAnnotation() a synchronized method
	  //	  synchronized (visible_seq) {
	  //	    visible_seq.addAnnotation(request_sym);
	  //	  }
	}
      }
	
      if (selected_type_count > 0) {
	if (USE_SIMPLE_VIEW) {
	  if (simple_viewer == null) { simple_viewer = GenometryViewer.displaySeq(visible_seq, false); }
	  simple_viewer.setAnnotatedSeq(visible_seq);
	}
	else if (gviewer != null) {
	  gviewer.setAnnotatedSeq(visible_seq, true, true);
	}
      }
    }  // end non-threaded feature request

  }

  /**
   *  When selected sequence changed, want to go through all previously visited
   *     DAS/2 versioned sources that share the seq's AnnotatedSeqGroup,
   *     For each (similar_versioned_source)
   *         for each type
   *            if (Das2TypeState set to AUTO_PER_SEQUENCE loading) && ( !state.fullyLoaded(seq) )
   *                 Do full feature load for seq
   *  For now assume that if a type's load state is not AUTO_PER_SEQUENCE, then no auto-loading, only
   *    manual loading, which is handled in another method...
   */
  public void seqSelectionChanged(SeqSelectionEvent evt) {
    if (IGB.DEBUG_EVENTS) {
      System.out.println(
          "Das2LoadView received SeqSelectionEvent, selected seq: " + evt.getSelectedSeq());
    }
    AnnotatedBioSeq newseq = evt.getSelectedSeq();
    if (current_seq != newseq) {
      current_seq = newseq;
      if (current_version != null) {
	current_region = current_version.getRegion(current_seq);
        //      System.out.println("current region: " + region.getID());
	//        das_regionCB.removeItemListener(this);
	//        das_regionCB.setSelectedItem(current_region.getID());
	//        das_regionCB.addItemListener(this);
      }
    }
  }

  /**
   *  When selected group changed, want to go through all previously visited
   *     DAS/2 servers (starting with the current one), and try and find
   *     a versioned source that shares the selected AnnotatedSeqGroup
   *  If found, take first found and set versioned source, source, and server accordingly
   *  If not found, blank out versioned source and source, and switch server to "Choose a server"
   *
   *  For now, just looking at current server
   */
  public void groupSelectionChanged(GroupSelectionEvent evt) {
    //    if (IGB.DEBUG_EVENTS)  {
      System.out.println("Das2LoadView received GroupSelectionEvent: " + evt);
      //    }
    java.util.List groups = evt.getSelectedGroups();    if (groups != null && groups.size() > 0) {
      AnnotatedSeqGroup newgroup = (AnnotatedSeqGroup)groups.get(0);
      if (current_group != newgroup) {
        current_group = newgroup;
        if (current_server != null)  {
          current_version = current_server.getVersionedSource(current_group);
	  if (current_version == null) {
	    // reset
	    das_serverCB.setSelectedItem(server_filler);
	    //	    das_sourceCB.setSelectedItem(source_filler);
	    //	    das_versionCB.setSelectedItem(version_filler);
	  }
	  else {
	    current_source = current_version.getSource();
	    System.out.println("   new das source: " + current_source.getID() +
			       ",  new das version: " + current_version.getID());
	    //	  das_sourceCB.removeItemListener(this);
	    das_sourceCB.setSelectedItem(current_source.getID());
	    das_versionCB.setSelectedItem(current_version.getID());
	    //	  das_sourceCB.addItemListener(this);
	  }
        }
      }
    }
  }

/** ComponentListener implementation, to allow putting off changes
 *     triggered by changing seq or seq group unless and until Das2LoadView is actually visible
 *
 *  turned this off, because really want Das2LoadView to respond regardless of whether it's
 *     visible or not.  May want to revisit at some point, because GUI itself doesn't need
 *     to respond if not visible, rather the auto-loading part needs to respond

  public void componentResized(ComponentEvent e) { }
  public void componentMoved(ComponentEvent e) { }
  public void componentHidden(ComponentEvent e) { }

  public void componentShown(ComponentEvent e) {
    System.out.println("Das2LoadView was just made visible");
    if (pending_group_change) {
      handleGroupChange();
      pending_group_change = false;
    }
    if (pending_seq_change)  {
      handleSeqChange();
      pending_seq_change = false;
    }
  }
*/

  public static void main(String[] args) {
    Das2LoadView testview = new Das2LoadView(true);
    JFrame frm = new JFrame();
    Container cpane = frm.getContentPane();
    cpane.setLayout(new BorderLayout());
    cpane.add("Center", testview);
    frm.setSize(new Dimension(400, 400));
    frm.addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent evt) { System.exit(0);}
    });
    frm.show();
  }

  /**  feature loading without SwingWorker
    Iterator iter = checkbox2type.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry keyval = (Map.Entry)iter.next();
      JCheckBox box = (JCheckBox)keyval.getKey();
      Das2Type dtype = (Das2Type)keyval.getValue();
      boolean selected = box.isSelected();
      if (selected) {
	String typeid = dtype.getID();
	System.out.println("selected type: " + typeid);
	Das2FeatureRequestSym request_sym =
	  new Das2FeatureRequestSym(dtype, current_region, overlap, null);
	Iterator formats = request_sym.getDas2Type().getFormats().keySet().iterator();
	while (formats.hasNext()) {
	  String format = (String)formats.next();
	  String mime_type = (String)request_sym.getDas2Type().getFormats().get(format);
	  System.out.println("   format = " + format + ", mime_type = " + mime_type);
	}
	System.out.println("hello");

	current_region.getFeatures(request_sym);
	System.out.println("after");
	synchronized (visible_seq) {
	  visible_seq.addAnnotation(request_sym);
	}
	System.out.println("after2");
      }
    }
    gviewer.setAnnotatedSeq(visible_seq, true, true);
  */
}

/**
 *  relates a Das2Type to it's status in IGB
 *    (whether it's load strategy is set to full sequence or visible range,
 *      possibly other details)
 */
class Das2TypeState {
  static String[] LOAD_STRINGS = new String[3];
  static int OFF = 0;
  static int WHOLE_SEQUENCE = 1;
  static int VISIBLE_RANGE = 2;
  static {
    LOAD_STRINGS[OFF] = "Off";
    LOAD_STRINGS[WHOLE_SEQUENCE] = "Whole Sequence";
    LOAD_STRINGS[VISIBLE_RANGE] = "Visible Range";
  }

  int load_strategy;
  Das2Type type;

  public Das2TypeState(Das2Type type, int load_strategy) {
    this.load_strategy = load_strategy;
    this.type = type;
  }
  public void setLoadStrategy(int strategy) { load_strategy = strategy; }
  public int getLoadStrategy() { return load_strategy; }
  public String getLoadString() { return LOAD_STRINGS[load_strategy]; }
  public Das2Type getDas2Type() { return type; }
}


class Das2TypesTableModel extends AbstractTableModel   {
  static int model_count = 0;

  int model_num;
  String[] column_names;
  java.util.List type_states;

  public Das2TypesTableModel(String[] columns, java.util.List states) {
    model_num = model_count;
    model_count++;

    column_names = columns;
    type_states = states;
    int col_count = column_names.length;
    int row_count = states.size();;
  }

    public int getColumnCount() {
      return column_names.length;
    }

    public int getRowCount() {
      return type_states.size();
    }

    public String getColumnName(int col) {
      return column_names[col];
    }

    public Object getValueAt(int row, int col) {
      Das2TypeState state = (Das2TypeState)type_states.get(row);
      Das2Type type = state.getDas2Type();
      switch(col) {
      case 0:
	return (type.getID() + " " + model_num);
      case 1:
	return (type.getOntology() + " " + model_num);
      case 2:
	return (type.getDerivation() + " " + model_num);
      case 3:
	return state.getLoadString();
      default:
	return " ";
      }
    }

    /*
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
    */

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    /*
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 2) {
            return false;
        } else {
            return true;
        }
    }
    */

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    /*
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
    */
}
