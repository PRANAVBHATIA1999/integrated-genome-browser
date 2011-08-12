package com.affymetrix.igb.search.mode;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.SwingConstants;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.util.DNAUtils;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.search.IStatus;
import com.affymetrix.igb.search.SearchView;

public class SearchModeResidue implements ISearchMode {
	private static final int MAX_RESIDUE_LEN_SEARCH = 1000000;
	private static final Color hitcolor = new Color(150, 150, 255);
	private IGBService igbService;

	@SuppressWarnings("serial")
	private class GlyphSearchResultsTableModel extends SearchResultsTableModel {
		private final int[] colWidth = {10,10,5,10,65};
		private final int[] colAlign = {SwingConstants.RIGHT,SwingConstants.RIGHT,SwingConstants.CENTER,SwingConstants.CENTER,SwingConstants.LEFT};
		
		private final List<GlyphI> tableRows = new ArrayList<GlyphI>(0);
		protected final String seq;

		public GlyphSearchResultsTableModel(List<GlyphI> results, String seq) {
			tableRows.addAll(results);
			this.seq = seq;
		}

		private final String[] column_names = {
			SearchView.BUNDLE.getString("searchTableStart"),
			SearchView.BUNDLE.getString("searchTableEnd"),
			SearchView.BUNDLE.getString("searchTableStrand"),
			SearchView.BUNDLE.getString("searchTableChromosome"),
			SearchView.BUNDLE.getString("searchTableMatch")
		};

		private static final int START_COLUMN = 0;
		private static final int END_COLUMN = 1;
		private static final int STRAND_COLUMN = 2;
		private static final int CHROM_COLUMN = 3;
		private static final int MATCH_COLUMN = 4;

		@Override
		public GlyphI get(int i) {
			return tableRows.get(i);
		}

		@Override
		public void clear() {
			tableRows.clear();
		}

		public int getRowCount() {
			return tableRows.size();
		}

		public int getColumnCount() {
			return column_names.length;
		}

		@SuppressWarnings("unchecked")
		public Object getValueAt(int row, int col) {
			GlyphI glyph = tableRows.get(row);
			Map<Object, Object> map = (Map<Object, Object>) glyph.getInfo();

			switch (col) {
			
				case START_COLUMN:
					return (int)glyph.getCoordBox().x;

				case END_COLUMN:
					return (int)(glyph.getCoordBox().x  + glyph.getCoordBox().width);

				case STRAND_COLUMN:
					Object direction = map.get("direction");
					if (direction != null) {
						if (direction.toString().equalsIgnoreCase("forward")) {
							return "+";
						} else if (direction.toString().equalsIgnoreCase("reverse")) {
							return "-";
						}
					}
					return "";

				case CHROM_COLUMN:
					return seq;
					
				case MATCH_COLUMN:
					Object match = map.get("match");
					if (match != null) {
						return match.toString();
					}
				return "";
			}

			return "";
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

		@Override
		public String getColumnName(int col) {
			return column_names[col];
		}
		
		@Override
		public Class<?> getColumnClass(int column) {
			if(column == START_COLUMN || column == END_COLUMN) {
				return Number.class;
			}
			return String.class;
		}

		@Override
		public int[] getColumnWidth() {
			return colWidth;
		}

		@Override
		public int[] getColumnAlign() {
			return colAlign;
		}

	}
	public SearchModeResidue(IGBService igbService) {
		super();
		this.igbService = igbService;
	}

	public boolean checkInput(String search_text, final BioSeq vseq, final String seq) {
		if (vseq == null ) {
			ErrorHandler.errorPanel(
					"Residues for " + seq + " not available.  Please load residues before searching.");
			return false;
		}
		if (search_text.length() < 3) {
			ErrorHandler.errorPanel("Search must contain at least 3 characters");
			return false;
		}
		try {
			Pattern.compile(search_text, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException pse) {
			ErrorHandler.errorPanel("Regular expression syntax error...\n" + pse.getMessage());
			return false;
		} catch (Exception ex) {
			ErrorHandler.errorPanel("Problem with regular expression...", ex);
			return false;
		}
		GenometryModel gmodel = GenometryModel.getGenometryModel();

		if (vseq != igbService.getSeqMapView().getAnnotatedSeq()){
			boolean confirm = igbService.confirmPanel("Sequence " + vseq.getID() +
					" is not same as selected sequence " + igbService.getSeqMapView().getAnnotatedSeq().getID() +
					". \nPlease select the sequence before proceeding." +
					"\nDo you want to select sequence now ?");
			if(!confirm)
				return false;
			SeqSpan viewspan = igbService.getSeqMapView().getVisibleSpan();
			int min = Math.max((viewspan.getMin() > vseq.getMax() ? -1 : viewspan.getMin()), vseq.getMin());
			int max = Math.min(viewspan.getMax(), vseq.getMax());
			SeqSpan newspan = new SimpleSeqSpan(min, max, vseq);
			gmodel.setSelectedSeq(vseq);
			igbService.getSeqMapView().zoomTo(newspan);
		}

		boolean isComplete = vseq.isComplete();
		boolean confirm = isComplete ? true : igbService.confirmPanel("Residues for " + seq
							+ " not loaded.  \nDo you want to load residues?");
		if (!confirm) {
			return false;
		}
		return true;
	}

	@Override
	public void finished(BioSeq vseq) {
		boolean isComplete = vseq.isComplete();
		if (!isComplete) {
			igbService.getSeqMapView().setAnnotatedSeq(vseq, true, true, true);
		}
	}
	public SearchResultsTableModel getEmptyTableModel() {
		return new GlyphSearchResultsTableModel(Collections.<GlyphI>emptyList(),"");
	}

	/**
	 * Display (highlight on SeqMap) the residues matching the specified regex.
	 */
	public SearchResultsTableModel run(String search_text, BioSeq chrFilter, String seq, boolean remote, IStatus statusHolder, List<GlyphI> glyphs) {
		boolean isComplete = chrFilter.isComplete();
		if (!isComplete) {
			igbService.loadResidues(igbService.getSeqMapView().getVisibleSpan(), true);
		}
		String friendlySearchStr = SearchModeHolder.friendlyString(search_text, chrFilter.getID());
		Pattern regex = null;
		try {
			regex = Pattern.compile(search_text, Pattern.CASE_INSENSITIVE);
		} catch (Exception ex) { // should not happen already checked above
			return null;
		}

		statusHolder.setStatus(friendlySearchStr + ": Working...");

		int residuesLength = chrFilter.getLength();
		int hit_count1 = 0;
		int hit_count2 = 0;
		int residue_offset1 = chrFilter.getMin();
		int residue_offset2 = chrFilter.getMax();
		Thread current_thread = Thread.currentThread();
		
		for(int i=0; i<residuesLength; i+=MAX_RESIDUE_LEN_SEARCH){
			if(current_thread.isInterrupted())
				break;
			
			int start = Math.max(i-search_text.length(), 0);
			int end = Math.min(i+MAX_RESIDUE_LEN_SEARCH, residuesLength);
			
			String residues = chrFilter.getResidues(start, end);
			hit_count1 += igbService.searchForRegexInResidues(true, regex, residues, Math.max(residue_offset1,start), glyphs, hitcolor);

			// Search for reverse complement of query string
			// flip searchstring around, and redo nibseq search...
			String rev_searchstring = DNAUtils.reverseComplement(residues);
			hit_count2 += igbService.searchForRegexInResidues(false, regex, rev_searchstring, Math.min(residue_offset2,end), glyphs, hitcolor);
		}

		statusHolder.setStatus("Found " + ": " + hit_count1 + " forward and " + hit_count2 + " reverse strand hits. Click row to view hit.");
		igbService.getSeqMap().updateWidget();

		Collections.sort(glyphs, new Comparator<GlyphI>() {
			public int compare(GlyphI g1, GlyphI g2) {
				return Integer.valueOf((int)g1.getCoordBox().x).compareTo((int)g2.getCoordBox().x);
			}
		});
		return new GlyphSearchResultsTableModel(glyphs, chrFilter.getID());
	}

	@Override
	public String getName() {
		return SearchView.BUNDLE.getString("searchRegexResidue");
	}

	@Override
	public String getTooltip() {
		return SearchView.BUNDLE.getString("searchRegexResidueTF");
	}

	@Override
	public void valueChanged(SearchResultsTableModel model, int srow, List<GlyphI> glyphs) {
		GlyphI glyph = ((GlyphSearchResultsTableModel)model).get(srow);
		for(GlyphI g : glyphs){
			igbService.getSeqMap().deselect(g);
		}
		if(glyph != null){
			int start = (int)glyph.getCoordBox().x;
			int end = (int)(glyph.getCoordBox().x + glyph.getCoordBox().width);
			igbService.getSeqMap().select(glyph);
			igbService.zoomToCoord(((GlyphSearchResultsTableModel)model).seq, start, end);
			igbService.getSeqMapView().centerAtHairline();
		}
	}

	@Override
	public boolean useRemote() {
		return false;
	}

	@Override
	public boolean useDisplaySelected() {
		return false;
	}

	@Override
	public boolean useGenomeInSeqList() {
		return false;
	}
}
