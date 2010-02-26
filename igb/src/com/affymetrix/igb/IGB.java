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
package com.affymetrix.igb;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.genoviz.util.ComponentPagePrinter;
import com.affymetrix.genoviz.swing.DisplayUtils;

import com.affymetrix.genometryImpl.SeqSymmetry;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.event.GroupSelectionEvent;
import com.affymetrix.genometryImpl.event.GroupSelectionListener;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.StateProvider;

import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.SynonymLookup;
import com.affymetrix.igb.bookmarks.Bookmark;
import com.affymetrix.igb.bookmarks.BookmarkController;
import com.affymetrix.igb.menuitem.*;
import com.affymetrix.igb.view.*;
import com.affymetrix.igb.parsers.XmlPrefsParser;
import com.affymetrix.igb.prefs.*;
import com.affymetrix.igb.bookmarks.SimpleBookmarkServer;
import com.affymetrix.igb.general.Persistence;
import com.affymetrix.igb.glyph.EdgeMatchAdjuster;
import com.affymetrix.igb.tiers.AffyLabelledTierMap;
import com.affymetrix.igb.tiers.AffyTieredMap.ActionToggler;
import com.affymetrix.igb.util.ComponentWriter;
import com.affymetrix.igb.util.LocalUrlCacher;
import com.affymetrix.igb.tiers.IGBStateProvider;
import com.affymetrix.igb.util.IGBAuthenticator;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.view.external.ExternalViewer;
import java.text.MessageFormat;

import static com.affymetrix.igb.IGBConstants.BUNDLE;
import static com.affymetrix.igb.IGBConstants.APP_NAME;
import static com.affymetrix.igb.IGBConstants.APP_VERSION;
import static com.affymetrix.igb.IGBConstants.APP_VERSION_FULL;
import static com.affymetrix.igb.IGBConstants.USER_AGENT;

/**
 *  Main class for the Integrated Genome Browser (IGB, pronounced ig-bee).
 *
 * @version $Id$
 */
public final class IGB extends Application
				implements ActionListener, ContextualPopupListener, GroupSelectionListener, SeqSelectionListener {

	static IGB singleton_igb;
	private static final String TABBED_PANES_TITLE = "Tabbed Panes";
	private static final String BUG_URL = "http://sourceforge.net/tracker/?group_id=129420&atid=714744";
	private static final String MENU_ITEM_HAS_DIALOG = BUNDLE.getString("menuItemHasDialog");
	private static final GenometryModel gmodel = GenometryModel.getGenometryModel();
	private static String[] main_args;
	private static final Map<Component, Frame> comp2window = new HashMap<Component, Frame>();
	private final Map<Component, PluginInfo> comp2plugin = new HashMap<Component, PluginInfo>();
	private final Map<Component, JCheckBoxMenuItem> comp2menu_item = new HashMap<Component, JCheckBoxMenuItem>();
	private final JMenu popup_windowsM = new JMenu(MessageFormat.format(MENU_ITEM_HAS_DIALOG, "Open in Window"));
	private SimpleBookmarkServer web_control = null;
	private JFrame frm;
	private JMenuBar mbar;
	private JMenu file_menu;
	private JMenu export_to_file_menu;
	private JMenu view_menu;
	private JMenu edit_menu;
	private JMenu bookmark_menu;
	private JMenu tools_menu;
	private JMenu help_menu;
	private JTabbedPane tab_pane;
	private JSplitPane splitpane;
	public BookMarkAction bmark_action; // needs to be public for the BookmarkManagerView plugin
	private LoadFileAction open_file_action;
	private JMenuItem gc_item;
	private JMenuItem memory_item;
	private JMenuItem about_item;
	private JMenuItem documentation_item;
	private JMenuItem bug_item;
	private JMenuItem console_item;
	private JMenuItem clear_item;
	private JMenuItem clear_graphs_item;
	private JMenuItem open_file_item;
	private JMenuItem print_item;
	private JMenuItem print_frame_item;
	private JMenuItem export_map_item;
	private JMenuItem export_labelled_map_item;
	private JMenuItem export_slice_item;
	private JMenuItem export_whole_frame;
	private JMenuItem preferences_item;
	private JMenuItem exit_item;
	private JMenuItem view_ucsc_item;
	private JMenuItem res2clip_item;
	private JMenuItem clamp_view_item;
	private JMenuItem unclamp_item;
	private JMenuItem rev_comp_item;
	private JCheckBoxMenuItem shrink_wrap_item;
	private JMenuItem adjust_edgematch_item;
	private JCheckBoxMenuItem toggle_hairline_label_item;
	private JCheckBoxMenuItem toggle_edge_matching_item;
	private JMenuItem autoscroll_item;
	private JMenuItem web_links_item;
	private JMenuItem move_tab_to_window_item;
	private JMenuItem move_tabbed_panel_to_window_item;
	private SeqMapView map_view;
	public DataLoadView data_load_view = null;
	private AltSpliceView slice_view = null;
	private final List<PluginInfo> plugins_info = new ArrayList<PluginInfo>(16);
	private final List<Object> plugins = new ArrayList<Object>(16);
	private FileTracker load_directory = FileTracker.DATA_DIR_TRACKER;
	private AnnotatedSeqGroup prev_selected_group = null;
	private BioSeq prev_selected_seq = null;
	
	private static final Logger APP = Logger.getLogger("app");

	/**
	 * Start the program.
	 */
	public static void main(String[] args) {
		try {

			// Configure HTTP User agent
			System.setProperty("http.agent", USER_AGENT);

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
							UIManager.setLookAndFeel(look_and_feel);
						}
					} catch (Exception ulfe) {
						// Windows look and feel is only supported on Windows, and only in
						// some version of the jre.  That is perfectly ok.
					}
				}
			}
			


			// Initialize the ConsoleView right off, so that ALL output will
			// be captured there.
			ConsoleView.init();

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

			main_args = args;

			String offline = get_arg("-offline", args);
			if (offline != null) {
				LocalUrlCacher.setOffLine("true".equals(offline));
			}

			singleton_igb = new IGB();

			PrefsLoader.loadIGBPrefs(main_args); // force loading of prefs

			singleton_igb.init();

			goToBookmark(args);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void goToBookmark(String[] args) {
		// If the command line contains a parameter "-href http://..." where
		// the URL is a valid IGB control bookmark, then go to that bookmark.
		final String url = get_arg("-href", args);
		if (url != null && url.length() > 0) {
			try {
				final Bookmark bm = new Bookmark(null, url);
				if (bm.isUnibrowControl()) {
					SwingUtilities.invokeLater(new Runnable() {

						public void run() {
							System.out.println("Loading bookmark: " + url);
							BookmarkController.viewBookmark(singleton_igb, bm);
						}
					});
				} else {
					System.out.println("ERROR: URL given with -href argument is not a valid bookmark: \n" + url);
				}
			} catch (MalformedURLException mue) {
				mue.printStackTrace(System.err);
			}
		}
	}

	public SeqMapView getMapView() {
		return map_view;
	}

	public JFrame getFrame() {
		return frm;
	}

	private void startControlServer() {
		// Use the Swing Thread to start a non-Swing thread
		// that will start the control server.
		// Thus the control server will be started only after current GUI stuff is finished,
		// but starting it won't cause the GUI to hang.

		Runnable r = new Runnable() {

			public void run() {
				web_control = new SimpleBookmarkServer(IGB.this);
			}
		};

		final Thread t = new Thread(r);

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				t.start();
			}
		});
	}

	/**
	 * Returns the value of the argument indicated by label.
	 * If arguments are
	 *   "-flag_2 -foo bar", then get_arg("foo", args)
	 * returns "bar", get_arg("flag_2") returns a non-null string,
	 * and get_arg("flag_5") returns null.
	 */
	public static String get_arg(String label, String[] args) {
		String to_return = null;
		boolean got_it = false;
		if (label != null && args != null) {
			for (String item : args) {
				if (got_it) {
					to_return = item;
					break;
				}
				if (item.equals(label)) {
					got_it = true;
				}
			}
		}
		if (got_it && to_return == null) {
			to_return = "true";
		}
		return to_return;
	}

	private static void loadSynonyms(String file) {
		try {
			SynonymLookup.getDefaultLookup().loadSynonyms(IGB.class.getResourceAsStream(file), true);
		} catch (IOException ex) {
			Logger.getLogger(IGB.class.getName()).log(Level.FINE, "Problem loading default synonyms file " + file, ex);
		}
	}

	private void init() {
		loadSynonyms("/synonyms.txt");
		loadSynonyms("/chromosomes.txt");

		if ("Mac OS X".equals(System.getProperty("os.name"))) {
			MacIntegration mi = MacIntegration.getInstance();
			if (this.getIcon() != null) {
				mi.setDockIconImage(this.getIcon());
			}
		}
		frm = new JFrame(APP_NAME + " " + APP_VERSION);
		RepaintManager rm = RepaintManager.currentManager(frm);

		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();

		// when HTTP authentication is needed, getPasswordAuthentication will
		//    be called on the authenticator set as the default
		Authenticator.setDefault(new IGBAuthenticator(frm));
		
		
		// force loading of prefs if hasn't happened yet
		// usually since IGB.main() is called first, prefs will have already been loaded
		//   via loadIGBPrefs() call in main().  But if for some reason an IGB instance
		//   is created without call to main(), will force loading of prefs here...
		PrefsLoader.loadIGBPrefs(main_args);

		StateProvider stateProvider = new IGBStateProvider();
		DefaultStateProvider.setGlobalStateProvider(stateProvider);


		frm.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		Image icon = getIcon();
		if (icon != null) {
			frm.setIconImage(icon);
		}

		mbar = MenuUtil.getMainMenuBar();
		frm.setJMenuBar(mbar);

		gmodel.addGroupSelectionListener(this);
		gmodel.addSeqSelectionListener(this);
		// WARNING!!  IGB _MUST_ be added as group and seq selection listener to model _BEFORE_ map_view is,
		//    otherwise assumptions for persisting group / seq / span prefs are not valid!

		map_view = SeqMapView.makeSeqMapView();
		map_view.setFrame(frm);
		gmodel.addSeqSelectionListener(map_view);
		gmodel.addGroupSelectionListener(map_view);
		gmodel.addSymSelectionListener(map_view);

		file_menu = MenuUtil.getMenu(BUNDLE.getString("fileMenu"));
		file_menu.setMnemonic(BUNDLE.getString("fileMenuMnemonic").charAt(0));

		edit_menu = MenuUtil.getMenu(BUNDLE.getString("editMenu"));
		edit_menu.setMnemonic(BUNDLE.getString("editMenuMnemonic").charAt(0));

		view_menu = MenuUtil.getMenu(BUNDLE.getString("viewMenu"));
		view_menu.setMnemonic(BUNDLE.getString("viewMenuMnemonic").charAt(0));

		bookmark_menu = MenuUtil.getMenu(BUNDLE.getString("bookmarksMenu"));
		bookmark_menu.setMnemonic(BUNDLE.getString("bookmarksMenuMnemonic").charAt(0));

		tools_menu = MenuUtil.getMenu(BUNDLE.getString("toolsMenu"));
		tools_menu.setMnemonic(BUNDLE.getString("toolsMenuMnemonic").charAt(0));

		help_menu = MenuUtil.getMenu(BUNDLE.getString("helpMenu"));
		help_menu.setMnemonic(BUNDLE.getString("helpMenuMnemonic").charAt(0));

		bmark_action = new BookMarkAction(this, map_view, bookmark_menu);

		open_file_action = new LoadFileAction(map_view.getFrame(), load_directory);
		clear_item = new JMenuItem(BUNDLE.getString("clearAll"), KeyEvent.VK_C);
		clear_graphs_item = new JMenuItem(BUNDLE.getString("clearGraphs"), KeyEvent.VK_L);
		open_file_item = new JMenuItem(BUNDLE.getString("openFile"), KeyEvent.VK_O);
		open_file_item.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/general/Open16.gif"));
		print_item = new JMenuItem(BUNDLE.getString("print"), KeyEvent.VK_P);
		print_item.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/general/Print16.gif"));
		print_frame_item = new JMenuItem(BUNDLE.getString("printWhole"), KeyEvent.VK_F);
		print_frame_item.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/general/Print16.gif"));
		export_to_file_menu = new JMenu(BUNDLE.getString("export"));
		export_to_file_menu.setMnemonic('T');
		export_map_item = new JMenuItem(BUNDLE.getString("mainView"), KeyEvent.VK_M);
		export_labelled_map_item = new JMenuItem(BUNDLE.getString("mainViewWithLabels"), KeyEvent.VK_L);
		export_slice_item = new JMenuItem(BUNDLE.getString("slicedViewWithLabels"), KeyEvent.VK_S);
		export_whole_frame = new JMenuItem(BUNDLE.getString("wholeFrame"));

		exit_item = new JMenuItem(BUNDLE.getString("exit"), KeyEvent.VK_E);

		adjust_edgematch_item = new JMenuItem(BUNDLE.getString("adjustEdgeMatchFuzziness"), KeyEvent.VK_F);
		view_ucsc_item = new JMenuItem(BUNDLE.getString("viewRegionInUCSCBrowser"));
		view_ucsc_item.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/development/WebComponent16.gif"));

		clamp_view_item = new JMenuItem(BUNDLE.getString("clampToView"), KeyEvent.VK_V);
		res2clip_item = new JMenuItem(BUNDLE.getString("copySelectedResiduesToClipboard"), KeyEvent.VK_C);
		res2clip_item.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/general/Copy16.gif"));
		unclamp_item = new JMenuItem(BUNDLE.getString("unclamp"), KeyEvent.VK_U);
		rev_comp_item = new JMenuItem(BUNDLE.getString("reverseComplement"));
		shrink_wrap_item = new JCheckBoxMenuItem(BUNDLE.getString("toggleShrinkWrapping"));
		shrink_wrap_item.setMnemonic(KeyEvent.VK_S);
		shrink_wrap_item.setState(map_view.getShrinkWrap());

		toggle_hairline_label_item = new JCheckBoxMenuItem(BUNDLE.getString("toggleHairlineLabel"));
		toggle_hairline_label_item.setMnemonic(KeyEvent.VK_H);
		boolean use_hairline_label = PreferenceUtils.getTopNode().getBoolean(SeqMapView.PREF_HAIRLINE_LABELED, true);
		if (map_view.isHairlineLabeled() != use_hairline_label) {
			map_view.toggleHairlineLabel();
		}
		toggle_hairline_label_item.setState(map_view.isHairlineLabeled());

		toggle_edge_matching_item = new JCheckBoxMenuItem(BUNDLE.getString("toggleEdgeMatching"));
		toggle_edge_matching_item.setMnemonic(KeyEvent.VK_M);
		toggle_edge_matching_item.setState(map_view.getEdgeMatching());
		autoscroll_item = new JMenuItem(BUNDLE.getString("autoScroll"), KeyEvent.VK_A);
		autoscroll_item.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/media/Movie16.gif"));
		move_tab_to_window_item = new JMenuItem(BUNDLE.getString("openCurrentTabInNewWindow"), KeyEvent.VK_O);
		move_tabbed_panel_to_window_item = new JMenuItem(BUNDLE.getString("openTabbedPanesInNewWindow"), KeyEvent.VK_P);

		fileMenu();

		editMenu();
		viewMenu();

		MenuUtil.addToMenu(tools_menu, web_links_item);

		gc_item = new JMenuItem("Invoke Garbage Collection", KeyEvent.VK_I);
		memory_item = new JMenuItem("Print Memory Usage", KeyEvent.VK_M);
		about_item = new JMenuItem(
				MessageFormat.format(
					MENU_ITEM_HAS_DIALOG,
					MessageFormat.format(
						BUNDLE.getString("about"),
						APP_NAME)),
				KeyEvent.VK_A);
		about_item.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/general/About16.gif"));
		console_item = new JMenuItem(MessageFormat.format(MENU_ITEM_HAS_DIALOG,BUNDLE.getString("showConsole"), KeyEvent.VK_C));
		console_item.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/development/Host16.gif"));
		bug_item = new JMenuItem(MessageFormat.format(MENU_ITEM_HAS_DIALOG,BUNDLE.getString("reportABug"), KeyEvent.VK_D));
		documentation_item = new JMenuItem(MessageFormat.format(MENU_ITEM_HAS_DIALOG,BUNDLE.getString("documentation"), KeyEvent.VK_D));
		documentation_item.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/general/Help16.gif"));

		MenuUtil.addToMenu(help_menu, about_item);
		MenuUtil.addToMenu(help_menu, bug_item);
		MenuUtil.addToMenu(help_menu, documentation_item);
		MenuUtil.addToMenu(help_menu, console_item);

		gc_item.addActionListener(this);
		memory_item.addActionListener(this);
		about_item.addActionListener(this);
		bug_item.addActionListener(this);
		documentation_item.addActionListener(this);
		console_item.addActionListener(this);
		clear_item.addActionListener(this);
		clear_graphs_item.addActionListener(this);
		open_file_item.addActionListener(this);
		print_item.addActionListener(this);
		print_frame_item.addActionListener(this);
		export_map_item.addActionListener(this);
		export_labelled_map_item.addActionListener(this);
		export_slice_item.addActionListener(this);
		export_whole_frame.addActionListener(this);
		exit_item.addActionListener(this);

		toggle_edge_matching_item.addActionListener(this);
		autoscroll_item.addActionListener(this);
		adjust_edgematch_item.addActionListener(this);
		view_ucsc_item.addActionListener(this);

		res2clip_item.addActionListener(this);
		rev_comp_item.addActionListener(this);
		shrink_wrap_item.addActionListener(this);
		clamp_view_item.addActionListener(this);
		unclamp_item.addActionListener(this);
		toggle_hairline_label_item.addActionListener(this);
		move_tab_to_window_item.addActionListener(this);
		move_tabbed_panel_to_window_item.addActionListener(this);

		Container cpane = frm.getContentPane();
		int table_height = 250;
		int fudge = 55;

		Rectangle frame_bounds = PreferenceUtils.retrieveWindowLocation("main window",
						new Rectangle(0, 0, 950, 600)); // 1.58 ratio -- near golden ratio and 1920/1200, which is native ratio for large widescreen LCDs.
		PreferenceUtils.setWindowSize(frm, frame_bounds);

		tab_pane = new JTabbedPane();

		cpane.setLayout(new BorderLayout());
		splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitpane.setDividerSize(8);
		splitpane.setDividerLocation(frm.getHeight() - (table_height + fudge));
		splitpane.setTopComponent(map_view);

		boolean tab_panel_in_a_window = (PreferenceUtils.getComponentState(TABBED_PANES_TITLE).equals(PreferenceUtils.COMPONENT_STATE_WINDOW));
		if (tab_panel_in_a_window) {
			openTabbedPanelInNewWindow(tab_pane);
		} else {
			splitpane.setBottomComponent(tab_pane);
		}

		cpane.add("Center", splitpane);

		// Using JTabbedPane.SCROLL_TAB_LAYOUT makes it impossible to add a
		// pop-up menu (or any other mouse listener) on the tab handles.
		// (A pop-up with "Open tab in a new window" would be nice.)
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4465870
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4499556
		tab_pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tab_pane.setMinimumSize(new Dimension(0, 0));

		cpane.add(status_bar, BorderLayout.SOUTH);

		// Show the frame before loading the plugins.  Thus any error panel
		// that is created by an exception during plugin set-up will appear
		// on top of the main frame, not hidden by it.

		frm.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent evt) {
				exit();
			}
		});
		frm.setVisible(true);

		plugins_info.add(new PluginInfo(DataLoadView.class.getName(), BUNDLE.getString("dataAccessTab"), true));
		plugins_info.add(new PluginInfo(PropertyView.class.getName(), BUNDLE.getString("selectionInfoTab"), true));
		plugins_info.add(new PluginInfo(SearchView.class.getName(), BUNDLE.getString("searchTab"), true));
		plugins_info.add(new PluginInfo(AltSpliceView.class.getName(), BUNDLE.getString("slicedViewTab"), true));
		plugins_info.add(new PluginInfo(SimpleGraphTab.class.getName(), BUNDLE.getString("graphAdjusterTab"), true));
		plugins_info.add(new PluginInfo(RestrictionControlView.class.getName(), BUNDLE.getString("restrictionSitesTab"), true));
		plugins_info.add(new PluginInfo(ExternalViewer.class.getName(), BUNDLE.getString("externalViewTab"), true));


		plugins_info.addAll(XmlPrefsParser.getPlugins());

		if (plugins_info == null || plugins_info.isEmpty()) {
			System.out.println("There are no plugins specified in preferences.");
		} else {
			for (PluginInfo pi : plugins_info) {
				Object plugin = setUpPlugIn(pi);
				plugins.add(plugin);
			}
		}

		for (Object plugin : plugins) {
			if (plugin instanceof DataLoadView) {
				data_load_view = (DataLoadView) plugin;
			}
		}

		if (slice_view != null) {
			MenuUtil.addToMenu(export_to_file_menu, export_slice_item);
			export_slice_item.setEnabled(true);
		}

		WebLink.autoLoad();


		// Need to let the QuickLoad system get started-up before starting
		//   the control server that listens to ping requests?
		// Therefore start listening for http requests only after all set-up is done.
		startControlServer();
	}

	private void fileMenu() {
		web_links_item = new JMenuItem(WebLinksManagerView.getShowFrameAction());
		preferences_item = new JMenuItem(MessageFormat.format(MENU_ITEM_HAS_DIALOG, BUNDLE.getString("preferences")), KeyEvent.VK_E);
		preferences_item.setIcon(MenuUtil.getIcon("toolbarButtonGraphics/general/Preferences16.gif"));
		preferences_item.addActionListener(this);
		MenuUtil.addToMenu(file_menu, open_file_item);
		MenuUtil.addToMenu(file_menu, clear_item);
		MenuUtil.addToMenu(file_menu, clear_graphs_item);
		file_menu.addSeparator();
		MenuUtil.addToMenu(file_menu, print_item);
		MenuUtil.addToMenu(file_menu, print_frame_item);
		file_menu.add(export_to_file_menu);
		MenuUtil.addToMenu(export_to_file_menu, export_map_item);
		MenuUtil.addToMenu(export_to_file_menu, export_labelled_map_item);
		MenuUtil.addToMenu(export_to_file_menu, export_whole_frame);
		file_menu.addSeparator();
		MenuUtil.addToMenu(file_menu, preferences_item);
		file_menu.addSeparator();
		MenuUtil.addToMenu(file_menu, exit_item);
	}

	private void editMenu() {
		MenuUtil.addToMenu(edit_menu, res2clip_item);
	}

	private void viewMenu() {
		JMenu strands_menu = new JMenu("Strands");
		strands_menu.add(new ActionToggler(getMapView().getSeqMap().show_plus_action));
		strands_menu.add(new ActionToggler(getMapView().getSeqMap().show_minus_action));
		strands_menu.add(new ActionToggler(getMapView().getSeqMap().show_mixed_action));
		view_menu.add(strands_menu);
		MenuUtil.addToMenu(view_menu, autoscroll_item);
		MenuUtil.addToMenu(view_menu, view_ucsc_item);
		MenuUtil.addToMenu(view_menu, toggle_edge_matching_item);
		MenuUtil.addToMenu(view_menu, adjust_edgematch_item);
		MenuUtil.addToMenu(view_menu, clamp_view_item);
		MenuUtil.addToMenu(view_menu, unclamp_item);
		MenuUtil.addToMenu(view_menu, shrink_wrap_item);
		MenuUtil.addToMenu(view_menu, toggle_hairline_label_item);
		MenuUtil.addToMenu(view_menu, move_tab_to_window_item);
		MenuUtil.addToMenu(view_menu, move_tabbed_panel_to_window_item);
	}

	/**
	 *  Puts the given component either in the tab pane or in its own window,
	 *  depending on saved user preferences.
	 */
	private Object setUpPlugIn(PluginInfo pi) {
		if (!pi.shouldLoad()) {
			return null;
		}

		String class_name = pi.getClassName();
		if (class_name == null || class_name.trim().length() == 0) {
			ErrorHandler.errorPanel("Bad Plugin",
							"Cannot create plugin '" + pi.getPluginName() + "' because it has no class name.",
							this.frm);
			PluginInfo.getNodeForName(pi.getPluginName()).putBoolean("load", false);
			return null;
		}

		Object plugin = null;
		Throwable t = null;
		try {
			plugin = PluginInfo.instantiatePlugin(class_name);
		} catch (InstantiationException e) {
			plugin = null;
			t = e;
		}

		if (plugin == null) {
			ErrorHandler.errorPanel("Bad Plugin",
							"Could not create plugin '" + pi.getPluginName() + "'.",
							this.frm, t);
			PluginInfo.getNodeForName(pi.getPluginName()).putBoolean("load", false);
			return null;
		}

		ImageIcon icon = null;

		if (plugin instanceof IPlugin) {
			IPlugin plugin_view = (IPlugin) plugin;
			this.setPluginInstance(plugin_view.getClass(), plugin_view);
			icon = (ImageIcon) plugin_view.getPluginProperty(IPlugin.TEXT_KEY_ICON);
		}

		if (plugin instanceof JComponent) {
			if (plugin instanceof DataLoadView) {
				data_load_view = (DataLoadView) plugin;
			}
			if (plugin instanceof AltSpliceView) {
				slice_view = (AltSpliceView) plugin;
			}

			comp2plugin.put((Component) plugin, pi);
			String title = pi.getDisplayName();
			String tool_tip = ((JComponent) plugin).getToolTipText();
			if (tool_tip == null) {
				tool_tip = title;
			}
			JComponent comp = (JComponent) plugin;
			boolean in_a_window = (PreferenceUtils.getComponentState(title).equals(PreferenceUtils.COMPONENT_STATE_WINDOW));
			addToPopupWindows(comp, title);
			JCheckBoxMenuItem menu_item = comp2menu_item.get(comp);
			menu_item.setSelected(in_a_window);
			if (in_a_window) {
				openCompInWindow(comp, tab_pane);
			} else {
				tab_pane.addTab(title, icon, comp, tool_tip);
			}
		}
		return plugin;
	}

	@Override
	public void setPluginInstance(Class c, IPlugin plugin) {
		super.setPluginInstance(c, plugin);
		if (c.equals(BookmarkManagerView.class)) {
			bmark_action.setBookmarkManager((BookmarkManagerView) plugin);
		}
	}

	public void actionPerformed(ActionEvent evt) {
		Object src = evt.getSource();
		if (src == open_file_item) {
			open_file_action.actionPerformed(evt);
		} else if (src == print_item) {
			try {
				map_view.getSeqMap().print();
			} catch (Exception ex) {
				errorPanel("Problem trying to print.", ex);
			}
		} else if (src == print_frame_item) {
			ComponentPagePrinter cprinter = new ComponentPagePrinter(getFrame());
			try {
				cprinter.print();
			} catch (Exception ex) {
				errorPanel("Problem trying to print.", ex);
			}
		} else if (src == export_map_item) {
			try {
				AffyLabelledTierMap tm = (AffyLabelledTierMap) map_view.getSeqMap();
				ComponentWriter.showExportDialog(tm.getNeoCanvas());
			} catch (Exception ex) {
				errorPanel("Problem during output.", ex);
			}
		} else if (src == export_labelled_map_item) {
			try {
				AffyLabelledTierMap tm = (AffyLabelledTierMap) map_view.getSeqMap();
				ComponentWriter.showExportDialog(tm.getSplitPane());
			} catch (Exception ex) {
				errorPanel("Problem during output.", ex);
			}
		} else if (src == export_whole_frame) {
			try {
				ComponentWriter.showExportDialog(getFrame());
			} catch (Exception ex) {
				errorPanel("Problem during output.", ex);
			}
		}else if (src == export_slice_item) {
			try {
				if (slice_view != null) {
					AffyLabelledTierMap tm = (AffyLabelledTierMap) slice_view.getSplicedView().getSeqMap();
					ComponentWriter.showExportDialog(tm.getSplitPane());
				}
			} catch (Exception ex) {
				errorPanel("Problem during output.", ex);
			}
		} else if (src == clear_item) {
			if (confirmPanel("Really clear entire view?")) {
				map_view.clear();
			}
		} else if (src == clear_graphs_item) {
			if (confirmPanel("Really clear graphs?")) {
				map_view.clearGraphs();
			}
		} else if (src == exit_item) {
			exit();
		} else if (src == res2clip_item) {
			map_view.copySelectedResidues();
		} else if (src == view_ucsc_item) {
			if (DEBUG_EVENTS) {
				System.out.println("trying to invoke UCSC genome browser");
			}
			map_view.invokeUcscView();
		} else if (src == autoscroll_item) {
			map_view.toggleAutoScroll();
		} else if (src == toggle_edge_matching_item) {
			map_view.setEdgeMatching(!map_view.getEdgeMatching());
			toggle_edge_matching_item.setState(map_view.getEdgeMatching());
		} else if (src == adjust_edgematch_item) {
			EdgeMatchAdjuster.showFramedThresholder(map_view.getEdgeMatcher(), map_view);
		}
		else if (src == shrink_wrap_item) {
			if (DEBUG_EVENTS) {
				System.out.println("trying to toggle map bounds shrink wrapping to extent of annotations");
			}
			map_view.setShrinkWrap(!map_view.getShrinkWrap());
			shrink_wrap_item.setState(map_view.getShrinkWrap());
		} else if (src == clamp_view_item) {
			map_view.clampToView();
		} else if (src == unclamp_item) {
			map_view.unclamp();
		} else if (src == toggle_hairline_label_item) {
			map_view.toggleHairlineLabel();
			boolean b = map_view.isHairlineLabeled();
			toggle_hairline_label_item.setState(b);
			PreferenceUtils.getTopNode().putBoolean(SeqMapView.PREF_HAIRLINE_LABELED, b);
		} else if (src == move_tab_to_window_item) {
			openTabInNewWindow(tab_pane);
		} else if (src == move_tabbed_panel_to_window_item) {
			openTabbedPanelInNewWindow(tab_pane);
		} else if (src == gc_item) {
			System.gc();
		} else if (src == about_item) {
			showAboutDialog();
		} else if (src == bug_item) {
			try {
				Desktop.getDesktop().browse(new URI(BUG_URL));
			} catch (URISyntaxException ex) {
				Logger.getLogger(IGB.class.getName()).log(Level.SEVERE, "Unable to open URL " + BUG_URL, ex);
			} catch (IOException ex) {
				Logger.getLogger(IGB.class.getName()).log(Level.SEVERE, "Unable to open URL " + BUG_URL, ex);
			}
		} else if (src == documentation_item) {
			showDocumentationDialog();
		} else if (src == console_item) {
			ConsoleView.showConsole();
		} else if (src == preferences_item) {
			PreferencesPanel pv = PreferencesPanel.getSingleton();
			JFrame f = pv.getFrame();
			f.setVisible(true);
		}
	}

	void showAboutDialog() {
		JPanel message_pane = new JPanel();
		message_pane.setLayout(new BoxLayout(message_pane, BoxLayout.Y_AXIS));
		JTextArea about_text = new JTextArea();

		String text = APP_NAME + ", version: " + APP_VERSION_FULL + "\n\n" +
						"IGB (pronounced ig-bee) is a product of the open source Genoviz project,\n" +
						"which develops interactive visualization software for genomics.\n" +
						"Affymetrix, Inc., donated Genoviz and IGB to the open source community in 2004.\n" +
						"IGB and Genoviz receive support from National Science Foundation's Arabidopsis 2010 program\n" +
						"and from a growing community of developers and scientists. For details, see:\n" +
						"http://igb.bioviz.org\n" +
						"http://genoviz.sourceforge.net\n\n" +
						"Source code for IGB is released under the Common Public License, v1.0.\n" +
						"IGB is Copyright (c) 2000-2005 Affymetrix, Inc.\n" +
						"IGB uses " +
						"the Fusion SDK from Affymetrix \n" +
						"and the Vector Graphics package from java.FreeHEP.org \n" +
						"(released under the LGPL license).\n\n";
		about_text.append(text);
		String cache_root = com.affymetrix.igb.util.LocalUrlCacher.getCacheRoot();
		File cache_file = new File(cache_root);
		if (cache_file.exists()) {
			about_text.append("\nCached data stored in: \n");
			about_text.append("  " + cache_file.getAbsolutePath() + "\n");
		}
		String data_dir = PreferenceUtils.getAppDataDirectory();
		if (data_dir != null) {
			File data_dir_f = new File(data_dir);
			about_text.append("\nApplication data stored in: \n  " +
							data_dir_f.getAbsolutePath() + "\n");
		}

		message_pane.add(new JScrollPane(about_text));
		JButton licenseB = new JButton("View IGB License");
		JButton apacheB = new JButton("View Apache License");
		JButton freehepB = new JButton("View FreeHEP Vector Graphics License");
		JButton fusionB = new JButton("View Fusion SDK License");
		licenseB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				GeneralUtils.browse("http://www.affymetrix.com/support/developer/tools/igbsource_terms.affx?to");
			}
		});
		apacheB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				GeneralUtils.browse("http://www.apache.org/licenses/LICENSE-2.0");
			}
		});
		freehepB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				GeneralUtils.browse("http://java.freehep.org/vectorgraphics/license.html");
			}
		});
		fusionB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				GeneralUtils.browse("http://www.affymetrix.com/support/developer/fusion/index.affx");
			}
		});
		JPanel buttonP = new JPanel(new GridLayout(2, 2));
		buttonP.add(licenseB);
		buttonP.add(apacheB);
		buttonP.add(freehepB);
		buttonP.add(fusionB);
		message_pane.add(buttonP);

		final JOptionPane pane = new JOptionPane(message_pane, JOptionPane.INFORMATION_MESSAGE,
						JOptionPane.DEFAULT_OPTION);
		final JDialog dialog = pane.createDialog(frm, MessageFormat.format(BUNDLE.getString("about"), APP_NAME));
		dialog.setVisible(true);
	}

	private void showDocumentationDialog() {
		JPanel message_pane = new JPanel();
		message_pane.setLayout(new BoxLayout(message_pane, BoxLayout.Y_AXIS));
		JTextArea about_text = new JTextArea();
		about_text.append(getDocumentationText());
		message_pane.add(new JScrollPane(about_text));

		JButton sfB = new JButton("Visit Genoviz Project");
		sfB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				GeneralUtils.browse("http://genoviz.sourceforge.net");
			}
		});
		Box buttonP = Box.createHorizontalBox();
		buttonP.add(sfB);

		message_pane.add(buttonP);

		final JOptionPane pane = new JOptionPane(message_pane, JOptionPane.INFORMATION_MESSAGE,
						JOptionPane.DEFAULT_OPTION);
		final JDialog dialog = pane.createDialog(frm, "Documentation");
		dialog.setResizable(true);
		dialog.setVisible(true);
	}

	private static String getDocumentationText() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		sb.append("Documentation and user forums for IGB can be found at SourceForge.\n");
		sb.append("\n");
		sb.append("The source code is hosted at SourceForge.net as part of the GenoViz project. \n");
		sb.append("There you can find downloads of source code, pre-compiled executables, \n");
		sb.append("extra documentation, and a place to report bugs or feature requests.\n");
		sb.append("\n");
		sb.append("Introduction Page: http://genoviz.sourceforge.net/\n");
		sb.append("User's Guide (PDF): \n http://genoviz.sourceforge.net/IGB_User_Guide.pdf\n");
		sb.append("Release Notes: \n http://genoviz.sourceforge.net/release_notes/igb_release.html");
		sb.append("\n");
		sb.append("Downloads: \n http://sourceforge.net/project/showfiles.php?group_id=129420\n");
		sb.append("Documentation: \n http://sourceforge.net/apps/trac/genoviz/wiki/IGB\n");
		sb.append("Bug Reports: \n http://sourceforge.net/tracker/?group_id=129420&atid=714744\n");
		sb.append("\n");

		return sb.toString();
	}

	/** Returns the icon stored in the jar file.
	 *  It is expected to be at com.affymetrix.igb.igb.gif.
	 *  @return null if the image file is not found or can't be opened.
	 */
	public Image getIcon() {
		Image icon = null;
		try {
			URL url = IGB.class.getResource("igb.gif");
			if (url != null) {
				icon = Toolkit.getDefaultToolkit().getImage(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// It isn't a big deal if we can't find the icon, just return null
		}
		return icon;
	}

	void exit() {
		boolean ask_before_exit = PreferenceUtils.getBooleanParam(PreferenceUtils.ASK_BEFORE_EXITING,
						PreferenceUtils.default_ask_before_exiting);
		String message = "Do you really want to exit?";
		if ((!ask_before_exit) || confirmPanel(message)) {
			if (bmark_action != null) {
				bmark_action.autoSaveBookmarks();
			}
			WebLink.autoSave();
			saveWindowLocations();
			Persistence.saveCurrentView(map_view);
			System.exit(0);
		}
	}

	/**
	 * Saves information about which plugins are in separate windows and
	 * what their preferred sizes are.
	 */
	private void saveWindowLocations() {
		// Save the main window location
		PreferenceUtils.saveWindowLocation(frm, "main window");

		for (Component comp : comp2plugin.keySet()) {
			Frame f = comp2window.get(comp);
			if (f != null) {
				PluginInfo pi = comp2plugin.get(comp);
				PreferenceUtils.saveWindowLocation(f, pi.getPluginName());
			}
		}
		Frame f = comp2window.get(tab_pane);
		if (f != null) {
			PreferenceUtils.saveWindowLocation(f, TABBED_PANES_TITLE);
		}
	}

	private void openTabInNewWindow(final JTabbedPane tab_pane) {
		Runnable r = new Runnable() {

			public void run() {
				int index = tab_pane.getSelectedIndex();
				if (index < 0) {
					errorPanel("No more panes!");
					return;
				}
				final JComponent comp = (JComponent) tab_pane.getComponentAt(index);
				openCompInWindow(comp, tab_pane);
			}
		};
		SwingUtilities.invokeLater(r);
	}

	private void openCompInWindow(final JComponent comp, final JTabbedPane tab_pane) {
		final String title;
		final String display_name;
		final String tool_tip = comp.getToolTipText();

		if (comp2plugin.get(comp) instanceof PluginInfo) {
			PluginInfo pi = comp2plugin.get(comp);
			title = pi.getPluginName();
			display_name = pi.getDisplayName();
		} else {
			title = comp.getName();
			display_name = comp.getName();
		}

		Image temp_icon = null;
		if (comp instanceof IPlugin) {
			IPlugin pv = (IPlugin) comp;
			ImageIcon image_icon = (ImageIcon) pv.getPluginProperty(IPlugin.TEXT_KEY_ICON);
			if (image_icon != null) {
				temp_icon = image_icon.getImage();
			}
		}
		if (temp_icon == null) {
			temp_icon = getIcon();
		}

		// If not already open in a new window, make a new window
		if (comp2window.get(comp) == null) {
			tab_pane.remove(comp);
			tab_pane.validate();

			final JFrame frame = new JFrame(display_name);
			final Image icon = temp_icon;
			if (icon != null) {
				frame.setIconImage(icon);
			}
			final Container cont = frame.getContentPane();
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			cont.add(comp);
			comp.setVisible(true);
			comp2window.put(comp, frame);
			frame.pack(); // pack() to set frame to its preferred size

			Rectangle pos = PreferenceUtils.retrieveWindowLocation(title, frame.getBounds());
			if (pos != null) {
				PreferenceUtils.setWindowSize(frame, pos);
			}
			frame.setVisible(true);
			frame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent evt) {
					// save the current size into the preferences, so the window
					// will re-open with this size next time
					PreferenceUtils.saveWindowLocation(frame, title);
					comp2window.remove(comp);
					cont.remove(comp);
					cont.validate();
					frame.dispose();
					tab_pane.addTab(display_name, null, comp, (tool_tip == null ? display_name : tool_tip));
					PreferenceUtils.saveComponentState(title, PreferenceUtils.COMPONENT_STATE_TAB);
					JCheckBoxMenuItem menu_item = comp2menu_item.get(comp);
					if (menu_item != null) {
						menu_item.setSelected(false);
					}
				}
			});
		} // extra window already exists, but may not be visible
		else {
			DisplayUtils.bringFrameToFront(comp2window.get(comp));
		}
		PreferenceUtils.saveComponentState(title, PreferenceUtils.COMPONENT_STATE_WINDOW);
	}

	private void openTabbedPanelInNewWindow(final JComponent comp) {

		final String title = TABBED_PANES_TITLE;
		final String display_name = title;
		//final String tool_tip = null;
		Image temp_icon = null;

		// If not already open in a new window, make a new window
		if (comp2window.get(comp) == null) {
			splitpane.remove(comp);
			splitpane.validate();

			final JFrame frame = new JFrame(display_name);
			final Image icon = temp_icon;
			if (icon != null) {
				frame.setIconImage(icon);
			}
			final Container cont = frame.getContentPane();
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			cont.add(comp);
			comp.setVisible(true);
			comp2window.put(comp, frame);
			frame.pack(); // pack() to set frame to its preferred size

			Rectangle pos = PreferenceUtils.retrieveWindowLocation(title, frame.getBounds());
			if (pos != null) {
				//check that it's not too small, problems with using two screens
				int posW = (int) pos.getWidth();
				if (posW < 650) {
					posW = 650;
				}
				int posH = (int) pos.getHeight();
				if (posH < 300) {
					posH = 300;
				}
				pos.setSize(posW, posH);
				PreferenceUtils.setWindowSize(frame, pos);
			}
			frame.setVisible(true);

			final Runnable return_panes_to_main_window = new Runnable() {

				public void run() {
					// save the current size into the preferences, so the window
					// will re-open with this size next time
					PreferenceUtils.saveWindowLocation(frame, title);
					comp2window.remove(comp);
					cont.remove(comp);
					cont.validate();
					frame.dispose();
					splitpane.setBottomComponent(comp);
					splitpane.setDividerLocation(0.70);
					PreferenceUtils.saveComponentState(title, PreferenceUtils.COMPONENT_STATE_TAB);
					JCheckBoxMenuItem menu_item = comp2menu_item.get(comp);
					if (menu_item != null) {
						menu_item.setSelected(false);
					}
				}
			};

			frame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent evt) {
					SwingUtilities.invokeLater(return_panes_to_main_window);
				}
			});

			JMenuBar mBar = new JMenuBar();
			frame.setJMenuBar(mBar);
			JMenu menu1 = new JMenu("Windows");
			menu1.setMnemonic('W');
			mBar.add(menu1);

			menu1.add(new AbstractAction("Return Tabbed Panes to Main Window") {

				public void actionPerformed(ActionEvent evt) {
					SwingUtilities.invokeLater(return_panes_to_main_window);
				}
			});
			menu1.add(new AbstractAction("Open Current Tab in New Window") {

				public void actionPerformed(ActionEvent evt) {
					openTabInNewWindow(tab_pane);
				}
			});
		} // extra window already exists, but may not be visible
		else {
			DisplayUtils.bringFrameToFront(comp2window.get(comp));
		}
		PreferenceUtils.saveComponentState(title, PreferenceUtils.COMPONENT_STATE_WINDOW);
	}

	public void popupNotify(JPopupMenu popup, List selected_items, SeqSymmetry primary_sym) {
		popup.add(popup_windowsM);
	}

	private void addToPopupWindows(final JComponent comp, final String title) {
		JCheckBoxMenuItem popupMI = new JCheckBoxMenuItem(title);
		popup_windowsM.add(popupMI);
		popupMI.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent evt) {
				JCheckBoxMenuItem src = (JCheckBoxMenuItem) evt.getSource();
				Frame frame = comp2window.get(comp);
				if (frame == null) {
					openCompInWindow(comp, tab_pane);
					src.setSelected(true);
				}
			}
		});
		comp2menu_item.put(comp, popupMI);
		popup_windowsM.add(new JCheckBoxMenuItem("foo"));
	}

	public String getApplicationName() {
		return APP_NAME;
	}

	public String getVersion() {
		return APP_VERSION_FULL;
	}

	/** Not yet implemented. */
	public String getResourceString(String key) {
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

	public Logger getLogger() {
		return APP;
	}
}
