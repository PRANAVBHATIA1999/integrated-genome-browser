/**
 *   Copyright (c) 1998-2005 Affymetrix, Inc.
 *
 *   Licensed under the Common Public License, Version 1.0 (the "License").
 *   A copy of the license must be included with any distribution of
 *   this source code.
 *   Distributions from Affymetrix, Inc., place this in the
 *   IGB_LICENSE.html file.
 *
 *   The license is also available at
 *   http://www.opensource.org/licenses/cpl.php
 */

package com.affymetrix.genoviz.glyph;

import com.affymetrix.genoviz.bioviews.Glyph;
import com.affymetrix.genoviz.bioviews.ViewI;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * A glyph that draws a line graph.
 * BasicGraphGlyph is still somewhat experimental.
 * In particular hit detection needs improvement.
 */
public class BasicGraphGlyph extends Glyph {

	private double xcoords[];
	private double ycoords[];

	@Override
	public void draw(ViewI view) {
		view.transformToPixels(getCoordBox(), getPixelBox());
		Graphics g = view.getGraphics();
		g.setColor(getBackgroundColor());
		int beg_index = 0;
		int end_index = xcoords.length-1;

		g.setColor(getForegroundColor());
		Point2D.Double coord = new Point2D.Double(0,0);
		Point curr_point = new Point(0,0);
		Point prev_point = new Point(0,0);

		coord.x = xcoords[beg_index];
		// flipping about yaxis... should probably make this optional
		coord.y = -ycoords[beg_index];
		view.transformToPixels(coord, prev_point);

		for (int i = beg_index; i <= end_index; i++) {
			coord.x = xcoords[i];
			// flipping about yaxis... should probably make this optional
			coord.y = -ycoords[i];
			view.transformToPixels(coord, curr_point);
			g.drawLine(prev_point.x, prev_point.y, curr_point.x, curr_point.y);
			prev_point.x = curr_point.x;
			prev_point.y = curr_point.y;
		}
	}

	/**
	 * Defines a set of points to graph.
	 * The parameters are two parallel arrays of x and y coordinates.
	 * So there are <var>n</var> points,
	 * each of which has coordinates (xcoords[<var>n</var>], ycoords[<var>n</var>]).
	 * @param xcoords the x coordinates in scene space.
	 * @param ycoords the y coordinates in scene space.
	 */
	public void setPointCoords(double xcoords[], double ycoords[]) {
		this.xcoords = xcoords;
		this.ycoords = ycoords;
	}
}
