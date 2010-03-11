package com.affymetrix.genometryImpl.parsers;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hiralv
 */
public class TwoBitParserTest {
	
	String noblocks = "ACTGGGTCTCAGTACTAGGAATTCCGTCATAGCTAAA";
	String noblocks_file = "test/data/2bit/noblocks.2bit";
	String nblocks = "NACNTCNNNNNNNNNNNNGTCTCANNNNNGTACTANNNNGGAATTCNNNNNCGTCATAGNNNCTAAANNN";
	String nblocks_file = "test/data/2bit/nblocks.2bit";
	String residues, file;
	File infile = null;


	@Before
	public void setup() throws Exception
	{
		assertTrue(new File(noblocks_file).exists());
		assertTrue(new File(nblocks_file).exists());
	}

	@Test
	public void testOriginals() throws Exception{
		residues = noblocks;
		file = noblocks_file;
		testOriginal();

		residues = nblocks;
		file = nblocks_file;
		testOriginal();

	}

	@Test
	public void testCaseFiles() throws Exception{
		residues = noblocks;
		file = noblocks_file;
		testCases();

		residues = nblocks;
		file = nblocks_file;
		testCases();
	}

	public void testOriginal() throws Exception
	{
		infile = new File(file);
		BioSeq seq= TwoBitParser.parse(infile);
		assertEquals(seq.getResidues(),residues);
		System.out.println(residues + "==" +seq.getResidues());
	}

	public void testCases() throws Exception
	{
		testCase(0,residues.length());  //From begining to end

		testCase(4,18);						// even, even
		testCase(6,7);						// even, odd
		testCase(1,4);						// odd , even
		testCase(1,5);						// odd , odd
//		testCase(-1,3);						// Start out of range
//		testCase(11,total_residues+4);		// End out of range
//		testCase(-5,total_residues+5);      // Start and end out of range

		testCase(2,22);
		testCase(6,7);
		testCase(1,2);
		testCase(5,15);
//		testCase(-9,9);
//		testCase(1,total_residues+9);
//		testCase(-15,total_residues+15);

		testCase(0,2);
		testCase(0,1);
		testCase(1,2);
		testCase(1,3);
//		testCase(-11,15);
//		testCase(10,total_residues+9);
//		testCase(-11,total_residues+15);
//
//		testCase(0,0);						// 0 length
//		testCase(1,0);						// Negative length
//		testCase(total_residues,0);
//		testCase(total_residues,-4);
//		testCase(total_residues,total_residues);
//		testCase(total_residues,total_residues+1);
//		testCase(total_residues+1,total_residues);
//		testCase(total_residues+1,total_residues+1);
//		testCase(total_residues+1,total_residues+5);
//		testCase(Integer.MIN_VALUE,Integer.MAX_VALUE);
	}

	public void testCase(int start, int end) throws Exception
	{
		infile = new File(file);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		boolean result = TwoBitParser.parse(infile,start,end,outStream);

		if (start < end) {
			start = Math.max(0, start);
			start = Math.min(residues.length(), start);

			end = Math.max(0, end);
			end = Math.min(residues.length(), end);
		}
		else
		{
			start = 0;
			end = 0;
		}

		assertTrue(result);
		assertEquals(residues.substring(start, end),outStream.toString());
		System.out.println(residues.substring(start, end) + "==" +outStream.toString());
		GeneralUtils.safeClose(outStream);
	}
}
