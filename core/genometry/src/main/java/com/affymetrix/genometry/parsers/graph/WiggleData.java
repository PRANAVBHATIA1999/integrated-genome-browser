package com.affymetrix.genometry.parsers.graph;

import cern.colt.GenericSorting;
import cern.colt.Swapper;
import cern.colt.function.IntComparator;
import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.symmetry.impl.GraphIntervalSym;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import java.util.Arrays;

/**
 * A class used to temporarily hold data during processing of Wiggle-format files.
 */
public final class WiggleData {

    private final IntArrayList xData;
    private final FloatArrayList yData;
    private final IntArrayList wData;

    private final String seq_id;

    public WiggleData(String seq_id) {
        this.xData = new IntArrayList();
        this.yData = new FloatArrayList();
        this.wData = new IntArrayList();
        this.seq_id = seq_id;
    }

    /**
     * Creates a GraphSym from the stored data, or returns null if no data
     * has been stored yet.
     */
    public GraphSym createGraph(GenomeVersion seq_group, String graph_id, String uri) {
        if (xData.isEmpty()) {
            return null;
        }

        int dataSize = xData.size();

        // Make an array copy of the data elements (not a clone, because that might be larger).
        int[] xList = Arrays.copyOf(xData.elements(), dataSize);
        xData.clear();
        float[] yList = Arrays.copyOf(yData.elements(), dataSize);
        yData.clear();
        int[] wList = Arrays.copyOf(wData.elements(), dataSize);
        wData.clear();

        sortXYZDataOnX(xList, yList, wList);

        int largest_x = xList[dataSize - 1] + wList[dataSize - 1];
        BioSeq seq = seq_group.addSeq(seq_id, largest_x, uri);

        return new GraphIntervalSym(xList, wList, yList, graph_id, seq);
    }

    /**
     * Create BioSeq
     */
    public BioSeq getBioSeq() {
        if (xData.isEmpty()) {
            return null;
        }

        int dataSize = xData.size();

        // Make an array copy of the data elements (not a clone, because that might be larger).
        int[] xList = Arrays.copyOf(xData.elements(), dataSize);
        xData.clear();
        float[] yList = Arrays.copyOf(yData.elements(), dataSize);
        yData.clear();
        int[] wList = Arrays.copyOf(wData.elements(), dataSize);
        wData.clear();

        sortXYZDataOnX(xList, yList, wList);

        int largest_x = xList[dataSize - 1] + wList[dataSize - 1];

        return (new BioSeq(seq_id, largest_x));
    }

    /**
     * Sort xList, yList, and wList based upon xList
     *
     * @param xList
     * @param yList
     * @param wList
     */
    public static void sortXYZDataOnX(final int[] xList, final float[] yList, final int[] wList) {
        Swapper swapper = (a, b) -> {
            int swapInt = xList[a];
            xList[a] = xList[b];
            xList[b] = swapInt;

            swapInt = wList[a];
            wList[a] = wList[b];
            wList[b] = swapInt;

            float swapFloat = yList[a];
            yList[a] = yList[b];
            yList[b] = swapFloat;
        };
        IntComparator comp = (a, b) -> ((Integer) xList[a]).compareTo(xList[b]);
        GenericSorting.quickSort(0, xList.length, comp, swapper);
    }

    public void add(int x, float y, int w) {
        xData.add(x);
        yData.add(y);
        wData.add(w);
    }

    public boolean isEmpty() {
        return xData.isEmpty();
    }
}
