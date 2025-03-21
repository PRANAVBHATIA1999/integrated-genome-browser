package org.lorainelab.igb.quickload.utils;

import static com.affymetrix.genometry.util.UriUtils.getInputStream;
import static org.lorainelab.igb.quickload.util.QuickloadUtils.getUri;
import com.google.common.io.CharStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author dcnorris
 */
public class QuickloadUtilsTest {

    @Test
    public void contentsTxtParserTest() throws IOException, URISyntaxException {
        //URI uri = new URI("http://quickload.bioviz.org/quickload/contents.txt");
        String path = "src/test/resources/quickload/contents.txt";
        File file = new File(path);
        URI mockURI = file.toURI();
        assertTrue(CharStreams.toString(new InputStreamReader(getInputStream(mockURI))).contains("A_thaliana_Jun_2009"));
    }

    @Test
    public void getUriTest() {
        String absoluteOnlineFileName = "http://igbquickload.org/quickload/A_thaliana_Jun_2009/Araport11.bed.gz";
        String relativeFileName = "../down stream/E_unicornis_Jul_2043_up.bed.gz";
        String absoluteLocal_fileName = new File("/Users/lorainelab/Desktop/2805/relative space/E_unicornis_Jul_2043/down stream/E_unicornis_Jul_2043_down.bed.gz").getAbsolutePath();
        String onlineUrl = "http://igbquickload.org/quickload/";
        String localUrl = "/Users/lorainelab/Desktop/2805/relative space/";
        String genomeVersionName = "A_thaliana_Apr_2008";
        String fileName = "E_unicornis_Jul_2043_down.bed.gz";
        URI uri = null;

        //test absolute online
        uri = getUri(absoluteOnlineFileName, onlineUrl, genomeVersionName);
        assertEquals("http://igbquickload.org/quickload/A_thaliana_Jun_2009/Araport11.bed.gz", uri.toString());

        //test relative online
        uri = getUri(relativeFileName, onlineUrl, genomeVersionName);
        assertEquals("http://igbquickload.org/quickload/A_thaliana_Apr_2008/../down%20stream/E_unicornis_Jul_2043_up.bed.gz", uri.toString());

        //test absolute local
        uri = getUri(absoluteLocal_fileName, localUrl, genomeVersionName);
        assertEquals(new File("/Users/lorainelab/Desktop/2805/relative space/E_unicornis_Jul_2043/down stream/E_unicornis_Jul_2043_down.bed.gz").toURI().toString(), uri.toString());

        uri = getUri(fileName, localUrl, genomeVersionName);
        assertEquals(new File("/Users/lorainelab/Desktop/2805/relative space/A_thaliana_Apr_2008/E_unicornis_Jul_2043_down.bed.gz").toURI().toString(), uri.toString());

        //test relative local
        uri = getUri(relativeFileName, localUrl, genomeVersionName);
        assertEquals(new File("/Users/lorainelab/Desktop/2805/relative space/A_thaliana_Apr_2008/../down stream/E_unicornis_Jul_2043_up.bed.gz").toURI().toString(), uri.toString());
    }

}
