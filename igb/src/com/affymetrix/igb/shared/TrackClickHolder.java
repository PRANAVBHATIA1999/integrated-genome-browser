package com.affymetrix.igb.shared;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

public class TrackClickHolder {
	private static TrackClickHolder instance = new TrackClickHolder();
	private TrackClickHolder() {
		super();
	}
	public static TrackClickHolder getInstance() {
		return instance;
	}

	private List<TrackClickListener> trackClickListeners = new ArrayList<TrackClickListener>();

	public void addTrackClickListener(TrackClickListener TrackClickListener) {
		trackClickListeners.add(TrackClickListener);
	}

	public void removeTrackClickListener(TrackClickListener TrackClickListener) {
		trackClickListeners.remove(TrackClickListener);
	}

	public List<TrackClickListener> getTrackClickListeners() {
		return trackClickListeners;
	}

	public void doTrackClick(JPopupMenu popup, List<TierGlyph> selectedGlyphs) {
		for (TrackClickListener l : trackClickListeners) {
			l.trackClickNotify(popup, selectedGlyphs);
		}
	}
}
