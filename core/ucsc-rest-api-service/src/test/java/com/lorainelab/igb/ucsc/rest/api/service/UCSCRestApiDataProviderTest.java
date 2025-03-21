package com.lorainelab.igb.ucsc.rest.api.service;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.general.DataContainer;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lorainelab.igb.ucsc.rest.api.service.UCSCRestApiDataProvider;
import org.lorainelab.igb.ucsc.rest.api.service.utils.ApiResponseHandler;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UCSCRestApiDataProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(UCSCRestApiDataProviderTest.class);
    private final static String UCSC_REST_URL = "https://api.genome.ucsc.edu";
    private static final String HUMAN_GENOME_ID = "hg38";
    private static UCSCRestApiDataProvider ucscRestApiDataProvider;
    private static GenomeVersion genomeVersion;
    private static DataContainer dataContainer;
    private final String validChromosomeName = "chr1";
    private final String notValidChromosomeName = "chr2";
    @Mock
    private CloseableHttpClient mockHttpClient;
    public static final String ucsc_genomes_test_file = "ucsc-genomes-data.json";
    public static final String human_chromosome_test_file = "human-chromosome-data.json";
    public static final String genome_sequence_test_file = "genome-sequence-data.json";
    public static final String available_tracks_test_file = "available-tracks-data.json";
    public static final String cloneEnd_schema_test_file = "cloneEnd-schema-data.json";

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        try (MockedStatic<HttpClients> mockedStatic = Mockito.mockStatic(HttpClients.class)) {
            mockedStatic.when(HttpClients::createDefault).thenReturn(mockHttpClient);
            URL resourceURL = Objects.requireNonNull(UCSCRestApiDataProviderTest.class.getClassLoader().getResource(ucsc_genomes_test_file));
            String filename = Paths.get(resourceURL.toURI()).toString();
            String mockResponse = Files.readString(Paths.get(filename));
            String apiUrl = "https://api.genome.ucsc.edu/list/ucscGenomes";
            when(mockHttpClient.execute(Mockito.argThat(httpget ->
                    httpget instanceof HttpGet && httpget.getURI().toString().equals(apiUrl)), any(ResponseHandler.class)))
                    .thenReturn(mockResponse);
            ucscRestApiDataProvider = new UCSCRestApiDataProvider(1, UCSC_REST_URL, "UCSC REST", "15", "https://genome.ucsc.edu/cgi-bin/hgTrackUi");
            ucscRestApiDataProvider.initialize();
        }
        genomeVersion = new GenomeVersion(HUMAN_GENOME_ID);
        dataContainer = new DataContainer(genomeVersion, ucscRestApiDataProvider);
    }

    @Test
    public void retrieveSupportedGenomeVersions() {
        Assertions.assertTrue(ucscRestApiDataProvider.getSupportedGenomeVersionNames().contains(HUMAN_GENOME_ID));
        ucscRestApiDataProvider.getSupportedGenomeVersionNames().forEach(logger::info);
    }

    @Test
    public void retrieveAssemblyInfo() throws IOException, URISyntaxException {
        try (MockedStatic<HttpClients> mockedStatic = Mockito.mockStatic(HttpClients.class)) {
            mockedStatic.when(HttpClients::createDefault).thenReturn(mockHttpClient);
            URL resourceURL = Objects.requireNonNull(UCSCRestApiDataProviderTest.class.getClassLoader().getResource(human_chromosome_test_file));
            String filename = Paths.get(resourceURL.toURI()).toString();
            String mockResponse = Files.readString(Paths.get(filename));
            String apiUrl = "https://api.genome.ucsc.edu/list/chromosomes?genome=hg38";
            when(mockHttpClient.execute(Mockito.argThat(httpget ->
                    httpget instanceof HttpGet && httpget.getURI().toString().equals(apiUrl)), any(ResponseHandler.class)))
                    .thenReturn(mockResponse);
            assertTrue(ucscRestApiDataProvider.getAssemblyInfo(genomeVersion).containsKey(validChromosomeName));
            assertFalse(ucscRestApiDataProvider.getAssemblyInfo(genomeVersion).containsKey(notValidChromosomeName));
        }
    }

    @Test
    public void retrieveSequence() throws IOException, URISyntaxException {
        try (MockedStatic<HttpClients> mockedStatic = Mockito.mockStatic(HttpClients.class)) {
            mockedStatic.when(HttpClients::createDefault).thenReturn(mockHttpClient);
            URL resourceURL = Objects.requireNonNull(UCSCRestApiDataProviderTest.class.getClassLoader().getResource(genome_sequence_test_file));
            String filename = Paths.get(resourceURL.toURI()).toString();
            String mockResponse = Files.readString(Paths.get(filename));
            String apiUrl = "https://api.genome.ucsc.edu/getData/sequence?genome=hg38&chrom=chr1&start=10000&end=12000";
            when(mockHttpClient.execute(Mockito.argThat(httpget ->
                    httpget instanceof HttpGet && httpget.getURI().toString().equals(apiUrl)), any(ResponseHandler.class)))
                    .thenReturn(mockResponse);
            SeqSpan span = new SimpleSeqSpan(10000, 12000, new BioSeq(validChromosomeName, 0));
            String sequence = ucscRestApiDataProvider.getSequence(dataContainer, span);
            assertFalse(sequence.isEmpty());
        }
    }

    @Test
    public void retrieveAvailableDataSets() throws IOException, URISyntaxException {
        try (MockedStatic<HttpClients> mockedStatic = Mockito.mockStatic(HttpClients.class)) {
            mockedStatic.when(HttpClients::createDefault).thenReturn(mockHttpClient);
            URL resourceURL = Objects.requireNonNull(UCSCRestApiDataProviderTest.class.getClassLoader().getResource(available_tracks_test_file));
            String availableTracksFilename = Paths.get(resourceURL.toURI()).toString();
            String availableTracksMockResponse = Files.readString(Paths.get(availableTracksFilename));
            String availableTracksApiUrl = "https://api.genome.ucsc.edu/list/tracks?genome=hg38";
            when(mockHttpClient.execute(Mockito.argThat(httpget ->
                    httpget instanceof HttpGet && httpget.getURI().toString().equals(availableTracksApiUrl)), any(ResponseHandler.class)))
                    .thenReturn(availableTracksMockResponse);
            Set<DataSet> availableDataSets = ucscRestApiDataProvider.getAvailableDataSets(dataContainer);
            assertTrue(availableDataSets.stream().anyMatch(dataSet -> dataSet.getDataSetName().equals("Genes and Gene Predictions/genePredArchive/AUGUSTUS (augustusGene)")));
        }
    }

    @Test
    public void testLinkoutUrl() {
        String linkoutUrl = "https://genome.ucsc.edu/cgi-bin/hgTrackUi?db=hg18&g=refGene";
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(linkoutUrl);
            String responseBody = httpClient.execute(httpget, new ApiResponseHandler());
            if(responseBody.isEmpty())
                logger.error("UCSC Linkout URL isn't responding");
        } catch (IOException e) {
            logger.error("UCSC Linkout URL isn't responding");
        }
    }
}

