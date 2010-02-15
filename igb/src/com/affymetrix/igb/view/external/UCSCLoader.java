package com.affymetrix.igb.view.external;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Returns location image string from the UCSC genome browser.
 * 
 * 
 * @author Ido M. Tamir
 */
public class UCSCLoader extends BrowserLoader {

	private static final Pattern fileNamePattern = Pattern.compile("(hgt_genome.*gif)");

	public String getUrlForView(String query, int pixWidth) {

		if (query.startsWith("db")) {
			String width = "pix=" + pixWidth + "&";
			return "http://genome.ucsc.edu/cgi-bin/hgTracks?" + width + query;
		} else {
			return query;
		}
	}

	public Image getImage(String query, int pixWidth, Map<String, String> cookies) {
		String url = getUrlForView(query, pixWidth);
		if (url.startsWith("http")) {
			url = getImageUrl(url, UCSCView.UCSCUSERID + "=" + cookies.get(UCSCView.UCSCUSERID), new UCSCURLFinder());
			if (url.startsWith("http")) {
				try {
					return ImageIO.read(new URL(url));
				} catch (IOException e) {
					Logger.getLogger(UCSCLoader.class.getName()).log(Level.FINE, "url was : " + url, e);
				}
			}
		}
		return createErrorImage(url, pixWidth);
	}

	class UCSCURLFinder implements URLFinder {

		public String findUrl(BufferedReader reader) throws IOException {
			String inputLine = "";
			while ((inputLine = reader.readLine()) != null) {
				Matcher m = fileNamePattern.matcher(inputLine);
				if (m.find() && m.groupCount() == 1) {
					Logger.getLogger(UCSCLoader.class.getName()).log(Level.FINE, "found fileName " + inputLine);
					String fileName = m.group(1);
					return "http://genome.ucsc.edu/trash/hgt/" + fileName;
				}
			}
			return "Error: could not find image URL in page";
		}
	}
}
