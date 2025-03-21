package com.affymetrix.genometry.quickload;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.style.DefaultStateProvider;
import com.affymetrix.genometry.style.ITrackStyleExtended;
import com.affymetrix.genometry.symloader.ResidueTrackSymLoader;
import com.affymetrix.genometry.symloader.SymLoader;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.BioSeqUtils;
import com.affymetrix.genometry.util.Constants;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometry.util.LocalUrlCacher;
import com.affymetrix.genometry.util.SeqUtils;
import com.affymetrix.genometry.util.ServerUtils;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jnicol
 * @version $Id$
 */
public class QuickLoadSymLoader extends SymLoader {

    protected SymLoader symL;	// parser factory
    protected GenometryModel gmodel = GenometryModel.getInstance();
    protected boolean loadResidueAsTrack = false;

    public QuickLoadSymLoader(URI uri, Optional<URI> indexUri, String featureName, GenomeVersion genomeVersion, boolean loadResidueAsTrack) {
        this(uri, indexUri, featureName, genomeVersion);
        this.loadResidueAsTrack = loadResidueAsTrack;
    }

    //temporary hack --- this entire class should be removed and refactored in a set of utilities
    public QuickLoadSymLoader(URI uri, Optional<URI> indexUri, String featureName, SymLoader symLoader, GenomeVersion genomeVersion) {
        super(uri, indexUri, featureName, genomeVersion);
        this.symL = symLoader;
    }

    public QuickLoadSymLoader(URI uri, Optional<URI> indexUri, String featureName, GenomeVersion genomeVersion) {
        super(uri, indexUri, featureName, genomeVersion);
    }

    @Override
    protected void init() {
        if (this.isInitialized) {
            return;
        }
        if (symL == null) {
            this.symL = ServerUtils.determineLoader(extension, uri, Optional.ofNullable(indexUri), featureName, genomeVersion);
        }
        this.isResidueLoader = (this.symL != null && this.symL.isResidueLoader());
        if (isResidueLoader && loadResidueAsTrack) {
            this.symL = new ResidueTrackSymLoader(symL);
        }
        this.isInitialized = true;
    }

    /**
     * @return possible strategies to load this URI.
     */
    @Override
    public List<LoadStrategy> getLoadChoices() {
        init();
        // If we're using a symloader, return its load choices.
        if (this.symL != null) {
            return this.symL.getLoadChoices();
        }
        Logger.getLogger(QuickLoadSymLoader.class.getName()).log(Level.SEVERE, "No symloader found.");
        return super.getLoadChoices();
    }

    @Override
    public boolean isResidueLoader() {
        init();
        if (symL != null) {
            return symL.isResidueLoader();
        }

        return isResidueLoader;
    }

    public static String detemineFriendlyName(URI uri) {
        String uriString = uri.toASCIIString().toLowerCase();
        String unzippedStreamName = GeneralUtils.stripEndings(uriString);
        String ext = GeneralUtils.getExtension(unzippedStreamName);

        String unzippedName = GeneralUtils.getUnzippedName(uri.toString());
        String strippedName = unzippedName.substring(unzippedName.lastIndexOf('/') + 1);
        String friendlyName = strippedName.substring(0, strippedName.toLowerCase().indexOf(ext));
        return friendlyName;
    }

    public Map<String, List<? extends SeqSymmetry>> loadFeatures(final SeqSpan overlapSpan, final DataSet dataSet)
            throws OutOfMemoryError, Exception {
        try {
            init();
            dataSet.addLoadingSpanRequest(overlapSpan);	// this span is requested to be loaded.

            if (this.symL != null && this.symL.isResidueLoader()) {
                loadResiduesThread(dataSet, overlapSpan);
                return Collections.<String, List<? extends SeqSymmetry>>emptyMap();
            } else {
                return loadSymmetriesThread(dataSet, overlapSpan);
            }
        } catch (Exception ex) {
            logException(ex);
            throw ex;
        } finally {
            if (Thread.currentThread().isInterrupted()) {
                dataSet.removeCurrentRequest(overlapSpan);
            } else {
                dataSet.addLoadedSpanRequest(overlapSpan);
            }
        }
    }

    protected Map<String, List<? extends SeqSymmetry>> loadSymmetriesThread(final DataSet dataSet, final SeqSpan overlapSpan)
            throws OutOfMemoryError, Exception {
        //Do not not anything in case of genome. Just refresh.
        if (Constants.GENOME_SEQ_ID.equals(overlapSpan.getBioSeq().getId())) {
            return Collections.<String, List<? extends SeqSymmetry>>emptyMap();
        }
        return loadAndAddSymmetries(dataSet, overlapSpan);
    }

    /**
     * For optimized file format load symmetries and add them.
     *
     * @param dataSet
     * @param span
     * @throws IOException
     * @throws OutOfMemoryError
     */
    private Map<String, List<? extends SeqSymmetry>> loadAndAddSymmetries(DataSet dataSet, final SeqSpan span)
            throws Exception, OutOfMemoryError {

        if (this.symL != null && !this.symL.getChromosomeList().contains(span.getBioSeq())) {
            return Collections.<String, List<? extends SeqSymmetry>>emptyMap();
        }

        setStyle(dataSet);

        // short-circuit if there's a failure... which may not even be signaled in the code
        if (!this.isInitialized) {
            this.init();
        }

        List<? extends SeqSymmetry> results = getRegion(span);

        if (Thread.currentThread().isInterrupted()) {
            return Collections.<String, List<? extends SeqSymmetry>>emptyMap();
        }

        if (results != null) {
            return addSymmtries(span, results, dataSet);
        }

        return Collections.<String, List<? extends SeqSymmetry>>emptyMap();
    }

    public void loadAndAddAllSymmetries(final DataSet dataSet)
            throws OutOfMemoryError {

        setStyle(dataSet);

        // short-circuit if there's a failure... which may not even be signaled in the code
        if (!this.isInitialized) {
            this.init();
        }

        List<? extends SeqSymmetry> results = this.getGenome();

        if (Thread.currentThread().isInterrupted() || results == null) {
            dataSet.setLoadStrategy(LoadStrategy.NO_LOAD); //Change the loadStrategy for this type of files.
            //LoadModeTable.updateVirtualFeatureList();
            results = null;
            return;
        }
        addAllSymmetries(dataSet, results);
    }

    protected void addAllSymmetries(final DataSet dataSet, List<? extends SeqSymmetry> results)
            throws OutOfMemoryError {

        //For a file format that adds SeqSymmetries from
        //within the parser handle them here.
        Map<BioSeq, List<SeqSymmetry>> seq_syms = SymLoader.splitResultsBySeqs(results);
        SeqSpan span = null;
        BioSeq seq = null;

        for (Entry<BioSeq, List<SeqSymmetry>> seq_sym : seq_syms.entrySet()) {
            seq = seq_sym.getKey();
            span = new SimpleSeqSpan(seq.getMin(), seq.getMax() - 1, seq);
            addSymmtries(span, seq_sym.getValue(), dataSet);
            dataSet.addLoadedSpanRequest(span); // this span is now considered loaded.
        }

    }

    private void setStyle(DataSet dataSet) {
        // TODO - not necessarily unique, since the same file can be loaded to multiple tracks for different organisms
        ITrackStyleExtended style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(this.uri.toString(), featureName, extension, dataSet.getProperties());
        style.setFeature(dataSet);

        // TODO - probably not necessary
        //style = DefaultStateProvider.getGlobalStateProvider().getAnnotStyle(featureName, featureName, extension, dataSet.dataSetProps);
        //style.setFeature(dataSet);
    }

    protected Map<String, List<? extends SeqSymmetry>> addSymmtries(final SeqSpan span, List<? extends SeqSymmetry> results, DataSet dataSet) {
        results = SeqUtils.filterForOverlappingSymmetries(span, results);
        return SymLoader.splitFilterAndAddAnnotation(span, results, dataSet);
    }

    private void loadResiduesThread(final DataSet dataSet, final SeqSpan span) throws Exception {
        String results = QuickLoadSymLoader.this.getRegionResidues(span);
        if (results != null && !results.isEmpty()) {
            // TODO: make this more general.  Since we can't currently optimize all residue requests,
            // we are simply considering the span loaded if it loads the entire chromosome
            // Since all request are optimized considering every request to be loaded -- HV 07/26/11
            //if (span.getMin() <= span.getBioSeq().getMin() && span.getMax() >= span.getBioSeq().getMax()) {
            //	dataSet.addLoadedSpanRequest(span);	// this span is now considered loaded.
            //}
            BioSeqUtils.addResiduesToComposition(span.getBioSeq(), results, span);
        }
    }

    /**
     * Get list of chromosomes used in the file/uri. Especially useful when
     * loading a file into an "unknown" genome
     *
     * @return List of chromosomes
     */
    @Override
    public List<BioSeq> getChromosomeList() throws Exception {
        init();
        if (this.symL != null) {
            return this.symL.getChromosomeList();
        }
        return super.getChromosomeList();
    }

    /**
     * Only used for non-symloader files.
     */
    @Override
    public List<? extends SeqSymmetry> getGenome() {
        init();
        try {
            BufferedInputStream bis = null;
            try {
                // This will also unzip the stream if necessary
                bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(this.uri);
                return symL.parse(bis, false);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(QuickLoadSymLoader.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                GeneralUtils.safeClose(bis);
            }
            return null;
        } catch (Exception ex) {
            logException(ex);
            return null;
        }
    }

    @Override
    public List<? extends SeqSymmetry> getRegion(SeqSpan span) throws Exception {
        init();
        if (this.symL != null) {
            return this.symL.getRegion(span);
        }
        return super.getRegion(span);
    }

    @Override
    public String getRegionResidues(SeqSpan span) throws Exception {
        init();
        if (this.symL != null && this.symL.isResidueLoader()) {
            return this.symL.getRegionResidues(span);
        }
        Logger.getLogger(QuickLoadSymLoader.class.getName()).log(
                Level.SEVERE, "Residue loading was called with a non-residue format.");
        return "";
    }

    @Override
    public boolean isMultiThreadOK() {
        if (symL != null) {
            return symL.isMultiThreadOK();
        }
        return false;
    }

    public SymLoader getSymLoader() {
        init();
        return symL;
    }

    public void logException(Exception ex) {
        String loggerName = QuickLoadSymLoader.class.getName();
        Level level = Level.SEVERE;

        if (symL != null) {
            loggerName = symL.getClass().getName();
        }

        if (ex instanceof RuntimeException) {
            level = Level.WARNING;
        }

        Logger.getLogger(loggerName).log(level, ex.getMessage(), ex);
    }
}
