/**
*   Copyright (c) 1998-2005 Affymetrix, Inc.
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

import java.util.*;
import java.awt.Color;
import java.awt.Font;
import com.affymetrix.genoviz.glyph.GlyphStyle;
import com.affymetrix.genoviz.util.NeoColorMap;

/**
 * produces {@link GlyphStyle}s.
 * The factory uses the Flyweight pattern producing Objects
 * that are shared by multiple glyphs.
 */
public class GlyphStyleFactory {

  private static Font default_font = new Font("Courier", Font.PLAIN, 12);
  private Hashtable styles;

  public GlyphStyleFactory() {
    styles = new Hashtable();
  };

  public String debug_str( Color fg, Color bg, Font fnt ) {
    NeoColorMap m = NeoColorMap.getColorMap();
    return ( m.getColorName( fg ) + m.getColorName( bg ) + fnt.getName() );
  }

  /**
   * produces a style.
   * If a style already exists with these parameters,
   * return a shared reference.
   * Otherwise create a new object,
   * store it internally,
   * and return a reference to it.
   */
  public GlyphStyle getStyle( Color fg, Color bg ) {
    return getStyle( fg, bg, default_font );
  }
  public GlyphStyle getStyle( Color fg, Color bg, Font fnt ) {
    GlyphStyle new_style = new GlyphStyle( fg, bg, fnt );
    GlyphStyle old_style = ( GlyphStyle )styles.get( new_style );

    if( !new_style.equals( old_style ) ) {
      styles.put( new_style, new_style );
      return new_style;
    }
    else {
      return old_style;
    }

  }

}
