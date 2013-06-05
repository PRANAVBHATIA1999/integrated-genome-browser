package com.gene.tutorialhelper;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceRegistration;

import com.affymetrix.genoviz.swing.recordplayback.JRPWidgetDecorator;
import com.affymetrix.igb.osgi.service.SimpleServiceRegistrar;
import org.osgi.framework.BundleContext;

public class Activator extends SimpleServiceRegistrar implements BundleActivator {

	@Override
	protected ServiceRegistration<?>[] getServices(BundleContext bundleContext) throws Exception {
		return new ServiceRegistration[]{
			bundleContext.registerService(JRPWidgetDecorator.class, new WidgetIdTooltip(), null)
		};
	}
}
