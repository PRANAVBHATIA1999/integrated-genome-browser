package com.affymetrix.igb.swing.bundle;

import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.common.ExtensionPointListener;
import com.affymetrix.igb.swing.JRPWidgetDecorator;
import com.affymetrix.igb.swing.script.ScriptManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * OSGi Activator for genoviz bundle
 */
public class Activator implements BundleActivator {

    private ServiceRegistration<ScriptManager> scriptManagerServiceReference;

    @Override
    public void start(BundleContext bundleContext) throws Exception {

        ExtensionPointHandler<JRPWidgetDecorator> extensionPoint = ExtensionPointHandler.getOrCreateExtensionPoint(bundleContext, JRPWidgetDecorator.class);
        extensionPoint.addListener(new ExtensionPointListener<JRPWidgetDecorator>() {

            @Override
            public void removeService(JRPWidgetDecorator decorator) {
            }

            @Override
            public void addService(JRPWidgetDecorator decorator) {
                ScriptManager.getInstance().addDecorator(decorator);
            }
        });
        scriptManagerServiceReference = bundleContext.registerService(ScriptManager.class, ScriptManager.getInstance(), null);
    }

    @Override
    public void stop(BundleContext _bundleContext) throws Exception {
        if (scriptManagerServiceReference != null) {
            scriptManagerServiceReference.unregister();
            scriptManagerServiceReference = null;
        }
    }
}
