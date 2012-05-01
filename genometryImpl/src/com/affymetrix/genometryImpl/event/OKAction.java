package com.affymetrix.genometryImpl.event;

import java.awt.event.ActionEvent;


public class OKAction extends GenericAction {
	private static final long serialVersionUID = 1l;
	private static final OKAction ACTION = new OKAction();

	public static OKAction getAction() {
		return ACTION;
	}

	private OKAction() {
		super("OK", null, null);//BUNDLE.getString("ok");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
	}
}
