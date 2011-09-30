package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.igb.IGB;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author sgblanch
 * @version $Id$
 */
public class ClampViewAction extends GenericAction {
	private static final long serialVersionUID = 1l;
	private static final ClampViewAction ACTION = new ClampViewAction();

	private ClampViewAction() {
		super();
		this.putValue(SELECTED_KEY, false);
	}

	public static ClampViewAction getAction(){
		return ACTION;
	}
	
	public void actionPerformed(ActionEvent e) {
		IGB.getSingleton().getMapView().toggleClamp();
	}

	@Override
	public String getText() {
		return BUNDLE.getString("clampToView");
	}

	@Override
	public int getShortcut() {
		return KeyEvent.VK_V;
	}
}
