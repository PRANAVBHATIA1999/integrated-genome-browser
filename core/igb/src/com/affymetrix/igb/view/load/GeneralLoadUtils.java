package com.affymetrix.igb.view.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.mutable.MutableLong;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.comparator.StringVersionDateComparator;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.parsers.Bprobe1Parser;
import com.affymetrix.genometryImpl.parsers.graph.BarParser;
import com.affymetrix.genometryImpl.parsers.useq.ArchiveInfo;
import com.affymetrix.genometryImpl.parsers.useq.USeqGraphParser;
import com.affymetrix.genometryImpl.quickload.QuickLoadSymLoader;
import com.affymetrix.genometryImpl.span.MutableDoubleSeqSpan;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.symmetry.MutableSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleMutableSeqSymmetry;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LoadUtils.RefreshStatus;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerStatus;
import com.affymetrix.genometryImpl.util.ServerTypeI;
import com.affymetrix.genometryImpl.util.SpeciesLookup;
import com.affymetrix.genometryImpl.util.SynonymLookup;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.genometryImpl.util.VersionDiscoverer;
import com.affymetrix.genometryImpl.symloader.BAM;
import com.affymetrix.genometryImpl.symloader.ResidueTrackSymLoader;
import com.affymetrix.genometryImpl.symloader.SymLoader;
import com.affymetrix.genometryImpl.symloader.SymLoaderInst;
import com.affymetrix.genometryImpl.symloader.SymLoaderInstNC;
import com.affymetrix.genometryImpl.thread.CThreadHolder;
import com.affymetrix.genometryImpl.thread.CThreadWorker;
import com.affymetrix.genometryImpl.thread.PositionCalculator;
import com.affymetrix.genometryImpl.thread.ProgressUpdater;
import com.affymetrix.genometryImpl.util.ParserController;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.util.ServerUtils;

import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.IGBServiceImpl;
import com.affymetrix.igb.action.FeatureLoadAction;
import com.affymetrix.igb.general.ServerList;
import com.affymetrix.igb.parsers.QuickLoadSymLoaderChp;
import com.affymetrix.igb.view.SeqGroupView;
import com.affymetrix.igb.view.SeqMapView;

/**
 *
 * @version $Id: GeneralLoadUtils.java 11492 2012-05-10 18:17:28Z hiralv $
 */
public final class GeneralLoadUtils {

	private static final boolean DEBUG = false;
	private static final Pattern tab_regex = Pattern.compile("\t");
	/**
	 * using negative start coord for virtual genome chrom because (at least for
	 * human genome) whole genome start/end/length can't be represented with
	 * positive 4-byte ints (limit is +/- 2.1 billion)
	 */
//    final double default_genome_min = -2100200300;
	private static final double default_genome_min = -2100200300;
	private static final GenometryModel gmodel = GenometryModel.getGenometryModel();
	// File name storing directory name associated with server on a cached server.
	public static final String SERVER_MAPPING = "/serverMapping.txt";
	/**
	 * Location of synonym file for correlating versions to species. The file
	 * lookup is done using {@link Class#getResourceAsStream(String)}. The
	 * default file is {@value}.
	 */
	private static final String SPECIES_SYNONYM_FILE = "/species.txt";
	private static final double MAGIC_SPACER_NUMBER = 10.0;	// spacer factor used to keep genome spacing reasonable
	private final static SeqMapView gviewer = Application.getSingleton().getMapView();
	// versions associated with a given genome.
	static final Map<String, List<GenericVersion>> species2genericVersionList =
			new LinkedHashMap<String, List<GenericVersion>>();	// the list of versions associated with the species
	static final Map<String, String> versionName2species =
			new HashMap<String, String>();	// the species associated with the given version.

	public static Map<String, String> getVersionName2Species() {
		return versionName2species;
	}

	public static Map<String, List<GenericVersion>> getSpecies2Generic() {
		return species2genericVersionList;
	}
	/**
	 * Private copy of the default Synonym lookup
	 *
	 * @see SynonymLookup#getDefaultLookup()
	 */
	private static final SynonymLookup LOOKUP = SynonymLookup.getDefaultLookup();

	static {
		try {
			SpeciesLookup.load(GeneralLoadUtils.class.getResourceAsStream(SPECIES_SYNONYM_FILE));
		} catch (IOException ex) {
			Logger.getLogger(GeneralLoadUtils.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			GeneralUtils.safeClose(GeneralLoadUtils.class.getResourceAsStream(SPECIES_SYNONYM_FILE));
		}
	}
	/**
	 * Map to store directory name associated with the server on a cached
	 * server.
	 */
	private static Map<String, String> servermapping = new HashMap<String, String>();

	/**
	 * 
	 */
//	private static RegionFinder regionFinder = new DefaultRegionFinder();
	
	/**
	 * Add specified server, finding species and versions associated with it.
	 *
	 * @param serverName
	 * @param serverURL
	 * @param serverType
	 * @return success of server add.
	 */
	public static GenericServer addServer(ServerList serverList, ServerTypeI serverType,
			String serverName, String serverURL, int order, boolean isDefault) {
		/*
		 * should never happen
		 */
		if (serverType == ServerTypeI.LocalFiles) {
			return null;
		}

		GenericServer gServer = serverList.addServer(serverType,
				serverName, serverURL, true, order, isDefault);
		if (gServer == null) {
			return null;
		}

		discoverServer(gServer);

		return gServer;
	}

	public static void removeServer(GenericServer server) {
		Iterator<Map.Entry<String, List<GenericVersion>>> entryIterator = species2genericVersionList.entrySet().iterator();
		Map.Entry<String, List<GenericVersion>> entry;
		Iterator<GenericVersion> versionIterator;
		GenericVersion version;

		while (entryIterator.hasNext()) {
			entry = entryIterator.next();
			versionIterator = entry.getValue().iterator();

			while (versionIterator.hasNext()) {
				version = versionIterator.next();

				if (version.gServer == server) {
					GeneralLoadView.getLoadView().removeAllFeautres(version.getFeatures());
					version.clear();
					versionIterator.remove();
				}
			}
			if (entry.getValue().isEmpty()) {
				entryIterator.remove();
			}
		}
		server.setEnabled(false);
		if (server.serverType == null) {
			IGBServiceImpl.getInstance().getRepositoryChangerHolder().repositoryRemoved(server.URL);
		}
	}
	private static final VersionDiscoverer versionDiscoverer = new VersionDiscoverer() {

		@Override
		public GenericVersion discoverVersion(String versionID,
				String versionName, GenericServer gServer,
				Object versionSourceObj, String speciesName) {
			return GeneralLoadUtils.discoverVersion(versionID, versionName, gServer, versionSourceObj, speciesName);
		}

		@Override
		public String versionName2Species(String versionName) {
			return versionName2species.get(versionName);
		}
	};

	public static boolean discoverServer(GenericServer gServer) {
		if (gServer.isPrimary()) {
			return true;
		}
		if (gServer.serverType == null) { // bundle repository
			return IGBServiceImpl.getInstance().getRepositoryChangerHolder().repositoryAdded(gServer.URL);
		}

		Application.getSingleton().addNotLockedUpMsg("Loading server " + gServer + " (" + gServer.serverType.toString() + ")");
		try {
			if (gServer == null || gServer.serverType == ServerTypeI.LocalFiles) {
				// should never happen
				return false;
			}
			if (gServer.serverType != null) {
				GenericServer primaryServer = ServerList.getServerInstance().getPrimaryServer();
				URL primaryURL = getServerDirectory(gServer.URL);
				if (!gServer.serverType.getSpeciesAndVersions(gServer, primaryServer, primaryURL, versionDiscoverer)) {
					ServerList.getServerInstance().fireServerInitEvent(gServer, ServerStatus.NotResponding, false);
					gServer.setEnabled(false);
					return false;
				}
			}
			ServerList.getServerInstance().fireServerInitEvent(gServer, ServerStatus.Initialized);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * An AnnotatedSeqGroup was added independently of the GeneralLoadUtils.
	 * Update GeneralLoadUtils state.
	 *
	 * @param aseq
	 * @return genome version
	 */
	public static GenericVersion getUnknownVersion(AnnotatedSeqGroup aseq) {
		String versionName = aseq.getID();
		String speciesName = "-- Unknown -- " + versionName;	// make it distinct, but also make it appear at the top of the species list.

		GenericServer server = ServerList.getServerInstance().getLocalFilesServer();

		return discoverVersion(versionName, versionName, server, null, speciesName);
	}

	public static GenericVersion getIGBFilesVersion(AnnotatedSeqGroup group, String speciesName) {
		return getXFilesVersion(ServerList.getServerInstance().getIGBFilesServer(), group, speciesName);
	}

	/**
	 * An AnnotatedSeqGroup was added independently of the GeneralLoadUtils.
	 * Update GeneralLoadUtils state.
	 *
	 * @return genome version
	 */
	public static GenericVersion getLocalFilesVersion(AnnotatedSeqGroup group, String speciesName) {
		return getXFilesVersion(ServerList.getServerInstance().getLocalFilesServer(), group, speciesName);
	}

	private static GenericVersion getXFilesVersion(GenericServer server, AnnotatedSeqGroup group, String speciesName) {
		String versionName = group.getID();
		if (speciesName == null) {
			speciesName = "-- Unknown -- " + versionName;	// make it distinct, but also make it appear at the top of the species list
		}

		for (GenericVersion gVersion : group.getEnabledVersions()) {
			if (gVersion.gServer == server) {
				return gVersion;
			}
		}

		return discoverVersion(versionName, versionName, server, null, speciesName);
	}

	private static synchronized GenericVersion discoverVersion(String versionID, String versionName, GenericServer gServer, Object versionSourceObj, String speciesName) {
		// Make sure we use the preferred synonym for the genome version.
		String preferredVersionName = LOOKUP.getPreferredName(versionName);
		AnnotatedSeqGroup group = gmodel.addSeqGroup(preferredVersionName); // returns existing group if found, otherwise creates a new group

		GenericVersion gVersion = new GenericVersion(group, versionID, preferredVersionName, gServer, versionSourceObj);
		List<GenericVersion> gVersionList = getSpeciesVersionList(speciesName);
		versionName2species.put(preferredVersionName, speciesName);
		if (!gVersionList.contains(gVersion)) {
			gVersionList.add(gVersion);
		}
		group.addVersion(gVersion);
		return gVersion;
	}

	/**
	 * Get list of versions for given species. Create it if it doesn't exist.
	 *
	 * @param speciesName
	 * @return list of versions for the given species.
	 */
	private static List<GenericVersion> getSpeciesVersionList(String speciesName) {
		List<GenericVersion> gVersionList;
		if (!species2genericVersionList.containsKey(speciesName)) {
			gVersionList = new ArrayList<GenericVersion>();
			species2genericVersionList.put(speciesName, gVersionList);
		} else {
			gVersionList = species2genericVersionList.get(speciesName);
		}
		return gVersionList;
	}

	/**
	 * Returns the list of features for the genome with the given version name.
	 * The list may (rarely) be empty, but never null.
	 */
	public static List<GenericFeature> getFeatures(AnnotatedSeqGroup group) {
		// There may be more than one server with the same versionName.  Merge all the version names.
		List<GenericFeature> featureList = new ArrayList<GenericFeature>();
		if (group != null) {
			Set<GenericVersion> versions = group.getEnabledVersions();
			if (versions != null) {
				for (GenericVersion gVersion : versions) {
					featureList.addAll(gVersion.getFeatures());
				}
			}
		}
		return featureList;
	}

	/**
	 * Only want to display features with visible attribute set to true.
	 *
	 * @return list of visible features
	 */
	public static List<GenericFeature> getVisibleFeatures() {
		AnnotatedSeqGroup group = GenometryModel.getGenometryModel().getSelectedSeqGroup();

		List<GenericFeature> visibleFeatures = new ArrayList<GenericFeature>();
		for (GenericFeature gFeature : getFeatures(group)) {
			if (gFeature.isVisible()) {
				visibleFeatures.add(gFeature);
			}
		}

		return visibleFeatures;
	}

	/*
	 * Returns the list of features for currently selected group.
	 */
	public static List<GenericFeature> getSelectedVersionFeatures() {
		AnnotatedSeqGroup group = GenometryModel.getGenometryModel().getSelectedSeqGroup();
		return getFeatures(group);
	}

	/**
	 * Returns the list of servers associated with the given versions.
	 *
	 * @param features -- assumed to be non-null.
	 * @return A list of servers associated with the given versions.
	 */
	public static List<GenericServer> getServersWithAssociatedFeatures(List<GenericFeature> features) {
		List<GenericServer> serverList = new ArrayList<GenericServer>();
		for (GenericFeature gFeature : features) {
			if (!serverList.contains(gFeature.gVersion.gServer)) {
				serverList.add(gFeature.gVersion.gServer);
			}
		}
		// make sure these servers always have the same order
		Collections.sort(serverList);
		return serverList;
	}

	/**
	 * Load the annotations for the given version. This is specific to one
	 * server.
	 *
	 * @param gVersion
	 */
	private static void loadFeatureNames(final GenericVersion gVersion) {
		boolean autoload = PreferenceUtils.getBooleanParam(
				PreferenceUtils.AUTO_LOAD, PreferenceUtils.default_auto_load);
		if (!gVersion.getFeatures().isEmpty()) {
			if (DEBUG) {
				System.out.println("Feature names are already loaded.");
			}
			return;
		}
		boolean needToDisplay = false; 
		if (gVersion.gServer.serverType == null) {
			System.out.println("WARNING: Unknown server class " + gVersion.gServer.serverType);
		} else {
			gVersion.gServer.serverType.discoverFeatures(gVersion, autoload);
			for (GenericFeature feature : gVersion.getFeatures()) {
				if (!feature.isVisible() && feature.featureProps != null && "yes".equalsIgnoreCase(feature.featureProps.get("auto_select"))) {
					new FeatureLoadAction(feature).actionPerformed(null);
					needToDisplay = true;
				}
			}
		}
		if (needToDisplay) {
			ThreadUtils.runOnEventQueue(new Runnable() {
				public void run() {
					gviewer.getSeqMap().updateWidget();
				}
			});
		}
	}

	/**
	 * Make sure this genome version has been initialized.
	 *
	 * @param versionName
	 */
	public static void initVersionAndSeq(final String versionName) {
		if (versionName == null) {
			return;
		}
		AnnotatedSeqGroup group = gmodel.getSeqGroup(versionName);
		for (GenericVersion gVersion : group.getEnabledVersions()) {
			if (!gVersion.isInitialized()) {
				loadFeatureNames(gVersion);
				gVersion.setInitialized();
			}
		}
		if (group.getSeqCount() == 0) {
			loadChromInfo(group);
		}
		addGenomeVirtualSeq(group);	// okay to run this multiple times
	}

	/**
	 * Load the sequence info for the given group. Try loading from DAS/2 before
	 * loading from DAS; chances are DAS/2 will be faster, and that the
	 * chromosome names will be closer to what is expected.
	 */
	private static void loadChromInfo(AnnotatedSeqGroup group) {
		for (ServerTypeI serverType : ServerUtils.getServerTypes()) {
			for (GenericVersion gVersion : group.getEnabledVersions()) {
				if (gVersion.gServer.serverType != serverType) {
					continue;
				}
				serverType.discoverChromosomes(gVersion.versionSourceObj);
				return;
			}
		}
	}

	private static void addGenomeVirtualSeq(AnnotatedSeqGroup group) {
		int chrom_count = group.getSeqCount();
		if (chrom_count <= 1) {
			// no need to make a virtual "genome" chrom if there is only a single chromosome
			return;
		}

		int spacer = determineSpacer(group, chrom_count);
		double seqBounds = determineSeqBounds(group, spacer, chrom_count);
		if (seqBounds > Integer.MAX_VALUE) {
			return;
		}
		if (group.getSeq(IGBConstants.GENOME_SEQ_ID) != null) {
			return; // return if we've already created the virtual genome
		}

		BioSeq genome_seq = null;
		try {
			genome_seq = group.addSeq(IGBConstants.GENOME_SEQ_ID, 0);
		} catch (IllegalStateException ex) {
			// due to multithreading, it's possible that this sequence has been created by another thread while doing this test.
			// we can safely return in this case.
			Logger.getLogger(GeneralLoadUtils.class.getName()).fine("Ignoring multithreading illegal state exception.");
			return;
		}

		for (int i = 0; i < chrom_count; i++) {
			BioSeq chrom_seq = group.getSeq(i);
			if (chrom_seq == genome_seq) {
				continue;
			}

			// Add seq to virtual genome.  Keep values above 0 if possible.
			addSeqToVirtualGenome(seqBounds < 0 ? 0.0 : default_genome_min, spacer, genome_seq, chrom_seq);
		}
	}

	/**
	 * Determine size of spacer between chromosomes in whole genome view.
	 */
	private static int determineSpacer(AnnotatedSeqGroup group, int chrom_count) {
		double spacer = 0;
		for (BioSeq chrom_seq : group.getSeqList()) {
			spacer += (chrom_seq.getLengthDouble()) / chrom_count;
		}
		return (int) (spacer / MAGIC_SPACER_NUMBER);
	}

	/**
	 * Make sure virtual genome doesn't overflow integer bounds.
	 *
	 * @param group
	 * @return true or false
	 */
	private static double determineSeqBounds(AnnotatedSeqGroup group, int spacer, int chrom_count) {
		double seq_bounds = default_genome_min;

		for (int i = 0; i < chrom_count; i++) {
			BioSeq chrom_seq = group.getSeq(i);
			int clength = chrom_seq.getLength();
			seq_bounds += clength + spacer;
		}
		return seq_bounds;
	}

	private static void addSeqToVirtualGenome(double genome_min, int spacer, BioSeq genome_seq, BioSeq chrom) {
		double glength = genome_seq.getLengthDouble();
		int clength = chrom.getLength();
		double new_glength = glength + clength + spacer;

		genome_seq.setBoundsDouble(genome_min, genome_min + new_glength);

		MutableSeqSymmetry mapping = (MutableSeqSymmetry) genome_seq.getComposition();
		if (mapping == null) {
			mapping = new SimpleMutableSeqSymmetry();
			mapping.addSpan(new MutableDoubleSeqSpan(genome_min, genome_min + clength, genome_seq));
			genome_seq.setComposition(mapping);
		} else {
			MutableDoubleSeqSpan mspan = (MutableDoubleSeqSpan) mapping.getSpan(genome_seq);
			mspan.setDouble(genome_min, genome_min + new_glength, genome_seq);
		}

		MutableSeqSymmetry child = new SimpleMutableSeqSymmetry();
		// using doubles for coords, because may end up with coords > MAX_INT
		child.addSpan(new MutableDoubleSeqSpan(glength + genome_min, glength + genome_min + clength, genome_seq));
		child.addSpan(new MutableDoubleSeqSpan(0, clength, chrom));

		mapping.addChild(child);
	}

	protected static void bufferDataForAutoload() {
		SeqSpan visible = gviewer.getVisibleSpan();
		BioSeq seq = gmodel.getSelectedSeq();

		if (visible == null || seq == null) {
			return;
		}

		int length = visible.getLength();
		int min = visible.getMin();
		int max = visible.getMax();
		SeqSpan leftSpan = new SimpleSeqSpan(Math.max(0, min - length), min, seq);
		SeqSpan rightSpan = new SimpleSeqSpan(max, Math.min(seq.getLength(), max + length), seq);

		for (GenericFeature gFeature : GeneralLoadUtils.getSelectedVersionFeatures()) {
			if (gFeature.getLoadStrategy() != LoadStrategy.AUTOLOAD) {
				continue;
			}

			if (checkBeforeLoading(gFeature)) {
				loadAndDisplaySpan(leftSpan, gFeature);
				loadAndDisplaySpan(rightSpan, gFeature);
			}
		}
	}

	private static boolean checkBeforeLoading(GenericFeature gFeature) {
		if (gFeature.getLoadStrategy() == LoadStrategy.NO_LOAD) {
			return false;	// should never happen
		}

//		Thread may have been cancelled. So removing test for now.
//		//Already loaded the data.
//		if((gFeature.gVersion.gServer.serverType == ServerType.LocalFiles)
//				&& ((QuickLoad)gFeature.symL).getSymLoader() instanceof SymLoaderInstNC){
//			return false;
//		}

		BioSeq selected_seq = gmodel.getSelectedSeq();
		BioSeq visible_seq = gviewer.getViewSeq();
		if ((selected_seq == null || visible_seq == null) && (gFeature.gVersion.gServer.serverType != ServerTypeI.LocalFiles)) {
			//      ErrorHandler.errorPanel("ERROR", "You must first choose a sequence to display.");
			//System.out.println("@@@@@ selected chrom: " + selected_seq);
			//System.out.println("@@@@@ visible chrom: " + visible_seq);
			return false;
		}
		if (visible_seq != selected_seq) {
			System.out.println("ERROR, VISIBLE SPAN DOES NOT MATCH GMODEL'S SELECTED SEQ!!!");
			System.out.println("   selected seq: " + selected_seq.getID());
			System.out.println("   visible seq: " + visible_seq.getID());
			return false;
		}

		return true;
	}

	/**
	 * Load and display annotations (requested for the specific feature). Adjust
	 * the load status accordingly.
	 */
	static public void loadAndDisplayAnnotations(GenericFeature gFeature) {
		if (!checkBeforeLoading(gFeature)) {
			return;
		}

		BioSeq selected_seq = gmodel.getSelectedSeq();
		if (selected_seq == null) {
			ErrorHandler.errorPanel("Couldn't find genome data on server for file, genome = " + gFeature.gVersion.group.getID());
			return;
		}
		SeqSpan overlap = null;
		if (gFeature.getLoadStrategy() == LoadStrategy.VISIBLE || gFeature.getLoadStrategy() == LoadStrategy.AUTOLOAD) {
			overlap = gviewer.getVisibleSpan();
			// TODO: Investigate edge case at max
			if (overlap.getMin() == selected_seq.getMin() && overlap.getMax() == selected_seq.getMax()) {
				overlap = new SimpleSeqSpan(selected_seq.getMin(), selected_seq.getMax() - 1, selected_seq);
			}
		} else if (gFeature.getLoadStrategy() == LoadStrategy.GENOME /*|| gFeature.getLoadStrategy() == LoadStrategy.CHROMOSOME*/) {
			// TODO: Investigate edge case at max
			overlap = new SimpleSeqSpan(selected_seq.getMin(), selected_seq.getMax() - 1, selected_seq);
		}

		loadAndDisplaySpan(overlap, gFeature);
	}

	public static void loadAndDisplaySpan(final SeqSpan span, final GenericFeature feature) {
		SeqSymmetry optimized_sym = null;
		// special-case chp files, due to their LazyChpSym DAS/2 loading
		if ((feature.gVersion.gServer.serverType == ServerTypeI.QuickLoad || feature.gVersion.gServer.serverType == ServerTypeI.LocalFiles)
				&& ((QuickLoadSymLoader) feature.symL).extension.endsWith("chp")) {
			feature.setLoadStrategy(LoadStrategy.GENOME);	// it should be set to this already.  But just in case...
			optimized_sym = new SimpleMutableSeqSymmetry();
			((SimpleMutableSeqSymmetry) optimized_sym).addSpan(span);
			loadFeaturesForSym(optimized_sym, feature);
			return;
		}

		optimized_sym = feature.optimizeRequest(span);

		if (feature.getLoadStrategy() != LoadStrategy.GENOME || feature.gVersion.gServer.serverType == ServerTypeI.DAS2) {
			// Don't iterate for DAS/2.  "Genome" there is used for autoloading.

			if (checkBamAndSamLoading(feature, optimized_sym)) {
				return;
			}

			loadFeaturesForSym(optimized_sym, feature);
			return;
		}

		//Since Das1 does not have whole genome return if it is not Quickload or LocalFile
		if (feature.gVersion.gServer.serverType != ServerTypeI.QuickLoad && feature.gVersion.gServer.serverType != ServerTypeI.LocalFiles) {
			return;
		}

		//If Loading whole genome for unoptimized file then load everything at once.
		if (((QuickLoadSymLoader) feature.symL).getSymLoader() instanceof SymLoaderInst) {
			if (optimized_sym != null) {
				loadAllSymmetriesThread(feature);
			}
			return;
		}

		iterateSeqList(feature);
	}

	static void iterateSeqList(final GenericFeature feature) {

		CThreadWorker<Void, BioSeq> worker = new CThreadWorker<Void, BioSeq>(MessageFormat.format(IGBConstants.BUNDLE.getString("loadFeature"), feature.featureName)) {

			@Override
			protected Void runInBackground() {
				try {
					final MutableLong currentPosition = new MutableLong(0);
					PositionCalculator positionCalculator = new PositionCalculator() {
						@Override
						public long getCurrentPosition() {
							return currentPosition.longValue();
						}
					};
					List<BioSeq> chrList = feature.symL.getChromosomeList();
					long totalLength = 0;
					for (BioSeq seq : chrList) {
						totalLength += seq.getLength();
					}
					ProgressUpdater progressUpdater = new ProgressUpdater("Load whole feature " + feature.featureName, 0, totalLength, positionCalculator);
					if (CThreadHolder.getInstance().getCurrentCThreadWorker() != null) {
						CThreadHolder.getInstance().getCurrentCThreadWorker().setProgressUpdater(progressUpdater);
					}
					final BioSeq current_seq = gmodel.getSelectedSeq();
					Thread thread = Thread.currentThread();

					if (current_seq != null) {
						loadOnSequence(current_seq);
						currentPosition.setValue(currentPosition.getValue() + current_seq.getLength());
						publish(current_seq);
					}

					for (BioSeq seq : chrList) {
						if (seq == current_seq) {
							continue;
						}

						if (thread.isInterrupted()) {
							break;
						}
						loadOnSequence(seq);
						currentPosition.setValue(currentPosition.getValue() + current_seq.getLength());
					}
				} catch (Exception ex) {
					((QuickLoadSymLoader) feature.symL).logException(ex);
				}
				return null;
			}

			@Override
			protected void process(List<BioSeq> seqs) {
				gviewer.setAnnotatedSeq(seqs.get(0), true, true);
			}

			@Override
			protected void finished() {
				if (isCancelled()) {
					feature.setLoadStrategy(LoadStrategy.NO_LOAD);
				}

				BioSeq seq = gmodel.getSelectedSeq();
				if (seq != null) {
					gviewer.setAnnotatedSeq(seq, true, true);
				} else if (gmodel.getSelectedSeqGroup() != null) {
					if (gmodel.getSelectedSeqGroup().getSeqCount() > 0) {
						// This can happen when loading a brand-new genome
						gmodel.setSelectedSeq(gmodel.getSelectedSeqGroup().getSeq(0));
					}
				}
				setLastRefreshStatus(feature, true);
				GeneralLoadView.getLoadView().refreshDataManagementView();
			}

			private void loadOnSequence(BioSeq seq) {
				if (IGBConstants.GENOME_SEQ_ID.equals(seq.getID())) {
					return; // don't load into Whole Genome
				}

				try {
					SeqSymmetry optimized_sym = feature.optimizeRequest(new SimpleSeqSpan(seq.getMin(), seq.getMax() - 1, seq));
					if (optimized_sym != null) {
						loadFeaturesForSym(feature, optimized_sym);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};

		CThreadHolder.getInstance().execute(feature, worker);
	}

	private static void loadFeaturesForSym(final SeqSymmetry optimized_sym, final GenericFeature feature) throws OutOfMemoryError {
		if (optimized_sym == null) {
			Logger.getLogger(GeneralLoadUtils.class.getName()).log(
					Level.INFO, "All of new query covered by previous queries for feature {0}", feature.featureName);
			setLastRefreshStatus(feature, false);
			return;
		}

		final int seq_count = gmodel.getSelectedSeqGroup().getSeqCount();		
		final CThreadWorker<List<SeqSymmetry>, Object> worker = new CThreadWorker<List<SeqSymmetry>, Object>("Loading feature " + feature.featureName, Thread.MIN_PRIORITY) {
			
			@Override
			protected List<SeqSymmetry> runInBackground() {
				try {
					return loadFeaturesForSym(feature, optimized_sym);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return Collections.<SeqSymmetry>emptyList();
			}

			@Override
			protected void finished() {
				
				BioSeq aseq = gmodel.getSelectedSeq();

				if (aseq != null) {
					gviewer.setAnnotatedSeq(aseq, true, true);
				} else if (gmodel.getSelectedSeqGroup() != null && gmodel.getSelectedSeqGroup().getSeqCount() > 0) {
					// This can happen when loading a brand-new genome
					aseq = gmodel.getSelectedSeqGroup().getSeq(0);
					gmodel.setSelectedSeq(aseq);
				}

				//Since sequence are never removed so if no. of sequence increases then refresh sequence table.
				if (gmodel.getSelectedSeqGroup() != null && gmodel.getSelectedSeqGroup().getSeqCount() > seq_count) {
					SeqGroupView.getInstance().refreshTable();
				}
				
				GeneralLoadView.getLoadView().refreshDataManagementView();
								
				if (this.isCancelled()) {
					return;
				}

				try {
					List<SeqSymmetry> result = get();
					setLastRefreshStatus(feature, result.size() > 0);
				} catch (Exception ex) {
					Logger.getLogger(GeneralLoadUtils.class.getName()).log(
							Level.SEVERE, "Unable to get refresh action result.", ex);
				}
			}
		};

		CThreadHolder.getInstance().execute(feature, worker);
	}

	//TO DO: Make this private again.
	public static List<SeqSymmetry> loadFeaturesForSym(GenericFeature feature, SeqSymmetry optimized_sym) throws OutOfMemoryError, IOException {
		List<SeqSpan> optimized_spans = new ArrayList<SeqSpan>();
		List<SeqSpan> spans = new ArrayList<SeqSpan>();
		List<SeqSymmetry> loaded = new ArrayList<SeqSymmetry>();
		convertSymToSpanList(optimized_sym, spans);
		optimized_spans.addAll(spans);
		if (feature.gVersion.gServer.serverType == null) {
			return Collections.<SeqSymmetry>emptyList();
		}
		Thread thread = Thread.currentThread();
		
		for (SeqSpan optimized_span : optimized_spans) {

			feature.addLoadingSpanRequest(optimized_span);	// this span is requested to be loaded.

			loaded.addAll(feature.gVersion.gServer.serverType.loadFeatures(optimized_span, feature));
				
			if (thread.isInterrupted()) {
				feature.removeCurrentRequest(optimized_span);
				break;
			}

			feature.addLoadedSpanRequest(optimized_span);
		}

		return loaded;
	}

	private static boolean checkBamAndSamLoading(GenericFeature feature, SeqSymmetry optimized_sym) {
		//start max
		boolean check = GeneralLoadView.getLoadView().isLoadingConfirm();
		GeneralLoadView.getLoadView().setShowLoadingConfirm(false);
		if (check && optimized_sym != null && feature.getExtension() != null
				&& (feature.getExtension().endsWith("bam") || feature.getExtension().endsWith("sam"))) {
			String message = "Region in view is big (> 500k), do you want to continue?";
			int childrenCount = optimized_sym.getChildCount();
			int spanWidth = 0;
			for (int childIndex = 0; childIndex < childrenCount; childIndex++) {
				SeqSymmetry child = optimized_sym.getChild(childIndex);
				for (int spanIndex = 0; spanIndex < child.getSpanCount(); spanIndex++) {
					spanWidth = spanWidth + (child.getSpan(spanIndex).getMax() - child.getSpan(spanIndex).getMin());
				}
			}

			if (spanWidth > 500000) {
				return !(Application.confirmPanel(message, PreferenceUtils.getTopNode(),
						PreferenceUtils.CONFIRM_BEFORE_LOAD, PreferenceUtils.default_confirm_before_load));
			}
		}
		return false;
		//end max
	}

	public static void setLastRefreshStatus(GenericFeature feature, boolean result) {
		if (result) {
			feature.setLastRefreshStatus(RefreshStatus.DATA_LOADED);
		} else {
			if (feature.getMethods().isEmpty()) {
				feature.setLastRefreshStatus(RefreshStatus.NO_DATA_LOADED);
			} else {
				feature.setLastRefreshStatus(RefreshStatus.NO_NEW_DATA_LOADED);
			}
		}
		//LoadModeTable.updateVirtualFeatureList();
	}

	/**
	 * Walk the SeqSymmetry, converting all of its children into spans.
	 *
	 * @param sym the SeqSymmetry to walk.
	 */
	private static void convertSymToSpanList(SeqSymmetry sym, List<SeqSpan> spans) {
		int childCount = sym.getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				convertSymToSpanList(sym.getChild(i), spans);
			}
		} else {
			int spanCount = sym.getSpanCount();
			for (int i = 0; i < spanCount; i++) {
				spans.add(sym.getSpan(i));
			}
		}
	}

	/**
	 * Get residues from servers: DAS/2, Quickload, or DAS/1.
	 * Also gets partial residues.
	 *
	 * @param genomeVersionName -- name of the genome.
	 * @param span	-- May be null. If not, then it's used for partial loading.
	 */
	// Most confusing thing here -- certain parsers update the composition, and certain ones do not.
	// DAS/1 and partial loading in DAS/2 do not update the composition, so it's done separately.
	public static boolean getResidues(Set<GenericVersion> versionsWithChrom, String genomeVersionName, BioSeq aseq, int min, int max, SeqSpan span) {
		if (span == null) {
			span = new SimpleSeqSpan(min, max, aseq);
		}
		List<GenericVersion> versions = new ArrayList<GenericVersion>(versionsWithChrom);
		String seq_name = aseq.getID();
		boolean residuesLoaded = false;
		for (GenericServer server : ServerList.getServerInstance().getAllServers()) {
			if (!server.isEnabled()) {
				continue;
			}
			String serverDescription = server.serverName + " " + server.serverType;
//			String msg = MessageFormat.format(IGBConstants.BUNDLE.getString("loadingSequence"), seq_name, serverDescription);
//			Application.getSingleton().addNotLockedUpMsg(msg);
			if (server.serverType != null && server.serverType.getResidues(server, versions, genomeVersionName, aseq, min, max, span)) {
				residuesLoaded = true;
			}
//			Application.getSingleton().removeNotLockedUpMsg(msg);
			if (residuesLoaded) {
				Application.getSingleton().setStatus(MessageFormat.format(IGBConstants.BUNDLE.getString("completedLoadingSequence"),
					seq_name, min, max, serverDescription));
				return true;
			}
		}
		Application.getSingleton().setStatus("");
		return false;
	}

	/**
	 * Load residues on span. First, attempt to load them with DAS/2 servers.
	 * Second, attempt to load them with QuickLoad servers. Third, attempt to
	 * load them with DAS/1 servers.
	 *
	 * @param aseq
	 * @param span	-- may be null, if the entire sequence is requested.
	 * @return true if succeeded.
	 */
	static boolean loadResidues(String genomeVersionName, BioSeq aseq, int min, int max, SeqSpan span) {

		/*
		 * This test does not work properly, so it's being commented out for
		 * now.
		 *
		 * if (aseq.isComplete()) { if (DEBUG) { System.out.println("already
		 * have residues for " + seq_name); } return false; }
		 */

		// Determine list of servers that might have this chromosome sequence.
		Set<GenericVersion> versionsWithChrom = new HashSet<GenericVersion>();
		versionsWithChrom.addAll(aseq.getSeqGroup().getEnabledVersions());

		if ((min <= 0) && (max >= aseq.getLength())) {
			min = 0;
			max = aseq.getLength();
		}

		if (aseq.isAvailable(min, max)) {
			Logger.getLogger(GeneralLoadUtils.class.getName()).log(Level.INFO,
					"All residues in range are already loaded on sequence {0}", new Object[]{aseq});
			return true;
		}

//		Application.getSingleton().addNotLockedUpMsg("Loading residues for "+aseq.getID());

		return getResidues(versionsWithChrom, genomeVersionName, aseq, min, max, span);
	}

	/**
	 * Get synonyms of version.
	 *
	 * @param versionName - version name
	 * @return a friendly HTML string of version synonyms (not including
	 * versionName).
	 */
	public static String listSynonyms(String versionName) {
		StringBuilder synonymBuilder = new StringBuilder(100);
		synonymBuilder.append("<html>").append(IGBConstants.BUNDLE.getString("synonymList"));
		Set<String> synonymSet = LOOKUP.getSynonyms(versionName);
		for (String synonym : synonymSet) {
			if (synonym.equalsIgnoreCase(versionName)) {
				continue;
			}
			synonymBuilder.append("<p>").append(synonym).append("</p>");
		}
		if (synonymSet.size() <= 1) {
			synonymBuilder.append(IGBConstants.BUNDLE.getString("noSynonyms"));
		}
		synonymBuilder.append("</html>");
		return synonymBuilder.toString();
	}

	/**
	 * Method to load server directory mapping.
	 */
	public static void loadServerMapping() {
		GenericServer primaryServer = ServerList.getServerInstance().getPrimaryServer();
		if (primaryServer == null) {
			return;
		}
		InputStream istr = null;
		InputStreamReader ireader = null;
		BufferedReader br = null;

		try {
			try {
				istr = LocalUrlCacher.getInputStream(primaryServer.friendlyURL.toExternalForm() + SERVER_MAPPING);
			} catch (Exception e) {
				Logger.getLogger(GeneralLoadUtils.class.getName()).log(
						Level.SEVERE, "Couldn''t open ''{0}" + SERVER_MAPPING + "\n:  {1}", new Object[]{primaryServer.friendlyURL.toExternalForm(), e.toString()});
				istr = null; // dealt with below
			}
			if (istr == null) {
				Logger.getLogger(GeneralLoadUtils.class.getName()).log(
						Level.INFO, "Could not load server mapping contents from\n{0}" + SERVER_MAPPING, primaryServer.friendlyURL.toExternalForm());
				return;
			}
			ireader = new InputStreamReader(istr);
			br = new BufferedReader(ireader);
			String line;
			while ((line = br.readLine()) != null) {
				if ((line.length() == 0) || line.startsWith("#")) {
					continue;
				}

				String[] fields = tab_regex.split(line);
				if (fields.length >= 2) {
					String serverURL = fields[0];
					String dirURL = primaryServer.URL + fields[1];
					servermapping.put(serverURL, dirURL);
				}
			}
		} catch (Exception ex) {
			ErrorHandler.errorPanel("Error loading server mapping", ex, Level.SEVERE);
		} finally {
			GeneralUtils.safeClose(istr);
			GeneralUtils.safeClose(ireader);
			GeneralUtils.safeClose(br);
		}
	}

	/**
	 * Get directory url on cached server from servermapping map.
	 *
	 * @param url	URL of the server.
	 * @return	Returns a directory if exists else null.
	 */
	public static URL getServerDirectory(String url) {
		if (ServerList.getServerInstance().getPrimaryServer() == null) {
			return null;
		}

		for (Entry<String, String> primary : servermapping.entrySet()) {
			if (url.equals(primary.getKey())) {
				try {
					return new URL(primary.getValue());
				} catch (MalformedURLException ex) {
					Logger.getLogger(GeneralLoadUtils.class.getName()).log(Level.SEVERE, null, ex);
					return null;
				}
			}
		}

		return null;
	}

	/**
	 * Set autoload variable in features.
	 *
	 * @param autoload
	 */
	public static void setFeatureAutoLoad(boolean autoload) {
		for (List<GenericVersion> genericVersions : species2genericVersionList.values()) {
			for (GenericVersion genericVersion : genericVersions) {
				for (GenericFeature genericFeature : genericVersion.getFeatures()) {
					if (autoload) {
						genericFeature.setAutoload(autoload);
					}
				}
			}
		}

		//It autoload data is selected then load.
		if (autoload) {
			GeneralLoadView.loadWholeRangeFeatures(null);
			GeneralLoadView.getLoadView().refreshTreeView();
			GeneralLoadView.getLoadView().refreshDataManagementView();
		}
	}

	public static List<String> getSpeciesList() {
		final List<String> speciesList = new ArrayList<String>();
		speciesList.addAll(species2genericVersionList.keySet());
		Collections.sort(speciesList);
		return speciesList;
	}

	public static List<String> getGenericVersions(final String speciesName) {
		final List<GenericVersion> versionList = species2genericVersionList.get(speciesName);
		final List<String> versionNames = new ArrayList<String>();
		if (versionList != null) {
			for (GenericVersion gVersion : versionList) {
				// the same versionName name may occur on multiple servers
				String versionName = gVersion.versionName;
				if (!versionNames.contains(versionName)) {
					versionNames.add(versionName);
				}
			}
			Collections.sort(versionNames, new StringVersionDateComparator());
		}
		return versionNames;
	}

	public static void openURI(URI uri, String fileName, AnnotatedSeqGroup loadGroup, String speciesName, boolean loadAsTrack) {
		// If server requires authentication then.
		// If it cannot be authenticated then don't add the feature.
		if (!LocalUrlCacher.isValidURI(uri)) {
			ErrorHandler.errorPanel("UNABLE TO FIND URL", uri + "\n URL provided not found or times out: ",Level.WARNING);
			return;
		}

		GenericFeature gFeature = getFeature(uri, fileName, speciesName, loadGroup, loadAsTrack);

		if (gFeature == null) {
			return;
		}

		if (gFeature != null) {
			addFeature(gFeature);
			
			if(gFeature.getLoadStrategy() == LoadStrategy.VISIBLE /*||
					gFeature.getLoadStrategy() == LoadStrategy.CHROMOSOME*/){
				Application.infoPanel(GenericFeature.howtoloadmsg, PreferenceUtils.getTopNode(), 
				GenericFeature.show_how_to_load, GenericFeature.default_show_how_to_load);
			}
		}
	}
	
	public static void addFeature(GenericFeature gFeature){
		if (gFeature.symL != null) {
			addChromosomesForUnknownGroup(gFeature);
		}

		// force a refresh of this server		
		ServerList.getServerInstance().fireServerInitEvent(ServerList.getServerInstance().getLocalFilesServer(), ServerStatus.Initialized, true, true);

		SeqGroupView.getInstance().setSelectedGroup(gFeature.gVersion.group.getID());

		GeneralLoadView.getLoadView().refreshDataManagementView();
	}
	
	private static void addChromosomesForUnknownGroup(final GenericFeature gFeature) {
		if (((QuickLoadSymLoader) gFeature.symL).getSymLoader() instanceof SymLoaderInstNC) {
			loadAllSymmetriesThread(gFeature);
			// force a refresh of this server. This forces creation of 'genome' sequence.
			ServerList.getServerInstance().fireServerInitEvent(ServerList.getServerInstance().getLocalFilesServer(), ServerStatus.Initialized, true, true);
			return;
		}

		final AnnotatedSeqGroup loadGroup = gFeature.gVersion.group;
		final String message = MessageFormat.format(IGBConstants.BUNDLE.getString("retrieveChr"), gFeature.featureName);
		final CThreadWorker<Boolean, Object> worker = new CThreadWorker<Boolean, Object>(message) {

			@Override
			protected Boolean runInBackground() {
				try {
					for (BioSeq seq : gFeature.symL.getChromosomeList()) {
						loadGroup.addSeq(seq.getID(), seq.getLength(), gFeature.symL.uri.toString());
					}
					return true;
				} catch (Exception ex) {
					((QuickLoadSymLoader) gFeature.symL).logException(ex);
					return removeFeature("Unable to retrieve chromosome. \n Would you like to remove feature " + gFeature.featureName);
				}

			}

			@Override
			protected boolean showCancelConfirmation() {
				return removeFeature("Cancel chromosome retrieval and remove " + gFeature.featureName + "?");
			}

			private boolean removeFeature(String msg) {
				if (Application.confirmPanel(msg)) {
					if (gFeature.gVersion.removeFeature(gFeature)) {
						SeqGroupView.getInstance().refreshTable();
					}
					return true;
				}
				return false;
			}

			@Override
			protected void finished() {
				boolean result = true;
				try {
					if (!isCancelled()) {
						result = get();
					} else {
						result = false;
					}
				} catch (Exception ex) {
					Logger.getLogger(GeneralLoadUtils.class.getName()).log(Level.SEVERE, null, ex);
				}
				ServerList.getServerInstance().fireServerInitEvent(ServerList.getServerInstance().getLocalFilesServer(), ServerStatus.Initialized, true, true);
				if (result) {
					SeqGroupView.getInstance().refreshTable();
					if (loadGroup.getSeqCount() > 0 && gmodel.getSelectedSeq() == null) {
						// select a chromosomes
						gmodel.setSelectedSeq(loadGroup.getSeq(0));
					}
				} else {
					gmodel.setSelectedSeq(gmodel.getSelectedSeq());
				}
			}
		};
		CThreadHolder.getInstance().execute(gFeature, worker);
	}

	public static GenericFeature getFeature(URI uri, String fileName, String speciesName, AnnotatedSeqGroup loadGroup, boolean loadAsTrack) {
		GenericFeature gFeature = GeneralLoadUtils.getLoadedFeature(uri);
		// Test to determine if a feature with this uri is contained in the load mode table
		if (gFeature == null) {
			GenericVersion version = GeneralLoadUtils.getLocalFilesVersion(loadGroup, speciesName);
			version = setVersion(uri, loadGroup, version);

			// In case of BAM
			if (version == null) {
				return null;
			}

			// handle URL case.
			String uriString = uri.toString();
			int httpIndex = uriString.toLowerCase().indexOf("http:");
			if (httpIndex > -1) {
				// Strip off initial characters up to and including http:
				// Sometimes this is necessary, as URLs can start with invalid "http:/"
				uriString = GeneralUtils.convertStreamNameToValidURLName(uriString);
				uri = URI.create(uriString);
			}
			boolean autoload = PreferenceUtils.getBooleanParam(PreferenceUtils.AUTO_LOAD, PreferenceUtils.default_auto_load);

			Map<String, String> featureProps = null;
			SymLoader symL = ServerUtils.determineLoader(SymLoader.getExtension(uri), uri, QuickLoadSymLoader.detemineFriendlyName(uri), version.group);
			if (symL != null && symL.isResidueLoader() && loadAsTrack) {
				symL = new ResidueTrackSymLoader(symL);
				featureProps = new HashMap<String, String>();
				featureProps.put("collapsed", "true");
				featureProps.put("show2tracks", "false");
			}
			String friendlyName = QuickLoadSymLoader.detemineFriendlyName(uri);
			QuickLoadSymLoader quickLoad = SymLoader.getExtension(uri).endsWith("chp")
					? new QuickLoadSymLoaderChp(uri, friendlyName, version, symL)
					: new QuickLoadSymLoader(uri, friendlyName, version, symL);
			gFeature = new GenericFeature(fileName, featureProps, version, quickLoad, File.class, autoload);

			version.addFeature(gFeature);

			gFeature.setVisible(); // this should be automatically checked in the feature tree

			GeneralLoadView.addFeatureTier(gFeature);
		} else {
			ErrorHandler.errorPanel("Cannot add same feature",
					"The feature " + uri + " has already been added.",Level.WARNING);
		}

		return gFeature;
	}

	/**
	 * Handle file formats that has SeqGroup info.
	 */
	private static GenericVersion setVersion(URI uri, AnnotatedSeqGroup loadGroup, GenericVersion version) {
		String unzippedStreamName = GeneralUtils.stripEndings(uri.toString());
		String extension = ParserController.getExtension(unzippedStreamName);

		if (extension.equals(".bam")) {
			if (!handleBam(uri)) {
				ErrorHandler.errorPanel("Cannot open file", "Could not find index file", Level.WARNING);
				version = null;
			}
		} else if (extension.equals(".useq")) {
			loadGroup = handleUseq(uri, loadGroup);
			version = getLocalFilesVersion(loadGroup, loadGroup.getOrganism());
		} else if (extension.equals(".bar")) {
			loadGroup = handleBar(uri, loadGroup);
			version = getLocalFilesVersion(loadGroup, loadGroup.getOrganism());
		} else if (extension.equals(".bp1") || extension.equals(".bp2")) {
			loadGroup = handleBp(uri, loadGroup);
			version = getLocalFilesVersion(loadGroup, loadGroup.getOrganism());
		}

		return version;
	}

	private static boolean handleBam(URI uri) {
		try {
			return BAM.hasIndex(uri);
		} catch (IOException ex) {
			Logger.getLogger(GeneralLoadUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	/**
	 * Get AnnotatedSeqGroup for BAR file format.
	 */
	private static AnnotatedSeqGroup handleBar(URI uri, AnnotatedSeqGroup group) {
		InputStream istr = null;
		try {
			istr = LocalUrlCacher.convertURIToBufferedUnzippedStream(uri);
			List<AnnotatedSeqGroup> groups = BarParser.getSeqGroups(uri.toString(), istr, group, gmodel);
			if (groups.isEmpty()) {
				return group;
			}

			//TODO: What if there are more than one seq group ?
			if (groups.size() > 1) {
				Logger.getLogger(GeneralLoadUtils.class.getName()).log(
						Level.WARNING, "File {0} has more than one group", new Object[]{uri.toString()
						});
			}

			return groups.get(0);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			GeneralUtils.safeClose(istr);
		}

		return group;
	}

	/**
	 * Get AnnotatedSeqGroup for USEQ file format.
	 */
	private static AnnotatedSeqGroup handleUseq(URI uri, AnnotatedSeqGroup group) {
		InputStream istr = null;
		ZipInputStream zis = null;
		try {
			istr = LocalUrlCacher.getInputStream(uri.toURL());
			zis = new ZipInputStream(istr);
			zis.getNextEntry();
			ArchiveInfo archiveInfo = new ArchiveInfo(zis, false);
			AnnotatedSeqGroup gr = USeqGraphParser.getSeqGroup(archiveInfo.getVersionedGenome(), gmodel);
			if (gr != null) {
				return gr;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			GeneralUtils.safeClose(istr);
			GeneralUtils.safeClose(zis);
		}

		return group;
	}

	private static AnnotatedSeqGroup handleBp(URI uri, AnnotatedSeqGroup group) {
		InputStream istr = null;
		try {
			istr = LocalUrlCacher.convertURIToBufferedUnzippedStream(uri);
			AnnotatedSeqGroup gr = Bprobe1Parser.getSeqGroup(istr, group, gmodel);
			if (gr != null) {
				return gr;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			GeneralUtils.safeClose(istr);
		}

		return group;
	}

	/**
	 * For unoptimized file formats load symmetries and add them.
	 *
	 * @param feature
	 */
	public static void loadAllSymmetriesThread(final GenericFeature feature) {
		final QuickLoadSymLoader quickLoad = (QuickLoadSymLoader) feature.symL;
		final SeqMapView gviewer = Application.getSingleton().getMapView();

		CThreadWorker<Object, Void> worker = new CThreadWorker<Object, Void>("Loading feature " + feature.featureName) {

			@Override
			protected Object runInBackground() {
				try {
					quickLoad.loadAndAddAllSymmetries(feature);
				} catch (Exception ex) {
					quickLoad.logException(ex);
				}
				return null;
			}

			@Override
			protected void finished() {
				try {
					BioSeq aseq = GenometryModel.getGenometryModel().getSelectedSeq();
					if (aseq != null) {
						gviewer.setAnnotatedSeq(aseq, true, true);
					} else if (GenometryModel.getGenometryModel().getSelectedSeq() == null && quickLoad.getVersion().group != null) {
						// This can happen when loading a brand-new genome
						GenometryModel.getGenometryModel().setSelectedSeq(quickLoad.getVersion().group.getSeq(0));
					}

					SeqGroupView.getInstance().refreshTable();
					GeneralLoadView.getLoadView().refreshDataManagementView();
				} catch (Exception ex) {
					Logger.getLogger(QuickLoadSymLoader.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		};

		CThreadHolder.getInstance().execute(feature, worker);
	}

	public static boolean isLoaded(GenericFeature gFeature) {
		GenericFeature f = getLoadedFeature(gFeature.getURI());
		if (f != null && f != gFeature) {
			gFeature.clear();
			GeneralLoadView.getLoadView().refreshTreeView();
			return true;
		}

		return false;
	}

	public static GenericFeature getLoadedFeature(URI uri) {
		if (GeneralLoadUtils.getVisibleFeatures() == null) {
			return null;
		}

		for (GenericFeature gFeature : GeneralLoadUtils.getVisibleFeatures()) {
			if (gFeature.getURI().equals(uri) && gFeature.isVisible()) {
				return gFeature;
			}
		}

		return null;
	}
}
