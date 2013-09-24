package com.affymetrix.genometryImpl.filter;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;

/**
 * This is a filter which is used to filter out the symmetries with no children
 * @author Anuj
 */
public class WithIntronFilter extends SymmetryFilter {

    @Override
    public String getName() {
        return "intron";
    }
	
    @Override
    public boolean filterSymmetry(BioSeq bioseq, SeqSymmetry ss) {
		if(ss.getChildCount() > 1)
            return true;
        return false;
    }
    
}
