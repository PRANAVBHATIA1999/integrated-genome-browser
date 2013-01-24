package com.affymetrix.genometryImpl.operator.extra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.comparator.SeqSymMinComparator;
import com.affymetrix.genometryImpl.operator.Operator;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.symmetry.MutableSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleScoredSymWithProps;
import com.affymetrix.genometryImpl.symmetry.SimpleSymWithProps;
import com.affymetrix.genometryImpl.util.SeqUtils;

/**
 *
 * @author hiralv
 */
public class ParentSummaryOperator implements Operator{

	private final FileTypeCategory fileTypeCategory;

	public ParentSummaryOperator(FileTypeCategory fileTypeCategory) {
		super();
		this.fileTypeCategory = fileTypeCategory;
	}
	
	@Override
	public String getName() {
		return fileTypeCategory.toString().toLowerCase()+"_parent_summary";
	}

	@Override
	public String getDisplay() {
		return ParentOperatorConstants.BUNDLE.getString("operator_" + getName());
	}

	@Override
	public SeqSymmetry operate(BioSeq aseq, List<SeqSymmetry> symList) {
		if(symList.isEmpty())
			return new SimpleSymWithProps();
		
		SeqSymmetry topSym = symList.get(0);
		List<SeqSymmetry> syms = new ArrayList<SeqSymmetry>();
		for(int i=0; i<topSym.getChildCount(); i++){
			syms.add(topSym.getChild(i));
		}
		
		Collections.sort(syms, new SeqSymMinComparator(aseq));
		
		SimpleSymWithProps result = new SimpleScoredSymWithProps(0);
		List<SeqSymmetry> temp = new ArrayList<SeqSymmetry>();
		double lastMax = syms.get(0).getSpan(aseq).getMax();
		
		for(SeqSymmetry sym : syms){
			SeqSpan currentSpan = sym.getSpan(aseq);
			
			if(currentSpan.getMin() > lastMax){
				MutableSeqSymmetry resultSym = new SimpleScoredSymWithProps(temp.size());
				SeqUtils.union(temp, resultSym, aseq, 2);
				result.addChild(resultSym);
				
				lastMax = Integer.MIN_VALUE;
				temp.clear();
			}
				
			temp.add(sym);
			lastMax = Math.max(lastMax, currentSpan.getMax());

		}
		
		//Remaining
		MutableSeqSymmetry resultSym = new SimpleScoredSymWithProps(temp.size());
		SeqUtils.union(temp, resultSym, aseq, 2);
		result.addChild(resultSym);
		temp.clear();
		
		syms.clear();
		
		return result;
	}

	@Override
	public int getOperandCountMin(FileTypeCategory category) {
		return category == fileTypeCategory ? 1 : 0;
	}

	@Override
	public int getOperandCountMax(FileTypeCategory category) {
		return category == fileTypeCategory ? 1 : 0;
	}

	@Override
	public Map<String, Class<?>> getParameters() {
		return null;
	}

	@Override
	public boolean setParameters(Map<String, Object> parms) {
		return false;
	}

	@Override
	public boolean supportsTwoTrack() {
		return true;
	}

	@Override
	public FileTypeCategory getOutputCategory() {
		return FileTypeCategory.Annotation;
	}
}

