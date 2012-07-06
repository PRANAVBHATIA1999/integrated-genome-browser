package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.shared.ParameteredAction;
import com.affymetrix.igb.shared.RepackTiersAction;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.MaxSlotsChooser;
import com.affymetrix.igb.tiers.TierLabelGlyph;
import java.util.List;

public abstract class ChangeExpandMaxActionA
extends RepackTiersAction
implements ParameteredAction {
	private static final long serialVersionUID = 1L;

	protected ChangeExpandMaxActionA(String text, String iconPath, String largeIconPath) {
		super(text, iconPath, largeIconPath);
	}

	/**
	 * Subclasses can return all tracks or selected tracks.
	 * This is the list that will be considered
	 * and operated on.
	 */
	protected abstract List<TierLabelGlyph> getTiers();

	/**
	 * Sets the maximum stack depth for all tiers
	 * returned by {@link #getTiers}.
	 */
	public void changeExpandMax(int max) {
		List<TierLabelGlyph> tier_label_glyphs = getTiers();
		for (TierLabelGlyph tlg : tier_label_glyphs) {
			TierGlyph tier = tlg.getReferenceTier();
			ITrackStyleExtended style = tier.getAnnotStyle();
			switch (tier.getDirection()) {
				case FORWARD:
					style.setForwardMaxDepth(max);
					break;
				case REVERSE:
					style.setReverseMaxDepth(max);
					break;
				default:
				case BOTH:
				case NONE:
				case AXIS:
					style.setMaxDepth(max);
			}
		}
		repack(true);
		this.getSeqMapView().seqMapRefresh();
		this.getSeqMapView().getSeqMap().updateWidget();
	}

	@Override
	public void performAction(Object parameter) {
		if(parameter.getClass() != Integer.class) {
			return;
		}
		changeExpandMax((Integer)parameter);
	}
	
	/**
	 * Get the optimal limit considering all tiers,
	 * not just those returned by {@link #getTiers}.
	 */
	public int getOptimum() {
		List<TierLabelGlyph> theTiers = getTierManager().getAllTierLabels();
		int ourOptimum = 1;
		for (TierLabelGlyph tlg : theTiers) {
			TierGlyph tg = (TierGlyph) tlg.getInfo();
			if(tg.getAnnotStyle().isGraphTier()) {
				continue;
			}
			int slotsNeeded = tg.getSlotsNeeded(getSeqMapView().getSeqMap().getView());
			ourOptimum = Math.max(ourOptimum, slotsNeeded);
		}
		return ourOptimum;
	}

	/**
	 * Figure out the actual and optimal limit for the tiers
	 * and present a dialog to choose a value.
	 */
	protected void changeExpandMax() {
		List<TierLabelGlyph> l = getTiers();
		int actualLimit = -1;
		int optimalLimit = 0;
		for (TierLabelGlyph tlg : l) {
			TierGlyph tg = tlg.getReferenceTier();
			int tierLimit = -1;
			ITrackStyleExtended style = tg.getAnnotStyle();
			if (style != null) {
				switch (tg.getDirection()) {
					case FORWARD:
						tierLimit = style.getForwardMaxDepth();
						break;
					case REVERSE:
						tierLimit = style.getReverseMaxDepth();
						break;
					default:
						tierLimit = style.getMaxDepth();
				}
			}
			if (0 == tierLimit) {
				actualLimit = 0;
			}
			else if (0 != actualLimit) {
				actualLimit = Math.max(actualLimit, tierLimit);
			}
			int slotsNeeded = tg.getSlotsNeeded(getSeqMapView().getSeqMap().getView());
			optimalLimit = Math.max(optimalLimit, slotsNeeded);
		}
		MaxSlotsChooser chooser = new MaxSlotsChooser(
				IGBConstants.BUNDLE.getString("maxHeight"),
				actualLimit, optimalLimit, this);
		chooser.setVisible(true);
	}

}
