package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import java.awt.event.ActionEvent;

/**
 *
 * @author hiralv
 */
public class RefreshAFeatureAction extends GenericAction {
	private static final long serialVersionUID = 1L;
	private GenericFeature feature;

	public static RefreshAFeatureAction createRefreshAFeatureAction(final GenericFeature feature) {
		final String text = "Load "+feature.featureName;
		RefreshAFeatureAction refreshAFeature = new RefreshAFeatureAction(text);
		refreshAFeature.setFeature(feature);
		return refreshAFeature;
	}

	private RefreshAFeatureAction(String text){
		super(text, "toolbarButtonGraphics/general/Refresh16.gif");
	}

	private void setFeature(GenericFeature feature) {
		this.feature = feature;
		this.enabled = (feature.getLoadStrategy() != LoadStrategy.NO_LOAD && feature.getLoadStrategy() != LoadStrategy.GENOME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		GeneralLoadUtils.loadAndDisplayAnnotations(feature);
	}
}
