package com.affymetrix.igb.action;

import javax.swing.JColorChooser;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;

import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.shared.Selections;
import java.awt.Color;

public class ChangeReverseColorAction extends ChangeColorActionA {
	private static final long serialVersionUID = 1L;
	private static final ChangeReverseColorAction ACTION = new ChangeReverseColorAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
		Selections.addRefreshSelectionListener(ACTION.listener);
	}
	public static ChangeReverseColorAction getAction() {
		return ACTION;
	}

	public ChangeReverseColorAction() {
		super(IGBConstants.BUNDLE.getString("changeReverseColorAction"), null, null);
	}

	@Override
	protected void setChooserColor(JColorChooser chooser, ITrackStyleExtended style) {
		chooser.setColor(style.getReverseColor());
	}

	@Override
	protected void setStyleColor(Color color, ITrackStyleExtended style) {
		style.setReverseColor(color);
	}
}
