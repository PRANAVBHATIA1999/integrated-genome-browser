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

import cern.colt.list.FloatArrayList;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.parsers.Parser;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.symmetry.impl.IndexedSingletonSym;
import com.affymetrix.genometry.symmetry.impl.ScoredContainerSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SimpleSymWithProps;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This class (and file format) have been replaced by ScoredIntervalParser (and sin file format)
 * Kept now only to parse in older data files.
 */
public final class ScoredMapParser implements Parser {

    static Pattern line_regex = Pattern.compile("\t");

    private GenometryModel gmodel;

    public ScoredMapParser() {
        gmodel = GenometryModel.getInstance();
    }

    public void parse(InputStream istr, String stream_name, BioSeq aseq, GenomeVersion seq_group) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(istr));
            String line = null;

            String unique_container_name = GenomeVersion.getUniqueGraphID(stream_name, seq_group);
            ScoredContainerSym parent = new ScoredContainerSym();
            parent.setID(unique_container_name);
            parent.addSpan(new SimpleSeqSpan(0, aseq.getLength(), aseq));
            parent.setProperty("method", stream_name);
            parent.setProperty(SimpleSymWithProps.CONTAINER_PROP, Boolean.TRUE);

            // assuming first line is header
            line = br.readLine();
            String[] headers = line_regex.split(line);
            List<String> score_names = new ArrayList<>();
            List<FloatArrayList> score_arrays = new ArrayList<>(headers.length);
            System.out.println("headers: " + line);
            for (int i = 2; i < headers.length; i++) {
                score_names.add(headers[i]);
                score_arrays.add(new FloatArrayList());
            }

            int line_count = 0;
            while ((line = br.readLine()) != null) {
                String[] fields = line_regex.split(line);
                int min = Integer.parseInt(fields[0]);
                int max = Integer.parseInt(fields[1]);
                SeqSymmetry child = new IndexedSingletonSym(min, max, aseq);
                parent.addChild(child);   // ScoredContainerSym.addChild() handles setting of child index and parent fields
                for (int field_index = 2; field_index < fields.length; field_index++) {
                    FloatArrayList flist = score_arrays.get(field_index - 2);
                    float score = Float.parseFloat(fields[field_index]);
                    flist.add(score);
                }
                line_count++;
            }
            System.out.println("data lines in file: " + line_count);
            int score_count = score_names.size();
            for (int i = 0; i < score_count; i++) {
                String score_name = score_names.get(i);
                FloatArrayList flist = score_arrays.get(i);
                float[] scores = flist.elements();
                parent.addScores(score_name, scores);
            }
            aseq.addAnnotation(parent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<? extends SeqSymmetry> parse(InputStream is,
            GenomeVersion genomeVersion, String nameType, String uri,
            boolean annotate_seq) throws Exception {
        final Optional<BioSeq> selectedSeq = gmodel.getSelectedSeq();
        // only annotate_seq = true processed here
        if (selectedSeq.isPresent()) {
            parse(is, uri, selectedSeq.get(), genomeVersion);
        }
        return null;
    }
}
