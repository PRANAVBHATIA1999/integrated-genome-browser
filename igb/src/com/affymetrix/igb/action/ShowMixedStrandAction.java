package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.tiers.AffyTieredMap;

public class ShowMixedStrandAction extends GenericAction {
   	private static final long serialVersionUID = 1L;
	private static final ShowMixedStrandAction ACTION = new ShowMixedStrandAction();

	public static ShowMixedStrandAction getAction() {
		return ACTION;
	}

	private ShowMixedStrandAction() {
		super();
	}

	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		AffyTieredMap.setShowMixed(!AffyTieredMap.isShowMixed());
		putValue(AffyTieredMap.SELECTED_KEY_, Boolean.valueOf(AffyTieredMap.isShowMixed()));
		AffyTieredMap map = ((IGB) IGB.getSingleton()).getMapView().getSeqMap();
		map.repackTheTiers(false, true, false);
	}

	@Override
	public String getText() {
		return "Show (+/-) tiers";
	}
}
