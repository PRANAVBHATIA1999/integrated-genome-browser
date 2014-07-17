package com.affymetrix.igb.shared;

import java.util.ArrayList;
import java.util.List;

import com.affymetrix.genometryImpl.operator.Operator;

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
            tiers = new ArrayList<StyledGlyph>();
            tiers.add(glyph);
            addTier(tiers);
        }
    }
}
