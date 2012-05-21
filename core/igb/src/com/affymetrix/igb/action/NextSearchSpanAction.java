package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.IGBConstants;

public class NextSearchSpanAction extends GenericAction {
	private static final long serialVersionUID = 1L;
	private static final NextSearchSpanAction ACTION = new NextSearchSpanAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static NextSearchSpanAction getAction() {
		return ACTION;
	}

	private NextSearchSpanAction() {
		super(IGBConstants.BUNDLE.getString("nextSearchSpan"), "16x16/actions/go-next.png","22x22/actions/go-next.png");
		setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		IGB.getSingleton().getMapView().getMapRangeBox().nextSpan();
	}
}
