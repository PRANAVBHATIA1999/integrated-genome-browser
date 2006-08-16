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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.affymetrix.genoviz.bioviews.ViewI;

/**
 *  A glyph that is drawn as a solid, outlined rectangle.
 *  <p>
 *  This is not intended to be used as a container for other glyphs,
 *  because it will recurse through its children and draw them even if it
 *  is too small for you to see the children.
 */
public class EfficientOutlinedRectGlyph extends EfficientSolidGlyph  {
  Color bgcolor = Color.white;
  
  public void draw(ViewI view) {
    Rectangle pixelbox = view.getScratchPixBox();
    view.transformToPixels(this, pixelbox);
    Graphics g = view.getGraphics();
    g.setColor(getBackgroundColor());
    
    pixelbox = fixAWTBigRectBug(view, pixelbox);
    
    pixelbox.width = Math.max ( pixelbox.width, min_pixels_width );
    pixelbox.height = Math.max ( pixelbox.height, min_pixels_height );
    
    //    g.setColor(getBackgroundColor());
    g.setColor(getColor());
    g.fillRect(pixelbox.x, pixelbox.y, pixelbox.width, pixelbox.height);
    if (pixelbox.width > 2 && pixelbox.height > 2) {
      //      g.setColor(getBackgroundColor().darker());
      g.setColor(bgcolor);
      g.fillRect(pixelbox.x+1, pixelbox.y+1, pixelbox.width-2, pixelbox.height-2);
    }

    super.draw(view);
  }

  /** Sets the outline color; the fill color is automatically calculated as  
   *  a darker shade. 
   */
  public void setColor(Color c) {
    super.setColor(c);
    bgcolor = c.darker();
  }
}
