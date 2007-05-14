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

package com.affymetrix.genoviz.datamodel;

import java.io.InputStream;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import com.affymetrix.genoviz.util.DNAUtils;

/**
 * Base implementation of the TraceI interface.
 *
 * <p> This implementation will have multiple sets of base calls.
 * It has an indicator of which set is the "active" one.
 * The required base call access methods will use the bases in the "active" set.
 * For the moment, the base calls are as they were, one set.
 */
public class Trace implements TraceI {

  /**
   * true if Sequence is reverse complement relative to trace bases
   * (for univiewer, need to test for this even when traces are not pulled up,
   * to determine correct direction to display for Sequence on the alignment.
   *  in the univiewer files, all sequences are forward in the alignment, but
   *  ones that have been flipped relative to their original traces should be
   *  displayed as reverse complement (arrows pointing left))
   * @deprecated I dont think this is ever used
   */
  protected boolean flipped;

  protected String name;

  protected Vector<BaseCall> baseVector;
  protected Vector<TraceSample> sampleVector;
  protected Hashtable<Integer,BaseCall> baseHashtable;
  protected StringBuffer seqBuffer;

  protected int left_clip;
  protected int right_clip;
  protected int max_trace_value;

  protected int orientation = FORWARD;

  protected int[] peak;

  private BaseCalls activeBaseCalls;

  public Trace() {
    sampleVector = new Vector<TraceSample>();
    baseVector = new Vector<BaseCall>();
    baseHashtable = new Hashtable<Integer,BaseCall>();
    seqBuffer = new StringBuffer();
  }

  public void setActiveBaseCalls( BaseCalls theCalls ) {
    if ( theCalls == this.activeBaseCalls ) {
      return; // It was already thus.
    }
    this.activeBaseCalls = theCalls;
  }
  public BaseCalls getActiveBaseCalls() {
    return this.activeBaseCalls;
  }

  /**
   * @return a Vector of TraceSamples,
   * one for each data point in the trace.
   */
  public Vector<TraceSample> getSampleVector() {
    return sampleVector;
  }

  /**
   *  sets the orientation.
   *
   *  @param forward true iff FORWARD
   *  @see TraceI#FORWARD
   *  @see TraceI#REVERSE
   */
  public void setOrientation(boolean forward) {
    if (forward) {
      orientation = FORWARD;
    } else {
      orientation = REVERSE;
    }
  }

  /**
   * @return an array of BaseCalls,
   * one for each base called in the trace.
   */
  public BaseCall[] getBaseArray() {
    return baseVector.toArray(new BaseCall[baseVector.size()]);
  }

  /**
   * @return the total number of called bases in this trace.
   */
  public int getBaseCount() {
    return baseVector.size();
  }

  /**
   * @return the total number of data points in this trace.
   */
  public int getTraceLength() {
    return sampleVector.size();
  }

  /**
   * @return the maximum TraceSample score in the whole trace.
   */
  public int getMaxValue() {
    return max_trace_value;
  }

  /**
   * @return a Vector of CalledBases, one for each base called in the trace.
   */
  public Vector getBaseVector() {
    return baseVector;
  }

  /**
   * @return the called sequence residues as a String.
   */
  public String getBaseString() {
    return seqBuffer.toString();
  }

  /**
   * @return the TraceSample for the indexed data point.
   */
  public TraceSample sampleAt(int index) {
    return sampleVector.elementAt(index);
  }

  /**
   * @return the BaseCall made at the indexed data point.
   */
  public BaseCall getBaseAtSampleIndex(int index) {
    return baseHashtable.get(index);
  }

  /**
   * searches the array of peak values
   * to find one near the given point.
   *
   * <p><em>Note that
   * this is like the Java 1.2 Arrays.binarySearch method.
   * When we go to Java 1.2,
   * this should be deprecated.
   * </em>
   *
   * @param theBases an array of peak values.
   * @param thePoint to look for.
   * @return an index into theBases.
   */
  private int binarySearch(int[] theBases, int thePoint) {
    int low = 0;
    int high = theBases.length-1;

    while (low <= high) {
      int mid =(low + high)/2;
      int midVal = theBases[mid];

      if (midVal < thePoint)
        low = mid + 1;
      else if (midVal > thePoint)
        high = mid - 1;
      else
        return mid; // thePoint found
    }
    return -(low + 1);  // thePoint not found.
  }

  /**
   * sets an array of peak points in the trace.
   * The array is used to find base indices
   * when given a point in the trace.
   *
   * <p><em>Note that
   * this should be done when setting the model.
   * i.e. at the same time that the baseVector is populated.
   * However, it would appear that parsers are using
   * getBaseVector to get a pointer to the vector
   * and adding items to it directly.
   * This practice may not be ideal.
   * They should also call this function
   * after populating baseVector.
   * </em>
   */
  public void setPeaks() {
    peak = new int[baseVector.size()];
    for (int i = baseVector.size()-1; 0 <= i; i--) {
      BaseCall b = (BaseCall)baseVector.elementAt(i);
      peak[i] = b.getTracePoint();
      //System.err.println(peak[i]);
    }
  }

  /**
   * gets a single peak.
   * The peaks correspond to called bases.
   * So the parameter passed should correspond
   * to a particular called base.
   *
   * @param theIndex into an array of peaks.
   * @return the magnitude of the trace at the given peak.
   */
  public int getPeak(int theIndex) {
    if (null == peak) {
      setPeaks();
    }
    return peak[theIndex];
  }

  /**
   * gets the index of the residue nearest the given point
   * in the trace.
   */
  public int getResidueIndexNear(int thePoint) {
    if (null == peak) {
      setPeaks();
    }
    int candidate = binarySearch(peak, thePoint);
    if (0 <= candidate) // exact match was found.
      return candidate;
    // Exact match was not found.
    int j = -(candidate + 1), i = j - 1;
    if (i < 0) return j;
    if (peak.length <= j) return i;
    if (thePoint < (peak[i] + peak[j]) / 2) return i;
    return j;
  }

  /**
   * @deprecated use {@link #getBaseCall}.
   */
  public BaseCall getBase(int index) {
    // quick fix for index out of bounds exceptions  6-31-98  GAH
    if (index < 0 || index > baseVector.size()-1)  {
      return null;
    }
    return (BaseCall) (baseVector.elementAt(index));
  }

  /**
   * gets the n'th base called.
   *
   * @param index into all the bases called.
   */
  public BaseCall getBaseCall(int index) {
    if (index < 0 || baseVector.size()-1 < index) {
      return null;
    }
    return (BaseCall) (baseVector.elementAt(index));
  }

  public String getName() {
    return name;
  }

  public boolean isFlipped() {
    return flipped;
  }

  public void setLeftClip(int left_clip) {
    this.left_clip = left_clip;
  }

  public int getLeftClip() {
    return left_clip;
  }

  public void setRightClip(int right_clip) {
    this.right_clip = right_clip;
  }

  public int getRightClip() {
    return right_clip;
  }

  /**
   * adds the trace values at a single sample point.
   *
   * @param sample_A the value of the Adenine trace at this point
   * @param sample_C the value of the Cytosine trace at this point
   * @param sample_G the value of the Guanine trace at this point
   * @param sample_T the value of the Thymine trace at this point
   */
  public void addSample(int sample_A, int sample_C, int sample_G, int sample_T){

    if (max_trace_value < sample_A) { max_trace_value = sample_A; }
    if (max_trace_value < sample_C) { max_trace_value = sample_C; }
    if (max_trace_value < sample_G) { max_trace_value = sample_G; }
    if (max_trace_value < sample_T) { max_trace_value = sample_T; }

    TraceSample sample = new TraceSample(sample_A,
        sample_C,
        sample_G,
        sample_T);
    sampleVector.addElement(sample);
  }

  public void replaceBaseCalls( BaseCall[] theCall ) {
    int i = this.baseVector.size();
    baseHashtable = new Hashtable<Integer,BaseCall>(i);
    baseVector = new Vector<BaseCall>(i);
    seqBuffer = new StringBuffer(i);
    for ( i = 0; i < theCall.length; i++ ) {
      addBase( theCall[i] );
    }
    setPeaks();
  }

  /**
   * adds a single called base.
   * Bases should be added in order from 5 prime to 3 prime.
   *
   * @param base the one to add.
   */
  public void addBase(BaseCall base) {
    baseHashtable.put(new Integer(base.getTracePoint()), base);
    baseVector.addElement(base);
    seqBuffer.append(base.getBase());
  }

  /**
   * Performs a virtual reverse complement of the entire trace.
   * Each sample point in trace is reversed,
   * so last point in trace is first point in reverse trace, and the
   * base values are switched for their complement
   */
  public Trace reverseComplement() {
    Trace rev = new Trace();
    Vector<TraceSample> rev_samples = new Vector<TraceSample>();
    int num_samples = sampleVector.size();
    TraceSample sample, rev_sample;

    // For reverse complement, each sample point in trace is reversed,
    // so last point in trace is first point in reverse trace, and the
    // base values are switched for their complement
    for (int i = num_samples-1; i >= 0; i--) {
      sample = (TraceSample)sampleVector.elementAt(i);
      rev_sample = sample.complement();
      rev_samples.addElement(rev_sample);
    }
    rev.sampleVector = rev_samples;
    rev.max_trace_value = max_trace_value;

    // flip around base array, and complement;
    // It is here for backward compatibility.
    BaseCall base;
    for (int i = baseVector.size() - 1; i >= 0; i--) {
      base = baseVector.elementAt(i).reverseComplement(num_samples);
      rev.addBase(base);
    }

    return rev;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Trace\n");
    sb.append("sampleVector.size(): " + sampleVector.size() + "\n");
    sb.append("baseVector.size(): " + baseVector.size() + "\n");
    int i = 0;
    Enumeration e = baseVector.elements();
    while (e.hasMoreElements()) {
      BaseCall cb = (BaseCall) e.nextElement();
      sb.append(cb.getBase());
      i++;
      if (i == 79) {
        sb.append('\n');
        i = 0;
      }
    }
    return sb.toString();
  }


  /**
   * @return an array of BaseCalls
   * one for each base called in the trace.
   */
  public BaseCall[] getBaseCalls() {
    BaseCall[] cba = new BaseCall[baseVector.size()];
    Enumeration e = baseVector.elements();
    int i = 0;
    while (e.hasMoreElements()) {
      BaseCall cb = (BaseCall) e.nextElement();
      cba[i] = cb;
      i++;
    }
    return cba;
  }

}
