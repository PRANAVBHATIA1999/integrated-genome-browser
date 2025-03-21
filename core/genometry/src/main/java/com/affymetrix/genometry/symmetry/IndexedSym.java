/**
 * Copyright (c) 2001-2004 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.genometry.symmetry;

import com.affymetrix.genometry.symmetry.impl.ScoredContainerSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;

public interface IndexedSym extends SeqSymmetry {

    public void setParent(ScoredContainerSym parent);

    public ScoredContainerSym getParent();

    public void setIndex(int index);

    public int getIndex();
}
