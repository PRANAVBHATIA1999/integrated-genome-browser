/**
*   Copyright (c) 2001-2005 Affymetrix, Inc.
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
package com.affymetrix.igb.view;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.symmetry.*;
import com.affymetrix.genoviz.bioviews.*;
import com.affymetrix.genoviz.event.*;
import com.affymetrix.genoviz.widget.*;
import com.affymetrix.igb.glyph.GraphGlyph;
import com.affymetrix.igb.tiers.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.*;
import javax.swing.*;

/**
 *  A MouseListener for the SeqMapView.
 *
 *  This handles selection by clicking, section by rubber-banding, and the
 *  decision about when to pop-up a menu.
 *
 *  It was necessary to deviate somewhat from "best-practice" standards about
 *  how to check for the pop-up trigger and whether things happen on
 *  mousePressed() or mouseReleased() and detection of "right" mouse clicks.
 *  This is because the GenoViz SDK RubberBand interferes with some possibilities.
 *  
 *  For example, we always show the popup during mouseReleased(), never
 *  mousePressed(), because that would interfere with the rubber band.
 *  For Windows users, this is the normal behavior anyway.  For Mac and Linux
 *  users, it is not standard, but should be fine.
 */
public class SeqMapViewMouseListener implements MouseListener, NeoRubberBandListener {


  // This flag determines whether selection events are processed on
  //  mousePressed() or mouseReleased().
  //
  // Users normally expect something to happen on mousePressed(), but
  // if updateWidget() is done in mousePressed(), it can occasionally make 
  // the rubber band draw oddly.
  //
  // A solution is to move all mouse event processing into mouseReleased(),
  // as was done in earlier versions of IGB.  But since most applications
  // respond to mousePressed(), users expect something to happen then.
  //
  // A better solution would be to fix the rubber band drawing routines
  // so that they respond properly after updateWidget()
  //
  // The program should work perfectly fine with this flag true or false, 
  // the rubber band simply looks odd sometimes (particularly with a fast drag) 
  // if this flag is true.
  private boolean SELECT_ON_MOUSE_PRESSED = false;

  SeqMapView smv;
  AffyTieredMap map;

  SeqMapViewMouseListener(SeqMapView smv) {
    this.smv = smv;
    this.map = smv.map;
  }

  public void mouseEntered(MouseEvent evt) { }

  public void mouseExited(MouseEvent evt) { }

  public void mouseClicked(MouseEvent evt) { }

  public void mousePressed(MouseEvent evt) {
    if (smv.map_auto_scroller != null) {
      smv.toggleAutoScroll(); // turn OFF autoscroll
    }

    if (SELECT_ON_MOUSE_PRESSED) processSelections(evt);
  }

  public void mouseReleased(MouseEvent evt) {

    if (! SELECT_ON_MOUSE_PRESSED) processSelections(evt);

    //  do popup in mouseReleased() so it doesn't interfere with rubber band
    if ((isOurPopupTrigger(evt)) &&
    ( ! (smv.last_selected_glyph instanceof GraphGlyph)) )   {
      smv.showPopup((NeoMouseEvent) evt);
    }
  }

  void processSelections(MouseEvent evt) {

    if (! (evt instanceof NeoMouseEvent)) { return; }
    NeoMouseEvent nevt = (NeoMouseEvent)evt;

    Point2D.Double zoom_point = new Point2D.Double(nevt.getCoordX(), nevt.getCoordY());

    GlyphI topgl = null;
    if (! nevt.getItems().isEmpty()) {
      topgl = (GlyphI) nevt.getItems().lastElement();
      topgl = zoomCorrectedGlyphChoice(topgl, zoom_point);
    }

    // Normally, clicking will clear previons selections before selecting new things.
    // but we preserve the current selections if:
    //  shift (Add To) or alt (Toggle) or pop-up (button 3) is being pressed
    boolean preserve_selections = 
      (isAddToSelectionEvent(nevt) || isToggleSelectionEvent(nevt) || isOurPopupTrigger(nevt));

    // Special case:  if pop-up button is pressed on top of a single item and
    // that item is not already selected, then do not preserve selections
    if (topgl != null && isOurPopupTrigger(nevt)) {
        if (isAddToSelectionEvent(nevt)) {
          // This particular special-special case is really splitting hairs....
          // It would be ok to get rid of it.
          preserve_selections = true;
        } else if (! map.getSelected().contains(topgl)) {
          // This is the important special case.  Needs to be kept.
          preserve_selections = false;
        }
    }

    if ( ! preserve_selections) {
      smv.clearSelection(); // Note that this also clears the selected sequence        
    }

    // seems no longer needed
    //map.removeItem(match_glyphs);  // remove all match glyphs in match_glyphs vector

    if (topgl != null) {
      if (isToggleSelectionEvent(nevt) && map.getSelected().contains(topgl)) {
        map.deselect(topgl);
        smv.last_selected_glyph = null;
        smv.last_selected_sym = null;
      }
      else {
        map.select(topgl);
        smv.last_selected_glyph = topgl;
        if (smv.last_selected_glyph.getInfo() instanceof SeqSymmetry) {
          smv.last_selected_sym = (SeqSymmetry)smv.last_selected_glyph.getInfo();
        }
        else {
          smv.last_selected_sym = null;
        }
      }
    }

    if (smv.show_edge_matches)  {
      smv.doEdgeMatching(map.getSelected(), false);
    }
    smv.setZoomSpotX(zoom_point.getX());
    smv.setZoomSpotY(zoom_point.getY());

    map.updateWidget(); 

    smv.postSelections();      
  }

  /**
   *  Tries to determine the glyph you really wanted to choose based on the
   *  one you clicked on.  Usually this will be the glyph you clicked on,
   *  but when the zoom level is such that the glyph is very small, this
   *  assumes you probably wanted to pick the parent glyph rather than
   *  one of its children.
   *
   *  @param topgl a Glyph
   *  @param zoom_point  the location where you clicked; if the returned glyph
   *   is different from the given glyph, the returned zoom_point will be
   *   at the center of that returned glyph, otherwise it will be unmodified.
   *   This parameter should not be supplied as null. 
   *  @return a Glyph, and also modifies the value of zoom_point
   */
  GlyphI zoomCorrectedGlyphChoice(GlyphI topgl, java.awt.geom.Point2D.Double zoom_point) {
    if (topgl == null) { return null; }
    // trying to do smarter selection of parent (for example, transcript)
    //     versus child (for example, exon)
    // calculate pixel width of topgl, if <= 2, and it has no children,
    //   and parent glyphs has pixel width <= 10, then select parent instead of child..
    Rectangle pbox = new Rectangle();
    Rectangle2D cbox = topgl.getCoordBox();
    map.getView().transformToPixels(cbox, pbox);

    if (pbox.width <= 2) {
      // if the selection is very small, move the x_coord to the center
      // of the selection so we can zoom-in on it.
      zoom_point.x = cbox.x + cbox.width/2;
      zoom_point.y = cbox.y + cbox.height/2;
    }

    if ((pbox.width <= 2) && (topgl.getChildCount() == 0) && (topgl.getParent() != null) ) {
      // Watch for null parents:
      // The reified Glyphs of the FlyweightPointGlyph made by OrfAnalyzer2 can have no parent
      cbox = topgl.getParent().getCoordBox();
      map.getView().transformToPixels(cbox, pbox);
      if (pbox.width <= 10) {
        topgl = topgl.getParent();
        if (pbox.width <= 2) { // Note: this pbox has new values than those tested above
          // if the selection is very small, move the x_coord to the center
          // of the selection so we can zoom-in on it.
          zoom_point.x = cbox.x + cbox.width/2;
          zoom_point.y = cbox.y + cbox.height/2;
        }
      }
    }

    return topgl;
  }


  /** Checks whether the mouse event is something that we consider to be
   *  a pop-up trigger.  (This has nothing to do with MouseEvent.isPopupTrigger()).
   *  Checks for isMetaDown() and isControlDown() to try and
   *  catch right-click simulation for one-button mouse operation on Mac OS X.
   */
  static boolean isOurPopupTrigger(MouseEvent evt) {
    if (evt == null) {return false;}
    else if (isToggleSelectionEvent(evt)) return false;
    else return (evt.isControlDown() ||  evt.isMetaDown() || 
         ((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0 ));
  }

  /** Checks whether this the sort of mouse click that should preserve 
      and add to existing selections.  */
  static boolean isAddToSelectionEvent(MouseEvent evt) {
    return (evt != null && (evt.isShiftDown()));
  }

  /** Checks whether this the sort of mouse click that should toggle selections. */
  static boolean isToggleSelectionEvent(MouseEvent evt) {
    //Make sure this does not conflict with pop-up trigger
    boolean b = (evt != null && evt.isControlDown() && evt.isShiftDown());
    return (b);
  }

  private transient MouseEvent rubber_band_start = null;

  public void rubberBandChanged(NeoRubberBandEvent evt) {        
    /*
     * Note that because using SmartRubberBand, rubber banding will only happen
     *   (and NeoRubberBandEvents will only be received) when the orginal mouse press to
     *    start the rubber band doesn't land on a hitable glyph
     */

    if (isOurPopupTrigger(evt)) { 
      return;
      // This doesn't stop the rubber band from being drawn, because you would
      // have to do that inside the SmartRubberBand itself.  But if you don't
      // have this return statement here, it is possible for the selections
      // reported in the pop-up menu to differ from what appears to be selected
      // visually.  This is because the mouseReleased event can get processed
      // before the selection happens here through the rubber-band methods
    }

    if (evt.getID() == NeoRubberBandEvent.BAND_START) {
      rubber_band_start = evt;
    }
    if (evt.getID() == NeoRubberBandEvent.BAND_END) {
      Rectangle2D cbox = new Rectangle2D();
      Rectangle pbox = evt.getPixelBox();
      map.getView().transformToCoords(pbox, cbox);

      TierGlyph axis_tier = smv.getAxisTier();
      boolean started_in_axis_tier = (rubber_band_start != null ) &&
        (axis_tier != null) &&
        axis_tier.inside(rubber_band_start.getX(), rubber_band_start.getY());

      if (started_in_axis_tier) {
        // started in axis tier: user is trying to select sequence residues

        if (pbox.width >= 2 && pbox.height >=2) {
          int seq_select_start = (int)Math.round(cbox.x);
          int seq_select_end = (int)Math.round(cbox.x + cbox.width);

          SeqSymmetry new_region = new SingletonSeqSymmetry(seq_select_start, seq_select_end, smv.aseq);
          smv.setSelectedRegion(new_region);
          smv.last_selected_sym = new_region;
          smv.last_selected_glyph = smv.seq_glyph;
          //	map.updateWidget();
        }
        else {
          // This is optional: clear selected region if drag is very small distance
          smv.setSelectedRegion(null);
        }

      } else {
        // started outside axis tier: user is trying to select glyphs

        doTheSelection(map.getItemsByCoord(cbox), rubber_band_start);
      }
      smv.setZoomSpotX(cbox.x + cbox.width);
      smv.setZoomSpotY(cbox.y + cbox.height);

      rubber_band_start = null; // for garbage collection
    }
  }

  void doTheSelection(GlyphI glyph, MouseEvent evt) {
    Vector v = new Vector(1);
    v.add(glyph);
    doTheSelection(v, evt);
  }

  void doTheSelection(Vector glyphs, MouseEvent evt) {

    if (isToggleSelectionEvent(evt)) {
      toggleSelections(map, glyphs);
    } else if (isAddToSelectionEvent(evt)) {
      map.select(glyphs);
    } else {
      smv.clearSelection();
      map.select(glyphs);
    }
    if (smv.show_edge_matches) {
      smv.doEdgeMatching(map.getSelected(), false);
    }
    map.updateWidget();

    smv.postSelections();
  }

  void toggleSelections(NeoMap map, Collection glyphs) {
    java.util.List current_selections = map.getSelected();
    Iterator iter = glyphs.iterator();
    while (iter.hasNext()) {
      GlyphI g = (GlyphI) iter.next();
      if (current_selections.contains(g)) {
        map.deselect(g);
      } else {
        map.select(g);
      }
    }
  }

}
