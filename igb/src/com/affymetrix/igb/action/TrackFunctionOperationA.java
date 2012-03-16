package com.affymetrix.igb.action;

import java.util.ArrayList;
import java.util.List;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.operator.Operator;
import com.affymetrix.genometryImpl.symloader.Delegate.DelegateParent;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleSymWithProps;
import com.affymetrix.genometryImpl.symmetry.UcscBedSym;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.igb.osgi.service.SeqMapViewI;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.TrackStyle;
import com.affymetrix.igb.shared.TrackUtils;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import com.affymetrix.igb.viewmode.MapViewModeHolder;

public abstract class TrackFunctionOperationA extends GenericAction {
	private static final long serialVersionUID = 1L;
	protected final SeqMapViewI gviewer;
	private final Operator operator;
	
	protected TrackFunctionOperationA(SeqMapViewI gviewer, Operator operator) {
		super();
		this.gviewer = gviewer;
		this.operator = operator;
	}

	protected void addTier(List<? extends GlyphI> tiers) {
		java.util.List<DelegateParent> dps = new java.util.ArrayList<DelegateParent>();
		StringBuilder meth = new StringBuilder();
		meth.append(operator.getName()).append(": ");

		for (GlyphI tier : tiers) {
			if(((TierGlyph)tier).getAnnotStyle().getFeature() == null
					|| ((TierGlyph)tier).getAnnotStyle().getOperator() != null){
				addNonUpdateableTier(tiers);
				return;
			}
			
			meth.append(((TierGlyph)tier).getAnnotStyle().getTrackName()).append(", ");
			dps.add(new DelegateParent(((TierGlyph)tier).getAnnotStyle().getMethodName(), 
					isForward((TierGlyph)tier), ((TierGlyph)tier).getAnnotStyle().getFeature()));
			
		}

		GenericFeature feature = TrackUtils.getInstance().createFeature(meth.toString(), operator, dps);
		GeneralLoadUtils.loadAndDisplayAnnotations(feature);
	}

	private void addNonUpdateableTier(List<? extends GlyphI> tiers){
		BioSeq aseq = GenometryModel.getGenometryModel().getSelectedSeq();
		TrackStyle preferredStyle = null;
		List<SeqSymmetry> seqSymList = new ArrayList<SeqSymmetry>();
		for (GlyphI tier : tiers) {
			SeqSymmetry rootSym = (SeqSymmetry)tier.getInfo();
			if (rootSym == null && tier.getChildCount() > 0) {
				rootSym = (SeqSymmetry)tier.getChild(0).getInfo();
			}
			if (rootSym != null) {
				seqSymList.add(rootSym);
				if (rootSym instanceof SimpleSymWithProps && preferredStyle == null && ((SimpleSymWithProps)rootSym).getProperty("method") != null) {
					preferredStyle = TrackStyle.getInstance(((SimpleSymWithProps)rootSym).getProperty("method").toString(), false);
				}
			}
		}
		SeqSymmetry result_sym = operator.operate(aseq, seqSymList);
		if (result_sym != null) {
			StringBuilder meth = new StringBuilder();
			if (result_sym instanceof UcscBedSym) {
				meth.append(((UcscBedSym)result_sym).getType());
			}
			else {
				meth.append(operator.getName()).append(": ");
				for (GlyphI tier : tiers) {
					meth.append(((TierGlyph)tier).getAnnotStyle().getTrackName()).append(", ");
				}
			}
			preferredStyle.setViewMode(MapViewModeHolder.getInstance().getDefaultFactoryFor(operator.getOutputCategory()).getName());
			TrackUtils.getInstance().addTrack(result_sym, meth.toString(), preferredStyle);
		}
	}
			
	@Override
	public String getText() {
		return "";
	}
	
	private static Boolean isForward(TierGlyph tier){
		if(tier.getAnnotStyle().isGraphTier()){
			return null;
		}
		
		if(tier.getDirection() == TierGlyph.Direction.BOTH || tier.getDirection() == TierGlyph.Direction.NONE){
			return null;
		}
		
		return tier.getDirection() == TierGlyph.Direction.FORWARD;
	}
	
}
