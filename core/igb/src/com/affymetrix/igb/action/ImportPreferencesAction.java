package com.affymetrix.igb.action;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.InvalidPreferencesFormatException;

import javax.swing.JFileChooser;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.prefs.IPrefEditorComponent;
import com.affymetrix.igb.prefs.PreferencesPanel;

public class ImportPreferencesAction extends GenericAction {

	private static final long serialVersionUID = 1L;
	private static final ImportPreferencesAction ACTION = new ImportPreferencesAction();
	private final static String IMPORT_ACTION_COMMAND = PreferencesPanel.WINDOW_NAME + " / " + BUNDLE.getString("ImportPreferences");

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ImportPreferencesAction getAction() {
		return ACTION;
	}

	private ImportPreferencesAction() {
		super(BUNDLE.getString("ImportPreferences"), null, null, null, KeyEvent.VK_I, null, true);
		putValue(ACTION_COMMAND_KEY, IMPORT_ACTION_COMMAND);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		super.actionPerformed(ae);
		JFileChooser chooser = PreferenceUtils.getJFileChooser();
		int option = chooser.showOpenDialog(PreferencesPanel.getSingleton());
		if (option == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			try {
				PreferenceUtils.importPreferences(f);
			} catch (InvalidPreferencesFormatException ipfe) {
				ErrorHandler.errorPanel("ERROR", "Invalid preferences format:\n" + ipfe.getMessage()
						+ "\n\nYou can only IMPORT preferences from a file that was created with EXPORT.  "
						+ "In particular, you cannot import the file 'igb_prefs.xml' that was "
						+ "used in earlier versions of this program.");
			} catch (Exception e) {
				ErrorHandler.errorPanel("ERROR", "Error importing preferences from file", e);
			}
		}
		IPrefEditorComponent[] components = PreferencesPanel.getSingleton().getPrefEditorComponents();
		for (int i = 0; i < components.length; i++) {
			components[i].refresh();
		}
	}
}
