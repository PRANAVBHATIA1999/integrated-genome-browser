package com.affymetrix.igb.action;

import javax.swing.KeyStroke;
import com.affymetrix.igb.prefs.KeyStrokeEditPanel;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.view.load.GeneralLoadView;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author sgblanch
 * @version $Id$
 */
public class RefreshDataAction extends AbstractAction {
	private static final long serialVersionUID = 1l;
	private static final RefreshDataAction singleton = new RefreshDataAction();
	
	
	private RefreshDataAction() {
		super(BUNDLE.getString("refreshDataButton"));
		String defStr = KeyStrokeEditPanel.keyStroke2String(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK));
		this.putValue(MNEMONIC_KEY, PreferenceUtils.getAccelerator(BUNDLE.getString("refreshDataButton"), 
				KeyEvent.VK_R, defStr));
		this.putValue(SHORT_DESCRIPTION, BUNDLE.getString("refreshDataTip"));
	}

	public static RefreshDataAction getAction() {
		return singleton;
	}

	public void actionPerformed(ActionEvent ae) {
		GeneralLoadView.getLoadView().loadVisibleData();
	}
}
