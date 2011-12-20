/**
 *   Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 *   Licensed under the Common Public License, Version 1.0 (the "License").
 *   A copy of the license must be included with any distribution of
 *   this source code.
 *   Distributions from Affymetrix, Inc., place this in the
 *   IGB_LICENSE.html file.
 *
 *   The license is also available at
 *   http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.shared;

import com.affymetrix.igb.osgi.service.IGBService;

import java.net.URI;

import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.util.GeneralUtils;


import static com.affymetrix.igb.IGBConstants.BUNDLE;

public abstract class OpenURIAction extends GenericAction {

	private static final long serialVersionUID = 1L;

	public static int unknown_group_count = 1;
	public static final String UNKNOWN_SPECIES_PREFIX = BUNDLE.getString("unknownSpecies");
	public static final String UNKNOWN_GENOME_PREFIX = BUNDLE.getString("unknownGenome");
	private static final String SELECT_SPECIES = BUNDLE.getString("speciesCap");
	protected static final GenometryModel gmodel = GenometryModel.getGenometryModel();
	protected final IGBService igbService;
	
	public OpenURIAction(IGBService _igbService){
		igbService = _igbService;
	}
	
	protected void openURI(URI uri, final String fileName, final boolean mergeSelected, 
			final AnnotatedSeqGroup loadGroup, final String speciesName, final boolean loadAsTrack) {
		igbService.openURI(uri, fileName, loadGroup, speciesName, loadAsTrack);
		
		if (!mergeSelected) {
			unknown_group_count++;
		}

	}

	protected boolean openURI(URI uri) {
		String unzippedName = GeneralUtils.getUnzippedName(uri.getPath());
		String friendlyName = unzippedName.substring(unzippedName.lastIndexOf("/") + 1);

		if (!checkFriendlyName(friendlyName)) {
			return false;
		}

		AnnotatedSeqGroup loadGroup = gmodel.getSelectedSeqGroup();
		boolean mergeSelected = loadGroup == null ? false : true;
		if (loadGroup == null) {
			loadGroup = gmodel.addSeqGroup(UNKNOWN_GENOME_PREFIX + " " + unknown_group_count);
		}

		String speciesName = igbService.getSelectedSpecies();
		if (SELECT_SPECIES.equals(speciesName)) {
			speciesName = UNKNOWN_SPECIES_PREFIX + " " + unknown_group_count;
		}
		openURI(uri, friendlyName, mergeSelected, loadGroup, speciesName, false);

		return true;
	}

	protected boolean checkFriendlyName(String friendlyName) {
		return true;
	}
}
