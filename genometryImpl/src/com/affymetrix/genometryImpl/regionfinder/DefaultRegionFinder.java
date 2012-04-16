
package com.affymetrix.genometryImpl.regionfinder;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.symmetry.MutableSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleMutableSeqSymmetry;
import com.affymetrix.genometryImpl.util.SeqUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hiralv
 */
public class DefaultRegionFinder implements RegionFinder{

	public SeqSpan findInterestingRegion(BioSeq aseq, List<SeqSymmetry> syms) {
		List<SeqSymmetry> less_syms  = new ArrayList<SeqSymmetry>();
		if(syms.size() > 100){
			for(int i=0; i<100; i++){
				less_syms.add(syms.get(i));
			}
		}else{
			less_syms = syms;
		}
		
		MutableSeqSymmetry resultSym = new SimpleMutableSeqSymmetry();
		SeqUtils.union(less_syms, resultSym, aseq);
		return resultSym.getSpan(aseq);
	}
	
}
