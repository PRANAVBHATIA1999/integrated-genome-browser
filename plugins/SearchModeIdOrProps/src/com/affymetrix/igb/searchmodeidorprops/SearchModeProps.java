package com.affymetrix.igb.searchmodeidorprops;


import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.IKeyWordSearch;
import com.affymetrix.igb.shared.IStatus;
import com.affymetrix.igb.shared.SearchResults;

public class SearchModeProps extends SearchModeIDOrProps implements IKeyWordSearch {
	private static final int SEARCH_ALL_ORDINAL = -8000;
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
		
	@Override
	public boolean useGenomeInSeqList() {
		return true;
	}

	@Override
	public int searchAllUse() {
		return SEARCH_ALL_ORDINAL;
	}

	@Override
	public SearchResults search(String search_text, final BioSeq chrFilter, IStatus statusHolder, boolean option) {
		return search(search_text, chrFilter, statusHolder, option, true);
	}
}
