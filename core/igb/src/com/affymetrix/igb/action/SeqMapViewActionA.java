package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genoviz.swing.recordplayback.ScriptManager;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.tiers.TierLabelManager;
import com.affymetrix.igb.view.SeqMapView;

/**
 * Superclass of all IGB actions that must refer to a {@link SeqMapView}.
 * These classes are automatically added to a static hash map.
 * This is done via the constructor, a dubious practice.
 */
public abstract class SeqMapViewActionA extends GenericAction {
	private static final long serialVersionUID = 1L;
//	protected static final Map<String, SeqMapViewActionA> ACTION_MAP = new HashMap<String, SeqMapViewActionA>();
	protected String id;
	private SeqMapView gviewer;
	private TierLabelManager handler;

	public SeqMapViewActionA(String text, String tooltip, String iconPath, String largeIconPath, int mnemonic) {
		super(text, tooltip, iconPath, largeIconPath, mnemonic);
	}

	public SeqMapViewActionA(String text, String tooltip, String iconPath, String largeIconPath, int mnemonic, Object extraInfo, boolean popup) {
		super(text, tooltip, iconPath, largeIconPath, mnemonic, extraInfo, popup);
	}
	
	public SeqMapViewActionA(String text, String iconPath, String largeIconPath) {
		super(text, iconPath, largeIconPath);
	}

	public SeqMapViewActionA(String text, int mnemonic) {
		super(text, mnemonic);
	}

	protected SeqMapView getSeqMapView() {
		if (gviewer == null) {
			if (id == null) {
				gviewer = Application.getSingleton().getMapView();
			}
			else {
				gviewer = (SeqMapView)ScriptManager.getInstance().getWidget(id);
			}
		}
		return gviewer;
	}

	protected TierLabelManager getTierManager() {
		if (handler == null) {
			handler = getSeqMapView().getTierManager();
		}
		return handler;
	}

	protected void refreshMap(boolean stretch_vertically, boolean stretch_horizonatally) {
		if (gviewer != null) {
			// if an AnnotatedSeqViewer is being used, ask it to update itself.
			// later this can be made more specific to just update the tiers that changed
			boolean preserve_view_x = !stretch_vertically;
			boolean preserve_view_y = !stretch_horizonatally;
			gviewer.updatePanel(preserve_view_x, preserve_view_y);
		} else {
			// if no AnnotatedSeqViewer (as in simple test programs), update the tiermap itself.
			getTierManager().repackTheTiers(false, stretch_vertically);
		}
	}
}
