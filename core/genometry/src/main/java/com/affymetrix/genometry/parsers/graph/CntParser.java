/**
 * Copyright (c) 2007 Affymetrix, Inc.
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

import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.parsers.Parser;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for textual ".cnt" files from the Affymetrix CNAT program.
 */
public final class CntParser implements Parser {

    private static final Pattern tag_val = Pattern.compile("(.*)=(.*)");
    private static final Pattern line_regex = Pattern.compile("\\t");
    private static final Pattern section_regex = Pattern.compile("\\[.*\\]");
    private static final String SECTION_HEADER = "[Header]";
    private static final String SECTION_COL_NAME = "[ColumnName]";
    // index of the first column containing data
    private static final int FIRST_DATA_COLUMN = 3;
    private final Map<String, FloatArrayList[]> seq2Floats = new HashMap<>();
    private final Map<String, IntArrayList> seqToIntList = new HashMap<>();

    private final Map<String, String> unique_gids = new HashMap<>();

    public List<GraphSym> parse(InputStream dis, GenomeVersion seq_group, boolean annotateSeq)
            throws IOException {

        String line;

        Thread thread = Thread.currentThread();
        BufferedReader reader = new BufferedReader(new InputStreamReader(dis));

        Matcher section_regex_matcher = section_regex.matcher("");
        Matcher tag_val_matcher = tag_val.matcher("");
        String current_section = "";
        Map<String, Object> headerData = new HashMap<>();
        List<GraphSym> results = new ArrayList<>();

        // First read the header
        while ((line = reader.readLine()) != null && (!thread.isInterrupted())) {
            section_regex_matcher.reset(line);
            if (section_regex_matcher.matches()) {
                current_section = line;
                if (SECTION_HEADER.equals(current_section)) {
                    continue;
                } else {
                    break;
                }
            }

            if (SECTION_HEADER.equals(current_section)) {
                tag_val_matcher.reset(line);
                if (tag_val_matcher.matches()) {
                    String tag = tag_val_matcher.group(1);
                    String val = tag_val_matcher.group(2);
                    headerData.put(tag, val);
                }
            } else {
                break; // finished with header, move to next section
            }
        }

        String[] column_names = null;

        while ((line = reader.readLine()) != null && (!thread.isInterrupted())) {
            section_regex_matcher.reset(line);
            if (section_regex_matcher.matches()) {
                current_section = line;
                if (SECTION_COL_NAME.equals(current_section)) {
                    continue;
                } else {
                    break;
                }
            }

            if (SECTION_COL_NAME.equals(current_section)) {
                column_names = line_regex.split(line);
            } else {
                break; // finished section, move to next section
            }
        }

        if (column_names == null) {
            throw new IOException("Column names were missing or malformed");
        }

        int numScores = column_names.length - FIRST_DATA_COLUMN;
        if (numScores < 1) {
            throw new IOException("No score columns in file");
        }

        while ((line = reader.readLine()) != null && (!thread.isInterrupted())) {

            String[] fields = line_regex.split(line);
            int field_count = fields.length;

            if (field_count != column_names.length) {
                throw new IOException("Line has wrong number of data columns.");
            }

            String seqid = fields[1];
            int x = Integer.parseInt(fields[2]);

            BioSeq aseq = seq_group.getSeq(seqid);
            if (aseq == null) {
                aseq = seq_group.addSeq(seqid, x);
            }
            if (x > aseq.getLength()) {
                aseq.setLength(x);
            }

            IntArrayList xVals = getXCoordsForSeq(aseq);
            xVals.add(x);

            FloatArrayList[] floats = getFloatsForSeq(aseq, numScores);
            for (int j = 0; j < numScores; j++) {
                FloatArrayList floatList = floats[j];
                float floatVal = parseFloat(fields[FIRST_DATA_COLUMN + j]);
                floatList.add(floatVal);
            }
        }   // end of line-reading loop

        for (Map.Entry<String, IntArrayList> entry : seqToIntList.entrySet()) {
            String seqid = entry.getKey();
            IntArrayList x = entry.getValue();
            x.trimToSize();
            FloatArrayList[] ys = seq2Floats.get(seqid);
            BioSeq seq = seq_group.getSeq(seqid);
            for (int i = 0; i < ys.length; i++) {
                FloatArrayList y = ys[i];
                y.trimToSize();
                String id = column_names[i + FIRST_DATA_COLUMN];
                if ("ChipNum".equals(id)) {
                    continue;
                }
                id = getGraphIdForColumn(id, seq_group);
                GraphSym graf = new GraphSym(x.elements(), y.elements(), id, seq);
                if (annotateSeq) {
                    seq.addAnnotation(graf);
                }
                results.add(graf);
            }
        }
        return results;

    }

    String getGraphIdForColumn(String column_id, GenomeVersion seq_group) {
        String gid = unique_gids.get(column_id);
        if (gid == null) {
            gid = GenomeVersion.getUniqueGraphID(column_id, seq_group);
            unique_gids.put(column_id, gid);
        }
        return gid;
    }

    public static float parseFloat(String s) {
        float val = 0.0f;
        try {
            val = Float.parseFloat(s);
        } catch (NumberFormatException nfe) {
            val = 0.0f;
        }
        return val;
    }

    FloatArrayList[] getFloatsForSeq(BioSeq seq, int numScores) {
        FloatArrayList[] floats = seq2Floats.get(seq.getId());

        if (floats == null) {
            floats = new FloatArrayList[numScores];
            for (int i = 0; i < numScores; i++) {
                floats[i] = new FloatArrayList();
            }
            seq2Floats.put(seq.getId(), floats);
        }

        return floats;
    }

    IntArrayList getXCoordsForSeq(BioSeq seq) {
        IntArrayList xcoords = seqToIntList.get(seq.getId());

        if (xcoords == null) {
            xcoords = new IntArrayList();
            seqToIntList.put(seq.getId(), xcoords);
        }

        return xcoords;
    }

    @Override
    public List<? extends SeqSymmetry> parse(InputStream is,
            GenomeVersion genomeVersion, String nameType, String uri,
            boolean annotate_seq) throws Exception {
        return parse(is, genomeVersion, annotate_seq);
    }
}
