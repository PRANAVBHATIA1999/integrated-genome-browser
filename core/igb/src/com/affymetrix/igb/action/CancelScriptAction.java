package com.affymetrix.igb.action;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.thread.CThreadWorker;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;

/**
 * note !!! - depending on the script, it may not be possible to cancel it
 */
public class CancelScriptAction extends GenericAction {

	private static final long serialVersionUID = 1L;
	private static final CancelScriptAction ACTION = new CancelScriptAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static CancelScriptAction getAction() {
		return ACTION;
	}

	private CancelScriptAction() {
		super(BUNDLE.getString("cancelScript"), null, "16x16/actions/media-playback-stop.png", "22x22/actions/media-playback-stop.png", KeyEvent.VK_X);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		final IGB igb = ((IGB)Application.getSingleton());
		synchronized(igb) {
			CThreadWorker<Void, Void> igbScriptWorker = igb.getScriptWorker();
			if (igbScriptWorker == null) {
				ErrorHandler.errorPanel("script error", "no script is running", Level.SEVERE);
			}
			else {
				igbScriptWorker.cancel(true);
				igb.setScriptWorker(null);
			}
		}
	}
}
