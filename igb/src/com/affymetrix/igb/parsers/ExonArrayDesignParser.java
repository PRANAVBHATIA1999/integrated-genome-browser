/**
*   Copyright (c) 2005-2006 Affymetrix, Inc.
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
package com.affymetrix.igb.parsers;

import java.io.*;
import java.util.*;

import com.affymetrix.genometry.*;
import com.affymetrix.genometry.span.SimpleMutableSeqSpan;
import com.affymetrix.igb.genometry.AnnotatedSeqGroup;
import com.affymetrix.igb.genometry.SmartAnnotBioSeq;
import com.affymetrix.igb.genometry.SingletonGenometryModel;
import com.affymetrix.igb.genometry.EfficientProbesetSymA;
import com.affymetrix.igb.genometry.SymWithProps;
import com.affymetrix.igb.genometry.SimpleSymWithProps;
import com.affymetrix.igb.genometry.SingletonSymWithIntId;
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
 *     transcript cluster ID prefix (UTF-8) -- combined with probeset id int, get full id
 *     transcript cluster ID postfix (UTF-8)  -- usually null
 *     exon cluster ID prefix (UTF-8) -- combined with probeset id int, get full id
 *     exon cluster ID postfix (UTF-8)  -- usually null
 *     probeset ID prefix (UTF-8) -- combined with probeset id int, get full id
 *     probeset ID postfix (UTF-8)  -- usually null
 *     PSR ID prefix (UTF-8) -- combined with PSR id int, get full id
 *     PSR ID postfix (UTF-8)  -- usually null
 *     Number of tag-val properties (int)
 *     for each tag-val
 *        tag (UTF-8)
 *        value (UTF-8)
 *     Number of seqs (int)
 *     for each seq
 *        seq name (UTF-8)
 *        seq length (int)
 *        // number of probesets for seq
 *        number of transcript clusters for seq
 *         for each transcript cluster
 *             id (int)
 *             start
 *             end
 *             strand? (byte)
 *             number of exon clusters
 *             for each exon cluster
 *                 id (int)
 *                 start
 *                 end
 *                 number of probesets
 *                 for each PSR
 *                     id (int)
 *                     start
 *                     end
 *                     number of probesets
 *                     for each probeset
 *                         id (int)
 *                         //  number of probes & strand (byte, 0 to 127 probes, sign indicates strand)
 *                         number of probes (byte) // don't need strand info here??
 *                         for each probe
 *                             min genome position (int, zero interbase)
 *
 *   Transcript cluster, exon cluster, (and intron cluster?) and psr are all modelled as SingletonSymWithIntId syms
 *   Probesets (and probe children) are modelled as EfficientProbesetSymA
 *</pre>
 */
public class ExonArrayDesignParser implements AnnotationWriter {
  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
  static boolean DEBUG = true;
  static java.util.List pref_list = new ArrayList();
  static {
    pref_list.add("ead");
  }

  public List parse(InputStream istr, AnnotatedSeqGroup group,
    boolean annotate_seq, String default_type) throws IOException {
    return parse(istr, group, annotate_seq, default_type, false);
  }

  public List parse(InputStream istr, AnnotatedSeqGroup group,
    boolean annotate_seq, String default_type, boolean populate_id_hash) throws IOException {
    System.out.println("in ExonArrayDesignParser, populating id hash: " + populate_id_hash);
    BufferedInputStream bis;
    Map tagvals = new LinkedHashMap();
    Map seq2syms = new LinkedHashMap();
    Map seq2lengths = new LinkedHashMap();
    DataInputStream dis = null;
    String id_prefix = "";
    List results = new ArrayList();
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

      int seq_count = dis.readInt();
      if (DEBUG) {
	System.out.println("format: " + format + ", format_version: " + format_version);
	System.out.println("seq_group_name: " + seq_group_name + ", seq_group_version: " + seq_group_version);
	System.out.println("type: " + specified_type);
	System.out.println("probe_length: " + probe_length);
	System.out.println("id_prefix: " + id_prefix);
	System.out.println("seq_count: " + seq_count);
      }

      for (int i = 0; i < seq_count; i++) {
        String seqid = dis.readUTF();
        int seq_length = dis.readInt();
        int probeset_count = dis.readInt();
        SeqSymmetry[] syms = new SeqSymmetry[probeset_count];
        seq2syms.put(seqid, syms);
	seq2lengths.put(seqid, new Integer(seq_length));
      }
      int tagval_count = dis.readInt();
      for (int i = 0; i < tagval_count; i++) {
        String tag = dis.readUTF();
        String val = dis.readUTF();
        tagvals.put(tag, val);
      }
      Iterator seqiter = seq2syms.keySet().iterator();
      while (seqiter.hasNext()) {
        String seqid = (String) seqiter.next();
        SeqSymmetry[] syms = (SeqSymmetry[]) seq2syms.get(seqid);
        int probeset_count = syms.length;
	System.out.println("seq: " + seqid + ", probeset count: " + probeset_count);

	MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq)group.getSeq(seqid);
        if (aseq == null) {
	  int seqlength = ((Integer)seq2lengths.get(seqid)).intValue();
	  aseq = group.addSeq(seqid, seqlength);
	}
	SimpleSymWithProps container_sym = new SimpleSymWithProps(probeset_count);
	container_sym.addSpan(new SimpleSeqSpan(0, aseq.getLength(), aseq) );
	container_sym.setProperty("method", annot_type);
	container_sym.setProperty("preferred_formats", pref_list);

        for (int i = 0; i < probeset_count; i++) {
          int nid = dis.readInt();
          int b = (int) dis.readByte();
          int probe_count = Math.abs(b);
          boolean forward = (b >= 0);
          if (probe_count == 0) {
            // EfficientProbesetSymA does not allow probe sets with 0 probes
            throw new IOException("Probe_count is zero for '"+ nid+ "'");
          }
	  int[] cmins = new int[probe_count];
          for (int k = 0; k < probe_count; k++) {
            int min = dis.readInt();
	    cmins[k] = min;
          }
	  SymWithProps psr_sym = null;
	  SeqSymmetry psym = new EfficientProbesetSymA(psr_sym, cmins, probe_length, forward, id_prefix, nid, aseq);
	  syms[i]  = psym;
	  container_sym.addChild(psym);
	  results.add(psym);
	  if (populate_id_hash) {
	    group.addToIndex(psym.getID(), psym);
	  }
        }
	if (annotate_seq) {
	  aseq.addAnnotation(container_sym);
	}
      }
      System.out.println("finished parsing probeset file");
    }

    finally {
      if (dis != null) try { dis.close(); } catch (Exception e) {}
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
  public boolean writeAnnotations(java.util.Collection syms, BioSeq aseq,
				  String type, OutputStream outstream) {
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
      List oneseq = new ArrayList();
      oneseq.add(aseq);
      writeEadHeader(type, oneseq, dos);
      writeSeqWithAnnots(syms, aseq, dos);
      dos.flush();
      success = true;
    }
    catch (Exception ex) { ex.printStackTrace(); }
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
  public boolean writeAnnotations(String annot_type, AnnotatedSeqGroup group, OutputStream outstream)  throws IOException {
    boolean success = false;
    DataOutputStream dos;
    if (outstream instanceof DataOutputStream) { dos = (DataOutputStream)outstream; }
    else if (outstream instanceof BufferedOutputStream) { dos = new DataOutputStream(outstream); }
    //    else { dos = new DataOutputStream(outstream); }
    else { dos = new DataOutputStream(new BufferedOutputStream(outstream)); }

    int scount = group.getSeqCount();
    List seqs = group.getSeqList();
    writeEadHeader(annot_type, seqs, dos);

    for (int i=0; i<scount; i++) {
      SmartAnnotBioSeq aseq = (SmartAnnotBioSeq)group.getSeq(i);
      SymWithProps typesym = aseq.getAnnotation(annot_type);
      List syms = new ArrayList();
      // collect all transcript cluster syms
      writeSeqWithAnnots(syms, aseq, dos);
    }
    success = true;
    return success;
  }


  /**
   *  assumes seqs are SmartAnnotBioSeqs, and belong to same AnnotatedSeqGroup
   */
  protected void writeEadHeader(String annot_type, List seqs, DataOutputStream dos) throws IOException {
    // extract example EfficientProbesetSymA from an annotated seq in group
    SmartAnnotBioSeq seq0 = (SmartAnnotBioSeq)seqs.get(0);

    AnnotatedSeqGroup group = seq0.getSeqGroup();
    SymWithProps typesym = seq0.getAnnotation(annot_type);
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

    SeqSymmetry transcript_cluster = typesym.getChild(0);
    SeqSymmetry exon_cluster = transcript_cluster.getChild(0);
    SeqSymmetry psr = exon_cluster.getChild(0);
    EfficientProbesetSymA probeset_exemplar = (EfficientProbesetSymA)psr.getChild(0);
    int probe_length = probeset_exemplar.getProbeLength();
    String id_prefix = probeset_exemplar.getPrefixID();

    String groupid = group.getID();
    dos.writeUTF("ead");
    dos.writeInt(1);
    dos.writeUTF(groupid);
    //      dos.writeUTF(version_id);
    dos.writeUTF("");  // version id blank -- version and group are combined in groupid
    dos.writeUTF(annot_type);
    dos.writeInt(probe_length);
    dos.writeUTF(id_prefix);
    // dos.writeUTF(id_postfix);

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
   *  mutspan is a MutableSeqSpan used for
   */
  protected static void writeTranscriptCluster(SingletonSymWithIntId tsym, MutableSeqSpan scratch_span, DataOutputStream dos)
    throws IOException {
    SeqSpan tspan = tsym.getSpan(0);
    MutableSeqSpan mutspan = scratch_span;
    if (mutspan == null) { mutspan = new SimpleMutableSeqSpan(0, 0, tspan.getBioSeq()); }
    int exon_cluster_count = tsym.getChildCount();
    dos.writeInt(tsym.getIntID());
    dos.writeInt(tspan.getStart());
    dos.writeInt(tspan.getEnd());
    dos.writeByte(exon_cluster_count);
    // write each exon cluster
    for (int i=0; i<exon_cluster_count; i++) {
      SingletonSymWithIntId esym = (SingletonSymWithIntId)tsym.getChild(i);
      SeqSpan espan = esym.getSpan(0);
      int psr_count = esym.getChildCount();
      dos.writeInt(esym.getIntID());
      dos.writeInt(espan.getStart());
      dos.writeInt(espan.getEnd());
      dos.writeByte(psr_count);
      // write each PSR
      for (int k=0; k<psr_count; k++) {
	SingletonSymWithIntId psym = (SingletonSymWithIntId)esym.getChild(k);
	SeqSpan pspan= psym.getSpan(0);
	int probeset_count = psym.getChildCount();
	dos.writeInt(psym.getIntID());
	dos.writeInt(pspan.getStart());
	dos.writeInt(pspan.getEnd());
	dos.writeByte(probeset_count);
	for (int m=0; m<probeset_count; m++) {
	  // write each probeset
	  EfficientProbesetSymA probeset_sym = (EfficientProbesetSymA)psym.getChild(m);
	  writeProbeset(probeset_sym, mutspan, dos);
	}
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
    String in_file = "";
    String out_file = "";
    String id_prefix = "";
    String genomeid= "";
    String versionid = "";
    String annot_type = "";

    if (args.length == 5 || args.length == 6) {
      in_file = args[0];
      out_file = args[1];
      id_prefix = args[2];
      annot_type = args[3];
      genomeid = args[4];
      if (args.length == 6) { versionid = args[5]; }
    } else {
      System.out.println("Usage:  java ... ExonArrayDesignParser <GFF infile> <BP1 outfile> <id_prefix> <annot type> <genomeid> [<version>]");
      System.out.println("Example:  java ... ExonArrayDesignParser foo.gff foo.ead HuEx HuEx-1_0-st-Probes H_sapiens_Jul_2003");
      System.exit(1);
    }


    System.out.println("Creating a '.bp1' format file: ");
    System.out.println("Input '"+in_file+"'");
    System.out.println("Output '"+out_file+"'");
    convertGff(in_file, out_file, genomeid, versionid, annot_type, id_prefix);
    System.out.println("DONE!  Finished converting GFF file to BP1 file.");
    System.out.println("");

    /*
    // After creating the file, parses it (for testing)
    try  {
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(out_file)));
      AnnotatedSeqGroup group = gmodel.addSeqGroup(genomeid + versionid);
      ExonArrayDesignParser parser = new ExonArrayDesignParser();
      parser.parse(bis, group, true, annot_type);
    }
    catch (Exception ex)  {
      ex.printStackTrace();
    }
    */

  }

  /**
   *  Converts a "GFF" file (or directory of GFF files) into a "ead" file.
   *  Assumes
   *     All annotations in GFF file are genome-based probes (contiguous intervals on genome);
   *     25-mer probes (for now)
   */
  public static void convertGff(String gff_file, String output_file, String genome_id,
				String version_id, String annot_type, String id_prefix)
    throws IOException {

    AnnotatedSeqGroup seq_group = new AnnotatedSeqGroup(genome_id);
    int probe_length = 25;
    List annots = null;
    try {
      System.out.println("parsing gff file: " + gff_file);
      GFFParser gff_parser = new GFFParser();
      BufferedInputStream bis = new BufferedInputStream( new FileInputStream( new File( gff_file) ) );
      annots = gff_parser.parse(bis, seq_group, false);
      bis.close();
    }
    catch (Exception ex) { ex.printStackTrace(); }
  }


}
