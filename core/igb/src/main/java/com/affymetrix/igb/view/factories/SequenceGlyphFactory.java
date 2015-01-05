package com.affymetrix.igb.view.factories;

import aQute.bnd.annotation.component.Component;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.RootSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.impl.SimpleSymWithResidues;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.glyph.FillRectGlyph;
import com.affymetrix.igb.shared.MapTierGlyphFactoryA;
import com.affymetrix.igb.shared.MapTierGlyphFactoryI;
import com.google.common.collect.ImmutableSet;
import com.lorainelab.igb.genoviz.extensions.api.StyledGlyph;
import com.lorainelab.igb.genoviz.extensions.api.SeqMapViewExtendedI;
import com.lorainelab.igb.genoviz.extensions.api.TierGlyph;
import java.util.Set;

@Component(name = SequenceGlyphFactory.COMPONENT_NAME, provide = {MapTierGlyphFactoryI.class})
public class SequenceGlyphFactory extends MapTierGlyphFactoryA {

    public static final String COMPONENT_NAME = "SequenceGlyphFactory";

    @Override
    public void createGlyphs(RootSeqSymmetry sym, ITrackStyleExtended style, SeqMapViewExtendedI smv, BioSeq seq) {
        if (sym != null) {
            TierGlyph tierGlyph = smv.getTrack(style, StyledGlyph.Direction.NONE);
            tierGlyph.setTierType(TierGlyph.TierType.SEQUENCE);
            tierGlyph.setDirection(StyledGlyph.Direction.NONE);
            tierGlyph.setInfo(sym);
            for (int i = 0; i < sym.getChildCount(); i++) {
                if (!(sym.getChild(i) instanceof SimpleSymWithResidues)) {
                    continue;
                }
                SimpleSymWithResidues childSym = (SimpleSymWithResidues) sym.getChild(i);
                SeqSpan pspan = smv.getViewSeqSpan(childSym);
                if (pspan == null || pspan.getLength() == 0) {
                    return;
                }  // if no span corresponding to seq, then return;	
                GlyphI residueGlyph = getAlignedResiduesGlyph(childSym, smv.getAnnotatedSeq(), false);
                if (residueGlyph != null) {
                    FillRectGlyph childGlyph = new FillRectGlyph();
                    childGlyph.setCoords(pspan.getMin(), 0, pspan.getLength(), style.getHeight() + 0.0001);
                    childGlyph.setColor(style.getForeground());
                    residueGlyph.setCoords(pspan.getMin(), 0, pspan.getLength(), style.getHeight() + 0.0001);
                    tierGlyph.setDataModelFromOriginalSym(childGlyph, childSym);
                    childGlyph.addChild(residueGlyph);
                    tierGlyph.addChild(childGlyph);
                }
            }
            doMiddlegroundShading(tierGlyph, smv, seq);
        }
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

    @Override
    public Set<FileTypeCategory> getSupportedCategories() {
        return ImmutableSet.<FileTypeCategory>builder()
                .add(FileTypeCategory.Sequence).build();
    }
}
