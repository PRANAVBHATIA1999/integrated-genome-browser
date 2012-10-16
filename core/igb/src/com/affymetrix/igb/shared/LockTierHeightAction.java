package com.affymetrix.igb.shared;

import com.affymetrix.igb.action.TierHeightAction;
import com.affymetrix.igb.view.factories.DefaultTierGlyph;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

/**
 *
 * @author hiralv
 */
public class LockTierHeightAction extends TierHeightAction{
	private static final long serialVersionUID = 1L;
	private final static LockTierHeightAction lockTierAction = new LockTierHeightAction();
	
	public static LockTierHeightAction getAction(){
		return lockTierAction;
	}
	
	static{
		Selections.addRefreshSelectionListener(getAction().enabler);
	}
	
	private LockTierHeightAction() {
		super(BUNDLE.getString("lockTierHeightAction"),  null, null);
	}

	@Override
	protected void setHeightFixed(DefaultTierGlyph dtg) {
		dtg.setHeightFixed(true);
	}
}
