package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.igb.IGB;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author sgblanch
 * @version $Id: ExitAction.java 11358 2012-05-02 13:28:22Z anuj4159 $
 */
public class ExitAction extends GenericAction {
	private static final long serialVersionUID = 1l;
	private static final ExitAction ACTION = new ExitAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ExitAction getAction() {
		return ACTION;
	}

	private ExitAction() {
		super(BUNDLE.getString("exit"), null, "16x16/actions/process-stop.png", "22x22/actions/process-stop.png", KeyEvent.VK_X);
	}

	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(
				new WindowEvent(
					IGB.getSingleton().getFrame(),
					WindowEvent.WINDOW_CLOSING));
	}
}
