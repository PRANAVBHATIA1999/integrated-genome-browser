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

package com.affymetrix.igb.genometry;

import java.util.*;

import com.affymetrix.genometry.*;

public class CurationSym extends SimpleSymWithProps {
  CurationSym prev_curation;
  CurationSym next_curation;
  ArrayList evidence;
  
  public void setSuccessor(CurationSym sym) {
    next_curation = sym;
  }

  public CurationSym getSuccessor() {
    return next_curation;
  }
  
  public void setPredecessor(CurationSym sym) {
    prev_curation = sym;
  }

  public CurationSym getPredecessor() {
    return prev_curation;
  }

  public void addEvidence(SeqSymmetry ev) {
    if (evidence == null) {
      evidence = new ArrayList();
    }
    evidence.add(ev);
  }

  public ArrayList getEvidence() {
    return evidence;
  }

}
