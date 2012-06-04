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

import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.*;
import com.affymetrix.genometryImpl.operator.DepthOperator;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.style.GraphType;
import com.affymetrix.genometryImpl.style.HeatMap;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.style.SimpleTrackStyle;
import com.affymetrix.genometryImpl.symmetry.MisMatchGraphSym;
import com.affymetrix.genometryImpl.symmetry.RootSeqSymmetry;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genoviz.bioviews.Glyph;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.color.ColorScheme;
import com.affymetrix.genoviz.color.ColorSchemeComboBox;
import com.affymetrix.genoviz.swing.recordplayback.*;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.*;
import com.jidesoft.combobox.ColorComboBox;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public final class TrackAdjusterTab
		implements SeqSelectionListener, SymSelectionListener, TrackstylePropertyMonitor.TrackStylePropertyListener, ListSelectionListener {

	//System.out.println() statements do not show on the screen, they are not translated.
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("trackAdjuster");
	private static final String SELECT_ALL_PROMPT = "Select All";
	private static final String SELECT_ALL_SUFFIX = " tracks";
	private static final String SELECT_ALL_ITEM = "";
	private static final String SELECT_NONE_ITEM = "no";
	private static TrackAdjusterTab singleton;
	private static final Map<GraphType, String> graphType2ViewMode = new EnumMap<GraphType, String>(GraphType.class);

	static {
		graphType2ViewMode.put(GraphType.BAR_GRAPH, "bargraph");
		graphType2ViewMode.put(GraphType.DOT_GRAPH, "dotgraph");
		graphType2ViewMode.put(GraphType.FILL_BAR_GRAPH, "fillbargraph");
		graphType2ViewMode.put(GraphType.HEAT_MAP, "heatmapgraph");
		graphType2ViewMode.put(GraphType.LINE_GRAPH, "linegraph");
		graphType2ViewMode.put(GraphType.MINMAXAVG, "minmaxavggraph");
		graphType2ViewMode.put(GraphType.STAIRSTEP_GRAPH, "stairstepgraph");
	}

	enum DisplayType {

		ANNOTATION,
		GRAPH,
		AUTO,
		PLUGIN;
	}

//	private BioSeq current_seq;
	private GenometryModel gmodel;
	private GenericAction annotationDepthTransformAction = new TransformAction(new DepthOperator(FileTypeCategory.Annotation));
	private GenericAction alignmentDepthTransformAction = new TransformAction(new DepthOperator(FileTypeCategory.Alignment));
	public boolean is_listening = true; // used to turn on and off listening to GUI events
	public GraphVisibleBoundsSetter vis_bounds_setter;
	boolean DEBUG_EVENTS = false;
	private static final String[] SELECT_ALL_ITEMS = new String[FileTypeCategory.values().length + 3];
	static {
		SELECT_ALL_ITEMS[0] = SELECT_ALL_PROMPT;
		SELECT_ALL_ITEMS[1] = SELECT_ALL_ITEM + SELECT_ALL_SUFFIX;
		SELECT_ALL_ITEMS[2] = SELECT_NONE_ITEM + SELECT_ALL_SUFFIX;
		for (int i = 0; i < FileTypeCategory.values().length; i++) {
			SELECT_ALL_ITEMS[i + 3] = FileTypeCategory.values()[i].name() + SELECT_ALL_SUFFIX;
		}
	}
	public ComboBoxModel selectAllCBModel = new DefaultComboBoxModel(SELECT_ALL_ITEMS);
	public JRPComboBox selectAllCB = new JRPComboBox("TrackAdjusterTab_selectAllCB");
	public JRPRadioButton graphP_mmavgB = new JRPRadioButton("TrackAdjusterTab_graphP_mmavgB", BUNDLE.getString("minMaxAvgButton"));
	public JRPRadioButton graphP_lineB = new JRPRadioButton("TrackAdjusterTab_graphP_lineB", BUNDLE.getString("lineButton"));
	public JRPRadioButton graphP_barB = new JRPRadioButton("TrackAdjusterTab_graphP_barB", BUNDLE.getString("barButton"));
	public JRPRadioButton graphP_dotB = new JRPRadioButton("TrackAdjusterTab_graphP_dotB", BUNDLE.getString("dotButton"));
	public JRPRadioButton graphP_hmapB = new JRPRadioButton("TrackAdjusterTab_graphP_hmapB");
//	public JRPRadioButton graphP_sstepB = new JRPRadioButton("TrackAdjusterTab_sstepB", BUNDLE.getString("stairStepButton"));
	public JRPRadioButton graphP_hidden_styleB = new JRPRadioButton("TrackAdjusterTab_graphP_hidden_styleB", BUNDLE.getString("hiddenStyleButton")); // this button will not be displayed
	public JRPRadioButton annotationDisplayAsB = new JRPRadioButton("TrackAdjusterTab_annotationDisplayAsB", BUNDLE.getString("AnnotationButton"));
	public JRPRadioButton graphDisplayAsB = new JRPRadioButton("TrackAdjusterTab_graphDisplayAsB", BUNDLE.getString("graphButton"));
	public JRPRadioButton autoDisplayAsB = new JRPRadioButton("TrackAdjusterTab_autoDisplayAsB", BUNDLE.getString("AutoButton"));
	public JRPRadioButton pluginDisplayAsB = new JRPRadioButton("TrackAdjusterTab_pluginDisplayAsB");
	public ButtonGroup stylegroup = new ButtonGroup();
	public ButtonGroup displayGroup = new ButtonGroup();
	public JPanel stylePanel = new JPanel();
	public JPanel rangePanel = new JPanel();
	public JPanel graphPanel = new JPanel();
	public JPanel annotationPanel = new JPanel();
	public ColorComboBox styleP_fgColorComboBox = new ColorComboBox();
	public ColorComboBox styleP_bgColorComboBox = new ColorComboBox();
	public ColorComboBox styleP_labelFGComboBox = new ColorComboBox();
	public final ColorSchemeComboBox styleP_colorSchemeBox;
	public JRPTextField annotP_maxStackDepthTextField = new JRPNumTextField("TrackAdjusterTab_annotP_maxStackDepthTextField");
	public JRPTextField trackName = new JRPTextField("TrackAdjusterTab_track_name");
	public static final Object[] SUPPORTED_SIZE = {8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
	public JRPComboBox styleP_trackNameSizeComboBox = new JRPComboBox("TrackAdjusterTab_styleP_trackNameSizeComboBox");
	public JRPSlider height_slider = new JRPSlider("TrackAdjusterTab_height_slider", JSlider.HORIZONTAL, 10, 500, 50);
	public final List<RootSeqSymmetry> rootSyms = new ArrayList<RootSeqSymmetry>();
	public final List<RootSeqSymmetry> graphSyms = new ArrayList<RootSeqSymmetry>();
	public final List<RootSeqSymmetry> annotSyms = new ArrayList<RootSeqSymmetry>();
	public final List<TierGlyph> selectedTiers = new ArrayList<TierGlyph>();
	public final List<ViewModeGlyph> allGlyphs = new ArrayList<ViewModeGlyph>();
	public final List<AbstractGraphGlyph> graphGlyphs = new ArrayList<AbstractGraphGlyph>();
	public final JRPCheckBox graphP_labelCB = new JRPCheckBox("TrackAdjusterTab_graphP_labelCB", BUNDLE.getString("labelCheckBox"));
	public final JRPCheckBox graphP_yaxisCB = new JRPCheckBox("TrackAdjusterTab_graphP_yaxisCB", BUNDLE.getString("yAxisCheckBox"));
	public final JRPCheckBox floatCB = new JRPCheckBox("TrackAdjusterTab_floatCB", BUNDLE.getString("floatingCheckBox"));
	private IGBService igbService;
	public JRPComboBox graphP_heat_mapCB;

	public static void init(IGBService igbService) {
		singleton = new TrackAdjusterTab(igbService);
	}

	public static synchronized TrackAdjusterTab getSingleton() {
		return singleton;
	}

	public TrackAdjusterTab(IGBService igbS) {
		igbService = igbS;
		
		graphP_heat_mapCB = new JRPComboBox("TrackAdjusterTab_heat_mapCB", HeatMap.getStandardNames());
		styleP_trackNameSizeComboBox.setModel(new DefaultComboBoxModel(SUPPORTED_SIZE));
		graphP_heat_mapCB.addItemListener(new HeatMapItemListener());

		graphP_barB.addActionListener(new GraphStyleSetter(GraphType.BAR_GRAPH));
		graphP_dotB.addActionListener(new GraphStyleSetter(GraphType.DOT_GRAPH));
		graphP_hmapB.addActionListener(new GraphStyleSetter(GraphType.HEAT_MAP)); //will need to be re-written to function for plugins
		graphP_lineB.addActionListener(new GraphStyleSetter(GraphType.LINE_GRAPH));
		graphP_mmavgB.addActionListener(new GraphStyleSetter(GraphType.MINMAXAVG));
//		graphP_sstepB.addActionListener(new GraphStyleSetter(GraphType.STAIRSTEP_GRAPH));

		stylegroup.add(graphP_barB);
		stylegroup.add(graphP_dotB);
		stylegroup.add(graphP_hmapB);
		stylegroup.add(graphP_lineB);
		stylegroup.add(graphP_mmavgB);
//		stylegroup.add(graphP_sstepB);
		stylegroup.add(graphP_hidden_styleB); // invisible button

		displayGroup.add(annotationDisplayAsB);
		displayGroup.add(graphDisplayAsB);
		displayGroup.add(autoDisplayAsB);
		displayGroup.add(pluginDisplayAsB);


		final ItemListener itemListener = new ItemListener() {

			public void itemStateChanged(ItemEvent ie) {
				switch (ie.getStateChange()) {
					case ItemEvent.DESELECTED:
						break;
					case ItemEvent.SELECTED:
						Object o = ie.getSource();
						if (o instanceof ColorSchemeComboBox) {
							ColorSchemeComboBox csb = (ColorSchemeComboBox) o;
							ColorScheme s = (ColorScheme) csb.getSelectedItem();
							ColorSchemeAction.getAction().tempAction(s);
						}
						break;
					default:
						System.err.println(
								"SchemeChoser.$ItemListener.itemStateChanged: Unexpected state change: "
								+ ie.getStateChange());
				}
			}
		};
		styleP_colorSchemeBox = new ColorSchemeComboBox() {
			private static final long serialVersionUID = 1L;

			@Override
			public void setChoices(int i) {
				this.removeItemListener(itemListener);
				super.setChoices(i);
				this.addItemListener(itemListener);
			}
		};
		styleP_colorSchemeBox.addItemListener(itemListener);
		styleP_colorSchemeBox.setChoices(0);
		igbS.addListSelectionListener(styleP_colorSchemeBox);

		graphP_hidden_styleB.setSelected(true); // deselect all visible radio buttons

		vis_bounds_setter = new GraphVisibleBoundsSetter(igbService.getSeqMap());


		refreshSelection(Collections.<RootSeqSymmetry>emptyList());

		gmodel = GenometryModel.getGenometryModel();
		gmodel.addSeqSelectionListener(this);
		gmodel.addSymSelectionListener(this);
		TrackstylePropertyMonitor.getPropertyTracker().addPropertyListener(this);
		igbService.addListSelectionListener(this);

		floatCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (floatCB.isSelected()) {
					GenericAction floatAction = GenericActionHolder.getInstance().getGenericAction("com.affymetrix.igb.action.FloatTiersAction");
					if (floatAction != null) {
						floatAction.actionPerformed(null);
					}
				}
				else {
					GenericAction unFloatAction = GenericActionHolder.getInstance().getGenericAction("com.affymetrix.igb.action.UnFloatTiersAction");
					if (unFloatAction != null) {
						unFloatAction.actionPerformed(null);
					}
				}
			}
		});

		graphP_labelCB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setShowLabels(graphP_labelCB.isSelected());
			}
		});

		graphP_yaxisCB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setShowAxis(graphP_yaxisCB.isSelected());
			}
		});
	}

	public void selectAllCBSelected() {
		String displayItem = (String)selectAllCB.getSelectedItem();
		if (SELECT_ALL_PROMPT.equals(displayItem)) {
			return;
		}
		String item = displayItem.substring(0, displayItem.length() - SELECT_ALL_SUFFIX.length());
		if (SELECT_ALL_ITEM.equals(item)) {
			SelectAllAction.getAction().execute();
		}
		else if (SELECT_NONE_ITEM.equals(item)) {
			DeselectAllAction.getAction().execute();
		}
		else {
			SelectAllAction.getAction(FileTypeCategory.valueOf(item)).execute();
		}
		selectAllCB.setSelectedIndex(0);
	}

	public boolean isTierGlyph(GlyphI glyph) {
		return glyph instanceof TierGlyph;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() instanceof TierGlyph) {
			if (((TierGlyph) e.getSource()).isSelected()) {
				System.out.println("add me");
			} else {
				System.out.println("Remove Me");
			}
		}
	}

	public void symSelectionChanged(SymSelectionEvent evt) {
		List<RootSeqSymmetry> selected_syms = evt.getAllSelectedSyms();
		// Only pay attention to selections from the main SeqMapView or its map.
		// Ignore the splice view as well as events coming from this class itself.

		Object src = evt.getSource();
		if (!(src == igbService.getSeqMapView() || src == igbService.getSeqMap())
				|| igbService.getSeqMap() == null || igbService.getSeqMapView() == null) {
			return;
		}

		refreshSelection(selected_syms);
	}

	private void refreshSelection(List<RootSeqSymmetry> selected_syms) {
		is_listening = false; // turn off propagation of events from the GUI while we modify the settings
		int symcount = selected_syms.size();
		collectSymsAndGlyphs(selected_syms, symcount);
		@SuppressWarnings("unchecked")
		List<TierGlyph> tierList = (List<TierGlyph>) igbService.getSeqMapView().getSelectedTiers();
		if (selectedTiers != tierList) {
			selectedTiers.clear();
			for (TierGlyph tier : tierList) {
				if (!selectedTiers.contains(tier)) {
					selectedTiers.add(tier);
				}
			}
		}
		resetMainPanel();
		resetStylePanel();
		resetAnnotationPanel();
		resetGraphAndRangePanel();
		is_listening = true; // turn back on GUI events
	}

	private void enablePanel(JPanel panel, boolean enabled) {
		panel.setEnabled(enabled);
		for (Component c : panel.getComponents()) {
			c.setEnabled(enabled);
		}
	}

	private void resetMainPanel() {
		// track name, view mode
		int selectedTrackCount = selectedTiers.size();
		boolean select = selectedTrackCount > 0;
		annotationDisplayAsB.setSelected(false);
		graphDisplayAsB.setSelected(false);
		autoDisplayAsB.setSelected(false);
		pluginDisplayAsB.setSelected(false);
		if (selectedTrackCount == 1) {
			ITrackStyleExtended style = selectedTiers.get(0).getAnnotStyle();
			RootSeqSymmetry rootSym = (RootSeqSymmetry)selectedTiers.get(0).getInfo();
			FileTypeCategory category = rootSym.getCategory();
			if (style == null) {
				return;
			}
			trackName.setText(style.getTrackName());
			String viewmode = style.getViewMode();
			if ("annotation".equals(viewmode) || "alignment".equals(viewmode)) {
				annotationDisplayAsB.setSelected(true);
			}
			else if (viewmode.indexOf("semantic") > -1) {
				autoDisplayAsB.setSelected(true);
			}
			else if (graphType2ViewMode.values().contains(viewmode)) {
				graphDisplayAsB.setSelected(true);
			}
			annotationDisplayAsB.setEnabled(category == FileTypeCategory.Annotation || category == FileTypeCategory.Alignment);
//			graphDisplayAsB.setEnabled(category == FileTypeCategory.Graph || category == FileTypeCategory.Annotation || category == FileTypeCategory.Alignment);
			graphDisplayAsB.setEnabled(category == FileTypeCategory.Graph);
			autoDisplayAsB.setEnabled(category == FileTypeCategory.Annotation || category == FileTypeCategory.Alignment);
		}
		else {
			trackName.setText("");
			annotationDisplayAsB.setEnabled(false);
			graphDisplayAsB.setEnabled(false);
			autoDisplayAsB.setEnabled(false);
		}

		if (select) {
			double the_height = allGlyphs.get(0).getAnnotStyle().getHeight();
			height_slider.setValue((int) the_height);
		}
		else {
			height_slider.setValue(0);
		}
		trackName.setEnabled(select);
		height_slider.setEnabled(select);
		pluginDisplayAsB.setEnabled(false);
	}

	private void resetStylePanel() {
		int selectedTrackCount = selectedTiers.size();
		boolean select = selectedTrackCount > 0;
		enablePanel(stylePanel, select);
		if (selectedTrackCount == 1) {
			ITrackStyleExtended style = selectedTiers.get(0).getAnnotStyle();
			if (style == null || style instanceof SimpleTrackStyle) {
				return;
			}
			styleP_fgColorComboBox.setSelectedColor(style.getForeground());
			styleP_bgColorComboBox.setSelectedColor(style.getBackground());
			styleP_labelFGComboBox.setSelectedColor(style.getLabelForeground());
			styleP_trackNameSizeComboBox.setSelectedItem((int) style.getTrackNameSize());
		}
		else {
			styleP_fgColorComboBox.setSelectedColor(null);
			styleP_labelFGComboBox.setSelectedColor(null);
			styleP_bgColorComboBox.setSelectedColor(null);
			styleP_trackNameSizeComboBox.setSelectedItem(null);
		}
	}

	private ITrackStyleExtended getStyle(RootSeqSymmetry rootSym) {
		ITrackStyleExtended style = null;
		for (ViewModeGlyph vg : allGlyphs) {
			if (vg.getInfo() == rootSym) {
				style = vg.getAnnotStyle();
			}
		}
		return style;
	}

	private void resetAnnotationPanel() {
		int selectedTrackCount = annotSyms.size();
		boolean select = selectedTrackCount > 0;
		enablePanel(annotationPanel, select);
		if (selectedTrackCount == 1) {
			ITrackStyleExtended style = getStyle(annotSyms.get(0));
			if (style == null) {
				return;
			}
			annotP_maxStackDepthTextField.setText(Integer.toString(style.getMaxDepth()));
		} else {
			annotP_maxStackDepthTextField.setText("");
		}
	}

	private void resetGraphAndRangePanel() {
		boolean select = graphGlyphs.size() > 0;
		enablePanel(graphPanel, select);
		enablePanel(rangePanel, select);
		// float check box, not in graph panels
		boolean allFloat = true;
		boolean anySelected = false;
		for (AbstractGraphGlyph gg : graphGlyphs) {
			anySelected = true;
			if (!gg.getAnnotStyle().getFloatTier()) {
				allFloat = false;
			}
		}
		if (igbService.getSeqMapView().getPixelFloater().getChildren() != null) {
			for (GlyphI gl : igbService.getSeqMapView().getPixelFloater().getChildren()) {
				ViewModeGlyph vg = (ViewModeGlyph)gl;
				if (vg.isSelected()) {
					anySelected = true;
					if (!((ViewModeGlyph)gl).getAnnotStyle().getFloatTier()) {
						allFloat = false;
					}
				}
			}
		}
		floatCB.setEnabled(anySelected);
		floatCB.setSelected(anySelected && allFloat);
		// graph and range panels

		boolean all_are_floating = false;
		boolean all_show_axis = false;
		boolean all_show_label = false;

		// Take the first glyph in the list as a prototype
		AbstractGraphGlyph first_glyph = null;
		GraphType graph_style = GraphType.LINE_GRAPH;
		HeatMap hm = null;
		if (select) {
			first_glyph = graphGlyphs.get(0);
			graph_style = first_glyph.getGraphStyle();
			if (graph_style == GraphType.HEAT_MAP) {
				hm = first_glyph.getHeatMap();
			}
			all_are_floating = first_glyph.getGraphState().getTierStyle().getFloatTier();
			all_show_axis = first_glyph.getGraphState().getShowAxis();
			all_show_label = first_glyph.getGraphState().getShowLabel();
		}

		// Now loop through other glyphs if there are more than one
		// and see if the graph_style and heatmap are the same in all selections
		for (AbstractGraphGlyph gl : graphGlyphs) {
			all_are_floating = all_are_floating && gl.getGraphState().getTierStyle().getFloatTier();
			all_show_axis = all_show_axis && gl.getGraphState().getShowAxis();
			all_show_label = all_show_label && gl.getGraphState().getShowLabel();
			if (graph_style == null) {
				graph_style = GraphType.LINE_GRAPH;
			} else if (first_glyph.getGraphStyle() != gl.getGraphStyle()) {
				graph_style = GraphType.LINE_GRAPH;
			}
			if (graph_style == GraphType.HEAT_MAP) {
				if (first_glyph.getHeatMap() != gl.getHeatMap()) {
					hm = null;
				}
			} else {
				hm = null;
			}
		}

		selectButtonBasedOnGraphStyle(graph_style);

		if (graph_style == GraphType.HEAT_MAP) {
			graphP_heat_mapCB.setEnabled(true);
			graphP_hmapB.setSelected(true);
			if (hm == null) {
				graphP_heat_mapCB.setSelectedIndex(-1);
			} else {
				graphP_heat_mapCB.setSelectedItem(hm.getName());
			}
		} else {
			graphP_heat_mapCB.setEnabled(false);
			graphP_hmapB.setSelected(false);
		}
		vis_bounds_setter.setGraphs(graphGlyphs);

		boolean type = select;
		for (AbstractGraphGlyph graf : graphGlyphs) {
			type = !(graf.getInfo() != null && graf.getInfo() instanceof MisMatchGraphSym);
			if (type) {
				break;
			}
		}

		enableButtons(stylegroup, type);
		if (select) {
			graphP_yaxisCB.setSelected(all_show_axis);
			graphP_labelCB.setSelected(all_show_label);
		}

	}

	private void collectSymsAndGlyphs(List<RootSeqSymmetry> selected_syms, int symcount) {
		if (rootSyms != selected_syms) {
			// in certain cases selected_syms arg and grafs list may be same, for example when method is being
			//     called to catch changes in glyphs representing selected sym, not the syms themselves)
			//     therefore don't want to change grafs list if same as selected_syms (especially don't want to clear it!)
			rootSyms.clear();
			annotSyms.clear();
			graphSyms.clear();
		}
		allGlyphs.clear();
		graphGlyphs.clear();
		for (Glyph glyph : igbService.getSelectedTierGlyphs()) {
			ViewModeGlyph vg = ((TierGlyph)glyph).getViewModeGlyph();
			allGlyphs.add(vg);
			if (vg instanceof AbstractGraphGlyph) {
				if (vg instanceof MultiGraphGlyph) {
					for (GlyphI child : vg.getChildren()) {
						if (rootSyms.contains(child.getInfo())) {
							graphGlyphs.add((AbstractGraphGlyph) child);
						}
					}
				}else if (!graphGlyphs.contains(vg)) {
					graphGlyphs.add((AbstractGraphGlyph)vg);
				}
			}
		}
		// First loop through and collect graphs and glyphs
		for (int i = 0; i < symcount; i++) {
			RootSeqSymmetry rootSym = selected_syms.get(i);
			// only add to grafs if list is not identical to selected_syms arg
			if (rootSyms != selected_syms) {
				rootSyms.add(rootSym);
				if (rootSym.getCategory() == FileTypeCategory.Annotation || rootSym.getCategory() == FileTypeCategory.Alignment) {
					annotSyms.add(rootSym);
				}
				if (rootSym.getCategory() == FileTypeCategory.Graph || rootSym.getCategory() == FileTypeCategory.ScoredContainer) {
					graphSyms.add(rootSym);
				}
			}
			// add all graph glyphs representing graph sym
			//	  System.out.println("found multiple glyphs for graph sym: " + multigl.size());
//				for (Glyph g : igbService.getVisibleTierGlyphs()) {
//					ViewModeGlyph vg = ((TierGlyph) g).getViewModeGlyph();
//					if (vg instanceof MultiGraphGlyph) {
//						for (GlyphI child : vg.getChildren()) {
//							if (grafs.contains(child.getInfo())) {
//								glyphs.add((AbstractGraphGlyph) child);
//							}
//						}
//					} else if (grafs.contains(vg.getInfo())) {
//						glyphs.add((AbstractGraphGlyph) vg);
//					}
//				}
			
			Glyph glyph = igbService.getSeqMap().getItem(rootSym);
			if (glyph != null) {
				if (glyph instanceof AbstractGraphGlyph) {
					if (glyph instanceof MultiGraphGlyph) {
						for (GlyphI child : glyph.getChildren()) {
							if (rootSyms.contains(child.getInfo())) {
								graphGlyphs.add((AbstractGraphGlyph) child);
							}
						}
					}else if (!graphGlyphs.contains(glyph)) {
						graphGlyphs.add((AbstractGraphGlyph)glyph);
					}
				}
			}
		}
	}

	private void selectButtonBasedOnGraphStyle(GraphType graph_style) {
		switch (graph_style) {
			case MINMAXAVG:
				graphP_mmavgB.setSelected(true);
				break;
			case LINE_GRAPH:
				graphP_lineB.setSelected(true);
				break;
			case BAR_GRAPH:
				graphP_barB.setSelected(true);
				break;
			case DOT_GRAPH:
				graphP_dotB.setSelected(true);
				break;
			case HEAT_MAP:
				//hmapB.setSelected(true);
				graphP_hmapB.setSelected(true);
				break;
//			case STAIRSTEP_GRAPH:
//				graphP_sstepB.setSelected(true);
//				break;
			default:
				graphP_hidden_styleB.setSelected(true);
				break;
		}
	}

	public void seqSelectionChanged(SeqSelectionEvent evt) {
		if (DEBUG_EVENTS) {
			System.out.println("SeqSelectionEvent, selected seq: " + evt.getSelectedSeq() + " received by " + this.getClass().getName());
		}
//		current_seq = evt.getSelectedSeq();
//		refreshSelection(gmodel.getSelectedSymmetries(current_seq));
		refreshSelection(Collections.<RootSeqSymmetry>emptyList());
	}

	private static void enableButtons(ButtonGroup g, boolean b) {
		Enumeration<AbstractButton> e = g.getElements();
		while (e.hasMoreElements()) {
			e.nextElement().setEnabled(b);
		}
	}

	private final class GraphStyleSetter extends GenericAction implements ActionListener {

		private static final long serialVersionUID = 1L;
		GraphType graphType = GraphType.LINE_GRAPH;

		public GraphStyleSetter(GraphType graphType) {
			super(null, null, null);
			this.graphType = graphType;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			super.actionPerformed(event);
			if (DEBUG_EVENTS) {
				System.out.println(this.getClass().getName() + " got an ActionEvent: " + event);
			}
			if (graphGlyphs.isEmpty() || !is_listening) {
				return;
			}

			Runnable r = new Runnable() {

				public void run() {
					String viewMode = graphType2ViewMode.get(graphType);
					HeatMap hm = (graphGlyphs.get(0)).getHeatMap();
					for (AbstractGraphGlyph sggl : new ArrayList<AbstractGraphGlyph>(graphGlyphs)) {
						sggl.setShowGraph(true);
						igbService.changeViewMode(igbService.getSeqMapView(), sggl.getAnnotStyle(), viewMode, (RootSeqSymmetry) sggl.getInfo(), sggl.getGraphState().getComboStyle());
						if ((graphType == GraphType.HEAT_MAP) && (hm != sggl.getHeatMap())) {
							hm = null;
						}
					}
					if (graphType == GraphType.HEAT_MAP) {
						graphP_heat_mapCB.setEnabled(true);
						graphP_hmapB.setSelected(true);
						if (hm == null) {
							graphP_heat_mapCB.setSelectedIndex(-1);
						} else {
							graphP_heat_mapCB.setSelectedItem(hm.getName());
						}
					} else {
						graphP_heat_mapCB.setEnabled(false);
						graphP_hmapB.setSelected(false);
						// don't bother to change the displayed heat map name
					}
				}
			};

			SwingUtilities.invokeLater(r);
		}
	}

	private final class HeatMapItemListener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {
			if (graphGlyphs.isEmpty() || !is_listening) {
				return;
			}

			if (e.getStateChange() == ItemEvent.SELECTED) {
				String name = (String) e.getItem();
				HeatMap hm = HeatMap.getStandardHeatMap(name);

				if (hm != null) {
					for (AbstractGraphGlyph gl : graphGlyphs) {
						if ("heatmapgraph".equals(gl.getName())) {
							gl.setShowGraph(true);
							gl.setHeatMap(hm);
						}
					}
					igbService.getSeqMap().updateWidget();
				}
			}
		}
	}

	public void setTrackHeight(double height) {
		for (ViewModeGlyph gl : allGlyphs) {
			Rectangle2D.Double cbox = gl.getCoordBox();
			gl.setCoords(cbox.x, cbox.y, cbox.width, height);

			// If a graph is joined with others in a combo tier, repack that tier.
			GlyphI parentgl = gl.getParent();
			if (isTierGlyph(parentgl)) {
				parentgl.pack(igbService.getView());
			}
		}
		igbService.packMap(false, true);
	}

	public void fgColorComboBoxActionPerformed() {
		if (igbService.getSeqMap() == null) {
			return;
		}
		Color color = styleP_fgColorComboBox.getSelectedColor();
		if (color != null) {
			for (TierGlyph tier : selectedTiers) {
				tier.getAnnotStyle().setForeground(color);
			}
		}
		igbService.getSeqMapView().updatePanel();
	}

	public void labelFGCBActionPerformed() {
		if (igbService.getSeqMap() == null) {
			return;
		}
		Color color = styleP_labelFGComboBox.getSelectedColor();
		if (color != null) {
			for (TierGlyph tier : selectedTiers) {
				tier.getAnnotStyle().setLabelForeground(color);
			}
		}
		igbService.getSeqMapView().updatePanel();
	}

	public void bgColorComboBoxActionPerformed() {
		if (igbService.getSeqMap() == null) {
			return;
		}
		Color color = styleP_bgColorComboBox.getSelectedColor();
		if (color != null) {
			for (TierGlyph tier : selectedTiers) {
				tier.getAnnotStyle().setBackground(color);
			}
		}
		ThreadUtils.runOnEventQueue(new Runnable() {

			public void run() {
				igbService.getSeqMap().updateWidget();
			}
		});
	}

	public void setTrackName(String name) {
		if (igbService.getSeqMapView() == null) {
			return;
		}
		if (selectedTiers != null) {
			igbService.getSeqMapView().renameTier(selectedTiers.get(0), name);
		}
	}

	public void setMaxDepth(String mdepth_string) {
		if (selectedTiers == null || mdepth_string == null) {
			return;
		}
		for (TierGlyph tier : selectedTiers) {
			int prev_max_depth = tier.getAnnotStyle().getMaxDepth();
			try {
				tier.getAnnotStyle().setMaxDepth(Integer.parseInt(mdepth_string));
			} catch (Exception ex) {
				tier.getAnnotStyle().setMaxDepth(prev_max_depth);
			}
		}
		igbService.getSeqMapView().setTierStyles();
		igbService.getSeqMapView().repackTheTiers(true, true);
	}

	public void setNameSize(String fontstring) {
		if (selectedTiers == null || fontstring == null) {
			return;
		}
		for (TierGlyph tier : selectedTiers) {
			ITrackStyleExtended style = tier.getAnnotStyle();
			float prev_font_size = style.getTrackNameSize();
			try {
				style.setTrackNameSize(Float.parseFloat(fontstring));
			} catch (Exception ex) {
				style.setTrackNameSize(prev_font_size);
			}
		}
		igbService.getSeqMapView().setTierStyles();
		igbService.getSeqMapView().repackTheTiers(true, true);
	}

	public void trackstylePropertyChanged(EventObject eo) {
		refreshSelection(rootSyms);
	}

	public void setViewMode(DisplayType displayType) {
		if (selectedTiers == null) {
			return;
		}
		is_listening = false;
		for (GlyphI g : new CopyOnWriteArrayList<TierGlyph>(selectedTiers)) {
			final TierGlyph tier = (TierGlyph) g;
			final RootSeqSymmetry rootSym = (RootSeqSymmetry) tier.getInfo();
			FileTypeCategory category = rootSym.getCategory();
			final ITrackStyleExtended style = tier.getAnnotStyle();
			final ITrackStyleExtended comboStyle = (tier.getViewModeGlyph() instanceof AbstractGraphGlyph)
					? ((AbstractGraphGlyph) tier.getViewModeGlyph()).getGraphState().getComboStyle() : null;
			MapViewGlyphFactoryI mode;
			if (graphDisplayAsB.isSelected() && category == FileTypeCategory.Annotation) {
				TrackListProvider tlp = new TrackListProvider() {
					@Override
					public List<TierGlyph> getTrackList() {
						return Arrays.asList(new TierGlyph[]{tier});
					}
				};
				ActionEvent ae = new ActionEvent(tlp, -1, null);
				annotationDepthTransformAction.actionPerformed(ae);
				continue;
			}
			if (graphDisplayAsB.isSelected() && category == FileTypeCategory.Alignment) {
				TrackListProvider tlp = new TrackListProvider() {
					@Override
					public List<TierGlyph> getTrackList() {
						return Arrays.asList(new TierGlyph[]{tier});
					}
				};
				ActionEvent ae = new ActionEvent(tlp, -1, null);
				alignmentDepthTransformAction.actionPerformed(ae);
				continue;
			}
			if (autoDisplayAsB.isSelected() && category == FileTypeCategory.Annotation) {
				mode = MapViewModeHolder.getInstance().getViewFactory("semantic_zoom_annotation");
			}
			else if (autoDisplayAsB.isSelected() && category == FileTypeCategory.Alignment) {
				mode = MapViewModeHolder.getInstance().getViewFactory("semantic_zoom_alignment");
			}
			else {
				mode = MapViewModeHolder.getInstance().getDefaultFactoryFor(category);
			}
			if (style.getSeparate() && !mode.supportsTwoTrack()) {
				style.setSeparate(false);
			}
			igbService.changeViewMode(igbService.getSeqMapView(), style, mode.getName(), rootSym, comboStyle);
		}
		igbService.getSeqMapView().updatePanel(false, false);
		is_listening = true;
	}

	private void setShowAxis(boolean b) {
		for (AbstractGraphGlyph gl : graphGlyphs) {
			gl.setShowAxis(b);
		}
		igbService.getSeqMap().updateWidget();
	}

	private void setShowLabels(boolean b) {
		for (AbstractGraphGlyph gl : graphGlyphs) {
			gl.setShowLabel(b);
		}
		igbService.getSeqMap().updateWidget();
	}
}
