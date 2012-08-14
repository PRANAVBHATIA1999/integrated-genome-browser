package com.affymetrix.igb.viewmode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genoviz.bioviews.ViewI;
import com.affymetrix.igb.shared.AbstractViewModeGlyph;
import com.affymetrix.igb.shared.FasterExpandPacker;
import com.affymetrix.igb.shared.MapViewGlyphFactoryA;
import com.affymetrix.igb.shared.SeqMapViewExtendedI;
import com.affymetrix.igb.shared.TierGlyph.Direction;
import com.affymetrix.igb.shared.ViewModeGlyph;

/**
 * creates a glyph with no children, so that it is never displayed
 */
public class DummyGlyphFactory extends MapViewGlyphFactoryA {
	private static final Map<String,Class<?>> PREFERENCES;
	static {
		Map<String,Class<?>> temp = new HashMap<String,Class<?>>();
		PREFERENCES = Collections.unmodifiableMap(temp);
	}
	private static final DummyGlyphFactory instance = new DummyGlyphFactory();
	public static DummyGlyphFactory getInstance() {
		return instance;
	}

	@Override
	public AbstractViewModeGlyph createViewModeGlyph(SeqSymmetry sym, ITrackStyleExtended style, Direction direction, SeqMapViewExtendedI gviewer) {
		return new DummyGlyph(style, direction);
	}

	// glyph class
	public class DummyGlyph extends AbstractViewModeGlyph implements ViewModeGlyph{

		public DummyGlyph(ITrackStyleExtended style, Direction tier_direction) {
			super();
			setStyle(style);
			this.setPacker(new FasterExpandPacker());
			this.setDirection(tier_direction);
		}

		@Override
		public void setPreferredHeight(double height, ViewI view) {
		}

		@Override
		public int getActualSlots() {
			return 0;
		}

		@Override
		public Map<String, Class<?>> getPreferences() {
			return PREFERENCES;
		}

		@Override
		public void setPreferences(Map<String, Object> preferences) {
		}
	}
	// end glyph class

	private DummyGlyphFactory() {
		super();
	}

	@Override
	public AbstractViewModeGlyph getViewModeGlyph(SeqSymmetry sym, ITrackStyleExtended style,
		Direction tier_direction, SeqMapViewExtendedI smv) {
		return new DummyGlyph(style, tier_direction);
	}

	@Override
	public String getName() {
		return "dummy";
	}

	@Override
	public boolean isCategorySupported(FileTypeCategory category) {
		return false;
	}
	
}
