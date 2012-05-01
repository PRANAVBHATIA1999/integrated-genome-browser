package com.affymetrix.igb.action;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.SeqMapViewPopup;
import com.affymetrix.igb.view.load.GeneralLoadView;

public class UseAsReferenceSeqAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private static final UseAsReferenceSeqAction ACTION = new UseAsReferenceSeqAction();

	public static UseAsReferenceSeqAction getAction() {
		return ACTION;
	}

	private UseAsReferenceSeqAction() {
		super(BUNDLE.getString("useAsReferenceSeqAction"), null, null);
	}

	private void useTrackAsReferenceSequence(TierGlyph tier) throws Exception {
		ITrackStyleExtended style = tier.getAnnotStyle();
		GenericFeature feature = style.getFeature();
		GeneralLoadView.getLoadView().useAsRefSequence(feature);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			super.actionPerformed(e);
			List<TierGlyph> current_tiers = getTierManager().getSelectedTiers();
			if (current_tiers.size() > 1) {
				ErrorHandler.errorPanel(IGBConstants.BUNDLE.getString("multTrackError"));
			}
			useTrackAsReferenceSequence(current_tiers.get(0));
		} catch (Exception ex) {
			Logger.getLogger(SeqMapViewPopup.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
