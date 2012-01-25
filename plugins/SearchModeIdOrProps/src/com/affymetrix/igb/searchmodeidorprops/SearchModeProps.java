package com.affymetrix.igb.searchmodeidorprops;

import java.util.List;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.ISearchModeSym;
import com.affymetrix.igb.shared.IStatus;

public class SearchModeProps extends SearchModeIDOrProps implements ISearchModeSym {
	private static final int SEARCH_ALL_ORDINAL = 3000;
	public SearchModeProps(IGBService igbService) {
		super(igbService);
	}

	@Override
	public String getName() {
		return BUNDLE.getString("searchRegexProps");
	}

	@Override
	public String getTooltip() {
		return BUNDLE.getString("searchRegexPropsTF");
	}

	public String getOptionName(int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public String getOptionTooltip(int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public boolean getOptionEnable(int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public boolean useOption() {
		return false;
	}

	@Override
	public boolean useGenomeInSeqList() {
		return true;
	}

	@Override
	public int searchAllUse() {
		return SEARCH_ALL_ORDINAL;
	}

	@Override
	public List<SeqSymmetry> search(String search_text, final BioSeq chrFilter, IStatus statusHolder, boolean option) {
		return search(search_text, chrFilter, statusHolder, option, true);
	}
}
