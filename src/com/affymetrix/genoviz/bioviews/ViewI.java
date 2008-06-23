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

package com.affymetrix.genoviz.bioviews;

import com.affymetrix.genoviz.awt.NeoCanvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A ViewI is an abstract window onto a particular {@link SceneI}.
 * The view maintains a mapping (with the help of {@link TransformI})
 * between the scene's coordinate space
 * and the pixel space of a particular AWT component.
 * Among other things
 * the view is responsible for initiating the drawing of {@link GlyphI} objects,
 * and for managing the transformation of coordinates to pixels and
 * vice versa to allow glyphs to draw themselves
 *
 * <p> ViewI, along with SceneI and GlyphI, is one of the three fundamental interfaces
 * comprising Affymetrix' inner 2D structured graphics architecture.
 */
public interface ViewI  {

  public Rectangle getScratchPixBox();

  /**
   * Set the Graphics that this view should draw on.
   */
  public void setGraphics(Graphics2D g);

  /**
   * Get the Graphics that this view should draw on.
   */
  public Graphics2D getGraphics();

  /**
   * Draw this view of a scene onto the view's component.
   */
  public void draw();

  /**
   * returns the SceneI that the view is of.
   * (no set method --
   *  current implementation sets the scene in the View constructor)
   */
  public SceneI getScene();

  /**
   * Sets the component that the view draws to.
   */
  public void setComponent(NeoCanvas c);

  /**
   *  Returns the component that the view draws to.
   */
  public NeoCanvas getComponent();


  /// Scene gives View a coord box (e.g. range of base pairs).
  /// Scene gives View a transform between pixel & coord space.
  /// Scene gives View a pixel box (e.g. the whole canvas).
  /**
   * sets the pixel box that bounds the view on the component the view draws to.
   * This is typically a rectangle the dimensions of the component
   * (0, 0, component.size().width, component.size().height)
   */
  public void setPixelBox(Rectangle rect);

  /**
   * Gets the pixel box that bounds the view on the component the view draws to.
   * This is typically the dimensions of the component
   * (0, 0, size().width, size().height)
   */
  public Rectangle getPixelBox();

  /**
   *  Sets the coordinate box that bounds the view, in other words the
   *  portion of the scene that is visible within this view.
   */
  public void setCoordBox(Rectangle2D.Double coordbox);

  /**
   *  returns the coordinate box that bounds the view, in other words the
   *  portion of the scene that is visible within this view.
   */
  public Rectangle2D.Double getCoordBox();

  public void setFullView(ViewI full_view);
  public ViewI getFullView();

  /**
   *  sets the TransformI that is used to transform widget coordinates to
   *  pixels and vice versa.
   */
  public void setTransform(TransformI t);

  /**
   *  returns the TransformI that is used to transform widget coordinates to
   *  pixels and vice versa.
   */
  public TransformI getTransform();

  /**
   *    The view is responsible for mapping coordinates to pixels and
   *    vice versa, via transformToPixels() and transformToCoords().
   *
   *    transformToPixels() transforms src rectangle in coordinate space
   *    to dst rectangle in pixel (screen) space.
   *    @return altered destination Rectangle
   */
  public Rectangle transformToPixels(Rectangle2D.Double src, Rectangle dst);

  /**
   *    The view is responsible for mapping coordinates to pixels and
   *    vice versa, via transformToPixels() and transformToCoords().
   *
   *    <p> transformToCoords transforms src rectangle in pixel (screen) space
   *    to dst rectangle in coord space.
   *    @return altered destination Rectangle2D
   */
  public Rectangle2D.Double transformToCoords(Rectangle src, Rectangle2D.Double dst);

  /**
   *
   * Transforms src Point2D in coordinate space to dst Point in pixel
   *    (screen) space.
   *  Returns altered destination Point
   */
  public Point transformToPixels(Point2D.Double src, Point dst);

  /**
   *    Transforms src Point in pixel (screen) space to dst Point2D in
   *    coord space.
   *
   *    Returns alterred destination Point2D
   */
  public Point2D transformToCoords(Point src, Point2D.Double dst);

  // needed to add this for efficiency -- some glyphs access component
  // width/height/bounds to get around Graphics drawing bugs  -- GAH 12/14/97
  /**
   *  primarily for use internal to implementation, getComponentSize()
   *  returns same results as getComponent.bounds(), but in a more
   *  efficient manner.
   *  (will move to implementation rather than interface in next release)
   */
  //TODO: delete
  public Dimension getComponentSize();

  // needed to add this for efficiency -- some glyphs access component
  // width/height/bounds to get around Graphics drawing bugs  -- GAH 4-22-98
  /**
   * gets the component dimensions.
   * The rectable returned is equivalent to
   * (0, 0, component.size().width, component.size().height),
   * but it is done in a more efficient manner.
   * It is primarily for use implementing glyphs.
   * It works around a java.awt 1.0 bug
   * that affects drawing large glyphs on smaller components.
   *
   * @return (0, 0, width, height)
   */
  //TODO: delete
  public Rectangle getComponentSizeRect();

}
