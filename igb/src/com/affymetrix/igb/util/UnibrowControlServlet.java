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
package com.affymetrix.igb.util;

import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.BioSeq;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.affymetrix.genometryImpl.span.SimpleMutableSeqSpan;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerType;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.igb.general.ServerList;
import com.affymetrix.igb.menuitem.LoadFileAction;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import com.affymetrix.igb.view.load.GeneralLoadView;

/**
 *  A way of allowing IGB to be controlled via hyperlinks.
 *  (This used to be an implementation of HttpServlet, but it isn't now.)
 * <pre>
 *  Can specify:
 *      genome version
 *      chromosome
 *      start of region in view
 *      end of region in view
 *  and bring up correct version, chromosome, and region with (at least)
 *      annotations that can be loaded via QuickLoaderView
 *  If the currently loaded genome doesn't match the one requested, might
 *      ask the user before switching.
 *
 * @version $Id: UnibrowControlServlet.java 7505 2011-02-10 20:27:35Z hiralv $
 *</pre>
 */
public final class UnibrowControlServlet {

	private static final UnibrowControlServlet instance = new UnibrowControlServlet();

	private UnibrowControlServlet() {
		super();
	}

	public static final UnibrowControlServlet getInstance() {
		return instance;
	}

	private static final GenometryModel gmodel = GenometryModel.getGenometryModel();

	public void loadResidues(int start, int end){
		BioSeq vseq = GenometryModel.getGenometryModel().getSelectedSeq();
		SeqSpan span = new SimpleMutableSeqSpan(start, end, vseq);
		GeneralLoadView.getLoadView().loadResidues(span, true);
	}

	public GenericFeature getFeature(GenericServer gServer, String feature_url){
		AnnotatedSeqGroup seqGroup = GenometryModel.getGenometryModel().getSelectedSeqGroup();
		GenericFeature feature = null;

		URI uri = URI.create(feature_url);
		GenericVersion gVersion = seqGroup.getVersionOfServer(gServer);
		if (gVersion == null && gServer.serverType != ServerType.LocalFiles) {
			Logger.getLogger(UnibrowControlServlet.class.getName()).log(
				Level.SEVERE, "Couldn''t find version {0} in server {1}",
				new Object[]{seqGroup.getID(), gServer.serverName});
			return null;
		}

		if(gVersion != null)
			feature = GeneralUtils.findFeatureWithURI(gVersion.getFeatures(), uri);

		if(feature == null && gServer.serverType == ServerType.LocalFiles){
			// For local file check if feature already exists.

			// If feature doesn't not exist then add it.
			String fileName = feature_url.substring(feature_url.lastIndexOf('/') + 1, feature_url.length());
			feature = LoadFileAction.getFeature(uri, fileName, seqGroup.getOrganism(), seqGroup);

		}

		return feature;
	}
	/**
	 * Finds server from server url and enables it, if found disabled.
	 * @param server_url	Server url string.
	 * @return	Returns GenericServer if found else null.
	 */
	public GenericServer loadServer(String server_url){
		GenericServer gServer = ServerList.getServerInstance().getServer(server_url);
		if (gServer == null) {
			Logger.getLogger(UnibrowControlServlet.class.getName()).log(
					Level.SEVERE, "Couldn''t find server {0}. Creating a local server.", server_url);

			gServer = ServerList.getServerInstance().getLocalFilesServer();

		} else if (!gServer.isEnabled()) {
			// enable the server for this session only
			gServer.enableForSession();
			GeneralLoadUtils.discoverServer(gServer);
		}
		return gServer;
	}

	public AnnotatedSeqGroup determineAndSetGroup(final String version) {
		final AnnotatedSeqGroup group;
		if (version == null || "unknown".equals(version) || version.trim().equals("")) {
			group = gmodel.getSelectedSeqGroup();
		} else {
			group = gmodel.getSeqGroup(version);
		}
		if (group != null && !group.equals(gmodel.getSelectedSeqGroup())) {
			GeneralLoadView.getLoadView().initVersion(version);
			gmodel.setSelectedSeqGroup(group);
		}
		return group;
	}

	/**
	 * This handles the "select" API parameter.  The "select" parameter can be followed by one
	 * or more comma separated IDs in the form: &select=<id_1>,<id_2>,...,<id_n>
	 * Example:  "&select=EPN1,U2AF2,ZNF524"
	 * Each ID that exists in IGB's ID to symmetry hash will be selected, even if the symmetries
	 * lie on different sequences.
	 * @param selectParam The select parameter passed in through the API
	 */
	public void performSelection(String selectParam) {

		if (selectParam == null || selectParam.length() == 0) {
			return;
		}

		// split the parameter by commas
		String[] ids = selectParam.split(",");

		if (ids.length == 0) {
			return;
		}

		AnnotatedSeqGroup group = GenometryModel.getGenometryModel().getSelectedSeqGroup();
		List<SeqSymmetry> sym_list = new ArrayList<SeqSymmetry>(ids.length);
		for (String id : ids) {
			sym_list.addAll(group.findSyms(id));
		}

		GenometryModel.getGenometryModel().setSelectedSymmetriesAndSeq(sym_list, UnibrowControlServlet.class);
	}
}
