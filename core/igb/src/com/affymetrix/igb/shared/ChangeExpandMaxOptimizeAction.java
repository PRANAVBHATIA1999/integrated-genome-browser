package com.affymetrix.igb.shared;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.igb.action.ChangeExpandMaxActionA;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor;
import com.affymetrix.igb.tiers.TierLabelGlyph;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Change the max slot depth on all selected tracks to an optimal value.
 */
public class ChangeExpandMaxOptimizeAction extends ChangeExpandMaxActionA {
	private static final long serialVersionUID = 1L;
	private static final ChangeExpandMaxOptimizeAction ACTION
			= new ChangeExpandMaxOptimizeAction();
	
	public static ChangeExpandMaxOptimizeAction getAction() {
		return ACTION;
	}

	private ChangeExpandMaxOptimizeAction() {
		super(BUNDLE.getString("changeExpandMaxOptimizeAction"), null, null);
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
		changeExpandMax(getOptimum());
		TrackstylePropertyMonitor.getPropertyTracker().actionPerformed(e);
	}
}
