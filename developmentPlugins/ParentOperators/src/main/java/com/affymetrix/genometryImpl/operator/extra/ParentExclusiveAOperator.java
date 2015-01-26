package com.affymetrix.genometry.operator.extra;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.operator.Operator;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;

public class ParentExclusiveAOperator extends ParentExclusiveOperator implements Operator {

    public ParentExclusiveAOperator(FileTypeCategory fileTypeCategory) {
        super(fileTypeCategory);
    }

    @Override
    public String getName() {
        return category.toString().toLowerCase() + "_parent_a_not_b";
    }

    @Override
    public String getDisplay() {
        return ParentOperatorConstants.BUNDLE.getString("operator_" + getName());
    }

    @Override
    public SeqSymmetry operate(BioSeq seq, java.util.List<SeqSymmetry> symList) {
        return operate(seq, symList.get(0), symList.get(1));
    }

}
