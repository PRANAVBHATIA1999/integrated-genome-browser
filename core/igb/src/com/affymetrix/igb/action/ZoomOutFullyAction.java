package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import java.awt.Adjustable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import com.affymetrix.genoviz.widget.NeoMap;
import com.affymetrix.igb.tiers.AffyTieredMap;

public class ZoomOutFullyAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private static ZoomOutFullyAction ACTION = new ZoomOutFullyAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ZoomOutFullyAction getAction() {
		return ACTION;
	}

	public ZoomOutFullyAction() {
		super("Home Position", "Zoom out fully", "16x16/actions/go-home.png", null, KeyEvent.VK_UNDEFINED);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		AffyTieredMap seqmap = getSeqMapView().getSeqMap();
		Adjustable adj = seqmap.getZoomer(NeoMap.X);
		adj.setValue(adj.getMinimum());
		adj = seqmap.getZoomer(NeoMap.Y);
		adj.setValue(adj.getMinimum());
	}
}
