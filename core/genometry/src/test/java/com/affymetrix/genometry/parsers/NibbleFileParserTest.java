package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.util.GeneralUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.lorainelab.igb.synonymlookup.services.impl.ChromosomeSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.GenomeVersionSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.SpeciesSynonymsLookupImpl;

/**
 * About this test - and bnib: Affymetrix many years ago created a random access
 * sequence file type called bnib. So far as I know (Ann, Jan 2018) no-one uses
 * now, except possibly David Nix and his DAS2 server at University of Utah. So
 * we are leaving it in the code base for now. However, we are using 2bit for
 * IGB Quickload because Jim Kent has made various programs for 2bit that make
 * 2bit more convenient.
 *
 * @author hiralv
 */
public class NibbleFileParserTest {

    String input_string = "ACTGGGTCTCAGTACTAGGAATTCCGTCATAGCTAAA";
    String infile_name = "data/bnib/BNIB_chrQ2.bnib";
    File infile = new File(NibbleFileParserTest.class.getClassLoader().getResource(infile_name).getFile());
    StringBuffer sb;
    InputStream isr;
    int total_residues = input_string.length();

    @Test
    public void testOriginal() throws Exception {
        sb = new StringBuffer();
        isr = GeneralUtils.getInputStream(infile, sb);
        // IGBF-1203: test fails without setUseSynonyms(false) because 
        // chrSynLookup variable in GenomeVersion object is null. 
        // Weirdly, testing like this passes: mvn clean install -DskipTests=False
        // Testing like this fails: mvn -Dtest=NibbleFileParserTest#testOriginal -DfailIfNoTests=false test
        // Bitbucket pipeline also fails.
        // It has something to do with the bundle context (OSGI container) not
        // activating the synonym service and setting chrSynLookup correctly. 
        GenomeVersion genomeVersion = new GenomeVersion("This is a fake genome.");
        genomeVersion.setUseSynonyms(false); // don't use synonym service
        // It's the right thing to not use the synonym service because if we do,
        // then we're testing the service AND the parser. This test should test the
        // parser, nothing else. 
        BioSeq seq = NibbleResiduesParser.parse(isr, genomeVersion);
        assertEquals(seq.getResidues(), input_string);
    }

    @Test
    public void testCases() throws Exception {
        testCase(0, input_string.length());  //From begining to end

        testCase(4, 18);						// even, even
        testCase(6, 7);						// even, odd
        testCase(1, 4);						// odd , even
        testCase(1, 5);						// odd , odd
        testCase(-1, 3);						// Start out of range
        testCase(11, total_residues + 4);		// End out of range
        testCase(-5, total_residues + 5);      // Start and end out of range

        testCase(2, 22);
        testCase(6, 7);
        testCase(1, 2);
        testCase(5, 15);
        testCase(-9, 9);
        testCase(1, total_residues + 9);
        testCase(-15, total_residues + 15);

        testCase(0, 2);
        testCase(0, 1);
        testCase(1, 2);
        testCase(1, 3);
        testCase(-11, 15);
        testCase(10, total_residues + 9);
        testCase(-11, total_residues + 15);

        testCase(0, 0);						// 0 length
        testCase(1, 0);						// Negative length
        testCase(total_residues, 0);
        testCase(total_residues, -4);
        testCase(total_residues, total_residues);
        testCase(total_residues, total_residues + 1);
        testCase(total_residues + 1, total_residues);
        testCase(total_residues + 1, total_residues + 1);
        testCase(total_residues + 1, total_residues + 5);
        testCase(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public void testCase(int start, int end) throws Exception {
        sb = new StringBuffer();
        isr = GeneralUtils.getInputStream(infile, sb);
        try (ByteArrayOutputStream outstream = new ByteArrayOutputStream()) {
            final GenomeVersion genomeVersion = new GenomeVersion("Test");
            genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
            genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
            genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
            boolean result = NibbleResiduesParser.parse(isr, genomeVersion, start, end, outstream);

            if (start < end) {
                start = Math.max(0, start);
                start = Math.min(total_residues, start);

                end = Math.max(0, end);
                end = Math.min(total_residues, end);
            } else {
                start = 0;
                end = 0;
            }

            assertTrue(result);
            assertEquals(input_string.substring(start, end), outstream.toString());
            //System.out.println(input_string.substring(start, end) + "==" +outstream.toString());
        }
    }

}
