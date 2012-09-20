package com.affymetrix.genometryImpl.symloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.broad.tribble.readers.LineReader;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.comparator.BioSeqComparator;
import com.affymetrix.genometryImpl.comparator.SeqSymMinComparator;
import com.affymetrix.genometryImpl.thread.CThreadHolder;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;

/**
 * This SymLoader is designed to be used for FileTypes that have a LineReader
 * for Tabix, but there is no .tbi index for the file. This SymLoader will use
 * the same LineReader, but not use the .tbi index.
 */
public abstract class UnindexedSymLoader extends SymLoader {
	
	private static final List<LoadStrategy> strategyList = new ArrayList<LoadStrategy>();
	static {
		strategyList.add(LoadStrategy.NO_LOAD);
		strategyList.add(LoadStrategy.VISIBLE);
//		strategyList.add(LoadStrategy.CHROMOSOME);
		strategyList.add(LoadStrategy.GENOME);
	}
	private LineProcessor lineProcessor;

	public UnindexedSymLoader(URI uri, String featureName, AnnotatedSeqGroup group){
		super(uri, featureName, group);
		lineProcessor = createLineProcessor(featureName);
	}

	protected abstract LineProcessor createLineProcessor(String featureName);

	@Override
	public List<LoadStrategy> getLoadChoices() {
		return strategyList;
	}

	@Override
	public void init() throws Exception {
		if (this.isInitialized) {
			return;
		}
		lineProcessor.init(uri);

		if (buildIndex()){
			super.init();
		}
	}

	@Override
	public List<BioSeq> getChromosomeList() throws Exception {
		init();
		List<BioSeq> chromosomeList = new ArrayList<BioSeq>(chrList.keySet());
		Collections.sort(chromosomeList,new BioSeqComparator());
		return chromosomeList;
	}

	@Override
	public List<? extends SeqSymmetry> getGenome() throws Exception {
		init();
		List<BioSeq> allSeq = getChromosomeList();
		List<SeqSymmetry> retList = new ArrayList<SeqSymmetry>();
		for(BioSeq seq : allSeq){
			retList.addAll(getChromosome(seq));
		}
		return retList;
	}


	@Override
	public List<? extends SeqSymmetry> getChromosome(BioSeq seq) throws Exception {
		init();
		return parse(seq,seq.getMin(),seq.getMax());
	}


	@Override
	public List<? extends SeqSymmetry> getRegion(SeqSpan span) throws Exception {
		init();
		return parse(span.getBioSeq(),span.getMin(),span.getMax());
	}

	protected LineReader getLineReader(final BufferedReader br, final int min, final int max) {

		return (new LineReader() {

			@Override
			public String readLine() throws IOException {
				String line = br.readLine();
				if (line == null || lineProcessor.getSpan(line).getMin() >= max) {
					return null;
				}
				while (line != null && lineProcessor.getSpan(line).getMax() <= min) {
					line = br.readLine();
				}
				return line;
			}

			@Override
			public void close() {
			}
		});
	}
	
	private List<? extends SeqSymmetry> parse(BioSeq seq, final int min, final int max) throws Exception {
		InputStream istr = null;
		try {
			File file = chrList.get(seq);
			if (file == null) {
				Logger.getLogger(UnindexedSymLoader.class.getName()).log(Level.FINE, "Could not find chromosome {0}", seq.getID());
				return Collections.<SeqSymmetry>emptyList();
			}
			istr = new FileInputStream(file);
			final BufferedReader br = new BufferedReader(new InputStreamReader(istr));
			
			LineReader lineReader = getLineReader(br, min, max);
				
			parseLinesProgressUpdater = new ParseLinesProgressUpdater("Unindexed process lines " + uri, 0, file.length());
			CThreadHolder.getInstance().getCurrentCThreadWorker().setProgressUpdater(parseLinesProgressUpdater);
			return lineProcessor.processLines(seq, lineReader, this);
		} finally {
			GeneralUtils.safeClose(istr);
		}
	}
	public Comparator<SeqSymmetry> getComparator(BioSeq seq) {
		return new SeqSymMinComparator(seq);
	}

	public int getMin(SeqSymmetry sym, BioSeq seq) {
		SeqSpan span = sym.getSpan(seq);
		return span.getMin();
	}

	public int getMax(SeqSymmetry sym, BioSeq seq) {
		SeqSpan span = sym.getSpan(seq);
		return span.getMax();
	}

	public LineProcessor getLineProcessor() {
		return lineProcessor;
	}

	protected boolean parseLines(InputStream istr, Map<String, Integer> chrLength, Map<String, File> chrFiles) throws Exception {
		parseLinesProgressUpdater = new ParseLinesProgressUpdater("Unindexed parse lines " + uri);
		CThreadHolder.getInstance().getCurrentCThreadWorker().setProgressUpdater(parseLinesProgressUpdater);
		BufferedReader br = null;
		BufferedWriter bw = null;

		Map<String, Boolean> chrTrack = new HashMap<String, Boolean>();
		Map<String, BufferedWriter> chrs = new HashMap<String, BufferedWriter>();
		List<String> infoLines = new ArrayList<String>();
		String line, seq_name = null, trackLine = null;
		char ch;
		try {
			Thread thread = Thread.currentThread();
			br = new BufferedReader(new InputStreamReader(istr));
			lastSleepTime = System.nanoTime();
			while ((line = br.readLine()) != null && (!thread.isInterrupted())) {
				notifyReadLine(line.length());
				
				ch = line.charAt(0);
				if (ch == 't' && line.startsWith("track")) {
					trackLine = line;
					chrTrack.clear();
					continue;
				}
				
				if(lineProcessor.processInfoLine(line, infoLines)){
					continue;
				}
				
				SeqSpan span = lineProcessor.getSpan(line);
				seq_name = span.getBioSeq().getID(); // seq id field
				int end = span.getMax();

				bw = chrs.get(seq_name);
				if (bw == null) {
					addToLists(chrs, seq_name, chrFiles, chrLength, "." + getExtension());
					bw = chrs.get(seq_name);
					for (String infoLine : infoLines) {
						bw.write(infoLine + "\n");
					}
				}
				
				if (!chrTrack.containsKey(seq_name)) {
					chrTrack.put(seq_name, true);
					if (trackLine != null) {
						bw.write(trackLine + "\n");
					}
				}
				
				bw.write(line + "\n");

				if (end > chrLength.get(seq_name)) {
					chrLength.put(seq_name, end);
				}

			}

			return !thread.isInterrupted();
		} finally {
			for (BufferedWriter b : chrs.values()) {
				GeneralUtils.safeClose(b);
			}
			GeneralUtils.safeClose(br);
			GeneralUtils.safeClose(bw);
		}
	}

	@Override
	protected void createResults(Map<String, Integer> chrLength, Map<String, File> chrFiles){
		for(Entry<String, Integer> bioseq : chrLength.entrySet()){
			String seq_name = bioseq.getKey();
			BioSeq seq = group.getSeq(seq_name);
			if (seq == null) {
				//System.out.println("seq not recognized, creating new seq: " + seq_name);
				seq = group.addSeq(seq_name, bioseq.getValue(), uri.toString());
			}
			
			chrList.put(seq, chrFiles.get(seq_name));
		}
	}
}
