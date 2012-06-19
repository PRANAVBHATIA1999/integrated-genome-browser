package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor;
import com.affymetrix.igb.tiers.TierLabelGlyph;
import java.util.List;

public class ChangeExpandMaxOptimizeAction extends ChangeExpandMaxActionA {
	private static final long serialVersionUID = 1L;
	private static final ChangeExpandMaxOptimizeAction ACTION = new ChangeExpandMaxOptimizeAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ChangeExpandMaxOptimizeAction getAction() {
		return ACTION;
	}

	private ChangeExpandMaxOptimizeAction() {
		super("All", null, null);
	}

	@Override
	protected List<TierLabelGlyph> getTiers() {
		return getTierManager().getAllTierLabels();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		changeExpandMax(getOptimum());
		TrackstylePropertyMonitor.getPropertyTracker().actionPerformed(e);
	}
}
