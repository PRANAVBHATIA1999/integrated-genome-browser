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

import java.awt.*;

import com.affymetrix.genoviz.bioviews.ViewI;

/**
 *  A glyph that is drawn as a centered circle.
 *
 *  Centered at center point of glyph's coordinates and with 
 *  diameter equal to the width or height, whichever is smaller.
 */
public class CenteredCircleGlyph extends SolidGlyph  {
	// could instead inherit from FillOvalGlyph and just override calcPixels

	public void draw(ViewI view) {
		calcPixels(view);
		if (getPixelBox().width == 0) { getPixelBox().width = 1; }
		if (getPixelBox().height == 0) { getPixelBox().height = 1; }
		Graphics g = view.getGraphics();
		g.setColor(getBackgroundColor());
		g.fillOval(getPixelBox().x, getPixelBox().y, getPixelBox().width, getPixelBox().height);
		super.draw(view);
	}

	public void calcPixels(ViewI view) {
		super.calcPixels(view);
		if (getPixelBox().width > getPixelBox().height) {
			getPixelBox().x = getPixelBox().x+getPixelBox().width/2-getPixelBox().height/2;
			getPixelBox().width = getPixelBox().height;
		}
		else if (getPixelBox().height > getPixelBox().width)  {
			getPixelBox().y = getPixelBox().y + getPixelBox().height/2 - getPixelBox().width/2;
			getPixelBox().height = getPixelBox().width;
		}
	}

}
