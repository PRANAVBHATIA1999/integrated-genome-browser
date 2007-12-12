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

package com.affymetrix.genoviz.bioviews;

import java.awt.*;
import java.util.*;
import com.affymetrix.genoviz.util.*;

/**
 * Abstract base class for many coordinate-based packers.
 * Concrete children need to implement at least
 * {@link PackerI#pack(GlyphI parent, GlyphI child, ViewI view)}.
 */
public abstract class AbstractCoordPacker implements PackerI, NeoConstants {

  protected boolean DEBUG = false;
  protected boolean DEBUG_CHECKS = false;

  // Please note that several children currently ignore the coord fuzziness setting.
  protected double coord_fuzziness = 1;

  protected double spacing = 2;

  protected NeoConstants.Direction  movetype;
  protected java.awt.geom.Rectangle2D.Double before = new java.awt.geom.Rectangle2D.Double();

  /**
   * constructs a packer that moves glyphs away from the horizontal axis.
   */
  public AbstractCoordPacker() {
    this(NeoConstants.Direction.MIRROR_VERTICAL);
  }

  /**
   * constructs a packer with a given direction to move glyphs.
   *
   * @param movetype indicates which direction the glyph_to_move should move.
   * @see #setMoveType
   */
  public AbstractCoordPacker(NeoConstants.Direction movetype) {
    setMoveType(movetype);
  }

  /**
   * sets the direction this packer should move glyphs.
   *
   * @param movetype indicates which direction the glyph_to_move should move.
   *                 It must be one of UP, DOWN, LEFT, RIGHT,
   *                 MIRROR_VERTICAL, or MIRROR_HORIZONTAL.
   *                 The last two mean "away from the orthoganal axis".
   */
  public void setMoveType(NeoConstants.Direction movetype) {
    this.movetype = movetype;
  }

  public Rectangle pack(GlyphI parent, ViewI view) {
    java.util.List<GlyphI> children = parent.getChildren();
    if (children == null) { return null; }
    for (GlyphI child : parent.getChildren()) {
      pack(parent, child, view);
    }
    return null;
  }

  /**
   *     Sets the fuzziness of hit detection in layout.
   *     This is the minimal distance glyph coordboxes need to be separated by
   *     in order to be considered not overlapping.
   * <p> <em>WARNING: better not make this greater than spacing.</em>
   * <p> Note that since Rectangle2D does not consider two rects
   *     that only share an edge to be intersecting,
   *     will need to have a coord_fuzziness &gt; 0
   *     in order to consider these to be overlapping.
   */
  public void setCoordFuzziness(double fuzz) {
    if (fuzz > spacing) {
      throw new IllegalArgumentException
          ("Can't set packer fuzziness greater than spacing");
    }
    else {
      coord_fuzziness = fuzz;
    }
  }

  public double getCoordFuzziness() {
    return coord_fuzziness;
  }


  /**
   * Sets the spacing desired between glyphs.
   * If glyphB is found to hit glyphA,
   * this is the distance away from glyphA's coordbox
   * that glyphB's coord box will be moved.
   */
  public void setSpacing(double sp) {
    if (sp < coord_fuzziness) {
      throw new IllegalArgumentException
          ("Can't set packer spacing less than fuzziness");
    }
    else {
      spacing = sp;
    }
  }

  public double getSpacing() {
    return spacing;
  }


  /**
   * moves one glyph to avoid another.
   * This is called from subclasses
   * in their <code>pack(parent, glyph, view)</code> methods.
   *
   * @param glyph_to_move
   * @param glyph_to_avoid
   * @param movetype indicates which direction the glyph_to_move should move.
   * @see #setMoveType
   */
  public void moveToAvoid(GlyphI glyph_to_move,
                          GlyphI glyph_to_avoid, 
                          NeoConstants.Direction movetype)  {
    java.awt.geom.Rectangle2D.Double movebox = glyph_to_move.getCoordBox();
    java.awt.geom.Rectangle2D.Double avoidbox = glyph_to_avoid.getCoordBox();

    /*
     * Mirror vertically about the horizontal coordinate axis
     */
    if (movetype == NeoConstants.Direction.MIRROR_VERTICAL) {
      /*
       *  if moving "up", doesn't matter what the glyph_to_avoid's height is,
       *  (that's "down"), but it does matter what the glyph_to_move's
       *  own height is
       */
      if (movebox.y < 0) {
        // move UP (decreasing y)

        // movebox.y = avoidbox.y - movebox.height - spacing;
        // switched to using moveAbsolute to handle packing glyph
        // that have children, since moveAbsolute (and moveRelative)
        // will descend through the glyph hierarchy -- GAH 10-2-97
        glyph_to_move.moveAbsolute(movebox.x,
                           avoidbox.y - movebox.height - spacing);
      }

      /*
       * if moving "down", doesn't matter what the glyph_to_move's height is
       * (that's "up"), but it does matter what the glyph_to_avoid's height is
       */
      else {
        glyph_to_move.moveAbsolute(movebox.x,
                           avoidbox.y + avoidbox.height + spacing);
      }
    }

    /*
     * Mirror horizontally about the vertical coordinate axis
     */
    else if (movetype == NeoConstants.Direction.MIRROR_HORIZONTAL) {
      if (movebox.x < 0) {
        // move LEFT (decreasing x)
        glyph_to_move.moveAbsolute(avoidbox.x - movebox.width - spacing,
                           movebox.y);
      }
      else {
        glyph_to_move.moveAbsolute(avoidbox.x + avoidbox.width + spacing,
                           movebox.y);
      }
    }

    /*
     * "down" means increasing y
     * if moving "down", doesn't matter what the glyph_to_move's height is
     * (that's "up"), but it does matter what the glyph_to_avoid's height is
     */
    else if (movetype == NeoConstants.Direction.DOWN) {
      glyph_to_move.moveAbsolute(movebox.x,
                         avoidbox.y + avoidbox.height + spacing);
    }

    /*
     *  "up" means decreasing y
     *  if moving "up", doesn't matter what the glyph_to_avoid's height is,
     *  (that's "down"), but it does matter what the glyph_to_move's
     *  own height is
     */
    else if (movetype == NeoConstants.Direction.UP) {
      glyph_to_move.moveAbsolute(movebox.x,
                         avoidbox.y - movebox.height - spacing);
    }

    /*
     * "right" means increasing x
     * if moving "right", doesn't matter what the glyph_to_move's width is
     * (that's "left"), but it does matter what the glyph_to_avoid's width is
     */
    else if (movetype == NeoConstants.Direction.RIGHT) {
      glyph_to_move.moveAbsolute(avoidbox.x + avoidbox.width + spacing,
                         movebox.y);
    }

    /*
     *  "left" means decreasing y
     *  if moving "left", doesn't matter what the glyph_to_avoid's width is,
     *  (that's "right"), but it does matter what the glyph_to_move's
     *  own width is
     */
    else if (movetype == NeoConstants.Direction.LEFT) {
      glyph_to_move.moveAbsolute(avoidbox.x - movebox.width - spacing,
                         movebox.y);
    }
    else {
      throw new IllegalArgumentException
          ("movetype must be one of UP, DOWN, LEFT, RIGHT, MIRROR_HORIZONTAL, or MIRROR_VERTICAL");
    }
  }

}
