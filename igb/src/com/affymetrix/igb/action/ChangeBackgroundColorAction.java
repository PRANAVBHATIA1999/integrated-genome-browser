package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;

import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor;

public class ChangeBackgroundColorAction extends ChangeColorActionA {
	private static final long serialVersionUID = 1L;
	private static ChangeBackgroundColorAction ACTION;

	public static ChangeBackgroundColorAction getAction() {
		if (ACTION == null) {
			ACTION = new ChangeBackgroundColorAction();
		}
		return ACTION;
	}

	public ChangeBackgroundColorAction() {
		super(IGBConstants.BUNDLE.getString("changeBGColorAction"), "images/change_color.png");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		changeColor(getTierManager().getSelectedTierLabels(), false);
		TrackstylePropertyMonitor.getPropertyTracker().actionPerformed(e);
	}
}
