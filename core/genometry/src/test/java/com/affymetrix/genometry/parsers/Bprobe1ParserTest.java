/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.symmetry.impl.EfficientProbesetSymA;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import org.lorainelab.igb.synonymlookup.services.impl.ChromosomeSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.GenomeVersionSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.SpeciesSynonymsLookupImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author auser
 */
//Bprobe1Parser parses and writes files in bp2 format
public class Bprobe1ParserTest {

    @Test
    //Tests the parse method
    public void testParseFromFile() throws Exception {

        String filename = "data/bp1/test.bp2";
        filename = Bprobe1ParserTest.class.getClassLoader().getResource(filename).getFile();
        assertTrue(new File(filename).exists());

        List<SeqSymmetry> result;
        try (InputStream istr = new FileInputStream(filename)) {
            assertNotNull(istr);
            GenomeVersion genomeVersion = new GenomeVersion("rn4");
            genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
            genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
            genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
            boolean annot_seq = true;
            String default_type = "test_type";
            boolean populate_id_hash = true;
            Bprobe1Parser parser = new Bprobe1Parser();
            result = parser.parse(istr, genomeVersion, annot_seq, default_type, populate_id_hash);
        }
        assertEquals(5, result.size());

        EfficientProbesetSymA sym1, sym2, sym3, sym4, sym5;

        sym1 = (EfficientProbesetSymA) result.get(0);
        assertEquals(25, sym1.getLength());
        assertEquals(187000341, sym1.getMin());
        assertEquals(187000366, sym1.getMax());
        assertEquals("RaGene-1_0-st:118032", sym1.getID());
        assertEquals(118032, sym1.getIntID());
        assertEquals(25, sym1.getProbeLength());
        assertEquals(1, sym1.getSpanCount());

        sym2 = (EfficientProbesetSymA) result.get(1);
        assertEquals(25, sym2.getLength());
        assertEquals(187000343, sym2.getMin());
        assertEquals(187000368, sym2.getMax());
        assertEquals("RaGene-1_0-st:874235", sym2.getID());

        sym3 = (EfficientProbesetSymA) result.get(2);
        assertEquals(25, sym3.getLength());
        assertEquals(187000372, sym3.getMin());
        assertEquals(187000397, sym3.getMax());
        assertEquals("RaGene-1_0-st:672767", sym3.getID());

        sym4 = (EfficientProbesetSymA) result.get(3);
        assertEquals(25, sym4.getLength());
        assertEquals(187000441, sym4.getMin());
        assertEquals(187000466, sym4.getMax());
        assertEquals("RaGene-1_0-st:964937", sym4.getID());

        sym5 = (EfficientProbesetSymA) result.get(4);
        assertEquals(25, sym5.getLength());
        assertEquals(187000456, sym5.getMin());
        assertEquals(187000481, sym5.getMax());
        assertEquals("RaGene-1_0-st:903927", sym5.getID());
        assertEquals(903927, sym5.getIntID());
        assertEquals(25, sym5.getProbeLength());
        assertEquals(1, sym5.getSpanCount());

    }
}
