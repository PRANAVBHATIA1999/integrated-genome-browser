package com.affymetrix.igb.trackAdjuster;

import java.util.EventObject;
import java.util.List;

import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.SeqSelectionEvent;
import com.affymetrix.genometryImpl.event.SeqSelectionListener;
import com.affymetrix.genometryImpl.event.SymSelectionEvent;
import com.affymetrix.genometryImpl.event.SymSelectionListener;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.symmetry.RootSeqSymmetry;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.AbstractGraphGlyph;
import com.affymetrix.igb.shared.TrackPreferencesA;
import com.affymetrix.igb.shared.TrackUtils;
import com.affymetrix.igb.shared.TrackstylePropertyMonitor;
import com.affymetrix.igb.shared.ViewModeGlyph;

public class TrackPreferencesSeqMapViewPanel extends TrackPreferencesA implements SeqSelectionListener, SymSelectionListener, TrackstylePropertyMonitor.TrackStylePropertyListener {
	private static final long serialVersionUID = 1L;

	public TrackPreferencesSeqMapViewPanel(IGBService _igbService) {
		super(_igbService);
		GenometryModel gmodel = GenometryModel.getGenometryModel();
		gmodel.addSeqSelectionListener(this);
		gmodel.addSymSelectionListener(this);
		TrackstylePropertyMonitor.getPropertyTracker().addPropertyListener(this);
//		igbService.addListSelectionListener(this);
	}

	private void refreshSelection() {
		is_listening = false; // turn off propagation of events from the GUI while we modify the settings
		@SuppressWarnings({ "unchecked", "rawtypes", "cast" })
		List<ViewModeGlyph> selected = (List)igbService.getSeqMapView().getAllSelectedTiers();
		allGlyphs.clear();
		allGlyphs.addAll(selected);
		graphGlyphs.clear();
		annotGlyphs.clear();
		for (ViewModeGlyph vg : allGlyphs) {
			if (vg instanceof AbstractGraphGlyph) {
				graphGlyphs.add((AbstractGraphGlyph)vg);
			}
			else if (vg.getInfo() != null && vg.getInfo() instanceof RootSeqSymmetry && (((RootSeqSymmetry)vg.getInfo()).getCategory() == FileTypeCategory.Annotation || ((RootSeqSymmetry)vg.getInfo()).getCategory() == FileTypeCategory.Alignment)) {
				annotGlyphs.add(vg);
			}
		}
		rootSyms.clear();
		rootSyms.addAll(TrackUtils.getInstance().getSymsFromViewModeGlyphs(selected));
		// First loop through and collect graphs and glyphs
		for (RootSeqSymmetry rootSym : rootSyms) {
			if (rootSym.getCategory() == FileTypeCategory.Annotation || rootSym.getCategory() == FileTypeCategory.Alignment) {
				annotSyms.add(rootSym);
			}
			if (rootSym.getCategory() == FileTypeCategory.Graph || rootSym.getCategory() == FileTypeCategory.ScoredContainer) {
				graphSyms.add(rootSym);
			}
		}
		is_listening = true; // turn back on GUI events
	}

	@Override
	public void symSelectionChanged(SymSelectionEvent evt) {
		// Only pay attention to selections from the main SeqMapView or its map.
		// Ignore the splice view as well as events coming from this class itself.

		Object src = evt.getSource();
		if (!(src == igbService.getSeqMapView() || src == igbService.getSeqMap())
				|| igbService.getSeqMap() == null || igbService.getSeqMapView() == null) {
			return;
		}

		refreshSelection();
		resetAll();
	}
	@Override
	public void seqSelectionChanged(SeqSelectionEvent evt) {
		refreshSelection();
		resetAll();
	}

	@Override
	public void trackstylePropertyChanged(EventObject eo) { // this is redundant when the source of the style change is this panel
		refreshSelection();
		resetAll();
	}
}
