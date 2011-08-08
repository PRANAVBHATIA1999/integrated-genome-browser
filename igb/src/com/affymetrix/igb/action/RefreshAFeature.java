package com.affymetrix.igb.action;

import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genoviz.swing.MenuUtil;
import com.affymetrix.igb.view.load.GeneralLoadUtils;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 *
 * @author hiralv
 */
public class RefreshAFeature extends AbstractAction {
	private static final long serialVersionUID = 1L;

	final GenericFeature feature;

	public RefreshAFeature(GenericFeature feature){
		super("Load "+feature.featureName, MenuUtil.getIcon("toolbarButtonGraphics/general/Refresh16.gif"));
		this.feature = feature;
		this.enabled = (feature.getLoadStrategy() != LoadStrategy.NO_LOAD && feature.getLoadStrategy() != LoadStrategy.GENOME);
	}

	public void actionPerformed(ActionEvent e) {
		GeneralLoadUtils.loadAndDisplayAnnotations(feature);
	}

}
