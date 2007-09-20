/**
*   Copyright (c) 2007 Affymetrix, Inc.
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
package com.affymetrix.genometryImpl.parsers;

import java.io.*;
import java.util.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.SimpleMutableSeqSpan;
import com.affymetrix.genometry.util.SeqUtils;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.SmartAnnotBioSeq;
import com.affymetrix.genometryImpl.SingletonGenometryModel;
import com.affymetrix.genometryImpl.EfficientProbesetSymA;
import com.affymetrix.genometryImpl.SharedProbesetInfo;
import com.affymetrix.genometryImpl.SymWithProps;
import com.affymetrix.genometryImpl.SimpleSymWithProps;
import com.affymetrix.genometryImpl.SingletonSymWithIntId;
import com.affymetrix.genometryImpl.util.Memer;
import com.affymetrix.genometry.span.SimpleSeqSpan;

/**
 *
 *  A highly optimized binary format for probesets that meet certain criteria, originally intended for
 *      use with all-exon arrays.
 *<pre>
 *  Preserving just the probe locations, and the grouping hierarchy:
 *       transcript_cluster
 *         exon_cluster
 *           PSR
 *             probeset
 *               probe
 *  and IDs of all levels above probe
 *  Making some assumptions that I'm pretty sure the exon probes meet:
 *    a) all probes are same length
 *    b) all probes align to a contiguous genome interval (no split probes)
 *    c) probeset, psr, exon_cluster, and transcript_cluster ids (minus a prefix/postfix) can be represented numerically
 *    d) all probes within a transcript_cluster are on same strand??
 *
 *  For probeset and below, this "microformat" averages out to about 5.3 bytes/probe for
 *    (> 10x compression relative to the already stripped down gff).
 *    PSR, exon_cluster, transcript_cluster add to memory requirements, still need to look at this)
 *
 *  -------------------------
 *  Format
 *  Header:
 *     Format (UTF-8)  "ead"
 *     Format version (int)
 *     Genome name (UTF-8)  [ need to deal with case where name and version are combined into one string?]
 *     Genome version (UTF-8)
 *     Annotation type (UTF-8)  -- need way of deciding whether to use this or extract from file name...
 *     Probe length (int)
 *     id_prefix (UTF-8)
 *     Number of tag-val properties (int)
 *     for each tag-val
 *        tag (UTF-8)
 *        value (UTF-8)
 *     Number of seqs (int)
 *     for each seq
 *        seq name (UTF-8)
 *        seq length (int)
 *        number of transcript clusters for seq (int)
 *         for each transcript cluster
 *             id (int)
 *             start
 *             end  (strand derived: (start <= end ? forward : reverse) )
 *             number of exon clusters (int)
 *             for each exon cluster
 *                 id (int)
 *                 start
 *                 end
 *                 number of PSRs (int)
 *                 for each PSR
 *                     id (int)
 *                     start
 *                     end
 *                     number of probesets (int)
 *                     for each probeset
 *                         id (int)
 *                         //  number of probes & strand (byte, 0 to 127 probes, sign indicates strand)
 *                         number of probes (byte) // don't need strand info here??
 *                         for each probe
 *                             min genome position (int, zero interbase)
 *
 *   Transcript cluster, exon cluster, (and intron cluster?) and psr are all modelled as SingletonSymWithIntId syms
 *   Probesets (and probe children) are modelled as EfficientProbesetSymA
 *
 *
 *    // Currently using a single id_prefix in header
 *    // for more flexibility in ID prefix/suffix, may eventually move to using
 *    //   the arbitrary tag-value properties to specify, with special-casing for:
 *           "transcript_cluster_prefix"
 *           "exon_cluster_prefix"
 *           "psr_prefix"
 *           "probeset_prefix"
 *           "transcript_cluster_suffix"
 *           "exon_cluster_suffix"
 *           "psr_suffix"
 *           "probeset_suffix"
 *    // if tag(s) not present, then not needed for full id construction
 *

 *</pre>
 */
public class ExonArrayDesignParser implements AnnotationWriter {
  static boolean USE_FULL_HIERARCHY = false;

  static boolean DEBUG = false;
  static List<String> pref_list = Arrays.asList("ead");

  public List<SeqSymmetry> parse(InputStream istr, AnnotatedSeqGroup group,
    boolean annotate_seq, String default_type) throws IOException {
    BufferedInputStream bis;
    Map<String,Object> tagvals = new LinkedHashMap<String,Object>();
    DataInputStream dis = null;
    List<SeqSymmetry> results = new ArrayList<SeqSymmetry>();
    try  {
      if (istr instanceof BufferedInputStream) {
        bis = (BufferedInputStream) istr;
      }
      else {
        bis = new BufferedInputStream(istr);
      }
      dis = new DataInputStream(bis);
      String format = dis.readUTF();
      int format_version = dis.readInt();
      String seq_group_name = dis.readUTF(); // genome name
      String seq_group_version = dis.readUTF(); // genome version
      // combining genome and version to get seq group id
      String seq_group_id = seq_group_name + seq_group_version;
      if (seq_group_id == null) {
        System.err.println("ead file does not specify a genome name or version, these are required!");
        return null;
      }
      if (! group.isSynonymous(seq_group_id)) {
        System.err.println("In ExonArrayDesignParser, mismatch between AnnotatedSeqGroup argument: " + group.getID() +
                           " and group name+version in ead file: " + seq_group_id);
        return null;
      }
      String specified_type = dis.readUTF();
      String annot_type;
      if ( (specified_type == null) || (specified_type.length() <= 0)) {
        annot_type = default_type;
      }
      else {
        annot_type = specified_type;
      }
      int probe_length = dis.readInt();
      String id_prefix = dis.readUTF();
      int tagval_count = dis.readInt();
      for (int i = 0; i < tagval_count; i++) {
        String tag = dis.readUTF();
        String val = dis.readUTF();
        tagvals.put(tag, val);
      }
      tagvals.put("method", annot_type);
      //      tagvals.put(("preferred_formats", pref_list);

      int seq_count = dis.readInt();
      if (DEBUG) {
        System.out.println("format: " + format + ", format_version: " + format_version);
        System.out.println("seq_group_name: " + seq_group_name + ", seq_group_version: " + seq_group_version);
        System.out.println("type: " + specified_type);
        System.out.println("probe_length: " + probe_length);
        System.out.println("id_prefix: " + id_prefix);
        System.out.println("seq_count: " + seq_count);
      }
      int total_tcluster_count = 0;
      int total_ecluster_count = 0;
      int total_psr_count = 0;
      int total_probeset_count = 0;
      int total_probe_count = 0;


      for (int seqindex = 0; seqindex < seq_count; seqindex++) {
        String seqid = dis.readUTF();
        int seq_length = dis.readInt();
        int transcript_cluster_count = dis.readInt();
        if (DEBUG)  {
          System.out.println("seq: " + seqid + ", transcript_cluster_count: " + transcript_cluster_count);
        }
        total_tcluster_count += transcript_cluster_count;

        MutableAnnotatedBioSeq aseq = group.getSeq(seqid);
        if (aseq == null) {
          aseq = group.addSeq(seqid, seq_length);
        }
        SharedProbesetInfo shared_info = new SharedProbesetInfo(aseq, probe_length, id_prefix, tagvals);

        SimpleSymWithProps container_sym = new SimpleSymWithProps(transcript_cluster_count);
        container_sym.addSpan(new SimpleSeqSpan(0, aseq.getLength(), aseq) );
        container_sym.setProperty("method", annot_type);
        container_sym.setProperty("preferred_formats", pref_list);
        container_sym.setProperty(SimpleSymWithProps.CONTAINER_PROP, Boolean.TRUE);


        if (USE_FULL_HIERARCHY) {
          for (int tindex=0; tindex < transcript_cluster_count; tindex++) {
            int tcluster_id = dis.readInt();
            int tstart = dis.readInt();
            int tend = dis.readInt();
            int exon_cluster_count = dis.readInt();
            total_ecluster_count += exon_cluster_count;
            SingletonSymWithIntId tcluster = new SingletonSymWithIntId(tstart, tend, aseq, id_prefix, tcluster_id);
            results.add(tcluster);
            container_sym.addChild(tcluster);
            //          if (DEBUG) {SeqUtils.printSymmetry(tcluster); }
            for (int eindex=0; eindex < exon_cluster_count; eindex++) {
              int ecluster_id = dis.readInt();
              int estart = dis.readInt();
              int eend = dis.readInt();
              int psr_count = dis.readInt();
              total_psr_count += psr_count;
              SingletonSymWithIntId ecluster = new SingletonSymWithIntId(estart, eend, aseq, id_prefix, ecluster_id);
              tcluster.addChild(ecluster);
              //            if (DEBUG) { SeqUtils.printSymmetry(ecluster); }
              for (int psr_index=0; psr_index < psr_count; psr_index++) {
                int psr_id = dis.readInt();
                int psr_start = dis.readInt();
                int psr_end = dis.readInt();
                int probeset_count = dis.readInt();
                total_probeset_count += probeset_count;
                SingletonSymWithIntId psr = new SingletonSymWithIntId(psr_start, psr_end, aseq, id_prefix, psr_id);
                ecluster.addChild(psr);
                //              if (DEBUG) { SeqUtils.printSymmetry(psr); }
                for (int probeset_index=0; probeset_index < probeset_count; probeset_index++) {
                  int nid = dis.readInt();
                  int b = (int) dis.readByte();
                  int probe_count = Math.abs(b);
                  total_probe_count += probe_count;
                  boolean forward = (b >= 0);
                  if (probe_count == 0) {
                    // EfficientProbesetSymA does not allow probe sets with 0 probes
                    throw new IOException("Probe_count is zero for '"+ nid+ "'");
                  }
                  int[] cmins = new int[probe_count];
                  for (int pindex = 0; pindex < probe_count; pindex++) {
                    int min = dis.readInt();
                    cmins[pindex] = min;
                  }
                  SeqSymmetry probeset_sym =
                    new EfficientProbesetSymA(shared_info, cmins, forward, nid);
                  psr.addChild(probeset_sym);
                  //                tcluster.addChild(probeset_sym);
                }  // end probeset loop
              }  // end psr loop
            }  // end exon cluster loop
            //          if (DEBUG && tindex < 1) { SeqUtils.printSymmetry(tcluster, "   ", true); }
          }  // end transcript cluster loop
        }
        else {
          for (int tindex=0; tindex < transcript_cluster_count; tindex++) {
            int tcluster_id = dis.readInt();
            int tstart = dis.readInt();
            int tend = dis.readInt();
            int probeset_count = dis.readInt();
            total_probeset_count += probeset_count;
            SingletonSymWithIntId tcluster = new SingletonSymWithIntId(tstart, tend, aseq, id_prefix, tcluster_id);
            results.add(tcluster);
            container_sym.addChild(tcluster);
            for (int probeset_index=0; probeset_index < probeset_count; probeset_index++) {
              int nid = dis.readInt();
              int b = (int) dis.readByte();
              int probe_count = Math.abs(b);
              total_probe_count += probe_count;
              boolean forward = (b >= 0);
              if (probe_count == 0) {
                // EfficientProbesetSymA does not allow probe sets with 0 probes
                throw new IOException("Probe_count is zero for '"+ nid+ "'");
              }
              int[] cmins = new int[probe_count];
              for (int pindex = 0; pindex < probe_count; pindex++) {
                int min = dis.readInt();
                cmins[pindex] = min;
              }
              SeqSymmetry probeset_sym =
                new EfficientProbesetSymA(shared_info, cmins, forward, nid);
              tcluster.addChild(probeset_sym);
            }
          }
        }

        if (annotate_seq && transcript_cluster_count > 0) {
          aseq.addAnnotation(container_sym);
        }
      }  // end seq loop


      System.out.println("transcript_cluster count: " + total_tcluster_count);
      if (DEBUG) {
        System.out.println("exon_cluster count: " + total_ecluster_count);
        System.out.println("psr count: " + total_psr_count);
      }
      System.out.println("probeset count: " + total_probeset_count);
      System.out.println("probe count: " + total_probe_count);
      System.out.println("finished parsing exon array design file");
      dis.close();
    }
    finally {
      if (dis != null) {try { dis.close(); } catch (Exception e) {}}
    }
    return results;
  }

  /**
   *  Implememts AnnotationWriter interface
   *  Assumes rigid structure for annotations:
   *  Standard top-level setup: TypeContainerSym with type = <annot_type> annotating each seq in group
   *  Within type container sym:
   *    Level 0: Transcript-cluster annots (SingletonSymWithIntId objects)
   *    Level 1: Exon-cluster (and intron-cluster?) annots (SingletonSymWithIntId objects)
   *    Level 2: PSR annots (SingletonSymWithIntId objects)
   *    Level 3: probeset annots (EfficieentProbesetSymA)
   *    Level 4: probes (virtual, encoded in EfficientProbesetSymA parent)
   */
  public boolean writeAnnotations(java.util.Collection<SeqSymmetry> syms, BioSeq aseq,
                                  String type, OutputStream outstream) throws IOException {
    boolean success = false;
    DataOutputStream dos = null;
    try {
      if (outstream instanceof DataOutputStream) { dos = (DataOutputStream)outstream; }
      else if (outstream instanceof BufferedOutputStream) {
          dos = new DataOutputStream(outstream);
      }
      // Changed to not wrap with a buffered output stream -- this must be handled in the calling code
      // Wrapping with a buffered output stream was causing EOFExceptions and socket errors in
      //     Genometry DAS/2 servlet
      //     (when running in Jetty -- possibly conflicts with Jetty's donwstream buffering of HTTP responses?)
      else { dos = new DataOutputStream(outstream); }
      List<BioSeq> oneseq = new ArrayList<BioSeq>();
      oneseq.add(aseq);
      SeqSymmetry tcluster_exemplar = null;

      if (syms.size() > 0)  { tcluster_exemplar = syms.iterator().next(); }
      writeEadHeader(tcluster_exemplar, type, oneseq, dos);
      writeSeqWithAnnots(syms, aseq, dos);
      dos.flush();
      success = true;
    }
    catch (Exception ex) { 
      IOException ioe = new IOException(ex.getLocalizedMessage());
      ioe.initCause(ex);
      throw ioe;
    }
    return success;
  }


  /**
   *  For writing out all annotations of a particular type for a whole genome in .ead format
   *  Assumes rigid structure for annotations:
   *  Standard top-level setup: TypeContainerSym with type = <annot_type> annotating each seq in group
   *  Within type container sym:
   *    Level 0: Transcript-cluster annots (SingletonSymWithIntId objects)
   *    Level 1: Exon-cluster (and intron-cluster?) annots (SingletonSymWithIntId objects)
   *    Level 2: PSR annots (SingletonSymWithIntId objects)
   *    Level 3: probeset annots (EfficieentProbesetSymA)
   *    Level 4: probes (virtual, encoded in EfficientProbesetSymA parent)
   *
   */
  public boolean writeAnnotations(String annot_type, AnnotatedSeqGroup group, OutputStream outstream)
  throws IOException {
    boolean success = false;
    try {
      DataOutputStream dos;
      if (outstream instanceof DataOutputStream) { dos = (DataOutputStream)outstream; }
      else if (outstream instanceof BufferedOutputStream) { dos = new DataOutputStream(outstream); }
      //    else { dos = new DataOutputStream(outstream); }
      else { dos = new DataOutputStream(new BufferedOutputStream(outstream)); }

      int scount = group.getSeqCount();
      List seqs = group.getSeqList();

      SeqSymmetry tcluster_exemplar = null;
      if (seqs.size() > 0) {
        SmartAnnotBioSeq aseq = (SmartAnnotBioSeq)group.getSeq(0);
        SymWithProps typesym = aseq.getAnnotation(annot_type);
        SeqSymmetry container = typesym.getChild(0);
        tcluster_exemplar = container.getChild(0);
      }

      writeEadHeader(tcluster_exemplar, annot_type, seqs, dos);

      for (int i=0; i<scount; i++) {
        SmartAnnotBioSeq aseq = (SmartAnnotBioSeq)group.getSeq(i);
        SymWithProps typesym = aseq.getAnnotation(annot_type);
        // transcript clusters should be third level down in hierarchy:
        //    1) type container
        //    2) intermediate container
        //    3) transcript cluster
        List<SeqSymmetry> syms = new ArrayList<SeqSymmetry>();
        int container_count = typesym.getChildCount();
        for (int k=0; k<container_count; k++) {
          SeqSymmetry csym = typesym.getChild(k);
          int tcount = csym.getChildCount();
          for (int m=0; m<tcount; m++) {
            SeqSymmetry tcluster = csym.getChild(m);
            syms.add(tcluster);
          }
        }
        // collect all transcript cluster syms
        writeSeqWithAnnots(syms, aseq, dos);
      }
      dos.flush();
      success = true;
    }
    catch (Exception ex) {
      IOException ioe = new IOException(ex.getLocalizedMessage());
      ioe.initCause(ex);
      throw ioe;
    }
    return success;
  }


  /**
   *  assumes seqs are SmartAnnotBioSeqs, and belong to same AnnotatedSeqGroup
   */
  protected void writeEadHeader(SeqSymmetry tcluster_exemplar, String annot_type, List seqs, DataOutputStream dos) throws IOException {
    // extract example EfficientProbesetSymA from an annotated seq in group
    SmartAnnotBioSeq seq0 = (SmartAnnotBioSeq)seqs.get(0);
    AnnotatedSeqGroup group = seq0.getSeqGroup();
    /*
    String transcript_cluster_prefix = (String)typesym.getProperty("transcript_cluster_prefix");
    String transcript_cluster_suffix = (String)typesym.getProperty("transcript_cluster_suffix");
    String exon_cluster_prefix = (String)typesym.getProperty("exon_cluster_prefix");
    String exon_cluster_suffix = (String)typesym.getProperty("exon_cluster_suffix");
    String psr_prefix = (String)typesym.getProperty("psr_prefix");
    String psr_suffix = (String)typesym.getProperty("psr_suffix");
    String probeset_prefix = (String)typesym.getProperty("probeset_prefix");
    String probeset_suffix = (String)typesym.getProperty("probeset_suffix");
    String probe_prefix = (String)typesym.getProperty("probe_prefix");
    String probe_suffix = (String)typesym.getProperty("probe_suffix");
    */

    //    SeqSymmetry transcript_cluster = typesym.getChild(0);
    EfficientProbesetSymA probeset_exemplar;
    if (USE_FULL_HIERARCHY) {
      SeqSymmetry exon_cluster = tcluster_exemplar.getChild(0);
      SeqSymmetry psr = exon_cluster.getChild(0);
      probeset_exemplar = (EfficientProbesetSymA)psr.getChild(0);
    }
    else {
      probeset_exemplar = (EfficientProbesetSymA)tcluster_exemplar.getChild(0);
    }
    int probe_length = probeset_exemplar.getProbeLength();
    String id_prefix = probeset_exemplar.getIDPrefix();

    String groupid = group.getID();
    dos.writeUTF("ead");
    dos.writeInt(1);
    dos.writeUTF(groupid);
    dos.writeUTF("");  // version id blank -- version and group are combined in groupid
    dos.writeUTF(annot_type);
    dos.writeInt(probe_length);
    dos.writeUTF(id_prefix);
    dos.writeInt(0);  // no tagval properties...
    dos.writeInt(seqs.size());
  }


  /**
   *  write out a seq data section
   *  assumes syms in collection contain span on aseq
   */
  protected static void writeSeqWithAnnots(java.util.Collection syms, BioSeq aseq, DataOutputStream dos) throws IOException {
    String seqid = aseq.getID();
    System.out.println("seqid: " + seqid + ", annot count: " + syms.size() );
    dos.writeUTF(seqid);
    dos.writeInt(aseq.getLength());
    dos.writeInt(syms.size());

    Iterator siter = syms.iterator();
    MutableSeqSpan mutspan = new SimpleMutableSeqSpan(0, 0, aseq);
    while (siter.hasNext())  {
      SingletonSymWithIntId psym = (SingletonSymWithIntId)siter.next();
      writeTranscriptCluster(psym, mutspan, dos);
    }
  }

  /**
   *  scratch_span is a MutableSeqSpan used for transient span instantiation
   */
  protected static void writeTranscriptCluster(SingletonSymWithIntId tsym, MutableSeqSpan scratch_span, DataOutputStream dos)
    throws IOException {
    SeqSpan tspan = tsym.getSpan(0);
    MutableSeqSpan mutspan = scratch_span;
    if (mutspan == null) { mutspan = new SimpleMutableSeqSpan(0, 0, tspan.getBioSeq()); }

    if (USE_FULL_HIERARCHY) {
      // when full transcript/exon/PSR/probeset/probe hierarchy is present, commented out for now...
      int exon_cluster_count = tsym.getChildCount();
      dos.writeInt(tsym.getIntID());
      dos.writeInt(tspan.getStart());
      dos.writeInt(tspan.getEnd());
      dos.writeInt(exon_cluster_count);
      // write each exon cluster
      for (int i=0; i<exon_cluster_count; i++) {
        SingletonSymWithIntId esym = (SingletonSymWithIntId)tsym.getChild(i);
        SeqSpan espan = esym.getSpan(0);
        int psr_count = esym.getChildCount();
        dos.writeInt(esym.getIntID());
        dos.writeInt(espan.getStart());
        dos.writeInt(espan.getEnd());
        dos.writeInt(psr_count);
        // write each PSR
        for (int k=0; k<psr_count; k++) {
          SingletonSymWithIntId psym = (SingletonSymWithIntId)esym.getChild(k);
          SeqSpan pspan= psym.getSpan(0);
          int probeset_count = psym.getChildCount();
          dos.writeInt(psym.getIntID());
          dos.writeInt(pspan.getStart());
          dos.writeInt(pspan.getEnd());
          dos.writeInt(probeset_count);
          for (int m=0; m<probeset_count; m++) {
            // write each probeset
            EfficientProbesetSymA probeset_sym = (EfficientProbesetSymA)psym.getChild(m);
            writeProbeset(probeset_sym, mutspan, dos);
          }
        }
      }
    }
    else {
      int probeset_count = tsym.getChildCount();
      dos.writeInt(tsym.getIntID());
      dos.writeInt(tspan.getStart());
      dos.writeInt(tspan.getEnd());
      dos.writeInt(probeset_count);
      for (int m=0; m<probeset_count; m++) {
        // write each probeset
        EfficientProbesetSymA probeset_sym = (EfficientProbesetSymA)tsym.getChild(m);
        writeProbeset(probeset_sym, mutspan, dos);
      }
    }

  }


  protected static void writeProbeset(EfficientProbesetSymA psym, MutableSeqSpan mutspan, DataOutputStream dos) throws IOException {
    SeqSpan pspan = psym.getSpan(0);
    int child_count = psym.getChildCount();
    int intid = psym.getIntID();
    // BioSeq aseq = pspan.getBioSeq();
    dos.writeInt(intid);  // probeset id representated as an integer
    // sign of strnad_and_count indicates forward (+) or reverse (-) strand
    byte strand_and_count = (byte)(pspan.isForward() ? child_count : -child_count);
    dos.writeByte(strand_and_count);

    for (int i=0; i<child_count; i++) {
      SeqSpan cspan = psym.getChildSpan(i, pspan.getBioSeq(), mutspan);
      dos.writeInt(cspan.getMin());
    }
  }

  public String getMimeType() { return "binary/ead"; }


  /**
   *  Reads a GFF file and writes a "ead" (binary exon array design) file.
   *<pre>
   *  The input gff file of genome-based probesets must meet these criteria:
   *    a) all probes are same length
   *    b) all probes align to a contiguous genome interval (no split probes)
   *    c) each probeset id can be represented with unique integer root within the set
   *          and a String prefix shared among all probesets in the file
   *    d) all probes within a probeset are on same strand
   *    e) less than 128 probes per probeset
   *          (but can be different number of probes in each probeset)
   *
   *  writes as output a file in ead format
   *  first arg is gff input file name
   *  second arg is ead output file name
   *  third arg is genomeid
   *  fourth arg is annot type name
   *  fifth arg is optional, and is genome versionid
   *  if no second arg, output is written to standard out??
   *</pre>
   */
  public static void main(String[] args) throws IOException {
    SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
    boolean WRITE = false;
    boolean READ = true;
    Memer mem = new Memer();
    String default_in_file = "c:/data/chp_data_exon/HuEx-1_0-st-v2.design-annot-hg18/gff";
    //    String default_in_file = "c:/data/chp_data_exon/HuEx-1_0-st-v2.design-annot-hg18/gff_test";
    //    String default_in_file = "c:/data/chp_data_exon/HuEx-1_0-st-v2.design-annot-hg18/gff";
    String default_out_file = "c:/data/chp_data_exon/HuEx-1_0-st-v2.design-annot-hg18/ead/HuEx-1_0-st-v2_3level.ead";
    //    String default_out_file = "c:/data/chp_data_exon/HuEx-1_0-st-v2.design-annot-hg18/ead/test_3level.ead";
    String default_genome_id = "H_sapiens_Mar_2006";
    String default_id_prefix = "HuEx-1_0-st-v2:";
    String default_annot_type = "HuEx-1_0-st-v2";
    //    String in_file = "";
    String in_file = default_in_file;
    String out_file = default_out_file;
    String id_prefix = default_id_prefix;
    String genomeid= default_genome_id;
    String versionid = "";
    String annot_type = default_annot_type;

    if (args.length == 5 || args.length == 6) {
      in_file = args[0];
      out_file = args[1];
      id_prefix = args[2];
      annot_type = args[3];
      genomeid = args[4];
      if (args.length == 6) { versionid = args[5]; }
    }
    /*
    else {
      System.out.println("Usage:  java ... ExonArrayDesignParser <GFF infile> <EAD outfile> <id_prefix> <annot type> <genomeid> [<version>]");
      System.out.println("Example:  java ... ExonArrayDesignParser foo.gff foo.ead 'HuEx-1_0-st-v2:' HuEx-1_0-st-v2 H_sapiens_Mar_2006");
      System.exit(1);
    }
    */

    if (WRITE) {
      System.out.println("Creating a '.ead' format file: ");
      System.out.println("Input '"+in_file+"'");
      System.out.println("Output '"+out_file+"'");
      ExonArrayDesignParser parser = new ExonArrayDesignParser();
      parser.convertGff(in_file, out_file, genomeid, versionid, annot_type, id_prefix);
      System.out.println("DONE!  Finished converting GFF file to EAD file.");
      System.out.println("");
    }
    if (READ) {
      try  {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(out_file)));
        AnnotatedSeqGroup group = gmodel.addSeqGroup(genomeid + versionid);
        ExonArrayDesignParser parser = new ExonArrayDesignParser();
        List results = parser.parse(bis, group, true, annot_type);
        mem.printMemory();
        System.gc();
        Thread.sleep(3000);
        mem.printMemory();
        Thread.sleep(3000);
        System.out.println("Finished reading ead file, transcript_clusters: " + results.size());

      }
      catch (Exception ex)  {
        ex.printStackTrace();
      }
    }

  }

  /**
   *  Converts a "GFF" file (or directory of GFF files) into a "ead" file.
   *  Assumes
   *     All annotations in GFF file are genome-based probes (contiguous intervals on genome);
   *     25-mer probes (for now)
   */
  public void convertGff(String in_file, String out_file, String genome_id,
                         String version_id, String annot_type, String id_prefix)  {
    Memer mem = new Memer();
    AnnotatedSeqGroup seq_group = new AnnotatedSeqGroup(genome_id);
    int probe_length = 25;
    try {
      File gff_file = new File(in_file);
      List<File> gfiles = new ArrayList<File>();
      if (gff_file.isDirectory()) {
        System.out.println("processing all gff files in directory: " + in_file);
        // process all gff files in directory
        File[] fils = gff_file.listFiles();
        for (int i=0; i<fils.length; i++)  {
          File fil = fils[i];
          String fname = fil.getName();
          if (fname.endsWith(".gff") || fname.endsWith(".gtf")) {
            gfiles.add(fil);
          }
        }
      }
      else {
        gfiles.add(gff_file);
      }
      int printcount = 0;
      HashMap<SmartAnnotBioSeq,SimpleSymWithProps> seq2container = new HashMap<SmartAnnotBioSeq,SimpleSymWithProps>();
      HashMap<BioSeq,SharedProbesetInfo> seq2info = new HashMap<BioSeq,SharedProbesetInfo>();

      for (int i=0; i<gfiles.size(); i++) {
        File gfile = gfiles.get(i);
        System.out.println("parsing gff file: " + gfile.getPath());

        GFFParser gff_parser = new GFFParser();
        BufferedInputStream bis = new BufferedInputStream( new FileInputStream(gfile));
        List annots = gff_parser.parse(bis, ".", seq_group, false, false);

        mem.printMemory();
        System.out.println("top-level annots: " + annots.size());
        // now convert each annot hierarchy to SingletonSymWithIntId and EfficientProbesetSymA
        // assuming 5-level deep hierarchy:
        // a) transcript-cluster
        //    b) exon-cluster
        //       c) PSR
        //          d) probeset
        //             e) probe
        // can't use just getID() for id because this depends on headers at start of gff that may not
        //    be set correctly (for example in some the tag for probeset ID is "probeset_name" instead of "probeset_id")
        int tcount = annots.size();

        for (int tindex=0; tindex < tcount; tindex++) {
          SymWithProps tcluster = (SymWithProps)annots.get(tindex);
          SeqSpan tspan = tcluster.getSpan(0);
          SmartAnnotBioSeq aseq = (SmartAnnotBioSeq) tspan.getBioSeq();

          SharedProbesetInfo shared_info = seq2info.get(aseq);
          if (shared_info == null) {
            shared_info = new SharedProbesetInfo(aseq, probe_length, id_prefix, null);
            seq2info.put(aseq, shared_info);
          }

          SimpleSymWithProps container = seq2container.get(aseq);
          if (container == null) {
            container = new SimpleSymWithProps();
            container.addSpan(new SimpleSeqSpan(0, aseq.getLength(), aseq));
            container.setProperty("method", annot_type);
            container.setProperty("preferred_formats", pref_list);
            container.setProperty(SimpleSymWithProps.CONTAINER_PROP, Boolean.TRUE);
            seq2container.put(aseq, container);
          }
          String tid = tcluster.getID();
          if (tid == null)  { tid = (String)tcluster.getProperty("transcript_cluster_id"); }
          SingletonSymWithIntId new_tcluster =
            new SingletonSymWithIntId(tspan.getStart(), tspan.getEnd(), aseq, id_prefix, Integer.parseInt(tid));
          container.addChild(new_tcluster);

          int ecount = tcluster.getChildCount();
          for (int eindex=0; eindex < ecount; eindex++) {
            SymWithProps ecluster = (SymWithProps)tcluster.getChild(eindex);
            SeqSpan espan = ecluster.getSpan(0);
            String eid = ecluster.getID();
            if (eid == null)  { eid = (String)ecluster.getProperty("exon_cluster_id"); }
            if (eid == null)  { eid = (String)ecluster.getProperty("intron_cluster_id"); }
            SingletonSymWithIntId new_ecluster =
              new SingletonSymWithIntId(espan.getStart(), espan.getEnd(), aseq, id_prefix, Integer.parseInt(eid));
            if (USE_FULL_HIERARCHY) {
              new_tcluster.addChild(new_ecluster);
            }

            int psrcount = ecluster.getChildCount();
            for (int psrindex=0; psrindex < psrcount; psrindex++) {
              SymWithProps psr = (SymWithProps)ecluster.getChild(psrindex);
              SeqSpan psrspan = psr.getSpan(0);
              String psrid = psr.getID();
              if (psrid == null) { psrid = (String)psr.getProperty("psr_id"); }

              SingletonSymWithIntId new_psr =
                new SingletonSymWithIntId(psrspan.getStart(), psrspan.getEnd(), aseq, id_prefix, Integer.parseInt(psrid));
              if (USE_FULL_HIERARCHY) {
                new_ecluster.addChild(new_psr);
              }

              int probeset_count = psr.getChildCount();
              for (int probeset_index=0; probeset_index < probeset_count; probeset_index++) {
                SymWithProps probeset = (SymWithProps)psr.getChild(probeset_index);
                SeqSpan probeset_span = probeset.getSpan(0);
                String probeset_id = probeset.getID();
                if (probeset_id == null) { probeset_id = (String)probeset.getProperty("probeset_id"); }
                int probeset_nid = Integer.parseInt(probeset_id);
                int probecount = probeset.getChildCount();
                int[] probemins = new int[probecount];
                if (printcount < 1 && tcluster.getChildCount() > 1) {
                  System.out.println("transcript_cluster_id: " + tid);
                  System.out.println("exon_cluster_id: " + eid);
                  System.out.println("psr_id: " + psrid);
                  System.out.println("probeset_id: " + probeset_id);
                  System.out.println("probeset_nid: " + probeset_nid);
                }
                for (int probeindex=0; probeindex < probecount; probeindex++) {
                  SeqSymmetry probe = probeset.getChild(probeindex);
                  probemins[probeindex] = probe.getSpan(0).getMin();
                }

                EfficientProbesetSymA new_probeset =
                  new EfficientProbesetSymA(shared_info, probemins, probeset_span.isForward(), probeset_nid);
                if (USE_FULL_HIERARCHY) {
                  new_psr.addChild(new_probeset);
                }
                else { new_tcluster.addChild(new_probeset); }
              }
            }
          }

          if (printcount < 1 && tcluster.getChildCount() > 1) {
            SeqUtils.printSymmetry(tcluster, "   ", true);   // print symmetry with props
            System.out.println("###########################");
            SeqUtils.printSymmetry(new_tcluster, "   ", true);
            printcount++;
          }
        }  // end transcript_cluster loop
        bis.close();
        System.gc();
      }
      System.gc();

      for (Map.Entry<SmartAnnotBioSeq,SimpleSymWithProps> ent : seq2container.entrySet()) {
        SmartAnnotBioSeq aseq = ent.getKey();
        SeqSymmetry container = ent.getValue();
        aseq.addAnnotation(container, annot_type);
      }
      mem.printMemory();

      FileOutputStream fos = new FileOutputStream(new File(out_file));
      writeAnnotations(annot_type, seq_group, fos);
    }
    catch (Exception ex) { ex.printStackTrace(); }

  }


}
