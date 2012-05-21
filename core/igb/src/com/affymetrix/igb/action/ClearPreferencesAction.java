package com.affymetrix.igb.action;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.stylesheet.XmlStylesheetParser;

public class ClearPreferencesAction extends GenericAction {

	private static final long serialVersionUID = 1l;
	private static final ClearPreferencesAction ACTION = new ClearPreferencesAction();
	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ClearPreferencesAction getAction() {
		return ACTION;
	}

	private ClearPreferencesAction() {
		super(BUNDLE.getString("ClearPreferences"), "16x16/actions/edit-clear.png", "22x22/actions/edit-clear.png");
	}

	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		// The option pane used differs from the confirmDialog only in
		// that "No" is the default choice.
		String[] options = {"Yes", "No"};
		if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
				Application.getSingleton().getFrame(), "Really reset all preferences to defaults?\n(this will also exit the application)", "Clear preferences?",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, options[1])) {

			try {
				XmlStylesheetParser.removeUserStylesheetFile();
				((IGB) Application.getSingleton()).defaultCloseOperations();
				PreferenceUtils.clearPreferences();
				System.exit(0);
			} catch (Exception ex) {
				ErrorHandler.errorPanel("ERROR", "Error clearing preferences", ex);
			}
		}
	}
}
