package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.symmetry.impl.UcscPslSym;
import org.lorainelab.igb.synonymlookup.services.impl.ChromosomeSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.GenomeVersionSynonymLookupImpl;
import org.lorainelab.igb.synonymlookup.services.impl.SpeciesSynonymsLookupImpl;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author jnicol
 */
public class BpsParserTest {

    /**
     * Verify that converting to a Bps file always works the same. (This doesn't
     * mean it's correct, just that its behavior hasn't changed.)
     */
    @Test
    public void testConvertToBps() {
        InputStream istr = null;
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        try {
            String filename = "data/psl/test1.psl";
            filename = BpsParserTest.class.getClassLoader().getResource(filename).getFile();
            istr = new FileInputStream(filename);
            assertNotNull(istr);
            GenomeVersion genomeVersion = new GenomeVersion("Test Group");
            genomeVersion.setChrSynLookup(new ChromosomeSynonymLookupImpl());
            genomeVersion.setGenomeVersionSynonymLookup(new GenomeVersionSynonymLookupImpl());
            genomeVersion.setSpeciesSynLookup(new SpeciesSynonymsLookupImpl());
            boolean annot_seq = true;
            String stream_name = "test_file";

            PSLParser parser = new PSLParser();
            List<UcscPslSym> syms = parser.parse(istr, stream_name, genomeVersion, genomeVersion, annot_seq, true);

            BpsParser instance2 = new BpsParser();
            boolean writeResult = instance2.writeAnnotations(syms, null, "", outstream);
            assertEquals(true, writeResult);

        } catch (Exception ex) {
            Logger.getLogger(BpsParserTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                istr.close();
            } catch (IOException ex) {
                Logger.getLogger(BpsParserTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            String filename = "data/bps/test1.bps";
            filename = BpsParserTest.class.getClassLoader().getResource(filename).getFile();
            istr = new FileInputStream(filename);
            assertNotNull(istr);

            BufferedInputStream bis = new BufferedInputStream(istr);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result = bis.read();
            while (result != -1) {
                byte b = (byte) result;
                buf.write(b);
                result = bis.read();
            }

            assertEquals(outstream.toString(), buf.toString());

        } catch (Exception ex) {
            Logger.getLogger(BpsParserTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                istr.close();
            } catch (IOException ex) {
                Logger.getLogger(BpsParserTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
