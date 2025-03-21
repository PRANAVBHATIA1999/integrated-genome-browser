/**
 * Copyright (c) 2006-2007 Affymetrix, Inc.
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

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.GeneralUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BgrParser implements GraphParser {

    /**
     * Writes bgr format.
     * <pre>
     *.bgr format:
     *  Old Header:
     *    UTF-8 encoded seq name
     *    UTF-8 encoded seq version
     *    4-byte int for total number of data points
     *  New Header:
     *    UTF-8 encoded:
     *       seq_name
     *       release_name (seq version)
     *       analysis_group_name
     *       map_analysis_group_name
     *       method_name
     *       parameter_set_name
     *       value_type_name
     *       control_group_name
     *    4-byte int for total number of data points
     *  Then for each data point:
     *    4-byte int for base position
     *    4-byte float for value
     * </pre>
     */
    public static boolean writeBgrFormat(GraphSym graf, OutputStream ostr)
            throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(ostr);
        try (DataOutputStream dos = new DataOutputStream(bos)) {
            Map<String, Object> headers = graf.getProperties();

            if (headers == null) {
                headers = new HashMap<>(); // use an empty map
            }
            if (headers.get("seq_name") == null) {
                if (graf.getGraphSeq() == null) {
                    dos.writeUTF("null");
                } else {
                    dos.writeUTF(graf.getGraphSeq().getId());
                }
            } else {
                dos.writeUTF((String) headers.get("seq_name"));
            }
            if (headers.get("release_name") == null) {
                dos.writeUTF("null");
            } else {
                dos.writeUTF((String) headers.get("release_name"));
            }
            if (headers.get("analysis_group_name") == null) {
                dos.writeUTF("null");
            } else {
                dos.writeUTF((String) headers.get("analysis_group_name"));
            }
            if (headers.get("map_analysis_group_name") == null) {
                dos.writeUTF("null");
            } else {
                dos.writeUTF((String) headers.get("map_analysis_group_name"));
            }
            if (headers.get("method_name") == null) {
                dos.writeUTF("null");
            } else {
                dos.writeUTF((String) headers.get("method_name"));
            }
            if (headers.get("parameter_set_name") == null) {
                dos.writeUTF("null");
            } else {
                dos.writeUTF((String) headers.get("parameter_set_name"));
            }
            if (headers.get("value_type_name") == null) {
                dos.writeUTF("null");
            } else {
                dos.writeUTF((String) headers.get("value_type_name"));
            }
            if (headers.get("control_group_name") == null) {
                dos.writeUTF("null");
            } else {
                dos.writeUTF((String) headers.get("control_group_name"));
            }
            writeGraphPoints(graf, dos);
        }
        return true;
    }

    public static List<GraphSym> parse(InputStream istr, String stream_name, GenomeVersion seq_group) throws IOException {
        List<GraphSym> results = new ArrayList<>();
        results.add(parse(istr, stream_name, seq_group, true));
        return results;
    }

    public static GraphSym parse(InputStream istr, String stream_name, GenomeVersion seq_group, boolean ensure_unique_id)
            throws IOException {
        int count = 0;
        BufferedInputStream bis = new BufferedInputStream(istr);
        DataInputStream dis = new DataInputStream(bis);
        HashMap<String, Object> props = new HashMap<>();
        String seq_name = dis.readUTF();
        String release_name = dis.readUTF();
        String analysis_group_name = dis.readUTF();
        System.out.println(seq_name + ", " + release_name + ", " + analysis_group_name);
        String map_analysis_group_name = dis.readUTF();
        String method_name = dis.readUTF();
        String parameter_set_name = dis.readUTF();
        String value_type_name = dis.readUTF();
        String control_group_name = dis.readUTF();
        props.put("seq_name", seq_name);
        props.put("release_name", release_name);
        props.put("analysis_group_name", analysis_group_name);
        props.put("map_analysis_group_name", map_analysis_group_name);
        props.put("method_name", method_name);
        props.put("parameter_set_name", parameter_set_name);
        props.put("value_type_name", value_type_name);
        props.put("control_group_name", control_group_name);

        int total_points = dis.readInt();
        int[] xcoords = new int[total_points];
        float[] ycoords = new float[total_points];
        int largest_x = 0; // assume the x-values are sorted, so the max is the last one read.
        Thread thread = Thread.currentThread();
        for (int i = 0; i < total_points && !thread.isInterrupted(); i++) {
            largest_x = xcoords[i] = dis.readInt();
            ycoords[i] = dis.readFloat();
            count++;
        }

        BioSeq seq = seq_group.getSeq(seq_name);
        if (seq == null) {
            seq = seq_group.addSeq(seq_name, largest_x, stream_name);
        }

        StringBuffer sb = new StringBuffer();
        append(sb, analysis_group_name);
        append(sb, value_type_name);
        append(sb, parameter_set_name);

        String graph_name;
        if (sb.length() == 0) {
            graph_name = stream_name;
        } else {
            graph_name = sb.toString();
        }

        // need to replace seq_name with name of graph (some combo of group name and conditions...)
        if (ensure_unique_id) {
            graph_name = GenomeVersion.getUniqueGraphID(graph_name, seq);
        }
        GraphSym graf = new GraphSym(xcoords, ycoords, graph_name, seq);
        graf.setProperties(props);
        return graf;
    }

    static void append(StringBuffer sb, String s) {
        if (s != null && !"null".equals(s) && s.trim().length() > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s);
        }
    }

    private static void writeGraphPoints(GraphSym graf, DataOutputStream dos) throws IOException {
        int total_points = graf.getPointCount();
        dos.writeInt(total_points);
        for (int i = 0; i < total_points; i++) {
            dos.writeInt(graf.getGraphXCoord(i));
            dos.writeFloat(graf.getGraphYCoord(i));
        }
    }

    @Override
    public List<? extends SeqSymmetry> parse(InputStream is,
            GenomeVersion genomeVersion, String nameType, String uri,
            boolean annotate_seq) throws Exception {
        // only annotate_seq = false processed here
        return parse(is, uri, genomeVersion);
    }

    @Override
    public List<GraphSym> readGraphs(InputStream istr, String stream_name,
            GenomeVersion seq_group, BioSeq seq) throws IOException {
        StringBuffer stripped_name = new StringBuffer();
        InputStream newstr = GeneralUtils.unzipStream(istr, stream_name, stripped_name);
        return GraphParserUtil.getInstance().wrapInList(BgrParser.parse(newstr, stream_name, seq_group, true));
    }

    @Override
    public void writeGraphFile(GraphSym gsym, GenomeVersion seq_group,
            String file_name) throws IOException {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file_name));
            BgrParser.writeBgrFormat(gsym, bos);
        } finally {
            GeneralUtils.safeClose(bos);
        }
    }
}
