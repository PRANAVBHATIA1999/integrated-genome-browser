/**
 *   Copyright (c) 2001-2004 Affymetrix, Inc.
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
package com.affymetrix.igb.das;

import java.util.*;
import org.w3c.dom.*;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.GenometryModel;
import com.affymetrix.genoviz.util.ErrorHandler;
import com.affymetrix.igb.util.LocalUrlCacher;
import com.affymetrix.igb.util.XMLUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public final class DasSource {

	private final URL server;
	private final URL master;
	private final String id;
	private final Set<String> sources = new HashSet<String>();
	private final Map<String, DasEntryPoint> entry_points = new LinkedHashMap<String, DasEntryPoint>();
	private final Map<String, DasType> types = new LinkedHashMap<String, DasType>();
	private boolean entries_initialized = false;
	private boolean types_initialized = false;

	public DasSource(URL server, URL master) {
		this.server = server;
		this.master = master;
		this.id = getID(master);
	}

	public static String getID(URL master) {
		String path = master.getPath();
		return path.substring(1 + path.lastIndexOf('/'), path.length());
	}

	public String getID() {
		return id;
	}

	public void add(String source) {
		sources.add(source);
	}

	/**
	 *  Equivalent to {@link GenometryModel#addSeqGroup(String)} with the
	 *  id from {@link #getID()}.  Caches the result.
	 */
	public AnnotatedSeqGroup getGenome() {
		return GenometryModel.getGenometryModel().addSeqGroup(this.getID());
	}

	public synchronized Map<String, DasEntryPoint> getEntryPoints() {
		if (!entries_initialized) {
			initEntryPoints();
		}
		return entry_points;
	}

	public synchronized Map<String, DasType> getTypes() {
		if (!types_initialized) {
			initTypes();
		}
		return types;
	}

	/** Get entry points from das server. */
	protected synchronized void initEntryPoints() {
		try {
			URL entryURL = new URL(master, master.getPath() + "/entry_points");

			System.out.println("Das Entry Request: " + entryURL);
			URLConnection connection = entryURL.openConnection();
			connection.setConnectTimeout(LocalUrlCacher.CONNECT_TIMEOUT);
			connection.setReadTimeout(LocalUrlCacher.READ_TIMEOUT);
			Document doc = XMLUtils.getDocument(connection);
			NodeList segments = doc.getElementsByTagName("SEGMENT");
			int length = segments.getLength();
			System.out.println("segments: " + length);
			for (int i = 0; i < length; i++) {
				Element seg = (Element) segments.item(i);
				String segid = seg.getAttribute("id");
				String startstr = seg.getAttribute("start");
				String stopstr = seg.getAttribute("stop");
				String sizestr = seg.getAttribute("size");  // can optionally use "size" instead of "start" and "stop"
				String seqtype = seg.getAttribute("type");  // optional
				String orient = seg.getAttribute("orientation");  // optional if using "size" attribute

				String description = null;
				Text desctext = (Text) seg.getFirstChild();
				if (desctext != null) {
					description = desctext.getData();
				}
				//	System.out.println("segment id: " + segid);
				int start = 1;
				int stop = 1;
				boolean forward = true;
				if (orient != null) {
					forward = (!orient.equals("-"));  // anything other than "-" is considered forward
				}
				if (startstr != null && startstr.length() > 0
						&& stopstr != null && stopstr.length() > 0) {
					start = Integer.parseInt(startstr);
					stop = Integer.parseInt(stopstr);
				} else if (sizestr != null) {
					stop = Integer.parseInt(sizestr);
				}
				DasEntryPoint entry_point = new DasEntryPoint(this, segid);
				entry_point.setSeqType(seqtype);
				entry_point.setDescription(description);
				entry_point.setInterval(start, stop, forward);
			}
		} catch (MalformedURLException ex) {
			ErrorHandler.errorPanel("Error initializing DAS entry points for\n" + getID() + " on " + server, ex);
		} catch (ParserConfigurationException ex) {
			ErrorHandler.errorPanel("Error initializing DAS entry points for\n" + getID() + " on " + server, ex);
		} catch (SAXException ex) {
			ErrorHandler.errorPanel("Error initializing DAS entry points for\n" + getID() + " on " + server, ex);
		} catch (IOException ex) {
			ErrorHandler.errorPanel("Error initializing DAS entry points for\n" + getID() + " on " + server, ex);
		}
		entries_initialized = true;
	}

	protected synchronized void initTypes() {
		for (String source : sources) {
			initType(source);
		}
		types_initialized = true;
	}

	protected void initType(String source) {
		try {
			URL typesURL = new URL(server, source + "/types");
			URL testMasterURL = new URL(master, source + "/types");
			System.out.println("Das Types Request: " + typesURL);
			URLConnection connection = typesURL.openConnection();
			connection.setConnectTimeout(LocalUrlCacher.CONNECT_TIMEOUT);
			connection.setReadTimeout(LocalUrlCacher.READ_TIMEOUT);
			Document doc = XMLUtils.getDocument(connection);
			NodeList typelist = doc.getElementsByTagName("TYPE");
			System.out.println("types: " + typelist.getLength());
			for (int i = 0; i < typelist.getLength(); i++) {
				Element typenode = (Element) typelist.item(i);
				String typeid = typenode.getAttribute("id");

				String name = typesURL.equals(testMasterURL) ? null : source + "/" + typeid;
				DasType type = new DasType(server, typeid, source, name);
				types.put(type.getName(), type);
			}
		} catch (MalformedURLException ex) {
			ErrorHandler.errorPanel("Error initializing DAS types for\n" + getID() + " on " + server, ex);
		} catch (ParserConfigurationException ex) {
			ErrorHandler.errorPanel("Error initializing DAS types for\n" + getID() + " on " + server, ex);
		} catch (SAXException ex) {
			ErrorHandler.errorPanel("Error initializing DAS types for\n" + getID() + " on " + server, ex);
		} catch (IOException ex) {
			ErrorHandler.errorPanel("Error initializing DAS types for\n" + getID() + " on " + server, ex);
		}
	}
}
