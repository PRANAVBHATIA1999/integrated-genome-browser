package com.affymetrix.genometry.operator;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.symmetry.impl.SeqSymSummarizer;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author lfrohman
 */
public abstract class ExclusiveOperator extends XorOperator implements Operator {

    public ExclusiveOperator(FileTypeCategory fileTypeCategory) {
        super(fileTypeCategory);
    }

    protected SeqSymmetry operate(BioSeq aseq, SeqSymmetry symsA, SeqSymmetry symB) {
        return exclusive(aseq, findChildSyms(symsA), findChildSyms(symB));
    }

    protected static SeqSymmetry exclusive(BioSeq seq, List<SeqSymmetry> symsA, List<SeqSymmetry> symsB) {
        SeqSymmetry xorSym = getXor(seq, symsA, symsB);
        //  if no spans for xor, then won't be any for one-sided xor either, so return null;
        if (xorSym == null) {
            return null;
        }
        List<SeqSymmetry> xorList = new ArrayList<>();
        xorList.add(xorSym);
        SeqSymmetry a_not_b = SeqSymSummarizer.getIntersection(symsA, xorList, seq);
        return a_not_b;
    }
}
