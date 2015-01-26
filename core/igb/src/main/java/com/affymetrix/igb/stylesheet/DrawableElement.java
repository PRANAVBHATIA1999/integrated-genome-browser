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

package com.affymetrix.igb.stylesheet;

import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genoviz.bioviews.GlyphI;
import com.lorainelab.igb.genoviz.extensions.api.SeqMapViewExtendedI;

interface DrawableElement extends Cloneable, XmlAppender {
  GlyphI symToGlyph(SeqMapViewExtendedI gviewer, SeqSymmetry sym, 
    GlyphI container, Stylesheet stylesheet, PropertyMap context);  
}
