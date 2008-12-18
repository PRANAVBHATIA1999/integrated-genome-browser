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

package com.affymetrix.igb.glyph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import com.affymetrix.igb.tiers.*;
import com.affymetrix.genoviz.event.*;
import com.affymetrix.genoviz.widget.*;
import com.affymetrix.genoviz.bioviews.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometryImpl.SingletonGenometryModel;
import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.igb.menuitem.FileTracker;
import com.affymetrix.igb.util.GraphGlyphUtils;
import com.affymetrix.igb.util.GraphSymUtils;
import com.affymetrix.genometryImpl.util.UniFileChooser;
import com.affymetrix.igb.Application;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.view.ContextualPopupListener;
import com.affymetrix.igb.view.SeqMapView;


/**
 *  This class provides a popup menu for a SeqMapView to allow certain graph manipulations.
 *  It is mostly an obsolete class, since most of the manipulations are now done without
 *  using pop-up menus.
 */
public class GraphSelectionManager
  implements MouseListener, MouseMotionListener, ActionListener, NeoGlyphDragListener,
  ContextualPopupListener, TierLabelManager.PopupListener {
  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();

  /**
   *  ALLOW_HORIZONTAL_SHIFT determines whether a glyph can be dragged
   *  horizontally as well as vertically.  In general this is _not_ a desirable
   *  behavior.  However, it has occasionally proven useful when trying to
   *  debug potential problems with the input graph data.
   **/
  boolean ALLOW_HORIZONTAL_SHIFT = false;

  boolean ALLOW_THRESHOLD_DRAG = false;
  //  ArrayList stored_threshMI = new ArrayList();
  //  Map threshMI_2_thresh = new HashMap();
  final static boolean DEBUG = false;

  boolean DRAG_IN_FRONT = true;
  int max_label_length = 50;

  static FileTracker output_file_tracker = FileTracker.OUTPUT_DIR_TRACKER;

  List graphlist = new ArrayList();
  GraphGlyph current_graph = null;
  GraphGlyph graph_to_scale = null;
  //   second_curent_graph is
  //   the graph selected just _before_ the current_graph in a multi-select
  //   (this is usually the previous current_graph if multi-selection is happening,
  //    but null if no multi-select)
  GraphGlyph second_current_graph = null;

  // The current_source will be the AffyTieredMap of the SeqMapView
  //  final NeoWidgetI current_source;
  NeoWidgetI current_source;

  //  boolean scaling_graph = true;
  boolean scaling_graph = false;
  double start_graph_ycoord;
  double start_graph_height;
  double start_mouse_ycoord;

  int xoffset_pop = 10;
  int yoffset_pop = 0;
  JPopupMenu popup;
  //  JMenuItem graph_style;
  JLabel graph_info;
  JLabel graph_info2;

  JMenu graph_style = new JMenu("Graph Style");
  JMenu decor = new JMenu("Decorations");
  //JMenu thresh = new JMenu("Thresholding");
  JMenu bounds = new JMenu("Adjust Visible Graph Bounds");
  JMenu combine = new JMenu("Combine 2 Graphs");
  JMenuItem span_graph;

  JMenuItem bar_graph;
  JMenuItem line_graph;
  JMenuItem dot_graph;
  JMenuItem stairstep_graph;
  JMenuItem min_max_graph;
  JMenuItem adjust_percent;
  JMenuItem adjust_false_positive;
  JMenuItem adjust_hilo;
  JMenuItem change_color;
  JMenuItem show_label;
  JMenuItem show_axis;
  JMenuItem show_bounds;
  JMenuItem show_graph;
  JMenuItem tweak_thresh;
  JMenuItem thresh_graph;
  JMenuItem max_gap_thresh;
  JMenuItem min_score_thresh;
  JMenuItem min_run_thresh;
  JMenuItem pickle_thresh;
  JMenuItem faster_draw_toggle;
  JMenuItem delete_graph;
  JMenuItem to_front;
  JMenuItem to_back;
  JMenuItem toggle_floating;
  JMenuItem diff_graphs;
  JMenuItem sum_graphs;
  JMenuItem ratio_graphs;
  JMenuItem product_graphs;
  JMenuItem save_graph;

  GlyphDragger dragger;
  SeqMapView gviewer;
  JFrame frm;

  public GraphSelectionManager(SeqMapView smv) {
    this();
    gviewer = smv;
    current_source = gviewer.getSeqMap();
    frm = Application.getSingleton().getFrame();
  }
  
  /** A constructor to use if there is no SeqMapView.  Note that graph arithmetic
   *  operations will not be allowed in this case.
   */
  public GraphSelectionManager(NeoWidgetI source) {
    this();
    gviewer = null;
    current_source = source;
    frm = Application.getSingleton().getFrame();
  }

  protected GraphSelectionManager() {
    popup = new JPopupMenu();

    graph_info = new JLabel("");
    graph_info2 = new JLabel("");

    adjust_percent = new JMenuItem("Adjust By Percentage");
    adjust_hilo = new JMenuItem("Adjust By Value");
    min_max_graph = new JMenuItem("MinMaxAvg Graph");
    line_graph = new JMenuItem("Line Graph");
    bar_graph = new JMenuItem("Bar Graph");
    dot_graph = new JMenuItem("Dot Graph");
    stairstep_graph = new JMenuItem("Stairstep Graph");
    thresh_graph = new JMenuItem("Toggle Threshold");
    tweak_thresh = new JMenuItem("Adjust Thresholds");
    change_color = new JMenuItem("Change Color");
    show_label = new JMenuItem("Toggle Label");
    show_bounds = new JMenuItem("Toggle Bounds");
    show_graph = new JMenuItem("Toggle Graph");
    show_axis = new JMenuItem("Toggle Y-axis");
    adjust_false_positive = new JMenuItem("Adjust Threshold By % FP");
    min_score_thresh = new JMenuItem("Adjust Score Threshold");
    max_gap_thresh = new JMenuItem("Adjust Max Gap Threshold");
    min_run_thresh = new JMenuItem("Adjust Min Run Threshold");
    pickle_thresh = new JMenuItem("Snapshot Thresholded Regions");
    faster_draw_toggle = new JMenuItem("Toggle Faster MinMaxAvg Draw");
    delete_graph = new JMenuItem("Delete Graph");
    to_front = new JMenuItem("Move To Front");
    to_back = new JMenuItem("Move To Back");
    toggle_floating = new JMenuItem("toggle floating");
    diff_graphs = new JMenuItem("Create Difference Graph (A-B)");
    sum_graphs = new JMenuItem("Create Sum Graph (A+B)");
    ratio_graphs = new JMenuItem("Create Ratio Graph (A/B)");
    product_graphs = new JMenuItem("Create Product Graph (A*B)");
    save_graph = new JMenuItem("Save Graph to File");

    popup.add(graph_info);
    popup.add(graph_info2);

    popup.add(combine);
    combine.add(diff_graphs);
    combine.add(sum_graphs);
    combine.add(ratio_graphs);
    combine.add(product_graphs);

    graph_style.add(min_max_graph);
    graph_style.add(line_graph);
    graph_style.add(bar_graph);
    graph_style.add(dot_graph);
    graph_style.add(stairstep_graph);

    decor.add(show_label);
    decor.add(show_axis);
    decor.add(show_bounds);
    decor.add(show_graph);

    //thresh.add(thresh_graph);
    //thresh.add(tweak_thresh);
    //thresh.add(adjust_false_positive);
    //thresh.add(pickle_thresh);

    bounds.add(adjust_hilo);
    bounds.add(adjust_percent);

    adjust_hilo.addActionListener(this);
    adjust_percent.addActionListener(this);
    adjust_false_positive.addActionListener(this);
    min_max_graph.addActionListener(this);
    faster_draw_toggle.addActionListener(this);
    line_graph.addActionListener(this);
    bar_graph.addActionListener(this);
    dot_graph.addActionListener(this);
    stairstep_graph.addActionListener(this);
    thresh_graph.addActionListener(this);
    tweak_thresh.addActionListener(this);
    max_gap_thresh.addActionListener(this);
    min_score_thresh.addActionListener(this);
    min_run_thresh.addActionListener(this);
    pickle_thresh.addActionListener(this);
    change_color.addActionListener(this);
    show_label.addActionListener(this);
    show_axis.addActionListener(this);
    show_bounds.addActionListener(this);
    show_graph.addActionListener(this);
    delete_graph.addActionListener(this);
    to_front.addActionListener(this);
    to_back.addActionListener(this);
    toggle_floating.addActionListener(this);
    diff_graphs.addActionListener(this);
    sum_graphs.addActionListener(this);
    ratio_graphs.addActionListener(this);
    product_graphs.addActionListener(this);
    save_graph.addActionListener(this);
  }

  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if (current_graph != null) {
      if (src == bar_graph) {
        if (DEBUG) System.out.println("picked bar graph");
        current_graph.setGraphStyle(GraphGlyph.BAR_GRAPH);
      }
      else if (src == line_graph) {
        if (DEBUG) System.out.println("picked line graph");
        current_graph.setGraphStyle(GraphGlyph.LINE_GRAPH);
      }
      else if (src == dot_graph) {
        if (DEBUG) System.out.println("picked dot graph");
        current_graph.setGraphStyle(GraphGlyph.DOT_GRAPH);
      }
      else if (src == stairstep_graph) {
        if (DEBUG) System.out.println("picked stairstep graph");
        current_graph.setGraphStyle(GraphGlyph.STAIRSTEP_GRAPH);
      }
      else if (src == min_max_graph) {
        if (current_graph instanceof SmartGraphGlyph) {
          current_graph.setGraphStyle(SmartGraphGlyph.MINMAXAVG);
        }
      }
      else if (src == adjust_hilo) {
        if (current_graph instanceof SmartGraphGlyph) {
          SmartGraphGlyph sgg = (SmartGraphGlyph)current_graph;
          if (DEBUG) System.out.println("setting up graph bounds adjuster");
          GraphVisibleBoundsSetter.showFramedThresholder(current_graph, current_source);
        }
      }
      else if (src == adjust_percent) {
        if (current_graph instanceof SmartGraphGlyph) {
          SmartGraphGlyph sgg = (SmartGraphGlyph)current_graph;
          if (DEBUG) System.out.println("setting up percent adjuster");
          PercentThresholder.showFramedThresholder(sgg, current_source);
        }
      }
      else if (src == thresh_graph) {
        if (current_graph instanceof SmartGraphGlyph) {
          if (second_current_graph != null || current_graph == null) {
            Application.errorPanel("ERROR", "Must select exactly one graph");
          } else {
            SmartGraphGlyph sgg = (SmartGraphGlyph)current_graph;
            boolean show = ! sgg.getShowThreshold();
            sgg.setShowThreshold(show);
          }
        }
      }
      else if (src == tweak_thresh) {
        if (current_graph instanceof SmartGraphGlyph) {
          showThresholds((SmartGraphGlyph)current_graph);
        }
      }
      else if (src == max_gap_thresh) {
        if (current_graph instanceof SmartGraphGlyph) {
          SmartGraphGlyph sgg = (SmartGraphGlyph)current_graph;
          //          if (sgg.getShowThreshold()) {
          sgg.setShowThreshold(true);
            if (DEBUG) System.out.println("setting up max_gap thresholder");
            MaxGapThresholder.showFramedThresholder(sgg, current_source);
            //          }
        }
      }
      else if (src == min_score_thresh) {
        if (current_graph instanceof SmartGraphGlyph) {
          SmartGraphGlyph sgg = (SmartGraphGlyph)current_graph;
          sgg.setShowThreshold(true);
          if (DEBUG) System.out.println("setting up max_gap thresholder");
          MinScoreThresholder.showFramedThresholder(sgg, current_source);
        }
      }
      else if (src == min_run_thresh) {
        if (current_graph instanceof SmartGraphGlyph) {
          SmartGraphGlyph sgg = (SmartGraphGlyph)current_graph;
          if (sgg.getShowThreshold()) {
            if (DEBUG) System.out.println("setting up min_run thresholder");
            MinRunThresholder.showFramedThresholder(sgg, current_source);
          }
        }
      }
//      else if (src == pickle_thresh) {
//        if (current_graph instanceof SmartGraphGlyph) {
//          SmartGraphGlyph sgg = (SmartGraphGlyph)current_graph;
//          pickleThreshold(sgg);
//        }
//      }
     /*
       else if (src == faster_draw_toggle) {
        if (current_graph instanceof SmartGraphGlyph) {
          SmartGraphGlyph sgg = (SmartGraphGlyph)current_graph;
          sgg.setFasterDraw(! sgg.getFasterDraw());
        }
      }
      */
      else if (src == change_color) {
        Color col = JColorChooser.showDialog(frm,
          "Graph Color Chooser", current_graph.getColor());
        if (col != null) {
          current_graph.setColor(col);
          // if graph is in a tier, change foreground color of tier also
          //   (which in turn triggers change in color for TierLabelGlyph...)
          if (current_graph.getParent() instanceof TierGlyph) {
            current_graph.getParent().setForegroundColor(col);
          }
        }
      }
      else if (src == show_bounds) {
        current_graph.setShowBounds(! current_graph.getShowBounds());
      }
      else if (src == show_graph) {
        current_graph.setShowGraph(! current_graph.getShowGraph());
      }
      else if (src == show_label) {
        current_graph.setShowLabel( ! current_graph.getShowLabel());
      }
      else if (src == show_axis) {
        if (current_graph instanceof SmartGraphGlyph) {
          SmartGraphGlyph sgg = (SmartGraphGlyph)current_graph;
          sgg.setShowAxis( ! sgg.getShowAxis());
        }
      }
      else if (src == delete_graph) {
        deleteGraph(current_source, current_graph);
        current_graph = null;  // for garbage collection, and other reasons
        graphlist.remove(current_source); // for garbage collection, etc.
      }
      // NOT YET WORKING --
      // need to put graph's parent PixelFloaterGlyphs in their own parent PixelFloaterGlyph,
      //   rather than have them as siblings of tiers -- otherwise, when moved to back, will
      //   end up _behind_ all the tiers, and since tiers fill in their backgrounds, the graphs
      //   will effectively disapear!
      else if (src == to_back) {
        current_source.toBackOfSiblings(current_graph);
        GlyphI parent = current_graph.getParent();
        if ((parent != null) && (! (parent instanceof TierGlyph))) {
          current_source.toBackOfSiblings(parent);
        }
      }
      else if (src == toggle_floating) {
        if (DEBUG) System.out.println("selected toggle floating, currently floating: " +
                           !(current_graph.getParent() instanceof TierGlyph));
        //        GraphGlyphUtils.toggleFloating(current_graph, gviewer);
        // toggle_floating is currently unused, so don't worry that the code is commented out
      }

      else if (src == diff_graphs) {
        graphArithmetic(current_graph, second_current_graph, GraphGlyphUtils.MATH_DIFFERENCE);
      }
      else if (src == sum_graphs) {
        graphArithmetic(current_graph, second_current_graph, GraphGlyphUtils.MATH_SUM);
      }
      else if (src == ratio_graphs) {
        graphArithmetic(current_graph, second_current_graph, GraphGlyphUtils.MATH_RATIO);
      }
      else if (src == product_graphs) {
        graphArithmetic(current_graph, second_current_graph, GraphGlyphUtils.MATH_PRODUCT);
      }
      else if (src == save_graph) {
        saveGraph(current_graph);
      }
      /*
      else if ((src instanceof JMenuItem) && (threshMI_2_thresh.get(src) != null)) {
        JMenuItem item = (JMenuItem)src;
        float val = ((Float)threshMI_2_thresh.get(item)).floatValue();
        if (DEBUG) System.out.println("thresh: " + item.getText() + ", value = " + val);
        if (current_graph instanceof SmartGraphGlyph) {
          SmartGraphGlyph sgg = (SmartGraphGlyph)current_graph;
          sgg.setShowThreshold(true);
          sgg.setMinScoreThreshold(val);
        }
      }
      */
      current_source.updateWidget();
    }
    graphlist.clear(); // for garbage collection, etc.
  }


  /** Deletes a graph from a widget, and tries to make sure the GraphSym can
   *  be garbage collected.  If the graph happens to occupy a
   *  tier in the source which is a tier map, then delete the tier as well.
   *  If the graph's symmetry is in a mutalbe bio seq, remove it from there.
   */
  void deleteGraph(NeoWidgetI source, GraphGlyph gl) {
    source.removeItem(gl);
    // clean-up references to the graph, allowing garbage-collection, etc.
    gmodel.clearSelectedSymmetries(this);

    Object info = gl.getInfo();
    GraphSym gsym = null;
    if (info instanceof GraphSym) {
      gsym = (GraphSym) info;
      AnnotatedBioSeq aseq = (AnnotatedBioSeq) gsym.getGraphSeq();
      if (aseq instanceof MutableAnnotatedBioSeq) {
        MutableAnnotatedBioSeq mut = (MutableAnnotatedBioSeq) aseq;
        mut.removeAnnotation(gsym);
      }
    }

    // if this is not a floating graph, get rid of the tier it was in
    if (source instanceof AffyTieredMap &&
      ! GraphGlyphUtils.hasFloatingAncestor(gl) ) {

      AffyTieredMap map = (AffyTieredMap) source;
      GlyphI parentgl = gl.getParent();
      parentgl.removeChild(gl);
      if (parentgl instanceof TierGlyph) {
        map.removeTier((TierGlyph)parentgl);
        map.packTiers(false, true, false);
        map.stretchToFit(false, false);
      }
    }
  }

  public void showThresholds(SmartGraphGlyph sgg) {
    if (! sgg.getShowThreshold())  {
      sgg.setShowThreshold(true);
      current_source.updateWidget();
    }
    MinScoreThresholder score_thresher = new MinScoreThresholder(sgg, current_source);
    MaxGapThresholder maxgap_thresher = new MaxGapThresholder(sgg, current_source);
    MinRunThresholder minrun_thresher = new MinRunThresholder(sgg, current_source);
    JFrame frm = new JFrame("Thresholds: " + sgg.getLabel());
    Container cpane = frm.getContentPane();
    cpane.setLayout(new GridLayout(3,1));
    cpane.add(score_thresher);
    cpane.add(maxgap_thresher);
    cpane.add(minrun_thresher);
    frm.addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        Window w = evt.getWindow();
        w.setVisible(false);
        w.dispose();
      }
    } );
    frm.pack();
    frm.setVisible(true);
  }

  static JFileChooser graph_file_chooser = null;

  /** Returns a file chooser that forces the user to use the 'gr' file extension. */
  static JFileChooser getFileChooser() {
    if (graph_file_chooser == null) {
      graph_file_chooser = UniFileChooser.getFileChooser("Graph File", "gr");
      graph_file_chooser.setCurrentDirectory(output_file_tracker.getFile());
    }
    return graph_file_chooser;
  }

  public void saveGraph(GraphGlyph graph) {
    Object info = graph.getInfo();
    if (info instanceof GraphSym) {
      FileOutputStream ostr = null;
      try {
        GraphSym gsym = (GraphSym)info;
        JFileChooser chooser = getFileChooser();
        //        int option = chooser.showSaveDialog(gviewer.getFrame());
        int option = chooser.showSaveDialog(frm);
        if (option == JFileChooser.APPROVE_OPTION) {
          output_file_tracker.setFile(chooser.getCurrentDirectory());
          File fil = chooser.getSelectedFile();
          GraphSymUtils.writeGraphFile(gsym, gmodel.getSelectedSeqGroup(), fil.getName());
        }
      }
      catch (Exception ex) {
         Application.errorPanel("Error saving graph", ex);
      } finally {
        if (ostr != null) try { ostr.close(); } catch (IOException ioe) {}
      }
    }
    else {
      Application.errorPanel("Can't Save", "Graph does not have associated GraphSym data model");
    }
  }

  void graphArithmetic(GraphGlyph graphA, GraphGlyph graphB, String function) {
    if (gviewer == null) {
      Application.errorPanel("This action is invalid at this time");
    }
    if (graphA == null || graphB == null) {
      // This error condition is likely never triggered
      Application.errorPanel("Must select exactly two graphs.");
    }
    String error = GraphGlyphUtils.graphsAreComparable(graphA, graphB);
    if (error != null) {
      ErrorHandler.errorPanel("ERROR", error);
      return;
    }
    else {
      GraphSym newsym = GraphGlyphUtils.graphArithmetic(graphA, graphB, function);
      
      MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq) newsym.getGraphSeq();
      aseq.addAnnotation(newsym);
      gviewer.setAnnotatedSeq(aseq, true, true);
      //GlyphI newglyph = gviewer.getSeqMap().getItem(newsym);
    }
  }




  /**
   *  Does nothing.  Formerly this was used to bring-up a pop-up menu, but
   *  that could cause conflicts with the other pop-up menu which is opened
   *  by the SeqMapViewMouseListener.  Thus now instead of opening our own
   *  pop-up, we use the routine {@link #popupNotify(JPopupMenu, List, SeqSymmetry)}
   *  provided by the interface ContextualPopupListener to add to the pop-up
   *  menu which the SeqMapView itself constructs.
   */
  public void mouseClicked(MouseEvent evt) {}

  public void mouseEntered(MouseEvent evt) { }
  public void mouseExited(MouseEvent evt) { }

  public void mousePressed(MouseEvent evt) {
    if (evt instanceof NeoMouseEvent) {
      NeoMouseEvent nevt = (NeoMouseEvent)evt;
      NeoWidget widg = (NeoWidget)nevt.getSource();
      Vector selected = nevt.getItems();
      for (int i=selected.size()-1; i >=0; i--) {
	//        System.out.println("selected: " + selected.elementAt(i));
        GlyphI gl = (GlyphI)selected.elementAt(i);
        if (ALLOW_THRESHOLD_DRAG &&
            (! (gl instanceof GraphGlyph)) &&
            (gl.getParent() instanceof GraphGlyph)) {
          // for now assume if child of GraphGlyph and not itself a GraphGlyph then it's the threshold glyph
          //          System.out.println("hit child of GraphGlyph");
          dragGraph(gl, nevt);
          break;
        }
        // only allow dragging and scaling if graph is contained within an ancestor PixelFloaterGlyph...
        else if (gl instanceof GraphGlyph && GraphGlyphUtils.hasFloatingAncestor(gl)) {
          GraphGlyph gr = (GraphGlyph)gl;
          if (nevt.isShiftDown() || nevt.isAltDown()) {
            scaleGraph(gr, nevt);
            break;
          }
          else {
            dragGraph(gr, nevt);
            break;
          }
        }
        else if (gl.getParent() instanceof GraphGlyph) {
          if (DEBUG) System.out.println("hit child of graph...");
        }
      }
    }
  }

  // only used for graph scaling (not for graph dragging)
  public void mouseReleased(MouseEvent evt) {
    if (evt instanceof NeoMouseEvent) {
      NeoMouseEvent nevt = (NeoMouseEvent)evt;
      scaling_graph = false;
      graph_to_scale = null;
      ((Component)nevt.getSource()).removeMouseMotionListener(this);
    }
  }

  public void mouseMoved(MouseEvent evt) { }

  // only used for graph scaling
  //   (not for graph dragging or thresholding, those are managed by a GlyphDragger)
  public void mouseDragged(MouseEvent evt) {
    //    System.out.println("dragging mouse");
    if (! (evt instanceof NeoMouseEvent)) { return; }
    NeoMouseEvent nevt = (NeoMouseEvent)evt;
    NeoWidgetI widg = (NeoWidgetI)nevt.getSource();
    if (scaling_graph)  {
      Rectangle2D bbox = graph_to_scale.getCoordBox();
      double coord_diff = start_mouse_ycoord - nevt.getCoordY();
      //      System.out.println(coord_diff);
      double graph_center = bbox.y + (bbox.height/2);
      double new_graph_height = start_graph_height + coord_diff;
      if (new_graph_height >= 0) {
        graph_to_scale.setCoords(bbox.x, graph_center - (new_graph_height/2),
                                 bbox.width, new_graph_height);
        widg.updateWidget();
      }
      else {
        //        System.out.println("uh-oh -- graph just went into negative height...");
      }
    }
  }

  //  public void dragGraph(GraphGlyph gl, NeoMouseEvent nevt) {
  public void dragGraph(GlyphI gl, NeoMouseEvent nevt) {
    NeoWidget widg = (NeoWidget)nevt.getSource();
    if (DRAG_IN_FRONT)  {
      if (widg instanceof NeoMap) {
        ((NeoMap)widg).toFront(gl);
      }
      else {
        // toFront() is specific to NeoMap, try toFrontOfSiblings() instead
        widg.toFrontOfSiblings(gl);
      }
    }
    dragger = new GlyphDragger((NeoWidgetI)nevt.getSource());
    dragger.setUseCopy(false);

    LinearTransform trans = new LinearTransform();
    LinearTransform vtrans = (LinearTransform)widg.getView().getTransform();
    //    gl.getGlobalChildTransform(widg.getView(), trans);
    if (gl instanceof ThreshGlyph) {
      gl.getParent().getGlobalTransform(widg.getView(), trans);
    }
    else {
      gl.getGlobalTransform(widg.getView(), trans);
    }

    trans.setScaleX(vtrans.getScaleX());
    trans.setOffsetX(vtrans.getOffsetX());

    if (nevt.isControlDown() && ALLOW_HORIZONTAL_SHIFT) {
      dragger.setConstraint(NeoWidgetI.HORIZONTAL, false);
    }
    else {
      dragger.setConstraint(NeoWidgetI.HORIZONTAL, true);
    }

    dragger.addGlyphDragListener(this);
    dragger.startDrag(gl, nevt, trans, false);

  }

  public void scaleGraph(GraphGlyph gl, NeoMouseEvent nevt) {

// The mouse motion listener is added here, and removed in heardGlpyhDrag()
    ((Component)nevt.getSource()).addMouseMotionListener(this);
    if (DEBUG) System.out.println("trying to scale graph");
    scaling_graph = true;
    graph_to_scale = gl;
    start_mouse_ycoord = nevt.getCoordY();
    start_graph_height = gl.getCoordBox().height;
  }

  public void heardGlyphDrag(NeoGlyphDragEvent evt) {
    int id = evt.getID();
    Object src = evt.getSource();
    if (id == evt.DRAG_IN_PROGRESS) {
      //      System.out.println("got a drag in progress event: " + evt);
      GlyphI gl = evt.getGlyph();
      if (gl.getParent() instanceof SmartGraphGlyph && src instanceof NeoWidget) {
        NeoWidget widg = (NeoWidget)src;
        ViewI view = widg.getView();
        GlyphI threshgl = gl;
        SmartGraphGlyph graphgl = (SmartGraphGlyph)threshgl.getParent();
        Rectangle2D tbox = threshgl.getCoordBox();
        float new_threshold = graphgl.getGraphValue(view, tbox.y);
        if (graphgl.getThresholdDirection() == GraphState.THRESHOLD_DIRECTION_GREATER) {
          graphgl.setMinScoreThreshold(new_threshold);
        } else {
          graphgl.setMaxScoreThreshold(new_threshold);
        }
      }
    }
    else if (id == evt.DRAG_ENDED) {
      dragger.removeGlyphDragListener(this);

      GlyphI gl = evt.getGlyph();
      if (gl instanceof GraphGlyph && src instanceof AffyTieredMap) {
        GraphGlyphUtils.checkPixelBounds((GraphGlyph) gl, (AffyTieredMap) src);
      }
    }
    // otherwise it must be DRAG_STARTED event, which can be ignored
    //   since this class called dragger.dragStart to begin with...
  }


  /** Make a simple lable for a graph glyph, no longer than max_label_length. */
  private String getGraphLabel(GraphGlyph gg) {
    if (gg==null) {return "";}
    String result = gg.getLabel();
    if (result == null) {result = "No label";}
    if (result.length() > max_label_length) {
      result = result.substring(0, max_label_length);
    }
    return result;
  }

  public void popupNotify(JPopupMenu the_popup, List selected_syms, SeqSymmetry primary_sym) {
    
    if (current_source == null) {
      // if there is no NeoWidgetI set for the current_source, then we cannot convert
      // selected symmetries into GlyphI's, so there is no point in adding items to
      // a popup menu.
      return;
    }
    
    Vector selected_graph_glyphs = new Vector(0);
    current_graph = null;
    second_current_graph = null;

    // convert the selected syms to a list of selected graph glyphs
    Iterator iter = selected_syms.iterator();
    while (iter.hasNext()) {
      SeqSymmetry sym = (SeqSymmetry) iter.next();
      GlyphI g = current_source.getItem(sym);
      if (g instanceof GraphGlyph) {
        selected_graph_glyphs.add(g);
      }
    }

    JMenu combine = new JMenu("Combine Graphs");
    if (selected_graph_glyphs.size() >= 2) {
      current_graph = (GraphGlyph) selected_graph_glyphs.get(0);
      second_current_graph = (GraphGlyph) selected_graph_glyphs.get(1);

      combine.setEnabled(true);
      JLabel graph_info_A = new JLabel("A: "+getGraphLabel(current_graph));
      JLabel graph_info_B = new JLabel("B: "+getGraphLabel(second_current_graph));

      combine.add(graph_info_A);
      combine.add(graph_info_B);
      combine.add(new JSeparator());
      combine.add(sum_graphs);
      combine.add(diff_graphs);
      combine.add(product_graphs);
      combine.add(ratio_graphs);
    } else {
      combine.setEnabled(false);
    }

    the_popup.add(combine);
  }

    public void popupNotify(JPopupMenu popup, TierLabelManager handler) {
      // This class was orignially written to implement ContextualPopupListener
      // for left-click on the tier handles.
      // This routine adapts it to also work as a TierLabelManager.PopupListener
      // for left-click on the TierLabelGlyph's

      List labels = handler.getSelectedTierLabels();
      List graph_glyphs = handler.getContainedGraphs(labels);

      List graph_syms = new ArrayList(graph_glyphs.size());
      for (int i=0; i<graph_glyphs.size(); i++) {
        GraphGlyph glyph = (GraphGlyph) graph_glyphs.get(i);
        graph_syms.add(glyph.getInfo()); // It will be a GraphSym object
      }
      GraphSym primary_sym = null;
      if (! graph_syms.isEmpty()) {
        primary_sym = (GraphSym) graph_syms.get(0);
      }

      this.popupNotify(popup, graph_syms, primary_sym);
    }
}


