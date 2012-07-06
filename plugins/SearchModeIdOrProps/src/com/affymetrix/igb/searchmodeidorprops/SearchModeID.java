package com.affymetrix.igb.searchmodeidorprops;

import java.text.MessageFormat;
import java.util.List;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.util.Constants;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.ISearchMode;
import com.affymetrix.igb.shared.IStatus;
import com.affymetrix.igb.shared.SearchResultsTableModel;

public class SearchModeID extends SearchModeIDOrProps implements ISearchMode {
	private static final String REMOTESERVERSEARCH = BUNDLE.getString("optionCheckBox");
	private static final String REMOTESERVERSEARCHTOOLTIP = BUNDLE.getString("optionCheckBoxTT");
	private static final String REMOTESERVERSEARCHSINGULAR = BUNDLE.getString("remoteServerSearchSingular");
	private static final String REMOTESERVERSEARCHPLURAL = BUNDLE.getString("remoteServerSearchPlural");
	private boolean optionSelected;
	
	public SearchModeID(IGBService igbService) {
		super(igbService);
	}

	@Override
	public SearchResultsTableModel run(String search_text, BioSeq chrFilter, String seq, final boolean remote, IStatus statusHolder) {
		return run(search_text, chrFilter, seq, false, remote, statusHolder);
	}

	@Override
	public String getName() {
		return BUNDLE.getString("searchRegexIDOrName");
	}

	@Override
	public String getTooltip() {
		return BUNDLE.getString("searchRegexIDOrNameTF");
	}

	@Override
	public String getOptionName(int i) {
		String remoteServerPluralText = i == 1 ? REMOTESERVERSEARCHSINGULAR : REMOTESERVERSEARCHPLURAL;
		return MessageFormat.format(REMOTESERVERSEARCH, "" + i, remoteServerPluralText);
	}

	@Override
	public String getOptionTooltip(int i) {
		String remoteServerPluralText = i == 1 ? REMOTESERVERSEARCHSINGULAR : REMOTESERVERSEARCHPLURAL;
		return MessageFormat.format(REMOTESERVERSEARCHTOOLTIP, "" + i, remoteServerPluralText);
	}
	
	@Override
	public boolean getOptionEnable(int i) {
		return i > 0;
	}
	
	@Override
	public boolean useOption() {
		return true;
	}

	@Override
	public void setOptionState(boolean selected){
		optionSelected = selected;
	}
	
	@Override
	public boolean getOptionState(){
		return optionSelected;
	}
	
	@Override
	public boolean useDisplaySelected() {
		return true;
	}

	@Override
	public boolean useGenomeInSeqList() {
		return true;
	}

	@Override
	public List<SeqSpan> findSpans(String search_text, SeqSpan visibleSpan) {
		return findSpans(findLocalSyms(search_text, null, Constants.GENOME_SEQ_ID, false, DUMMY_STATUS));
	}

	@Override
	public List<SeqSymmetry> search(String search_text, final BioSeq chrFilter, IStatus statusHolder) {
		return findLocalSyms(search_text, chrFilter, (chrFilter == null) ? "genome" : chrFilter.getID(), false, statusHolder);
	}
}
