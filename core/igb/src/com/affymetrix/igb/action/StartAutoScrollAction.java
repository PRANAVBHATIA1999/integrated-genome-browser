package com.affymetrix.igb.action;

import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author hiralv
 */
public class StartAutoScrollAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1l;
	private static StartAutoScrollAction ACTION = new StartAutoScrollAction();
	
	private StartAutoScrollAction(){
		super(BUNDLE.getString("startAutoScroll"), "16x16/actions/autoscroll.png",
			"22x22/actions/autoscroll.png");
		setEnabled(true);
	}
	
	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static StartAutoScrollAction getAction() { 
		return ACTION; 
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		start();
	}
	
	public void start(){
		// Calculate start, end and bases per pixels
		Rectangle2D.Double cbox = getTierMap().getViewBounds();
		int start_pos = (int) cbox.x;
		int end_pos = getSeqMapView().getViewSeq().getLength();
	
		getSeqMapView().getAutoScroll().configure(this.getTierMap(), start_pos, end_pos);
		getSeqMapView().getAutoScroll().start(this.getTierMap());
		setEnabled(false);
		StopAutoScrollAction.getAction().setEnabled(true);
	}
}
