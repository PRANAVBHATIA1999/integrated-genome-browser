package com.affymetrix.igb.action;

import java.awt.Color;

import javax.swing.JColorChooser;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;

import com.affymetrix.igb.IGBConstants;

public class ChangeForegroundColorAction extends ChangeColorActionA {
	private static final long serialVersionUID = 1L;
	private static final ChangeForegroundColorAction ACTION = new ChangeForegroundColorAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ChangeForegroundColorAction getAction() {
		return ACTION;
	}

	public ChangeForegroundColorAction() {
		super(IGBConstants.BUNDLE.getString("changeFGColorAction"), "16x16/categories/applications-graphics.png", "22x22/categories/applications-graphics.png");
	}

	@Override
	protected void setChooserColor(JColorChooser chooser, ITrackStyleExtended style) {
		chooser.setColor(style.getForeground());
	}

	@Override
	protected void setStyleColor(Color color, ITrackStyleExtended style) {
		style.setForeground(color);
	}

}
