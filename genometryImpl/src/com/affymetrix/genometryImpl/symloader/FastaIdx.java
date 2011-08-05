package com.affymetrix.genometryImpl.symloader;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.SeqSpan;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.picard.reference.IndexedFastaSequenceFile;
import net.sf.picard.reference.ReferenceSequence;

/**
 * For fasta files with a SamTools .fai index created
 * with the "samtools faidx" utility
 */
public class FastaIdx extends FastaCommon {
	final IndexedFastaSequenceFile fastaFile;

	public FastaIdx(URI uri, String featureName, AnnotatedSeqGroup group) {
		super(uri, "", group);
		IndexedFastaSequenceFile tempFile;
		try {
			tempFile = new IndexedFastaSequenceFile(new File(uri));
		}
		catch (FileNotFoundException x) {
			tempFile = null;
		}
		fastaFile = tempFile;
	}

	public List<BioSeq> getSequenceList(FastaSequenceIndex fsi) {
		List<BioSeq> sequenceList = new ArrayList<BioSeq>();
		Iterator<FastaSequenceIndexEntry> iter = fsi.iterator();
		while (iter.hasNext()) {
			FastaSequenceIndexEntry ent = iter.next();
			BioSeq seq = new BioSeq(ent.getContig(), "", (int)ent.getSize());
			sequenceList.add(seq);
		}
		return sequenceList;
	}

	/**
	 * Get seqids and lengths for all chromosomes.
	 */
	@Override
	protected boolean initChromosomes() throws Exception {
		String uriString = uri.toString();
		if (uriString.startsWith(FILE_PREFIX)) {
			uriString = uri.getPath();
		}
		FastaSequenceIndex fsi = new FastaSequenceIndex(new File(uriString + ".fai"));
		List<BioSeq> sequenceList = getSequenceList(fsi);
		for (BioSeq seqLoop : sequenceList) {
			String seqid = seqLoop.getID();
			BioSeq seq = group.getSeq(seqid);
			int count = seqLoop.getLength();
			if (seq == null) {
				chrSet.add(new BioSeq(seqid, "", count));
			} else {
				group.addSeq(seqid, count);
				chrSet.add(seq);
			}
		}
		return true;
	}

	@Override
	public String getRegionResidues(SeqSpan span) throws Exception {
		ReferenceSequence sequence = fastaFile.getSubsequenceAt(span.getBioSeq().getID(), span.getMin() + 1, Math.min(span.getMax() + 1, span.getBioSeq().getLength()));
		return new String(sequence.getBases());
	}

	/**
	 * @return if this SymLoader is valid, there is a readable
	 * fasta index file for the data source
	 */
	public boolean isValid() {
		return fastaFile != null;
	}
}
