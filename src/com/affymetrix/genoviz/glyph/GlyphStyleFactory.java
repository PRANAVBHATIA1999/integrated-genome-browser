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

package com.affymetrix.genoviz.glyph;

import java.util.*;
import java.awt.Color;
import java.awt.Font;

/**
 * produces {@link GlyphStyle}s.
 * The factory uses the Flyweight pattern producing Objects
 * that are shared by multiple glyphs.
 */
//TODO: Get rid of background/foreground color distinctions.
public class GlyphStyleFactory {

  private static final Font defaultFont = new Font("Courier", Font.PLAIN, 12);
  private final Hashtable<GlyphStyle,GlyphStyle> styles;

  public GlyphStyleFactory() {
    styles = new Hashtable<GlyphStyle,GlyphStyle>();
  };

  /**
   * Produces a style with the default font.
   */
  public GlyphStyle getStyle( Color fg, Color bg ) {
    return getStyle( fg, bg, defaultFont );
  }

  /**
   * Produces a style.
   * If a style already exists with these parameters,
   * return a shared reference.
   * Otherwise create a new object,
   * store it internally,
   * and return a reference to it.
   */
  public GlyphStyle getStyle( Color fg, Color bg, Font fnt ) {
    GlyphStyle new_style = new GlyphStyle( fg, bg, fnt );
    GlyphStyle old_style = styles.get( new_style );

    if( !new_style.equals( old_style ) ) {
      styles.put( new_style, new_style );
      return new_style;
    }
    else {
      return old_style;
    }
  }

  /** Returns the total number of known styles.
   *  This is essentially for debugging.
   * @return The number of style objects that have been created.
   */
  public int getNumberOfStyles() {
    return styles.size();
  }
}
