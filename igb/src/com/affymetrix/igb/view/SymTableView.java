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

import java.util.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometryImpl.SingletonGenometryModel;
import com.affymetrix.genometryImpl.SymWithProps;
import com.affymetrix.igb.event.*;
import com.affymetrix.genometryImpl.event.*;

public class SymTableView extends PropertySheet implements SymSelectionListener {
  static int testcount = 0;
  Vector default_order;

  public SymTableView() {
    this(true);
  }

  public SymTableView(boolean useDefaultKeystrokes) {
    super(useDefaultKeystrokes);
    setPreferredSize(new java.awt.Dimension(100, 250));
    setMinimumSize(new java.awt.Dimension(100, 250));
    SingletonGenometryModel.getGenometryModel().addSymSelectionListener(this);
    default_order = new Vector(8);
    default_order.add("gene name");
    default_order.add("name");
    default_order.add("id");
    default_order.add("type");
    default_order.add("start");
    default_order.add("end");
    default_order.add("length");
  }
  
  public void setDefaultColumnOrder(List columns) {
    default_order = new Vector(columns);
  }

  public void symSelectionChanged(SymSelectionEvent evt) {
    Object src = evt.getSource();
    // if selection event originally came from here, then ignore it...
    if (src == this) { return; }
    List selected_syms = evt.getSelectedSyms();
    SeqMapView mapView = null;
    if (src instanceof SeqMapView) {
       mapView = (SeqMapView) src;
    }
    showSyms(selected_syms, mapView);
  }
  
  List currentSyms = Collections.EMPTY_LIST;

  public void showSyms(List selected_syms, SeqMapView seqMap) {

    currentSyms = selected_syms;
    
    int symCount = selected_syms.size();
    Vector propvec = new Vector();
    for (int i=0; i<symCount; i++) {
      SeqSymmetry sym = (SeqSymmetry)selected_syms.get(i);
      Map props = null;
      if (sym instanceof SymWithProps) {
	// using Propertied.cloneProperties() here instead of Propertied.getProperties()
	//   because adding start, end, id, and length as additional key-val pairs to props Map
	//   and don't want these to bloat up sym's properties
        props = ((SymWithProps)sym).cloneProperties();
      }
      if (props == null && sym instanceof DerivedSeqSymmetry) {
        SeqSymmetry original_sym = ((DerivedSeqSymmetry) sym).getOriginalSymmetry();
        if (original_sym instanceof SymWithProps) {
          props = ((SymWithProps) original_sym).cloneProperties();
        }
      }
      if (props == null) {
	// make an empty hashtable if sym has no properties...
	props = new Hashtable();
      }
      String symid = sym.getID();
      if (symid != null)  {
        props.put("id", symid);
      }
      if (seqMap != null) {
        SeqSpan span = seqMap.getViewSeqSpan(sym);
	if (span != null) {
	  props.put("start", String.valueOf(span.getStart()));
	  props.put("end", String.valueOf(span.getEnd()));
	  props.put("length", String.valueOf(span.getLength()));
	}
      }
      testcount++;
      propvec.add(props);
    }
    Map[] prop_array = new Map[propvec.size()];
    propvec.copyInto(prop_array);
    
    this.showProperties(prop_array, default_order, "");
  }

  public static void printMap(Map hash)  {
    Iterator iter = hash.entrySet().iterator();
    while (iter.hasNext())  {
      System.out.println(iter.next());
    }

  }
  
  public List getCurrentSyms() {
    return new ArrayList(currentSyms);
  }
  
  public void destroy() {
    currentSyms = Collections.EMPTY_LIST;
  }
}


