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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.net.URI;
import javax.swing.JOptionPane;

import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.parsers.FileTypeHolder;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.UniFileFilter;
import com.affymetrix.genoviz.swing.recordplayback.ScriptManager;
import com.affymetrix.genoviz.swing.recordplayback.ScriptProcessorHolder;

import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.action.RunScriptAction;
import com.affymetrix.igb.IGBServiceImpl;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

public abstract class OpenURIAction extends GenericAction {

	private static final long serialVersionUID = 1L;

	public static int unknown_group_count = 1;
	public static final String UNKNOWN_SPECIES_PREFIX = BUNDLE.getString("customSpecies");
	public static final String UNKNOWN_GENOME_PREFIX = BUNDLE.getString("customGenome");
	protected static final GenometryModel gmodel = GenometryModel.getGenometryModel();
	protected final IGBService igbService;
	
	public OpenURIAction(String text, String tooltip, String iconPath, String largeIconPath, int mnemonic, Object extraInfo, boolean popup){
		super(text, tooltip, iconPath, largeIconPath, mnemonic, extraInfo, popup);
		igbService = IGBServiceImpl.getInstance();
	}
			
	protected void openURI(URI uri, final String fileName, final boolean mergeSelected, 
		final AnnotatedSeqGroup loadGroup, final String speciesName, boolean loadAsTrack) {
		
		if(ScriptManager.getInstance().isScript(uri.toString())){
			int result = JOptionPane.showConfirmDialog(igbService.getFrame(), "Do you want to run the script?", "Found Script", JOptionPane.YES_NO_OPTION);
			if(result == JOptionPane.YES_OPTION){
				RunScriptAction.getAction().runScript(uri.toString());
			}
			return;
		}
		
		igbService.openURI(uri, fileName, loadGroup, speciesName, loadAsTrack);
		
		if (!mergeSelected) {
			unknown_group_count++;
		}

	}
	
	protected UniFileFilter getAllKnowFilter(){
		Map<String, List<String>> nameToExtensionMap = FileTypeHolder.getInstance().getNameToExtensionMap(null);
		Set<String> all_known_endings = new HashSet<String>();
		//filters.add(new UniFileFilter(ScriptProcessorHolder.getInstance().getScriptExtensions(), "Script File"));
		
		for (String name : nameToExtensionMap.keySet()) {
			all_known_endings.addAll(nameToExtensionMap.get(name));
		}
		
		UniFileFilter all_known_types = new UniFileFilter(
				all_known_endings.toArray(new String[all_known_endings.size()]),
				"Known Types");
		all_known_types.setExtensionListInDescription(false);
		all_known_types.addCompressionEndings(GeneralUtils.compression_endings);
		
		return all_known_types;
	}
	
	protected List<UniFileFilter> getSupportedFiles(FileTypeCategory category){
		Map<String, List<String>> nameToExtensionMap = FileTypeHolder.getInstance().getNameToExtensionMap(category);
		List<UniFileFilter> filters = new ArrayList<UniFileFilter>(nameToExtensionMap.keySet().size() + 1);
		
		for (String name : nameToExtensionMap.keySet()) {
			UniFileFilter uff = new UniFileFilter(nameToExtensionMap.get(name).toArray(new String[]{}), name + " Files");
			uff.addCompressionEndings(GeneralUtils.compression_endings);
			filters.add(uff);
		}
		filters.add(new UniFileFilter(ScriptProcessorHolder.getInstance().getScriptExtensions().toArray(new String[]{}), "Script File"));
		
		return filters;
	}
	
	protected abstract String getID();
}
