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
package com.affymetrix.genoviz.glyph.efficient;

import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.ViewI;

import java.awt.*;
import java.awt.geom.Rectangle2D;

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
 *     and renamed EffLineContGlyph.
 *
 *  Optimized to just draw a filled rect if glyph is small, and skip drawing children
 *
 */
public class EffLineContGlyph extends EffSolidGlyph  {
  static boolean optimize_child_draw = true;
  static boolean DEBUG_OPTIMIZED_FILL = false;
  boolean move_children = true;

  @Override
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

    if (pixelbox.width < min_pixels_width) { pixelbox.width = min_pixels_width; }
    if (pixelbox.height < min_pixels_height) { pixelbox.height = min_pixels_height; }
    // draw the box

    g.fillRect(pixelbox.x, pixelbox.y, pixelbox.width, pixelbox.height);

    super.draw(view);
  }

  @Override
  public void draw(ViewI view) {
    Rectangle pixelbox = view.getScratchPixBox();
    view.transformToPixels(this, pixelbox);
    
    applyMinimumPixelBounds(pixelbox);

    Graphics g = view.getGraphics();
    g.setColor(getBackgroundColor());

    // We use fillRect instead of drawLine, because it may be faster.
    g.fillRect(pixelbox.x, pixelbox.y+pixelbox.height/2, pixelbox.width, 1);

    super.draw(view);
  }

  /**
   *  If {@link #isMoveChildren()}, forces children to center on line.
   */
  @Override
  public void addChild(GlyphI glyph) {
    if (isMoveChildren()) {
      double child_height = adjustChild(glyph);
      super.addChild(glyph);
      if (child_height > this.height) {
        this.height = child_height;
        adjustChildren();
      }
    } else {
      super.addChild(glyph);
    }
  }


  @Override
  public boolean hit(Rectangle2D.Double coord_hitbox, ViewI view)  {
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

  protected double adjustChild(GlyphI child) {
    if (isMoveChildren()) {
      // child.cbox.y is modified, but not child.cbox.height)
      // center the children of the LineContainerGlyph on the line
      final Rectangle2D.Double cbox = child.getCoordBox();
      final double ycenter = this.y + this.height/2;
      // use moveAbsolute or moveRelative to make sure children of "child" glyph also get moved
      child.moveRelative(0, ycenter - cbox.height/2 - cbox.y);
      return cbox.height;
    } else {
      return this.height;
    }
  }

  protected void adjustChildren() {
    double max_height = 0.0;
    if (isMoveChildren()) {
      java.util.List childlist = this.getChildren();
      if (childlist != null) {
        int child_count = this.getChildCount();
        for (int i=0; i<child_count; i++) {
          GlyphI child = (GlyphI)childlist.get(i);
          double child_height = adjustChild(child);
          max_height = Math.max(max_height, child_height);
        }
      }
    }
    if (max_height > this.height) {
      this.height = max_height;
      adjustChildren(); // have to adjust children again after a height change.
    }
  }

  @Override
  public void pack() {
    if ( isMoveChildren()) {
      this.adjustChildren();
      // Maybe now need to adjust size of total glyph to take into account
      // any expansion of the children ?
    } else {
      super.pack();
    }
  }
}
