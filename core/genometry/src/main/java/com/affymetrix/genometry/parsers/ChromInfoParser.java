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

import com.affymetrix.genometry.GenomeVersion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public final class ChromInfoParser {

    private static final Pattern tab_regex = Pattern.compile("\t");

    /**
     * Parses a chrom_info.txt file, creates a new GenomeVersion and
 adds it to the GenometryModel.
     */
    public static boolean parse(InputStream istr, GenomeVersion seq_group, String uri)
            throws IOException {
        BufferedReader dis = new BufferedReader(new InputStreamReader(istr));
        String line;
        boolean isEmpty = true;
        while ((line = dis.readLine()) != null && (!Thread.currentThread().isInterrupted())) {
            if ((line.length() == 0) || line.startsWith("#")) {
                continue;
            }
            String[] fields = tab_regex.split(line);
            if (fields.length == 0) {
                continue;
            }
            if (fields.length == 1) {
                System.out.println("WARNING: chromInfo line does not match.  Ignoring: " + line);
                continue;
            }
            String chrom_name = fields[0];
            int chrLength = 0;
            try {
                chrLength = Integer.parseInt(fields[1]);
            } catch (NumberFormatException ex) {
                System.out.println("WARNING: chromInfo line does not match.  Ignoring: " + line);
                continue;
            }
            seq_group.addSeq(chrom_name, chrLength, uri);	// adds if it doesn't already exist.
            isEmpty = false;
        }
        return !isEmpty;
    }
}
