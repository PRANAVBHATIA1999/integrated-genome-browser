/**
 *   Copyright (c) 2006-2007 Affymetrix, Inc.
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

import com.affymetrix.genometryImpl.parsers.graph.BarParser;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.MutableSeqSymmetry;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.ZipInputStream;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.util.SeqUtils;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.SimpleSymWithProps;
import com.affymetrix.genometryImpl.SeqSymSummarizer;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.CompositeGraphSym;
import com.affymetrix.genometryImpl.comparator.SeqSpanComparator;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.das2.Das2RequestLog;
import com.affymetrix.genometryImpl.parsers.BedParser;
import com.affymetrix.genometryImpl.parsers.BgnParser;
import com.affymetrix.genometryImpl.parsers.Bprobe1Parser;
import com.affymetrix.genometryImpl.parsers.BpsParser;
import com.affymetrix.genometryImpl.parsers.BrsParser;
import com.affymetrix.genometryImpl.parsers.CytobandParser;
import com.affymetrix.genometryImpl.parsers.Das2FeatureSaxParser;
import com.affymetrix.genometryImpl.parsers.ExonArrayDesignParser;
import com.affymetrix.genometryImpl.parsers.GFFParser;
import com.affymetrix.genometryImpl.parsers.PSLParser;
import com.affymetrix.genometryImpl.parsers.useq.ArchiveInfo;
import com.affymetrix.genometryImpl.parsers.useq.USeqGraphParser;
import com.affymetrix.genometryImpl.parsers.useq.USeqRegionParser;
import com.affymetrix.genometryImpl.parsers.useq.USeqUtilities;
import com.affymetrix.genoviz.util.GeneralUtils;

/*
 * Desired optimizations:
 *
 *      Split up by range // not really an optmization, but necessary for other optimizations
 *   0. Split up by type  // not really an optmization, but necessary for other optimizations
 *   1. Format selection
 *   2. overlap with prior query filter
 *   3. whole-sequence-based persistent caching???
 *   4. addition of containment constraints to ensure uniqueness
 *   4. full persistent caching based on (2)
 */
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.util.LocalUrlCacher;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class Das2ClientOptimizer {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_HEADERS = false;

    /**
     *  For DAS/2 version >= 300, the segment part of location-based feature filters is split
     *  out into a separate query field, "segment", that applies to all location-based filters in the query
     *  (overlaps, inside, ??)
     */
    private static final String default_format = "das2feature";
    

    // input is a single Das2FeatureRequestSym
    // output is List of _optimized_ Das2FeatureRequestSyms that are equivalent to input request,
    //    based on current state of GenometryModel/AnnotatedSeqGroup/BioSeq
    //    annotations --
    //    Could this strategy handle persistent caching??   Would need to redirect to
    //      file:// URLs on disk, but how to deal with proper setting of headers???
    //      would need to have additional info persisted on disk with header info...
    //      but for general case, need more info on disk anyway, to quickly find bounds
    //
    // also attaches
    // assume for now one type, one overlap span
    public static List<Das2FeatureRequestSym> loadFeatures(Das2FeatureRequestSym request_sym) {
        List<Das2FeatureRequestSym> output_requests = new ArrayList<Das2FeatureRequestSym>();
       
        SeqSpan overlap_span = request_sym.getOverlapSpan();
        BioSeq seq = overlap_span.getBioSeq();
        
        Das2Type type = request_sym.getDas2Type();
        String typeid = type.getID();

        if (seq == null) {
            System.out.println("Can't optimize DAS/2 query for type: " + typeid + ", seq is null!");
            output_requests.add(request_sym);
        } else {
			OptimizeDas2Query(seq, typeid, type, output_requests, request_sym);
        }

        for (Das2FeatureRequestSym request : output_requests) {
            optimizedLoadFeatures(request);
        }

        return output_requests;
    }


    private static void OptimizeDas2Query(
			BioSeq aseq, String typeid, Das2Type type, List<Das2FeatureRequestSym> output_requests, Das2FeatureRequestSym request_sym) {
        Das2RequestLog request_log = request_sym.getLog();
		// overlap_span and overlap_sym should actually be the same object, a LeafSeqSymmetry
		SeqSymmetry overlap_sym = request_sym.getOverlapSym();
		Das2Region region = request_sym.getRegion();
        MutableSeqSymmetry cont_sym;
        // this should work even for graphs, now that graphs are added to BioSeq's type hash (with id as type)
        cont_sym = (MutableSeqSymmetry) aseq.getAnnotation(typeid);
        // little hack for GraphSyms, need to resolve when to use id vs. name vs. type

		if (cont_sym == null && typeid.endsWith(".bar")) {
			cont_sym = (MutableSeqSymmetry) aseq.getAnnotation(type.getName());
            if (DEBUG) {
                System.out.println("trying to use type name for bar type, name: " + type.getName() + ", id: " + typeid);
				System.out.println("cont_sym: " + cont_sym);
			}
        }
        if ((cont_sym == null) || (cont_sym.getChildCount() == 0)) {
            if (DEBUG) {
                System.out.println("Can't optimize DAS/2 query, no previous annotations of type: " + typeid);
            }
            output_requests.add(request_sym);
        } else {
            int prevcount = cont_sym.getChildCount();
            if (DEBUG) {
                System.out.println("  child count: " + prevcount);
            }

            List<SeqSymmetry> prev_overlaps = new ArrayList<SeqSymmetry>(prevcount);
            for (int i = 0; i < prevcount; i++) {
                SeqSymmetry prev_request = cont_sym.getChild(i);
                if (prev_request instanceof Das2FeatureRequestSym) {
                    prev_overlaps.add(((Das2FeatureRequestSym) prev_request).getOverlapSym());
                } else if (prev_request instanceof GraphSym) {
                    prev_overlaps.add(prev_request);
                }
            }
            SeqSymmetry prev_union = SeqSymSummarizer.getUnion(prev_overlaps, aseq);
            List<SeqSymmetry> qnewlist = new ArrayList<SeqSymmetry>();
            qnewlist.add(overlap_sym);
            List<SeqSymmetry> qoldlist = new ArrayList<SeqSymmetry>();
            qoldlist.add(prev_union);
            SplitQuery(qnewlist, qoldlist, aseq, request_log, typeid, prev_union, type, region, output_requests);
        }
    }


    private static void SplitQuery(List<SeqSymmetry> qnewlist, List<SeqSymmetry> qoldlist, BioSeq aseq, Das2RequestLog request_log, String typeid, SeqSymmetry prev_union, Das2Type type, Das2Region region, List<Das2FeatureRequestSym> output_requests) {
        SeqSymmetry split_query = SeqSymSummarizer.getExclusive(qnewlist, qoldlist, aseq);
        if (split_query == null || split_query.getChildCount() == 0) {
            // all of current query overlap range covered by previous queries, so return empty list
            if (DEBUG) {
                System.out.println("ALL OF NEW QUERY COVERED BY PREVIOUS QUERIES FOR TYPE: " + typeid);

            }
            return;
        }

        SeqSpan split_query_span = split_query.getSpan(aseq);
        if (DEBUG) {
            System.out.println("DAS/2 optimizer, split query: " + SeqUtils.symToString(split_query));
        }
        // figure out min/max within bounds based on location of previous queries relative to new query
        int first_within_min;
        int last_within_max;
        List<SeqSpan> union_spans = SeqUtils.getLeafSpans(prev_union, aseq);
        SeqSpanComparator spancomp = new SeqSpanComparator();
        // since prev_union was created via SeqSymSummarizer, spans should come out already
        //   sorted by ascending min (and with no overlaps)
        //          Collections.sort(union_spans, spancomp);
        int insert = Collections.binarySearch(union_spans, split_query_span, spancomp);
        if (insert < 0) {
            insert = -insert - 1;
        }
        if (insert == 0) {
            first_within_min = 0;
        } else {
            first_within_min = (union_spans.get(insert - 1)).getMax();
        }
        // since sorted by min, need to make sure that we are at the insert index
        //   at which get(index).min >= exclusive_span.max,
        //   so increment till this (or end) is reached
        while ((insert < union_spans.size()) && ((union_spans.get(insert)).getMin() < split_query_span.getMax())) {
            insert++;
        }
        if (insert == union_spans.size()) {
            last_within_max = aseq.getLength();
        } else {
            last_within_max = (union_spans.get(insert)).getMin();
        }
        // done determining first_within_min and last_within_max
        splitIntoSubSpans(split_query, aseq, first_within_min, last_within_max, request_log, type, region, output_requests, typeid);
    }

    private static void splitIntoSubSpans(
            SeqSymmetry split_query, BioSeq aseq, int first_within_min, int last_within_max, Das2RequestLog request_log, Das2Type type, Das2Region region, List<Das2FeatureRequestSym> output_requests, String typeid) {
        int split_count = split_query.getChildCount();
        int cur_within_min;
        int cur_within_max;
        for (int k = 0; k < split_count; k++) {
            SeqSymmetry csym = split_query.getChild(k);
            SeqSpan ospan = csym.getSpan(aseq);
            if (k == 0) {
                cur_within_min = first_within_min;
            } else {
                cur_within_min = ospan.getMin();
            }
            if (k == (split_count - 1)) {
                cur_within_max = last_within_max;
            } else {
                cur_within_max = ospan.getMax();
            }
            SeqSpan ispan = new SimpleSeqSpan(cur_within_min, cur_within_max, aseq);
            if (DEBUG) {
                System.out.println("   new request: " + SeqUtils.spanToString(ispan));
            }
            Das2FeatureRequestSym new_request = new Das2FeatureRequestSym(type, region, ospan, ispan);
            output_requests.add(new_request);
        }
    }

    private static Das2RequestLog optimizedLoadFeatures(Das2FeatureRequestSym request_sym) {
        Das2RequestLog request_log = request_sym.getLog();
        request_log.setSuccess(true);

        Das2Region region = request_sym.getRegion();
        SeqSpan overlap_span = request_sym.getOverlapSpan();
        SeqSpan inside_span = request_sym.getInsideSpan();
        String overlap_filter = Das2FeatureSaxParser.getRangeString(overlap_span, false);
        String inside_filter = inside_span == null ? null : Das2FeatureSaxParser.getRangeString(inside_span, false);
       
        if (DEBUG) {
            System.out.println("^^^^^^^  in Das2ClientOptimizer.optimizedLoadFeatures(), overlap = " + overlap_filter +
                    ", inside = " + inside_filter);
        }
        Das2Type type = request_sym.getDas2Type();
        String format = request_sym.getFormat();
        // if format already specified in Das2FeatureRequestSym, don't optimize
        if (format == null) {
            format = FormatPriorities.getFormat(type);
            request_sym.setFormat(format);
        }

        BioSeq aseq = region.getAnnotatedSeq();
        Das2VersionedSource versioned_source = region.getVersionedSource();
        AnnotatedSeqGroup seq_group = versioned_source.getGenome();

        Das2Capability featcap = versioned_source.getCapability(Das2VersionedSource.FEATURES_CAP_QUERY);
        String request_root = featcap.getRootURI().toString();

        if (DEBUG) {
            System.out.println("   request root: " + request_root);
            System.out.println("   preferred format: " + format);
        }

        try {
            String query_part = DetermineQueryPart(region, overlap_filter, inside_filter, type, format);

            if (format == null) {
                format = default_format;
            }

            String feature_query = request_root + "?" + query_part;
            if (DEBUG) {
                System.out.println("feature query URL:  " + feature_query);
                System.out.println("url-encoded query URL:  " + URLEncoder.encode(feature_query, IGBConstants.UTF8));
                System.out.println("url-decoded query:  " + URLDecoder.decode(feature_query, IGBConstants.UTF8));

            }
			boolean success = LoadFeaturesFromQuery(overlap_span, aseq, feature_query, format, request_log, seq_group, type, request_sym);
			request_log.setSuccess(success);
		} catch (Exception ex) {
			ex.printStackTrace();
			request_log.setSuccess(false);
			//request_log.setException(ex);
		}
		return request_log;
    }

    private static String DetermineQueryPart(Das2Region region, String overlap_filter, String inside_filter, Das2Type type, String format) throws UnsupportedEncodingException {
      StringBuffer buf = new StringBuffer(200);
		buf.append("segment=");
		buf.append(URLEncoder.encode(region.getID(), IGBConstants.UTF8));
		buf.append(";");
        buf.append("overlaps=");
        buf.append(URLEncoder.encode(overlap_filter, IGBConstants.UTF8));
        buf.append(";");
        if (inside_filter != null) {
            buf.append("inside=");
            buf.append(URLEncoder.encode(inside_filter, IGBConstants.UTF8));
            buf.append(";");
        }
        buf.append("type=");
        buf.append(URLEncoder.encode(type.getID(), IGBConstants.UTF8));
        if (format != null) {
            buf.append(";");
            buf.append("format="); 
            buf.append(URLEncoder.encode(format, IGBConstants.UTF8));
        }
        String query_part = buf.toString();

        return query_part;
    }

    private static boolean LoadFeaturesFromQuery(
            SeqSpan overlap_span, BioSeq aseq, String feature_query, String format, Das2RequestLog request_log,
            AnnotatedSeqGroup seq_group, Das2Type type, Das2FeatureRequestSym request_sym)
            throws SAXException, IOException, IOException {

        /**
         *  Need to look at content-type of server response
         */
        BufferedInputStream bis = null;
        InputStream istr = null;
        String content_subtype = null;
        
        try {
            // if overlap_span is entire length of sequence, then check for caching
            if ((overlap_span.getMin() == 0) && (overlap_span.getMax() == aseq.getLength())) {
                istr = LocalUrlCacher.getInputStream(feature_query);
                if (istr == null) {
                    System.out.println("Server couldn't be accessed with query " + feature_query);
                    request_log.setSuccess(false);
                    return false;
                }
                // for now, assume that when caching, content type returned is same as content type requested
                content_subtype = format;
            } else {
                URL query_url = new URL(feature_query);
                if (DEBUG) {
                    System.out.println("    opening connection " + feature_query);
                }
                // casting to HttpURLConnection, since Das2 servers should be either accessed via either HTTP or HTTPS
                HttpURLConnection query_con = (HttpURLConnection) query_url.openConnection();
                int response_code = query_con.getResponseCode();
                String response_message = query_con.getResponseMessage();

                //request_log.setHttpResponse(response_code, response_message);

                if (DEBUG) {
                    System.out.println("http response code: " + response_code + ", " + response_message);
                }

                if (DEBUG_HEADERS) {
                    int hindex = 0;
                    while (true) {
                        String val = query_con.getHeaderField(hindex);
                        String key = query_con.getHeaderFieldKey(hindex);
                        if (val == null && key == null) {
                            break;
                        }
                        System.out.println("header:   key = " + key + ", val = " + val);
                        hindex++;
                    }
                }

                if (response_code != 200) {
                    System.out.println("WARNING, HTTP response code not 200/OK: " + response_code + ", " + response_message);
                }

                if (response_code >= 400 && response_code < 600) {
                    System.out.println("Server returned error code, aborting response parsing!");
                    request_log.setSuccess(false);
                    return false;
                }
                String content_type = query_con.getContentType();
				istr = query_con.getInputStream();

				content_subtype = content_type.substring(content_type.indexOf("/") + 1);
				int sindex = content_subtype.indexOf(';');
				if (sindex >= 0) {
					content_subtype = content_subtype.substring(0, sindex);
					content_subtype = content_subtype.trim();
				}
				if (DEBUG) {
					System.out.println("content type: " + content_type);
					System.out.println("content subtype: " + content_subtype);
				}
				if (content_subtype == null || content_type.equals("unknown") || content_subtype.equals("unknown") || content_subtype.equals("xml") || content_subtype.equals("plain")) {
					// if content type is not descriptive enough, go by what was requested
					content_subtype = format;
				}
            }

            if (request_log.getSuccess()) {
				AddParsingLogMessage(content_subtype);
                List feats = DetermineFormatAndParse(content_subtype, request_log, istr, feature_query, seq_group, type);
                addSymmetriesAndAnnotations(feats, request_sym, request_log, aseq);
            }
            return request_log.getSuccess();
        } finally {
            GeneralUtils.safeClose(bis);
            GeneralUtils.safeClose(istr);
        }
    }

    private static List DetermineFormatAndParse(
            String extension, Das2RequestLog request_log, InputStream istr, String feature_query, AnnotatedSeqGroup seq_group,
            Das2Type type)
            throws IOException, SAXException {
		BufferedInputStream bis = new BufferedInputStream(istr);
		GenometryModel gmodel = GenometryModel.getGenometryModel();
        List feats = null;
        if (extension.equals(Das2FeatureSaxParser.FEATURES_CONTENT_SUBTYPE)
                || extension.equals("das2feature")
                || extension.equals("das2xml")
                || extension.startsWith("x-das-feature")) {
            Das2FeatureSaxParser parser = new Das2FeatureSaxParser();
            InputSource isrc = new InputSource(bis);
            feats = parser.parse(isrc, feature_query, seq_group, false);
        } else if (extension.equals("bed")) {
            BedParser parser = new BedParser();
			feats = parser.parse(bis, gmodel, seq_group, false, type.getID(), false);
        } else if (extension.equals("bgn")) {
            BgnParser parser = new BgnParser();
            feats = parser.parse(bis, type.getID(), seq_group, false);
        } else if (extension.equals("bps")) {
            DataInputStream dis = new DataInputStream(bis);
            feats = BpsParser.parse(dis, type.getID(), null, seq_group, false, false);
        } else if (extension.equals("brs")) {
            DataInputStream dis = new DataInputStream(bis);
            feats = BrsParser.parse(dis, type.getID(), seq_group, false);
        } else if (extension.equals("bar")) {
            feats = BarParser.parse(bis, gmodel, seq_group, type.getName(), false);
        } else if (extension.equals("useq")) {
        	//find out what kind of data it is, graph or region, from the ArchiveInfo object
        	ZipInputStream zis = new ZipInputStream(bis); 
    		zis.getNextEntry(); 
        	ArchiveInfo archiveInfo = new ArchiveInfo(zis, false);
            if (archiveInfo.getDataType().equals(ArchiveInfo.DATA_TYPE_VALUE_GRAPH)){
            	USeqGraphParser gp = new USeqGraphParser();
                feats = gp.parseGraphSyms(zis, gmodel, type.getName(), archiveInfo);
            }
            else {
            	 USeqRegionParser rp = new USeqRegionParser();
                 feats = rp.parse(zis, seq_group, type.getName(), false, archiveInfo);
            }  
        }else if (extension.equals("bp2")) {
            Bprobe1Parser bp1_reader = new Bprobe1Parser();
            // parsing probesets in bp2 format, also adding probeset ids
            feats = bp1_reader.parse(bis, seq_group, false, type.getName(), false);
        } else if (extension.equals("ead")) {
            ExonArrayDesignParser parser = new ExonArrayDesignParser();
            feats = parser.parse(bis, seq_group, false, type.getName());
        } else if (extension.equals("gff")) {
            GFFParser parser = new GFFParser();
            feats = parser.parse(bis, ".", seq_group, false, false);
        } else if (extension.equals("link.psl")) {
            PSLParser parser = new PSLParser();
            parser.setIsLinkPsl(true);
            parser.enableSharedQueryTarget(true);
            // annotate _target_ (which is chromosome for consensus annots, and consensus seq for probeset annots
            // why is annotate_target parameter below set to false?
            feats = parser.parse(bis, type.getName(), null, seq_group, null, false, false, false); // do not annotate_other (not applicable since not PSL3)
        } else if (extension.equals("cyt")) {
            CytobandParser parser = new CytobandParser();
            feats = parser.parse(bis, seq_group, false);
        } else if (extension.equals("psl")) {
            // reference to LoadFileAction.ParsePSL
            PSLParser parser = new PSLParser();
            parser.enableSharedQueryTarget(true);
            DataInputStream dis = new DataInputStream(bis);
            feats = parser.parse(dis, type.getName(), null, seq_group, null, false, false, false);
        } else {
            System.out.println("ABORTING FEATURE LOADING, FORMAT NOT RECOGNIZED: " + extension);
            request_log.setSuccess(false);
        }
        return feats;
    }

     private static void AddParsingLogMessage(String content_subtype) {
        System.out.println("PARSING " + content_subtype.toUpperCase() + " FORMAT FOR DAS2 FEATURE RESPONSE");
    }

     private static void addSymmetriesAndAnnotations(List feats, Das2FeatureRequestSym request_sym, Das2RequestLog request_log, BioSeq aseq) {
        boolean no_graphs = true;
        if (feats == null || feats.isEmpty()) {
            // because many operations will treat empty Das2FeatureRequestSym as a leaf sym, want to
            //    populate with empty sym child/grandchild
            //    [ though a better way might be to have request sym's span on aseq be dependent on children, so
            //       if no children then no span on aseq (though still an overlap_span and inside_span) ]
            SimpleSymWithProps child = new SimpleSymWithProps();
            SimpleSymWithProps grandchild = new SimpleSymWithProps();
            child.addChild(grandchild);
            request_sym.addChild(child);
        } else if (request_log.getSuccess()) {
            // checking success again, could have changed before getting to this point...
            int feat_count = feats.size();
            System.out.println("parsed query results, annot count = " + feat_count);
            for (int k = 0; k < feat_count; k++) {
                SeqSymmetry feat = (SeqSymmetry) feats.get(k);
                if (feat instanceof GraphSym) {
                    addChildGraph((GraphSym) feat, request_sym.getDas2Type(), request_sym.getOverlapSpan());
                    no_graphs = false; // should either be all graphs or no graphs
                } else {
                    request_sym.addChild(feat);
                }
            }
        }
        if (no_graphs) {
            // if graphs, then adding to annotation BioSeq is already handled by addChildGraph() method
            synchronized (aseq) {
                aseq.addAnnotation(request_sym);
            }
        }
    }


    /**
     *  Given a child GraphSym, find the appropriate parent [Composite]GraphSym and add child to it
     *
     *  Assumes ids of parent graphs are unique among annotations on seq
     *  Also use Das2FeatureRequestSym overlap span as span for child GraphSym
     *  Uses type URI as graph ID, type name as graph name
     */
   private static void addChildGraph(GraphSym cgraf, Das2Type type, SeqSpan overlapSpan) {
		if (DEBUG) {
			System.out.println("adding a child GraphSym to parent graph");
		}
		BioSeq aseq = cgraf.getGraphSeq();
		GraphSym pgraf = getParentGraph(type, aseq, cgraf);

		// since GraphSyms get a span automatically set to the whole seq when constructed, need to first
		//    remove that span, then add overlap span from Das2FeatureRequestSym
		//    could instead create new span based on start and end xcoord, but for better integration with
		//    rest of Das2ClientOptimizer span of request is preferred
		cgraf.removeSpan(cgraf.getSpan(aseq));
		cgraf.addSpan(overlapSpan);
		if (DEBUG) {
			System.out.println("   span of child graf: " + SeqUtils.spanToString(cgraf.getSpan(aseq)));
		}
		pgraf.addChild(cgraf);
		//add properties of child to parent
		pgraf.setProperties(cgraf.getProperties());
	}


	private static GraphSym getParentGraph(Das2Type type, BioSeq aseq, GraphSym cgraf) {
		// check and see if parent graph already exists
		String id = type.getID();
		String name = type.getName();
		
		//is it a useq graph? modify name and id for strandedness?
		if (name.endsWith(USeqUtilities.USEQ_EXTENSION_NO_PERIOD)){
			//strip off useq
			id = id.replace(USeqUtilities.USEQ_EXTENSION_WITH_PERIOD, "");
			name = name.replace(USeqUtilities.USEQ_EXTENSION_WITH_PERIOD, "");
			//add strand?
			Object obj = cgraf.getProperty(GraphSym.PROP_GRAPH_STRAND);
			if (obj != null){
				String strand = null;
				Integer strInt = (Integer)obj;
				if (strInt.equals(GraphSym.GRAPH_STRAND_PLUS)) strand = "+";
				else if (strInt.equals(GraphSym.GRAPH_STRAND_MINUS)) strand = "-";
				if (strand != null){
					id = id+strand;
					name = name+strand;
				}
			}
		}
		
		if (DEBUG) {
			System.out.println("   child graph id: " + id);
			System.out.println("   child graph name: " + name);
			System.out.println("   seq: " + aseq.getID());
		}
		GraphSym pgraf = (GraphSym) aseq.getAnnotation(id);
		if (pgraf == null) {
			if (DEBUG) {
				System.out.println("$$$$ creating new parent composite graph sym");
			}
			// don't need to uniquify ID, since already know it's null (since no sym retrieved from aseq)
			pgraf = new CompositeGraphSym(id, aseq);
			pgraf.setGraphName(name);
			aseq.addAnnotation(pgraf);
		}
		return pgraf;
	}
}
