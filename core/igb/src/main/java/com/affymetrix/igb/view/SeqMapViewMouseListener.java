package com.affymetrix.igb.view;

import com.affymetrix.genometry.event.PropertyListener;
import com.affymetrix.genometry.style.ITrackStyleExtended;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SingletonSeqSymmetry;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.event.NeoGlyphDragEvent;
import com.affymetrix.genoviz.event.NeoGlyphDragListener;
import com.affymetrix.genoviz.event.NeoMouseEvent;
import com.affymetrix.genoviz.event.NeoRubberBandEvent;
import com.affymetrix.genoviz.event.NeoRubberBandListener;
import com.affymetrix.genoviz.widget.NeoMap;
import com.affymetrix.igb.action.StopAutoScrollAction;
import com.affymetrix.igb.tiers.AffyLabelledTierMap;
import com.affymetrix.igb.tiers.AffyTieredMap;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.lorainelab.igb.genoviz.extensions.glyph.GraphGlyph;
import org.lorainelab.igb.genoviz.extensions.glyph.TierGlyph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A MouseListener for the SeqMapView.
 *
 * This handles selection by clicking, section by rubber-banding, and the
 * decision about when to pop-up a menu.
 *
 * It was necessary to deviate somewhat from "best-practice" standards about how
 * to check for the pop-up trigger and whether things happen on mousePressed()
 * or mouseReleased() and detection of "right" mouse clicks. This is because the
 * GenoViz SDK RubberBand interferes with some possibilities.
 *
 * For example, we always show the popup during mouseReleased(), never
 * mousePressed(), because that would interfere with the rubber band. For
 * Windows users, this is the normal behavior anyway. For Mac and Linux users,
 * it is not standard, but should be fine.
 */
public final class SeqMapViewMouseListener implements MouseListener, MouseMotionListener,
        NeoRubberBandListener, NeoGlyphDragListener, PropertyListener {

    private static final Logger LOG = LoggerFactory.getLogger(SeqMapViewMouseListener.class);

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
    private static final boolean SELECT_ON_MOUSE_PRESSED = false;
    private boolean PROCESS_SUB_SELECTION = true;
    private final SeqMapView smv;
    private final AffyTieredMap map;
    private transient MouseEvent rubber_band_start = null;
    private transient boolean is_graph_dragging = false;
    private int num_last_selections = 0;
    private int no_of_prop_being_displayed = 0;
    int select_start, select_end;
    private GlyphI sub_sel_glyph;
//	private int toolTipInitialDelay;
//	private int toolTipDismissDelay;
    private boolean shouldSubSelect = false;
    public static String PREF_SHOW_TOOLTIP = "Show properties in tooltip";

    SeqMapViewMouseListener(SeqMapView smv) {
        this.smv = smv;
        this.map = smv.seqmap;
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
//		if (evt.getSource() == map && show_tooltip) {
//			toolTipDismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
//			ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
//		}
//		toolTipInitialDelay = ToolTipManager.sharedInstance().getInitialDelay();
//		ToolTipManager.sharedInstance().setInitialDelay(0);
    }

    @Override
    public void mouseExited(MouseEvent evt) {
//		if (evt.getSource() == map && show_tooltip) {
//			ToolTipManager.sharedInstance().setDismissDelay(toolTipDismissDelay);
//		}
//		ToolTipManager.sharedInstance().setInitialDelay(toolTipInitialDelay);
        smv.disableToolTip();
    }

    public void mouseClicked(MouseEvent evt) {
        smv.disableToolTip();
        showGraphProperties(false, evt);
    }

    public void mousePressed(MouseEvent evt) {

        // Disable tooltip when clicked
        smv.disableToolTip();

        if (map instanceof AffyLabelledTierMap) {
            ((AffyLabelledTierMap) map).getLabelMap().clearSelected();
        }

        // turn OFF autoscroll in mousePressed()
        StopAutoScrollAction.getAction().actionPerformed(null);

        if (PROCESS_SUB_SELECTION) {
            processSubSelection(evt);
        }

        // process selections in mousePressed() or mouseReleased()
        if (SELECT_ON_MOUSE_PRESSED) {
            processSelections(evt, true);
        }
    }

    public void mouseReleased(MouseEvent evt) {

        num_last_selections = map.getSelected().size();

        if (isOurPopupTrigger(evt) && isTopParentAlreadySelected(evt)) {
            smv.showPopup((NeoMouseEvent) evt);
            return;
        }

        if (PROCESS_SUB_SELECTION) {
            processSubSelection(evt);
        }

        // process selections in mousePressed() or mouseReleased()
        if (true) {
            // if rubber-banding is going on, don't post selections now,
            // because that will be handled in rubberBandChanged().
            // Still need to call processSelections, though, to set
            // the zoom point and to select the items under the current mouse point.
            if (sub_sel_glyph == null) {
                //processSelections(evt, rubber_band_start == null);
                processSelections(evt, true);
            }
        }

        //  do popup in mouseReleased(), never in mousePressed(),
        //  so it doesn't interfere with rubber band
        if (isOurPopupTrigger(evt)) {
            //clear selections
            NeoMouseEvent nevt = (NeoMouseEvent) evt;
            if (nevt.getItems().isEmpty()) {
                smv.clearSelection();
            }
            smv.showPopup((NeoMouseEvent) evt);
        }

        sub_sel_glyph = null;
        // if the GraphSelectionManager is also trying to control popup menus,
        // then there needs to be code here to prevent both this and that from
        // trying to do a popup at the same time.  But it is tricky.  So for
        // now we let ONLY this class trigger the pop-up.
    }

    public void mouseDragged(MouseEvent evt) {
        if (PROCESS_SUB_SELECTION) {
            processSubSelection(evt);
        }
    }

    public void mouseMoved(MouseEvent evt) {
        if (!(evt instanceof NeoMouseEvent) || !smv.getShowPropertiesTooltip()) {
            return;
        }

//		optimziedTooltip(evt);
        if (!smv.isPopupActive()) {
            oldToolTip(evt);
        }

    }

    private void oldToolTip(MouseEvent evt) {
        NeoMouseEvent nevt = (NeoMouseEvent) evt;
        Point2D.Double zoom_point = new Point2D.Double(nevt.getCoordX(), nevt.getCoordY());

        List<GlyphI> hits = nevt.getItems();
        int hcount = hits.size();

        if (!nevt.getItems().isEmpty()) {
            GlyphI topgl = nevt.getItems().get(hcount - 1);
            topgl = map.zoomCorrectedGlyphChoice(topgl, zoom_point);
            if (evt.getSource() == map) {
                smv.getSeqMap().setCursor(SeqMapView.openHandCursor);
            }
            smv.setToolTip(evt, topgl);
            return;
        }

        boolean isShowingGraphToolTip = showGraphProperties(true, evt);

        if (smv.getSeqMap().getCursor() != smv.getMapMode().defCursor && evt.getSource() == map) {
            smv.getSeqMap().setCursor(smv.getMapMode().defCursor);
        }

        if (!isShowingGraphToolTip) {
            smv.setToolTip(evt, null);	// empty tooltip
        }
    }

    private void optimziedTooltip(MouseEvent evt) {
        NeoMouseEvent nevt = (NeoMouseEvent) evt;
        Rectangle2D.Double cbox = new Rectangle2D.Double(nevt.getCoordX(), nevt.getCoordY(), 1, 1);
        List<GlyphI> glyphs = smv.doTheSelection(cbox);

        if (!glyphs.isEmpty()) {
            if (evt.getSource() == map) {
                smv.getSeqMap().setCursor(SeqMapView.openHandCursor);
            }
            smv.setToolTip(evt, glyphs.get(glyphs.size() - 1));
            return;
        }

        boolean isShowingGraphToolTip = showGraphProperties(true, evt);

        if (smv.getSeqMap().getCursor() != smv.getMapMode().defCursor && evt.getSource() == map) {
            smv.getSeqMap().setCursor(smv.getMapMode().defCursor);
        }

        if (!isShowingGraphToolTip) {
            smv.setToolTip(evt, null);	// empty tooltip
        }
    }

    // show properites in tool tip or display in selection info tab table
    private boolean showGraphProperties(boolean isToolTip, MouseEvent evt) {
        // Do we intersect any graph glyphs?
        List<GraphGlyph> glyphlist = smv.collectGraphs();
        Point2D pbox = evt.getPoint();
        for (GraphGlyph glyph : glyphlist) {
            if (glyph.getPixelBox().contains(pbox)) {
                Point2D cbox = new Point2D.Double();
                map.getView().transformToCoords(pbox, cbox);

                if (isToolTip) {
                    smv.setToolTip(evt, (int) cbox.getX(), glyph);
                    return true;
                } else {
                    smv.showProperties((int) cbox.getX(), glyph);
                }
                break;
            }
        }
        return false;
    }

    public void heardGlyphDrag(NeoGlyphDragEvent evt) {
        if (evt.getID() == NeoGlyphDragEvent.DRAG_IN_PROGRESS) {
            is_graph_dragging = true;
            rubber_band_start = null;
        } else if (evt.getID() == NeoGlyphDragEvent.DRAG_ENDED) {
            is_graph_dragging = false;
        }
    }

    private boolean isTopParentAlreadySelected(MouseEvent evt) {
        NeoMouseEvent nevt = (NeoMouseEvent) evt;
        Point2D.Double zoomPoint = nevt.getPoint2D();
        List<GlyphI> hits = nevt.getItems();

        GlyphI topgl = null;
        if (!hits.isEmpty()) {
            topgl = nevt.getItems().get(hits.size() - 1);
            topgl = map.zoomCorrectedGlyphChoice(topgl, zoomPoint);
        }

        if (topgl == null) {
            return false;
        }

        GlyphI parent = null;
        while (topgl != null && !(topgl instanceof TierGlyph)) {
            parent = topgl;
            topgl = topgl.getParent();
        }

        return parent.isSelected();
    }

    private void processSelections(MouseEvent evt, boolean post_selections) {
        if (!(evt instanceof NeoMouseEvent) || is_graph_dragging) {
            return;
        }

        NeoMouseEvent nevt = (NeoMouseEvent) evt;
        Point2D.Double zoom_point = new Point2D.Double(nevt.getCoordX(), nevt.getCoordY());


        List<GlyphI> hits = nevt.getItems();
        int hcount = hits.size();
        boolean isSlopRowSelection = false;

        GlyphI topgl = null;
        if (!nevt.getItems().isEmpty()) {
//			//Pick first drawable glyph from last
//			for(int i = hcount - 1; i >= 0; i--){
//				if(!nevt.getItems().get(i).getSkipDraw() && !nevt.getItems().get(i).getParent().getSkipDraw()){
//					topgl = nevt.getItems().get(i);
//					break;
//				}
//			}
//			
//			//For backward compatibility
//			if(topgl == null){
//				topgl = nevt.getItems().get(hcount - 1);
//			}
//			topgl = map.zoomCorrectedGlyphChoice(topgl, zoom_point);
            topgl = nevt.getItems().get(hcount - 1);
            topgl = map.zoomCorrectedGlyphChoice(topgl, zoom_point);
            if (topgl.isOverlapped()) {
                isSlopRowSelection = true;
//				topgl = null;
            }
        }

        // If drag began in the axis tier, then do NOT do normal selection stuff,
        // because we are selecting sequence instead.
        // (This only really matters when SELECT_ON_MOUSE_PRESSED is false.
        //  If SELECT_ON_MOUSE_PRESSED is true, topgl will already be null
        //  because a drag can only start when you begin the drag on blank space.)
//		if (startedInAxisTier()) {
//			topgl = null;
//		}
        // Normally, clicking will clear previons selections before selecting new things.
        // but we preserve the current selections if:
        //  shift (Add To) or alt (Toggle) or pop-up (button 3) is being pressed
        boolean preserve_selections
                = (isAddToSelectionEvent(nevt) || isToggleSelectionEvent(nevt) || isOurPopupTrigger(nevt));

        // Special case:  if pop-up button is pressed on top of a single item and
        // that item is not already selected, then do not preserve selections
        if (topgl != null && isOurPopupTrigger(nevt)) {
            if (isAddToSelectionEvent(nevt)) {
                // This particular special-special case is really splitting hairs....
                // It would be ok to get rid of it.
                preserve_selections = true;
            } else if (!map.getSelected().contains(topgl)) {
                // This is the important special case.  Needs to be kept.
                preserve_selections = false;
            }
        }
        
        // Keep glyphs selected when drag mouseup event occurs on non-glyph whitespace background canvas
        // ClickCount is 2 for double click, 1 for click, 0 for drag
        if (smv.getMapMode() == SeqMapView.MapMode.MapScrollMode
                && nevt.getClickCount() == 0
                && (nevt.getItems() != null && nevt.getItems().size() == 0)) {
            preserve_selections = true;
        }

        if (!preserve_selections) {
            smv.clearSelection(); // Note that this also clears the selected sequence region
        }

        // seems no longer needed
        //map.removeItem(match_glyphs);  // remove all match glyphs in match_glyphs
        List<GraphGlyph> graphs = new ArrayList<>();
        ITrackStyleExtended combo_style = null;
        if (topgl != null && topgl instanceof GraphGlyph) {
            combo_style = ((GraphGlyph) topgl).getGraphState().getComboStyle();
        }

        if (!isSlopRowSelection) {
            if (preserve_selections) {
                for (GlyphI hit : hits) {
                    Object obj = hit;
                    if (obj instanceof GraphGlyph) {
                        graphs.add((GraphGlyph) obj);
                    }
                }
            } else if (combo_style != null) {
                for (GlyphI hit : hits) {
                    Object obj = hit;
                    if (obj instanceof GraphGlyph
                            && ((GraphGlyph) obj).getGraphState().getComboStyle() == combo_style) {
                        graphs.add((GraphGlyph) obj);
                    }
                }
            } else if (topgl != null && topgl instanceof GraphGlyph) {
                graphs.add((GraphGlyph) topgl);
            }
        }

        boolean toggle_event = isToggleSelectionEvent(evt);
        if (isSlopRowSelection) {
            for (GlyphI glyph : hits) {
                glyph = map.zoomCorrectedGlyphChoice(glyph, zoom_point);

                if (glyph == null) {
                    continue;
                }
                if (toggle_event && glyph.isSelected()) {
                    map.deselect(glyph);
                } else if (!glyph.supportsSubSelection()) {
                    map.select(glyph);
                }
            }
        } else if (topgl != null) {
            //      if (toggle_event && map.getSelected().contains(topgl)) {
            if (toggle_event && topgl.isSelected()) {
                map.deselect(topgl);
            } else if (!topgl.supportsSubSelection()) {
                map.select(topgl);
            }

            int gcount = graphs.size();
            for (GraphGlyph gl : graphs) {
                if (gl != topgl) {  // if gl == topgl, already handled above...
                    if (toggle_event && gl.isSelected()) {
                        map.deselect(gl);
                    } else {
                        map.select(gl);
                    }
                }
            }
        }

        boolean nothing_changed = (preserve_selections && (topgl == null));
        boolean selections_changed = !nothing_changed;

        if (smv.showEdgeMatches && selections_changed && topgl != null && topgl.getParent() != smv.getAxisTier()) {
            smv.doEdgeMatching(map.getSelected(), false);
        }
        smv.setZoomSpotX(zoom_point.getX());
        smv.setZoomSpotY(zoom_point.getY());

        map.updateWidget();

        if (selections_changed && post_selections) {
            smv.postSelections();
        }
    }

    /**
     * Checks whether the mouse event is something that we consider to be a
     * pop-up trigger. (This has nothing to do with
     * MouseEvent.isPopupTrigger()). Checks for isMetaDown() and isControlDown()
     * to try and catch right-click simulation for one-button mouse operation on
     * Mac OS X.
     */
    private static boolean isOurPopupTrigger(MouseEvent evt) {
        if (evt == null) {
            return false;
        }
        if (isToggleSelectionEvent(evt)) {
            return false;
        }
        return evt.isControlDown() || evt.isMetaDown() || ((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0);
    }

    /**
     * Checks whether this the sort of mouse click that should preserve and add
     * to existing selections.
     */
    private static boolean isAddToSelectionEvent(MouseEvent evt) {
        return (evt != null && (evt.isShiftDown()));
    }

    /**
     * Checks whether this the sort of mouse click that should toggle
     * selections.
     */
    private static boolean isToggleSelectionEvent(MouseEvent evt) {
        //Make sure this does not conflict with pop-up trigger
        boolean b = (evt != null && evt.isControlDown() && evt.isShiftDown());
        return (b);
    }

    public void rubberBandChanged(NeoRubberBandEvent evt) {
        /*
         * Note that because using SmartRubberBand, rubber banding will only
         * happen (and NeoRubberBandEvents will only be received) when the
         * orginal mouse press to start the rubber band doesn't land on a
         * hitable glyph
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
            Rectangle2D.Double cbox = new Rectangle2D.Double();
            Rectangle pbox = evt.getPixelBox();
            map.getView().transformToCoords(pbox, cbox);

            // If either width or height is zero then no selection is made.
            if (cbox.width != 0 && cbox.height != 0) {
                // setZoomSpot is best if done before updateWidget
                smv.setZoomSpotX(cbox.x + cbox.width);
                smv.setZoomSpotY(cbox.y + cbox.height);

                if (rubber_band_start != null) {
                    // started outside axis tier: user is trying to select glyphs
                    List<GlyphI> glyphs = smv.doTheSelection(cbox);
                    showSelection(glyphs, rubber_band_start);
                }
            }

            rubber_band_start = null; // for garbage collection
        }
    }

    private void showSelection(List<GlyphI> glyphs, MouseEvent evt) {
        boolean something_changed = true;

        if (isToggleSelectionEvent(evt)) {
            if (glyphs.isEmpty()) {
                something_changed = false;
            }
            toggleSelections(map, glyphs);
        } else if (isAddToSelectionEvent(evt)) {
            if (glyphs.isEmpty()) {
                something_changed = false;
            }
            map.select(glyphs);
        } else if (glyphs.isEmpty() && num_last_selections == 0
                && no_of_prop_being_displayed == 0) {
            something_changed = false;
        } else {
            something_changed = true;
            smv.clearSelection();
            map.select(glyphs);
        }
        if (smv.showEdgeMatches && something_changed) {
            smv.doEdgeMatching(map.getSelected(), false);
        }

        map.updateWidget();

        if (something_changed) {
            smv.postSelections();
        }
    }

    private static void toggleSelections(NeoMap map, Collection<GlyphI> glyphs) {
        List<GlyphI> current_selections = map.getSelected();
        for (GlyphI g : glyphs) {
            if (current_selections.contains(g)) {
                map.deselect(g);
            } else {
                map.select(g);
            }
        }
    }

    public void propertyDisplayed(int prop_displayed) {
        no_of_prop_being_displayed = prop_displayed;
    }

    private void processSubSelection(MouseEvent evt) {
        if (!(evt instanceof NeoMouseEvent)
                || (smv.getMapMode() != SeqMapView.MapMode.MapSelectMode) || is_graph_dragging) {
            return;
        }

        int id = evt.getID();
        NeoMouseEvent nevt = (NeoMouseEvent) evt;

        if (id == MouseEvent.MOUSE_PRESSED) {
            GlyphI topgl = null;
            if (!nevt.getItems().isEmpty()) {
                topgl = nevt.getItems().get(0);
            }
            if (topgl != null && topgl.supportsSubSelection()) {
                shouldSubSelect = true;
            }
        } else if (id == MouseEvent.MOUSE_DRAGGED) {
            if (sub_sel_glyph == null) {
                if (!shouldSubSelect) {
                    return;
                }

                GlyphI topgl = null;
                if (!nevt.getItems().isEmpty()) {
                    topgl = nevt.getItems().get(0);
                }
                if (topgl != null && topgl.supportsSubSelection()) {
                    smv.clearSelection();
                    sub_sel_glyph = topgl;
                    select_start = (int) nevt.getCoordX();
                    select_end = select_start;
                    map.select(sub_sel_glyph, select_start, select_end);
                }
            } else {
                updateSubSelection(nevt);
            }
        } else if (sub_sel_glyph != null && id == MouseEvent.MOUSE_RELEASED) {
            updateSubSelection(nevt);
            subSelectionEnd();
            smv.postSelections();
        }

        if (sub_sel_glyph != null) {
            map.updateWidget();
        }
    }

    private void updateSubSelection(NeoMouseEvent nevt) {
        if (select_end != (int) nevt.getCoordX()) {
            select_end = (int) nevt.getCoordX();
            map.select(sub_sel_glyph, select_start, select_end);
        }
    }

    private void subSelectionEnd() {
        int min = Math.min(select_start, select_end);
        int max = Math.max(select_start, select_end);

        // First check if it is axis glyph
        if (sub_sel_glyph == smv.getSeqMap().getAxis()) {
            map.deselect(sub_sel_glyph);
            if (max > min) {
                smv.zoomTo(min, max);
            }
        } else { // If it is not axis glyph then assume it to be sequence glyph
            //Add one for interbase ???
            SeqSymmetry new_region = new SingletonSeqSymmetry(min, max + 1, smv.getAnnotatedSeq());
            smv.setSelectedRegion(new_region, sub_sel_glyph, true);
        }
        shouldSubSelect = false;
    }

}
