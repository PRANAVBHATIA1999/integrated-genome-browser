package com.affymetrix.igb.shared;

import com.affymetrix.genometryImpl.event.EnableDisableAbleAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.igb.action.ChangeExpandMaxActionA;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.tiers.TierLabelGlyph;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Change the max slot depth on all selected tracks to an optimal value.
 */
public class ChangeExpandMaxOptimizeAction extends ChangeExpandMaxActionA implements EnableDisableAbleAction{
	private static final long serialVersionUID = 1L;
	private static final ChangeExpandMaxOptimizeAction ACTION
			= new ChangeExpandMaxOptimizeAction();
	
	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ChangeExpandMaxOptimizeAction getAction() {
		return ACTION;
	}

	private ChangeExpandMaxOptimizeAction() {
		super(BUNDLE.getString("changeExpandMaxOptimizeAction"), "16x16/actions/optimize_stack_depth.png", "22x22/actions/optimize_stack_depth.png");
		putValue(SHORT_DESCRIPTION, BUNDLE.getString("changeExpandMaxOptimizeActionTooltip"));
	}

	/**
	 * @return visible selected tiers.
	 */
	@Override
	protected List<TierLabelGlyph> getTiers() {
		List<TierLabelGlyph> answer = new ArrayList<TierLabelGlyph>();
		List<TierLabelGlyph> theTiers = getTierManager().getSelectedTierLabels();
		for (TierLabelGlyph tlg : theTiers) {
			TierGlyph tg = tlg.getReferenceTier();
//			if (!tg.getAnnotStyle().isGraphTier()) {
//				System.out.println(this.getClass().getName()
//						+ ".getOptimum: found a graph tier: " + tg.getLabel());
//				answer.add(tlg);
//			}
			if (tg.isVisible()) {
				answer.add(tlg);
			}
		}
		return answer;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		List<TierLabelGlyph> theTiers = getTiers();
		for (TierLabelGlyph tlg : theTiers) {
			TierGlyph tg = tlg.getReferenceTier();
			int slotsNeeded = tg.getSlotsNeeded(getSeqMapView().getSeqMap().getView());
			changeExpandMax(tg, slotsNeeded);
		}
		repack(true, false);
		getSeqMapView().seqMapRefresh();
		getSeqMapView().getSeqMap().updateWidget();
		TrackstylePropertyMonitor.getPropertyTracker().actionPerformed(e);
	}
	
	@Override
	public boolean getEnableDisable() {
		return (Selections.allGlyphs.size() > 0 && Selections.isAllAnnot());
	}
}
