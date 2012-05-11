package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import java.awt.event.ActionEvent;

import com.affymetrix.genoviz.widget.NeoAbstractWidget;
import com.affymetrix.igb.tiers.AffyTieredMap;

public class ScrollUpAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private static final ScrollUpAction ACTION = new ScrollUpAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ScrollUpAction getAction() {
		return ACTION;
	}

	public ScrollUpAction() {
		super("Scroll Up","16x16/actions/go-up.png", "22x22/actions/go-up.png" );
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		AffyTieredMap seqmap = getSeqMapView().getSeqMap();
		int[] visible = seqmap.getVisibleOffset();
		seqmap.scroll(NeoAbstractWidget.Y, visible[0] - (visible[1] - visible[0]) / 10);
		seqmap.updateWidget();
	}
}
