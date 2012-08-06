package com.gene.findannotations;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.TypeContainerAnnot;
import com.affymetrix.genoviz.bioviews.Glyph;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.DummyRootSeqSymmetry;
import com.affymetrix.igb.shared.ISearchModeSym;
import com.affymetrix.igb.shared.IStatus;
import com.affymetrix.igb.shared.TierGlyph;

public class FindAnnotationsAction extends GenericAction {
	private static final long serialVersionUID = 1L;
	private final IGBService igbService;
	private final JTextField textField;
	private final JCheckBox selectedTracksCB;
	private final JTable resultsTable;
	private final JButton trackFromHitsButton;
	private final IStatus status;

	public FindAnnotationsAction(IGBService igbService, JTextField textField, JCheckBox selectedTracksCB, JTable resultsTable, JButton trackFromHitsButton, IStatus status) {
		super(null, null, null);
		this.igbService = igbService;
		this.textField = textField;
		this.selectedTracksCB = selectedTracksCB;
		this.resultsTable = resultsTable;
		this.trackFromHitsButton = trackFromHitsButton;
		this.status = status;
	}

	private List<TypeContainerAnnot> getTrackSyms() {
		List<TypeContainerAnnot> trackSyms = new ArrayList<TypeContainerAnnot>();
		List<Glyph> glyphs;
		if (selectedTracksCB.isSelected()) {
			glyphs = igbService.getSelectedTierGlyphs();
		}
		else {
			glyphs = igbService.getVisibleTierGlyphs();
		}
		for (Glyph tierGlyph : glyphs) {
			Object info = tierGlyph.getInfo();
			if ((info == null || info instanceof DummyRootSeqSymmetry) && tierGlyph instanceof TierGlyph) {
				String type = ((TierGlyph)tierGlyph).getAnnotStyle().getMethodName();
				if (type != null) {
					trackSyms.add(new TypeContainerAnnot(type));
				}
			}
			else if (info instanceof TypeContainerAnnot) {
				trackSyms.add((TypeContainerAnnot)info);
			}
		}
		return trackSyms;
	}

	private List<SeqSymmetry> searchTrack(ISearchModeSym searchMode, TypeContainerAnnot trackSym, String searchText) {
		List<SeqSymmetry> results = null;
		if (searchMode.searchAllUse() >= 0) {
			status.setStatus(MessageFormat.format(FindAnnotationsView.BUNDLE.getString("findannotationsSearching"), trackSym.getType(), searchMode.getName()));
			Date start = new Date();
			try {
				String errorMessage = searchMode.checkInput(searchText, null, null);
				if (errorMessage != null) {
					status.setStatus(errorMessage);
				}
				else {
					results = searchMode.searchTrack(searchText, null, trackSym, status, false);
					Date end = new Date();
					if (results == null) {
						status.setStatus(MessageFormat.format(FindAnnotationsView.BUNDLE.getString("findannotationsResults"), searchMode.getName(), trackSym.getType(), "0", "" + ((end.getTime() - start.getTime()) / 1000)));
					}
					else {
						status.setStatus(MessageFormat.format(FindAnnotationsView.BUNDLE.getString("findannotationsResults"), searchMode.getName(), trackSym.getType(), "" + results.size(), "" + ((end.getTime() - start.getTime()) / 1000)));
					}
				}
			}
			catch (Exception x) {
				status.setStatus(MessageFormat.format(FindAnnotationsView.BUNDLE.getString("findannotationsSearchError"), searchMode.getName(), trackSym.getType(), x.getMessage()));
			}
		}
		return results;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		final String searchText = textField.getText();
		((AnnotationsTableModel)resultsTable.getModel()).setResults(searchText, new ArrayList<SeqSymmetry>()); // clear table while loading data
		resultsTable.setRowSorter(new TableRowSorter<TableModel>(resultsTable.getModel()));
		((AnnotationsTableModel)resultsTable.getModel()).fireTableDataChanged();
		final List<TypeContainerAnnot> trackSyms = getTrackSyms();
		new SwingWorker<Set<SeqSymmetry>, Void>() {
			@Override
			protected Set<SeqSymmetry> doInBackground() throws Exception {
				Set<SeqSymmetry> results = new HashSet<SeqSymmetry>(); // use Set to eliminate duplicates
				Date totalStart = new Date();
				List<ISearchModeSym> searchModes = ExtensionPointHandler.getExtensionPoint(ISearchModeSym.class).getExtensionPointImpls();
				Collections.sort(searchModes,
					new Comparator<ISearchModeSym>() {
						@Override
						public int compare(ISearchModeSym o1, ISearchModeSym o2) {
							return o1.searchAllUse() - o2.searchAllUse();
						}
					}
				);
				for (TypeContainerAnnot trackSym : trackSyms) {
					List<SeqSymmetry> searchResults = null;
					for (ISearchModeSym searchMode : searchModes) {
						searchResults = searchTrack(searchMode, trackSym, searchText);
						if (searchResults != null) {
							break;
						}
					}
					if (searchResults != null) {
						results.addAll(searchResults);
					}
				}
				Date totalEnd = new Date();
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, "search for " + searchText + " took " + ((totalEnd.getTime() - totalStart.getTime()) / 1000) + " seconds");
				return results;
			}
			@Override
		    public void done() {
				try {
					Set<SeqSymmetry> results = get();
					List<SeqSymmetry> sortedResults = new ArrayList<SeqSymmetry>(results);
					((AnnotationsTableModel)resultsTable.getModel()).setResults(searchText, sortedResults);
					resultsTable.setRowSorter(new TableRowSorter<TableModel>(resultsTable.getModel()));
					((AnnotationsTableModel)resultsTable.getModel()).fireTableStructureChanged();
					trackFromHitsButton.setEnabled(results.size() > 0);
					status.setStatus(MessageFormat.format(FindAnnotationsView.BUNDLE.getString("findannotationsComplete"), "" + results.size()));
				}
				catch (Exception x) {
					status.setStatus(MessageFormat.format(FindAnnotationsView.BUNDLE.getString("findannotationsException"), x.getMessage()));
				}
		    }			
		}.execute();
	}
}
