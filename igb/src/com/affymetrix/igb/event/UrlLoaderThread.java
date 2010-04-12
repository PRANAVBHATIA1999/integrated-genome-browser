/**
 *   Copyright (c) 2001-2007 Affymetrix, Inc.
 *
 *   Licensed under the Common Public License, Version 1.0 (the "License").
 *   A copy of the license must be included with any distribution of
 *   this source code.
 *   Distributions from Affymetrix, Inc., place this in the
 *   IGB_LICENSE.html file.
 *
 *   The license is also available at
 *   http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.igb.event;

import java.net.*;
import java.io.*;
import javax.swing.SwingUtilities;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.igb.Application;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.parsers.BpsParser;
import com.affymetrix.genometryImpl.parsers.Das2FeatureSaxParser;
import com.affymetrix.genometryImpl.parsers.PSLParser;
import com.affymetrix.genometryImpl.parsers.das.DASFeatureParser;
import com.affymetrix.genoviz.util.GeneralUtils;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.view.SeqMapView;
import com.affymetrix.igb.menuitem.LoadFileAction;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @version $Id$
 */
public final class UrlLoaderThread extends Thread {

	private static final GenometryModel gmodel = GenometryModel.getGenometryModel();
	private final URL[] urls;
	private final String[] tier_names;
	private final SeqMapView gviewer;
	private final String[] file_extensions;

	/**
	 *  Creates a thread that can be used to load data.
	 *  A ThreadProgressMonitor will be opened to show the user that something is
	 *  happening.
	 *  @param smv The SeqMapView instance to load data into
	 *  @param urls  The URLs that will load data
	 *  @param file_extensions  File extensions, such as ".gff", to help determine
	 *     which parser to use if it is not possible to determine that in any other way.
	 *     It is ok for any of these to be either blank or null.
	 *  @param tier_names  The names for the data tiers.  If you specify <code>null</code>,
	 *  the tier names will be determined from the "type" parameter of each URL.
	 *  If a non-null array is provided, the length must match the length of the
	 *  das_urls array.
	 */
	public UrlLoaderThread(SeqMapView smv, URL[] urls, String[] file_extensions, String[] tier_names) {
		if (tier_names != null && urls.length != tier_names.length) {
			throw new IllegalArgumentException("Array lengths do not match");
		}
		this.gviewer = smv;
		this.urls = urls;
		this.tier_names = tier_names;
		this.file_extensions = file_extensions;
	}

	@Override
	public void run() {
		BioSeq aseq = gmodel.getSelectedSeq();
		AnnotatedSeqGroup seq_group = gmodel.getSelectedSeqGroup();
		try {
			// should really move to using gmodel's currently selected  _group_ of sequences rather than
			//    a single sequence...
			if (aseq == null) {
				throw new RuntimeException("UrlLoaderThread: aborting because there is no currently selected seq");
			}
			if (seq_group == null) {
				throw new RuntimeException("UrlLoaderThread: aborting because there is no currently selected seq group");
			}

			for (int i = 0; i < urls.length; i++) {
				if (isInterrupted()) {
					break;
				}

				URL url = urls[i];


				String tier_name = null;
				if (tier_names != null) {
					tier_name = tier_names[i];
				} else {
					tier_name = parseTermName(url, "DAS_Data");
				}
				String file_extension = null;
				if (file_extensions != null) {
					file_extension = file_extensions[i];
				}

				System.out.println("Attempting to load data from URL: " + url.toExternalForm());
				try {
					parseUrl(url, file_extension, tier_name);
				} catch (IOException ioe) {
					handleException(ioe);
					if (isInterrupted()) {
						return;
					}
					continue; // try the next url
				}
				if (isInterrupted()) {
					return;
				}

				if (isInterrupted()) {
					break;
				}

				// update the view, except for the last time where we let the "finally" block do it
				if (i < urls.length) {
					updateViewer(gviewer, aseq);
				}
			}

		} catch (Exception e) {
			if (!(e instanceof InterruptedException)) {
				handleException(e);
			}
		} finally {
			//if (monitor != null) {monitor.closeDialogEventually();}
			// update the view again, mainly in case the thread was interrupted
			updateViewer(gviewer, aseq);
		}
	}

	private void parseUrl(URL url, String file_extension, String tier_name)
			throws IOException {
		String where_from = url.getHost();
		if (where_from == null || where_from.length() == 0) {
			where_from = url.getPath();
		}
		//Application.getSingleton().setNotLockedUpStatus("Connecting to " + where_from);
		if (isInterrupted()) {
			Application.getSingleton().removeNotLockedUpMsg("Loading feature " + tier_name);
			return;
		}

		parseDataFromURL(gviewer, url, file_extension, tier_name);

		Application.getSingleton().removeNotLockedUpMsg("Loading feature " + tier_name);
	}

	private void handleException(final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				if (e instanceof UnknownHostException) {
					Application.getSingleton().setStatus("Unknown host: " + e.getMessage());
				} else if (e instanceof FileNotFoundException) {
					ErrorHandler.errorPanel(gviewer.getFrame(), "File not found",
							"File missing or not readable:\n " + e.getMessage(), null);
				} else {
					Application.getSingleton().setStatus(e.getMessage());
					e.printStackTrace();
				}
			}
		});
	}

	private void updateViewer(final SeqMapView gviewer, final BioSeq seq) {
		if (gviewer == null || seq == null) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					gviewer.setAnnotatedSeq(seq, true, true);
				} catch (Exception e) {
					handleException(e);
				}
			}
		});
	}

	/** Use SwingUtilities.invokeLater() to schedule this Thread to be
	 *  started later.  This lets Swing finish-up whatever else it was doing
	 *  before calling start() on this Thread.  (The Thread itself doesn't actually
	 *  run on the Swing event thread.)
	 *
	 *  In many cases, you could just call start() instead of calling this.
	 *  But, if you already have some events pending on the Swing event thread,
	 *  then calling this will make sure they finish first.  For example,
	 *  this method is needed in the UnibrowControlServlet when a manipulation
	 *  of the QuickLoad GUI needs to be followed by a file load.
	 */
	public void runEventually() {
		// Note: we do NOT want to simply call SwingUtilities.invokeLater(this)
		// because that would cause this thread to actually run ON the Swing thread
		// (potentially freezing the GUI)
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				start();
			}
		});
	}

	// Parses term names from a "type" string.
	// Returns default_value if parsing fails, or there is no "type" string.
	// Example:  "type=one;two;three"  -> {"one", "two", "three"}
	private static String parseTermName(URL url, String default_value) {
		//TODO: unlike the rest of this class, this IS specific to DAS and should be moved to DasUtils
		String value = null;
		String query = url.getQuery();
		try {
			int index1 = query.indexOf("type=");
			if (index1 > -1) {
				int index1b = index1 + 5;
				int index2 = query.indexOf(';', index1b);
				if (index2 == -1) {
					index2 = query.length();
				}
				value = query.substring(index1b, index2);
			}
		} catch (Exception e) {
			// do nothing.  Just use the default string value
		}
		if (value == null || value.length() == 0) {
			value = default_value;
		}
		return value;
	}

	/**
	 *  Opens a binary data stream from the given url and adds the resulting
	 *  data to the given BioSeq.
	 *  @param type  a parameter passed on to parsePSL
	 */
	private static void parseDataFromURL(SeqMapView gviewer, URL url, String file_extension, String type)
			throws java.net.UnknownHostException, java.io.IOException {

		BioSeq aseq = gmodel.getSelectedSeq();
		AnnotatedSeqGroup seq_group = gmodel.getSelectedSeqGroup();
		//TODO: This is an important part of the data loading code, but it could be improved.

		Map<String, List<String>> respHeaders = new HashMap<String, List<String>>();
		InputStream stream = null;
		List<String> list;
		String content_type = "content/unknown";
		int content_length = -1;

		try {
			stream = LocalUrlCacher.getInputStream(url, false, null, respHeaders);
			list = respHeaders.get("Content-Type");
			if (list != null && !list.isEmpty()) {
				content_type = list.get(0);
			}

			list = respHeaders.get("Content-Length");
			if (list != null && !list.isEmpty()) {
				try {
					content_length = Integer.parseInt(list.get(0));
				} catch (NumberFormatException ex) {
					content_length = -1;
				}
			}

			if (content_length == 0) { // Note: length == -1 means "length unknown"
				throw new IOException("\n" + url + " returned no data.");
			}

			if ("file".equalsIgnoreCase(url.getProtocol()) || "ftp".equalsIgnoreCase(url.getProtocol())) {
				System.out.println("Attempting to load data from file: " + url.toExternalForm());

				// Note: we want the filename so we can guess the filetype from the ending, like ".psl" or ".psl.gz"
				// url.getPath() is OK for this purpose, url.getFile() is not because
				// url.getFile() = url.getPath() + url.getQuery()
				String filename = url.getPath();
				LoadFileAction.load(gviewer.getFrame(), stream, filename, seq_group, aseq);
			} else if (content_type == null
					|| content_type.startsWith("content/unknown")
					|| content_type.startsWith("application/zip")
					|| content_type.startsWith("application/octet-stream")) {
				System.out.println("Attempting to load data from: " + url.toExternalForm());
				System.out.println("Using file extension: " + file_extension);

				String filename = url.getPath();
				if (file_extension != null && !"".equals(file_extension.trim())) {
					if (!file_extension.startsWith(".")) {
						filename += ".";
					}
					filename += file_extension;
				}
				LoadFileAction.load(gviewer.getFrame(), stream, filename, seq_group, aseq);
			} else if (content_type.startsWith("binary/bps")) {
				parseBinaryBps(stream, type);
			} else if (content_type.startsWith(Das2FeatureSaxParser.FEATURES_CONTENT_TYPE)) {
				parseDas2XML(stream, url);
			} else if (content_type.startsWith("text/plain")
					|| content_type.startsWith("text/html")
					|| content_type.startsWith("text/xml")) {
				// Note that some http servers will return "text/html" even when that is untrue.
				// we could try testing whether the filename extension is a recognized extension, like ".psl"
				// and if so passing to LoadFileAction.load(.. feat_request_con.getInputStream() ..)
				parseDas1XML(stream);
			} else if (content_type.startsWith("text/psl")) {
				parsePSL(stream, type);
			} else {
				throw new IOException("Declared data type " + content_type + " cannot be processed");
			}
		} finally {
			GeneralUtils.safeClose(stream);
		}
	}

	/**
	 *  Opens a text input stream from the given url, parses it has a
	 *  PSL file, and then adds the resulting data to the given BioSeq,
	 *  using the parser {@link PSLParser}.
	 */
	private static void parsePSL(InputStream stream, String type)
			throws IOException {
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(stream);
			AnnotatedSeqGroup group = gmodel.getSelectedSeqGroup();
			PSLParser parser = new PSLParser();
			parser.enableSharedQueryTarget(true);
			parser.parse(bis, type, null, group, false, true);
			group.symHashChanged(parser);
		} finally {
			GeneralUtils.safeClose(bis);
		}
	}

	/**
	 *  Opens a text input stream from the given url and adds the resulting
	 *  data to the given BioSeq.
	 */
	private static void parseDas1XML(InputStream stream)
			throws IOException {
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(stream);
			DASFeatureParser das_parser = new DASFeatureParser();
			das_parser.parse(bis, gmodel.getSelectedSeqGroup());
		} catch (XMLStreamException ex) {
			Logger.getLogger(UrlLoaderThread.class.getName()).log(Level.SEVERE, "Unable to parse DAS response", ex);
		} finally {
			GeneralUtils.safeClose(bis);
		}
	}

	/**
	 *  Opens a text input stream from the given url and adds the resulting
	 *  data to the given BioSeq.
	 */
	private static void parseDas2XML(InputStream stream, URL url)
			throws IOException {
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(stream);
			InputSource input_source = new InputSource(bis);

			Das2FeatureSaxParser das_parser = new Das2FeatureSaxParser();
			das_parser.parse(input_source, URI.create(url.toString()).toString(), gmodel.getSelectedSeqGroup(), true);

		} catch (SAXException e) {
			IOException ioe = new IOException("Error parsing DAS2 XML");
			ioe.initCause(e);
			throw ioe;
		} finally {
			GeneralUtils.safeClose(bis);
		}
	}

	/**
	 *  Opens a binary data stream from the given url and adds the resulting
	 *  binary/bps data to the given BioSeq.
	 *  @param type  a parameter passed on to
	 *  {@link BpsParser#parse(DataInputStream, String, AnnotatedSeqGroup, AnnotatedSeqGroup, boolean, boolean)}.
	 */
	private static void parseBinaryBps(InputStream stream, String type)
			throws IOException {
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		try {
			bis = new BufferedInputStream(stream);
			dis = new DataInputStream(bis);

			BpsParser bps_parser = new BpsParser();
			AnnotatedSeqGroup group = gmodel.getSelectedSeqGroup();

			BpsParser.parse(dis, type, null, group, false, true);
			group.symHashChanged(bps_parser);
		} finally {
			GeneralUtils.safeClose(dis);
			GeneralUtils.safeClose(bis);
		}
	}
}
