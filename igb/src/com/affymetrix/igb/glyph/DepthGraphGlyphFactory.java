package com.affymetrix.igb.glyph;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymSummarizer;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.style.GraphType;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;

import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.igb.shared.ExtendedMapViewGlyphFactoryI;
import com.affymetrix.igb.shared.GraphGlyph;
import com.affymetrix.igb.shared.SeqMapViewExtendedI;
import com.affymetrix.igb.shared.TierGlyph;

/**
 *
 * @author hiralv
 */
public class DepthGraphGlyphFactory implements ExtendedMapViewGlyphFactoryI {
	
	public String getName(){
		return "depth";
	}
	
	public void init(java.util.Map options) { }

	public void createGlyph(SeqSymmetry sym, SeqMapViewExtendedI smv) {

		String meth = BioSeq.determineMethod(sym);
		
		if (meth == null) {
			return;
		}
	
		ITrackStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(meth);
		
		TierGlyph[] tiers = smv.getTiers(false, style, true);
		if (style.getSeparate()) {
			addDepthGraph(meth, sym, tiers[0], tiers[1], smv);			
		} else {
			// use only one tier
			addDepthGraph(meth, sym, tiers[0], tiers[0], smv);
		}

	}
	
	private void addDepthGraph(String meth, SeqSymmetry sym, TierGlyph ftier, TierGlyph rtier, SeqMapViewExtendedI smv){
		
		SeqSpan pspan = smv.getViewSeqSpan(sym);
		
		if (pspan == null || pspan.getLength() == 0) {
			return;
		}
		
		BioSeq seq = smv.getAnnotatedSeq();
		java.util.List<SeqSymmetry> syms = new java.util.ArrayList<SeqSymmetry>();
		syms.add(sym);
		GraphSym gsym = null;
		
		if (ftier == rtier) {
			gsym = SeqSymSummarizer.getSymmetrySummary(syms, seq, false, meth);
			addToParent(pspan, gsym, ftier);
		} else {
			gsym = SeqSymSummarizer.getSymmetrySummary(syms, seq, false, meth, true);
			addToParent(pspan, gsym, ftier);
			
			gsym = SeqSymSummarizer.getSymmetrySummary(syms, seq, false, meth, false);
			addToParent(pspan, gsym, rtier);
		}

	}
	
	private void addToParent(SeqSpan pspan, GraphSym gsym, TierGlyph tier){
		if(gsym != null){
			GraphState state = new GraphState(tier.getAnnotStyle());
			GraphGlyph graph_glyph = new GraphGlyph(gsym, state);
			graph_glyph.drawHandle(false);
			graph_glyph.setSelectable(false);
			graph_glyph.setGraphStyle(GraphType.STAIRSTEP_GRAPH);
			graph_glyph.setCoords(pspan.getMin(), 0, pspan.getLength(), tier.getCoordBox().getHeight());
			addToTier(tier, graph_glyph, gsym);
		}
	}

	public void addToTier(TierGlyph tier, GlyphI glyph, SeqSymmetry sym) {
		tier.addChild(glyph);
		tier.setInfo(sym);
	}
	
	public boolean isFileSupported(String format) {
		return true;
	}
}
