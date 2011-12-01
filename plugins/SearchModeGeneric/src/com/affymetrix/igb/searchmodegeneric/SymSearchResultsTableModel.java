package com.affymetrix.igb.searchmodegeneric;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.das2.SimpleDas2Feature;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SymWithProps;
import com.affymetrix.genometryImpl.symmetry.UcscPslSym;
import com.affymetrix.igb.shared.SearchResultsTableModel;

public class SymSearchResultsTableModel extends SearchResultsTableModel {
	private static final long serialVersionUID = 1L;
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("searchmodegeneric");
	private final int[] colWidth = {};
	private final int[] colAlign = {};
	
	protected final List<SeqSymmetry> tableRows = new ArrayList<SeqSymmetry>(0);
	
	private final String[] column_names = {
		BUNDLE.getString("searchTableID"),
		BUNDLE.getString("searchTableTier"),
		BUNDLE.getString("searchTableGeneName"),
		BUNDLE.getString("searchTableStart"),
		BUNDLE.getString("searchTableEnd"),
		BUNDLE.getString("searchTableChromosome"),
		BUNDLE.getString("searchTableStrand")
	};
	private static final int ID_COLUMN = 0;
	private static final int TIER_COLUMN = 1;
	private static final int GENE_NAME_COLUMN = 2;
	private static final int START_COLUMN = 3;
	private static final int END_COLUMN = 4;
	private static final int CHROM_COLUMN = 5;
	private static final int STRAND_COLUMN = 6;

	public SymSearchResultsTableModel(List<SeqSymmetry> results) {
		tableRows.addAll(results);
	}

	public Object getValueAt(int row, int col) {
		SeqSymmetry sym = tableRows.get(row);
		SeqSpan span = sym.getSpan(0);
		switch (col) {
			case ID_COLUMN:
				return sym.getID();
			case TIER_COLUMN:
				return BioSeq.determineMethod(sym);
			case GENE_NAME_COLUMN:
				if (sym instanceof SimpleDas2Feature) {
					String geneName = ((SimpleDas2Feature)sym).getName();
					return geneName == null ? "" : geneName;
				}
				if (sym instanceof SymWithProps) {
					String geneName = (String)((SymWithProps)sym).getProperty("gene name");
					return geneName == null ? "" : geneName;
				}
				return "";
			case START_COLUMN:
				if (sym instanceof UcscPslSym) {
					return (((UcscPslSym) sym).getSameOrientation()) ? 
						((UcscPslSym) sym).getTargetMin() : ((UcscPslSym) sym).getTargetMax();
				}
				return (span == null ? "" : span.getStart());
			case END_COLUMN:
				if (sym instanceof UcscPslSym) {
					return (((UcscPslSym) sym).getSameOrientation()) ?
						((UcscPslSym) sym).getTargetMax() : ((UcscPslSym) sym).getTargetMin();
				}
				return (span == null ? "" : span.getEnd());
			case CHROM_COLUMN:
				if (sym instanceof UcscPslSym) {
					return ((UcscPslSym) sym).getTargetSeq().getID();
				}
				return ((span == null || span.getBioSeq() == null) ? "" : span.getBioSeq().getID());
			case STRAND_COLUMN:
				if (sym instanceof UcscPslSym) {
					return (
							(((UcscPslSym) sym).getSameOrientation())
							? "+" : "-");
				}
				if (span == null) {
					return "";
				}
				return (span.isForward() ? "+" : "-");
		}
		return "";
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
		
	public int getColumnCount() {
		return column_names.length;
	}

	@Override
	public String getColumnName(int col) {
		return column_names[col];
	}

	public int getRowCount() {
		return tableRows.size();
	}

	@Override
	public Class<?> getColumnClass(int column) {
		if(column == START_COLUMN || column == END_COLUMN) {
			return Number.class;
		}
		return String.class;
	}

	@Override
	public SeqSymmetry get(int i) {
		return tableRows.get(i);
	}

	@Override
	public void clear(){
		tableRows.clear();
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
