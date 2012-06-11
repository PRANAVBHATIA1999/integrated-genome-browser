package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;
import javax.swing.event.MouseInputAdapter;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.igb.tiers.AccordionTierResizer;
import com.affymetrix.igb.tiers.TierResizer;
import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author hiralv
 */
public class ToggleTrackResizingAction extends SeqMapViewActionA {
	private static ToggleTrackResizingAction ACTION = new ToggleTrackResizingAction();
	
	public static ToggleTrackResizingAction getAction(){
		return ACTION;
	}
	
	private final TrackResizingAction trackAjustAllAction;
	private final TrackResizingAction trackAdjustAdjacentAction;
	private TrackResizingAction selectedAction;
	
	protected ToggleTrackResizingAction() {
		super(null, null, null);
		trackAjustAllAction = new TrackResizingAction(BUNDLE.getString("adjustAllTracks"), new AccordionTierResizer(getTierMap()));
		trackAdjustAdjacentAction = new TrackResizingAction(BUNDLE.getString("adjustAdjacentTracks"), new TierResizer(getTierMap()));
		toggle(trackAjustAllAction);
	}
	
	public TrackResizingAction getAdjustAllAction(){
		return trackAjustAllAction;
	}
	
	public TrackResizingAction getAdjustAdjacentAction(){
		return trackAdjustAdjacentAction;
	}
		
	private void addListeners(TrackResizingAction trackResizingAction){
		selectedAction.putValue(SELECTED_KEY, true);
		getLabelMap().addMouseListener(trackResizingAction.resizer);
		getLabelMap().addMouseMotionListener(trackResizingAction.resizer);
	}
	
	private void removeListeners(TrackResizingAction trackResizingAction){
		getLabelMap().removeMouseListener(trackResizingAction.resizer);
		getLabelMap().removeMouseMotionListener(trackResizingAction.resizer);
	}
	
	private void toggle(TrackResizingAction trackResizingAction){
		if(trackResizingAction == null || trackResizingAction == selectedAction)
			return;
		
		if(selectedAction != null){
			selectedAction.putValue(SELECTED_KEY, false);
			selectedAction.setEnabled(true);
			removeListeners(selectedAction);
		}
		
		selectedAction = trackResizingAction;
		
		selectedAction.putValue(SELECTED_KEY, true);
		selectedAction.setEnabled(false);
		addListeners(selectedAction);
	}
	
	private class TrackResizingAction extends GenericAction{
		final MouseInputAdapter resizer;
		protected TrackResizingAction(String text, MouseInputAdapter resizer) {
			super(text, null, null);
			this.resizer = resizer;
		}
		
		@Override
		public void actionPerformed(ActionEvent e){
			ToggleTrackResizingAction.this.toggle(this);
		}
	};
}
