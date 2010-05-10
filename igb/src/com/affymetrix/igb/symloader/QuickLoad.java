package com.affymetrix.igb.symloader;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.general.FeatureRequestSym;
import com.affymetrix.genometryImpl.general.SymLoader;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.parsers.AnnotsXmlParser.AnnotMapElt;
import com.affymetrix.genometryImpl.parsers.BgnParser;
import com.affymetrix.genometryImpl.parsers.Bprobe1Parser;
import com.affymetrix.genometryImpl.parsers.BpsParser;
import com.affymetrix.genometryImpl.parsers.BrptParser;
import com.affymetrix.genometryImpl.parsers.BrsParser;
import com.affymetrix.genometryImpl.parsers.BsnpParser;
import com.affymetrix.genometryImpl.parsers.CytobandParser;
import com.affymetrix.genometryImpl.parsers.ExonArrayDesignParser;
import com.affymetrix.genometryImpl.parsers.FishClonesParser;
import com.affymetrix.genometryImpl.parsers.GFF3Parser;
import com.affymetrix.genometryImpl.parsers.GFFParser;
import com.affymetrix.genometryImpl.parsers.PSLParser;
import com.affymetrix.genometryImpl.parsers.VarParser;
import com.affymetrix.genometryImpl.parsers.gchp.AffyCnChpParser;
import com.affymetrix.genometryImpl.parsers.gchp.ChromLoadPolicy;
import com.affymetrix.genometryImpl.parsers.graph.CntParser;
import com.affymetrix.genometryImpl.parsers.graph.ScoredIntervalParser;
import com.affymetrix.genometryImpl.parsers.graph.ScoredMapParser;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.IAnnotStyleExtended;
import com.affymetrix.genometryImpl.symloader.BAM;
import com.affymetrix.genometryImpl.symloader.BED;
import com.affymetrix.genometryImpl.symloader.BNIB;
import com.affymetrix.genometryImpl.symloader.Bar;
import com.affymetrix.genometryImpl.symloader.Fasta;
import com.affymetrix.genometryImpl.symloader.Gr;
import com.affymetrix.genometryImpl.symloader.Sgr;
import com.affymetrix.genometryImpl.symloader.TwoBit;
import com.affymetrix.genometryImpl.symloader.USeq;
import com.affymetrix.genometryImpl.symloader.Wiggle;
import com.affymetrix.genometryImpl.util.ClientOptimizer;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.GraphSymUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.genometryImpl.util.ServerUtils;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.menuitem.OpenGraphAction;
import com.affymetrix.igb.parsers.ChpParser;
import com.affymetrix.igb.util.ThreadUtils;
import com.affymetrix.igb.view.QuickLoadServerModel;
import com.affymetrix.igb.view.SeqMapView;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author jnicol
 */
public final class QuickLoad extends SymLoader {
	private final GenericVersion version;
	public final String featureName;
	private SymLoader symL;	// parser factory
	public boolean isResidueLoader = false;	// Let other classes know if this is just residues

	public QuickLoad(GenericVersion version, String featureName) {
		super(determineURI(version, featureName));
		this.featureName = featureName;
		this.version = version;
		this.symL = determineLoader();
	}

	public QuickLoad(GenericVersion version, URI uri) {
		super(uri);
		String unzippedName = GeneralUtils.getUnzippedName(uri.toString());
		String strippedName = unzippedName.substring(0, unzippedName.lastIndexOf(this.extension));
		String friendlyName = strippedName.substring(strippedName.lastIndexOf("/") + 1);
		this.featureName = friendlyName;
		this.version = version;
		this.symL = determineLoader();
	}

	@Override
	protected void init() {
		this.isInitialized = true;
	}

	/**
	 * Return possible strategies to load this URI.
	 * @return
	 */
	@Override
	public List<LoadStrategy> getLoadChoices() {
		// If we're using a symloader, return its load choices.
		if (this.symL != null) {
			return this.symL.getLoadChoices();
		}
		return super.getLoadChoices();
	}

	private static URI determineURI(GenericVersion version, String featureName) {
		URI uri = null;

		if (version.gServer.URL == null || version.gServer.URL.length() == 0) {
			int httpIndex = featureName.toLowerCase().indexOf("http:");
			if (httpIndex > -1) {
				// Strip off initial characters up to and including http:
				// Sometimes this is necessary, as URLs can start with invalid "http:/"
				featureName = GeneralUtils.convertStreamNameToValidURLName(featureName);
				uri = URI.create(featureName);
			} else {
				uri = (new File(featureName)).toURI();
			}
		} else {
			uri = URI.create(
					version.gServer.URL + "/"
					+ version.versionID + "/"
					+ determineFileName(version, featureName));
		}
		return uri;
	}

	private static String determineFileName(GenericVersion version, String featureName) {
		URL quickloadURL = null;
		try {
			quickloadURL = new URL((String) version.gServer.serverObj);
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			return "";
		}

		QuickLoadServerModel quickloadServer = QuickLoadServerModel.getQLModelForURL(GenometryModel.getGenometryModel(), quickloadURL);
		List<AnnotMapElt> annotsList = quickloadServer.getAnnotsMap(version.versionID);

		// Linear search, but over a very small list.
		for (AnnotMapElt annotMapElt : annotsList) {
			if (annotMapElt.title.equals(featureName)) {
				return annotMapElt.fileName;
			}
		}
		return "";
	}


	public boolean loadFeatures(final SeqSpan overlapSpan, final LoadStrategy strategy)
			throws OutOfMemoryError {

		final SeqMapView gviewer = Application.getSingleton().getMapView();
		Executor vexec = ThreadUtils.getPrimaryExecutor(this.version.gServer);
		if (this.isResidueLoader) {
			final BioSeq seq = GenometryModel.getGenometryModel().getSelectedSeq();
			return loadResiduesThread(strategy, overlapSpan, seq, gviewer, vexec);
		}

		return loadSymmetriesThread(strategy, overlapSpan, gviewer, vexec);

	}

	private boolean loadSymmetriesThread(
			final LoadStrategy strategy, final SeqSpan overlapSpan, final SeqMapView gviewer, Executor vexec)
			throws OutOfMemoryError {

		SwingWorker<List<? extends SeqSymmetry>, Void> worker = new SwingWorker<List<? extends SeqSymmetry>, Void>() {

			public List<? extends SeqSymmetry> doInBackground() {
				try {
					List<FeatureRequestSym> output_requests = new ArrayList<FeatureRequestSym>();
					if (strategy == LoadStrategy.GENOME) {
						for (BioSeq aseq : QuickLoad.this.version.group.getSeqList()) {
							if (aseq.getID().equals(IGBConstants.GENOME_SEQ_ID)) {
								continue;
							}
							SeqSpan overlap = new SimpleSeqSpan(0, aseq.getLength(), aseq);
							FeatureRequestSym requestSym = new FeatureRequestSym(overlap, null);
							ClientOptimizer.OptimizeQuery(aseq, uri, null, featureName, output_requests, requestSym);
						}
					} else {
						FeatureRequestSym requestSym = new FeatureRequestSym(overlapSpan, null);
						ClientOptimizer.OptimizeQuery(requestSym.getOverlapSpan().getBioSeq(), uri, null, featureName, output_requests, requestSym);
					}
					if (output_requests.isEmpty()) {
						return null;
					}
					List<? extends SeqSymmetry> results;
					List<SeqSymmetry> overallResults = new ArrayList<SeqSymmetry>();
					for (FeatureRequestSym request : output_requests) {
						// short-circuit if there's a failure... which may not even be signaled in the code
						results = loadFeature(strategy, request.getOverlapSpan());
						results = ServerUtils.filterForOverlappingSymmetries(request.getOverlapSpan(), results);
						if (request.getInsideSpan() != null) {
							results = ServerUtils.specifiedInsideSpan(request.getInsideSpan(), results);
						}
						if (results != null && !results.isEmpty()) {
							request.setProperty("method", uri.toString());
							SymLoader.addToRequestSym(results, request, QuickLoad.this.uri, QuickLoad.this.featureName, request.getOverlapSpan());
							SymLoader.addAnnotations(results, request, request.getOverlapSpan().getBioSeq());
						}
						overallResults.addAll(results);
					}
					return overallResults;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return null;
			}

			@Override
			public void done() {
				try {
					final List<? extends SeqSymmetry> results = get();
					if (results != null && !results.isEmpty()) {
						gviewer.setAnnotatedSeq(overlapSpan.getBioSeq(), true, true);
						//SeqGroupView.refreshTable();
					}
				} catch (Exception ex) {
					Logger.getLogger(QuickLoad.class.getName()).log(Level.SEVERE, null, ex);
				} finally {
					Application.getSingleton().removeNotLockedUpMsg("Loading feature " + QuickLoad.this.featureName);
				}
			}
		};
		vexec.execute(worker);
		return true;
	}


	private List<? extends SeqSymmetry> loadFeature(final LoadStrategy strategy, SeqSpan overlapSpan) throws IOException, OutOfMemoryError {
		if (!this.isInitialized) {
			this.init();
		}
		IAnnotStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(this.uri.toString());
		if (style != null) {
			style.setHumanName(featureName);
		}
		style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(featureName);
		if (style != null) {
			style.setHumanName(featureName);
		}
		if (strategy == LoadStrategy.GENOME || strategy == LoadStrategy.CHROMOSOME) {
			return this.getChromosome(overlapSpan.getBioSeq());
		}
		if (strategy == LoadStrategy.VISIBLE) {
			return this.getRegion(overlapSpan);
		}
		return null;
	}

	public boolean loadResiduesThread(final LoadStrategy strategy, final SeqSpan span, final BioSeq seq, final SeqMapView gviewer, Executor vexec) {
		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

			public String doInBackground() {
				try {
					String results = QuickLoad.this.getRegionResidues(span);
					return results;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return null;
			}
			@Override
			public void done() {
				try {
					final String results = get();
					if (results != null && !results.isEmpty()) {
						BioSeq.addResiduesToComposition(seq, results, span);
						gviewer.setAnnotatedSeq(seq, true, true);
					}
				} catch (Exception ex) {
					Logger.getLogger(QuickLoad.class.getName()).log(Level.SEVERE, null, ex);
				} finally {
					Application.getSingleton().removeNotLockedUpMsg("Loading feature " + QuickLoad.this.featureName);
				}
			}
		};

		vexec.execute(worker);
		return true;
	}

	/**
	 * Get list of chromosomes used in the file/uri.
	 * Especially useful when loading a file into an "unknown" genome
	 * @return List of chromosomes
	 */
	@Override
	public List<BioSeq> getChromosomeList() {
		if (this.symL != null) {
			return this.symL.getChromosomeList();
		}
		return super.getChromosomeList();
	}


	@Override
	public List<? extends SeqSymmetry> getGenome() {
		if (this.symL != null) {
			return this.symL.getGenome();
		}

		if (GraphSymUtils.isAGraphFilename(this.extension)) {
			BufferedInputStream bis = null;
			try {
				GenometryModel gmodel = GenometryModel.getGenometryModel();
				bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(this.uri);
				List<GraphSym> graphs = GraphSymUtils.readGraphs(bis, this.uri.toString(), gmodel, gmodel.getSelectedSeqGroup(), null);
				GraphSymUtils.setName(graphs, OpenGraphAction.getGraphNameForURL(this.uri.toURL()));
				return graphs;
			} catch (Exception ex) {
				Logger.getLogger(QuickLoad.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				GeneralUtils.safeClose(bis);
			}
		}

		List<? extends SeqSymmetry> feats = null;
		try {
			if (this.extension.endsWith(".chp")) {
				// special-case CHP files. ChpParser only has
				//    a parse() method that takes the file name
				// (ChpParser uses Affymetrix Fusion SDK for actual file parsing)
				// Also cannot handle compressed chp files
				File f = LocalUrlCacher.convertURIToFile(this.uri);
				return ChpParser.parse(f.getAbsolutePath());
			}
			BufferedInputStream bis = null;
			try {
				// This will also unzip the stream if necessary
				bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(this.uri);
				feats = Parse(this.extension, bis, this.version, this.featureName);
				return feats;
			} catch (FileNotFoundException ex) {
				Logger.getLogger(QuickLoad.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				GeneralUtils.safeClose(bis);
			}
			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	public List<? extends SeqSymmetry> getChromosome(BioSeq seq) {
		if (this.symL != null) {
			return this.symL.getChromosome(seq);
		}
		return super.getChromosome(seq);
	}

	@Override
	public List<? extends SeqSymmetry> getRegion(SeqSpan span) {
		if (this.symL != null) {
			return this.symL.getRegion(span);
		}
		return super.getRegion(span);
	}

	@Override
	public String getRegionResidues(SeqSpan span) {
		if (this.symL != null && this.isResidueLoader) {
			return this.symL.getRegionResidues(span);
		}
		Logger.getLogger(QuickLoad.class.getName()).log(
				Level.SEVERE, "Residue loading was called with a non-residue format.");
		return "";
	}

	/**
	 * Determine the appropriate loader.
	 * @return
	 */
	private SymLoader determineLoader() {
		// residue loaders
		if (this.extension.endsWith(".bnib")) {
			isResidueLoader = true;
			return new BNIB(this.uri, this.version.group);
		}
		if (this.extension.endsWith(".fa") || this.extension.endsWith(".fas") || this.extension.endsWith(".fasta")) {
			isResidueLoader = true;
			return new Fasta(this.uri, this.version.group);
		}
		if (this.extension.endsWith(".2bit")) {
			isResidueLoader = true;
			return new TwoBit(this.uri);
		}


		// symmetry loaders
		if (this.extension.endsWith(".bam")) {
			return new BAM(this.uri, this.featureName, this.version.group);
		}
		if (this.extension.endsWith(".bar")) {
			return new Bar(this.uri, this.featureName, this.version.group);
		}
		if (this.extension.endsWith(".bed")) {
			return new BED(this.uri, this.featureName, this.version.group);
		}
		if (this.extension.endsWith(".gr")) {
			return new Gr(this.uri, this.featureName, this.version.group);
		}
		if (this.extension.endsWith(".sgr")) {
			return new Sgr(this.uri, this.featureName, this.version.group);
		}
		if (this.extension.endsWith(".useq")) {
			return new USeq(this.uri, this.featureName, this.version.group);
		}
		if (this.extension.endsWith(".wig")) {
			return new Wiggle(this.uri, this.featureName, this.version.group);
		}
		return null;
	}


	private static List<? extends SeqSymmetry> Parse(
			String extension, InputStream istr, GenericVersion version, String featureName)
			throws Exception {
		BufferedInputStream bis = new BufferedInputStream(istr);
		extension = extension.substring(extension.lastIndexOf('.') + 1);	// strip off first .
		if (extension.equals("bgn")) {
			BgnParser parser = new BgnParser();
			return parser.parse(bis, featureName, version.group, false);
		}
		if (extension.equals("bps")) {
			DataInputStream dis = new DataInputStream(bis);
			return BpsParser.parse(dis, featureName, null, version.group, false, false);
		}
		if (extension.equals("bp1") || extension.equals("bp2")) {
			Bprobe1Parser bp1_reader = new Bprobe1Parser();
			// parsing probesets in bp2 format, also adding probeset ids
			return bp1_reader.parse(bis, version.group, false, featureName, false);
		}
		if (extension.equals("brpt")) {
			List<SeqSymmetry> alist = BrptParser.parse(bis, featureName, version.group, false);
			Logger.getLogger(QuickLoad.class.getName()).log(Level.FINE,
					"total repeats loaded: " + alist.size());
			return alist;
		}
		if (extension.equals("brs")) {
			DataInputStream dis = new DataInputStream(bis);
			return BrsParser.parse(dis, featureName, version.group, false);
		}
		if (extension.equals("bsnp")) {
			List<SeqSymmetry> alist = BsnpParser.parse(bis, featureName, version.group, false);
			Logger.getLogger(QuickLoad.class.getName()).log(Level.FINE,
					"total snps loaded: " + alist.size());
			return alist;
		}
		/*if (extension.equals("cnchp") || extension.equals("lohchp")) {
			AffyCnChpParser parser = new AffyCnChpParser();
			parser.parse(null, ChromLoadPolicy.getLoadAllPolicy(), bis, featureName, version.group);
			return;
		}
		if (extension.equals(".cnt")) {
			CntParser parser = new CntParser();
			parser.parse(bis, version.group);
			return;
		}*/
		if (extension.equals("cyt")) {
			CytobandParser parser = new CytobandParser();
			return parser.parse(bis, version.group, false);
		}
		if (extension.equals("ead")) {
			ExonArrayDesignParser parser = new ExonArrayDesignParser();
			return parser.parse(bis, version.group, false, featureName);
		}
		/*if (extension.equals("." + FishClonesParser.FILE_EXT)) {
			FishClonesParser parser = new FishClonesParser(true);
			parser.parse(bis, featureName, version.group);
			return;
		}*/
		if (extension.equals("gff") || extension.equals("gtf")) {
			GFFParser parser = new GFFParser();
			return parser.parse(bis, featureName, version.group, false, false);
		}
		if (extension.equals("gff3")) {
			/* Force parsing as GFF3 */
			GFF3Parser parser = new GFF3Parser();
			return parser.parse(bis, featureName, version.group);
		}
		if (extension.equals("link.psl")) {
			PSLParser parser = new PSLParser();
			parser.setIsLinkPsl(true);
			parser.enableSharedQueryTarget(true);
			// annotate _target_ (which is chromosome for consensus annots, and consensus seq for probeset annots
			// why is annotate_target parameter below set to false?
			return parser.parse(bis, featureName, null, version.group, null, false, false, false); // do not annotate_other (not applicable since not PSL3)
		}
		/*if (extension.equals("map")) {
			ScoredMapParser parser = new ScoredMapParser();
			parser.parse(bis, featureName, input_seq, version.group);
			return;
		}*/
		if (extension.equals("psl") || extension.equals("psl3")) {
			// reference to LoadFileAction.ParsePSL
			PSLParser parser = new PSLParser();
			parser.enableSharedQueryTarget(true);
			DataInputStream dis = new DataInputStream(bis);
			return parser.parse(dis, featureName, null, version.group, null, false, false, false);
		}
		if (extension.equals("sin") || extension.equals("egr") || extension.equals("txt")) {
			ScoredIntervalParser parser = new ScoredIntervalParser();
			return parser.parse(bis, featureName, version.group, false);
		}
		/*if (extension.equals("var")) {
			VarParser parser = new VarParser();
			parser.parse(bis, version.group);
			return;
		}*/
		Logger.getLogger(QuickLoad.class.getName()).log(Level.WARNING,
				"ABORTING FEATURE LOADING, FORMAT NOT RECOGNIZED: " + extension);
		return null;
	}
}
