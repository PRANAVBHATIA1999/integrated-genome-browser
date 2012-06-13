/**
 * Copyright (c) 2006 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.trackAdjuster;

import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import java.util.*;
import javax.swing.GroupLayout;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneUI.BasicHorizontalLayoutManager;

/**
 * TrackAdjusterTab consists of two GUIBuilder designed panels, the
 * TrackPreferencesGUI, which is a common Panel, and the YScaleAxisGUI
 * used only by TrackAdjuster. Since TrackPreferencesGUI is common, it
 * does not handle user actions, the TrackAdjusterA handles them. The
 * YScaleAxisGUI handles it's own actions.
 */
public final class TrackAdjusterTab extends IGBTabPanel {
	private static final long serialVersionUID = 1L;
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("trackAdjuster");
	private static final int TAB_POSITION = 3;

	public TrackAdjusterTab(IGBService _igbService) {
		super(_igbService, BUNDLE.getString("trackAdjusterTab"), BUNDLE.getString("trackAdjusterTab"), false, TAB_POSITION);
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		TrackPreferencesSeqMapViewPanel trackPanel = new TrackPreferencesSeqMapViewPanel(igbService);
		YScaleAxisGUI yAxisPanel= new YScaleAxisGUI(igbService);
		add(trackPanel);
	    add(yAxisPanel);
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addComponent(trackPanel)
				.addComponent(yAxisPanel)
				);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(trackPanel)
				.addComponent(yAxisPanel))
				);
	}
}
