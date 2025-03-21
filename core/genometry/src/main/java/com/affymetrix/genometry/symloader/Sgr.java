package com.affymetrix.genometry.symloader;

import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.comparator.BioSeqComparator;
import com.affymetrix.genometry.parsers.AnnotationWriter;
import com.affymetrix.genometry.parsers.graph.GrParser;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.LoadUtils.LoadStrategy;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
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

public final class Sgr extends SymLoader implements AnnotationWriter {

    private static final Pattern line_regex = Pattern.compile("\\s+");  // replaced single tab with one or more whitespace

    private static final List<LoadStrategy> strategyList = new ArrayList<>();

    static {
        strategyList.add(LoadStrategy.NO_LOAD);
        strategyList.add(LoadStrategy.VISIBLE);
//		strategyList.add(LoadStrategy.CHROMOSOME);
        strategyList.add(LoadStrategy.GENOME);
    }

    public Sgr(URI uri, Optional<URI> indexUri, String featureName, GenomeVersion seq_group) {
        super(uri, indexUri, featureName, seq_group);
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
    public List<LoadStrategy> getLoadChoices() {
        return strategyList;
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
        return parse(seq, seq.getMin(), seq.getMax() + 1); //interbase format
    }

    @Override
    public List<GraphSym> getRegion(SeqSpan span) throws Exception {
        init();
        return parse(span.getBioSeq(), span.getMin(), span.getMax() + 1); //interbaseformat
    }

    public String getMimeType() {
        return "text/sgr";
    }

    private List<GraphSym> parse(BioSeq seq, int min, int max) throws Exception {
        List<GraphSym> results = new ArrayList<>();
        IntArrayList xlist = new IntArrayList();
        FloatArrayList ylist = new FloatArrayList();
        BufferedReader br = null;
        FileOutputStream fos = null;

        try {
            File file = chrList.get(seq);
            if (file == null) {
                Logger.getLogger(Sgr.class.getName()).log(Level.FINE, "Could not find chromosome {0}", seq.getId());
                return Collections.<GraphSym>emptyList();
            }

            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            // Using whole file path so probably not needed : HV 02/20/12
            // Making sure the ID is unique on the seq
            // will make sure the GraphState is also unique on the seq.
            // String gid = GenomeVersion.getUniqueGraphTrackID(uri.toString(), this.featureName);
            boolean sort = parseLines(br, xlist, ylist, min, max, !file.canWrite());

            GraphSym sym = createResults(xlist, seq, ylist, uri.toString(), sort);

            results.add(sym);

            if (sort) {
                fos = new FileOutputStream(file);
                writeSgrFormat(sym, fos);
            }

            file.setReadOnly();

            return results;
        } catch (Exception ex) {
            throw ex;
        } finally {
            GeneralUtils.safeClose(br);
            GeneralUtils.safeClose(fos);
        }

    }

    /**
     * Parse the lines in the one-chromosome stream.
     *
     * @param br
     * @param xlist
     * @param ylist
     * @param min
     * @param max
     * @param sorted
     * @return boolean indicating whether we need to sort the data
     * @throws IOException
     * @throws NumberFormatException
     */
    private static boolean parseLines(
            BufferedReader br, IntArrayList xlist, FloatArrayList ylist, int min, int max, boolean sorted)
            throws IOException, NumberFormatException {
        String[] fields;
        String line;
        int x = 0;
        float y = 0.0f;
        int prevx = 0;
        boolean sort = false;

        while ((line = br.readLine()) != null) {
            if (line.length() == 0 || line.charAt(0) == '#' || line.charAt(0) == '%') {
                continue;
            }
            fields = line_regex.split(line);
            if (fields == null || fields.length == 0) {
                continue;
            }

            x = Integer.parseInt(fields[1]);

            if (x >= max) {
                // only look in range
                if (sorted) {
                    break;	// if data is sorted on x, no further points will be < max
                }
                continue;
            }

            if (x < min) {
                // only look in range
                continue;
            }

            y = Float.parseFloat(fields[2]);
            xlist.add(x);
            ylist.add(y);

            if (!sorted) {
                if (prevx > x) {
                    sort = true;
                } else {
                    prevx = x;
                }
            }
        }

        return sort;
    }

    public static boolean writeSgrFormat(GraphSym graf, OutputStream ostr) throws IOException {
        BioSeq seq = graf.getGraphSeq();
        if (seq == null) {
            throw new IOException("You cannot use the '.sgr' format when the sequence is unknown. Use '.gr' instead.");
        }
        String seq_id = seq.getId();

        BufferedOutputStream bos = null;
        DataOutputStream dos = null;
        try {
            bos = new BufferedOutputStream(ostr);
            dos = new DataOutputStream(bos);
            writeGraphPoints(graf, dos, seq_id);
        } finally {
            GeneralUtils.safeClose(bos);
            GeneralUtils.safeClose(dos);
        }
        return true;
    }

    private static void writeGraphPoints(GraphSym graf, DataOutputStream dos, String seq_id) throws IOException {
        int total_points = graf.getPointCount();
        for (int i = 0; i < total_points; i++) {
            dos.writeBytes(seq_id + "\t" + graf.getGraphXCoord(i) + "\t"
                    + graf.getGraphYCoordString(i) + "\n");
        }
    }

    private static GraphSym createResults(
            IntArrayList xlist, BioSeq aseq, FloatArrayList ylist, String gid, boolean sort) {
        int[] xcoords = Arrays.copyOf(xlist.elements(), xlist.size());
        xlist = null;
        float[] ycoords = Arrays.copyOf(ylist.elements(), ylist.size());
        ylist = null;

        if (sort) {
            GrParser.sortXYDataOnX(xcoords, ycoords);
        }

        return new GraphSym(xcoords, ycoords, gid, aseq);
    }

    /**
     * Parse lines in the arbitrary-chromosome stream.
     *
     * @param istr
     * @param chrLength
     * @param chrFiles
     */
    @Override
    protected boolean parseLines(InputStream istr, Map<String, Integer> chrLength, Map<String, File> chrFiles) throws Exception {
        Map<String, BufferedWriter> chrs = new HashMap<>();
        BufferedReader br = null;
        BufferedWriter bw = null;
        String[] fields = null;
        String line, seqid;
        int x;

        try {
            br = new BufferedReader(new InputStreamReader(istr));
            Thread thread = Thread.currentThread();
            while ((line = br.readLine()) != null && !thread.isInterrupted()) {
                if (line.length() == 0 || line.charAt(0) == '#' || line.charAt(0) == '%') {
                    continue;
                }

                fields = line_regex.split(line);
                seqid = fields[0];
                x = Integer.parseInt(fields[1]);

                if (!chrs.containsKey(seqid)) {
                    addToLists(chrs, seqid, chrFiles, chrLength, ".sgr");
                }

                if (x > chrLength.get(seqid)) {
                    chrLength.put(seqid, x);
                }

                bw = chrs.get(seqid);
                bw.write(line + "\n");
            }
            return !thread.isInterrupted();
        } catch (Exception ex) {
            throw ex;
        } finally {
            for (BufferedWriter b : chrs.values()) {
                try {
                    b.flush();
                } catch (IOException ex) {
                    Logger.getLogger(Sgr.class.getName()).log(Level.SEVERE, null, ex);
                }
                GeneralUtils.safeClose(b);
            }
            GeneralUtils.safeClose(bw);
            GeneralUtils.safeClose(br);
        }
    }

    public boolean writeAnnotations(Collection<? extends SeqSymmetry> syms, BioSeq seq, String type, OutputStream ostr) throws IOException {
        BufferedOutputStream bos = null;
        DataOutputStream dos = null;
        try {
            bos = new BufferedOutputStream(ostr);
            dos = new DataOutputStream(bos);

            Iterator<? extends SeqSymmetry> iter = syms.iterator();
            for (GraphSym graf; iter.hasNext();) {
                graf = (GraphSym) iter.next();
                writeGraphPoints(graf, dos, graf.getGraphSeq().getId());
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            GeneralUtils.safeClose(dos);
        }

        return false;
    }

}
