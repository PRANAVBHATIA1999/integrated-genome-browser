package com.affymetrix.igb.graph;

import org.osgi.framework.BundleActivator;

import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.common.ExtensionPointListener;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genometryImpl.operator.Operator;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.window.service.WindowActivator;

public class Activator extends WindowActivator implements BundleActivator {
	@Override
	protected IGBTabPanel getPage(IGBService igbService) {
		SimpleGraphTabGUI.init(igbService);
		final SimpleGraphTabGUI simpleGraphTabGUI = SimpleGraphTabGUI.getSingleton();
		ExtensionPointHandler<Operator> operatorExtensionPoint = ExtensionPointHandler.getOrCreateExtensionPoint(bundleContext, Operator.class);
		operatorExtensionPoint.addListener(new ExtensionPointListener<Operator>() {

			@Override
			public void addService(Operator operator) {
				simpleGraphTabGUI.sgt.addOperator(operator);
			}

			@Override
			public void removeService(Operator operator) {
				simpleGraphTabGUI.sgt.removeOperator(operator);
			}
		});
		ExtensionPointHandler<GenericAction> genericActionExtensionPoint = ExtensionPointHandler.getOrCreateExtensionPoint(bundleContext, GenericAction.class);
		genericActionExtensionPoint.addListener(new ExtensionPointListener<GenericAction>() {
			@Override
			public void removeService(GenericAction genericAction) {
			}
			
			@Override
			public void addService(GenericAction genericAction) {
				// TODO not a good way to do this
				if ("com.affymetrix.igb.thresholding.ThresholdingAction".equals(genericAction.getId())) {
					simpleGraphTabGUI.sgt.setThresholdAction(genericAction);
				}
			}
		});
		return simpleGraphTabGUI;
	}
}
