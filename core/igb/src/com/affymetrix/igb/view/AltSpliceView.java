/**
 * Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.view;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.swing.recordplayback.JRPCheckBox;
import com.affymetrix.genoviz.swing.recordplayback.JRPTextField;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.event.*;
import com.affymetrix.genometryImpl.symmetry.GraphSym;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genoviz.swing.recordplayback.JRPNumTextField;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.IGBServiceImpl;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.tiers.TierLabelManager;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

public class AltSpliceView extends IGBTabPanel
		implements ActionListener, ComponentListener, ItemListener,
		SymSelectionListener, SeqSelectionListener, PreferenceChangeListener,
		TierLabelManager.PopupListener ,SeqMapRefreshed{

	private static AltSpliceView singleton;
	private static final long serialVersionUID = 1L;
	private static final int TAB_POSITION = 3;
	private final AltSpliceSeqMapView spliced_view;
	private final OrfAnalyzer orf_analyzer;
	private final JRPTextField buffer_sizeTF;
	private final JLabel buffer_sizeL;
	private final JRPCheckBox slice_by_selectionCB;
	private List<SeqSymmetry> last_selected_syms = new ArrayList<SeqSymmetry>();
	private BioSeq last_seq_changed = null;
	private boolean pending_sequence_change = false;
	private boolean pending_selection_change = false;
	private boolean slice_by_selection_on = true;

	public static AltSpliceView getSingleton() {
		if (singleton == null) {
			singleton = new AltSpliceView(IGBServiceImpl.getInstance());
		}

		return singleton;
	}

	public AltSpliceView(IGBService igbService) {
		super(igbService, BUNDLE.getString("slicedViewTab"), BUNDLE.getString("slicedViewTab"), false, TAB_POSITION);
		this.setLayout(new BorderLayout());
		spliced_view = new AltSpliceSeqMapView(false);
		spliced_view.subselectSequence = false;
		orf_analyzer = new OrfAnalyzer(spliced_view);
		buffer_sizeTF = new JRPNumTextField("AltSpliceView_buffer_size", 4);
		buffer_sizeTF.setText("" + spliced_view.getSliceBuffer());
		slice_by_selectionCB = new JRPCheckBox("AltSpliceView_slice_by_selectionCB", "Slice By Selection", true);

		JPanel buf_adjustP = new JPanel(new FlowLayout());
		buffer_sizeL = new JLabel("Slice Buffer: ");
		buf_adjustP.add(buffer_sizeL);
		buf_adjustP.add(buffer_sizeTF);

		JPanel pan1 = new JPanel(new GridLayout(1, 2));

		pan1.add(slice_by_selectionCB);
		pan1.add(buf_adjustP);
		JPanel options_panel = new JPanel(new BorderLayout());

		options_panel.add("West", pan1);
		options_panel.add("East", orf_analyzer);
		JSplitPane splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitpane.setResizeWeight(1);  // allocate as much space as possible to top panel
		splitpane.setDividerSize(8);
		splitpane.setTopComponent(spliced_view);
		splitpane.setBottomComponent(options_panel);
		this.add("Center", splitpane);

		this.addComponentListener(this);
		buffer_sizeTF.addActionListener(this);
		slice_by_selectionCB.addItemListener(this);

		spliced_view.setAnnotatedSeq(GenometryModel.getGenometryModel().getSelectedSeq());
		GenometryModel.getGenometryModel().addSeqSelectionListener(this);
		GenometryModel.getGenometryModel().addSymSelectionListener(this);
		PreferenceUtils.getTopNode().addPreferenceChangeListener(this);

		TierLabelManager tlman = spliced_view.getTierManager();
		if (tlman != null) {
			tlman.addPopupListener(this);
		}
		IGB.getSingleton().getMapView().addToRefreshList(this);
		resetAll();
	}

	/**
	 * This method is notified when selected symmetries change. It usually
	 * triggers a re-computation of the sliced symmetries to draw. If no
	 * selected syms, then don't change. Any Graphs in the selected symmetries
	 * will be ignored (because graphs currently span entire sequence and
	 * slicing on them can use too much memory).
	 */
	public void symSelectionChanged(SymSelectionEvent evt) {
		if (IGBService.DEBUG_EVENTS) {
			System.out.println("AltSpliceView received selection changed event");
		}
		Object src = evt.getSource();
		// ignore if symmetry selection originated from this AltSpliceView -- don't want to
		//   reslice based on internal selection!
		if ((src != this) && (src != spliced_view)) {
			// catching spliced_view as source of event because currently sym selection events actually originating
			//    from AltSpliceView have their source set to the AltSpliceView's internal SeqMapView...
			last_selected_syms = evt.getSelectedGraphSyms();
			last_selected_syms = removeGraphs(last_selected_syms);
			if (last_selected_syms.size() > 0) {
				if (!this.isShowing()) {
					pending_selection_change = true;
				} else if (slice_by_selection_on) {
					this.sliceAndDice(last_selected_syms);
					pending_selection_change = false;
				} else {
					spliced_view.select(last_selected_syms, false);
					pending_selection_change = false;
				}
			}
		}
	}

	/**
	 * Takes a list of SeqSymmetries and removes any GraphSyms from it.
	 */
	private static List<SeqSymmetry> removeGraphs(List<SeqSymmetry> syms) {
		List<SeqSymmetry> v = new ArrayList<SeqSymmetry>(syms.size());
		for (SeqSymmetry sym : syms) {
			if (!(sym instanceof GraphSym)) {
				v.add(sym);
			}
		}
		return v;
	}

	public void seqSelectionChanged(SeqSelectionEvent evt) {
		if (IGBService.DEBUG_EVENTS) {
			System.out.println("AltSpliceView received SeqSelectionEvent, selected seq: " + evt.getSelectedSeq());
		}
		BioSeq newseq = GenometryModel.getGenometryModel().getSelectedSeq();
		if (last_seq_changed != newseq) {
			last_seq_changed = newseq;
			if (this.isShowing() && slice_by_selection_on) {
				spliced_view.setAnnotatedSeq(last_seq_changed);
				pending_sequence_change = false;
			} else {
				pending_sequence_change = true;
			}
		}
	}

	private void setSliceBySelection(boolean b) {
		slice_by_selection_on = b;
	}

	public void setSliceBuffer(int buf_size) {
		buffer_sizeTF.setText(String.valueOf(buf_size));
		spliced_view.setSliceBuffer(buf_size, 
				new Runnable() {

				public void run() {
					orf_analyzer.redoOrfs();
				}
			}
		);
	}

	private void sliceAndDice(List<SeqSymmetry> syms) {
		if (syms.size() > 0) {
			spliced_view.sliceAndDice(syms,
				new Runnable() {

				public void run() {
					orf_analyzer.redoOrfs();
				}
			});
		}
	}

	// ComponentListener implementation
	public void componentResized(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
		if (pending_sequence_change && slice_by_selection_on) {
			spliced_view.setAnnotatedSeq(last_seq_changed);
			pending_sequence_change = false;
		}
		if (pending_selection_change) {
			if (slice_by_selection_on) {
				this.sliceAndDice(last_selected_syms);
			} else {
				spliced_view.select(last_selected_syms, false);
			}
			pending_selection_change = false;
		}
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void actionPerformed(ActionEvent evt) {
		Object src = evt.getSource();
		if (src == buffer_sizeTF) {
			String str = buffer_sizeTF.getText();
			if (str != null) {
				try {
					int new_buf_size = Integer.parseInt(str);
					this.setSliceBuffer(new_buf_size);
				} catch (NumberFormatException e) {
					//do nothing
				}
			}
		}
	}

	public void itemStateChanged(ItemEvent evt) {
		Object src = evt.getSource();
		if (src == slice_by_selectionCB) {
			setSliceBySelection(evt.getStateChange() == ItemEvent.SELECTED);
		}
	}

	public void popupNotify(JPopupMenu popup, final TierLabelManager handler) {
		if (handler != spliced_view.getTierManager()) {
			return;
		}

		Action hide_action = new GenericAction("Hide Tier", null, null) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				super.actionPerformed(e);
				spliced_view.doEdgeMatching(Collections.<GlyphI>emptyList(), false);
				handler.hideTiers(handler.getSelectedTierLabels(), false, true);
			}
		};

		Action restore_all_action = new GenericAction("Show All", null, null) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				super.actionPerformed(e);
				// undo all edge-matching, because packing will behave badly otherwise.
				spliced_view.doEdgeMatching(Collections.<GlyphI>emptyList(), false);
				handler.showTiers(handler.getAllTierLabels(), true, true);
			}
		};

		hide_action.setEnabled(!handler.getSelectedTierLabels().isEmpty());
		restore_all_action.setEnabled(true);

		if (popup.getComponentCount() > 0) {
			popup.add(new JSeparator());
		}
		popup.add(hide_action);
		popup.add(restore_all_action);
	}

	public SeqMapView getSplicedView() {
		return spliced_view;
	}

	@Override
	public boolean isEmbedded() {
		return true;
	}

	public void preferenceChange(PreferenceChangeEvent evt) {
		if (!evt.getNode().equals(PreferenceUtils.getTopNode())
				|| !this.isShowing()) {
			return;
		}

		if (evt.getKey().equals(OrfAnalyzer.PREF_STOP_CODON_COLOR)
				|| evt.getKey().equals(OrfAnalyzer.PREF_DYNAMIC_ORF_COLOR)
				|| evt.getKey().equals(OrfAnalyzer.PREF_BACKGROUND_COLOR)) {
			// Each time changed the color, it would triger this method twice and caused a concurrent modification exception 
			ThreadUtils.runOnEventQueue(new Runnable() {

				public void run() {
					orf_analyzer.redoOrfs();
				}
			});
		}
	}

	public JRPTextField getBufferSizeTF() {
		return this.buffer_sizeTF;
	}

	public void refreshView() {
		orf_analyzer.redoOrfs();
	}
	
	protected final void resetAll(){
		boolean enable = igbService != null && igbService.getVisibleTierGlyphs() != null && igbService.getVisibleTierGlyphs().size() > 1;
		buffer_sizeL.setEnabled(enable);
		buffer_sizeTF.setEnabled(enable);
		orf_analyzer.setEnabled(enable);
		slice_by_selectionCB.setEnabled(enable);
		spliced_view.enableSeqMap(enable);
		orf_analyzer.enableView(enable);
	}

	public void mapRefresh() {
		resetAll();
	}
}
