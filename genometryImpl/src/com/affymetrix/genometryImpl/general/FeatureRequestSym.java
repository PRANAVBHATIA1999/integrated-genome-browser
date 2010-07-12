/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.affymetrix.genometryImpl.general;

import com.affymetrix.genometryImpl.symmetry.LeafSingletonSymmetry;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.ScoredContainerSym;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.SimpleSymWithProps;
import com.affymetrix.genometryImpl.SymWithProps;
import com.affymetrix.genometryImpl.parsers.BedParser;
import com.affymetrix.genometryImpl.parsers.BgnParser;
import com.affymetrix.genometryImpl.parsers.Bprobe1Parser;
import com.affymetrix.genometryImpl.parsers.BpsParser;
import com.affymetrix.genometryImpl.parsers.BrptParser;
import com.affymetrix.genometryImpl.parsers.BrsParser;
import com.affymetrix.genometryImpl.parsers.BsnpParser;
import com.affymetrix.genometryImpl.parsers.CytobandParser;
import com.affymetrix.genometryImpl.parsers.Das2FeatureSaxParser;
import com.affymetrix.genometryImpl.parsers.ExonArrayDesignParser;
import com.affymetrix.genometryImpl.parsers.FishClonesParser;
import com.affymetrix.genometryImpl.parsers.GFF3Parser;
import com.affymetrix.genometryImpl.parsers.GFFParser;
import com.affymetrix.genometryImpl.parsers.PSLParser;
import com.affymetrix.genometryImpl.parsers.VarParser;
import com.affymetrix.genometryImpl.parsers.das.DASFeatureParser;
import com.affymetrix.genometryImpl.parsers.gchp.AffyCnChpParser;
import com.affymetrix.genometryImpl.parsers.graph.BarParser;
import com.affymetrix.genometryImpl.parsers.graph.CntParser;
import com.affymetrix.genometryImpl.parsers.graph.ScoredIntervalParser;
import com.affymetrix.genometryImpl.parsers.useq.ArchiveInfo;
import com.affymetrix.genometryImpl.parsers.useq.USeqGraphParser;
import com.affymetrix.genometryImpl.parsers.useq.USeqRegionParser;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.util.ClientOptimizer;
import com.affymetrix.genometryImpl.util.Constants;
import com.affymetrix.genometryImpl.util.GraphSymUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import org.xml.sax.InputSource;

/**
 *
 * @author jnicol
 */
public class FeatureRequestSym extends SimpleSymWithProps {


  private final LeafSingletonSymmetry overlap_span; // LeafSingletonSym also implements SeqSymmetry interface
  private final SeqSpan inside_span;

  //  for now trying to do without container info in constructor
  public FeatureRequestSym(SeqSpan overlap, SeqSpan inside) {
    overlap_span = new LeafSingletonSymmetry(overlap);
	inside_span = inside;
	
    this.setProperty(SimpleSymWithProps.CONTAINER_PROP, Boolean.TRUE);
  }

  /**
   *  Returns the overlap span, the span specified in the original query
   *    that returned annotation must overlap with.
   */
  // May need to returns a sym instead of span to better match up with SeqSymmetrySummarizer methods??
  public final SeqSpan getOverlapSpan() { return overlap_span; }

  /**
   *  Convenience method for returning overlap span as a SeqSymmetry with 1 span and 0 children.
   */
  public final SeqSymmetry getOverlapSym() { return overlap_span; }
  
  /**
   *  Returns the inside span, the span specified in the original query
   *    that returned annotation must be contained within.
   */
  public final SeqSpan getInsideSpan() { return inside_span; }

  /**
   * Add the specified symmetries to the FeatureRequestSym.  It is assumed these correspond to the same chromosome.
   * @param feats - list of symmetries
   * @param request_sym - FeatureRequestSym
   * @param id
   * @param name
   * @param overlapSpan
   */
  public static void addToRequestSym(
			List<? extends SeqSymmetry> feats, SimpleSymWithProps request_sym, URI id, String name, SeqSpan overlapSpan) {
		int feat_count = feats == null ? 0 : feats.size();
		Logger.getLogger(FeatureRequestSym.class.getName()).log(
				Level.INFO, "parsed query results, annot count = {0}", feat_count);
		if (feat_count == 0) {
			// because many operations will treat empty FeatureRequestSym as a leaf sym, want to
			//    populate with empty sym child/grandchild
			//    [ though a better way might be to have request sym's span on aseq be dependent on children, so
			//       if no children then no span on aseq (though still an overlap_span and inside_span) ]
			SimpleSymWithProps child = new SimpleSymWithProps();
			child.addChild(new SimpleSymWithProps());
			request_sym.addChild(child);
		} else {
			for (SeqSymmetry feat : feats) {
				if (feat instanceof GraphSym) {
					GraphSymUtils.addChildGraph((GraphSym) feat, id, name, overlapSpan);
				} else {
					request_sym.addChild(feat);
				}
			}
		}
	}

   public static Map<String, List<SeqSymmetry>> splitResultsByTracks(FeatureRequestSym request, List<? extends SeqSymmetry> results) {
		Map<String, List<SeqSymmetry>> track2Results = new HashMap<String, List<SeqSymmetry>>();
		List<SeqSymmetry> resultList = null;
		String method = null;
		for (SeqSymmetry result : results) {
			method = (result instanceof SymWithProps) ? (String) ((SymWithProps) result).getProperty("method") : null;
			if (track2Results.containsKey(method)) {
				resultList = track2Results.get(method);
			} else {
				resultList = new ArrayList<SeqSymmetry>();
				track2Results.put(method, resultList);
			}
			resultList.add(result);
		}

	  return track2Results;
  }

  /**
   * If children of FeatureRequestSym have track information, then split the FeatureRequestSym based on them.
   * This is necessary for some formats (such as GFF1).
   * @param requests
   */
   public static Collection<FeatureRequestSym> splitFeatureSymByTracks(FeatureRequestSym request, List<? extends SeqSymmetry> results) {
		Map<String, FeatureRequestSym> track2requestSym = new HashMap<String, FeatureRequestSym>();
		FeatureRequestSym frs = null;
		String method = null;
		for (Map.Entry<String, List<SeqSymmetry>> entry : splitResultsByTracks(request, results).entrySet()) {
			method = entry.getKey();
			frs = new FeatureRequestSym(request.getOverlapSpan(), request.getInsideSpan());
			frs.setProperty("method", method);
		}
		for (SeqSymmetry result : results) {
			method = (result instanceof SymWithProps) ? (String) ((SymWithProps) result).getProperty("method") : null;
			if (track2requestSym.containsKey(method)) {
				frs = track2requestSym.get(method);
			} else {
				frs = new FeatureRequestSym(request.getOverlapSpan(), request.getInsideSpan());
				frs.setProperty("method", method);
				track2requestSym.put(method, frs);
			}
			frs.addChild(result);
		}
	  
	  return track2requestSym.values();
  }

	public static void addAnnotations(
			List<? extends SeqSymmetry> feats, SimpleSymWithProps request_sym, BioSeq aseq) {
		boolean skipOverallAdd = false;
		if (feats != null && !feats.isEmpty()) {
			for (SeqSymmetry feat : feats) {
				if (feat instanceof GraphSym) {
					// if graphs, then adding to annotation BioSeq is handled by addChildGraph() method
					return;
				}
				if (feat instanceof ScoredContainerSym) {
					// TODO: This is a hack for the EGR format, which is using containers for its symmetries.
					aseq.addAnnotation(feat);
					skipOverallAdd = true;	// don't add twice
				}
			}
		}

		if (!skipOverallAdd) {
			synchronized (aseq) {
				aseq.addAnnotation(request_sym);
			}
		}
	}

	public static List<FeatureRequestSym> determineFeatureRequestSyms(SymLoader symL, URI uri, String featureName, LoadStrategy strategy, SeqSpan overlapSpan) {
		List<FeatureRequestSym> output_requests = new ArrayList<FeatureRequestSym>();
		if (strategy == LoadStrategy.GENOME && symL != null) {
			buildFeatureSymListByChromosome(symL.getChromosomeList(), uri, featureName, output_requests);
		} else {
			// Note that if we're loading the whole genome and symL isn't defined, we return one requestSym.  That's okay -- it will be ignored
			FeatureRequestSym requestSym = new FeatureRequestSym(overlapSpan, null);
			ClientOptimizer.OptimizeQuery(requestSym.getOverlapSpan().getBioSeq(), uri, null, featureName, output_requests, requestSym);
		}
		return output_requests;
	}

	public static void buildFeatureSymListByChromosome(List<BioSeq> seqList, URI uri, String featureName, List<FeatureRequestSym> output_requests) {
		for (BioSeq aseq : seqList) {
			if (aseq.getID().equals(Constants.GENOME_SEQ_ID)) {
				continue;
			}
			SeqSpan overlap = new SimpleSeqSpan(0, aseq.getLength(), aseq);
			FeatureRequestSym requestSym = new FeatureRequestSym(overlap, null);
			ClientOptimizer.OptimizeQuery(aseq, uri, null, featureName, output_requests, requestSym);
		}
	}

	/**
	 * parse the input stream, with parser determined by extension.
	 * @param extension
	 * @param uri - the URI corresponding to the file/URL
	 * @param istr
	 * @param group
	 * @param featureName
	 * @return list of symmetries
	 * @throws Exception
	 */
	public static List<? extends SeqSymmetry> Parse(
			String extension, URI uri, InputStream istr, AnnotatedSeqGroup group, String featureName)
			throws Exception {
		BufferedInputStream bis = new BufferedInputStream(istr);
		extension = extension.substring(extension.lastIndexOf('.') + 1);	// strip off first .

		// These extensions are overloaded by QuickLoad
		if (extension.equals("bar")) {
			return BarParser.parse(bis, GenometryModel.getGenometryModel(), group, null, 0, Integer.MAX_VALUE, featureName, false);
		}
		if (extension.equals("bed")) {
			BedParser parser = new BedParser();
			return parser.parse(bis, GenometryModel.getGenometryModel(), group, false, uri.toString(), false);
		}
		if (extension.equals("useq")) {
			//find out what kind of data it is, graph or region, from the ArchiveInfo object
			ZipInputStream zis = new ZipInputStream(bis);
			zis.getNextEntry();
			ArchiveInfo archiveInfo = new ArchiveInfo(zis, false);
			if (archiveInfo.getDataType().equals(ArchiveInfo.DATA_TYPE_VALUE_GRAPH)) {
				USeqGraphParser gp = new USeqGraphParser();
				return gp.parseGraphSyms(zis, GenometryModel.getGenometryModel(), featureName, archiveInfo);
			}
			USeqRegionParser rp = new USeqRegionParser();
			return rp.parse(zis, group, featureName, false, archiveInfo);
		}

		// Not overloaded extensions
		if (extension.equals("bgn")) {
			BgnParser parser = new BgnParser();
			return parser.parse(bis, featureName, group, false);
		}
		if (extension.equals("bps")) {
			DataInputStream dis = new DataInputStream(bis);
			return BpsParser.parse(dis, featureName, null, group, false, false);
		}
		if (extension.equals("bp1") || extension.equals("bp2")) {
			Bprobe1Parser bp1_reader = new Bprobe1Parser();
			// parsing probesets in bp2 format, also adding probeset ids
			return bp1_reader.parse(bis, group, false, featureName, false);
		}
		if (extension.equals("brpt")) {
			List<SeqSymmetry> alist = BrptParser.parse(bis, featureName, group, false);
			Logger.getLogger(FeatureRequestSym.class.getName()).log(
					Level.FINE, "total repeats loaded: {0}", alist.size());
			return alist;
		}
		if (extension.equals("brs")) {
			DataInputStream dis = new DataInputStream(bis);
			return BrsParser.parse(dis, featureName, group, false);
		}
		if (extension.equals("bsnp")) {
			List<SeqSymmetry> alist = BsnpParser.parse(bis, featureName, group, false);
			Logger.getLogger(FeatureRequestSym.class.getName()).log(
					Level.FINE, "total snps loaded: {0}", alist.size());
			return alist;
		}
		if (extension.equals("cnchp") || extension.equals("lohchp")) {
			AffyCnChpParser parser = new AffyCnChpParser();
			return parser.parse(null, bis, featureName, group, false);
		}
		if (extension.equals(".cnt")) {
			CntParser parser = new CntParser();
			return parser.parse(bis, group, false);
		}
		if (extension.equals("cyt")) {
			CytobandParser parser = new CytobandParser();
			return parser.parse(bis, group, false);
		}
		if (extension.equals("das") || extension.equals("dasxml")) {
			DASFeatureParser parser = new DASFeatureParser();
			parser.setAnnotateSeq(false);
			return (List<? extends SeqSymmetry>) parser.parse(bis, group);
		}
		if (extension.equals(Das2FeatureSaxParser.FEATURES_CONTENT_SUBTYPE)
				|| extension.equals("das2feature")
				|| extension.equals("das2xml")
				|| extension.startsWith("x-das-feature")) {
			Das2FeatureSaxParser parser = new Das2FeatureSaxParser();
			return parser.parse(new InputSource(bis), uri.toString(), group, false);
		}
		if (extension.equals("ead")) {
			ExonArrayDesignParser parser = new ExonArrayDesignParser();
			return parser.parse(bis, group, false, featureName);
		}
		if (extension.equals("." + FishClonesParser.FILE_EXT)) {
			FishClonesParser parser = new FishClonesParser(false);
			return parser.parse(bis, featureName, group);
		}
		if (extension.equals("gff") || extension.equals("gtf")) {
			GFFParser parser = new GFFParser();
			return parser.parse(bis, featureName, group, false, false);
		}
		if (extension.equals("gff3")) {
			/* Force parsing as GFF3 */
			GFF3Parser parser = new GFF3Parser();
			return parser.parse(bis, featureName, group);
		}
		if (extension.equals("link.psl")) {
			PSLParser parser = new PSLParser();
			parser.setIsLinkPsl(true);
			parser.enableSharedQueryTarget(true);
			// annotate _target_ (which is chromosome for consensus annots, and consensus seq for probeset annots
			// why is annotate_target parameter below set to false?
			return parser.parse(bis, featureName, null, group, null, false, false, false); // do not annotate_other (not applicable since not PSL3)
		}
		if (extension.equals("psl") || extension.equals("psl3")) {
			// reference to LoadFileAction.ParsePSL
			PSLParser parser = new PSLParser();
			parser.enableSharedQueryTarget(true);
			DataInputStream dis = new DataInputStream(bis);
			return parser.parse(dis, featureName, null, group, null, false, false, false);
		}
		if (extension.equals("sin") || extension.equals("egr") || extension.equals("txt")) {
			ScoredIntervalParser parser = new ScoredIntervalParser();
			return parser.parse(bis, featureName, group, false);
		}
		if (extension.equals("var")) {
			return VarParser.parse(bis, group);
		}

		Logger.getLogger(FeatureRequestSym.class.getName()).log(
				Level.WARNING, "ABORTING FEATURE LOADING, FORMAT NOT RECOGNIZED: {0}", extension);
		return null;
	}

}
