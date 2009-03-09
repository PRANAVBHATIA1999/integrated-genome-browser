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

package com.affymetrix.genoviz.pack;

import com.affymetrix.genoviz.transform.LinearTwoDimTransform;
import com.affymetrix.genoviz.tiers.*;
import java.awt.*;
import java.util.*;
import com.affymetrix.genoviz.bioviews.*;
import com.affymetrix.genoviz.glyph.LabelGlyph;
import com.affymetrix.genoviz.util.GeometryUtils;
import com.affymetrix.genoviz.util.NeoConstants.Direction;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class EfficientExpandPacker extends ExpandPacker {
  @Override
  public Rectangle pack(GlyphI parent) {

    //    System.out.println("begin ExpandPacker.pack(glyph, view)");
    if (! (parent instanceof TierGlyph)) {
      throw new RuntimeException("EfficientExpandPacker can currently only work as packer for TierGlyph");
    }
    TierGlyph tier = (TierGlyph)parent;
    List<GlyphI> sibs = tier.getChildren();
    if (sibs == null || sibs.size() <= 0) { return null; }  // return if nothing to pack

    GlyphI child;
    Rectangle2D.Double cbox;
    Rectangle2D.Double pbox = tier.getCoordBox();
    // resetting height of tier to just spacers
    tier.setCoords(pbox.x, 0, pbox.width, 2 * parent_spacer);

    double ymin = Double.POSITIVE_INFINITY;
    double ymax = Double.NEGATIVE_INFINITY;
    double prev_xmax = Double.NEGATIVE_INFINITY;
    int sibs_size = sibs.size();

    for (int index=0; index<sibs_size; index++) {
      child = sibs.get(index);
      cbox = child.getCoordBox();
      boolean prev_overlap = (prev_xmax > cbox.x);
      if (! (child instanceof LabelGlyph)) {
	pack(tier, child, index, prev_overlap);
      }
      ymin = Math.min(cbox.y, ymin);
      ymax = Math.max(cbox.y + cbox.height, ymax);
      prev_xmax = Math.max(cbox.x + cbox.width, prev_xmax);
    }

    // ensure that tier is expanded/shrunk vertically to just fit its children (+ spacers)
    //   (maybe can get rid of this, since also handled for each child pack in pack(tier, child, view))

    // move children so "top" edge (y) of top-most child (ymin) is "bottom" edge 
    //    (y+height) of bottom-most (ymax) child is at 
    sibs = tier.getChildren();
    pbox = tier.getCoordBox();

    double coord_height = ymax - ymin;
    coord_height = coord_height + (2 * parent_spacer);
    for (int i=0; i<sibs.size(); i++) {
      child = sibs.get(i);
      child.moveRelative(0, parent_spacer - ymin);
      //      System.out.println(child.getCoordBox());
    }

    Rectangle2D.Double newbox = new Rectangle2D.Double();
    Rectangle2D.Double tempbox = new Rectangle2D.Double();  
    child = sibs.get(0);
    newbox.setRect(pbox.x, child.getCoordBox().y, 
                   pbox.width, child.getCoordBox().height);
    sibs_size = sibs.size();
    if (STRETCH_HORIZONTAL && STRETCH_VERTICAL) {
      for (int i=1; i<sibs_size; i++) {
	child = sibs.get(i);
	GeometryUtils.union(newbox, child.getCoordBox(), newbox);
      }
    }
    else if (STRETCH_VERTICAL) {
      for (int i=1; i<sibs_size; i++) {
	child = sibs.get(i);
	Rectangle2D.Double childbox = child.getCoordBox();
	tempbox.setRect(newbox.x, childbox.y, newbox.width, childbox.height);
	GeometryUtils.union(newbox, tempbox, newbox);
      }
    }
    else if (STRETCH_HORIZONTAL) {  // NOT YET TESTED
      for (int i=1; i<sibs_size; i++) {
	child = sibs.get(i);
	Rectangle2D.Double childbox = child.getCoordBox();
	tempbox.setRect(childbox.x, newbox.y, childbox.width, newbox.height);
	GeometryUtils.union(newbox, tempbox, newbox);
      }
    }
    newbox.y = newbox.y - parent_spacer;
    newbox.height = newbox.height + (2 * parent_spacer);

    // trying to transform according to tier's internal transform  
    //   (since packing is done base on tier's children)
    if (tier instanceof TransformTierGlyph)  {
      TransformTierGlyph transtier = (TransformTierGlyph)tier;
      LinearTwoDimTransform tier_transform = transtier.getTransform();
      tier_transform.transform(newbox, newbox);
    }

    tier.setCoords(newbox.x, newbox.y, newbox.width, newbox.height);
    //    System.out.println("packed tier, coords are: " + tier.getCoordBox());
    //    System.out.println("end ExpandPacker.pack(glyph, view)");
    return null;
  }

  /*
   *  like pack(parent, child, view, avoid_sibs), except 
   *     uses search ability of TierGlyph to speed up collection of sibs that overlap, 
   *     and also requires index of child in parent to optimize
   * packs a child.
   * This adjusts the child's offset
   * until it no longer reports hitting any of its siblings.
   */
  public Rectangle pack(GlyphI parent, GlyphI child, int child_index, 
    boolean avoid_sibs) {
    TierGlyph tier = (TierGlyph)parent;

    Rectangle2D.Double childbox, siblingbox;
    Rectangle2D.Double pbox = parent.getCoordBox();
    childbox = child.getCoordBox();
    if (movetype == Direction.UP) {
      //      System.out.println("moving up");
      child.moveAbsolute(childbox.x,
                         pbox.y + pbox.height - childbox.height - parent_spacer);
    }
    else {  
      // assuming if movetype != UP then it is DOWN 
      //    (ignoring LEFT, RIGHT, MIRROR_VERTICAL, etc. for now)
      //      System.out.println("moving down");
      child.moveAbsolute(childbox.x, pbox.y+parent_spacer);
    }
    childbox = child.getCoordBox();
    List<GlyphI> sibsinrange = null;
    boolean childMoved = true;
    if (avoid_sibs) {
      sibsinrange = tier.getPriorOverlaps(child_index);
      if (sibsinrange == null) {
        childMoved = false; 
      }
      before.setRect(childbox);
    }
    else {
      childMoved = false;
    }
    while (childMoved) {
      childMoved = false;
      int sibsinrange_size = sibsinrange.size();
      for (int j=0; j<sibsinrange_size; j++) {
        GlyphI sibling = sibsinrange.get(j);
        if (sibling == child) { continue; }
        siblingbox = sibling.getCoordBox();
        if (child.getCoordBox().intersects(siblingbox) ) {
          before.setRect(child.getCoordBox());
	  moveToAvoid(child, sibling, movetype);
	  childMoved |= ! before.equals(child.getCoordBox()); 
        }
      }
    }

    // adjusting tier bounds to encompass child (plus spacer)
    // maybe can get rid of this now?
    //   since also handled in pack(parent, view)
    childbox = child.getCoordBox();
    //     if first child, then shrink to fit...
    if (parent.getChildren().size() <= 1) {
      pbox.y = childbox.y - parent_spacer;
      pbox.height = childbox.height + 2 * parent_spacer;
    }
    else {
      if (pbox.y > (childbox.y - parent_spacer)) {
        double yend = pbox.y + pbox.height;
        pbox.y = childbox.y - parent_spacer;
        pbox.height = yend - pbox.y; 
      }
      if ((pbox.y+pbox.height) < (childbox.y + childbox.height + parent_spacer)) {
        double yend = childbox.y + childbox.height + parent_spacer;
        pbox.height = yend - pbox.y;
      }
    }

    return null;
  }

  /**
   * Packs a child.
   * This adjusts the child's offset
   * until it no longer reports hitting any of its siblings.
   */
  @Override
  public Rectangle pack(GlyphI parent, GlyphI child, boolean avoid_sibs) {
    Rectangle2D.Double childbox, siblingbox;
    Rectangle2D.Double pbox = parent.getCoordBox();
    childbox = child.getCoordBox();
    if (movetype == Direction.UP) {
      //      System.out.println("moving up");
      child.moveAbsolute(childbox.x,
                         pbox.y + pbox.height - childbox.height - parent_spacer);
    }
    else {  
      // assuming if movetype != UP then it is DOWN 
      //    (ignoring LEFT, RIGHT, MIRROR_VERTICAL, etc. for now)
      //      System.out.println("moving down");
      child.moveAbsolute(childbox.x, pbox.y+parent_spacer);
    }
    childbox = child.getCoordBox();
    List<GlyphI> sibsinrange = null;
    boolean childMoved = true;
    List<GlyphI> sibs = parent.getChildren();
    if (sibs == null) { return null; }
    if (avoid_sibs) {
      sibsinrange = new ArrayList<GlyphI>();
      int sibs_size = sibs.size();
      for (int i=0; i<sibs_size; i++) {
	GlyphI sibling = sibs.get(i);
	siblingbox = sibling.getCoordBox();
	if (!(siblingbox.x > (childbox.x+childbox.width) ||
	      ((siblingbox.x+siblingbox.width) < childbox.x)) ) {
	  sibsinrange.add(sibling);
	}
      }
      //      sibsinrange = tier.getIntersectedChildren(query_glyph);
      if (DEBUG_CHECKS)  { System.out.println("sibs in range: " + sibsinrange.size()); }
    
      before.setRect(childbox);
    }
    else {
      childMoved = false;
    }
    while (childMoved) {
      childMoved = false;
      int sibsinrange_size = sibsinrange.size();
      for (int j=0; j<sibsinrange_size; j++) {
        GlyphI sibling = sibsinrange.get(j);
        if (sibling == child) { continue; }
        siblingbox = sibling.getCoordBox();
	if (DEBUG_CHECKS)  { System.out.println("checking against: " + sibling); }
        final Rectangle2D childBox = child.getCoordBox();
        if (childBox.intersects(siblingbox) ) {
	  if (DEBUG_CHECKS)  { System.out.println("hit sib"); }
          before.setRect(childBox);
	  moveToAvoid(child, sibling, movetype);
	  childMoved |= ! before.equals(child.getCoordBox()); 
        }
      }
    }

    // adjusting tier bounds to encompass child (plus spacer)
    // maybe can get rid of this now?
    //   since also handled in pack(parent, view)
    childbox = child.getCoordBox();
    //     if first child, then shrink to fit...
    if (parent.getChildren().size() <= 1) {
      pbox.y = childbox.y - parent_spacer;
      pbox.height = childbox.height + 2 * parent_spacer;
    }
    else {
      if (pbox.y > (childbox.y - parent_spacer)) {
        double yend = pbox.y + pbox.height;
        pbox.y = childbox.y - parent_spacer;
        pbox.height = yend - pbox.y; 
      }
      if ((pbox.y+pbox.height) < (childbox.y + childbox.height + parent_spacer)) {
        double yend = childbox.y + childbox.height + parent_spacer;
        pbox.height = yend - pbox.y;
      }
    }

    return null;
  }


}

