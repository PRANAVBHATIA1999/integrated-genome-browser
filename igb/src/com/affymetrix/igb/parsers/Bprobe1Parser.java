package com.affymetrix.igb.parsers;

import java.io.*;
import java.util.*;

import com.affymetrix.genometry.*;
import com.affymetrix.igb.genometry.AnnotatedSeqGroup;
import com.affymetrix.igb.genometry.SingletonGenometryModel;
import com.affymetrix.igb.genometry.EfficientProbesetSymA;
import com.affymetrix.igb.genometry.SimpleSymWithProps;
import com.affymetrix.genometry.span.SimpleSeqSpan;

/**
 *
 *  A highly optimized binary format for probesets that meet certain criteria, originally intended for
 *      use with all-exon arrays
 *  Preserving just the probe locations, the grouping of probes into probesets, and
 *  the probeset ids, and making some assumptions that I'm pretty sure the exon probes meet:
 *    a) all probes are same length
 *    b) all probes align to a contiguous genome interval (no split probes)
 *    c) probeset ids can be represented numerically
 *    d) all probes within a probeset are on same strand
 *
 *  This "microformat" averages out to about 5.3 bytes/probe
 *    (> 10x compression relative to the already stripped down gff).
 *  At four million probes that's about 21 MB for all of them.
 *
 *  -------------------------
 *  Format
 *  Header:
 *     Format (UTF-8)  "bprobe1"
 *     Format version (int)
 *     Genome name (UTF-8)  [ need to deal with case where name and version are combined into one string?]
 *     Genome version (UTF-8)
 *     Annotation type (UTF-8)  -- need way of deciding whether to use this or extract from file name...
 *     Probe length (int)
 *     Number of seqs (int)
 *     for each seq
 *        seq name (UTF-8)
 *        seq length (int)
 *        number of probesets for seq
 *     Number of tag-val properties (int)
 *     for each tag-val
 *        tag (UTF-8)
 *        value (UTF-8)
 *     for each seq
 *         for each probeset
 *             id (int)
 *             number of probes & strand (byte, 0 to 127 probes, sign indicates strand)
 *             for each probe
 *                 min genome position (int, zero interbase)
 *
 */
public class Bprobe1Parser {
  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();
  static boolean DEBUG = true;

  public AnnotatedSeqGroup parse(InputStream istr, AnnotatedSeqGroup group, boolean annotate_seq, String default_type) {
    BufferedInputStream bis;
    AnnotatedSeqGroup seqs = null;
    Map tagvals = new LinkedHashMap();
    Map seq2syms = new LinkedHashMap();
    Map seq2lengths = new LinkedHashMap();
    try  {
      if (istr instanceof BufferedInputStream) {
        bis = (BufferedInputStream) istr;
      }
      else {
        bis = new BufferedInputStream(istr);
      }
      DataInputStream dis = new DataInputStream(bis);
      String format = dis.readUTF();
      int format_version = dis.readInt();
      String seq_group_name = dis.readUTF(); // genome name
      String seq_group_version = dis.readUTF(); // genome version
      // combining genome and version to get seq group id
      String seq_group_id = seq_group_name + seq_group_version;
      if (seq_group_id == null) {
	System.err.println("bprobe1 file does not specify a genome name or version, these are required!");
	return null;
      }
      seqs = gmodel.addSeqGroup(seq_group_id);
      if (group != null && group != seqs) {
	System.err.println("In Bprobe1Parser, mismatch between AnnotatedSeqGroup argument: " + group.getID() +
			   " and group name+version in bprobe1 file: " + seqs.getID());
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

	MutableAnnotatedBioSeq aseq = (MutableAnnotatedBioSeq)seqs.getSeq(seqid);
        if (aseq == null) {
	  int seqlength = ((Integer)seq2lengths.get(seqid)).intValue();
	  aseq = seqs.addSeq(seqid, seqlength);
	}
	SimpleSymWithProps container_sym = new SimpleSymWithProps(probeset_count);
	container_sym.addSpan(new SimpleSeqSpan(0, aseq.getLength(), aseq) );
	container_sym.setProperty("method", annot_type);


        for (int i = 0; i < probeset_count; i++) {
          int probeset_id = dis.readInt();
          int b = (int) dis.readByte();
          int probe_count = Math.abs(b);
          boolean forward = (b >= 0);
	  int[] cmins = new int[probe_count];
          for (int k = 0; k < probe_count; k++) {
            int min = dis.readInt();
	    cmins[k] = min;
          }
	  syms[i] = new EfficientProbesetSymA(cmins, probe_length, forward, probeset_id, aseq);
	  container_sym.addChild(syms[i]);
        }
	if (annotate_seq) {
	  aseq.addAnnotation(container_sym);
	}
      }
      System.out.println("finished parsing bp1 file");
      dis.close();
    }

    catch (Exception ex)  {
      ex.printStackTrace();
    }
    return seqs;
  }

  /**
   *  Assumes
   *     All annotations in GFF file are genome-based probes (contiguous intervals on genome)
   *     25-mer probes (for now)
   */
  public static void convertGff(String gff_file, String output_file,
				String seq_group, String seq_group_version, String annot_type) {
    int probe_length = 25;
    Map tagvals = new HashMap();
    tagvals.put("tagval_test_1", "testing1");
    tagvals.put("tagval_test_2", "testing2");
    try {
      System.out.println("parsing gff file: " + gff_file);
      GFFParser gff_parser = new GFFParser();
      BufferedInputStream bis = new BufferedInputStream( new FileInputStream( new File( gff_file) ) );
      Map seqs = new LinkedHashMap();
      List annots = gff_parser.parse(bis, seqs);
      bis.close();
      int total_annot_count = annots.size();
      int seq_count = seqs.size();
      System.out.println("done parsing, seq count = " + seq_count + ", total annot count = " + total_annot_count);

      BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( new File( output_file) ) );
      DataOutputStream dos = new DataOutputStream(bos);

      dos.writeUTF("bp1");
      dos.writeInt(1);
      dos.writeUTF(seq_group);
      dos.writeUTF(seq_group_version);
      dos.writeUTF(annot_type);
      dos.writeInt(probe_length);
      dos.writeInt(seq_count);
      Iterator iter = seqs.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry ent = (Map.Entry)iter.next();
	AnnotatedBioSeq aseq = (AnnotatedBioSeq)ent.getValue();
	String seqid = aseq.getID();
	int seq_length = aseq.getLength();
	int annot_count = aseq.getAnnotationCount();
	System.out.println("seqid: " + seqid + ", annot count: " + annot_count );
	dos.writeUTF(seqid);
	dos.writeInt(seq_length);
	dos.writeInt(annot_count);
      }
      int tagval_count = tagvals.size();
      dos.writeInt(tagval_count);
      Iterator tviter = tagvals.entrySet().iterator();
      while (tviter.hasNext()) {
	Map.Entry ent = (Map.Entry)tviter.next();
	String tag = (String)ent.getKey();
	String val = (String)ent.getValue();
	dos.writeUTF(tag);
	dos.writeUTF(val);
      }
      iter = seqs.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry ent = (Map.Entry)iter.next();
	AnnotatedBioSeq aseq = (AnnotatedBioSeq)ent.getValue();
	int annot_count = aseq.getAnnotationCount();
	System.out.println("seqid: " + aseq.getID() + ", annot count: " + annot_count );
	for (int i=0; i<annot_count; i++) {
	  SeqSymmetry psym = aseq.getAnnotation(i);
          SeqSpan pspan = psym.getSpan(aseq);
	  int child_count = psym.getChildCount();
	  String symid = psym.getID();
	  //          System.out.println("probeset_id: " + symid);
	  int symint = Integer.parseInt(symid);
	  dos.writeInt(symint);  // probeset id representated as an integer
	  // sign of strnad_and_count indicates forward (+) or reverse (-) strand
	  byte strand_and_count = (byte)(pspan.isForward() ? child_count : -child_count);
	  dos.writeByte(strand_and_count);
	  for (int k=0; k<child_count; k++) {
	    SeqSymmetry csym = psym.getChild(k);
	    dos.writeInt(csym.getSpan(aseq).getMin());
	  }
	}
      }
      dos.close();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   *  takes as input a gff file of genome-based probesets that meet the criteria:
   *    a) all probes are same length
   *    b) all probes align to a contiguous genome interval (no split probes)
   *    c) probeset ids can be represented numerically
   *    d) all probes within a probeset are on same strand
   *    e) less than 128 probes per probeset
   *          (but can be different number of probes in each probeset)
   *
   *  writes as output a file in bprobe1 format
   *  first arg is gff input file name
   *  second arg is bprobe output file name
   *  if no second arg, output is written to standard out??
   */
  public static void main(String[] args) {
    //    String in_file = "c:/data/more_wta_data/HuEx-1_0-st-Probes.head.gff";
    //    String out_file = "c:/data/more_wta_data/bprobe_test1.bp1";
    String in_file = "c:/data/more_wta_data/chr22-probes.gff";
    String out_file = "c:/data/more_wta_data/chr22_probes.bp1";
    String genomeid= "H_sapiens_Jul_2003";
    String versionid = "";
    String annot_type = "HeEx-1_0-st-Probes";

    convertGff(in_file, out_file, genomeid, versionid, annot_type);

    try  {
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(out_file)));
      AnnotatedSeqGroup group = gmodel.addSeqGroup(genomeid + versionid);
      Bprobe1Parser parser = new Bprobe1Parser();
      parser.parse(bis, group, true, annot_type);
    }
    catch (Exception ex)  {
      ex.printStackTrace();
    }

  }

  public void makeBprobe1(String gff_input_file, String bprobe_output_file) {

  }


}
