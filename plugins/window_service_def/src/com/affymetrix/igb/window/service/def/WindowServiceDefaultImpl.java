package com.affymetrix.igb.window.service.def;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.osgi.service.IGBTabPanel.TabState;
import com.affymetrix.igb.window.service.IWindowService;
import com.affymetrix.igb.window.service.def.JTabbedTrayPane.TrayState;
import com.affymetrix.igb.window.service.def.TabHolder;

public class WindowServiceDefaultImpl implements IWindowService, TabStateHandler, TrayStateChangeListener {

	private class TabStateMenuItem extends JRadioButtonMenuItem {
		private static final long serialVersionUID = 1L;
		private final TabState tabState;
		private TabStateMenuItem(final IGBTabPanel igbTabPanel, TabState _tabState) {
			super(BUNDLE.getString(_tabState.name()));
			tabState = _tabState;
		    addActionListener(
				new ActionListener() {
					TabState state = tabState;
					@Override
					public void actionPerformed(ActionEvent e) {
						setTabState(igbTabPanel, state);
					}
				}
			);
		}
		public TabState getTabState() {
			return tabState;
		}
	}

	private static final int EMBEDDED_TAB_COUNT_TOTAL = 10; // hack - this number MUST be updated if an embedded tab is added
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("window_service_def");
	private final HashMap<TabState, JMenuItem> move_tab_to_window_items;
	private final HashMap<TabState, JMenuItem> move_tabbed_panel_to_window_items;
	private JMenu tabs_menu;
	private JFrame frm;
	private Map<TabState, TabHolder> tabHolders;
	private Map<IGBTabPanel, JMenu> tabMenus;
	private Map<JMenu, Integer> tabMenuPositions;
	private Container cpane;
	private boolean tabSeparatorSet = false;
	private int embeddedTabCount = 0;

	public WindowServiceDefaultImpl() {
		super();
		move_tab_to_window_items = new HashMap<TabState, JMenuItem>();
		move_tabbed_panel_to_window_items = new HashMap<TabState, JMenuItem>();
		tabHolders = new HashMap<TabState, TabHolder>();
		tabHolders.put(TabState.COMPONENT_STATE_WINDOW, new WindowTabs(this));
		tabHolders.put(TabState.COMPONENT_STATE_HIDDEN, new HiddenTabs());
		tabMenus = new HashMap<IGBTabPanel, JMenu>();
		tabMenuPositions = new HashMap<JMenu, Integer>();
	}

	@Override
	public void setMainFrame(JFrame jFrame) {
		frm = jFrame;
		cpane = frm.getContentPane();
		cpane.setLayout(new BorderLayout());
		frm.addComponentListener(new ComponentListener()
		{
				@Override
		        public void componentResized(ComponentEvent evt) {
		    		for (TabState tabState : tabHolders.keySet()) {
		    			tabHolders.get(tabState).resize();
		    		}
		        }

				@Override
				public void componentMoved(ComponentEvent e) {}

				@Override
				public void componentShown(ComponentEvent e) {
		    		for (TabState tabState : tabHolders.keySet()) {
		    			tabHolders.get(tabState).resize();
		    		}
				}

				@Override
				public void componentHidden(ComponentEvent e) {}
		});
	}

	@Override
	public void setStatusBar(JComponent status_bar) {
		cpane.add(status_bar, BorderLayout.SOUTH);
	}

	@Override
	public void setSeqMapView(JPanel map_view) {
		JTabbedTrayPane bottom_pane = new JTabbedTrayBottomPane(map_view);
		bottom_pane.addTrayStateChangeListener(this);
		tabHolders.put(TabState.COMPONENT_STATE_BOTTOM_TAB, bottom_pane);
		try {
			bottom_pane.invokeTrayState(TrayState.valueOf(PreferenceUtils.getComponentState(bottom_pane.getTitle())));
		}
		catch (Exception x) {
			bottom_pane.invokeTrayState(TrayState.getDefaultTrayState());
		}
		JTabbedTrayPane left_pane = new JTabbedTrayLeftPane(bottom_pane);
		left_pane.addTrayStateChangeListener(this);
		tabHolders.put(TabState.COMPONENT_STATE_LEFT_TAB, left_pane);
		try {
			left_pane.invokeTrayState(TrayState.valueOf(PreferenceUtils.getComponentState(left_pane.getTitle())));
		}
		catch (Exception x) {
			left_pane.invokeTrayState(TrayState.getDefaultTrayState());
		}
		JTabbedTrayPane right_pane = new JTabbedTrayRightPane(left_pane);
		right_pane.addTrayStateChangeListener(this);
		tabHolders.put(TabState.COMPONENT_STATE_RIGHT_TAB, right_pane);
		try {
			right_pane.invokeTrayState(TrayState.valueOf(PreferenceUtils.getComponentState(right_pane.getTitle())));
		}
		catch (Exception x) {
			right_pane.invokeTrayState(TrayState.getDefaultTrayState());
		}
		cpane.add("Center", right_pane);
	}

	@Override
	public void setViewMenu(JMenu view_menu) {
		view_menu.addSeparator();
		tabs_menu = new JMenu(BUNDLE.getString("showTabs"));
		for (final TabState tabState : TabState.values()) {
			if (tabState.isTab()) {
				JMenuItem move_tab_to_window_item = new JMenuItem(MessageFormat.format(BUNDLE.getString("openCurrentTabInNewWindow"), BUNDLE.getString(tabState.name())));
				move_tab_to_window_item.addActionListener(
					new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							setTabState(tabHolders.get(tabState).getSelectedIGBTabPanel(), TabState.COMPONENT_STATE_WINDOW);
						}
					}
				);
				move_tab_to_window_item.setEnabled(false);
				tabs_menu.add(move_tab_to_window_item);
				move_tab_to_window_items.put(tabState, move_tab_to_window_item);
			}
		}
		for (final TabState tabState : TabState.values()) {
			if (tabState.isTab()) {
				JMenuItem move_tabbed_panel_to_window_item = new JMenuItem(MessageFormat.format(BUNDLE.getString("openTabbedPanesInNewWindow"), BUNDLE.getString(tabState.name())));
				move_tabbed_panel_to_window_item.addActionListener(
					new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							((JTabbedTrayPane)tabHolders.get(tabState)).invokeTrayState(TrayState.WINDOW);
						}
					}
				);
				move_tabbed_panel_to_window_item.setEnabled(false);
				tabs_menu.add(move_tabbed_panel_to_window_item);
				move_tabbed_panel_to_window_items.put(tabState, move_tabbed_panel_to_window_item);
				trayStateChanged((JTabbedTrayPane)tabHolders.get(tabState), ((JTabbedTrayPane)tabHolders.get(tabState)).getTrayState());
			}
		}
		tabs_menu.addSeparator();
		view_menu.add(tabs_menu);
	}

	/**
	 * Saves information about which plugins are in separate windows and
	 * what their preferred sizes are.
	 */
	private void saveWindowLocations() {
		// Save the main window location
		PreferenceUtils.saveWindowLocation(frm, "main window");

		for (TabHolder tabHolder : tabHolders.values()) {
			tabHolder.close();
		}
		for (IGBTabPanel comp : tabHolders.get(TabState.COMPONENT_STATE_WINDOW).getPlugins()) {
			PreferenceUtils.saveWindowLocation(comp.getFrame(), comp.getName());
		}
	}

	/**
	 * add a new tab pane to the window service
	 * @param tabPanel the tab pane
	 */
	public void addTab(final IGBTabPanel tabPanel) {
		TabState tabState = tabPanel.getDefaultState();
		try {
			tabState = TabState.valueOf(PreferenceUtils.getComponentState(tabPanel.getName()));
		}
		catch (Exception x) {}
		setTabState(tabPanel, tabState);
		JMenu pluginMenu = new JMenu(tabPanel.getDisplayName());
		tabMenus.put(tabPanel, pluginMenu);
		tabMenuPositions.put(pluginMenu, tabPanel.getPosition());
		ButtonGroup group = new ButtonGroup();

		for (TabState tabStateLoop : tabPanel.getDefaultState().getCompatibleTabStates()) {
		    JRadioButtonMenuItem menuItem = new TabStateMenuItem(tabPanel, tabStateLoop);
		    group.add(menuItem);
		    pluginMenu.add(menuItem);
		}
		setTabMenu(tabPanel);
		if (tabPanel.getPosition() == IGBTabPanel.DEFAULT_TAB_POSITION) {
			if (!tabSeparatorSet) {
				tabs_menu.addSeparator();
				tabSeparatorSet = true;
			}
			tabs_menu.add(pluginMenu);
		}
		else {
			int menuPosition = 0;
			boolean tabItemFound = false;
			while (menuPosition < tabs_menu.getItemCount() && ((tabMenuPositions.get(tabs_menu.getItem(menuPosition)) == null && !tabItemFound) || tabPanel.getPosition() > tabMenuPositions.get(tabs_menu.getItem(menuPosition)))) {
				tabItemFound |= tabMenuPositions.get(tabs_menu.getItem(menuPosition)) != null;
				menuPosition++;
			}
			tabs_menu.insert(pluginMenu, menuPosition);
		}
		// here we keep count of the embedded tabs that are added. When
		// all the embedded tabs have been added, we initialize the tab panes
		// this is to prevent the flashing of new tabs added
		if (tabPanel.isEmbedded()) {
			embeddedTabCount++;
		}
		if (embeddedTabCount == EMBEDDED_TAB_COUNT_TOTAL) {
			for (TabHolder tabHolder : tabHolders.values()) {
				tabHolder.setFocusFound();
			}
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					frm.setVisible(true);
				}
			});
		}
	}

	/**
	 * set the tab menu for a tab pane
	 * @param plugin the tab pane
	 */
	private void setTabMenu(final IGBTabPanel plugin) {
		JMenu menu = tabMenus.get(plugin);
		TabState tabState = getTabState(plugin);
		for (int i = 0; i < menu.getItemCount(); i++) {
			JMenuItem menuItem = menu.getItem(i);
			if (menuItem != null && menuItem instanceof TabStateMenuItem) {
		    	menuItem.setSelected(((TabStateMenuItem)menuItem).getTabState() == tabState);
			}
		}
	}

	/**
	 * remove a tab pane from the window service
	 * @param plugin the tab pane
	 */
	public void removeTab(final IGBTabPanel plugin) {
		for (TabState tabState : tabHolders.keySet()) {
			tabHolders.get(tabState).removeTab(plugin);
		}
		for (Component item : Arrays.asList(tabs_menu.getMenuComponents())) {
			if (item instanceof JMenuItem && ((JMenuItem)item).getText().equals(plugin.getDisplayName())) {
				tabs_menu.remove(item);
			}
		}
		PreferenceUtils.saveComponentState(plugin.getName(), null);
	}

	/**
	 * set a given tab pane to a given tab state
	 * @param panel the tab pane
	 * @param tabState the new tab state
	 */
	private void setTabState(IGBTabPanel panel, TabState tabState) {
		if (panel == null) {
			return;
		}
		TabState oldTabState = getTabState(panel);
		if (oldTabState != null) {
			tabHolders.get(oldTabState).removeTab(panel);
		}
		if (tabState == null) {
			removeTab(panel);
		}
		else {
			tabHolders.get(tabState).addTab(panel);
		}
		PreferenceUtils.saveComponentState(panel.getName(), tabState.name());
	}

	@Override
	public void setTabStateAndMenu(IGBTabPanel panel, TabState tabState) {
		setTabState(panel, tabState);
		setTabMenu(panel);
	}

	@Override
	public void startup() {}

	@Override
	public void shutdown() {
		saveWindowLocations();
	}

	@Override
	public void setDefaultState(IGBTabPanel panel) {
		setTabState(panel, panel.getDefaultState());
		setTabMenu(panel);
	}

	/**
	 * get the tab state of a given tab pane
	 * @param panel the tab pane
	 * @return the tab state of the give pane
	 */
	private TabState getTabState(IGBTabPanel panel) {
		for (TabState tabState : tabHolders.keySet()) {
			if (tabHolders.get(tabState).getPlugins().contains(panel)) {
				return tabState;
			}
		}
		return null;
	}

	@Override
	public Set<IGBTabPanel> getPlugins() {
		HashSet<IGBTabPanel> plugins = new HashSet<IGBTabPanel>();
		for (TabState tabState : tabHolders.keySet()) {
			plugins.addAll(tabHolders.get(tabState).getPlugins());
		}
		return plugins;
	}

	@Override
	public void trayStateChanged(JTabbedTrayPane trayPane, TrayState trayState) {
		for (JMenuItem menuItem : new JMenuItem[]{move_tab_to_window_items.get(trayPane.getTabState()), move_tabbed_panel_to_window_items.get(trayPane.getTabState())}) {
			if (menuItem != null) {
				menuItem.setEnabled(trayState != TrayState.HIDDEN && trayState != TrayState.WINDOW);
			}
		}
	}
}
