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

package com.affymetrix.igb.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.affymetrix.igb.Application;
import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.igb.das2.*;
import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.event.*;
import com.affymetrix.igb.tiers.AnnotStyle;
import com.affymetrix.igb.util.ErrorHandler;
import com.affymetrix.swing.threads.SwingWorker;
import com.affymetrix.igb.util.UnibrowPrefsUtil;
import com.affymetrix.igb.util.GenometryViewer;
import javax.swing.treetable.*;

import javax.swing.event.*;  // temporary visualization till hooked into IGB

public class Das2LoadView extends JComponent
  implements ActionListener, TableModelListener,
	     SeqSelectionListener, GroupSelectionListener,
             TreeSelectionListener {

  static boolean INCLUDE_NAME_SEARCH = false;
  static boolean USE_DAS2_OPTIMIZER = true;
  static boolean DEBUG_EVENTS = false;
  static boolean DEFAULT_THREAD_FEATURE_REQUESTS = true;
  static boolean USE_SIMPLE_VIEW = false;
  static boolean USE_TYPES_TREE_TABLE = false;

  static OldDas2TypesTableModel empty_table_model = new OldDas2TypesTableModel(new ArrayList());

  static SeqMapView gviewer = null;
  static GenometryViewer simple_viewer = null;

  JTabbedPane tpane = new JTabbedPane();
  JTextField searchTF = new JTextField(40);
  JComboBox typestateCB;
  JButton load_featuresB;
  JTable types_table;
  JTable types_tree_table;
  JScrollPane table_scroller;
  JScrollPane tree_table_scroller;
  Map das_servers;
  Map version2typestates = new LinkedHashMap();

  Das2LoadView myself = null;
  Das2ServerInfo current_server;
  Das2Source current_source;
  Das2VersionedSource current_version;
  Das2Region current_region;

  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
  AnnotatedSeqGroup current_group = null;
  AnnotatedBioSeq current_seq = null;

  JTree tree;

  public Das2LoadView() {
    this(false);
  }

  /*
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
      gviewer = Application.getSingleton().getMapView();
    }

    DefaultMutableTreeNode top = new DefaultMutableTreeNode("DAS/2 Genome Servers");
    das_servers = Das2Discovery.getDas2Servers();
    Iterator iter = das_servers.values().iterator();
    while (iter.hasNext()) {
      Das2ServerInfo server = (Das2ServerInfo)iter.next();
      String server_name = server.getName();
      OldDas2ServerTreeNode snode = new OldDas2ServerTreeNode(server);
      top.add(snode);
    }
    tree = new JTree(top);

    load_featuresB = new JButton("Load Features");
    load_featuresB.setToolTipText("Load selected feature types for this region.");
    load_featuresB.setEnabled(false);
    typestateCB = new JComboBox();
    String[] load_states = OldDas2TypeState.LOAD_STRINGS;
    for (int i=1; i<load_states.length; i++) {
      typestateCB.addItem(load_states[i]);
    }
    JComponent load_features_box = Box.createHorizontalBox();
    load_features_box.add(Box.createHorizontalGlue());
    load_features_box.add(load_featuresB);
    load_features_box.add(Box.createHorizontalGlue());

    types_table = new JTable();
    types_table.setModel(empty_table_model);
    table_scroller = new JScrollPane(types_table);


    ArrayList test_states = new ArrayList();
    JPanel types_tree_panel = null;
    if (USE_TYPES_TREE_TABLE) {
      Das2TypesTreeTableModel types_tree_model = new Das2TypesTreeTableModel(test_states);
      types_tree_table = new JTreeTable(types_tree_model);
      tree_table_scroller = new JScrollPane(types_tree_table);
      types_tree_panel = new JPanel(new BorderLayout());
      types_tree_panel.setBorder(new TitledBorder("Available Annotation Types"));
      types_tree_panel.add("Center", tree_table_scroller);
    }

    this.setLayout(new BorderLayout());

    JPanel types_panel = new JPanel(new BorderLayout());
    types_panel.setBorder(new TitledBorder("Available Annotation Types"));

    JPanel namesearchP = new JPanel();

    namesearchP.add(new JLabel("name search: "));
    namesearchP.add(searchTF);

    //    types_panel.add("North", namesearchP);
    types_panel.add("Center", table_scroller);
    types_panel.add("South", load_features_box);

    final JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitpane.setOneTouchExpandable(true);
    //    splitpane.setDividerSize(8);
    //    splitpane.setDividerLocation(frm.getHeight() - (table_height + fudge));
    splitpane.setLeftComponent(new JScrollPane(tree));
    if (INCLUDE_NAME_SEARCH  || USE_TYPES_TREE_TABLE) {
      tpane.addTab("Types", types_panel);
      if (USE_TYPES_TREE_TABLE)  { tpane.addTab("TypesTree", types_tree_panel); }
      if (INCLUDE_NAME_SEARCH)  {tpane.addTab("Name Search", namesearchP); }
      splitpane.setRightComponent(tpane);
    }
    else {
      splitpane.setRightComponent(types_panel);
    }

    // As soon as this component becomes visible, set the splitpane position
    this.addComponentListener(new ComponentAdapter() {
      public void componentShown(ComponentEvent evt) {
        splitpane.setDividerLocation(0.35);
        // but only do this the FIRST time this component is made visible
        Das2LoadView.this.removeComponentListener(this);
      }
    });

    this.add("Center", splitpane);

    load_featuresB.addActionListener(this);
    //    this.addComponentListener(this);  turned off pending change mechanism for now
    gmodel.addSeqSelectionListener(this);
    gmodel.addGroupSelectionListener(this);

    tree.getSelectionModel().setSelectionMode
      (TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.addTreeSelectionListener(this);

    searchTF.addActionListener(this);
  }


  public void valueChanged(TreeSelectionEvent evt) {
    Object node = tree.getLastSelectedPathComponent();
    if (node == null) return;
    if (node instanceof OldDas2VersionTreeNode) {
      current_version = ((OldDas2VersionTreeNode)node).getVersionedSource();
      System.out.println(current_version);
      System.out.println("  clicked on Das2VersionTreeNode to select genome: " + current_version.getGenome().getID());
      setRegionsAndTypes();
    }
  }

  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if (src == load_featuresB) {
      System.out.println("Das2LoadView received ActionEvent on load features button");
      loadFeaturesInView(true);
    }
    else if (src == searchTF) {
      String name = searchTF.getText();
      System.out.println("trying to search for annotation name: " + name);
      loadFeaturesByName(name);
      MutableAnnotatedBioSeq aseq = gmodel.getSelectedSeq();
      gviewer.setAnnotatedSeq(aseq, true, true);
    }
  }


  public void setRegionsAndTypes() {
    load_featuresB.setEnabled(false);
    types_table.setModel(empty_table_model);
    types_table.validate();
    types_table.repaint();

    final SwingWorker worker = new SwingWorker() {
	Map seqs = null;
	Map types = null;
	public Object construct() {
	  seqs = current_version.getSegments();
	  types = current_version.getTypes();
	  return null;
	}
	public void finished() {
	  /*
	   *  assumes that the types available for a given versioned source do not change
	   *    during the session
	   */
	  java.util.List type_states = 	(java.util.List)version2typestates.get(current_version);
	  if (type_states == null) {
	    type_states = new ArrayList();
            if (types != null) {
              Iterator iter = types.values().iterator();
              while (iter.hasNext()) {
                // need a map of Das2Type to Das2TypeState that persists for entire session,
                //    and reuse Das2TypeStates when possible (because no guarantee that
                //    Das2TypeState backing store has been updated during session)
                Das2Type dtype = (Das2Type)iter.next();
                OldDas2TypeState tstate = new OldDas2TypeState(dtype);
                type_states.add(tstate);
              }
            }
	    version2typestates.put(current_version, type_states);
	  }
	  OldDas2TypesTableModel new_table_model = new OldDas2TypesTableModel(type_states);
	  types_table.setModel(new_table_model);
	  new_table_model.addTableModelListener(myself);
	  TableColumn col = types_table.getColumnModel().getColumn(OldDas2TypesTableModel.LOAD_STRATEGY_COLUMN);
	  col.setCellEditor(new DefaultCellEditor(typestateCB));

	  types_table.validate();
	  types_table.repaint();
          if (gviewer != null) {
            load_featuresB.setEnabled(true);
          }
	  // need to do this here within finished(), otherwise may get threading issues where
	  //    GroupSelectionEvents are being generated before group gets populated with seqs
	  System.out.println("gmodel selected group:  " + gmodel.getSelectedSeqGroup());
	  System.out.println("current_vers.getGenome: " + current_version.getGenome());
	  if (gmodel.getSelectedSeqGroup() != current_version.getGenome()) {
	    gmodel.setSelectedSeq(null);
	    System.out.println("setting selected group to : " + current_version.getGenome());
	    gmodel.setSelectedSeqGroup(current_version.getGenome());
	  }
	  else {
	    current_seq = gmodel.getSelectedSeq();
	    loadWholeSequenceAnnots();
	  }
	}
      };
    worker.start();
  }

  public void loadFeaturesByName(String name) {
    if (current_version != null) {
      // Das2VersionedSource.getFeaturesByName() should also add features as annotations to seqs...
      java.util.List feats = current_version.getFeaturesByName(name);
    }
  }

  public void loadFeaturesInView() {
    loadFeaturesInView(false);
  }

  public void loadFeaturesInView(boolean restrict_to_current_vsource) {
    MutableAnnotatedBioSeq selected_seq = gmodel.getSelectedSeq();
    if (! (selected_seq instanceof SmartAnnotBioSeq)) {
      ErrorHandler.errorPanel("ERROR", "selected seq is not appropriate for loading DAS2 data");
      return;
    }
    final SeqSpan overlap = gviewer.getVisibleSpan();
    final MutableAnnotatedBioSeq visible_seq = (MutableAnnotatedBioSeq)overlap.getBioSeq();
    if (selected_seq == null) {
      ErrorHandler.errorPanel("ERROR", "You must first choose a sequence to display.");
      return;
    }
    if (visible_seq != selected_seq) {
      System.out.println("ERROR, VISIBLE SPAN DOES NOT MATCH GMODEL'S SELECTED SEQ!!!");
      return;
    }

    System.out.println("seq = " + visible_seq.getID() +
		       ", min = " + overlap.getMin() + ", max = " + overlap.getMax());
    SmartAnnotBioSeq aseq = (SmartAnnotBioSeq)selected_seq;
    AnnotatedSeqGroup genome = aseq.getSeqGroup();
    java.util.List vsources;

    // iterate through Das2TypeStates
    //    if off, ignore
    //    if load_in_visible_range, do range in view annotation request

    // maybe add a fully_loaded flag so know which ones to skip because they're done?
    if (restrict_to_current_vsource) {
      vsources = new ArrayList();
      vsources.add(current_version);
    }
    else {
      boolean FORCE_SERVER_LOAD = false;
      vsources = Das2Discovery.getVersionedSources(genome, false);
    }

    ArrayList requests = new ArrayList();

    for (int i=0; i<vsources.size(); i++) {
      Das2VersionedSource vsource = (Das2VersionedSource)vsources.get(i);
      if (vsource == null) { continue; }
      java.util.List type_states = (java.util.List) version2typestates.get(vsource);
      if (type_states == null) { continue; }
      Das2Region region = vsource.getSegment(aseq);
      Iterator titer = type_states.iterator();
      while (titer.hasNext()) {
	OldDas2TypeState tstate = (OldDas2TypeState)titer.next();
	Das2Type dtype = tstate.getDas2Type();
	//  only add to request list if set for loading and strategy is VISIBLE_RANGE loading
	if (tstate.getLoad() && tstate.getLoadStrategy() == OldDas2TypeState.VISIBLE_RANGE) {
	  System.out.println("type to load for visible range: " + dtype.getID());
	  Das2FeatureRequestSym request_sym =
	    new Das2FeatureRequestSym(dtype, region, overlap, null);
	  requests.add(request_sym);
	}
      }
    }
    if (requests.size() == 0) {
      ErrorHandler.errorPanel("Select some data", "You must first zoom in to " +
        "your area of interest and then select some data types "
        +"from the table above before pressing the \"Load\" button.");
    }
    else {
      processFeatureRequests(requests, true);
    }
  }


  /**
   *  Takes a list of Das2FeatureRequestSyms, and pushes them through the Das2ClientOptimizer to
   *     make DAS/2 feature requests and load annotations from the response documents.
   *  Uses SwingWorker to run requests on a separate thread
   *  If update_display, then updates IGB's main view after annotations are loaded (on GUI thread)
   *
   *  could probably add finer resolution of threading here,
   *  so every request (one per type) launches on its own thread
   *  But for now putting them all on same (non-event) thread controlled by SwingWorker
   */
  public static void processFeatureRequests(java.util.List requests, final boolean update_display) {
    processFeatureRequests(requests, update_display, DEFAULT_THREAD_FEATURE_REQUESTS);
  }

  public static void processFeatureRequests(java.util.List requests, final boolean update_display, boolean thread_requests) {
    final java.util.List request_syms = requests;
    final java.util.List result_syms = new ArrayList();

    if ((request_syms == null) || (request_syms.size() == 0)) { return; }
    SwingWorker worker = new SwingWorker() {
	public Object construct() {
	  for (int i=0; i<request_syms.size(); i++) {
	    Das2FeatureRequestSym request_sym = (Das2FeatureRequestSym)request_syms.get(i);

            // Create an AnnotStyle so that we can automatically set the
            // human-readable name to the DAS2 name, rather than the ID, which is a URI
            Das2Type type = request_sym.getDas2Type();
            AnnotStyle style = AnnotStyle.getInstance(type.getID());
            style.setHumanName(type.getName());

            if (USE_DAS2_OPTIMIZER) {
	      result_syms.addAll(Das2ClientOptimizer.loadFeatures(request_sym));
	    }
	    else {
	      request_sym.getRegion().getFeatures(request_sym);
	      MutableAnnotatedBioSeq aseq = request_sym.getRegion().getAnnotatedSeq();
	      aseq.addAnnotation(request_sym);
              result_syms.add(request_sym);
	    }
	  }
	  return null;
	}

        public void finished() {

          // Could examine or print the request logs now....
//          Iterator iter = result_syms.iterator();
//          while (iter.hasNext()) {
//            Das2FeatureRequestSym request = (Das2FeatureRequestSym) iter.next();
//            Das2RequestLog request_log = request.getLog();
//            // could print out the request logs or something .....
//          }

	  if (update_display) {
	    if (USE_SIMPLE_VIEW) {
	      Das2FeatureRequestSym request_sym = (Das2FeatureRequestSym)request_syms.get(0);
	      MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq)request_sym.getOverlapSpan().getBioSeq();
	      if (simple_viewer == null) { simple_viewer = GenometryViewer.displaySeq(aseq, false); }
	      simple_viewer.setAnnotatedSeq(aseq);
	    }
	    else if (gviewer != null) {
	      MutableAnnotatedBioSeq aseq = gmodel.getSelectedSeq();
	      gviewer.setAnnotatedSeq(aseq, true, true);
	    }
	  }
	}
      };

    if (thread_requests) {
      worker.start();
    }
    else {
      // if not threaded, then want to execute code in above subclass of SwingWorker, but within this thread
      //   so just ignore the thread features of SwingWorker and call construct() and finished() directly to
      //   to execute in this thread
      try {
	worker.construct();
	worker.finished();
      }
      catch (Exception ex) { ex.printStackTrace(); }
    }
  }

  /**
   *  Called when selected sequence is changed.
   *  Want to go through all previously visited
   *     DAS/2 versioned sources that share the seq's AnnotatedSeqGroup,
   *     For each (similar_versioned_source)
   *         for each type
   *            if (Das2TypeState set to AUTO_PER_SEQUENCE loading) && ( !state.fullyLoaded(seq) )
   *                 Do full feature load for seq
   *  For now assume that if a type's load state is not AUTO_PER_SEQUENCE, then no auto-loading, only
   *    manual loading, which is handled in another method...
   */
  public void seqSelectionChanged(SeqSelectionEvent evt) {
    if (DEBUG_EVENTS) {
      System.out.println("Das2LoadView received SeqSelectionEvent, selected seq: " + evt.getSelectedSeq());
    }
    AnnotatedBioSeq newseq = evt.getSelectedSeq();
    if (current_seq != newseq) {
      current_seq = newseq;
      loadWholeSequenceAnnots();
    }
  }

  protected void loadWholeSequenceAnnots() {
    if (current_seq == null)  { return; }
    if (current_version != null) {
      SeqSpan overlap = new SimpleSeqSpan(0, current_seq.getLength(), current_seq);
      current_region = current_version.getSegment(current_seq);
      java.util.List type_states = (java.util.List)version2typestates.get(current_version);
      Iterator titer = type_states.iterator();
      ArrayList requests = new ArrayList();
      while (titer.hasNext()) {
	OldDas2TypeState tstate = (OldDas2TypeState)titer.next();
	Das2Type dtype = tstate.getDas2Type();
	if (tstate.getLoad() && tstate.getLoadStrategy() == OldDas2TypeState.WHOLE_SEQUENCE)  {
	  System.out.println("type to load for entire sequence range: " + dtype.getID());
	  Das2FeatureRequestSym request_sym =
	    new Das2FeatureRequestSym(dtype, current_region, overlap, null);
	  requests.add(request_sym);
	}
      }

      if (requests.size() > 0) {
	processFeatureRequests(requests, true);
      }
    }
  }

  /**
   *  When selected group changed, want to go through all previously visited
   *     DAS/2 servers (starting with the current one), and try and find
   *     a versioned source that shares the selected AnnotatedSeqGroup.
   *  If found, take first found and set versioned source, source, and server accordingly
   *  If not found, blank out versioned source and source, and switch server to "Choose a server"
   *
   *  For now, just looking at current server
   */
  public void groupSelectionChanged(GroupSelectionEvent evt) {
    if (DEBUG_EVENTS)  {
      System.out.println("Das2LoadView received GroupSelectionEvent: " + evt);
    }
    AnnotatedSeqGroup newgroup = evt.getSelectedGroup();
    if (current_group != newgroup) {
      current_group = newgroup;
      if (current_server != null)  {
        current_version = current_server.getVersionedSource(current_group);
        if (current_version == null) {
          // reset
          current_server = null;
          current_source = null;
          // need to reset table also...
          types_table.setModel(empty_table_model);
          types_table.validate();
          types_table.repaint();
        }
        else {
          current_source = current_version.getSource();
          current_server = current_source.getServerInfo();
          System.out.println("   new das source: " + current_source.getID() +
                             ",  new das version: " + current_version.getID());
        }
      }
    }
  }


  public void tableChanged(TableModelEvent evt) {
    if (DEBUG_EVENTS)  {
      System.out.println("Das2LoadView received table model changed event: " + evt);
    }
    OldDas2TypesTableModel type_model = (OldDas2TypesTableModel)evt.getSource();
    int col = evt.getColumn();
    int firstrow = evt.getFirstRow();
    int lastrow = evt.getLastRow();
    OldDas2TypeState  tstate = type_model.getTypeState(firstrow);

    if ((current_seq != null) && (col == OldDas2TypesTableModel.LOAD_STRATEGY_COLUMN ||
         col == OldDas2TypesTableModel.LOAD_BOOLEAN_COLUMN)) {
      // All attributes of TableModelEvent are in the TableModel coordinates, not
      // necessarily the same as the JTable coordinates, so use the model
      //      Object val = type_model.getValueAt(firstrow, col);
      //      System.out.println("value of changed table cell: " + val);

      SeqSpan overlap = new SimpleSeqSpan(0, current_seq.getLength(), current_seq);
      current_region = current_version.getSegment(current_seq);

      Das2Type dtype = tstate.getDas2Type();
      if (tstate.getLoad() && tstate.getLoadStrategy() == OldDas2TypeState.WHOLE_SEQUENCE)  {
	System.out.println("type to load for entire sequence range: " + dtype.getID());
	Das2FeatureRequestSym request_sym =
	  new Das2FeatureRequestSym(dtype, current_region, overlap, null);
	ArrayList requests = new ArrayList();
	requests.add(request_sym);
	processFeatureRequests(requests, true);
      }
    }
  }


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
    frm.setVisible(true);
  }

}

/**
 *  Relates a Das2VersionedSource to it's status in IGB.
 *  For example, whether _any_ annotations should be loaded from it
 *    (overrides Das2TypeState.load for any Das2Types served from that Das2VersionedSource)
 */
class OldDas2VersionState {
  static Preferences root_node = UnibrowPrefsUtil.getTopNode();
  static Preferences das2_node = root_node.node("das2");
  static boolean default_load = true;

}

/**
 *  Relates a Das2Type to it's status in IGB.
 *  For example, whether it's load strategy is set to "full sequence"
 *  or "visible range", and possibly other details.
 */
class OldDas2TypeState {
  static boolean default_load = false;
  static String[] LOAD_STRINGS = new String[3];
  static int VISIBLE_RANGE = 1;   // MANUAL_VISIBLE_RANGE
  static int WHOLE_SEQUENCE = 2;  // AUTO_WHOLE_SEQUENCE
  static int default_load_strategy = VISIBLE_RANGE;

  /*
   *  Want to retrieve type state from Preferences if possible
   *    node: ~/das2/server.root.url/typestate
   *    key: [typeid+"_loadstate"]  value: [load_state]     (load state is an integer??)
   */

  static Preferences root_node = UnibrowPrefsUtil.getTopNode();
  static Preferences das2_node = root_node.node("das2");

  static {
    LOAD_STRINGS[VISIBLE_RANGE] = "Visible Range";
    LOAD_STRINGS[WHOLE_SEQUENCE] = "Whole Sequence";
  }

  boolean load;
  int load_strategy;
  Das2Type type;
  Preferences lnode_strategy;
  Preferences lnode_load;

  public OldDas2TypeState(Das2Type dtype) {
    this.type = dtype;
    Das2VersionedSource version = type.getVersionedSource();
    Das2Source source = version.getSource();
    Das2ServerInfo server = source.getServerInfo();
    String server_root_url = server.getID();
    if (server_root_url.startsWith("http://")) { server_root_url = server_root_url.substring(7); }
    if (server_root_url.indexOf("//") > -1) {
      System.out.println("need to replace all double slashes in path!");
    }
    String base_node_id = version.getID();
    base_node_id = base_node_id.replaceAll("/{2,}", "/");
    String subnode_strategy = base_node_id + "/type_load_strategy";
    String subnode_load = base_node_id + "/type_load";
    // System.out.println("subnode_strategy = " + subnode_strategy);
    //    System.out.println("subnode_load = " + subnode_load);
    //        System.out.println("subnode = " + subnode);
    //    System.out.println("    length: " + subnode.length());
    lnode_load = UnibrowPrefsUtil.getSubnode(das2_node, subnode_load);
    lnode_strategy = UnibrowPrefsUtil.getSubnode(das2_node, subnode_strategy);
    load = lnode_load.getBoolean(UnibrowPrefsUtil.shortKeyName(type.getID()), default_load);
    load_strategy = lnode_strategy.getInt(UnibrowPrefsUtil.shortKeyName(type.getID()), default_load_strategy);
  }

  public void setLoad(boolean b) {
    load = b;
    lnode_load.putBoolean(UnibrowPrefsUtil.shortKeyName(type.getID()), load);
  }

  public boolean getLoad() {
    return load;
  }

  public void setLoadStrategy(String strat) {
    for (int i=0; i<LOAD_STRINGS.length; i++) {
      if (strat.equals(LOAD_STRINGS[i])) {
	setLoadStrategy(i);
	break;
      }
    }
  }

  public void setLoadStrategy(int strategy) {
    load_strategy = strategy;
    lnode_strategy.putInt(UnibrowPrefsUtil.shortKeyName(type.getID()), strategy);
  }

  public int getLoadStrategy() { return load_strategy; }
  public String getLoadString() { return LOAD_STRINGS[load_strategy]; }
  public Das2Type getDas2Type() { return type; }
  public String toString() { return getDas2Type().getName(); }
}


class OldDas2TypesTableModel extends AbstractTableModel   {
  static String[] column_names = { "load", "name", "ID", "ontology", "source", "range" };
  static int LOAD_BOOLEAN_COLUMN = 0;
  static int NAME_COLUMN = 1;
  static int ID_COLUMN = 2;
  static int ONTOLOGY_COLUMN = 3;
  static int SOURCE_COLUMN = 4;
  static int LOAD_STRATEGY_COLUMN = 5;

  static int model_count = 0;

  int model_num;
  java.util.List type_states;

  public OldDas2TypesTableModel(java.util.List states) {
    model_num = model_count;
    model_count++;
    type_states = states;
    int col_count = column_names.length;
    int row_count = states.size();
  }

  public OldDas2TypeState getTypeState(int row) {
    return (OldDas2TypeState)type_states.get(row);
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
    OldDas2TypeState state = getTypeState(row);
    Das2Type type = state.getDas2Type();
    if (col == NAME_COLUMN) {
      return type.getName();
    }
    else if (col == ID_COLUMN) {
      return type.getID();
    }
    else if (col == ONTOLOGY_COLUMN) {
      return type.getOntology();
    }
    else if (col == SOURCE_COLUMN) {
      return type.getDerivation();
    }
    else if (col == LOAD_STRATEGY_COLUMN) {
      return state.getLoadString();
    }
    else if (col == LOAD_BOOLEAN_COLUMN) {
      return (state.getLoad() ? Boolean.TRUE : Boolean.FALSE);
    }
    return null;
  }

  public Class getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }

  public boolean isCellEditable(int row, int col) {
    if (col == LOAD_STRATEGY_COLUMN || col == LOAD_BOOLEAN_COLUMN) { return true; }
    else { return false; }
  }

  public void setValueAt(Object value, int row, int col) {
    //      System.out.println("Das2TypesTableModel.setValueAt() called, row = " + row +
    //			 ", col = " + col + "val = " + value.toString());
    OldDas2TypeState state = (OldDas2TypeState)type_states.get(row);
    if (col == LOAD_STRATEGY_COLUMN)  {
      state.setLoadStrategy(value.toString());
    }

    else if (col == LOAD_BOOLEAN_COLUMN) {
      Boolean bool = (Boolean)value;
      state.setLoad(bool.booleanValue());
    }

    fireTableCellUpdated(row, col);
  }
}


/**
 *  TreeNode wrapper around a Das2ServerInfo object.
 */
class OldDas2ServerTreeNode extends OldDataSourcesAbstractNode {
  Das2ServerInfo server;
  // using Vector instead of generic List because TreeNode interface requires children() to return Enumeration
  Vector child_nodes = null;

  public OldDas2ServerTreeNode(Das2ServerInfo server) {
    this.server = server;
  }

  public int getChildCount() {
    if (child_nodes == null) { populate(); }
    return child_nodes.size();
  }

  public TreeNode getChildAt(int childIndex) {
    if (child_nodes == null) { populate(); }
    return (TreeNode)child_nodes.get(childIndex);
  }

  public Enumeration children() {
    if (child_nodes == null) { populate(); }
    return child_nodes.elements();
  }

  /**
   *  First time children are accessed, this will trigger dynamic access to DAS2 server.
   */
  protected void populate() {
    if (child_nodes == null) {
      Map sources = server.getSources();
      child_nodes = new Vector(sources.size());
      Iterator iter = sources.values().iterator();
      while (iter.hasNext()) {
	Das2Source source = (Das2Source)iter.next();
	OldDas2SourceTreeNode child = new OldDas2SourceTreeNode(source);
	child_nodes.add(child);
      }
    }
  }

  public boolean getAllowsChildren() { return true; }
  public boolean isLeaf() { return false; }
  public String toString() { return server.getName(); }
  /** NOT YET IMPLEMENTED */
  public int getIndex(TreeNode node) {
    System.out.println("Das2ServerTreeNode.getIndex() called: " + toString());
    return -1;
  }
}

/**
 *  TreeNode wrapper around a Das2Source object.
 */
class OldDas2SourceTreeNode extends OldDataSourcesAbstractNode {
  Das2Source source;
  Vector version_nodes;

  public OldDas2SourceTreeNode(Das2Source source) {
    this.source = source;
    Map versions = source.getVersions();
    version_nodes = new Vector(versions.size());
    Iterator iter = versions.values().iterator();
    while (iter.hasNext()) {
      Das2VersionedSource version = (Das2VersionedSource)iter.next();
      OldDas2VersionTreeNode child = new OldDas2VersionTreeNode(version);
      version_nodes.add(child);
    }
  }
  public Das2Source getSource() { return source; }
  public int getChildCount() { return version_nodes.size(); }
  public TreeNode getChildAt(int childIndex) { return (TreeNode)version_nodes.get(childIndex); }
  public Enumeration children() { return version_nodes.elements(); }
  public boolean getAllowsChildren() { return true; }
  public boolean isLeaf() { return false; }
  public String toString() { return source.getName(); }
  /** NOT YET IMPLEMENTED */
  public int getIndex(TreeNode node) {
    System.out.println("Das2ServerTreeNode.getIndex() called: " + toString());
    return -1;
  }

}

/**
 * TreeNode wrapper around a Das2VersionedSource object.
 * Maybe don't really need this, since Das2VersionedSource could itself serve
 * as a leaf.
 */
class OldDas2VersionTreeNode extends OldDataSourcesAbstractNode {
  Das2VersionedSource version;

  public OldDas2VersionTreeNode(Das2VersionedSource version) { this.version = version; }
  public Das2VersionedSource getVersionedSource() { return version; }
  public String toString() { return version.getName(); }

  // using Vector instead of generic List because TreeNode interface requires children() to return Enumeration
  Vector child_nodes = null;

  public int getChildCount() {
    if (child_nodes == null) { populate(); }
    return child_nodes.size();
  }

  public TreeNode getChildAt(int childIndex) {
    if (child_nodes == null) { populate(); }
    return (TreeNode)child_nodes.get(childIndex);
  }

  public Enumeration children() {
    if (child_nodes == null) { populate(); }
    return child_nodes.elements();
  }

  /**
   *  First time children are accessed, this will trigger dynamic access to DAS2 server.
   */
  protected void populate() {
    if (child_nodes == null) {
      Map types = version.getTypes();
      child_nodes = new Vector(types.size());
      Iterator iter = types.values().iterator();
      while (iter.hasNext()) {
	Das2Type type = (Das2Type)iter.next();
	OldDas2TypeTreeNode child = new OldDas2TypeTreeNode(type);
	child_nodes.add(child);
      }
    }
  }

  public boolean getAllowsChildren() { return true; }
  public boolean isLeaf() { return false; }
  public int getIndex(TreeNode node) {
    System.out.println("Das2VersionTreeNode.getIndex() called: " + toString());
    return -1;
  }
}



class OldDas2TypeTreeNode extends OldDataSourcesAbstractNode {
  //  Das2TypeState type_state;
  Das2Type type;
  public OldDas2TypeTreeNode(Das2Type type) { this.type = type; }
  //  public Das2TypeState getTypeState() { return type_state; }
  //  public Das2Type getDas2Type(}) { return type_state.getDas2Type(); }

  public String toString() { return type.getName(); }
  public int getChildCount() { return 0; }
  public TreeNode getChildAt(int index) { return null; }
  public Enumeration children() { return null; }
  public boolean getAllowsChildren() { return false; }
  public boolean isLeaf() { return true; }
  public int getIndex(TreeNode node) {
    System.out.println("Das2TypeTreeNode.getIndex() called: " + toString());
    return -1;
  }
}



/**
 *   Stubs out MutableTreeNode methods that aren't used for Das2*Node objects.
 */
abstract class OldDataSourcesAbstractNode implements MutableTreeNode {
  TreeNode parent;
  public void insert(MutableTreeNode child, int index)  {}
  public void remove(int index) {}
  public void remove(MutableTreeNode node) {}
  public void removeFromParent() {}
  public void setParent(MutableTreeNode newParent) {
    this.parent = parent;
  }
  public TreeNode getParent() {
    return parent;
  }
  public void setUserObject(Object object) {}
}

