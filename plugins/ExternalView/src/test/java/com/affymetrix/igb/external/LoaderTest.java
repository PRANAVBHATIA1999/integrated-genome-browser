package com.affymetrix.igb.external;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.assertNotNull;
import org.junit.Ignore;
import org.junit.Test;

public class LoaderTest {


    @Ignore //ignored for now, but will re-enable as an integration test
    @Test
    public void checkDownLoadEnsembl() throws ImageUnavailableException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put(EnsemblView.ENSEMBLSESSION, "");
        cookies.put(EnsemblView.ENSEMBLWIDTH, "800");
        Loc loc = new Loc("hg38", "chr4", 113775472, 113777472);//chr4:113,435,486-113,777,472
        BufferedImage image = new ENSEMBLoader().getImage(loc, 800, cookies);
        assertNotNull(image);
    }

    @Ignore //ignored for now, but will re-enable as an integration test
    @Test
    public void checkDownLoadUCSC() throws ImageUnavailableException {
        Map<String, String> cookies = new HashMap<>();
        cookies.put(UCSCView.UCSCUSERID, "");
        Loc loc = new Loc("hg19", "chr1", 6203693, 6206373);
        BufferedImage image = new UCSCLoader().getImage(loc, 800, cookies);
        assertNotNull(image);
    }

}
