package com.affymetrix.genometryImpl.das;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.mutable.MutableInt;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.parsers.das.DASFeatureParser;
import com.affymetrix.genometryImpl.parsers.das.DASSymmetry;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symloader.SymLoader;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.thread.CThreadHolder;
import com.affymetrix.genometryImpl.thread.PositionCalculator;
import com.affymetrix.genometryImpl.thread.ProgressUpdater;
import com.affymetrix.genometryImpl.util.Constants;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.genometryImpl.util.QueryBuilder;
import com.affymetrix.genometryImpl.util.ServerTypeI;
import com.affymetrix.genometryImpl.util.SpeciesLookup;
import com.affymetrix.genometryImpl.util.SynonymLookup;
import com.affymetrix.genometryImpl.util.VersionDiscoverer;
import java.util.*;

public class DasServerType implements ServerTypeI {
	/** boolean to indicate should script continue to run if error occurs **/
	private static final boolean DEBUG = true;
	private static final boolean exitOnError = false;
	private static final String dsn = "dsn.xml";
	private static final String name = "DAS";
	public static final int ordinal = 30;
	private static final GenometryModel gmodel = GenometryModel.getGenometryModel();
	/**
	 * Private copy of the default Synonym lookup
	 * @see SynonymLookup#getDefaultLookup()
	 */
	private static final SynonymLookup LOOKUP = SynonymLookup.getDefaultLookup();
	private static final DasServerType instance = new DasServerType();
	public static DasServerType getInstance() {
		return instance;
	}

	protected DasServerType() {
		super();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int compareTo(ServerTypeI o) {
		return ordinal - o.getOrdinal();
	}

	@Override
	public int getOrdinal() {
		return ordinal;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Gets server path for a mapping on DAS server.
	 * @param id	Genome id
	 * @param file	File name.
	 */
	private String getPath(String id, URL server, String file){
		try {
			URL server_path = new URL(server, id + "/" + file);
			return server_path.toExternalForm();
		} catch (MalformedURLException ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	/**
	 * Gets files for a genome and copies it to it's directory.
	 * @param local_path	Local path from where mapping is to saved.
	 */
	@SuppressWarnings("unused")
	private boolean getAllDasFiles(String id, URL server, URL master, String local_path){
		local_path += "/" + id;
		GeneralUtils.makeDir(local_path);

		File file;
		final Map<String, String> DasFilePath = new HashMap<String, String>();

		String entry_point = getPath(master.getPath(),master, DasSource.ENTRY_POINTS);
		
		String types = getPath(id,server,DasSource.TYPES);

		DasFilePath.put(entry_point, DasSource.ENTRY_POINTS + Constants.xml_ext);
		DasFilePath.put(types, DasSource.TYPES + Constants.xml_ext);

		for(Entry<String, String> fileDet : DasFilePath.entrySet()){
			file = GeneralUtils.getFile(fileDet.getKey(), false);

			if((file == null || !GeneralUtils.moveFileTo(file,fileDet.getValue(),local_path)) && exitOnError)
				return false;

		}

		return true;
	}

	@Override
	public boolean processServer(GenericServer gServer, String path) {
		File file = GeneralUtils.getFile(gServer.URL, false);
		if(!GeneralUtils.moveFileTo(file,dsn,path))
			return false;
		
		DasServerInfo server = (DasServerInfo) gServer.serverObj;
		Map<String, DasSource> sources = server.getDataSources();

		if (sources == null || sources.values() == null || sources.values().isEmpty()) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING,"Couldn't find species for server: ",gServer);
			return false;
		}

		for (DasSource source : sources.values()) {
			
			if(!getAllDasFiles(source.getID(),source.getServerURL(), source.getMasterURL(), path)){
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not find all files for {0} !!!", gServer.serverName);
				return false;
			}

			for(String src : source.getSources()){
				if(!getAllDasFiles(src,source.getServerURL(), source.getMasterURL(), path)){
					Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not find all files for {0} !!!", gServer.serverName);
					return false;
				}
			}

		}

		return true;
	}

	@Override
	public String formatURL(String url) {
		while (url.endsWith("/")) {
			url = url.substring(0, url.length()-1);
		}
		return url;
	}

	@Override
	public Object getServerInfo(String url, String name) {
		return new DasServerInfo(url);
	}

	@Override
	public String adjustURL(String url) {
		String tempURL = url;
		if (tempURL.endsWith("/dsn")) {
			tempURL = tempURL.substring(0, tempURL.length() - 4);
		}
		return tempURL;
	}

	@Override
	public boolean loadStrategyVisibleOnly() {
		return true;
	}

	@Override
	public void discoverFeatures(GenericVersion gVersion, boolean autoload) {
		DasSource version = (DasSource) gVersion.versionSourceObj;
		List<Entry<String, String>> types = new ArrayList<Entry<String, String>>(version.getTypes().entrySet());
		final MutableInt nameLoop = new MutableInt(0);
		ProgressUpdater progressUpdater = new ProgressUpdater("DAS discover features", 0, types.size(), 
			new PositionCalculator() {
				@Override
				public long getCurrentPosition() {
					return nameLoop.intValue();
				}
			}
		);
		CThreadHolder.getInstance().getCurrentCThreadWorker().setProgressUpdater(progressUpdater);
		for (; nameLoop.intValue() < types.size(); nameLoop.increment()) {
			Entry<String,String> type = types.get(nameLoop.intValue());
			String type_name = type.getKey();
			if (type_name == null || type_name.length() == 0) {
				System.out.println("WARNING: Found empty feature name in " + gVersion.versionName + ", " + gVersion.gServer.serverName);
				continue;
			}
			gVersion.addFeature(new GenericFeature(type_name, null, gVersion, null, type.getValue(), autoload));
		}
	}

	@Override
	public void discoverChromosomes(Object versionSourceObj) {
		// Discover chromosomes from DAS
		DasSource version = (DasSource) versionSourceObj;

		version.getGenome();
		version.getEntryPoints();
	}

	@Override
	public boolean hasFriendlyURL() {
		return false;
	}

	@Override
	public boolean canHandleFeature() {
		return true;
	}

	/**
	 * Discover species from DAS
	 * @param gServer
	 * @return false if there's an obvious problem
	 */
	@Override
	public boolean getSpeciesAndVersions(GenericServer gServer, GenericServer primaryServer, URL primaryURL, VersionDiscoverer versionDiscoverer) {
		DasServerInfo server = (DasServerInfo) gServer.serverObj;
		if (primaryURL == null) {
			try {
				primaryURL = new URL(gServer.URL);
				primaryServer = null;
			}
			catch (MalformedURLException x) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "cannot load URL " + gServer.URL + " for DAS server " + gServer.serverName, x);
			}
		}
		Map<String, DasSource> sources = server.getDataSources(primaryURL, primaryServer);
		if (sources == null || sources.values() == null || sources.values().isEmpty()) {
			System.out.println("WARNING: Couldn't find species for server: " + gServer);
			return false;
		}
		for (DasSource source : sources.values()) {
			String speciesName = SpeciesLookup.getSpeciesName(source.getID());
			String versionName = LOOKUP.findMatchingSynonym(gmodel.getSeqGroupNames(), source.getID());
			String versionID = source.getID();
			versionDiscoverer.discoverVersion(versionID, versionName, gServer, source, speciesName);
		}
		return true;
	}

	protected String getSegment(SeqSpan span, GenericFeature feature) {
		BioSeq current_seq = span.getBioSeq();
		Set<String> segments = ((DasSource) feature.gVersion.versionSourceObj).getEntryPoints();
		return SynonymLookup.getDefaultLookup().findMatchingSynonym(segments, current_seq.getID());
	}

	/**
	 * Load annotations from a DAS server.
	 *
	 * @param feature the generic feature that is to be loaded from the server.
	 * @param span containing the ranges for which you want annotations.
	 */
	@Override
	public List<? extends SeqSymmetry> loadFeatures(SeqSpan span, GenericFeature feature) {
		String segment = getSegment(span, feature);

		QueryBuilder builder = new QueryBuilder(feature.typeObj.toString());
		builder.add("segment", segment);
		builder.add("segment", segment + ":" + (span.getMin() + 1) + "," + span.getMax());

		ITrackStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(feature.typeObj.toString(), feature.featureName, "das1", feature.featureProps);
		style.setFeature(feature);

		// TODO - probably not necessary
		//style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(feature.featureName, feature.featureName, "das1");
		//style.setFeature(feature);

		URI uri = builder.build();
		if (DEBUG) System.out.println("Loading DAS feature " + feature.featureName + " with uri " + uri);
		List<DASSymmetry> dassyms = parseData(uri);
		// Special case : When a feature make more than one Track, set feature for each track.
		if (dassyms != null) {
			if (Thread.currentThread().isInterrupted()) {
				dassyms = null;
				return Collections.<SeqSymmetry>emptyList();
			}
			
			SymLoader.addAnnotations(dassyms, span, uri, feature);
			for (DASSymmetry sym : dassyms) {
				feature.addMethod(sym.getType());
			}
		}
		
		return dassyms;
	}

	/**
	 *  Opens a binary data stream from the given uri and adds the resulting
	 *  data.
	 */
	private List<DASSymmetry> parseData(URI uri) {
		Map<String, List<String>> respHeaders = new HashMap<String, List<String>>();
		InputStream stream = null;
		List<String> list;
		String content_type = "content/unknown";
		int content_length = -1;

		try {
			stream = LocalUrlCacher.getInputStream(uri.toURL(), true, null, respHeaders);
			list = respHeaders.get("Content-Type");
			if (list != null && !list.isEmpty()) {
				content_type = list.get(0);
			}

			list = respHeaders.get("Content-Length");
			if (list != null && !list.isEmpty()) {
				try {
					content_length = Integer.parseInt(list.get(0));
				} catch (NumberFormatException ex) {
					content_length = -1;
				}
			}

			if (content_length == 0) { // Note: length == -1 means "length unknown"
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "{0} returned no data.", uri);
				return null;
			}

			if (content_type.startsWith("text/plain")
					|| content_type.startsWith("text/html")
					|| content_type.startsWith("text/xml")) {
				// Note that some http servers will return "text/html" even when that is untrue.
				// we could try testing whether the filename extension is a recognized extension, like ".psl"
				// and if so passing to LoadFileAction.load(.. feat_request_con.getInputStream() ..)
				AnnotatedSeqGroup group = GenometryModel.getGenometryModel().getSelectedSeqGroup();
				DASFeatureParser das_parser = new DASFeatureParser();
				das_parser.setAnnotateSeq(false);
				
				BufferedInputStream bis = null;
				try {
					bis = new BufferedInputStream(stream);
					return das_parser.parse(bis, group);
				} catch (XMLStreamException ex) {
					Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Unable to parse DAS response", ex);
				} finally {
					GeneralUtils.safeClose(bis);
				}
			} else {
				Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Declared data type {0} cannot be processed", content_type);
			}
		} catch (Exception ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Exception encountered: no data returned for url " + uri, ex);
		} finally {
			GeneralUtils.safeClose(stream);
		}

		return null;
	}

	@Override
	public boolean isAuthOptional() {
		return false;
	}

	@Override
	public boolean getResidues(GenericServer server,
			List<GenericVersion> versions, String genomeVersionName,
			BioSeq aseq, int min, int max, SeqSpan span) {
		String seq_name = aseq.getID();
		for (GenericVersion version : versions) {
			if (!server.equals(version.gServer)) {
				continue;
			}
			DasResiduesHandler dasResiduesHandler = new DasResiduesHandler();
			String residues = dasResiduesHandler.getDasResidues(version, seq_name, min, max);
//			String residues = DasLoader.getDasResidues(version, seq_name, min, max);
			if (residues != null) {
				BioSeq.addResiduesToComposition(aseq, residues, span);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isSaveServersInPrefs() {
		return true;
	}
}
