package com.affymetrix.searchmodesymmetryfilter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceRegistration;

import com.affymetrix.genometry.filter.SymmetryFilterProps;
import com.affymetrix.igb.service.api.IGBService;
import com.affymetrix.igb.service.api.XServiceRegistrar;
import com.affymetrix.igb.shared.ISearchModeSym;
import org.osgi.framework.BundleContext;

public class Activator extends XServiceRegistrar<IGBService> implements BundleActivator {

    public Activator() {
        super(IGBService.class);
    }

    @Override
    protected ServiceRegistration<?>[] getServices(BundleContext bundleContext, IGBService igbService) throws Exception {
        return new ServiceRegistration[]{
            bundleContext.registerService(ISearchModeSym.class, new SearchModeSymmetryFilter(igbService, new SymmetryFilterProps(), 2000), null)
        };
    }
}
