/**
 * Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.*;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.StateProvider;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.util.*;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.glyph.FillRectGlyph;
import com.affymetrix.genoviz.swing.MenuUtil;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenu;
import static com.affymetrix.igb.IGBConstants.*;

import com.affymetrix.igb.general.Persistence;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.osgi.service.IStopRoutine;
import com.affymetrix.igb.prefs.WebLink;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.IGBStateProvider;
import com.affymetrix.igb.tiers.TrackStyle;
import com.affymetrix.igb.util.IGBAuthenticator;
import com.affymetrix.igb.util.IGBTrustManager;
import com.affymetrix.igb.util.MainMenuUtil;
import com.affymetrix.igb.util.StatusBarOutput;
import com.affymetrix.igb.view.AltSpliceView;
import com.affymetrix.igb.view.IGBToolBar;
import com.affymetrix.igb.view.SeqGroupViewGUI;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.load.GeneralLoadViewGUI;
import com.affymetrix.igb.view.welcome.MainWorkspaceManager;
import com.affymetrix.igb.window.service.IMenuCreator;
import com.affymetrix.igb.window.service.IWindowService;
import com.jidesoft.plaf.LookAndFeelFactory;
import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.util.Map.Entry;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;


/**
 * Main class for the Integrated Genome Browser (IGB, pronounced ig-bee).
 *
 * @version $Id: IGB.java 11452 2012-05-07 22:32:36Z lfrohman $
 */
public final class IGB extends Application
		implements GroupSelectionListener, SeqSelectionListener {

	private static final String GUARANTEED_URL = "http://www.google.com"; // if URL goes away, the program will always give a "not connected" error
	private static final String COUNTER_URL = "http://www.igbquickload.org/igb/counter";
	public static final String NODE_PLUGINS = "plugins";
	private JFrame frm;
	private JMenuBar mbar;
	private IGBToolBar tool_bar;
	private SeqMapView map_view;
	private AnnotatedSeqGroup prev_selected_group = null;
	private BioSeq prev_selected_seq = null;
	public static volatile String commandLineBatchFileStr = null;	// Used to run batch file actions if passed via command-line
	private IWindowService windowService;
	private HashSet<IStopRoutine> stopRoutines;
	private SwingWorker<Void, Void> scriptWorker = null; // thread for running scripts - only one script can run at a time
	private static final Logger ourLogger = Logger.getLogger(IGB.class.getPackage().getName());

	public IGB() {
		super();
		stopRoutines = new HashSet<IStopRoutine>();
	}

	@Override
	public SeqMapView getMapView() {
		return map_view;
	}

	@Override
	public JFrame getFrame() {
		return frm;
	}

	private static void loadSynonyms(String file, SynonymLookup lookup) {
		InputStream istr = null;
		try {
			istr = IGB.class.getResourceAsStream(file);
			lookup.loadSynonyms(IGB.class.getResourceAsStream(file), true);
		} catch (IOException ex) {
			ourLogger.log(Level.FINE,
					"Problem loading default synonyms file " + file, ex);
		} finally {
			GeneralUtils.safeClose(istr);
		}
	}

	//TODO: Remove this redundant call to set LAF. For now it fixes bug introduced by OSGi.
	public static void setLaf() {

		// Turn on anti-aliased fonts. (Ignored prior to JDK1.5)
		System.setProperty("swing.aatext", "true");

		// Letting the look-and-feel determine the window decorations would
		// allow exporting the whole frame, including decorations, to an eps file.
		// But it also may take away some things, like resizing buttons, that the
		// user is used to in their operating system, so leave as false.
		JFrame.setDefaultLookAndFeelDecorated(false);

		// if this is != null, then the user-requested l-and-f has already been applied
		if (System.getProperty("swing.defaultlaf") == null) {
			String os = System.getProperty("os.name");
			if (os != null && os.toLowerCase().contains("windows")) {
				try {
					Class<?> cl = null;
					try {
						for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
							if ("Nimbus".equals(info.getName())) {
								cl = Class.forName(info.getClassName());
								break;
							}
						}
					} catch (Exception e) {
						// If Nimbus is not available, you can set the GUI to another look and feel.
					}
					if(cl == null){
						// If this is Windows and Nimbus is not installed, then use the Windows look and feel.
						cl = Class.forName("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					}
					LookAndFeel look_and_feel = (LookAndFeel) cl.newInstance();

					if (look_and_feel.isSupportedLookAndFeel()) {
						LookAndFeelFactory.installJideExtension();
						// Is there a better way to do it? HV 03/02/12
						for (Entry<Object, Object> obj : look_and_feel.getDefaults().entrySet()) {
							UIManager.getDefaults().put(obj.getKey(), obj.getValue());
						}
						UIManager.setLookAndFeel(look_and_feel);
					}
				} catch (Exception ulfe) {
					// Windows look and feel is only supported on Windows, and only in
					// some version of the jre.  That is perfectly ok.
				}
			}
		}
	}

	public void init(String[] args) {
		setLaf();
	
		// Set up a custom trust manager so that user is prompted
		// to accept or reject untrusted (self-signed) certificates
		// when connecting to server over HTTPS
		IGBTrustManager.installTrustManager();

		// Configure HTTP User agent
		System.setProperty("http.agent", USER_AGENT);

		// Initialize the ConsoleView right off, so that ALL output will
		// be captured there.
		ConsoleView.init(APP_NAME);
		printDetails(args);

		// Initialize statusbar output logger.
		StatusBarOutput.initStatusBarOutput();
		
		loadSynonyms("/synonyms.txt", SynonymLookup.getDefaultLookup());
		loadSynonyms("/chromosomes.txt", SynonymLookup.getChromosomeLookup());

		if ("Mac OS X".equals(System.getProperty("os.name"))) {
			MacIntegration mi = MacIntegration.getInstance();
			if (this.getIcon() != null) {
				mi.setDockIconImage(this.getIcon());
			}
		}

		frm = new JFrame(APP_NAME + " " + APP_VERSION);

		// when HTTP authentication is needed, getPasswordAuthentication will
		//    be called on the authenticator set as the default
		Authenticator.setDefault(new IGBAuthenticator(frm));
		
		StateProvider stateProvider = new IGBStateProvider();
		DefaultStateProvider.setGlobalStateProvider(stateProvider);

		frm.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		Image icon = getIcon();
		if (icon != null) {
			frm.setIconImage(icon);
		}

		GenometryModel gmodel = GenometryModel.getGenometryModel();
		gmodel.addGroupSelectionListener(this);
		gmodel.addSeqSelectionListener(this);
		// WARNING!!  IGB _MUST_ be added as group and seq selection listener to model _BEFORE_ map_view is,
		//    otherwise assumptions for persisting group / seq / span prefs are not valid!

		MenuUtil.setAccelerators(
			new AbstractMap<String, KeyStroke>() {

				@Override
				public Set<java.util.Map.Entry<String, KeyStroke>> entrySet() {
					return null;
				}

				@Override
				public KeyStroke get(Object action_command) {
					return PreferenceUtils.getAccelerator((String) action_command);
				}
			}
		);
		map_view = new SeqMapView(true, "SeqMapView");
		gmodel.addSeqSelectionListener(map_view);
		gmodel.addGroupSelectionListener(map_view);
		gmodel.addSymSelectionListener(map_view);

		mbar = new JMenuBar();
		frm.setJMenuBar(mbar);
		MainMenuUtil.getInstance().loadMenu(mbar, "IGB");

		Rectangle frame_bounds = PreferenceUtils.retrieveWindowLocation("main window",
				new Rectangle(0, 0, 1100, 700)); // 1.58 ratio -- near golden ratio and 1920/1200, which is native ratio for large widescreen LCDs.
		PreferenceUtils.setWindowSize(frm, frame_bounds);

		// Show the frame before loading the plugins.  Thus any error panel
		// that is created by an exception during plugin set-up will appear
		// on top of the main frame, not hidden by it.

		frm.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent evt) {
				JFrame frame = (JFrame) evt.getComponent();
				String message = "Do you really want to exit?";

				if (confirmPanel(message, PreferenceUtils.getTopNode(), PreferenceUtils.ASK_BEFORE_EXITING, PreferenceUtils.default_ask_before_exiting)) {
					TrackStyle.autoSaveUserStylesheet();
					Persistence.saveCurrentView(map_view);
					defaultCloseOperations();
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				} else {
					frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				}
			}
		});

		WebLink.autoLoad();

		GeneralLoadViewGUI.init(IGBServiceImpl.getInstance());
		MainWorkspaceManager.getWorkspaceManager().setSeqMapViewObj(map_view);
		SeqGroupViewGUI.init(IGBServiceImpl.getInstance());
		checkInternetConnection();
		notifyCounter();
	}
	
		private void printDetails(String[] args) {
		System.out.println("Starting \"" + APP_NAME + " " + APP_VERSION_FULL + "\"");
		System.out.println("UserAgent: " + USER_AGENT);
		System.out.println("Java version: " + System.getProperty("java.version") + " from " + System.getProperty("java.vendor"));
		Runtime runtime = Runtime.getRuntime();
		System.out.println("Locale: " + Locale.getDefault());
		System.out.println("System memory: " + runtime.maxMemory() / 1024);
		if (args != null) {
			System.out.print("arguments: ");
			for (String arg : args) {
				System.out.print(" " + arg);
			}
			System.out.println();
		}

		System.out.println();
	}

	private void notifyCounter(){
		LocalUrlCacher.isValidURL(COUNTER_URL);
	}
	
	private void checkInternetConnection() {
		boolean connected = LocalUrlCacher.isValidURL(GUARANTEED_URL);
		if (!connected) {
			ErrorHandler.errorPanel(IGBConstants.BUNDLE.getString("internetError"));
		}
	}

	public void addStopRoutine(IStopRoutine routine) {
		stopRoutines.add(routine);
	}

	public void defaultCloseOperations() {
		for (IStopRoutine stopRoutine : stopRoutines) {
			stopRoutine.stop();
		}
	}

	public IGBTabPanel[] setWindowService(final IWindowService windowService) {
		this.windowService = windowService;
		windowService.setMainFrame(frm);

		windowService.setSeqMapView(MainWorkspaceManager.getWorkspaceManager());

		windowService.setStatusBar(status_bar);
		if (tool_bar == null) {
			tool_bar = new IGBToolBar();
		}
		windowService.setToolBar(tool_bar);
		windowService.setViewMenu(getMenu("tabs"));
		windowService.setMenuCreator(
				new IMenuCreator() {

					@Override
					public JMenuBar createMenu(String id) {
						JMenuBar menubar = new JMenuBar();
						MainMenuUtil.getInstance().loadMenu(menubar, id);
						return menubar;
					}
				});
		return new IGBTabPanel[]{GeneralLoadViewGUI.getLoadView(), SeqGroupViewGUI.getInstance(), AltSpliceView.getSingleton()};
	}

	public JRPMenu addTopMenu(String id, String text) {
		return MenuUtil.getRPMenu(mbar, id, text);
	}
	
	public int addToolbarAction(GenericAction genericAction) {
		if (tool_bar == null) {
			tool_bar = new IGBToolBar();
		}
		addToolbarAction(genericAction, tool_bar.getItemCount());
		
		return tool_bar.getItemCount();
	}

	public void addToolbarAction(GenericAction genericAction, int index){
		if (tool_bar == null) {
			tool_bar = new IGBToolBar();
		}		
		addAction(genericAction);

		tool_bar.addToolbarAction(genericAction, index);
	}
	
	public void removeToolbarAction(GenericAction action) {
		if(tool_bar == null)
			return;
		tool_bar.removeToolbarAction(action);
	}
	
	void saveToolBar() {
		if(tool_bar == null)
			return;
		
		tool_bar.saveToolBar();
	}
	
	/**
	 * Returns the icon stored in the jar file. It is expected to be at
	 * com.affymetrix.igb.igb.gif.
	 *
	 * @return null if the image file is not found or can't be opened.
	 */
	@Override
	public Image getIcon() {
		ImageIcon imageIcon = CommonUtils.getInstance().getIcon("images/igb.gif");
		if (imageIcon != null) {
			return imageIcon.getImage();
		}
		return null;
	}

	@Override
	public void groupSelectionChanged(GroupSelectionEvent evt) {
		AnnotatedSeqGroup selected_group = evt.getSelectedGroup();
		if ((prev_selected_group != selected_group) && (prev_selected_seq != null)) {
			Persistence.saveSeqSelection(prev_selected_seq);
			Persistence.saveSeqVisibleSpan(map_view);
		}
		prev_selected_group = selected_group;
	}

	@Override
	public void seqSelectionChanged(SeqSelectionEvent evt) {
		BioSeq selected_seq = evt.getSelectedSeq();
		if ((prev_selected_seq != null) && (prev_selected_seq != selected_seq)) {
			Persistence.saveSeqVisibleSpan(map_view);
		}
		prev_selected_seq = selected_seq;
		getFrame().setTitle(getTitleBar(selected_seq));
	}

	public int searchForRegexInResidues(
			boolean forward, Pattern regex, String residues, int residue_offset, GlyphI axis_tier, List<GlyphI> glyphs, Color hitColor) {
		int hit_count = 0;
		Matcher matcher = regex.matcher(residues);
		while (matcher.find() && !Thread.currentThread().isInterrupted()) {
			int start = residue_offset + (forward ? matcher.start(0) : -matcher.end(0));
			int end = residue_offset + (forward ? matcher.end(0) : -matcher.start(0));
			//int end = matcher.end(0) + residue_offset;
			Map<String, String> props = new HashMap<String, String>();
			props.put("direction", forward ? "forward" : "reverse");
			props.put("match", matcher.group(0));
			props.put("pattern", regex.pattern());

			GlyphI gl = new FillRectGlyph() {

				@Override
				public void moveAbsolute(double x, double y) {
				}

				;
				@Override
				public void moveRelative(double diffx, double diffy) {
				}
			;
			};
			gl.setInfo(props);
			gl.setColor(hitColor);
			double pos = forward ? 10 : 15;
			gl.setCoords(start, pos, end - start, 10);
			axis_tier.addChild(gl);
			glyphs.add(gl);
			hit_count++;
		}
		return hit_count;
	}

	public IWindowService getWindowService() {
		return windowService;
	}

	public JRPMenu getMenu(String menuId) {
		String id = "IGB_main_" + menuId + "Menu";
		int num_menus = mbar.getMenuCount();
		for (int i = 0; i < num_menus; i++) {
			JRPMenu menu_i = (JRPMenu) mbar.getMenu(i);
			if (id.equals(menu_i.getId())) {
				return menu_i;
			}
		}
		return null;
	}

	public Set<IGBTabPanel> getTabs() {
		return windowService.getPlugins();
	}

	@Override
	public void setSelField(Map<String, Object> properties, String message){
		tool_bar.setSelectionText(properties, message);
	}
	
	/**
	 * Get a named view.
	 */
	public IGBTabPanel getView(String viewName) {
		for (IGBTabPanel plugin : windowService.getPlugins()) {
			if (plugin.getClass().getName().equals(viewName)) {
				return plugin;
			}
		}
		String message = getClass().getName() + ".getView() failed for " + viewName;
		ourLogger.severe(message);
		return null;
	}

	/**
	 * Get a named view.
	 * This differs from {@link #getView(String)}
	 * in that it wants the display name instead of the full name.
	 * This is easier for scripting.
	 * @param viewName the display name of a tab
	 *        instead of a full package and class name.
	 */
	public IGBTabPanel getViewByDisplayName(String viewName) {
		for (IGBTabPanel plugin : windowService.getPlugins()) {
			if (plugin.getDisplayName().equals(viewName)) {
				return plugin;
			}
		}
		String message = getClass().getName() + ".getView() failed for \"" + viewName + "\"";
		try {
			ourLogger.severe(message);
		}
		catch (Exception x) {
			System.err.println(message);
		}
		return null;
	}

	public SwingWorker<Void, Void> getScriptWorker() {
		return scriptWorker;
	}

	public void setScriptWorker(SwingWorker<Void, Void> scriptWorker) {
		this.scriptWorker = scriptWorker;
	}

	/**
	 * Put the action's accelerator key (if there is one)
	 * in the panel's input and action maps.
	 * This makes the action available via shortcut,
	 * even if it is "hidden" in a pop up menu.
	 * @param theAction to which the shortcut points.
	 */
	public void addAction(GenericAction theAction) {
		JPanel panel = (JPanel) this.frm.getContentPane();
		Object o = theAction.getValue(Action.ACCELERATOR_KEY);
		if (null != o && o instanceof KeyStroke) {
			KeyStroke ks = (KeyStroke) o;
			InputMap im = panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
			ActionMap am = panel.getActionMap();
//			GenericActionHolder h = GenericActionHolder.getInstance();
			String actionIdentifier = theAction.getId();
			im.put(ks, actionIdentifier);
			am.put(actionIdentifier, theAction);
		}
	}

	private static String getTitleBar(BioSeq seq) {
		StringBuilder title = new StringBuilder(128);
		if (seq != null) {
			if (title.length() > 0) {
				title.append(" - ");
			}
			String seqid = seq.getID().trim();
			Pattern pattern = Pattern.compile("chr([0-9XYM]*)");
			if (pattern.matcher(seqid).matches()) {
				seqid = seqid.replace("chr", "Chromosome ");
			}

			title.append(seqid);
			String version_info = getVersionInfo(seq);
			if (version_info != null) {
				title.append("  (").append(version_info).append(')');
			}
		}
		if (title.length() > 0) {
			title.append(" - ");
		}
		title.append(IGBConstants.APP_NAME).append(" ").append(IGBConstants.APP_VERSION);
		return title.toString();
	}
	
	private static String getVersionInfo(BioSeq seq) {
		if (seq == null) {
			return null;
		}
		String version_info = null;
		if (seq.getSeqGroup() != null) {
			AnnotatedSeqGroup group = seq.getSeqGroup();
			if (group.getDescription() != null) {
				version_info = group.getDescription();
			} else {
				version_info = group.getID();
			}
		}
		if (version_info == null) {
			version_info = seq.getVersion();
		}
		if ("hg17".equals(version_info)) {
			version_info = "hg17 = NCBI35";
		} else if ("hg18".equals(version_info)) {
			version_info = "hg18 = NCBI36";
		}
		return version_info;
	}
}
