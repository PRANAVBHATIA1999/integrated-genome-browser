package com.affymetrix.igb.tutorial;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import javax.swing.JMenuItem;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.affymetrix.genometryImpl.event.GenericActionHolder;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genoviz.swing.AMenuItem;
import com.affymetrix.genoviz.swing.recordplayback.JRPMenu;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.SimpleServiceRegistrar;
import com.affymetrix.igb.osgi.service.XServiceRegistrar;
import com.affymetrix.igb.window.service.IWindowService;

public class Activator extends SimpleServiceRegistrar implements BundleActivator {
	
	private static final String DEFAULT_PREFS_TUTORIAL_RESOURCE = "/tutorial_default_prefs.xml";
	private static final Logger ourLogger = Logger.getLogger(Activator.class.getPackage().getName());

	/**
	 * Load default prefs from jar (with Preferences API).
	 * This will be the standard method soon.
	 */
	private void loadDefaultTutorialPrefs() {

		InputStream default_prefs_stream = null;
		try {
			ourLogger.log(Level.INFO, "loading default tutorial preferences from: {0}",
					DEFAULT_PREFS_TUTORIAL_RESOURCE);
			default_prefs_stream = Activator.class.getResourceAsStream(DEFAULT_PREFS_TUTORIAL_RESOURCE);
			Preferences.importPreferences(default_prefs_stream);
			//prefs_parser.parse(default_prefs_stream, "", prefs_hash);
		} catch (InvalidPreferencesFormatException ex) {
			ourLogger.log(Level.SEVERE, DEFAULT_PREFS_TUTORIAL_RESOURCE, ex);
		} catch (IOException ex) {
			ourLogger.log(Level.SEVERE,	DEFAULT_PREFS_TUTORIAL_RESOURCE, ex);
			ourLogger.log(Level.INFO, "          continuing...");
		} finally {
			GeneralUtils.safeClose(default_prefs_stream);
		}
	}

	private Preferences getTopNode() {
		return Preferences.userRoot().node("/com/affymetrix/igb");
	}

	private Preferences getTutorialsNode() {
		return getTopNode().node("tutorials");
	}
	
	private void initActions(){
		TweeningZoomAction.getAction();
		VerticalStretchZoomAction.getAction();
	}
	
	private ServiceRegistration<?>[] registerService(final BundleContext bundleContext,
			final IGBService igbService, final IWindowService windowService) throws Exception {
		final TutorialManager tutorialManager = new TutorialManager(igbService, windowService);
		GenericActionHolder.getInstance().addGenericActionListener(tutorialManager);
		JRPMenu tutorialMenu = new JRPMenu("Tutorial_tutorialMenu", "Tutorials");
		Properties tutorials = new Properties();
		Preferences tutorialsNode = getTutorialsNode();
		
		try {
			for (String key : tutorialsNode.keys()) {
				String tutorialUri = tutorialsNode.get(key, null);
				tutorials.clear();
				tutorials.load(new URL(tutorialUri + "/tutorials.properties").openStream());
				Enumeration<?> tutorialNames = tutorials.propertyNames();
				while (tutorialNames.hasMoreElements()) {
					String name = (String) tutorialNames.nextElement();
					String description = (String) tutorials.get(name);
					RunTutorialAction rta = new RunTutorialAction(tutorialManager, description, tutorialUri + "/" + name);
					JMenuItem item = new JMenuItem(rta);
					tutorialMenu.add(item);
				}
			}

			return new ServiceRegistration[]{bundleContext.registerService(AMenuItem.class, new AMenuItem(tutorialMenu, "help"), null)};

		} catch (FileNotFoundException fnfe) {
			ourLogger.log(Level.WARNING, "Could not find file {0}.\n          coninuing...", fnfe.getMessage());
		} catch (java.net.ConnectException ce) {
			ourLogger.log(Level.WARNING, "Could not connect: {0}.\n          coninuing...", ce.getMessage());
		}

		return null;
	}
	
	@Override
	protected ServiceRegistration<?>[] getServices(BundleContext bundleContext) throws Exception {
		initActions();
		
		XServiceRegistrar<IGBService> igbServiceRegistrar = new XServiceRegistrar<IGBService>(IGBService.class) {
			
			@Override
			protected ServiceRegistration<?>[] registerService(final BundleContext bundleContext, final IGBService igbService) throws Exception {

				XServiceRegistrar<IWindowService> windowServiceRegistrar = new XServiceRegistrar<IWindowService>(IWindowService.class) {
					
					@Override
					protected ServiceRegistration<?>[] registerService(final BundleContext bundleContext, final IWindowService windowService) throws Exception {
						return Activator.this.registerService(bundleContext, igbService, windowService);
					}
				};
				windowServiceRegistrar.start(bundleContext);
				return new ServiceRegistration[] { bundleContext.registerService(BundleActivator.class, windowServiceRegistrar, null) };
			}
		};

		igbServiceRegistrar.start(bundleContext);
		return new ServiceRegistration[] { bundleContext.registerService(BundleActivator.class, igbServiceRegistrar, null) };
	}
}
