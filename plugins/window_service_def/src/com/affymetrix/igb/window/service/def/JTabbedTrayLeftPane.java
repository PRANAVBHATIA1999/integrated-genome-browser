package com.affymetrix.igb.window.service.def;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import com.affymetrix.igb.osgi.service.TabState;

public class JTabbedTrayLeftPane extends JTabbedTrayHorizontalPane {
	private static final long serialVersionUID = 1L;
	private static final double LEFT_DIVIDER_PROPORTIONAL_LOCATION = 0.30;

	public JTabbedTrayLeftPane(JComponent _baseComponent) {
		super(TabState.COMPONENT_STATE_LEFT_TAB, _baseComponent, JTabbedPane.LEFT, JSplitPane.HORIZONTAL_SPLIT, LEFT_DIVIDER_PROPORTIONAL_LOCATION);
		setRightComponent(_baseComponent);
		setDividerLocation(0);
	}

	@Override
	protected int getFullSize() {
		return getWidth();
	}

	@Override
	protected int getRetractDividerLocation() {
		int index = tab_pane.getSelectedIndex() < 0 ? 0 : tab_pane.getSelectedIndex();
		return tab_pane.getWidth() - tab_pane.getComponentAt(index).getWidth();
	}

	@Override
	protected int getHideDividerLocation() {
		return 0;
	}

	@Override
	protected void setTabComponent() {
		setLeftComponent(tab_pane);
	}
}
