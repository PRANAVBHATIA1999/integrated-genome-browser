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
package com.affymetrix.igb.view;

import com.affymetrix.genometryImpl.util.LoadUtils;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.parsers.AnnotsXmlParser;
import com.affymetrix.genometryImpl.parsers.AnnotsXmlParser.AnnotMapElt;
import com.affymetrix.genometryImpl.parsers.ChromInfoParser;
import com.affymetrix.genometryImpl.parsers.LiftParser;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.SynonymLookup;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class QuickLoadServerModel {

	private static final SynonymLookup LOOKUP = SynonymLookup.getDefaultLookup();
	private static final Pattern tab_regex = Pattern.compile("\t");
	private String root_url;
	private final List<String> genome_names = new ArrayList<String>();
	private final Map<String, Boolean> genome2init = new HashMap<String, Boolean>();
	// A map from String genome name to a Map of (typeName,fileName) on the server for that group
	private final Map<String, List<AnnotMapElt>> genome2annotsMap = new HashMap<String, List<AnnotMapElt>>();
	private static final Map<String, QuickLoadServerModel> url2quickload = new HashMap<String, QuickLoadServerModel>();
	private String primary_url;

	/**
	 * Initialize quickload server model for given url.
	 * @param url	server url.
	 * @param loadGenome	boolean to check if genomes should be loaded from server.
	 */
	private QuickLoadServerModel(String url, boolean loadGenome) {
		root_url = url;
		if (!root_url.endsWith("/")) {
			root_url = root_url + "/";
		}
		if(loadGenome)
			loadGenomeNames();
	}

	/**
	 * Initialize quickload server model for given url.
	 * @param url	server url.
	 * @param loadGenome	boolean to check if genomes should be loaded from server.
	 * @return	an instance of QuickLoadServerModel
	 */
	private static synchronized QuickLoadServerModel getQLModelForURL(URL url, boolean loadGenome) {
		String ql_http_root = url.toExternalForm();
		if (!ql_http_root.endsWith("/")) {
			ql_http_root = ql_http_root + "/";
		}
		QuickLoadServerModel ql_server = url2quickload.get(ql_http_root);
		if (ql_server == null) {
			ql_server = new QuickLoadServerModel(ql_http_root, loadGenome);
			url2quickload.put(ql_http_root, ql_server);
			LocalUrlCacher.loadSynonyms(LOOKUP, ql_http_root + "synonyms.txt");
		}
		return ql_server;
	}

	/**
	 * Initialize quickload server model for given url. Then add gnomes names provided in set.
	 * @param url	server url.
	 * @param primary_url	url of primary server. 
	 * @param genome_names	Set of genomes names to be added.
	 * @return an instance of QuickLoadServerModel
	 */
	public static synchronized QuickLoadServerModel getQLModelForURL(URL url, URL primary_url, Set<String> genome_names) {
		QuickLoadServerModel gl_server = getQLModelForURL(url, false);
		gl_server.genome_names.addAll(genome_names);
		String primary_root = primary_url.toExternalForm();
		if (!primary_root.endsWith("/")) {
			primary_root = primary_root + "/";
		}
		gl_server.primary_url = primary_root;
		return gl_server;
	}

	/**
	 * Initialize quickload server model for given url.
	 */
	public static synchronized QuickLoadServerModel getQLModelForURL(URL url) {
		return getQLModelForURL(url, true);
	}
	
	private static boolean getCacheAnnots() {
		return true;
	}

	private String getRootUrl() {
		return root_url;
	}

	public List<String> getGenomeNames() {
		return genome_names;
	}

	private AnnotatedSeqGroup getSeqGroup(String genome_name) {
		return GenometryModel.getGenometryModel().addSeqGroup(LOOKUP.findMatchingSynonym(
				GenometryModel.getGenometryModel().getSeqGroupNames(), genome_name));
	}

	public List<AnnotMapElt> getAnnotsMap(String genomeName) {
		return this.genome2annotsMap.get(genomeName);
	}

	/**
	 *  Returns the list of String typeNames that this QuickLoad server has
	 *  for the genome with the given name.
	 *  The list may (rarely) be empty, but never null.
	 */
	public List<String> getTypes(String genome_name) {
		genome_name = LOOKUP.findMatchingSynonym(genome_names, genome_name);
		if (genome2init.get(genome_name) != Boolean.TRUE) {
			initGenome(genome_name);
		}
		if (getAnnotsMap(genome_name) == null) {
			return Collections.<String>emptyList();
		}
		List<String> typeNames = new ArrayList<String>();
		for (AnnotMapElt annotMapElt : getAnnotsMap(genome_name)) {
			typeNames.add(annotMapElt.title);
		}
		
		return typeNames;
	}

	public Map<String, String> getProps(String genomeName, String featureName) {
		Map<String, String> props = null;
		List<AnnotMapElt> annotList = getAnnotsMap(genomeName);
		if (annotList != null) {
			AnnotMapElt annotElt = AnnotMapElt.findTitleElt(featureName, annotList);
			if (annotElt != null) {
				return annotElt.props;
			}
		}
		return props;
	}

	private synchronized void initGenome(String genome_name) {
		Logger.getLogger(QuickLoadServerModel.class.getName()).log(Level.FINE,
			"initializing data for genome: " + genome_name);
		if (loadSeqInfo(genome_name) && loadAnnotationNames(genome_name)) {
			genome2init.put(genome_name, Boolean.TRUE);
			return;
		}

		// Clear the type list if something went wrong.
		List<AnnotMapElt> annotList = getAnnotsMap(genome_name);
		if (annotList != null) {
			annotList.clear();
		}
	}

	/**
	 *  Determines the list of annotation files available in the genome directory.
	 *  Looks for ~genome_dir/annots.txt file which lists annotation files
	 *  available in same directory.  Returns true or false depending on
	 *  whether the file is successfully loaded.
	 *  You can retrieve the typeNames with {@link #getTypes(String)}
	 */
	private boolean loadAnnotationNames(String genome_name) {
		genome_name = LOOKUP.findMatchingSynonym(genome_names, genome_name);
		String genome_root = root_url + genome_name + "/";
		Logger.getLogger(QuickLoadServerModel.class.getName()).log(Level.FINE,
				"loading list of available annotations for genome: " + genome_name);

		// Make a new list of typeNames, in case this is being re-initialized
		// If this search fails, then we're just returning an empty map.
		List<AnnotMapElt> annotList = new ArrayList<AnnotMapElt>();
		genome2annotsMap.put(genome_name, annotList);

		return processAnnotsXml(genome_root + IGBConstants.annotsXml, annotList) ||
				processAnnotsTxt(genome_root + IGBConstants.annotsTxt, annotList);
	}

	/**
	 * Process the annots.xml file (if it exists).
	 * This has friendly type names.
	 * @param filename
	 * @param annotList
	 * @return true or false
	 */
	private static boolean processAnnotsXml(String filename, List<AnnotMapElt> annotList) {
		InputStream istr = null;
		try {
			istr = LocalUrlCacher.getInputStream(filename, false, null, true);
			if (istr == null) {
				// Search failed.  That's fine, since there's a backup test for annots.txt.
				return false;
			}

			AnnotsXmlParser.parseAnnotsXml(istr, annotList);
			return true;
		} catch (Exception ex) {
			System.out.println("Couldn't process file " + filename);
			ex.printStackTrace();
			return false;
		} finally {
			GeneralUtils.safeClose(istr);
		}
	}

	/**
	 * Process the annots.txt file (if it exists).
	 * @param filename
	 * @param annotList
	 * @return true or false
	 */
	private static boolean processAnnotsTxt(String filename, List<AnnotMapElt> annotList) {
		InputStream istr = null;
		BufferedReader br = null;
		try {
			istr = LocalUrlCacher.getInputStream(filename, getCacheAnnots());
			if (istr == null) {
				// Search failed.  getInputStream has already logged warnings about this.
				return false;
			}
			br = new BufferedReader(new InputStreamReader(istr));
			String line;
			while ((line = br.readLine()) != null) {
				String[] fields = tab_regex.split(line);
				if (fields.length >= 1) {
					String annot_file_name = fields[0];
					if (annot_file_name == null || annot_file_name.length() == 0) {
						continue;
					}
					String friendlyName = LoadUtils.stripFilenameExtensions(annot_file_name);
					AnnotMapElt annotMapElt = new AnnotMapElt(annot_file_name, friendlyName);
					annotList.add(annotMapElt);
				}
			}
			return true;
		} catch (Exception ex) {
			System.out.println("Couldn't find or couldn't process file " + filename);
			ex.printStackTrace();
			return false;
		} finally {
			GeneralUtils.safeClose(br);
			GeneralUtils.safeClose(istr);
		}
	}

	private boolean loadSeqInfo(String genome_name) {
		String liftAll = IGBConstants.liftAllLft;
		String modChromInfo = IGBConstants.modChromInfoTxt;
		genome_name = LOOKUP.findMatchingSynonym(genome_names, genome_name);
		boolean success = false;
		String genome_root = root_url + genome_name + "/";
		Logger.getLogger(QuickLoadServerModel.class.getName()).log(Level.FINE,
				"loading list of chromosomes for genome: " + genome_name);
		InputStream lift_stream = null;
		InputStream cinfo_stream = null;
		try {

			Logger.getLogger(QuickLoadServerModel.class.getName()).log(Level.FINE,
					"lift URL: " + genome_root + liftAll);
			String lift_path = genome_root + liftAll;
			try {
				// don't warn about this file, since we'll look for modChromInfo file
				lift_stream = LocalUrlCacher.getInputStream(lift_path, getCacheAnnots(), null, true);
			} catch (Exception ex) {
				// exception can be ignored, since we'll look for modChromInfo file.
				Logger.getLogger(QuickLoadServerModel.class.getName()).log(Level.FINE,
						"couldn't find " + liftAll + ", looking instead for " + modChromInfo);
				lift_stream = null;
			}
			if (lift_stream == null) {
				String cinfo_path = genome_root + modChromInfo;
				try {
					cinfo_stream = LocalUrlCacher.getInputStream(cinfo_path, getCacheAnnots(), null, false);
				} catch (Exception ex) {
					System.err.println("ERROR: could find neither " + lift_path + " nor " + cinfo_path);
					ex.printStackTrace();
					cinfo_stream = null;
				}
			}

			boolean annot_contigs = false;
			if (lift_stream != null) {
				LiftParser.parse(lift_stream, GenometryModel.getGenometryModel(), genome_name, annot_contigs);
				success = true;
			} else if (cinfo_stream != null) {
				ChromInfoParser.parse(cinfo_stream, GenometryModel.getGenometryModel(), genome_name);
				success = true;
			}
		} catch (Exception ex) {
			ErrorHandler.errorPanel("ERROR", "Error loading data for genome '" + genome_name + "'", ex);
		} finally {
			GeneralUtils.safeClose(lift_stream);
			GeneralUtils.safeClose(cinfo_stream);
		}
		return success;
	}

	private synchronized void loadGenomeNames() {
		String contentsTxt = IGBConstants.contentsTxt;
		InputStream istr = null;
		InputStreamReader ireader = null;
		BufferedReader br = null;
		try {
			try {
				istr = LocalUrlCacher.getInputStream(root_url + contentsTxt, getCacheAnnots());
			} catch (Exception e) {
				System.out.println("ERROR: Couldn't open '" + root_url + contentsTxt + "\n:  " + e.toString());
				istr = null; // dealt with below
			}
			if (istr == null) {
				System.out.println("Could not load QuickLoad contents from\n" + root_url + contentsTxt);
				return;
			}
			ireader = new InputStreamReader(istr);
			br = new BufferedReader(ireader);
			String line;
			while ((line = br.readLine()) != null) {
				if ( (line.length() == 0) || line.startsWith("#"))  { continue; }
				AnnotatedSeqGroup group = null;
				String[] fields = tab_regex.split(line);
				if (fields.length >= 1) {
					String genome_name = fields[0];
					genome_name = genome_name.trim();
					if (genome_name.length() == 0) {
						System.out.println("Found blank QuickLoad genome -- skipping");
						continue;
					}
					group = this.getSeqGroup(genome_name);  // returns existing group if found, otherwise creates a new group
					
					if(fields.length >= 3){
						GeneralLoadUtils.getSpeciesList(fields[2]).add(genome_name);
					}else{
						genome_names.add(genome_name);
					}
				}
				// if quickload server has description, and group is new or doesn't yet have description, add description to group
				if ((fields.length >= 2) && (group.getDescription() == null)) {
					group.setDescription(fields[1]);
				}
			}
		} catch (Exception ex) {
			ErrorHandler.errorPanel("ERROR", "Error loading genome names", ex);
		} finally {
			GeneralUtils.safeClose(istr);
			GeneralUtils.safeClose(ireader);
			GeneralUtils.safeClose(br);
		}
	}

	@Override
	public String toString() {
		return "QuickLoadServerModel: url='" + getRootUrl() + "'";
	}
}
