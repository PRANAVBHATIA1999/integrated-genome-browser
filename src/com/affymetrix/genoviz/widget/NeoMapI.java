/**
*   Copyright (c) 1998-2008 Affymetrix, Inc.
*
*   Licensed under the Common Public License, Version 1.0 (the "License").
*   A copy of the license must be included with any distribution of
*   this source code.
*
*   The license is also available at
*   http://www.opensource.org/licenses/cpl.php
*/

package com.affymetrix.genoviz.widget;

import java.awt.*;
import java.util.List;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.transform.OneDimTransform;
import com.affymetrix.genoviz.bioviews.RubberBand;
import com.affymetrix.genoviz.bioviews.WidgetAxis;
import com.affymetrix.genoviz.event.*;
import com.affymetrix.genoviz.glyph.AxisGlyph;
import com.affymetrix.genoviz.glyph.HorizontalAxisGlyph;
import java.awt.geom.Rectangle2D;
import javax.swing.JSlider;

/**
 * This interface provides general purpose controls for a map.
 * Included are
 * configuring and adding glyphs,
 * creating zoom controls,
 * associating a data model,
 * and selecting glyphs in a map.
 *
 * <p> NeoMapI represents the abstract application programmer interface
 * for a map.  An implementation of a map, such as NeoMap, is used
 * to instantiate a map, and these implementations can include additional
 * functionality.
 *
 * <p> Example:
 *
 * <pre>
 * NeoMapI map = new NeoMap();
 *
 * // add zoom adjustable (in this case JScrollBar)
 * map.setRangeZoomer(new JScrollBar(JScrollBar.HORIZONTAL));
 *
 * map.setMapRange(-500,500); // X bounds
 * map.setMapOffset(-20,20);  // Y bounds
 *
 * // ensure that initial display shows the complete X and Y bounds
 * map.stretchToFit(true,true);
 *
 * // add an axis on the center of this map
 * map.addAxis(0);
 *
 * // associate a private datum to an item
 * setDataModel(item1,myPrivData);
 * </pre>
 *
 * @author Gregg Helt
 *
 */
public interface NeoMapI extends NeoWidgetI {

  /**
   * For methods inherited from NeoWidgetI that require a sub-component id.
   * For NeoMapI the component <em>is</em> the only sub-component,
   * and its id is MAP.
   */
  public static final int MAP = 400;

  /**
   * sets the coordinates describing the range of the map.
   * The map's starting coordinate is set to <code>start</code> and
   * its final coordinate is set to <code>end-1</code>.
   * Affects the primary axis.
   *
   * @param start  the integer indicating the starting
   *   coordinate of the map
   * @param end  the integer indicating one coordinate
   *   beyond the final coordinate of the map.
   *
   * @see #setMapOffset
   */
  public void setMapRange(int start, int end);

  /**
   * sets the coordinates describing the offset of the map.
   * The map's starting coordinate is set to <code>start</code> and
   * its final coordinate is set to <code>end-1</code>.
   * Affects the secondary axis.
   *
   * @param start  the integer indicating the starting
   *   coordinate of the map
   * @param end  the integer indicating one coordinate
   *   beyond the final coordinate of the map.
   */
  public void setMapOffset(int start, int end);

  /**
   * Adds an axis number line along the primary axis at <code>offset</code>
   * along the secondary axis.
   */
  public AxisGlyph addAxis(int offset);
  
  /**
   * Adds an axis number line along the primary axis at <code>offset</code>
   * along the secondary axis.
   */
  public HorizontalAxisGlyph addHorizontalAxis(int offset);

  /**
   * Adds an adjustable to control zooming along the primary axis.
   *
   * @param adj an {@link Adjustable} to be associated with
   *  the primary axis, typically a scrollbar.
   * @see #setOffsetZoomer
   */
  public void setRangeZoomer(JSlider adj);

  /**
   * Adds an zoom adjustable to control zooming along the secondary axis.
   *
   * @param adj a {@link Adjustable} to be associated with
   *  the secondary axis, typically a scrollbar.
   * @see #setRangeZoomer
   */
  public void setOffsetZoomer(JSlider adj);

  /**
   * sets a rubber band for this map.
   * This allows a rubber band (or subclass thereof)
   * to be configured before setting this widget to use it.
   * Note that by default
   * widgets come with their own internal rubber band.
   * This method replaces that rubber band.
   *
   * @param theBand to use. <code>null</code> turns off rubber banding.
   */
  public void setRubberBand( RubberBand theBand );

  public void repack();

  /**
   * Convenience function for zooming the Range.
   *
   * @see #zoom 
   */
  public void zoomRange(double zoom_scale);

  /**
   * Convenience function for zooming the Offset.
   *
   * @see #zoom
   */
  public void zoomOffset(double zoom_scale);

  //TODO: Why not just use component.setBackground() ?
  public void setMapColor(Color col);

  public Color getMapColor();


  /**
   * Sets the transform of the scrollbar specified by id
   * to the specified transform.
   *
   * @param dim  the axis of the scrollbar to
   *             receive the specified transform.
   * @param trans the transform to be applied to the values of the
   *              scrollbar.
   */
  public void setScrollTransform(WidgetAxis dim, OneDimTransform trans);

  /**
   * sets the bounds for the given axis on the map.
   * @param dim  the axis to bind, X or Y
   * @param start
   * @param end
   */
  public void setBounds(WidgetAxis dim, int start, int end);

  /**
   * returns the bounding rectangle in pixels of the displayed item, tag.
   */
  public Rectangle getPixelBounds(GlyphI gl);

  /**
   * returns a list of all <code>Glyph</code>s at
   *  <code>x,y</code> in this widget.
   *
   * @param x the double describing the X position
   * @param y the double describing the Y position
   *
   * @return a List of <code>Glyph</code>s
   * at <code>x,y</code>
   */
  public List<GlyphI> getItems(double x, double y);

  /**
   * returns a list of all <code>Glyphs</code> within the
   * <code>pixrect</code> in this widget.
   *
   * @param pixrect the <code>Rectangle</code> describing the
   *   bounding box of interest
   *
   * @return a List of <code>Glyphs</code>
   *  in <code>pixrect</code>
   */
  public List<GlyphI> getItems(Rectangle pixrect);

  /**
   * retrieve a List of all drawn glyphs that overlap
   * the coordinate rectangle coordrect.
   */
  public List<GlyphI> getItemsByCoord(java.awt.geom.Rectangle2D.Double coordrect);

  /**
   * retrieve all drawn glyphs that overlap the pixel at point x, y.
   */
  public List<GlyphI> getItemsByPixel(int x, int y);

  /**
   * adds a glyph as a child of another glyph.
   * GlyphIs can be hierarchically associated with other GlyphIs,
   * to control drawing, hit detection, packing, etc..
   * The following code creates a child glyph between 55 and 60
   * and associates it with a parent glyph between 50 and 100.
   *
   * <pre> GlyphI item = map.addItem(50,100);
   * map.addItem(item, map.addItem(55,60));</pre>
   *
   * @param parent the GlyphI that will be the parent
   * @param child the GlyphI that will be the child
   */
  public GlyphI addItem(GlyphI parent, GlyphI child);

  /**
   * Add a previously created GlyphI to a map.
   * This allows for example the removal of a glyph from one map
   * and adding the same glyph to another map.
   */
  public void addItem(GlyphI gl);

  /**
   * Make this glyph be drawn in front of all other glyphs.
   * Except will not be drawn in front of transient glyphs.
   */
  public void toFront(GlyphI gl);

  /**
   * Make this glyph be drawn behind all other glyphs.
   */
  public void toBack(GlyphI gl);

  /**
   * Returns the an array of ints that specify the range,
   * or start and end of the primary axis, in coordinates.
   */
  public int[] getMapRange();

  /**
   * Returns the an array of ints that specify the offset,
   * or start and end of the secondary axis, in coordinates.
   */
  public int[] getMapOffset();

  /**
   * Returns a Rectangle2D with the maps bounds (x, y, width, height).
   */
  public Rectangle2D.Double getCoordBounds();

  /**
   * Returns a Rectangle2D with the
   * coordinate bounds (x, y, width, height)
   * currently displayed in the map's view.
   */
  public Rectangle2D.Double getViewBounds();

  /**
   * Adds a viewbox listener to listen for changes
   * to bounds of map's visible area.
   */
  public void addViewBoxListener(NeoViewBoxListener l);

  /**
   * Removes a viewbox listener.
   */
  public void removeViewBoxListener(NeoViewBoxListener l);

  /**
   * Adds a range listener, to listen for changes to the
   * start and end of the viewable range of the map.
   */
  public void addRangeListener(NeoRangeListener l);

  /**
   * Removes a range listener.
   */
  public void removeRangeListener(NeoRangeListener l);

  /**
   * Add a rubberband listener to listen for rubber band
   * events on the map.
   */
  public void addRubberBandListener(NeoRubberBandListener l);

  /**
   * Removes a rubberband listener.
   */
  public void removeRubberBandListener(NeoRubberBandListener l);
}
