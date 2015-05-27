package com.lorainelab.quickload.util;

import com.affymetrix.genometry.data.SpeciesInfo;
import static com.affymetrix.genometry.util.UriUtils.getInputStream;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.lorainelab.quickload.QuickloadConstants;
import static com.lorainelab.quickload.QuickloadConstants.ANNOTS_XML;
import com.lorainelab.quickload.model.annots.QuickloadFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class QuickloadUtils {

    private static final Logger logger = LoggerFactory.getLogger(QuickloadUtils.class);
    private static final AnnotsParser ANNOTS_PARSER = new AnnotsParser();

    public static void loadGenomeVersionSynonyms(String urlString, Multimap<String, String> genomeVersionSynonyms) {
        try {
            urlString = toExternalForm(urlString);
            urlString += QuickloadConstants.SYNONYMS_TXT;
            URI uri = new URI(urlString);
            parseGenomeVersionSynonyms(getInputStream(uri), genomeVersionSynonyms);
        } catch (URISyntaxException | IOException ex) {
            logger.warn("Optional quickload synonyms.txt file could not be loaded from {}", urlString);
        }

    }

    private static void parseGenomeVersionSynonyms(InputStream istream, Multimap<String, String> genomeVersionSynonyms) throws IOException {
        try (Reader reader = new InputStreamReader(istream)) {
            Stream<CSVRecord> recordStream = getCSVRecordStreamFromTabDelimitedResource(reader);
            recordStream.filter(record -> record.size() >= 2).forEach(record -> {
                genomeVersionSynonyms.putAll(record.get(0), record);
            });
        }
    }

    public static void loadSpeciesInfo(String urlString, Set<SpeciesInfo> speciesInfo) {
        try {
            urlString = toExternalForm(urlString);
            urlString += QuickloadConstants.SPECIES_TXT;
            URI uri = new URI(urlString);
            parseSpeciesInfo(getInputStream(uri), speciesInfo);
        } catch (IOException | URISyntaxException ex) {
            logger.warn("Optional species.txt could not be loaded from: {}", urlString);
        }
    }

    private static void parseSpeciesInfo(InputStream istream, Set<SpeciesInfo> speciesInfo) throws IOException {
        try (Reader reader = new InputStreamReader(istream)) {
            Stream<CSVRecord> recordStream = getCSVRecordStreamFromTabDelimitedResource(reader);
            recordStream.forEach(record -> {
                String speciesName = record.get(0);
                String commonName = null;
                String genomeVersionPrefix = null;
                if (record.size() >= 2) {
                    commonName = record.get(1);
                }
                if (record.size() >= 3) {
                    genomeVersionPrefix = record.get(2);
                }
                SpeciesInfo info = new SpeciesInfo(speciesName, commonName, genomeVersionPrefix);
                speciesInfo.add(info);
            });
        }
    }

    public static void loadSupportedGenomeVersionInfo(String urlString, Map<String, Optional<String>> supportedGenomeVersionInfo) throws IOException, URISyntaxException {
        try {
            urlString = toExternalForm(urlString);
            urlString += QuickloadConstants.CONTENTS_TXT;
            URI uri = new URI(urlString);
            processContentsTextFile(getInputStream(uri), supportedGenomeVersionInfo);
        } catch (IOException | URISyntaxException ex) {
            logger.warn("Could not read contents.txt from: {}", urlString);
            throw ex;
        }
    }

    private static void processContentsTextFile(InputStream istream, Map<String, Optional<String>> supportedGenomeVersionInfo) throws IOException {
        try (Reader reader = new InputStreamReader(istream)) {
            Stream<CSVRecord> recordStream = getCSVRecordStreamFromTabDelimitedResource(reader);
            recordStream.forEach(record -> {
                if (record.size() >= 1) {
                    String genomeName = record.get(0);
                    if (record.size() >= 2) {
                        String description = record.get(1);
                        supportedGenomeVersionInfo.put(genomeName, Optional.of(description));
                    } else {
                        supportedGenomeVersionInfo.put(genomeName, Optional.empty());
                    }
                }
            });
        }
    }

    public static Optional<Set<QuickloadFile>> getGenomeVersionData(String quickloadUrl, String genomeVersionName, Map<String, Optional<String>> supportedGenomeVersionInfo) {
        String genomeVersionBaseUrl = getGenomeVersionBaseUrl(quickloadUrl, genomeVersionName);
        String annotsXmlUrl = genomeVersionBaseUrl + QuickloadConstants.ANNOTS_XML;
        try {
            URI uri = new URI(annotsXmlUrl);
            try (Reader reader = new InputStreamReader(getInputStream(uri))) {
                List<QuickloadFile> annotsFiles = ANNOTS_PARSER.getQuickloadFileList(reader);
                if (!annotsFiles.isEmpty()) {
                    return Optional.of(Sets.newLinkedHashSet(annotsFiles));
                } else {
                    logger.error("Could not read annots.xml or this file was empty. Skipping this genome version for quickload site {}", genomeVersionBaseUrl);
                    supportedGenomeVersionInfo.remove(genomeVersionName);
                }
            }
        } catch (URISyntaxException | IOException ex) {
            logger.error("Missing required {} file for genome version {}, skipping this genome version for quickload site {}", ANNOTS_XML, genomeVersionName, genomeVersionBaseUrl, ex);
        }
        return Optional.empty();
    }

    public static Optional<Map<String, Integer>> getAssemblyInfo(String quickloadUrl, String genomeVersionName) throws IOException, URISyntaxException {
        String genomeVersionBaseUrl = getGenomeVersionBaseUrl(quickloadUrl, genomeVersionName);
        String genomeTxtUrl = genomeVersionBaseUrl + QuickloadConstants.GENOME_TXT;
        Map<String, Integer> assemblyInfo = Maps.newLinkedHashMap();
        URI uri = new URI(genomeTxtUrl);
        try (Reader reader = new InputStreamReader(getInputStream(uri))) {
            getCSVRecordStreamFromTabDelimitedResource(reader).filter(record -> record.size() == 2).forEach(record -> {
                assemblyInfo.put(record.get(0), Integer.parseInt(record.get(1)));
            });
        }
        if (!assemblyInfo.isEmpty()) {
            return Optional.of(assemblyInfo);
        }
        return Optional.empty();
    }

    public static String getGenomeVersionBaseUrl(String quickloadUrl, String genomeVersionName) {
        final String externalQuickloadUrl = toExternalForm(quickloadUrl);
        final Function<String, String> toGenomeVersionBaseUrl = name -> externalQuickloadUrl + name;
        return toExternalForm(toGenomeVersionBaseUrl.apply(genomeVersionName));
    }

    public static String toExternalForm(String urlString) {
        if (!urlString.endsWith("/")) {
            urlString += "/";
        }
        return urlString;
    }

    private static Stream<CSVRecord> getCSVRecordStreamFromTabDelimitedResource(final Reader reader) throws IOException {
        Iterable<CSVRecord> records = CSVFormat.TDF
                .withCommentMarker('#')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(true)
                .parse(reader);
        return StreamSupport.stream(records.spliterator(), false);
    }

}
