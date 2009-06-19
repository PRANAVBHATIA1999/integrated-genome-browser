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

import java.util.*;
import com.affymetrix.genoviz.bioviews.*;
import java.awt.geom.Rectangle2D;

public final class FlyPointLinkerGlyph extends FlyweightPointGlyph {
  boolean flyweights_ordered = true;
  boolean link_points = true;
  int distance_min_threshold = 0;
  GlyphI link_glyph;  // template glyph for links

  // xmin is _not_ the min val in xcoords, but rather designates the lower bound of 
  //   the analysis that led to these points  (for example, in an ORF analysis the xcoords 
  //   designate location of stop codons, but the ORF analysis likely begins 
  //   somewhere upstream of the first stop codon
  int xmin;
  // xmax is _not_ the max val in xcoords, but rather designates the upper bound of 
  //   the analysis that led to these points 
  int xmax;

  /**
   *  make sure link_template.coordbox.height <= this.coordbox.height
   */
  //  public FlyPointLinkerGlyph(GlyphI point_template, GlyphI link_template, int[] xarray, int flength) {
  public FlyPointLinkerGlyph(GlyphI point_template, GlyphI link_template, int[] xarray, int flength, 
			     int xmin, int xmax) {
    super(point_template, xarray, flength);
    this.link_glyph = link_template;
    this.xmin = xmin;
    this.xmax = xmax;
  }
  
  public void setMinThreshold(int min) {
    distance_min_threshold = min;
  }

  public int getMinThreshold() { 
    return distance_min_threshold; 
  }

  public void drawFlyweights(ViewI view) {
    Rectangle2D.Double vbox = view.getCoordBox();
    Rectangle2D.Double cbox = this.getCoordBox();
    Rectangle2D.Double tbox = template_glyph.getCoordBox();
    Rectangle2D.Double lbox = link_glyph.getCoordBox();

    tbox.width = flylength;
    tbox.y = cbox.y;
    tbox.height = cbox.height;  

    double ymid = cbox.y + cbox.height/2;
    lbox.y = ymid - lbox.height/2;  // retain lbox.height

    if (xcoords != null) {
      int flycount = xcoords.length;
      int prev_pos = xcoords[0];
      int pos;
      int drawcount = 0;

      // if flyweight xcoords array is ordered, then use binary search to speed up 
      //   scan for array entries within (and just outside) view
      if (flyweights_ordered) {
	int view_start = (int)(vbox.x);
	int view_end = (int)(vbox.x + vbox.width);
	int start_index = Arrays.binarySearch(xcoords, view_start);
	int end_index = Arrays.binarySearch(xcoords, view_end);
	
	if (start_index < 0) {
	  // want start_index to be index of max xcoord <= view_start
	  //  (insertion point - 1)  [as defined in Arrays.binarySearch() docs]
	  start_index = (-start_index -1) - 1; 
	  if (start_index < 0) { start_index = 0; }
	}
	if (end_index < 0) {
	  // want end_index to be index of min xcoord >= view_end
	  //   (insertion point)  [as defined in Arrays.binarySearch() docs]
	  end_index = -end_index -1;
	  if (end_index < 0) { end_index = 0; }
	  else if (end_index >= xcoords.length) { end_index = xcoords.length - 1; }
	}
	// special casing for when looking at first point in xcoord list, to 
	//   ensure line is drawn at beginning if appropriate
	if (start_index == 0) {
	  //	  prev_pos = view_start;
	  prev_pos = Math.max(view_start, xmin);
	}
	for (int i=start_index; i<=end_index; i++) {
	  pos = xcoords[i];
	  tbox.x = pos;
	  template_glyph.draw(view);
	  int distance = (pos - prev_pos);
	  if (distance >= distance_min_threshold) {
	    lbox.x = prev_pos + 3.0f;
	    lbox.width = (pos - prev_pos - 3.0f);
	    link_glyph.draw(view);
	  }
	  prev_pos = pos;
	}
	// special casing for when looking at last point in xcoord list, to 
	//   ensure line is drawn at end if appropriate
	if (end_index == (xcoords.length - 1)) {
	  //	  pos = view_end;
	  pos = Math.min(view_end, xmax);
	  int distance = (pos - prev_pos);
	  if (distance >= distance_min_threshold) {
	    lbox.x = prev_pos + 3.0f;
	    lbox.width = (pos - prev_pos - 3.0f);
	    link_glyph.draw(view);
	  }
	}
      }

      // if xcoords array not ordered, do a linear scan through all entries to 
      //     find which ones are within view
      else {  
	System.out.println("Ackk!!! FlyPointLinkerGlyph xcoords not ordered");
	for (int i=0; i<flycount; i++) {
	  pos = xcoords[i];
	  tbox.x = pos;
	  if (template_glyph.withinView(view)) {
	    template_glyph.draw(view);
	    int distance = (pos - prev_pos);
	    if (distance >= distance_min_threshold) {
	      lbox.x = prev_pos + 3.0f;
	      lbox.width = (pos - prev_pos - 3.0f);
	      link_glyph.draw(view);
	    }
	  }
	  prev_pos = pos;
	}
      }
    }
  }
  
}

