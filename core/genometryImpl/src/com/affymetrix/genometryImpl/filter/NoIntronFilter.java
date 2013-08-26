package com.affymetrix.genometryImpl.filter;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;

/**
 * This is a filter which is used to filter out the symmetries with no children
 * @author Anuj
 */
public class NoIntronFilter extends AbstractFilter {

    @Override
    public String getName() {
        return "No Intron";
    }

	@Override
	public String getDisplay() {
		return getName();
	}
	
    @Override
    public boolean setParameterValue(String key, Object o) {
        return false;
    }

    @Override
    public Object getParameterValue(String key) {
        return null;
    }

    @Override
    public boolean filterSymmetry(BioSeq bioseq, SeqSymmetry ss) {
		if(ss.getChildCount() <= 1)
            return false;
        return true;
    }
    
}
