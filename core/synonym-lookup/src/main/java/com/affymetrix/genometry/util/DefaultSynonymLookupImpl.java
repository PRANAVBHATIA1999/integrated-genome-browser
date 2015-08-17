/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometry.util;

import aQute.bnd.annotation.component.Component;
import com.lorainelab.igb.services.synonymlookup.DefaultSynonymLookup;

/**
 *
 * @author Tarun
 */
@Component(name = DefaultSynonymLookupImpl.COMPONENT_NAME, immediate = true, provide = DefaultSynonymLookup.class)
public class DefaultSynonymLookupImpl extends SynonymLookup implements DefaultSynonymLookup {

    public static final String COMPONENT_NAME = "DefaultSynonymLookupImpl";

}
