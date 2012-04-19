package com.affymetrix.igb.thresholding;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.event.GenericAction;
import com.affymetrix.genoviz.swing.MenuUtil;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenuItem;
import com.affymetrix.igb.osgi.service.IGBService;

public class Activator implements BundleActivator {
	private BundleContext bundleContext;
	private ServiceRegistration<GenericAction> thresholdingActionRegistration;
	private SelectionListener selectionListener;

	private void registerServices(IGBService igbService) {
		ThresholdingAction thresholdingAction = new ThresholdingAction(igbService);
		thresholdingActionRegistration = bundleContext.registerService(GenericAction.class, thresholdingAction, null);
		JRPMenuItem menuItem = new JRPMenuItem("Thresholding_thresholding", thresholdingAction);
		MenuUtil.addToMenu(igbService.getMenu("tools"), menuItem);
		selectionListener = new SelectionListener(igbService, thresholdingAction);
		GenometryModel.getGenometryModel().addSeqSelectionListener(selectionListener);
		GenometryModel.getGenometryModel().addSymSelectionListener(selectionListener);
	}

	@Override
	public void start(BundleContext bundleContext_) throws Exception {
		this.bundleContext = bundleContext_;
    	ServiceReference<IGBService> igbServiceReference = bundleContext.getServiceReference(IGBService.class);

        if (igbServiceReference != null)
        {
        	IGBService igbService = bundleContext.getService(igbServiceReference);
        	registerServices(igbService);
        }
        else
        {
        	ServiceTracker<IGBService,Object> serviceTracker = new ServiceTracker<IGBService,Object>(bundleContext, IGBService.class, null) {
        	    public Object addingService(ServiceReference<IGBService> igbServiceReference) {
                	IGBService igbService = bundleContext.getService(igbServiceReference);
                   	registerServices(igbService);
                    return super.addingService(igbServiceReference);
        	    }
        	};
        	serviceTracker.open();
        }
    }

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		thresholdingActionRegistration.unregister();
		selectionListener = null;
	}
}
