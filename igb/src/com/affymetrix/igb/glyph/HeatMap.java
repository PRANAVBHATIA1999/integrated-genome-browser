/**
 *   Copyright (c) 2006 Affymetrix, Inc.
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

import com.affymetrix.igb.util.UnibrowPrefsUtil;
import java.awt.Color;
import java.util.*;

public class HeatMap {
  
  /** Name of the Black-and-White Standard HeatMap. */
  public static final String HEATMAP_0 = "Black/White";

  /** Name of the Violet Standard HeatMap. */
  public static final String HEATMAP_1 = "Violet";

  /** Name of the Blue/Yellow Standard HeatMap. */
  public static final String HEATMAP_2 = "Blue/Yellow";

  /** Name of the Red/Green Standard HeatMap. */
  public static final String HEATMAP_3 = "Red/Black/Green";

  /** Name of the second Blue/Yellow Standard HeatMap. */
  public static final String HEATMAP_4 = "Blue/Yellow 2";
  
  /** Name of the Transparent Black-and-White Standard HeatMap. */
  public static final String HEATMAP_T_0 = "Transparent B/W";
  
  /** Name of the Transparent Violet Standard HeatMap. */
  public static final String HEATMAP_T_1 = "Transparent Blue";

  /** Name of the Transparent Blue/Yellow Standard HeatMap. */
  public static final String HEATMAP_T_2 = "Transparent Red";

  /** Name of the Transparent Red/Green Standard HeatMap. */
  public static final String HEATMAP_T_3 = "Transparent Green";

  public static final String PREF_HEATMAP_NAME = "Default Heatmap";
  public static final String def_heatmap_name = HEATMAP_2;
    
  public static String[] HEATMAP_NAMES = {
    HEATMAP_0, HEATMAP_1, HEATMAP_2, HEATMAP_3, HEATMAP_4,
    HEATMAP_T_0, HEATMAP_T_2, HEATMAP_T_3, HEATMAP_T_1
  };
  
  static Map name_to_color_array = new HashMap();  
  
  String name;
  Color[] colors;
  
  public HeatMap(String name, Color[] colors) {
    this.name = name;
    this.colors = colors;
  }
  
  public String getName() {
    return name;
  }
  
  public Color[] getColors() {
    return colors;
  }
  
  /**
   *  Returns one of the standard pre-defined heat maps using the names in
   *  HEATMAP_NAMES, or null if one with the given name does not exist.
   */
  public static HeatMap getStandardHeatMap(String name) {
    HeatMap hm = (HeatMap) name_to_color_array.get(name);
    if (hm == null) {
      int r,g,b;
      int bins = 256;
      Color[] cc = new Color[bins];
      
      if (HEATMAP_0.equals(name)) {
        r=0; g=0; b=0;
        for (int i=0; i<bins; i++) {
          cc[i] = new Color(r++, g++, b++);
        }
      } else if (HEATMAP_1.equals(name)) {
        r=0; g=0; b=0;
        for (int i=0; i<bins; i++) {
          cc[i] = new Color(r++, g, b++);
        }
      } else if (HEATMAP_2.equals(name)) {
        r=0; g=0; b=255;
        for (int i=0; i<bins; i++) {
          cc[i] = new Color(r++, g++, b--);
        }
      } else if (HEATMAP_3.equals(name)) {
        // red-black-green colormap
        for (int i=0; i<bins; i++) {
          r = Math.max(255 - 2*i, 0);
          g = Math.min(Math.max(2 * (i-128), 0), 255);
          b = 0;
          cc[i] = new Color(r, g, b);
        }
      } else if (HEATMAP_4.equals(name)) {
        r=0; g=0; b=128;
        for (int i=0; i<bins; i++) {
          cc[i] = new Color(r++, g++, b);
          if (i % 2 == 0)  { b--; }
        }
      } 
      
      // Now the transparent ones
      else if (HEATMAP_T_0.equals(name)) {
        r=0; g=0; b=0;
        for (int i=0; i<bins; i++) {
          cc[i] = new Color(r++, g++, b++, 128);
        }
      } else if (HEATMAP_T_1.equals(name)) {
        r=0; g=0; b=0;
        for (int i=0; i<bins; i++) {
          cc[i] = new Color(r, g, b++, 128);
        }
      } else if (HEATMAP_T_2.equals(name)) {
        r=0; g=0; b=0;
        for (int i=0; i<bins; i++) {
          cc[i] = new Color(r++, g, b, 128);
        }
      } else if (HEATMAP_T_3.equals(name)) {
        r=0; g=0; b=0;
        for (int i=0; i<bins; i++) {
          cc[i] = new Color(r, g++, b, 128);
        }
      } else {
        cc = null;
      }

      if (cc != null) {
        hm = new HeatMap(name, cc);
        name_to_color_array.put(name, hm);
      }
    }
    
    return hm;
  }
    
  public static HeatMap getDefaultHeatmap() {
    String name = UnibrowPrefsUtil.getTopNode().get(PREF_HEATMAP_NAME, def_heatmap_name);
    
    HeatMap result = getStandardHeatMap(name);
    if (result == null) {
      result = getStandardHeatMap(def_heatmap_name);
    }
    return result;
  }  
  
  /** Make a HeatMap that interpolates linearly between the two given colors. */
  public static HeatMap makeLinearHeatmap(String name, Color low, Color high) {
    Color[] colors = new Color[256];
    HeatMap heat_map = new HeatMap(name, colors);

    for (int i=0; i<256; i++) {
      float x = (i*1.0f)/255.0f;
      int r = (int) ((1.0f - x) * low.getRed() + x * high.getRed());
      int g = (int) ((1.0f - x) * low.getGreen() + x * high.getGreen());
      int b = (int) ((1.0f - x) * low.getBlue() + x * high.getBlue());
      colors[i] = new Color(r, g, b);
    }
    
    return heat_map;
  }
}
