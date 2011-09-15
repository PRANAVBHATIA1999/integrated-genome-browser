package com.affymetrix.igb.shared;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import com.affymetrix.genoviz.swing.MenuUtil;
import com.affymetrix.igb.action.IGBActionHolder;

public abstract class IGBAction extends AbstractAction implements ActionListener {
	private static final long serialVersionUID = 1L;

	public IGBAction() {
		super();
		putValue(Action.NAME, getText());
		if (getIconPath() != null) {
			ImageIcon icon = MenuUtil.getIcon(getIconPath());
			if (icon == null) {
				System.out.println("icon " + getIconPath() + " returned null");
			}
			putValue(Action.SMALL_ICON, icon);
		}
		if (getShortcut() != KeyEvent.VK_UNDEFINED) {
			this.putValue(MNEMONIC_KEY, getShortcut());
		}
		IGBActionHolder.getInstance().addIGBAction(this);
	}
	public String getIconPath() {
		return null;
	}
	public abstract String getText();
	public void actionPerformed(ActionEvent e) {
		IGBActionHolder.getInstance().notifyActionPerformed(this);
	}
	public int getShortcut() { return KeyEvent.VK_UNDEFINED; }
}
