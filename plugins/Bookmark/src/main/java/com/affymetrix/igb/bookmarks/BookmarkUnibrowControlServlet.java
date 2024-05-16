/**
 * Copyright (c) 2001-2007 Affymetrix, Inc.
 * <p>
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy of the license must be included with
 * any distribution of this source code. Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 * <p>
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.bookmarks;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.LocalDataProvider;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.data.DataProvider;
import com.affymetrix.genometry.general.DataContainer;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.general.DataSetUtils;
import com.affymetrix.genometry.span.SimpleMutableSeqSpan;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.style.ITrackStyleExtended;
import com.affymetrix.genometry.style.SimpleTrackStyle;
import com.affymetrix.genometry.thread.CThreadHolder;
import com.affymetrix.genometry.thread.CThreadWorker;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometry.util.ModalUtils;
import com.affymetrix.genometry.util.ThreadUtils;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.bookmarks.model.Bookmark;
import com.affymetrix.igb.bookmarks.model.Bookmark.SYM;
import com.affymetrix.igb.shared.OpenURIAction;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.primitives.Ints;
import org.lorainelab.igb.genoviz.extensions.SeqMapViewI;
import org.lorainelab.igb.services.IgbService;
import org.lorainelab.igb.synonymlookup.services.GenomeVersionSynonymLookup;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

/**
 * A way of allowing IGB to be controlled via hyperlinks. (This used to be an implementation of HttpServlet, but it
 * isn't now.)
 * <pre>
 *  Can specify:
 *      genome version
 *      chromosome
 *      start of region in view
 *      end of region in view
 *  and bring up corect version, chromosome, and region with (at least)
 *      annotations that can be loaded via QuickLoaderView
 *  If the currently loaded genome doesn't match the one requested, might
 *      ask the user before switching.
 *
 * @version $Id: UnibrowControlServlet.java 7505 2011-02-10 20:27:35Z hiralv $
 * </pre>
 */
public final class BookmarkUnibrowControlServlet {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BookmarkUnibrowControlServlet.class);
    private static GenomeVersionSynonymLookup LOOKUP;
    private static BundleContext bundleContext;

    private BookmarkUnibrowControlServlet() {
        Bundle bundle = FrameworkUtil.getBundle(BookmarkUnibrowControlServlet.class);
        if (bundle != null) {
            bundleContext = bundle.getBundleContext();
            ServiceReference<GenomeVersionSynonymLookup> serviceReference = bundleContext.getServiceReference(GenomeVersionSynonymLookup.class);
            LOOKUP = bundleContext.getService(serviceReference);
        }
    }

    public static BookmarkUnibrowControlServlet getInstance() {
        return BookmarkUnibrowControlServletHolder.INSTANCE;
    }

    private static class BookmarkUnibrowControlServletHolder {

        private static final BookmarkUnibrowControlServlet INSTANCE = new BookmarkUnibrowControlServlet();
    }

    private static final GenometryModel gmodel = GenometryModel.getInstance();
    private static final Pattern query_splitter = Pattern.compile("[;\\&]");

    /**
     * Loads a bookmark.
     *
     * @param igbService
     * @param parameters Must be a Map where the only values are String and String[] objects. For example, this could be
     *                   the Map returned by {@link javax.servlet.ServletRequest#getParameterMap()}.
     */
    public void goToBookmark(final IgbService igbService, final ListMultimap<String, String> parameters, final boolean isGalaxyBookmark) {
        String batchFileStr = getFirstValueEntry(parameters, IgbService.SCRIPTFILETAG);
        if (StringUtils.isNotBlank(batchFileStr)) {
            igbService.doActions(batchFileStr);
            return;
        }

        String threadDescription = "Loading Bookmark Data";
        if (isGalaxyBookmark) {
            threadDescription = "Loading Your Galaxy Data";
        }
        if (Boolean.valueOf(getFirstValueEntry(parameters, Bookmark.CYVERSE_DATA))) {
            threadDescription = "Loading Your BioViz Connect Data";
        }

        CThreadWorker<Object, Void> worker = new CThreadWorker<Object, Void>(threadDescription) {

            @Override
            protected Object runInBackground() {
                try {
                    String seqid = getFirstValueEntry(parameters, Bookmark.SEQID);
                    String version = getFirstValueEntry(parameters, Bookmark.VERSION);
                    String start_param = getFirstValueEntry(parameters, Bookmark.START);
                    String end_param = getFirstValueEntry(parameters, Bookmark.END);
//				String comment_param = getStringParameter(parameters, Bookmark.COMMENT);
                    String select_start_param = getFirstValueEntry(parameters, Bookmark.SELECTSTART);
                    String select_end_param = getFirstValueEntry(parameters, Bookmark.SELECTEND);
                    boolean loadResidue = Boolean.valueOf(getFirstValueEntry(parameters, Bookmark.LOADRESIDUES));
                    boolean cyverseData = Boolean.valueOf(getFirstValueEntry(parameters, Bookmark.CYVERSE_DATA));
                    // For historical reasons, there are two ways of specifying graphs in a bookmark
                    // Eventually, they should be treated more similarly, but for now some
                    // differences remain
                    // parameter "graph_file" can be handled by goToBookmark()
                    //    Does not check whether the file was previously loaded
                    //    Loads in GUI-friendly thread
                    //    Must be a file name, not a generic URL
                    // parameter "graph_source_url_0", "graph_source_url_1", ... is handled elsewhere
                    //    Checks to avoid double-loading of files
                    //    Loading can freeze the GUI
                    //    Can be any URL, not just a file
                    boolean has_properties = (parameters.get(SYM.FEATURE_URL + "0") != null);
                    boolean loaddata = true;
                    //boolean loaddas2data = true;
                    int start = 0;
                    int end = 0;

                    //missing seqid or start or end? Attempt to set to current view

                    if (missingString(new String[]{seqid, start_param, end_param})) {
                        boolean pickOne = false;
                        //get GenomeVersion for bookmark
                        String preferredVersionName = LOOKUP.getPreferredName(version);
                        GenomeVersion genomeVersion = gmodel.getSeqGroup(preferredVersionName);
                        if (genomeVersion != null) {
                            //same genome version as that in view?
                            SeqMapViewI currentSeqMap = igbService.getSeqMapView();
                            if (currentSeqMap != null) {
                                //get visible span
                                SeqSpan currentSpan = currentSeqMap.getVisibleSpan();
                                if (currentSpan != null && currentSpan.getBioSeq() != null) {
                                    //check genome version, if same then set coordinates
                                    GenomeVersion currentGroup = currentSpan.getBioSeq().getGenomeVersion();
                                    if (!isGalaxyBookmark && !cyverseData && (currentGroup != null && currentGroup.equals(genomeVersion))) {
                                        start = currentSpan.getStart();
                                        end = currentSpan.getEnd();
                                        seqid = currentSpan.getBioSeq().getId();
                                    } else {
                                        pickOne = true;
                                    }
                                } else {
                                    pickOne = true;
                                }
                            } //pick first chromosome and 1M span
                            else {
                                pickOne = true;
                            }
                        }
                        //pick something, only works if version was loaded.
                        if (pickOne && !isGalaxyBookmark && !cyverseData && genomeVersion != null) {
                            BioSeq bs = genomeVersion.getSeq(0);
                            if (bs != null) {
                                int len = bs.getLength();
                                seqid = bs.getId();
                                start = len / 3 - 500000;
                                if (start < 0) {
                                    start = 0;
                                }
                                end = start + 500000;
                                if (end > len) {
                                    end = len - 1;
                                }
                            }
                        }
                    }

                    //attempt to parse from bookmark?
                    if (start == 0 && end == 0) {
                        List<Integer> intValues = initializeIntValues(start_param, end_param, select_start_param, select_end_param);
                        start = intValues.get(0);
                        end = intValues.get(1);
                    }

                    List<String> server_urls = parameters.get(Bookmark.SERVER_URL);
                    List<String> query_urls = parameters.get(Bookmark.QUERY_URL);
                    List<DataProvider> dataProivders = null;
                    if (server_urls.isEmpty() || query_urls.isEmpty() || server_urls.size() != query_urls.size()) {
                        loaddata = false;
                    } else {
                        dataProivders = loadServers(igbService, server_urls, version);
                    }

                    final BioSeq seq;
                    seq = goToBookmark(igbService, seqid, version, start, end).orNull();
                        
                    if (seq == null) {
                        if (isGalaxyBookmark) {
                            loadUnknownData(parameters, igbService);
                            return null;
                        } else if (cyverseData) {
                            loadUnknownData(parameters, igbService);
                            BookmarkController.forceStyleChange(parameters);
                            return null;
                        }
                        return null;

                    }

                    if (loaddata) {
                        // TODO: Investigate edge case at max
                        if (seq.getMin() == start && seq.getMax() == end) {
                            end -= 1;
                        }
                        if (dataProivders != null && dataProivders.isEmpty()) {
                            String preferredVersionName = LOOKUP.getPreferredName(version);
                            GenomeVersion genomeVersion = gmodel.getSeqGroup(preferredVersionName);
                            directlyLoadUrls(genomeVersion, parameters, igbService);
                            if(cyverseData){
                                BookmarkController.forceStyleChange(parameters);
                            }
                            return null;
                        }
                        // IGBF-1364: add parameters argument, contains parameters from bookmark URL
                        List<DataSet> gFeatures = loadData(igbService, gmodel.getSelectedGenomeVersion(), dataProivders, query_urls, start, end, parameters);
                                                         
                        if (has_properties) {
                            List<String> graph_urls = getGraphUrls(parameters);
                            final Map<String, ITrackStyleExtended> combos = new HashMap<>();

                            for (int i = 0; !parameters.get(SYM.FEATURE_URL.toString() + i).isEmpty(); i++) {
                                String combo_name = BookmarkUnibrowControlServlet.getInstance().getFirstValueEntry(parameters, Bookmark.GRAPH.COMBO.toString() + i);
                                if (combo_name != null) {
                                    ITrackStyleExtended combo_style = combos.get(combo_name);
                                    if (combo_style == null) {
                                        combo_style = new SimpleTrackStyle("Joined Graphs", true);
                                        combo_style.setTrackName("Joined Graphs");
                                        combo_style.setExpandable(true);
                                        //combo_style.setCollapsed(true);
                                        //combo_style.setLabelBackground(igbService.getDefaultBackgroundColor());
                                        combo_style.setBackground(igbService.getDefaultBackgroundColor());
                                        //combo_style.setLabelForeground(igbService.getDefaultForegroundColor());
                                        combo_style.setForeground(igbService.getDefaultForegroundColor());
                                        combo_style.setTrackNameSize(igbService.getDefaultTrackSize());
                                        combos.put(combo_name, combo_style);
                                    }
                                }
                            }

                            gFeatures.stream().filter(feature -> feature != null && graph_urls.contains(feature.getURI().toString())).forEach(feature -> {
                                ThreadUtils.getPrimaryExecutor(feature).execute(() -> BookmarkController.applyProperties(igbService, seq, parameters, feature, combos));
                            });
                        }
                    }

                    String selectParam = getFirstValueEntry(parameters, "select");
                    if (selectParam != null) {
                        igbService.performSelection(selectParam);
                    }

                    if (loadResidue) {
                        final java.util.Optional<BioSeq> selectedSeq = gmodel.getSelectedSeq();
                        if (selectedSeq.isPresent()) {
                            BioSeq vseq = selectedSeq.get();
                            SeqSpan span = new SimpleMutableSeqSpan(start, end, vseq);
                            igbService.loadResidues(span, true);
                        }
                    }
                } catch (Throwable t) {
                    //Catch all to ensure thread does not continue indefinitely
                    logger.error("Error while loading bookmark.", t);
                    return null;
                }

                return null;
            }

            @Override
            protected void finished() {
            }
        };
        CThreadHolder.getInstance().execute(parameters, worker);
    }

    /**
     * Checks for nulls or Strings with zero length.
     *
     * @param params
     * @return
     */
    public static boolean missingString(String[] params) {
        for (String s : params) {
            if (StringUtils.isBlank(s)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getGraphUrls(ListMultimap<String, String> multimap) {
        List<String> graph_paths = new ArrayList<>();
        for (int i = 0; !multimap.get(SYM.FEATURE_URL.toString() + i).isEmpty(); i++) {
            graph_paths.add(getFirstValueEntry(multimap, SYM.FEATURE_URL.toString() + i));
        }
        return graph_paths;
    }

    private List<DataSet> loadData(final IgbService igbService, final GenomeVersion genomeVersion, final List<DataProvider> dataProivders, final List<String> query_urls, int start, int end,
                                   final ListMultimap<String, String> parameters) {
        List<DataSet> gFeatures = new ArrayList<>();
        int i = 0;
        for (String queryUrl : query_urls) {
            gFeatures.add(getFeature(igbService, genomeVersion, dataProivders.get(i), queryUrl));
            i++;
        }
        boolean show_message = false;
        for (DataSet gFeature : gFeatures) {
            if (gFeature != null) {
                loadFeature(igbService, gFeature, start, end);
                if (!show_message && (gFeature.getLoadStrategy() == LoadStrategy.VISIBLE)) {
                    gFeature.setVisible();
                    show_message = true;
                }
            }
        }
        igbService.updateGeneralLoadView();
        // IGBF-1364: only show "zoom in click load data" message if bookmark URL did not specify a
        // a region. Not necessary to zoom in at that point. Also, clicking bookmark URLs triggers data loading.
        if (show_message && parameters.get(Bookmark.START).isEmpty() && parameters.get(Bookmark.END).isEmpty()) {
            ModalUtils.infoPanel(DataSet.LOAD_WARNING_MESSAGE,
                    DataSet.show_how_to_load, DataSet.default_show_how_to_load);
        }
        return gFeatures;
    }

    private void loadChromosomesFor(final IgbService igbService, final GenomeVersion genomeVersion, final List<DataProvider> dataProivders, final List<String> query_urls) {
        List<DataSet> gFeatures = getFeatures(igbService, genomeVersion, dataProivders, query_urls);
        gFeatures.stream().filter(gFeature -> gFeature != null).forEach(igbService::loadChromosomes);
    }

    private List<DataSet> getFeatures(final IgbService igbService, final GenomeVersion genomeVersion, final List<DataProvider> dataProivders, final List<String> query_urls) {
        List<DataSet> gFeatures = new ArrayList<>();

        boolean show_message = false;
        int i = 0;
        for (String query_url : query_urls) {
            DataSet gFeature = getFeature(igbService, genomeVersion, dataProivders.get(i), query_url);
            gFeatures.add(gFeature);
            if (gFeature != null) {
                gFeature.setVisible();
                gFeature.setPreferredLoadStrategy(LoadStrategy.VISIBLE);
                if (gFeature.getLoadStrategy() == LoadStrategy.VISIBLE /*||
                         gFeatures[i].getLoadStrategy() == LoadStrategy.CHROMOSOME*/) {
                    show_message = true;
                }
            }
            i++;
        }

        igbService.updateGeneralLoadView();

        // Show message on how to load
        if (show_message) {
            ModalUtils.infoPanel(DataSet.LOAD_WARNING_MESSAGE,
                    DataSet.show_how_to_load, DataSet.default_show_how_to_load);
        }

        return gFeatures;
    }

    private DataSet getFeature(final IgbService igbService, final GenomeVersion genomeVersion, final DataProvider dataProvider, final String queryUrl) {
        if (dataProvider == null) {
            return null;
        }
        java.util.Optional<DataSet> dataSet = java.util.Optional.empty();
        if (dataProvider instanceof LocalDataProvider) {
            /*~kiran:IGBF-1287: Adapting to the new method signature by passing null for trackLabel*/
            directlyLoadFile(queryUrl, null, igbService, genomeVersion);
            java.util.Optional<DataContainer> dataContainer = genomeVersion.getAvailableDataContainers()
                    .stream()
                    .filter(dc -> dc.getDataProvider() instanceof LocalDataProvider)
                    .findFirst();
            if (dataContainer.isPresent()) {
                dataSet = dataContainer.get().getDataSets().stream()
                        .filter(ds -> ds.getURI().toString().equals(queryUrl))
                        .findFirst();
            }
        } else {
            dataSet = igbService.getDataSet(genomeVersion, dataProvider, queryUrl, false);
        }
        if (!dataSet.isPresent()) {
            Logger.getLogger(GeneralUtils.class.getName()).log(
                    Level.SEVERE, "Couldn''t find feature for bookmark url {}", queryUrl);
            return null;
        }

        return dataSet.get();
    }

    private void loadFeature(IgbService igbService, DataSet dataSet, int start, int end) {
        if (dataSet == null) {
            logger.error("Unable to load null dataSet");
            return;
        }
        final java.util.Optional<BioSeq> selectedSeq = gmodel.getSelectedSeq();
        if (selectedSeq.isPresent()) {
            BioSeq seq = selectedSeq.get();
            //a bit of a hack to force track creation since with no overlap there is currently no track being created.
            if (end == 0) {
                end = 1;
            }
            SeqSpan overlap = new SimpleSeqSpan(start, end, seq);
            dataSet.setVisible();
            dataSet.setPreferredLoadStrategy(LoadStrategy.VISIBLE);
            if (dataSet.getLoadStrategy() != LoadStrategy.VISIBLE) {
                overlap = new SimpleSeqSpan(seq.getMin(), seq.getMax(), seq);
            }
            igbService.loadAndDisplaySpan(overlap, dataSet);
        }
    }

    private List<DataProvider> loadServers(IgbService igbService, List<String> server_urls, String version) {
        return server_urls.stream().map((String server_url) -> {
            if (server_url.isEmpty()) {
                String preferredVersionName = LOOKUP.getPreferredName(version);
                GenomeVersion genomeVersion = gmodel.getSeqGroup(preferredVersionName);
                return java.util.Optional.<DataProvider>of((DataProvider) genomeVersion.getLocalDataSetProvider());
            } else {
                return igbService.loadServer(server_url);
            }
        }).filter(dataProvider -> dataProvider.isPresent())
                .map((dataProvider) -> dataProvider.get()).collect(Collectors.toList());
    }

    private void loadDataFromURLs(final IgbService igbService, final String[] data_urls, final String[] extensions, final String[] tier_names) {
        try {
            if (data_urls != null && data_urls.length != 0) {
                URL[] urls = new URL[data_urls.length];
                for (int i = 0; i < data_urls.length; i++) {
                    urls[i] = new URL(data_urls[i]);
                }
                final UrlLoaderThread t = new UrlLoaderThread(igbService, urls, extensions, tier_names);
                t.runEventually();
                t.join();
            }
        } catch (MalformedURLException e) {
            ErrorHandler.errorPanel("Error loading bookmark\nData URL malformed\n", e);
        } catch (InterruptedException ex) {
        }
    }

    private List<Integer> initializeIntValues(String start_param, String end_param,
                                              String select_start_param, String select_end_param) {

        Integer start = 0;
        Integer end = 0;
        if (StringUtils.isNotBlank(start_param)) {
            start = Ints.tryParse(start_param);
            if (start == null) {
                start = 0;
            }
        }
        if (StringUtils.isNotBlank(end_param)) {
            end = Ints.tryParse(end_param);
            if (end == null) {
                end = 0;
            }
        }
        Integer selstart = -1;
        Integer selend = -1;
        if (StringUtils.isNotBlank(select_start_param) && StringUtils.isNotBlank(select_end_param)) {
            selstart = Ints.tryParse(select_start_param);
            selend = Ints.tryParse(select_end_param);
            if (selstart == null) {
                selstart = -1;
            }
            if (selend == null) {
                selend = -1;
            }
        }
        ImmutableList<Integer> intValues = ImmutableList.<Integer>builder().add(start).add(end).add(selstart).add(selend).build();
        return intValues;
    }

    /**
     * Loads the sequence and goes to the specified location. If version doesn't match the currently-loaded version,
     * asks the user if it is ok to proceed. NOTE: This schedules events on the AWT event queue. If you want to make
     * sure that everything has finished before you do something else, then you have to schedule that something else to
     * occur on the AWT event queue.
     *
     * @param graph_files it is ok for this parameter to be null.
     * @return true indicates that the action succeeded
     */
    private Optional<BioSeq> goToBookmark(IgbService igbService, String seqid, String version, int start, int end) {
        GenomeVersion book_group = null;
        try {
            String preferredVersionName = LOOKUP.getPreferredName(version);
            book_group = igbService.determineAndSetGroup(preferredVersionName).orElse(null);
        } catch (Throwable ex) {
            logger.error("info", ex);
        }

        if (book_group == null) {
            return Optional.absent();
        }

        final BioSeq book_seq = determineSeq(seqid, book_group);
        if (book_seq == null) {
            ErrorHandler.errorPanel("No seqid", "The bookmark did not specify a valid seqid: specified '" + seqid + "'");
            return Optional.absent();
        } else // gmodel.setSelectedSeq() should trigger a gviewer.setAnnotatedSeq() since
            //     gviewer is registered as a SeqSelectionListener on gmodel
            if (!gmodel.getSelectedSeq().isPresent() || book_seq != gmodel.getSelectedSeq().orElse(null)) {
                gmodel.setSelectedSeq(book_seq);
            }
        igbService.getSeqMapView().setRegion(start, end, book_seq);
        return Optional.fromNullable(book_seq);
    }

    public static BioSeq determineSeq(String seqid, GenomeVersion group) {
        // hopefully setting gmodel's selected seq group above triggered population of seqs
        //   for group if not already populated
        BioSeq book_seq;
        if (StringUtils.isBlank(seqid) || "unknown".equals(seqid)) {
            book_seq = gmodel.getSelectedSeq().orElse(null);
            if (book_seq == null && gmodel.getSelectedGenomeVersion() != null && gmodel.getSelectedGenomeVersion().getSeqCount() > 0) {
                book_seq = gmodel.getSelectedGenomeVersion().getSeq(0);
            }
        } else {
            book_seq = group.getSeq(seqid);
        }
        return book_seq;
    }

    String getFirstValueEntry(ListMultimap<String, String> multimap, String key) {
        if (multimap.get(key).isEmpty()) {
            return null;
        }
        return multimap.get(key).get(0);
    }

    private void directlyLoadUrls(GenomeVersion genomeVersion, final ListMultimap<String, String> parameters, IgbService igbService) {
        List<String> query_urls = parameters.get(Bookmark.QUERY_URL);
        /*~kiran:IGBF-1287: Making use of sym_name for track labels*/
        List<String> trackLabels = parameters.get(SYM.NAME + "0");
        Map<String, String> trackLabelsMap = IntStream.range(0, query_urls.size()).boxed()
                .collect(Collectors.toMap(i -> (trackLabels != null && trackLabels.size() > i) ? trackLabels.get(i) : DataSetUtils.extractNameFromPath(query_urls.get(i)), i -> query_urls.get(i)));
        trackLabelsMap.forEach((trackLabel, urlToLoad) -> {
            directlyLoadFile(urlToLoad, trackLabel, igbService, genomeVersion);
        });

    }

private void directlyLoadFile(String urlToLoad, String trackLabel, IgbService igbService, GenomeVersion genomeVersion) {
        /*~kiran:IGBF-1287: Using filename if trackLabel is null*/
        if (trackLabel == null) {
            trackLabel = DataSetUtils.extractNameFromPath(urlToLoad);
        }
        try {
            igbService.openURI(new URI(urlToLoad), trackLabel, genomeVersion, genomeVersion.getSpeciesName(), false);
        } catch (URISyntaxException ex) {
            logger.error("Invalid bookmark syntax.", ex);
        }
    }

    private void loadUnknownData(final ListMultimap<String, String> parameters, IgbService igbService) {
        List<String> query_urls = parameters.get(Bookmark.QUERY_URL);
        List<String> trackLabels = parameters.get(SYM.NAME + "0");
        //These bookmarks should only contain one url
        if (!query_urls.isEmpty()) {
            try {
                String urlToLoad = query_urls.get(0);
                String speciesName = igbService.getSelectedSpecies();
                String trackLabel = (trackLabels != null && trackLabels.size() > 0) ? trackLabels.get(0) : DataSetUtils.extractNameFromPath(query_urls.get(0));
                GenomeVersion loadGroup = GenometryModel.getInstance().getSelectedGenomeVersion();
                if(StringUtils.isNotEmpty(speciesName) && loadGroup!=null){
                    igbService.openURI(new URI(urlToLoad), trackLabel, loadGroup, speciesName, false);
                }else{
                    GenomeVersion customLoadGroup = OpenURIAction.retrieveSeqGroup("Custom Genome");
                    igbService.openURI(new URI(urlToLoad), trackLabel, customLoadGroup, "Custom Genome", false);
                }
            } catch (URISyntaxException ex) {
                logger.error("Invalid bookmark syntax.", ex);
            }
        }
    }
}
