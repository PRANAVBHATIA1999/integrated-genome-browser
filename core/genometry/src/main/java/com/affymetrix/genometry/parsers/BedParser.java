/**
 * Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License").
 * A copy of the license must be included with any distribution of
 * this source code.
 * Distributions from Affymetrix, Inc., place this in the
 * IGB_LICENSE.html file.
 *
 * The license is also available at
 * http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.Scored;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.comparator.SeqSymMinComparator;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SimpleScoredSymWithProps;
import com.affymetrix.genometry.symmetry.impl.SimpleSymWithProps;
import com.affymetrix.genometry.symmetry.impl.UcscBedDetailSym;
import com.affymetrix.genometry.symmetry.impl.UcscBedSym;
import com.google.common.base.Strings;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * A parser for UCSC's BED format.
 * <pre>
 *
 *  From http://genome.ucsc.edu/goldenPath/help/customTrack.html#BED
 *  BED format provides a flexible way to define the data lines that are displayed
 *  in an annotation track. BED lines have three required fields and nine additional
 *  optional fields. The number of fields per line must be consistent throughout
 *  any single set of data in an annotation track.
 *
 * Some BED files from UCSC contain an initial column *before* the chromosome name.
 * We simply ignore this column; we recognize that it is there by the fact that
 * the strand is given in (zero-based-)column 6 rather than 5.
 *
 * The first three required BED fields are:
 *    [0] chrom - The name of the chromosome (e.g. chr3, chrY, chr2_random) or contig (e.g. ctgY1).
 *    [1] chromStart - The starting position of the feature in the chromosome or contig.
 *           The first base in a chromosome is numbered 0.
 *    [2] chromEnd - The ending position of the feature in the chromosome or contig. The chromEnd
 *           base is not included in the display of the feature. For example, the first 100 bases
 *           of a chromosome are defined as chromStart=0, chromEnd=100, and span the bases numbered 0-99.
 * The 9 additional optional BED fields are:
 *    [3] name - Defines the name of the BED line. This label is displayed to the left of the BED line
 *          in the Genome Browser window when the track is open to full display mode.
 *    [4] score - A score between 0 and 1000. If the track line useScore attribute is set to 1 for
 *          this annotation data set, the score value will determine the level of gray in which
 *          this feature is displayed (higher numbers = darker gray).
 *    [5] strand - Defines the strand - either '+' or '-'.
 *    [6] thickStart - The starting position at which the feature is drawn thickly (for example,
 *          the start codon in gene displays).
 *    [7] thickEnd - The ending position at which the feature is drawn thickly (for example,
 *          the stop codon in gene displays).
 *        If thickStart = thickEnd, that should be interpreted as the absence of a thick region
 *    [8] itemRgb - a color for the item, in the format "RRR,GGG,BBB"; or use "0" for default color
 *        These colors will be used only if the track line property itemRgb is "On".
 *    [9] blockCount - The number of blocks (exons) in the BED line.
 *    [10] blockSizes - A comma-separated list of the block sizes. The number of items in this list
 *          should correspond to blockCount.
 *    [11] blockStarts - A comma-separated list of block starts. All of the blockStart positions
 *          should be calculated relative to chromStart. The number of items in this list should
 *          correspond to blockCount.
 * Example:
 *   Here's an example of an annotation track that uses a complete BED definition:
 *
 *  track name=pairedReads description="Clone Paired Reads" useScore=1
 *  chr22 1000 5000 cloneA 960 + 1000 5000 0 2 567,488, 0,3512
 *  chr22 2000 6000 cloneB 900 - 2000 6000 0 2 433,399, 0,3601
 *
 * </pre>
 */
public class BedParser implements AnnotationWriter, IndexWriter, Parser {

    // Used later to allow bed files to be output as a supported format in the DAS/2 types query.
    private static List<String> pref_list = new ArrayList<>();

    static {
        pref_list.add("bed");
    }

    private static final boolean DEBUG = false;
    private static final Pattern tab_regex = Pattern.compile("\\t");
    private static final Pattern line_regex = Pattern.compile("\\s+");
    private static final Pattern COMMA_REGEX = Pattern.compile(",");

    private boolean annotate_seq = true;
    private boolean create_container_annot = false;
    private String default_type = null;

    private final TrackLineParser track_line_parser = new TrackLineParser();

    public List<SeqSymmetry> parse(InputStream istr, GenometryModel gmodel,
            GenomeVersion genomeVersion, boolean annot_seq,
            String stream_name, boolean create_container)
            throws IOException {
        if (DEBUG) {
            System.out.println("BED parser called, annotate seq: " + annot_seq
                    + ", create_container_annot: " + create_container);
        }
        annotate_seq = annot_seq;
        this.create_container_annot = create_container;
        default_type = stream_name;

        if (stream_name.endsWith(".bed")) {
            default_type = stream_name.substring(0, stream_name.lastIndexOf(".bed"));
        }
        BufferedInputStream bis;
        if (istr instanceof BufferedInputStream) {
            bis = (BufferedInputStream) istr;
        } else {
            bis = new BufferedInputStream(istr);
        }
        DataInputStream dis = new DataInputStream(bis);
        return parse(dis, gmodel, genomeVersion, default_type);
    }

    private List<SeqSymmetry> parse(DataInputStream dis, GenometryModel gmodel, GenomeVersion seq_group, String default_type)
            throws IOException {
        if (DEBUG) {
            System.out.println("called BedParser.parseWithEvents()");
        }
        /*
         *  seq2types is hash for making container syms (if create_container_annot == true)
         *  each entry in hash is: BioSeq ==> type2psym hash
         *     Each type2csym is hash where each entry is "type" ==> container_sym
         *  so two-step process to find container sym for a particular type on a particular seq:
         *    Map type2csym = (Map)seq2types.get(seq);
         *    MutableSeqSymmetry container_sym = (MutableSeqSymmetry)type2csym.get(type);
         */
        List<SeqSymmetry> symlist = new ArrayList<>();
        Map<BioSeq, Map<String, SeqSymmetry>> seq2types = new HashMap<>();
        String line;
        String type = default_type;
        String bedType = null;
        boolean use_item_rgb = true;

        Thread thread = Thread.currentThread();
        BufferedReader reader = new BufferedReader(new InputStreamReader(dis));
        while ((line = reader.readLine()) != null && (!thread.isInterrupted())) {
            if (line.startsWith("track")) {
                track_line_parser.parseTrackLine(line);
//				ITrackStyleExtended style = TrackLineParser.createTrackStyle(track_line_parser.getCurrentTrackHash(), default_type, "bed");
                String trackLineName = track_line_parser.getTrackLineContent().get(TrackLineParser.NAME);
                if (StringUtils.isNotBlank(trackLineName)) {
                    if (type.contains(".bed")) {
                        type = type.substring(0, type.indexOf(".bed")) + " " + trackLineName;
                    } else {
                        type = type + "_" + trackLineName;
                    }
                    track_line_parser.getTrackLineContent().put(TrackLineParser.NAME, type);
                }
//				String item_rgb_string = track_line_parser.getCurrentTrackHash().get(TrackLineParser.ITEM_RGB);
//				use_item_rgb = item_rgb_string != null && item_rgb_string.length() > 0 ? "on".equalsIgnoreCase(item_rgb_string) : true;
//				style.setColorProvider(use_item_rgb? new RGB() : null);
                bedType = track_line_parser.getTrackLineContent().get("type");
            } else if (line.startsWith("browser")) {
                // currently take no action for browser lines
            } else {
                if (DEBUG) {
                    System.out.println(line);
                }
                parseLine(line, seq_group, gmodel, type, use_item_rgb, bedType, symlist, seq2types);
            }
        }
        return symlist;
    }

    private void parseLine(String line, GenomeVersion seq_group, GenometryModel gmodel,
            String type, boolean use_item_rgb, String bedType,
            List<SeqSymmetry> symlist, Map<BioSeq, Map<String, SeqSymmetry>> seq2types)
            throws NumberFormatException, IOException {
        boolean bedDetail = "bedDetail".equals(bedType);
        String detailId = null;
        String detailDescription = null;
//		String[] fields = bedDetail ? tab_regex.split(line) : line_regex.split(line);
        String[] fields = tab_regex.split(line);
        int field_count = fields.length;
        if (field_count == 1) {
            fields = line_regex.split(line);
        }
        if (bedDetail) {
            detailId = fields[field_count - 2];
            detailDescription = fields[field_count - 1];
            field_count -= 2;
        }
        if (field_count < 3) {
            return;
        }

        String seq_name = null;
        String annot_name = null;
        int min;
        int max;
        String itemRgb = "";
        int thick_min = Integer.MIN_VALUE; // Integer.MIN_VALUE signifies that thick_min is not used
        int thick_max = Integer.MIN_VALUE; // Integer.MIN_VALUE signifies that thick_max is not used
        float score = Float.NEGATIVE_INFINITY; // Float.NEGATIVE_INFINITY signifies that score is not used
        boolean forward;
        int[] blockSizes = null;
        int[] blockStarts = null;
        int[] blockMins = null;
        int[] blockMaxs = null;
        boolean includes_bin_field = field_count > 6 && (fields[6].startsWith("+") || fields[6].startsWith("-") || fields[6].startsWith("."));
        int findex = 0;
        if (includes_bin_field) {
            findex++;
        }
        seq_name = fields[findex++]; // seq id field
        BioSeq seq = seq_group.getSeq(seq_name);
        if ((seq == null) && (seq_name.indexOf(';') > -1)) {
            // if no seq found, try and split up seq_name by ";", in case it is in format
            //    "seqid;genome_version"
            String seqid = seq_name.substring(0, seq_name.indexOf(';'));
            String version = seq_name.substring(seq_name.indexOf(';') + 1);
            //            System.out.println("    seq = " + seqid + ", version = " + version);
            if ((gmodel.getSeqGroup(version) == seq_group) || seq_group.getName().equals(version)) {
                // for format [chrom_name];[genome_version]
                seq = seq_group.getSeq(seqid);
                if (seq != null) {
                    seq_name = seqid;
                }
            } else if ((gmodel.getSeqGroup(seqid) == seq_group) || seq_group.getName().equals(seqid)) {
                // for format [genome_version];[chrom_name]
                String temp = seqid;
                seqid = version;
                version = temp;
                seq = seq_group.getSeq(seqid);
                if (seq != null) {
                    seq_name = seqid;
                }
            }
        }
        if (seq == null) {
            //System.out.println("seq not recognized, creating new seq: " + seq_name);
            seq = seq_group.addSeq(seq_name, 0);
        }
        int beg = Integer.parseInt(fields[findex++]); // start field
        int end = Integer.parseInt(fields[findex++]); // stop field
        if (field_count >= 4) {
            annot_name = parseName(fields[findex++]);
            if (annot_name == null || annot_name.length() == 0) {
                annot_name = seq_group.getUniqueID();
            }
        }
        if (field_count >= 5) {
            score = parseScore(fields[findex++]);
        } // score field
        if (field_count >= 6) {
            forward = !(fields[findex++].equals("-"));
        } else {
            forward = (beg <= end);
        }
        min = Math.min(beg, end);
        max = Math.max(beg, end);
        if (field_count >= 8) {
            thick_min = Integer.parseInt(fields[findex++]); // thickStart field
            thick_max = Integer.parseInt(fields[findex++]); // thickEnd field
        }
        if (field_count >= 9) {
            itemRgb = fields[findex++];
        } else {
            findex++;
        }
        if (field_count >= 12) {
            int blockCount = Integer.parseInt(fields[findex++]); // blockCount field
            blockSizes = parseIntArray(fields[findex++]); // blockSizes field
            if (blockCount != blockSizes.length) {
                System.out.println("WARNING: block count does not agree with block sizes.  Ignoring " + annot_name + " on " + seq_name);
                return;
            }
            blockStarts = parseIntArray(fields[findex++]); // blockStarts field
            if (blockCount != blockStarts.length) {
                System.out.println("WARNING: block size does not agree with block starts.  Ignoring " + annot_name + " on " + seq_name);
                return;
            }
            blockMins = makeBlockMins(min, blockStarts);
            blockMaxs = makeBlockMaxs(blockSizes, blockMins);
        } else {
            /*
             * if no child blocks, make a single child block the same size as the parent
             * Very Inefficient, ideally wouldn't do this
             * But currently need this because of GenericAnnotGlyphFactory use of annotation depth to
             *     determine at what level to connect glyphs -- if just leave blockMins/blockMaxs null (no children),
             *     then factory will create a line container glyph to draw line connecting all the bed annots
             * Maybe a way around this is to adjust depth preference based on overall depth (1 or 2) of bed file?
             */
            blockMins = new int[1];
            blockMins[0] = min;
            blockMaxs = new int[1];
            blockMaxs[0] = max;
        }
        if (max > seq.getLength()) {
            seq.setLength(max);
        }
        if (DEBUG) {
            System.out.println("fields: " + field_count + ", type = " + type + ", seq = " + seq_name + ", min = " + min + ", max = " + max + ", name = " + annot_name + ", score = " + score + ", forward = " + forward + ", thickmin = " + thick_min + ", thickmax = " + thick_max);
            if (blockMins != null) {
                int count = blockMins.length;
                if (blockSizes != null && blockStarts != null && blockMins != null && blockMaxs != null) {
                    for (int i = 0; i < count; i++) {
                        System.out.println("   " + i + ": blockSize = " + blockSizes[i] + ", blockStart = " + blockStarts[i] + ", blockMin = " + blockMins[i] + ", blockMax = " + blockMaxs[i]);
                    }
                }
            }
        }
        SymWithProps bedline_sym = null;
        bedline_sym = bedDetail
                ? new UcscBedDetailSym(type, seq, min, max, annot_name, score, forward, thick_min, thick_max, blockMins, blockMaxs, detailId, detailDescription)
                : new UcscBedSym(type, seq, min, max, annot_name, score, forward, thick_min, thick_max, blockMins, blockMaxs);
        if (use_item_rgb && itemRgb != null) {
            java.awt.Color c = null;
            try {
                c = TrackLineParser.reformatColor(itemRgb);
            } catch (Exception e) {
                throw new IOException("Could not parse a color from String '" + itemRgb + "'");
            }
            if (c != null) {
                bedline_sym.setProperty(TrackLineParser.ITEM_RGB, c);
            }
        }
        symlist.add(bedline_sym);
        if (annotate_seq) {
            this.annotationParsed(bedline_sym, seq2types);
        }
        if (annot_name != null) {
//			seq_group.addToIndex(annot_name, bedline_sym);
        }
    }

    /**
     * Converts the data in the score field, if present, to a floating-point number.
     */
    private static float parseScore(String s) {
        if (s == null || s.length() == 0 || s.equals(".") || s.equals("-")) {
            return 0.0f;
        }
        return Float.parseFloat(s);
    }

    /**
     * Parses the name field from the file. Gene names are allowed to be non-unique.
     *
     * @param s
     * @return annot_name
     */
    private static String parseName(String s) {
        String annot_name = s; // create a new String so the entire input line doesn't get preserved
        return annot_name;
    }

    private void annotationParsed(SeqSymmetry bedline_sym, Map<BioSeq, Map<String, SeqSymmetry>> seq2types) {
        BioSeq seq = bedline_sym.getSpan(0).getBioSeq();
        if (create_container_annot) {
            String type = track_line_parser.getTrackLineContent().get(TrackLineParser.NAME);
            if (type == null) {
                type = default_type;
            }
            Map<String, SeqSymmetry> type2csym = seq2types.get(seq);
            if (type2csym == null) {
                type2csym = new HashMap<>();
                seq2types.put(seq, type2csym);
            }
            SimpleSymWithProps parent_sym = (SimpleSymWithProps) type2csym.get(type);
            if (parent_sym == null) {
                parent_sym = new SimpleSymWithProps();
                parent_sym.addSpan(new SimpleSeqSpan(0, seq.getLength(), seq));
                parent_sym.setProperty("method", type);
                parent_sym.setProperty("preferred_formats", pref_list);   // Used to indicate to DAS/2 server to support the formats in the pref_list.
                parent_sym.setProperty(SimpleSymWithProps.CONTAINER_PROP, Boolean.TRUE);
                seq.addAnnotation(parent_sym);
                type2csym.put(type, parent_sym);
            }
            parent_sym.addChild(bedline_sym);
        } else {
            seq.addAnnotation(bedline_sym);
        }
    }

    public static int[] parseIntArray(String intArray) {
        if (Strings.isNullOrEmpty(intArray)) {
            return new int[0];
        }
        String[] intstrings = COMMA_REGEX.split(intArray);
        int count = intstrings.length;
        int[] results = new int[count];
        for (int i = 0; i < count; i++) {
            int val = Integer.parseInt(intstrings[i]);
            results[i] = val;
        }
        return results;
    }

    /**
     * Converting blockStarts to blockMins.
     *
     * @param blockStarts in coords relative to min of annotation
     * @return blockMins in coords relative to sequence that annotation is "on"
     */
    public static int[] makeBlockMins(int min, int[] blockStarts) {
        int count = blockStarts.length;
        int[] blockMins = new int[count];
        for (int i = 0; i < count; i++) {
            blockMins[i] = blockStarts[i] + min;
        }
        return blockMins;
    }

    public static int[] makeBlockMaxs(int[] blockMins, int[] blockSizes) {
        int count = blockMins.length;
        int[] blockMaxs = new int[count];
        for (int i = 0; i < count; i++) {
            blockMaxs[i] = blockMins[i] + blockSizes[i];
        }
        return blockMaxs;
    }

    public static void writeBedFormat(DataOutputStream out, List<SeqSymmetry> syms, BioSeq seq)
            throws IOException {
        for (SeqSymmetry sym : syms) {
            writeSymmetry(out, sym, seq);
        }
    }

    /**
     * Writes bed file format.
     * WARNING. This currently assumes that each child symmetry contains
     * a span on the seq given as an argument.
     */
    public static void writeSymmetry(DataOutputStream out, SeqSymmetry sym, BioSeq seq)
            throws IOException {
        if (DEBUG) {
            System.out.println("writing sym: " + sym);
        }
        SeqSpan span = sym.getSpan(seq);
        if (span == null) {
            return;
        }

        if (sym instanceof UcscBedSym) {
            UcscBedSym bedsym = (UcscBedSym) sym;
            if (seq == bedsym.getBioSeq()) {
                bedsym.outputBedFormat(out);
                out.write('\n');
            }
            return;
        }

        SymWithProps propsym = null;
        if (sym instanceof SymWithProps) {
            propsym = (SymWithProps) sym;
        }

        writeOutFile(out, seq, span, sym, propsym);
    }

    private static void writeOutFile(DataOutputStream out, BioSeq seq, SeqSpan span, SeqSymmetry sym, SymWithProps propsym) throws IOException {
        out.write(seq.getId().getBytes());
        out.write('\t');
        int min = span.getMin();
        int max = span.getMax();
        out.write(Integer.toString(min).getBytes());
        out.write('\t');
        out.write(Integer.toString(max).getBytes());
        int childcount = sym.getChildCount();
        if ((!span.isForward()) || (childcount > 0) || (propsym != null)) {
            out.write('\t');
            if (propsym != null) {
                if (propsym.getProperty("name") != null) {
                    out.write(((String) propsym.getProperty("name")).getBytes());
                } else if (propsym.getProperty("id") != null) {
                    out.write(((String) propsym.getProperty("id")).getBytes());
                } else {
                    out.write((seq.getId() + ":" + Integer.toString(min) + "-" + Integer.toString(max) + ":" + (span.isForward() ? "+" : "-")).getBytes());
                }
            }
            out.write('\t');
            if ((propsym != null) && (propsym.getProperty("score") != null)) {

                Float score = (Float) propsym.getProperty("score");
                if (Float.compare(score, Scored.UNKNOWN_SCORE) == 0) {
                    out.write('.');
                } else if (score == Math.round(score)) {
                    out.write(Integer.toString(score.intValue()).getBytes());
                } else {
                    out.write(score.toString().getBytes());
                }

            } else if (sym instanceof Scored) {

                Float score = ((Scored) sym).getScore();
                if (Float.compare(score, Scored.UNKNOWN_SCORE) == 0) {
                    out.write('.');
                } else if (score == Math.round(score)) {
                    out.write(Integer.toString(score.intValue()).getBytes());
                } else {
                    out.write(Float.toString(score).getBytes());
                }

            } else {
                out.write('0');
            }
            out.write('\t');
            if (span.isForward()) {
                out.write('+');
            } else {
                out.write('-');
            }
            if (childcount > 0) {
                writeOutChildren(out, propsym, min, max, childcount, sym, seq);
            }
        }
        out.write('\n');
    }

    protected static void writeOutChildren(DataOutputStream out, SymWithProps propsym, int min, int max, int childcount, SeqSymmetry sym, BioSeq seq) throws IOException {
        out.write('\t');
        if ((propsym != null) && (propsym.getProperty("cds min") != null)) {
            out.write(propsym.getProperty("cds min").toString().getBytes());
        } else {
            out.write(Integer.toString(min).getBytes());
        }
        out.write('\t');
        if ((propsym != null) && (propsym.getProperty("cds max") != null)) {
            out.write(propsym.getProperty("cds max").toString().getBytes());
        } else if (sym instanceof SimpleScoredSymWithProps) { // Summary feature doesn't have a translation
            out.write(Integer.toString(min).getBytes());
        } else {
            out.write(Integer.toString(max).getBytes());
        }
        out.write('\t');
        out.write('0');
        out.write('\t');
        out.write(Integer.toString(childcount).getBytes());
        out.write('\t');
        int[] blockSizes = new int[childcount];
        int[] blockStarts = new int[childcount];
        for (int i = 0; i < childcount; i++) {
            SeqSymmetry csym = sym.getChild(i);
            SeqSpan cspan = csym.getSpan(seq);
            blockSizes[i] = cspan.getLength();
            blockStarts[i] = cspan.getMin() - min;
        }
        for (int i = 0; i < childcount; i++) {
            out.write(Integer.toString(blockSizes[i]).getBytes());
            out.write(',');
        }
        out.write('\t');
        for (int i = 0; i < childcount; i++) {
            out.write(Integer.toString(blockStarts[i]).getBytes());
            out.write(',');
        }
    }

    /**
     * Implementing AnnotationWriter interface to write out annotations
     * to an output stream as "BED" format.
     *
     */
    public boolean writeAnnotations(Collection<? extends SeqSymmetry> syms, BioSeq seq,
            String type, OutputStream outstream) {
        if (DEBUG) {
            System.out.println("in BedParser.writeAnnotations()");
        }
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new BufferedOutputStream(outstream));
            for (SeqSymmetry sym : syms) {
                writeSymmetry(dos, sym, seq);
            }
            dos.flush();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void writeSymmetry(SeqSymmetry sym, BioSeq seq, OutputStream os) throws IOException {
        DataOutputStream dos = null;
        if (os instanceof DataOutputStream) {
            dos = (DataOutputStream) os;
        } else {
            dos = new DataOutputStream(os);
        }
        BedParser.writeSymmetry(dos, sym, seq);
    }

    public List<SeqSymmetry> parse(DataInputStream dis, String annot_type, GenomeVersion genomeVersion) {
        try {
            return this.parse(dis, GenometryModel.getInstance(), genomeVersion, false, annot_type, false);
        } catch (IOException ex) {
            Logger.getLogger(BedParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Comparator<SeqSymmetry> getComparator(BioSeq seq) {
        return new SeqSymMinComparator(seq);
    }

    public int getMin(SeqSymmetry sym, BioSeq seq) {
        SeqSpan span = sym.getSpan(seq);
        return span.getMin();
    }

    public int getMax(SeqSymmetry sym, BioSeq seq) {
        SeqSpan span = sym.getSpan(seq);
        return span.getMax();
    }

    public List<String> getFormatPrefList() {
        return pref_list;
    }

    /**
     * Returns "text/bed".
     */
    public String getMimeType() {
        return "text/bed";
    }

    @Override
    public List<? extends SeqSymmetry> parse(InputStream is, GenomeVersion genomeVersion, String nameType,
            String uri, boolean annotate_seq) throws Exception {
        // really need to switch create_container (last argument) to true soon!
        return parse(is, GenometryModel.getInstance(), genomeVersion, annotate_seq, uri, false);
    }
}
