package com.affymetrix.searchmodesymmetryfilter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.filter.SymmetryFilter;
import com.affymetrix.genometry.filter.SymmetryFilterI;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.TypeContainerAnnot;
import com.lorainelab.igb.services.IgbService;
import com.lorainelab.igb.services.search.ISearchModeSym;
import com.lorainelab.igb.services.search.IStatus;
import com.lorainelab.igb.services.search.SearchResults;
import com.lorainelab.igb.genoviz.extensions.TierGlyph;

public class SearchModeSymmetryFilter implements ISearchModeSym {

    private final int searchAllOrdinal;
    private final IgbService igbService;
    private final SymmetryFilterI filter;
    private boolean optionSelected;

    public SearchModeSymmetryFilter(IgbService igbService, SymmetryFilterI filter, int searchAllOrdinal) {
        super();
        this.igbService = igbService;
        this.filter = filter;
        this.searchAllOrdinal = searchAllOrdinal;
    }

    @Override
    public String getName() {
        return "Filter " + filter.getName();
    }

    @Override
    public int searchAllUse() {
        return searchAllOrdinal;
    }

    @Override
    public String getTooltip() {
        return getName();
    }

    @Override
    public boolean useGenomeInSeqList() {
        return false;
    }

    @Override
    public String checkInput(String search_text, BioSeq vseq, String seq) {
        if (filter instanceof SymmetryFilter) {
            SymmetryFilter absFilter = (SymmetryFilter) filter;
            return absFilter.setParameterValue(absFilter.getParametersType().entrySet().iterator().next().getKey(), search_text)
                    ? null : "Error setting param " + search_text;
        }
        return "Current filter does not accept any parameters";
    }

    @Override
    public SearchResults<SeqSymmetry> search(String search_text, BioSeq chrFilter, IStatus statusHolder, boolean option) {
        List<SeqSymmetry> results = new ArrayList<>();
        if (filter instanceof SymmetryFilter
                && !search_text.equals(((SymmetryFilter) filter).getParameterValue(((SymmetryFilter) filter).getParametersType().entrySet().iterator().next().getKey()))) {
            throw new IllegalStateException("filter value changed from "
                    + ((SymmetryFilter) filter).getParameterValue(((SymmetryFilter) filter).getParametersType().entrySet().iterator().next().getKey()) + " to " + search_text);
        }
        List<TierGlyph> glyphs = igbService.getAllTierGlyphs();
        for (TierGlyph selectedTierGlyph : glyphs) {
            Object info = selectedTierGlyph.getInfo();
            if (info instanceof TypeContainerAnnot) {
                List<SeqSymmetry> searchResults = searchTrack(search_text, (TypeContainerAnnot) info);
                if (searchResults != null) {
                    results.addAll(searchResults);
                }
            }
        }
        String statusStr = MessageFormat.format("Searching {0} - found {1} matches", search_text, "" + results.size());
        statusHolder.setStatus(statusStr);
        return new SearchResults<>(getName(), search_text, chrFilter != null ? chrFilter.getId() : "genome", statusStr, results);
    }

    private List<SeqSymmetry> searchSym(SeqSymmetry sym) {
        List<SeqSymmetry> searchResults = new ArrayList<>();
        if (filter.filterSymmetry(null, sym)) {
            searchResults.add(sym);
        }
        int childCount = sym.getChildCount();
        for (int i = 0; i < childCount; i++) {
            //if(current_thread.isInterrupted())
            //	break;

            searchResults.addAll(searchSym(sym.getChild(i)));
        }
        return searchResults;
    }

    @Override
    public List<SeqSymmetry> searchTrack(String search_text, TypeContainerAnnot trackSym) {
        if (filter instanceof SymmetryFilter
                && !search_text.equals(((SymmetryFilter) filter).getParameterValue(((SymmetryFilter) filter).getParametersType().entrySet().iterator().next().getKey()))) {
            throw new IllegalStateException("filter value changed from "
                    + ((SymmetryFilter) filter).getParameterValue(((SymmetryFilter) filter).getParametersType().entrySet().iterator().next().getKey()) + " to " + search_text);
        }
        List<SeqSymmetry> results = searchSym(trackSym);
        return results;
    }
}
