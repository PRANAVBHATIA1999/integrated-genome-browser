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
import java.util.*;

import javax.swing.*;

import com.affymetrix.genoviz.awt.NeoCanvas;
import com.affymetrix.genoviz.util.ComponentPagePrinter;
import com.affymetrix.genoviz.util.NeoConstants;
import com.affymetrix.igb.shared.TierGlyph;

import java.awt.Component;
import java.awt.geom.Rectangle2D;

/**
 *  Wraps a AffyTieredMap and another map that has tier labels which 
 *    track changes in tiers (size, placement) of AffyTieredMap.
 */
public final class AffyLabelledTierMap extends AffyTieredMap  {
  private static final long serialVersionUID = 1L;

  private AffyTieredMap labelmap;
  private JSplitPane mapsplitter;
  private final List<TierLabelGlyph> label_glyphs = new ArrayList<TierLabelGlyph>();
  private List<TierLabelGlyph> ordered_glyphs = null;
  private JPanel can_panel;
  private NeoCanvas ncan;
  
  public AffyLabelledTierMap(boolean hscroll_show, boolean vscroll_show) {
    super(hscroll_show, vscroll_show, NeoConstants.HORIZONTAL);
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
    mapsplitter.setDividerSize(8);
    mapsplitter.setDividerLocation(100);
    ncan = this.getNeoCanvas();
    mapsplitter.setLeftComponent(labelmap);
    
    can_panel = new JPanel();
    can_panel.setLayout(new BorderLayout());
    can_panel.add("Center", ncan);
    mapsplitter.setRightComponent(can_panel);

    this.setLayout(new BorderLayout());
    add("Center", mapsplitter);

    if (hscroll_show) {
      add(hscroll_loc, (Component) scroller[X]);
    }
    if (vscroll_show)  {
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
    label_glyphs.clear();
    ordered_glyphs = null;
  }

  public List<TierLabelGlyph> getTierLabels() {
    return label_glyphs;
  }

  private static final double FUDGE_FACTOR = 0.2;
  private List<TierLabelGlyph> getOrderedGlyphs() {
    List<TierLabelGlyph> orderedGlyphs = new ArrayList<TierLabelGlyph>();
    for (TierLabelGlyph tierLabelGlyph : label_glyphs) {
      if (tierLabelGlyph.isVisible()) {
        orderedGlyphs.add(tierLabelGlyph);
      }
    }
    Collections.sort(orderedGlyphs, new Comparator<TierLabelGlyph>() {
      @Override
      public int compare(TierLabelGlyph o1, TierLabelGlyph o2) {
        if (o1.getCoordBox().getY() + o1.getCoordBox().getHeight() < o2
            .getCoordBox().getY() + FUDGE_FACTOR) {
          return -1;
        }
        if (o1.getCoordBox().getY() + FUDGE_FACTOR > o2.getCoordBox()
            .getY() + o2.getCoordBox().getHeight()) {
          return 1;
        }
        return 0;
      }
    });
    return orderedGlyphs;
  }

  public List<TierLabelGlyph> getOrderedTierLabels() {
	  if (ordered_glyphs == null) {
		  ordered_glyphs = getOrderedGlyphs();
	  }
	  return ordered_glyphs;
  }
 
  public AffyTieredMap getLabelMap() {
    return labelmap;
  }

	@Override
  public void packTiers(boolean full_repack, boolean stretch_map, boolean extra_for_now, boolean manual) { 
    super.packTiers(full_repack, stretch_map, extra_for_now, manual);
    Rectangle2D.Double lbox = labelmap.getCoordBounds();
	for (TierLabelGlyph label_glyph : label_glyphs) {
      TierGlyph tier_glyph = (TierGlyph)label_glyph.getInfo();
      Rectangle2D.Double tbox = tier_glyph.getCoordBox();
      label_glyph.setCoords(lbox.x, tbox.y, lbox.width, tbox.height);
      label_glyph.setVisibility(tier_glyph.isVisible());
    }
    ordered_glyphs = null;
  }

  /**
   * Adds a tier to the map and generates a label for it.
   */
	@Override
  public void addTier(TierGlyph mtg, int tier_index) {
    super.addTier(mtg, tier_index);
    createTierLabel(mtg, tier_index);
  }
  
  /** Creates a TierLabelGlyph for the given TierGlyph.  
   *  Called by addTier() methods.  Override this to 
   *  add additional settings to the glyph.
   */
  private void createTierLabel(TierGlyph mtg, int tier_index) {
    TierLabelGlyph label_glyph = new TierLabelGlyph(mtg, tier_index);
    // No need to set the TierLabelGlyph colors or label:
    // it reads that information dynamically from the given TierGlyph
    
    labelmap.addItem(label_glyph);
    // set info for string glyph to point to tier glyph
    //   (which also sets value returned by label_glyph.getInfo())
    labelmap.setDataModel(label_glyph, mtg);  
    label_glyphs.add(label_glyph);
    ordered_glyphs = null;
  }

	@Override
  public void removeTier(TierGlyph toRemove) {
    super.removeTier(toRemove);
    TierLabelGlyph label_glyph = labelmap.<TierLabelGlyph>getItem(toRemove);
    if (label_glyph != null) {
      labelmap.removeItem(label_glyph);
      label_glyphs.remove(label_glyph);
      ordered_glyphs = null;
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
  public void repackTheTiers(boolean full_repack, boolean stretch_vertically, boolean manual) {
    super.repackTheTiers(full_repack, stretch_vertically, manual);  
    labelmap.repackTheTiers(full_repack, stretch_vertically, manual);  
  }

  /** Prints this component, including the label map. */
  @Override
	public void print() throws java.awt.print.PrinterException {
    print(true);
  }
  
  /** Prints this component.
   *  @param print_labels whether or not to print the label map along with the map
   */
  private void print(boolean print_labels) throws java.awt.print.PrinterException {
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
  public void componentResized(java.awt.event.ComponentEvent evt) {
	if (evt.getSource() == canvas) {
		int[] range = this.getVisibleOffset();
		stretchToFit(false, true);
		
		int coord_height = range[1] - range[0];
		double pixel_height = this.getView().getPixelBox().height;
		double ppc = pixel_height / coord_height; 
		ppc = Math.min(ppc, getMaxZoom(Y));
		ppc = Math.max(ppc, getMinZoom(Y));
		zoom(Y, ppc);
		scroll(Y, range[0]);
		setZoomBehavior(Y, CONSTRAIN_COORD, (range[0] + range[1]) / 2);
		
		updateWidget();
	}
  }

}
