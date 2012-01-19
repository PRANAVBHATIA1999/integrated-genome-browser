package com.affymetrix.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

//import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import com.affymetrix.common.CommonUtils;

/**
 * all OSGi functionality is handled here. Singleton pattern.
 */
public class OSGiHandler {
	private static final ResourceBundle CONFIG_BUNDLE = ResourceBundle.getBundle("config");
	private static final String FORWARD_SLASH = "/";
	private Framework m_fwk;
	private String bundlePathToInstall;
	private String bundleSymbolicNameToUninstall;

	private static OSGiHandler instance = new OSGiHandler();
	public static OSGiHandler getInstance() {
		return instance;
	}

	private OSGiHandler() {
		super();
	}

	public static void main(final String[] args) {
        getInstance().startOSGi(args);
	}

	/**
	 * get the OSGi cache directory
	 * @return the OSGi cache directory
	 */
	private String getCacheDir() {
		return CommonUtils.getInstance().getAppDataDirectory() + "cache/v" + CommonUtils.getInstance().getAppVersionFull() + "-bundle-cache";
	}

	/**
	 * clear the OSGi cache
	 */
	public void clearCache() {
		deleteDirectory(new File(getCacheDir()));
	}

	/**
	 * delete the specified directory, and all its contents
	 * @param path the path of the directory to delete
	 * @return true if and only if the file or directory is successfully deleted; false otherwise
	 */
	private boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	private static FrameworkFactory getFrameworkFactory() throws Exception
    {
		ServiceLoader<FrameworkFactory> factoryLoader =
				ServiceLoader.load(FrameworkFactory.class);
		Iterator<FrameworkFactory> it = factoryLoader.iterator();
		if (!it.hasNext()) {
	        System.err.println("Could not create framework, no OSGi implementation found");
	        System.exit(0);
		}
		return it.next();
	}

	private final void loadFramework(String argArray) {
		try
	    {
			Map<String, String> configProps = new HashMap<String, String>();
			configProps.put("org.osgi.framework.storage", getCacheDir());
			for (String key : CONFIG_BUNDLE.keySet()) {
				configProps.put(key, CONFIG_BUNDLE.getString(key));
			}
			configProps.put("args", argArray);
			FrameworkFactory factory = getFrameworkFactory();
	        m_fwk = factory.newFramework(configProps);
	        m_fwk.init();
//	        AutoProcessor.process(configProps, m_fwk.getBundleContext());
	        m_fwk.start();
	    }
	    catch (Exception ex)
	    {
	        System.err.println("Could not create framework: " + ex);
	        ex.printStackTrace();
	        System.exit(0);
	    }
	}

	/**
	 * start OSGi, load and start the OSGi implementation
	 * load the embedded bundles, if not cached, and start all bundles
	 * @param args the command line arguments
	 */
	public synchronized void startOSGi(String[] args) {
		if (CommonUtils.getInstance().getArg("-cbc", args) != null) { // just clear bundle cache and return
			clearCache();
			return;
		}

		setLaf();

		String argArray = Arrays.toString(args);
		loadFramework(argArray.substring(1, argArray.length() - 1));

        try
        {
            m_fwk.start();
            BundleContext bundleContext = m_fwk.getBundleContext();
            if (bundleContext.getBundles().length <= 1) {
            	loadEmbeddedBundles(bundleContext);
            }
    		uninstallBundles(bundleContext, CommonUtils.getInstance().getArg("-uninstall_bundle", args));
    		installBundles(bundleContext, CommonUtils.getInstance().getArg("-install_bundle", args));
    		for (Bundle bundle : bundleContext.getBundles()) {
    			bundle.start();
    		}
          	Logger.getLogger(getClass().getName()).log(Level.INFO, "OSGi is started with " + m_fwk.getSymbolicName() + " version " + m_fwk.getVersion());
        }
        catch (Exception ex)
        {
        	ex.printStackTrace(System.err);
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Could not create framework, plugins disabled: {0}", ex.getMessage());
        }
    }

	private void uninstallBundles(BundleContext bundleContext, String uninstall_bundle) throws BundleException {
		if (uninstall_bundle != null) {
			for (Bundle bundle : bundleContext.getBundles()) {
				if (uninstall_bundle.equals(bundle.getSymbolicName())) {
					bundle.uninstall();
					Logger.getLogger(getClass().getName()).log(Level.INFO, "uninstalled bundle: {0}", uninstall_bundle);
				}
			}
		}
		if (bundleSymbolicNameToUninstall != null) {
			for (Bundle bundle : bundleContext.getBundles()) {
				if (bundleSymbolicNameToUninstall.equals(bundle.getSymbolicName())) {
					bundle.uninstall();
					Logger.getLogger(getClass().getName()).log(Level.INFO, "uninstalled bundle: {0}", bundleSymbolicNameToUninstall);
				}
			}
		}
	}

	private void installBundles(BundleContext bundleContext, String install_bundle) throws BundleException {
		if (install_bundle != null) {
			Bundle bundle = bundleContext.installBundle(install_bundle);
			if (bundle != null) {
				Logger.getLogger(getClass().getName()).log(Level.INFO, "installed bundle: {0}", install_bundle);
			}
		}
		if (bundlePathToInstall != null) {
			Bundle bundle = bundleContext.installBundle(bundlePathToInstall);
			if (bundle != null) {
				Logger.getLogger(getClass().getName()).log(Level.INFO, "installed bundle: {0}", bundlePathToInstall);
			}
		}
	}

	private void loadEmbeddedBundles(BundleContext bundleContext) throws IOException {
		for (String fileName : getJarFileNames()) {
 			URL locationURL = OSGiHandler.class.getResource(FORWARD_SLASH + fileName);
			if (locationURL != null){
				Logger.getLogger(getClass().getName()).log(Level.INFO, "loading {0}",new Object[]{fileName});
				try {
					bundleContext.installBundle(locationURL.toString());
				}
    	        catch (Exception ex)
    	        {
    	        	ex.printStackTrace(System.err);
					Logger.getLogger(getClass().getName()).log(Level.WARNING, "Could not install {0}",new Object[]{fileName});
    	        }
			}
			else{
				Logger.getLogger(getClass().getName()).log(Level.WARNING, "Could not find {0}",new Object[]{fileName});
			}
		}
	}

	private List<String> getJarFileNames() throws IOException {
		String OSGiImplFile = ResourceBundle.getBundle("main").getString("OSGiImplFile");
        List<String> entries = new ArrayList<String>();
		URL codesource = this.getClass().getProtectionDomain().getCodeSource().getLocation();
		if (codesource.toString().endsWith(".jar")) { // ant exe or webstart
	    	ZipInputStream zipinputstream = null;
	        zipinputstream = new ZipInputStream(codesource.openStream());
	        ZipEntry zipentry = zipinputstream.getNextEntry();
	        while (zipentry != null) 
	        { 
	            //for each entry to be extracted
	            String entryName = zipentry.getName();
	            if (entryName.endsWith(".jar")) {
	            	entries.add(entryName);
	            }
	            File newFile = new File(entryName);
	            String directory = newFile.getParent();
	            
	            if(directory == null)
	            {
	                if(newFile.isDirectory())
	                    break;
	            }
	            zipinputstream.closeEntry();
	            zipentry = zipinputstream.getNextEntry();
	        }//while
	        zipinputstream.close();
		}
		else { // ant run
			File dir = new File("bundles");
			FilenameFilter ff = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			};
			entries = Arrays.asList(dir.list(ff));
		}
		entries.remove(OSGiImplFile); // don't install OSGiImpl as a bundle
        return entries;
	}

	/**
	 * set the Swing look and feel
	 */
	private static void setLaf() {

		// Turn on anti-aliased fonts. (Ignored prior to JDK1.5)
		System.setProperty("swing.aatext", "true");

		// Letting the look-and-feel determine the window decorations would
		// allow exporting the whole frame, including decorations, to an eps file.
		// But it also may take away some things, like resizing buttons, that the
		// user is used to in their operating system, so leave as false.
		JFrame.setDefaultLookAndFeelDecorated(false);

		// if this is != null, then the user-requested l-and-f has already been applied
		if (System.getProperty("swing.defaultlaf") == null) {
			String os = System.getProperty("os.name");
			if (os != null && os.toLowerCase().contains("windows")) {
				try {
					// It this is Windows, then use the Windows look and feel.
					Class<?> cl = Class.forName("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					LookAndFeel look_and_feel = (LookAndFeel) cl.newInstance();

					if (look_and_feel.isSupportedLookAndFeel()) {
						UIManager.setLookAndFeel(look_and_feel);
					}
				} catch (Exception ulfe) {
					// Windows look and feel is only supported on Windows, and only in
					// some version of the jre.  That is perfectly ok.
				}
			}
		}
	}

	public BundleContext getBundleContext() {
		if (m_fwk == null) {
			return null;
		}
		return m_fwk.getBundleContext();
	}

	public boolean installBundle(String filePath) {
		if (m_fwk == null) {
			bundlePathToInstall = filePath;
			return true;
		}
		Bundle bundle = null;
		if (filePath != null) {
			try {
	            BundleContext bundleContext = m_fwk.getBundleContext();
				bundle = bundleContext.installBundle(filePath);
				bundle.start();
			}
			catch(Exception x) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "error installing bundle", x);
				bundle = null;
			}
			if (bundle != null) {
				Logger.getLogger(getClass().getName()).log(Level.INFO, "installed bundle: {0}", filePath);
			}
		}
		return bundle != null;
	}

	public boolean uninstallBundle(String symbolicName) {
		if (m_fwk == null) {
			bundleSymbolicNameToUninstall = symbolicName;
			return true;
		}
		boolean found = false;
		if (symbolicName != null) {
            BundleContext bundleContext = m_fwk.getBundleContext();
			for (Bundle bundle : bundleContext.getBundles()) {
				if (symbolicName.equals(bundle.getSymbolicName())) {
					try {
						bundle.uninstall();
						Logger.getLogger(getClass().getName()).log(Level.INFO, "uninstalled bundle: {0}", symbolicName);
						found = true;
					}
					catch(Exception x) {
						Logger.getLogger(getClass().getName()).log(Level.SEVERE, "error uninstalling bundle", x);
						found = false;
					}
				}
			}
		}
		return found;
	}
}
