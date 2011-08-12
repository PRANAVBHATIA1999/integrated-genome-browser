package com.affymetrix.igb.osgi.service;

import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.event.GenericServerInitListener;
import com.affymetrix.genometryImpl.event.RepositoryChangeListener;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.genometryImpl.operator.graph.GraphOperator;
import com.affymetrix.genoviz.bioviews.Glyph;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.View;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenu;
import com.affymetrix.genoviz.widget.NeoAbstractWidget;
import com.affymetrix.igb.osgi.service.IGBTabPanel.TabState;

/**
 * OSGi Service to allow bundles indirect access to IGB internals.
 * 
 */
public interface IGBService {
	public static final String UTF8 = "UTF-8";
	/**
	 * A potential parameter in either the URL of a bookmark, or a command-line option.  This allows a response file to be loaded, instead of anything else.
	 */
	public final static String SCRIPTFILETAG = "scriptfile";
	public static final String IGB_TIER_HEADER = "IGB-Tier";
	// service extension point names
	public static final String GRAPH_TRANSFORMS = "igb.graph.transform";
	public static final String TAB_PANELS = "igb.tab_panels";
	public static final String SYM_FEATURE_URL = "feature_url_";

	public final static boolean DEBUG_EVENTS = false;
	/**
	 * add a menu to the main IGB menu
	 * @param menu the JMenu to add to the main IGB menu
	 * @return true if the menu was added, false otherwise
	 */
	public boolean addMenu(JRPMenu menu);
	/**
	 * remove a menu from the main IGB menu
	 * @param menuName the name of the JMenu to remove from the main IGB menu
	 * @return true if the menu was removed, false otherwise
	 */
	public boolean removeMenu(String menuName);
	/**
	 * Add a lockedUp message to the list of locked messages and display with
	 * a little progress bar so that the app doesn't look locked up.
	 * @param s text of the message
	 */
	public void addNotLockedUpMsg(String message);
	/**
	 * Remove a lockedUp message from the list of locked messages and undisplay it
	 * @param s text of the message
	 */
	public void removeNotLockedUpMsg(String message);
	/**
	 *  Sets the text in the status bar.
	 *  Will optionally echo a copy of the string to System.out.
	 *  It is safe to call this method even if the status bar is not being displayed.
	 *  @param echo  Whether to echo a copy to System.out.
	 */
	public void setStatus(String message);
	/**
	 * Shows a panel asking for the user to confirm something.
	 *
	 * @param message the message String to display to the user
	 * @return true if the user confirms, else false.
	 */
	public boolean confirmPanel(String text);
	/**
	 * get the specified icon
	 * @param name of the icon
	 * @return the specified icon
	 */
	public ImageIcon getIcon(String name);
	/**
	 * add a routine to the list of routines that will run when
	 * the program shuts down
	 * @param routine the routine to run
	 */
	public void addStopRoutine(IStopRoutine routine);
	/**
	 * get the file menu of the application
	 * @return the file menu of the IGB application
	 */
	public JRPMenu getFileMenu();
	/**
	 * get the view menu of the application
	 * @return the view menu of the IGB application
	 */
	public JRPMenu getViewMenu();
	// for plugins page
	/**
	 * get the list of all repositories from the 
	 * Bundle Repository Preferences tab
	 * @return the list of bundle repositories (URLs)
	 */
	public List<String> getRepositories();
	/**
	 * mark a bundle repository as down / unavailable
	 * due to an error trying to connect
	 * @param url the URL to mark
	 */
	public void failRepository(String url);
	/**
	 * display the Bundle Repository tab of the Preferences page
	 */
	public void displayRepositoryPreferences();
	/**
	 * add a RepositoryChangeListener, to be called when there
	 * is a change to the Bundle Repositories on the Bundle
	 * Repository tab of the Preferences page
	 * @param repositoryChangeListener the listener
	 */
	public void addRepositoryChangeListener(RepositoryChangeListener repositoryChangeListener);
	/**
	 * remove a RepositoryChangeListener - so that is is no longer
	 * called for changes to the Bundle Repository tab of
	 * the Perferences page
	 * @param repositoryChangeListener the listener
	 */
	public void removeRepositoryChangeListener(RepositoryChangeListener repositoryChangeListener);
	/**
	 * Returns the value of the argument indicated by label.
	 * If arguments are
	 *   "-flag_2 -foo bar", then get_arg("foo", args)
	 * returns "bar", get_arg("flag_2") returns a non-null string,
	 * and get_arg("flag_5") returns null.
	 * @param label label to search for
	 * @param args the args to search
	 * @return the value found, or "true" if found with no value, or
	 * null if not found
	 */
	public String get_arg(String label, String[] args);

	// for BookMark
	/**
	 * get the constant value for AppName
	 * @return the constant value for the AppName
	 */
	public String getAppName();
	/**
	 * get the constant value for AppVersion
	 * @return the constant value for the AppVersion
	 */
	public String getAppVersion();
	public void loadAndDisplaySpan(final SeqSpan span, final GenericFeature feature);
	public void updateGeneralLoadView();
	public void updateDependentData();
	public void doActions(final String batchFileStr);
	public void doSingleAction(String line);
	public void performSelection(String selectParam);
	public void loadResidues(int start, int end);
	public GenericFeature getFeature(GenericServer gServer, String feature_url);
	public GenericServer loadServer(String server_url);
	public AnnotatedSeqGroup determineAndSetGroup(final String version);
	public Color getDefaultBackgroundColor();
	public Color getDefaultForegroundColor();
	/**
	 * set the state of the given tab to the given state and update
	 * the view menu to the new value
	 * @param igbTabPanel the tab to change
	 * @param tabState the new state
	 */
	public void setTabStateAndMenu(IGBTabPanel igbTabPanel, TabState tabState);
	// for RestrictionSites/SearchView
	/**
	 * get a count of the number of hits that match the specified regular
	 * expression and mark the in the Seq Map View
	 * @param forward - true = forward search, false = reverse search
	 * @param regex - the regular expression to match
	 * @param residues the residues to search
	 * @param residue_offset the starting offset within the residues
	 * @param glyphs the glyphs to mark
	 * @param hitColor the color to mark them with
	 * @return
	 */
	public int searchForRegexInResidues(
			boolean forward, Pattern regex, String residues, int residue_offset, List<GlyphI> glyphs, Color hitColor);
	// for RestrictionSites
	public void updateWidget();
	public void removeItem(List<GlyphI> vec);
	// for SearchView
	public void zoomToCoord(String seqID, int start, int end);
	public void mapRefresh(List<GlyphI> glyphs);
	public GlyphI getItem(SeqSymmetry sym);
	public void removeItem(GlyphI gl);
	public void select(GlyphI g);
	public void deselect(GlyphI g);
	public void clearSelected();
	/**
	 * get the SeqMapViewI, the main window for IGB
	 * @return the SeqMapViewI
	 */
	public SeqMapViewI getSeqMapView();
	// for GeneralLoadView
	/**
	 * get the string entered for command line batch file
	 * @return the Command Line Batch File String
	 */
	public String getCommandLineBatchFileStr();
	/**
	 * set the string for the command line batch file
	 * @param str the String
	 */
	public void setCommandLineBatchFileStr(String str);
	// for SearchView
	/**
	 * get the constant value for GenomeSeqId
	 * @return the constant value for the GenomeSeqId
	 */
	public String getGenomeSeqId();
	public boolean loadResidues(final SeqSpan viewspan, final boolean partial);
	// for Graph Adjuster
	/**
	 * get the main JFrame for the application
	 * @return the main JFrame for the IGB instance
	 */
	public JFrame getFrame();
	/**
	 * save the current state of the application
	 */
	public void saveState();
	/**
	 * load the current state of the application
	 */
	public void loadState();
	public IGBTabPanel getView(String className);
	/**
	 * select the given tab in the tab panel, bringing it to the front
	 * @param panel the IGBTabPanel
	 */
	public void selectTab(IGBTabPanel panel);
	public NeoAbstractWidget getGraphCurrentSource();
	public void packGlyph(GlyphI glyph);
	public void deleteGlyph(GlyphI glyph);
	public void packMap(boolean fitx, boolean fity);
	public View getView();
	public List<GlyphI> getItems(GraphSym graf);
	public boolean isMainSrc(Object src);
	public void setTrackStyle(String meth, Color col, String description);
	public boolean doOperateGraphs(GraphOperator operator, List<? extends GlyphI> graph_glyphs);
	// for plugins
	public List<Glyph> getAllTierGlyphs();
	public void addSeqMapMouseListener(MouseListener mouseListener);
	public void addSeqMapMouseMotionListener(MouseMotionListener mouseMotionListener);

	// ServerList
	public boolean areAllServersInited();
	public void addServerInitListener(GenericServerInitListener listener);
	public void removeServerInitListener(GenericServerInitListener listener);
	public GenericServer getServer(String URLorName);

	//
	public void addMisMatchTier(GlyphI atier, String prefix);

}
