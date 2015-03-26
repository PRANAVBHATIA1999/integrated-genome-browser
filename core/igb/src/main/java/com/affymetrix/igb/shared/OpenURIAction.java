/**
 * Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.shared;

import com.affymetrix.genometry.AnnotatedSeqGroup;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.parsers.FileTypeHolder;
import com.affymetrix.genometry.util.UniFileFilter;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.IGBServiceImpl;
import com.affymetrix.igb.action.RunScriptAction;
import com.affymetrix.igb.action.SeqMapViewActionA;
import com.affymetrix.igb.swing.ScriptManager;
import com.affymetrix.igb.swing.ScriptProcessorHolder;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenURIAction extends SeqMapViewActionA {

    private static final long serialVersionUID = 1L;

    public static int CUSTOM_GENOME_COUNTER = 1;
    public static final String UNKNOWN_SPECIES_PREFIX = BUNDLE.getString("customSpecies");
    public static final String UNKNOWN_GENOME_PREFIX = BUNDLE.getString("customGenome");
    protected static final GenometryModel gmodel = GenometryModel.getInstance();
    protected static final String SELECT_SPECIES = BUNDLE.getString("speciesCap");

    public static UniFileFilter getAllSupportedExtensionsFilter() {
        Map<String, List<String>> nameToExtensionMap = FileTypeHolder.getInstance().getNameToExtensionMap(null);
        Set<String> allKnownEndings = new HashSet<>();
        nameToExtensionMap.values().forEach(allKnownEndings::addAll);
        allKnownEndings.addAll(ScriptProcessorHolder.getInstance().getScriptExtensions());
        UniFileFilter allKnownTypes = new UniFileFilter(allKnownEndings, "Known Types", true);
        allKnownTypes.setExtensionListInDescription(false);
        return allKnownTypes;
    }

    public static List<UniFileFilter> getSupportedFiles(FileTypeCategory category) {
        Map<String, List<String>> nameToExtensionMap = FileTypeHolder.getInstance().getNameToExtensionMap(category);
        List<UniFileFilter> filters = new ArrayList<>();
        nameToExtensionMap.entrySet().stream()
                .map(entry -> new UniFileFilter(entry.getValue(), entry.getKey() + " Files", true))
                .forEach(filters::add);
        return filters;
    }

    public static AnnotatedSeqGroup retrieveSeqGroup(String name) {
        return gmodel.addSeqGroup(name);
    }
    protected final IGBServiceImpl igbService;

    public OpenURIAction(String text, String tooltip, String iconPath, String largeIconPath, int mnemonic, Object extraInfo, boolean popup) {
        super(text, tooltip, iconPath, largeIconPath, mnemonic, extraInfo, popup);
        igbService = IGBServiceImpl.getInstance();
    }

    public void openURI(URI uri, final String fileName, final boolean mergeSelected, final AnnotatedSeqGroup loadGroup, final String speciesName, boolean isReferenceSequence) {

        if (ScriptManager.getInstance().isScript(uri.toString())) {
            RunScriptAction.getAction().runScript(uri.toString());
            return;
        }

        igbService.openURI(uri, fileName, loadGroup, speciesName, isReferenceSequence);

        if (!mergeSelected) {
            CUSTOM_GENOME_COUNTER++;
            gmodel.setSelectedSeqGroup(loadGroup);
        }

    }

}
