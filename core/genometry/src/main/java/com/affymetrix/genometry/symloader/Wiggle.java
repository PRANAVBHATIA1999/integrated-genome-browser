/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometry.symloader;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.comparator.BioSeqComparator;
import com.affymetrix.genometry.parsers.AnnotationWriter;
import com.affymetrix.genometry.parsers.TrackLineParser;
import com.affymetrix.genometry.parsers.graph.BarParser;
import com.affymetrix.genometry.parsers.graph.WiggleData;
import com.affymetrix.genometry.style.GraphState;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.LoadUtils.LoadStrategy;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import htsjdk.tribble.readers.LineReader;

/**
 *
 * @author hiralv
 */
public class Wiggle extends SymLoader implements AnnotationWriter, LineProcessor {

    private static final String TRACK = "track type={0}_{1} name=\"{2}_{3}\"";
    private static final String TRACK_TYPE = "wiggle_0";
    private int TRACK_COUNTER = 0;

    private static enum WigFormat {

        BED4, VARSTEP, FIXEDSTEP
    }

    private static final Pattern field_regex = Pattern.compile("\\s+");  // one or more whitespace
    private static final boolean ensure_unique_id = true;

    private static final List<LoadStrategy> strategyList = new ArrayList<>();

    static {
        strategyList.add(LoadStrategy.NO_LOAD);
        strategyList.add(LoadStrategy.VISIBLE);
//		strategyList.add(LoadStrategy.CHROMOSOME);
        strategyList.add(LoadStrategy.GENOME);
    }

    public Wiggle(URI uri, Optional<URI> indexUri, String featureName, GenomeVersion seq_group) {
        super(uri, indexUri, featureName, seq_group);
    }

    @Override
    public List<LoadStrategy> getLoadChoices() {
        return strategyList;
    }

    public void init(URI uri) {
    }

    @Override
    public void init() throws Exception {
        if (this.isInitialized) {
            return;
        }
        if (buildIndex()) {
            super.init();
        }
    }

    @Override
    public List<BioSeq> getChromosomeList() throws Exception {
        init();
        List<BioSeq> chromosomeList = new ArrayList<>(chrList.keySet());
        Collections.sort(chromosomeList, new BioSeqComparator());
        return chromosomeList;
    }

    @Override
    public List<GraphSym> getGenome() throws Exception {
        init();
        List<BioSeq> allSeq = getChromosomeList();
        List<GraphSym> retList = new ArrayList<>();
        for (BioSeq seq : allSeq) {
            retList.addAll(getChromosome(seq));
        }
        return retList;
    }

    @Override
    public List<GraphSym> getChromosome(BioSeq seq) throws Exception {
        init();
        return parse(seq, seq.getMin(), seq.getMax());
    }

    @Override
    public List<GraphSym> getRegion(SeqSpan span) throws Exception {
        init();
        return parse(span.getBioSeq(), span.getMin(), span.getMax());
    }

    /**
     * Reads a Wiggle-formatted file using any combination of the three formats
     * BED4,VARSTEP,FIXEDSTEP. The format must be specified on the first line
     * following a track line, otherwise BED4 is assumed.
     */
    private List<GraphSym> parse(BioSeq seq, int min, int max) throws Exception {
        FileInputStream fis = null;
        InputStream istr = null;

        try {
            File file = chrList.get(seq);
            if (file == null) {
                Logger.getLogger(Wiggle.class.getName()).log(Level.FINE, "Could not find chromosome {0}", seq.getId());
                return Collections.<GraphSym>emptyList();
            }

            final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            Iterator<String> it = new Iterator<String>() {

                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public String next() {
                    try {
                        return br.readLine();
                    } catch (IOException x) {
                        Logger.getLogger(this.getClass().getName()).log(
                                Level.SEVERE, "error reading wig file", x);
                    }
                    return null;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };

            return parse(it, seq, min, max);

        } catch (Exception ex) {
            throw ex;
        } finally {
            GeneralUtils.safeClose(istr);
            GeneralUtils.safeClose(fis);
        }

    }

    @Override
    public List<? extends SeqSymmetry> processLines(BioSeq seq, final LineReader lineReader) {
        Iterator<String> it = new Iterator<String>() {

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public String next() {
                String line = null;
                try {
                    line = lineReader.readLine();
                } catch (IOException x) {
                    Logger.getLogger(this.getClass().getName()).log(
                            Level.SEVERE, "error reading bed file", x);
                    line = null;
                }
                return line;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return parse(it, seq, seq.getMin(), seq.getMax());
    }

    private List<GraphSym> parse(Iterator<String> it, BioSeq seq, int min, int max) {
        TrackLineParser track_line_parser = new TrackLineParser();
        WigFormat current_format = WigFormat.BED4;
        List<GraphSym> grafs = new ArrayList<>();
        WiggleData current_data = null;
        Map<String, WiggleData> current_datamap = new HashMap<>(); // Map: seq_id -> WiggleData
        boolean previous_track_line = false;

        String line;

        // these may be used by fixedStep or variableStep
        String current_seq_id = null;
        int current_start = 0;
        int current_step = 0;
        int current_span = 0;
        while ((line = it.next()) != null && !Thread.currentThread().isInterrupted()) {
            if (line.length() == 0) {
                continue;
            }
            char firstChar = line.charAt(0);
            if (firstChar == '#' || firstChar == '%' || (firstChar == 'b' && line.startsWith("browser"))) {
                continue;
            }

            if (firstChar == 't' && line.startsWith("track")) {
                if (previous_track_line) {
                    // finish previous graph(s) using previous track properties
                    grafs.addAll(createGraphSyms(track_line_parser.getTrackLineContent(), genomeVersion, current_datamap, uri.toString(), extension));
                }

                track_line_parser.parseTrackLine(line);
                previous_track_line = true;

                current_format = WigFormat.BED4; // assume BED4 until changed.
                current_data = null;
                current_datamap = new HashMap<>();
                continue;
            }

            if (firstChar == 'v' && line.startsWith("variableStep")) {
                String[] fields = field_regex.split(line);
                current_format = WigFormat.VARSTEP;
                current_seq_id = Wiggle.parseFormatFields(fields, "chrom", "unknown");
                current_span = Integer.parseInt(Wiggle.parseFormatFields(fields, "span", "1"));
                continue;
            }

            if (firstChar == 'f' && line.startsWith("fixedStep")) {
                String[] fields = field_regex.split(line);
                current_format = WigFormat.FIXEDSTEP;
                current_seq_id = Wiggle.parseFormatFields(fields, "chrom", "unknown");
                current_start = Integer.parseInt(Wiggle.parseFormatFields(fields, "start", "1"));
                if (current_start < 1) {
                    throw new IllegalArgumentException("'fixedStep' format with start of " + current_start + ".");
                }
                current_step = Integer.parseInt(Wiggle.parseFormatFields(fields, "step", "1"));
                current_span = Integer.parseInt(Wiggle.parseFormatFields(fields, "span", "1"));
                continue;
            }

            current_start = parseData(
                    previous_track_line, line, current_format, current_data, current_datamap, current_seq_id, current_span, current_start, current_step, seq, min, max);
        }
        grafs.addAll(createGraphSyms(track_line_parser.getTrackLineContent(), genomeVersion, current_datamap, uri.toString(), extension));

        return grafs;
    }

    private static int parseData(boolean previous_track_line, String line, WigFormat current_format,
            WiggleData current_data, Map<String, WiggleData> current_datamap, String current_seq_id,
            int current_span, int current_start, int current_step, BioSeq reqSeq, int min, int max)
            throws IllegalArgumentException {
        // There should have been one track line at least...
        String[] fields = field_regex.split(line.trim()); // trim() because lines are allowed to start with whitespace

        switch (current_format) {
            case BED4:
                parseDataLine(fields, current_data, current_datamap, min, max);
                break;
            case VARSTEP:
                parseDataLine(fields, current_data, current_datamap, current_seq_id, current_span, min, max);
                break;
            case FIXEDSTEP:
                parseDataLine(fields, current_data, current_datamap, current_seq_id, current_span, current_start, min, max);
                current_start += current_step; // We advance the start based upon the step.
                break;
        }
        return current_start;
    }

    /**
     * Parse a single line of data (BED4 format).
     *
     * @param fields
     * @param current_data
     * @param current_datamap
     */
    private static void parseDataLine(
            String[] fields,
            WiggleData current_data,
            Map<String, WiggleData> current_datamap, int min, int max) {

        // chrom  start end value
        String seq_id = fields[0];	// chrom

        current_data = current_datamap.get(seq_id);
        if (current_data == null) {
            current_data = new WiggleData(seq_id);
            current_datamap.put(seq_id, current_data);
        }

        int x1 = Integer.parseInt(fields[1]);	// start, or perhaps end
        int x2 = Integer.parseInt(fields[2]);	// start, or perhaps end
        int start = Math.min(x1, x2);
        int width = Math.max(x1, x2) - start;

        if (!checkRange(x1, width, min, max)) {
            return;
        }

        current_data.add(x1, Float.parseFloat(fields[3]), width);
    }

    /**
     * Parse a single line of data (variableStep format).
     *
     * @param fields
     * @param current_data
     * @param current_datamap
     * @param current_seq_id
     * @param current_span
     */
    private static void parseDataLine(
            String[] fields,
            WiggleData current_data,
            Map<String, WiggleData> current_datamap,
            String current_seq_id,
            int current_span, int min, int max) {

        current_data = current_datamap.get(current_seq_id);
        if (current_data == null) {
            current_data = new WiggleData(current_seq_id);
            current_datamap.put(current_seq_id, current_data);
        }

        int current_start = Integer.parseInt(fields[0]);
        if (current_start < 1) {
            throw new IllegalArgumentException("'variableStep' format with start of " + current_start + ".");
        }
        current_start -= 1;	// This is because fixedStep and variableStep sequences are 1-indexed.  See http://genome.ucsc.edu/goldenPath/help/wiggle.html

        if (!checkRange(current_start, current_span, min, max)) {
            return;
        }

        current_data.add(current_start, Float.parseFloat(fields[1]), current_span);

    }

    /**
     * Parse a single line of data (fixedStep format).
     *
     * @param fields
     * @param current_data
     * @param current_datamap
     * @param current_seq_id
     * @param current_span
     * @param current_start
     */
    private static void parseDataLine(
            String[] fields,
            WiggleData current_data,
            Map<String, WiggleData> current_datamap,
            String current_seq_id,
            int current_span,
            int current_start, int min, int max) {

        current_data = current_datamap.get(current_seq_id);
        if (current_data == null) {
            current_data = new WiggleData(current_seq_id);
            current_datamap.put(current_seq_id, current_data);
        }

        current_start -= 1;	// This is because fixedStep and variableStep formats are 1-indexed.  See http://genome.ucsc.edu/goldenPath/help/wiggle.html

        if (!checkRange(current_start, current_span, min, max)) {
            return;
        }

        current_data.add(current_start, Float.parseFloat(fields[0]), current_span);
    }

    private static boolean checkRange(int start, int width, int min, int max) {

        //getChromosome && getRegion
        if (start + width < min || start > max) {
            return false;
        }

        return true;
    }

    /**
     * Parse the line, looking for the field name. If it can't be found, return
     * the default value.
     *
     * @param name
     * @param fields
     * @param default_val
     * @return string
     */
    private static String parseFormatFields(String[] fields, String name, String default_val) {
        String fieldName = name + "=";
        for (String field : fields) {
            if (field.startsWith(fieldName)) {
                return field.substring(name.length() + 1);
            }
        }
        return default_val;
    }

    /**
     * Finishes the current data section and creates a list of GraphSym objects.
     */
    private static List<GraphSym> createGraphSyms(Map<String, String> track_hash, GenomeVersion seq_group,
            Map<String, WiggleData> current_datamap, String stream_name, String extension) {
        if (current_datamap == null) {
            return Collections.<GraphSym>emptyList();
        }

        List<GraphSym> grafs = new ArrayList<>(current_datamap.size());

        //TrackLineParser.createTrackStyle(track_hash, stream_name);
        String graph_id = track_hash.get(TrackLineParser.NAME);

        if (graph_id == null) {
            graph_id = stream_name;
        }

        String graph_name = graph_id;

        if (!stream_name.equals(graph_id)) {
            graph_id = GenomeVersion.getUniqueGraphTrackID(stream_name, graph_id);
        }
//		track_hash.put(TrackLineParser.NAME, graph_id);

        TrackLineParser.createGraphStyle(track_hash, graph_id, graph_name, extension);

        // Need iterator because we're removing data on the fly
        Iterator<WiggleData> wiggleDataIterator = current_datamap.values().iterator();
        while (wiggleDataIterator.hasNext()) {
            GraphSym gsym = wiggleDataIterator.next().createGraph(seq_group, graph_id, stream_name);

            if (gsym != null) {
                grafs.add(gsym);
            }
            wiggleDataIterator.remove();	// free up memory now that we've created the graph.
        }

        return grafs;
    }

    private static void writeGraphPoints(Collection<? extends SeqSymmetry> syms, BufferedWriter bw) throws IOException {

        //Create multiple GraphSym objects (for multiple chromosomes)
        Iterator iterator = syms.iterator();
        while (iterator.hasNext()) {
            GraphSym graf = (GraphSym) iterator.next();
            int total_points = graf.getPointCount();
            for (int i = 0; i < total_points; i++) {
                int x2 = graf.getGraphXCoord(i) + graf.getGraphWidthCoord(i);
                String seq_id = (graf.getGraphSeq() == null ? "." : graf.getGraphSeq().getId());
                bw.write(seq_id + "\t" + graf.getGraphXCoord(i) + "\t" + x2
                        + "\t" + graf.getGraphYCoord(i) + '\n');
            }
        }
    }

    protected String getTrackType() {
        return TRACK_TYPE;
    }

    /**
     * Writes the given GraphIntervalSym in wiggle-BED format. Also writes a
     * track line as a header.
     */
    public void writeBedFormat(Collection<? extends SeqSymmetry> syms, String genome_version, OutputStream outstream) throws IOException {
        //Create one GraphSym object from the provided Syms. (only need 1 header)
        GraphSym graf = null;
        Iterator<? extends SeqSymmetry> iter = syms.iterator();
        if (iter.hasNext()) {
            graf = (GraphSym) iter.next();
        }
        
        if (graf != null) {  
            BioSeq seq = graf.getGraphSeq();
            String human_name = graf.getGraphState().getTierStyle().getTrackName();
            String gname = graf.getGraphName();
            GraphState state = graf.getGraphState();
            Color color = state.getTierStyle().getForeground();
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new OutputStreamWriter(outstream));
                //Write out header
                if (genome_version != null && genome_version.length() > 0) {
                    bw.write("# genome_version = " + genome_version + '\n');
                }
                bw.write("track type=" + getTrackType() + " name=\"" + gname + "\"");
                bw.write(" description=\"" + human_name + "\"");
                bw.write(" visibility=full");
                bw.write(" color=" + color.getRed() + "," + color.getGreen() + "," + color.getBlue());
                bw.write(" viewLimits=" + Float.toString(state.getVisibleMinY()) + ":" + Float.toString(state.getVisibleMaxY()));
                bw.write('\n');               
                //Populate file contents
                writeGraphPoints(syms, bw);
                bw.flush();
            } finally {
                GeneralUtils.safeClose(bw);
            }
        }
    }

    /**
     * Writes the give GraphSym in bar format.
     *
     * @param syms	GraphSym to be written.
     * @param type	Type required for bar format.
     * @param ostr	Outputstream to write bar file.
     * @return	Returns true if bar file is written sucessfully.
     */
    public static boolean writeBarFormat(
            Collection<? extends SeqSymmetry> syms,
            String type, OutputStream ostr) {

        BarParser instance = new BarParser();
        return instance.writeAnnotations(syms, null, type, ostr);
    }

    public String getMimeType() {
        return "text/wig";
    }

    @Override
    protected boolean parseLines(InputStream istr, Map<String, Integer> chrLength, Map<String, File> chrFiles) throws Exception {
        BufferedReader br = null;
        BufferedWriter bw = null;

        WigFormat current_format = WigFormat.BED4;
        boolean previous_track_line = false;
        String previous_seq_id = null;
        String line, current_seq_id = null, trackLine = null;
        int current_start = 0;
        int current_step = 0;
        int current_span = 0;
        int length = 0;

        Map<String, Boolean> chrTrack = new HashMap<>();
        Map<String, BufferedWriter> chrs = new HashMap<>();

        try {

            br = new BufferedReader(new InputStreamReader(istr));
            Thread thread = Thread.currentThread();
            while ((line = br.readLine()) != null && !thread.isInterrupted()) {
                if (line.length() == 0) {
                    continue;
                }
                char firstChar = line.charAt(0);
                if (firstChar == '#' || firstChar == '%' || (firstChar == 'b' && line.startsWith("browser"))) {
                    continue;
                }
                if (firstChar == 't' && line.startsWith("track")) {
                    chrTrack = new HashMap<>();
                    trackLine = line;
                    previous_track_line = true;
                    current_format = WigFormat.BED4; // assume BED4 until changed.
                    continue;
                }
                if ((firstChar == 'v' && line.startsWith("variableStep")) || (firstChar == 'f' && line.startsWith("fixedStep"))) {
                    String[] fields = field_regex.split(line);
                    if (firstChar == 'v') {
                        current_format = WigFormat.VARSTEP;
                        current_seq_id = Wiggle.parseFormatFields(fields, "chrom", "unknown");
                        current_span = Integer.parseInt(Wiggle.parseFormatFields(fields, "span", "1"));
                    } else {
                        current_format = WigFormat.FIXEDSTEP;
                        current_seq_id = Wiggle.parseFormatFields(fields, "chrom", "unknown");
                        current_start = Integer.parseInt(Wiggle.parseFormatFields(fields, "start", "1"));
                        if (current_start < 1) {
                            throw new IllegalArgumentException("'fixedStep' format with start of " + current_start + ".");
                        }
                        current_step = Integer.parseInt(Wiggle.parseFormatFields(fields, "step", "1"));
                        current_span = Integer.parseInt(Wiggle.parseFormatFields(fields, "span", "1"));
                    }

                    if (!previous_track_line) {
                        // Creat a dummy track only it's the same sequence
                        if (current_seq_id.equals(previous_seq_id)) {
                            trackLine = MessageFormat.format(TRACK, uri.toString(), TRACK_COUNTER, featureName, TRACK_COUNTER++);
                            Logger.getLogger(Wiggle.class.getName()).log(Level.WARNING, "Wiggle format error: line does not have a previous ''track'' line."
                                    + " Creating a dummy track line. {0}", trackLine);
                            //throw new IllegalArgumentException("Wiggle format error: line does not have a previous 'track' line");
                        }
                    }

                    previous_seq_id = current_seq_id;

                    if (!chrs.containsKey(current_seq_id)) {
                        addToLists(chrs, current_seq_id, chrFiles, chrLength, ".wig");
                    }
                    bw = chrs.get(current_seq_id);
                    if (!chrTrack.containsKey(current_seq_id) && trackLine != null) {
                        chrTrack.put(current_seq_id, true);
                        bw.write(trackLine + "\n");
                    }
                    bw.write(line + "\n");
                    continue;
                }

                String[] fields = field_regex.split(line.trim());
                switch (current_format) {
                    case BED4:
                        if (fields.length < 4) {
                            throw new IllegalArgumentException("Wiggle format error: Improper " + current_format + " line: " + line);
                        }
                        current_seq_id = fields[0];
                        length = Integer.parseInt(fields[2]);
                        if (!chrs.containsKey(current_seq_id)) {
                            addToLists(chrs, current_seq_id, chrFiles, chrLength, ".wig");
                        }
                        bw = chrs.get(current_seq_id);
                        if (!chrTrack.containsKey(current_seq_id) && trackLine != null) {
                            chrTrack.put(current_seq_id, true);
                            bw.write(trackLine + "\n");
                        }
                        break;

                    case VARSTEP:
                        if (fields.length < 2) {
                            throw new IllegalArgumentException("Wiggle format error: Improper " + current_format + " line: " + line);
                        }
                        current_start = Integer.parseInt(fields[0]) - 1;
                        length = current_start + current_span;
                        break;

                    case FIXEDSTEP:
                        if (fields.length < 1) {
                            throw new IllegalArgumentException("Wiggle format error: Improper " + current_format + " line: " + line);
                        }
                        length = current_start + current_span - 1;
                        current_start += current_step;
                        break;
                }

                bw.write(line + "\n");
                if (length > chrLength.get(current_seq_id)) {
                    chrLength.put(current_seq_id, length);
                }
            }

            return !thread.isInterrupted();
        } catch (Exception ex) {
            throw ex;
        } finally {
            for (BufferedWriter b : chrs.values()) {
                try {
                    b.flush();
                } catch (IOException ex) {
                    Logger.getLogger(Wiggle.class.getName()).log(Level.SEVERE, null, ex);
                }
                GeneralUtils.safeClose(b);
            }
            GeneralUtils.safeClose(br);
            GeneralUtils.safeClose(bw);
        }
    }

    //Saves graph (.bedgraph) (.gr) files. 
    //Modified in IGBF-1090<JDaly, DKalkarni> to be one function instead of two.
    @Override
    public boolean writeAnnotations(Collection<? extends SeqSymmetry> syms, BioSeq seq, String genome_version, OutputStream outstream) throws IOException {

        try {
            writeBedFormat(syms, genome_version, outstream);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public SeqSpan getSpan(String line) {
        return null; // not used yet
    }

    public boolean processInfoLine(String line, List<String> infoLines) {
        return false; // not used yet
    }

    @Override
        public boolean isMultiThreadOK() {
        return true;
    }

}
