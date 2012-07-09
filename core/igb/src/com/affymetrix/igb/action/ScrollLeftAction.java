package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import java.awt.event.ActionEvent;

import com.affymetrix.genoviz.widget.NeoAbstractWidget;
import com.affymetrix.igb.tiers.AffyTieredMap;

public class ScrollLeftAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private static ScrollLeftAction ACTION = new ScrollLeftAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ScrollLeftAction getAction() {
		return ACTION;
	}

	public ScrollLeftAction() {
		super("Scroll Left", "16x16/actions/go-previous.png",
				"22x22/actions/go-previous.png" );
		this.ordinal = -4007010;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		AffyTieredMap seqmap = getSeqMapView().getSeqMap();
		int[] visible = seqmap.getVisibleRange();
		seqmap.scroll(NeoAbstractWidget.X, visible[0] - (visible[1] - visible[0]) / 10);
		seqmap.updateWidget();
	}
}
