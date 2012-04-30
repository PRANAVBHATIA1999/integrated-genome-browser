package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;

import com.affymetrix.genoviz.widget.NeoAbstractWidget;
import com.affymetrix.igb.tiers.AffyTieredMap;

public class ScrollRightAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private static ScrollRightAction ACTION;

	public static ScrollRightAction getAction() {
		if (ACTION == null) {
			ACTION = new ScrollRightAction();
		}
		return ACTION;
	}

	public ScrollRightAction() {
		super("Scroll Right",  "16x16/actions/go-next.png", "22x22/actions/go-next.png");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		AffyTieredMap seqmap = getSeqMapView().getSeqMap();
		int[] visible = seqmap.getVisibleRange();
		seqmap.scroll(NeoAbstractWidget.X, visible[0] + (visible[1] - visible[0]) / 10);
		seqmap.updateWidget();
	}
}
