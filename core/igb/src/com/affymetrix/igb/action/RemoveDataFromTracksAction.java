package com.affymetrix.igb.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.style.GraphState;
import com.affymetrix.genometryImpl.style.ITrackStyleExtended;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.shared.GraphGlyph;
import com.affymetrix.igb.view.TrackView;
import static com.affymetrix.igb.shared.Selections.*;
import com.affymetrix.igb.shared.StyledGlyph;

public class RemoveDataFromTracksAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private static final RemoveDataFromTracksAction ACTION = new RemoveDataFromTracksAction();

	static{
		GenericActionHolder.getInstance().addGenericAction(ACTION);
	}
	
	public static RemoveDataFromTracksAction getAction() {
		return ACTION;
	}

	protected RemoveDataFromTracksAction() {
		super(IGBConstants.BUNDLE.getString("deleteAction"), null,
				"16x16/actions/remove_data.png",
				"22x22/actions/remove_data.png", KeyEvent.VK_UNDEFINED);
		this.ordinal = -9007300;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		BioSeq seq = GenometryModel.getGenometryModel().getSelectedSeq();

		if (IGB.confirmPanel(MessageFormat.format(IGBConstants.BUNDLE.getString("confirmDelete"), seq.getID()), PreferenceUtils.getTopNode(),
				PreferenceUtils.CONFIRM_BEFORE_CLEAR, PreferenceUtils.default_confirm_before_clear)) {
			
			// First split the graph.
			for (StyledGlyph vg : allGlyphs) {
				//If graphs is joined then apply color to combo style too.
				// TODO: Use code from split graph
				if (vg instanceof GraphGlyph) {
					ITrackStyleExtended style = ((GraphGlyph) vg).getGraphState().getComboStyle();
					if (style != null) {
						GraphState gstate = ((GraphGlyph) vg).getGraphState();
						gstate.setComboStyle(null, 0);
						gstate.getTierStyle().setJoin(false);
					}
				}
			}
			
			for (ITrackStyleExtended style : allStyles) {
				String method = style.getMethodName();
				if (method != null) {
					TrackView.getInstance().delete(getSeqMapView().getSeqMap(), method, style);
				}
			}
		}
		getSeqMapView().dataRemoved();	// refresh
	}
}
