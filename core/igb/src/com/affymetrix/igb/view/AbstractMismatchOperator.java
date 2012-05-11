
package com.affymetrix.igb.view;

import java.util.List;
import java.util.Map;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.operator.Operator;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.TypeContainerAnnot;

import com.affymetrix.igb.view.load.GeneralLoadView;

/**
 *
 * @author hiralv
 */
public abstract class AbstractMismatchOperator implements Operator {

	public abstract SeqSymmetry getMismatch(List<SeqSymmetry> syms, BioSeq seq, boolean binary_depth, String id, int start, int end);
	
	public SeqSymmetry operate(BioSeq aseq, List<SeqSymmetry> symList) {
		if (symList.size() != 1 || !(symList.get(0) instanceof TypeContainerAnnot)) {
			return null;
		}
		
		int[] startEnd = getStartEnd(symList.get(0), aseq);
		SeqSpan loadSpan = new SimpleSeqSpan(startEnd[0], startEnd[1], aseq);

		//Load Residues
		if(!aseq.isAvailable(loadSpan)){
			if(!GeneralLoadView.getLoadView().loadResidues(loadSpan, true)){
				return null;
			}
		}
		
		return getMismatch(symList, aseq, false, "", startEnd[0], startEnd[1]);
	}

	public int getOperandCountMin(FileTypeCategory category) {
		return category == FileTypeCategory.Alignment ? 1 : 0;
	}

	public int getOperandCountMax(FileTypeCategory category) {
		return category == FileTypeCategory.Alignment ? 1 : 0;
	}

	public Map<String, Class<?>> getParameters() {
		return null;
	}

	public boolean setParameters(Map<String, Object> parms) {
		return false;
	}

	public boolean supportsTwoTrack() {
		return false;
	}

	public FileTypeCategory getOutputCategory() {
		return FileTypeCategory.Mismatch;
	}
	
	private static int[] getStartEnd(SeqSymmetry tsym, BioSeq aseq){
		int start = Integer.MAX_VALUE, end = Integer.MIN_VALUE;

		for(int i=0; i<tsym.getChildCount(); i++){
			SeqSymmetry childSym = tsym.getChild(i);
			SeqSpan span = childSym.getSpan(aseq);
			if(span.getMax() > end){
				end = span.getMax();
			}

			if(span.getMin() < start){
				start = span.getMin();
			}
		}

		return new int[]{start, end};
	}
}
