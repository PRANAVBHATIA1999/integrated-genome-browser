package com.affymetrix.igb.shared;

import com.affymetrix.genometryImpl.style.GraphState;

import com.affymetrix.genoviz.bioviews.GlyphI;
import com.affymetrix.genoviz.bioviews.ViewI;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author hiralv
 */
public class GraphFasterExpandPacker extends FasterExpandPacker {

	private static final GraphGlyphPosComparator comparator = new GraphGlyphPosComparator();

	@Override
	public Rectangle pack(GlyphI parent, ViewI view, boolean manual) {
		int child_count = parent.getChildCount();
		if (child_count == 0) {
			return null;
		}
		Collections.sort(parent.getChildren(), comparator);
		return super.pack(parent, view, manual);
	}

	private static class GraphGlyphPosComparator implements Comparator<GlyphI> {

		public int compare(GlyphI g1, GlyphI g2) {
			if (!(g1 instanceof AbstractGraphGlyph) || !(g2 instanceof AbstractGraphGlyph)) {
				return Double.compare(g1.getCoordBox().x, g2.getCoordBox().x);
			}

			AbstractGraphGlyph gg1 = (AbstractGraphGlyph) g1;
			GraphState gs1 = gg1.graf.getGraphState();

			AbstractGraphGlyph gg2 = (AbstractGraphGlyph) g2;
			GraphState gs2 = gg2.graf.getGraphState();

			return Double.compare(gs1.getPosition(), gs2.getPosition());
		}
	}
}
