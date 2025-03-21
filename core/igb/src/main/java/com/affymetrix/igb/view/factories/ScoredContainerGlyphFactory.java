package com.affymetrix.igb.view.factories;

import org.osgi.service.component.annotations.Component;
import cern.colt.list.FloatArrayList;
import cern.colt.list.IntArrayList;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.style.ITrackStyleExtended;
import com.affymetrix.genometry.symmetry.DerivedSeqSymmetry;
import com.affymetrix.genometry.symmetry.IndexedSym;
import com.affymetrix.genometry.symmetry.RootSeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.GraphIntervalSym;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.ScoredContainerSym;
import com.affymetrix.genometry.util.SeqUtils;
import com.google.common.collect.ImmutableSet;
import org.lorainelab.igb.genoviz.extensions.SeqMapViewExtendedI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author hiralv
 */
@Component(name = ScoredContainerGlyphFactory.COMPONENT_NAME, service = {MapTierGlyphFactoryI.class}, immediate = true)
public class ScoredContainerGlyphFactory extends MapTierGlyphFactoryA {

    public static final String COMPONENT_NAME = "ScoredContainerGlyphFactory";
    private static final String[] supportedFormat = {"sin", "egr", "egr.txt", "map", "chp"};

//	private static final boolean DEBUG = false;
    private static final boolean separate_by_strand = true;

    private static final MapTierGlyphFactoryI annotFactory = new AnnotationGlyphFactory();
    private static final MapTierGlyphFactoryI glyphFactory = new GraphGlyphFactory();

    /**
     * Does nothing.
     */
    public void init(Map<String, Object> options) {
    }

    private void displayGraphs(ScoredContainerSym original_container, SeqMapViewExtendedI smv) {
        BioSeq aseq = smv.getAnnotatedSeq();
        if (original_container.getSpan(aseq) == null) {
            return;
        }

        GraphIntervalSym[] the_graph_syms = determineGraphSyms(smv, aseq, original_container);
        for (GraphIntervalSym gis : the_graph_syms) {
            glyphFactory.createGlyphs(gis, gis.getGraphState().getTierStyle(), smv, null);

        }
    }

    private static GraphIntervalSym[] determineGraphSyms(SeqMapViewExtendedI smv, BioSeq aseq, ScoredContainerSym original_container) {
        BioSeq vseq = smv.getViewSeq();
        GenomeVersion seq_group = GenometryModel.getInstance().getSelectedGenomeVersion();
        if (aseq != vseq) {
            DerivedSeqSymmetry derived_sym = SeqUtils.copyToDerived(original_container);
            SeqUtils.transformSymmetry(derived_sym, smv.getTransformPath());
            return makeGraphsFromDerived(derived_sym, seq_group, vseq);
        }
        // aseq == vseq, so no transformation needed
        return makeGraphs(original_container, seq_group);
    }

    private static GraphIntervalSym[] makeGraphs(ScoredContainerSym container, GenomeVersion seq_group) {
        int score_count = container.getScoreCount();
        List<GraphIntervalSym> results = null;
        if (separate_by_strand) {
            results = new ArrayList<>(score_count * 2);
        } else {
            results = new ArrayList<>(score_count);
        }

        for (int i = 0; i < score_count; i++) {
            String score_name = container.getScoreName(i);
            if (separate_by_strand) {
                GraphIntervalSym forward_gsym = container.makeGraphSym(score_name, true, seq_group);
                if (forward_gsym != null) {
                    results.add(forward_gsym);
                }
                GraphIntervalSym reverse_gsym = container.makeGraphSym(score_name, false, seq_group);
                if (reverse_gsym != null) {
                    results.add(reverse_gsym);
                }
            } else {
                GraphIntervalSym gsym = container.makeGraphSym(score_name, seq_group);
                if (gsym != null) {
                    results.add(gsym);
                }
            }
        }
        return results.toArray(new GraphIntervalSym[results.size()]);
    }

    private static GraphIntervalSym[] makeGraphsFromDerived(DerivedSeqSymmetry derived_parent_sym,
            GenomeVersion seq_group, BioSeq seq) {
        ScoredContainerSym original_container = (ScoredContainerSym) derived_parent_sym.getOriginalSymmetry();

        int score_count = original_container.getScoreCount();
        List<GraphIntervalSym> results = null;
        if (separate_by_strand) {
            results = new ArrayList<>(score_count * 2);
        } else {
            results = new ArrayList<>(score_count);
        }

        for (int i = 0; i < score_count; i++) {
            String score_name = original_container.getScoreName(i);
            if (separate_by_strand) {
                GraphIntervalSym forward_gsym = makeGraphSymFromDerived(derived_parent_sym, score_name, seq_group, seq, '+');
                if (forward_gsym != null) {
                    results.add(forward_gsym);
                }
                GraphIntervalSym reverse_gsym = makeGraphSymFromDerived(derived_parent_sym, score_name, seq_group, seq, '-');
                if (reverse_gsym != null) {
                    results.add(reverse_gsym);
                }
            } else {
                GraphIntervalSym gsym = makeGraphSymFromDerived(derived_parent_sym, score_name, seq_group, seq, '.');
                if (gsym != null) {
                    results.add(gsym);
                }
            }
        }

        return results.toArray(new GraphIntervalSym[results.size()]);
    }

    // strands should be one of '+', '-' or '.'
    // name -- should be a score name in the original ScoredContainerSym
    private static GraphIntervalSym makeGraphSymFromDerived(DerivedSeqSymmetry derived_parent, String name,
            GenomeVersion seq_group, BioSeq seq, final char strands) {
        ScoredContainerSym original_container = (ScoredContainerSym) derived_parent.getOriginalSymmetry();

        float[] original_scores = original_container.getScores(name);

        // Simply knowing the correct graph ID is the key to getting the correct
        // graph state, with the accompanying tier style and tier combo style.
        String id = original_container.getGraphID(seq_group, name, strands);

        if (original_scores == null) {
            System.err.println("ScoreContainerSym.makeGraphSym() called, but no scores found for: " + name);
            return null;
        }

        int derived_child_count = derived_parent.getChildCount();
        IntArrayList xcoords = new IntArrayList(derived_child_count);
        IntArrayList wcoords = new IntArrayList(derived_child_count);
        FloatArrayList ycoords = new FloatArrayList(derived_child_count);

        for (int i = 0; i < derived_child_count; i++) {
            Object child = derived_parent.getChild(i);
            if (child instanceof DerivedSeqSymmetry) {
                DerivedSeqSymmetry derived_child = (DerivedSeqSymmetry) derived_parent.getChild(i);
                SeqSpan cspan = derived_child.getSpan(seq);
                if (cspan != null) {
                    if (strands == '.' || (strands == '+' && cspan.isForward())
                            || (strands == '-' && !cspan.isForward())) {
                        xcoords.add(cspan.getMin());
                        wcoords.add(cspan.getLength());
                        IndexedSym original_child = (IndexedSym) derived_child.getOriginalSymmetry();
                        // the index of this child in the original parent symmetry.
                        // it is very possible that original_index==i in all cases,
                        // but I'm not sure of that yet
                        int original_index = original_child.getIndex();
                        ycoords.add(original_scores[original_index]);
                    }
                }
            }
        }
        xcoords.trimToSize();
        wcoords.trimToSize();
        ycoords.trimToSize();
        GraphIntervalSym gsym = null;
        if (!xcoords.isEmpty()) {
            gsym = new GraphIntervalSym(xcoords.elements(),
                    wcoords.elements(), ycoords.elements(), id, seq);
            if (strands == '-') {
                gsym.setProperty(GraphSym.PROP_GRAPH_STRAND, GraphSym.GRAPH_STRAND_MINUS);
            } else if (strands == '+') {
                gsym.setProperty(GraphSym.PROP_GRAPH_STRAND, GraphSym.GRAPH_STRAND_PLUS);
            } else {
                gsym.setProperty(GraphSym.PROP_GRAPH_STRAND, GraphSym.GRAPH_STRAND_BOTH);
            }
        }
        return gsym;
    }

    @Override
    public String getName() {
        return "scored";
    }

    public boolean isFileSupported(String fileFormat) {
        if (fileFormat == null) {
            return false;
        }

        for (String format : supportedFormat) {
            if (format.equals(fileFormat)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void createGlyphs(RootSeqSymmetry sym, ITrackStyleExtended style, SeqMapViewExtendedI smv, BioSeq seq) {
        if (sym instanceof ScoredContainerSym) {
            annotFactory.createGlyphs(sym, style, smv, seq);
            displayGraphs((ScoredContainerSym) sym, smv);

        } else {
            System.err.println("GenericGraphGlyphFactory.createGlyph() called, but symmetry "
                    + "passed in is NOT a GraphSym: " + sym);
        }
    }

    @Override
    public Set<FileTypeCategory> getSupportedCategories() {
        return ImmutableSet.<FileTypeCategory>builder()
                .add(FileTypeCategory.ScoredContainer).build();
    }
}
