package com.affymetrix.igb.graphTypes;

import com.affymetrix.genometry.style.GraphType;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genoviz.bioviews.ViewI;
import org.lorainelab.igb.genoviz.extensions.glyph.GraphGlyph;
import java.awt.Graphics;
import java.awt.Point;

public class DotGraphType extends GraphGlyph.GraphStyle {

    public DotGraphType(GraphGlyph graphGlyph) {
        graphGlyph.super();
    }

    @Override
    public String getName() {
        return "dotgraph";
    }

    @Override
    protected void doBigDraw(Graphics g, GraphSym graphSym,
            Point curr_x_plus_width, Point max_x_plus_width, float ytemp,
            int draw_end_index, double offset, double yscale, ViewI view, int i) {
        if (!graphSym.hasWidth()) {
            g.drawLine(curr_point.x, curr_point.y, curr_point.x, curr_point.y); // point
//			g.fillRect(curr_point.x, curr_point.y, curr_x_plus_width.x, 4); // point
        } else {
            g.drawLine(curr_point.x, curr_point.y, curr_x_plus_width.x, curr_point.y);
//			final int width = Math.max(1, curr_x_plus_width.x - curr_point.x);
//			g.fillRect(curr_point.x, curr_point.y, width, 4);
        }
    }

    @Override
    public GraphType getGraphStyle() {
        return GraphType.DOT_GRAPH;
    }

}
