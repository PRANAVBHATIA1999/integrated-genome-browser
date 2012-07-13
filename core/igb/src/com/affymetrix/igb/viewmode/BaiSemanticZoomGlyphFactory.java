package com.affymetrix.igb.viewmode;

import java.net.URI;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.parsers.FileTypeHolder;
import com.affymetrix.genometryImpl.symloader.BaiZoomSymLoader;
import com.affymetrix.genometryImpl.symloader.SymLoader;
import com.affymetrix.igb.shared.MapViewGlyphFactoryI;

public class BaiSemanticZoomGlyphFactory extends GzIndexedSemanticZoomGlyphFactory {
	public static final String BAI_ZOOM_DISPLAYER_EXTENSION = "bai";

	public BaiSemanticZoomGlyphFactory(MapViewGlyphFactoryI defaultDetailGlyphFactory, MapViewGlyphFactoryI heatMapGraphGlyphFactory, MapViewGlyphFactoryI graphGlyphFactory) {
		super(defaultDetailGlyphFactory, heatMapGraphGlyphFactory, graphGlyphFactory);
	}

	@Override
	public String getName() {
		return BAI_ZOOM_DISPLAYER_EXTENSION + "_semantic_zoom";
	}

	@Override
	public String getExtension() {
		return BAI_ZOOM_DISPLAYER_EXTENSION;
	}

	@Override
	protected FileTypeCategory getFileTypeCategory() {
		return FileTypeCategory.Alignment;
	}

	@Override
	protected SymLoader createSummarySymLoader(URI uri, String featureName, AnnotatedSeqGroup group) {
		return new BaiZoomSymLoader(uri, featureName, group);
	}

	@Override
	public boolean isURISupported(String uri) {
		String extension = FileTypeHolder.getInstance().getExtensionForURI(uri);
		return "bam".equals(extension) && super.isURISupported(uri);
	}

	@Override
	public boolean canAutoLoad(String uri) {
		return isURISupported(uri);
	}
}
