package com.affymetrix.genometryImpl.operator;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryConstants;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.impl.SimpleSymWithResidues;

public final class CopySequenceOperator implements Operator, ICopy {

	@Override
	public String getName() {
		return "copysequence";
	}

	@Override
	public String getDisplay() {
		return GenometryConstants.BUNDLE.getString("operator_" + getName());
	}

	@Override
	public SeqSymmetry operate(BioSeq aseq, List<SeqSymmetry> symList) {
		SimpleSymWithResidues residueSym = (SimpleSymWithResidues)symList.get(0).getChild(0);
		return new SimpleSymWithResidues(residueSym.getType(), residueSym.getBioSeq(), residueSym.getMin(),
				residueSym.getMax(), residueSym.getName(), residueSym.getScore(), residueSym.isForward(),
				residueSym.hasCdsSpan() ? residueSym.getCdsSpan().getMin() : Integer.MIN_VALUE,
				residueSym.hasCdsSpan() ? residueSym.getCdsSpan().getMax() : Integer.MAX_VALUE,
				ArrayUtils.clone(residueSym.getBlockMins()), ArrayUtils.clone(residueSym.getBlockMaxs()), new String(residueSym.getResidues()));
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
	public Operator newInstance(){
		try {
			return getClass().getConstructor().newInstance();
		} catch (Exception ex) {
			
		}
		return null;
	}
}
