package com.affymetrix.igb.window.service;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceRegistration;

import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.osgi.service.ServiceRegistrar;
import org.osgi.framework.BundleContext;

/**
 * This is the main Activator for all tab panel bundles.
 * Those bundles have an Activator that extends this class
 * and they only need to implement the getPage() method
 */
public abstract class WindowActivator extends ServiceRegistrar implements BundleActivator {

	@Override
	protected ServiceRegistration<?>[] registerService(BundleContext bundleContext, IGBService igbService) throws Exception {
		return new ServiceRegistration[] {
			bundleContext.registerService(IGBTabPanel.class, getPage(bundleContext, igbService), null),	
		};
	}
	
	/**
	 * get the tab panel for the bundle
	 * @param igbService the IGBService implementation
	 * @return the tab panel
	 */
	protected abstract IGBTabPanel getPage(BundleContext bundleContext, IGBService igbService);

}
