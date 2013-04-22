package com.affymetrix.igb.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.operator.Operator;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symloader.Delegate;
import com.affymetrix.genometryImpl.symloader.Delegate.DelegateParent;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleSymWithProps;
import com.affymetrix.genometryImpl.symmetry.UcscBedSym;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LoadUtils;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.igb.action.SeqMapViewActionA;
import com.affymetrix.igb.general.ServerList;
import com.affymetrix.igb.tiers.IGBStateProvider;
import com.affymetrix.igb.tiers.TrackStyle;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import com.affymetrix.igb.view.load.GeneralLoadView;

public abstract class TrackFunctionOperationA extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private final Operator mOperator;
	
	protected TrackFunctionOperationA(Operator operator, String text) {
		super(text, null, null);
		this.mOperator = operator;
	}

	protected TrackFunctionOperationA(Operator operator) {
		this(operator, null);
	}

	protected void addTier(List<StyledGlyph> vgs) {
		java.util.List<DelegateParent> dps = new java.util.ArrayList<DelegateParent>();
		
		for (StyledGlyph vg : vgs) {
			if(vg.getAnnotStyle().getFeature() == null){
				addNonUpdateableTier(vgs);
				return;
			}
			
			dps.add(new DelegateParent(vg.getAnnotStyle().getMethodName(), 
					isForward(vg), vg.getAnnotStyle().getFeature()));
			
		}

		GenericFeature feature = createFeature(getMethod(vgs, false), getMethod(vgs, true), getOperator(), dps, vgs.get(0).getAnnotStyle());
		GeneralLoadUtils.loadAndDisplayAnnotations(feature);
	}

	protected Operator getOperator() {
		return mOperator;
	}

	protected String getMethod(List<? extends GlyphI> vgs, boolean append_symbol) {
		StringBuilder meth = new StringBuilder();
		meth.append(getOperator().getDisplay()).append("- ");
		boolean started = false;
		for (GlyphI gl : vgs) {
			if (started) {
				meth.append(", ");
			}
			meth.append(((StyledGlyph)gl).getAnnotStyle().getTrackName());
			if(append_symbol){
				meth.append(((StyledGlyph)gl).getDirection().getDisplay());
			}
			started = true;
		}
		return meth.toString();
	}
	
	private void addNonUpdateableTier(List<? extends GlyphI> vgs){
		BioSeq aseq = GenometryModel.getGenometryModel().getSelectedSeq();
		TrackStyle preferredStyle = null;
		List<SeqSymmetry> seqSymList = new ArrayList<SeqSymmetry>();
		for (GlyphI gl : vgs) {
			SeqSymmetry rootSym = (SeqSymmetry)gl.getInfo();
			if (rootSym == null && gl.getChildCount() > 0) {
				rootSym = (SeqSymmetry)gl.getChild(0).getInfo();
			}
			if (rootSym != null) {
				seqSymList.add(rootSym);
				if (rootSym instanceof SimpleSymWithProps && preferredStyle == null && ((SimpleSymWithProps)rootSym).getProperty("method") != null) {
					preferredStyle = IGBStateProvider.getInstance(((SimpleSymWithProps)rootSym).getProperty("method").toString());
				}
			}
		}
		Operator operator = getOperator();
		SeqSymmetry result_sym = operator.operate(aseq, seqSymList);
		if (result_sym != null) {
			StringBuilder meth = new StringBuilder();
			if (result_sym instanceof UcscBedSym) {
				meth.append(((UcscBedSym)result_sym).getType());
			}
			else {
				meth.append(operator.getDisplay()).append("- ");
				for (GlyphI gl : vgs) {
					meth.append(((StyledGlyph)gl).getAnnotStyle().getTrackName()).append(", ");
				}
			}
			TrackUtils.getInstance().addTrack(result_sym, meth.toString(), preferredStyle);
		}
	}
			
	private static Boolean isForward(StyledGlyph vg){
		if(vg.getAnnotStyle().isGraphTier()){
			return null;
		}
		
		if(vg.getDirection() == TierGlyph.Direction.BOTH || vg.getDirection() == TierGlyph.Direction.NONE){
			return null;
		}
		
		return vg.getDirection() == TierGlyph.Direction.FORWARD;
	}
	
	private GenericFeature createFeature(String method, String featureName, Operator operator, List<Delegate.DelegateParent> dps, ITrackStyleExtended preferredStyle) {
		method = IGBStateProvider.getUniqueName("file:/"+removeIllegalCharacters(method));
		
		java.net.URI uri;
		try {
			uri = java.net.URI.create(method);
		} catch (java.lang.IllegalArgumentException ex){
			if(ex.getCause() instanceof java.net.URISyntaxException){
				java.net.URISyntaxException uriex = (java.net.URISyntaxException)ex.getCause();
				Logger.getLogger(TrackFunctionOperationA.class.getName()).log(Level.INFO, "{0}.\nCharacter {1}", new Object[]{uriex.getMessage(), method.charAt(uriex.getIndex())});
			}else{
				Logger.getLogger(TrackFunctionOperationA.class.getName()).log(Level.INFO, "Illegal character in string {0}", method);
			}
			
			//method = GeneralUtils.URLEncode(featureName);
			//method = TrackStyle.getUniqueName("file:/"+method);
			uri = java.net.URI.create(GeneralUtils.URLEncode(method));
		}
		
		GenericVersion version = GeneralLoadUtils.getIGBFilesVersion(GenometryModel.getGenometryModel().getSelectedSeqGroup(), GeneralLoadView.getLoadView().getSelectedSpecies());
		GenericFeature feature = new GenericFeature(featureName, null, version, new Delegate(uri, featureName, version.group, operator, dps), null, false);
		version.addFeature(feature);
		feature.setVisible(); // this should be automatically checked in the feature tree
		
		ServerList.getServerInstance().fireServerInitEvent(ServerList.getServerInstance().getIGBFilesServer(), LoadUtils.ServerStatus.Initialized, true, true);
		
		GeneralLoadView.getLoadView().refreshDataManagementView();
		
		ITrackStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(method, featureName, Delegate.EXT, null);
		if(preferredStyle != null){
			style.copyPropertiesFrom(preferredStyle);
			style.setSeparate(false);
		}
		if(operator.getOutputCategory() == FileTypeCategory.Graph ||
				operator.getOutputCategory() == FileTypeCategory.Mismatch){
			style.setExpandable(false);
			style.setGraphTier(true);
		}
	
		style.setTrackName(featureName);
		style.setLabelField(null);
		
		return feature;
	}
	
	private static String removeIllegalCharacters(String string){
		string = string.replaceAll("\\s+", "_");
		string = string.replaceAll("\\|", "_");
		string = string.replaceAll("\u221E", "infinite");
		string = string.replaceAll("\\[", "(");
		string = string.replaceAll("\\]", ")");
		return string;
	}
}
