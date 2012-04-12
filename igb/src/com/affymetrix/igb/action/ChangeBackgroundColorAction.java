package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;

import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor;
import com.affymetrix.igb.view.SeqMapView;

public class ChangeBackgroundColorAction extends ChangeColorActionA {
	private static final long serialVersionUID = 1L;
	private static ChangeBackgroundColorAction ACTION;

	public static ChangeBackgroundColorAction getAction() {
		if (ACTION == null) {
			ACTION = new ChangeBackgroundColorAction(Application.getSingleton().getMapView());
		}
		return ACTION;
	}

	public ChangeBackgroundColorAction(SeqMapView gviewer) {
		super(gviewer);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		changeColor(handler.getSelectedTierLabels(), false);
		TrackstylePropertyMonitor.getPropertyTracker().actionPerformed(e);
	}

	@Override
	public String getText() {
		return IGBConstants.BUNDLE.getString("changeBGColorAction");
	}

	@Override
	public String getIconPath() {
		return "images/change_color.png";
	}
}
