/**
 *   Copyright (c) 2001-2007 Affymetrix, Inc.
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

package com.affymetrix.genometry;

public interface MutableAnnotatedBioSeq extends AnnotatedBioSeq, MutableBioSeq {

	public void addAnnotation(SeqSymmetry annot);
	public void removeAnnotation(SeqSymmetry annot);
	//public void removeAnnotation(int index);
}
