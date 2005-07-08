/**
*   Copyright (c) 2001-2004 Affymetrix, Inc.
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

package com.affymetrix.genometry.seq;

import java.util.Vector;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.*;
import com.affymetrix.genometry.util.SeqUtils;

/**
 *  A CompositeBioSeq that can have start less than 0.
 *  (Other BioSeqs only have a length, and start = 0, end = length is implicit)
 *  <p>
 *  <strong>Strongly urge</strong> that this is only used when BioSeq 
 *  <em>has</em> to have negative coords.
 *  <p>
 *  For example when renumbering from a given point in an
 *  AnnotatedBioSeq and want to do this via genometry, so need a
 *  CompositeBioSeq whose composition is the full span of the
 *  AnnotatedBioSeq, mapped (via a single symmetry) to the
 *  CompositeBioSeq with the desired zero-point at 0 (which pushes
 *  coords 5' to zero-point into negative coords)
 */
public class CompositeNegSeq extends SimpleCompositeBioSeq {

  int start;
  int end;

  /**
   *  assume start < end
   */
  public CompositeNegSeq(String id, int min, int max) {
    super(id);
    this.start = min;
    this.end = max;
    if (min > max) { 
      throw new RuntimeException("problem in CompositeNegSeq constructor! min must be < max");
    }
    this.length = end - start;
  }

  public int getMin() { return start; }
  public int getMax() { return end; }

  public void setBounds(int min, int max) {
    start = min;
    end = max;
    length = end - start;
  }

  public boolean isComplete() {
    return isComplete(start, end);
  }

  /**
   *  overriding getResidues() to include residues where position < 0
   */
  public String getResidues() { 
    return getResidues(start, end);
  }

  public String getResidues(int res_start, int res_end, char fillchar) {
    SeqSpan residue_span = new SimpleSeqSpan(res_start, res_end, this);
    int reslength = Math.abs(res_end - res_start);
    char[] char_array = new char[reslength];
    // start with all spaces
    for (int i=0; i<reslength; i++) {
      char_array[i] = fillchar;
    }
    SeqSymmetry rootsym = this.getComposition();
    // adjusting index into array to compensate for possible seq start < 0
    int array_offset = -start;  
    getResidues(residue_span, fillchar, rootsym, char_array, array_offset);
    String res = new String(char_array);
    return res;
  }
  

}


