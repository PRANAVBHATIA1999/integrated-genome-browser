/**
 *   Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 *   Licensed under the Common Public License, Version 1.0 (the "License").
 *   A copy of the license must be included with any distribution of
 *   this source code.
 *   Distributions from Affymetrix, Inc., place this in the
 *   IGB_LICENSE.html file.
 *
 *   The license is also available at
 *   http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.util.ErrorHandler;
import java.io.*;
import java.util.*;
import java.awt.event.*;

import javax.swing.*;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genometryImpl.util.UniFileFilter;
import com.affymetrix.genoviz.swing.recordplayback.JRPFileChooser;
import com.affymetrix.genoviz.swing.recordplayback.ScriptManager;
import com.affymetrix.genoviz.swing.recordplayback.ScriptProcessorHolder;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;

import com.affymetrix.igb.shared.FileTracker;

import static com.affymetrix.igb.IGBConstants.BUNDLE;
import java.util.logging.Level;

public final class RunScriptAction extends GenericAction {

	private static final long serialVersionUID = 1L;
	private static final RunScriptAction ACTION = new RunScriptAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static RunScriptAction getAction() {
		return ACTION;
	}
	private final JFrame gviewerFrame;
	private final FileTracker load_dir_tracker;
	private JRPFileChooser chooser = null;

	private RunScriptAction() {
		super(BUNDLE.getString("runScript"), null,
				"16x16/actions/run_script.png",
				"22x22/actions/run_script.png",
				KeyEvent.VK_R, null, true);

		this.gviewerFrame = ((IGB) IGB.getSingleton()).getFrame();
		load_dir_tracker = FileTracker.DATA_DIR_TRACKER;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		loadFile(load_dir_tracker, gviewerFrame);
	}

	private JRPFileChooser getFileChooser(String id) {
		chooser = new JRPFileChooser(id);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new UniFileFilter(
				ScriptProcessorHolder.getInstance().getScriptExtensions(),
				"Script File"));

		Set<String> all_known_endings = new HashSet<String>();
		for (javax.swing.filechooser.FileFilter filter : chooser.getChoosableFileFilters()) {
			if (filter instanceof UniFileFilter) {
				UniFileFilter uff = (UniFileFilter) filter;
				uff.addCompressionEndings(GeneralUtils.compression_endings);
				all_known_endings.addAll(uff.getExtensions());
			}
		}
		UniFileFilter all_known_types = new UniFileFilter(
				all_known_endings.toArray(new String[all_known_endings.size()]),
				"Known Types");
		all_known_types.setExtensionListInDescription(false);
		all_known_types.addCompressionEndings(GeneralUtils.compression_endings);
		chooser.addChoosableFileFilter(all_known_types);
		chooser.setFileFilter(all_known_types);
		return chooser;
	}

	/** Load a file into the global singleton genometry model. */
	private void loadFile(final FileTracker load_dir_tracker, final JFrame gviewerFrame) {
		JRPFileChooser fileChooser = getFileChooser("runScript");
		File currDir = load_dir_tracker.getFile();
		if (currDir == null) {
			currDir = new File(System.getProperty("user.home"));
		}
		fileChooser.setCurrentDirectory(currDir);
		fileChooser.rescanCurrentDirectory();

		int option = fileChooser.showOpenDialog(gviewerFrame);

		if (option != JFileChooser.APPROVE_OPTION) {
			return;
		}

		load_dir_tracker.setFile(fileChooser.getCurrentDirectory());

		final File file = fileChooser.getSelectedFile();
		if (ScriptManager.getInstance().isScript(file.getAbsolutePath())) {
			runScript(file.getAbsolutePath());
		} else {
			ErrorHandler.errorPanel("script error", file.getAbsolutePath() + " is not a valid script file", Level.SEVERE);
		}
	}
	
	public void runScript(final String filePath) {
		final IGB igb = ((IGB) Application.getSingleton());
		synchronized (igb) {
			if (igb.getScriptWorker() != null) {
				ErrorHandler.errorPanel("script error", "another script is running, only one can run at a time", Level.SEVERE);
			} else {
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() {
						ScriptManager.getInstance().runScript(filePath);
						return null;
					}

					@Override
					protected void done() {
						igb.setScriptWorker(null);
					}
				};
				igb.setScriptWorker(worker);
				ThreadUtils.getPrimaryExecutor(ScriptManager.SCRIPTING).execute(worker);
			}
		}
	}
}
