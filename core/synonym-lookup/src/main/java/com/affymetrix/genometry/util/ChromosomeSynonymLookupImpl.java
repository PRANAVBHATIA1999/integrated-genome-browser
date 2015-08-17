/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometry.util;

import aQute.bnd.annotation.component.Component;
import com.lorainelab.igb.services.synonymlookup.ChromosomeSynonymLookup;

/**
 *
 * @author Tarun
 */
@Component(name = ChromosomeSynonymLookupImpl.COMPONENT_NAME, immediate = true, provide = ChromosomeSynonymLookup.class)
public class ChromosomeSynonymLookupImpl extends SynonymLookup implements ChromosomeSynonymLookup {

    public static final String COMPONENT_NAME = "ChromosomeSynonymLookupImpl";
}
