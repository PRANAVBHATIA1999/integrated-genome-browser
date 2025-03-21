package com.affymetrix.igb.searchmodeidorprops;

import org.osgi.service.component.annotations.Component;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import org.lorainelab.igb.services.search.ISearchHints;
import org.lorainelab.igb.services.search.ISearchMode;
import org.lorainelab.igb.services.search.ISearchModeExtended;
import org.lorainelab.igb.services.search.ISearchModeSym;
import org.lorainelab.igb.services.search.IStatus;
import org.lorainelab.igb.services.search.SearchResults;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.Action;

@Component(name = SearchModeID.COMPONENT_NAME, service = ISearchMode.class, immediate = true)
public class SearchModeID extends SearchModeIDOrProps implements ISearchModeSym, ISearchModeExtended, ISearchHints {

    public static final String COMPONENT_NAME = "SearchModeID";
    private static final int SEARCH_ALL_ORDINAL = 2;
    private static final String REMOTESERVERSEARCH = BUNDLE.getString("optionCheckBox");
    private static final String REMOTESERVERSEARCHTOOLTIP = BUNDLE.getString("optionCheckBoxTT");
    private static final String REMOTESERVERSEARCHSINGULAR = BUNDLE.getString("remoteServerSearchSingular");
    private static final String REMOTESERVERSEARCHPLURAL = BUNDLE.getString("remoteServerSearchPlural");
    private boolean optionSelected;

    public SearchModeID() {

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
        int i = 0;
        String remoteServerPluralText = i == 1 ? REMOTESERVERSEARCHSINGULAR : REMOTESERVERSEARCHPLURAL;
        return MessageFormat.format(REMOTESERVERSEARCH, "" + i, remoteServerPluralText);
    }

    @Override
    public String getOptionTooltip() {
        int i = 0;
        String remoteServerPluralText = i == 1 ? REMOTESERVERSEARCHSINGULAR : REMOTESERVERSEARCHPLURAL;
        return MessageFormat.format(REMOTESERVERSEARCHTOOLTIP, "" + i, remoteServerPluralText);
    }

    @Override
    public boolean getOptionEnable() {
        int i = 0;
        return i > 0;
    }

    @Override
    public void setOptionState(boolean selected) {
        optionSelected = selected;
    }

    @Override
    public boolean getOptionState() {
        return optionSelected;
    }

    @Override
    public int searchAllUse() {
        return SEARCH_ALL_ORDINAL;
    }

    @Override
    public SearchResults<SeqSymmetry> search(String search_text, final BioSeq chrFilter, IStatus statusHolder, boolean option) {
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

        Set<String> results = new HashSet<>();

        GenometryModel.getInstance().getSelectedGenomeVersion().searchHints(results, regex, 20);

        return results;
    }

    @Override
    public Action getCustomAction() {
        return null;
    }

}
