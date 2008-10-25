/**
*   Copyright (c) 2001-2008 Affymetrix, Inc.
*
*   Licensed under the Common Public License, Version 1.0 (the "License").
*   A copy of the license must be included with any distribution of
*   this source code.
*
*   The license is also available at
*   http://www.opensource.org/licenses/cpl.php
*/

package com.affymetrix.genoviz.tiers;

import com.affymetrix.genoviz.transform.LinearTwoDimTransform;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

import com.affymetrix.genoviz.bioviews.*;
import com.affymetrix.genoviz.glyph.*;
import java.awt.geom.Rectangle2D;

/**
 *  A TierGlyph that can keep a constant height in pixels even
 *  when the Y-zoom is modified.
 *  <em>Assumption:</em> This Glyph will appear in a single view.
 */
/*
 *  only use transform for operations on children.
 *    coordinates of the tier itself are maintained in coordinate system
 *    of the incoming view...
 *
 *  currently assuming no modifications to tier_transform, etc. are made between
 *     call to modifyView(view) and call to restoreView(view);
 *
 *  Note that if the tier has any "middleground" glyphs, 
 *     these are _not_ considered children, so transform does not apply to them
 *
 */
public class TransformTierGlyph extends TierGlyph {
  boolean DEBUG_PICK_TRAVERSAL = false;

  /*
   *  if fixed_pixel_height == true,
   *    then adjust transform during pack, etc. to keep tier
   *    same height in pixels
   *  (assumes tier only appears in one map / scene)
   */
  boolean fixed_pixel_height = false;
  int fixedPixHeight = 1;

  LinearTwoDimTransform tier_transform = new LinearTwoDimTransform();

  LinearTwoDimTransform modified_view_transform = new LinearTwoDimTransform();
  Rectangle2D.Double modified_view_coordbox = new Rectangle2D.Double();

  LinearTwoDimTransform incoming_view_transform;
  Rectangle2D.Double incoming_view_coordbox;

  // for caching in pickTraversal() methods
  Rectangle2D.Double internal_pickRect = new Rectangle2D.Double();
  // for caching in pickTraversal(pixbox, picks, view) method
  Rectangle2D.Double pix_rect = new Rectangle2D.Double();

  public TransformTierGlyph() {
    super();
  }
  
  public TransformTierGlyph(IAnnotStyle style)  {
    super(style);
  }

  public void setTransform(LinearTwoDimTransform trans) {
    tier_transform = trans;
  }

  public LinearTwoDimTransform getTransform() {
    return tier_transform;
  }


  /** Do nothing. Otherwise the calculations of fixed-tier size 
   *  do not work.
   */
  @Override
  protected void addRoomForLabel() {
    return;
  }
  
  
  
  @Override
  public void drawChildren(ViewI view) {

    // MODIFY VIEW
    incoming_view_transform = (LinearTwoDimTransform)view.getTransform();
    incoming_view_coordbox = view.getCoordBox();

    // figure out draw transform by combining tier transform with view transform
    // should allow for arbitrarily deep nesting of transforms too, since cumulative
    //     transform is set to be view transform, and view transform is restored after draw...

    // should just copy values instead of creating new object every time,
    //    but for now just creating new object for convenience
    //    modified_view_transform = new LinearTwoDimTransform(incoming_view_transform);

    //    modified_view_transform = new LinearTwoDimTransform(incoming_view_transform);
    //    modified_view_transform.append(tier_transform);
    //    modified_view_transform.prepend(tier_transform);
    //    view.setTransform(modified_view_transform);
    //    view.setTransform(tier_transform);

    // should switch soon to doing this completely through
    //    LinearTwoDimTransform calls, and eliminate new AffineTransform creation...
    AffineTransform trans2D = new AffineTransform();
    trans2D.translate(0.0, incoming_view_transform.getOffsetY());
    trans2D.scale(1.0, incoming_view_transform.getScaleY());

    //    trans2D.translate(1.0, this.getCoordBox().y);
    //    System.out.println("tier transform: offset = " + tier_transform.getOffsetY() +
    //    		       ", scale = " + tier_transform.getScaleY());

    trans2D.translate(1.0, tier_transform.getOffsetY());
    trans2D.scale(1.0, tier_transform.getScaleY());

    modified_view_transform = new LinearTwoDimTransform();
    modified_view_transform.setScaleX(incoming_view_transform.getScaleX());
    modified_view_transform.setOffsetX(incoming_view_transform.getOffsetX());
    modified_view_transform.setScaleY(trans2D.getScaleY());
    modified_view_transform.setOffsetY(trans2D.getTranslateY());
    view.setTransform(modified_view_transform);

    // need to set view coordbox based on nested transformation
    //   (for methods like withinView(), etc.)
    view.transformToCoords(view.getPixelBox(), modified_view_coordbox);
    view.setCoordBox(modified_view_coordbox);

    // CALL NORMAL DRAWCHILDREN(), BUT WITH MODIFIED VIEW
    super.drawChildren(view);

    // RESTORE ORIGINAL VIEW
    view.setTransform(incoming_view_transform);
    view.setCoordBox(incoming_view_coordbox);

  }

  public void fitToPixelHeight(ViewI view) {
    // use view transform to determine how much "more" scaling must be
    //       done within tier to keep its
    LinearTwoDimTransform view_transform = (LinearTwoDimTransform)view.getTransform();
    double yscale = 0.0d;
    if ( 0.0d != coordbox.height ) {
      yscale = (double)fixedPixHeight / coordbox.height;
    }
    //    System.out.println("yscale: " + yscale);
    yscale = yscale / view_transform.getScaleY();
    //    System.out.println("yscale2: " + yscale);
    tier_transform.setScaleY(tier_transform.getScaleY() * yscale );
    //    tier_transform.setOffsetY(tier_transform.getOffsetY() * yscale);
    /*
    tier_transform.setOffsetY(tier_transform.getOffsetY()
			      - (tier_transform.getOffsetY() * yscale) );
    */

    coordbox.height = coordbox.height * yscale;
  }


  //
  // need to redo pickTraversal, etc. to take account of transform also...
  //
    @Override
  public void pickTraversal(Rectangle2D.Double pickRect, List<GlyphI> pickList,
                            ViewI view)  {

    // copied form first part of Glyph.pickTraversal()
    if (isVisible && intersects(pickRect, view))  {
      if (hit(pickRect, view))  {
        if (!pickList.contains(this)) {
          pickList.add(this);
        }
      }

      if (children != null)  {
	// modify pickRect on the way in
	//   (transform from view coords to local (tier) coords)
	//    [ an inverse transform? ]
	tier_transform.inverseTransform(pickRect, internal_pickRect);

	// copied from second part of Glyph.pickTraversal()
        GlyphI child;
        int childnum = children.size();
        for ( int i = 0; i < childnum; i++ ) {
          child = children.get( i );
          child.pickTraversal(internal_pickRect, pickList, view );
        }
      }
      if (DEBUG_PICK_TRAVERSAL)  { debugLocation(pickRect); }
    }
  }

  // don't move children! just change tier's transform offset
  @Override
  public void moveRelative(double diffx, double diffy) {
    coordbox.x += diffx;
    coordbox.y += diffy;
    //    tier_transform.setOffsetY(coordbox.y);
    //    tier_transform.setOffsetY(diffy);
    tier_transform.setOffsetY(tier_transform.getOffsetY() + diffy);
    //    System.out.println("Hmm: called moveRelative: diffx = " + diffx + ", diffy = " + diffy);
  }


  private void debugLocation(Rectangle2D.Double pickRect) {
    // just for debugging
    tier_transform.inverseTransform(pickRect, internal_pickRect);
    GlyphI pick_glyph = new FillRectGlyph();
    pick_glyph.setCoords(internal_pickRect.x, internal_pickRect.y,
			 internal_pickRect.width, internal_pickRect.height);
    System.out.println("pick at: ");
    System.out.println("view coords: " + pickRect);
    System.out.println("tier coords: " + internal_pickRect);

    pick_glyph.setColor(Color.black);
    this.addChild(pick_glyph);
  }

  public boolean hasFixedPixelHeight() {
    return fixed_pixel_height;
  }

  public void setFixedPixelHeight(boolean b) {
    fixed_pixel_height = b;
  }

  public void setFixedPixHeight(int pix_height) {
    fixedPixHeight = pix_height;
  }

  public int getFixedPixHeight() {
    return fixedPixHeight;
  }

  /**
   *  WARNING - NOT YET TESTED
   *  This may very well not work at all!
   */
  //xxx TODO
  public void getChildTransform(LinearTwoDimTransform trans) {
    //    LinearTwoDimTransform vt = (LinearTwoDimTransform)view.getTransform();
    // mostly copied from drawChildren() ...
    // keep same X scale and offset, but concatenate internal Y transform
    System.err.println("Getting child transform");
    AffineTransform trans2D = new AffineTransform();
    trans2D.translate(0.0, trans.getOffsetY());
    trans2D.scale(1.0, trans.getScaleY());
    trans2D.translate(1.0, tier_transform.getOffsetY());
    trans2D.scale(1.0, tier_transform.getScaleY());

    trans.setScaleY(trans2D.getScaleY());
    trans.setOffsetY(trans2D.getTranslateY());
  }


}

