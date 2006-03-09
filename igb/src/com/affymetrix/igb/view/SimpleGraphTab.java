/**
*   Copyright (c) 2006 Affymetrix, Inc.
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

import com.affymetrix.genometry.AnnotatedBioSeq;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.event.SeqSelectionEvent;
import com.affymetrix.igb.event.SeqSelectionListener;
import com.affymetrix.igb.event.SymSelectionEvent;
import com.affymetrix.igb.event.SymSelectionListener;
import com.affymetrix.igb.genometry.GraphSym;
import com.affymetrix.igb.genometry.SingletonGenometryModel;
import com.affymetrix.igb.glyph.GraphGlyph;
import com.affymetrix.igb.glyph.GraphScoreThreshSetter;
import com.affymetrix.igb.glyph.GraphVisibleBoundsSetter;
import com.affymetrix.igb.glyph.HeatMap;
import com.affymetrix.igb.glyph.SmartGraphGlyph;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
// import org.jdesktop.layout.*;

public class SimpleGraphTab extends JPanel
implements SeqSelectionListener, SymSelectionListener {

  SeqMapView gviewer = null;
  AnnotatedBioSeq current_seq;
  SingletonGenometryModel gmodel;
  
  boolean is_listening = true; // used to turn on and off listening to GUI events

  GraphScoreThreshSetter score_thresh_adjuster;  
  GraphVisibleBoundsSetter vis_bounds_setter;

  // Whether to use this tab or not
  public static boolean USE_SIMPLE_GRAPH_TAB = true;

  boolean DEBUG_EVENTS = false;

  JLabel selected_graphs_label = new JLabel("No Graphs Selected");
  JRadioButton mmavgB = new JRadioButton("Min/Max/Avg");
  JRadioButton lineB = new JRadioButton("Line");
  JRadioButton barB = new JRadioButton("Bar");
  JRadioButton dotB = new JRadioButton("Dot");
  JRadioButton sstepB = new JRadioButton("Stairstep");
  JRadioButton hmapB = new JRadioButton("Heat Map");
  JRadioButton hidden_styleB = new JRadioButton("No Selectoin"); // this button will not be displayed
  ButtonGroup stylegroup = new ButtonGroup();
  
  JComboBox heat_mapCB;

  JSlider height_slider = new JSlider(JSlider.HORIZONTAL, 10, 500, 50);
  
  JButton selectAllB = new JButton("Select All");
  JButton resetB = new JButton("Reset Appearance");
  JButton advB = new JButton("Advanced...");
  JButton threshB = new JButton("Thresholding...");

  public SimpleGraphTab() {
    this(IGB.getSingletonIGB());
  }

  public SimpleGraphTab(IGB igb) {
    if (igb == null) {
      this.gviewer = new SeqMapView(); // for testing only
    } else {
      this.gviewer = igb.getMapView();
    }

    this.setLayout(new BorderLayout());

    Vector v = new Vector(8);
    v.add(HeatMap.HEATMAP_0);
    v.add(HeatMap.HEATMAP_1);
    v.add(HeatMap.HEATMAP_2);
    v.add(HeatMap.HEATMAP_3);
    v.add(HeatMap.HEATMAP_4);
    v.add(HeatMap.HEATMAP_T_0);
    v.add(HeatMap.HEATMAP_T_1);
    v.add(HeatMap.HEATMAP_T_2);
    v.add(HeatMap.HEATMAP_T_3);
    heat_mapCB = new JComboBox(v);
    heat_mapCB.addItemListener(new HeatMapItemListener());

    // A box to contain the heat-map JComboBox, to help get the alignment right
    Box heat_mapCB_box = Box.createHorizontalBox();
    heat_mapCB_box.setAlignmentX(0.0f);
    heat_mapCB_box.add(Box.createHorizontalStrut(16));
    heat_mapCB_box.add(heat_mapCB);
    heat_mapCB_box.add(Box.createHorizontalGlue());
    heat_mapCB_box.setMaximumSize(heat_mapCB_box.getPreferredSize());
    
    Box stylebox = Box.createVerticalBox();
    stylebox.setAlignmentX(1.0f);
    stylebox.add(barB);
    stylebox.add(dotB);
    stylebox.add(lineB);
    stylebox.add(mmavgB);
    stylebox.add(sstepB);
    stylebox.add(hmapB);
    stylebox.add(heat_mapCB_box);

    barB.addActionListener(new GraphStyleSetter(SmartGraphGlyph.BAR_GRAPH));
    dotB.addActionListener(new GraphStyleSetter(SmartGraphGlyph.DOT_GRAPH));
    hmapB.addActionListener(new GraphStyleSetter(SmartGraphGlyph.HEAT_MAP));
    lineB.addActionListener(new GraphStyleSetter(SmartGraphGlyph.LINE_GRAPH));
    mmavgB.addActionListener(new GraphStyleSetter(SmartGraphGlyph.MINMAXAVG));
    sstepB.addActionListener(new GraphStyleSetter(SmartGraphGlyph.STAIRSTEP_GRAPH));

    stylegroup.add(barB);
    stylegroup.add(dotB);
    stylegroup.add(hmapB);
    stylegroup.add(lineB);
    stylegroup.add(mmavgB);
    stylegroup.add(sstepB);
    stylegroup.add(hidden_styleB); // invisible button
    stylebox.setBorder(new TitledBorder("Style"));

    hidden_styleB.setSelected(true); // deselect all visible radio buttons

    if (gviewer == null) {
      vis_bounds_setter = new GraphVisibleBoundsSetter(null);
    } else {
      vis_bounds_setter = new GraphVisibleBoundsSetter(gviewer.getSeqMap());
    }
    score_thresh_adjuster = new GraphScoreThreshSetter(gviewer, vis_bounds_setter);

    Box scalebox = Box.createVerticalBox();
    //    scalebox.setBorder(new TitledBorder("Graph Scaling"));
    scalebox.setBorder(new TitledBorder("Y-axis Scale"));
    scalebox.add(vis_bounds_setter);    
    height_slider.setBorder(new TitledBorder("Height"));
    scalebox.add(height_slider);
    
    height_slider.addChangeListener(new GraphHeightSetter());
    
    Box butbox = Box.createHorizontalBox();
    butbox.add(Box.createHorizontalGlue());
    butbox.add(selectAllB);
    butbox.add(Box.createHorizontalStrut(5));
    butbox.add(resetB);
    butbox.add(Box.createHorizontalStrut(5));
    butbox.add(advB);
    butbox.add(Box.createHorizontalStrut(5));
    butbox.add(threshB);
    butbox.add(Box.createHorizontalGlue());
    
    selectAllB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gviewer != null) { gviewer.selectAllGraphs(); }
      }
    });
    
    threshB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showGraphScoreThreshSetter();
      }
    });

    this.add(selected_graphs_label, "North");
    this.add(stylebox, "West");
    this.add(scalebox, "Center");
    this.add(butbox, "South");

    setSeqMapView(this.gviewer); // called for the side-effects

    gmodel = SingletonGenometryModel.getGenometryModel();
    gmodel.addSeqSelectionListener(this);
    gmodel.addSymSelectionListener(this);
  }

  void showGraphScoreThreshSetter() {
    score_thresh_adjuster.showFrame();
  }
  
  void setSeqMapView(SeqMapView smv) {
    this.gviewer = smv;
  }

  void enableButtons(ButtonGroup g, boolean b) {
    Enumeration e = g.getElements();
    while (e.hasMoreElements()) {
      AbstractButton but = (AbstractButton) e.nextElement();
      but.setEnabled(b);
    }
  }

  HeatMap getCommonHeatMap() {
    // Take the first glyph in the list as a prototype
    SmartGraphGlyph first_glyph = null;
    int graph_style = -1;
    HeatMap hm = null;
    if (! glyphs.isEmpty()) {
      first_glyph = (SmartGraphGlyph) glyphs.get(0);
      graph_style = first_glyph.getGraphStyle();
      hm = first_glyph.getHeatMap();
    }

    // Now loop through other glyphs if there are more than one
    // and see if the graph_style and heatmap are the same in all selections
    int num_glyphs = glyphs.size();
    for (int i=1; i < num_glyphs; i++) {
      SmartGraphGlyph gl = (SmartGraphGlyph) glyphs.get(i);
      if (first_glyph.getGraphStyle() != gl.getGraphStyle()) {
        graph_style = -1;
      }
      if (first_glyph.getHeatMap() != gl.getHeatMap()) {
        hm = null;
      }
    }
    return hm;
  }

  java.util.List grafs = new ArrayList();
  java.util.List glyphs = new ArrayList();
  
  public void symSelectionChanged(SymSelectionEvent evt) {
    if (DEBUG_EVENTS) {
      System.out.println("SymSelectionEvent received by " + this.getClass().getName());
    }
    Object src = evt.getSource();
    // if selection event originally came from here, then ignore it...
    if (src == this) { return; }

    is_listening = false; // turn off propagation of events from the GUI while we modify the settings
    
    java.util.List selected_syms = evt.getSelectedSyms();
    int symcount = selected_syms.size();

    grafs.clear();
    glyphs.clear();

    // First loop through and collect graphs and glyphs, discard any that are not SmartGraphGlyph's
    for (int i=0; i<symcount; i++) {
      if (selected_syms.get(i) instanceof GraphSym) {
        GraphSym graf = (GraphSym) selected_syms.get(i);
        grafs.add(graf);
        GraphGlyph gl = (GraphGlyph) gviewer.getSeqMap().getItem(graf);
        if (gl != null) {
          glyphs.add(gl);
          if (gl instanceof SmartGraphGlyph) {
            SmartGraphGlyph sggl = (SmartGraphGlyph) gl;
          }
        }
      }
    }

    int num_glyphs = glyphs.size();
    double the_height = -1; // -1 indicates unknown height

    // Take the first glyph in the list as a prototype
    SmartGraphGlyph first_glyph = null;
    int graph_style = -1;
    HeatMap hm = null;
    if (! glyphs.isEmpty()) {
      first_glyph = (SmartGraphGlyph) glyphs.get(0);
      graph_style = first_glyph.getGraphStyle();
      if (graph_style == GraphGlyph.HEAT_MAP) {
        hm = first_glyph.getHeatMap();
      }
      the_height = first_glyph.getGraphState().getGraphHeight();
    }

    // Now loop through other glyphs if there are more than one
    // and see if the graph_style and heatmap are the same in all selections
    for (int i=1; i < num_glyphs; i++) {
      SmartGraphGlyph gl = (SmartGraphGlyph) glyphs.get(i);
      if (first_glyph.getGraphStyle() != gl.getGraphStyle()) {
        graph_style = -1;
      }
      if (graph_style == GraphGlyph.HEAT_MAP) {
        if (first_glyph.getHeatMap() != gl.getHeatMap()) {
          hm = null;
        }
      } else {
        hm = null;
      }
    }

    if (num_glyphs == 0) {
      selected_graphs_label.setText("No graphs selected");
    } else if (num_glyphs == 1) {
      GraphSym graf_0 =(GraphSym) grafs.get(0);
      selected_graphs_label.setText(graf_0.getGraphName());
    } else {
      selected_graphs_label.setText(num_glyphs + " graphs selected");
    }

    switch(graph_style) {
      case SmartGraphGlyph.MINMAXAVG:
        mmavgB.setSelected(true);
        break;
      case GraphGlyph.LINE_GRAPH:
        lineB.setSelected(true);
        break;
      case GraphGlyph.BAR_GRAPH:
        barB.setSelected(true);
        break;
      case GraphGlyph.DOT_GRAPH:
        dotB.setSelected(true);
        break;
      case GraphGlyph.HEAT_MAP:
        hmapB.setSelected(true);
        break;
      case GraphGlyph.STAIRSTEP_GRAPH:
        sstepB.setSelected(true);
        break;
      default:
        hidden_styleB.setSelected(true);
        break;
    }

    if (graph_style == GraphGlyph.HEAT_MAP) {
      heat_mapCB.setEnabled(true);
      if (hm == null) {
        heat_mapCB.setSelectedIndex(-1);
      } else {
        heat_mapCB.setSelectedItem(hm.getName());
      }
    } else {
      heat_mapCB.setEnabled(false);
    }

    if (the_height != -1) {
      height_slider.setValue((int) the_height);
    }
    vis_bounds_setter.setGraphs(glyphs);
    score_thresh_adjuster.setGraphs(glyphs);
    
    boolean b = ! (grafs.isEmpty());
    height_slider.setEnabled(b);
    resetB.setEnabled(b);
    advB.setEnabled(b);
    threshB.setEnabled(true);
    enableButtons(stylegroup, b);
    
    is_listening = true; // turn back on GUI events
  }

  public void seqSelectionChanged(SeqSelectionEvent evt) {
    if (DEBUG_EVENTS)  {
      System.out.println("SeqSelectionEvent, selected seq: " + evt.getSelectedSeq() + " recieved by " + this.getClass().getName());
    }
    AnnotatedBioSeq newseq = evt.getSelectedSeq();
    if (newseq != current_seq) {
      current_seq = newseq;
      java.util.List selected_syms = gviewer.getSelectedSyms();
      SymSelectionEvent newevt = new SymSelectionEvent(gviewer, selected_syms);
      symSelectionChanged(newevt);
    }
  }

  public static void main(String[] args) {
    SimpleGraphTab graph_tab = new SimpleGraphTab();
    JFrame fr = new JFrame();
    fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Container cpan = fr.getContentPane();
    cpan.add(graph_tab);
    fr.pack();
    fr.show();
  }

  class GraphStyleSetter implements ActionListener {

    int style = 0;

    public GraphStyleSetter(int style) {
      this.style = style;
    }

    public void actionPerformed(ActionEvent event) {
      if (DEBUG_EVENTS) {
        System.out.println(this.getClass().getName() + " got an ActionEvent: " + event);
      }
      if (gviewer == null || glyphs.isEmpty() || ! is_listening) {
        return;
      }

      Runnable r = new Runnable() {
        public void run() {
          SmartGraphGlyph first_glyph = (SmartGraphGlyph) glyphs.get(0);
          if (style == GraphGlyph.HEAT_MAP) {
            // set to heat map FIRST so that getHeatMap() below will return default map instead of null
            first_glyph.setGraphStyle(GraphGlyph.HEAT_MAP);
          }
          HeatMap hm = ((SmartGraphGlyph) glyphs.get(0)).getHeatMap();
          for (int i=0; i<grafs.size(); i++) {
            SmartGraphGlyph sggl = (SmartGraphGlyph) glyphs.get(i);
            sggl.setShowGraph(true);
            sggl.setGraphStyle(style); // leave the heat map whatever it was
            if ((style == GraphGlyph.HEAT_MAP) && (hm != sggl.getHeatMap())) {
              hm = null;
            }
          }
          if (style == GraphGlyph.HEAT_MAP) {
            heat_mapCB.setEnabled(true);
            if (hm == null) {
              heat_mapCB.setSelectedIndex(-1);
            } else {
              heat_mapCB.setSelectedItem(hm.getName());
            }
          } else {
            heat_mapCB.setEnabled(false);
            // don't bother to change the displayed heat map name
          }
          gviewer.getSeqMap().updateWidget();
        }
      };

      SwingUtilities.invokeLater(r);
    }
  }

  class HeatMapItemListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (gviewer == null || glyphs.isEmpty() || ! is_listening) {
        return;
      }

      if (e.getStateChange() == ItemEvent.SELECTED) {
        String name = (String) e.getItem();
        HeatMap hm = HeatMap.getStandardHeatMap(name);

        if (hm != null) {
          for (int i=0; i<glyphs.size(); i++) {
            GraphGlyph gl = (GraphGlyph) glyphs.get(i);
            gl.setShowGraph(true);
            gl.setGraphStyle(GraphGlyph.HEAT_MAP);
            gl.setHeatMap(hm);
          }
          gviewer.getSeqMap().updateWidget();
        }
      }
    }
  }
  
  class GraphHeightSetter implements  ChangeListener {
    public void stateChanged(ChangeEvent e) {
      if (gviewer == null || glyphs.isEmpty() || ! is_listening) {
        return;
      }
      
      JSlider source = (JSlider) e.getSource();
      if (source.getValueIsAdjusting()) {
        setTheHeights((double) height_slider.getValue());
      }
    }
    
    void setTheHeights(double height) {
      if (gviewer == null) { 
        return; // for testing
      }
      for (int i=0; i<glyphs.size(); i++) {
        SmartGraphGlyph gl = (SmartGraphGlyph) glyphs.get(i);
        gl.getGraphState().setGraphHeight(height);
      }
      gviewer.setAnnotatedSeq(gmodel.getSelectedSeq(), true, true);
    }
  }  
}
