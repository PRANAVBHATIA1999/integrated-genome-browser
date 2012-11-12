package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author hiralv
 */
public class StopAutoScrollAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1l;
	private static StopAutoScrollAction ACTION = new StopAutoScrollAction();
	
	private StopAutoScrollAction(){
		super(BUNDLE.getString("stopAutoScroll"), "toolbarButtonGraphics/media/Stop16.gif",
			"toolbarButtonGraphics/media/Stop24.gif");
		setEnabled(false);
	}
	
	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static StopAutoScrollAction getAction() { 
		return ACTION; 
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		getSeqMapView().getAutoScroll().stop();
		
		//Should configure stop as well?
		Rectangle2D.Double cbox = this.getTierMap().getViewBounds();
		getSeqMapView().getAutoScroll().set_start_pos((int)cbox.x);
	
		setEnabled(false);
		StartAutoScrollAction.getAction().setEnabled(true);
	}
}
