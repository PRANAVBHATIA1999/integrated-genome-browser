/**
*   Copyright (c) 2001-2005 Affymetrix, Inc.
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

import com.affymetrix.genoviz.util.Memer;
import com.affymetrix.genoviz.util.ComponentPagePrinter;

import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.menuitem.*;
import com.affymetrix.igb.view.*;
import com.affymetrix.igb.bookmarks.Bookmark;
import com.affymetrix.igb.bookmarks.BookmarkController;
import com.affymetrix.igb.event.*;
import com.affymetrix.igb.glyph.EdgeMatchAdjuster;
import com.affymetrix.igb.parsers.XmlPrefsParser;
import com.affymetrix.igb.prefs.*;
import com.affymetrix.igb.servlets.UnibrowControlServer;
import com.affymetrix.igb.util.UnibrowAuthenticator;
import com.affymetrix.igb.util.UnibrowPrefsUtil;
import com.affymetrix.igb.util.WebBrowserControl;
import com.affymetrix.igb.util.SynonymLookup;
import com.affymetrix.igb.util.ErrorHandler;

/**
 *  Main class for the Integrated Genome Browser (IGB, pronounced ig-bee).
 */
public class IGB implements ActionListener, ContextualPopupListener  {
  static IGB singleton_igb;
  public static String APP_NAME = IGBConstants.APP_NAME;
  public static String IGB_VERSION = IGBConstants.IGB_VERSION;

  public static final boolean DEBUG_EVENTS = false;
  public static boolean CURATION_ENABLED = true;
  public static boolean ALLOW_PARTIAL_SEQ_LOADING = true;

  public static final String PREF_SEQUENCE_ACCESSIBLE = "Sequence accessible";
  public static boolean default_sequence_accessible = true;

  public static Color default_bg_col = Color.black;
  public static Color default_label_col = Color.white;

  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
  static Map prefs_hash;
  static String[] main_args;
  static Map comp2window = new HashMap(); // Maps Component -> Frame
  Map comp2plugin = new HashMap(); // Maps Component -> PluginInfo
  private static Map id2sym_hash = new HashMap();
  private static Vector sym_map_change_listeners = new Vector(1);

  JMenu popup_windowsM = new JMenu("Open in Window...");
  Memer mem = new Memer();
  UnibrowControlServer web_control = null;

  JFrame frm;
  JMenuBar mbar;
  JMenu file_menu;
  JMenu view_menu;
  JMenu bookmark_menu;
  JMenu help_menu;
  JTabbedPane tab_pane;

  public BookMarkAction bmark_action; // needs to be public for the BookmarkManagerView plugin
  LoadFileAction open_file_action;
  OpenGraphAction open_graph_action;
  DasFeaturesAction2 load_das_action;

  JMenuItem gc_item;
  JMenuItem memory_item;
  JMenuItem about_item;

  JMenuItem clear_item;
  JMenuItem clear_graphs_item;

  JMenuItem open_file_item;
  JMenuItem open_graph_item;
  JMenuItem load_das_item;
  JMenuItem print_item;
  JMenuItem print_frame_item;
  JMenuItem preferences_item;
  JMenuItem exit_item;

  JMenuItem view_ucsc_item;

  JMenuItem res2clip_item;
  JMenuItem clamp_view_item;
  JMenuItem unclamp_item;
  JMenuItem rev_comp_item;
  JMenuItem shrink_wrap_item;
  JMenuItem adjust_edgematch_item;
  JMenuItem toggle_hairline_label_item;
  JMenuItem toggle_edge_matching_item;
  JMenuItem autoscroll_item;
  JMenuItem bgcolor_item;

  JMenuItem move_tab_to_window_item;

  SeqMapView map_view;
  //QuickLoaderView quickload_view;

  CurationControl curation_control;
  AlignControl align_control;

  java.util.List plugin_list;

  static String user_dir = System.getProperty("user.dir");
  static String user_home = System.getProperty("user.home");

  FileTracker load_directory = FileTracker.DATA_DIR_TRACKER;

  static String default_prefs_resource = "/igb_default_prefs.xml";
  public static final String DEFAULT_PREFS_FILENAME = "igb_prefs.xml";
  static String default_user_prefs_files =
    (new File(user_home, DEFAULT_PREFS_FILENAME)).getAbsolutePath() +
    ";" +
    (new File(user_dir, DEFAULT_PREFS_FILENAME)).getAbsolutePath();

  static String rest_file = "rest_enzymes"; // located in same directory as this class


  /**
   * Start the program.
   */
  public static void main(String[] args) {
   try {
    main_args = args;
    getIGBPrefs(); // force loading of prefs

    String url_from_xml_prefs = (String) IGB.getIGBPrefs().get("QuickLoadUrl");
    if (url_from_xml_prefs != null) {
      UnibrowPrefsUtil.getLocationsNode().put(QuickLoaderView.PREF_QUICKLOAD_URL, url_from_xml_prefs);
    }

    SynonymLookup dlookup = new SynonymLookup();
    String quick_load_url = QuickLoaderView.getQuickLoadUrl();
    dlookup.loadSynonyms(quick_load_url + "synonyms.txt");
    SynonymLookup.setDefaultLookup(dlookup);

    singleton_igb = new IGB();
    singleton_igb.init();
    
    // If the command line contains a parameter "-href http://..." where
    // the URL is a valid IGB control bookmark, then go to that bookmark.
    String url = get_arg("-href", args);
    if (url != null && url.length() > 0) {
      try {
        System.out.println("Loading bookmark: "+url);
        Bookmark bm = new Bookmark(null, url);
        if (bm.isUnibrowControl()) {
          BookmarkController.viewBookmark(singleton_igb, bm);
        }
      } catch (MalformedURLException mue) {
        mue.printStackTrace(System.err);
      }
    }
   } catch (Exception e) {
     e.printStackTrace();
     System.exit(1);
   }
  }


  public IGB() { }

  public static boolean isSequenceAccessible() {
    //return UnibrowPrefsUtil.getBooleanParam(PREF_SEQUENCE_ACCESSIBLE, default_sequence_accessible);
    return default_sequence_accessible;
  }

  /**
   * This method is called only when deploying IGB
   * via a JNLP client such as webstart. This will download
   * a copy of a file named {@link #DEFAULT_PREFS_FILENAME}
   * to the client machine so the users
   * can customize their preferences.
   */
  public static void downloadPrefsFile() {
    //download prefs file if not available in user.dir
    //destination dir is decided by browser.
    File dest_prefs_file = new File(user_home, DEFAULT_PREFS_FILENAME);
    if (! dest_prefs_file.exists()) {
      try {
          InputStream prefs_strm = IGB.class.getResourceAsStream("/"+DEFAULT_PREFS_FILENAME);
        if (prefs_strm==null) {
          System.out.println("Could not locate "+DEFAULT_PREFS_FILENAME+" to download");
        } else {
          OutputStream out_strm = new FileOutputStream(dest_prefs_file);
          int c;
          while ((c = prefs_strm.read()) != -1) { out_strm.write(c); }
          prefs_strm.close();
          out_strm.close();
          informPanel("Preferences file for " + APP_NAME + " has been downloaded to: \n" +
                      dest_prefs_file + "\nPlease edit this file if you want to " +
                      "customize preferences or \nreplace this file if you already have" +
                      " custom preferences for " + APP_NAME);
        }
      } catch (IOException ioe) {
        System.out.println("Could not copy "+DEFAULT_PREFS_FILENAME+" to "
          + dest_prefs_file.getAbsolutePath());
      }
    }
  }

  public SeqMapView getMapView() {
    return map_view;
  }

  // currently not needed
  //public QuickLoaderView getQuickLoaderView() {
  //  return quickload_view;
  //}

  public static IGB getSingletonIGB() {
    return singleton_igb;
  }

  public JMenuBar getMenuBar() { return mbar; }
  public JFrame getFrame() { return frm; }
  public JTabbedPane getTabPane() { return tab_pane; }

  public void startControlServer() {
    web_control = new UnibrowControlServer(this);
  }

  public UnibrowControlServer getControlServer() {
    return web_control;
  }

  /**
   * Parse the command line arguments.  Find out what prefs file to use.
   * Return the name of the file as a String, or null if not invoked with
   * -prefs option.
   */
  public static String[] get_prefs_files(String[] args) {
    String files = get_arg("-prefs", args);
    if (files==null) {files = default_user_prefs_files;}
    StringTokenizer st = new StringTokenizer(files, ";");
    Set result = new HashSet();
    result.add(st.nextToken());
    for (int i=0; st.hasMoreTokens(); i++) {
      result.add(st.nextToken());
    }
    return (String[]) result.toArray(new String[result.size()]);
  }

  public static String get_deployment_type(String[] args) {
    String deployment_type = get_arg("-deploy", args);
    return deployment_type;
  }

  public static String get_default_prefs_url(String[] args) {
    String def_prefs_url = get_arg("-default_prefs_url", args);
    return def_prefs_url;
  }


  /**
   * Returns the value of the argument indicated by label.
   * e.g., if given
   *   -foo bar
   * returns bar.  Expects to find both in the given array.
   */
  public static String get_arg(String label,String[] args) {
    String to_return = null;
    if (label != null && args != null) {
      int num_args = args.length;
      boolean got_it = false;
      for (int i = 0 ; i < num_args ; i++) {
	String item = args[i];
	if (got_it) {
	  to_return = item;
	  break;
	}
	if (item.equals(label)) {
	  got_it = true;
	}
      }
    }
    return to_return;
  }

  /**
   *  Map of tags (usually names or ids) to SeqSymmetries for currently
   *  loaded genome and/or chromosome.
   */
  public static final Map getSymHash() {
    return id2sym_hash;
  }

  public static void clearSymHash() {
    id2sym_hash.clear();
    symHashChanged(IGB.class); // IGB.class is the most obvious event source
  }

  public static SingletonGenometryModel getGenometryModel() {
    return gmodel;
  }

  /** Call this method if you alter the Map returned by {@link #getSymHash}.
   *  @param source  The source responsible for the change, used in constructing
   *    the {@link SymMapChangeEvent}.
   */
  public static void symHashChanged(Object source) {
    java.util.List list = getSymMapChangeListeners();
    for (int i=0; i<list.size(); i++) {
      SymMapChangeListener l = (SymMapChangeListener) list.get(i);
      l.symMapModified(new SymMapChangeEvent(source, getSymHash()));
    }
  }

  public static java.util.List getSymMapChangeListeners() {
    return sym_map_change_listeners;
  }

  public static void addSymMapChangeListener(SymMapChangeListener l) {
    sym_map_change_listeners.add(l);
  }

  public static void removeSymMapChangeListener(SymMapChangeListener l) {
    sym_map_change_listeners.remove(l);
  }

  /**
   *  Returns IGB prefs hash
   *  If prefs haven't been loaded yet, will force loading of prefs
   */
  public static Map getIGBPrefs() {
    if (prefs_hash != null)  { return prefs_hash; }
    else {
      prefs_hash = new HashMap();

      /**  first load default prefs from jar */
      InputStream default_prefs_stream = null;
      XmlPrefsParser prefs_parser = new XmlPrefsParser();
      try {
	default_prefs_stream = IGB.class.getResourceAsStream(default_prefs_resource);
	System.out.println("loading default prefs from: " + default_prefs_resource);
	prefs_parser.parse(default_prefs_stream, "", prefs_hash);
      } catch (Exception ex) {
	System.out.println("Problem parsing prefs from: " + default_prefs_resource);
	ex.printStackTrace();
      } finally {
	try {default_prefs_stream.close();} catch (Exception e) {}
      }

      String deploy_type = get_deployment_type(main_args);
      if (deploy_type != null) {
	if (deploy_type.equals("webstart")) {
	  System.out.println("*****" + deploy_type + " deployment********");
	  downloadPrefsFile();
	}
      }

      String def_prefs_url = get_default_prefs_url(main_args);
      if (def_prefs_url != null) {
	InputStream default_prefs_url_str = null;
	try {
	  URL prefs_url = new URL(def_prefs_url);
	  default_prefs_url_str = prefs_url.openStream();
	  System.out.println("loading default prefs from url: " + def_prefs_url);
	  new XmlPrefsParser().parse(default_prefs_url_str, def_prefs_url, prefs_hash);
	} catch (Exception ex) {
	  System.out.println("Problem parsing prefs from url: " + def_prefs_url);
	  ex.printStackTrace();
	} finally {
	  try {default_prefs_url_str.close();} catch (Exception e) {}
	}
      }
      String[] prefs_files = get_prefs_files(main_args);
      if (prefs_files.length > 0) {
	prefs_parser = new XmlPrefsParser();
	for (int i=0; i<prefs_files.length; i++) {
	  String filename = prefs_files[i];
	  InputStream strm = null;

	  try {
	    System.out.flush();
	    System.out.println("loading user prefs from: " + filename);
	    File fil = new File(filename);
	    if (fil.exists()) {
	      strm = new FileInputStream(fil);
	      prefs_parser.parse(strm, fil.getCanonicalPath(), prefs_hash);
	    }
	    else {
	      System.out.println("could not find prefs file: " + filename);
	    }
	  } catch (Exception ex) {
	    System.out.flush();
	    System.out.println("Problem parsing prefs from: " + filename);
	    System.out.println(ex.toString());
	  } finally {
	    try {strm.close();} catch (Exception e) {}
	  }
	}
      }

    }
    return prefs_hash;
  }

  protected void init() {
    // force loading of prefs if hasn't happened yet
    // usually since IGB.main() is called first, prefs will have already been loaded
    //   via getUnibrowPrefs() call in main().  But if for some reason an IGB instance
    //   is created without call to main(), will force loading of prefs here...
    getIGBPrefs();

    startControlServer();

    frm = new JFrame(APP_NAME);

    // when HTTP authentication is needed, getPasswordAuthentication will
    //    be called on the authenticator set as the default
    Authenticator.setDefault(new UnibrowAuthenticator(frm));

    frm.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    Image icon = getIcon();
    if (icon != null) { frm.setIconImage(icon); }

    mbar = new JMenuBar();
    frm.setJMenuBar(mbar);
    file_menu = new JMenu("File");
    file_menu.setMnemonic('F');
    mbar.add( file_menu );

    view_menu = new JMenu("View");
    view_menu.setMnemonic('V');
    mbar.add(view_menu);

    bookmark_menu = new JMenu("Bookmarks");
    bookmark_menu.setMnemonic('B');
    mbar.add(bookmark_menu);

    help_menu = new JMenu("Help");
    help_menu.setMnemonic('H');
    mbar.add(help_menu);

    //    select_broker = new SymSelectionBroker();

    map_view = new SeqMapView();

    gmodel.addSeqSelectionListener(map_view);
    gmodel.addGroupSelectionListener(map_view);
    gmodel.addSymSelectionListener(map_view);
    //    gmodel.addSeqModifiedListener(map_view);

    map_view.setFrame(frm);
    map_view.setColorHash(prefs_hash);

    bmark_action = new BookMarkAction(this, map_view, bookmark_menu);

    align_control = new AlignControl(this, map_view);
    if (CURATION_ENABLED) {
      curation_control = new CurationControl(map_view);
    }

    open_file_action = new LoadFileAction(map_view, load_directory);
    open_graph_action = new OpenGraphAction(map_view, load_directory);
    load_das_action = new DasFeaturesAction2(map_view);
    clear_item = new JMenuItem("Clear All", KeyEvent.VK_C);
    clear_graphs_item = new JMenuItem("Clear Graphs", KeyEvent.VK_L);
    open_file_item = new JMenuItem("Open file", KeyEvent.VK_O);
    open_graph_item = new JMenuItem("Add Graph", KeyEvent.VK_G);
    load_das_item = new JMenuItem("Load DAS Features", KeyEvent.VK_D);
    print_item = new JMenuItem("Print", KeyEvent.VK_P);
    print_frame_item = new JMenuItem("Print Whole Frame", KeyEvent.VK_F);

    exit_item = new JMenuItem("Exit", KeyEvent.VK_E);

    adjust_edgematch_item = new JMenuItem("Adjust edge match fuzziness", KeyEvent.VK_F);
    view_ucsc_item = new JMenuItem("View Region in UCSC Browser", KeyEvent.VK_R);

    clamp_view_item = new JMenuItem("Clamp To View", KeyEvent.VK_V);
    res2clip_item = new JMenuItem("Copy Selected Residues to Clipboard", KeyEvent.VK_C);
    unclamp_item = new JMenuItem("Unclamp", KeyEvent.VK_U);
    rev_comp_item = new JMenuItem("Reverse Complement", KeyEvent.VK_R);
    shrink_wrap_item = new JMenuItem("Toggle Shrink Wrapping", KeyEvent.VK_S);

    toggle_hairline_label_item = new JMenuItem("Toggle Hairline Label", KeyEvent.VK_H);
    toggle_edge_matching_item = new JMenuItem("Toggle Edge Matching");
    autoscroll_item = new JMenuItem("AutoScroll");
    move_tab_to_window_item = new JMenuItem("Open Tab in New Window", KeyEvent.VK_O);

    preferences_item = new JMenuItem("Preferences ...", KeyEvent.VK_E);
    preferences_item.addActionListener(this);

    MenuUtil.addToMenu(file_menu, open_file_item);
    MenuUtil.addToMenu(file_menu, open_graph_item);
    MenuUtil.addToMenu(file_menu, load_das_item);
    MenuUtil.addToMenu(file_menu, clear_item);
    MenuUtil.addToMenu(file_menu, clear_graphs_item);
    file_menu.addSeparator();
    MenuUtil.addToMenu(file_menu, print_item);
    MenuUtil.addToMenu(file_menu, print_frame_item);
    file_menu.addSeparator();
    MenuUtil.addToMenu(file_menu, preferences_item);

    file_menu.addSeparator();
    MenuUtil.addToMenu(file_menu, exit_item);

    // rev_comp option currently not working, so disabled
    //    MenuUtil.addToMenu(view_menu, rev_comp_item);
    MenuUtil.addToMenu(view_menu, autoscroll_item);
    MenuUtil.addToMenu(view_menu, res2clip_item);
    MenuUtil.addToMenu(view_menu, view_ucsc_item);

    MenuUtil.addToMenu(view_menu, toggle_edge_matching_item);
    MenuUtil.addToMenu(view_menu, adjust_edgematch_item);
    MenuUtil.addToMenu(view_menu, clamp_view_item);
    MenuUtil.addToMenu(view_menu, unclamp_item);
    MenuUtil.addToMenu(view_menu, shrink_wrap_item);
    MenuUtil.addToMenu(view_menu, toggle_hairline_label_item);
    MenuUtil.addToMenu(view_menu, move_tab_to_window_item);

    gc_item = new JMenuItem("Invoke Garbage Collection", KeyEvent.VK_I);
    memory_item = new JMenuItem("Print Memory Usage", KeyEvent.VK_M);
    about_item = new JMenuItem("About " + APP_NAME + "...", KeyEvent.VK_A);

    MenuUtil.addToMenu(help_menu, about_item);
    //MenuUtil.addToMenu(help_menu, gc_item);
    //MenuUtil.addToMenu(help_menu, memory_item);

    gc_item.addActionListener(this);
    memory_item.addActionListener(this);
    about_item.addActionListener(this);
    clear_item.addActionListener(this);
    clear_graphs_item.addActionListener(this);
    open_file_item.addActionListener(this);
    open_graph_item.addActionListener(this);
    load_das_item.addActionListener(this);
    print_item.addActionListener(this);
    print_frame_item.addActionListener(this);
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

    Container cpane = frm.getContentPane();
    int table_height = 250;
    int fudge = 55;
    RepaintManager rm = RepaintManager.currentManager(frm);
    Rectangle frame_bounds = UnibrowPrefsUtil.retrieveWindowLocation("main window",
        new Rectangle(0, 0, 800, 600));
    UnibrowPrefsUtil.setWindowSize(frm, frame_bounds);

    tab_pane = new JTabbedPane();

    cpane.setLayout(new BorderLayout());
    JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitpane.setOneTouchExpandable(true);
    splitpane.setDividerSize(8);
    splitpane.setDividerLocation(frm.getHeight() - (table_height + fudge));
    splitpane.setTopComponent(map_view);
    splitpane.setBottomComponent(tab_pane);
    cpane.add("Center", splitpane);

    ArrayList plugin_list = new ArrayList(16);
    PluginInfo quickload = new PluginInfo(QuickLoaderView.class.getName(), "QuickLoad", true);
    plugin_list.add(quickload);

    PluginInfo selection_info = new PluginInfo(SymTableView.class.getName(), "Selection Info", true);
    plugin_list.add(selection_info);

    plugin_list.addAll(getPluginsFromXmlPrefs(getIGBPrefs()));
    //plugin_list = null;
    //try {
    //  plugin_list = PluginInfo.getAllPlugins();
    //} catch (java.util.prefs.BackingStoreException bse) {
    //  UnibrowPrefsUtil.handleBSE(this.frm, bse);
    //}


    if (plugin_list == null || plugin_list.isEmpty()) {
      System.out.println("There are no plugins specified in preferences.");
    } else {
      Iterator iter = plugin_list.iterator();
      while (iter.hasNext()) {
        PluginInfo pi = (PluginInfo) iter.next();
        setUpPlugIn(pi);
      }
    }

    tab_pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tab_pane.setMinimumSize(new Dimension(0,0));

    frm.addWindowListener( new WindowAdapter() {
	public void windowClosing(WindowEvent evt) {exit();}
      });
    frm.show();

  }

  /**
   *  Puts the given component either in the tab pane or in its own window,
   *  depending on saved user preferences.
   */
  void setUpPlugIn(PluginInfo pi) {

    if (! pi.shouldLoad()) return;

    String class_name = pi.getClassName();
    if (class_name == null || class_name.trim().length()==0) {
      ErrorHandler.errorPanel("Bad Plugin",
        "Cannot create plugin '"+pi.getPluginName()+"' because it has no class name.",
        this.frm);
      PluginInfo.getNodeForName(pi.getPluginName()).putBoolean("load", false);
      return;
    }

    Object plugin = pi.instantiatePlugin(class_name);

    if (plugin == null) {
      ErrorHandler.errorPanel("Bad Plugin",
        "Could not create plugin '"+pi.getPluginName()+"'.",
        this.frm);
      PluginInfo.getNodeForName(pi.getPluginName()).putBoolean("load", false);
      return;
    }

    Icon icon = null;

    if (plugin instanceof IPlugin) {
      IPlugin plugin_view = (IPlugin) plugin;
      icon = (Icon) plugin_view.getPluginProperty(IPlugin.TEXT_KEY_ICON);

      plugin_view.putPluginProperty(IPlugin.TEXT_KEY_IGB, this);
      plugin_view.putPluginProperty(IPlugin.TEXT_KEY_SEQ_MAP_VIEW, map_view);
      // An alternative to having IPlugin interface is checking for
      // these other interfaces ....
      //    if (plugin instanceof SymSelectionListener) { }
      //    if (plugin instanceof SeqSelectionListener) { }
      //    if (plugin instanceof GroupSelectionListener) { }

      // ... or plugins that need to know about SeqMapView or other components accessible via
      //     IGB class should access them via IGB singleton method calls
      //     and can add themselves as listeners for various events in their constructor ...
    }

    if (plugin instanceof JComponent) {
      comp2plugin.put(plugin, pi);
      String title = pi.getDisplayName();
      String tool_tip = ((JComponent) plugin).getToolTipText();
      if (tool_tip == null) {tool_tip = title;}
      JComponent comp = (JComponent) plugin;
      boolean in_a_window = (UnibrowPrefsUtil.getComponentState(title).equals(UnibrowPrefsUtil.COMPONENT_STATE_WINDOW));
      //boolean in_a_window = PluginInfo.PLACEMENT_WINDOW.equals(pi.getPlacement());
      if (in_a_window) {
        //openCompInWindow(comp, title, tool_tip, null, tab_pane);
        openCompInWindow(comp, tab_pane);
      }
      else {
        tab_pane.addTab(title, icon, comp, tool_tip);
      }
      addToPopupWindows(comp, title);
    }
  }

  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if (src == open_graph_item) {
      open_graph_action.actionPerformed(evt);
    }
    else if (src == open_file_item) {
      open_file_action.actionPerformed(evt);
    }
    else if (src == load_das_item) {
      load_das_action.actionPerformed(evt);
    }
    else if (src == print_item) {
      try {
        map_view.getSeqMap().print();
      }
      catch (Exception ex) {
        errorPanel("Problem trying to print.", ex);
      }
    }
    else if (src == print_frame_item) {
      ComponentPagePrinter cprinter = new ComponentPagePrinter(frm);
      try {
        cprinter.print();
      }
      catch (Exception ex) {
        errorPanel("Problem trying to print.", ex);
      }
    }
    else if (src == clear_item) {
      if (confirmPanel("Really clear entire view?")) {
        map_view.clear();
      }
    }
    else if (src == clear_graphs_item) {
      if (confirmPanel("Really clear graphs?")) {
        map_view.clearGraphs();
      }
    }
    else if (src == exit_item) {
      exit();
    }
    else if (src == res2clip_item) {
      System.out.println("trying to copy last rubberbanded region to clipboard");
      map_view.copySelectedResidues();
    }
    else if (src == view_ucsc_item) {
      System.out.println("trying to invoke UCSC genome browser");
      map_view.invokeUcscView();
    }
    else if (src == autoscroll_item) {
      map_view.toggleAutoScroll();
    }
    else if (src == toggle_edge_matching_item) {
      map_view.setEdgeMatching(! map_view.getEdgeMatching());
    }
    else if (src == adjust_edgematch_item) {
      System.out.println("trying to adjust edge-matching fuzziness");
      EdgeMatchAdjuster.showFramedThresholder(map_view.getEdgeMatcher(), map_view);
    }
    // rev comp not working
    //    else if (src == rev_comp_item) {
    //      map_view.reverseComplement();
    //    }
    else if (src == shrink_wrap_item) {
      System.out.println("trying to toggle map bounds shrink wrapping to extent of annotations");
      map_view.setShrinkWrap(! map_view.getShrinkWrap());
    }
    else if (src == clamp_view_item) {
      System.out.println("trying to clamp to view");
      map_view.clampToView();
    }
    else if (src == unclamp_item) {
      System.out.println("trying to unclamp");
      map_view.unclamp();
    }
    else if (src == toggle_hairline_label_item) {
      map_view.toggleHairlineLabel();
    } else if (src == move_tab_to_window_item) {
      openTabInNewWindow(tab_pane);
    }
    else if (src == gc_item) {
      System.gc();
    }
    else if (src == memory_item) {
      mem.printMemory();
    }
    else if (src == about_item) {
      showAboutDialog();
    } else if (src == preferences_item) {
      PreferencesPanel pv = PreferencesPanel.getSingleton();
      JFrame f = pv.getFrame();
      f.show();
    }
  }


  public void showAboutDialog() {
    JPanel message_pane = new JPanel();
    message_pane.setLayout(new BoxLayout(message_pane, BoxLayout.Y_AXIS));
    JTextArea about_text = new JTextArea();
    about_text.append(APP_NAME + ", version: " + IGB_VERSION + "\n");
    about_text.append("Copyright 2001-2005 Affymetrix Inc." + "\n");
    about_text.append("\n");
    about_text.append(APP_NAME + " uses the Xerces\n");
    about_text.append("package from the Apache Software Foundation, \n");
    about_text.append("and the Jetty package from Mort Bay Consulting.\n");
    Iterator names = XmlPrefsParser.getFilenames(prefs_hash).iterator();
    if (names.hasNext()) {
      about_text.append("\nLoaded the following preference file(s): \n");
      while (names.hasNext()) {
        about_text.append("  " + (String) names.next() + "\n");
      }
    }
    String cache_root = com.affymetrix.igb.util.LocalUrlCacher.getCacheRoot();
    File cache_file = new File(cache_root);
    if (cache_file.exists()) {
      about_text.append("\nCached data stored in: \n");
      about_text.append("  " + cache_file.getAbsolutePath() + "\n");
    }
    String data_dir = UnibrowPrefsUtil.getAppDataDirectory();
    if (data_dir != null) {
      about_text.append("\nApplication data stored in: \n  "+ data_dir +"\n");
    }

    message_pane.add(new JScrollPane(about_text));
    JButton licenseB = new JButton("View IGB License");
    licenseB.setForeground(Color.darkGray);
    JButton apacheB = new JButton("View Apache License");
    apacheB.setForeground(Color.darkGray);
    JButton jettyB = new JButton("View Jetty License");
    jettyB.setForeground(Color.darkGray);
    licenseB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          WebBrowserControl.displayURL("http://www.affymetrix.com/support/developer/tools/igbsource_terms.affx?to");
        }
      } );
    apacheB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          WebBrowserControl.displayURL("http://www.apache.org/licenses/LICENSE-2.0");
        }
      } );
    jettyB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          WebBrowserControl.displayURL("http://jetty.mortbay.org/jetty/");
        }
      } );
    JPanel buttonP = new JPanel(new GridLayout(3,1));
    buttonP.add(licenseB);
    buttonP.add(apacheB);
    buttonP.add(jettyB);
    message_pane.add(buttonP);

    JOptionPane.showMessageDialog(frm,
				 message_pane,
				 ("About " + APP_NAME),
				 JOptionPane.INFORMATION_MESSAGE,
				 null);
  }


  /** Returns the icon stored in the jar file.
   *  It is expected to be at com.affymetrix.igb.affychip.gif.
   *  @return null if the image file is not found or can't be opened.
   */
  public static Image getIcon() {
    Image icon = null;
    try {
      URL url = IGB.class.getResource("affychip.gif");
      if (url != null) {
        icon = Toolkit.getDefaultToolkit().getImage(url);
      }
    } catch (Exception e) {
      // It isn't a big deal if we can't find the icon, just return null
    }
    return icon;
  }

  private void exit() {
    boolean ask_before_exit = UnibrowPrefsUtil.getBooleanParam(UnibrowPrefsUtil.ASK_BEFORE_EXITING, false);
    String message = "Really exit?";
    if ( (! ask_before_exit) || confirmPanel(message)) {
      if (bmark_action != null) {
        bmark_action.autoSaveBookmarks();
      }
      saveWindowLocations();
      System.exit(0);
    }
  }

  /**
   * Saves information about which plugins are in separate windows and
   * what their preferred sizes are.
   */
  private void saveWindowLocations() {
    // Save the main window location
    UnibrowPrefsUtil.saveWindowLocation(frm, "main window");

    Iterator iter = comp2plugin.keySet().iterator();
    while (iter.hasNext()) {
      Component comp = (Component) iter.next();
      PluginInfo pi = (PluginInfo) comp2plugin.get(comp);
      Frame f = (Frame) comp2window.get(comp);
      if (f != null) {
        UnibrowPrefsUtil.saveWindowLocation(f, pi.getPluginName());
      }
    }
  }

  /* Determines whether stack traces will be printed by the errorPanel routine. */
  private static final boolean print_stack_traces = true;

  /** Opens a JOptionPane.ERROR_MESSAGE panel with the IGB
   *  panel as its parent.
   */
  public static void errorPanel(String title, String message) {
    errorPanel(title, message, (Throwable) null);
  }

  /** Opens a JOptionPane.ERROR_MESSAGE panel with the IGB
   *  panel as its parent.
   */
  public static void errorPanel(String title, String message, Throwable e) {
    IGB igb = getSingletonIGB();
    JFrame frame = (igb==null) ? null : igb.frm;
    errorPanel(frame, title, message, e);
  }

  /** Opens a JOptionPane.ERROR_MESSAGE panel with the given frame
   *  as its parent.
   *  This is designed to probably be safe from the EventDispatchThread or from
   *  any other thread.
   *  @param frame the parent frame, null is ok.
   *  @param e an exception (or error), if any.  null is ok. If not null,
   *  the exception text will be appended to the message and
   *  a stack trace might be printed on standard error.
   */
  public static void errorPanel(final JFrame frame, final String title, String message, final Throwable e) {
    ErrorHandler.errorPanel(frame, title, message, e);
  }

  /** Opens a JOptionPane.ERROR_MESSAGE panel with the IGB
   *  panel as its parent, and the title "ERROR".
   */
  public static void errorPanel(String message) {
    errorPanel("ERROR", message);
  }

  /** Opens a JOptionPane.ERROR_MESSAGE panel with the IGB
   *  panel as its parent, and the title "ERROR".
   */
  public static void errorPanel(String message, Throwable e) {
    errorPanel("ERROR", message, e);
  }

  /** Shows a panel asking for the user to confirm something.
   *  @return true if the user confirms, else false.
   */
  public static boolean confirmPanel(String message) {
    IGB igb = getSingletonIGB();
    JFrame frame = (igb==null) ? null : igb.frm;
    return (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
      frame, message, "Confirm", JOptionPane.OK_CANCEL_OPTION));
  }

  public static void informPanel(String message) {
    IGB igb = getSingletonIGB();
    JFrame frame = (igb==null) ? null : igb.frm;
    JOptionPane.showMessageDialog(frame, message, "Inform", JOptionPane.INFORMATION_MESSAGE);
  }

  public void openTabInNewWindow(final JTabbedPane tab_pane) {
    int index = tab_pane.getSelectedIndex();
    if (index<0) {
      errorPanel("No more panes!");
      return;
    }
    final JComponent comp = (JComponent) tab_pane.getComponentAt(index);
    final String title = tab_pane.getTitleAt(index);
    final String tool_tip = tab_pane.getToolTipTextAt(index);
    //openCompInWindow(comp, title, tool_tip, null, tab_pane);
    openCompInWindow(comp, tab_pane);
  }

  void openCompInWindow(final JComponent comp, final JTabbedPane tab_pane) {
    PluginInfo pi = (PluginInfo) comp2plugin.get(comp);

    final String title = pi.getPluginName();
    final String display_name = pi.getDisplayName();
    final String tool_tip = comp.getToolTipText();
    Image temp_icon = null;
    if (comp instanceof IPlugin) {
      IPlugin pv = (IPlugin) comp;
      temp_icon = (Image) pv.getPluginProperty(IPlugin.TEXT_KEY_ICON);
    }
    if (temp_icon==null) { temp_icon = getIcon(); }

    // If not already open in a new window, make a new window
    if (comp2window.get(comp) == null) {
      tab_pane.remove(comp);
      tab_pane.validate();

      final JFrame frame = new JFrame(display_name);
      final Image icon = temp_icon;
      if (icon != null) { frame.setIconImage(icon); }
      final Container cont = frame.getContentPane();
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      cont.add(comp);
      comp.setVisible(true);
      comp2window.put(comp, frame);
      frame.pack(); // pack() to set frame to its preferred size

      Rectangle pos = UnibrowPrefsUtil.retrieveWindowLocation(title, frame.getBounds());
      if (pos != null) {
        UnibrowPrefsUtil.setWindowSize(frame, pos);
      }
      frame.show();
      frame.addWindowListener( new WindowAdapter() {
	  public void windowClosing(WindowEvent evt) {
            // save the current size into the preferences, so the window
            // will re-open with this size next time
            UnibrowPrefsUtil.saveWindowLocation(frame, title);
	    comp2window.remove(comp);
	    cont.remove(comp);
	    cont.validate();
	    frame.dispose();
	    tab_pane.addTab(display_name, null, comp, (tool_tip == null ? display_name : tool_tip));
            UnibrowPrefsUtil.saveComponentState(title, UnibrowPrefsUtil.COMPONENT_STATE_TAB);
            //PluginInfo.getNodeForName(title).put(PluginInfo.KEY_PLACEMENT, PluginInfo.PLACEMENT_TAB);
	  }
	});
    }
    // extra window already exists, but may not be visible
    else {
      Frame win = (Frame) comp2window.get(comp);
      boolean isShowing = win.isShowing();
      if ((win.getExtendedState() & Frame.ICONIFIED) == Frame.ICONIFIED) {
        // de-iconify it while leaving the maximized/minimized state flags alone
        win.setExtendedState(win.getExtendedState() & ~Frame.ICONIFIED);
      }
      if (! win.isShowing()) { win.show(); }
      win.toFront();
    }
    UnibrowPrefsUtil.saveComponentState(title, UnibrowPrefsUtil.COMPONENT_STATE_WINDOW);
    //PluginInfo.getNodeForName(title).put(PluginInfo.KEY_PLACEMENT, PluginInfo.PLACEMENT_WINDOW);
  }

  public void popupNotify(JPopupMenu popup,  java.util.List selected_items) {
    popup.add(popup_windowsM);
  }

  void addToPopupWindows(final JComponent comp, final String title) {
    JMenuItem popupMI = new JMenuItem(title);
    popup_windowsM.add(popupMI);
    popupMI.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent evt) {
	  Object src = evt.getSource();
	  //openCompInWindow(comp, title, tool_tip, null, tab_pane);
          openCompInWindow(comp, tab_pane);
	}
      } );
  }

  /**
   *  Determines which plugins to use based on the preferences file.
   *  Several basic plugins are turned on or off using boolean flags
   *  such as "USE_SLICE_VIEW".  The user can turn these on or off, but
   *  cannot affect their ordering.  Several of these default to "true"; others
   *  default to "false".
   *  Any other plugin can be turned on with the "&lt;plugin ... &gt;" tags.
   *  The ordering of those tags is maintained in the order of the tab panes
   *  they create.
   */
  java.util.List getPluginsFromXmlPrefs(Map prefs_hash) {
    ArrayList plugin_list = new ArrayList(16);

    boolean USE_ANNOT_BROWSER = true;
    boolean USE_SLICE_VIEW = true;
    boolean USE_GRAPH_ADJUSTER = true;
    boolean USE_PATTERN_SEARCHER = true;
    boolean USE_BOOKMARK_MANAGER = false;
    boolean USE_RESTRICTION_MAPPER = false;
    boolean USE_PIVOT_VIEW = false;

    if (prefs_hash.get("USE_GRAPH_ADJUSTER") != null) {
      USE_GRAPH_ADJUSTER = ((Boolean)prefs_hash.get("USE_GRAPH_ADJUSTER")).booleanValue(); }
    if (prefs_hash.get("USE_PIVOT_VIEW") != null) {
      USE_PIVOT_VIEW = ((Boolean)prefs_hash.get("USE_PIVOT_VIEW")).booleanValue(); }
    if (prefs_hash.get("USE_SLICE_VIEW") != null) {
      USE_SLICE_VIEW = ((Boolean)prefs_hash.get("USE_SLICE_VIEW")).booleanValue();  }
    if (prefs_hash.get("USE_RESTRICTION_MAPPER") != null) {
      USE_RESTRICTION_MAPPER = ((Boolean)prefs_hash.get("USE_RESTRICTION_MAPPER")).booleanValue(); }
    if (prefs_hash.get("USE_PATTERN_SEARCHER") != null) {
      USE_PATTERN_SEARCHER = ((Boolean)prefs_hash.get("USE_PATTERN_SEARCHER")).booleanValue(); }
    if (prefs_hash.get("USE_ANNOT_BROWSER") != null) {
      USE_ANNOT_BROWSER = ((Boolean)prefs_hash.get("USE_ANNOT_BROWSER")).booleanValue(); }
    if (prefs_hash.get("USE_BOOKMARK_MANAGER") != null) {
      USE_BOOKMARK_MANAGER = ((Boolean)prefs_hash.get("USE_BOOKMARK_MANAGER")).booleanValue(); }

    if (USE_SLICE_VIEW) {
      PluginInfo pi = new PluginInfo(AltSpliceView.class.getName(), "Sliced View", true);
      plugin_list.add(pi);
    }
    if (USE_GRAPH_ADJUSTER) {
      PluginInfo pi = new PluginInfo(GraphAdjusterView.class.getName(), "Graph Adjuster", true);
      plugin_list.add(pi);
    }
    if (USE_PATTERN_SEARCHER) {
      PluginInfo pi = new PluginInfo(SeqSearchView.class.getName(), "Pattern Search", true);
      plugin_list.add(pi);
    }
    if (USE_BOOKMARK_MANAGER) {
      PluginInfo pi = new PluginInfo(BookmarkManagerView.class.getName(), "Bookmarks", true);
      plugin_list.add(pi);
    }
    if (USE_PIVOT_VIEW) {
      PluginInfo pi = new PluginInfo(ExperimentPivotView.class.getName(), "Pivot View", true);
      plugin_list.add(pi);
    }
    if (USE_ANNOT_BROWSER) {
      PluginInfo pi = new PluginInfo(AnnotBrowserView.class.getName(), "Annotation Browser", true);
      plugin_list.add(pi);
    }
    if (USE_RESTRICTION_MAPPER) {
      PluginInfo pi = new PluginInfo(RestrictionControlView.class.getName(), "Restriction Sites", true);
      plugin_list.add(pi);
    }

    Map other_plugins = XmlPrefsParser.getNamedMap(prefs_hash, XmlPrefsParser.PLUGINS);
    Iterator iter = other_plugins.values().iterator();
    while (iter.hasNext()) {
      PluginInfo pi = (PluginInfo) iter.next();
      plugin_list.add(pi);
    }

    return plugin_list;
  }


}
