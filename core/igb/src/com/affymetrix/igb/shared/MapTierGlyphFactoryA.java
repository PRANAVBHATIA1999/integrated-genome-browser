package com.affymetrix.igb.shared;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.symmetry.DerivedSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SymWithResidues;
import com.affymetrix.genometryImpl.util.SeqUtils;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.glyph.FillRectGlyph;
import com.affymetrix.igb.IGBConstants;
import java.util.Map;
import java.util.MissingResourceException;

public abstract class MapTierGlyphFactoryA implements MapTierGlyphFactoryI {
	
	@Override
	public void init(Map<String, Object> options) {	}
	
	@Override
	public String getDisplayName() {
		String displayName = null;
		try {
			displayName = IGBConstants.BUNDLE.getString("viewmode_" + getName());
		}
		catch(MissingResourceException x) {
			displayName = getName();
		}
		return displayName;
	}

	@Override
	public boolean supportsTwoTrack() {
		return false;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
	
	protected static void doMiddlegroundShading(TierGlyph tierGlyph, SeqMapViewExtendedI gviewer, BioSeq seq) {
		
		GenericFeature feature = tierGlyph.getAnnotStyle().getFeature();
		if (feature != null) {
			SeqSymmetry inverse = SeqUtils.inverse(feature.getRequestSym(), seq);
			if (seq != gviewer.getViewSeq()) {
				inverse = gviewer.transformForViewSeq(inverse, seq);
			}
			int child_count = inverse.getChildCount();
			if (child_count > 0) {
				for (int i = 0; i < child_count; i++) {
					SeqSymmetry child = inverse.getChild(i);
					SeqSpan ospan = child.getSpan(gviewer.getViewSeq());
					if (ospan != null && ospan.getLength() > 1) {
						GlyphI mglyph = new FillRectGlyph();
						mglyph.setCoords(ospan.getMin(), 0, ospan.getLength() - 1, 0);
						tierGlyph.addMiddleGlyph(mglyph);
					}
				}
			} else {
				GlyphI mglyph = new FillRectGlyph();
				mglyph.setCoords(seq.getMin(), 0, seq.getLength() - 1, 0);
				tierGlyph.addMiddleGlyph(mglyph);
			}
		}
	}
	
	/**
	 * Determine and set the appropriate residues for this element.
	 */
	protected static AlignedResidueGlyph getAlignedResiduesGlyph(SeqSymmetry insym, BioSeq annotseq, boolean setMask) {
		SeqSymmetry sym = insym;
		if (insym instanceof DerivedSeqSymmetry) {
			sym = getMostOriginalSymmetry(insym);
		}

		if (!(sym instanceof SymWithResidues)) {
			return null;
		}
		
		SeqSpan span = sym.getSpan(annotseq);
		if (span == null) {
			return null;
		}

		String residueStr = ((SymWithResidues) sym).getResidues();

		if (residueStr == null || residueStr.length() == 0) {
			return null;
		}
		
		AlignedResidueGlyph csg = new AlignedResidueGlyph();
		csg.setResidues(residueStr);
		csg.setHitable(false);
		csg.setDefaultShowMask(false);
		if(setMask){
			String bioSeqResidue = annotseq.getResidues(span.getMin(), span.getMin() + residueStr.length());
			if (bioSeqResidue != null) {
				csg.setResidueMask(bioSeqResidue);
			}
		}
		return csg;
			
		// SEQ array has unexpected behavior;  commenting out for now.
		/*if (((SymWithProps) sym).getProperty("SEQ") != null) {
		byte[] seqArr = (byte[]) ((SymWithProps) sym).getProperty("SEQ");
		for (int i = 0; i < seqArr.length; i++) {
		System.out.print((char) seqArr[i]);
		}
		System.out.println();
		isg.setResidueMask(seqArr);
		}*/
	}
	
	protected static SeqSymmetry getMostOriginalSymmetry(SeqSymmetry sym) {
		if (sym instanceof DerivedSeqSymmetry) {
			return getMostOriginalSymmetry(((DerivedSeqSymmetry) sym).getOriginalSymmetry());
		}
		return sym;
	}
}
