package com.affymetrix.igb.window.service;

import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JMenuBar;

import com.affymetrix.igb.service.api.IgbTabPanel;
import com.affymetrix.igb.service.api.IgbTabPanelI;
import com.affymetrix.igb.service.api.IgbTabPanelI.TabState;

public interface IWindowService {
	/**
	 * run when the Window Service starts
	 */
	public void startup();
	/**
	 * run when the Window Service stops - when the program exits
	 */
	public void shutdown();
	/**
	 * save the state of the window
	 */
	public void saveState();
	/**
	 * restore the state of the window
	 */
	public void restoreState();
	/**
	 * pass in the main frame of the application (JFrame for Swing)
	 * @param jFrame the main frame of the application
	 */
	public void setMainFrame(JFrame jFrame);
	/**
	 * pass in the SeqMapView, this is the main IGB view
	 * @param jPanel the JPanel that contains the main IGB view
	 */
	public void setSeqMapView(JPanel jPanel);
	public void setMenuCreator(IMenuCreator menuCreator);
	/**
	 * pass in the view menu
	 * @param view_menu the view menu
	 */
	public void setTabsMenu(JMenuBar mbar);
	/**
	 * pass in the status bar of the application, this is where
	 * message and some icons are displayed
	 * @param status_bar the status bar
	 */
	public void setStatusBar(JComponent status_bar);
	/**
	 * pass in the tool bar of the application, this is where
	 * action icons are displayed
	 * @param tool_bar the tool bar
	 */
	public void setToolBar(JToolBar tool_bar);
	/**
	 * pass in the top component1 (above topComponent2)
	 * a generic component at the top of IGB
	 * @param topComponent1 the top component
	 */
	public void setTopComponent1(JComponent topComponent1);
	/**
	 * pass in the top component2 (below topComponent1)
	 * a generic component at the top of IGB
	 * @param topComponent2 the top component
	 */
	public void setTopComponent2(JComponent topComponent2);
	/**
	 * get all the tab panels that have been added
	 * @return the set of tab panels added
	 */
	public Set<IgbTabPanel> getPlugins();
	/**
	 * set the state of the given tab to the given state and update
	 * the view menu to the new value
	 * @param panel the tab to change
	 * @param tabState the new state
	 */
	public void setTabStateAndMenu(IgbTabPanelI panel, TabState tabState);
	/**
	 * select the given tab in the tab panel, bringing it to the front
	 * @param panel the IGBTabPanel
	 */
	public void selectTab(IgbTabPanel panel);
}
