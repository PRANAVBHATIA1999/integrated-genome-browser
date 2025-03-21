/*
 * WiggleParserTest.java
 * JUnit based test
 *
 * Created on October 18, 2006, 2:36 PM
 */
package com.affymetrix.genometry.parsers.graph;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.Scored;
import com.affymetrix.genometry.style.GraphState;
import com.affymetrix.genometry.symloader.Wiggle;
import com.affymetrix.genometry.symmetry.impl.GraphIntervalSym;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.google.code.externalsorting.ExternalMergeSort;
import org.lorainelab.igb.synonymlookup.services.impl.ChromosomeSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.GenomeVersionSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.SpeciesSynonymsLookupImpl;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Ed Erwin
 */
public class WiggleParserTest {

    @Test
    public void testParse() throws Exception {
        String filename = "data/wiggle/wiggleExample.wig";

        InputStream istr = this.getClass().getClassLoader().getResourceAsStream(filename);
        assertNotNull(istr);

        GenomeVersion genomeVersion = new GenomeVersion("test");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());

        WiggleParser parser = new WiggleParser();

        List<GraphSym> results = parser.parse(istr, genomeVersion, true, filename);

        assertEquals(3, results.size());

        GraphSym gr0 = results.get(0);

        BioSeq seq = gr0.getGraphSeq();

        // BED format
        assertTrue(gr0 instanceof GraphIntervalSym);
        assertEquals("chr19", gr0.getGraphSeq().getId());
        assertEquals(9, gr0.getPointCount());
//		assertEquals(59302000, gr0.getSpan(seq).getMin());
//		assertEquals(59304700, gr0.getSpan(seq).getMax());

        // variableStep format
        GraphSym gr1 = results.get(1);
        assertTrue(gr1 instanceof GraphIntervalSym);
        assertEquals(9, gr1.getChildCount());
        assertTrue(gr1.getChild(0) instanceof Scored);
//		assertEquals(59304701 - 1, gr1.getSpan(seq).getMin());	// variableStep: 1-relative format
//		assertEquals(59308021 - 1, gr1.getSpan(seq).getMax());	// variableStep: 1-relative foramt

        // fixedStep format
        GraphSym gr2 = results.get(2);
        assertTrue(gr2 instanceof GraphIntervalSym);
        assertEquals(10, gr2.getChildCount());
//		assertEquals(59307401 - 1, gr2.getSpan(seq).getMin());			// fixedStep: 1-relative format
//		assertEquals(59310301 - 1, gr2.getSpan(seq).getMax());			// fixedStep: 1-relative format
        assertEquals(300.0f, ((Scored) gr2.getChild(7)).getScore(), 0.00000001);

        assertEquals("Bed Format", gr0.getID());
        assertEquals("variableStep", gr1.getID());
        assertEquals("fixedStep", gr2.getID());

        GraphState state = gr1.getGraphState();
        assertEquals(0.0, state.getVisibleMinY(), 0.00001);
        assertEquals(25.0, state.getVisibleMaxY(), 0.00001);

        assertEquals(59310301 - 1, seq.getLength());	// fixedStep was 1-relative format.

    }

    //Test to see if one file is created.
    @Test
    public void testWiggle1() throws Exception {
        String filename = "data/wiggle/wiggleExample.wig";
        URL url = this.getClass().getClassLoader().getResource(filename);
        GenomeVersion genomeVersion = new GenomeVersion("test");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        Wiggle wiggle = new Wiggle(url.toURI(), Optional.empty(), filename, genomeVersion);
        wiggle.setExternalSortService(new ExternalMergeSort());
        BioSeq aseq = genomeVersion.addSeq("chr19", 59310300);

        List<GraphSym> results = wiggle.getGenome();

        assertEquals(3, results.size());

        GraphSym gr0 = results.get(0);

        BioSeq seq = gr0.getGraphSeq();

        // BED format
        assertTrue(gr0 instanceof GraphIntervalSym);
        assertEquals("chr19", gr0.getGraphSeq().getId());
        assertEquals(9, gr0.getPointCount());
//		assertEquals(59302000, gr0.getSpan(seq).getMin());
//		assertEquals(59304700, gr0.getSpan(seq).getMax());

        // variableStep format
        GraphSym gr1 = results.get(1);
        assertTrue(gr1 instanceof GraphIntervalSym);
        assertEquals(9, gr1.getChildCount());
        assertTrue(gr1.getChild(0) instanceof Scored);
//		assertEquals(59304701 - 1, gr1.getSpan(seq).getMin());	// variableStep: 1-relative format
//		assertEquals(59308021 - 1, gr1.getSpan(seq).getMax());	// variableStep: 1-relative foramt

        // fixedStep format
        GraphSym gr2 = results.get(2);
        assertTrue(gr2 instanceof GraphIntervalSym);
        assertEquals(10, gr2.getChildCount());
//		assertEquals(59307401 - 1, gr2.getSpan(seq).getMin());			// fixedStep: 1-relative format
//		assertEquals(59310301 - 1, gr2.getSpan(seq).getMax());			// fixedStep: 1-relative format
        assertEquals(300.0f, ((Scored) gr2.getChild(7)).getScore(), 0.00000001);

        String filelocation = url.toURI().toString();
        assertEquals(GenomeVersion.getUniqueGraphTrackID(filelocation, "Bed Format"), gr0.getID());
        assertEquals(GenomeVersion.getUniqueGraphTrackID(filelocation, "variableStep"), gr1.getID());
        assertEquals(GenomeVersion.getUniqueGraphTrackID(filelocation, "fixedStep"), gr2.getID());

        GraphState state = gr1.getGraphState();
        assertEquals(0.0, state.getVisibleMinY(), 0.00001);
        assertEquals(25.0, state.getVisibleMaxY(), 0.00001);

        assertEquals(59310301 - 1, seq.getLength());	// fixedStep was 1-relative format.

        results = wiggle.getChromosome(aseq);
        assertEquals(3, results.size());

        gr0 = results.get(0);

        assertTrue(gr0 instanceof GraphIntervalSym);
        assertEquals("chr19", gr0.getGraphSeq().getId());
        assertEquals(9, gr0.getPointCount());
//		assertEquals(59302000, gr0.getSpan(seq).getMin());
//		assertEquals(59304700, gr0.getSpan(seq).getMax());

        gr1 = results.get(1);
        assertTrue(gr1 instanceof GraphIntervalSym);
        assertEquals(9, gr1.getChildCount());
        assertTrue(gr1.getChild(0) instanceof Scored);
//		assertEquals(59304701 - 1, gr1.getSpan(seq).getMin());	// variableStep: 1-relative format
//		assertEquals(59308021 - 1, gr1.getSpan(seq).getMax());	// variableStep: 1-relative foramt

        // fixedStep format
        gr2 = results.get(2);
        assertTrue(gr2 instanceof GraphIntervalSym);
        assertEquals(10, gr2.getChildCount());
//		assertEquals(59307401 - 1, gr2.getSpan(seq).getMin());			// fixedStep: 1-relative format
//		assertEquals(59310301 - 1, gr2.getSpan(seq).getMax());			// fixedStep: 1-relative format
        assertEquals(300.0f, ((Scored) gr2.getChild(7)).getScore(), 0.00000001);

        assertEquals(GenomeVersion.getUniqueGraphTrackID(filelocation, "Bed Format"), gr0.getID());
        assertEquals(GenomeVersion.getUniqueGraphTrackID(filelocation, "variableStep"), gr1.getID());
        assertEquals(GenomeVersion.getUniqueGraphTrackID(filelocation, "fixedStep"), gr2.getID());

        List<BioSeq> allSeqs = wiggle.getChromosomeList();
        assertEquals(allSeqs.size(), 1);

        seq = allSeqs.get(0);
        assertEquals(seq.getId(), "chr19");
    }

    //Test to see if multiple files are created.
    @Test
    public void testWiggle2() throws Exception {
        String filename = "data/wiggle/wiggleExample2.wig";
        URL url = WiggleParserTest.class.getClassLoader().getResource(filename);
        GenomeVersion genomeVersion = new GenomeVersion("test");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        Wiggle wiggle = new Wiggle(url.toURI(), Optional.empty(), filename, genomeVersion);
        wiggle.setExternalSortService(new ExternalMergeSort());
        List<GraphSym> results = wiggle.getGenome();

        testResults2(url.getFile(), results, true);
    }

    public boolean testResults2(String filename, List<GraphSym> results, boolean checkId) {

        assertEquals(3, results.size());

        GraphSym gr0 = results.get(0);

        BioSeq seq = gr0.getGraphSeq();

        // BED format
        assertTrue(gr0 instanceof GraphIntervalSym);
        assertEquals("chr19", gr0.getGraphSeq().getId());
        assertEquals(9, gr0.getPointCount());
//		assertEquals(59302000, gr0.getSpan(seq).getMin());
//		assertEquals(59304700, gr0.getSpan(seq).getMax());

        // variableStep format
        GraphSym gr1 = results.get(1);
        seq = gr1.getGraphSeq();
        assertTrue(gr1 instanceof GraphIntervalSym);
        assertEquals("chr20", gr1.getGraphSeq().getId());
        assertEquals(9, gr1.getChildCount());
        assertTrue(gr1.getChild(0) instanceof Scored);
//		assertEquals(59304701 - 1, gr1.getSpan(seq).getMin());	// variableStep: 1-relative format
//		assertEquals(59308021 - 1, gr1.getSpan(seq).getMax());	// variableStep: 1-relative foramt

        // fixedStep format
        GraphSym gr2 = results.get(2);
        seq = gr2.getGraphSeq();
        assertTrue(gr2 instanceof GraphIntervalSym);
        assertEquals("chr21", gr2.getGraphSeq().getId());
        assertEquals(10, gr2.getChildCount());
//		assertEquals(59307401 - 1, gr2.getSpan(seq).getMin());			// fixedStep: 1-relative format
//		assertEquals(59310301 - 1, gr2.getSpan(seq).getMax());			// fixedStep: 1-relative format
        assertEquals(300.0f, ((Scored) gr2.getChild(7)).getScore(), 0.00000001);

        if (checkId) {
            String filelocation = new File(filename).toURI().toString();
            assertEquals(GenomeVersion.getUniqueGraphTrackID(filelocation, "Bed Format"), gr0.getID());
            assertEquals(GenomeVersion.getUniqueGraphTrackID(filelocation, "variableStep"), gr1.getID());
            assertEquals(GenomeVersion.getUniqueGraphTrackID(filelocation, "fixedStep"), gr2.getID());
        }

        return true;
    }

    public void testWriteBarFormat() throws Exception {
        String filename = "wiggleExample.wig";
        URL url = WiggleParserTest.class.getClassLoader().getResource(filename);
        GenomeVersion genomeVersion = new GenomeVersion("test");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        Wiggle wiggle = new Wiggle(url.toURI(), Optional.empty(), filename, genomeVersion);

        List<GraphSym> results = wiggle.getGenome();

        GraphSym gr0 = results.get(0);

        ByteArrayOutputStream outstream = new ByteArrayOutputStream();

        boolean written = Wiggle.writeBarFormat(results, genomeVersion.getName(), outstream);
        assertTrue(written);

        GenometryModel gmodel = GenometryModel.getInstance();

        InputStream istr = new ByteArrayInputStream(outstream.toByteArray());
        results = BarParser.parse(url.getFile(), istr, gmodel, genomeVersion, null, 0, Integer.MAX_VALUE, "chr19", true, false);

        GraphSym gr1 = results.get(0);
        assertEquals(gr0.getGraphSeq().getId(), gr1.getGraphSeq().getId());
        assertEquals(gr0.getGraphSeq().getMin(), gr1.getGraphSeq().getMin());
        assertEquals(gr0.getGraphSeq().getMax(), gr1.getGraphSeq().getMax());
    }

//	@Test
    public void testWriteAnnotation() throws Exception {
        String filename = "wiggleExample2.wig";
        URL url = WiggleParserTest.class.getClassLoader().getResource(filename);
        GenomeVersion genomeVersion = new GenomeVersion("test");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        Wiggle wiggle = new Wiggle(url.toURI(), Optional.empty(), filename, genomeVersion);

        List<GraphSym> results = wiggle.getGenome();

        testResults2(url.getFile(), results, true);

        ByteArrayOutputStream outstream = new ByteArrayOutputStream();

        wiggle.writeAnnotations(results, null, null, outstream);

        File outfile = createFileFromString(outstream.toString());
        Wiggle outwiggle = new Wiggle(outfile.toURI(), Optional.empty(), outfile.getName(), genomeVersion);
        List<GraphSym> outresults = outwiggle.getGenome();

        testResults2(url.getFile(), outresults, false);
        File testFile = new File(WiggleParserTest.class.getClassLoader().getResource("testFile.wig").getFile());

        String strFileContent;
        try (FileInputStream fin = new FileInputStream(testFile)) {
            byte fileContent[] = new byte[(int) testFile.length()];
            fin.read(fileContent);
            strFileContent = new String(fileContent);
        }

        assertEquals(strFileContent, outstream.toString());
    }

    public File createFileFromString(String string) throws Exception {
        File tempFile = File.createTempFile("tempFile", ".wig");
        tempFile.deleteOnExit();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile, true))) {
            bw.write(string);
        }
        return tempFile;
    }
    /*
     * @Test
     * public void testWriteGraphs() {
     * fail("test not implemented");
     * }
     */
}
