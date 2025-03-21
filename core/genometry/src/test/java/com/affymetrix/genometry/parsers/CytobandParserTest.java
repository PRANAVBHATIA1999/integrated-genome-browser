/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.parsers.CytobandParser.Arm;
import com.affymetrix.genometry.parsers.CytobandParser.CytobandSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import org.lorainelab.igb.synonymlookup.services.impl.ChromosomeSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.GenomeVersionSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.SpeciesSynonymsLookupImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author auser
 */
public class CytobandParserTest {

    /**
     * Test of parse method, of class CytobandParser.
     */
    @Test
    public void testParse() throws Exception {
        String filename = "data/cyt/test1.cyt";
        InputStream istr = CytobandParserTest.class.getClassLoader().getResourceAsStream(filename);
        assertNotNull(istr);

        GenomeVersion genomeVersion = new GenomeVersion("Test Group");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        boolean annot_seq = true;

        CytobandParser instance = new CytobandParser();
        List<SeqSymmetry> result = instance.parse(istr, genomeVersion, annot_seq);
        assertEquals(7, result.size());
        CytobandSym sym = (CytobandSym) result.get(2);
        assertEquals("gpos25", sym.getBand());
        assertEquals(4300000, sym.getLength());
        assertEquals(39600000, sym.getMin());
        assertEquals(43900000, sym.getMax());
        assertEquals(0, sym.getChildCount());
        assertEquals(43900000, sym.getEnd());
        assertEquals(Arm.SHORT, sym.getArm());

    }

    /**
     * Test of writeAnnotations method, of class CytobandParser.
     */
    @Test
    public void testWriteAnnotations() throws IOException {

        String string = "chr1\t39600000\t43900000\tp34.2\tgpos25\n"
                + "chr1\t43900000\t46500000\tp34.1\tgneg\n"
                + "chr1\t56200000\t58700000\tp32.2\tgpos50\n";

        InputStream istr = new ByteArrayInputStream(string.getBytes());
        GenomeVersion genomeVersion = new GenomeVersion("Test Group");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        boolean annot_seq = true;

        CytobandParser instance = new CytobandParser();

        Collection<SeqSymmetry> syms = new ArrayList<>();

        syms = instance.parse(istr, genomeVersion, annot_seq);

        BioSeq seq = genomeVersion.getSeq("chr1");
        String type = "test_type";
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();

        boolean result = instance.writeAnnotations(syms, seq, type, outstream);
        assertEquals(true, result);
        assertEquals(string, outstream.toString());

    }

    /**
     * Test of writeCytobandFormat method, of class CytobandParser.
     */
    @Test
    public void testWriteCytobandFormat() throws Exception {
        Writer out = new StringWriter();
        GenomeVersion genomeVersion = new GenomeVersion("Test Group");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        String filename = "data/cyt/test1.cyt";
        filename = CytobandParserTest.class.getClassLoader().getResource(filename).getFile();
        InputStream istr = new FileInputStream(filename);
        assertNotNull(istr);
        GenomeVersion genomeVersion2 = new GenomeVersion("Test Group");
        genomeVersion2.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion2.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion2.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        boolean annot_seq = true;
        CytobandParser instance = new CytobandParser();
        List<SeqSymmetry> result = instance.parse(istr, genomeVersion2, annot_seq);
        BioSeq aseq = genomeVersion2.getSeq(0);
        CytobandSym sym = (CytobandSym) result.get(2);
        CytobandParser.writeCytobandFormat(out, sym, aseq);
        assertEquals("chr1\t39600000\t43900000\tp34.2\tgpos25\n", out.toString());

    }

    /**
     * Test of getMimeType method, of class CytobandParser.
     */
    @Test
    public void testGetMimeType() {

        CytobandParser instance = new CytobandParser();
        assertEquals("txt/plain", instance.getMimeType());
    }
}
