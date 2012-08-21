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
package com.affymetrix.igb.shared;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.GraphSym;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.ViewI;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;


/**
 * An implementation of graphs for NeoMaps,
 * capable of rendering graphs in a variety of styles.
 * Started with {@link com.affymetrix.genoviz.glyph.BasicGraphGlyph}
 * and improved from there.
 * <p><em><strong>This is only meant for graphs on horizontal maps.</strong></em>
 * </p>
 */
public class AbstractGraphGlyph extends AbstractViewModeGlyph implements ViewModeGlyph{
	private static final NumberFormat nformat = new DecimalFormat();
	protected GraphGlyph graphGlyph;

	private static final Map<String,Class<?>> PREFERENCES;
	static {
		Map<String,Class<?>> temp = new HashMap<String,Class<?>>();
		temp.put("y_axis", Boolean.class);
		PREFERENCES = Collections.unmodifiableMap(temp);
	}

	public AbstractGraphGlyph(ITrackStyleExtended style) {
		super();
		setStyle(style);
	}
	
	@Override
	public void setStyle(ITrackStyleExtended style) {
		super.setStyle(style);
		if(graphGlyph != null){
			graphGlyph.setColor(style.getForeground());
		}
	}
	
	@Override
	public void processParentCoordBox(Rectangle2D.Double parentCoordBox) {
		setCoordBox(getCoordBox()); // so all use the same coordbox
	}
		
	@Override
	public boolean initUnloaded() {
		BioSeq seq = GenometryModel.getGenometryModel().getSelectedSeq();//smv.getAnnotatedSeq();	 
		if (getInfo() != null && ((GraphSym) getInfo()).getGraphSeq() != seq) {
			return true;
		}
		return super.initUnloaded();
	}
		
	@Override
	public String getLabel() {
		return style.getTrackName();
	}
	
	@Override
	public void setLabel(String str) {
		style.setTrackName(str);
	}
		
	@Override
	public void setPreferredHeight(double height, ViewI view) {
		GlyphI child = getChild(0);
		Rectangle2D.Double  c = child.getCoordBox();
		child.setCoords(c.x, c.y, c.width, height);
		//Note : Fix to handle height in a view mode.
		// But this also causes minor change in height while switching back to default view mode.
		setCoords(getCoordBox().x, getCoordBox().y, getCoordBox().width, height + 2 * getSpacing());
		this.style.setHeight(height + 2 * getSpacing());
		child.pack(view);
	}

	@Override
	public int getActualSlots() {
		return 1;
	}

	/**
	 * Determine how short a glyph can be so we can avoid empty vertical space.
	 * Originally implemented for annotation tracks.
	 * Here we hope for a {@link GraphSym} as the glyph's info.
	 * If we don't find one, we return the answer from the super class.
	 * Subclasses can specialize this, of course.
	 * TODO Do we want y max? or |y max - y min| or [y max|?
	 *      or even max(|y min|, [y max|)?
	 *      The old basic graph glyph used to flip y values
	 *      because pixels start at 0 and go negative.
	 * @param theView limits the data to consider.
	 * @return How tall the glyph must be to show all the data in view.
	 *         Cannot be negative?
	 */
	@Override
	public int getSlotsNeeded(ViewI theView) {
		if (null == theView) {
			throw new IllegalArgumentException("theView cannot be null.");
		}
		Object o = this.getInfo();
		if (null != o) {
			if (o instanceof GraphSym) {
				GraphSym model = (GraphSym) o;
				// Figure it out.
				float[] bounds = getRangeInView(model, theView);
				assert bounds[0] <= bounds[1];
				float answer = bounds[1] - bounds[0];
				if (answer <= 0) {
					return 0;
				}
				if (Integer.MAX_VALUE <= answer) {
					return Integer.MAX_VALUE;
				}
				return (int) answer;
			}
		}
		return super.getSlotsNeeded(theView);
	}

	@Override
	protected boolean shouldDrawToolBar(){
		return false;
	}
	
	@Override
	public Map<String, Class<?>> getPreferences() {
		return new HashMap<String, Class<?>>(PREFERENCES);
	}

	@Override
	public void setPreferences(Map<String, Object> preferences) {
	}
	
	/**
	 * Determine the extreme values of <var>y</var> in theView.
	 * We do not need to translate between scene coordinates
	 * to those of the graph symmetry.
	 * They are essentially the same thing(?), just different precisions.
	 * Graph symmetry coordinates are not pixels.
	 * TODO Maybe this should be a method of GraphSym?
	 * TODO Could use a "Range" or "Interval" object instead of float[2].
	 * TODO Should a null view be an illegal argument?
	 * @param theData containing points (<var>x</var>,<var>y</var>).
	 * @return the minimum and maximum values of <var>y</var>
	 *         restricted to the <var>x</var> values in theView.
	 */
	private float[] getRangeInView(GraphSym theData, ViewI theView) {
		if (null == theData) {
			throw new IllegalArgumentException("theData cannot be null.");
		}
		int[] ourDomain = theData.getGraphXCoords();
		float[] ourRange = theData.getGraphYCoords();
		assert ourDomain.length == ourRange.length;
		float[] empty = {0, 0};
		if (ourDomain.length < 1) {
			return empty; // Artificial. Maybe should throw illegal arg.
		}
		Rectangle2D.Double b = theView.getCoordBox();
		long lowerBound = Long.MIN_VALUE;
		long upperBound = Long.MAX_VALUE;
		if (null != theView) {
			lowerBound = Math.round(b.x);
			upperBound = Math.round(Math.floor((lowerBound + b.width) - Double.MIN_VALUE));
		}
		float rangeMinimum = Float.POSITIVE_INFINITY;
		float rangeMaximum = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < ourDomain.length; i++) {
			if (lowerBound <= ourDomain[i] && ourDomain[i] <= upperBound) {
				rangeMinimum = Math.min(rangeMinimum, ourRange[i]);
				rangeMaximum = Math.max(rangeMaximum, ourRange[i]);
			}
		}
		float[] answer = {rangeMinimum, rangeMaximum};
		return answer;
	}

	@Override
	public void addChild(GlyphI glyph, int position) {
		addChild(glyph);
	}
	
	@Override
	public void addChild(GlyphI child){
		super.addChild(child);
		if(child instanceof GraphGlyph){
			graphGlyph = (GraphGlyph)child;
		}
	}
	
	@Override
	public void removeAllChildren() {
		super.removeAllChildren();
		graphGlyph = null;
	}
	
/*************************** Should be removed ********************************/
	
	public GraphGlyph getGraphGlyph(){
		return graphGlyph;
	}

/*************************** Should be removed ********************************/
}
