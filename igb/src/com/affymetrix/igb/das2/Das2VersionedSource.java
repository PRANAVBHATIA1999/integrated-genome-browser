/**
*   Copyright (c) 2001-2005 Affymetrix, Inc.
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

package com.affymetrix.igb.das2;

import java.io.*;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import org.w3c.dom.*;

import com.affymetrix.igb.IGB;
import com.affymetrix.igb.util.DasUtils;
import com.affymetrix.igb.util.SynonymLookup;
import com.affymetrix.igb.genometry.AnnotatedSeqGroup;
import com.affymetrix.igb.genometry.SingletonGenometryModel;
import com.affymetrix.igb.util.ErrorHandler;
import com.affymetrix.igb.das.DasLoader;

/**
 *
 *  started with com.affymetrix.igb.das.DasSource and modified
 */
public class Das2VersionedSource  {
  static boolean DO_FILE_TEST = false;
  static String test_file = "file:/C:/data/das2_responses/alan_server/regions.xml";

  static SingletonGenometryModel gmodel = SingletonGenometryModel.getGenometryModel();

  Das2Source source;
  String id;
  String description;
  String info_url;
  Date creation_date;
  Date modified_date;
  Map capabilities;
  Map namespaces;
  Map regions = new LinkedHashMap();
  Map properties;
  java.util.List assembly;

  AnnotatedSeqGroup genome = null;
  Map types = new LinkedHashMap();
  boolean regions_initialized = false;
  boolean types_initialized = false;

  public Das2VersionedSource(Das2Source das_source, String version_id, boolean init) {
    id = version_id;
    source = das_source;
    if (init) {
      initRegions();
      initTypes();
    }
  }

  public String getID() { return id; }
  public String getDescription() { return description; }
  public String getInfoUrl() { return info_url; }
  public Date getCreationDate() { return creation_date; }
  public Date getLastModifiedDate() { return modified_date; }
  public Das2Source getSource() { return source; }

  /** NOT YET IMPLEMENTED */
  public List getAssembly()   { return assembly; }
  /** NOT YET IMPLEMENTED */
  public Map getProperties()  { return properties; }
  /** NOT YET IMPLEMENTED */
  public Map getNamespaces()  { return namespaces; }
  /** NOT YET IMPLEMENTED */
  public Map getCapabilities()  { return capabilities; }

  public AnnotatedSeqGroup getGenome() {
    if (genome == null) {
      genome = gmodel.addSeqGroup(id);
    }
    return genome;
  }

  void setID(String id)  { this.id = id; }
  void setDescription(String desc) { this.description = desc; }
  void setInfoUrl(String url) { this.info_url = url; }




  public Map getRegions() {
    if (! regions_initialized)  {
      initRegions();
    }
    return regions;
  }

  void addRegion(Das2Region region) {
    regions.put(region.getID(), region);
  }

  public Map getTypes() {
    if (! types_initialized) {
      initTypes();
    }
    return types;
  }

  void addType(Das2Type type) {
    types.put(type.getID(), type);
  }


  /** Get regions from das server. */
  protected void initRegions() {
    String region_request;
    if (DO_FILE_TEST)  {
      region_request = test_file;
    }
    else {
      region_request = getSource().getServerInfo().getRootUrl() + "/" +
          this.getID() + "/region";
    }
    try {
      System.out.println("Das Region Request: " + region_request);
      Document doc = DasLoader.getDocument(region_request);
      Element top_element = doc.getDocumentElement();
      NodeList regionlist = doc.getElementsByTagName("REGION");
      System.out.println("regions: " + regionlist.getLength());
      for (int i=0; i< regionlist.getLength(); i++)  {
	Element reg = (Element)regionlist.item(i);
        String region_id = reg.getAttribute("id");
	String startstr = reg.getAttribute("start");
	String endstr = reg.getAttribute("end");
	String region_name = reg.getAttribute("name");
	String region_info_url = reg.getAttribute("doc_href");

	String description = null;
	int start = 0;
	int end = 1;
	if (startstr != null && endstr != null) {
	  start = Integer.parseInt(startstr);
	  end = Integer.parseInt(endstr);
	}
	/*
         System.out.println("  region id = " + region_id +
			   ", start = " + start + ", end = " + end +
			   ", name = " + region_name +
			   ", info url = " + region_info_url);
        */
	Das2Region region = new Das2Region(this, region_id);
	region.setInterval(start, end, true);
	this.addRegion(region);
      }
    }
    catch (Exception ex) {
      ErrorHandler.errorPanel("Error initializing DAS region points for\n"+region_request, ex);
    }
    //TODO should regions_initialized be true if an exception occured?
    regions_initialized = true;
  }

  // get annotation types from das server
  protected void initTypes() {
    String types_request = getSource().getServerInfo().getRootUrl() +
        "/" + this.getID() + "/type";
    //    String types_request = "file:/C:/data/das2_responses/alan_server/types_short.xml";
    try {
      System.out.println("Das Types Request: " + types_request);
      Document doc = DasLoader.getDocument(types_request);
      Element top_element = doc.getDocumentElement();
      NodeList typelist = doc.getElementsByTagName("TYPE");
      System.out.println("types: " + typelist.getLength());
      for (int i=0; i< typelist.getLength(); i++)  {
	Element typenode = (Element)typelist.item(i);
        String typeid = typenode.getAttribute("id");
	//	String method = typenode.getAttribute("method");
	//	String category = typenode.getAttribute("category");
	//	String countstr = null;
	//	Text count_text = (Text)typenode.getFirstChild();
	//	if (count_text != null) { countstr = count_text.getData(); }
	//	System.out.println("type id: " + typeid);
	Das2Type type = new Das2Type(typeid, this);
	this.addType(type);
      }
    }
    catch (Exception ex) {
      ErrorHandler.errorPanel("Error initializing DAS types for\n"+types_request, ex);
    }
    //TODO should types_initialized be true after an exception?
    types_initialized = true;
  }

}
