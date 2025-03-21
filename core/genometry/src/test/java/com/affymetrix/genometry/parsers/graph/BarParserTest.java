package com.affymetrix.genometry.parsers.graph;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.parsers.graph.BarParser.BarFileHeader;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import org.lorainelab.igb.synonymlookup.services.impl.ChromosomeSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.GenomeVersionSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.SpeciesSynonymsLookupImpl;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author auser
 */
public class BarParserTest {

    @Test
    public void TestParseFromFile() throws IOException {

        String filename = "data/bar/small.bar";

        InputStream istr = this.getClass().getClassLoader().getResourceAsStream(filename);
        assertNotNull(istr);

        GenometryModel gmodel = GenometryModel.getInstance();
        GenomeVersion genomeVersion = new GenomeVersion("Test Group");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        String stream_name = "chr15_random";

        boolean ensure_unique_id = true;

        List<GraphSym> results = BarParser.parse(filename, istr, gmodel, genomeVersion, null, 0, Integer.MAX_VALUE, stream_name, ensure_unique_id, false);
        assertEquals(1, results.size());
        GraphSym gr0 = results.get(0);
        assertEquals(stream_name, gr0.getGraphSeq().getId());
        assertEquals(38, gr0.getPointCount());
        assertEquals(0, gr0.getGraphYCoord(2), 0);
        assertEquals(0, gr0.getGraphYCoord(3), 0.01);
        assertEquals(1879565, gr0.getGraphXCoord(3));

    }

    public void TestWriteAnnotations() throws IOException {
        String string
                = "chr15_random	1880113	0.23001233\n"
                + "chr15_random	1880219	0.21503295\n";

        InputStream istr = new ByteArrayInputStream(string.getBytes());

        GenomeVersion genomeVersion = new GenomeVersion("Test Group");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        String stream_name = "test_file";
        boolean ensure_unique_id = true;

        List<GraphSym> results = SgrParser.parse(istr, stream_name, genomeVersion, ensure_unique_id);

        GraphSym gr0 = results.get(0);
        BioSeq seq = gr0.getGraphSeq();
        Collection<SeqSymmetry> syms = new ArrayList<>();
        syms.add(gr0);
        String type = "test_type";
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        BarParser instance = new BarParser();
        boolean result1 = instance.writeAnnotations(syms, seq, type, outstream);
        assertEquals(true, result1);
        assertEquals(string, outstream.toString());
    }

    @Test
    public void TestParseBarHeader() throws Exception {
        int[] a = new int[]{2, 1};
        String filename = "data/bar/small.bar";
        InputStream istr = this.getClass().getClassLoader().getResourceAsStream(filename);
        BufferedInputStream bis = new BufferedInputStream(istr);
        DataInputStream dis = new DataInputStream(bis);
        BarFileHeader h = BarParser.parseBarHeader(dis);
        float exp_version = 2.0f;
        float version = h.version;
        assertEquals(exp_version, version, 0.001);
        assertEquals(1, h.seq_count);

        int[] val_types = h.val_types;
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], val_types[i]);
        }
        assertEquals(2, h.vals_per_point);

    }

    @Test
    public void TestGetSlice() throws Exception {
        String filename = "data/bar/small.bar";
        URL url = BarParserTest.class.getClassLoader().getResource(filename);
        GenomeVersion genomeVersion = new GenomeVersion("test_group");
        genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
        genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
        genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
        BioSeq aseq = genomeVersion.addSeq("chr15_random", 1881177);
        GraphSym gr0 = BarParser.getRegion(url.getFile(), new SimpleSeqSpan(1880135, 1880205, aseq));
        assertEquals("chr15_random", gr0.getGraphSeq().getId());
        assertEquals(2, gr0.getPointCount());
        assertEquals(0.2127714902162552, gr0.getGraphYCoord(0), 0.01);
        assertEquals(0.23889116942882538, gr0.getGraphYCoord(1), 0.01);
        assertEquals(1880186, gr0.getGraphXCoord(1));
        assertEquals(1880149, gr0.getGraphXCoord(0));
        /**
         *
         * FileOutputStream fout; File file=new File("slice.bar"); fout = new FileOutputStream(file);
         * BufferedOutputStream bos = new BufferedOutputStream(fout); DataOutputStream dos = new DataOutputStream(bos);
         * BioSeq seq = (BioSeq) gr0.getGraphSeq(); Collection<SeqSymmetry> syms = new ArrayList(); syms.add(gr0);
         * String type = "test_type"; BarParser instance=new BarParser(); instance.writeAnnotations(syms,seq,type,dos);
         * dos.close();
         *
         *
         */

    }

}
