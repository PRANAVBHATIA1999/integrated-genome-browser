/**
 * Copyright (c) 2001-2006 Affymetrix, Inc.
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
package com.lorainelab.das;

import com.affymetrix.genometry.general.DataContainer;
import com.affymetrix.genometry.util.LocalUrlCacher;
import com.affymetrix.genometry.util.QueryBuilder;
import com.affymetrix.genometry.util.SynonymLookup;
import com.affymetrix.genometry.util.XMLUtils;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A class to help load and parse documents from a DAS server.
 */
public abstract class DasLoader {

    private static final Logger logger = LoggerFactory.getLogger(DasLoader.class);
    private static final Pattern white_space = Pattern.compile("\\s+");

    /**
     * Get residues for a given region.
     * min and max are specified in genometry coords (interbase-0),
     * and since DAS is base-1, inside this method min/max get modified to
     * (min+1)/max before passing to DAS server
     *
     * @param version
     * @param seqid
     * @param min
     * @param max
     * @return a string of residues from the DAS server or null
     */
    public static String getDasResidues(DataContainer version, String seqid, int min, int max) {

        Set<String> segments = null;//((DasSource) version.versionSourceObj).getEntryPoints();
        String segment = SynonymLookup.getDefaultLookup().findMatchingSynonym(segments, seqid);
        URI request;
        String residues = null;

        try {
            request = URI.create(version.getDataProvider().getUrl());
            URL url = new URL(request.toURL(), version.getName() + "/dna?");
            QueryBuilder builder = new QueryBuilder(url.toExternalForm());
            builder.add("segment", segment + ":" + (min + 1) + "," + max);
            request = builder.build();
            try (InputStream result_stream = LocalUrlCacher.getInputStream(request.toString())) {
                residues = parseDasResidues(new BufferedInputStream(result_stream));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return residues;
    }

    private static String parseDasResidues(InputStream das_dna_result)
            throws IOException, SAXException, ParserConfigurationException {
        InputSource isrc = new InputSource(das_dna_result);

        Document doc = XMLUtils.nonValidatingFactory().newDocumentBuilder().parse(isrc);
        Element top_element = doc.getDocumentElement();
        NodeList top_children = top_element.getChildNodes();

        for (int i = 0; i < top_children.getLength(); i++) {
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }
            Node top_child = top_children.item(i);
            String cname = top_child.getNodeName();
            if (cname == null || !cname.equalsIgnoreCase("sequence")) {
                continue;
            }
            NodeList seq_children = top_child.getChildNodes();
            for (int k = 0; k < seq_children.getLength(); k++) {
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                Node seq_child = seq_children.item(k);
                if (seq_child == null || !seq_child.getNodeName().equalsIgnoreCase("DNA")) {
                    continue;
                }
                NodeList dna_children = seq_child.getChildNodes();
                for (int m = 0; m < dna_children.getLength(); m++) {
                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }
                    Node dna_child = dna_children.item(m);
                    if (dna_child instanceof org.w3c.dom.Text) {
                        String residues = ((Text) dna_child).getData();
                        Matcher matcher = white_space.matcher("");
                        residues = matcher.reset(residues).replaceAll("");
                        return residues;
                    }
                }
            }
        }
        return null;
    }
}
