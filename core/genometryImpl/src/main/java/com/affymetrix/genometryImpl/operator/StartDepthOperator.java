package com.affymetrix.genometryImpl.operator;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.symmetry.impl.SeqSymSummarizer;
import com.affymetrix.genometryImpl.symmetry.impl.SeqSymmetry;
import java.util.List;

/**
 *
 * @author hiralv
 */
public class StartDepthOperator  extends AbstractAnnotationTransformer implements Operator {
	
	public StartDepthOperator(FileTypeCategory fileTypeCategory) {
		super(fileTypeCategory);
	}

	@Override
	public String getName() {
		return fileTypeCategory.toString().toLowerCase() + "_start_depth";
	}

	@Override
	public SeqSymmetry operate(BioSeq aseq, List<SeqSymmetry> symList) {		
		return SeqSymSummarizer.getSymmetryStartSummary(symList, aseq, false, null, 2);
	}

	@Override
	public FileTypeCategory getOutputCategory() {
		return FileTypeCategory.Graph;
	}
}

