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
package com.affymetrix.genometry.parsers.graph;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.GeneralUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GrParser implements GraphParser {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GrParser.class);

    private GenometryModel gmodel;

    public GrParser() {
        gmodel = GenometryModel.getInstance();
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

    public static GraphSym parse(InputStream istr, BioSeq aseq, String name) throws IOException {
        return parse(istr, aseq, name, true);
    }

    public static GraphSym parse(InputStream istr, BioSeq aseq, String name, boolean ensure_unique_id)
            throws IOException {
        GraphSym graf = null;
        String line = null;
        String headerstr = null;
        boolean hasHeader = false;
        int count = 0;

        IntArrayList xlist = new IntArrayList();
        FloatArrayList ylist = new FloatArrayList();

        InputStreamReader isr = new InputStreamReader(istr);
        BufferedReader br = new BufferedReader(isr);
        // check first line, may be a header for column labels...
        line = br.readLine();
        if (line == null) {
            logger.warn("Can't find data.");
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
                logger.warn("Format not recognized");
                return null;
            }
            xlist.add(firstx);
            ylist.add(firsty);
            count++;  // first line parses as numbers, so is not a header, increment count
        } catch (Exception ex) {
            // if first line does not parse as numbers, must be a header...
            // set header flag, don't count as a line...
            headerstr = line;
            logger.debug("Found header on graph file: " + line);
            hasHeader = true;
        }
        int x = 0;
        float y = 0;
        int xprev = Integer.MIN_VALUE;
        boolean sorted = true;
        while ((line = br.readLine()) != null) {
            if (line.indexOf(' ') > 0) {
                x = Integer.parseInt(line.substring(0, line.indexOf(' ')));
                y = Float.parseFloat(line.substring(line.indexOf(' ') + 1));
            } else if (line.indexOf('\t') > 0) {
                x = Integer.parseInt(line.substring(0, line.indexOf('\t')));
                y = Float.parseFloat(line.substring(line.indexOf('\t') + 1));
            }
            xlist.add(x);
            ylist.add(y);
            count++;
            // checking on whether graph is sorted...
            if (xprev > x) {
                sorted = false;
            }
            xprev = x;
        }
        if (name == null && hasHeader) {
            name = headerstr;
        }
        int xcoords[] = Arrays.copyOf(xlist.elements(), xlist.size());
        xlist = null;
        float ycoords[] = Arrays.copyOf(ylist.elements(), ylist.size());
        ylist = null;

        if (!sorted) {
            logger.info("Graph not sorted. Sorting by base coord");
            sortXYDataOnX(xcoords, ycoords);
        }
        if (ensure_unique_id) {
            name = GenomeVersion.getUniqueGraphID(name, aseq);
        }
        graf = new GraphSym(xcoords, ycoords, name, aseq);
        logger.info("Loaded graph data, total points = " + count);
        return graf;
    }

    /**
     * Sort xList, yList, and wList based upon xList.
     */
    public static void sortXYDataOnX(final int[] xList, final float[] yList) {
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

    @Override
    public List<? extends SeqSymmetry> parse(InputStream is,
            GenomeVersion genomeVersion, String nameType, String uri,
            boolean annotate_seq) throws Exception {
        throw new IllegalStateException("Gr should not be processed here");
    }

    @Override
    public List<GraphSym> readGraphs(InputStream istr, String stream_name,
            GenomeVersion seq_group, BioSeq seq) throws IOException {
        StringBuffer stripped_name = new StringBuffer();
        InputStream newstr = GeneralUtils.unzipStream(istr, stream_name, stripped_name);
        if (seq == null) {
            seq = gmodel.getSelectedSeq().orElse(null);
        }
        // If this is a newly-created seq group, then go ahead and add a new
        // unnamed seq to it if necessary.
        if (seq_group.getSeqCount() == 0) {
            seq = seq_group.addSeq("unnamed", 1000);
        }
        if (seq == null) {
            throw new IOException("Must select a sequence before loading a graph of type 'gr'");
        }
        GraphSym graph = GrParser.parse(newstr, seq, stream_name);
        int max_x = graph.getMaxXCoord();
        BioSeq gseq = graph.getGraphSeq();
        seq_group.addSeq(gseq.getId(), max_x); // this stretches the seq to hold the graph
        return GraphParserUtil.getInstance().wrapInList(graph);
    }

    @Override
    public void writeGraphFile(GraphSym gsym, GenomeVersion seq_group,
            String file_name) throws IOException {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file_name));
            GrParser.writeGrFormat(gsym, bos);
        } finally {
            GeneralUtils.safeClose(bos);
        }
    }
}
