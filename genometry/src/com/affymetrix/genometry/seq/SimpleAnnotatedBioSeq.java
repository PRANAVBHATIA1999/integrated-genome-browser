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

import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.AnnotatedBioSeq;
import com.affymetrix.genometry.MutableAnnotatedBioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.SeqSymmetry;

public class SimpleAnnotatedBioSeq
  extends SimpleBioSeq
  implements MutableAnnotatedBioSeq {

  protected List annots;

  public SimpleAnnotatedBioSeq(String id, int length, List annots) {
    this(id, length);
    this.annots = annots;
  }

  public SimpleAnnotatedBioSeq(String id, String residues) {
    super(id, residues);
  }

  public SimpleAnnotatedBioSeq(String id, int length)  {
    super(id, length);
  }

  public SimpleAnnotatedBioSeq(String id)  {
    super(id);
  }

  public void addAnnotation(SeqSymmetry annot) {
    if (null == annots) { annots = new ArrayList(); }
    annots.add(annot);
  }

  public void removeAnnotation(SeqSymmetry annot) {
    if (null != annots) annots.remove(annot);
  }

  public void removeAnnotation(int index) {
    if (null != annots) annots.remove(index);
  }

  public int getAnnotationCount() { 
    if (null != annots) return annots.size();
    else return 0;
  }

  public SeqSymmetry getAnnotation(int index) {
    if (null != annots && index < annots.size())
      return (SeqSymmetry)annots.get(index);
    else
      return null;
  }

  public SeqSymmetry getAnnotationByID(String id) {
    if (null != annots) {
      int s = annots.size();
      for (int i = 0; i<s; i++) {
        SeqSymmetry ss = (SeqSymmetry)annots.get(i);
        if (null != ss) {
          String ssid = ss.getID();
          if (null != ssid && ssid.equals(id)) {
            return ss;
          }
        }
      } 
    }
    return null;
  }
  
  
  /**
   *  NOT YET IMPLEMENTED
   */
  public List getIntersectedAnnotations(SeqSpan span) {
    return annots;
  }

  /**
   *  NOT YET IMPLEMENTED
   */
  public List getContainedAnnotations(SeqSpan span) {
    return annots;
  }

}


