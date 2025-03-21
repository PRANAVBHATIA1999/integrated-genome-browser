package com.affymetrix.genometry.filter;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hiralv
 */
public class ChainFilter implements SymmetryFilterI {

    List<SymmetryFilterI> filters;

    @Override
    public String getDisplay() {
        return "Chain Filter";
    }

    @Override
    public String getName() {
        return "chain_filter";
    }

    public void setFilter(List<SymmetryFilterI> filters) {
        this.filters = filters;
    }

    public List<SymmetryFilterI> getFilters() {
        return filters;
    }

    @Override
    public boolean filterSymmetry(BioSeq seq, SeqSymmetry sym) {
        boolean allow = true;
        for (SymmetryFilterI filter : filters) {
            allow &= filter.filterSymmetry(seq, sym);
            if (!allow) {
                break;
            }
        }
        return allow;
    }

    @Override
    public SymmetryFilterI newInstance() {
        ChainFilter newInstance = new ChainFilter();
        List<SymmetryFilterI> newInstanceFilters = new ArrayList<>();
        for (SymmetryFilterI filter : filters) {
            newInstanceFilters.add(filter.newInstance());
        }
        newInstance.setFilter(newInstanceFilters);

        return newInstance;
    }

    @Override
    public boolean isFileTypeCategorySupported(FileTypeCategory fileTypeCategory) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
