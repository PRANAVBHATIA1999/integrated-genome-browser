package com.affymetrix.genometryImpl.general;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.GraphSym;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.UcscPslSym;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.GraphSymUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.genometryImpl.util.ParserController;
import java.io.InputStream;
import com.affymetrix.genometryImpl.util.SortTabFile;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
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
	public final URI uri;
	protected final String extension;	// used for ServerUtils call
	public boolean isResidueLoader = false;	// Let other classes know if this is just residues
	protected volatile boolean isInitialized = false;
	protected final Map<BioSeq,File> chrList = new HashMap<BioSeq,File>();
	private final Map<String,Boolean> chrSort = new HashMap<String,Boolean>();
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
		
		String uriString = uri.toASCIIString().toLowerCase();
		String unzippedStreamName = GeneralUtils.stripEndings(uriString);
		extension = ParserController.getExtension(unzippedStreamName);

    }

	protected void init() {
		this.isInitialized = true;
	}

	protected void buildIndex(){
		BufferedInputStream bis = null;
		Map<String, Integer> chrLength = new HashMap<String, Integer>();
		Map<String, File> chrFiles = new HashMap<String, File>();

		try {
			bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(uri);
			parseLines(bis, chrLength, chrFiles);
			createResults(chrLength, chrFiles);
		} catch (Exception ex) {
			Logger.getLogger(SymLoader.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			GeneralUtils.safeClose(bis);
		}

	}

	protected void sortCreatedFiles(){
		//Now Sort all files
		for (Entry<BioSeq, File> file : chrList.entrySet()) {
			chrSort.put(file.getKey().getID(), SortTabFile.sort(file.getValue()));
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
	public List<BioSeq> getChromosomeList() {
		return Collections.<BioSeq>emptyList();
	}
	
    /**
     * @return List of symmetries in genome
     */
    public List<? extends SeqSymmetry> getGenome() {

		if (GraphSymUtils.isAGraphFilename(this.extension)) {
			BufferedInputStream bis = null;
			try {
				GenometryModel gmodel = GenometryModel.getGenometryModel();
				bis = LocalUrlCacher.convertURIToBufferedStream(this.uri);
				List<GraphSym> graphs = GraphSymUtils.readGraphs(bis, this.uri.toString(), gmodel, gmodel.getSelectedSeqGroup(), null);
				GraphSymUtils.setName(graphs, GraphSymUtils.getGraphNameForURL(this.uri.toURL()));
				return graphs;
			} catch (Exception ex) {
				Logger.getLogger(SymLoader.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				GeneralUtils.safeClose(bis);
			}
		}
		
		List<? extends SeqSymmetry> feats = null;
		try {
			BufferedInputStream bis = null;
			try {
				// This will also unzip the stream if necessary
				bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(this.uri);
				feats = FeatureRequestSym.Parse(this.extension, this.uri, bis, group, this.featureName, null);
				return feats;
			} catch (FileNotFoundException ex) {
				Logger.getLogger(SymLoader.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				GeneralUtils.safeClose(bis);
			}
			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Logger.getLogger(this.getClass().getName()).log(
					Level.SEVERE, "Retrieving genome is not defined");
        return null;
    }

    /**
     * @param seq - chromosome
     * @return List of symmetries in chromosome
     */
    public List<? extends SeqSymmetry> getChromosome(BioSeq seq) {
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
    public List<? extends SeqSymmetry> getRegion(SeqSpan overlapSpan) {
		Logger.getLogger(this.getClass().getName()).log(
					Level.WARNING, "Retrieving region is not supported.  Returning entire chromosome.");
		List<? extends SeqSymmetry> chrResults = this.getChromosome(overlapSpan.getBioSeq());
		return chrResults;
    }

	/**
     * Get residues in the region of the chromosome.  This is generally only defined for some parsers
     * @param span - span of chromosome
     * @return String of residues
     */
    public String getRegionResidues(SeqSpan span) {
		Logger.getLogger(this.getClass().getName()).log(
					Level.WARNING, "Not supported.  Returning empty string.");
		return "";
    }

	protected void parseLines(InputStream istr, Map<String, Integer> chrLength, Map<String, File> chrFiles){
		Logger.getLogger(this.getClass().getName()).log(
					Level.SEVERE, "parseLines is not defined");
	}
	
	protected static void addToLists(
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
			chrList.put(group.addSeq(key, bioseq.getValue()), chrFiles.get(key));
		}
	}

}
