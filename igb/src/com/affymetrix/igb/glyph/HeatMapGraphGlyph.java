package com.affymetrix.igb.glyph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.symmetry.GraphSym;
import com.affymetrix.genoviz.bioviews.ViewI;

public class HeatMapGraphGlyph extends AbstractGraphGlyph {
	public HeatMapGraphGlyph(GraphSym graf, GraphState gstate) {
		super(graf, gstate);
	}

	@Override
	public String getViewMode() {
		return "heatmapgraph";
	}

	@Override
	protected void doBigDraw(Graphics g, GraphSym graphSym,
			Point curr_x_plus_width, Point max_x_plus_width,
			float ytemp, int draw_end_index, int i
		) {
		double heatmap_scaling = (double) (state.getHeatMap().getColors().length - 1) / (getVisibleMaxY() - getVisibleMinY());
		int heatmap_index = (int) (heatmap_scaling * (ytemp - getVisibleMinY()));
		if (heatmap_index < 0) {
			heatmap_index = 0;
		} else if (heatmap_index > 255) {
			heatmap_index = 255;
		}
		g.setColor(state.getHeatMap().getColor(heatmap_index));
		drawRectOrLine(g, curr_point.x, getPixelBox().y, Math.max(1, curr_x_plus_width.x - curr_point.x), getPixelBox().height + 1);
	}

	@Override
	protected void drawAxisLabel(ViewI view) {
		return;
	}

	@Override
	protected void drawSingleRect(
			int ymin_pixel, int plot_bottom_ypixel, int plot_top_ypixel, int ymax_pixel, Graphics g, int ysum, int points_in_pixel, int i) {
		int ystart = Math.max(Math.min(ymin_pixel, plot_bottom_ypixel), plot_top_ypixel);
		double heatmap_scaling = 1;
		if (state.getHeatMap() != null) {
			Color[] heatmap_colors = state.getHeatMap().getColors();
			// scale based on pixel position, not cooord position, since most calculations below are in pixels
			heatmap_scaling = (double) (heatmap_colors.length - 1) / (-plot_top_ypixel + plot_bottom_ypixel);
		}
		g.setColor(state.getHeatMap().getColor((int) (heatmap_scaling * (plot_bottom_ypixel - ystart))));
		drawRectOrLine(g, prev_point.x, plot_top_ypixel, 1, plot_bottom_ypixel - plot_top_ypixel);
	}

	@Override
	protected void doDraw(ViewI view) {
		double xpixels_per_coord = ( view.getTransform()).getScaleX();
		double xcoords_per_pixel = 1 / xpixels_per_coord;
		if ((xcoords_per_pixel < transition_scale)) {
			this.oldDraw(view);
		} else {
			drawSmart(view);
		}
	}
}
