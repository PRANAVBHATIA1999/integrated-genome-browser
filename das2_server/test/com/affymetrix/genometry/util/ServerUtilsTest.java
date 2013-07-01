package com.affymetrix.genometry.util;

import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.comparator.UcscPslComparator;
import com.affymetrix.genometryImpl.parsers.BpsParser;
import com.affymetrix.genometryImpl.parsers.BpsParserTest;
import com.affymetrix.genometryImpl.parsers.ChromInfoParser;
import com.affymetrix.genometryImpl.parsers.IndexWriter;
import com.affymetrix.genometryImpl.parsers.PSLParser;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.symmetry.UcscPslSym;
import com.affymetrix.genometryImpl.util.DasServerUtils;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.IndexingUtils;
import com.affymetrix.genometryImpl.util.IndexingUtils.IndexedSyms;
import com.affymetrix.genometryImpl.util.Optimize;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jnicol
 */
public class ServerUtilsTest {
	private static GenometryModel gmodel = GenometryModel.getGenometryModel();
	private static String baseDir = "test/data/server/A_thaliana";
	private static String versionString = "A_thaliana_TAIR8";
	AnnotatedSeqGroup genome = null;

	@Before
	public void setUp() {
		InputStream chromstream = null;
		InputStream istr = null;
		try {
			// Load chromosomes
			File chrom_info_file = new File(baseDir + "/" + versionString + "/mod_chromInfo.txt");
			chromstream = new FileInputStream(chrom_info_file);
			ChromInfoParser.parse(chromstream, gmodel.addSeqGroup(versionString));
			
			// Load genome
			genome = gmodel.getSeqGroup(versionString);
			String stream_name = baseDir + "/" + versionString + "/mRNA1.mm.psl";
			File current_file = new File(stream_name);
			istr = new BufferedInputStream(new FileInputStream(current_file));

			PSLParser parser = new PSLParser();
			parser.setTrackNamePrefix("blah");
			parser.setCreateContainerAnnot(true);
			parser.parse(istr, "mRNA1.sm", null, genome, null, false, true, false);  // annotate target

			// optimize genome by replacing second-level syms with IntervalSearchSyms
			Optimize.genome(genome);

		} catch (Exception ex) {
			Logger.getLogger(ServerUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
			fail();
		} finally {
			GeneralUtils.safeClose(istr);
			GeneralUtils.safeClose(chromstream);
		}
	}

	@Test
	public void testGenome() {
		assertNotNull(genome);
		assertEquals("A_thaliana_TAIR8", genome.getID());
		assertEquals(7, genome.getSeqCount());
		BioSeq seq = genome.getSeq("chr1");
		assertNotNull(seq);
	}
	
	@Test
	public void testOverlapAndInsideSpan() {
		String seqid="chr1";

		String overlap = "90000:11200177";
		SeqSpan overlap_span = DasServerUtils.getLocationSpan(seqid, overlap, genome);

		assertNotNull(overlap_span);
		assertEquals(90000,overlap_span.getMin());
		assertEquals(11200177,overlap_span.getMax());

		String query_type="mRNA1.sm";
		List<SeqSymmetry> result = null;
		result = DasServerUtils.getOverlappedSymmetries(overlap_span, query_type);
		assertNotNull(result);
		
		List<UcscPslSym> tempResult = new ArrayList<UcscPslSym>(result.size());
		for(SeqSymmetry res : result) {
			tempResult.add((UcscPslSym)res);
		}

		Comparator<UcscPslSym> UCSCCompare = new UcscPslComparator();
		Collections.sort(tempResult,UCSCCompare);
		
		assertEquals(384,tempResult.size());
		assertEquals(136731, tempResult.get(0).getTargetMin());
		assertEquals(137967, tempResult.get(0).getTargetMax());

		String inside = "92000:4600000";
		SeqSpan inside_span = DasServerUtils.getLocationSpan(seqid, inside, genome);
		assertNotNull(inside_span);
		assertEquals(92000,inside_span.getMin());
		assertEquals(4600000,inside_span.getMax());
		assertEquals(seqid, inside_span.getBioSeq().getID());

		result = DasServerUtils.specifiedInsideSpan(inside_span, result);
		assertEquals(138, result.size());
	}


	/**
	 * Testing that max overlap method works as designed.
	 * Testing half-open spans on both sides, inside spans, and outside spans.
	 */
	@Test
	public void testInternalOverlapCode() {
		int[] overlapRange = new int[2];
		int[] outputRange = new int[2];
		overlapRange[0] = 30068522;
		overlapRange[1] = 30072392;
		int[]min = new int[6];
		int[]max = new int[6];
		min[0] = 30068510; max[0] = 30068520; // completely outside range on left side; should be excluded
		min[1] = 30068521; max[1] = 30072392; // outside range on left side; should be included
		min[2] = 30068522; max[2] = 30072392; // exactly the range; should be included
		min[3] = 30068522; max[3] = 30072393; // outside range on right side; should be included
		min[4] = 30068532; max[4] = 30072382; // completely inside the range; should be included
		min[5] = 30072400; max[5] = 30072410; // completely outside range on right side; should be excluded
		
		IndexingUtils.findMaxOverlap(overlapRange, outputRange, min, max);
		assertEquals(1,outputRange[0]);
		assertEquals(4,outputRange[1]);
		
		overlapRange[0] = 30068523;
		overlapRange[1] = 30072392;
		IndexingUtils.findMaxOverlap(overlapRange, outputRange, min, max);
		assertEquals(1,outputRange[0]);
		assertEquals(4,outputRange[1]);

		overlapRange[0] = 30068523;
		overlapRange[1] = 30068530;
		IndexingUtils.findMaxOverlap(overlapRange, outputRange, min, max);
		assertEquals(1,outputRange[0]);
		assertEquals(3,outputRange[1]);

		overlapRange[0] = 30068530;
		overlapRange[1] = 30068531;
		IndexingUtils.findMaxOverlap(overlapRange, outputRange, min, max);
		assertEquals(1,outputRange[0]);
		assertEquals(3,outputRange[1]);

		overlapRange[0] = 30068510;
		overlapRange[1] = 30068520;
		IndexingUtils.findMaxOverlap(overlapRange, outputRange, min, max);
		assertEquals(0,outputRange[0]);
		assertEquals(0,outputRange[1]);

		overlapRange[0] = 30068511;
		overlapRange[1] = 30068520;
		IndexingUtils.findMaxOverlap(overlapRange, outputRange, min, max);
		assertEquals(0,outputRange[0]);
		assertEquals(0,outputRange[1]);

		overlapRange[0] = 30068510;
		overlapRange[1] = 30068519;
		IndexingUtils.findMaxOverlap(overlapRange, outputRange, min, max);
		assertEquals(0,outputRange[0]);
		assertEquals(0,outputRange[1]);
	}
	
	@Test
	public void testOverlap2() {
		String seqid="chr1";

		String overlap = "30068522:30072392";
		SeqSpan overlap_span = DasServerUtils.getLocationSpan(seqid, overlap, genome);

		assertNotNull(overlap_span);
		//assertEquals(22949908,overlap_span.getMin());
		//assertEquals(22950581,overlap_span.getMax());

		String query_type="mRNA1.sm";
		List<SeqSymmetry> result = null;
		result = DasServerUtils.getOverlappedSymmetries(overlap_span, query_type);
		assertNotNull(result);
		assertEquals(6, result.size());	// not sure why all 6 of these are here

		BioSeq seq = genome.getSeq(seqid);
		assertNotNull(seq);

		IndexWriter iWriter = new PSLParser();
		List<SeqSymmetry> sortedSyms = IndexingUtils.getSortedAnnotationsForChrom(
				result, seq, iWriter.getComparator(seq));
		assertEquals(6, sortedSyms.size());
		/*for(SeqSymmetry res : sortedSyms) {
			tempResult.add((UcscPslSym)res);
			System.out.println("sorted ID: " + res.getID());
		}*/

		String testFileName = baseDir + "/" + versionString + "/mRNA_INDEX_TEST.psl";
		File testFile = new File(testFileName);
		IndexedSyms iSyms = new IndexedSyms(sortedSyms.size(), testFile, query_type, "psl", iWriter);
		try {
			IndexingUtils.writeIndexedAnnotations(sortedSyms, seq, genome, iSyms);
		} catch (IOException ex) {
			Logger.getLogger(ServerUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
			fail();
		}

		List<SeqSymmetry> result2 = DasServerUtils.getIndexedOverlappedSymmetries(overlap_span, iSyms, "testOUT", genome);
		/*	for(SeqSymmetry res : result2) {
			tempResult.add((UcscPslSym)res);
			System.out.println("ID: " + res.getID());
		}*/
		assertEquals(6, result2.size());

		if (testFile.exists()) {
			testFile.delete();
		}
	}

	

}
