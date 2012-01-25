package com.affymetrix.igb.searchmodeidorprops;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.shared.ISearchModeSym;

public class Activator implements BundleActivator {
	private BundleContext bundleContext;
	private ServiceRegistration<ISearchModeSym> searchModeIDRegistration;
	private ServiceRegistration<ISearchModeSym> searchModePropsRegistration;
	private ServiceRegistration<RemoteSearchI> remoteSearchDAS2Registration;

	private void registerService(ServiceReference<IGBService> igbServiceReference) {
        try
        {
        	IGBService igbService = bundleContext.getService(igbServiceReference);
    		searchModeIDRegistration = bundleContext.registerService(ISearchModeSym.class, new SearchModeID(igbService), null);
    		searchModePropsRegistration = bundleContext.registerService(ISearchModeSym.class, new SearchModeProps(igbService), null);
    		remoteSearchDAS2Registration = bundleContext.registerService(RemoteSearchI.class, new RemoteSearchDAS2(), null);
        }
        catch (Exception ex) {
            System.out.println(this.getClass().getName() + " - Exception in Activator.createPage() -> " + ex.getMessage());
            ex.printStackTrace(System.out);
        }
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		this.bundleContext = bundleContext;
		ExtensionPointHandler.getOrCreateExtensionPoint(bundleContext, RemoteSearchI.class);
    	ServiceReference<IGBService> igbServiceReference = bundleContext.getServiceReference(IGBService.class);

        if (igbServiceReference != null)
        {
        	registerService(igbServiceReference);
        }
        else
        {
        	ServiceTracker<IGBService,Object> serviceTracker = new ServiceTracker<IGBService,Object>(bundleContext, IGBService.class.getName(), null) {
        	    public Object addingService(ServiceReference<IGBService> igbServiceReference) {
        	    	registerService(igbServiceReference);
        	        return super.addingService(igbServiceReference);
        	    }
        	};
        	serviceTracker.open();
        }
    }

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		searchModeIDRegistration.unregister();
		searchModePropsRegistration.unregister();
		remoteSearchDAS2Registration.unregister();
	}
}
