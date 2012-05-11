package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import java.awt.Adjustable;
import java.awt.event.ActionEvent;

import com.affymetrix.genoviz.widget.NeoMap;
import com.affymetrix.igb.tiers.AffyTieredMap;

public class ZoomInYAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private static ZoomInYAction ACTION = new ZoomInYAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ZoomInYAction getAction() {
		return ACTION;
	}

	public ZoomInYAction() {
		super("Zoom in vertically", "toolbarButtonGraphics/general/ZoomIn16.gif", null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		AffyTieredMap seqmap = getSeqMapView().getSeqMap();
		Adjustable adj = seqmap.getZoomer(NeoMap.Y);
		adj.setValue(adj.getValue() + (adj.getMaximum() - adj.getMinimum()) / 20);
	}
}
