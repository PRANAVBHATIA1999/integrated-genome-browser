package com.affymetrix.igb.action;


import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.igb.shared.RepackTiersAction;
import java.awt.event.ActionEvent;

import com.affymetrix.igb.IGBConstants;

public class RepackSelectedTiersAction extends RepackTiersAction {
	private static final long serialVersionUID = 1L;
	private static RepackSelectedTiersAction ACTION = new RepackSelectedTiersAction();

	public RepackSelectedTiersAction() {
		super(IGBConstants.BUNDLE.getString("repackSelectedTracksAction"),
				"16x16/actions/Repack.png",
				"22x22/actions/Repack.png");
		this.ordinal = -6008500;
	}

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static RepackSelectedTiersAction getAction() {
		return ACTION;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		repackTiers(getTierManager().getSelectedTierLabels());
	}
}
