package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.view.SeqMapView;

public class CenterAtHairlineAction extends GenericAction {
	private static final long serialVersionUID = 1L;
	private static final CenterAtHairlineAction ACTION = new CenterAtHairlineAction();

	public static CenterAtHairlineAction getAction() {
		return ACTION;
	}

	private CenterAtHairlineAction() {
		super();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		SeqMapView gviewer = IGB.getSingleton().getMapView();
		gviewer.centerAtHairline();
	}

	@Override
	public String getText() {
		return IGBConstants.BUNDLE.getString("centerZoomStripe");
	}
}
