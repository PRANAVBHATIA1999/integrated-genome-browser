/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.affymetrix.igb.glyph;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.regex.Pattern;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.parsers.CytobandParser;
import com.affymetrix.genometryImpl.parsers.CytobandParser.CytobandSym;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.RootSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SymWithProps;
import com.affymetrix.genometryImpl.symmetry.TypeContainerAnnot;

import com.affymetrix.genoviz.bioviews.Glyph;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.genoviz.glyph.EfficientOutlinedRectGlyph;
import com.affymetrix.genoviz.glyph.EfficientPaintRectGlyph;
import com.affymetrix.genoviz.glyph.InvisibleBoxGlyph;
import com.affymetrix.genoviz.glyph.RoundRectMaskGlyph;

import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.factories.TransformTierGlyph;

public abstract class CytobandGlyph {
	public static final Pattern CYTOBAND_TIER_REGEX = Pattern.compile(".*" + CytobandParser.CYTOBAND_TIER_NAME);
	private final static Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 10);

	/**
	 *  Creates a cytoband glyph.  Handling two cases:
	 * 1. cytoband syms are children of TypeContainerAnnot;
	 * 2. cytoband syms are grandchildren of TypeContainerAnnot
	 *        (when cytobands are loaded via DAS/2, child of TypeContainerAnnot
	 *         will be a Das2FeatureRequestSym, which will have cytoband children).
	 */
	public static Glyph makeCytobandGlyph(BioSeq sma, GlyphI axis_tier, int tier_index, SeqMapView smv) {
		List<RootSeqSymmetry> cyto_tiers = sma.getAnnotations(CYTOBAND_TIER_REGEX);
		if (cyto_tiers.isEmpty()) {
			return null;
		}
		SymWithProps cyto_annots = cyto_tiers.get(0);
		if (!(cyto_annots instanceof TypeContainerAnnot)) {
			return null;
		}

		int cyto_height = 11; // the pointed glyphs look better if this is an odd number

		RoundRectMaskGlyph cytoband_glyph_A = null;
		RoundRectMaskGlyph cytoband_glyph_B = null;
		List<CytobandSym> bands = CytobandParser.generateBands(cyto_annots);
		int centromerePoint = CytobandParser.determineCentromerePoint(bands);

		GlyphI efg;
		for (int q = 0; q < bands.size(); q++) {
			CytobandSym cyto_sym = bands.get(q);
			SeqSymmetry sym2 = smv.transformForViewSeq(cyto_sym, sma);
			SeqSpan cyto_span = smv.getViewSeqSpan(sym2);
			if (cyto_span == null) {
				continue;
			}
			if (CytobandParser.BAND_ACEN.equals(cyto_sym.getBand())) {
				efg = new EfficientPaintRectGlyph();
				efg.setCoords(cyto_span.getStartDouble(), 2.0, cyto_span.getLengthDouble(), cyto_height);
				((EfficientPaintRectGlyph) efg).setPaint(CytobandParser.acen_paint);

			} else if (CytobandParser.BAND_STALK.equals(cyto_sym.getBand())) {
				efg = new EfficientPaintRectGlyph();
				efg.setCoords(cyto_span.getStartDouble(), 2.0, cyto_span.getLengthDouble(), cyto_height);
				((EfficientPaintRectGlyph) efg).setPaint(CytobandParser.stalk_paint);

			} else if ("".equals(cyto_sym.getBand())) {
				efg = new EfficientOutlinedRectGlyph();
				efg.setCoords(cyto_span.getStartDouble(), 2.0, cyto_span.getLengthDouble(), cyto_height);
			} else {
				efg = new com.affymetrix.genoviz.glyph.LabelledRectGlyph();
				efg.setCoords(cyto_span.getStartDouble(), 2.0, cyto_span.getLengthDouble(), cyto_height);
				((com.affymetrix.genoviz.glyph.LabelledRectGlyph) efg).setForegroundColor(cyto_sym.getTextColor());
				((com.affymetrix.genoviz.glyph.LabelledRectGlyph) efg).setText(cyto_sym.getID());
				((com.affymetrix.genoviz.glyph.LabelledRectGlyph) efg).setFont(SMALL_FONT);
			}
			efg.setColor(cyto_sym.getColor());
			smv.getSeqMap().setDataModelFromOriginalSym(efg, cyto_sym);

			if (q <= centromerePoint) {
				cytoband_glyph_A = createSingleCytobandGlyph(cytoband_glyph_A, axis_tier, efg);
			} else {
				cytoband_glyph_B = createSingleCytobandGlyph(cytoband_glyph_B, axis_tier, efg);
			}
		}

		String meth = BioSeq.determineMethod(cyto_annots);
		final ITrackStyleExtended  style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(meth);
		TransformTierGlyph cytobandTier = new TransformTierGlyph(style);
		
		if (smv.getSeqMap().getTiers().size() >= tier_index) {
			smv.getSeqMap().addTier(cytobandTier, tier_index);
		} else {
			smv.getSeqMap().addTier(cytobandTier, false);
		}
		
		InvisibleBoxGlyph cytoband_glyph = new InvisibleBoxGlyph() {
			@Override
			public void setVisibility(boolean isVisible) {
				style.setShow(isVisible);
				super.setVisibility(isVisible);
			}

			@Override
			public boolean isVisible() {
				return style.getShow();
			}
			
			@Override
			public void pickTraversal(Rectangle2D.Double pickRect, List<GlyphI> pickList, ViewI view)  {
				if(isVisible()){
					super.pickTraversal(pickRect, pickList, view);
				}
			}
		
			@Override
			public void drawTraversal(ViewI view)  {
				if(isVisible()){
					super.drawTraversal(view);
				}
			}
		
		};
		cytoband_glyph.setMoveChildren(false);
		if (cytoband_glyph_A != null && cytoband_glyph_B != null) {
			cytoband_glyph.setCoordBox(cytoband_glyph_A.getCoordBox());
			cytoband_glyph.getCoordBox().add(cytoband_glyph_B.getCoordBox());
			cytoband_glyph.addChild(cytoband_glyph_A);
			cytoband_glyph.addChild(cytoband_glyph_B);
			return cytoband_glyph;
		}

		// Handle case where centomere is at beginning or end (telocentric)
		if (cytoband_glyph_A != null) {
			cytoband_glyph.setCoordBox(cytoband_glyph_A.getCoordBox());
			cytoband_glyph.addChild(cytoband_glyph_A);
		} else if (cytoband_glyph_B != null) {
			cytoband_glyph.setCoordBox(cytoband_glyph_B.getCoordBox());
			cytoband_glyph.addChild(cytoband_glyph_B);
		}

		return cytoband_glyph;
	}

	private static RoundRectMaskGlyph createSingleCytobandGlyph(
			RoundRectMaskGlyph cytobandGlyph, GlyphI axis_tier, GlyphI efg) {
		if (cytobandGlyph == null) {
			cytobandGlyph = new RoundRectMaskGlyph(axis_tier.getBackgroundColor());
			//cytobandGlyph.setColor(Color.GRAY);
			cytobandGlyph.setCoordBox(efg.getCoordBox());
		}
		cytobandGlyph.addChild(efg);
		cytobandGlyph.getCoordBox().add(efg.getCoordBox());
		return cytobandGlyph;
	}
}
