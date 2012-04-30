package com.affymetrix.igb.action;

import static com.affymetrix.igb.IGBConstants.BUNDLE;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

import com.affymetrix.igb.prefs.PreferencesPanel;
import com.affymetrix.igb.prefs.TierPrefsView;
import com.affymetrix.igb.tiers.TierLabelGlyph;
import com.affymetrix.igb.tiers.TierLabelManager;
import com.affymetrix.igb.tiers.TrackConstants;

public class CustomizeAction extends SeqMapViewActionA {
	private static final long serialVersionUID = 1L;
	private static final CustomizeAction ACTION = new CustomizeAction();

	public static CustomizeAction getAction() {
		return ACTION;
	}

	private CustomizeAction() {
		super(BUNDLE.getString("customizeAction"), null, "16x16/categories/preferences-system.png", "22x22/categories/preferences-system.png", KeyEvent.VK_UNDEFINED, null, true);
	}

	private void showCustomizer() {
		TierLabelManager handler = getTierManager();
		PreferencesPanel pv = PreferencesPanel.getSingleton();
		pv.setTab(PreferencesPanel.TAB_TIER_PREFS_VIEW);
		((TierPrefsView) pv.tpvGUI.tdv).setTier_label_glyphs(handler.getSelectedTierLabels());

		// If and only if the selected track is coordinate track, will open 'Other Options' panel 
		if (handler.getSelectedTierLabels().size() == 1) {
			final TierLabelGlyph label = handler.getSelectedTierLabels().get(0);
			String name = label.getReferenceTier().getAnnotStyle().getTrackName();
			if (name.equals(TrackConstants.NAME_OF_COORDINATE_INSTANCE)) {
				pv.setTab(PreferencesPanel.TAB_OTHER_OPTIONS_VIEW);
			}
		}

		JFrame f = pv.getFrame();
		f.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		showCustomizer();
	}
}
