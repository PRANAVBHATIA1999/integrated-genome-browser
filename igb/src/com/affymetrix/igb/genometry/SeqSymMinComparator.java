/**
*   Copyright (c) 2001-2006 Affymetrix, Inc.
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

package com.affymetrix.igb.genometry;

import java.util.Comparator;
import com.affymetrix.genometry.*;

/**
 *  Sorts SeqSymmetries based first on {@link SeqSpan#getMin()},
 *   then on {@link SeqSpan#getMax()}.
 *
 *  @see  com.affymetrix.igb.genometry.SeqSymStartComparator
 */
public class SeqSymMinComparator implements Comparator {
  boolean ascending;
  BioSeq seq;

  /** Constructor.
   *  @param s  sequence to base the sorting on
   *  @param b  true to sort ascending, false for descending
   */
  public SeqSymMinComparator(BioSeq s, boolean b) {
    this.seq = s;
    this.ascending = b;
  }

  public void reset(BioSeq s, boolean b) {
    this.seq = s;
    this.ascending = b;
  }

  public int compare(Object obj1, Object obj2) {
    SeqSymmetry sym1 = (SeqSymmetry)obj1;
    SeqSymmetry sym2 = (SeqSymmetry)obj2;
    SeqSpan span1 = sym1.getSpan(seq);
    SeqSpan span2 = sym2.getSpan(seq);
    final int min1 = span1.getMin();
    final int min2 = span2.getMin();
    if (ascending) {
      if (min1 < min2) { return -1; }
      else if (min1 > min2) { return 1; }
      final int max1 = span1.getMax();
      final int max2 = span2.getMax();
      if (max1 < max2) { return -1; }
      else if (max1 > max2) { return 1; }
      else { return 0; }
    }
    else {
      if (min1 > min2) { return -1; }
      else if (min1 < min2) { return 1; }
      final int max1 = span1.getMax();
      final int max2 = span2.getMax();
      if (max1 > max2) { return -1; }
      else if (max1 < max2) { return 1; }
      else { return 0; }
    }
  }
  
}
