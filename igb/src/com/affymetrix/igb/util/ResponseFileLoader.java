package com.affymetrix.igb.util;

import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.igb.action.RefreshDataAction;
import com.affymetrix.igb.bookmarks.UnibrowControlServlet;
import com.affymetrix.igb.menuitem.LoadFileAction;
import java.io.BufferedReader;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jnicol
 * Parse actions from IGB response file.
 */
public class ResponseFileLoader {

	public static File getResponseFile(String[] args) {
		for (int i=0;i<args.length;i++) {
			System.err.println("arg " + args[i]);
			if (args[i].equalsIgnoreCase("-batch")) {
				if (i+1 < args.length) {
					File f = new File(args[i+1]);
					if (!f.exists()) {
						Logger.getLogger(ResponseFileLoader.class.getName()).severe("File " + f.toString() + " does not exist.");
						return null;
					}
					return f;
				} else {
					Logger.getLogger(ResponseFileLoader.class.getName()).severe("File was not specified.");
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Read out and execute the actions from the stream.
	 * @param bis
	 */
	public static void doActions(BufferedReader br) {
		try {
			Thread.sleep(5000);	// give it time to init
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split("\\t");
				System.out.println("parsing line " + line + " with field len" + fields.length);
				doSingleAction(fields);
			}
		} catch (Exception ex) {
			Logger.getLogger(ResponseFileLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void doSingleAction(String[] fields) {
		String action = fields[0].toLowerCase();
		if (action.equals("genome") && fields.length == 2) {
			// go to genome
			goToGenome(fields[1]);
			return;
		}
		if (action.equals("goto") && fields.length == 2) {
			// go to region
			goToRegion(fields[1]);
			return;
		}
		if (action.equals("load")) {
			if (fields.length == 2) {
				loadFile(fields[1]);
				return;
			}
			if (fields.length == 3) {
				loadData(fields[1], fields[2]);
				return;
			}
		}
		if (action.equals("refresh")) {
			RefreshDataAction.getAction().actionPerformed(null);
		}
	}

	private static void goToGenome(String genomeVersion) {
		UnibrowControlServlet.determineAndSetGroup(genomeVersion);
	}

	private static void goToRegion(String region) {

	}

	private static void loadData(String serverType, String URIorFeature) {
		if (serverType.equalsIgnoreCase("file")) {
			loadFile(URIorFeature);
		}
		if (serverType.equalsIgnoreCase("quickload")) {
			//loadQuickLoad(URIorFeature);
		}
		if (serverType.equalsIgnoreCase("das")) {
			//loadDAS(URIorFeature):
		}
		if (serverType.equalsIgnoreCase("das2")) {
			//loadDAS(URIorFeature):
		}
	}

	private static void loadFile(String fileName) {
		File f = new File(fileName);
		LoadFileAction.openURI(f.toURI(), f.getName(), true, GenometryModel.getGenometryModel().getSelectedSeqGroup());
	}

}
