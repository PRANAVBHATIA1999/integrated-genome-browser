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
import com.affymetrix.igb.util.XMLUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public final class DasSource {

  private static final GenometryModel gmodel = GenometryModel.getGenometryModel();

  private final String id;
  private String version;
  private String name;
  private String description;
  private String info_url;
  private String mapmaster;
  private final DasServerInfo server;
  private AnnotatedSeqGroup genome = null;
  private final Map<String,DasEntryPoint> entry_points = new LinkedHashMap<String,DasEntryPoint>();
  private final Map<String,DasType> types = new LinkedHashMap<String,DasType>();
  private boolean entries_initialized = false;
  private boolean types_initialized = false;

  public DasSource(DasServerInfo source_server, String source_id, boolean init) {
    id = source_id;
    server = source_server;
    if (init) {
      initEntryPoints();
      initTypes();
    }
  }

  public String getID() { return id; }
  public String getVersion() { return version; }
  public String getName() { return name; }
  public String getDescription() { return description; }
  public String getInfoUrl() { return info_url; }

  public String getMapMaster() { return mapmaster; }  // or should this return URL?
  public DasServerInfo getDasServerInfo() { return server; }

  /**
   *  Equivalent to {@link GenometryModel#addSeqGroup(String)} with the
   *  id from {@link #getID()}.  Caches the result.
   */
  public AnnotatedSeqGroup getGenome() {
    if (genome == null) {
      genome = gmodel.addSeqGroup(id);
    }
    return genome;
  }

  public synchronized Map<String,DasEntryPoint> getEntryPoints() {
    if (! entries_initialized)  {
      initEntryPoints();
    }
    return entry_points;
  }

  public synchronized Map<String,DasType> getTypes() {
    if (! types_initialized) {
      initTypes();
    }
    return types;
  }

  void setVersion(String version) { this.version = version; }
  void setName(String name) { this.name = name; }
  void setDescription(String desc) { this.description = desc; }
  void setInfoUrl(String url) { this.info_url = url; }
  void setMapMaster(String master) { this.mapmaster = master; }

  void addEntryPoint(DasEntryPoint entry_point) {
    entry_points.put(entry_point.getID(), entry_point);
  }

  void addType(DasType type) {
    types.put(type.getID(), type);
  }

  /** Get entry points from das server. */
  protected synchronized void initEntryPoints() {
    URL entryURL;
	try {
		if (mapmaster != null && !mapmaster.isEmpty()) {
			entryURL = new URL(mapmaster + "/entry_points");
		} else {
			entryURL = new URL(getDasServerInfo().getURI().toURL(), getID() + "/entry_points");
		}

      System.out.println("Das Entry Request: " + entryURL);
      Document doc = XMLUtils.getDocument(entryURL.openConnection());
      NodeList segments = doc.getElementsByTagName("SEGMENT");
	  int length = segments.getLength();
      System.out.println("segments: " + length);
      for (int i=0; i< length; i++)  {
	Element seg = (Element)segments.item(i);
        String segid = seg.getAttribute("id");
	String startstr = seg.getAttribute("start");
	String stopstr = seg.getAttribute("stop");
	String sizestr = seg.getAttribute("size");  // can optionally use "size" instead of "start" and "stop"
	String seqtype = seg.getAttribute("type");  // optional
	String orient = seg.getAttribute("orientation");  // optional if using "size" attribute

	String description = null;
        Text desctext = (Text)seg.getFirstChild();
	if (desctext != null) { description = desctext.getData(); }
	//	System.out.println("segment id: " + segid);
	int start = 1;
	int stop = 1;
	boolean forward = true;
	if (orient != null) {
	  forward = (! orient.equals("-"));  // anything other than "-" is considered forward
	}
	if (startstr != null && startstr.length() > 0
            && stopstr != null && stopstr.length() > 0) {
	  start = Integer.parseInt(startstr);
	  stop = Integer.parseInt(stopstr);
	}
	else if (sizestr != null) {
	  stop = Integer.parseInt(sizestr);
	}
	DasEntryPoint entry_point = new DasEntryPoint(this, segid);
	entry_point.setSeqType(seqtype);
	entry_point.setDescription(description);
	entry_point.setInterval(start, stop, forward);
      }
	} catch (MalformedURLException ex) {
      ErrorHandler.errorPanel("Error initializing DAS entry points for\n" + getID() + " on " + getDasServerInfo().getURI(), ex);
    } catch (ParserConfigurationException ex) {
		ErrorHandler.errorPanel("Error initializing DAS entry points for\n" + getID() + " on " + getDasServerInfo().getURI(), ex);
	} catch (SAXException ex) {
		ErrorHandler.errorPanel("Error initializing DAS entry points for\n" + getID() + " on " + getDasServerInfo().getURI(), ex);
	} catch (IOException ex) {
		ErrorHandler.errorPanel("Error initializing DAS entry points for\n" + getID() + " on " + getDasServerInfo().getURI(), ex);
	}
    //TODO should entries_initialized be true if an exception occurred?
    entries_initialized = true;
  }

  // get annotation types from das server
  protected synchronized void initTypes() {
    try {
		URL typesURL = new URL(getDasServerInfo().getURI().toURL(), getID() + "/types");
      System.out.println("Das Types Request: " + typesURL);
      Document doc = XMLUtils.getDocument(typesURL.openConnection());
      NodeList typelist = doc.getElementsByTagName("TYPE");
      System.out.println("types: " + typelist.getLength());
      for (int i=0; i< typelist.getLength(); i++)  {
	Element typenode = (Element)typelist.item(i);
        String typeid = typenode.getAttribute("id");
	String method = typenode.getAttribute("method");
	String category = typenode.getAttribute("category");

	//String countstr = null;
	//Text count_text = (Text)typenode.getFirstChild();
	//if (count_text != null) { countstr = count_text.getData(); }

	//	System.out.println("type id: " + typeid);
	DasType type = new DasType(this, typeid, method, category);
	this.addType(type);
      }
    }
    catch (Exception ex) {
      ErrorHandler.errorPanel("Error initializing DAS types for\n" + getID() + " on " + getDasServerInfo().getURI(), ex);
    }
    //TODO should types_initialized be true after an exception?
    types_initialized = true;
  }
}
