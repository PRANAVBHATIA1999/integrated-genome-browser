
package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.util.PreferenceUtils;

import com.affymetrix.igb.tiers.TrackConstants;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.tiers.IGBStateProvider;
/**
 *
 * @author hiralv
 */
public class ShowLockedTrackIconAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private static final ShowLockedTrackIconAction ACTION = new ShowLockedTrackIconAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
		PreferenceUtils.saveToPreferences(TrackConstants.PREF_SHOW_LOCKED_TRACK_ICON, TrackConstants.default_show_locked_track_icon, ACTION);
	}
	
	public static ShowLockedTrackIconAction getAction() {
		return ACTION;
	}

	private ShowLockedTrackIconAction() {
		super(BUNDLE.getString("showLockedTrackIcon"), "16x16/actions/blank_placeholder.png", null);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		IGBStateProvider.setShowLockIcon((Boolean)getValue(SELECTED_KEY));
		getSeqMapView().getSeqMap().repackTheTiers(true, true);
	}

	@Override
	public boolean isToggle() {
		return true;
	}
}
