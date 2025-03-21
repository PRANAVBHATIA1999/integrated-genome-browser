package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.comparator.UcscPslComparator;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetryConverter;
import com.affymetrix.genometry.symmetry.impl.SimpleSymWithProps;
import com.affymetrix.genometry.symmetry.impl.UcscPslSym;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.SeqUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BpsParser implements AnnotationWriter, IndexWriter, Parser {

    private static final UcscPslComparator comp = new UcscPslComparator();
    private static final List<String> pref_list = new ArrayList<>();

    static {
        pref_list.add("bps");
        pref_list.add("psl");
    }

    private static final int estimated_count = 80000;

    /**
     * Reads binary PSL data from the given stream. Note that this method <b>can</b>
     * be interrupted early by Thread.interrupt(). The input stream will always be closed
     * before exiting this method.
     */
    public static List<UcscPslSym> parse(DataInputStream dis, String annot_type,
            GenomeVersion query_group, GenomeVersion target_group,
            boolean annot_query, boolean annot_target)
            throws IOException {

        // make temporary seq groups to avoid null pointers later
        if (query_group == null) {
            query_group = new GenomeVersion("Query");
            query_group.setUseSynonyms(false);
        }
        if (target_group == null) {
            target_group = new GenomeVersion("Target");
            target_group.setUseSynonyms(false);
        }

        Map<String, SeqSymmetry> target2sym = new HashMap<>(); // maps target chrom name to top-level symmetry
        Map<String, SeqSymmetry> query2sym = new HashMap<>(); // maps query chrom name to top-level symmetry
        List<UcscPslSym> results = new ArrayList<>(estimated_count);
        int count = 0;

        try {
            Thread thread = Thread.currentThread();
            // Loop will usually be ended by EOFException, but
            // can also be interrupted by Thread.interrupt()
            while (!thread.isInterrupted()) {
                int matches = dis.readInt();
                int mismatches = dis.readInt();
                int repmatches = dis.readInt();
                int ncount = dis.readInt();
                int qNumInsert = dis.readInt();
                int qBaseInsert = dis.readInt();
                int tNumInsert = dis.readInt();
                int tBaseInsert = dis.readInt();
                boolean qforward = dis.readBoolean();
                String qname = dis.readUTF();
                int qsize = dis.readInt();
                int qmin = dis.readInt();
                int qmax = dis.readInt();

                BioSeq queryseq = query_group.getSeq(qname);
                if (queryseq == null) {
                    queryseq = query_group.addSeq(qname, qsize);
                }
                if (queryseq.getLength() < qsize) {
                    queryseq.setLength(qsize);
                }

                String tname = dis.readUTF();
                int tsize = dis.readInt();
                int tmin = dis.readInt();
                int tmax = dis.readInt();

                BioSeq targetseq = target_group.getSeq(tname);
                if (targetseq == null) {
                    targetseq = target_group.addSeq(tname, tsize);
                }
                if (targetseq.getLength() < tsize) {
                    targetseq.setLength(tsize);
                }

                int blockcount = dis.readInt();
                int[] blockSizes = new int[blockcount];
                int[] qmins = new int[blockcount];
                int[] tmins = new int[blockcount];
                for (int i = 0; i < blockcount; i++) {
                    blockSizes[i] = dis.readInt();
                }
                for (int i = 0; i < blockcount; i++) {
                    qmins[i] = dis.readInt();
                }
                for (int i = 0; i < blockcount; i++) {
                    tmins[i] = dis.readInt();
                }
                count++;

                UcscPslSym sym
                        = new UcscPslSym(annot_type, matches, mismatches, repmatches, ncount,
                                qNumInsert, qBaseInsert, tNumInsert, tBaseInsert, qforward,
                                queryseq, qmin, qmax, targetseq, tmin, tmax,
                                blockcount, blockSizes, qmins, tmins, false);
                results.add(sym);

                if (annot_query) {
                    SimpleSymWithProps query_parent_sym = (SimpleSymWithProps) query2sym.get(qname);
                    if (query_parent_sym == null) {
                        query_parent_sym = new SimpleSymWithProps();
                        query_parent_sym.addSpan(new SimpleSeqSpan(0, queryseq.getLength(), queryseq));
                        query_parent_sym.setProperty("method", annot_type);
                        query_parent_sym.setProperty("preferred_formats", pref_list);
                        query_parent_sym.setProperty(SimpleSymWithProps.CONTAINER_PROP, Boolean.TRUE);
                        queryseq.addAnnotation(query_parent_sym);
                        query2sym.put(qname, query_parent_sym);
                    }
//					query_group.addToIndex(sym.getName(), sym);
                    query_parent_sym.addChild(sym);
                }

                if (annot_target) {
                    SimpleSymWithProps target_parent_sym = (SimpleSymWithProps) target2sym.get(tname);
                    if (target_parent_sym == null) {
                        target_parent_sym = new SimpleSymWithProps();
                        target_parent_sym.addSpan(new SimpleSeqSpan(0, targetseq.getLength(), targetseq));
                        target_parent_sym.setProperty("method", annot_type);
                        target_parent_sym.setProperty("preferred_formats", pref_list);
                        target_parent_sym.setProperty(SimpleSymWithProps.CONTAINER_PROP, Boolean.TRUE);
                        targetseq.addAnnotation(target_parent_sym);
                        target2sym.put(tname, target_parent_sym);
                    }
                    target_parent_sym.addChild(sym);
//					target_group.addToIndex(sym.getName(), sym);
                }
            }
        } catch (EOFException ex) {
        } finally {
            GeneralUtils.safeClose(dis);
        }

        if (count == 0) {
            Logger.getLogger(BpsParser.class.getName()).log(
                    Level.INFO, "BPS total counts == 0 ???");
        } else {
            Collections.sort(results, comp);
        }
        return results;
    }

    /**
     * Implementing AnnotationWriter interface to write out annotations
     * to an output stream as "binary PSL".
     *
     */
    public boolean writeAnnotations(Collection<? extends SeqSymmetry> syms, BioSeq seq,
            String type, OutputStream outstream) {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new BufferedOutputStream(outstream));
            for (SeqSymmetry sym : syms) {
                if (!(sym instanceof UcscPslSym)) {
                    int spancount = sym.getSpanCount();
                    if (spancount == 1) {
                        sym = SeqSymmetryConverter.convertToPslSym(sym, type, seq);
                    } else {
                        BioSeq seq2 = SeqUtils.getOtherSeq(sym, seq);
                        sym = SeqSymmetryConverter.convertToPslSym(sym, type, seq2, seq);
                    }
                }
                this.writeSymmetry(sym, seq, dos);
            }
            dos.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            GeneralUtils.safeClose(dos);
        }
        return true;
    }

    public Comparator<UcscPslSym> getComparator(BioSeq seq) {
        return comp;
    }

    public void writeSymmetry(SeqSymmetry sym, BioSeq seq, OutputStream os) throws IOException {
        DataOutputStream dos = null;
        if (os instanceof DataOutputStream) {
            dos = (DataOutputStream) os;
        } else {
            dos = new DataOutputStream(os);
        }
        ((UcscPslSym) sym).outputBpsFormat(dos);
    }

    public int getMin(SeqSymmetry sym, BioSeq seq) {
        return ((UcscPslSym) sym).getTargetMin();
    }

    public int getMax(SeqSymmetry sym, BioSeq seq) {
        return ((UcscPslSym) sym).getTargetMax();
    }

    public List<String> getFormatPrefList() {
        return BpsParser.pref_list;
    }

    public List<UcscPslSym> parse(DataInputStream dis, String annot_type, GenomeVersion genomeVersion) {
        try {
            return BpsParser.parse(dis, annot_type, null, genomeVersion, false, false);
        } catch (IOException ex) {
            Logger.getLogger(BpsParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Implementing AnnotationWriter interface to write out annotations
     * to an output stream as "binary PSL".
     *
     */
    public String getMimeType() {
        return "binary/bps";
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            String text_file = args[0];
            String bin_file = args[1];
            convertPslToBps(text_file, bin_file);
        } else {
            System.out.println("Usage:  java ... BpsParser <text infile> <binary outfile>");
            System.exit(1);
        }
    }

    private static void convertPslToBps(String psl_in, String bps_out) {
        System.out.println("reading text psl file");
        List<UcscPslSym> psl_syms = readPslFile(psl_in);
        System.out.println("done reading text psl file, annot count = " + psl_syms.size());
        System.out.println("writing binary psl file");
        writeBinary(bps_out, psl_syms);
        System.out.println("done writing binary psl file");
    }

    private static List<UcscPslSym> readPslFile(String file_name) {

        List<UcscPslSym> results = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            File fil = new File(file_name);
            long flength = fil.length();
            fis = new FileInputStream(fil);
            InputStream istr = null;
            byte[] bytebuf = new byte[(int) flength];
            bis = new BufferedInputStream(fis);
            bis.read(bytebuf);
            bis.close();
            ByteArrayInputStream bytestream = new ByteArrayInputStream(bytebuf);
            istr = bytestream;

            PSLParser parser = new PSLParser();
            // don't bother annotating the sequences, just get the list of syms
            results = parser.parse(istr, file_name, null, null, false, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            GeneralUtils.safeClose(bis);
            GeneralUtils.safeClose(fis);
        }
        return results;
    }

    private static void writeBinary(String file_name, List<UcscPslSym> syms) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        DataOutputStream dos = null;
        try {
            File outfile = new File(file_name);
            fos = new FileOutputStream(outfile);
            bos = new BufferedOutputStream(fos);
            dos = new DataOutputStream(bos);
            for (UcscPslSym psl : syms) {
                psl.outputBpsFormat(dos);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            GeneralUtils.safeClose(dos);
            GeneralUtils.safeClose(bos);
            GeneralUtils.safeClose(fos);
        }
    }

    @Override
    public List<? extends SeqSymmetry> parse(InputStream is, GenomeVersion genomeVersion,
            String nameType, String uri, boolean annotate_seq) throws Exception {
        DataInputStream dis = new DataInputStream(is);
        return parse(dis, uri, null, genomeVersion, false, annotate_seq);
    }
}
