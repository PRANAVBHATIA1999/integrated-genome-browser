/**
*   Copyright (c) 2001-2006 Affymetrix, Inc.
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

import com.affymetrix.genoviz.bioviews.*;

/** A subclass of EfficientLabelledGlyph that makes all its children
 *  center themselves vertically on the same line.
 */
public class EfficientLabelledLineGlyph extends EfficientLabelledGlyph
   implements LabelledGlyph  {

  boolean move_children = true;

  public void draw(ViewI view) {
    //    super.draw(view);
    Rectangle2D full_view_cbox = view.getFullView().getCoordBox();
    Graphics g = view.getGraphics();

    // perform an intersection of the view and this glyph, in the X axis only.
    scratch_cbox.x = Math.max(this.x, full_view_cbox.x);
    scratch_cbox.width = Math.min(this.x + this.width, full_view_cbox.x + full_view_cbox.width) - scratch_cbox.x;
    scratch_cbox.y = this.y;
    scratch_cbox.height = this.height;

    Rectangle pixelbox = view.getScratchPixBox();
    view.transformToPixels(scratch_cbox, pixelbox);

    int original_pix_width = pixelbox.width;
    if (pixelbox.width == 0) { pixelbox.width = 1; }
    if (pixelbox.height == 0) { pixelbox.height = 1; }

    // We use fillRect instead of drawLine, because it may be faster.
    g.setColor(getBackgroundColor());
    if (show_label) {
      if (getChildCount() <= 0) {
        //        fillDraw(view);
        if (label_loc == NORTH) {
          g.fillRect(pixelbox.x, pixelbox.y+(pixelbox.height/2),
                     pixelbox.width, (int)Math.max(1, pixelbox.height/2));
        } else {
          g.fillRect(pixelbox.x, pixelbox.y,
                     pixelbox.width, (int)Math.max(1, pixelbox.height/2));
        }
      }
      else {
        if (label_loc == NORTH) { // label occupies upper half, so center line in lower half
          g.fillRect(pixelbox.x, pixelbox.y+((3*pixelbox.height)/4), pixelbox.width, 1);
        }
        else if (label_loc == SOUTH)  {  // label occupies lower half, so center line in upper half
          g.fillRect(pixelbox.x, pixelbox.y+(pixelbox.height/4), pixelbox.width, 1);
        }
      }

      if (label != null && (label.length() > 0)) {
	int xpix_per_char = original_pix_width / label.length();
	int ypix_per_char = (pixelbox.height/2 - pixel_separation);
	// only draw if there is enough width for smallest font
	if ((xpix_per_char >= min_char_xpix) && (ypix_per_char >= min_char_ypix))  {
	  if (xpix_per_char > max_char_xpix) { xpix_per_char = max_char_xpix; }
	  if (ypix_per_char > max_char_ypix) { ypix_per_char = max_char_ypix; }

	    Font xmax_font = xpix2fonts[xpix_per_char];
	    Font ymax_font = ypix2fonts[ypix_per_char];
	    Font chosen_font = (xmax_font.getSize()<ymax_font.getSize()) ? xmax_font : ymax_font;
	    //	    Graphics2D g2 = (Graphics2D)g;
	    Graphics g2 = g;
	    g2.setFont(chosen_font);
	    FontMetrics fm = g2.getFontMetrics();

	    int text_width = fm.stringWidth(label);
	    int text_height = fm.getAscent(); // trying to fudge a little (since ascent isn't quite what I want)

	    if (((! toggle_by_width) ||
		 (text_width <= pixelbox.width))  &&
		((! toggle_by_height) ||
		 (text_height <= (pixel_separation + pixelbox.height/2))) )  {
	      int xpos = pixelbox.x + (pixelbox.width/2) - (text_width/2);
	      if (label_loc == NORTH) {
		g2.drawString(label, xpos,
			      //		       pixelbox.y + text_height);
			      pixelbox.y + pixelbox.height/2 - pixel_separation - 2);
	      }
	      else if (label_loc == SOUTH) {
		g2.drawString(label, xpos,
			      pixelbox.y + pixelbox.height/2 + text_height + pixel_separation - 1);
	      }
	    }
	}
      }
    }
    else { // show_label = false, so center line within entire pixelbox
      if (getChildCount() <= 0) {
        g.fillRect(pixelbox.x, pixelbox.y+(pixelbox.height/2),
                   pixelbox.width, (int)Math.max(1, pixelbox.height/2));
      }
      else {
        g.fillRect(pixelbox.x, pixelbox.y+(pixelbox.height/2), pixelbox.width, 1);
      }
    }
  }

  /**
   *  Overriding addChild to force a call to adjustChildren().
   */
  public void addChild(GlyphI glyph) {
    if (isMoveChildren()) {
      double child_height = adjustChild(glyph);
      super.addChild(glyph);
      if (this.height < 2.0 * child_height) {
        this.height = 2.0 * child_height;
        adjustChildren();
      }
    } else {
      super.addChild(glyph);
    }
  }

  protected double adjustChild(GlyphI child) {
    if (isMoveChildren()) {
      // child.cbox.y is modified, but not child.cbox.height)
      // center the children of the LineContainerGlyph on the line
      final Rectangle2D cbox = child.getCoordBox();
      double ycenter;
      // use moveAbsolute or moveRelative to make sure children also get moved
      
      if (show_label) {
        if (label_loc == NORTH) {
          ycenter = this.y + (0.75 * this.height);
          child.moveRelative(0, ycenter - cbox.height/2 - cbox.y);
        } else {
          ycenter = this.y + (0.25 * this.height);
          child.moveRelative(0, ycenter - cbox.height/2 - cbox.y);
        }
      } else {
        ycenter = this.y + this.height * 0.5;
      }
      child.moveRelative(0, ycenter - (cbox.height * 0.5) - cbox.y);      
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
    if (this.height < 2.0 * max_height) {
      this.height = 2.0 * max_height;
      adjustChildren(); // have to adjust children again after a height change.
    }
  }

  public void pack(ViewI view) {
    if ( isMoveChildren()) {
      this.adjustChildren();
      // Maybe now need to adjust size of total glyph to take into account
      // any expansion of the children ?
    } else {
      super.pack(view);
    }
  }

  public void setLabelLocation(int loc) {
    if (loc != getLabelLocation()) {
      adjustChildren();
    }
    super.setLabelLocation(loc);
  }

  public void setShowLabel(boolean b) {
    if (b != getShowLabel()) {
      adjustChildren();
    }
    super.setShowLabel(b);
  }

  /**
   * If true, {@link #addChild(GlyphI)} will automatically center the child vertically.
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
