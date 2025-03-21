package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.symmetry.impl.SingletonSymWithProps;
import com.affymetrix.genometry.symmetry.impl.UcscGffSym;
import org.lorainelab.igb.synonymlookup.services.impl.ChromosomeSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.GenomeVersionSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.SpeciesSynonymsLookupImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GFFParserTest {

    @Test
    public void testParse() throws Exception {
        //System.out.println("parse");

        String filename = "data/gff1/GFF1_example.gff";
        filename = GFFParserTest.class.getClassLoader().getResource(filename).getFile();
        assertTrue(new File(filename).exists());
        InputStream istr = new FileInputStream(filename);
        assertNotNull(istr);

        GenomeVersion genomeVersion = new GenomeVersion("test");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        GFFParser instance = new GFFParser();

        List expResult = null;
        List result = instance.parse(istr, genomeVersion, true);

        //for (int i=0; i<result.size(); i++) {
        //  SeqUtils.printSymmetry((SeqSymmetry) result.get(i), "|  ", true);
        //}
        assertEquals(22, result.size());

        SingletonSymWithProps sym;
        sym = (SingletonSymWithProps) result.get(0);
        assertEquals(99, sym.getStart());
        assertEquals(200, sym.getEnd());
        assertEquals(101, sym.getLength());
        assertEquals(true, sym.isForward());
        //assertEquals("Testing", sym.getProperty("source"));
        assertEquals("Track A", sym.getProperty("method"));

        if (sym.getChildCount() == 1) {
            // Currently the parser makes groups out of everything, even when there
            // is only one child per group.  In the future, it is possible that
            // the single symmetry will be used without a parent.
            sym = (SingletonSymWithProps) sym.getChild(0);
            assertEquals("exon", sym.getProperty("type"));
            assertEquals(Float.valueOf("200.0"), sym.getProperty("score"));
        }

        sym = (SingletonSymWithProps) result.get(3);
        assertEquals(2, sym.getChildCount());
        assertEquals("Track A", sym.getProperty("method"));

        sym = (SingletonSymWithProps) result.get(4);
        assertEquals(3, sym.getChildCount());
        assertEquals("Track A", sym.getProperty("method"));
        assertEquals(800, sym.getSpan(0).getStart());
        assertEquals(419, sym.getSpan(0).getEnd());

        sym = (SingletonSymWithProps) result.get(5);
        assertEquals(1, sym.getChildCount());
        assertEquals("Scored Track", sym.getProperty("method"));

        sym = (SingletonSymWithProps) result.get(16);
        assertEquals(4, sym.getChildCount());
        assertEquals("Scored Track", sym.getProperty("method"));
        assertEquals(99, sym.getSpan(0).getStart());
        assertEquals(1200, sym.getSpan(0).getEnd());

        sym = (UcscGffSym) sym.getChild(2);
        assertEquals(600.0f, sym.getProperty("score"));

        sym = (SingletonSymWithProps) result.get(17);
        assertEquals(6, sym.getChildCount());
        assertEquals("Scored Track", sym.getProperty("method"));
        assertEquals(99, sym.getSpan(0).getStart());
        assertEquals(1200, sym.getSpan(0).getEnd());

        sym = (UcscGffSym) sym.getChild(2);
        assertEquals(200.0f, sym.getProperty("score"));

        sym = (SingletonSymWithProps) result.get(18);
        assertEquals(1, sym.getChildCount());
        assertEquals("Track B", sym.getProperty("method"));
        //assertEquals("sourceB", sym.getProperty("source"));

    }

    /**
     * Test that the regular expression designed to tell the difference between GFF1 and GFF2 from the "group" or
     * "attributes" field actually works
     */
    @Test
    public void testGFF1RegularExpression() {

        assertTrue(UcscGffSym.gff1_regex.matcher("foo").matches());
        assertTrue(UcscGffSym.gff1_regex.matcher("foo ").matches());
        assertTrue(UcscGffSym.gff1_regex.matcher("foo # this is a comment ").matches());
        assertTrue(UcscGffSym.gff1_regex.matcher("foo #").matches());
        assertTrue(UcscGffSym.gff1_regex.matcher("foo# comment").matches());
        assertTrue(UcscGffSym.gff1_regex.matcher("foo ").matches());

        // do not allow spaces before the group id
        assertFalse(UcscGffSym.gff1_regex.matcher("  foo").matches());

        assertFalse(UcscGffSym.gff1_regex.matcher("foo bar").matches());
        assertFalse(UcscGffSym.gff1_regex.matcher("group_id \"foo\" ; transcript_id \"bar\"").matches());
    }

    /**
     * Test of processDirective method, of class com.affymetrix.igb.parsers.GFF3Parser.
     */
    @Test
    public void testProcessDirective() throws Exception {
        //System.out.println("processDirective");

        GFFParser instance = new GFFParser();

        instance.processDirective("##gff-version 1");
        assertEquals(1, instance.gff_version);

        instance.processDirective("##gff-version 2");
        assertEquals(2, instance.gff_version);

        instance.processDirective("##gff-version 3");
        assertEquals(3, instance.gff_version);
    }

}
