package com.affymetrix.igb.trackOperations;

import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.TrackViewPanel;

/**
 *
 * @author hiralv
 */
public class GraphTrackPanel extends TrackViewPanel {
	private static final long serialVersionUID = 1L;
	private static final int TAB_POSITION = 4;
	
	public GraphTrackPanel(IGBService _igbService) {
		super(_igbService, "Graph", "Graph", false, TAB_POSITION);
	}

	@Override
	protected void selectAllButtonReset() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void clearButtonReset() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void saveButtonReset() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void deleteButtonReset() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected void restoreButtonReset() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public boolean isEmbedded() {
		return true;
	}
}
