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

import com.jidesoft.plaf.LookAndFeelFactory;
import com.affymetrix.common.CommonUtils;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.GroupSelectionEvent;
import com.affymetrix.genometryImpl.event.GroupSelectionListener;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.StateProvider;
import com.affymetrix.genometryImpl.thread.CThreadWorker;
import com.affymetrix.genometryImpl.util.*;

import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.glyph.FillRectGlyph;
import com.affymetrix.genoviz.swing.MenuUtil;
import com.affymetrix.genoviz.swing.recordplayback.JRPButton;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenu;

import com.affymetrix.igb.general.Persistence;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.osgi.service.IStopRoutine;
import com.affymetrix.igb.prefs.WebLink;
import com.affymetrix.igb.shared.TransformTierGlyph;
import com.affymetrix.igb.tiers.IGBStateProvider;
import com.affymetrix.igb.tiers.TrackStyle;
import com.affymetrix.igb.util.IGBAuthenticator;
import com.affymetrix.igb.util.IGBTrustManager;
import com.affymetrix.igb.util.MainMenuUtil;
import com.affymetrix.igb.util.StatusBarOutput;
import com.affymetrix.igb.view.AltSpliceView;
import com.affymetrix.igb.view.SeqGroupViewGUI;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.load.GeneralLoadViewGUI;
import com.affymetrix.igb.view.welcome.MainWorkspaceManager;
import com.affymetrix.igb.window.service.IMenuCreator;
import com.affymetrix.igb.window.service.IWindowService;
import static com.affymetrix.igb.IGBConstants.*;


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
	private JToolBar tool_bar;
	private SeqMapView map_view;
	private AnnotatedSeqGroup prev_selected_group = null;
	private BioSeq prev_selected_seq = null;
	public static volatile String commandLineBatchFileStr = null;	// Used to run batch file actions if passed via command-line
	private IWindowService windowService;
	private HashSet<IStopRoutine> stopRoutines;
	private CThreadWorker<Void, Void> scriptWorker = null; // thread for running scripts - only one script can run at a time

	public IGB() {
		super();
		stopRoutines = new HashSet<IStopRoutine>();
	}

	public SeqMapView getMapView() {
		return map_view;
	}

	public JFrame getFrame() {
		return frm;
	}

	private static void loadSynonyms(String file, SynonymLookup lookup) {
		InputStream istr = null;
		try {
			istr = IGB.class.getResourceAsStream(file);
			lookup.loadSynonyms(IGB.class.getResourceAsStream(file), true);
		} catch (IOException ex) {
			Logger.getLogger(IGB.class.getName()).log(Level.FINE, "Problem loading default synonyms file " + file, ex);
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
					// It this is Windows, then use the Windows look and feel.
					Class<?> cl = Class.forName("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
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

	public void init() {
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

		// Initialize statusbar output logger.
		StatusBarOutput.initStatuBarOutput();
		
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
			tool_bar = new JToolBar();
		}
		windowService.setToolBar(tool_bar);
		windowService.setViewMenu(getMenu("view"));
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

	public void addToolbarButton(JRPButton button) {
		if (tool_bar == null) {
			tool_bar = new JToolBar();
		}
		tool_bar.add(button);
	}

	/**
	 * Returns the icon stored in the jar file. It is expected to be at
	 * com.affymetrix.igb.igb.gif.
	 *
	 * @return null if the image file is not found or can't be opened.
	 */
	public Image getIcon() {
		ImageIcon imageIcon = CommonUtils.getInstance().getIcon("images/igb.gif");
		if (imageIcon != null) {
			return imageIcon.getImage();
		}
		return null;
	}

	public void groupSelectionChanged(GroupSelectionEvent evt) {
		AnnotatedSeqGroup selected_group = evt.getSelectedGroup();
		if ((prev_selected_group != selected_group) && (prev_selected_seq != null)) {
			Persistence.saveSeqSelection(prev_selected_seq);
			Persistence.saveSeqVisibleSpan(map_view);
		}
		prev_selected_group = selected_group;
	}

	public void seqSelectionChanged(SeqSelectionEvent evt) {
		BioSeq selected_seq = evt.getSelectedSeq();
		if ((prev_selected_seq != null) && (prev_selected_seq != selected_seq)) {
			Persistence.saveSeqVisibleSpan(map_view);
		}
		prev_selected_seq = selected_seq;
	}

	public int searchForRegexInResidues(
			boolean forward, Pattern regex, String residues, int residue_offset, TransformTierGlyph axis_tier, List<GlyphI> glyphs, Color hitColor) {
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

	public IGBTabPanel getView(String viewName) {
		for (IGBTabPanel plugin : windowService.getPlugins()) {
			if (plugin.getClass().getName().equals(viewName)) {
				return plugin;
			}
		}
		Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, getClass().getName() + ".getView() failed for " + viewName);
		return null;
	}

	//Easier for scripting if we don't require full name.
	//This method only take display name of a tab instead of full package+classname
	public IGBTabPanel getViewByDisplayName(String viewName) {
		for (IGBTabPanel plugin : windowService.getPlugins()) {
			if (plugin.getDisplayName().equals(viewName)) {
				return plugin;
			}
		}
		Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, getClass().getName() + ".getView() failed for " + viewName);
		return null;
	}

	public CThreadWorker<Void, Void> getScriptWorker() {
		return scriptWorker;
	}

	public void setScriptWorker(CThreadWorker<Void, Void> scriptWorker) {
		this.scriptWorker = scriptWorker;
	}
}
