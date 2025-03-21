package com.affymetrix.genometry.symloader;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.filter.SymmetryFilterI;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.operator.Operator;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.quickload.QuickLoadSymLoader;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.symmetry.MutableSeqSymmetry;
import com.affymetrix.genometry.symmetry.RootSeqSymmetry;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.SimpleMutableSeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.TypeContainerAnnot;
import com.affymetrix.genometry.util.LoadUtils;
import com.affymetrix.genometry.util.SeqUtils;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hiralv
 */
public class Delegate extends QuickLoadSymLoader {

    public static final String EXT = "igbtrack";
    private static final List<LoadUtils.LoadStrategy> defaultStrategyList = new ArrayList<>();

    static {
        defaultStrategyList.add(LoadUtils.LoadStrategy.NO_LOAD);
        defaultStrategyList.add(LoadUtils.LoadStrategy.VISIBLE);
    }

    private Operator operator;
    private List<DelegateParent> dps;
    private final List<LoadUtils.LoadStrategy> strategyList;

    public Delegate(URI uri, Optional<URI> indexUri, String featureName, GenomeVersion genomeVersion,
            Operator operator, List<DelegateParent> dps) {
        super(uri, indexUri, featureName, genomeVersion);
        this.operator = operator;
        this.dps = dps;
        strategyList = new ArrayList<>();
        if (dps != null) {
            this.extension = EXT;
            strategyList.addAll(defaultStrategyList);
        } else {
            this.extension = "";
            strategyList.add(LoadUtils.LoadStrategy.NO_LOAD);
        }
    }

    @Override
    protected void init() {
        this.symL = null;
        this.isInitialized = true;
    }

    /**
     * @return possible strategies to load this URI.
     */
    @Override
    public List<LoadUtils.LoadStrategy> getLoadChoices() {
        return strategyList;
    }

    /**
     * Get list of chromosomes used in the file/uri. Especially useful when
     * loading a file into an "unknown" genome
     *
     * @return List of chromosomes
     */
    @Override
    public List<BioSeq> getChromosomeList() throws Exception {
        return this.getAnnotatedSeqGroup().getSeqList();
    }

    /**
     * Get a region of the chromosome.
     *
     * @param overlapSpan - span of overlap
     * @return List of symmetries satisfying requirements
     */
    @Override
    public List<? extends SeqSymmetry> getRegion(SeqSpan overlapSpan) throws Exception {
        List<SeqSymmetry> result = new ArrayList<>();
        List<SeqSymmetry> syms = new ArrayList<>();

        if (dps.isEmpty()) {
            return result;
        }

        for (DelegateParent dp : dps) {
            if (overlapSpan.getBioSeq().getAnnotation(dp.name) == null) {
                return result;
            }

            syms.add(dp.getSeqSymmetry(overlapSpan.getBioSeq()));
        }

        SymWithProps sym = (SymWithProps) operator.operate(overlapSpan.getBioSeq(), syms);
        if (sym == null) {
            return result;
        }

        if (!(sym instanceof RootSeqSymmetry)) {
            if (operator.getOutputCategory() == FileTypeCategory.Alignment
                    || operator.getOutputCategory() == FileTypeCategory.Annotation) {
                TypeContainerAnnot container = new TypeContainerAnnot(uri.toString(), EXT, false);
                for (int i = 0; i < sym.getChildCount(); i++) {
                    container.addChild(sym.getChild(i));
                }
                sym = container;
            } else {
                Logger.getLogger(Delegate.class.getName()).log(Level.SEVERE,
                        "{0} does not output rootseqsymmetry and output format is {1}",
                        new Object[]{operator.getName(), operator.getOutputCategory()});
            }
        } else {
            sym.setProperty("meth", uri.toString());
        }

        sym.setProperty("method", uri.toString());
        sym.setProperty("id", uri.toString());
        result.add(sym);
        return result;
    }

    @Override
    public Map<String, List<? extends SeqSymmetry>> loadFeatures(final SeqSpan overlapSpan, final DataSet feature)
            throws OutOfMemoryError, Exception {
        boolean notUpdatable = false;

        if (dps == null || dps.isEmpty()) {
            return Collections.<String, List<? extends SeqSymmetry>>emptyMap();
        }

        List<SeqSymmetry> requests = new ArrayList<>();
        for (DelegateParent dp : dps) {
            if (!dp.feature.isVisible()) {
                notUpdatable = true;
                Thread.currentThread().interrupt();
                break;
            }

            while (dp.feature.isLoading(overlapSpan) && !dp.feature.isLoaded(overlapSpan)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Delegate.class.getName()).log(Level.WARNING, "Thread interruped cancelling loading");
                    return Collections.<String, List<? extends SeqSymmetry>>emptyMap();
                }
            }
            requests.add(dp.feature.getRequestSym());
        }

        if (notUpdatable) {
            dps.forEach(Delegate.DelegateParent::clear);
            dps = null;
            operator = null;
            strategyList.remove(LoadUtils.LoadStrategy.VISIBLE);
            feature.setLoadStrategy(LoadUtils.LoadStrategy.NO_LOAD);
            return Collections.<String, List<? extends SeqSymmetry>>emptyMap();
        }

        MutableSeqSymmetry query_sym = new SimpleMutableSeqSymmetry();
        query_sym.addSpan(overlapSpan);
        requests.add(query_sym);

        Map<String, List<? extends SeqSymmetry>> loaded = new HashMap<>();
        List<SeqSpan> operlapSpans = new ArrayList<>();
        SeqUtils.convertSymToSpanList(SeqUtils.intersection(requests, overlapSpan.getBioSeq()), operlapSpans);

        for (SeqSpan span : operlapSpans) {
            loaded.putAll(super.loadFeatures(span, feature));

            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }

        return loaded;
    }

    @Override
    protected Map<String, List<? extends SeqSymmetry>> addSymmtries(
            final SeqSpan span, List<? extends SeqSymmetry> results, DataSet feature) {
        Map<String, List<? extends SeqSymmetry>> added = new HashMap<>();
        added.put(uri.toString(), results);

        if (results == null || results.isEmpty()) {
            return Collections.<String, List<? extends SeqSymmetry>>emptyMap();
        }

        if (results.get(0) instanceof GraphSym) {
            GraphSym graphSym = (GraphSym) results.get(0);
            if (results.size() == 1 && graphSym.isSpecialGraph()) {
                addWholeGraph(graphSym, feature);
            } else {
                // We assume that if there are any GraphSyms, then we're dealing with a list of GraphSyms.
                //grafs.add((GraphSym)feat);
//					if (feat instanceof GraphSym) {
//						GraphSymUtils.addChildGraph((GraphSym) feat, uri.toString(), ((GraphSym) feat).getGraphName(), uri.toString(), span);
//						feature.addMethod(uri.toString());
//					}
                results.stream().filter(feat -> feat instanceof GraphSym).forEach(feat -> {
                    addWholeGraph((GraphSym) feat, feature);
                });
            }

            return added;
        }

        BioSeq seq = span.getBioSeq();
        if (seq.getAnnotation(uri.toString()) != null) {
            //TODO: Check for other top level glyphs.
            TypeContainerAnnot sym = (TypeContainerAnnot) seq.getAnnotation(uri.toString());
            sym.clear();
        }

        results.forEach(seq::addAnnotation);

        feature.setMethod(uri.toString());

        return added;
    }

    private void addWholeGraph(GraphSym graphSym, DataSet feature) {
        BioSeq seq = graphSym.getGraphSeq();

        // Remove previous graph
        SeqSymmetry previousGraph = seq.getAnnotation(uri.toString());
        if (previousGraph != null) {
            seq.removeAnnotation(previousGraph);
        }

        // Add full length span
        graphSym.removeSpan(graphSym.getSpan(seq));
        graphSym.addSpan(new SimpleSeqSpan(0, seq.getLength(), seq));

        seq.addAnnotation(graphSym);
        feature.setMethod(uri.toString());
        graphSym.getGraphName(); //Temp fix to setGraphTier true in TrackStyle
    }

    public static class DelegateParent {

        String name;
        Boolean direction;
        DataSet feature;
        SymmetryFilterI filter;

        public DelegateParent(String name, Boolean direction, DataSet feature, SymmetryFilterI filter) {
            this.name = name;
            this.direction = direction;
            this.feature = feature;
            this.filter = filter;
        }

        SeqSymmetry getSeqSymmetry(BioSeq seq) {
            return getChildren(seq);
        }

        private SeqSymmetry getChildren(BioSeq seq) {
            SeqSymmetry parentSym = seq.getAnnotation(name);
            if (direction == null && filter == null) {
                return parentSym;
            }

            TypeContainerAnnot tca = new TypeContainerAnnot(name);

            SeqSymmetry sym;
            SeqSpan span;
            for (int i = 0; i < parentSym.getChildCount(); i++) {
                sym = parentSym.getChild(i);

                if (filter != null && !filter.filterSymmetry(seq, sym)) {
                    continue;
                }

                if (direction != null) {
                    span = sym.getSpan(seq);
                    if (span == null || span.getLength() == 0 || span.isForward() != direction) {
                        continue;
                    }
                }

                tca.addChild(sym);
            }

            return tca;
        }

        void clear() {
            name = null;
            feature = null;
        }
    }
}
