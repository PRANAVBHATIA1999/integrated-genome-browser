package com.affymetrix.igb.util;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.general.GenericFeature;
import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.util.ErrorHandler;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.igb.Application;
import com.affymetrix.igb.IGBConstants;
import com.affymetrix.igb.action.RefreshDataAction;
import com.affymetrix.igb.bookmarks.UnibrowControlServlet;
import com.affymetrix.igb.general.ServerList;
import com.affymetrix.igb.menuitem.LoadFileAction;
import com.affymetrix.igb.view.MapRangeBox;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jnicol
 * Parse actions from IGB response file.
 */
public class ScriptFileLoader {
	private static String splitter = "\\s";
	public static String getScriptFileStr(String[] args) {
		if (args == null) {
			return null;
		}
		for (int i=0;i<args.length;i++) {
			if (args[i].equalsIgnoreCase("-" + IGBConstants.SCRIPTFILETAG)) {
				if (i+1 < args.length) {
					return args[i+1];
				} else {
					Logger.getLogger(ScriptFileLoader.class.getName()).severe("File was not specified.");
					return null;
				}
			}
		}
		return null;
	}

	public static void doActions(String batchFileStr) {
		if (batchFileStr == null || batchFileStr.length() == 0) {
			Logger.getLogger(ScriptFileLoader.class.getName()).log(
					Level.SEVERE, "Couldn't find response file: " + batchFileStr);
			return;
		}
		// A response file was requested.  Run response file parser, and ignore any other parameters.
		File f = new File(batchFileStr);
		if (!f.exists()) {
			URI uri = URI.create(batchFileStr);
			if (uri == null) {
				Logger.getLogger(ScriptFileLoader.class.getName()).log(
					Level.SEVERE, "Not a valid script file: " + batchFileStr);
				return;
			}
			f = LocalUrlCacher.convertURIToFile(uri);
		}
		if (f == null || !f.exists()) {
			Logger.getLogger(ScriptFileLoader.class.getName()).log(
					Level.SEVERE, "Couldn't find response file: " + batchFileStr);
			return;
		}
		
		ScriptFileLoader.doActions(f);
	}

	/**
	 * Read and execute the actions from a file.
	 * @param bis
	 */
	private static void doActions(File f) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
			ScriptFileLoader.doActions(br);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ScriptFileLoader.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			GeneralUtils.safeClose(br);
		}
	}


	/**
	 * Read and execute the actions from the stream.
	 * @param bis
	 */
	private static void doActions(BufferedReader br) {
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				Logger.getLogger(ScriptFileLoader.class.getName()).info("line: " + line);
				doSingleAction(line);
				Thread.sleep(1000);	// user actions don't happen instantaneously, so give a short sleep time between batch actions.
			}
		} catch (Exception ex) {
			Logger.getLogger(ScriptFileLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void doSingleAction(String line) {
		String[] fields = line.split(splitter);
		String action = fields[0].toLowerCase();
		if (action.equals("genome") && fields.length >= 2) {
			// go to genome
			goToGenome(join(fields,1));
			return;
		}
		if (action.equals("goto") && fields.length >= 2) {
			// go to region
			goToRegion(join(fields,1));
			return;
		}
		if (action.equals("load")) {
			// Allowing multiple files to be specified, split by commas
			String[] loadFiles = join(fields,1).split(",");
			for (int i=0;i<loadFiles.length;i++) {
				if (i > 0) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ex) {
						Logger.getLogger(ScriptFileLoader.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				loadFile(loadFiles[i]);
			}
			return;
		}
		if (action.equals("loadfromserver")) {
			if (fields.length >= 4) {
				loadData(fields[1], fields[2], join(fields,3));
				return;
			}
		}
		if (action.equals("loadmode")) {
			if (fields.length >=2) {
				loadMode(fields[1], join(fields,2));
			}
		}
		if (action.equals("print")) {
			if (fields.length == 1) {
				try {
					Application.getSingleton().getMapView().getSeqMap().print(0, true);
				} catch (Exception ex) {
					ErrorHandler.errorPanel("Problem trying to print.", ex);
				}
				return;
			}
		}
		if (action.equals("refresh")) {
			RefreshDataAction.getAction().actionPerformed(null);
		}
		if (action.equals("select") && fields.length>=2) {
			UnibrowControlServlet.performSelection(fields[1]);
		}
		if (action.equals("sleep") && fields.length == 2) {
			try {
				int sleepTime = Integer.parseInt(fields[1]);
				Thread.sleep(sleepTime);
			} catch (Exception ex) {
				Logger.getLogger(ScriptFileLoader.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private static void goToGenome(String genomeVersion) {
		AnnotatedSeqGroup group = UnibrowControlServlet.determineAndSetGroup(genomeVersion);
		if (group == null) {
			return;
		}
		for (int i=0;i<100;i++) {
			// sleep until versions are initialized
			for (GenericVersion version: group.getEnabledVersions()) {
				if (version.isInitialized() && group == GenometryModel.getGenometryModel().getSelectedSeqGroup()) {
					continue;
				}
				try {
					Thread.sleep(300); // not finished initializing versions
				} catch (InterruptedException ex) {
					Logger.getLogger(ScriptFileLoader.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	private static void goToRegion(String region) {
		MapRangeBox.setRange(Application.getSingleton().getMapView(), region);
	}

	private static void loadData(String serverType, String serverURIorName, String feature) {
		/*ServerType sType = ServerType.LocalFiles;
		if (serverType.equalsIgnoreCase("quickload")) {
			sType = ServerType.QuickLoad;
			//loadQuickLoad(URIorFeature);
		} else if (serverType.equalsIgnoreCase("das")) {
			sType = ServerType.DAS;
			//loadDAS(URIorFeature):
		} else if (serverType.equalsIgnoreCase("das2")) {
			sType = ServerType.DAS2;
			//loadDAS(URIorFeature):
		}*/
		GenericServer server = ServerList.getServer(serverURIorName);
		if (server != null) {
			
		} else {
			Logger.getLogger(ScriptFileLoader.class.getName()).severe("Couldn't find server :" + serverURIorName);
		}

	}

	private static void loadFile(String fileName) {
		File f = new File(fileName.trim());
		LoadFileAction.openURI(f.toURI(), f.getName(), true, GenometryModel.getGenometryModel().getSelectedSeqGroup());
	}

	private static void loadMode(String loadMode, String featureURIStr) {
		URI featureURI = null;
		File featureFile = new File(featureURIStr.trim());
		if (featureFile.exists()) {
			featureURI = featureFile.toURI();
		} else {
			featureURI = URI.create(featureURIStr.trim());
		}
		LoadStrategy s = LoadStrategy.NO_LOAD;
		if (loadMode.equalsIgnoreCase("no_load")) {
			s = LoadStrategy.NO_LOAD;
		} else if (loadMode.equalsIgnoreCase("visible")) {
			s = LoadStrategy.VISIBLE;
		} else if (loadMode.equalsIgnoreCase("chromosome")) {
			s = LoadStrategy.CHROMOSOME;
		} else if (loadMode.equalsIgnoreCase("genome")) {
			s = LoadStrategy.GENOME;
		}
		GenericFeature feature = ServerList.findFeatureWithURI(featureURI);
		if (feature != null) {
			feature.loadStrategy = s;
		} else {
			Logger.getLogger(ScriptFileLoader.class.getName()).severe("Couldn't find feature :" + featureURIStr);
		}
	}

	/**
	 * Join fields from startField to end of fields.
	 * @param fields
	 * @param startField
	 * @return
	 */
	private static String join(String[] fields, int startField) {
		StringBuilder buffer = new StringBuilder("");
		for(int i=startField;i<fields.length;i++) {
			buffer.append(fields[i]);
		}
		return buffer.toString();
	}

}
