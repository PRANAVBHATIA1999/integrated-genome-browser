package com.affymetrix.genometry.operator;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SimpleSymWithResidues;
import com.affymetrix.genometry.GenometryConstants;
import org.osgi.service.component.annotations.Component;

/**
 * operation for sequenc tracks that returns the complement base at each
 * location
 */
@Component(name = ComplementSequenceOperator.COMPONENT_NAME, immediate = true)
public class ComplementSequenceOperator implements Operator {

    public static final String COMPONENT_NAME = "ComplementSequenceOperator";
    private static final char[] COMPLEMENT = new char[256];

    static {
        COMPLEMENT['A'] = 'T';
        COMPLEMENT['T'] = 'A';
        COMPLEMENT['C'] = 'G';
        COMPLEMENT['G'] = 'C';
        COMPLEMENT['N'] = 'N';
        COMPLEMENT['a'] = 't';
        COMPLEMENT['t'] = 'a';
        COMPLEMENT['c'] = 'g';
        COMPLEMENT['g'] = 'c';
        COMPLEMENT['n'] = 'n';
    }

    @Override
    public String getName() {
        return "complement";
    }

    @Override
    public String getDisplay() {
        return GenometryConstants.BUNDLE.getString("operator_" + getName());
    }

    @Override
    public SeqSymmetry operate(BioSeq aseq, List<SeqSymmetry> symList) {
        SimpleSymWithResidues residueSym = (SimpleSymWithResidues) symList.get(0).getChild(0);
        String residues = residueSym.getResidues();
        StringBuilder complementResidues = new StringBuilder(residues.length());
        for (int i = 0; i < residues.length(); i++) {
            complementResidues.append(COMPLEMENT[residues.charAt(i)]);
        }
        return new SimpleSymWithResidues("complement:" + residueSym.getType(), residueSym.getBioSeq(), residueSym.getMin(),
                residueSym.getMax(), residueSym.getName(), residueSym.getScore(), residueSym.isForward(),
                residueSym.hasCdsSpan() ? residueSym.getCdsSpan().getMin() : Integer.MIN_VALUE,
                residueSym.hasCdsSpan() ? residueSym.getCdsSpan().getMax() : Integer.MAX_VALUE,
                ArrayUtils.clone(residueSym.getBlockMins()), ArrayUtils.clone(residueSym.getBlockMaxs()), complementResidues.toString());
    }

    @Override
    public int getOperandCountMin(FileTypeCategory category) {
        return category == FileTypeCategory.Sequence ? 1 : 0;
    }

    @Override
    public int getOperandCountMax(FileTypeCategory category) {
        return category == FileTypeCategory.Sequence ? 1 : 0;
    }

    @Override
    public boolean supportsTwoTrack() {
        return false;
    }

    @Override
    public FileTypeCategory getOutputCategory() {
        return FileTypeCategory.Sequence;
    }

    @Override
    public Operator newInstance() {
        try {
            return getClass().getConstructor().newInstance();
        } catch (Exception ex) {

        }
        return null;
    }
}
