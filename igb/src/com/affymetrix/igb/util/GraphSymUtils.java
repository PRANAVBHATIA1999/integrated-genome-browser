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

package com.affymetrix.igb.util;

import java.io.*;
import java.util.*;

import com.affymetrix.genoviz.util.Timer;
import com.affymetrix.genometry.*;
import com.affymetrix.genometry.util.SeqUtils;
import com.affymetrix.igb.genometry.AnnotatedSeqGroup;
import com.affymetrix.igb.genometry.SmartAnnotBioSeq;
import com.affymetrix.igb.genometry.GraphSym;
import com.affymetrix.igb.genometry.SingletonGenometryModel;
import com.affymetrix.igb.parsers.Streamer;
import com.affymetrix.igb.parsers.SgrParser;
import com.affymetrix.igb.parsers.BarParser;
import com.affymetrix.igb.parsers.GrParser;
import com.affymetrix.igb.parsers.BgrParser;

public class GraphSymUtils {

  static Comparator pointcomp = new Point2DComparator(true, true);
  static boolean DEBUG_READ = false;
  static boolean DEBUG_DATA = false;

  /** 8-byte floating-point.  Names of the other data-type constants can be interpreted similarly. */
  public static int BYTE8_FLOAT = 0;
  public static int BYTE4_FLOAT = 1;
  public static int BYTE4_SIGNED_INT = 2;
  public static int BYTE2_SIGNED_INT = 3;
  public static int BYTE1_SIGNED_INT = 4;
  public static int BYTE4_UNSIGNED_INT = 5;
  public static int BYTE2_UNSIGNED_INT = 6;
  public static int BYTE1_UNSIGNED_INT = 7;

  public static String[] valstrings =
  { "BYTE8_FLOAT", "BYTE4_FLOAT",
    "BYTE4_SIGNED_INT", "BYTE2_SIGNED_INT", "BYTE1_SIGNED_INT",
    "BYTE4_UNSIGNED_INT", "BYTE2_UNSIGNED_INT", "BYTE1_UNSIGNED_INT" };

  /**
   *  Transforms a GraphSym based on a SeqSymmetry.
   *  This is _not_ a general algorithm for transforming GraphSyms with an arbitrary mapping sym --
   *    it is simpler, and assumes that the mapping symmetry is of depth=2 (or possibly 1?) and
   *    breadth = 2, and that they're "regular" (parent sym and each child sym have seqspans pointing
   *    to same two BioSeqs
   *  ensure_unique_id indicates whether should try to muck with id so it's not same as any GraphSym on the seq
   *     (including the original_graf, if it's one of seq's annotations)
   *     For transformed GraphSyms probably should set ensure_unique_id to false, unless result is actually added onto toseq...
   */
  public static GraphSym transformGraphSym(GraphSym original_graf, SeqSymmetry mapsym, boolean ensure_unique_id) {
    //    System.out.println("called GraphGlyphUtils.transformGraphSym(), mapping sym:");
    //    SeqUtils.printSymmetry(mapsym);
    //    System.out.println("");
    BioSeq fromseq = original_graf.getGraphSeq();
    SeqSpan fromspan = mapsym.getSpan(fromseq);
    GraphSym new_graf = null;
    if (fromseq != null && fromspan != null) {
      BioSeq toseq = SeqUtils.getOtherSeq(mapsym, fromseq);
      SeqSpan tospan = mapsym.getSpan(toseq);
      if (toseq != null && fromseq != null) {
	int[] xcoords = original_graf.getGraphXCoords();
	float[] ycoords = original_graf.getGraphYCoords();
	if (xcoords != null && ycoords != null) {
	  double graf_base_length = xcoords[xcoords.length-1] - xcoords[0];
	  // calculating graf length from xcoords, since graf's span
	  //    is (usually) incorrectly set to start = 0, end = seq.getLength();
	  double points_per_base = (double)xcoords.length / (double)graf_base_length;
	  int initcap = (int)(points_per_base * toseq.getLength() * 1.5);
	  //    System.out.println("initial capacity for new_xcoords DoubleList: " + initcap);
	  IntList new_xcoords = new IntList(initcap);
	  FloatList new_ycoords = new FloatList(initcap);
	  List leaf_syms = SeqUtils.getLeafSyms(mapsym);
	  for (int i=0; i<leaf_syms.size(); i++) {
	    SeqSymmetry leafsym = (SeqSymmetry)leaf_syms.get(i);
	    SeqSpan fspan = leafsym.getSpan(fromseq);
	    SeqSpan tspan = leafsym.getSpan(toseq);
	    if (fspan == null || tspan == null) { continue; }
	    boolean opposite_spans = fspan.isForward() ^ tspan.isForward();
	    int ostart = fspan.getStart();
	    int oend = fspan.getEnd();
	    int tstart = tspan.getStart();
	    int tend = tspan.getEnd();
	    double scale = tspan.getLengthDouble() / fspan.getLengthDouble();
	    if (opposite_spans) { scale = -scale; }
	    double offset = tspan.getStartDouble() - (scale * fspan.getStartDouble());
	    int kmax = xcoords.length;
	    // should really use a binary search here to speed things up...
	    // but right now just doing a brute force scan for each leaf span to map to toseq
	    //    any graph points that overlap fspan in fromseq
	    // assumes graph is sorted
	    int start_index = Arrays.binarySearch(xcoords, ostart);
	    if (start_index < 0)  { start_index = -start_index -1; }
	    else {
	      // start_index > 0, so found exact match, but possible it's part of group of exact matches,
	      //    so move to left until find a non-match
	      while ((start_index > 0) && (xcoords[start_index-1] == xcoords[start_index]))  { start_index--; }
	    }
	    if (start_index < 0) { start_index = 0; } // making sure previous conditional didn't result in index < 0
	    for (int k=start_index; k<kmax; k++) {
	      int old_xcoord = xcoords[k];
	      if (old_xcoord >= oend) { break; }
	      int new_xcoord = (int)((scale * old_xcoord) + offset);
	      new_xcoords.add(new_xcoord);
	      new_ycoords.add(ycoords[k]);
	    }
	  }
          String newid = original_graf.getID();
          if (ensure_unique_id)  { newid = GraphSymUtils.getUniqueGraphID(newid, toseq); }
	  new_graf = new GraphSym(new_xcoords.copyToArray(), new_ycoords.copyToArray(),
				  original_graf.getGraphName(), toseq);
	}
      }
    }
    return new_graf;
  }



  /** Detects whether the given filename ends with a recognized ending for
   *  a graph filetype. Compression endings like gz and zip are removed
   *  before testing the name.
   */
  public static boolean isAGraphFilename(String name) {
    String lc = Streamer.stripEndings(name).toLowerCase();
    return (
      lc.endsWith(".gr") ||
      lc.endsWith(".bgr") ||
      lc.endsWith(".bar") ||
      lc.endsWith(".sgr")
      );
  }

  /**
   *  Reads one or more graphs from an input stream.
   *  Equivalent to a call to the other readGraphs() method using seq = null.
   */
  public static List readGraphs(InputStream istr, String stream_name, AnnotatedSeqGroup seq_group) throws IOException  {
    return readGraphs(istr, stream_name, seq_group, (MutableAnnotatedBioSeq) null);
  }

  /**
   *  Reads one or more graphs from an input stream.
   *  Some graph file formats can contain only one graph, others contain
   *  more than one.  For consistency, always returns a List (possibly empty).
   *  Will accept "bar", "bgr", "gr", or "sgr".
   *  Loaded graphs will be attached to their respective BioSeq's, if they
   *  are instances of MutableAnnotatedBioSeq.
   *  @param seq  Ignored in most cases.  But for "gr" files that
   *   do not specify a BioSeq, use this parameter to specify it.  If null
   *   then SingletonGenometryModel.getSelectedSeq() will be used.
   */
  public static List readGraphs(InputStream istr, String stream_name, AnnotatedSeqGroup seq_group, BioSeq seq) throws IOException  {
    List grafs = null;
    StringBuffer stripped_name = new StringBuffer();
    InputStream newstr = Streamer.unzipStream(istr, stream_name, stripped_name);
    String sname = stripped_name.toString().toLowerCase();

    if (seq == null) {
      seq = SingletonGenometryModel.getGenometryModel().getSelectedSeq();
    }
    if (sname.endsWith(".bar"))  {
      grafs = BarParser.parse(newstr, seq_group, stream_name);
    }
    else if (sname.endsWith(".gr")) {
      if (seq == null) {
        throw new IOException("Must select a sequence before loading a graph of type 'gr'");
      }
      grafs = wrapInList(GrParser.parse(newstr, seq, stream_name));
    }
    /*
    else if (sname.endsWith(".sbar")) {
      if (seq == null) {
        throw new IOException("Must select a sequence before loading a graph of type 'sbar'");
      }
      grafs = wrapInList(readSbarFormat(newstr, seq));
    }
    */
    else if (sname.endsWith(".bgr")) {
      grafs = wrapInList(BgrParser.parse(newstr, seq_group));
    }
    else if (sname.endsWith(".sgr")) {
      SgrParser sgr_parser = new SgrParser();
      grafs = sgr_parser.parse(newstr, stream_name, seq_group, false);
    } else {
      throw new IOException("Unrecognized filename for a graph file:\n"+stream_name);
    }

    processGraphSyms(grafs, stream_name, stream_name);

    if (grafs == null) {
      grafs = Collections.EMPTY_LIST;
    }
    return grafs;
  }

  /**
   *  Returns input id if no GraphSyms on seq with given id.
   *  Otherwise uses id to build a new id that is not used by a GraphSym (or top-level container sym )
   *     currently on the seq
   *  The id returned is only unique for GraphSyms on that seq, may be used for graphs on other seqs
   */
  public static String getUniqueGraphID(String id, BioSeq seq) {
    if (id == null) { return null; }
    String newid = id;
    if (seq instanceof SmartAnnotBioSeq) {
      SmartAnnotBioSeq sab = (SmartAnnotBioSeq)seq;
      int prevcount = 0;
      while (sab.getAnnotation(newid) != null) {
	prevcount++;
	newid = id + "." + prevcount;
      }
    }
    else if (seq instanceof AnnotatedBioSeq)  {
      AnnotatedBioSeq aseq = (AnnotatedBioSeq)seq;
      // check every annotation on seq, but assume graphs are directly attached to seq, so
      //   don't have to do recursive descent into children?
      // potentially really bad performance, but this is just a fallback -- most
      //      seqs that GraphSyms are being attached to will be SmartAnnotBioSeqs and dealt with
      //      in the other branch of the conditional
      int prevcount = 0;
      int acount = aseq.getAnnotationCount();
      boolean hit = true;
      while (hit) {
	hit = false;
	for (int i=0; i<acount; i++) {
	  SeqSymmetry sym = aseq.getAnnotation(i);
	  if ((sym instanceof GraphSym) && (newid.equals(sym.getID()))) {
	      prevcount++;
	      newid = id + "." + prevcount;
	      hit = true;
	      break;
	  }
	}

      }
    }
    else {
      // BioSeq is not an AnnotatedBioSeq, so just punt and return input
    }
    return newid;
  }

  /** This is a wrapper around readGraphs() for the case where you expect to
   *  have a single GraphSym returned.  This will return only the first graph
   *  from the list returned by readGraphs(), or null.
   */
  public static GraphSym readGraph(InputStream istr, String stream_name, String graph_name, BioSeq seq) throws IOException  {
    //TODO: Maybe this should throw an exception if the file contains more than one graph?
    AnnotatedSeqGroup seq_group = SingletonGenometryModel.getGenometryModel().getSelectedSeqGroup();
    if (seq != null && seq instanceof MutableAnnotatedBioSeq) {
      MutableAnnotatedBioSeq gseq = seq_group.getSeq(seq.getID());
      if (gseq == null)  { seq_group.addSeq( (MutableAnnotatedBioSeq) seq); }
      else if (gseq != seq)  {
        throw new RuntimeException("ERROR! graph seq with id: " + seq.getID() +
                                   " is not same as seq found in group with id: " + gseq.getID() +
                                   " that matches via group.getSeq(id)");
      }
      // if seq is already part of AnnotatedSeqGroup, don't need to add to group
    }
    List grafs = readGraphs(istr, stream_name, seq_group, seq);
    GraphSym graf = null;
    if (grafs.size() > 0) {
      graf = (GraphSym) grafs.get(0);
      if (graph_name != null) {
	System.out.println("in GraphSymUtils.readGraph(), renaming graph");
	System.out.println("   old name: " + graf.getGraphName());
	System.out.println("   new name: " + graph_name);
        graf.setGraphName(graph_name);
      }
    }
    return graf;
  }


  static List wrapInList(GraphSym gsym) {
    List grafs = null;
    if (gsym != null) {
      grafs = new ArrayList();
      grafs.add(gsym);
    }
    return grafs;
  }

  /*
   *  Does some post-load processing of Graph Syms.
   *  For each GraphSym in the list,
   *  Adds it as an annotation of the BioSeq it refers to.
   *  Sets the "source_url" to the given stream name.
   *  Calls setGraphName() with the given name;
   *  Converts to a trans frag graph if "TransFrag" is part of the graph name.
   *  @param grafs  a List, empty or null is OK.
   */
  static void processGraphSyms(List grafs, String original_stream_name, String graph_name) {
    if (grafs != null)  {
      for (int i=0; i<grafs.size(); i++) {
        GraphSym gsym = (GraphSym)grafs.get(i);
        BioSeq gseq = gsym.getGraphSeq();
        if (gseq instanceof SmartAnnotBioSeq) {
	  String gid = gsym.getID();
	  String newid = getUniqueGraphID(gid, gseq);
	  if (newid != gid) {
	    gsym.setID(newid);
	  }
        }
	gsym.lockID();
	if (gseq instanceof MutableAnnotatedBioSeq)   {
	  ((MutableAnnotatedBioSeq)gseq).addAnnotation(gsym);
	}
        if (gsym != null)  {
          gsym.setProperty("source_url", original_stream_name);
        }
        if ((gsym.getGraphName() != null) && (gsym.getGraphName().indexOf("TransFrag") >= 0)) {
          gsym = GraphSymUtils.convertTransFragGraph(gsym);
        }
      }
    }
  }


  public static GraphSym revCompGraphSym(GraphSym gsym, BioSeq symseq, BioSeq revcomp_symseq) {
    int xpos[] = gsym.getGraphXCoords();
    float ypos[] = gsym.getGraphYCoords();
    int rcxpos[] = new int[xpos.length];
    float rcypos[] = new float[ypos.length];
    int seqlength = symseq.getLength();
    for (int i=0; i<xpos.length; i++) {
      rcxpos[i] = seqlength - xpos[xpos.length - i -1];
    }
    for (int i=0; i<ypos.length; i++) {
      rcypos[i] = ypos[ypos.length - i -1];
    }
    String newid = "rev_comp ( " + gsym.getID() + " )";
    newid = GraphSymUtils.getUniqueGraphID(newid, revcomp_symseq);
    GraphSym revcomp_gsym =
      new GraphSym(rcxpos, rcypos, newid, revcomp_symseq);
    return revcomp_gsym;
  }

  /** Passes to {@link com.affymetrix.igb.parsers.BgrParser#writeBgrFormat(GraphSym, OutputStream)}
   *  or {@link com.affymetrix.igb.parsers.GrParser#writeGrFormat(GraphSym, OutputStream)} depending
   *  on the suffix of the filename.
   *  @return true if the file was written sucessfully
   */
  public static boolean writeGraphFile(GraphSym gsym, String file_name) throws IOException {
    boolean result = false;
    BufferedOutputStream bos = null;
    try {
      if (file_name.endsWith(".bgr") || file_name.endsWith(".gr")) {
        bos = new BufferedOutputStream(new FileOutputStream(file_name));
        if (file_name.endsWith(".bgr")) { result =  BgrParser.writeBgrFormat(gsym, bos); }
        else if (file_name.endsWith(".gr")) { result = GrParser.writeGrFormat(gsym, bos); }
        //    else if (filename.endsWith(".sbar")){ result = writeSbarFormat(gsym, bos); }
      }
      else {
        ErrorHandler.errorPanel("Graph file name must end in .gr or .bgr suffix");
        result = false;
      }
    }
    finally {
      if (bos != null) try { bos.close(); } catch (IOException ioe) {}
    }
    return result;
  }


  public static void writeTagVal(DataOutputStream dos, String tag, String val)
    throws IOException  {
    dos.writeInt(tag.length());
    dos.writeBytes(tag);
    dos.writeInt(val.length());
    dos.writeBytes(val);
  }

  public static HashMap readTagValPairs(DataInputStream dis, int pair_count) throws IOException  {
    HashMap tvpairs = new HashMap(pair_count);
    if (DEBUG_READ) { System.out.println("seq tagval count: " + pair_count); }
    for (int i=0; i<pair_count; i++) {
      int taglength = dis.readInt();
      byte[] barray = new byte[taglength];
      dis.readFully(barray);
      String tag = new String(barray);
      // maybe should intern?
      //      String tag = (new String(barray)).intern();
      int vallength = dis.readInt();
      barray = new byte[vallength];
      dis.readFully(barray);
      String val = new String(barray);
      //      String val = (new String(barray)).intern();
      tvpairs.put(tag, val);
      if (DEBUG_READ)  { System.out.println("    tag = " + tag + ", val = " + val); }
    }
    return tvpairs;
  }

  /**
   *  Calculate percentile rankings of graph values.
   *  In the resulting array, the value of scores[i] represents
   *  the value at percentile (100 * i)/(scores.length - 1).
   *
   *  This is an expensive calc, due to sort of copy of scores array
   *    Plan to change this to a sampling strategy if scores.length greater than some cutoff (maybe 100,000 ?)
   */
  public static float[] calcPercents2Scores(float[] scores, float bins_per_percent) {
    Timer tim = new Timer();
    boolean USE_SAMPLING = true;
    int max_sample_size = 100000;
    float abs_max_percent = 100.0f;
    float percents_per_bin = 1.0f / bins_per_percent;

    int num_scores = scores.length;
    float[] ordered_scores;
    // sorting a large array is an expensive operation timewise, so if scores array is
    //   larger than a certain size, do approximate ranking instead by sampling the scores array
    //   and ranking over smaple
    //
    // in performance comparisons of System.arraycopy() vs. piecewise loop,
    //     piecewise takes about twice as long for copying same number of elements,
    //     but this 2x performance hit should be overwhelmed by time taken for larger array sort
    tim.start();
    if (USE_SAMPLING && (num_scores > (2 * max_sample_size)) ) {
      int sample_step = num_scores / max_sample_size;
      int sample_index = 0;
      ordered_scores = new float[max_sample_size];
      for (int i=0; i<max_sample_size; i++) {
	ordered_scores[i] = scores[sample_index];
	sample_index += sample_step;
      }
    }
    else {
      ordered_scores = new float[num_scores];
      System.arraycopy(scores, 0, ordered_scores, 0, num_scores);
    }
    Arrays.sort(ordered_scores);
    int num_percents = (int)(abs_max_percent * bins_per_percent + 1);
    float[] percent2score = new float[num_percents];

    float scores_per_percent = ordered_scores.length / 100.0f;
    for (float percent = 0.0f; percent <= abs_max_percent; percent += percents_per_bin) {
      int score_index = (int)(percent * scores_per_percent);
      if (score_index >= ordered_scores.length) { score_index = ordered_scores.length -1; }
      //      System.out.println("percent: " + percent + ", score_index: " + score_index
      //			 + ", percent_index: " + (percent * bins_per_percent));
      percent2score[(int)Math.round(percent * bins_per_percent)] = ordered_scores[score_index];
    }
    // just making sure max 100% is really 100%...
    percent2score[percent2score.length - 1] = ordered_scores[ordered_scores.length - 1];
    long t = tim.read();
    //    System.out.println("time taken for GraphSymUtils.calcPercents2Scores(): " + (t/1000f));
    return percent2score;
  }


  public static GraphSym convertTransFragGraph(GraphSym trans_frag_graph) {
    //    System.out.println("$$$ GraphSymUtils.convertTransFragGraph() called $$$");
    int transfrag_max_spacer = 20;
    BioSeq seq = trans_frag_graph.getGraphSeq();
    int[] xcoords = trans_frag_graph.getGraphXCoords();
    float[] ycoords = trans_frag_graph.getGraphYCoords();
    IntList newx = new IntList();
    FloatList newy = new FloatList();
    int xcount = xcoords.length;
    if (xcount < 2) { return null; }

    // transfrag ycoords should be irrelevant
    int xmin = xcoords[0];
    float y_at_xmin = ycoords[0];
    int prevx = xcoords[0];
    float prevy = ycoords[0];
    int curx = xcoords[0];
    float cury = ycoords[0] ;
    //    System.out.println("xcount: " + xcount);
    for (int i=1; i<xcount; i++) {
      curx = xcoords[i];
      cury = ycoords[i];
      if ((curx - prevx) > transfrag_max_spacer) {
	//	System.out.println("adding xmin = " + xmin + ", xmax = " + prevx + ", length = " + (prevx-xmin));
	newx.add(xmin);
	newy.add(y_at_xmin);
	newx.add(prevx);
	newy.add(prevy);
	//	if (i == (xcount-2)) { break; }
	//	System.out.println("i = " + i + ", xcount = " + xcount);
	if (i == (xcount-2)) {
	//	if (i == (xcount-2)) {
	  System.out.println("breaking, i = " + i + ", xcount = " + xcount);
	  break;
	}
	xmin = curx;
	y_at_xmin = cury;
	i++;
      }
      prevx = xcoords[i];
      prevy = ycoords[i];
    }
    //    System.out.println("adding xmin = " + xmin + ", curx = " + prevx + ", length = " + (curx-xmin));
    newx.add(xmin);
    newy.add(y_at_xmin);
    newx.add(curx);
    newy.add(cury);
    String newid = GraphSymUtils.getUniqueGraphID(trans_frag_graph.getGraphName(), seq);
    GraphSym span_graph = new GraphSym(newx.copyToArray(), newy.copyToArray(), newid, seq);

    // copy properties over...
    span_graph.setProperties(trans_frag_graph.cloneProperties());

    if (DEBUG_DATA) {
      int[] xnew = span_graph.getGraphXCoords();
      float[] ynew = span_graph.getGraphYCoords();
      for (int i=0; i<xnew.length; i++) {
        System.out.println("TransFrag graph point: x = " + xnew[i] + ", y = " + ynew[i]);
      }
    }

    // add transfrag property...
    span_graph.setProperty("TransFrag", "TransFrag");
    return span_graph;

  }
}

  /** Passing over to {@link #readGrFormat} */
  /*
 public static GraphSym loadGraph(File data_file, BioSeq aseq) {
    GraphSym gsym = null;
    try {
      FileInputStream fis = new FileInputStream(data_file);
      gsym = GrParser.parse(fis, aseq, null);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return gsym;
  }
  */

  /**
   * Writes sbar format.
   * <pre>
     Basic BAR (binary array) format:

     #bytes                type        content
     HEADER SECTION:
     8                char        "barr\r\n\032\n"
     4                float        version (1.0)
     4                int        #rows in data section (nrow)
     4                int        #columns in data section (ncol)
     4xncol                int        field types (see below for possible values)
     4                int        #tag-value pairs (ntag)
     4                int        tag len
     4xtaglen        char        tag string
     4                int        value len
     4xvaluelen        char        value string
   * </pre>
  */
  /*
  public static boolean writeSbarFormat(GraphSym graf, OutputStream ostr) throws IOException {
    boolean success = false;
    BufferedOutputStream bos = new BufferedOutputStream(ostr);
    DataOutputStream dos = new DataOutputStream(bos);
    int[] xcoords = graf.getGraphXCoords();
    float[] ycoords = graf.getGraphYCoords();
    int total_points = xcoords.length;

    // WRITING HEADER
    dos.writeBytes("barr\r\n\032\n");  // char  "barr\r\n\032\n"
    dos.writeFloat(1.0f);       // int  #rows in data section (nrow)
    dos.writeInt(total_points); // int  #columns in data section (ncol)
    dos.writeInt(2); // int  #columns in data section (ncol)
    dos.writeInt(2); // int  first column type ==> type 2 ==> 4-byte signed int
    dos.writeInt(1); // int  second column type==> type1 ==> 4-byte float

    Map props = graf.getProperties();
    int propcount = 0;
    if (props != null) { propcount = props.size(); }
    int tvcount = propcount + 2;
    dos.writeInt(tvcount); // int  #tag-value pairs (ntag)

    writeTagVal(dos, "graph_name", graf.getGraphName());
    writeTagVal(dos, "seq_name", graf.getGraphSeq().getID());

    if (props != null)  {
      Iterator propit = props.entrySet().iterator();
      while (propit.hasNext()) {
        Map.Entry keyval = (Map.Entry)propit.next();
        Object key = keyval.getKey();
        Object val = keyval.getValue();
        // doing toString() just in case properties contains non-String objects
        writeTagVal(dos, key.toString(), val.toString());
      }
    }

    // WRITING DATA
    for (int i=0; i<total_points; i++) {
      dos.writeInt((int)xcoords[i]);
      dos.writeFloat((float)ycoords[i]);
    }

    // WRITING FOOTER
    dos.writeBytes("END\n");
    dos.close();
    success = true;
    System.out.println("wrote .bar file to stream");
    return success;
  }
  */

  /*
  public static List readMbarFormat(InputStream istr, Map seqs, String stream_name)
    throws IOException {
    List graphs = null;
    BufferedInputStream bis = new BufferedInputStream(istr);
    DataInputStream dis = new DataInputStream(bis);
    String graph_name = "unknown";
    boolean bar2 = false;
    if (stream_name != null) { graph_name = stream_name; }
    // READING HEADER
    //    dis.readBytes("barr\r\n\032\n");  // char  "barr\r\n\032\n"
    byte[] headbytes = new byte[8];
    //    byte[] headbytes = new byte[10];
    dis.readFully(headbytes);
    String headstr = new String(headbytes);
    float version = dis.readFloat();       // int  #rows in data section (nrow)
    if (version >= 2.0f) { bar2 = true; }  // setting boolean bar2 for bar version2-specific conditionals
    int total_seqs = dis.readInt();
    int vals_per_point = dis.readInt(); // int  #columns in data section (ncol)
    if (DEBUG_READ) {
      System.out.println("header: " + headstr);
      System.out.println("version: " + version);
      System.out.println("total seqs: " + total_seqs);
      System.out.println("vals per point: " + vals_per_point);
    }
    int[] val_types = new int[vals_per_point];
    for (int i=0; i<vals_per_point; i++) {
      val_types[i] = dis.readInt();
      if (DEBUG_READ)  { System.out.println("val type for column " + i + ": " + valstrings[val_types[i]]); }
    }
    int tvcount = dis.readInt();
    if (DEBUG_READ) { System.out.println("tag-value count: " + tvcount); }
    HashMap file_tagvals = readTagValPairs(dis, tvcount);
    if (file_tagvals.get("file_type") != null) {
      graph_name += ":" + (String)file_tagvals.get("file_type");
    }
    int total_total_points = 0;
    for (int k=0; k<total_seqs; k++) {
      int namelength = dis.readInt();
      //      String
      byte[] barray = new byte[namelength];
      dis.readFully(barray);
      String seqname = new String(barray);
      if (DEBUG_READ)  { System.out.println("seq: " + seqname); }

      String groupname = null;
      if (bar2) {
	int grouplength = dis.readInt();
	barray = new byte[grouplength];
	dis.readFully(barray);
	groupname = new String(barray);
	if (DEBUG_READ)  { System.out.println("group length: " + grouplength + ", group: " + groupname); }
      }

      int verslength = dis.readInt();
      barray = new byte[verslength];
      dis.readFully(barray);
      String seqversion = new String(barray);
      if (DEBUG_READ) { System.out.println("version length: " + verslength + ", version: " + seqversion); }

      // hack to extract seq version and seq name from seqname field for bar files that were made
      //   with the version and name concatenated (with ";" separator) into the seqname field
      int sc_pos = seqname.lastIndexOf(";");
      if (sc_pos >= 0) {
        seqversion = seqname.substring(0, sc_pos);
	seqname = seqname.substring(sc_pos+1);
	if (DEBUG_READ)  { System.out.println("seqname = " + seqname + ", seqversion = " + seqversion); }
      }

      HashMap seq_tagvals = null;
      if (bar2) {
	int seq_tagval_count = dis.readInt();
	seq_tagvals = readTagValPairs(dis, seq_tagval_count);
      }

      int total_points = dis.readInt();
      total_total_points += total_points;
      System.out.println("seq " + k + ": name = " + seqname + ", version = " + seqversion +
			 ", group = " + groupname +
			 ", data points = " + total_points);
      //      System.out.println("total data points for graph " + k + ": " + total_points);
      MutableAnnotatedBioSeq seq = null;
      SynonymLookup lookup = SynonymLookup.getDefaultLookup();
      Iterator iter = seqs.values().iterator();
      // can't just hash, because _could_ be a synonym instead of an exact match

      while (iter.hasNext()) {
	// testing both seq id and version id (if version id is available)
        MutableAnnotatedBioSeq testseq = (MutableAnnotatedBioSeq) iter.next();
        if (lookup.isSynonym(testseq.getID(), seqname)) {
	  // GAH 1-23-2005
	  // need to ensure that if bar2 format, the seq group is also a synonym!
	  // GAH 7-7-2005
	  //    but now there's some confusion about seqversion vs seqgroup, so try all three possibilities:
	  //      groupname
	  //      seqversion
	  //      groupname + ":" + seqversion
	  if (seqversion == null || seqversion.equals("") || (! (testseq instanceof Versioned))) {
	    seq = testseq;
	    break;
	  }
	  else {
	    String test_version = ((Versioned)testseq).getVersion();
	    if ((lookup.isSynonym(test_version, seqversion)) ||
		(lookup.isSynonym(test_version, groupname)) ||
		(lookup.isSynonym(test_version, (groupname + ":" + seqversion))) ) {
	      if (DEBUG_READ) { System.out.println("found synonymn"); }
	      seq = testseq;
	      break;
	    }
	  }
        }
      }
      if (seq == null) {
	if (bar2 && groupname != null) {
	  seqversion = groupname + ":" + seqversion;
	}
        System.out.println("seq not found, creating new seq:  name = " + seqname + ", version = " + seqversion);
        seq = new NibbleBioSeq(seqname, seqversion, 500000000);
      }
      //      System.out.println("seq: " + seq);
      if (vals_per_point == 1) {
        System.err.println("PARSING FOR BAR FILES WITH 1 VALUE PER POINT NOT YET IMPLEMENTED");
      }
      else if (vals_per_point == 2) {
        if (val_types[0] == BYTE4_SIGNED_INT &&
            val_types[1] == BYTE4_FLOAT) {
          if (graphs == null) { graphs = new ArrayList(); }
          //          System.out.println("reading graph data: " + k);
          int xcoords[] = new int[total_points];
          float ycoords[] = new float[total_points];
	  float prev_max_xcoord = -1;
	  boolean sort_reported = false;
          for (int i= 0; i<total_points; i++) {
            //            xcoords[i] = (double)dis.readInt();
            //            ycoords[i] = (double)dis.readFloat();
            int col0 = dis.readInt();
            float col1 = dis.readFloat();
	    if (col0 < prev_max_xcoord && (! sort_reported)) {
	      if (DEBUG_READ) { System.out.println("WARNING!! not sorted by ascending xcoord"); }
	      sort_reported = true;
	    }
	    prev_max_xcoord = col0;
            xcoords[i] = col0;
            ycoords[i] = col1;
            if ((DEBUG_DATA) && (i<100)) {
              System.out.println("Data[" + i + "]:\t" + col0 + "\t" + col1);
            }
          }
          GraphSym graf = new GraphSym(xcoords, ycoords, graph_name, seq);
	  //          graf.setProperties(new HashMap(file_tagvals));
	  copyProps(graf, file_tagvals);
	  if (bar2)  { copyProps(graf, seq_tagvals); }
	  //	  graf.setProperty("method", graph_name);
          //          System.out.println("done reading graph data: " + graf);
          graphs.add(graf);
        }
        else {
          System.err.println("currently, first val must be int4, second must be float4");
        }
      }
      else if (vals_per_point == 3) {
        // System.err.println("PARSING FOR BAR FILES WITH 3 VALUES PER POINT NOT YET IMPLEMENTED");
        if (val_types[0] == BYTE4_SIGNED_INT &&
            val_types[1] == BYTE4_FLOAT &&
            val_types[2] == BYTE4_FLOAT) {
          if (graphs == null) { graphs = new ArrayList(); }
          if (DEBUG_READ)  { System.out.println("reading graph data: " + k); }
          int xcoords[] = new int[total_points];
          float ycoords[] = new float[total_points];
          float zcoords[] = new float[total_points];
          for (int i = 0; i<total_points; i++) {
            //            xcoords[i] = (double)dis.readInt();
            //            ycoords[i] = (double)dis.readFloat();
            int col0 = dis.readInt();
            float col1 = dis.readFloat();
            float col2 = dis.readFloat();
            xcoords[i] = col0;
            ycoords[i] = col1;
            zcoords[i] = col2;
            if (DEBUG_DATA && i < 100) {
              System.out.println("Data[" + i + "]:\t" + col0 + "\t" + col1 + "\t" + col2); }
          }
          String pm_name = graph_name + " : pm";
          String mm_name = graph_name + " : mm";
          GraphSym pm_graf =
            new GraphSym(xcoords, ycoords, graph_name + " : pm", seq);
          GraphSym mm_graf =
            new GraphSym(xcoords, zcoords, graph_name + " : mm", seq);
	  //          mm_graf.setProperties(new HashMap(file_tagvals));
	  //          pm_graf.setProperties(new HashMap(file_tagvals));
	  copyProps(pm_graf, file_tagvals);
	  copyProps(mm_graf, file_tagvals);
          pm_graf.setGraphName(pm_name);
          mm_graf.setGraphName(mm_name);
          //pm_graf.setProperty("graph_name", pm_name);
          //mm_graf.setProperty("graph_name", mm_name);
	  if (bar2)  {
	    copyProps(pm_graf, seq_tagvals);
	    copyProps(mm_graf, seq_tagvals);
	  }
          System.out.println("done reading graph data: ");
          System.out.println("pmgraf, yval = column1: " + pm_graf);
          System.out.println("mmgraf, yval = column2: " + mm_graf);
	  pm_graf.setProperty("probetype", "PM (perfect match)");
	  mm_graf.setProperty("probetype", "MM (mismatch)");
          graphs.add(pm_graf);
          graphs.add(mm_graf);
        }
        else {
          System.err.println("currently, first val must be int4, second must be float4");
        }
      }
    }
    System.out.println("total data points in bar file: " + total_total_points);

    return graphs;
  }
  */

  /*
  public static GraphSym readSbarFormat(InputStream istr, BioSeq aseq) throws IOException {
    BufferedInputStream bis = new BufferedInputStream(istr);
    DataInputStream dis = new DataInputStream(bis);
    // READING HEADER
    //    dis.readBytes("barr\r\n\032\n");  // char  "barr\r\n\032\n"
    byte[] headbytes = new byte[8];
    dis.readFully(headbytes);
    String headstr = new String(headbytes);

    float version = dis.readFloat();       // int  #rows in data section (nrow)
    int total_points = dis.readInt(); // int  #columns in data section (ncol)
    int vals_per_point = dis.readInt(); // int  #columns in data section (ncol)
    int[] val_types = new int[vals_per_point];
    for (int i=0; i<vals_per_point; i++) {
      val_types[i] = dis.readInt();
    }
    if (vals_per_point < 2 ||
        val_types[0] != BYTE4_SIGNED_INT ||
        val_types[1] != BYTE4_FLOAT) {
      System.err.println("Can't parse: GraphSymUtils readSbarFormat() currently requires that data " +
                         " be in format 4BYTE_SIGNED_INT 4BYTE_FLOAT");
      return null;
    }

    int tvcount = dis.readInt();
    if (DEBUG_READ) {
      System.out.println("header top string: " + headstr);
      System.out.println("version: " + version);
      System.out.println("total points: " + total_points);
      System.out.println("vals_per_point: " + vals_per_point);
      System.out.println("tag-value count: " + tvcount);

    }
    HashMap tagvals = readTagValPairs(dis, tvcount);

    int[] xcoords = new int[total_points];
    float[] ycoords = new float[total_points];

    if (val_types[0] == BYTE4_SIGNED_INT &&
        val_types[1] == BYTE4_FLOAT) {
      for (int i= 0; i<total_points; i++) {
        xcoords[i] = dis.readInt();
        ycoords[i] = dis.readFloat();
        //        if (DEBUG_READ && i<10) {
        //          System.out.println("pos = " + xcoords[i] + ", score = " + ycoords[i]);
        //        }
      }
    }

    else {
      for (int i= 0; i<total_points; i++) {
        if (val_types[0] == BYTE4_SIGNED_INT) { xcoords[i] = dis.readInt(); }
        else if (val_types[0] == BYTE4_FLOAT) { xcoords[i] = (int)dis.readFloat(); }
        else if (val_types[0] == BYTE8_FLOAT) { xcoords[i] = (int)dis.readDouble(); }
        else { System.err.println(" x format unrecognized!"); }
        if (val_types[1] == BYTE4_SIGNED_INT) { ycoords[i] = (float)dis.readInt(); }
        else if (val_types[1] == BYTE4_FLOAT) { ycoords[i] = dis.readFloat(); }
        else if (val_types[1] == BYTE8_FLOAT) { ycoords[i] = (float)dis.readDouble(); }
        else if (val_types[1] == BYTE1_SIGNED_INT) { ycoords[i] = (float)dis.readByte(); }
        else if (val_types[1] == BYTE1_UNSIGNED_INT) { ycoords[i] = (float)dis.readUnsignedByte(); }
        else if (val_types[1] == BYTE2_SIGNED_INT) { ycoords[i] = (float)dis.readShort(); }
        else if (val_types[1] == BYTE2_UNSIGNED_INT) { ycoords[i] = (float)dis.readUnsignedShort(); }
        else if (val_types[1] == BYTE4_SIGNED_INT) { ycoords[i] = (float)dis.readInt(); }
        else if (val_types[1] == BYTE4_UNSIGNED_INT) { ycoords[i] = (float)dis.readInt(); }
        else { System.err.println(" x format unrecognized!"); }
        if (DEBUG_READ && i<10) {
          System.out.println("pos = " + xcoords[i] + ", score = " + ycoords[i]);
        }
      }
    }
    GraphSym graf = new GraphSym(xcoords, ycoords, (String)tagvals.get("graph_name"), aseq);
    graf.setProperties(tagvals);
    dis.close();
    return graf;
  }

  public static void copyProps(GraphSym graf, Map tagvals) {
    if (tagvals == null) { return; }
    Iterator iter = tagvals.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry tagval = (Map.Entry)iter.next();
      String tag = (String)tagval.getKey();
      String val = (String)tagval.getValue();
      graf.setProperty(tag, val);
    }
  }
  */
