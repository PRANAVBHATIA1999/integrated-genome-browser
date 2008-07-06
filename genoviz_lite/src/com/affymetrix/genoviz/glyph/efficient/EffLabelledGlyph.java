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

import java.awt.*;

import com.affymetrix.genoviz.bioviews.*;
import com.affymetrix.genoviz.glyph.*;
import com.affymetrix.genoviz.util.GeneralUtils;
import com.affymetrix.genoviz.util.GeometryUtils;
import com.affymetrix.genoviz.util.NeoConstants.Placement;
import java.awt.geom.Rectangle2D;

public class EffLabelledGlyph extends EffSolidGlyph implements LabelledGlyph2  {

  static boolean OUTLINE_PIXELBOX = false;
  static boolean DEBUG_OPTIMIZED_FILL = false;
  static boolean optimize_child_draw = true;

  static Rectangle2D.Double scratch_cbox = new Rectangle2D.Double();

  static int max_char_ypix = 40; // maximum allowed pixel height of chars
  static int max_char_xpix = 30; // maximum allowed pixel width of chars
  static int min_char_ypix = 5;  // minimum allowed pixel height of chars
  static int min_char_xpix = 4;  // minimum allowed pixel width of chars

  // ypix2fonts: index is char height in pixels, entry is Font that gives that char height (or smaller)
  static Font[] ypix2fonts = new Font[max_char_ypix+1];
  // xpix2fonts: index is char width in pixels, entry is Font that gives that char width (or smaller)
  static Font[] xpix2fonts = new Font[max_char_xpix+1];


  static int pixel_separation = 1;
  static double pixels_per_inch = (double)Toolkit.getDefaultToolkit().getScreenResolution();
  static double points_per_inch = 72;
  static double points_per_pixel = points_per_inch / pixels_per_inch;

  boolean show_label = true;
  boolean toggle_by_width = true;
  boolean toggle_by_height = true;

  CharSequence label;
  Placement labelPlacment = Placement.ABOVE;

  static {
    setBaseFont(new Font("Monospaced", Font.PLAIN, 1));
  }
  
  public static void setBaseFont(Font base_fnt) {
    int pntcount = 3;
    while (true) {
      // converting to float to trigger correct deriveFont() method...
      Font newfnt = base_fnt.deriveFont((float)(pntcount));
      FontMetrics fm = GeneralUtils.getFontMetrics(newfnt);
      int text_width = fm.stringWidth("G");
      int text_height = fm.getAscent();

      if (text_width > max_char_xpix || text_height > max_char_ypix) {
        break;
      }
      xpix2fonts[text_width] = newfnt;
      ypix2fonts[text_height] = newfnt;
      pntcount++;
    }
    Font smaller_font = null;
    for (int i=0; i<xpix2fonts.length; i++) {
      if (xpix2fonts[i] != null) {  smaller_font = xpix2fonts[i]; }
      else { xpix2fonts[i] = smaller_font; }
    }
    smaller_font = null;
    for (int i=0; i<ypix2fonts.length; i++) {
      if (ypix2fonts[i] != null) {  smaller_font = ypix2fonts[i]; }
      else { ypix2fonts[i] = smaller_font; }
    }
  }

    @Override
  public void drawTraversal(ViewI view)  {
    if (optimize_child_draw) {
      Rectangle pixelbox = view.getScratchPixBox();
      view.transformToPixels(this, pixelbox);
      if (withinView(view) && isVisible) {
        if ((pixelbox.width <=3) ||
            (pixelbox.height <=3) )  {
          // || (getChildCount() <=0)) {
          // still ends up drawing children for selected, but in general
          //    only a few glyphs are ever selected at the same time, so should be fine
          if (selected) { drawSelected(view); }
          else  { fillDraw(view); }
        }
        else {
          super.drawTraversal(view);  // big enough to draw normal self and children
        }
      }
    }
    else {
      super.drawTraversal(view);  // no optimization, so draw normal self and children
    }
  }

  public void fillDraw(ViewI view) {
    super.draw(view);
    Rectangle pixelbox = view.getScratchPixBox();
    Graphics g = view.getGraphics();
    if (DEBUG_OPTIMIZED_FILL) { g.setColor(Color.white); }
    else { g.setColor(color); }

    if (show_label) {
      scratch_cbox.x = this.x;
      scratch_cbox.width = this.width;
      if (labelPlacment == Placement.ABOVE) {
        scratch_cbox.y = this.y + this.height/2;
        scratch_cbox.height = this.height/2;
      }
      else if (labelPlacment == Placement.BELOW) {
        scratch_cbox.y = this.y;
        scratch_cbox.height = this.height/2;
      }
      view.transformToPixels(scratch_cbox, pixelbox);
    }
    else {
      view.transformToPixels(this, pixelbox);
    }
    
    applyMinimumPixelBounds(pixelbox);
    g.fillRect(pixelbox.x, pixelbox.y, pixelbox.width, pixelbox.height);
  }


    @Override
  public void draw(ViewI view) {
    super.draw(view);
    Rectangle pixelbox = view.getScratchPixBox();
    Graphics g = view.getGraphics();
    view.transformToPixels(this, pixelbox);
    int original_pix_width = pixelbox.width;
    if (pixelbox.width == 0) { pixelbox.width = 1; }
    if (pixelbox.height == 0) { pixelbox.height = 1; }

//    Rectangle compbox = view.getComponentSizeRect();
    Rectangle compbox = view.getComponent().getBounds(); //TODO: Check if this is correct
    if ((pixelbox.x < compbox.x) ||
        ((pixelbox.x + pixelbox.width) > (compbox.x + compbox.width))) {
      pixelbox = GeometryUtils.intersection(compbox, pixelbox, pixelbox);
    }
    if (OUTLINE_PIXELBOX) {
      g.setColor(Color.yellow);
      g.drawRect(pixelbox.x, pixelbox.y, pixelbox.width, pixelbox.height);
    }
    // We use fillRect instead of drawLine, because it may be faster.
    g.setColor(getBackgroundColor());
    if (show_label) {
      if (label != null && (label.length() > 0)) {
        int xpix_per_char = original_pix_width / label.length();
        int ypix_per_char = (pixelbox.height/2 - pixel_separation);
        // only draw if there is enough width for smallest font
        if ((xpix_per_char >= min_char_xpix) && (ypix_per_char >= min_char_ypix))  {
          if (xpix_per_char > max_char_xpix) { xpix_per_char = max_char_xpix; }
          if (ypix_per_char > max_char_ypix) { ypix_per_char = max_char_ypix; }
          // Graphics2D g2 = (Graphics2D)g;
          Graphics g2 = g;
          Font xmax_font = xpix2fonts[xpix_per_char];
          Font ymax_font = ypix2fonts[ypix_per_char];
          Font chosen_font = (xmax_font.getSize()<ymax_font.getSize()) ? xmax_font : ymax_font;
          g2.setFont(chosen_font);
          FontMetrics fm = g2.getFontMetrics();
          int text_width = fm.stringWidth(label.toString());
          int text_height = fm.getAscent(); // trying to fudge a little (since ascent isn't quite what I want)

          if (((! toggle_by_width) ||
               (text_width <= pixelbox.width))  &&
              ((! toggle_by_height) ||
               (text_height <= (pixel_separation + pixelbox.height/2))) )  {
            int xpos = pixelbox.x + (pixelbox.width/2) - (text_width/2);
            if (Placement.ABOVE.equals(labelPlacment)) {
              g2.drawString(label.toString(), xpos,
                            //                       pixelbox.y + text_height);
                            pixelbox.y + pixelbox.height/2 - pixel_separation - 2);
            }
            else if (Placement.BELOW.equals(labelPlacment)) {
              g2.drawString(label.toString(), xpos,
                            pixelbox.y + pixelbox.height/2 + text_height + pixel_separation - 1);
            }
          }
        }
      }
    }
  }


  public boolean hit(Rectangle2D coord_hitbox, ViewI view)  {
    return isVisible?coord_hitbox.intersects(this):false;
  }

  @Override
  public Placement getLabelLocation() { 
      return labelPlacment; 
  }

  @Override
  public void setLabelLocation(Placement loc) {
    labelPlacment = loc;
  }

  @Override
  public boolean getShowLabel() { 
      return show_label; 
  }
  @Override
  public void setShowLabel(boolean b) {
    show_label = b;
  }

  @Override
  public CharSequence getLabel() { return label; }
  @Override
  public void setLabel(CharSequence label) { this.label = label; }
}
