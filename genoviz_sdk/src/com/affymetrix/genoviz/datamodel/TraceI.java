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

/**
 * models a sequence trace typically generated by an
 * automated sequencing machine.
 */
public interface TraceI {

  /**
   * indicates that the sequence is on the forward strand
   * relative to some consensus sequence.
   */
  public static final int FORWARD = 1;

  /**
   * indicates that the sequence is on the reverse strand
   * relative to some consensus sequence.
   */
  public static final int REVERSE = -1;

  /**
   *  sets the orientation.
   *
   *  @param forward true iff FORWARD
   *  @see #FORWARD
   *  @see #REVERSE
   */
  public void setOrientation(boolean forward);

  /**
   * gets the number of bases base the trace represents.
   *
   * @return the total number of bases called in this trace.
   */
  public int getBaseCount();

  /**
   * gets the number of places in the trace that data were sampled.
   *
   * @return the total number of data points in this trace
   */
  public int getTraceLength();

  /**
   * gets the maximum trace sample score in the trace.
   *
   * @return the maximum TraceSample score in the whole trace
   */
  public int getMaxValue();

  /**
   * gets the bases called from the trace.
   *
   * @return an array of the CalledBases.
   * @deprecated use {@link #getActiveBaseCalls()}.getBaseCalls().
   */
  public BaseCall[] getBaseArray();

  /**
   * gets the bases called from the trace.
   *
   * @return a String representing the bases called.
   * @deprecated use {@link #getActiveBaseCalls()}.getBaseString().
   */
  public String getBaseString();

  /**
   * gets the sample at a given point.
   *
   * @param index points to a position in the trace
   * @return the TraceSample for the indexed data point.
   */
  public TraceSample sampleAt(int index);

  /**
   * gets the base called at a given point in the trace.
   * Note that there may be several points within a particular called base.
   * However, there will be only one base called at a particular point.
   *
   * @param index points to a position in the trace.
   * @return the CalledBase at the indexed data point.
   * @deprecated use {@link #getActiveBaseCalls()}.getBaseCallAtSampleIndex().
   */
  public BaseCall getBaseAtSampleIndex(int index);

  /**
   * replace all the base calls with a new set.
   *
   * @param theCalls to use as replacements.
   * @deprecated use {@link #getActiveBaseCalls()}.setBaseCalls().
   */
  public void replaceBaseCalls( BaseCall[] theCalls );

  /**
   * gets the base called at a given point in the called sequence.
   * Note that an index of five means give me the fifth base called.
   *
   * @param index points to a position in the sequence of called bases.
   * @return the indexed base call.
   * @deprecated use {@link #getActiveBaseCalls()}.getBaseCall().
   */
  public BaseCall getBaseCall(int index);

  /**
   * gets the name assigned to the sequence.
   *
   * @return the name.
   */
  public String getName();

  /**
   * returns
   * true if Sequence is reverse complement relative to trace bases,
   * false otherwise.
   */
  public boolean isFlipped();

  /**
   * Performs a virtual reverse complement of the entire trace.
   * Each sample point in the trace is reversed,
   * so that the last point in the trace is the first point
   * in the reverse trace,
   * and the base values are switched for their complement.
   *
   * @return the new reverse complement Trace.
   */
  public Trace reverseComplement();

  public void setActiveBaseCalls( BaseCalls theCalls ) ;

  public BaseCalls getActiveBaseCalls() ;

}
