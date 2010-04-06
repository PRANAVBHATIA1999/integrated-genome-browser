package com.affymetrix.igb.quickload;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.SimpleSymWithProps;
import com.affymetrix.genometryImpl.general.GenericSymRequest;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.parsers.AnnotsXmlParser.AnnotMapElt;
import com.affymetrix.genometryImpl.parsers.BAMParser;
import com.affymetrix.genometryImpl.parsers.BedParser;
import com.affymetrix.genometryImpl.parsers.BgnParser;
import com.affymetrix.genometryImpl.parsers.Bprobe1Parser;
import com.affymetrix.genometryImpl.parsers.BpsParser;
import com.affymetrix.genometryImpl.parsers.BrsParser;
import com.affymetrix.genometryImpl.parsers.CytobandParser;
import com.affymetrix.genometryImpl.parsers.ExonArrayDesignParser;
import com.affymetrix.genometryImpl.parsers.GFFParser;
import com.affymetrix.genometryImpl.parsers.PSLParser;
import com.affymetrix.genometryImpl.parsers.graph.BarParser;
import com.affymetrix.genometryImpl.parsers.useq.ArchiveInfo;
import com.affymetrix.genometryImpl.parsers.useq.USeqGraphParser;
import com.affymetrix.genometryImpl.parsers.useq.USeqRegionParser;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.IAnnotStyleExtended;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.GraphSymUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.das2.Das2ClientOptimizer;
import com.affymetrix.igb.menuitem.OpenGraphAction;
import com.affymetrix.igb.parsers.ChpParser;
import com.affymetrix.igb.util.LocalUrlCacher;
import com.affymetrix.igb.util.ThreadUtils;
import com.affymetrix.igb.view.QuickLoadServerModel;
import com.affymetrix.igb.view.SeqMapView;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import javax.swing.SwingWorker;

/**
 *
 * @author jnicol
 */
public class QuickLoadFeatureLoading extends GenericSymRequest {
	private File f;
	private final GenericVersion version;
	public final String featureName;

	public QuickLoadFeatureLoading(GenericVersion version, String featureName) {
		super(determineURI(version, featureName));
		this.featureName = featureName;
		this.version = version;
	}

	@Override
	protected void init() {
		String scheme = this.uri.getScheme().toLowerCase();
		if (scheme.length() == 0 || scheme.equals("file")) {
			f = new File(this.uri.getRawPath());
		} else if (scheme.startsWith("http")) {
			InputStream istr = null;
			try {
				String uriStr = this.uri.toString();
				istr = LocalUrlCacher.getInputStream(uriStr);
				StringBuffer stripped_name = new StringBuffer();
				InputStream str = GeneralUtils.unzipStream(istr, uriStr, stripped_name);
				String stream_name = stripped_name.toString();

				if (str instanceof BufferedInputStream) {
					str = (BufferedInputStream) str;
				} else {
					str = new BufferedInputStream(str);
				}

				f = GeneralUtils.convertStreamToFile(str, stream_name.substring(stream_name.lastIndexOf("/")));
			} catch (IOException ex) {
				Logger.getLogger(QuickLoadFeatureLoading.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				GeneralUtils.safeClose(istr);
			}
		} else {
			System.out.println("URL scheme: " + scheme + " not recognized");
		}
		this.isInitialized = true;
	}

	private static URI determineURI(GenericVersion version, String featureName) {
		URI uri = null;

		try {
			if (version.gServer.URL == null || version.gServer.URL.length() == 0) {
				int httpIndex = featureName.toLowerCase().indexOf("http:");
				if (httpIndex > -1) {
					// Strip off initial characters up to and including http:
					// Sometimes this is necessary, as URLs can start with invalid "http:/"
					featureName = GeneralUtils.convertStreamNameToValidURLName(featureName);
					uri = new URI(featureName);
				} else {
					uri = new URI("file://" + featureName);
				}
			} else {
				uri = new URI(
						version.gServer.URL + "/"
						+ version.versionID + "/"
						+ determineFileName(version, featureName));
			}
		} catch (URISyntaxException ex) {
			Logger.getLogger(QuickLoadFeatureLoading.class.getName()).log(Level.SEVERE, null, ex);
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

	public boolean loadFeatures(final SeqMapView gviewer, final SeqSpan overlapSpan, final LoadStrategy strategy)
			throws OutOfMemoryError {

		Executor vexec = ThreadUtils.getPrimaryExecutor(this.version.gServer);

		SwingWorker<List<? extends SeqSymmetry>, Void> worker = new SwingWorker<List<? extends SeqSymmetry>, Void>() {

			public List<? extends SeqSymmetry> doInBackground() {
				try {
					List<? extends SeqSymmetry> results = loadFeature(strategy, overlapSpan);
					if (results != null && !results.isEmpty()) {
						SimpleSymWithProps requestSym = new SimpleSymWithProps();
						requestSym.setProperty("meth", QuickLoadFeatureLoading.this.f.getName());
						Das2ClientOptimizer.addToRequestSym(
								results,
								requestSym,
								QuickLoadFeatureLoading.this.f.getName(),
								QuickLoadFeatureLoading.this.featureName,
								overlapSpan);
						if (strategy == LoadStrategy.CHROMOSOME || strategy == LoadStrategy.VISIBLE) {
							Das2ClientOptimizer.addAnnotations(results, requestSym, GenometryModel.getGenometryModel().getSelectedSeq());
						}
						else if (strategy == LoadStrategy.GENOME) {
							for (BioSeq aseq : QuickLoadFeatureLoading.this.version.group.getSeqList()) {
								Das2ClientOptimizer.addAnnotations(results, requestSym, aseq);
							}
						}
					}
					return results;
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
						BioSeq aseq = GenometryModel.getGenometryModel().getSelectedSeq();
						gviewer.setAnnotatedSeq(aseq, true, true);
					}
				} catch (Exception ex) {
					Logger.getLogger(QuickLoadFeatureLoading.class.getName()).log(Level.SEVERE, null, ex);
				} finally {
					Application.getSingleton().removeNotLockedUpMsg("Loading feature " + QuickLoadFeatureLoading.this.featureName);
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
		String unzippedName = GeneralUtils.getUnzippedName(this.f.getName());
		String strippedName = unzippedName.substring(0, unzippedName.lastIndexOf(this.extension));
		style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(strippedName);
		if (style != null) {
			style.setHumanName(featureName);
		}
		if (GraphSymUtils.isAGraphFilename(this.f.getName())) {
			GenometryModel gmodel = GenometryModel.getGenometryModel();
			FileInputStream fis = new FileInputStream(this.f);
			List<GraphSym> graphs = GraphSymUtils.readGraphs(fis, this.uri.toString(), gmodel, gmodel.getSelectedSeqGroup(), null);
			GraphSymUtils.setName(graphs, OpenGraphAction.getGraphNameForURL(this.uri.toURL()));
			return graphs;
		}
		if (strategy == LoadStrategy.GENOME) {
			return this.getGenome();
		}
		if (strategy == LoadStrategy.CHROMOSOME) {
			return this.getChromosome(GenometryModel.getGenometryModel().getSelectedSeq());
		}
		if (strategy == LoadStrategy.VISIBLE) {
			return this.getRegion(overlapSpan);
		}
		return null;
	}

	@Override
	public List<? extends SeqSymmetry> getGenome() {
		List<? extends SeqSymmetry> feats = null;
		try {
		if (this.extension.endsWith(".chp")) {
			// special-case CHP files. ChpParser only has
			//    a parse() method that takes the file name
			// (ChpParser uses Affymetrix Fusion SDK for actual file parsing)
			// Also cannot handle compressed chp files
			ChpParser.parse(f.getAbsolutePath());
			return feats;
		}
		if (this.extension.endsWith("bam")) {

			// special-case BAM files, because Picard can only parse from files.
			if (this.version.group == null) {
				//ErrorHandler.errorPanel(gviewerFrame, "ERROR", MERGE_MESSAGE, null);
			} else {
				BAMParser parser = new BAMParser(this.f, this.version.group);
				parser.getGenome();
			}
			return feats;
		}
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			feats = Parse(this.extension, fis, this.version, this.featureName);
			return feats;
		} catch (FileNotFoundException ex) {
			Logger.getLogger(QuickLoadFeatureLoading.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			GeneralUtils.safeClose(fis);
		}
		return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	public List<? extends SeqSymmetry> getChromosome(BioSeq seq) {
		if (this.extension.endsWith("bam")) {

			// special-case BAM files, because Picard can only parse from files.
			if (this.version.group == null) {
				//ErrorHandler.errorPanel(gviewerFrame, "ERROR", MERGE_MESSAGE, null);
			} else {
				BAMParser parser = new BAMParser(this.f, this.version.group);
				parser.getChromosome(seq);
			}
			return null;
		}
		return super.getChromosome(seq);
	}

	@Override
	public List<? extends SeqSymmetry> getRegion(SeqSpan span) {
		if (this.extension.endsWith("bam")) {

			// special-case BAM files, because Picard can only parse from files.
			if (this.version.group == null) {
				//ErrorHandler.errorPanel(gviewerFrame, "ERROR", MERGE_MESSAGE, null);
			} else {
				BAMParser parser = new BAMParser(this.f, this.version.group);
				parser.getRegion(span);
			}
			return null;
		}
		return super.getRegion(span);
	}


    private static List<? extends SeqSymmetry> Parse(
            String extension, InputStream istr, GenericVersion version, String featureName)
            throws Exception {
		BufferedInputStream bis = new BufferedInputStream(istr);
		GenometryModel gmodel = GenometryModel.getGenometryModel();
        List<? extends SeqSymmetry> feats = null;
		extension = extension.substring(extension.lastIndexOf('.') + 1);	// strip off first .
        if (extension.equals("bed")) {
            BedParser parser = new BedParser();
			feats = parser.parse(bis, gmodel, version.group, false, featureName, false);
        } else if (extension.equals("bgn")) {
            BgnParser parser = new BgnParser();
            feats = parser.parse(bis, featureName, version.group, false);
        } else if (extension.equals("bps")) {
            DataInputStream dis = new DataInputStream(bis);
            feats = BpsParser.parse(dis, featureName, null, version.group, false, false);
        } else if (extension.equals("brs")) {
            DataInputStream dis = new DataInputStream(bis);
            feats = BrsParser.parse(dis, featureName, version.group, false);
        } else if (extension.equals("bar")) {
            feats = BarParser.parse(bis, gmodel, version.group, featureName, false);
        } else if (extension.equals("useq")) {
        	//find out what kind of data it is, graph or region, from the ArchiveInfo object
        	ZipInputStream zis = new ZipInputStream(bis);
    		zis.getNextEntry();
        	ArchiveInfo archiveInfo = new ArchiveInfo(zis, false);
            if (archiveInfo.getDataType().equals(ArchiveInfo.DATA_TYPE_VALUE_GRAPH)){
            	USeqGraphParser gp = new USeqGraphParser();
                feats = gp.parseGraphSyms(zis, gmodel, featureName, archiveInfo);
            }
            else {
            	 USeqRegionParser rp = new USeqRegionParser();
                 feats = rp.parse(zis, version.group, featureName, false, archiveInfo);
            }
        }else if (extension.equals("bp2")) {
            Bprobe1Parser bp1_reader = new Bprobe1Parser();
            // parsing probesets in bp2 format, also adding probeset ids
            feats = bp1_reader.parse(bis, version.group, false, featureName, false);
        } else if (extension.equals("ead")) {
            ExonArrayDesignParser parser = new ExonArrayDesignParser();
            feats = parser.parse(bis, version.group, false, featureName);
        } else if (extension.equals("gff")) {
            GFFParser parser = new GFFParser();
            feats = parser.parse(bis, ".", version.group, false, false);
        } else if (extension.equals("link.psl")) {
            PSLParser parser = new PSLParser();
            parser.setIsLinkPsl(true);
            parser.enableSharedQueryTarget(true);
            // annotate _target_ (which is chromosome for consensus annots, and consensus seq for probeset annots
            // why is annotate_target parameter below set to false?
            feats = parser.parse(bis, featureName, null, version.group, null, false, false, false); // do not annotate_other (not applicable since not PSL3)
        } else if (extension.equals("cyt")) {
            CytobandParser parser = new CytobandParser();
            feats = parser.parse(bis, version.group, false);
        } else if (extension.equals("psl")) {
            // reference to LoadFileAction.ParsePSL
            PSLParser parser = new PSLParser();
            parser.enableSharedQueryTarget(true);
            DataInputStream dis = new DataInputStream(bis);
            feats = parser.parse(dis, featureName, null, version.group, null, false, false, false);
        } else {
            System.out.println("ABORTING FEATURE LOADING, FORMAT NOT RECOGNIZED: " + extension);
        }
        return feats;
    }


}
