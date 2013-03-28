package com.affymetrix.igb.tabs.graph;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.util.ThreadUtils;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.GraphGlyph;

import static com.affymetrix.igb.shared.Selections.*;
/**
 *  Puts all selected graphs in separate tiers by setting the
 *  combo state of each graph's state to null.        
 */
public class SplitGraphsAction extends GenericAction {
	private static final long serialVersionUID = 1l;

	public SplitGraphsAction(IGBService igbService) {
		super("Split", null, null);
		this.igbService = igbService;
	}

	private final IGBService igbService;

	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		super.actionPerformed(e);
		
		for (GraphGlyph gg : graphGlyphs) {
			GraphGlyph.split(gg);
		}
		//igbService.getSeqMapView().postSelections();
		updateDisplay();
	}
	private void updateDisplay() {
		ThreadUtils.runOnEventQueue(new Runnable() {
	
			public void run() {
//				igbService.getSeqMap().updateWidget();
//				igbService.getSeqMapView().setTierStyles();
//				igbService.getSeqMapView().repackTheTiers(true, true);
				igbService.getSeqMapView().updatePanel(true, true);
			}
		});
	}

}
