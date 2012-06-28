/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.igb.action;

import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.prefs.PreferencesPanel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.*;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author dcnorris
 */
public class ExportPreferencesAction extends GenericAction {

	private static final long serialVersionUID = 1L;
	private static final ExportPreferencesAction ACTION = new ExportPreferencesAction();
	private final static String EXPORT_ACTION_COMMAND = PreferencesPanel.WINDOW_NAME + " / " + BUNDLE.getString("ExportPreferences");

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ExportPreferencesAction getAction() {
		return ACTION;
	}

	private ExportPreferencesAction() {
		super(BUNDLE.getString("ExportPreferences"), null, "16x16/actions/document-save.png", null, KeyEvent.VK_E, null, true);
		putValue(ACTION_COMMAND_KEY, EXPORT_ACTION_COMMAND);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		super.actionPerformed(ae);
		JFileChooser chooser = PreferenceUtils.getJFileChooser();
		int option = chooser.showSaveDialog(IGB.getSingleton().getMapView().getSeqMap().getNeoCanvas());
		if (option == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			try {
				Preferences prefs = PreferenceUtils.getTopNode();
				PreferenceUtils.exportPreferences(prefs, f);
			} catch (Exception e) {
				ErrorHandler.errorPanel("ERROR", "Error saving preferences to file", e);
			}
		}
	}
}
