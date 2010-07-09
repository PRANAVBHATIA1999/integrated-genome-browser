package com.affymetrix.genometryImpl.symloader;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.general.SymLoader;
import com.affymetrix.genometryImpl.parsers.NibbleResiduesParser;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.SeekableStream;

/**
 *
 * @author jnicol
 */
public class BNIB extends SymLoader {
	private List<BioSeq> chrList = null;
	private final AnnotatedSeqGroup group;

	private static List<LoadStrategy> strategyList = new ArrayList<LoadStrategy>();
	static {
		// BAM files are generally large, so only allow loading visible data.
		strategyList.add(LoadStrategy.NO_LOAD);
		strategyList.add(LoadStrategy.VISIBLE);
		strategyList.add(LoadStrategy.CHROMOSOME);
	}

	public BNIB(URI uri, AnnotatedSeqGroup group) {
		super(uri);
		this.group = group;
		this.isResidueLoader = true;
	}

	@Override
	public void init() {
		if (this.isInitialized) {
			return;
		}
		super.init();
	}

	@Override
	public List<LoadStrategy> getLoadChoices() {
		return strategyList;
	}

	@Override
	public List<BioSeq> getChromosomeList() {
		if (this.chrList != null) {
			return this.chrList;
		}

		init();
		chrList = new ArrayList<BioSeq>(1);
		SeekableStream sis = null;
		try {
			BioSeq seq = NibbleResiduesParser.determineChromosome(LocalUrlCacher.getSeekableStream(uri), group);
			if (seq != null) {
				chrList.add(seq);
			}
		} catch (Exception ex) {
			Logger.getLogger(TwoBit.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			GeneralUtils.safeClose(sis);
		}
		return chrList;
	}

	@Override
	public String getRegionResidues(SeqSpan span) {
		init();

		SeekableStream sis = null;
		ByteArrayOutputStream outStream = null;
		try {
			outStream = new ByteArrayOutputStream();
			NibbleResiduesParser.parse(LocalUrlCacher.getSeekableStream(uri), span.getStart(), span.getEnd(), outStream);
			byte[] bytes = outStream.toByteArray();
			return new String(bytes);
		} catch (Exception ex) {
			Logger.getLogger(TwoBit.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		} finally {
			GeneralUtils.safeClose(outStream);
			GeneralUtils.safeClose(sis);
		}
	}

}
