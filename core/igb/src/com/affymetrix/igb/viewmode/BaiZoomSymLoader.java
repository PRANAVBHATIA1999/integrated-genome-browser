package com.affymetrix.igb.viewmode;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import net.sf.samtools.StubBAMFileIndex;
import net.sf.samtools.util.SeekableFileStream;
import net.sf.samtools.util.SeekableHTTPStream;
import net.sf.samtools.util.SeekableStream;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.parsers.FileTypeHolder;
import com.affymetrix.genometryImpl.symloader.SymLoader;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.SynonymLookup;
import com.affymetrix.igb.viewmode.IndexZoomSymLoader;

public class BaiZoomSymLoader extends IndexZoomSymLoader {

	public BaiZoomSymLoader(URI uri, String featureName, AnnotatedSeqGroup group) {
		super(uri, featureName, group);
	}

	@Override
	protected SymLoader getDataFileSymLoader() throws Exception {
		return FileTypeHolder.getInstance().getFileTypeHandler("bam").createSymLoader(getBamURI(uri), featureName, group);
	}

	private int getRefNo(String igbSeq, SAMSequenceDictionary ssd) {
		List<SAMSequenceRecord> sList = ssd.getSequences();
		for (int i = 0; i < sList.size(); i++) {
			String bamSeq = SynonymLookup.getChromosomeLookup().getPreferredName(sList.get(i).getSequenceName());
			if (igbSeq.equals(bamSeq)) {
				return i;
			}
		}
		return -1;
	}

	private URI getBamURI(URI baiUri) throws Exception {
		String bamUriString = baiUri.toString().substring(0, baiUri.toString().length() - ".bai".length());
		if (!bamUriString.startsWith("file:") && !bamUriString.startsWith("http:") && !bamUriString.startsWith("https:") && !bamUriString.startsWith("ftp:")) {
			bamUriString = GeneralUtils.getFileScheme() + bamUriString;
		}
		return new URI(bamUriString);
	}

	protected SeekableStream getSeekableStream(URI uri) throws Exception {
		if (uri.toString().startsWith("http:") || uri.toString().startsWith("https:")) {
			return new SeekableHTTPStream(new URL(uri.toString()));
		} else {
			return new SeekableFileStream(new File(GeneralUtils.fixFileName(uri.toString())));
		}
	}

	@Override
	protected Iterator<Map<Integer, List<List<Long>>>> getBinIter(String seq) {
		try {
			SeekableStream ssData = getSeekableStream(getBamURI(uri));
			SeekableStream ssIndex = getSeekableStream(uri);
			SAMFileReader sfr = new SAMFileReader(ssData, false);
			SAMSequenceDictionary ssd = sfr.getFileHeader().getSequenceDictionary();
			int refno = getRefNo(seq.toString(), ssd);
			if (refno == -1) {
				return null;
			} else {
				StubBAMFileIndex sbfi = new StubBAMFileIndex(ssIndex, uri, ssd);
				return sbfi.getBinIter(refno);
			}
		} catch (Exception x) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "cannot read BAI file " + uri, x);
			return null;
		}
	}
}
