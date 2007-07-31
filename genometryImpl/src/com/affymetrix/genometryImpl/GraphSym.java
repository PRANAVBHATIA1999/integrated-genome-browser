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

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.GraphStateI;

/**
 *  A SeqSymmetry for holding graph data.
 */
public abstract class GraphSym extends SimpleSymWithProps {

  /** A property that can optionally be set to give a hint about the graph strand for display. */
  public static final String PROP_GRAPH_STRAND = "Graph Strand";
  public static final Integer GRAPH_STRAND_PLUS = new Integer(1);
  public static final Integer GRAPH_STRAND_MINUS = new Integer(-1);
  public static final Integer GRAPH_STRAND_BOTH = new Integer(2);
  public static final Integer GRAPH_STRAND_NEITHER = new Integer(0);
  
  int xcoords[];
  Object ycoords; // should be an array of float or int, etc.
  BioSeq graph_original_seq;
  String gid;

  /**
   *  id_locked is a temporary fix to allow graph id to be changed after construction, 
   *  but then lock once lockID() is called.
   *  Really want to forbid setting id except in constructor, but currently some code 
   *    needs to modify this after construction, but before adding as annotation to graph_original_seq
   */
  boolean id_locked = false;

  protected GraphSym(int[] x, Object y, String id, BioSeq seq) {
    super();
    this.graph_original_seq = seq;

    SeqSpan span = new SimpleSeqSpan(0, seq.getLength(), seq);
    this.addSpan(span);
    this.xcoords = x;
    this.ycoords = y;
    this.gid = id;
  }

  public void lockID() {
    id_locked = true;
  }

  public void setGraphName(String name) {
    getGraphState().getTierStyle().setHumanName(name);
    setProperty("name", name);
  }

  public String getGraphName() {
    String gname = getGraphState().getTierStyle().getHumanName();
    if (gname == null) {
      gname = this.getID();
    }
    return gname;
  }

  public String getID() {
    return gid;
  }

  /**
   *  Not allowed to call GraphSym.setID(), id
   */
  public void setID(String id) {
    if (id_locked) {
      System.out.println("%%%%%%% WARNING: called GraphSym.setID(), not allowed!");
      //      SmartAnnotBioSeq sab = (SmartAnnotBioSeq)getGraphSeq();
      //      System.out.println("   seq = " + sab.getID() + ", group = " + sab.getSeqGroup().getID());
      System.out.println("    old id: " + this.getID());
      System.out.println("    new id: " + id);
    }
    else {
      gid = id;
    }
    //    throw new RuntimeException("Attempted to call GraphSym.setID(), but not allowed to modify GraphSym id!");
  }

  public int getPointCount() {
    if (xcoords == null) { return 0; }
    else { return xcoords.length; }
  }

  public int[] getGraphXCoords() {
    return xcoords;
  }
  
  public abstract float getGraphYCoord(int i);

  public abstract String getGraphYCoordString(int i);

  /** Returns a copy of the graph Y coordinates as a float[], even if the Y coordinates
   *  were originally specified as non-floats.
   */
  public abstract float[] copyGraphYCoords();
  

  /**
   *  Get the seq that the graph's xcoords are specified in
   */
  public BioSeq getGraphSeq() {
    return graph_original_seq;
  }

  /**
   *  Returns the graph state.  Will never be null.
   */
  public GraphStateI getGraphState() {
    GraphStateI state = DefaultStateProvider.getGlobalStateProvider().getGraphState(this.gid);
    return state;
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
}
