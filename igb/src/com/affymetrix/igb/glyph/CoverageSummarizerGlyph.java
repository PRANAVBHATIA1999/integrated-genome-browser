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
import java.util.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import com.affymetrix.genoviz.bioviews.*;
import com.affymetrix.genoviz.glyph.SolidGlyph;

import com.affymetrix.genometry.*;

/**
 *
 *
 *  Intended for dynamically summarizing coverage of a collection of spans.
 *  Show graph based on xcoords/pixel of current view, with a point per pixel,
 *  and the yval of the point represents the fraction of the xcoords interval spanned by the pixel
 *  that is covered by the spans that intersect the xcoords interval.
 *
 * Currently assumes that none of the "child" spans overlap
 *    (or in other words, that the spans going into this summarizer have alread been "projected")
 *  now working on figuring out if projected, and if not projecting first...
 *
 * two modes:
 *    absolute glyph count,
 *    fraction coverage count
 */
public class CoverageSummarizerGlyph extends SolidGlyph {
  public static int COVERAGE = 2;
  public static int SIMPLE = 3;
  public static int DEFAULT_STYLE = COVERAGE;

  static Font default_font = new Font("Courier", Font.PLAIN, 12);
  static NumberFormat nformat = new DecimalFormat();

  int[] mins = null;
  int[] maxs = null;
  double[] yval_for_xpixel = null;
  Point2D curr_coord = new Point2D(0,0);
  Point curr_pixel = new Point(0,0);
  int glyph_style = DEFAULT_STYLE;
  /**
   *  @param spans a list of SeqSpan's all defined on the same BioSeq
   */
  public void setCoveredIntervals(java.util.List spans) {
    int spancount = spans.size();
    if (spancount > 0) {
      int[] newmins = new int[spancount];
      int[] newmaxs = new int[spancount];
      BioSeq firstseq = ((SeqSpan)spans.get(0)).getBioSeq();
      for (int i=0; i<spancount; i++) {
	SeqSpan span = (SeqSpan)spans.get(i);
	newmins[i] = span.getMin();
	newmaxs[i] = span.getMax();
	if (span.getBioSeq() != firstseq) {
	  throw new RuntimeException("in CoverageSummarizerGlyph, not all input spans point to same seq!!!");
	}
      }
      setCoveredIntervals(newmins, newmaxs);
    }
  }

  /**
   *  Each index i in min_array/max_array indicates a span from
   *     min_array[i] to max_array[i].
   *  The arrays are assumed to be sorted in ascending order.
   */
  public void setCoveredIntervals(int[] min_array, int[] max_array) {
    mins = min_array;
    maxs = max_array;
    // need to test to make sure projected and in ascending order --
    //   if not, then perform projection and sort
    /*
    int cnt = mins.length;
    for (int i=0; i<cnt; i++) {
      if (mins
    }
    */
  }

  public void setStyle(int style) {
    if (style != COVERAGE &&
	style != SIMPLE) {
      System.err.println("Eror in CoverageSummarizerGlyph.setStyle(), style not recognized: " + style);
    }
    else {
      glyph_style = style;
    }
  }

  public int getStyle() { return glyph_style; }

  public void draw(ViewI view) {
    if (mins == null || maxs == null)  { return; }
    // could size cache to just the view's pixelbox, but then may end up creating a
    //   new int array every time the pixelbox changes (which with view damage or
    //   scrolling optimizations turned on could be often)
    int view_pixel_width = ((View)view).getComponentSize().width;

    // could check for exact match with view_pixel_width, but allowing larger comp size here
    //    may be good for multiple maps that share the same scene, so that new int array
    //    isn't created every time paint switches from mapA to mapB -- the array will
    //    be reused and be the length of the component with greatest width...
    if ((yval_for_xpixel == null) || (yval_for_xpixel.length < view_pixel_width)) {
      yval_for_xpixel = new double[view_pixel_width];
    }

    // figure out how many bases per pixel
    double pixels_per_coord = ((LinearTransform)view.getTransform()).getScaleX();
    double coords_per_pixel = 1.0 / pixels_per_coord;
    view.transformToPixels(coordbox, pixelbox);

    int pbox_yheight = pixelbox.y + pixelbox.height;
    Graphics g = view.getGraphics();
    Rectangle2D view_coordbox = view.getCoordBox();
    double min_xcoord = view_coordbox.x;
    double max_xcoord = view_coordbox.x + view_coordbox.width;

    int beg_index = 0;
    int end_index = maxs.length-1;

    double coord_length = maxs[end_index] - mins[beg_index];

    int draw_beg_index = Arrays.binarySearch(mins, (int)min_xcoord);
    int draw_end_index = Arrays.binarySearch(maxs, (int)max_xcoord) + 1;
    if (draw_beg_index < 0) {
      // want draw_beg_index to be index of max xcoord <= view_start
      //  (insertion point - 1)  [as defined in Arrays.binarySearch() docs]
      draw_beg_index = (-draw_beg_index -1) - 1;
      if (draw_beg_index < 0) { draw_beg_index = 0; }
    }
    if (draw_end_index < 0) {
      // want draw_end_index to be index of min xcoord >= view_end
      //   (insertion point)  [as defined in Arrays.binarySearch() docs]
      draw_end_index = -draw_end_index -1;
      if (draw_end_index < 0) { draw_end_index = 0; }
      else if (draw_end_index >= maxs.length) { draw_end_index = maxs.length - 1; }
      if (draw_end_index < (maxs.length-1)) { draw_end_index++; }
    }

    int interval_index = draw_beg_index;
    double min = mins[interval_index];
    double max = maxs[interval_index];

    double min_coverage = 1;
    double max_coverage = 0; // max _coverage_ (covered_coords/coords_per_pixel)
    double max_covered = 0;  // max covered coords in a single pixel

    for (int i=0; i<view_pixel_width; i++) {
      yval_for_xpixel[i] = 0;
    }
    boolean hangover = false;

    // sweep through all xpixels via yval_for_pixel cache, and sum up coverage in each pixel
    for (int i=0; i<view_pixel_width; i++) {
      double pixel_start_coord = (i * coords_per_pixel) + min_xcoord;
      double pixel_end_coord = pixel_start_coord + coords_per_pixel;
      double covered_coords = 0;
      // scan along intervals till find next one with max > starting xcoord of current pixel
      // (of course based on subsequent code this will usually be the next one...)
      while ((interval_index <= draw_end_index) &&
	     maxs[interval_index] <= pixel_start_coord) {
	interval_index++;
      }
      // scan along intervals, collecting lengths of intervals,
      //    till find interval with min > end of current pixel
      //      while ((min <= pixel_end_coord) && (interval_index <= draw_end_index))  {
      while ((interval_index <= draw_end_index) &&
	     (interval_index < mins.length) &&
	     (mins[interval_index] <= pixel_end_coord)) {
	min = mins[interval_index];
	max = maxs[interval_index];
	if (min < pixel_start_coord) { min = pixel_start_coord; } // left partial overlap, cut off
	if (max > pixel_end_coord) {  // right partial overlap, cut off
	  max = pixel_end_coord;
	  hangover = true;
	}
	double length = max - min;
	covered_coords += length;
	// still need to deal with case where transfrag extends across multiple pixels
	//   (and thus shouldn't always increment interval_index...)
	interval_index++;
      }
      if (hangover) { interval_index--; }
      double coverage = covered_coords / coords_per_pixel;
      max_coverage = (coverage > max_coverage ? coverage : max_coverage);  // equivalent to Math.max(c, mc)...
      min_coverage = (coverage < min_coverage ? coverage : min_coverage);  // equivalent to Math.min(c, mc)...
      max_covered = (covered_coords > max_covered ? covered_coords : max_covered);
      yval_for_xpixel[i] = coverage;
    }

    int yzero = pixelbox.y + pixelbox.height;
    int ymax = pixelbox.y + 2;
    g.setColor(Color.gray);
    g.drawLine(pixelbox.x, yzero, pixelbox.x + pixelbox.width,  yzero);

    if (max_coverage != 0) {
      // now calculate scaling to fit in max coverage...
      double yscale = coordbox.height / max_coverage;
      double yoffset = coordbox.y + coordbox.height;
      double xoffset = coords_per_pixel / 2.0;

      g.setColor(this.getColor());
      double prev_yval = -1;
      for (int pindex =0 ; pindex < view_pixel_width; pindex++) {
	curr_coord.x = (pindex * coords_per_pixel) + xoffset;
	double curr_yval = yval_for_xpixel[pindex];
	if (curr_yval != 0) {
	  curr_coord.y = yoffset - (curr_yval * yscale);
	  view.transformToPixels(curr_coord, curr_pixel);
	  if (glyph_style == SIMPLE || coords_per_pixel <= 10) {
	    g.drawLine(pindex, yzero, pindex, ymax);
	  }
	  else {
	    g.drawLine(pindex, yzero, pindex, curr_pixel.y);
	  }
	}
      }
    }

    // drawing outline around bounding box
    g.setColor(Color.blue);
    g.setFont(default_font);
    //    g.drawString(nformat.format(max_coverage*100), 3, pixelbox.y + 10);
    String msg =
      "Max coverage in view: " +
      nformat.format(max_coverage) + ", " +
      //      nformat.format(max_covered) + ", " +
      "Bases/Pixel: " +
      nformat.format(coords_per_pixel);

    g.drawString(msg, 3, pixelbox.y + 10);
  }


}
