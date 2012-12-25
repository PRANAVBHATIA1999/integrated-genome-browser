package com.affymetrix.genometryImpl.symloader;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.util.BlockCompressedStreamPosition;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.broad.tribble.readers.LineReader;
import org.broad.tribble.source.tabix.TabixLineReader;
import org.broad.tribble.util.BlockCompressedInputStream;

/**
 * This SymLoader is intended to be used for data sources that
 * are indexed with a tabix file. This SymLoader uses the TabixReader
 * from the Broad Institute
 */
public class SymLoaderTabix extends SymLoader {
	protected final Map<BioSeq, String> seqs = new HashMap<BioSeq, String>();
	private TabixLineReader tabixLineReader;
	private final LineProcessor lineProcessor;
	private static final List<LoadStrategy> strategyList = new ArrayList<LoadStrategy>();
	static {
		strategyList.add(LoadStrategy.NO_LOAD);
		strategyList.add(LoadStrategy.AUTOLOAD);
		strategyList.add(LoadStrategy.VISIBLE);
//		strategyList.add(LoadStrategy.CHROMOSOME);
		strategyList.add(LoadStrategy.GENOME);
	}
	
	public SymLoaderTabix(URI uri, String featureName, AnnotatedSeqGroup group, LineProcessor lineProcessor){
		super(uri, featureName, group);
		this.lineProcessor = lineProcessor;
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
		return tabixLineReader != null;
	}

	@Override
	public void init() throws Exception  {
		if (this.isInitialized){
			return;
		}
		try {
			String uriString = uri.toString();
			if (uriString.startsWith(FILE_PREFIX)) {
				uriString = uri.getPath();
			}
			this.tabixLineReader = new TabixLineReader(uriString);
		}
		catch (Exception x) {
			this.tabixLineReader = null;
			Logger.getLogger(SymLoaderTabix.class.getName()).log(Level.SEVERE,
						"Could not initialize tabix line reader for {0}.",
						new Object[]{featureName});
			return;
		}
		
		if (!isValid()) {
			throw new IllegalStateException("tabix file does not exist or was not read");
		}
		
		lineProcessor.init(uri);
		for (String seqID : tabixLineReader.getSequenceNames()) {
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
	public List<BioSeq> getChromosomeList() throws Exception  {		
		init();
		return new ArrayList<BioSeq>(seqs.keySet());
	}

	@Override
	 public List<? extends SeqSymmetry> getGenome() throws Exception  {
		init();
		List<BioSeq> allSeq = getChromosomeList();
		List<SeqSymmetry> retList = new ArrayList<SeqSymmetry>();
		for(BioSeq seq : allSeq){
			retList.addAll(getChromosome(seq));
		}
		return retList;
	 }

	@Override
	public List<? extends SeqSymmetry> getChromosome(BioSeq seq) throws Exception  {
		return getRegion(new SimpleSeqSpan(0, Integer.MAX_VALUE / 2, seq)); // end faked
	}

	@Override
	public List<? extends SeqSymmetry> getRegion(SeqSpan overlapSpan) throws Exception  {
		init();
		String seqID = seqs.get(overlapSpan.getBioSeq());
		final LineReader lineReader = tabixLineReader.query(seqID, (overlapSpan.getStart() + 1), + overlapSpan.getEnd());
		if (lineReader == null) {
			return new ArrayList<SeqSymmetry>();
		}
		long[] startEnd = getStartEnd(lineReader);
		if(startEnd == null){
			return new ArrayList<SeqSymmetry>();
		}
		System.out.println("***** " + startEnd[0] + ":" + startEnd[1]);
		return lineProcessor.processLines(overlapSpan.getBioSeq(), lineReader, this);
    }

	private long[] getStartEnd(LineReader lineReader) {
		long[] startEnd = new long[2];
		try {
			Field field = lineReader.getClass().getDeclaredField("it");
			field.setAccessible(true);
			Object it = field.get(lineReader);
			// Probably no data in the region
			if(it == null){
				return null;
			}
			field = it.getClass().getDeclaredField("off");
			field.setAccessible(true);
			Object[] off = (Object[])field.get(it);
			field = off[0].getClass().getDeclaredField("u");
			field.setAccessible(true);
			long startPos = (Long)field.get(off[0]);
			startEnd[0] = new BlockCompressedStreamPosition(startPos).getApproximatePosition();
			field = off[off.length - 1].getClass().getDeclaredField("v");
			field.setAccessible(true);
			long endPos = (Long)field.get(off[0]);
			startEnd[1] = new BlockCompressedStreamPosition(endPos).getApproximatePosition();
		}
		catch(IllegalAccessException x) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "unable to display progress for " + uri, x);
		}
		catch(NoSuchFieldException x) {
			Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "unable to display progress for " + uri, x);
		}
		return startEnd;
	}

    /**
     * copied from the igv 1.5.64 source
     * @param path path of data source
     * @return if the data source has a valid tabix index
     */
    public static boolean isTabix(String path) {
        if (!path.endsWith("gz")) {
            return false;
        }

        BlockCompressedInputStream is = null;
        try {
            if (path.startsWith("ftp:")) {
            	return false; // ftp not supported by BlockCompressedInputStream
            }
            else if (path.startsWith("http:") || path.startsWith("https:")) {
                is = new BlockCompressedInputStream(new URL(path + ".tbi"));
            }
            else {
                is = new BlockCompressedInputStream(new File(URLDecoder.decode(path, GeneralUtils.UTF8) + ".tbi"));
            }

            byte[] bytes = new byte[4];
            is.read(bytes);
            return (char) bytes[0] == 'T' && (char) bytes[1] == 'B';
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }

    public static SymLoader getSymLoader(SymLoader sym){
		try {
			URI uri = new URI(sym.uri.toString() + ".tbi");
			if(LocalUrlCacher.isValidURI(uri)){
				String uriString = sym.uri.toString();
				if (uriString.startsWith(FILE_PREFIX)) {
					uriString = sym.uri.getPath();
				}
				if (isTabix(uriString) && sym instanceof LineProcessor) {
					return new SymLoaderTabix(sym.uri, sym.featureName, sym.group, (LineProcessor)sym);
				}
			}
		} catch (URISyntaxException ex) {
			Logger.getLogger(SymLoaderTabix.class.getName()).log(Level.SEVERE, null, ex);
		}
		return sym;
	}
}

