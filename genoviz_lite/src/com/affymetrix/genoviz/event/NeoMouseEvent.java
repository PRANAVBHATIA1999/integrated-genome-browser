/**
*   Copyright (c) 1998-2008 Affymetrix, Inc.
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

package com.affymetrix.genoviz.event;

import com.affymetrix.genoviz.bioviews.GlyphI;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.List;

import com.affymetrix.genoviz.widget.NeoWidgetI;
import java.awt.geom.Point2D;

/**
 * A mouse event ocurring over a NeoWidget.
 * (Actually anything implementing {@link NeoWidgetI}.)
 * NeoMouseEvent provides methods for querying
 * the widget coordinates and widget location of a mouse event.
 * Also for retrieving glyphs under the event's coordinates.
 */
public class NeoMouseEvent extends MouseEvent implements NeoCoordEventI {
  static final long serialVersionUID = 1L;

  protected int location = -1;
  protected EventObject original_event;
  protected double xcoord;
  protected double ycoord;
  protected java.util.List<GlyphI> cached_items = null;

  /**
   * Constructs an event with the specified original event, source component,
   * widget location, and widget x/y coords.
   * @param ome the original MouseEvent that this NeoMouseEvent is based on
   * @param source the Neo widget that generated the event
   * @param location id of the widget location the event ocurred over, if
   *        the widget has internal structure
   */
  public NeoMouseEvent(MouseEvent ome, Component source, int location,
      double xcoord, double ycoord) {
    this(ome, source, xcoord, ycoord);
    this.location = location;
  }

  /**
   * Constructs an event with the specified original event, source component,
   * widget x/y coords, and unknown location.
   * @param ome the original MouseEvent that this NeoMouseEvent is based on
   * @param source the Neo widget that generated the event
   */
  public NeoMouseEvent(MouseEvent ome, Component source,
      double xcoord, double ycoord) {
    super(source, ome.getID(), ome.getWhen(),
        ome.getModifiers(), ome.getX(), ome.getY(),
        ome.getClickCount(), ome.isPopupTrigger());
    this.original_event = ome;
    this.xcoord = xcoord;
    this.ycoord = ycoord;
  }

  /** {@inheritDoc } */
  @Override
  public double getCoordX() {
    return xcoord;
  }

  /** {@inheritDoc } */
  @Override
  public double getCoordY() {
    return ycoord;
  }

  /** {@inheritDoc } */
  @Override
  public Point2D.Double getPoint2D() {
    return new Point2D.Double(getCoordX(), getCoordY());
  }

  /** {@inheritDoc } */
  @Override
  public EventObject getOriginalEvent() {
    return original_event;
  }

  /** {@inheritDoc } */
  @Override
  public List<GlyphI> getItems() {
    if (cached_items == null) {
      Object src = getSource();
      if (! (src instanceof NeoWidgetI)) {
        return null;
      }
      cached_items = ((NeoWidgetI)src).getItems(getCoordX(), getCoordY());
    }
    return cached_items;
  }

  /**
   * A String for debugging.
   */
  @Override
  public String toString() {
    String s = "NeoMouseEvent:";
    s += " at ( " + xcoord + ", " + ycoord + " )";
    s += " originally: " + original_event.toString();

    return s;
  }
}
