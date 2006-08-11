/**
*   Copyright (c) 2001-2006 Affymetrix, Inc.
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

package com.affymetrix.igb.util;

import java.awt.*;

import com.affymetrix.genoviz.bioviews.*;
import com.affymetrix.genometry.*;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.glyph.*;
import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.tiers.*;
import java.util.prefs.Preferences;

public class GraphGlyphUtils {
  public static final boolean DEBUG = false;

  public static final String PREF_USE_FLOATING_GRAPHS = "use floating graphs";
  public static final String PREF_ATTACHED_COORD_HEIGHT = "default attached graph coord height";

  /** Pref for whether newly-constructed graph glyphs should only show a
   *  limited range of values.
   */
  public static final String PREF_APPLY_PERCENTAGE_FILTER = "apply graph percentage filter";
  public static final boolean default_apply_percentage_filter = true;

  /** Whether to use a TransformTierGlyph to maintain a fixed pixel height for attached graphs. */
  static final boolean use_fixed_pixel_height = false;

  /** Default value for height of attached (non-floating) graphs.  Although
   *  the height will ultimately have to be expressed as a double rather than
   *  an integer, there is no good reason to bother the users with that detail,
   *  so the default should be treated as an integer.
   */
  public static final int default_coord_height = 100;
  public static final boolean default_use_floating_graphs = false;

  public static final Color[] default_graph_colors =
      new Color[] {Color.CYAN, Color.PINK, Color.ORANGE, Color.YELLOW, Color.RED, Color.GREEN};

  /** The names of preferences for storing default graph colors can be
   *  constructed from this prefix by adding "0", "1", etc., up to
   *  default_graph_colors.length - 1.
   */
  public static final String PREF_GRAPH_COLOR_PREFIX = "graph color ";



  /**
   *  Checks to make sure the the boundaries of a floating glyph are
   *  inside the map view.
   *  See {@link checkPixelBounds(GraphGlyph, AffyTieredMap)}.
   */
  public static boolean checkPixelBounds(GraphGlyph gl, SeqMapView gviewer) {
    AffyTieredMap map = (AffyTieredMap)gviewer.getSeqMap();
    return checkPixelBounds(gl, map);
  }

  /**
   *  Checks to make sure the the boundaries of a floating glyph are
   *  inside the map view.
   *  Return true if graph coords were changed, false otherwise.
   *  If the glyph is not a floating glyph, this will have no effect on it
   *  and will return false.
   *  Assumes that graph glyph is a child of a PixelFloaterGlyph, so that
   *   the glyph's coord box is also it's pixel box.
   */
  public static boolean checkPixelBounds(GraphGlyph gl, AffyTieredMap map) {
    boolean changed_coords = false;
    if (gl.getGraphState().getFloatGraph() == true) {
      Rectangle mapbox = map.getView().getPixelBox();
      Rectangle2D gbox = gl.getCoordBox();
      if (gbox.y < mapbox.y) {
        gl.setCoords(gbox.x, mapbox.y, gbox.width, gbox.height);
        //      System.out.println("adjusting graph coords + : " + gl.getCoordBox());
        changed_coords = true;
      }
      else if (gbox.y > (mapbox.y + mapbox.height - 10)) {
        gl.setCoords(gbox.x, mapbox.y + mapbox.height - 10, gbox.width, gbox.height);
        //      System.out.println("adjusting graph coords - : " + gl.getCoordBox());
        changed_coords = true;
      }
    }
    return changed_coords;
  }

  public static boolean hasFloatingAncestor(GlyphI gl) {
    if (gl == null)  { return false; }
    if (gl instanceof PixelFloaterGlyph) { return true; }
    else if (gl.getParent() == null) { return false; }
    else { return hasFloatingAncestor(gl.getParent()); }
  }

  /**
   *  very preliminary start on making MultiGraphs
   */
  public static GraphGlyph displayMultiGraph(java.util.List grafs,
					     AnnotatedBioSeq aseq, AffyTieredMap map,
					     java.util.List cols, double graph_yloc, double graph_height,
					     boolean use_floater) {
    System.out.println("trying to make SmartGraphGlyph for sliding window stats");
    MultiGraph multi_graph_glyph = new MultiGraph();
    multi_graph_glyph.setBackgroundColor(Color.white);
    multi_graph_glyph.setForegroundColor(Color.white);
    multi_graph_glyph.setColor(Color.white);

    Rectangle2D mapbox = map.getCoordBounds();
    multi_graph_glyph.setCoords(mapbox.x, graph_yloc, mapbox.width, graph_height);
    float maxy = Float.NEGATIVE_INFINITY;
    float miny = Float.POSITIVE_INFINITY;

    for (int i=0; i<grafs.size(); i++) {
      GraphSym graf = (GraphSym)grafs.get(i);
      GraphState gstate = GraphState.getTemporaryGraphState();
      SmartGraphGlyph graph_glyph = new SmartGraphGlyph(graf.getGraphXCoords(), graf.getGraphYCoords(), gstate);
      // graph_glyph.setFasterDraw(true);
      // graph_glyph.setCalcCache(true);
      graph_glyph.setSelectable(false);
      gstate.getTierStyle().setHumanName(graf.getGraphName());
      graph_glyph.setXPixelOffset(i);

      //BioSeq graph_seq = graf.getGraphSeq();
      // graph_glyph.setPointCoords(graf.getGraphXCoords(), graf.getGraphYCoords());

      System.out.println("graf name: " + graf.getGraphName());
      graph_glyph.setGraphStyle(SmartGraphGlyph.MINMAXAVG);

      //    Color col = Color.yellow;
      Color col = (Color)cols.get(i);
      graph_glyph.setColor(col);
      map.setDataModel(graph_glyph, graf);

      System.out.println("Map Bounds: " + mapbox);
      graph_glyph.setCoords(mapbox.x, graph_yloc, mapbox.width, graph_height);
      maxy = Math.max(graph_glyph.getVisibleMaxY(), maxy);
      miny = Math.min(graph_glyph.getVisibleMinY(), miny);
      System.out.println("Graph Bounds: " + graph_glyph.getCoordBox());
      multi_graph_glyph.addGraph(graph_glyph);
    }

    //java.util.List glyphs = multi_graph_glyph.getGraphs();
    multi_graph_glyph.setVisibleMinY(0);
    //    multi_graph_glyph.setVisibleMinY(miny);
    multi_graph_glyph.setVisibleMaxY(maxy);

    PixelFloaterGlyph floater = new PixelFloaterGlyph();
    floater.addChild(multi_graph_glyph);
    map.addItem(floater);

    map.updateWidget();
    return multi_graph_glyph;
  }

  public static Preferences getGraphPrefsNode() {
    return UnibrowPrefsUtil.getTopNode().node("graphs");
  }

  /**
   * @deprecated
   */
  public static Color getDefaultGraphColor(int i) {
    int index = (i % default_graph_colors.length);
    String color_pref_name = PREF_GRAPH_COLOR_PREFIX + index;
    Color col = UnibrowPrefsUtil.getColor(getGraphPrefsNode(), color_pref_name, default_graph_colors[index]);
    return col;
  }

}
