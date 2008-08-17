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

package com.affymetrix.genoviz.glyph;

import java.awt.*;
import com.affymetrix.genoviz.bioviews.*;
import com.affymetrix.genoviz.util.NeoConstants;

/**
 * A glyph used to display a text string.
 * Not to be confused with {@link LabelGlyph}
 * (which is used to label other glyphs with text).
 */
public class StringGlyph extends SolidGlyph {

  final static Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 12);

  private String str;
  private Font fnt = DEFAULT_FONT;
  private NeoConstants.Placement placement;
  private boolean show_background = false;


  @Override
  public String toString() {
    return ("StringGlyph: string: \""+str+"\"  +coordbox: "+coordbox);
  }

  public StringGlyph (String str) {
    this();
    this.str = str;
  }

  public StringGlyph () {
    placement = NeoConstants.Placement.CENTER;
  }

  public void setString (String str) {
    this.str = str;
  }
  public String getString () {
    return str;
  }

  public void setShowBackground(boolean show) {
    show_background = show;
  }
  public boolean getShowBackground() {
    return show_background;
  }

  public void draw(ViewI view) {
    Graphics g = view.getGraphics();
    g.setPaintMode();
    if ( null != fnt ) {
      g.setFont(fnt);
    }
    FontMetrics fm = g.getFontMetrics();
    int text_width = 0;
    if ( null != str ) {
      text_width = fm.stringWidth(str);
    }
    int text_height = fm.getAscent() + fm.getDescent();
    //int blank_width = fm.charWidth ('z')*2;

    //Rectangle2D view_box = view.getCoordBox();
    view.transformToPixels(coordbox, pixelbox);
    if (placement == NeoConstants.Placement.LEFT) {
      //pixelbox.x = pixelbox.x;
    }
    else if (placement == NeoConstants.Placement.RIGHT) {
      pixelbox.x = pixelbox.x + pixelbox.width - text_width;
    }
    else {
      pixelbox.x = pixelbox.x + pixelbox.width/2 - text_width/2;
    }
    if (placement == NeoConstants.Placement.ABOVE) {
      //pixelbox.y = pixelbox.y;
    }
    else if (placement == NeoConstants.Placement.BELOW) {
      pixelbox.y = pixelbox.y + pixelbox.height;
    }
    else {
      pixelbox.y = pixelbox.y + pixelbox.height/2 + text_height/2;
    }
    pixelbox.width = text_width;
    pixelbox.height = text_height+1; // +1 for an extra pixel below the text
                                     // so letters like 'g' still have at
                                     // least one pixel below them

    if( getShowBackground() ) { // show background
      Color bgc = getBackgroundColor();
      if ( null != bgc ) {
        g.setColor( getBackgroundColor() );
        g.fillRect( pixelbox.x, pixelbox.y - pixelbox.height,
            pixelbox.width, pixelbox.height);
      }
    }


    if ( null != str ) {
      // display string
      g.setColor( getForegroundColor() );
      // define adjust such that: ascent-adjust = descent+adjust
      // (But see comment above about the extra -1 pixel)
      int adjust = (int) ((fm.getAscent()-fm.getDescent())/2.0) -1;
      g.drawString (str, pixelbox.x, pixelbox.y -pixelbox.height/2+adjust);
    }

    super.draw(view);
  }

  /** Sets the font.  If you attemt to set the font to null, it will set itself
   *  to a default font.
   */
  public void setFont(Font f) {
    if (f==null) {
      this.fnt = DEFAULT_FONT;
    } else {
      this.fnt = f;
    }
  }

  public Font getFont() {
    return this.fnt;
  }

/**
 * Sets alignment of text inside the coordbox.
 * @param placement {@link com.affymetrix.genoviz.util.NeoConstants.Placement#CENTER},
 * {@link com.affymetrix.genoviz.util.NeoConstants.Placement#LEFT}, or {@link com.affymetrix.genoviz.util.NeoConstants.Placement#RIGHT}.
 */
  public void setPlacement(NeoConstants.Placement placement) {
    this.placement = placement;
  }

/**
 * Alignment of text inside the coordbox.
 * @return {@link com.affymetrix.genoviz.util.NeoConstants.Placement#CENTER},
 * {@link com.affymetrix.genoviz.util.NeoConstants.Placement#LEFT}, or 
 * {@link com.affymetrix.genoviz.util.NeoConstants.Placement#RIGHT}.
 */
  public NeoConstants.Placement getPlacement() {
    return placement;
  }

  /**
   * @deprecated use {@link #setForegroundColor}.
   * Also see {@link #setBackgroundColor}.
   */
  @Deprecated
  public void setColor( Color c ) {
    setForegroundColor( c );
  }

  /**
   * @deprecated use {@link #getForegroundColor}.
   * Also see {@link #setBackgroundColor}.
   */
  @Deprecated
  public Color getColor() {
    return getForegroundColor();
  }

}
