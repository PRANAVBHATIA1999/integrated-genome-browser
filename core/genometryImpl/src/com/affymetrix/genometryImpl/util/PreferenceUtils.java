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
package com.affymetrix.genometryImpl.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.affymetrix.common.CommonUtils;

/**
 * Helps to save and load preferences such as locations of windows.
 * The {@link Preferences} class is pretty easy to use, so the real utility of
 * this class is to make sure that preference names and values follow
 * some conventions and take valid values.
 *
 * @version $Id: PreferenceUtils.java 10863 2012-03-26 13:37:15Z anuj4159 $
 */
public abstract class PreferenceUtils {

	public static final String PREFS_MAIN = "/com/affymetrix";
	public static final String MENU_NODE_NAME = "main_menu";
	/** The name of a boolean preference. Setting to true to be sure to save bookmarks.*/
	public static final String ASK_BEFORE_EXITING = "Ask before exiting";
	public static final String CONFIRM_BEFORE_DELETE = "Confirm before delete";
	public static final String CONFIRM_BEFORE_CLEAR = "Confirm before clear";
	public static final String CONFIRM_BEFORE_LOAD = "Confirm before load";
	public static final String CONFIRM_BEFORE_REFRESH = "Confirm before refresh";
	public static final String AUTO_LOAD = "Auto Load Data";
	public static final String COVERAGE_SUMMARY_HEATMAP = "Coverage Summary as HeatMap";
	public static final String DISPLAY_ERRORS_STATUS_BAR = "Display Errors on Status Bar";
	public static final String SHOW_COLLAPSE_OPTION = "Show Collapse Option";
	public static final String SHOW_EDGEMATCH_OPTION = "Show Edge Matching Option";
	public static final String SHOW_IGB_TRACKMARK_OPTION = "Show IGB Track Mark";
	public static final boolean default_display_errors = false;
	public static final boolean default_ask_before_exiting = true;
	public static final boolean default_confirm_before_delete = true;
	public static final boolean default_confirm_before_clear = true;
	public static final boolean default_confirm_before_load = true;
	public static final boolean default_confirm_before_refresh = true;
	public static final boolean default_auto_load = true;
	public static final boolean default_coverage_summary_heatmap = true;
	private static final String DEFAULT_PREFS_MODE = "igb";
	private static final String SLASH_STANDIN = "%";
	private static String prefs_mode = DEFAULT_PREFS_MODE;
	public static final String  PREFS_THRESHOLD = "Threshold Value";
	public static final String  PREFS_AUTOLOAD = "Enable Auto load";
	private static JFileChooser static_chooser = null;
	private static final SortedSet<String> keystroke_node_names = Collections.<String>synchronizedSortedSet(new TreeSet<String>());

	/**
	 * Returns the top preferences node for the "com/affymetrix/igb" package.
	 * Warning: do not try to store a preference node in a
	 * static variable.  This method will re-create the
	 * node even if it has been deleted with Preferences.removeNode(),
	 * whereas trying to use a static variable could lead to
	 * IllegalStateException's.
	 */
	public static Preferences getTopNode() {
		return Preferences.userRoot().node(PREFS_MAIN + "/" + prefs_mode);
	}

	public static Preferences getAltNode(String name) {
		return Preferences.userRoot().node(PREFS_MAIN + "/" + name);
	}

	public static Preferences getKeystrokesNode() {
		return PreferenceUtils.getTopNode().node("keystrokes");
	}

	public static Preferences getToolbarNode() {
		return PreferenceUtils.getTopNode().node("toolbar");
	}

	public static Preferences getLocationsNode() {
		return PreferenceUtils.getTopNode().node("locations");
	}

	public static Preferences getGenomesNode() {
		return PreferenceUtils.getTopNode().node("genomes");
	}

	public static Preferences getServersNode() {
		return PreferenceUtils.getTopNode().node("servers");
	}

	public static Preferences getRepositoriesNode() {
		return PreferenceUtils.getTopNode().node("repositories");
	}

	public static Preferences getGraphPrefsNode() {
		return PreferenceUtils.getTopNode().node("graphs");
	}

	public static Preferences getWindowPrefsNode() {
		return PreferenceUtils.getTopNode().node("window");
	}

	public static Preferences getSessionPrefsNode() {
		return PreferenceUtils.getTopNode().node("session");
	}

	public static Preferences getExportPrefsNode() {
		return PreferenceUtils.getTopNode().node("export");
	}

	public static Preferences getCertificatePrefsNode(){
		return PreferenceUtils.getTopNode().node("certificate");
	}
		
	public static void saveIntParam(String param_name, int param) {
		try {
			getTopNode().putInt(param_name, param);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public static int getIntParam(String param_name, int def) {
		return getTopNode().getInt(param_name, def);
	}

	public static boolean getBooleanParam(String param_name, boolean def) {
		return getTopNode().getBoolean(param_name, def);
	}

	/**
	 * Saves the current location of a window to the user's preferences.
	 *
	 * @param w     the window, in the desired location
	 * @param name  a unique identifying name
	 */
	public static void saveWindowLocation(Window w, String name) {
		Rectangle r = w.getBounds();
		try {
			Preferences p = getWindowPrefsNode();
			p.putInt(name + " x", r.x);
			p.putInt(name + " y", r.y);
			p.putInt(name + " width", r.width);
			p.putInt(name + " height", r.height);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Retrieves the previously-stored preferred location of a window.
	 * Since this data may not have been saved, you must supply a default value.
	 *
	 * @param name   a unique identifying name
	 * @param def    a default location
	 */
	public static Rectangle retrieveWindowLocation(String name, Rectangle def) {
		Rectangle r = new Rectangle();
		Preferences p = getWindowPrefsNode();
		r.x = p.getInt(name + " x", def.x);
		r.y = p.getInt(name + " y", def.y);
		r.width = p.getInt(name + " width", def.width);
		r.height = p.getInt(name + " height", def.height);
		return r;
	}

	/**
	 * Sets the size of a window, making sure the window does not
	 * start at a negative coordinate, nor have a size larger than 99% of the
	 * current screen size.
	 * (You are responsible for calling doLayout() yourself.)
	 *
	 * @param w
	 * @param r 
	 */
	public static void setWindowSize(Window w, Rectangle r) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension dim = kit.getScreenSize();
		if (r.x < 0 || r.x > dim.width) {
			r.x = 0;
		}
		if (r.y < 0 || r.y > dim.height) {
			r.y = 0;
		}
		if (r.width < 50) {
			r.width = 50;
		}
		if (r.height < 50) {
			r.height = 50;
		}
		r.width = Math.min(r.width, (int) (dim.width * 0.99));
		r.height = Math.min(r.height, (int) (dim.height * 0.99));

		w.setBounds(r);
		if (w instanceof Frame) {
			((Frame) w).setState(Frame.NORMAL);
		}
	}

	/**
	 * Saves the state of a component.
	 *
	 * @param name name of tray or tab
	 * @param state state of tray or tab
	 */
	public static void saveComponentState(String name, String state) {
		if (state == null) {
			getWindowPrefsNode().remove(name + " state");
		} else {
			getWindowPrefsNode().put(name + " state", state);
		}
	}

	/**
	 * Returns the previously-stored state of a component.
	 *
	 * @param name name of tray or tab
	 * @return state of tray or tab
	 */
	public static String getComponentState(String name) {
		return getWindowPrefsNode().get(name + " state", null);
	}

	/**
	 * Saves the selected tab of a tray.
	 *
	 * @param tray name of the tray 
	 * @param tab name of selected tab
	 */
	public static void saveSelectedTab(String tray, String tab) {
		if (tab == null) {
			getWindowPrefsNode().remove(tray + " selected");
		} else {
			getWindowPrefsNode().put(tray + " selected", tab);
		}
	}

	/**
	 * Returns the selected tab of a tray.
	 *
	 * @param tray name of the tray 
	 * @return name of selected tab
	 */
	public static String getSelectedTab(String tray) {
		return getWindowPrefsNode().get(tray + " selected", null);
	}

	/**
	 * Saves the divider location of a tray as a percentage.
	 *
	 * @param tray name of the tray 
	 * @param dividerProportionalLocation the location of the divider in extended state
	 * as a percentage
	 */
	public static void saveDividerLocation(String tray, double dividerProportionalLocation) {
		getWindowPrefsNode().put(tray + " dvdrloc", String.valueOf(dividerProportionalLocation));
	}

	/**
	 * Returns the previously-stored divider location of a tray.
	 *
	 * @param tray name of the tray 
	 * @return the location of the divider in extended state as a percentage
	 */
	public static double getDividerLocation(String tray) {
		String locString = getWindowPrefsNode().get(tray + " dvdrloc", null);
		double dividerLocation = -1;
		try {
			dividerLocation = Double.parseDouble(locString);
		} catch (Exception x) {
		}
		return dividerLocation;
	}

	/**
	 * Gets a static re-usable file chooser that prefers XML files.
	 */
	public static JFileChooser getJFileChooser() {
		if (static_chooser == null) {
			static_chooser = new UniFileChooser("XML File", "xml");
		}
		static_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		//static_chooser.setCurrentDirectory(FileTracker.DATA_DIR_TRACKER.getFile());
		static_chooser.rescanCurrentDirectory();
		return static_chooser;
	}

	/**
	 * Exports the preferences subtree to a file.
	 * Calls {@link Preferences#exportSubtree(OutputStream)}.
	 *
	 * @param prefs
	 * @param f
	 * @throws IOException
	 * @throws BackingStoreException
	 */
	public static void exportPreferences(Preferences prefs, File f)
			throws IOException, BackingStoreException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			prefs.exportSubtree(fos);
		} finally {
			GeneralUtils.safeClose(fos);
		}
	}

	/**
	 * Imports and merges preferences from a file.
	 * It is not possible to limit the effects of this action
	 * to only preferences in the "com.affymetrix.igb" subtree.
	 * Any preferences in the file will be read and merged into the
	 * user's preferences.
	 *
	 * @param f
	 * @throws IOException
	 * @throws InvalidPreferencesFormatException
	 * @see Preferences#importPreferences(InputStream)
	 */
	public static void importPreferences(File f)
			throws IOException, InvalidPreferencesFormatException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			Preferences.importPreferences(fis);
		} finally {
			GeneralUtils.safeClose(fis);
		}
	}

	/**
	 * Imports and merges preferences from a file.
	 * It is not possible to limit the effects of this action
	 * to only preferences in the "com.affymetrix.igb" subtree.
	 * Any preferences in the file will be read and merged into the
	 * user's preferences.
	 *
	 * @throws IOException
	 * @throws InvalidPreferencesFormatException
	 * @see Preferences#importPreferences(InputStream)
	 */
	public static void importPreferences(URL url)
			throws IOException, InvalidPreferencesFormatException {
		InputStream is = null;
		try {
			URLConnection uc = url.openConnection();
			is = uc.getInputStream();
			Preferences.importPreferences(is);
		} finally {
			GeneralUtils.safeClose(is);
		}
	}

	/**
	 * Clears ALL stored preferences under the top node of {@link #getTopNode()}.
	 * This could have serious consequences.  The most significant negative
	 * consequence is that any PreferenceChangeListener's on any of the
	 * preference nodes will stop getting messages.
	 *
	 * @throws BackingStoreException 
	 */
	public static void clearPreferences() throws BackingStoreException {
		getTopNode().removeNode();
	}

	/**
	 * clear all preferences for all preference modes.
	 * @throws BackingStoreException
	 */
	public static void clearAllPreferences() throws BackingStoreException {
		Preferences.userRoot().node(PREFS_MAIN).removeNode();
	}

	/**
	 * print preferences in xml format
	 * @throws BackingStoreException
	 */
	public static void printPreferences() throws BackingStoreException, IOException {
		getTopNode().exportSubtree(System.out);
	}

	/**
	 * print all preferences for all preference modes in xml format.
	 * @throws BackingStoreException
	 */
	public static void printAllPreferences() throws BackingStoreException, IOException {
		Preferences.userRoot().node(PREFS_MAIN).exportSubtree(System.out);
	}

	public static Collection<String> getKeystrokesNodeNames() {
		/* Can not return an unmodifiableSet() because of synchronization */
		return keystroke_node_names;
	}

	/**
	 * Finds the KeyStroke that was specified in the preferences
	 * for the given action_command String.
	 *
	 * @param action_command  a String used to uniquely identify an action
	 *        both in the program and in the preferences file;  these names
	 *        need to be globably unique within the application
	 * @return null if no preference was set or the given String is null
	 */
	public static KeyStroke getAccelerator(String action_command) {
		if (action_command == null) {
			return null;
		}
		String str = getKeystrokesNode().get(action_command, "");
		KeyStroke ks = KeyStroke.getKeyStroke(str);

		keystroke_node_names.add(action_command);

		if (ks == null) {
			if ("".equals(str)) {
				//System.out.println("No accelerator set for '"+ action_command +"'");
			} else {
				System.out.println("Bad format accelerator set for '" + action_command + "':");
				System.out.println("  invalid '" + str + "'");
			}
			// put a blank value in the keystroke so that the user will be able to
			// see which preferences are settable
			// (actually, this is no longer necessary now that the keystroke_node_names
			// Set is being used to keep track of these.)
			getKeystrokesNode().put(action_command, "");
		}

		return ks;
	}

	/**
	 * Returns the location of the application data directory.
	 * The String will always end with "/".
	 */
	public static String getAppDataDirectory() {
		return CommonUtils.getInstance().getAppDataDirectory();
	}

	/**
	 * Stores a color preference, encoded as a String.
	 *
	 * @param node
	 * @param key
	 * @param c
	 */
	public static void putColor(Preferences node, String key, Color c) {
		node.put(key, "0x" + getColorString(c));
	}

	public static String getColorString(Color c) {
		int i = c.getRGB() & 0xFFFFFF;
		String s = Integer.toHexString(i).toUpperCase();
		while (s.length() < 6) {
			s = "0" + s;
		}
		return s;
	}

	/**
	 * Retrieves a color preference
	 * that was stored with {@link #putColor(Preferences, String, Color)}.
	 */
	public static Color getColor(Preferences node, String key, Color default_color) {
		Color result = default_color;
		String value = node.get(key, "unknown");
		if (!value.equals("unknown")) {
			try {
				result = Color.decode(value);
			} catch (Exception e) {
				System.out.println("Couldn't decode color preference for '" + key + "' from '" + value + "'");
			}
		}
		return result;
	}

	/**
	 * Creates a JCheckBox associated with a boolean preference.
	 * Will initialize itself with the value of the given
	 * preference and will update itself, via a PreferenceChangeListener,
	 * if the preference value changes.
	 */
	public static JCheckBox createCheckBox(String title, final Preferences node,
			final String pref_name, boolean default_val) {
		final JCheckBox check_box = new JCheckBox(title);
		check_box.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				node.putBoolean(pref_name, check_box.isSelected());
			}
		});
		check_box.setSelected(node.getBoolean(pref_name, default_val));
		node.addPreferenceChangeListener(new PreferenceChangeListener() {

			public void preferenceChange(PreferenceChangeEvent evt) {
				if (evt.getNode().equals(node) && evt.getKey().equals(pref_name)) {
					check_box.setSelected(Boolean.valueOf(evt.getNewValue()).booleanValue());
				}
			}
		});
		return check_box;
	}

	/**
	 * Creates a JComboBox associated with a String preference.
	 * Will initialize itself with the value of the given
	 * preference and will update itself, via a PreferenceChangeListener,
	 * if the preference value changes.
	 */
	public static JComboBox createComboBox(final Preferences node,
			final String pref_name, String[] options, String default_value) {

		final String[] interned_options = new String[options.length];
		for (int i = 0; i < options.length; i++) {
			interned_options[i] = options[i].intern();
		}
		default_value.intern();

		final JComboBox combo_box = new JComboBox(interned_options);

		// Note that no check is made that the given default_value is
		// actually one of the given options.  The combo_box will ignore
		// an attempt to set itself to a value that isn't in its option list.
		String current_stored_value = node.get(pref_name, default_value).intern();

		combo_box.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				String selection = (String) combo_box.getSelectedItem();
				if (selection != null) { // selection == null is probably impossible
					node.put(pref_name, selection);
				}
			}
		});

		combo_box.setSelectedItem(current_stored_value);
		node.addPreferenceChangeListener(new PreferenceChangeListener() {

			public void preferenceChange(PreferenceChangeEvent evt) {
				if (evt.getNode().equals(node) && evt.getKey().equals(pref_name)) {
					if (!combo_box.getSelectedItem().equals(evt.getNewValue()) && evt.getNewValue() != null) {
						// Note: checking that selection differs from new value prevents infinite loop.
						combo_box.setSelectedItem((evt.getNewValue()).intern());
					}
				}
			}
		});
		return combo_box;
	}

	private static String shortNodeName(String s, boolean remove_slash) {
		String short_s;
		if (s.length() >= Preferences.MAX_NAME_LENGTH) {
			short_s = UrlToFileName.toMd5(s);
		} else {
			short_s = s;
		}
		if (remove_slash) {
			short_s = short_s.replaceAll("/", SLASH_STANDIN);
		}
		return short_s;
	}

	/**
	 * Create a subnode, making sure to shorten the name if necessary.
	 */
	public static Preferences getSubnode(Preferences parent, String name) {
		return getSubnode(parent, name, false);
	}

	/**
	 * Create a subnode, making sure to shorten the name if necessary.
	 */
	public static Preferences getSubnode(Preferences parent, String name, boolean remove_slash) {
		String short_name = shortNodeName(name, remove_slash);
		return parent.node(short_name);
	}

	public static JFrame createFrame(String name, JPanel panel) {
		final JFrame frame;

		if (name.length() > 70) {
			throw new IllegalArgumentException("Title of the frame must be less than 70 chars.");
		}

		// If not already open in a new window, make a new window
		frame = new JFrame(name);
		frame.setName(name);

		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		panel.setVisible(true);
		frame.pack(); // pack() to set frame to its preferred size

		Rectangle pos = PreferenceUtils.retrieveWindowLocation(frame.getTitle(), frame.getBounds());
		if (pos != null) {
			PreferenceUtils.setWindowSize(frame, pos);
		}

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent evt) {
				// save the current size into the preferences, so the window
				// will re-open with this size next time
				PreferenceUtils.saveWindowLocation(frame, frame.getTitle());
			}
		});

		// window already exists, but may not be visible
		return frame;
	}

	public static void setPrefsMode(String mode) {
		prefs_mode = mode;
	}
}
