package com.affymetrix.igb.viewmode;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.style.DefaultStateProvider;
import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.GraphSym;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.util.GraphSymUtils;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.igb.shared.*;

public abstract class AbstractGraphGlyphFactory extends MapViewGlyphFactoryA {

	private static final Logger ourLogger =
			Logger.getLogger(AbstractGraphGlyphFactory.class.getPackage().getName());

	private boolean check_same_seq = true;
	/** Name of a parameter for the init() method.  Set to Boolean.TRUE or Boolean.FALSE.
	 *  Determines whether the glyph factory will try to determine whether the GraphSym
	 *  that it is drawing is defined on the currently-displayed bioseq.
	 *  In some cases, you may want to intentionally display a graph on a seq that
	 *  has a different ID without checking to see if the IDs match.
	 */
	private static final String CHECK_SAME_SEQ_OPTION = "Check Same Seq";
	/** Name of a parameter for the init() method.  Set to an instance of Double.
	 *  Controls a parameter of the GraphGlyph.
	 *  @see GraphGlyph#setTransitionScale(double)
	 */
	/** Allows you to set the parameter CHECK_SAME_SEQ_OPTION. */
	@Override
	public void init(Map<String, Object> options) {
		Boolean ccs = (Boolean) options.get(CHECK_SAME_SEQ_OPTION);
		if (ccs != null) {
			check_same_seq = ccs.booleanValue();
		}
	}

	/**
	 *  Makes a GraphGlyph to represent the input GraphSym,
	 *     and either adds it as a floating graph to the SeqMapView or adds it
	 *     in a tier, depending on getGraphState().getGraphStyle().getFloatGraph()
	 *     and getGraphState().getComboStyle().
	 *  All graphs that share the same tier style or the same combo tier style,
	 *     will go in the same tier.  Graphs with a non-null combo tier style
	 *     will go into an attached tier, never a floating glyph.
	 *  Also adds to the SeqMapView's GraphState-to-TierGlyph hash if needed.
	 */
	private AbstractGraphGlyph displayGraph(GraphSym graf, SeqMapViewExtendedI smv, boolean check_same_seq) {
		BioSeq aseq = smv.getAnnotatedSeq();
		BioSeq vseq = smv.getViewSeq();
		BioSeq graph_seq = graf.getGraphSeq();
		boolean isGenome = false;

		if (check_same_seq && graph_seq != aseq) {
			// may need to modify to handle case where GraphGlyph's seq is one of seqs in aseq's composition...
			return null;
		}

		// GAH 2006-03-26
		//    want to add code here to handle situation where a "virtual" seq is being display on SeqMapView,
		//       and it is composed of GraphSym's from multiple annotated seqs, but they're really from the
		//       same data source (or they're the "same" data on different chromosomes for example)
		//       In this case want these displayed as a single graph

		//   match these up based on identical graph names / ids, then:
		//    Approach 1)
		//       build a CompositeGraphSym on the virtual seq
		//       make a single GraphGlyph
		//    Approach 2)
		//       create a new CompositeGraphGlyph subclass (or do I already have this?)
		//       make multiple GraphGlyphs
		//    Approach 3)
		//       ???


		GraphSym newgraf = graf;
		if (check_same_seq && graph_seq != vseq) {
			if (vseq != null && "genome".equals(vseq.getID())) {
				//TODO: Fix bug 1856102 "Genome View Bug" here. See Gregg's comments above.
				isGenome = true;
			}
			// The new graph doesn't need a new GraphState or a new ID.
			// Changing any graph properties will thus apply to the original graph.
			SeqSymmetry mapping_sym = smv.transformForViewSeq(graf, graph_seq);
			newgraf = GraphSymUtils.transformGraphSym(graf, mapping_sym);
		}
		if (newgraf == null || newgraf.getPointCount() == 0) {
			return null;
		}

		String graph_name = newgraf.getGraphName();
		if (graph_name == null) {
			// this probably never actually happens
			graph_name = "Graph #" + System.currentTimeMillis();
			newgraf.setGraphName(graph_name);
		}

		return displayGraphSym(newgraf, graf, smv, isGenome);
	}

	protected abstract AbstractGraphGlyph createViewModeGlyph(GraphSym newgraf, GraphState gstate, SeqMapViewExtendedI smv);

	/**
	 * Almost exactly the same as ScoredContainerGlyphFactory.displayGraphSym.
	 */
	private AbstractGraphGlyph displayGraphSym(GraphSym newgraf, GraphSym graf, SeqMapViewExtendedI smv, boolean isGenome) {
		GraphState gstate = graf.getGraphState();
		AbstractGraphGlyph graph_glyph = createViewModeGlyph(newgraf, gstate, smv);
		graph_glyph.addChild(graph_glyph.getGraphGlyph());
		ITrackStyleExtended tier_style = gstate.getTierStyle();
		tier_style.setTrackName(newgraf.getGraphName());
//		tier_style.setCollapsed(isGenome);
		if (gstate.getComboStyle() != null) {
			tier_style = gstate.getComboStyle();
		}

		graph_glyph.setCoords(0, tier_style.getY(), newgraf.getGraphSeq().getLength(), gstate.getTierStyle().getHeight());
		graph_glyph.getGraphGlyph().setCoords(0, tier_style.getY(), newgraf.getGraphSeq().getLength(), gstate.getTierStyle().getHeight());
//		SeqSpan pspan = smv.getViewSeqSpan(newgraf);
////		if (pspan == null || pspan.getLength() == 0) {
//		if (pspan == null) {
//			return null;
//		}
//		graph_glyph.setCoords(pspan.getMin(), tier_style.getY(), pspan.getLength(), gstate.getTierStyle().getHeight());
		smv.setDataModelFromOriginalSym(graph_glyph, graf); // has side-effect of graph_glyph.setInfo(graf)
		smv.setDataModelFromOriginalSym(graph_glyph.getGraphGlyph(), graf);
		// Allow floating glyphs ONLY when combo style is null.
		// (Combo graphs cannot yet float.)
		//if (/*gstate.getComboStyle() == null && */ gstate.getTierStyle().getFloatGraph()) {
		//	GraphGlyphUtils.checkPixelBounds(graph_glyph, map);
		//	smv.addToPixelFloaterGlyph(graph_glyph);
		//} else {
			/*
			TierGlyph.Direction direction = TierGlyph.Direction.NONE;
			if (GraphSym.GRAPH_STRAND_MINUS.equals(graf.getProperty(GraphSym.PROP_GRAPH_STRAND))) {
				direction = TierGlyph.Direction.REVERSE;
			}else if(GraphSym.GRAPH_STRAND_PLUS.equals(graf.getProperty(GraphSym.PROP_GRAPH_STRAND))) {
				direction = TierGlyph.Direction.FORWARD;
			}
			TierGlyph tglyph = smv.getGraphTrack(tier_style, direction);
			if(gstate.getComboStyle() != null && !(tglyph.getPacker() instanceof GraphFasterExpandPacker)){
				tglyph.setExpandedPacker(new GraphFasterExpandPacker());
			}
			if (isGenome && !(tglyph.getPacker() instanceof CollapsePacker)) {
				CollapsePacker cp = new CollapsePacker();
				cp.setParentSpacer(0); // fill tier to the top and bottom edges
				cp.setAlignment(CollapsePacker.ALIGN_CENTER);
				tglyph.setPacker(cp);
			}
			tglyph.addChild(graph_glyph);
			tglyph.pack(map.getView(), false);
*/
			if (graph_glyph.getScene() != null) {
				graph_glyph.pack(smv.getSeqMap().getView());
				graph_glyph.getGraphGlyph().pack(smv.getSeqMap().getView());
			}
		//}
		return graph_glyph;
	}

	@Override
	public boolean isCategorySupported(FileTypeCategory category) {
		if (category == FileTypeCategory.Graph){
			return true;
		}
		return false;
	}

	@Override
	public AbstractViewModeGlyph getViewModeGlyph(SeqSymmetry sym, ITrackStyleExtended style, TierGlyph.Direction tier_direction, SeqMapViewExtendedI smv) {
		AbstractViewModeGlyph result = null;
		if (sym == null) {
			result = createViewModeGlyph(sym, style, tier_direction, smv);
		} else if (sym instanceof GraphSym) {
			result = displayGraph((GraphSym) sym, smv, check_same_seq);
			if (result == null) {
				result = createViewModeGlyph(sym, style, tier_direction, smv);
			}
			else {
				if(smv.getViewSeq() != smv.getAnnotatedSeq()){
//					GenomeGraphGlyph genomeGraphGlyph = new GenomeGraphGlyph(smv, style);
//					genomeGraphGlyph.setCoords(0, style.getY(), smv.getViewSeq().getLength(), style.getHeight());
//					if (genomeGraphGlyph.getScene() != null) {
//						genomeGraphGlyph.pack(smv.getSeqMap().getView());
//					}
//					//((AbstractGraphGlyph)result).drawHandle(false);
//					genomeGraphGlyph.addChild(result);
//					result = genomeGraphGlyph;
				}
			}
		} else {
			ourLogger.log(Level.SEVERE, 
					"GenericGraphGlyphFactory.getViewModeGlyph() called, but symmetry passed in is NOT a GraphSym: {0}", sym);
		}
		return result;
	}
	
	@Override
	public AbstractViewModeGlyph createViewModeGlyph(SeqSymmetry sym, ITrackStyleExtended style, TierGlyph.Direction tier_direction, SeqMapViewExtendedI smv){
		GraphState gState;
		if(sym == null){
			sym = new GraphSym(new int[]{smv.getVisibleSpan().getMin()}, new float[]{0}, style.getMethodName(), smv.getAnnotatedSeq());
			gState = getGraphState(style);
		}else{
			gState = ((GraphSym)sym).getGraphState();
		}
		AbstractViewModeGlyph result = createViewModeGlyph((GraphSym)sym, gState, smv);
		result.setCoords(0, style.getY(), smv.getViewSeq().getLength(), style.getHeight());
		return result;
	}
	
	private static GraphState getGraphState(ITrackStyleExtended style){
		String featureName = null, extension = null;
		Map<String, String> featureProps = null;
		if(style.getFeature() != null){
			featureName = style.getFeature().featureName;
			extension = style.getFeature().getExtension();
			featureProps = style.getFeature().featureProps;
		}
		return DefaultStateProvider.getGlobalStateProvider().getGraphState(style.getMethodName(), featureName, extension, featureProps);
	}
	
	public static class GenomeGraphGlyph extends MultiGraphGlyph{

		public GenomeGraphGlyph(GraphGlyph graphGlyph) {
			super(graphGlyph);
			setStyle(style);
		}
				
		@Override
		public void draw(ViewI view) {
			//drawHandle(view);
			//drawLabel(view);
		}
		
//		@Override
//		public String getName() {
//			return "genome";
//		}
//
//		@Override
//		public GraphType getGraphStyle() {
//			return null;
//		}
		
	}
}
