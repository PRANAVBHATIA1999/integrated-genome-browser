package com.lorainelab.bam;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.symloader.SymLoader;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.google.code.externalsorting.ExternalMergeSort;
import com.lorainelab.synonymlookup.services.impl.ChromosomeSynonymLookupImpl;
import com.lorainelab.synonymlookup.services.impl.GenomeVersionSynonymLookupImpl;
import com.lorainelab.synonymlookup.services.impl.SpeciesSynonymsLookupImpl;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author hiralv
 */
public class SAMParserTest {

    @Test
    public void testParseFromFile() throws Exception {

        String filename = "data/bam/combined_mapping_q.sorted.sam";
        filename = SAMParserTest.class.getClassLoader().getResource(filename).getFile();
        assertTrue(new File(filename).exists());

        InputStream istr = new FileInputStream(filename);
        DataInputStream dis = new DataInputStream(istr);
        assertNotNull(dis);

        GenomeVersion group = new GenomeVersion("M_musculus_Mar_2006");
        group.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        group.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        group.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        BioSeq seq = group.addSeq("chr1", 197069962);

        SymLoader symL = new SAM(new File(filename).toURI(), "featureName", group);
        symL.setExternalSortService(new ExternalMergeSort());
        assertNotNull(symL);

        List<? extends SeqSymmetry> result = symL.getChromosome(seq);
        assertNotNull(result);
        assertEquals(190, result.size());	// 190 alignments in sample file

        SeqSymmetry sym = result.get(0);	// first (positive) strand
        assertEquals("0", sym.getID());
        assertEquals(51119999, sym.getSpan(seq).getMin());
        assertEquals(51120035, sym.getSpan(seq).getMax());
        assertTrue(sym.getSpan(seq).isForward());
        Object residues = ((SymWithProps) sym).getProperty("residues");
        assertNotNull(residues);
        assertEquals("CACTGCTTAAAAATCCTCTTTAAAGAGCAAGCAAAT", residues.toString());

        sym = result.get(58);	// first negative strand
        assertEquals("0", sym.getID());
        assertEquals(51120691, sym.getSpan(seq).getMin());
        assertEquals(51120727, sym.getSpan(seq).getMax());
        assertFalse(sym.getSpan(seq).isForward());
        residues = ((SymWithProps) sym).getProperty("residues");
        assertNotNull(residues);
        assertEquals("ACATTCATCTAATTCATGAATGGCAAAGACACGTCC", residues.toString());

        result = symL.getRegion(new SimpleSeqSpan(51120000, 51120038, seq));
        assertEquals(3, result.size());

        result = symL.getRegion(new SimpleSeqSpan(51120000, 51120039, seq));
        assertEquals(3, result.size());
        sym = result.get(0);	// first (positive) strand
        assertEquals("0", sym.getID());
        assertEquals(51119999, sym.getSpan(seq).getMin());
        assertEquals(51120035, sym.getSpan(seq).getMax());

        for (SeqSymmetry symOut : result) {
            System.out.println(symOut.getID() + " " + symOut.getSpan(seq).getStart() + " " + symOut.getSpan(seq).getEnd());
        }
    }
}
