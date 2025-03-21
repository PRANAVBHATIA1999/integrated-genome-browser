/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.synonymlookup.services.impl;

import org.lorainelab.igb.synonymlookup.services.ChromosomeSynonymLookup;
import java.io.IOException;
import java.io.InputStream;

import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tarun
 */
@Component(name = ChromosomeSynonymLookupImpl.COMPONENT_NAME, immediate = true, service = ChromosomeSynonymLookup.class)
public class ChromosomeSynonymLookupImpl extends SynonymLookup implements ChromosomeSynonymLookup {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ChromosomeSynonymLookupImpl.class);
    public static final String COMPONENT_NAME = "ChromosomeSynonymLookupImpl";

    public ChromosomeSynonymLookupImpl() {
        InputStream resourceAsStream = ChromosomeSynonymLookupImpl.class.getClassLoader().getResourceAsStream("chromosomes.txt");
        try {
            loadSynonyms(resourceAsStream, true);
        } catch (IOException ex) {
            logger.debug(ex.getMessage(), ex);
        }
    }
    
    
}
