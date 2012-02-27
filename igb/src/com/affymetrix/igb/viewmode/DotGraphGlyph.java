
package com.affymetrix.igb.viewmode;

import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.symmetry.GraphSym;
import com.affymetrix.igb.shared.AbstractGraphGlyph;

import java.awt.Graphics;
import java.awt.Point;

/**
 *
 * @author lfrohman
 */
class DotGraphGlyph extends AbstractGraphGlyph {

	public DotGraphGlyph(GraphSym graf, GraphState gstate) {
		super(graf, gstate);
	}

	@Override
	public String getName() {
		return "dotgraph";
	}

	@Override
	protected void doBigDraw(Graphics g, GraphSym graphSym, Point curr_x_plus_width, Point max_x_plus_width, float ytemp, int draw_end_index, int i) {
		if (!graphSym.hasWidth()) {
			g.drawLine(curr_point.x, curr_point.y, curr_point.x, curr_point.y); // point
		} else {
			g.drawLine(curr_point.x, curr_point.y, curr_x_plus_width.x, curr_point.y);
		}
	}
    
}
