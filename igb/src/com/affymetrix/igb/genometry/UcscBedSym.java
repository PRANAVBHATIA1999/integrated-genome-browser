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

import java.io.*;
import java.util.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.*;
import com.affymetrix.genometry.symmetry.*;

/**
 *  A SeqSymmetry (as well as SeqSpan) representation of UCSC BED format annotatations.
 *  <pre>
 *  From http://genome.ucsc.edu/goldenPath/help/customTrack.html#BED
 *  BED format provides a flexible way to define the data lines that are displayed
 *  in an annotation track. BED lines have three required fields and nine additional
 *  optional fields. The number of fields per line must be consistent throughout
 *  any single set of data in an annotation track.
 *
 * The first three required BED fields are:
 *    chrom - The name of the chromosome (e.g. chr3, chrY, chr2_random) or contig (e.g. ctgY1).
 *    chromStart - The starting position of the feature in the chromosome or contig.
 *         The first base in a chromosome is numbered 0.
 *    chromEnd - The ending position of the feature in the chromosome or contig. The chromEnd
 *         base is not included in the display of the feature. For example, the first 100 bases
 *         of a chromosome are defined as chromStart=0, chromEnd=100, and span the bases numbered 0-99.
 * The 9 additional optional BED fields are:
 *    name - Defines the name of the BED line. This label is displayed to the left of the BED line
 *        in the Genome Browser window when the track is open to full display mode.
 *    score - A score between 0 and 1000. If the track line useScore attribute is set to 1 for
 *        this annotation data set, the score value will determine the level of gray in which
 *        this feature is displayed (higher numbers = darker gray).
 *    strand - Defines the strand - either '+' or '-'.
 *    thickStart - The starting position at which the feature is drawn thickly (for example,
 *        the start codon in gene displays).
 *    thickEnd - The ending position at which the feature is drawn thickly (for example,
 *        the stop codon in gene displays).
 *    reserved - This should always be set to zero.
 *    blockCount - The number of blocks (exons) in the BED line.
 *    blockSizes - A comma-separated list of the block sizes. The number of items in this list
 *        should correspond to blockCount.
 *    blockStarts - A comma-separated list of block starts. All of the blockStart positions
 *        should be calculated relative to chromStart. The number of items in this list should
 *        correspond to blockCount.
 * WARNING -- relying on parser to modify blockStarts so they are in chrom/seq coordinates
 *            (NOT relative to chromStart)
 *
 * Example:
 *   Here's an example of an annotation track that uses a complete BED definition:
 *
 *  track name=pairedReads description="Clone Paired Reads" useScore=1
 *  chr22 1000 5000 cloneA 960 + 1000 5000 0 2 567,488, 0,3512
 *  chr22 2000 6000 cloneB 900 - 2000 6000 0 2 433,399, 0,3601
 * </pre>
 */
public class UcscBedSym implements SeqSpan, SeqSymmetry, SupportsCdsSpan, TypedSym, SymWithProps  {
  BioSeq seq; // "chrom"
  int txMin; // "chromStart"
  int txMax; // "chromEnd"
  String name; // "name"
  float score; // "score"
  boolean forward; // "strand"
  int cdsMin = Integer.MIN_VALUE;  // "thickStart" (if = Integer.MIN_VALUE then cdsMin not used)
  int cdsMax = Integer.MIN_VALUE;  // "thickEnd" (if = Integer.MIN_VALUE then cdsMin not used)
  int[] blockMins; // "blockStarts" + "txMin"
  int[] blockMaxs; // "blockStarts" + "txMin" + "blockSizes"
  String type;
  Map props;
  boolean hasCdsSpan = false;

  public UcscBedSym(String type, BioSeq seq, int txMin, int txMax, String name, float score,
		    boolean forward, int cdsMin, int cdsMax, int[] blockMins, int[] blockMaxs) {
    this.type = type;
    this.seq = seq;  // replace chrom name-string with reference to chrom BioSeq
    this.txMin = txMin;
    this.txMax = txMax;
    this.name = name;
    this.score = score;
    this.forward = forward;
    this.cdsMin = cdsMin;
    this.cdsMax = cdsMax;
    hasCdsSpan = ((cdsMin != Integer.MIN_VALUE) && (cdsMax != Integer.MIN_VALUE));

    this.blockMins = blockMins;
    this.blockMaxs = blockMaxs;
  }

  public String getName() { return name; }
  public String getType() { return type; }

  public boolean hasCdsSpan() { return hasCdsSpan; }
  public SeqSpan getCdsSpan() {
    if (! hasCdsSpan()) { return null; }
    if (forward) { return new SimpleSeqSpan(cdsMin, cdsMax, seq); }
    else { return new SimpleSeqSpan(cdsMax, cdsMin, seq); }
  }

  public String getID() { return name; }
  public SeqSpan getSpan(BioSeq bs) {
    if (bs.equals(this.seq)) { return this; }
    else { return null; }
  }

  public SeqSpan getSpan(int index) {
    if (index == 0) { return this; }
    else { return null; }
  }

  public boolean getSpan(BioSeq bs, MutableSeqSpan span) {
    if (bs.equals(this.seq)) {
      if (forward) {
	span.set(txMin, txMax, seq);
      }
      else {
	span.set(txMax, txMin, seq);
      }
      return true;
    }
    else { return false; }
  }

  public boolean getSpan(int index, MutableSeqSpan span) {
    if (index == 0) {
      if (forward) {
	span.set(txMin, txMax, seq);
      }
      else {
	span.set(txMax, txMin, seq);
      }
      return true;
    }
    else { return false; }
  }
  
  /** Always returns 1. */
  public int getSpanCount() { return 1; }
  
  /** Returns null if index is not 1. */
  public BioSeq getSpanSeq(int index) {
    if (index == 0) { return seq; }
    else { return null; }
  }

  public int getChildCount() {
    if (blockMins == null)  { return 0; }
    else  { return blockMins.length; }
  }

  public SeqSymmetry getChild(int index) {
    if (blockMins == null || (blockMins.length <= index)) { return null; }
    if (forward) {
      // blockMins are in seq coordinates, NOT relative to txMin
      //    (transforming blockStarts in BED format to blockMins in seq coordinates
      //       is handled by BedParser)
      //      return new SingletonSeqSymmetry(blockMins[index],
      //      				      blockMins[index] + blockSizes[index], seq);
      return new SingletonSeqSymmetry(blockMins[index], blockMaxs[index], seq);
    }
    else {
      return new SingletonSeqSymmetry(blockMaxs[index], blockMins[index], seq);
    }
  }
  
  // SeqSpan implementation
  public int getStart() { return (forward ? txMin : txMax); }
  public int getEnd() { return (forward ? txMax : txMin); }
  public int getMin() { return txMin; }
  public int getMax() { return txMax; }
  public int getLength() { return (txMax - txMin); }
  public boolean isForward() { return forward; }
  public BioSeq getBioSeq() { return seq; }
  public double getStartDouble() { return (double)getStart(); }
  public double getEndDouble() { return (double)getEnd(); }
  public double getMaxDouble() { return (double)getMax(); }
  public double getMinDouble() { return (double)getMin(); }
  public double getLengthDouble() { return (double)getLength(); }
  public boolean isIntegral() { return true; }

  public Map getProperties() {
    return cloneProperties();
  }

  public Map cloneProperties() {
    HashMap tprops = new HashMap();
    tprops.put("id", name);
    tprops.put("type", type);
    tprops.put("name", name);
    tprops.put("seq id", seq.getID());
    tprops.put("forward", new Boolean(forward));
    tprops.put("cds min", new Integer(cdsMin));
    tprops.put("cds max", new Integer(cdsMax));
    tprops.put("score", new Float(score));
    if (props != null) {
      tprops.putAll(props);
    }
    return tprops;
  }

  public Object getProperty(String key) {
    // test for standard gene sym  props
    if (key.equals("id")) { return name; }
    else if (key.equals("type")) { return getType(); }
    else if (key.equals("method"))  { return getType(); }
    else if (key.equals("name")) { return name; }
    else if (key.equals("seq id")) { return seq.getID(); }
    else if (key.equals("forward")) { return new Boolean(forward); }
    else if (key.equals("cds min")) { return new Integer(cdsMin); }
    else if (key.equals("cds max")) { return new Integer(cdsMax); }
    else if (key.equals("score")) { return new Float(score); }
    else if (props != null)  {
      return props.get(key);
    }
    else  { return null; }
  }

  public boolean setProperty(String name, Object val) {
    if (props == null) {
      props = new Hashtable();
    }
    props.put(name, val);
    return true;
  }

  public void outputBedFormat(Writer out) throws IOException  {
    out.write(seq.getID());
    out.write('\t');
    out.write(Integer.toString(txMin));
    out.write('\t');
    out.write(Integer.toString(txMax));
    // only first three fields are required

    // only keep going if has name
    if (name != null) {
      out.write('\t');
      out.write(name);
      // only keep going if has score field
      if (score > Float.NEGATIVE_INFINITY) {
	out.write('\t');
	out.write(Float.toString(score));
	out.write('\t');
	if (forward) { out.write("+"); }
	else { out.write("-"); }
	// only keep going if has thickstart/thickend
	if (cdsMin > Integer.MIN_VALUE &&
	    cdsMax > Integer.MIN_VALUE)  {
	  out.write('\t');
	  out.write(Integer.toString(cdsMin));
	  out.write('\t');
	  out.write(Integer.toString(cdsMax));
	  // only keep going if has blockcount/blockSizes/blockStarts
	  int child_count = this.getChildCount();
	  if (child_count > 0) {
	    out.write('\t');
	    // writing out extra "reserved" field, which currently should always be 0
	    out.write('0');
	    out.write('\t');
	    out.write(Integer.toString(child_count));
	    out.write('\t');
	    // writing blocksizes
	    for (int i=0; i<child_count; i++) {
	      out.write(Integer.toString(blockMaxs[i]-blockMins[i]));
	      out.write(',');
	    }
	    out.write('\t');
	    // writing blockstarts
	    for (int i=0; i<child_count; i++) {
	      out.write(Integer.toString(blockMins[i]-txMin));
	      out.write(',');
	    }
	  }
	}
      }
    }
    out.write('\n');

  }


}


