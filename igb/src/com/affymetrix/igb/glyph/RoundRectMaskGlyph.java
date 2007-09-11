/**
*   Copyright (c) 2007 Affymetrix, Inc.
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

import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.genoviz.util.GeometryUtils;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A glyph that is drawn as a outlined rounded rectangle with a transparent
 * center in the shape of a rounded rectangle, through which the 
 * children can be seen.  Outside of the rounded rectangle, all children
 * are clipped (and thus invisible).  This can be used, for instance, as
 * a glyph representing one arm of a chromosome, with the cytobands
 * as children.
 */
public class RoundRectMaskGlyph extends EfficientGlyph  {

  RoundRectangle2D rr2d = new RoundRectangle2D.Double();
    
  Shape getShapeInPixels(ViewI view) {
    Rectangle pixelbox = view.getScratchPixBox();
    view.transformToPixels(this, pixelbox);

    pixelbox = fixAWTBigRectBug(view, pixelbox);

    if (pixelbox.width == 0) { pixelbox.width = 1; }
    if (pixelbox.height == 0) { pixelbox.height = 1; }
    Graphics g = view.getGraphics();
    g.setColor(getBackgroundColor());

    // temp fix for AWT drawing bug when rect gets too big -- GAH 2/6/98
    Rectangle compbox = view.getComponentSizeRect();
    pixelbox = GeometryUtils.intersection(compbox, pixelbox, pixelbox);

    //Point arcSize = view.transformToPixels(new Point2D(arcWidth, arcHeight), new Point());
    int arcSize = Math.min(pixelbox.width, pixelbox.height);
    rr2d.setRoundRect(pixelbox.x, pixelbox.y, pixelbox.width, pixelbox.height,
      arcSize, arcSize);
    
    return rr2d;
  }

  public void draw(ViewI view) {    
    Shape s = getShapeInPixels(view);
    
    Graphics2D g2 = (Graphics2D) view.getGraphics();

    g2.setColor(getColor());
    g2.setStroke(new BasicStroke(2));
    g2.draw(s);
  }
  
  public void drawChildren(ViewI view) {    
    Shape s = getShapeInPixels(view);
    
    Graphics2D g2 = (Graphics2D) view.getGraphics();

    Shape oldClip = g2.getClip();
    g2.setClip(s);
    super.drawChildren(view);
    g2.setClip(oldClip);
  }
}
