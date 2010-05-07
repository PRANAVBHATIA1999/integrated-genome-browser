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
import java.awt.HeadlessException;
import javax.swing.*;
import java.net.URI;
import com.affymetrix.genometryImpl.SeqSymmetry;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.parsers.BedParser;
import com.affymetrix.genometryImpl.parsers.BgnParser;
import com.affymetrix.genometryImpl.parsers.Bprobe1Parser;
import com.affymetrix.genometryImpl.parsers.BpsParser;
import com.affymetrix.genometryImpl.parsers.BrptParser;
import com.affymetrix.genometryImpl.parsers.BrsParser;
import com.affymetrix.genometryImpl.parsers.BsnpParser;
import com.affymetrix.genometryImpl.parsers.CytobandParser;
import com.affymetrix.genometryImpl.parsers.Das2FeatureSaxParser;
import com.affymetrix.genometryImpl.parsers.ExonArrayDesignParser;
import com.affymetrix.genometryImpl.parsers.FastaParser;
import com.affymetrix.genometryImpl.parsers.FishClonesParser;
import com.affymetrix.genometryImpl.parsers.GFFParser;
import com.affymetrix.genometryImpl.parsers.GFF3Parser;
import com.affymetrix.genometryImpl.parsers.NibbleResiduesParser;
import com.affymetrix.genometryImpl.parsers.PSLParser;
import com.affymetrix.genometryImpl.parsers.SegmenterRptParser;
import com.affymetrix.genometryImpl.parsers.VarParser;
import com.affymetrix.genometryImpl.parsers.das.DASFeatureParser;
import com.affymetrix.genometryImpl.parsers.gchp.AffyCnChpParser;
import com.affymetrix.genometryImpl.parsers.gchp.ChromLoadPolicy;
import com.affymetrix.genometryImpl.parsers.graph.CntParser;
import com.affymetrix.genometryImpl.parsers.graph.ScoredMapParser;
import com.affymetrix.genometryImpl.parsers.graph.ScoredIntervalParser;
import com.affymetrix.genometryImpl.parsers.useq.USeqRegionParser;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genoviz.swing.threads.InvokeUtils;
import com.affymetrix.igb.Application;
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
				load(gviewer.getFrame(), stream, filename, seq_group, aseq);
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
				load(gviewer.getFrame(), stream, filename, seq_group, aseq);
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



	/** Loads from an InputStream.
	 *  Detects the type of file based on the filename ending of the
	 *  stream_name parameter, for example ".dasxml".
	 *  The stream will be passed through uncompression routines
	 *  if necessary.
	 */
	private static void load(JFrame gviewerFrame, InputStream instr, String stream_name, AnnotatedSeqGroup selected_group, BioSeq input_seq) throws IOException {
		if (selected_group == null) {
			// this should never happen
			throw new IOException("Must select a genome before loading a file");
		}

		Logger.getLogger(UrlLoaderThread.class.getName()).log(Level.INFO,
				"loading file: " + stream_name);

		Exception the_exception = null;
		InputStream str = null;

		try {
			StringBuffer stripped_name = new StringBuffer();
			str = GeneralUtils.unzipStream(instr, stream_name, stripped_name);
			stream_name = stripped_name.toString();

			if (str instanceof BufferedInputStream) {
				str = (BufferedInputStream) str;
			} else {
				str = new BufferedInputStream(str);
			}
			DoParse(str, selected_group, input_seq, stream_name, gviewerFrame);
		} catch (Exception ex) {
			the_exception = ex;
			//ErrorHandler.errorPanel(gviewerFrame, "ERROR", "Error loading file", ex);
		} finally {
			GeneralUtils.safeClose(str);
		}

		// The purpose of calling setSelectedSeqGroup, even if identity of
		// the seq group has not changed, is to make sure that
		// the DataLoadView and the AnnotBrowserView update their displays.
		// (Because the contents of the seq group may have changed.)
		//
		// Note that this must be done regardless of whether this load() method was
		// called from inside this class or in loading a bookmark, etc.

		gmodel.setSelectedSeqGroup(gmodel.getSelectedSeqGroup());

		if (the_exception != null) {
			if (the_exception instanceof IOException) {
				throw (IOException) the_exception;
			} else {
				IOException new_exception = new IOException();
				new_exception.initCause(the_exception);
				throw new_exception;
			}
		}
	}


	private static void DoParse(
					InputStream str, AnnotatedSeqGroup selected_group, BioSeq input_seq,
					String stream_name, JFrame gviewerFrame)
					throws IOException, InterruptedException, HeadlessException, SAXException {
		String lcname = stream_name.toLowerCase();

		int dotIndex = stream_name.lastIndexOf('.');
		String annot_type = dotIndex <= 0 ? stream_name : stream_name.substring(0, dotIndex);

		// Sequence files
		if (lcname.endsWith(".bnib")) {
			BioSeq aseq = NibbleResiduesParser.parse(str, selected_group);
			if (aseq != gmodel.getSelectedSeq()) {
				//TODO: maybe set the current seq to this seq
				Logger.getLogger(LoadFileAction.class.getName()).log(Level.WARNING,
					"This is not the currently-selected sequence.");
			}
			return;
		}
		if (lcname.endsWith(".fa") || lcname.endsWith(".fas") || lcname.endsWith(".fasta")) {
			FastaParser.parseAll(str, selected_group);
			return;
		}

		// Annotation and graphs
		if (lcname.endsWith(".bed")) {
			BedParser parser = new BedParser();
			// really need to switch create_container (last argument) to true soon!
			parser.parse(str, gmodel, selected_group, true, annot_type, false);
			return;
		}
		if (lcname.endsWith(".bgn")) {
			BgnParser parser = new BgnParser();
			parser.parse(str, annot_type, selected_group, true);
			return;
		}

		if (lcname.endsWith(".bps")) {
			DataInputStream dis = new DataInputStream(str);
			BpsParser.parse(dis, annot_type, null, selected_group, false, true);
			return;
		}
		if (lcname.endsWith(".bp1") || lcname.endsWith(".bp2")) {
			Bprobe1Parser parser = new Bprobe1Parser();
			parser.parse(str, selected_group, true, annot_type, true);
			return;
		}
		if (lcname.endsWith(".brpt")) {
			BrptParser parser = new BrptParser();
			List<SeqSymmetry> alist = parser.parse(str, annot_type, selected_group, true);
			Logger.getLogger(LoadFileAction.class.getName()).log(Level.FINE,
					"total repeats loaded: " + alist.size());
			return;
		}
		if (lcname.endsWith(".brs")) {
			BrsParser.parse(str, annot_type, selected_group);
			return;
		}
		if (lcname.endsWith(".bsnp")) {
			List<SeqSymmetry> alist = BsnpParser.parse(str, annot_type, selected_group, true);
			Logger.getLogger(LoadFileAction.class.getName()).log(Level.FINE,
					"total snps loaded: " + alist.size());
			return;
		}

		if (lcname.endsWith(".cnchp") || lcname.endsWith(".lohchp")) {
			AffyCnChpParser parser = new AffyCnChpParser();
			parser.parse(null, ChromLoadPolicy.getLoadAllPolicy(), str, stream_name, selected_group);
			return;
		}
		if (lcname.endsWith(".cnt")) {
			CntParser parser = new CntParser();
			parser.parse(str, selected_group);
			return;
		}
		if (lcname.endsWith(".cyt")) {
			CytobandParser parser = new CytobandParser();
			parser.parse(str, selected_group, true);
			return;
		}
		if (lcname.endsWith(".das") || lcname.endsWith(".dasxml")) {
			DASFeatureParser parser = new DASFeatureParser();
			try {
				parser.parse(str, selected_group);
			} catch (XMLStreamException ex) {
				Logger.getLogger(LoadFileAction.class.getName()).log(Level.SEVERE, null, ex);
			}
			return;
		}
		if (lcname.endsWith(".das2xml")) {
			Das2FeatureSaxParser parser = new Das2FeatureSaxParser();
			parser.parse(new InputSource(str), stream_name, selected_group, true);
			return;
		}
		if (lcname.endsWith(".ead")) {
			ExonArrayDesignParser parser = new ExonArrayDesignParser();
			parser.parse(str, selected_group, true, annot_type);
			return;
		}
		if (lcname.endsWith("." + FishClonesParser.FILE_EXT)) {
			FishClonesParser parser = new FishClonesParser(true);
			parser.parse(str, annot_type, selected_group);
			return;
		}
		if (lcname.endsWith(".gff") || lcname.endsWith(".gtf")) {
			// assume it's GFF1, GFF2, GTF, or GFF3 format
			GFFParser parser = new GFFParser();
			parser.setUseStandardFilters(true);
			parser.parse(str, annot_type, selected_group, false);
			return;
		}
		if (lcname.endsWith(".gff3")) {
			/* Force parcing as GFF3 */
			GFF3Parser parser = new GFF3Parser();
			parser.parse(str, annot_type, selected_group);
			return;
		}

		if (lcname.endsWith(".map")) {
			ScoredMapParser parser = new ScoredMapParser();
			parser.parse(str, stream_name, input_seq, selected_group);
			return;
		}
		if (lcname.endsWith(".psl") || lcname.endsWith(".psl3")) {
			ParsePSL(lcname, gviewerFrame, str, stream_name, selected_group);
			return;
		}
		if ((lcname.endsWith("." + SegmenterRptParser.CN_REGION_FILE_EXT) || lcname.endsWith("." + SegmenterRptParser.LOH_REGION_FILE_EXT))) {
			SegmenterRptParser parser = new SegmenterRptParser();
			parser.parse(str, stream_name, selected_group);
			return;
		}
		if (lcname.endsWith(".sin") || lcname.endsWith(".egr") || lcname.endsWith(".txt")) {
			ScoredIntervalParser parser = new ScoredIntervalParser();
			parser.parse(str, stream_name, selected_group);
			return;
		}
		if (lcname.endsWith(".useq")) {
			USeqRegionParser parser = new USeqRegionParser();
			parser.parse(str, selected_group, stream_name, true, null);
			return;
		}
		if (lcname.endsWith(".var")) {
			VarParser parser = new VarParser();
			parser.parse(str, selected_group);
			return;
		}
		ErrorHandler.errorPanel(gviewerFrame, "FORMAT NOT RECOGNIZED", "Format not recognized for file: " + stream_name, null);
	}

	// Parse PSL files.  These files specifically have .psl or .psl3 extensions.
	private static void ParsePSL(
					String lcname, JFrame gviewerFrame, InputStream str, String stream_name, AnnotatedSeqGroup selected_group)
					throws IOException, HeadlessException, InterruptedException {
		PSLParser parser = new PSLParser();
		parser.enableSharedQueryTarget(true);
		int psl_option = -1;

		// If the name ends with ".link.psl" then assume it is a mapping
		// of probe sets to consensus seqs to genome, and thus select
		// psl_option = 1 "target".

		if (lcname.endsWith(".link.psl")) {
			parser.setIsLinkPsl(true);
			psl_option = 1; // "target"
		} else if (lcname.endsWith(".psl")) {
			psl_option = 1;
		} else {
			// .psl3 file
			// the user has to tell us whether to annotate the "query" or "target" or "other"
			Object[] options = new Object[]{"Query", "Target", "Other"};

			if (SwingUtilities.isEventDispatchThread()) {
				psl_option = JOptionPane.showOptionDialog(gviewerFrame, "Annotate which sequence?", "PSL annotation options", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, "Target");
			} else {
				psl_option = InvokeUtils.invokeOptionDialog(gviewerFrame, "Annotate which sequence?", "PSL annotation options", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, "Target");
			}
		}
		boolean annotate_query = psl_option == 0;
		boolean annotate_target = psl_option == 1;
		boolean annotate_other = psl_option == 2;

		if (annotate_query) {
			parser.parse(str, stream_name, selected_group, null, null, annotate_query, annotate_target, annotate_other);
		} else if (annotate_target) {
			parser.parse(str, stream_name, null, selected_group, null, annotate_query, annotate_target, annotate_other);
		} else if (annotate_other) {
			parser.parse(str, stream_name, null, null, selected_group, annotate_query, annotate_target, annotate_other);
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
