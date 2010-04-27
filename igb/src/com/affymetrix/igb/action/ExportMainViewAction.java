package com.affymetrix.igb.action;

import com.affymetrix.igb.IGB;
import com.affymetrix.igb.util.ComponentWriter;
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
public class ExportMainViewAction extends AbstractAction {
	private static final long serialVersionUID = 1l;

	public ExportMainViewAction() {
		super(MessageFormat.format(
					BUNDLE.getString("menuItemHasDialog"),
					BUNDLE.getString("mainView")));
		this.putValue(MNEMONIC_KEY, KeyEvent.VK_M);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			ComponentWriter.showExportDialog(IGB.getSingleton().getMapView().getSeqMap().getNeoCanvas());
		} catch (Exception ex) {
			IGB.errorPanel("Problem during output.", ex);
		}
	}
}
