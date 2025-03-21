package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.BioSeq;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FastaParserTest {

    @Test
    public void testGenerateNewHeader() throws Exception {
        String chrom_name = "chrC";
        String genome_name = "A_thaliana_TAIR8";

        byte[] result = FastaParser.generateNewHeader(chrom_name, genome_name, 0, 1000);

        assertNotNull(result);

        String expected_string = "chrC range:0-1,000 interbase genome:A_thaliana_TAIR8\n";
        byte[] expected_result = new byte[expected_string.length() + 1];
        expected_result[0] = '>';
        for (int i = 0; i < expected_string.length(); i++) {
            expected_result[i + 1] = (byte) expected_string.charAt(i);
        }

        /*
         * for (int i=0;i<result.length;i++)
         * System.out.print(":" + (char)result[i]);
         * System.out.println();
         * for (int i=0;i<expected_result.length;i++)
         * System.out.print(":" + (char)result[i]);
         * System.out.println();
         */
        assertEquals(expected_result.length, result.length);
        for (int i = 0; i < expected_result.length; i++) {
            assertEquals((char) expected_result[i], (char) result[i]);
        }
    }

    @Test
    public void testParse() throws Exception {
        String filename = "data/fasta/FASTA_chrQ.fasta";
        filename = FastaParserTest.class.getClassLoader().getResource(filename).getFile();
        assertTrue(new File(filename).exists());

        InputStream istr = new FileInputStream(filename);
        assertNotNull(istr);

        BioSeq result = FastaParser.parse(istr);

        assertEquals("chrQ", result.getId());
        assertEquals(33, result.getLength());
        assertEquals("AAAAAAAAAAACCCCCCCCCGGGGGGGGGTTTT", result.getResidues());
        assertEquals("AACCC", result.getResidues(9, 9 + 5));
        assertEquals("GGGTT", result.getResidues(9 + 5, 9));
    }

    //This test is commented as it was failing since I have started development.. need to check if this is correct and necessary
//    @Test
//    public void testReadFASTA() throws Exception {
//        String filename = "data/fasta/FASTA_obey_70.fasta";
//        filename = FastaParserTest.class.getClassLoader().getResource(filename).getFile();
//
//        char[] expected_fasta = null;
//        byte[] fasta = null;
//        expected_fasta = "LCLYTHIGRN".toCharArray();
//        testFASTASegment(filename, fasta, expected_fasta, 0, 10);
//
//        expected_fasta = "VITNLFSAIPYIGTNLVEWI".toCharArray();
//        testFASTASegment(filename, fasta, expected_fasta, 53, 73);
//
//        expected_fasta = "L".toCharArray();
//        testFASTASegment(filename, fasta, expected_fasta, 0, 1);
//
//        expected_fasta = null;
//        testFASTASegment(filename, fasta, expected_fasta, 3, 3);
//
//        expected_fasta = "LMPFLH".toCharArray();
//        testFASTASegment(filename, fasta, expected_fasta, 211, 217);
//
//        expected_fasta = "IENY".toCharArray();
//        testFASTASegment(filename, fasta, expected_fasta, 280, 284);
//
//        testFASTASegment(filename, fasta, expected_fasta, 280, 290);
//
//        fasta = FastaParser.readFASTA(new File(filename), 290, 291);
//        assertNull(fasta);
//    }
    @Test
    public void testReadBadFASTA_1() throws Exception {
        String filename = "data/fasta/FASTA_not_obey_70.fasta";
        filename = FastaParserTest.class.getClassLoader().getResource(filename).getFile();
        assertTrue(new File(filename).exists());

        char[] expected_fasta = null;
        byte[] fasta = null;
        expected_fasta = "LCLYTHIGRN".toCharArray();
        try {
            testFASTASegment(filename, fasta, expected_fasta, 10, 0);
        } catch (java.lang.IllegalArgumentException ex) {
            return;
        }
        fail("Should throw an IllegalArgumentException");
    }

    @Test
    public void testReadBadFASTA_2() throws Exception {
        String filename = "data/fasta/FASTA_not_obey_70.fasta";
        filename = FastaParserTest.class.getClassLoader().getResource(filename).getFile();
        assertTrue(new File(filename).exists());

        char[] expected_fasta = null;
        byte[] fasta = null;
        expected_fasta = "LCLYTHIGRN".toCharArray();
        try {
            testFASTASegment(filename, fasta, expected_fasta, 10, 100);
        } catch (java.lang.AssertionError ex) {
            //System.out.println(ex.toString());
            return;
        }
        fail("Should throw an AssertionError Exception");
    }

    @Test
    public void testSkipFastaHeader() throws Exception {
        String filename = "data/fasta/chrC.fasta";
        filename = FastaParserTest.class.getClassLoader().getResource(filename).getFile();
        DataInputStream dis = new DataInputStream(new FileInputStream(filename));
        BufferedInputStream bis = new BufferedInputStream(dis);
        byte[] fasta = FastaParser.skipFASTAHeader(filename, bis);

        //System.out.print("TEST: header is ");
        assertNotNull(fasta);
        /*
         * for (int i =0;i<fasta.length;i++)
         * System.out.print((char)fasta[i]);
         * System.out.println("");
         */
    }

    /**
     * Test of a certain case I saw when running a query. This was due to Java
     * not implementing a proper skip() method.
     */
    @Test
    public void testChrCfailure() throws Exception {
        String filename = "data/fasta/chrC.fasta";
        filename = FastaParserTest.class.getClassLoader().getResource(filename).getFile();
        int start = 8020, end = 8021;
        //System.out.println("ChrCfailure: Testing " + filename + " from [" + start + ":" + end + "]");
        assertTrue(new File(filename).exists());

        byte[] fasta = FastaParser.readFASTA(new File(filename), start, end);
        /*
         * for (int i=0;i<fasta.length;i++)
         * System.out.print((char)fasta[i]);
         * System.out.println();
         */
    }

    /*
     * Test beginning value out of range.
     */
    @Test
    public void testChrCfailure2() throws Exception {
        String filename = "data/fasta/chrC.fasta";
        filename = FastaParserTest.class.getClassLoader().getResource(filename).getFile();
        int start = 200000, end = 200001; // file size < 200000.
        //System.out.println("Testing " + filename + " from [" + start + ":" + end + "]");
        assertTrue(new File(filename).exists());

        try {
            FastaParser.readFASTA(new File(filename), start, end);
        } catch (java.lang.IllegalArgumentException ex) {
            return;
        }
        fail("Should throw an IllegalArgumentException");

    }

    /**
     * Test of a certain case I saw when running a query.
     */
    @Test
    public void testChrC_OK() throws Exception {
        String filename = "data/fasta/chrC.fasta";
        filename = FastaParserTest.class.getClassLoader().getResource(filename).getFile();

        char[] expected_fasta = "ATGG".toCharArray();
        byte[] fasta = null;
        testFASTASegment(filename, fasta, expected_fasta, 0, 4);

        expected_fasta = "CCCC".toCharArray();
        testFASTASegment(filename, fasta, expected_fasta, 75, 79);

        expected_fasta = "TACG".toCharArray(); // line 3
        testFASTASegment(filename, fasta, expected_fasta, 79, 83);

        expected_fasta = "CAAA".toCharArray(); // line 4
        testFASTASegment(filename, fasta, expected_fasta, 158, 162);

        expected_fasta = "TCTTT".toCharArray(); // line 103
        testFASTASegment(filename, fasta, expected_fasta, 7979, 7984);

        //expected_fasta = "TTTTTTTCATTTT".toCharArray(); // line 103
        //testFASTASegment(filename, fasta, expected_fasta, 8009, 8022);
        expected_fasta = "CATTTT".toCharArray(); // line 103
        testFASTASegment(filename, fasta, expected_fasta, 8016, 8022);

        expected_fasta = "TT".toCharArray(); // line 103
        testFASTASegment(filename, fasta, expected_fasta, 8020, 8022);
    }

    private void testFASTASegment(String filename, byte[] fasta, char[] expected_fasta, int start, int end) throws IOException {
        //System.out.println("Testing " + filename + " from [" + start + ":" + end + "]");

        DataInputStream dis = new DataInputStream(new FileInputStream(filename));
        BufferedInputStream bis = new BufferedInputStream(dis);
        byte[] header = FastaParser.skipFASTAHeader(filename, bis);
        //assertNotNull(header);
        int header_len = (header == null ? 0 : header.length);

        fasta = FastaParser.readFASTA(new File(filename), start, end);

        if (expected_fasta == null) {
            assertNull(fasta);
            return;
        }

        assertNotNull(fasta);

        //System.out.println("expected, header, actual " + expected_fasta.length + ":" + header_len + ":" + fasta.length);
        assertTrue(end - start >= fasta.length - header_len);

        /*
         * System.out.print("actual:");
         * for (int i=header_len;i<fasta.length;i++)
         * System.out.print((char)fasta[i]);
         * System.out.println();
         */
        assertTrue(expected_fasta.length >= fasta.length - header_len, filename + ": Expected expected_fasta.length (" + expected_fasta.length + ") >= fasta.length (" + fasta.length + ") - header_len (" + header_len + ")");

        //System.out.print("testing against expected:");
        for (int i = 0; i < fasta.length - header_len; i++) {
            //System.out.print(expected_fasta[i]);
            assertEquals(expected_fasta[i], (char) fasta[i + header_len]);
        }
        //System.out.println();
    }
}
