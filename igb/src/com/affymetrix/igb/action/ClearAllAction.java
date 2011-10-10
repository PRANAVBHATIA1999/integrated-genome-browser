package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.igb.IGB;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author sgblanch
 * @version $Id$
 */
public class ClearAllAction extends GenericAction {
	private static final long serialVersionUID = 1l;
	private static final ClearAllAction ACTION = new ClearAllAction();

	public static ClearAllAction getAction() {
		return ACTION;
	}

	public void actionPerformed(ActionEvent e) {
		if (IGB.confirmPanel("Really clear entire view?")) {
			//IGB.getSingleton().getMapView().clear();
		}
	}

	@Override
	public String getText() {
		return MessageFormat.format(
				BUNDLE.getString("menuItemHasDialog"),
				BUNDLE.getString("clearAll"));
	}

	@Override
	public int getMnemonic() {
		return KeyEvent.VK_C;
	}
}
