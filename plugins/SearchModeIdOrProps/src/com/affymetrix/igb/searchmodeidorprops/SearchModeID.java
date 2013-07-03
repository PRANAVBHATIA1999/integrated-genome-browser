package com.affymetrix.igb.searchmodeidorprops;

import java.text.MessageFormat;
import java.util.List;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.util.ServerTypeI;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.ISearchHints;
import com.affymetrix.igb.shared.ISearchModeExtended;
import com.affymetrix.igb.shared.ISearchModeSym;
import com.affymetrix.igb.shared.IStatus;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SearchModeID extends SearchModeIDOrProps implements ISearchModeSym, ISearchModeExtended, ISearchHints {
	private static final int SEARCH_ALL_ORDINAL = -9000;
	private static final String REMOTESERVERSEARCH = BUNDLE.getString("optionCheckBox");
	private static final String REMOTESERVERSEARCHTOOLTIP = BUNDLE.getString("optionCheckBoxTT");
	private static final String REMOTESERVERSEARCHSINGULAR = BUNDLE.getString("remoteServerSearchSingular");
	private static final String REMOTESERVERSEARCHPLURAL = BUNDLE.getString("remoteServerSearchPlural");
	private boolean optionSelected;
	
	public SearchModeID(IGBService igbService) {
		super(igbService);
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
	public boolean useGenomeInSeqList() {
		return true;
	}
			
	@Override
	public String getOptionName() {
		int i = getRemoteServerCount();
		String remoteServerPluralText = i == 1 ? REMOTESERVERSEARCHSINGULAR : REMOTESERVERSEARCHPLURAL;
		return MessageFormat.format(REMOTESERVERSEARCH, "" + i, remoteServerPluralText);
	}

	@Override
	public String getOptionTooltip() {
		int i = getRemoteServerCount();
		String remoteServerPluralText = i == 1 ? REMOTESERVERSEARCHSINGULAR : REMOTESERVERSEARCHPLURAL;
		return MessageFormat.format(REMOTESERVERSEARCHTOOLTIP, "" + i, remoteServerPluralText);
	}
	
	@Override
	public boolean getOptionEnable() {
		int i = getRemoteServerCount();
		return i > 0;
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
	public int searchAllUse() {
		return SEARCH_ALL_ORDINAL;
 	}

	@Override
	public List<SeqSymmetry> search(String search_text, final BioSeq chrFilter, IStatus statusHolder, boolean option) {
		return search(search_text, chrFilter, statusHolder, option, false);
	}

	@Override
	public Set<String> search(String search_term) {
		String regexText = search_term;
		if (!(regexText.contains("*") || regexText.contains("^") || regexText.contains("$"))) {
			// Not much of a regular expression.  Assume the user wants to match at the start and end
			regexText = ".*" + regexText + ".*";
		}
		Pattern regex = Pattern.compile(regexText, Pattern.CASE_INSENSITIVE);
		
		Set<String> results = new HashSet<String>();
		
		GenometryModel.getGenometryModel().getSelectedSeqGroup().searchHints(results, regex, 20);
		
		return results;
	}
	
	private int getRemoteServerCount() {
		AnnotatedSeqGroup group = GenometryModel.getGenometryModel().getSelectedSeqGroup();
		if (group == null) {
			return 0;
		}
		int count = 0;
		for (GenericVersion gVersion : group.getEnabledVersions()) {
			if (gVersion.gServer.serverType == ServerTypeI.DAS2) {
				count++;
			}
		}
		return count;
	}
}
