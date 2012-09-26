
package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.tiers.TrackStyle;
import java.awt.event.ActionEvent;

import static com.affymetrix.igb.IGBConstants.BUNDLE;
/**
 *
 * @author hiralv
 */
public class ShowLockedTrackIconAction extends GenericAction {
	private static final long serialVersionUID = 1L;
	private static final ShowLockedTrackIconAction ACTION = new ShowLockedTrackIconAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static ShowLockedTrackIconAction getAction() {
		return ACTION;
	}

	private ShowLockedTrackIconAction() {
		super(BUNDLE.getString("showLockedTrackIcon"), null, null);
		this.putValue(SELECTED_KEY, TrackStyle.getShowLockIcon());
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		boolean b = !TrackStyle.getShowLockIcon();
		TrackStyle.setShowLockIcon(b);
		this.putValue(SELECTED_KEY, b);
		((IGB) IGB.getSingleton()).getMapView().getSeqMap().repackTheTiers(true, true);
	}

	@Override
	public boolean isToggle() {
		return true;
	}
}
