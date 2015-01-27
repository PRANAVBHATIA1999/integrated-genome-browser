package com.gene.findannotations;

import org.osgi.framework.BundleActivator;

import com.affymetrix.igb.service.api.IGBService;
import com.affymetrix.igb.service.api.IgbTabPanel;
import com.affymetrix.igb.window.service.WindowActivator;
import org.osgi.framework.BundleContext;

public class Activator extends WindowActivator implements BundleActivator {

    @Override
    protected IgbTabPanel getPage(BundleContext bundleContext, IGBService igbService) {
        return new FindAnnotationsView(igbService);
    }
}
