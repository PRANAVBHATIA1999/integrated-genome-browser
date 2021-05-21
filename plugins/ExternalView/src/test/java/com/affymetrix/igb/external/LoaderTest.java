package com.affymetrix.igb.external;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.junit.Ignore;
import org.junit.Test;

public class LoaderTest {

    @Ignore
    @Test
    /**
     * This test fails because we are unable to get the image back from the
     * Ensembl site. Their API appears to have changed since this code was
     * written.
     */
    public void checkDownLoadEnsembl() throws ImageUnavailableException {
        String message = "ENSEMBLoader should be able to retrieve an image from Ensembl genome browser Web site.";
        Map<String, String> cookies = new HashMap<>();
        cookies.put(EnsemblView.ENSEMBLSESSION, "");
        cookies.put(EnsemblView.ENSEMBLWIDTH, "800");
        Loc loc = new Loc("hg38", "chr4", 113775472, 113777472);//chr4:113,435,486-113,777,472
        BufferedImage image = new ENSEMBLoader().getImage(loc, 800, cookies);
        assertNotNull(message,image);
    }
 
    @Test
    public void checkLoadMap() throws IOException {
        String message = "ENSEMBLoader should map UCSC to Ensembl genome versions.";
        Map<String, EnsemblURL> map = ENSEMBLoader.loadMap();
        assertNotNull(message,map);
    }
 
    @Test
    public void checkUrlStringFormation() {
        // Note that this URL appears to now be incorrect. The Ensembl genome browser interface has changed since this
        // code was first written. Looks like much of the functionality and appearance are computing now
        // in the Web browser and it doesn't makes sense to retrieve a PNG image anymore?
        String message = "ENSEMBLoader should build a URL for retrieving an image from Ensembl genome browser Web site.";
        String right_answer = "http://useast.ensembl.org/Homo_sapiens//Component/Location/Web/ViewBottom?r=4:113775473-113777472;image_width=500;export=png";
        Loc loc = new Loc("hg38", "chr4", 113775472, 113777472);//chr4:113,435,486-113,777,472
        ENSEMBLoader loader = new ENSEMBLoader();
        String url_string = loader.getUrlForView(loc,500);
        assertEquals(url_string,right_answer);
    }

    @Test
    public void checkDownLoadUCSC() throws ImageUnavailableException {
        String message = "UCSCLoader should be able to retrieve an image from the UCSC Genome Browser Web site.";
        Map<String, String> cookies = new HashMap<>();
        cookies.put(UCSCView.UCSCUSERID, "");
        Loc loc = new Loc("hg19", "chr1", 6203693, 6206373);
        BufferedImage image = new UCSCLoader().getImage(loc, 800, cookies);
        assertNotNull(image);
    }

}
