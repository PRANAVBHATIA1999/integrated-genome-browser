package com.affymetrix.igb.action;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

import java.awt.event.ActionEvent;

import com.affymetrix.igb.shared.TrackstylePropertyMonitor;

public class ChangeExpandMaxAllAction extends ChangeExpandMaxActionA {
	private static final long serialVersionUID = 1L;
	private static final ChangeExpandMaxAllAction ACTION = new ChangeExpandMaxAllAction();

	public static ChangeExpandMaxAllAction getAction() {
		return ACTION;
	}

	private ChangeExpandMaxAllAction() {
		super(BUNDLE.getString("changeExpandMaxAllAction"), null, null);
	}

	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		changeExpandMax(getTierManager().getAllTierLabels());
		TrackstylePropertyMonitor.getPropertyTracker().actionPerformed(e);
	}
}
