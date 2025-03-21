package com.affymetrix.genometry.symloader;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryConstants;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.filter.SymmetryFilterIntersecting;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.parsers.FileTypeHandler;
import com.affymetrix.genometry.parsers.FileTypehandlerRegistry;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.UcscPslSym;
import com.affymetrix.genometry.util.BioSeqUtils;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.GraphSymUtils;
import com.affymetrix.genometry.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometry.util.LocalUrlCacher;
import org.lorainelab.igb.cache.api.CacheStatus;
import org.lorainelab.igb.cache.api.RemoteFileCacheService;
import org.lorainelab.igb.externalsort.api.ComparatorMetadata;
import org.lorainelab.igb.externalsort.api.ExternalSortConfiguration;
import org.lorainelab.igb.externalsort.api.ExternalSortService;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jnicol
 */
public abstract class SymLoader {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SymLoader.class);

    public static final String TOO_MANY_CONTIGS_EXCEPTION = "Too many open files";
    public static final int UNKNOWN_CHROMOSOME_LENGTH = 1; // for unknown chromosomes when the length is not known
    public String extension;	// used for ServerUtils call
    public URI uri; //fwang4:qlmirror
    protected URI indexUri;
    protected boolean isResidueLoader = false;	// Let other classes know if this is just residues
    protected volatile boolean isInitialized = false;
    protected final Map<BioSeq, File> chrList = new HashMap<>();
    protected final Map<BioSeq, Boolean> chrSort = new HashMap<>();
    protected final GenomeVersion genomeVersion;
    public final String featureName;
    protected static RemoteFileCacheService remoteFileCacheService;
    protected ExternalSortService externalSortService;
    protected BundleContext bundleContext;
    private static final Predicate<String> IS_NOT_TRACK_LINE = line -> !line.startsWith("track");
    private static final Predicate<String> IS_NOT_COMMENT_LINE = line -> !line.startsWith("#");
    private static final Predicate<String> IS_NOT_BROWSER_LINE = line -> !line.startsWith("browser");
    private static final Predicate<String> IS_PARSEABLE_LINE = IS_NOT_COMMENT_LINE.and(IS_NOT_TRACK_LINE).and(IS_NOT_BROWSER_LINE);
    protected ComparatorMetadata comparatorMetadata = new ComparatorMetadata();

    private static final List<LoadStrategy> strategyList = new ArrayList<>();

    static {
        strategyList.add(LoadStrategy.NO_LOAD);
        strategyList.add(LoadStrategy.VISIBLE);
        strategyList.add(LoadStrategy.GENOME);
    }

    /**
     * A SymLoader contains business logic that transforms a genomic data file into
     * visualizations on the IGB screen.
     * When a user opens a file in IGB, a Symloader for that file is created and 
     * persists for as long as the file is being viewed.
     * The Symloader keeps tracks of what data have been loaded thus far, and also
     * keeps track of any chromosome names contained in the file.
     *
     * @param uri - location of the genomic data file to be loaded (target file)
     * @param indexUri - index file that maps genomic location to bytes in the target data file
     * @param featureName - track name once the data are loaded into a track in IGB
     * @param genomeVersion - mapping of chromosome names to sizes
     */
    public SymLoader(URI uri, Optional<URI> indexUri, String featureName, GenomeVersion genomeVersion) {
        final Bundle bundle = FrameworkUtil.getBundle(SymLoader.class);
        if (bundle != null) { // this could happen in unit tests
            bundleContext = bundle.getBundleContext();
            initCacheServiceTracker();
        }
        this.uri = uri;
        this.featureName = featureName;
        this.genomeVersion = genomeVersion;
        // IGB depends on the file extension suffix to determine data file format
        extension = getExtension(uri);
        if (indexUri.isPresent()) {
            this.indexUri = indexUri.get();
        }
    }

    public ExternalSortService getExternalSortService() {
        return externalSortService;
    }

    public void setExternalSortService(ExternalSortService externalSortService) {
        this.externalSortService = externalSortService;
    }

    public URI getIndexUri() {
        return indexUri;
    }

    public void setIndexUri(URI indexUri) {
        this.indexUri = indexUri;
    }

    protected void init() throws Exception {
        this.isInitialized = true;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFeatureName() {
        return featureName;
    }

    private void initCacheServiceTracker() {
        ServiceTracker<RemoteFileCacheService, Object> rcDependencyTracker;

        rcDependencyTracker = new ServiceTracker<RemoteFileCacheService, Object>(bundleContext, RemoteFileCacheService.class, null) {
            @Override
            public Object addingService(ServiceReference<RemoteFileCacheService> serviceReference) {
                remoteFileCacheService = bundleContext.getService(serviceReference);
                return super.addingService(serviceReference);
            }
        };

        rcDependencyTracker.open();

        ServiceTracker<ExternalSortService, Object> esDependencyTracker;

        esDependencyTracker = new ServiceTracker<ExternalSortService, Object>(bundleContext, ExternalSortService.class, null) {
            @Override
            public Object addingService(ServiceReference<ExternalSortService> serviceReference) {
                externalSortService = bundleContext.getService(serviceReference);
                return super.addingService(serviceReference);
            }
        };

        esDependencyTracker.open();
    }

    private Optional<BufferedInputStream> checkRemoteFileCache(URL fileUrl) throws IOException {
        BufferedInputStream bis = null;

        if (!remoteFileCacheService.cacheExists(fileUrl)) {
            Optional<InputStream> fileIs = remoteFileCacheService.getFilebyUrl(fileUrl, false);
            try {
                CacheStatus cacheStatus = remoteFileCacheService.getCacheStatus(fileUrl);
                if (cacheStatus.isDataExists()) {
                    StringBuffer stripped_name = new StringBuffer();
                    InputStream is = GeneralUtils.unzipStream(new FileInputStream(cacheStatus.getData()), cacheStatus.getUrl(), stripped_name);
                    if (is instanceof BufferedInputStream) {
                        bis = (BufferedInputStream) is;
                    } else {
                        bis = new BufferedInputStream(is);
                    }
                    return Optional.ofNullable(bis);
                }
            } finally {
                if (fileIs.isPresent()) {
                    fileIs.get().close();
                }
            }
        }
        return Optional.empty();
    }

    protected boolean buildIndex() throws Exception {
        BufferedInputStream bis = null;
        Map<String, Integer> chrLength = new HashMap<>();
        Map<String, File> chrFiles = new HashMap<>();
        BufferedInputStream sortedResultIs = null;
        Optional<File> sortedResult = Optional.empty();
        try {

            URL fileUrl = uri.toURL();
            if (remoteFileCacheService != null && BedUtils.isRemoteBedFile(fileUrl)) {
                Optional<BufferedInputStream> cachedStream = checkRemoteFileCache(fileUrl);
                if (cachedStream.isPresent()) {
                    bis = cachedStream.get();
                } else {
                    bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(uri);
                }
            } else {
                bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(uri);
            }
            if (bis == null) {
                throw new IOException("Input Stream NULL");
            }
            ExternalSortConfiguration conf = new ExternalSortConfiguration();
            conf.setMaxMemoryInBytes(50_000_000);
            conf.setMaxTmpFiles(100);

            if (BedUtils.isRemoteBedFile(fileUrl)) {
                CacheStatus cacheStatus = remoteFileCacheService.getCacheStatus(fileUrl);
                if (cacheStatus.isDataExists()) {
                    conf.setNumHeaderRows(findChromStartIndex(cacheStatus.getData()));
                    sortedResult = externalSortService.merge(cacheStatus.getData(), cacheStatus.getUrl(), comparatorMetadata, conf);
                    if (sortedResult.isPresent()) {
                        sortedResultIs = new BufferedInputStream(new FileInputStream(sortedResult.get()));
                    }

                }
            } else {
                //TODO: Replace with new remoteFileCacheService when other formats are approved
                File input = LocalUrlCacher.convertURIToFile(uri);
                conf.setNumHeaderRows(findChromStartIndex(input));
                sortedResult = externalSortService.merge(input, uri.toString(), comparatorMetadata, conf);
                if (sortedResult.isPresent()) {
                    sortedResultIs = new BufferedInputStream(new FileInputStream(sortedResult.get()));
                }
            }
            if (sortedResult.isPresent()) {
                if (parseLines(sortedResultIs, chrLength, chrFiles)) {
                    createResults(chrLength, chrFiles);
                    //Delete temp file
                    GeneralUtils.safeClose(sortedResultIs);
                    Files.delete(sortedResult.get().toPath());
                    return true;
                }
            } else {
                StringBuffer strippedName = new StringBuffer();
                try (InputStream is = GeneralUtils.unzipStream(uri.toURL().openStream(), uri.toString(), strippedName)) {
                    if (parseLines(is, chrLength, chrFiles)) {
                        createResults(chrLength, chrFiles);
                        return true;
                    }
                }
            }

        } catch (Exception ex) {
            throw ex;
        } finally {
            GeneralUtils.safeClose(bis);

        }
        return false;
    }

    private int findChromStartIndex(File file) {
        int index = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                if (IS_PARSEABLE_LINE.test(line)) {
                    break;
                }
                index++;
            }
        } catch (IOException ex) {
            return 0;
        }
        return index;
    }

    public GenomeVersion getAnnotatedSeqGroup() {
        return genomeVersion;
    }

    /**
     * @return possible strategies to load this URI.
     */
    public List<LoadStrategy> getLoadChoices() {
        return strategyList;
    }

    /**
     * Get list of chromosomes used in the file/uri. Especially useful when loading a file into an "unknown" genome
     *
     * @return List of chromosomes
     */
    public List<BioSeq> getChromosomeList() throws Exception {
        return Collections.<BioSeq>emptyList();
    }

    /**
     * @return List of symmetries in genome
     */
    public List<? extends SeqSymmetry> getGenome() throws Exception {

        BufferedInputStream bis = null;
        try {
            // This will also unzip the stream if necessary
            bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(this.uri);
            return parse(bis, false);
        } catch (Exception ex) {
            throw ex;
        } finally {
            GeneralUtils.safeClose(bis);
        }

        //Logger.getLogger(this.getClass().getName()).log(
        //		Level.SEVERE, "Retrieving genome is not defined");
        //return null;
    }

    /**
     * @param seq - chromosome
     * @return List of symmetries in chromosome
     */
    public List<? extends SeqSymmetry> getChromosome(BioSeq seq) throws Exception {
        Logger.getLogger(this.getClass().getName()).log(
                Level.FINE, "Retrieving chromosome is not optimized");
        List<? extends SeqSymmetry> genomeResults = this.getGenome();
        if (seq == null || genomeResults == null) {
            return genomeResults;
        }
        return filterResultsByChromosome(genomeResults, seq);
    }

    public List<String> getFormatPrefList() {
        return Collections.<String>emptyList();
    }

    public String getExtension() {
        return extension;
    }

    public static String getExtension(URI uri) {
        if (uri == null) {
            return null;
        }
        return getExtension(uri.toASCIIString().toLowerCase());
    }

    public static String getExtension(String uriString) {
        String unzippedStreamName = GeneralUtils.stripEndings(uriString);
        String extension = GeneralUtils.getExtension(unzippedStreamName);
        extension = extension.substring(extension.indexOf('.') + 1);	// strip off first .
        extension = extension.split("\\?")[0]; // strip off potential query params
        return extension;
    }

    /**
     * Return the symmetries that match the given chromosome.
     */
    public static List<SeqSymmetry> filterResultsByChromosome(List<? extends SeqSymmetry> genomeResults, BioSeq seq) {
        List<SeqSymmetry> results = new ArrayList<>();
        for (SeqSymmetry sym : genomeResults) {
            BioSeq seq2 = null;
            if (sym instanceof UcscPslSym) {
                seq2 = ((UcscPslSym) sym).getTargetSeq();
            } else {
                seq2 = sym.getSpanSeq(0);
            }
            if (seq.equals(seq2)) {
                results.add(sym);
            }
        }
        return results;
    }

    public URI getURI() {
        return this.uri;
    }

    public void setURI(URI newURI) {
        this.uri = newURI;
    }

    /**
     * Get a region of the chromosome.
     *
     * @param overlapSpan - span of overlap
     * @return List of symmetries satisfying requirements
     */
    public List<? extends SeqSymmetry> getRegion(final SeqSpan overlapSpan) throws Exception {
        Logger.getLogger(this.getClass().getName()).log(
                Level.WARNING, "Retrieving region is not supported.  Returning entire chromosome.");
        return this.getChromosome(overlapSpan.getBioSeq());
    }

    public boolean isResidueLoader() {
        return isResidueLoader;
    }

    /**
     * Get residues in the region of the chromosome. This is generally only defined for some parsers
     *
     * @param span - span of chromosome
     * @return String of residues
     */
    public String getRegionResidues(SeqSpan span) throws Exception {
        Logger.getLogger(this.getClass().getName()).log(
                Level.WARNING, "Not supported.  Returning empty string.");
        return "";
    }

    protected boolean parseLines(InputStream istr, Map<String, Integer> chrLength, Map<String, File> chrFiles) throws Exception {
        Logger.getLogger(this.getClass().getName()).log(
                Level.SEVERE, "parseLines is not defined");
        return false;
    }

    protected void addToLists(
            Map<String, BufferedWriter> chrs, String current_seq_id, Map<String, File> chrFiles, Map<String, Integer> chrLength, String format) throws IOException {

        String fileName = current_seq_id;
        if (fileName.length() < 3) {
            fileName += "___";
        }
        format = !format.startsWith(".") ? "." + format : format;
        File tempFile = File.createTempFile(fileName, format);
        tempFile.deleteOnExit();
        chrs.put(current_seq_id, new BufferedWriter(new FileWriter(tempFile, true)));
        chrFiles.put(current_seq_id, tempFile);
        chrLength.put(current_seq_id, 0);
    }

    protected void createResults(Map<String, Integer> chrLength, Map<String, File> chrFiles) {
        for (Entry<String, Integer> bioseq : chrLength.entrySet()) {
            String key = bioseq.getKey();
            chrList.put(genomeVersion.addSeq(key, bioseq.getValue(), uri.toString()), chrFiles.get(key));
        }
    }

    /**
     * Split list of symmetries by track.
     *
     * @param results - list of symmetries
     * @return - Map<String trackName,List<SeqSymmetry>>
     */
    public static Map<String, List<SeqSymmetry>> splitResultsByTracks(List<? extends SeqSymmetry> results) {
        Map<String, List<SeqSymmetry>> track2Results = new HashMap<>();
        List<SeqSymmetry> resultList = null;
        String method = null;
        for (SeqSymmetry result : results) {
            method = BioSeqUtils.determineMethod(result);
            if (track2Results.containsKey(method)) {
                resultList = track2Results.get(method);
            } else {
                resultList = new ArrayList<>();
                track2Results.put(method, resultList);
            }
            resultList.add(result);
        }

        return track2Results;
    }

    public static Map<BioSeq, List<SeqSymmetry>> splitResultsBySeqs(List<? extends SeqSymmetry> results) {
        Map<BioSeq, List<SeqSymmetry>> seq2Results = new HashMap<>();
        List<SeqSymmetry> resultList = null;
        BioSeq seq = null;
        for (SeqSymmetry result : results) {

            for (int i = 0; i < result.getSpanCount(); i++) {
                seq = result.getSpan(i).getBioSeq();

                if (seq2Results.containsKey(seq)) {
                    resultList = seq2Results.get(seq);
                } else {
                    resultList = new ArrayList<>();
                    seq2Results.put(seq, resultList);
                }
                resultList.add(result);
            }

        }

        return seq2Results;
    }

    public static Map<String, List<? extends SeqSymmetry>> splitFilterAndAddAnnotation(final SeqSpan span, List<? extends SeqSymmetry> results, DataSet feature) {
        Map<String, List<SeqSymmetry>> entries = SymLoader.splitResultsByTracks(results);
        Map<String, List<? extends SeqSymmetry>> added = new HashMap<>();
        SymmetryFilterIntersecting filter = new SymmetryFilterIntersecting();
        filter.setParameterValue(filter.getParametersType().entrySet().iterator().next().getKey(), feature.getRequestSym());

        for (Entry<String, List<SeqSymmetry>> entry : entries.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }

            List<? extends SeqSymmetry> filteredFeats = filterOutExistingSymmetries(span.getBioSeq(), entry.getValue(), filter);
            if (filteredFeats.isEmpty()) {
                continue;
            }

            // Some format do not annotate. So it might not have method name. e.g bgn
            if (entry.getKey() != null) {
                feature.setMethod(entry.getKey());
                addAnnotations(filteredFeats, span, feature.getURI(), feature);
                added.put(entry.getKey(), filteredFeats);
            } else {
                Logger.getLogger(SymLoader.class.getName()).log(Level.SEVERE, "No method name in loaded syms for {0}", feature.getDataSetName());
            }
        }

        return added;
    }

    public static void addAnnotations(
            List<? extends SeqSymmetry> filteredFeats, SeqSpan span, URI uri, DataSet feature) {
        if (filteredFeats.size() > 0 && filteredFeats.get(0) instanceof GraphSym) {
            GraphSym graphSym = (GraphSym) filteredFeats.get(0);
            if (filteredFeats.size() == 1 && graphSym.isSpecialGraph()) {
                BioSeq seq = graphSym.getGraphSeq();
                seq.addAnnotation(graphSym);
            } else {
                // We assume that if there are any GraphSyms, then we're dealing with a list of GraphSyms.
                //grafs.add((GraphSym)feat);
                filteredFeats.stream().filter(feat -> feat instanceof GraphSym).forEach(feat -> {
                    GraphSymUtils.addChildGraph((GraphSym) feat, feat.getID(), ((GraphSym) feat).getGraphName(), uri.toString(), span);
                });
            }

            return;
        }

        BioSeq seq = span.getBioSeq();
        for (SeqSymmetry feat : filteredFeats) {
            seq.addAnnotation(feat, feature.getExtension(), true);
        }

    }

    private static List<? extends SeqSymmetry> filterOutExistingSymmetries(BioSeq seq, List<? extends SeqSymmetry> syms, SymmetryFilterIntersecting filter) {
        List<SeqSymmetry> filteredFeats = new ArrayList<>(syms.size());

        for (SeqSymmetry sym : syms) {
            if (filter.filterSymmetry(seq, sym)) {
                filteredFeats.add(sym);
            }
        }

        return filteredFeats;
    }

    public static List<BioSeq> getChromosomes(URI uri, String featureName, String groupID) throws Exception {
        GenomeVersion temp_group = new GenomeVersion(groupID);
        SymLoader temp = new SymLoader(uri, Optional.empty(), featureName, temp_group) {
        };
        List<? extends SeqSymmetry> syms = temp.getGenome();
        List<BioSeq> seqs = new ArrayList<>();
        seqs.addAll(temp_group.getSeqList());

        // Force GC
        syms.clear();
        syms = null;
        temp = null;
        temp_group = null;

        return seqs;
    }

    public List<? extends SeqSymmetry> parse(InputStream is, boolean annotate_seq)
            throws Exception {
        FileTypeHandler fileTypeHandler = FileTypehandlerRegistry.getFileTypeHolder().getFileTypeHandler(extension);
        if (fileTypeHandler == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, MessageFormat.format(GenometryConstants.BUNDLE.getString("noHandler"), extension));
            return null;
        }
        return fileTypeHandler.getParser().parse(new BufferedInputStream(is), genomeVersion, featureName, uri.toString(), false);
    }

    public void clear() {
    }

    /**
     * Tells if it is okay to load data on multiple threads.
     *
     * @return
     */
    public boolean isMultiThreadOK() {
        return false;
    }
}
