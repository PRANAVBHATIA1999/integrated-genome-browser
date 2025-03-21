package com.affymetrix.genometry.operator;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.filter.SymmetryFilter;
import com.affymetrix.genometry.filter.SymmetryFilterI;
import com.affymetrix.genometry.general.IParameters;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SimpleSymWithProps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterOperator implements Operator, IParameters {

    private final FileTypeCategory category;
    private final SymmetryFilterI filter;

    public FilterOperator(FileTypeCategory category, SymmetryFilterI filter) {
        super();
        this.category = category;
        this.filter = filter;
    }

    @Override
    public String getName() {
        return "filter_operator_" + category.toString() + "_" + filter.getName();
    }

    @Override
    public String getDisplay() {
        return "filter " + category.toString() + " by " + filter.getName();
    }

    @Override
    public SeqSymmetry operate(BioSeq aseq, List<SeqSymmetry> symList) {
        if (symList.size() != 1) {
            return null;
        }
        SimpleSymWithProps sym = new SimpleSymWithProps();
        for (int i = 0; i < symList.get(0).getChildCount(); i++) {
            SeqSymmetry child = symList.get(0).getChild(i);
            if (filter.filterSymmetry(aseq, child)) {
                sym.addChild(child);
            }
        }
        return sym;
    }

    @Override
    public int getOperandCountMin(FileTypeCategory category) {
        return 0;
    }

    @Override
    public int getOperandCountMax(FileTypeCategory category) {
        return category == this.category ? Integer.MAX_VALUE : 0;
    }

    @Override
    public Map<String, Class<?>> getParametersType() {
        Map<String, Class<?>> parameters = new HashMap<>();
        parameters.put(filter.getName(), String.class);
        return parameters;
    }

    @Override
    public boolean setParametersValue(Map<String, Object> parms) {
        if (filter instanceof SymmetryFilter) {
            SymmetryFilter absFilter = (SymmetryFilter) filter;
            return absFilter.setParametersValue(parms);
        }
        return false;
    }

    @Override
    public Object getParameterValue(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Object> getParametersPossibleValues(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean setParameterValue(String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPrintableString() {
        return "";
    }

    @Override
    public boolean supportsTwoTrack() {
        return false;
    }

    @Override
    public FileTypeCategory getOutputCategory() {
        return category;
    }

    @Override
    public Operator newInstance() {
        try {
            return getClass().getConstructor(FileTypeCategory.class, SymmetryFilterI.class).newInstance(category, filter);
        } catch (Exception ex) {
        }
        return null;
    }
}
