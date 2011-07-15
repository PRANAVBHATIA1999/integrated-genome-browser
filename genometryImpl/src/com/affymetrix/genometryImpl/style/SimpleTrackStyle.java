/**
 *   Copyright (c) 2007 Affymetrix, Inc.
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

package com.affymetrix.genometryImpl.style;

import java.awt.Color;

public final class SimpleTrackStyle extends DefaultTrackStyle implements ITrackStyleExtended {

	// Should be called only from within package or from StateProvider.
	protected SimpleTrackStyle(String name, boolean is_graph) {
		super(name, is_graph);
	}

	String url;
	public void setUrl(String url) {this.url = url;}
	public String getUrl() { return url; }

	boolean colorByScore = false;
	public void setColorByScore(boolean b) {this.colorByScore = b;}
	public boolean getColorByScore() { return this.colorByScore;}

	/** Default implementation returns the same as {@link #getColor()}. */
	public Color getScoreColor(float f) { return getForeground(); }

	int depth=2;
	public void setShow2Tracks(int i) {this.depth = i;}
	public int getShow2Tracks() {return this.depth;}

	boolean separate = true;
	public void setSeparate(boolean b) { this.separate = b; }
	public boolean getSeparate() { return this.separate; }

	String labelField = "id";
	public void setLabelField(String s) { this.labelField = s; }
	public String getLabelField() { return labelField; }

	@Override
		public void copyPropertiesFrom(ITrackStyle g) {
			super.copyPropertiesFrom(g);  
			if (g instanceof ITrackStyleExtended) {
				ITrackStyleExtended as = (ITrackStyleExtended) g;
				setUrl(as.getUrl());
				setColorByScore(as.getColorByScore());
				setShow2Tracks(as.getShow2Tracks());
				setSeparate(as.getSeparate());
				setLabelField(as.getLabelField());
			}
		}

	public String getFileType() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setDirectionType(int s) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getDirectionType() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setStartColor(Color c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Color getStartColor() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setEndColor(Color c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Color getEndColor() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
