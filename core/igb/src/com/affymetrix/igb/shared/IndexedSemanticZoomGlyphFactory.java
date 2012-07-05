package com.affymetrix.igb.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.operator.Operator;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.style.HeatMap;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symloader.SymLoader;
import com.affymetrix.genometryImpl.symmetry.*;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.igb.shared.TierGlyph.Direction;
import com.affymetrix.igb.viewmode.DynamicStyleHeatMap;

public abstract class IndexedSemanticZoomGlyphFactory extends SemanticZoomGlyphFactory {
	protected final MapViewGlyphFactoryI defaultGlyphFactory;
	private final MapViewGlyphFactoryI graphGlyphFactory;
//	protected final Operator transformOperator = new com.affymetrix.genometryImpl.operator.LogTransform(Math.E);
	protected final Operator transformOperator = new com.affymetrix.genometryImpl.operator.PowerTransformer(0.5);

	public IndexedSemanticZoomGlyphFactory(MapViewGlyphFactoryI defaultGlyphFactory, MapViewGlyphFactoryI graphGlyphFactory) {
		super();
		this.defaultGlyphFactory = defaultGlyphFactory;
		this.graphGlyphFactory = graphGlyphFactory;
	}

	public abstract String getIndexedFileName(String method, Direction direction);
	protected abstract FileTypeCategory getFileTypeCategory();

	protected boolean hasIndex(String uri) {
		if (uri == null) {
			return false;
		}
		return GeneralUtils.urlExists(getIndexedFileName(uri, Direction.BOTH));
	}

	protected MapViewGlyphFactoryI getGraphGlyphFactory() {
		return graphGlyphFactory;
	}

	@Override
	public boolean isCategorySupported(FileTypeCategory category) {
		return defaultGlyphFactory.isCategorySupported(category);
	}

	@Override
	public boolean isURISupported(String uri) {
		return hasIndex(uri);
	}

	@Override
	public boolean canAutoLoad(String uri) {
		return hasIndex(uri);
	}

	protected AbstractGraphGlyph getEmptyGraphGlyph(ITrackStyleExtended trackStyle, SeqMapViewExtendedI gviewer) {
		GraphSym graf = new GraphSym(new int[]{gviewer.getVisibleSpan().getMin()}, new int[]{gviewer.getVisibleSpan().getLength()}, 
				new float[]{0}, trackStyle.getMethodName(), gviewer.getVisibleSpan().getBioSeq());
		return (AbstractGraphGlyph)getGraphGlyphFactory().getViewModeGlyph(graf, trackStyle, Direction.BOTH, gviewer);
	}

	@Override
	public ViewModeGlyph getViewModeGlyph(SeqSymmetry sym,
			ITrackStyleExtended style, Direction direction,
			SeqMapViewExtendedI smv) {
		SemanticZoomGlyph szg = (SemanticZoomGlyph) super.getViewModeGlyph(sym, style, direction, smv);
		szg.setLastUsedGlyph(szg.getGlyph(smv));
		return szg;
	}
	// glyph class
	public abstract class IndexedSemanticZoomGlyph extends SemanticZoomGlyphFactory.SemanticZoomGlyph implements SeqSelectionListener {
		protected ViewModeGlyph defaultGlyph;
//		protected final SeqMapViewExtendedI smv;
		protected SymLoader detailSymL;
		protected SymLoader summarySymL;
//		protected SimpleSeqSpan saveSpan;
			
		public IndexedSemanticZoomGlyph(SeqSymmetry sym) {
			super(sym);
//			this.smv = smv;
//			saveSpan = null;
		}
		
		@Override
		protected void init(SeqSymmetry sym, ITrackStyleExtended trackStyle,
			Direction direction, SeqMapViewExtendedI gviewer) {
			viewModeGlyphs = new HashMap<String, ViewModeGlyph>();
			defaultGlyph = getEmptyGraphGlyph(trackStyle, gviewer);
			detailGlyph = defaultGlyphFactory.getViewModeGlyph(sym, trackStyle, Direction.BOTH, gviewer);
		}

		protected RootSeqSymmetry getRootSym() {
			return (RootSeqSymmetry)GenometryModel.getGenometryModel().getSelectedSeq().getAnnotation(style.getMethodName());
		}
				
		protected ViewModeGlyph getSummaryGlyph(SeqMapViewExtendedI smv) throws Exception {
			ViewModeGlyph resultGlyph = null;
			List<? extends SeqSymmetry> symList = summarySymL.getRegion(smv.getVisibleSpan());
			if (symList.size() > 0) {
				GraphSym gsym = (GraphSym)symList.get(0);
				List<SeqSymmetry> operList = new ArrayList<SeqSymmetry>();
				operList.add(gsym);
				BioSeq aseq = GenometryModel.getGenometryModel().getSelectedSeq();
				GraphSym opersym = (GraphSym)transformOperator.operate(aseq, operList);
				if (PreferenceUtils.getBooleanParam(PreferenceUtils.COVERAGE_SUMMARY_HEATMAP, PreferenceUtils.default_coverage_summary_heatmap)) {
					HeatMap styleHeatMap = new DynamicStyleHeatMap(HeatMap.FOREGROUND_BACKGROUND, style, 0.0f, 0.5f);
					opersym.getGraphState().setHeatMap(styleHeatMap);
				}
				resultGlyph = getGraphGlyphFactory().getViewModeGlyph(opersym, style, Direction.BOTH, smv);
			}
			if (resultGlyph != null) {
				((AbstractGraphGlyph)resultGlyph).drawHandle(false);
				resultGlyph.setCoords(resultGlyph.getCoordBox().x, resultGlyph.getCoordBox().y, resultGlyph.getCoordBox().width, style.getMaxDepth() * style.getHeight());
			}
			return resultGlyph;
		}

		@Override
		public ViewModeGlyph getGlyph(SeqMapViewExtendedI smv) {
//			BioSeq seq = smv.getAnnotatedSeq();
			ViewI view = smv.getSeqMap().getView();
//	        int endBase = startBase + length;
//	        SimpleSeqSpan span = new SimpleSeqSpan(startBase, endBase, seq);
//	        if (span.equals(saveSpan) && lastUsedGlyph != null) {
//	        	return lastUsedGlyph;
//	        }
			try {
				ViewModeGlyph resultGlyph = null;
				if (isDetail(view)) {
					resultGlyph = getDetailGlyph(smv, defaultGlyphFactory);
				}
				else {
					resultGlyph = getSummaryGlyph(smv);
				}
				if (resultGlyph == null) {
					resultGlyph = getEmptyGraphGlyph(style, smv);
				}
				if(resultGlyph == lastUsedGlyph)
					return resultGlyph;
				
				prepareViewModeGlyph(resultGlyph, view);
				
//				saveSpan = span;
				lastUsedGlyph = resultGlyph;
				viewModeGlyphs.put("lastUsed", lastUsedGlyph);
				return resultGlyph;
			}
			catch (Exception x) {
				Logger logger = Logger.getLogger(this.getClass().getName());
				logger.log(Level.SEVERE, "Error in Indexed Semantic zoom", x);
				return null;
			}
		}

		protected void prepareViewModeGlyph(ViewModeGlyph resultGlyph, ViewI view) {
			int startBase = (int)Math.round(view.getCoordBox().getX());
			int length = (int)Math.round(view.getCoordBox().getWidth());
			
			resultGlyph.setSelectable(false);
			double y = resultGlyph.getCoordBox().y;
			if (y == 0) {
				y = getCoordBox().y;
			}
			double height = resultGlyph.getCoordBox().height;
			if (height == 0) {
				height = getCoordBox().height;
			}
			getCoordBox().setRect(startBase, y, length, height);
			resultGlyph.setCoordBox(getCoordBox());
			resultGlyph.setVisibility(true);
			resultGlyph.setParent(getParent());
			resultGlyph.setScene(getScene());
			double saveY = resultGlyph.getCoordBox().y;
//				if (resultGlyph.getScene() != null) {
//					resultGlyph.pack(view);
//				}
			if (resultGlyph.getCoordBox().y != saveY) {
				resultGlyph.moveAbsolute(resultGlyph.getCoordBox().x, saveY);
			}
		}

		@Override
		public ViewModeGlyph getDefaultGlyph() {
			return defaultGlyph;
		}

		@Override
		public void processParentCoordBox(java.awt.geom.Rectangle2D.Double parentCoordBox) {
			super.processParentCoordBox(parentCoordBox);
			if (defaultGlyph != null) {
				defaultGlyph.setCoordBox(parentCoordBox);
			}
		}

		@Override
		public Object getInfo() {
			RootSeqSymmetry rootSym = getRootSym();
			if (rootSym == null) {
				rootSym = new DummyRootSeqSymmetry(getFileTypeCategory()); // so that it is not null
			};
			return rootSym;
		}
		
		@Override
		public void seqSelectionChanged(SeqSelectionEvent evt) {
			detailGlyph = null;
		}
	}
	// end glyph class
}
