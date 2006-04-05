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

package com.affymetrix.igb.genometry;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.AnnotatedBioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.igb.glyph.GraphGlyph;
import com.affymetrix.igb.glyph.GraphState;
import com.affymetrix.igb.util.GraphSymUtils;

/**
 *  A SeqSymmetry for holding graph data.
 */
public class GraphSym extends SimpleSymWithProps implements Cloneable {
  int xcoords[];
  float ycoords[];
  BioSeq graph_original_seq;
  GraphState state;
  String gid;

 /** Property name that can be used to set/get the strand this graph corresponds to.
   *  The property value should be a Character, equal to '+', '-' or '.'.
   */
  public static final String PROP_GRAPH_STRAND = "Graph Strand";

  public Object clone() throws CloneNotSupportedException {
    GraphSym newsym = (GraphSym)super.clone();
    newsym.setGraphName(this.getGraphName() + ":clone");
    //    newsym.setID(this.getID() + ":clone");
    newsym.gid = GraphSymUtils.getUniqueGraphID(this.getID() + ":clone", (AnnotatedBioSeq)this.getGraphSeq());
    newsym.setGraphState(new GraphState(state));
    return newsym;
  }

  /** add a constructor to explicitly set span? */
  //  this would be for slices, which need a span that expresses the bounds of the slice
  //     (which will often be slightly bigger than the xcoord min and max)
  //  public GraphSym(int[] x, float[] y, String name, BioSeq seq, SeqSpan span) {

  public GraphSym(int[] x, float[] y, String id, BioSeq seq) {
    super();
    this.graph_original_seq = seq;
    this.state = new GraphState();
    SeqSpan span = new SimpleSeqSpan(0, seq.getLength(), seq);
    this.addSpan(span);
    this.xcoords = x;
    this.ycoords = y;
    this.gid = id;
    //    System.out.println("GraphSym created, id = " + gid);
    //    setID(id);
    //    setGraphName(name);
  }

  public void setGraphName(String name) {
    //    System.out.println("called GraphSym.setGraphName(): " + name);
    state.setLabel(name);
    setProperty("name", name);
  }

  public String getGraphName() {
    //    System.out.println("called GraphSym.getGraphName()");
    String gname = state.getLabel();
    if (gname == null) {
      gname = this.getID();
    }
    //    return state.getLabel();
    return gname;
  }

  public String getID() {
    //    System.out.println("called GraphSym.getID()");
    return gid;
  }

  /**
   *  Not allowed to call GraphSym.setID(), id
   */
  public void setID(String id) {
    throw new RuntimeException("Attempted to call GraphSym.setID(), but not allowed to modify GraphSym id!");
  }

  public int getPointCount() {
    if (xcoords == null) { return 0; }
    else { return xcoords.length; }
  }

  public int[] getGraphXCoords() {
    return xcoords;
  }

  public float[] getGraphYCoords() {
    return ycoords;
  }

  /**
   *  Get the seq that the graph's xcoords are specified in
   */
  public BioSeq getGraphSeq() {
    return graph_original_seq;
  }

  /**
   *  Returns the graph state.  Will never be null.
   */
  public GraphState getGraphState() {
    return state;
  }

  /** Sets the graph state.  This will get rid of any previous state settings,
   *  including the name.
   */
  public void setGraphState(GraphState state) {
    if (state == null) {
      throw new NullPointerException();
    }
    this.state = state;
  }

  /**
   *  Overriding request for property "method" to return graph name.
   */
  public Object getProperty(String key) {
    if (key.equals("method")) {
      return getGraphName();
    }
    else if (key.equals("id")) {
      return this.getID();
    }
    else {
      return super.getProperty(key);
    }
  }

  public boolean setProperty(String name, Object val) {
    if (name.equals("id")) {
      this.setID(name);
      return false;
    }
    else {
      return super.setProperty(name, val);
    }
  }




  //  List thresh_names = null;
  //  FloatList thresh_vals = null;

  /*
  public void addStoredThreshold(String thresh_name, float score_thresh) {
    if (thresh_names == null) { thresh_names = new ArrayList(); }
    if (thresh_vals == null) { thresh_vals = new FloatList(); }
    thresh_names.add(thresh_name);
    thresh_vals.add(score_thresh);
  }

  public int getStoredThreshCount() {
    if (thresh_vals == null) { return 0; }
    return thresh_vals.size();
  }

  public String getStoredThreshName(int i) {
    if (thresh_names == null) { return null; }
    return (String)thresh_names.get(i);
  }

  public float getStoredThreshValue(int i) {
    if (thresh_vals == null) { return Float.NEGATIVE_INFINITY; }
    return thresh_vals.get(i);
  }
  */

}
