/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymlookup.services.impl;

import org.lorainelab.igb.synonymlookup.services.GenomeVersionSynonymLookup;
import java.io.IOException;
import java.io.InputStream;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tarun
 */
@Component(name = GenomeVersionSynonymLookupImpl.COMPONENT_NAME, immediate = true, service = GenomeVersionSynonymLookup.class)
public class GenomeVersionSynonymLookupImpl extends SynonymLookup implements GenomeVersionSynonymLookup {

    private static final Logger logger = LoggerFactory.getLogger(GenomeVersionSynonymLookupImpl.class);
    public static final String COMPONENT_NAME = "GenomeVersionSynonymLookupImpl";

    public GenomeVersionSynonymLookupImpl() {
        InputStream resourceAsStream = GenomeVersionSynonymLookupImpl.class.getClassLoader().getResourceAsStream("synonyms.txt");
        try {
            loadSynonyms(resourceAsStream, true);
        } catch (IOException ex) {
            logger.debug(ex.getMessage(), ex);
        }
    }

}
