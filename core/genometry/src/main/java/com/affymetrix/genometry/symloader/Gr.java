/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometry.symloader;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.parsers.AnnotationWriter;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometry.util.LocalUrlCacher;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hiralv
 */
public final class Gr extends SymLoader implements AnnotationWriter {

    private File f;
    private boolean isSorted = false;
    private File tempFile = null;
    private static final String UNNAMED = "unnamed";
    private BioSeq unnamed;

    private static final List<LoadStrategy> strategyList = new ArrayList<>();

    static {
        strategyList.add(LoadStrategy.NO_LOAD);
        strategyList.add(LoadStrategy.VISIBLE);
//		strategyList.add(LoadStrategy.CHROMOSOME);
    }

    public Gr() {
        super(null, Optional.empty(), null, null);
    }

    public Gr(URI uri, Optional<URI> indexUri, String featureName, GenomeVersion seq_group) {
        super(uri, indexUri, featureName, seq_group);
    }

    @Override
    public void init() throws Exception {
        if (this.isInitialized) {
            return;
        }
        super.init();
        this.f = LocalUrlCacher.convertURIToFile(uri);
        sort();
    }

    private void sort() throws Exception {
        unnamed = genomeVersion.getSeq(UNNAMED);

        if (unnamed == null) {
            unnamed = new BioSeq(UNNAMED, 0);
        }

        GraphSym sym = parse(unnamed, Integer.MIN_VALUE, Integer.MAX_VALUE);

        if (!isSorted) {
            FileOutputStream fos = null;
            try {
                tempFile = File.createTempFile(f.getName(), ".gr");
                tempFile.deleteOnExit();
                fos = new FileOutputStream(tempFile);
                writeGrFormat(sym, fos);
            } catch (Exception ex) {
                throw ex;
            } finally {
                GeneralUtils.safeClose(fos);
            }
        }
        isSorted = true;

        if (unnamed.getLength() < sym.getMaxXCoord()) {
            unnamed.setLength(sym.getMaxXCoord());
        }

    }

    @Override
    public List<BioSeq> getChromosomeList() throws Exception {
        init();
        List<BioSeq> seqs = genomeVersion.getSeqList();
        if (!seqs.isEmpty()) {
            return seqs;
        }

        seqs = new ArrayList<>();
        seqs.add(unnamed);

        return seqs;
    }

    @Override
    public List<LoadStrategy> getLoadChoices() {
        return strategyList;
    }

    @Override
    public List<GraphSym> getGenome() throws Exception {
        init();
        BioSeq seq = genomeVersion.addSeq(this.featureName, Integer.MAX_VALUE - 1, uri.toString());
        return getChromosome(seq);
    }

    @Override
    public List<GraphSym> getChromosome(BioSeq seq) throws Exception {
        init();
        List<GraphSym> results = new ArrayList<>();
        results.add(parse(seq, seq.getMin(), seq.getMax() + 1));
        return results;
    }

    @Override
    public List<GraphSym> getRegion(SeqSpan span) throws Exception {
        init();
        List<GraphSym> results = new ArrayList<>();
        results.add(parse(span.getBioSeq(), span.getMin(), span.getMax() + 1));
        return results;
    }

    public String getMimeType() {
        return "text/gr";
    }

    public static boolean writeGrFormat(GraphSym graf, OutputStream ostr) throws IOException {
        BufferedOutputStream bos = null;
        DataOutputStream dos = null;
        try {
            bos = new BufferedOutputStream(ostr);
            dos = new DataOutputStream(bos);
            writeGraphPoints(graf, dos);
        } finally {
            GeneralUtils.safeClose(dos);
        }
        return true;
    }

    private static void writeGraphPoints(GraphSym graf, DataOutputStream dos) throws IOException {
        int total_points = graf.getPointCount();
        for (int i = 0; i < total_points; i++) {
            dos.writeBytes("" + graf.getGraphXCoord(i) + "\t"
                    + graf.getGraphYCoordString(i) + "\n");
        }
    }

    private GraphSym parse(BioSeq aseq, int min, int max) throws Exception {
        GraphSym graf = null;
        String line = null;
        String headerstr = null;
        String name = this.uri.toString();
        boolean hasHeader = false;
        int count = 0;

        IntArrayList xlist = new IntArrayList();
        FloatArrayList ylist = new FloatArrayList();

        FileInputStream fis = null;
        InputStream is = null;
        BufferedReader br = null;

        try {

            if (tempFile != null) {
                fis = new FileInputStream(tempFile);
            } else {
                fis = new FileInputStream(this.f);
            }

            is = GeneralUtils.unzipStream(fis, f.getName(), new StringBuffer());
            br = new BufferedReader(new InputStreamReader(is));
            // check first line, may be a header for column labels...
            line = br.readLine();
            if (line == null) {
                System.out.println("can't find any data in file!");
                return null;
            }

            try {
                int firstx;
                float firsty;
                if (line.indexOf(' ') > 0) {
                    firstx = Integer.parseInt(line.substring(0, line.indexOf(' ')));
                    firsty = Float.parseFloat(line.substring(line.indexOf(' ') + 1));
                } else if (line.indexOf('\t') > 0) {
                    firstx = Integer.parseInt(line.substring(0, line.indexOf('\t')));
                    firsty = Float.parseFloat(line.substring(line.indexOf('\t') + 1));
                } else {
                    System.out.println("format not recognized");
                    return null;
                }
                if (!(firstx < min || firstx >= max)) {
                    xlist.add(firstx);
                    ylist.add(firsty);
                    count++;  // first line parses as numbers, so is not a header, increment count
                }
            } catch (Exception ex) {
                // if first line does not parse as numbers, must be a header...
                // set header flag, don't count as a line...
                headerstr = line;
                System.out.println("Found header on graph file: " + line);
                hasHeader = true;
            }
            int x = 0;
            float y = 0;
            int xprev = Integer.MIN_VALUE;
            boolean sorted = true;
            while ((line = br.readLine()) != null) {
                int indexOfDelimiter = line.indexOf(' ');
                if (indexOfDelimiter > 0) {
                    x = Integer.parseInt(line.substring(0, indexOfDelimiter));
                    y = Float.parseFloat(line.substring(indexOfDelimiter + 1));
                } else {
                    indexOfDelimiter = line.indexOf('\t');
                    if (indexOfDelimiter > 0) {
                        x = Integer.parseInt(line.substring(0, indexOfDelimiter));
                        y = Float.parseFloat(line.substring(indexOfDelimiter + 1));
                    } else {
                        Logger.getLogger(Gr.class.getName()).log(
                                Level.WARNING, "Line {0} doesn''t match... ignoring", line);
                        continue;
                    }
                }

                if (x >= max) {
                    if (isSorted) {
                        break;
                    } else {
                        continue;
                    }
                }

                if (x < min) {
                    continue;
                }

                xlist.add(x);
                ylist.add(y);
                count++;
                // checking on whether graph is sorted...
                if (!isSorted) {
                    if (xprev > x) {
                        sorted = false;
                    } else {
                        xprev = x;
                    }
                }

            }
            isSorted = sorted;

            graf = createResults(name, hasHeader, headerstr, xlist, ylist, sorted, aseq);

            System.out.println("loaded graph data, total points = " + count);

            return graf;
        } catch (Exception ex) {
            throw ex;
        } finally {
            GeneralUtils.safeClose(br);
        }

    }

    private GraphSym createResults(
            String name, boolean hasHeader, String headerstr,
            IntArrayList xlist, FloatArrayList ylist,
            boolean sorted, BioSeq aseq) {
        GraphSym graf;
        if (name == null && hasHeader) {
            name = headerstr;
        }
        int[] xcoords = Arrays.copyOf(xlist.elements(), xlist.size());
        xlist = null;
        float[] ycoords = Arrays.copyOf(ylist.elements(), ylist.size());
        ylist = null;
        if (!sorted) {
            System.err.println("input graph not sorted, sorting by base coord");
            sortXYDataOnX(xcoords, ycoords);
        }
        graf = new GraphSym(xcoords, ycoords, name, aseq);
        return graf;
    }

    /**
     * Sort xList, yList, and wList based upon xList
     */
    private static void sortXYDataOnX(final int[] xList, final float[] yList) {
        Swapper swapper = (a, b) -> {
            int swapInt = xList[a];
            xList[a] = xList[b];
            xList[b] = swapInt;

            float swapFloat = yList[a];
            yList[a] = yList[b];
            yList[b] = swapFloat;
        };
        IntComparator comp = (a, b) -> ((Integer) xList[a]).compareTo(xList[b]);
        GenericSorting.quickSort(0, xList.length, comp, swapper);
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
                writeGraphPoints(graf, dos);
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
