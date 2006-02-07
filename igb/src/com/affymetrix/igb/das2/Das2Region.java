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

package com.affymetrix.igb.das2;

import java.io.*;
import java.net.*;
import java.util.*;
import org.xml.sax.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.util.SeqUtils;
import com.affymetrix.igb.genometry.SmartAnnotBioSeq;
import com.affymetrix.igb.genometry.AnnotatedSeqGroup;
import com.affymetrix.igb.parsers.Das2FeatureSaxParser;

public class Das2Region {
  String region_id;
  int length;
  String name;
  String info_url;  // doc_href
  //  java.util.List assembly;  // or should this be a SeqSymmetry??   // or composition of CompositeBioSeq??
  SeqSpan segment_span;
  MutableAnnotatedBioSeq aseq;
  Das2VersionedSource versioned_source;
  //  int start;   // no longer used
  //  int end;     // no longer used
  //  boolean forward;  // no longer used

  //  public Das2Region(Das2VersionedSource source, String id, int start, int end, boolean forward_orient) {
  public Das2Region(Das2VersionedSource source, String id, String nm, String info, int ln) {
    region_id = id;
    name = nm;
    info_url = info;
    length = ln;

    versioned_source = source;
    AnnotatedSeqGroup genome = versioned_source.getGenome();
    // a)  see if id of Das2Region maps directly to an already seen annotated seq in genome
    aseq = genome.getSeq(region_id);
    if (aseq == null) { aseq = genome.getSeq(name); }
    // b) if can't find a previously seen genome for this DasSource, then
    //     create a new genome entry
    if (aseq == null) {
      aseq = new SmartAnnotBioSeq(region_id, genome.getID(), length);
      genome.addSeq(aseq);
    }
    segment_span = new SimpleSeqSpan(0, length, aseq);
  }


  public String getID() { return region_id; }  // or should ID be a URI?
  public String getName() { return name; }
  public String getInfoUrl() { return info_url; }
  public Das2VersionedSource getVersionedSource() { return versioned_source; }

  public SeqSpan getSegment() {
    return segment_span;
  }

  // or should this return a SmartAnnotbioSeq???
  public MutableAnnotatedBioSeq getAnnotatedSeq() {
    return aseq;
  }


  /**
   *  Retrieves features from a DAS2 server based on inforation in the Das2FeatureRequestSym.
   *  Takes an uninitialized Das2FeatureRequestSym as an argument,
   *    constructs a DAS2 query based on sym,
   *    sends query to DAS2 server
   *    hands returned data to a parser, get List of annotations back from parser
   *    adds annotations as children to Das2FeatureRequestSym
   *    (should it also add request_sym to SmartAnnotBioSeq (becomes child of container, or
   *         should that be handled before/after getFeatures() is called?  leaning towards the latter...)
   *    also at some point needs to figure out what format to request returned result in
   *         (based on Das2Type of request_sym)
   *
   *  returns true if feature query returns successfully, false otherwise
   *
   */
  public boolean getFeatures(Das2FeatureRequestSym request_sym) {
    boolean success = true;
      SeqSpan overlap_span = request_sym.getOverlapSpan();
      String overlap_filter = Das2FeatureSaxParser.getPositionString(overlap_span, false, false);
      SeqSpan inside_span = request_sym.getInsideSpan();
      String inside_filter = Das2FeatureSaxParser.getPositionString(inside_span, false, false);
      System.out.println("in Das2Region.getFeatures(), overlap = " + overlap_filter + ", inside = " + inside_filter);
      Das2Type type = request_sym.getDas2Type();
      String format = FormatPriorities.getFormat(type);

      String version_url =
	getVersionedSource().getSource().getServerInfo().getRootUrl() + "/" +
	//      getVersionedSource().getSource().getID() + "/" +
	getVersionedSource().getID();
      System.out.println("   version url: " + version_url);
      System.out.println("   preferred format: " + format);

      StringBuffer buf = new StringBuffer(200);

      buf.append(version_url);
      buf.append("/feature?");
      buf.append("overlaps=");
      buf.append(overlap_filter);
      buf.append(";");
      if (inside_filter != null) {
	buf.append("inside=");
	buf.append(inside_filter);
	buf.append(";");
      }
      buf.append("type=");
      buf.append(type.getID());

      String feature_query = buf.toString();
      System.out.println("feature query:  " + feature_query);

      // need to move actual parsing out of here and into something more generalized
      //     also need to figure out where to interject a Das2QueryOptimizer...
      //      Das2ClientParserController.parseFromUrl(feature_query);
      try {
	Das2FeatureSaxParser parser = new Das2FeatureSaxParser();
	URL query_url = new URL(feature_query);
	URLConnection query_con = query_url.openConnection();
	InputStream istr = query_con.getInputStream();
	BufferedInputStream bis = new BufferedInputStream(istr);
	List feats = parser.parse(new InputSource(bis), getVersionedSource().getGenome(), false);
	int feat_count = feats.size();
	System.out.println("parsed query results, annot count = " + feat_count);
	for (int k=0; k<feat_count; k++) {
	  SeqSymmetry feat = (SeqSymmetry)feats.get(k);
	  //	SeqUtils.printSymmetry(feat);
	  request_sym.addChild(feat);
	}
	success = true;
	bis.close();
	istr.close();
      }
      catch (Exception ex) {
	ex.printStackTrace();
	success = false;
      }
    return success;
  }

}
