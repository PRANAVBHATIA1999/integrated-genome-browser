/*  Copyright (c) 2012 Genentech, Inc.
 *
 *  Licensed under the Common Public License, Version 1.0 (the "License").
 *  A copy of the license must be included
 *  with any distribution of this source code.
 *  Distributions from Genentech, Inc. place this in the IGB_LICENSE.html file.
 *
 *  The license is also available at
 *  http://www.opensource.org/licenses/CPL
 */
package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.TierLabelGlyph;
import com.affymetrix.igb.tiers.TierLabelManager;
import com.affymetrix.igb.view.SeqMapView;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Size the tiers vertically so that all glyphs in view horizontally will fit.
 * Unlike the "repack" operation this should not shrink or expand the tiers
 * to make it fit in the view.
 * i.e. You may need to scroll afterward.
 * Instead of changing the glyph sizes to fit the panel,
 * this keeps glyph sizes constant.
 * 
 * <p>This does not yet extend IGB's GenericAction.
 * When it does remove this paragraph.
 * </p>
 * @author Eric Blossom
 */
public class ZoomingRepackAction extends AbstractAction {

	private final SeqMapView gviewer;

	/**
	 * Create an action for the given tiered map.
	 */
	public ZoomingRepackAction(SeqMapView theSubject) {
		super("Optimize All Tracks");
		putValue(Action.SHORT_DESCRIPTION, "Optimize track stack heights for the region in view.");
		this.gviewer = theSubject;
	}

	/**
	 * Repacks tiers (tracks).
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		TierLabelManager handler = this.gviewer.getTierManager();
		List<TierLabelGlyph> theTiers = handler.getAllTierLabels();
		ViewI ourView = gviewer.getSeqMap().getView();
		for (TierLabelGlyph tl : theTiers) {
			TierGlyph t = (TierGlyph) tl.getInfo();
			int a = t.getSlotsNeeded(ourView);
			ITrackStyleExtended style = t.getAnnotStyle();
			TierGlyph.Direction d = t.getDirection();
			switch (d) {
				case REVERSE:
					style.setReverseMaxDepth(a);
					break;
				default:
				case FORWARD:
					style.setForwardMaxDepth(a);
					break;
			}
			com.affymetrix.igb.shared.ViewModeGlyph vmg = t.getViewModeGlyph();
			if (vmg instanceof com.affymetrix.igb.shared.AbstractGraphGlyph) {
				// So far this has only been tested with annotation depth graphs.
				com.affymetrix.igb.shared.AbstractGraphGlyph gg
						= (com.affymetrix.igb.shared.AbstractGraphGlyph) vmg;
				gg.setVisibleMaxY(a);
			}
		}
		// Now repack with the newly appointed maxima.
		boolean fullRepack = true, stretchMap = true, stretchAllTiers = true;
		gviewer.getSeqMap().packTiers(fullRepack, stretchMap, stretchAllTiers);
		gviewer.getSeqMap().updateWidget();
		// Full update doesn't seem to happen.
		// Or, rather, it happens when the user clicks on the map.
	}

}
