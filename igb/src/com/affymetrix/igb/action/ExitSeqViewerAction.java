package com.affymetrix.igb.action;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author sgblanch
 * @version $Id: ExitAction.java 5804 2010-04-28 18:54:46Z sgblanch $
 */
public class ExitSeqViewerAction extends AbstractAction {
	private static final long serialVersionUID = 1l;
	Frame mapframe;
	public ExitSeqViewerAction(Frame mapframe) {
		super(BUNDLE.getString("closeSequenceViewer"));
		this.putValue(MNEMONIC_KEY, KeyEvent.VK_W);
		this.mapframe=mapframe;
	}

	public void actionPerformed(ActionEvent e) {
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
				new WindowEvent(mapframe,
					WindowEvent.WINDOW_CLOSING));
	}
}
