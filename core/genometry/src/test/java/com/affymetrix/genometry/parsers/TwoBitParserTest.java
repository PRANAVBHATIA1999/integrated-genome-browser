package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.util.GeneralUtils;
import org.lorainelab.igb.synonymlookup.services.impl.ChromosomeSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.GenomeVersionSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.SpeciesSynonymsLookupImpl;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author hiralv
 */
public class TwoBitParserTest {

    String noblocks = "ACTGGGTCTCAGTACTAGGAATTCCGTCATAGCTAAA";
    String noblocks_file = "data/2bit/noblocks.2bit";
    String nblocks = "NACNTCNNNNNNNNNNNNGTCTCANNNNNGTACTANNNNGGAATTCNNNNNCGTCATAGNNNCTAAANNN";
    String nblocks_file = "data/2bit/nblocks.2bit";
    String maskblocks = "acTGGgtctaAGTACTAGGAattccgtcatagcTAAa";
    String maskblocks_file = "data/2bit/maskblocks.2bit";
    String mnblocks = "aNcTNGGNgtcNtaNAGNTACNTAGNGANaNttcNcgNNNNNtcNNNatNNagNNcTANNAaNN";
    String mnblocks_file = "data/2bit/mnblocks.2bit";
    String residues, file;
    File infile = null;

    @Test
    public void testCaseFiles() throws Exception {
        residues = noblocks;
        file = noblocks_file;
        testOriginal();
        testCases();

        residues = nblocks;
        file = nblocks_file;
        testOriginal();
        testCases();

        residues = maskblocks;
        file = maskblocks_file;
        testOriginal();
        testCases();

        residues = mnblocks;
        file = mnblocks_file;
        testOriginal();
        testCases();
    }

    public void testOriginal() throws Exception {
        infile = new File(TwoBitParserTest.class.getClassLoader().getResource(file).getFile());
        final GenomeVersion genomeVersion = new GenomeVersion("No_Data");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        BioSeq seq = TwoBitParser.parse(infile.toURI(), genomeVersion).get(0);
        assertEquals(seq.getResidues(), residues);
    }

    public void testCases() throws Exception {
        testCase(0, residues.length());

        testCase(4, 18);
        testCase(6, 7);
        testCase(1, 4);
        testCase(1, 5);
        testCase(-1, 3);
        testCase(11, residues.length() + 4);
        testCase(-5, residues.length() + 5);

        testCase(2, 22);
        testCase(6, 7);
        testCase(1, 2);
        testCase(5, 15);
        testCase(-9, 9);
        testCase(1, residues.length() + 9);
        testCase(-15, residues.length() + 15);

        testCase(0, 2);
        testCase(0, 1);
        testCase(1, 2);
        testCase(1, 3);
        testCase(-11, 15);
        testCase(10, residues.length() + 9);
        testCase(-11, residues.length() + 15);

        testCase(0, 0);						// 0 length
        testCase(1, 0);						// Negative length
        testCase(residues.length(), 0);
        testCase(residues.length(), -4);
        testCase(residues.length(), residues.length());
        testCase(residues.length(), residues.length() + 1);
        testCase(residues.length() + 1, residues.length());
        testCase(residues.length() + 1, residues.length() + 1);
        testCase(residues.length() + 1, residues.length() + 5);
        testCase(Integer.MIN_VALUE, Integer.MAX_VALUE);

        Random generator = new Random(Calendar.getInstance().getTimeInMillis());
        for (int i = 0; i < 100; i++) {
            testCase(generator.nextInt(residues.length()), generator.nextInt(residues.length()));
        }
    }

    public void testCase(int start, int end) throws Exception {
        infile = new File(TwoBitParserTest.class.getClassLoader().getResource(file).getFile());
        testACase(infile.toURI(), start, end);
    }

    private void testACase(URI uri, int start, int end) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        boolean result = TwoBitParser.parse(uri, start, end, outStream);
        if (start < end) {
            start = Math.max(0, start);
            start = Math.min(residues.length(), start);
            end = Math.max(0, end);
            end = Math.min(residues.length(), end);
        } else {
            start = 0;
            end = 0;
        }
        assertTrue(result);
        assertEquals(residues.substring(start, end), outStream.toString());
        //System.out.println(residues.substring(start, end) + "==" +outStream.toString());
        GeneralUtils.safeClose(outStream);
    }
}
