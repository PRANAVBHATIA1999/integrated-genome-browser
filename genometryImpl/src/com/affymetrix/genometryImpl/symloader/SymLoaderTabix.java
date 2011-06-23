package com.affymetrix.genometryImpl.symloader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.broad.tribble.readers.TabixReader;

/**
 * This SymLoader is intended to be used for data sources that
 * are indexed with a tabix file. This SymLoader uses the TabixReader
 * from the Broad Institute
 */
public class SymLoaderTabix extends SymLoader {

	protected final Map<BioSeq, String> seqs = new HashMap<BioSeq, String>();
	private TabixReader tabixReader;
	private final LineProcessor lineProcessor;
	private static final List<LoadStrategy> strategyList = new ArrayList<LoadStrategy>();
	static {
		strategyList.add(LoadStrategy.NO_LOAD);
		strategyList.add(LoadStrategy.VISIBLE);
		strategyList.add(LoadStrategy.CHROMOSOME);
		strategyList.add(LoadStrategy.GENOME);
	}
	
	public SymLoaderTabix(URI uri, String featureName, AnnotatedSeqGroup group, LineProcessor lineProcessor){
		super(uri, featureName, group);
		this.lineProcessor = lineProcessor;
		try {
			String uriString = uri.toString();
			if (uriString.startsWith(FILE_PREFIX)) {
				uriString = uri.getPath();
			}
			this.tabixReader = new TabixReader(uriString);
		}
		catch (Exception x) {
			this.tabixReader = null;
		}
	}

	@Override
	public List<LoadStrategy> getLoadChoices() {
		return strategyList;
	}

	/**
	 * @return if this SymLoader is valid, there is a readable
	 * tabix file for the data source
	 */
	public boolean isValid() {
		return tabixReader != null;
	}

	@Override
	public void init(){
		if (!isValid()) {
			throw new IllegalStateException("tabix file does not exist or was not read");
		}
		if (this.isInitialized){
			return;
		}
		super.init();
		lineProcessor.init(uri);
		for (String seqID : tabixReader.getSequenceNames()) {
			BioSeq seq = group.getSeq(seqID);
			if (seq == null) {
				int length = 1000000000;
				seq = group.addSeq(seqID, length);
				Logger.getLogger(SymLoaderTabix.class.getName()).log(Level.INFO,
						"Sequence not found. Adding {0} with default length {1}",
						new Object[]{seqID,length});
			}
			seqs.put(seq, seqID);
		}
		this.isInitialized = true;
	}

	public LineProcessor getLineProcessor() {
		return lineProcessor;
	}

	@Override
	public List<String> getFormatPrefList() {
		return lineProcessor.getFormatPrefList();
	}

	@Override
	public List<BioSeq> getChromosomeList(){		
		init();
		return new ArrayList<BioSeq>(seqs.keySet());
	}

	@Override
	 public List<? extends SeqSymmetry> getGenome() {
		init();
		List<BioSeq> allSeq = getChromosomeList();
		List<SeqSymmetry> retList = new ArrayList<SeqSymmetry>();
		for(BioSeq seq : allSeq){
			retList.addAll(getChromosome(seq));
		}
		return retList;
	 }

	@Override
	public List<? extends SeqSymmetry> getChromosome(BioSeq seq) {
		init();
		String seqID = seqs.get(seq);
		return lineProcessor.processLines(seq, tabixReader.query(seqID));
	}

	@Override
	public List<? extends SeqSymmetry> getRegion(SeqSpan overlapSpan) {
		init();
		String seqID = seqs.get(overlapSpan.getBioSeq());
		TabixReader.TabixLineReader tabixLineReader = tabixReader.query(seqID + ":" + (overlapSpan.getStart() + 1) + "-" + overlapSpan.getEnd());
		if (tabixLineReader == null) {
			return new ArrayList<SeqSymmetry>();
		}
		return lineProcessor.processLines(overlapSpan.getBioSeq(), tabixLineReader);
    }
	
	public static SymLoader getSymLoader(SymLoader sym){
		try {
			URI uri = new URI(sym.uri.toString() + ".tbi");
			if(LocalUrlCacher.isValidURI(uri)){
				String uriString = sym.uri.toString();
				if (uriString.startsWith(FILE_PREFIX)) {
					uriString = sym.uri.getPath();
				}
				if (TabixReader.isTabix(uriString)) {
					return new SymLoaderTabix(sym.uri, sym.featureName, sym.group, (LineProcessor)sym);
				}
			}
		} catch (URISyntaxException ex) {
			Logger.getLogger(SymLoaderTabix.class.getName()).log(Level.SEVERE, null, ex);
		}
		return sym;
	}
}

