package com.affymetrix.igb.osgi.service;

import java.awt.Rectangle;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class IGBTabPanel extends JPanel implements Comparable<IGBTabPanel> {
	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_TAB_POSITION = Integer.MAX_VALUE - 1;

	/**
	 * the current state of the tab
	 */
	public enum TabState {
		COMPONENT_STATE_LEFT_TAB(true, true),
		COMPONENT_STATE_RIGHT_TAB(true, true),
		COMPONENT_STATE_BOTTOM_TAB(true, false),
		COMPONENT_STATE_WINDOW(false, false),
		COMPONENT_STATE_HIDDEN(false, false);

		private final boolean tab;
		private final boolean portrait;

		TabState(boolean tab, boolean portrait) {
			this.tab = tab;
			this.portrait = portrait;
		}

		/**
		 * this state is a tab (left, right or botton)
		 * @return true if this state is a tab, false for hidden or
		 * windowed
		 */
		public boolean isTab() {
			return tab;
		}

		/**
		 * get the default tab state
		 * @return the default tab state
		 */
		public static TabState getDefaultTabState() {
			return COMPONENT_STATE_BOTTOM_TAB;
		}

		/**
		 * get the list of all tab states that the user can change
		 * the tab - depends on the initial tab state of the tab.
		 * @return a list of all compatible tab states
		 */
		public List<TabState> getCompatibleTabStates() {
			List<TabState> compatibleTabStates = new ArrayList<TabState>();
			for (TabState tabState : TabState.values()) {
				if (portrait == tabState.portrait || !isTab() || !tabState.isTab()) {
					compatibleTabStates.add(tabState);
				}
			}
			return compatibleTabStates;
		}
	}

	protected final IGBService igbService;
	private final String displayName;
	private final String title;
	private final boolean focus;
	private final int position;
	private JFrame frame;
	private Rectangle trayRectangle;

	public IGBTabPanel(IGBService igbService, String displayName, String title, boolean main) {
		this(igbService, displayName, title, main, DEFAULT_TAB_POSITION);
	}
	
	protected IGBTabPanel(IGBService igbService, String displayName, String title, boolean focus, int position) {
		super();
		this.igbService = igbService;
		this.displayName = displayName;
		this.title = title;
		this.focus = focus;
		this.position = position;
	}

	@Override
	public String getName() {
		return getClass().getName();
	}

	/**
	 * get the name to display to the user
	 * @return the name to display to the user (on the tab)
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * get the title of the tab panel
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * if this tab should be the tab selected when IGB starts
	 * @return true if this tab panel should get initial
	 * selection/focus, false otherwise
	 */
	public boolean isFocus() {
		return focus;
	}

	/**
	 * get the position of the tab in the tray
	 * @return the tab position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * get the default / initial state of this tab panel
	 * @return the default state of this tab
	 */
	public TabState getDefaultState() {
		return TabState.COMPONENT_STATE_BOTTOM_TAB;
	}

	/**
	 * get the main Frame for this panel (only applies when
	 * a separate window - tab state WINDOW)
	 * @return the JFrame of this tab panel
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * set the main Frame for this panel (only applies when
	 * a separate window - tab state WINDOW)
	 * @param the JFrame of this tab panel
	 */
	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	@Override
	public String toString() {
		return "IGBTabPanel: " + "displayName = " + displayName + ", class = " + this.getClass().getName();
	}

	/** Returns the icon stored in the jar file.
	 *  It is expected to be at com.affymetrix.igb.igb.gif.
	 *  @return null if the image file is not found or can't be opened.
	 */
	public Icon getIcon() {
		ImageIcon icon = null;
		try {
			URL url = IGBTabPanel.class.getResource("igb.gif");
			if (url != null) {
				icon = new ImageIcon(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// It isn't a big deal if we can't find the icon, just return null
		}
		return icon;
	}

	/**
	 * specify if this is an embedded tab (included in the IGB distribution).
	 * DO NOT override this value, unless you are creating a tab that
	 * will be included in the real IGB distribution. If you are not
	 * sure, then leave as is (false).
	 * @return true if this is an embedded tab panel, false otherwise
	 */
	public boolean isEmbedded() {
		return false;
	}

	/**
	 * there are some tabs that, when they are moved to a separate popup
	 * window, they are too small. For those tabs, we handle them specially,
	 * so that the size is OK.
	 * @return if this tab needs to have the minimum size checked
	 */
	public boolean isCheckMinimumWindowSize() {
		return false;
	}

	public final Rectangle getTrayRectangle() {
		return trayRectangle;
	}

	public final void setTrayRectangle(Rectangle tabRectangle) {
		this.trayRectangle = tabRectangle;
	}

	@Override
	public int compareTo(IGBTabPanel o) {
		int ret = Integer.valueOf(position).compareTo(o.position);

		if(ret != 0)
			return ret;

		return this.getDisplayName().compareTo(o.getDisplayName());
	}

	public void saveSession() {}

	public void loadSession() {}
}
