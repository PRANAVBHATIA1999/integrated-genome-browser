package com.affymetrix.igb.shared;

import com.affymetrix.genometry.operator.Operator;
import org.lorainelab.igb.genoviz.extensions.glyph.StyledGlyph;
import java.util.ArrayList;
import java.util.List;

public class TrackTransformAction extends TrackFunctionOperationA {

    private static final long serialVersionUID = 1L;

    public TrackTransformAction(Operator operator) {
        super(operator);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        super.actionPerformed(e);
        List<StyledGlyph> tiers;
        for (StyledGlyph glyph : Selections.allGlyphs) {
            tiers = new ArrayList<>();
            tiers.add(glyph);
            addTier(tiers);
        }
    }
}
