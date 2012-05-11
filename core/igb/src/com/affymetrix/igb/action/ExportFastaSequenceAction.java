package com.affymetrix.igb.action;


import com.affymetrix.genometryImpl.event.GenericAction;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.view.AbstractSequenceViewer;

/**
 *
 * @author sgblanch
 * @version $Id: ExitAction.java 5804 2010-04-28 18:54:46Z sgblanch $
 */
public class ExportFastaSequenceAction extends GenericAction {
	private static final long serialVersionUID = 1l;
	AbstractSequenceViewer sv;
	public ExportFastaSequenceAction(AbstractSequenceViewer sv) {
		super(BUNDLE.getString("fastasequence"), KeyEvent.VK_S);
		this.sv=sv;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		sv.exportSequenceFasta(false);
	}
}
