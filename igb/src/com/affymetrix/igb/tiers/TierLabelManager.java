package com.affymetrix.igb.tiers;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genoviz.comparator.GlyphMinYComparator;
import com.affymetrix.genometryImpl.style.IAnnotStyle;
import com.affymetrix.genoviz.bioviews.GlyphDragger;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.SceneI;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.genoviz.event.NeoMouseEvent;
import com.affymetrix.genoviz.util.NeoConstants;
import com.affymetrix.genoviz.widget.NeoAbstractWidget;
import com.affymetrix.igb.glyph.GraphGlyph;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 * @version $Id$
 */
public final class TierLabelManager {

	private final AffyLabelledTierMap tiermap;
	private final AffyTieredMap labelmap;
	private final JPopupMenu popup;
	private final static int xoffset_pop = 10;
	private final static int yoffset_pop = 0;
	private final Set<PopupListener> popup_listeners = new CopyOnWriteArraySet<PopupListener>();
	private final Set<TrackSelectionListener> track_selection_listeners = new CopyOnWriteArraySet<TrackSelectionListener>();
	private final Comparator<GlyphI> tier_sorter = new GlyphMinYComparator();
	/**
	 *  Determines whether selecting a tier label of a tier that contains only
	 *  GraphGlyphs should cause the graphs in that tier to become selected.
	 */
	private boolean do_graph_selections = false;

	private final MouseListener mouse_listener = new MouseListener() {

		TierLabelGlyph dragging_label = null;

		public void mouseEntered(MouseEvent evt) {
		}

		public void mouseExited(MouseEvent evt) {
		}

		/** Tests whether the mouse event is due to the 3rd button.
		 *  (For the sake of Macintosh, considers Meta key and Control key as
		 *  simulation of 3rd button.)
		 */
		boolean isOurPopupTrigger(MouseEvent evt) {
			int mods = evt.getModifiers();
			return (evt.isMetaDown() || evt.isControlDown()
					|| ((mods & InputEvent.BUTTON3_MASK) != 0));
		}

		public void mouseClicked(MouseEvent evt) {
		}

		public void mousePressed(MouseEvent evt) {
			if (evt instanceof NeoMouseEvent && evt.getSource() == labelmap) {
				NeoMouseEvent nevt = (NeoMouseEvent) evt;
				List selected_glyphs = nevt.getItems();
				GlyphI topgl = null;
				if (!selected_glyphs.isEmpty()) {
					topgl = (GlyphI) selected_glyphs.get(selected_glyphs.size() - 1);
				}

				// Dispatch track selection event
				doTrackSelection(topgl);

				// Normally, clicking will clear previons selections before selecting new things.
				// but we preserve the current selections if:
				//  1. shift or alt key is pressed, or
				//  2. the pop-up key is being pressed
				//     2a. on top of nothing
				//     2b. on top of something previously selected
				boolean preserve_selections = false;
				if (nevt.isAltDown() || nevt.isShiftDown()) {
					preserve_selections = true;
				} else if (topgl != null && isOurPopupTrigger(nevt)) {
					if (labelmap.getSelected().contains(topgl)) {
						preserve_selections = true;
					}
				}
				if (!preserve_selections) {
					labelmap.clearSelected();
				}
				List<GlyphI> selected = nevt.getItems();
				labelmap.select(selected);
				doGraphSelections();

				tiermap.updateWidget(); // make sure selections becomes visible
				if (isOurPopupTrigger(evt)) {
					doPopup(evt);
				} else if (selected.size() > 0) {
					// take glyph at end of selected, just in case there is more
					//    than one -- the last one should be on top...
					TierLabelGlyph gl = (TierLabelGlyph) selected.get(selected.size() - 1);
					labelmap.toFront(gl);
					dragLabel(gl, nevt);
				}
			}
		}

		// if a tier has been dragged, then try to sort out rearrangement of tiers
		//    in tiermap based on new positions of labels in labelmap
		public void mouseReleased(MouseEvent evt) {
			if (evt.getSource() == labelmap && dragging_label != null) {
				sortTiers();
				dragging_label = null;
			}
		}

		private void dragLabel(TierLabelGlyph gl, NeoMouseEvent nevt) {
			dragging_label = gl;
			GlyphDragger dragger = new GlyphDragger((NeoAbstractWidget) nevt.getSource());
			dragger.setUseCopy(false);
			dragger.startDrag(gl, nevt);
			dragger.setConstraint(NeoConstants.HORIZONTAL, true);
		}
	}; // end of mouse listener class

	public TierLabelManager(AffyLabelledTierMap map) {
		tiermap = map;
		popup = new JPopupMenu();

		labelmap = tiermap.getLabelMap();
		labelmap.addMouseListener(this.mouse_listener);

		labelmap.getScene().setSelectionAppearance(SceneI.SELECT_OUTLINE);
		labelmap.setPixelFuzziness(0); // there are no gaps between tiers, need no fuzziness
	}

	/** Returns a list of TierGlyph items representing the selected tiers. */
	List<TierGlyph> getSelectedTiers() {
		List<TierGlyph> selected_tiers = new ArrayList<TierGlyph>();

		for (TierLabelGlyph tlg : getSelectedTierLabels()) {
			// TierGlyph should be data model for tier label, access via label.getInfo()
			TierGlyph tier = (TierGlyph) tlg.getInfo();
			selected_tiers.add(tier);
		}
		return selected_tiers;
	}

	/** Returns a list of selected TierLabelGlyph items. */
	@SuppressWarnings("unchecked")
	public List<TierLabelGlyph> getSelectedTierLabels() {
		// The below loop is unnecessary, but is done to fix generics compiler warnings.
		List<TierLabelGlyph> tlg = new ArrayList<TierLabelGlyph>(labelmap.getSelected().size());
		for (GlyphI g : labelmap.getSelected()) {
			if (g instanceof TierLabelGlyph) {
				tlg.add((TierLabelGlyph) g);
			}
		}
		return tlg;
	}

	/** Returns a list of all TierLabelGlyph items. */
	public List<TierLabelGlyph> getAllTierLabels() {
		return tiermap.getTierLabels();
	}

	/** Selects all non-hidden tiers. */
	void selectAllTiers() {
		for (TierLabelGlyph tierlabel : getAllTierLabels()) {
			if (tierlabel.getReferenceTier().getAnnotStyle().getShow()) {
				labelmap.select(tierlabel);
			}
		}
		doGraphSelections();
		//labelmap.updateWidget();
		tiermap.updateWidget(); // make sure selections becomes visible
	}

	/**
	 *  Determines whether selecting a tier label of a tier that contains only
	 *  GraphGlyphs should cause the graphs in that tier to become selected.
	 */
	public void setDoGraphSelections(boolean b) {
		do_graph_selections = b;
	}

	private void doGraphSelections() {
		if (!do_graph_selections) {
			return;
		}

		GenometryModel gmodel = GenometryModel.getGenometryModel();

		List<SeqSymmetry> symmetries = new ArrayList<SeqSymmetry>();
		symmetries.addAll(gmodel.getSelectedSymmetriesOnCurrentSeq());

		for (TierLabelGlyph tierlabel : getAllTierLabels()) {
			TierGlyph tg = tierlabel.getReferenceTier();
			int child_count = tg.getChildCount();
			if (child_count > 0 && tg.getChild(0) instanceof GraphGlyph) {
				// It would be nice if we could assume that a tier contains only
				// GraphGlyph's or only non-GraphGlyph's, but that is not true.
				//
				// When graph thresholding is turned on, there can be one or
				// two other EfficientFillRectGlyphs that are a child of the tier glyph
				// but are not instances of GraphGlyph.  They can be ignored.
				// (I would like to change them to be children of the GraphGlyph, but
				// haven't done it yet.)

				// Assume that if first child is a GraphGlyph, then so are all others
				for (int i = 0; i < child_count; i++) {
					GlyphI ob = tg.getChild(i);
					if (!(ob instanceof GraphGlyph)) {
						// ignore the glyphs that are not GraphGlyph's
						continue;
					}
					GraphGlyph child = (GraphGlyph) ob;
					SeqSymmetry sym = (SeqSymmetry) child.getInfo();
					// sym will be a GraphSym, but we don't need to cast it
					if (tierlabel.isSelected()) {
						if (!symmetries.contains(sym)) {
							symmetries.add(sym);
						}
					} else if (symmetries.contains(sym)) {
						symmetries.remove(sym);
					}
				}
			}
		}

		gmodel.setSelectedSymmetries(symmetries, tiermap);
	}

	/** Gets all the GraphGlyph objects inside the given list of TierLabelGlyph's. */
	public static List<GraphGlyph> getContainedGraphs(List<TierLabelGlyph> tier_label_glyphs) {
		List<GraphGlyph> result = new ArrayList<GraphGlyph>();
		for (TierLabelGlyph tlg : tier_label_glyphs) {
			result.addAll(getContainedGraphs(tlg));
		}
		return result;
	}

	/** Gets all the GraphGlyph objects inside the given TierLabelGlyph. */
	private static List<GraphGlyph> getContainedGraphs(TierLabelGlyph tlg) {
		List<GraphGlyph> result = new ArrayList<GraphGlyph>();
		TierGlyph tier = (TierGlyph) tlg.getInfo();
		int child_count = tier.getChildCount();
		if (child_count > 0 && tier.getChild(0) instanceof GraphGlyph) {
			for (int j = 0; j < child_count; j++) {
				result.add((GraphGlyph) tier.getChild(j));
			}
		}
		return result;
	}

	/** Restores multiple hidden tiers and then repacks.
	 *  @param tier_labels  a List of GlyphI objects for each of which getInfo() returns a TierGlyph.
	 *  @param full_repack  Whether to do a full repack
	 *  @param fit_y  Whether to change the zoom to fit all the tiers in the view
	 *  @see #repackTheTiers(boolean, boolean)
	 */
	public void showTiers(List<TierLabelGlyph> tier_labels, boolean full_repack, boolean fit_y) {
		for (TierLabelGlyph g : tier_labels) {
			if (g.getInfo() instanceof TierGlyph) {
				TierGlyph tier = (TierGlyph) g.getInfo();
				tier.getAnnotStyle().setShow(true);
			}
		}

		repackTheTiers(full_repack, fit_y);
	}

	/** Hides multiple tiers and then repacks.
	 *  @param tier_labels  a List of GlyphI objects for each of which getInfo() returns a TierGlyph.
	 *  @param fit_y  Whether to change the zoom to fit all the tiers in the view
	 */
	public void hideTiers(List<TierLabelGlyph> tier_labels, boolean full_repack, boolean fit_y) {
		for (TierLabelGlyph g : tier_labels) {
			if (g.getInfo() instanceof TierGlyph) {
				TierGlyph tier = (TierGlyph) g.getInfo();
				tier.getAnnotStyle().setShow(false);
			}
		}

		repackTheTiers(full_repack, fit_y);
	}

	/**
	 * Collapse or expand tiers.
	 * @param tier_labels
	 * @param collapsed - boolean indicating whether to collapse or expand tiers.
	 */
	void setTiersCollapsed(List<TierLabelGlyph> tier_labels, boolean collapsed) {
		for (TierLabelGlyph tlg : tier_labels) {
			IAnnotStyle style = tlg.getReferenceTier().getAnnotStyle();
			if (style.getExpandable()) {
				style.setCollapsed(collapsed);

				// When collapsing, make them all be the same height as the tier.
				// (this is for simplicity in figuring out how to draw things.)
				if (collapsed) {
					List<GraphGlyph> graphs = getContainedGraphs(tlg);
					double tier_height = style.getHeight();
					for (GraphGlyph graph : graphs) {
						graph.getGraphState().getTierStyle().setHeight(tier_height);
					}
				}

				for (ViewI v : tlg.getReferenceTier().getScene().getViews()) {
					tlg.getReferenceTier().pack(v);
				}
			}
		}

		repackTheTiers(true, true);
	}

	/**
	 *  Sorts all tiers and then calls packTiers() and updateWidget().
	 */
	void sortTiers() {
		List<TierLabelGlyph> label_glyphs = tiermap.getTierLabels();
		Collections.sort(label_glyphs, tier_sorter);

		// Commenting out below code to resolve for bug 2926882 (formerly hidden minus strand
		// tracks re-appear above the axis)
		
//		List<TierGlyph> tiers = tiermap.getTiers();
//		tiers.clear();
//		for (TierLabelGlyph label : label_glyphs) {
//			TierGlyph tier = (TierGlyph) label.getInfo();
//			tiers.add(tier);
//		}
		
		// then repack of course (tiermap repack also redoes labelmap glyph coords...)
		tiermap.packTiers(false, true, false);
		tiermap.updateWidget();
	}

	/**
	 *  Repacks tiers.  Should be called after hiding or showing tiers or
	 *  changing their heights.
	 */
	void repackTheTiers(boolean full_repack, boolean stretch_vertically) {
		tiermap.repackTheTiers(full_repack, stretch_vertically);
	}

	public void addPopupListener(PopupListener p) {
		popup_listeners.add(p);
	}

	/** Removes all elements from the popup, then notifies all {@link TierLabelManager.PopupListener}
	 *  objects (which may add items to the menu), then displays the popup
	 *  (if it isn't empty).
	 */
	private void doPopup(MouseEvent e) {
		popup.removeAll();

		for (PopupListener pl : popup_listeners) {
			pl.popupNotify(popup, this);
		}

		if (popup.getComponentCount() > 0) {
			popup.show(labelmap, e.getX() + xoffset_pop, e.getY() + yoffset_pop);
		}
	}

	/** An interface that lets listeners modify the popup menu before it is shown. */
	public interface PopupListener {

		/** Called before the {@link TierLabelManager} popup menu is displayed.
		 *  The listener may add elements to the popup menu before it gets displayed.
		 */
		public void popupNotify(JPopupMenu popup, TierLabelManager handler);
	}

	public void addTrackSelectionListener(TrackSelectionListener l) {
		track_selection_listeners.add(l);
	}

	public void doTrackSelection(GlyphI topLevelGlyph) {
		for (TrackSelectionListener l : track_selection_listeners) {
			l.trackSelectionNotify(topLevelGlyph, this);
		}
	}

	/** An interface that to listener for track selection events. */
	public interface TrackSelectionListener {

		public void trackSelectionNotify(GlyphI topLevelGlyph, TierLabelManager handler);
	}
}
