package com.affymetrix.igb.shared;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.igb.action.SeqMapViewActionA;
import com.affymetrix.igb.tiers.TierLabelGlyph;

/**
 * note - this class contains an instance of SeqMapView. For now, there
 * is just one instance using the regular SeqMapView, no instance for
 * AltSpliceView
 */
public abstract class RepackTiersAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;


	protected RepackTiersAction(String text, String iconPath, String largeIconPath) {
		super(text, iconPath, largeIconPath);
	}
	public void repack(final boolean full_repack) {
		AbstractAction action = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				getTierManager().repackTheTiers(full_repack, true);
			}
		};
		
		getSeqMapView().preserveSelectionAndPerformAction(action);
	}

	/**
	 * Handles tier (track) repacking actions.
	 *
	 * @param theTiers generally either all or selected tiers.
	 */
	protected void repackTiers(List<TierLabelGlyph> theTiers) {
		ViewI ourView = getSeqMapView().getSeqMap().getView();
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
			if (t instanceof com.affymetrix.igb.shared.GraphTierGlyph) {
				// So far this has only been tested with annotation depth graphs.
				com.affymetrix.igb.shared.GraphGlyph gg
						= (com.affymetrix.igb.shared.GraphGlyph) t;
				gg.setVisibleMaxY(a);
			}
		}
		// Now repack with the newly appointed maxima.
		repack(true);
	}
}
