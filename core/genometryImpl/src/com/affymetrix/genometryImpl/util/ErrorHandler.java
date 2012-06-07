/**
 * Copyright (c) 2001-2005 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.genometryImpl.util;

import java.awt.Component;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.*;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.OKAction;
import com.affymetrix.genometryImpl.event.ReportBugAction;

/**
 * Simple routines for bringing-up an error message panel and also logging the
 * error to standard output.
 *
 * @author ed
 */
public abstract class ErrorHandler implements DisplaysError{
	private static DisplaysError displayHandler;
	static{
		setDisplayHandler(null);
	}
	
	
	public static void setDisplayHandler(DisplaysError dH){
		if(dH == null){
			displayHandler = new ErrorHandler(){};
		}else{
			displayHandler = dH;
		}
	}
	
	/**
	 * Error panel with default title.
	 */
	public static void errorPanel(String message) {
		errorPanel("ERROR", message, Level.SEVERE);
	}

	/**
	 * Error panel with default title and given Throwable.
	 *
	 * @param message
	 * @param e
	 */
	public static void errorPanel(String message, Throwable e, Level level) {
		errorPanel("ERROR", message, e, level);
	}

	public static void errorPanel(String title, String message, Level level) {
		errorPanel(title, message, (Throwable) null, level);
	}

	public static void errorPanel(String title, String message, Component c, Level level) {
		errorPanel(title, message, c, (Throwable) null, level);
	}
		
	public static void errorPanel(String title, String message, Component c, Throwable e, Level level) {
		JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, c);
		errorPanel(frame, title, message, e, level);
	}

	public static void errorPanel(String title, String message, Throwable e, Level level) {
		errorPanel((JFrame) null, title, message, e, level);
	}

	public static void errorPanel(final String title, String message, final List<Throwable> errs, Level level) {
		errorPanel((JFrame) null, title, message, errs, null, level);
	}
	
	private static void errorPanel(final JFrame frame, final String title, String message, final Throwable e, Level level) {
		List<Throwable> errs = new ArrayList<Throwable>();
		if (e != null) {
			errs.add(e);
		}
		errorPanel(frame, title, message, errs, null, level);
	}
	
	public static void errorPanelWithReportBug(String title, String message, Level level) {
		List<GenericAction> actions = new ArrayList<GenericAction>();
		actions.add(OKAction.getAction());
		actions.add(ReportBugAction.getAction());
		errorPanel((JFrame) null, title, message, new ArrayList<Throwable>(), actions, level);
	}
	
	public static void errorPanelWithReportBug(String title, String message) {
		List<GenericAction> actions = new ArrayList<GenericAction>();
		actions.add(OKAction.getAction());
		actions.add(ReportBugAction.getAction());
		errorPanel((JFrame) null, title, message, new ArrayList<Throwable>(), actions, Level.SEVERE);
	}

	/**
	 * Opens a JOptionPane.ERROR_MESSAGE panel with the given frame as its
	 * parent. This is designed to probably be safe from the EventDispatchThread
	 * or from any other thread.
	 *
	 * @param frame the parent frame, null is ok.
	 * @param title
	 * @param message
	 * @param e an exception (or error), if any. null is ok. If not null, the
	 * exception text will be appended to the message and a stack trace might be
	 * printed on standard error.
	 */
	public static void errorPanel(final JFrame frame, final String title, String message, final List<Throwable> errs, final List<GenericAction> actions, Level level) {
		// logging the error to standard out is redundant, but preserves
		// the past behavior.  The flush() methods make sure that
		// messages from system.out and system.err don't get out-of-synch
		System.out.flush();
		System.err.flush();
		System.err.println();
		System.err.println("-------------------------------------------------------");

		if (!errs.isEmpty()) {
			for (Throwable e : errs) {
				String error_message = e.toString();
				message = message + "\n" + error_message;
				Throwable cause = e.getCause(); 
				while (cause != null) {
					message += "\n\nCaused by:\n" + cause.toString();
					cause = cause.getCause();
				}
			}
		}

		System.err.println(title + ": " + message);
		if (!errs.isEmpty()) {
			for (Throwable e : errs) {
				if (e instanceof FileNotFoundException) {
					// do nothing.  Error already printed above, stack trace not usually useful
					//System.err.println("FileNotFoundException: " + e.getMessage());
				} else {
					message += "\nSee console output for more details about this error.";
					e.printStackTrace(System.err);
				}
			}
		}

		System.err.println("-------------------------------------------------------");
		System.err.println();
		System.err.flush();
		Toolkit.getDefaultToolkit().beep();
		displayHandler.showError(frame, title, message, actions, level);
	}

	private static JScrollPane makeScrollPane(String message) {
		JTextPane text = new JTextPane();
		text.setContentType("text/plain");
		text.setText(message);
		text.setEditable(false);
		text.setCaretPosition(0); // scroll to the top
		JScrollPane scroller = new JScrollPane(text);
		scroller.setPreferredSize(new java.awt.Dimension(400, 100));
		return scroller;
	}

	private static void processDialog(JOptionPane pane, JDialog dialog, List<GenericAction> actions) {
		dialog.setVisible(true);
		Object selectedValue = pane.getValue();
		if (selectedValue != null && actions != null) {
			for (GenericAction action : actions) {
				if (action != null && selectedValue.equals(action.getText())) {
					action.actionPerformed(null);
				}
			}
		}
	}
		
	public void showError(final JFrame frame, final String title, final String message, final List<GenericAction> actions, Level level) {
		final Component scroll_pane = makeScrollPane(message);

		String[] options = null;
		if (actions != null) {
			options = new String[actions.size()];
			for (int i = 0; i < actions.size(); i++) {
				options[i] = actions.get(i).getText();
			}
		}
		final JOptionPane pane = new JOptionPane(scroll_pane, JOptionPane.ERROR_MESSAGE,
				JOptionPane.DEFAULT_OPTION, null, options);
		final JDialog dialog = pane.createDialog(frame, title);
		dialog.setResizable(true);

		if (SwingUtilities.isEventDispatchThread()) {
			processDialog(pane, dialog, actions);
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					processDialog(pane, dialog, actions);
				}
			});
		}
	}

}