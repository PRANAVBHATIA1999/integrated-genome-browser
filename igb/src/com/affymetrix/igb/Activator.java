package com.affymetrix.igb;

import java.util.Arrays;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.affymetrix.common.ExtensionPointHandler;
import com.affymetrix.genometryImpl.event.TierMaintenanceListener;
import com.affymetrix.genometryImpl.event.TierMaintenanceListenerHolder;
import com.affymetrix.genometryImpl.operator.annotation.AnnotationOperator;
import com.affymetrix.genometryImpl.operator.annotation.AnnotationOperatorHolder;
import com.affymetrix.genometryImpl.operator.graph.GraphOperator;
import com.affymetrix.genometryImpl.parsers.NibbleResiduesParser;
import com.affymetrix.igb.glyph.GlyphProcessorHolder;
import com.affymetrix.igb.glyph.MapViewModeHolder;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import com.affymetrix.igb.tiers.TrackClickHolder;
import com.affymetrix.igb.window.service.IWindowService;
import com.affymetrix.igb.shared.ExtendedMapViewGlyphFactoryI;
import com.affymetrix.igb.shared.GlyphProcessor;
import com.affymetrix.igb.shared.TrackClickListener;

/**
 * OSGi Activator for igb bundle
 */
public class Activator implements BundleActivator {
	protected BundleContext bundleContext;
    String[] args;

	@Override
	public void start(BundleContext _bundleContext) throws Exception {
		this.bundleContext = _bundleContext;
        args = new String[]{};
        if (bundleContext.getProperty("args") != null) {
        	args = bundleContext.getProperty("args").split("[ ]*,[ ]*");
			if ("-convert".equals(args[0])) {
				String[] runArgs = Arrays.copyOfRange(args, 1, args.length);
				NibbleResiduesParser.main(runArgs);
				System.exit(0);
			}
        }
		// Verify jidesoft license.
		com.jidesoft.utils.Lm.verifyLicense("Dept. of Bioinformatics and Genomics, UNCC",
			"Integrated Genome Browser", ".HAkVzUi29bDFq2wQ6vt2Rb4bqcMi8i1");
    	ServiceReference windowServiceReference = bundleContext.getServiceReference(IWindowService.class.getName());

        if (windowServiceReference != null)
        {
        	run(windowServiceReference);
        }
        else
        {
        	ServiceTracker serviceTracker = new ServiceTracker(bundleContext, IWindowService.class.getName(), null) {
        	    public Object addingService(ServiceReference windowServiceReference) {
        	    	run(windowServiceReference);
        	        return super.addingService(windowServiceReference);
        	    }
        	};
        	serviceTracker.open();
        }
	}

	@Override
	public void stop(BundleContext _bundleContext) throws Exception {}

	/**
	 * method to start IGB, called when the window service is available,
	 * creates and initializes IGB and registers the IGBService
	 * add any extension points handling here
	 * @param windowServiceReference - the OSGi ServiceReference for the window service
	 */
	private void run(ServiceReference windowServiceReference) {
        IWindowService windowService = (IWindowService) bundleContext.getService(windowServiceReference);
        final IGB igb = new IGB();
        igb.init(args);
        final IGBTabPanel[] tabs = igb.setWindowService(windowService);
        // set IGBService
		bundleContext.registerService(IGBService.class.getName(), IGBServiceImpl.getInstance(), new Properties());
		// register tabs created in IGB itself - IGBTabPanel is an extension point
		for (IGBTabPanel tab : tabs) {
			bundleContext.registerService(IGBTabPanel.class.getName(), tab, new Properties());
		}
		ExtensionPointHandler.addExtensionPoint(bundleContext,
			new ExtensionPointHandler(TrackClickListener.class) {
				@Override
				public void addService(Object o) {
					TrackClickHolder.getInstance().addTrackClickListener((TrackClickListener)o);
				}
				@Override
				public void removeService(Object o) {}
			}
		);
		ExtensionPointHandler.addExtensionPoint(bundleContext,
			new ExtensionPointHandler(GlyphProcessor.class) {
				@Override
				public void addService(Object o) {
					GlyphProcessorHolder.getInstance().addGlyphProcessor((GlyphProcessor)o);
				}
				@Override
				public void removeService(Object o) {
					GlyphProcessorHolder.getInstance().removeGlyphProcessor((GlyphProcessor)o);
				}
			}
		);
		ExtensionPointHandler.addExtensionPoint(bundleContext,
			new ExtensionPointHandler(TierMaintenanceListener.class) {
				@Override
				public void addService(Object o) {
					TierMaintenanceListenerHolder.getInstance().addTierMaintenanceListener((TierMaintenanceListener)o);
				}
				@Override
				public void removeService(Object o) {
					TierMaintenanceListenerHolder.getInstance().removeTierMaintenanceListener((TierMaintenanceListener)o);
				}
			}
		);
		ExtensionPointHandler.addExtensionPoint(bundleContext,
			new ExtensionPointHandler(AnnotationOperator.class) {
				@Override
				public void addService(Object o) {
					AnnotationOperatorHolder.getInstance().addAnnotationOperator((AnnotationOperator)o);
				}
				@Override
				public void removeService(Object o) {
					AnnotationOperatorHolder.getInstance().removeAnnotationOperator((AnnotationOperator)o);
				}
			}
		);
		ExtensionPointHandler.addExtensionPoint(bundleContext,
			new ExtensionPointHandler(ExtendedMapViewGlyphFactoryI.class) {
				@Override
				public void addService(Object o) {
					MapViewModeHolder.getInstance().addViewFactory((ExtendedMapViewGlyphFactoryI)o);
				}
				@Override
				public void removeService(Object o) {
					MapViewModeHolder.getInstance().removeViewFactory((ExtendedMapViewGlyphFactoryI)o);
				}
			}
		);
		ExtensionPointHandler.addExtensionPoint(bundleContext,
			new ExtensionPointHandler(GraphOperator.class) {
				@Override
				public void addService(Object o) {
					igb.getMapView().addGraphOperator((GraphOperator)o);
				}
				@Override
				public void removeService(Object o) {
					igb.getMapView().removeGraphOperator((GraphOperator)o);
				}
			}
		);
	}
}
