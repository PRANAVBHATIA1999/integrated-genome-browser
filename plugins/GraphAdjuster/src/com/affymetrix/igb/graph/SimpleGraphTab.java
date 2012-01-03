/**
 *   Copyright (c) 2006 Affymetrix, Inc.
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
package com.affymetrix.igb.graph;

import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.swing.recordplayback.JRPButton;
import com.affymetrix.genoviz.swing.recordplayback.JRPCheckBox;
import com.affymetrix.genoviz.swing.recordplayback.JRPComboBox;
import com.affymetrix.genoviz.swing.recordplayback.JRPComboBoxWithSingleListener;
import com.affymetrix.genoviz.swing.recordplayback.JRPRadioButton;
import com.affymetrix.genoviz.swing.recordplayback.JRPSlider;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;
import com.affymetrix.genoviz.util.ErrorHandler;

import com.affymetrix.genometryImpl.BioSeq;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.event.SymSelectionEvent;
import com.affymetrix.genometryImpl.event.SymSelectionListener;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.operator.graph.GraphOperator;
import com.affymetrix.genometryImpl.operator.transform.FloatTransformer;
import com.affymetrix.genometryImpl.operator.transform.IdentityTransform;
import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.style.GraphType;
import com.affymetrix.genometryImpl.style.HeatMap;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.style.SimpleTrackStyle;
import com.affymetrix.genometryImpl.symmetry.GraphIntervalSym;
import com.affymetrix.genometryImpl.symmetry.GraphSym;
import com.affymetrix.genometryImpl.symmetry.MisMatchGraphSym;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.GraphSymUtils;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genoviz.bioviews.ViewI;

import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.FileTracker;
import com.affymetrix.igb.shared.GraphGlyph;
import com.affymetrix.igb.shared.TierGlyph;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;

import java.util.Map.Entry;
import javax.swing.*;
import javax.swing.event.*;

public final class SimpleGraphTab
		implements SeqSelectionListener, SymSelectionListener {

	//System.out.println() statements do not show on the screen, they are not translated.

	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("graph");
	private static SimpleGraphTab singleton;
	BioSeq current_seq;
	GenometryModel gmodel;
	boolean is_listening = true; // used to turn on and off listening to GUI events
	GraphScoreThreshSetter score_thresh_adjuster;
	public GraphVisibleBoundsSetter vis_bounds_setter;
	boolean DEBUG_EVENTS = false;
	public JLabel selected_graphs_label = new JLabel(BUNDLE.getString("selectedGraphsLabel"));
	public JRPRadioButton mmavgB = new JRPRadioButton("SimpleGraphTab_mmavgB", BUNDLE.getString("minMaxAvgButton"));
	public JRPRadioButton lineB = new JRPRadioButton("SimpleGraphTab_lineB", BUNDLE.getString("lineButton"));
	public JRPRadioButton barB = new JRPRadioButton("SimpleGraphTab_barB", BUNDLE.getString("barButton"));
	public JRPRadioButton dotB = new JRPRadioButton("SimpleGraphTab_dotB", BUNDLE.getString("dotButton"));
	public JRPRadioButton sstepB = new JRPRadioButton("SimpleGraphTab_sstepB", BUNDLE.getString("stairStepButton"));
	public JRPRadioButton hmapB = new JRPRadioButton("SimpleGraphTab_hmapB", BUNDLE.getString("heatMapButton"));
	public JRPRadioButton hidden_styleB = new JRPRadioButton("SimpleGraphTab_hidden_styleB", "No Selection"); // this button will not be displayed
	public ButtonGroup stylegroup = new ButtonGroup();
	public com.jidesoft.combobox.ColorComboBox fgColorComboBox = new com.jidesoft.combobox.ColorComboBox();
	public com.jidesoft.combobox.ColorComboBox bgColorComboBox = new com.jidesoft.combobox.ColorComboBox();
	public JRPSlider height_slider = new JRPSlider("SimpleGraphTab_height_slider", JSlider.HORIZONTAL, 10, 500, 50);
	public final List<GraphSym> grafs = new ArrayList<GraphSym>();
	public final List<GraphGlyph> glyphs = new ArrayList<GraphGlyph>();
	public final JRPCheckBox labelCB = new JRPCheckBox("SimpleGraphTab_hidden_labelCB", BUNDLE.getString("labelCheckBox"));
	public final JRPCheckBox yaxisCB = new JRPCheckBox("SimpleGraphTab_hidden_yaxisCB", BUNDLE.getString("yAxisCheckBox"));
	public final JRPCheckBox floatCB = new JRPCheckBox("SimpleGraphTab_hidden_floatCB", BUNDLE.getString("floatingCheckBox"));
	private IGBService igbService;
	private final Action select_all_graphs_action = new GenericAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			igbService.getSeqMapView().selectAllGraphs();
		}

		@Override
		public String getText() {
			return BUNDLE.getString("selectAllGraphs");
		}
	};
	private final Action delete_selected_graphs_action = new GenericAction() {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			deleteGraphs(gmodel, grafs);
		}

		@Override
		public String getText() {
			return BUNDLE.getString("deleteSelectedGraphs");
		}
	};
	private final Action save_selected_graphs_action = new GenericAction() {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			saveGraphs(gmodel, grafs);
		}

		@Override
		public String getText() {
			return BUNDLE.getString("saveSelectedGraphs") + "...";
		}
	};
	private final Action graph_threshold_action = new GenericAction() {

		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			super.actionPerformed(e);
			showGraphScoreThreshSetter();
		}

		@Override
		public String getText() {
			return BUNDLE.getString("graphThresholding") + "...";
		}
	};
	public final JRPButton selectAllB = new JRPButton("SimpleGraphTab_selectAllB", select_all_graphs_action);
	public final JRPButton saveB = new JRPButton("SimpleGraphTab_saveB", save_selected_graphs_action);
	public final JRPButton deleteB = new JRPButton("SimpleGraphTab_deleteB", delete_selected_graphs_action);
	public final JRPButton threshB = new JRPButton("SimpleGraphTab_threshB", graph_threshold_action);
	public final JRPTextField paramT = new JRPTextField("SimpleGraphTab_paramT", "", 2);
	public final JRPButton combineB = new JRPButton("SimpleGraphTab_combineB", BUNDLE.getString("combineButton"));
	public final JRPButton splitB = new JRPButton("SimpleGraphTab_splitB", BUNDLE.getString("splitButton"));
	public JRPComboBox heat_mapCB;
	public AdvancedGraphPanel advanced_panel;
	public static void init(IGBService igbService) {
		singleton = new SimpleGraphTab(igbService);
	}

	public static synchronized SimpleGraphTab getSingleton() {
		return singleton;
	}

	public SimpleGraphTab(IGBService igbS) {
		igbService = igbS;
		advanced_panel = new SimpleGraphTab.AdvancedGraphPanel();

		heat_mapCB = new JRPComboBox("SimpleGraphTab_heat_mapCB", HeatMap.getStandardNames());
		heat_mapCB.addItemListener(new HeatMapItemListener());

		barB.addActionListener(new GraphStyleSetter(GraphType.BAR_GRAPH));
		dotB.addActionListener(new GraphStyleSetter(GraphType.DOT_GRAPH));
		hmapB.addActionListener(new GraphStyleSetter(GraphType.HEAT_MAP));
		lineB.addActionListener(new GraphStyleSetter(GraphType.LINE_GRAPH));
		mmavgB.addActionListener(new GraphStyleSetter(GraphType.MINMAXAVG));
		sstepB.addActionListener(new GraphStyleSetter(GraphType.STAIRSTEP_GRAPH));

		stylegroup.add(barB);
		stylegroup.add(dotB);
		stylegroup.add(hmapB);
		stylegroup.add(lineB);
		stylegroup.add(mmavgB);
		stylegroup.add(sstepB);
		stylegroup.add(hidden_styleB); // invisible button

		hidden_styleB.setSelected(true); // deselect all visible radio buttons

		vis_bounds_setter = new GraphVisibleBoundsSetter(igbService.getSeqMap());
		score_thresh_adjuster = new GraphScoreThreshSetter(igbService, vis_bounds_setter);

		height_slider.addChangeListener(new GraphHeightSetter());

		resetSelectedGraphGlyphs(Collections.EMPTY_LIST);

		gmodel = GenometryModel.getGenometryModel();
		gmodel.addSeqSelectionListener(this);
		gmodel.addSymSelectionListener(this);
	}

	public boolean isTierGlyph(GlyphI glyph) {
		return glyph instanceof TierGlyph;
	}

	private void showGraphScoreThreshSetter() {
		score_thresh_adjuster.showFrame();
	}

	public void addFloatTransformer(FloatTransformer floatTransformer) {
		advanced_panel.loadTransforms();
	}

	public void removeFloatTransformer(FloatTransformer floatTransformer) {
		advanced_panel.loadTransforms();
	}

	public void addGraphOperator(GraphOperator graphOperator) {
		advanced_panel.loadOperators();
	}

	public void removeGraphOperator(GraphOperator graphOperator) {
		advanced_panel.loadOperators();
	}

	public void symSelectionChanged(SymSelectionEvent evt) {
		List<SeqSymmetry> selected_syms = evt.getSelectedSyms();
		// Only pay attention to selections from the main SeqMapView or its map.
		// Ignore the splice view as well as events coming from this class itself.

		Object src = evt.getSource();
		if (!(src == igbService.getSeqMapView() || src == igbService.getSeqMap())) {
			return;
		}

		resetSelectedGraphGlyphs(selected_syms);
	}

	private void resetSelectedGraphGlyphs(List<?> selected_syms) {
		int symcount = selected_syms.size();
		is_listening = false; // turn off propagation of events from the GUI while we modify the settings
		collectGraphsAndGlyphs(selected_syms, symcount);

		int num_glyphs = glyphs.size();
		//    System.out.println("number of selected graphs: " + num_glyphs);
		double the_height = -1; // -1 indicates unknown height

		boolean all_are_floating = false;
		boolean all_show_axis = false;
		boolean all_show_label = false;
		boolean any_are_combined = false; // are any selections inside a combined tier
		boolean all_are_combined = false; // are all selections inside (a) combined tier(s)

		// Take the first glyph in the list as a prototype
		GraphGlyph first_glyph = null;
		GraphType graph_style = GraphType.LINE_GRAPH;
		HeatMap hm = null;
		if (!glyphs.isEmpty()) {
			first_glyph = glyphs.get(0);
			graph_style = first_glyph.getGraphStyle();
			if (graph_style == GraphType.HEAT_MAP) {
				hm = first_glyph.getHeatMap();
			}
			the_height = first_glyph.getGraphState().getTierStyle().getHeight();
			all_are_floating = first_glyph.getGraphState().getFloatGraph();
			all_show_axis = first_glyph.getGraphState().getShowAxis();
			all_show_label = first_glyph.getGraphState().getShowLabel();
			boolean this_one_is_combined = (first_glyph.getGraphState().getComboStyle() != null);
			any_are_combined = this_one_is_combined;
			all_are_combined = this_one_is_combined;
		}

		// Now loop through other glyphs if there are more than one
		// and see if the graph_style and heatmap are the same in all selections
		for (GraphGlyph gl : glyphs) {
			all_are_floating = all_are_floating && gl.getGraphState().getFloatGraph();
			all_show_axis = all_show_axis && gl.getGraphState().getShowAxis();
			all_show_label = all_show_label && gl.getGraphState().getShowLabel();
			boolean this_one_is_combined = (gl.getGraphState().getComboStyle() != null);
			any_are_combined = any_are_combined || this_one_is_combined;
			all_are_combined = all_are_combined && this_one_is_combined;

			if (first_glyph.getGraphStyle() != gl.getGraphStyle()) {
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

		if (num_glyphs == 0) {
			selected_graphs_label.setText(SimpleGraphTab.BUNDLE.getString("noSelectError"));
		} else if (num_glyphs == 1) {
			GraphSym graf_0 = grafs.get(0);
			selected_graphs_label.setText(graf_0.getGraphName());
			GlyphI g = igbService.getSeqMap().getItem(grafs.get(0));
			GraphGlyph gl = (GraphGlyph) g;
			Color color = gl.getGraphState().getTierStyle().getBackground();
			bgColorComboBox.setSelectedColor(color);
			color = gl.getGraphState().getTierStyle().getForeground();
			fgColorComboBox.setSelectedColor(color);
		} else {
			selected_graphs_label.setText(MessageFormat.format(SimpleGraphTab.BUNDLE.getString("selected"), num_glyphs));
			fgColorComboBox.setSelectedColor(null);
			bgColorComboBox.setSelectedColor(null);
		}

		selectButtonBasedOnGraphStyle(graph_style);

		if (graph_style == GraphType.HEAT_MAP) {
			heat_mapCB.setEnabled(true);
			if (hm == null) {
				heat_mapCB.setSelectedIndex(-1);
			} else {
				heat_mapCB.setSelectedItem(hm.getName());
			}
		} else {
			heat_mapCB.setEnabled(false);
		}

		if (the_height != -1) {
			height_slider.setValue((int) the_height);
		}
		vis_bounds_setter.setGraphs(glyphs);
		score_thresh_adjuster.setGraphs(glyphs);

		if (!glyphs.isEmpty()) {
			floatCB.setSelected(all_are_floating);
			yaxisCB.setSelected(all_show_axis);
			labelCB.setSelected(all_show_label);
		}

		boolean b = !(grafs.isEmpty());
		height_slider.setEnabled(b);
		graph_threshold_action.setEnabled(b);
		boolean type = b;
		for (GraphSym graf : grafs) {
			type = !(graf instanceof MisMatchGraphSym);
			if (type) {
				break;
			}
		}

		enableButtons(stylegroup, type);

		floatCB.setEnabled(b);
		yaxisCB.setEnabled(b);
		labelCB.setEnabled(b);

		fgColorComboBox.setEnabled(b);
		bgColorComboBox.setEnabled(b);

		save_selected_graphs_action.setEnabled(grafs.size() == 1);
		delete_selected_graphs_action.setEnabled(b);
		advanced_panel.setPanelEnabled();

		combineB.setEnabled(!all_are_combined && grafs.size() > 1);
		splitB.setEnabled(any_are_combined);

		is_listening = true; // turn back on GUI events
	}

	private void collectGraphsAndGlyphs(List<?> selected_syms, int symcount) {
		if (grafs != selected_syms) {
			// in certain cases selected_syms arg and grafs list may be same, for example when method is being
			//     called to catch changes in glyphs representing selected sym, not the syms themselves)
			//     therefore don't want to change grafs list if same as selected_syms (especially don't want to clear it!)
			grafs.clear();
		}
		glyphs.clear();
		// First loop through and collect graphs and glyphs
		for (int i = 0; i < symcount; i++) {
			if (selected_syms.get(i) instanceof GraphSym) {
				GraphSym graf = (GraphSym) selected_syms.get(i);
				// only add to grafs if list is not identical to selected_syms arg
				if (grafs != selected_syms) {
					grafs.add(graf);
				}
				List<GlyphI> multigl = igbService.getSeqMap().getItems(graf);
				// add all graph glyphs representing graph sym
				//	  System.out.println("found multiple glyphs for graph sym: " + multigl.size());
				for (GlyphI g : multigl) {
					glyphs.add((GraphGlyph) g);
				}
			}
		}
	}

	private void selectButtonBasedOnGraphStyle(GraphType graph_style) {
		switch (graph_style) {
			case MINMAXAVG:
				mmavgB.setSelected(true);
				break;
			case LINE_GRAPH:
				lineB.setSelected(true);
				break;
			case BAR_GRAPH:
				barB.setSelected(true);
				break;
			case DOT_GRAPH:
				dotB.setSelected(true);
				break;
			case HEAT_MAP:
				hmapB.setSelected(true);
				break;
			case STAIRSTEP_GRAPH:
				sstepB.setSelected(true);
				break;
			default:
				hidden_styleB.setSelected(true);
				break;
		}
	}

	public void seqSelectionChanged(SeqSelectionEvent evt) {
		if (DEBUG_EVENTS) {
			System.out.println("SeqSelectionEvent, selected seq: " + evt.getSelectedSeq() + " received by " + this.getClass().getName());
		}
		current_seq = evt.getSelectedSeq();
		resetSelectedGraphGlyphs(gmodel.getSelectedSymmetries(current_seq));
	}

	private static void enableButtons(ButtonGroup g, boolean b) {
		Enumeration<AbstractButton> e = g.getElements();
		while (e.hasMoreElements()) {
			e.nextElement().setEnabled(b);
		}
	}

	public AdvancedGraphPanel getAdvancedPanel() {
		return advanced_panel;
	}

	private final class GraphStyleSetter implements ActionListener {

		GraphType style = GraphType.LINE_GRAPH;

		public GraphStyleSetter(GraphType style) {
			this.style = style;
		}

		public void actionPerformed(ActionEvent event) {
			if (DEBUG_EVENTS) {
				System.out.println(this.getClass().getName() + " got an ActionEvent: " + event);
			}
			if (glyphs.isEmpty() || !is_listening) {
				return;
			}

			Runnable r = new Runnable() {

				public void run() {
					GraphGlyph first_glyph = glyphs.get(0);
					if (style == GraphType.HEAT_MAP) {
						// set to heat map FIRST so that getHeatMap() below will return default map instead of null
						first_glyph.setGraphStyle(GraphType.HEAT_MAP);
					}
					HeatMap hm = (glyphs.get(0)).getHeatMap();
					for (GraphGlyph sggl : glyphs) {
						sggl.setShowGraph(true);
						sggl.setGraphStyle(style); // leave the heat map whatever it was
						if ((style == GraphType.HEAT_MAP) && (hm != sggl.getHeatMap())) {
							hm = null;
						}
					}
					if (style == GraphType.HEAT_MAP) {
						heat_mapCB.setEnabled(true);
						if (hm == null) {
							heat_mapCB.setSelectedIndex(-1);
						} else {
							heat_mapCB.setSelectedItem(hm.getName());
						}
					} else {
						heat_mapCB.setEnabled(false);
						// don't bother to change the displayed heat map name
					}
					igbService.getSeqMap().updateWidget();
				}
			};

			SwingUtilities.invokeLater(r);
		}
	}

	private void updateViewer() {
		final List<GraphSym> previous_graph_syms = new ArrayList<GraphSym>(grafs);
		// set selections to empty so that options get turned off
		resetSelectedGraphGlyphs(Collections.EMPTY_LIST);
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				igbService.getSeqMapView().setAnnotatedSeq(gmodel.getSelectedSeq(), true, true);
				resetSelectedGraphGlyphs(previous_graph_syms);
			}
		});
	}

	private final class HeatMapItemListener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {
			if (glyphs.isEmpty() || !is_listening) {
				return;
			}

			if (e.getStateChange() == ItemEvent.SELECTED) {
				String name = (String) e.getItem();
				HeatMap hm = HeatMap.getStandardHeatMap(name);

				if (hm != null) {
					for (GraphGlyph gl : glyphs) {
						gl.setShowGraph(true);
						gl.setGraphStyle(GraphType.HEAT_MAP);
						gl.setHeatMap(hm);
					}
					igbService.getSeqMap().updateWidget();
				}
			}
		}
	}

	private final class GraphHeightSetter implements ChangeListener {

		public void stateChanged(ChangeEvent e) {
			if (glyphs.isEmpty() || !is_listening) {
				return;
			}

			if (e.getSource() == height_slider) {
				setTheHeights((double) height_slider.getValue());
			}
		}

		private void setTheHeights(double height) {
			for (GraphGlyph gl : glyphs) {
				Rectangle2D.Double cbox = gl.getCoordBox();
				gl.setCoords(cbox.x, cbox.y, cbox.width, height);

				// If a graph is joined with others in a combo tier, repack that tier.
				GlyphI parentgl = gl.getParent();
				if (isTierGlyph(parentgl)) {
					//	  System.out.println("Glyph: " + gl.getLabel() + ", packer: " + parentgl.getPacker());
					parentgl.pack(igbService.getView(), false);
				}
			}
			igbService.packMap(false, true);
		}
	}

	final class AdvancedGraphPanel {

		private class HoverEffect implements MouseListener {

			private String A = null;
			private String B = null;

			public void mouseClicked(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
				JRPComboBoxWithSingleListener comp = (JRPComboBoxWithSingleListener) e.getComponent();
				String selection = (String) comp.getSelectedItem();
				GraphOperator operator = name2operator.get(selection);

				if (grafs.size() >= operator.getOperandCountMin()
						&& grafs.size() <= operator.getOperandCountMax()) {
					setGraphName(comp, operator);
				} else {
					comp.setToolTipText(GeneralUtils.getOperandMessage(grafs.size(), operator.getOperandCountMin(), operator.getOperandCountMax(), "graph"));
				}
			}

			public void mouseExited(MouseEvent e) {
				JRPComboBoxWithSingleListener comp = (JRPComboBoxWithSingleListener) e.getComponent();
				String selection = (String) comp.getSelectedItem();
				unsetGraphName(name2operator.get(selection));
			}

			public void setGraphName(JRPComboBoxWithSingleListener comp, GraphOperator operator) {
				if (operator.getOperandCountMin() == 2 && operator.getOperandCountMax() == 2) {
					A = grafs.get(0).getGraphName();
					B = grafs.get(1).getGraphName();

					grafs.get(0).setGraphName("A");
					grafs.get(1).setGraphName("B");

					comp.setToolTipText(null);
					ThreadUtils.runOnEventQueue(new Runnable() {

						public void run() {
							igbService.getSeqMap().updateWidget();
						}
					});
				}
			}

			public void unsetGraphName(GraphOperator operator) {
				if (operator.getOperandCountMin() == 2 && operator.getOperandCountMax() == 2) {
					if (A != null && B != null && grafs.size() > 1) {
						grafs.get(0).setGraphName(A);
						grafs.get(1).setGraphName(B);

						ThreadUtils.runOnEventQueue(new Runnable() {

							public void run() {
								igbService.getSeqMap().updateWidget();
							}
						});
						A = null;
						B = null;

					}
				}
			}
		}
		private final Map<String, FloatTransformer> name2transform;
		private final Map<String, GraphOperator> name2operator;
		public final JRPButton transformationGoB = new JRPButton("SimpleGraphTab_transformationGoB", BUNDLE.getString("goButton"));
		public final JLabel transformation_label = new JLabel(BUNDLE.getString("transformationLabel"));
		public final JRPComboBoxWithSingleListener transformationCB = new JRPComboBoxWithSingleListener("SimpleGraphTab_transformation");
		public final JLabel operation_label = new JLabel(BUNDLE.getString("operationLabel"));
		public final JRPComboBoxWithSingleListener operationCB = new JRPComboBoxWithSingleListener("SimpleGraphTab_operation");
		public final JRPButton operationGoB = new JRPButton("SimpleGraphTab_operationGoB", BUNDLE.getString("goButton"));
		private final HoverEffect hovereffect;
		private final ItemListener operationListener = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				setPanelEnabled();
			}
		};

		public AdvancedGraphPanel() {
			name2transform = new HashMap<String, FloatTransformer>();
			name2operator = new HashMap<String, GraphOperator>();
			hovereffect = new HoverEffect();
			paramT.setText("");
			paramT.setEditable(false);

			transformationCB.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					String selection = (String) transformationCB.getSelectedItem();
					if (selection == null) {
						paramT.setEditable(false);
					} else {
						FloatTransformer trans = name2transform.get(selection);
						String paramPrompt = trans.getParamPrompt();
						if (paramPrompt == null) {
							paramT.setEditable(false);
						} else {
							paramT.setEditable(true);
						}
					}
				}
			});

			operationCB.addMouseListener(hovereffect);
			operationCB.addItemListener(operationListener);

			transformationGoB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					doTransformGraphs();
				}
			});

			operationGoB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String selection = (String) operationCB.getSelectedItem();
					GraphOperator operator = name2operator.get(selection);
					hovereffect.unsetGraphName(operator);
					if (igbService.doOperateGraphs(operator, glyphs)) {
						updateViewer();
					}
				}
			});

			floatCB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					floatGraphs(floatCB.isSelected());
				}
			});

			labelCB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setShowLabels(labelCB.isSelected());
				}
			});

			yaxisCB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					setShowAxis(yaxisCB.isSelected());
				}
			});

			combineB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					combineGraphs();
				}
			});
			splitB.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					splitGraphs();
				}
			});
		}

		public void loadTransforms() {
			transformationCB.removeAllItems();
			name2transform.clear();
			TreeSet<FloatTransformer> floatTransformers = new TreeSet<FloatTransformer>(
				new Comparator<FloatTransformer>() {
					@Override
					public int compare(FloatTransformer o1, FloatTransformer o2) {
						return o1.getName().compareTo(o2.getName());
					}
				}
			);
			floatTransformers.addAll(ExtensionPointHandler.getExtensionPoint(FloatTransformer.class).getExtensionPointImpls());
			for (FloatTransformer transformer : floatTransformers) {
				name2transform.put(transformer.getName(), transformer);
				transformationCB.addItem(transformer.getName());
			}
		}

		public void loadOperators() {
			operationCB.removeAllItems();
			name2operator.clear();
			TreeSet<GraphOperator> graphOperators = new TreeSet<GraphOperator>(
				new Comparator<GraphOperator>() {
					@Override
					public int compare(GraphOperator o1, GraphOperator o2) {
						return o1.getName().compareTo(o2.getName());
					}
				}
			);
			graphOperators.addAll(ExtensionPointHandler.getExtensionPoint(GraphOperator.class).getExtensionPointImpls());
			for (GraphOperator graphOperator : graphOperators) {
				name2operator.put(graphOperator.getName(), graphOperator);
				operationCB.addItem(graphOperator.getName());
			}
		}

		/**
		 *  Puts all selected graphs in the same tier.
		 *  Current glyph factories do not support floating the combined graphs.
		 */
		private void combineGraphs() {
			int gcount = grafs.size();
			float height = 0;

			// Note that the combo_style does not implement IFloatableTierStyle
			// because the glyph factory doesn't support floating combo graphs anyway.
			ITrackStyleExtended combo_style = null;

			Map<Color, Integer> colorMap = new HashMap<Color, Integer>();
			// If any of them already has a combo style, use that one
			for (int i = 0; i < gcount && combo_style == null; i++) {
				GraphSym gsym = grafs.get(i);
				combo_style = gsym.getGraphState().getComboStyle();
				Color col = gsym.getGraphState().getTierStyle().getBackground();
				int c = 0;
				if(colorMap.containsKey(col)){
					c = colorMap.get(col) + 1;
				}
				colorMap.put(col, c);
			}
			
			// otherwise, construct a new combo style
			if (combo_style == null) {
				combo_style = new SimpleTrackStyle("Joined Graphs", true);
				combo_style.setTrackName("Joined Graphs");
				combo_style.setExpandable(true);
			//	combo_style.setCollapsed(true);
				combo_style.setForeground(igbService.getDefaultForegroundColor());
				Color background =igbService.getDefaultBackgroundColor();
				int c = -1;
				for(Entry<Color, Integer> color : colorMap.entrySet()){
					if(color.getValue() > c){
						background = color.getKey();
					}
				}
				combo_style.setBackground(background);
			}

			// Now apply that combo style to all the selected graphs
			int i = 0;
			for (GraphSym gsym : grafs) {
				GraphState gstate = gsym.getGraphState();
				gstate.setComboStyle(combo_style, i++);
				gstate.setFloatGraph(false); // ignored since combo_style is set
				height += gsym.getGraphState().getTierStyle().getHeight();
			}
			combo_style.setHeight(height/i);

			updateViewer();
		}

		/**
		 *  Puts all selected graphs in separate tiers by setting the
		 *  combo state of each graph's state to null.
		 */
		private void splitGraphs() {
			if (grafs.isEmpty()) {
				return;
			}

			for (GraphSym gsym : grafs) {
				GraphState gstate = gsym.getGraphState();
				gstate.setComboStyle(null, 0);

				// For simplicity, set the floating state of all new tiers to false.
				// Otherwise, have to calculate valid, non-overlapping y-positions and heights.
				gstate.setFloatGraph(false); // for simplicity
			}
			updateViewer();
		}

		private void setShowAxis(boolean b) {
			for (GraphGlyph gl : glyphs) {
				gl.setShowAxis(b);
			}
			igbService.getSeqMap().updateWidget();
		}

		private void setShowLabels(boolean b) {
			for (GraphGlyph gl : glyphs) {
				gl.setShowLabel(b);
			}
			igbService.getSeqMap().updateWidget();
		}

		private void doTransformGraphs() {
			String selection = (String) transformationCB.getSelectedItem();
			FloatTransformer trans = name2transform.get(selection);
			if (trans.setParameter(paramT.getText())) {
				List<GraphSym> newgrafs = transformGraphs(grafs, trans.getDisplay(), trans);
				if (!newgrafs.isEmpty()) {
					updateViewer();
				}
			} else {
				ErrorHandler.errorPanel(MessageFormat.format(BUNDLE.getString("invalidParam"), paramT.getText(), trans.getParamPrompt()));
			}
		}

		private void floatGraphs(boolean do_float) {
			boolean something_changed = false;
			for (GraphGlyph gl : glyphs) {
				GraphState gstate = gl.getGraphState();
//				if (gstate.getComboStyle() != null) {
//					gstate.setComboStyle(null);
//					something_changed = true;
//				}
				boolean is_floating = gstate.getFloatGraph();
				if (do_float && (!is_floating)) {
					//GraphGlyphUtils.floatGraph(gl, gviewer);

					// figure out correct height
					Rectangle2D.Double coordbox = gl.getCoordBox();
					Rectangle pixbox = new Rectangle();
					igbService.getView().transformToPixels(coordbox, pixbox);
					gstate.getTierStyle().setY(pixbox.y);
					gstate.getTierStyle().setHeight(pixbox.height);

					gstate.setFloatGraph(true);
					something_changed = true;
				} else if ((!do_float) && is_floating) {
					//GraphGlyphUtils.attachGraph(gl, gviewer);

					// figure out correct height
					Rectangle2D.Double tempbox = gl.getCoordBox();  // pixels, since in PixelFloaterGlyph 1:1 mapping of pixel:coord
					Rectangle pixbox = new Rectangle((int) tempbox.x, (int) tempbox.y, (int) tempbox.width, (int) tempbox.height);
					Rectangle2D.Double coordbox = new Rectangle2D.Double();
					igbService.getView().transformToCoords(pixbox, coordbox);
					gstate.getTierStyle().setY(coordbox.y); // currently y has no effect on attached graphs, but will someday
					gstate.getTierStyle().setHeight(coordbox.height);

					gstate.setFloatGraph(false);
					something_changed = true;
				}
			}
			if (something_changed) {
				updateViewer();
			}
		}

		public void setPanelEnabled() {
			transformationGoB.setEnabled(!(grafs.isEmpty()));
			transformationCB.setEnabled(transformationGoB.isEnabled());
			paramT.setEnabled(transformationGoB.isEnabled());
			operationCB.setEnabled(grafs.size() >= 2);
			String selection = (String) operationCB.getSelectedItem();
			GraphOperator operator = name2operator.get(selection);
			boolean canGraph = (operator != null
					&& grafs.size() >= operator.getOperandCountMin()
					&& grafs.size() <= operator.getOperandCountMax());
			operationGoB.setEnabled(operationCB.isEnabled() && canGraph);
			if (canGraph || operator == null) {
				operationGoB.setToolTipText(null);
			} else {
				operationGoB.setToolTipText(GeneralUtils.getOperandMessage(grafs.size(), operator.getOperandCountMin(), operator.getOperandCountMax(), "graph"));
			}
		}
	}

	// from GraphAdjusterView
	private void deleteGraphs(GenometryModel gmodel, List<GraphSym> grafs) {
		int gcount = grafs.size();
		for (int i = 0; i < gcount; i++) {
			GraphSym graf = grafs.get(i);
			deleteGraph(gmodel, graf);
		}
		gmodel.clearSelectedSymmetries(SimpleGraphTab.class);
		igbService.getSeqMap().updateWidget();
	}

	/**
	 *  Removes a GraphSym from the annotated bio seq it is annotating (if any),
	 *     and tries to make sure the GraphSym can be garbage collected.
	 *  Tries to delete the GraphGlyph representing the GraphSym.  If the GraphSym
	 *  happens to be a child of a tier in the widget, and the tier has no children
	 *  left after deleting the graph, then delete the tier as well.
	 */
	private void deleteGraph(GenometryModel gmodel, GraphSym gsym) {
		GraphGlyph gl = (GraphGlyph) igbService.getSeqMap().getItem(gsym);

//		if (gl != null) {
//			igbService.getSeqMap().removeItem(gl);
//			// clean-up references to the graph, allowing garbage-collection, etc.
//			igbService.getSeqMapView().select(Collections.<SeqSymmetry>emptyList());
//		}
//		
//		BioSeq aseq = gsym.getGraphSeq();
//		if (aseq != null) {
//			aseq.unloadAnnotation(gsym);
//		}

		if (gl == null) {
			return;
		}

		// if this is not a floating graph, then it's in a tier,
		//    so check tier -- if this graph is only child, then get rid of the tier also
		if (!gl.getGraphState().getFloatGraph()) {

			GlyphI parentgl = gl.getParent();
			parentgl.removeChild(gl);
			if (parentgl.getChildCount() == 0) {  // if no children left in tier, then remove it
				if (isTierGlyph(parentgl)) {
					igbService.deleteGlyph(parentgl);
					igbService.packMap(false, false);
				}
			}
		} else {
			igbService.deleteGraph(gsym);
		}
	}

	private void saveGraphs(GenometryModel gmodel, List<GraphSym> grafs) {
		int gcount = grafs.size();
		if (gcount > 1) {
			// actually shouldn't get here, since save button is disabled if more than one graph
			ErrorHandler.errorPanel(SimpleGraphTab.BUNDLE.getString("graphSaveError1"));
		} else if (gcount == 1) {
			GraphSym gsym = grafs.get(0);
			try {
				GraphSaverFileChooser chooser = new GraphSaverFileChooser(gsym);
				chooser.setCurrentDirectory(FileTracker.DATA_DIR_TRACKER.getFile());
				int option = chooser.showSaveDialog(igbService.getFrame());
				if (option == JFileChooser.APPROVE_OPTION) {
					FileTracker.DATA_DIR_TRACKER.setFile(chooser.getCurrentDirectory());
					File fil = chooser.getSelectedFile();
					GraphSymUtils.writeGraphFile(gsym, gmodel.getSelectedSeqGroup(), fil.getAbsolutePath());
				}
			} catch (Exception ex) {
				ErrorHandler.errorPanel(MessageFormat.format(SimpleGraphTab.BUNDLE.getString("graphSaveError1"), ex));
			}
		}
	}

	private List<GraphSym> transformGraphs(List<GraphSym> grafs, String trans_name, FloatTransformer transformer) {
		List<GraphSym> newgrafs = new ArrayList<GraphSym>(grafs.size());
		for (GraphSym graf : grafs) {
			float[] new_ycoords;

			if (transformer instanceof IdentityTransform && graf instanceof GraphSym) {
				new_ycoords = (graf).getGraphYCoords();
			} else {
				int pcount = graf.getPointCount();
				new_ycoords = new float[pcount];
				for (int k = 0; k < pcount; k++) {
					new_ycoords[k] = transformer.transform(graf.getGraphYCoord(k));
				}
			}
			String newname = trans_name + " (" + graf.getGraphName() + ") ";

			// Transforming on this one seq only, not the whole genome
			String newid = trans_name + " (" + graf.getID() + ") ";
			newid = GraphSymUtils.getUniqueGraphID(newid, graf.getGraphSeq());
			GraphSym newgraf;
			if (graf instanceof GraphIntervalSym) {
				newgraf = new GraphIntervalSym(graf.getGraphXCoords(),
						((GraphIntervalSym) graf).getGraphWidthCoords(),
						new_ycoords, newid, graf.getGraphSeq());
			} else {
				newgraf = new GraphSym(graf.getGraphXCoords(),
						new_ycoords, newid, graf.getGraphSeq());
			}

			newgraf.setProperty(GraphSym.PROP_GRAPH_STRAND, graf.getProperty(GraphSym.PROP_GRAPH_STRAND));


			GraphState newstate = newgraf.getGraphState();
			newstate.copyProperties(graf.getGraphState());
			newstate.getTierStyle().setTrackName(newname); // this is redundant
			if (!(transformer instanceof IdentityTransform)) {
				// unless this is an identity transform, do not copy the min-max range
				newstate.setVisibleMinY(Float.NEGATIVE_INFINITY);
				newstate.setVisibleMaxY(Float.POSITIVE_INFINITY);
			}

			newgraf.getGraphSeq().addAnnotation(newgraf);
			newgrafs.add(newgraf);
		}
		return newgrafs;
	}

	private void applyFGColorChange(List<GraphSym> graf_syms, Color color) {
		for (GraphSym graf : graf_syms) {
			// using getItems() instead of getItem(), in case graph sym is represented by multiple graph glyphs
			List<GlyphI> glist = igbService.getSeqMap().getItems(graf);
			for (GlyphI g : glist) {
				GraphGlyph gl = (GraphGlyph) g;
				gl.setColor(color); // this automatically sets the GraphState color
				// if graph is in a tier, change foreground color of tier also
				//   (which in turn triggers change in color for TierLabelGlyph...)
				GlyphI glParent = gl.getParent();
				if (isTierGlyph(glParent)) {
					glParent.setForegroundColor(color);
					List<ViewI> views = glParent.getScene().getViews();
					for (ViewI v : views) {
						if (gl.withinView(v)) {
							gl.draw(v);
						}
					}
				}
			}
		}
	}

	private void applyBGColorChange(List<GraphSym> graf_syms, Color color) {
		for (GraphSym graf : graf_syms) {
			GlyphI g = igbService.getSeqMap().getItem(graf);
			GraphGlyph gl = (GraphGlyph) g;
			gl.getGraphState().getTierStyle().setBackground(color);
			ITrackStyleExtended combo = gl.getGraphState().getComboStyle();
			if(combo != null){
				combo.setBackground(color);
			}
		}
	}

	private void changeColor(List<GraphSym> graf_syms,
			com.jidesoft.combobox.ColorComboBox cbox) {
		if (graf_syms.isEmpty()) {
			return;
		}

		Color color = cbox.getSelectedColor();
		if (color != null) {
			if (cbox.equals(fgColorComboBox)) {
				applyFGColorChange(graf_syms, color);
			} else if (cbox.equals(bgColorComboBox)) {
				applyBGColorChange(graf_syms, color);
			}
		}

		igbService.getSeqMap().updateWidget();
	}

	public void fgColorComboBoxActionPerformed() {
		changeColor(grafs, fgColorComboBox);
	}

	public void bgColorComboBoxActionPerformed() {
		changeColor(grafs, bgColorComboBox);
	}
}
