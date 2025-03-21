package org.lorainelab.igb.das.utils;

import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.SeqSpan;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.google.common.collect.Maps;
import org.lorainelab.igb.das.model.dsn.DasDsn;
import org.lorainelab.igb.das.model.ep.DasEp;
import org.lorainelab.igb.das.model.gff.DasGff;
import org.lorainelab.igb.das.model.types.DasTypes;
import org.lorainelab.igb.synonymlookup.services.GenomeVersionSynonymLookup;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author dcnorris
 */
public class DasServerUtils {

    private static final Logger logger = LoggerFactory.getLogger(DasServerUtils.class);
    private static final String DSN = "dsn";
    private static final String SEQUENCE_REQUEST_PREFIX = "dna";
    private static final String SEGMENT = "segment";
    private final static String ENTRY_POINTS = "entry_points";
    private final static String TYPES = "types";
    private final static String FEATURES = "features";
    private final static String TYPE = "type";
    //lazily initialize to avoid overhead if never needed
    private static JAXBContext dasDsnContext;
    private static Unmarshaller dasDsnUnmarshaller;
    private static JAXBContext dasEpContext;
    private static Unmarshaller dasEpUnmarshaller;
    private static JAXBContext dasTypesContext;
    private static Unmarshaller dasTypesUnmarshaller;
    private static JAXBContext dasFeatureContext;
    private static Unmarshaller dasFeatureUnmarshaller;
    private GenomeVersion genomeVersion;

    public static Optional<DasDsn> retrieveDsnResponse(String rootUrl) throws HttpRequestException {
        String url = checkValidAndSetUrl(rootUrl.trim());
        HttpRequest remoteHttpRequest = HttpRequest.get(toExternalForm(url) + DSN)
                .acceptGzipEncoding()
                .uncompress(true)
                .trustAllCerts()
                .trustAllHosts()
                .followRedirects(true);
        try (InputStream inputStream = remoteHttpRequest.buffer()) {
            if (dasDsnContext == null) {
                dasDsnContext = JAXBContext.newInstance(DasDsn.class);
                dasDsnUnmarshaller = dasDsnContext.createUnmarshaller();
            }
            SAXSource source = createSaxSource(inputStream);
            Optional<DasDsn> dasDsn = Optional.ofNullable((DasDsn) dasDsnUnmarshaller.unmarshal(source));
            return dasDsn;
        } catch (JAXBException ex) {
            logger.error("Could not initialize JAXBContext for {} ", DasDsn.class.getCanonicalName(), ex);
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public static String retrieveDna(String genomeContextRoot, SeqSpan seqSpan) throws HttpRequestException {
        String segmentParam = seqSpan.getBioSeq().getId() + ":" + seqSpan.getMin() + "," + seqSpan.getMax();
        String url = checkValidAndSetUrl(genomeContextRoot.trim());
        HttpRequest remoteHttpRequest = HttpRequest.get(toExternalForm(url) + SEQUENCE_REQUEST_PREFIX, true, SEGMENT, segmentParam)
                .acceptGzipEncoding()
                .uncompress(true)
                .trustAllCerts()
                .trustAllHosts()
                .followRedirects(true);
        try (InputStream inputStream = remoteHttpRequest.buffer()) {
            DasResiduesHandler dasResiduesHandler = new DasResiduesHandler();
            return dasResiduesHandler.getDasResidues(inputStream, seqSpan);
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    public static synchronized Optional<DasEp> retrieveDasEpResponse(String genomeContextRoot) throws HttpRequestException {
        String url = checkValidAndSetUrl(genomeContextRoot.trim());
        final String request = toExternalForm(url) + ENTRY_POINTS;
        HttpRequest remoteHttpRequest = HttpRequest.get(request)
                .acceptGzipEncoding()
                .uncompress(true)
                .trustAllCerts()
                .trustAllHosts()
                .followRedirects(true);
        try (InputStream inputStream = remoteHttpRequest.buffer()) {
            if (dasEpContext == null) {
                dasEpContext = JAXBContext.newInstance(DasEp.class);
                dasEpUnmarshaller = dasEpContext.createUnmarshaller();
            }
            SAXSource saxSource = createSaxSource(inputStream);
            Optional<DasEp> dasEp = Optional.ofNullable((DasEp) dasEpUnmarshaller.unmarshal(saxSource));
            return dasEp;
        } catch (IOException | JAXBException | SAXException | ParserConfigurationException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public static Map<String, Integer> getAssemblyInfo(GenomeVersion genomeVersion, Map<String, String> genomeContextRootMap) {
        final String genomeVersionName = genomeVersion.getName();
        Optional<String> contextRootkey = getContextRootKey(genomeVersionName, genomeContextRootMap, genomeVersion.getGenomeVersionSynonymLookup());
        if (contextRootkey.isPresent()) {
            String contextRoot = genomeContextRootMap.get(contextRootkey.get());
            return retrieveAssemblyInfoByContextRoot(contextRoot);
        }
        return Maps.newLinkedHashMap();
    }

    public static Map<String, Integer> retrieveAssemblyInfoByContextRoot(String contextRoot) {
        Map<String, Integer> assemblyInfo = Maps.newLinkedHashMap();
        Optional<DasEp> retrieveDasEpResponse = DasServerUtils.retrieveDasEpResponse(contextRoot);
        if (retrieveDasEpResponse.isPresent()) {
            DasEp entryPointInfo = retrieveDasEpResponse.get();
            entryPointInfo.getENTRYPOINTS().getSEGMENT().stream().forEach(segment -> {
                assemblyInfo.put(segment.getId(), Integer.parseInt(segment.getStop()));
            });
        }
        return assemblyInfo;
    }

    public static Optional<String> getContextRootKey(final String genomeVersionName, Map<String, String> genomeContextRootMap, GenomeVersionSynonymLookup genomeVersionSynonymLookup) {
        if (genomeContextRootMap.containsKey(genomeVersionName)) {
            return Optional.of(genomeVersionName);
        } else {
            Set<String> genomeVersionSynonyms = genomeVersionSynonymLookup.getSynonyms(genomeVersionName);
            Optional<String> matchingSynonym = genomeVersionSynonyms.stream().filter(syn -> genomeContextRootMap.containsKey(syn)).findFirst();
            if (matchingSynonym.isPresent()) {
                return Optional.of(matchingSynonym.get());
            }
        }
        return Optional.empty();
    }

    public static Optional<DasTypes> retrieveDasTypesResponse(String genomeContextRoot) throws HttpRequestException { 
        String url = checkValidAndSetUrl(genomeContextRoot.trim());
        HttpRequest remoteHttpRequest = HttpRequest.get(toExternalForm(url) + TYPES)
                .acceptGzipEncoding()
                .uncompress(true)
                .trustAllCerts()
                .trustAllHosts()
                .followRedirects(true);
        try (InputStream inputStream = remoteHttpRequest.buffer()) {
            if (dasTypesContext == null) {
                dasTypesContext = JAXBContext.newInstance(DasTypes.class);
                dasTypesUnmarshaller = dasTypesContext.createUnmarshaller();
            }
            SAXSource saxSource = createSaxSource(inputStream);
            Optional<DasTypes> dasTypes = Optional.ofNullable((DasTypes) dasTypesUnmarshaller.unmarshal(saxSource));
            return dasTypes;
        } catch (IOException | JAXBException | SAXException | ParserConfigurationException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public static Optional<DasGff> retrieveDasGffResponse(String genomeContextRoot, String type, SeqSpan seqSpan) throws HttpRequestException {
        final int min = seqSpan.getMin() == 0 ? 1 : seqSpan.getMin();
        final int max = seqSpan.getMax() - 1;
        String segmentParam = seqSpan.getBioSeq().getId() + ":" + min + "," + max;
        String url = checkValidAndSetUrl(genomeContextRoot.trim());
        HttpRequest remoteHttpRequest = HttpRequest.get(toExternalForm(url) + FEATURES, true, TYPE, type, SEGMENT, segmentParam)
                .acceptGzipEncoding()
                .uncompress(true)
                .trustAllCerts()
                .trustAllHosts()
                .followRedirects(true);
        try (InputStream inputStream = remoteHttpRequest.buffer()) {
            if (dasFeatureContext == null) {
                dasFeatureContext = JAXBContext.newInstance(DasGff.class);
                dasFeatureUnmarshaller = dasFeatureContext.createUnmarshaller();
            }
            SAXSource saxSource = createSaxSource(inputStream);
            Optional<DasGff> dasGff = Optional.ofNullable((DasGff) dasFeatureUnmarshaller.unmarshal(saxSource));
            return dasGff;
        } catch (IOException | JAXBException | SAXException | ParserConfigurationException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public static String toExternalForm(String urlString) {
        urlString = urlString.trim();
        if (!urlString.endsWith("/")) {
            urlString += "/";
        }
        return urlString;
    }

    private static SAXSource createSaxSource(final InputStream inputSteam) throws SAXException, ParserConfigurationException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        spf.setFeature("http://xml.org/sax/features/validation", false);
        XMLReader xr = (XMLReader) spf.newSAXParser().getXMLReader();
        SAXSource source = new SAXSource(xr, new InputSource(inputSteam));
        return source;
    }
    
    public static String checkValidAndSetUrl(String uriString) {
        try {
            URL obj = new URL(uriString);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setReadTimeout(5000);
            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                if (code == HttpURLConnection.HTTP_MOVED_TEMP || code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_SEE_OTHER) {
                    String newUrl = conn.getHeaderField("Location");
                    return newUrl;
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uriString;
    }

}
