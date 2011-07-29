package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author sgblanch
 * @version $Id$
 */
public class UnclampViewAction extends AbstractAction {
	private static final long serialVersionUID = 1l;

	public UnclampViewAction() {
		super(BUNDLE.getString("unclamp"));
		this.putValue(MNEMONIC_KEY, KeyEvent.VK_U);
	}

	public void actionPerformed(ActionEvent e) {
		//IGB.getSingleton().getMapView().unclamp();
	}

}
