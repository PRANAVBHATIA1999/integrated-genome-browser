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
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import com.affymetrix.genoviz.bioviews.*;
import com.affymetrix.genoviz.glyph.*;
import com.affymetrix.genoviz.widget.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.*;

import com.affymetrix.igb.tiers.*;
import com.affymetrix.igb.event.*;
import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.glyph.*;
import com.affymetrix.igb.parsers.*;
import com.affymetrix.igb.IGB;

public class ExperimentPivotView extends JComponent
      implements SymSelectionListener, ActionListener, AnnotatedSeqViewer, SeqSelectionListener
{

  static String BLANK = "";
  static String LINE = "Line";
  static String STAIRSTEP = "Bar";  // really stairstep for now...
  static String HEATMAP = "Heat Map";
  static String HEATMAP1 = "Violet Heat Map";
  static String HEATMAP2 = "Blue/Yellow Heat Map";
  static String HEATMAP3 = "Red/Green Heat Map";
  static String HEATMAP4 = "Blue/Yellow Heat Map2";
  static String TOTAL_MIN_MAX = "Total Min/Max";
  static String ROW_MIN_MAX = "Row Min/Max";
  static String COLUMN_MIN_MAX = "Column Min/Max";
  static Map string2style;
  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
  static Color[] BLUE_YELLOW_HEATMAP;
  static Color[] BLUE_YELLOW_HEATMAP_2;
  static Color[] RED_GREEN_HEATMAP;
  static Color[] VIOLET_HEATMAP;

  int numscores = 0;
  int score_spacing = 10;
  int graph_xoffset = 2;
  float overall_score_min = Float.POSITIVE_INFINITY;
  float overall_score_max = Float.NEGATIVE_INFINITY;
  Color[] current_heatmap = VIOLET_HEATMAP;

  AffyTieredMultiMap map;
  TierLabelManager tier_manager;  
  AnnotatedBioSeq currentSeq;  // current annotated seq
  java.util.List experiment_graphs = new ArrayList();
  //  int experiment_style = GraphGlyph.LINE_GRAPH;
  //  int experiment_style = GraphGlyph.STAIRSTEP_GRAPH;
  int experiment_style = GraphGlyph.HEAT_MAP;
  String experiment_scaling = TOTAL_MIN_MAX;

  JComboBox styleCB = new JComboBox();
  JComboBox scaleCB = new JComboBox();

  Color[] tcolors = { (new Color(0, 0, 0)), new Color(20, 20, 20) };

  static {
    int red, green, blue;
    int bins = 256;

    BLUE_YELLOW_HEATMAP = new Color[bins];
    red = 0;
    green = 0;
    blue = 255;
    for (int i=0; i<bins; i++) {
      Color col = new Color(red, green, blue);
      BLUE_YELLOW_HEATMAP[i] = col;
      blue--;
      red++;
      green++;
      //      if (i % 2 == 0) { red++; }
      //      else { green++; }
    }

    BLUE_YELLOW_HEATMAP_2 = new Color[bins];
    red = 0;
    green = 0;
    blue = 128;
    for (int i=0; i<bins; i++) {
      Color col = new Color(red, green, blue);
      BLUE_YELLOW_HEATMAP_2[i] = col;
      if (i % 2 == 0)  { blue--; }
      red++;
      green++;
      //      if (i % 2 == 0) { red++; }
      //      else { green++; }
    }

    VIOLET_HEATMAP = new Color[bins];
    red = 0;
    green = 0;
    blue = 0;
    for (int i=0; i<bins; i++) {
      Color col = new Color(red, green, blue);
      VIOLET_HEATMAP[i] = col;
      blue++;
      red++;
    }

    RED_GREEN_HEATMAP = new Color[bins];
    red = 255;
    green = 0;
    blue = 0;
    for (int i=0; i<bins; i++) {
      Color col = new Color(red, green, blue);
      RED_GREEN_HEATMAP[i] = col;
      red--;
      green++;
    }

    string2style = new HashMap();
    string2style.put(LINE, new Integer(SmartGraphGlyph.LINE_GRAPH));
    string2style.put(STAIRSTEP, new Integer(SmartGraphGlyph.STAIRSTEP_GRAPH));
    //    string2style.put(HEATMAP, new Integer(SmartGraphGlyph.HEAT_MAP));
    string2style.put(HEATMAP1, VIOLET_HEATMAP);
    string2style.put(HEATMAP2, BLUE_YELLOW_HEATMAP);
    string2style.put(HEATMAP4, BLUE_YELLOW_HEATMAP_2);
    string2style.put(HEATMAP3, RED_GREEN_HEATMAP);
  }


  public ExperimentPivotView() {
    super();
    //    styleCB.addItem(HEATMAP);
    styleCB.addItem(HEATMAP1);
    styleCB.addItem(HEATMAP2);
    styleCB.addItem(HEATMAP4);
    styleCB.addItem(HEATMAP3);
    styleCB.addItem(STAIRSTEP);
    styleCB.addItem(LINE);
    JPanel style_pan = new JPanel();
    style_pan.setLayout(new GridLayout(1, 2));
    style_pan.add(new JLabel("Graph Style: ", JLabel.RIGHT));
    style_pan.add(styleCB);

    scaleCB.addItem(TOTAL_MIN_MAX);
    scaleCB.addItem(ROW_MIN_MAX);
//    scaleCB.addItem(COLUMN_MIN_MAX);    NOT YET IMPLEMENTED
    JPanel scale_pan = new JPanel();
    scale_pan.setLayout(new GridLayout(1, 2));
    scale_pan.add(new JLabel("Graph Scaling: ", JLabel.RIGHT));
    scale_pan.add(scaleCB);

    JButton b = new JButton( new PivotViewExporter( this ) );
    JPanel optionsP = new JPanel();
    optionsP.add(style_pan);
    optionsP.add(scale_pan);
    optionsP.add( b );

    map = new AffyTieredMultiMap();
    map.setBackground(Color.black);
    AffyTieredMap expmap = map.getExtraMap();
    expmap.setBackground(Color.black);
    NeoMap labelmap = map.getLabelMap();
    labelmap.setBackground(Color.black);
    //map.addScroller( map.VERTICAL, map.NORTH ); // for testing...
    map.addScroller( map.HORIZONTAL, map.EAST );

    tier_manager = new TierLabelManager(map);
    ExperimentPivotView.PivotViewPopup pvp = new ExperimentPivotView.PivotViewPopup();
    tier_manager.addPopupListener(pvp);

    this.setLayout(new BorderLayout());
    add("Center", map);
    add("North", optionsP);

    styleCB.addActionListener(this);
    scaleCB.addActionListener(this);
    // setView(Unibrow.getSingletonUnibrow().getMapView());
    gmodel.addSeqSelectionListener(this);
    gmodel.addSymSelectionListener(this);
  }

  public void setExperimentStyle(int style)  {
    setExperimentStyle(style, true);
  }

  public void setExperimentStyle(int style, boolean update_widget) {
    experiment_style = style;
    int graph_count = experiment_graphs.size();
    for (int i=0; i<graph_count; i++) {
      GraphGlyph gl = (GraphGlyph)experiment_graphs.get(i);
      gl.setGraphStyle(experiment_style);
    }
    // need to adjust "extra map" insets (which in turn determine size of experiment graphs
    //    relative to size of extramap tiers), because for rendering graph as a heat map
    //    want the graph to fill the tier it's contained in, but for rendering graph as
    //    a line graph or bar graph then want some spacing between the graphs, so need
    //    graphs to be smaller than the tiers they are contained in
    if (experiment_style == GraphGlyph.HEAT_MAP) {
      map.setExtraMapInset(0);
    }
    else {
      map.setExtraMapInset(1);
    }
    if (update_widget) {
      map.repack();
      map.stretchToFit(false, true);
      map.updateWidget();
    }
  }

  /**
   * Set sequence without preserving selection or view.
   * @param seq to use.
   */
  public void setAnnotatedSeq(AnnotatedBioSeq seq) {
    setAnnotatedSeq(seq, false, false);
  }
  /**
   * Set the current sequence.
   * This is the context for the pivot view.
   * @param seq the new annotated sequence.
   * @param preserve_selection ignored.
   * @param preserve_view ignored.
   */
  public void setAnnotatedSeq(AnnotatedBioSeq seq,
			      boolean preserve_selection,
			      boolean preserve_view) {
    this.currentSeq = seq;
  }
  public AnnotatedBioSeq getAnnotatedSeq() {
    return this.currentSeq;
  }

  private SeqMapView originalView = null;
  private SeqMapView bigPicture;

  /**
   * Points to the big picture.
   */
  protected void setView(SeqMapView theView) {
    assert null != theView;
    this.originalView = theView;
    this.bigPicture = new SeqMapView(false);
    this.bigPicture.setFrame( this.originalView.getFrame() );
    //    this.bigPicture.setGraphStateHash( this.originalView.getGraphStateHash() );
    AffyTieredMap m = this.bigPicture.getSeqMap();
    this.map.setNorthMap( m );
    this.map.updateWidget();
  }

  public void seqSelectionChanged(SeqSelectionEvent evt)  {
    if (IGB.DEBUG_EVENTS)  {
      System.out.println("ExperimentPivotView received SeqSelectionEvent, selected seq: " + evt.getSelectedSeq());
    }
    AnnotatedBioSeq newseq = evt.getSelectedSeq();
    if (gmodel.getSelectedSeq() != newseq ) {
      throw new IllegalStateException( "Event source is not the same as genometry model selected sequence." );
    }
    setAnnotatedSeq(newseq);
  }

  private void resetThisWidget( java.util.List theSyms ) {
    if (this.currentSeq == null) {
      System.err.println("ERROR: ExperimentPivotView.resetThisWidget() called, "  +
			 "but no current annotated seq: " + this.currentSeq );
      return;
    }
    int symcount = theSyms.size();
    map.clearWidget();
    // assume that all syms are on same seq...
    //      SeqSymmetry sym1 = (SeqSymmetry)syms.get(0);

    experiment_graphs = new ArrayList();
    map.setMapRange(0, this.currentSeq.getLength());

    ScoredContainerSym parent = null;  // holds IndexedSyms
    for (int i=0; i<symcount; i++) {
      SeqSymmetry sym = (SeqSymmetry)theSyms.get(i);
      if (sym instanceof IndexedSym) {
        IndexedSym isym = (IndexedSym)sym;
        numscores = isym.getParent().getScoreCount();
        parent = isym.getParent();
        if (numscores < 0) { numscores = 0; }
        break;
      }
    }


    //AffyTieredMap extramap = map.getExtraMap();
    //extramap.setMapRange(0, (numscores * score_spacing));
    map.setExtraMapRange(0, (numscores * score_spacing));
    VerticalGraphLabels extra_labels = new VerticalGraphLabels();
    extra_labels.setForegroundColor(Color.black);
    TierGlyph headerTier = new TierGlyph();
    headerTier.setLabel( "score names" );
    headerTier.addChild( extra_labels );
    map.addNorthEastTier( headerTier );
    String[] scoreNames = new String[numscores];

    if (parent != null) {
      for (int i=0; i<numscores; i++) {
        scoreNames[i] = parent.getScoreName(i);
        extra_labels.addLabel(scoreNames[i]);
      }
      extra_labels.setInfo( scoreNames );
    }
    headerTier.setCoords( 0, 0, ( numscores * score_spacing + 1 ), 11 );
    extra_labels.setCoords( 0, 0, ( numscores * score_spacing ), 10 ); // does something, but what?
    
    overall_score_min = Float.POSITIVE_INFINITY;
    overall_score_max = Float.NEGATIVE_INFINITY;
    double xmin = Double.POSITIVE_INFINITY;
    double xmax = Double.NEGATIVE_INFINITY;

    for (int i=0; i<symcount; i++) {
      TierGlyph mtg = new TierGlyph();
      mtg.setFillColor(tcolors[i % tcolors.length]);
      mtg.setForegroundColor(Color.yellow);
      SeqSymmetry sym = (SeqSymmetry)theSyms.get(i);
      SeqSpan span = sym.getSpan(this.currentSeq);
      if ( null != span ) {
        xmin = Math.min(xmin, span.getMin());
        xmax = Math.max(xmax, span.getMax());
      }
      if (sym instanceof SymWithProps) {
        mtg.setLabel((String)((SymWithProps)sym).getProperty("id"));
      }
      map.addTier(mtg);
      addAnnotGlyph(sym, this.currentSeq, mtg);
      GraphGlyph gr = addGraph(sym, mtg);
      if (gr != null) {
        overall_score_min = Math.min(overall_score_min, gr.getGraphMinY());
        overall_score_max = Math.max(overall_score_max, gr.getGraphMaxY());
      }
    }

    setExperimentScaling(experiment_scaling, false);
    setExperimentStyle(experiment_style, true); // setting the flag to false has weird 
    // This is done in clampToSpan: map.stretchToFit();
    SeqSpan select_span = new SimpleSeqSpan((int)xmin, (int)xmax, this.currentSeq);
    int length = (int)(xmax - xmin);
    SeqSpan zoomto_span = new SimpleSeqSpan((int)(xmin - (0.02 * length)),
                                            (int)(xmax + (0.02 * length)), this.currentSeq);
    zoomTo(zoomto_span);
    clampToSpan(zoomto_span);
  }

  public void symSelectionChanged( SymSelectionEvent theEvent ) {
    java.util.List syms = theEvent.getSelectedSyms();
    if ( ! syms.isEmpty() ) {
      resetThisWidget( syms );
    }
  }

  public void setExperimentScaling(String scaling, boolean update_widget) {
    int graph_count = experiment_graphs.size();
    for (int i=0; i<graph_count; i++) {
      GraphGlyph gr = (GraphGlyph)experiment_graphs.get(i);
      if (scaling == TOTAL_MIN_MAX) {
	gr.setVisibleMinY(overall_score_min);
	gr.setVisibleMaxY(overall_score_max);
      }
      else if (scaling == ROW_MIN_MAX) {
	gr.setVisibleMinY(gr.getGraphMinY());
	gr.setVisibleMaxY(gr.getGraphMaxY());
      }
      else if (scaling == COLUMN_MIN_MAX) {
        // NOT YET IMPLEMENTED
      }
    }
    if (update_widget) {
      map.updateWidget();
    }
  }

  public void setHeatMapColors(Color[] colors, boolean update_widget) {
    int graph_count = experiment_graphs.size();
    for (int i=0; i<graph_count; i++) {
      GraphGlyph gr = (GraphGlyph)experiment_graphs.get(i);
      gr.initHeatMap(colors);
    }
    current_heatmap = colors;
    if (update_widget) {
      map.updateWidget();
    }
  }

  public GraphGlyph addGraph(SeqSymmetry sym, TierGlyph mtg) {
    GraphGlyph gl = null;
    if (sym instanceof IndexedSym) {
      IndexedSym isym = (IndexedSym)sym;

      AffyTieredMap extramap = map.getExtraMap();
      TierGlyph egl = (TierGlyph)extramap.getItem(mtg);
      egl.setFillColor(mtg.getFillColor());

      float[] ifloats = isym.getParent().getChildScores(isym);
      int point_count = ifloats.length;
      int[] xcoords = new int[point_count+1];
      float[] ycoords = new float[point_count+1];
      for (int i=0; i<point_count; i++) {
	xcoords[i] = i*score_spacing + graph_xoffset;
	ycoords[i] = ifloats[i];
      }
      xcoords[point_count] = point_count * score_spacing;
      ycoords[point_count] = 0;
      gl = new GraphGlyph(xcoords, ycoords);
      gl.initHeatMap(current_heatmap);
      gl.setGraphStyle(experiment_style);
      gl.setShowHandle(false);
      gl.setShowBounds(false);
      gl.setShowLabel(false);
      gl.setShowAxis(false);
      // gl.setPointCoords(xcoords, ycoords);
      gl.setColor(new Color(255, 0, 255));
      gl.setCoords(0, 0, 100, 100);
      gl.setInfo( ifloats );
      egl.addChild(gl);
      experiment_graphs.add(gl);
    }
    return gl;
  }

  public GlyphI addAnnotGlyph(SeqSymmetry sym, BioSeq theSeq, GlyphI parent)  {
    int child_count = sym.getChildCount();
    SeqSpan span = sym.getSpan(theSeq);
    if (span == null)  { return null; }
    GlyphI gl = null;
    if (child_count <= 0) {
      gl = new FillRectGlyph();
      gl.setColor(Color.yellow);
      gl.setCoords(span.getMin(), 0, span.getMax() - span.getMin(), 20);
      parent.addChild(gl);
    }
    else if (span != null)  {
      gl = new EfficientLineContGlyph();
      gl.setColor(Color.yellow);
      gl.setCoords(span.getMin(), 0, span.getMax() - span.getMin(), 20);
      parent.addChild(gl);
      for (int i=0; i<child_count; i++) {
	SeqSymmetry child = sym.getChild(i);
	addAnnotGlyph(child, theSeq, gl);
      }
    }
    return gl;
  }

  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if (src == styleCB) {
      String selection = (String)((JComboBox)styleCB).getSelectedItem();
      if (selection != BLANK) {
	Object obj = string2style.get(selection);
	if (obj instanceof Integer) {
	  int style = ((Integer)string2style.get(selection)).intValue();
	  setExperimentStyle(style);
	}
	else if (obj instanceof Color[]) {
	  Color[] colors = (Color[])obj;
	  setHeatMapColors(colors, false);
	  setExperimentStyle(GraphGlyph.HEAT_MAP);
	}
      }
    }
    else if (src == scaleCB) {
      String selection = (String)((JComboBox)scaleCB).getSelectedItem();
      if (selection != BLANK) {
	System.out.println("selected for scaling: " + selection);
	experiment_scaling = selection;
	setExperimentScaling(experiment_scaling, true);
      }
    }
  }

  /**
   * Produce a table for export.
   * The table will have the following columns:
   * label, span start, span end,
   * and a column for each column of graph data (if any).
   * There will be one row for each visible tier, and the rows will be
   * in order based on the order of the tiers.  Hidden tiers are not included. 
   */
  public TableModel getTable() {

    AffyTieredMap atm = this.map.getNorthEastMap();
    TierGlyph tg = atm.getTier( "score names" );
    String[] scoreName;
    if (tg == null) {
      scoreName = new String[0];
      return new DefaultTableModel(0, 0);
    } else {
      GlyphI ggg = tg.getChild( 0 ); // assume only one.
      VerticalGraphLabels vgl = ( VerticalGraphLabels ) ggg;
      scoreName = ( String[] ) ggg.getInfo();
    }
    if (scoreName == null) {
      // even if there are no scores, you can still export the start and end data
      scoreName = new String[0];
    }

    Object[] cols = new String[3 + scoreName.length];
    cols[0] = "";
    cols[1] = "start";
    cols[2] = "end";
    for ( int i = 3; i < cols.length; i++ ) {
      cols[i] = scoreName[i-3];
    }

    Object[][] data;
    java.util.List tiers = this.map.getTiers();
    
    int non_hidden_tiers = 0;
    Iterator iter = map.getTiers().iterator();
    while (iter.hasNext()) {
      if (((TierGlyph) iter.next()).getState() != TierGlyph.HIDDEN) {
        non_hidden_tiers++;
      }
    }
    data = new Object[non_hidden_tiers][cols.length];
    
    // Note that the two iterators must be parallel.
    Iterator it = tiers.iterator();
    Iterator it2 = map.getExtraMap().getTiers().iterator();
    int i = -1;
    while ( it.hasNext() ) {
      if ( ! it2.hasNext() ) {
        throw new RuntimeException("Maps in Pivot View are out-of-sync");
      }
      TierGlyph tg1 = (TierGlyph) it.next();
      TierGlyph tg2 = (TierGlyph) it2.next();
      
      if (tg1.getState() != TierGlyph.HIDDEN) {
        i++;
      
        data[i][0] = tg1.getLabel();
        // Here we assume only one child glyph:
        GlyphI g = tg1.getChild( 0 );
        if ( null != g ) {
          Rectangle2D box = g.getCoordBox();
          data[i][1] = new Double( box.x );
          data[i][2] = new Double( box.x + box.width );
        }
        if ((tg2 != null) && (tg2.getChildCount() > 0))  {
          GlyphI g2 = tg2.getChild(0);
          if ( (null != g2) && (g2.getInfo() instanceof float[])) {
            float[] f = (float[]) g2.getInfo();
            for (int j = 0; j < f.length; j++) {
              data[i][j + 3] = new Float(f[j]);
            }
          }
        }
      }
    }
    AbstractTableModel answer = new DefaultTableModel( data, cols );
    return answer;
  }
  
  
  
  /**
   *  main for testing.
   */
  public static void main(String[] args) throws Exception {
    //    String testgff = System.getProperty("user.dir") + "/exptest.gff";
    String testgff = "c:/data/igb_testdata/exptest.gff";
    MutableAnnotatedBioSeq testseq = null;
    FileInputStream fis = new FileInputStream(new File(testgff));
    GFFParser parser = new GFFParser();
    SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
    AnnotatedSeqGroup seq_group = gmodel.addSeqGroup("Test Group");
    
    java.util.List syms = parser.parse(fis, seq_group, false);
    SeqSymmetry first_sym = (SeqSymmetry) syms.get(0);
    testseq = (MutableAnnotatedBioSeq) first_sym.getSpan(0).getBioSeq();

    JFrame frm = new JFrame( "ExperimentPivotView Test" );
    frm.setDefaultCloseOperation( frm.EXIT_ON_CLOSE );

    Container cpane = frm.getContentPane();
    cpane.setLayout(new BorderLayout());
    ExperimentPivotView epview = new ExperimentPivotView();
    cpane.add("Center", epview);

    frm.setSize(600, 400);
    frm.show();

    ArrayList symlist = new ArrayList();
    int acount = testseq.getAnnotationCount();

    // Make up some scores:
    ScoredContainerSym bucket = new ScoredContainerSym();
    float[] scrsUp = { 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f };
    float[] scrsDn = { 5f, 4f, 3f, 2f, 1f, 0f };
    bucket.addScores( "Ascending", scrsUp );
    bucket.addScores( "Descending", scrsDn );

    for (int i=0; i<acount; i++) {
      SeqSymmetry sym = testseq.getAnnotation(i);
      { // Put IndexedSyms in the sym list instead of the ones from testseq:
        SimpleSymWithProps psym = (SimpleSymWithProps) sym;
        int spansIncluded = sym.getSpanCount();
        if ( 0 < spansIncluded ) {
          SeqSpan span = sym.getSpan( spansIncluded - 1 );
          IndexedSym isym = new IndexedSingletonSym( span.getStart(), span.getEnd(), span.getBioSeq() );
          bucket.addChild( isym );
          sym = isym; // Use this new sym instead.
        }
      }
      symlist.add(sym);
    }

    /*/ More experimentation: Replace the annotations with the new ones in the symlist.
    for ( int i = acount-1; 0 <= i; i-- ) {
      testseq.removeAnnotation( i );
    }
    Iterator it = symlist.iterator();
    while ( it.hasNext() ) {
      Object o = it.next();
      SeqSymmetry s = ( SeqSymmetry ) o;
      testseq.addAnnotation( s );
    }*/

    Collections.sort(symlist, new SeqSymMinComparator(testseq, true));

    gmodel.setSelectedSeq( testseq, epview );

    SymSelectionEvent symevt = new SymSelectionEvent(epview, symlist);
    epview.symSelectionChanged(symevt);
  }


  public void clampToSpan(SeqSpan span) {
    map.setMapRange((int)span.getMin(), (int)span.getMax());
    map.stretchToFit(false, false);
    map.updateWidget();
  }


  public void zoomTo(SeqSpan span) {
    int smin = span.getMin();
    int smax = span.getMax();
    float coord_width = smax - smin;
    float pixel_width = map.getView().getPixelBox().width;
    float pixels_per_coord = pixel_width / coord_width;
    map.zoom(NeoWidgetI.X, pixels_per_coord);
    map.scroll(NeoWidgetI.X, smin);
    map.setZoomBehavior(map.X, map.CONSTRAIN_COORD, (smin + smax)/2);
    map.updateWidget();
  }
  
  class PivotViewPopup implements TierLabelManager.PopupListener {

    TierLabelManager tlh = null;
    
    Action hide_action = new AbstractAction("Hide") {
      public void actionPerformed(ActionEvent e) {
        hideSelectedTiers();
      }
    };
    
    Action show_all_action = new AbstractAction("Show All") {
      public void actionPerformed(ActionEvent e) {
        showAllTiers();
      }
    };
    
    Action sort_action = new AbstractAction("Sort") {
      public void actionPerformed(ActionEvent e) {
        sortTiers();
      }
    };

    PivotViewPopup() {
    }
    
    void hideSelectedTiers() {
      tlh.hideTiers(tlh.getSelectedTierLabels(), false, false);
      tlh = null;
    }
    
    void showAllTiers() {
      tlh.showTiers(tlh.getAllTierLabels(), false, true);
      tlh = null;
    }
    
    void sortTiers() {
      java.util.List all_tiers = tlh.getAllTierLabels();
      Collections.sort(all_tiers, the_comparator);
      tlh.orderTiersByLabels(all_tiers);
      tlh.repackTheTiers(false, false);
      tlh = null;
    }
    
    public void popupNotify(JPopupMenu popup, TierLabelManager handler) {
      this.tlh = handler;
      if (tlh == null) {
        return;
      }
      
      java.util.List selected_labels = handler.getSelectedTierLabels();
      java.util.List all_labels = handler.getAllTierLabels();
      hide_action.setEnabled(! selected_labels.isEmpty());
      show_all_action.setEnabled( ! all_labels.isEmpty() );
      sort_action.setEnabled( all_labels.size() > 1 );
      
      if (popup.getComponentCount() > 0) {
        popup.add(new JSeparator());
      }
      popup.add(hide_action);
      popup.add(show_all_action);
      popup.add(sort_action);
    }

    // A comparator for sorting based on the x-position of the first glyph in a tier
    Comparator the_comparator = new Comparator() {
      public int compare(Object obj1, Object obj2) {
        TierGlyph tg1 = ((TierLabelGlyph) obj1).getReferenceTier();
        TierGlyph tg2 = ((TierLabelGlyph) obj2).getReferenceTier();
        
        if (tg1.getChildCount() == 0 || tg2.getChildCount() == 0) {
          // No tier should be empty, but if so sort by number of items in tiers
          if (tg1.getChildCount() == tg2.getChildCount()) return 0;
          else return (tg1.getChildCount() < tg2.getChildCount() ? -1 : 1);
        }
                
        Rectangle2D box1 = tg1.getChild(0).getCoordBox();
        Rectangle2D box2 = tg2.getChild(0).getCoordBox();        
        if (box1.x < box2.x) { return -1; }
        else if (box1.x > box2.x) { return 1; }
        else { return 0; }
      }
    };
    
  }

}
