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

package com.affymetrix.igb.glyph;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import com.affymetrix.genoviz.widget.*;

public class MinRunThresholder extends JPanel 
  implements ChangeListener, ActionListener  {

  static int frm_width = 400;
  static int frm_height = 200;
  //  SmartGraphGlyph sgg;
  java.util.List graphs = new ArrayList();
  NeoWidgetI widg;
  JSlider tslider;
  JTextField minrunTF;
  int thresh_min = 0;
  int thresh_max = 250;
  int minrun_thresh = 0;

  int max_chars = 9;
  int max_pix_per_char = 6;
  int tf_min_xpix = max_chars * max_pix_per_char;
  int tf_max_xpix = tf_min_xpix + (2 * max_pix_per_char);
  int tf_min_ypix = 20;
  int tf_max_ypix = 25;

  static MinRunThresholder showFramedThresholder(SmartGraphGlyph sgg, NeoWidgetI widg) {
    MinRunThresholder dthresher = new MinRunThresholder(sgg, widg);
    JFrame frm = new JFrame("Graph MinRun Threshold Control");
    Container cpane = frm.getContentPane();
    cpane.setLayout(new BorderLayout());
    cpane.add("Center", dthresher);
    //    frm.setSize(frm_width, frm_height);
    frm.addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent evt) { 
	Window w = evt.getWindow();
	w.setVisible(false);
	w.dispose();
      }
    } );
    frm.pack();
    frm.show();
    return dthresher;
  }

  public MinRunThresholder(SmartGraphGlyph gl, NeoWidgetI w) {
    this(w);
    setGraph(gl);
  }

  public MinRunThresholder(NeoWidgetI w) {
    widg = w;

    tslider = new JSlider(JSlider.HORIZONTAL);
    tslider.setPreferredSize(new Dimension(400, 15));

    minrunTF = new JTextField(max_chars);
    minrunTF.setMinimumSize(new Dimension(tf_min_xpix, tf_min_ypix));
    minrunTF.setMaximumSize(new Dimension(tf_max_xpix, tf_max_ypix));

    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(new JLabel("Min Run: "));
    this.add(minrunTF);
    this.add(tslider);

    tslider.addChangeListener(this);
    minrunTF.addActionListener(this);
  }

  public void setGraphs(java.util.List newgraphs) {
    graphs.clear();
    tslider.removeChangeListener(this);
    minrunTF.removeActionListener(this);

    int gcount = newgraphs.size();
    if (gcount > 0) {
      int newthresh = 0;
      for (int i=0; i<gcount; i++) {
	SmartGraphGlyph gl = (SmartGraphGlyph)newgraphs.get(i);
	graphs.add(gl);
	newthresh += (int)gl.getMinRunThreshold();
      }
      minrun_thresh = newthresh / gcount;
      tslider.setMinimum(thresh_min);
      tslider.setMaximum(thresh_max);
      tslider.setValue(minrun_thresh);
      minrunTF.setText(Integer.toString(minrun_thresh));
    }

    tslider.addChangeListener(this);
    minrunTF.addActionListener(this);
  }


  public void setGraph(SmartGraphGlyph gl) {
    java.util.List newgraphs = new ArrayList();
    newgraphs.add(gl);
    setGraphs(newgraphs);
  }

  public void stateChanged(ChangeEvent evt) {
    if (graphs.size() <= 0) { return; }
    Object src = evt.getSource();
    if (src == tslider) {
      int current_thresh = tslider.getValue();
      if (current_thresh != minrun_thresh) {
	minrun_thresh = current_thresh;
	for (int i=0; i<graphs.size(); i++) {
	  SmartGraphGlyph sgg = (SmartGraphGlyph)graphs.get(i);
	  sgg.setMinRunThreshold(minrun_thresh);
	}
	minrunTF.removeActionListener(this);
	minrunTF.setText(Integer.toString(minrun_thresh));
	minrunTF.addActionListener(this);
	widg.updateWidget();
      }
    }
  }

  public void actionPerformed(ActionEvent evt) {
    if (graphs.size() <= 0) { return; }
    Object src = evt.getSource();
    if (src == minrunTF) {
      int new_thresh = Integer.parseInt(minrunTF.getText());
      if (new_thresh != minrun_thresh) {
	if ((new_thresh < thresh_min) || (new_thresh > thresh_max)) {
	  // new threshold outside of min/max possible, so keep current threshold instead
	  minrunTF.setText(Integer.toString(minrun_thresh));
	}
	else {
	  minrun_thresh = new_thresh;
	  for (int i=0; i<graphs.size(); i++) {
	    SmartGraphGlyph sgg = (SmartGraphGlyph)graphs.get(i);
	    sgg.setMinRunThreshold(minrun_thresh);
	  }
	  tslider.removeChangeListener(this);
	  tslider.setValue(minrun_thresh);
	  tslider.addChangeListener(this);
	  widg.updateWidget();
	}
      }
    }
  }

  public void deleteGraph(GraphGlyph gl) {
    graphs.remove(gl);
    setGraphs(graphs);
  }


}

