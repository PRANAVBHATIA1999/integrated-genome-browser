package com.affymetrix.igb;

import com.affymetrix.igb.prefs.IPlugin;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.util.ThreadUtils;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.StatusBar;
import java.awt.Image;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public abstract class Application {

	public final static boolean DEBUG_EVENTS = false;
	protected final StatusBar status_bar;
	private final Set<String> progressStringList = new LinkedHashSet<String>(); // list of progress bar messages.
	static Application singleton = null;
	private final Map<Class, IPlugin> plugin_hash = new HashMap<Class, IPlugin>();

	public Application() {
		singleton = this;
		status_bar = new StatusBar();
	}

	public void setPluginInstance(Class c, IPlugin plugin) {
		plugin_hash.put(c, plugin);
		plugin.putPluginProperty(IPlugin.TEXT_KEY_APP, this);
		plugin.putPluginProperty(IPlugin.TEXT_KEY_SEQ_MAP_VIEW, this.getMapView());
	}

	public IPlugin getPluginInstance(Class c) {
		return plugin_hash.get(c);
	}

	public static Application getSingleton() {
		return singleton;
	}

	abstract public Image getIcon();

	abstract public JFrame getFrame();

	abstract public SeqMapView getMapView();

	abstract public String getApplicationName();

	abstract public String getVersion();

	/** Allows you to get arbitrary parameter strings, possibly from a ResourceBundle. */
	abstract public String getResourceString(String key);

	public static boolean isSequenceAccessible() {
		return true;
	}

	public final void addNotLockedUpMsg(final String s) {
		ThreadUtils.runOnEventQueue(new Runnable() {

			public void run() {
				progressStringList.add(s);
				status_bar.setCancelPopup(progressStringList);
				if (status_bar.getStatus().trim().length() == 0) {
					setNotLockedUpStatus(s, true);
				}
			}
		});
	}

	public final void removeNotLockedUpMsg(final String s) {
		ThreadUtils.runOnEventQueue(new Runnable() {

			public void run() {
				if (!progressStringList.remove(s)) {
					Logger.getLogger(Application.class.getName()).log(Level.FINE,
						"Didn't find progress message: " + s);
				}
				status_bar.setCancelPopup(progressStringList);
				if (status_bar.getStatus().equals(s) || status_bar.getStatus().trim().length() == 0) {
					// Time to change status message.
					if (progressStringList.isEmpty()) {
						setNotLockedUpStatus(null, false);
					} else {
						setNotLockedUpStatus(progressStringList.iterator().next(), true);
					}
				}
			}
		});
	}

	/**
	 * Set the status, and show a little progress bar so that the app doesn't look locked up.
	 * @param s
	 */
	private final void setNotLockedUpStatus(String s, boolean visible) {
		status_bar.setStatus(s);
		status_bar.progressBar.setVisible(visible);
	}
	
	/** Sets the text in the status bar.
	 *  Will also echo a copy of the string to System.out.
	 *  It is safe to call this method even if the status bar is not being displayed.
	 */
	public final void setStatus(String s) {
		setStatus(s, true);
	}

	/** Sets the text in the status bar.
	 *  Will optionally echo a copy of the string to System.out.
	 *  It is safe to call this method even if the status bar is not being displayed.
	 *  @param echo  Whether to echo a copy to System.out.
	 */
	public final void setStatus(final String s, final boolean echo) {
		ThreadUtils.runOnEventQueue(new Runnable() {

			public void run() {
				status_bar.setStatus(s);
				if (echo && s != null && !s.isEmpty()) {
					System.out.println(s);
				}
			}
		});
	}

	/** Opens a JOptionPane.ERROR_MESSAGE panel with the application frame
	 *  as its parent.
	 */
	public static void errorPanel(String title, String message) {
		Application app = getSingleton();
		JFrame frame = (app == null) ? null : app.getFrame();
		ErrorHandler.errorPanel(frame, title, message, null);
	}

	/** Opens a JOptionPane.ERROR_MESSAGE panel with the IGB
	 *  panel as its parent, and the title "ERROR".
	 */
	public static void errorPanel(String message) {
		Application app = getSingleton();
		JFrame frame = (app == null) ? null : app.getFrame();
		ErrorHandler.errorPanel(frame, "ERROR", message, null);
	}

	/** Opens a JOptionPane.ERROR_MESSAGE panel with the IGB
	 *  panel as its parent, and the title "ERROR".
	 *  @param e an exception (or error), if any.  null is ok. If not null,
	 *  the exception text will be appended to the message and
	 *  a stack trace might be printed on standard error.
	 */
	public static void errorPanel(String message, Throwable e) {
		Application app = getSingleton();
		JFrame frame = (app == null) ? null : app.getFrame();
		ErrorHandler.errorPanel(frame, "ERROR", message, e);
	}

	/** Shows a panel asking for the user to confirm something.
	 *  @return true if the user confirms, else false.
	 */
	public static boolean confirmPanel(String message) {
		Application app = getSingleton();
		JFrame frame = (app == null) ? null : app.getFrame();
		return (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
				frame, message, "Confirm", JOptionPane.YES_NO_OPTION));
	}
}
