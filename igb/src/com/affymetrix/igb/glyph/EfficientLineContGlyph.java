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

import com.affymetrix.genoviz.bioviews.Glyph;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.Rectangle2D;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.genoviz.util.GeometryUtils;

import java.awt.*;

/**
 *  A glyph that displays as a centered line and manipulates children to center on the 
 *  same line.
 *
 *  Very convenient for representing data that has a range but 
 *  has multiple sub-ranges within it, such as genes which have a known intron/exon 
 *  structure.
 *
 *  This is a new version of ImprovedLineContGlyph, 
 *     subclassed from EfficientGlyph instead of Glyph, 
 *     and renamed EfficientLineContGlyph.
 *
 *  Optimized to just draw a filled rect if glyph is small, and skip drawing children
 *
 */
public class EfficientLineContGlyph extends EfficientSolidGlyph  {
  static boolean optimize_child_draw = true;
  static boolean DEBUG_OPTIMIZED_FILL = false;
  boolean move_children = true;

  public void drawTraversal(ViewI view)  {
    if (optimize_child_draw) {
      Rectangle pixelbox = view.getScratchPixBox();
      view.transformToPixels(this, pixelbox);
      if (withinView(view) && isVisible) {
	if (pixelbox.width <=3 || pixelbox.height <=3) {
	  // still ends up drawing children for selected, but in general 
	  //    only a few glyphs are ever selected at the same time, so should be fine
	  if (selected) { drawSelected(view); }  
	  else  { fillDraw(view); }
	}
	else {
	  super.drawTraversal(view);  // big enough to draw children
	}
      }
    }
    else {
      super.drawTraversal(view);  // no optimization, so draw children
    }
  }    

  public void fillDraw(ViewI view) {
    Rectangle pixelbox = view.getScratchPixBox();
    view.transformToPixels(this, pixelbox);
    Graphics g = view.getGraphics();
    //    g.setColor(getBackgroundColor());
    //    g.setColor(color);
    if (DEBUG_OPTIMIZED_FILL) {
      g.setColor(Color.white);
    }
    else {
      g.setColor(color);
    }
    
    pixelbox = fixAWTBigRectBug(view, pixelbox);

    if (pixelbox.width < 1) { pixelbox.width = 1; }
    if (pixelbox.height < 1) { pixelbox.height = 1; }
    // draw the box
    g.fillRect(pixelbox.x, pixelbox.y, pixelbox.width, pixelbox.height);

    super.draw(view);
  }
    
  public void draw(ViewI view) {
    Rectangle pixelbox = view.getScratchPixBox();
    view.transformToPixels(this, pixelbox);
    if (pixelbox.width == 0) { pixelbox.width = 1; }
    if (pixelbox.height == 0) { pixelbox.height = 1; }
    Graphics g = view.getGraphics();
    g.setColor(getBackgroundColor());

    pixelbox = fixAWTBigRectBug(view, pixelbox);

    // We use fillRect instead of drawLine, because it may be faster.
    g.fillRect(pixelbox.x, pixelbox.y+pixelbox.height/2, pixelbox.width, 1);

    super.draw(view);
  }

  /**
   *  If {@link #isMoveChildren()}, forces children to center on line.
   */
  public void addChild(GlyphI glyph) {
    if (isMoveChildren()) {
      // child.cbox.y is modified, but not child.cbox.height)
      // center the children of the LineContainerGlyph on the line
      Rectangle2D cbox = glyph.getCoordBox();
      double ycenter = this.y + this.height/2;
      cbox.y = ycenter - cbox.height/2;
    }
    super.addChild(glyph);
  }


  public boolean hit(Rectangle2D coord_hitbox, ViewI view)  {
    return isVisible ? coord_hitbox.intersects(this) : false;
  }

  /**
   * If true, {@link #addChild(GlyphI)} will automatically center the child vertically.
   * Default is true.
   */
  public boolean isMoveChildren() {
    return this.move_children;
  }  

  /**
   * Set whether {@link #addChild(GlyphI)} will automatically center the child vertically.
   */
  public void setMoveChildren(boolean move_children) {
    this.move_children = move_children;
  }
}
