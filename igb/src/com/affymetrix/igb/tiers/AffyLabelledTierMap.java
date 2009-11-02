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

package com.affymetrix.igb.tiers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import com.affymetrix.genoviz.awt.NeoCanvas;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.util.ComponentPagePrinter;
import java.awt.Component;
import java.awt.geom.Rectangle2D;

/**
 *  Wraps a AffyTieredMap and another map that has tier labels which 
 *    track changes in tiers (size, placement) of AffyTieredMap.
 */
public class AffyLabelledTierMap extends AffyTieredMap  {
  
  AffyTieredMap labelmap;
  private JSplitPane mapsplitter;
  private List<TierLabelGlyph> label_glyphs = new ArrayList<TierLabelGlyph>();
  private JPanel can_panel;
  
  public AffyLabelledTierMap() {
    super();
  }

  public AffyLabelledTierMap(boolean hscroll_show, boolean vscroll_show) {
    super(hscroll_show, vscroll_show);
  }

  /**
   *  Overriding initComponenetLayout from NeoMap
   *    (called in NeoMap constructor...).
   */
	@Override
  public void initComponentLayout() {
    labelmap = new AffyTieredMap(false, false, scroller[Y]);
    labelmap.setRubberBandBehavior(false);
    this.setBackground(Color.blue);
    labelmap.setBackground(Color.lightGray);
    // setMapColor() controls what I normally think of as the background.

    mapsplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    //mapsplitter.setOneTouchExpandable(true);		// confusing to new users
    mapsplitter.setDividerSize(8);
    mapsplitter.setDividerLocation(100);
    NeoCanvas ncan = this.getNeoCanvas();
    mapsplitter.setLeftComponent(labelmap);
    
    can_panel = new JPanel();
    can_panel.setLayout(new BorderLayout());
    can_panel.add("Center", ncan);
    mapsplitter.setRightComponent(can_panel);

    this.setLayout(new BorderLayout());
    add("Center", mapsplitter);

    if (hscroll_show && scroller[X] instanceof Component) {
      add(hscroll_loc, (Component) scroller[X]);
    }
    if (vscroll_show && scroller[Y] instanceof Component)  {
      add(vscroll_loc, (Component)scroller[Y]);
    }
  }

	@Override
  public void setMapColor(Color c) {
    super.setMapColor(c);
    labelmap.setMapColor(c);
  }

	@Override
  public void setBackground(Color c) {
    super.setBackground(c);
    labelmap.setBackground(c);
  }

	@Override
  public void clearWidget() {
    super.clearWidget();
    labelmap.clearWidget();
    label_glyphs = new ArrayList<TierLabelGlyph>();
  }

  public List<TierLabelGlyph> getTierLabels() {
    return label_glyphs;
  }

  public AffyTieredMap getLabelMap() {
    return labelmap;
  }

	@Override
  public void packTiers(boolean full_repack, boolean stretch_map, boolean extra_for_now) { 
    super.packTiers(full_repack, stretch_map, extra_for_now);
    //Rectangle2D.Double bbox = this.getCoordBounds();
    //    labelmap.setMapOffset((int)bbox.y, (int)(bbox.y + bbox.height));
    // this should actually get dealt with in AffyTieredMap, since packTiers() calls 
    //     this.setFloatBounds(), which in turn calls labelmap.setFloatOffset()
    //    labelmap.setFloatBounds(bbox.y, bbox.y + bbox.height);
    Rectangle2D.Double lbox = labelmap.getCoordBounds();
    for (int i=0; i<label_glyphs.size(); i++) {
      GlyphI label_glyph = (GlyphI)label_glyphs.get(i);
      TierGlyph tier_glyph = (TierGlyph)label_glyph.getInfo();
      Rectangle2D.Double tbox = tier_glyph.getCoordBox();
      //      label_glyph.setCoords(lbox.x, tbox.y, lbox.width, tbox.height);
      label_glyph.setCoords(lbox.x, tbox.y, lbox.width, tbox.height);
      //      System.out.println(label_glyph.getCoordBox());
      label_glyph.setVisibility(tier_glyph.isVisible());
    }
  }

  /**
   * Adds a tier to the map and generates a label for it.
   * <p>We don't need to override {@link AffyTieredMap#addTier(TierGlyph)}
   * because it calls {@link AffyTieredMap#addTier(TierGlyph, boolean)},
   * which, in turn calls {@link AffyTieredMap#addTier(TierGlyph,int)}
   * which we override here.
   */
	@Override
  public void addTier(TierGlyph mtg, int tier_index) {
    super.addTier(mtg, tier_index);
    createTierLabel(mtg);    
  }
  
  /** Creates a TierLabelGlyph for the given TierGlyph.  
   *  Called by addTier() methods.  Override this to 
   *  add additional settings to the glyph.
   */
  public TierLabelGlyph createTierLabel(TierGlyph mtg) {
    TierLabelGlyph label_glyph = new TierLabelGlyph(mtg);
    // No need to set the TierLabelGlyph colors or label:
    // it reads that information dynamically from the given TierGlyph
    
    labelmap.addItem(label_glyph);
    // set info for string glyph to point to tier glyph
    //   (which also sets value returned by label_glyph.getInfo())
    labelmap.setDataModel(label_glyph, mtg);  
    label_glyphs.add(label_glyph);
    return label_glyph;
  }

	@Override
  public void removeTier(TierGlyph toRemove) {
    super.removeTier(toRemove);
    TierLabelGlyph label_glyph = labelmap.<TierLabelGlyph>getItem(toRemove);
    if (label_glyph != null) {
      labelmap.removeItem(label_glyph);
      label_glyphs.remove(label_glyph);
    }
  }

	@Override
  public void setFloatBounds(int axis, double start, double end) {
    super.setFloatBounds(axis, start, end);
    if (axis == Y && labelmap != null) { 
      labelmap.setFloatBounds(axis, start, end);
    }
  }

	@Override
  public void setBounds(int axis, int start, int end) {
    super.setBounds(axis, start, end);
    if (axis == Y && labelmap != null) { 
      labelmap.setBounds(axis, start, end);
    }
  }

	@Override
  public void zoom(int axisid, double zoom_scale) { 
    super.zoom(axisid, zoom_scale);
    if (axisid == Y && labelmap != null) {
      labelmap.zoom(axisid, zoom_scale);
    }
  }

	/*
	@Override
  public void scroll(int axisid, double value) {
    super.scroll(axisid, value);
    if (axisid == Y && labelmap != null) {
      labelmap.scroll(axisid, value);
    }
  }
	 */

	@Override
  public void setZoomBehavior(int axisid, int constraint, double coord) {
    super.setZoomBehavior(axisid, constraint, coord);
    labelmap.setZoomBehavior(axisid, constraint, coord);
  }

	@Override
  public void updateWidget() {
    super.updateWidget();
    labelmap.updateWidget();
  }

	@Override
  public void updateWidget(boolean full_update) {
    super.updateWidget(full_update);
    labelmap.updateWidget(full_update);
  }

	@Override
  public void stretchToFit(boolean fitx, boolean fity) {
    super.stretchToFit(fitx, fity);
    labelmap.stretchToFit(fitx, fity);
  }

	@Override
  public void repackTheTiers(boolean full_repack, boolean stretch_vertically) {
    super.repackTheTiers(full_repack, stretch_vertically);  
    labelmap.repackTheTiers(full_repack, stretch_vertically);  
  }

  /** Prints this component, including the label map. */
  @Override
	public void print() throws java.awt.print.PrinterException {
    print(true);
  }
  
  /** Prints this component.
   *  @param print_labels whether or not to print the label map along with the map
   */
  public void print(boolean print_labels) throws java.awt.print.PrinterException {
    ComponentPagePrinter cpp = null;
    if (print_labels) {
      cpp = new ComponentPagePrinter(mapsplitter);
    } else {
      cpp = new ComponentPagePrinter(can_panel);
    }
    cpp.print();
    cpp = null; // for garbage collection
  }
  
  /** Returns the JSplitPane that contains the label map and the tier map.
   *  This is mostly useful for printing.
   */
  public JSplitPane getSplitPane() {
    return mapsplitter;
  }


	@Override
	public void componentResized(ComponentEvent evt) {
		if (evt.getSource() == canvas) {
			this.stretchToFit(false, true);
			this.updateWidget();
		}
	}

}
