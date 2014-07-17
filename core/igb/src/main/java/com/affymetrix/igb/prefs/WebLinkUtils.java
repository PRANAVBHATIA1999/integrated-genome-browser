package com.affymetrix.igb.prefs;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.PreferenceUtils;
import com.affymetrix.genometryImpl.weblink.WebLink;
import com.affymetrix.genometryImpl.weblink.WebLinkList;
import com.affymetrix.igb.parsers.XmlPrefsParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import org.w3c.dom.Element;

/**
 *
 * @author hiralv
 */
public class WebLinkUtils {
	private static final String separator = System.getProperty("line.separator");
	private static final String FILE_NAME = "weblinks.xml";	// Name of the xml file used to store the web links data.
	static final WebLinkList LOCAL_WEBLINK_LIST = new WebLinkList("local", true);
	static final WebLinkList SERVER_WEBLINK_LIST = new WebLinkList("default", false);
	
	/**
	 *  Returns the file that is used to store the user-edited web links.
	 */
	private static File getLinksFile() {
		return new File(PreferenceUtils.getAppDataDirectory(), FILE_NAME);
	}
	
	/**
	 *  Loads links from the file specified by {@link #getLinksFile()}.
	 */
	public static void autoLoad() {
		File f = getLinksFile();
		if (f == null || !f.exists()) {
			return;
		}
		String filename = f.getAbsolutePath();
		try {
			Logger.getLogger(WebLinkUtils.class.getName()).log(Level.INFO,
					"Loading web links from file \"{0}\"", filename);

			importWebLinks(f);
		} catch (Exception ioe) {
			Logger.getLogger(WebLinkUtils.class.getName()).log(Level.SEVERE,
					"Could not load web links from file \"{0}\"", filename);
		}
	}

	/** Save the current web links into the file that was specified
	 *  by {@link #getLinksFile()}.
	 *  @return true for sucessfully saving the file
	 */
	public static boolean autoSave() {
		File f = getLinksFile();
		String filename = f.getAbsolutePath();
		try {
			Logger.getLogger(WebLinkUtils.class.getName()).log(Level.INFO,
					"Saving web links to file \"{0}\"", filename);
			File parent_dir = f.getParentFile();
			if (parent_dir != null) {
				parent_dir.mkdirs();
			}
			exportWebLinks(f, true);
			return true;
		} catch (IOException ioe) {
			Logger.getLogger(WebLinkUtils.class.getName()).log(Level.SEVERE,
					"Error while saving web links to \"{0}\"", filename);
		}
		return false;
	}
	
	public static void importWebLinks(File f) throws FileNotFoundException, IOException {
		XmlPrefsParser.parse(new FileInputStream(f));
	}

	public static void exportWebLinks(File f, boolean include_warning) throws IOException {
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {

			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
			bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			bw.write(separator);
			bw.write("");
			bw.write(separator);
			bw.write("<!--");
			bw.write(separator);
			bw.write("  This file was generated by " + CommonUtils.getInstance().getAppName() + " " + CommonUtils.getInstance().getAppVersion() + "\n");
			bw.write(separator);
			if (include_warning) {
				bw.write("  WARNING: This file is automatically created by the application.");
				bw.write(separator);
				bw.write("  Edit the Web-Links from inside the application.");
				bw.write(separator);
			}
			bw.write("-->");
			bw.write(separator);
			bw.write("");
			bw.write(separator);
			bw.write("<prefs>");
			bw.write(separator);

			for (WebLink link : getLocalList().getWebLinkList()) {
				String xml = link.toXML();
				bw.write(xml);
				bw.write(separator);
			}

			bw.write("</prefs>");
			bw.write(separator);
			bw.write(separator);
		} finally {
			GeneralUtils.safeClose(bw);
			GeneralUtils.safeClose(fw);
		}
	}
	
	public static WebLinkList getServerList() {
		return SERVER_WEBLINK_LIST;
	}

	public static WebLinkList getLocalList() {
		return LOCAL_WEBLINK_LIST;
	}

	public static WebLinkList getWebLinkList(String type) {
		if (SERVER_WEBLINK_LIST.getName().equalsIgnoreCase(type)) {
			return SERVER_WEBLINK_LIST;
		}
		return LOCAL_WEBLINK_LIST;
	}

	public static class WeblinkElementHandler implements XmlPrefsParser.ElementHandler {

		/**
		 * Sets up a regular-expression matching between a method name or id and
		 * a url, which can be used, for example, in SeqMapView to "get more
		 * info" about an item. For example: <p>
		 * <code>&gt;annotation_url annot_type_regex="google" match_case="false" url="http://www.google.com/search?q=$$" /&lt;</code>
		 * <code>&gt;annotation_url annot_id_regex="^AT*" match_case="false" url="http://www.google.com/search?q=$$" /&lt;</code>
		 * <p> Note that the url can contain "$$" which will later be
		 * substituted with the "id" of the annotation to form a link. By
		 * default, match is case-insensitive; use match_case="true" if you want
		 * to require an exact match.
		 */
		@Override
		public void processElement(Element el) {

			String url = el.getAttribute("url");
			if (url == null || url.trim().length() == 0) {
				System.out.println("ERROR: Empty data in preferences file for an 'annotation_url':" + el.toString());
				return;
			}

			WebLink.RegexType type_regex = WebLink.RegexType.TYPE;
			String annot_regex_string = el.getAttribute("annot_type_regex");
			if (annot_regex_string == null || annot_regex_string.trim().length() == 0) {
				type_regex = WebLink.RegexType.ID;
				annot_regex_string = el.getAttribute("annot_id_regex");
			}
			if (annot_regex_string == null || annot_regex_string.trim().length() == 0) {
				System.out.println("ERROR: Empty data in preferences file for an 'annotation_url':" + el.toString());
				return;
			}

			String name = el.getAttribute("name");
			String species = el.getAttribute("species");
			String IDField = el.getAttribute("id_field");
			String type = el.getAttribute("type");
			if (type == null) {
				type = WebLink.LOCAL;
			}
			WebLink link = new WebLink();
			link.setRegexType(type_regex);
			link.setName(name);
			link.setIDField(IDField);
			link.setUrl(url);
			link.setType(type);
			link.setSpeciesName(species);
			try {
				if ("false".equalsIgnoreCase(el.getAttribute("match_case"))) {
					link.setRegex("(?-i)" + annot_regex_string);
				} else {
					link.setRegex(annot_regex_string);
				}
			} catch (PatternSyntaxException pse) {
				System.out.println("ERROR: Regular expression syntax error in preferences\n" + pse.getMessage());
			}
			WebLinkUtils.getWebLinkList(type).addWebLink(link);
		}

		@Override
		public String getElementTag() {
			return "annotation_url";
		}
	
	}
}
