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
package com.affymetrix.igb.bookmarks;

import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.util.regex.*;

import com.affymetrix.genometryImpl.symmetry.SingletonSeqSymmetry;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerType;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.view.DataLoadView;
import com.affymetrix.igb.event.UrlLoaderThread;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.das.DasServerInfo;
import com.affymetrix.genometryImpl.das.DasSource;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.das2.Das2Capability;
import com.affymetrix.genometryImpl.das2.Das2FeatureRequestSym;
import com.affymetrix.genometryImpl.das2.Das2Region;
import com.affymetrix.genometryImpl.das2.Das2ServerInfo;
import com.affymetrix.genometryImpl.das2.Das2Source;
import com.affymetrix.genometryImpl.das2.Das2Type;
import com.affymetrix.genometryImpl.das2.Das2VersionedSource;
import com.affymetrix.genometryImpl.quickload.QuickLoadServerModel;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.general.FeatureLoading;
import com.affymetrix.igb.general.ServerList;
import com.affymetrix.igb.menuitem.OpenGraphAction;
import com.affymetrix.igb.featureloader.Das2;
import com.affymetrix.igb.util.ScriptFileLoader;
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
 *  and bring up corect version, chromosome, and region with (at least)
 *      annotations that can be loaded via QuickLoaderView
 *  If the currently loaded genome doesn't match the one requested, might
 *      ask the user before switching.
 *</pre>
 */
public final class UnibrowControlServlet {

	private static final GenometryModel gmodel = GenometryModel.getGenometryModel();
	private static final Pattern query_splitter = Pattern.compile("[;\\&]");

	/** Convenience method for retrieving a String parameter from a parameter map
	 *  of an HttpServletRequest.
	 *  @param map Should be a Map, such as from {@link javax.servlet.ServletRequest#getParameterMap()},
	 *  where the only keys are String and String[] objects.
	 *  @param key Should be a key where you only want a single String object as result.
	 *  If the value in the map is a String[], only the first item in the array will
	 *  be returned.
	 */
	static String getStringParameter(Map map, String key) {
		Object o = map.get(key);
		if (o instanceof String) {
			return (String) o;
		} else if (o instanceof String[]) {
			return ((String[]) o)[0];
		} else if (o != null) {
			// This is a temporary case, for handling Integer objects holding start and end
			// in the old BookMarkAction.java class.  The new version of that class
			// puts everything into String[] objects, so this case can go away.
			return o.toString();
		}
		return null;
	}

	/** Loads a bookmark.
	 *  @param parameters Must be a Map where the only values are String and String[]
	 *  objects.  For example, this could be the Map returned by
	 *  {@link javax.servlet.ServletRequest#getParameterMap()}.
	 */
	public static void goToBookmark(Application uni, Map<String, String[]> parameters) throws NumberFormatException {
		String batchFileStr = getStringParameter(parameters, IGBConstants.SCRIPTFILETAG);
		if (batchFileStr != null && batchFileStr.length() > 0) {
			ScriptFileLoader.doActions(batchFileStr);
			return;
		}

		String seqid = getStringParameter(parameters, Bookmark.SEQID);
		String version = getStringParameter(parameters, Bookmark.VERSION);
		String start_param = getStringParameter(parameters, Bookmark.START);
		String end_param = getStringParameter(parameters, Bookmark.END);
		String select_start_param = getStringParameter(parameters, Bookmark.SELECTSTART);
		String select_end_param = getStringParameter(parameters, Bookmark.SELECTEND);

		// For historical reasons, there are two ways of specifying graphs in a bookmark
		// Eventually, they should be treated more similarly, but for now some
		// differences remain
		// parameter "graph_file" can be handled by goToBookmark()
		//    Does not check whether the file was previously loaded
		//    Loads in GUI-friendly thread
		//    Must be a file name, not a generic URL
		// parameter "graph_source_url_0", "graph_source_url_1", ... is handled elsewhere
		//    Checks to avoid double-loading of files
		//    Loading can freeze the GUI
		//    Can be any URL, not just a file
		String[] graph_files = parameters.get("graph_file");
		boolean has_graph_source_urls = (parameters.get("graph_source_url_0") != null);

		int values[] = parseValues(start_param, end_param, select_start_param, select_end_param);
		int start = values[0],
			end   = values[1],
			selstart = values[2],
			selend   = values[3];

		boolean ok = goToBookmark(uni, seqid, version, start, end, selstart, selend, graph_files);
		if (!ok) {
			return; /* user cancelled the change of genome, or something like that */
		}
	
		if (has_graph_source_urls) {
			BookmarkController.loadGraphsEventually(uni.getMapView(), parameters);
		}

		String[] query_urls = parameters.get(Bookmark.QUERY_URL);
		String[] server_urls = parameters.get(Bookmark.SERVER_URL);
		loadData(server_urls, query_urls, start, end);

		//String[] data_urls = parameters.get(Bookmark.DATA_URL);
		//String[] url_file_extensions = parameters.get(Bookmark.DATA_URL_FILE_EXTENSIONS);
		//loadDataFromURLs(uni, data_urls, url_file_extensions, null);
		String selectParam = getStringParameter(parameters, "select");
		if (selectParam != null) {
			performSelection(selectParam);
		}

	}

	/**
	 *  find Das2ServerInfo (or create if not already existing), based on das2_server_url
	 *       to add later?  If no
	 *  find Das2VersionedSource based on Das2ServerInfo and das2_query_url (search for version's FEATURE capability URL matching path of das2_query_url)
	 *  create Das2FeatureRequestSym
	 *  call processFeatureRequests(request_syms, update_display, thread_requests)
	 *       (which in turn call Das2ClientOptimizer.loadFeatures(request_sym))
	 */
	private static void loadDataFromDas2(final Application uni, final String[] das2_server_urls, final String[] das2_query_urls) {
		if (das2_server_urls == null || das2_query_urls == null || das2_query_urls.length == 0 || das2_server_urls.length != das2_query_urls.length) {
			return;
		}
		List<Das2FeatureRequestSym> das2_requests = new ArrayList<Das2FeatureRequestSym>();
		List<String> opaque_requests = new ArrayList<String>();
		//createDAS2andOpaqueRequests(das2_server_urls, das2_query_urls, das2_requests, opaque_requests);
		for (Das2FeatureRequestSym frs : das2_requests) {
			URI uri = frs.getDas2Type().getURI();
			String version = frs.getDas2Type().getVersionedSource().getName();
			GenericFeature feature = GeneralUtils.findFeatureWithURI(GeneralLoadUtils.getFeatures(version), uri);
			if (feature != null) {
				feature.setVisible();
				DataLoadView view = ((IGB) Application.getSingleton()).data_load_view;
				view.tableChanged();
				Application.getSingleton().addNotLockedUpMsg("Loading feature " + feature.featureName);
				List<Das2FeatureRequestSym> frsWrapperList = new ArrayList<Das2FeatureRequestSym>();
				frsWrapperList.add(frs);
				Das2.processFeatureRequests(frsWrapperList, feature, true);
			} else {
				Logger.getLogger(GeneralUtils.class.getName()).log(
						Level.SEVERE, "Couldn't find feature for bookmark URL: {0}", uri);
			}
		}
		if (!opaque_requests.isEmpty()) {
			String[] data_urls = new String[opaque_requests.size()];
			for (int r = 0; r < opaque_requests.size(); r++) {
				data_urls[r] = opaque_requests.get(r);
			}
			loadDataFromURLs(uni, data_urls, null, null);
		}
	}

	private static void createDAS2andOpaqueRequests(
			final String[] das2_server_urls, final String[] das2_query_urls, List<Das2FeatureRequestSym> das2_requests, List<String> opaque_requests) {
		for (int i = 0; i < das2_server_urls.length; i++) {
			String das2_server_url = GeneralUtils.URLDecode(das2_server_urls[i]);
			String das2_query_url = GeneralUtils.URLDecode(das2_query_urls[i]);
			String cap_url = null;
			String seg_uri = null;
			String type_uri = null;
			String overstr = null;
			String format = null;
			boolean use_optimizer = true;
			int qindex = das2_query_url.indexOf('?');
			if (qindex > -1) {
				cap_url = das2_query_url.substring(0, qindex);
				String query = das2_query_url.substring(qindex + 1);
				String[] query_array = query_splitter.split(query);
				for (int k = -0; k < query_array.length; k++) {
					String tagval = query_array[k];
					int eqindex = tagval.indexOf('=');
					String tag = tagval.substring(0, eqindex);
					String val = tagval.substring(eqindex + 1);
					if (tag.equals("format") && (format == null)) {
						format = val;
					} else if (tag.equals("type") && (type_uri == null)) {
						type_uri = val;
					} else if (tag.equals("segment") && (seg_uri == null)) {
						seg_uri = val;
					} else if (tag.equals("overlaps") && (overstr == null)) {
						overstr = val;
					} else {
						use_optimizer = false;
						break;
					}
				}
				if (type_uri == null || seg_uri == null || overstr == null) {
					use_optimizer = false;
				}
			} else {
				use_optimizer = false;
			}
			//
			// only using optimizer if query has 1 segment, 1 overlaps, 1 type, 0 or 1 format, no other params
			// otherwise treat like any other opaque data url via loadDataFromURLs call
			//
			if (!use_optimizer) {
				opaque_requests.add(das2_query_url);
				continue;
			}

			try {
				GenericServer gServer = ServerList.getServer(das2_server_url);
				if (gServer == null) {
					gServer = ServerList.addServer(ServerType.DAS2, das2_server_url, das2_server_url, true);
				} else if (!gServer.isEnabled()) {
					gServer.setEnabled(true);
					GeneralLoadUtils.discoverServer(gServer);
					// enable the server.
					// TODO - this will be saved in preferences as enabled, although it shouldn't.
				}
				Das2ServerInfo server = (Das2ServerInfo) gServer.serverObj;
				server.getSources(); // forcing initialization of server sources, versioned sources, version sources capabilities
				Das2VersionedSource version = Das2Capability.getCapabilityMap().get(cap_url);
				if (version == null) {
					Logger.getLogger(UnibrowControlServlet.class.getName()).log(Level.SEVERE, "Couldn''t find version in url: {0}", cap_url);
					continue;
				}
				Das2Type dtype = version.getTypes().get(type_uri);
				if (dtype == null) {
					Logger.getLogger(UnibrowControlServlet.class.getName()).log(Level.SEVERE, "Couldn''t find type: {0} in server: {1}", new Object[]{type_uri, das2_server_url});
					continue;
				}
				Das2Region segment = version.getSegments().get(seg_uri);
				if (segment == null) {
					Logger.getLogger(UnibrowControlServlet.class.getName()).log(Level.SEVERE, "Couldn''t find segment: {0} in server: {1}", new Object[]{seg_uri, das2_server_url});
					continue;
				}
				String[] minmax = overstr.split(":");
				int min = Integer.parseInt(minmax[0]);
				int max = Integer.parseInt(minmax[1]);
				SeqSpan overlap = new SimpleSeqSpan(min, max, segment.getAnnotatedSeq());
				Das2FeatureRequestSym request = new Das2FeatureRequestSym(dtype, segment, overlap);
				request.setFormat(format);
				das2_requests.add(request);
			} catch (Exception ex) {
				// something went wrong with deconstructing DAS/2 query URL, so just add URL to list of opaque requests
				ex.printStackTrace();
				use_optimizer = false;
				opaque_requests.add(das2_query_url);
			}
		}
	}

	private static void loadData(final String[] server_urls, final String[] query_urls, int start, int end){
		if (server_urls == null || query_urls == null
				|| query_urls.length == 0 || server_urls.length != query_urls.length) {
			return;
		}

		AnnotatedSeqGroup seqGroup = GenometryModel.getGenometryModel().getSelectedSeqGroup();
		BioSeq seq = GenometryModel.getGenometryModel().getSelectedSeq();
		String version = seqGroup.getID();

		for (int i = 0; i < server_urls.length; i++) {
			String server_url = server_urls[i];
			String query_url = query_urls[i];

			try {
				GenericServer gServer = ServerList.getServer(server_url);
				if (gServer == null) {
					//TOD0 - What if server is not found.
				} else if (!gServer.isEnabled()) {
					gServer.setEnabled(true);
					GeneralLoadUtils.discoverServer(gServer);
					// enable the server.
					// TODO - this will be saved in preferences as enabled, although it shouldn't.
				}

				GenericFeature feature = null;
				URI uri = null;

				if(gServer.serverType == ServerType.DAS2){
					
					Das2ServerInfo server = (Das2ServerInfo) gServer.serverObj;
					server.getSources(); // forcing initialization of server sources, versioned sources, version sources capabilities

					Das2VersionedSource das2version = server.getVersionedSource(seqGroup);
					if (version == null) {
						Logger.getLogger(UnibrowControlServlet.class.getName()).log(Level.SEVERE, "Couldn''t find version : {0}", version);
						continue;
					}

					Das2Type dtype = das2version.getTypes().get(query_url);
					if (dtype == null) {
						Logger.getLogger(UnibrowControlServlet.class.getName()).log(Level.SEVERE, "Couldn''t find type: {0} in server: {1}", new Object[]{query_url, server_url});
						continue;
					}
					uri = dtype.getURI();

				} else if(gServer.serverType == ServerType.DAS){
					String source = null;
					String type = null;
					int qindex = query_url.indexOf('?');
					if (qindex <= -1) {
						continue;
					}
					source = query_url.substring(0, qindex);
					type = query_url.substring(qindex + 1, query_url.length());

					DasServerInfo dasServerInfo = (DasServerInfo) gServer.serverObj;
					DasSource dasSource = dasServerInfo.getDataSources().get(source);

					if (dasSource == null) {
						Logger.getLogger(UnibrowControlServlet.class.getName()).log(Level.SEVERE, "Couldn''t find dasSource with id {0}", source);
						continue;
					}

					dasSource.getTypes();
					uri = URI.create(server_url + "/" + source  + "/" + type);
	
				} else if (gServer.serverType == ServerType.QuickLoad){
					
					URL quickloadURL = new URL((String) gServer.serverObj);
					QuickLoadServerModel quickloadServer = QuickLoadServerModel.getQLModelForURL(quickloadURL);
					
					if(quickloadServer.getTypes(version).isEmpty()){
						Logger.getLogger(UnibrowControlServlet.class.getName()).log(Level.SEVERE, "Couldn''t find type with version {0}", version);
						continue;
					}

					uri = URI.create(query_url);
	
				}

				feature = GeneralUtils.findFeatureWithURI(GeneralLoadUtils.getFeatures(version), uri);
				if (feature != null) {
					feature.setVisible();
					GenericFeature.setPreferredLoadStrategy(feature, LoadStrategy.VISIBLE);
					GeneralLoadView.getLoadView().createFeaturesTable();
					if(seq != null){
						SeqSpan overlap = new SimpleSeqSpan(start, end, seq);
						GeneralLoadUtils.loadAndDisplaySpan(overlap, feature);
					}
				} else {
					Logger.getLogger(GeneralUtils.class.getName()).log(
							Level.SEVERE, "Couldn't find feature for bookmark url {0}", query_url);
				}

			} catch (Exception ex) {
				// something went wrong with deconstructing quickload/das query URL.
				ex.printStackTrace();
			}
		}
	}

	private static void loadDataFromURLs(final Application uni, final String[] data_urls, final String[] extensions, final String[] tier_names) {
		try {
			if (data_urls != null && data_urls.length != 0) {
				URL[] urls = new URL[data_urls.length];
				for (int i = 0; i < data_urls.length; i++) {
					urls[i] = new URL(data_urls[i]);
				}
				final UrlLoaderThread t = new UrlLoaderThread(uni.getMapView(), urls, extensions, tier_names);
				t.runEventually();
				t.join();
			}
		} catch (MalformedURLException e) {
			ErrorHandler.errorPanel("Error loading bookmark\nData URL malformed\n", e);
		} catch (InterruptedException ex) {
		}
	}

	private static int[] parseValues(String start_param, String end_param,
					String select_start_param, String select_end_param)
					throws NumberFormatException {

		int start = 0;
		int end = Integer.MAX_VALUE;
		if (start_param == null || start_param.equals("")) {
			System.err.println("No start value found in the bookmark URL");
		} else {
			start = Integer.parseInt(start_param);
		}
		if (end_param == null || end_param.equals("")) {
			System.err.println("No end value found in the bookmark URL");
		} else {
			end = Integer.parseInt(end_param);
		}
		int selstart = -1;
		int selend = -1;
		if (select_start_param != null && select_end_param != null && select_start_param.length() > 0 && select_end_param.length() > 0) {
			selstart = Integer.parseInt(select_start_param);
			selend = Integer.parseInt(select_end_param);
		}
		return new int[]{start, end, selstart, selend};
	}

	/** Loads the sequence and goes to the specified location.
	 *  If version doesn't match the currently-loaded version,
	 *  asks the user if it is ok to proceed.
	 *  NOTE:  This schedules events on the AWT event queue.  If you want
	 *  to make sure that everything has finished before you do something
	 *  else, then you have to schedule that something else to occur
	 *  on the AWT event queue.
	 *  @param graph_files it is ok for this parameter to be null.
	 *  @return true indicates that the action suceeded
	 */
	private static boolean goToBookmark(final Application uni, final String seqid, final String version,
					final int start, final int end, final int selstart, final int selend,
					final String[] graph_files) {

		final SeqMapView gviewer = uni.getMapView();
		final AnnotatedSeqGroup book_group = determineAndSetGroup(version);
		if (book_group == null) {
			ErrorHandler.errorPanel("Bookmark genome version seq group '" + version + "' not found.\n" +
							"You may need to choose a different server.");
			return false; // cancel
		}

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					setSeqAndRegion();

					// Now process "graph_files" url's
					if (graph_files != null) {
						URL[] graph_urls = new URL[graph_files.length];
						for (int i = 0; i < graph_files.length; i++) {
							graph_urls[i] = new URL(graph_files[i]);
						}
						Thread t = OpenGraphAction.loadAndShowGraphs(graph_urls, gmodel.getSelectedSeq(), gviewer);
						t.start();
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			private void setSeqAndRegion() {
				BioSeq book_seq = determineSeq(seqid, book_group);
				if (book_seq == null) {
					ErrorHandler.errorPanel("No seqid", "The bookmark did not specify a valid seqid: specified '" + seqid + "'");
				} else {
					// gmodel.setSelectedSeq() should trigger a gviewer.setAnnotatedSeq() since
					//     gviewer is registered as a SeqSelectionListener on gmodel
					if (book_seq != gmodel.getSelectedSeq()) {
						gmodel.setSelectedSeq(book_seq);
					}
					setRegion(gviewer, start, end, book_seq);
					if (selstart >= 0 && selend >= 0) {
						final SingletonSeqSymmetry regionsym = new SingletonSeqSymmetry(selstart, selend, book_seq);
						gviewer.setSelectedRegion(regionsym, true);
					}
				}
			}

		});
		return true; // was not cancelled, was successful
	}

	public static AnnotatedSeqGroup determineAndSetGroup(final String version) {
		final AnnotatedSeqGroup group;
		if (version == null || "unknown".equals(version) || version.trim().equals("")) {
			group = gmodel.getSelectedSeqGroup();
		} else {
			group = gmodel.getSeqGroup(version);
		}
		if (group != null && !group.equals(gmodel.getSelectedSeqGroup())) {
			// TODO -- move this code into GeneralLoadView's group change handler
			Application.getSingleton().addNotLockedUpMsg("Loading chromosomes for " + version);
			try {
				// Make sure this genome versionName's feature names are initialized.
				GeneralLoadUtils.initVersionAndSeq(version);
			} finally {
				Application.getSingleton().removeNotLockedUpMsg("Loading chromosomes for " + version);
			}
			gmodel.setSelectedSeqGroup(group);
		}
		return group;
	}


	public static BioSeq determineSeq(String seqid, AnnotatedSeqGroup group) {
		// hopefully setting gmodel's selected seq group above triggered population of seqs
		//   for group if not already populated
		BioSeq book_seq;
		if (seqid == null || "unknown".equals(seqid) || seqid.trim().length() == 0) {
			book_seq = gmodel.getSelectedSeq();
			if (book_seq == null && gmodel.getSelectedSeqGroup().getSeqCount() > 0) {
				book_seq = gmodel.getSelectedSeqGroup().getSeq(0);
			}
		} else {
			book_seq = group.getSeq(seqid);
		}
		return book_seq;
	}

	public static void setRegion(SeqMapView gviewer, int start, int end, BioSeq book_seq) {
		if (start >= 0 && end > 0 && end != Integer.MAX_VALUE) {
			final SeqSpan view_span = new SimpleSeqSpan(start, end, book_seq);
			gviewer.zoomTo(view_span);
			final double middle = (start + end) / 2.0;
			gviewer.setZoomSpotX(middle);
		}
	}

	/**
	 * This handles the "select" API parameter.  The "select" parameter can be followed by one
	 * or more comma separated IDs in the form: &select=<id_1>,<id_2>,...,<id_n>
	 * Example:  "&select=EPN1,U2AF2,ZNF524"
	 * Each ID that exists in IGB's ID to symmetry hash will be selected, even if the symmetries
	 * lie on different sequences.
	 * @param selectParam The select parameter passed in through the API
	 */
	public static void performSelection(String selectParam) {

		if (selectParam == null) {
			return;
		}

		// split the parameter by commas
		String[] ids = selectParam.split(",");

		if (selectParam.length() == 0) {
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
