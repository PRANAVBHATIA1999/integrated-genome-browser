package com.affymetrix.igb.action;

import java.util.ArrayList;
import java.util.List;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.operator.Operator;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.symloader.Delegate;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleSymWithProps;
import com.affymetrix.genometryImpl.symmetry.UcscBedSym;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LoadUtils;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.igb.general.ServerList;
import com.affymetrix.igb.osgi.service.SeqMapViewI;
import com.affymetrix.igb.shared.TierGlyph;
import com.affymetrix.igb.tiers.TrackStyle;
import com.affymetrix.igb.util.TrackUtils;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import com.affymetrix.igb.view.load.GeneralLoadView;
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
		java.util.List<String> symsStr = new java.util.ArrayList<String>();
		java.util.List<GenericFeature> features = new java.util.ArrayList<GenericFeature>();
		StringBuilder meth = new StringBuilder();
		meth.append(operator.getName()).append(": ");


		for (GlyphI tier : tiers) {
			symsStr.add(((TierGlyph)tier).getAnnotStyle().getMethodName());
			meth.append(((TierGlyph)tier).getAnnotStyle().getTrackName()).append(", ");
			if(((TierGlyph)tier).getAnnotStyle().getFeature() == null){
				addNonUpdateableTier(tiers);
				return;
			}
				
			features.add(((TierGlyph)tier).getAnnotStyle().getFeature());
		}

		String method = TrackStyle.getUniqueName(GeneralUtils.URLEncode(meth.toString()));
		//TODO : Remove below conditions afte view mode refactoring is complete.
		if(!method.contains("$.")){
			method += "$.";
		}
		DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(method).setViewMode("default");

		GenericVersion version = GeneralLoadUtils.getIGBFilesVersion(GenometryModel.getGenometryModel().getSelectedSeqGroup(), GeneralLoadView.getLoadView().getSelectedSpecies());
		java.net.URI uri = java.net.URI.create("file:/"+method);

		GenericFeature feature = new GenericFeature(meth.toString(), null, version, new Delegate(uri, meth.toString(), version, operator, symsStr, features), null, false);
		version.addFeature(feature);
		feature.setVisible(); // this should be automatically checked in the feature tree

		ServerList.getServerInstance().fireServerInitEvent(ServerList.getServerInstance().getIGBFilesServer(), LoadUtils.ServerStatus.Initialized, true, true);

//		SeqGroupView.getInstance().setSelectedGroup(feature.gVersion.group.getID());

		GeneralLoadView.getLoadView().createFeaturesTable();

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
}
