package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.util.GeneralUtils;
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
public class DocumentationAction extends AbstractAction {
	private static final long serialVersionUID = 1l;

	public DocumentationAction() {
		super(MessageFormat.format(
					BUNDLE.getString("menuItemHasDialog"),
					BUNDLE.getString("documentation")),
				MenuUtil.getIcon("toolbarButtonGraphics/general/Help16.gif"));
		this.putValue(MNEMONIC_KEY, KeyEvent.VK_D);
	}

	public void actionPerformed(ActionEvent e) {
		GeneralUtils.browse("http://wiki.transvar.org/confluence/display/igbman");
	}

}
