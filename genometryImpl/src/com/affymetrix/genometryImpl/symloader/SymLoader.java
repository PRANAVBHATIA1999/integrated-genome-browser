package com.affymetrix.genometryImpl.symloader;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.parsers.FileTypeHandler;
import com.affymetrix.genometryImpl.parsers.FileTypeHolder;
import com.affymetrix.genometryImpl.symmetry.GraphSym;
import com.affymetrix.genometryImpl.symmetry.MutableSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.SimpleMutableSeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.UcscPslSym;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.GraphSymUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.genometryImpl.util.ParserController;
import com.affymetrix.genometryImpl.util.SeqUtils;
import com.affymetrix.genometryImpl.util.SortTabFile;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jnicol
 * Could be improved with iterators.  But for now this should be fine.
 */
public abstract class SymLoader {
	public static final String FILE_PREFIX = "file:";
	public static final int UNKNOWN_CHROMOSOME_LENGTH = 1; // for unknown chromosomes when the length is not known
	public String extension;	// used for ServerUtils call
	public final URI uri;
	protected boolean isResidueLoader = false;	// Let other classes know if this is just residues
	protected volatile boolean isInitialized = false;
	protected final Map<BioSeq,File> chrList = new HashMap<BioSeq,File>();
	protected final Map<BioSeq,Boolean> chrSort = new HashMap<BioSeq,Boolean>();
	protected final AnnotatedSeqGroup group;
	public final String featureName;

	private static final List<LoadStrategy> strategyList = new ArrayList<LoadStrategy>();
	static {
		strategyList.add(LoadStrategy.NO_LOAD);
		strategyList.add(LoadStrategy.CHROMOSOME);
		strategyList.add(LoadStrategy.GENOME);
	}

	public SymLoader(URI uri, String featureName, AnnotatedSeqGroup group) {
        this.uri = uri;
		this.featureName = featureName;
		this.group = group;
		extension = getExtension(uri);
    }

	protected void init() throws Exception {
		this.isInitialized = true;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getFeatureName() {
		return featureName;
	}

	protected boolean buildIndex() throws Exception {
		BufferedInputStream bis = null;
		Map<String, Integer> chrLength = new HashMap<String, Integer>();
		Map<String, File> chrFiles = new HashMap<String, File>();

		try {
			bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(uri);
			if(parseLines(bis, chrLength, chrFiles)){
				createResults(chrLength, chrFiles);
				Logger.getLogger(SymLoader.class.getName()).fine("Indexing successful");
				return true;
			}
		} catch(Exception ex){
			throw ex;
		} finally {
			GeneralUtils.safeClose(bis);
		}
		return false;
	}

	protected void sortCreatedFiles() throws Exception {
		//Now Sort all files
		for (Entry<BioSeq, File> file : chrList.entrySet()) {
			chrSort.put(file.getKey(), SortTabFile.sort(file.getValue()));
		}
	}
	/**
	 * Return possible strategies to load this URI.
	 * @return
	 */
	public List<LoadStrategy> getLoadChoices() {
		return strategyList;
	}
	/**
	 * Get list of chromosomes used in the file/uri.
	 * Especially useful when loading a file into an "unknown" genome
	 * @return List of chromosomes
	 */
	public List<BioSeq> getChromosomeList() throws Exception {
		return Collections.<BioSeq>emptyList();
	}
	
    /**
     * @return List of symmetries in genome
     */
    public List<? extends SeqSymmetry> getGenome() throws Exception {

		BufferedInputStream bis = null;
		try {
			// This will also unzip the stream if necessary
			bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(this.uri);
			return parse(bis, false);
		} catch(Exception ex){
			throw ex;
		} finally {
			GeneralUtils.safeClose(bis);
		}

		//Logger.getLogger(this.getClass().getName()).log(
		//		Level.SEVERE, "Retrieving genome is not defined");
		//return null;
	}

    /**
     * @param seq - chromosome
     * @return List of symmetries in chromosome
     */
    public List<? extends SeqSymmetry> getChromosome(BioSeq seq) throws Exception {
		Logger.getLogger(this.getClass().getName()).log(
					Level.FINE, "Retrieving chromosome is not optimized");
		List<? extends SeqSymmetry> genomeResults = this.getGenome();
		if (seq == null || genomeResults == null) {
			return genomeResults;
		}
		return filterResultsByChromosome(genomeResults, seq);
    }

	public List<String> getFormatPrefList(){
		return Collections.<String>emptyList();
	}

	public String getExtension(){
		return extension;
	}

	public static String getExtension(URI uri){
		return getExtension(uri.toASCIIString().toLowerCase());
	}
	
	public static String getExtension(String uriString){
		String unzippedStreamName = GeneralUtils.stripEndings(uriString);
		String extension = ParserController.getExtension(unzippedStreamName);
		extension = extension.substring(extension.indexOf('.') + 1);	// strip off first .
		return extension;
	}
	
	/**
	 * Return the symmetries that match the given chromosome.
	 * @param genomeResults
	 * @param seq
	 * @return
	 */
	public static List<SeqSymmetry> filterResultsByChromosome(List<? extends SeqSymmetry> genomeResults, BioSeq seq) {
		List<SeqSymmetry> results = new ArrayList<SeqSymmetry>();
		for (SeqSymmetry sym : genomeResults) {
			BioSeq seq2 = null;
			if (sym instanceof UcscPslSym) {
				seq2 = ((UcscPslSym) sym).getTargetSeq();
			} else {
				seq2 = sym.getSpanSeq(0);
			}
			if (seq.equals(seq2)) {
				results.add(sym);
			}
		}
		return results;
	}

    /**
     * Get a region of the chromosome.
     * @param seq - chromosome
     * @param overlapSpan - span of overlap
     * @return List of symmetries satisfying requirements
     */
    public List<? extends SeqSymmetry> getRegion(SeqSpan overlapSpan) throws Exception {
		Logger.getLogger(this.getClass().getName()).log(
					Level.WARNING, "Retrieving region is not supported.  Returning entire chromosome.");
		List<? extends SeqSymmetry> chrResults = this.getChromosome(overlapSpan.getBioSeq());
		return chrResults;
    }

	public boolean isResidueLoader(){
		return isResidueLoader;
	}
	
	/**
     * Get residues in the region of the chromosome.  This is generally only defined for some parsers
     * @param span - span of chromosome
     * @return String of residues
     */
    public String getRegionResidues(SeqSpan span) throws Exception {
		Logger.getLogger(this.getClass().getName()).log(
					Level.WARNING, "Not supported.  Returning empty string.");
		return "";
    }

	protected boolean parseLines(InputStream istr, Map<String, Integer> chrLength, Map<String, File> chrFiles) throws Exception {
		Logger.getLogger(this.getClass().getName()).log(
					Level.SEVERE, "parseLines is not defined");
		return false;
	}
	
	protected void addToLists(
			Map<String, BufferedWriter> chrs, String current_seq_id, Map<String, File> chrFiles, Map<String,Integer> chrLength, String format) throws IOException {

		String fileName = current_seq_id;
		if (fileName.length() < 3) {
			fileName += "___";
		}
		format = !format.startsWith(".") ? "." + format : format;
		File tempFile = File.createTempFile(fileName, format);
		tempFile.deleteOnExit();
		chrs.put(current_seq_id, new BufferedWriter(new FileWriter(tempFile, true)));
		chrFiles.put(current_seq_id, tempFile);
		chrLength.put(current_seq_id, 0);
	}


	protected void createResults(Map<String, Integer> chrLength, Map<String, File> chrFiles){
		for(Entry<String, Integer> bioseq : chrLength.entrySet()){
			String key = bioseq.getKey();
			chrList.put(group.addSeq(key, bioseq.getValue(), uri.toString()), chrFiles.get(key));
		}
	}

	/**
   * Split list of symmetries by track.
   * @param results - list of symmetries
   * @return - Map<String trackName,List<SeqSymmetry>>
   */
  public static Map<String, List<SeqSymmetry>> splitResultsByTracks(List<? extends SeqSymmetry> results) {
		Map<String, List<SeqSymmetry>> track2Results = new HashMap<String, List<SeqSymmetry>>();
		List<SeqSymmetry> resultList = null;
		String method = null;
		for (SeqSymmetry result : results) {
			method = BioSeq.determineMethod(result);
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

  public static Map<BioSeq, List<SeqSymmetry>> splitResultsBySeqs(List<? extends SeqSymmetry> results){
	  Map<BioSeq, List<SeqSymmetry>> seq2Results = new HashMap<BioSeq, List<SeqSymmetry>>();
	  List<SeqSymmetry> resultList = null;
	  BioSeq seq = null;
		for (SeqSymmetry result : results) {

			for(int i=0; i<result.getSpanCount(); i++){
				seq = result.getSpan(i).getBioSeq();

				if (seq2Results.containsKey(seq)) {
					resultList = seq2Results.get(seq);
				} else {
					resultList = new ArrayList<SeqSymmetry>();
					seq2Results.put(seq, resultList);
				}
				resultList.add(result);
			}

		}

	  return seq2Results;
  }

	public static boolean splitFilterAndAddAnnotation(final SeqSpan span, List<? extends SeqSymmetry> results, GenericFeature feature){
		Map<String, List<SeqSymmetry>> entries = SymLoader.splitResultsByTracks(results);
		for (Entry<String, List<SeqSymmetry>> entry : entries.entrySet()) {
			if (entry.getValue().isEmpty()) {
				continue;
			}
			SymLoader.filterAndAddAnnotations(entry.getValue(), span, feature.getURI(), feature);
			// Some format do not annotate. So it might not have method name. e.g bgn
			if (entry.getKey() != null) {
				feature.addMethod(entry.getKey());
			}
		}

		return (entries != null && !entries.isEmpty());
	}
	
	public static void filterAndAddAnnotations(
			List<? extends SeqSymmetry> feats, SeqSpan span, URI uri, GenericFeature feature) {
		if (feats == null || feats.isEmpty()) {
			return;
		}
		SeqSymmetry originalRequestSym = feature.getRequestSym();
		List<? extends SeqSymmetry> filteredFeats = filterOutExistingSymmetries(originalRequestSym, feats, span.getBioSeq());	
		if (filteredFeats.isEmpty()) {
			return;
		}
		if (filteredFeats.get(0) instanceof GraphSym) {
			GraphSym graphSym = (GraphSym)filteredFeats.get(0);
			if (filteredFeats.size() == 1 && graphSym.isSpecialGraph()) {
				BioSeq seq = graphSym.getGraphSeq();
				seq.addAnnotation(graphSym);
			}
			else {
				// We assume that if there are any GraphSyms, then we're dealing with a list of GraphSyms.
				for(SeqSymmetry feat : filteredFeats) {
					//grafs.add((GraphSym)feat);
					if (feat instanceof GraphSym) {
						GraphSymUtils.addChildGraph((GraphSym) feat, ((GraphSym) feat).getID(), ((GraphSym) feat).getGraphName(), uri.toString(), span);
					}
				}
			}

			return;
		}

		BioSeq seq = span.getBioSeq();
		for (SeqSymmetry feat : filteredFeats) {
			seq.addAnnotation(feat);
		}
	}


	private static List<? extends SeqSymmetry> filterOutExistingSymmetries(SeqSymmetry original_sym, List<? extends SeqSymmetry> syms, BioSeq seq) {
		List<SeqSymmetry> newSyms = new ArrayList<SeqSymmetry>(syms.size());	// roughly this size
		MutableSeqSymmetry dummySym = new SimpleMutableSeqSymmetry();
		for (SeqSymmetry sym : syms) {

			/**
			 * Since GraphSym is only SeqSymmetry containing all points.
			 * The intersection may find some points intersecting and
			 * thus not add whole GraphSym at all. So if GraphSym is encountered
			 * the it's not checked if it is intersecting. 
			 */
			if (sym instanceof GraphSym) {
				// if graphs, then adding to annotation BioSeq is handled by addChildGraph() method
				return syms;
			}

			dummySym.clear();
			if (SeqUtils.intersection(sym, original_sym, dummySym, seq)) {
				// There is an intersection with previous requests.  Ignore this symmetry
				continue;
			}
			newSyms.add(sym);
		}
		return newSyms;
	}

	public static List<BioSeq> getChromosomes(URI uri, String featureName, String groupID) throws Exception {
		AnnotatedSeqGroup temp_group = new AnnotatedSeqGroup(groupID);
		SymLoader temp = new SymLoader(uri, featureName, temp_group) {};
		List<? extends SeqSymmetry> syms = temp.getGenome();
		List<BioSeq> seqs = new ArrayList<BioSeq>();
		seqs.addAll(temp_group.getSeqList());
		
		// Force GC
		syms.clear();
		syms = null;
		temp = null;
		temp_group = null;

		return seqs;
	}

	public List<? extends SeqSymmetry> parse(InputStream is, boolean annotate_seq)
		throws Exception {
		FileTypeHandler fileTypeHandler = FileTypeHolder.getInstance().getFileTypeHandlerForURI("."+extension);
		if (fileTypeHandler == null) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "no handler for file type " + extension);
			return new ArrayList<SeqSymmetry>();
		}
		return fileTypeHandler.getParser().parse(new BufferedInputStream(is), group, featureName, uri.toString(), false);
	}
}
