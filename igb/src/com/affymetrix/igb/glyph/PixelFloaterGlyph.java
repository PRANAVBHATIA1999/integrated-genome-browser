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
import com.affymetrix.genoviz.bioviews.*;

/**
 * The PixelFloaterGlyph is meant to be a container / wrapper for glyphs that wish to 
 *   hold their position in pixel space.
 * Any descendants in a PixelFloaterGlyph will be drawn as if the view maps directly 
 *   to pixels in X, Y, or both -- in other words the view's transform is the identity transform 
 *   in one or both dimensions.
 * The PixelFloaterGlyph also reimplements intersects, etc., so that the 
 *   PixelFloaterGlyph is always intersected by anything that intersects the 
 *   current view (the View's viewbox).
 */
public class PixelFloaterGlyph extends Glyph  {
  LinearTransform childtrans = new LinearTransform();
  Rectangle2D view_pix_box = new Rectangle2D();
  boolean OUTLINE_BOUNDS = false;
  boolean XPIXEL_FLOAT = false;
  boolean YPIXEL_FLOAT = true;

  /**
   *  Should only have to modify view to set Y part of transform to identity 
   *     transform.
   *  not sure if need to set view's coord box...
   */
  public void drawTraversal(ViewI view) {
    LinearTransform vtrans = (LinearTransform)view.getTransform();
    Rectangle2D vbox = view.getCoordBox();
    Rectangle pbox = view.getPixelBox();
    //    yidentity.setScaleX(vtrans.getScaleX());
    //    yidentity.setOffsetX(vtrans.getOffsetX());
    setChildTransform(view);
    view_pix_box.reshape(vbox.x, (double)pbox.y, 
    			 vbox.width, (double)pbox.height);
    //    view.setTransform(yidentity);
    view.setTransform(childtrans);
    view.setCoordBox(view_pix_box);
    super.drawTraversal(view);
    view.setTransform(vtrans);
    view.setCoordBox(vbox);
  }

  public void draw(ViewI view) {
    if (OUTLINE_BOUNDS) {
      Graphics g = view.getGraphics();
      g.setColor(Color.yellow);
      Rectangle2D cbox = this.getCoordBox();
      g.drawRect(view.getPixelBox().x + 1, (int)cbox.y, 
		 view.getPixelBox().width - 2, (int)cbox.height);
    }
  }

  protected void setChildTransform(ViewI view)  {
    LinearTransform vtrans = (LinearTransform)view.getTransform();
    if (YPIXEL_FLOAT) {
      childtrans.setScaleY(1.0);
      childtrans.setOffsetY(0.0);
    }
    else {
      childtrans.setScaleY(vtrans.getScaleY());
      childtrans.setOffsetY(vtrans.getOffsetY());
    }
    if (XPIXEL_FLOAT) {
      childtrans.setScaleX(1.0);
      childtrans.setOffsetX(0.0);
    }
    else {
      childtrans.setScaleX(vtrans.getScaleX());
      childtrans.setOffsetX(vtrans.getOffsetX());
    }
  }

  public boolean intersects(Rectangle rect)  {
    return isVisible;
  }
  public boolean intersects(Rectangle2D rect, ViewI view)  {
    return isVisible;
  }
  public boolean withinView(ViewI view) {
    return true;
  }

  Rectangle scratchRect = new Rectangle();
  public void pickTraversal(Rectangle2D pickRect, Vector pickVector,
                            ViewI view)  {
    LinearTransform vtrans = (LinearTransform)view.getTransform();
    double cached_y = pickRect.y;
    double cached_height = pickRect.height;
    Rectangle2D vbox = view.getCoordBox();
    Rectangle pbox = view.getPixelBox();
    view_pix_box.reshape(vbox.x, (double)pbox.y, 
    			 vbox.width, (double)pbox.height);

    view.transformToPixels(pickRect, scratchRect);
    pickRect.y = (double)scratchRect.y;
    pickRect.height = (double)scratchRect.height;

    //    yidentity.setScaleX(vtrans.getScaleX());
    //    yidentity.setOffsetX(vtrans.getOffsetX());
    //    view.setTransform(yidentity);
    setChildTransform(view);
    view.setTransform(childtrans);

    view.setCoordBox(view_pix_box);

    super.pickTraversal(pickRect, pickVector, view);

    pickRect.y = cached_y;
    pickRect.height = cached_height;
    view.setTransform(vtrans);
    view.setCoordBox(vbox);
    
  }

  public void getChildTransform(ViewI view, LinearTransform trans) {
    //    trans.copyTransform(identity);
    setChildTransform(view);
    trans.copyTransform(childtrans);
  }

}
