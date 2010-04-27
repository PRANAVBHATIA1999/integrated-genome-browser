package com.affymetrix.igb.action;

import com.affymetrix.igb.IGB;
import com.affymetrix.igb.menuitem.MenuUtil;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author sgblanch
 * @version $Id$
 */
public class PrintAction extends AbstractAction {
	private static final long serialVersionUID = 1l;

	public PrintAction() {
		super(MessageFormat.format(
					BUNDLE.getString("menuItemHasDialog"),
					BUNDLE.getString("print")),
				MenuUtil.getIcon("toolbarButtonGraphics/general/Print16.gif"));
		this.putValue(MNEMONIC_KEY, KeyEvent.VK_P);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			IGB.getSingleton().getMapView().getSeqMap().print();
		} catch (Exception ex) {
			IGB.errorPanel("Problem trying to print.", ex);
		}
	}
}
