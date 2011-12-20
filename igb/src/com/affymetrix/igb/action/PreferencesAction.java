package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.igb.prefs.PreferencesPanel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author sgblanch
 * @version $Id$
 */
public class PreferencesAction extends GenericAction {
	private static final long serialVersionUID = 1l;
	private static final PreferencesAction ACTION = new PreferencesAction();

	public static PreferencesAction getAction() {
		return ACTION;
	}

	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		PreferencesPanel.getSingleton().getFrame().setVisible(true);
	}

	@Override
	public String getText() {
		return BUNDLE.getString("preferences");
	}

	@Override
	public String getIconPath() {
		return null;
	}

	@Override
	public int getMnemonic() {
		return KeyEvent.VK_E;
	}

	@Override
	public boolean isPopup() {
		return true;
	}
}
