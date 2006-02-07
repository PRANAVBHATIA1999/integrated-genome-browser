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
import java.lang.Object.*;
import java.net.URI.*;
import java.util.regex.*;

import com.affymetrix.genometry.BioSeq;
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
  static String SEGMENTS_QUERY = "segments";
  static String TYPES_QUERY = "types";

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
  String vsource_root_url;

  AnnotatedSeqGroup genome = null;
  protected Map types = new LinkedHashMap();
  protected boolean regions_initialized = false;
  protected boolean types_initialized = false;
  protected String types_filter = null;

  LinkedList platforms = new LinkedList();

  public Das2VersionedSource(Das2Source das_source, String version_id, boolean init) {
    id = version_id;
    source = das_source;
    vsource_root_url = source.getRootUrl() + "/" + version_id;
    if (init) {
      initRegions();
      initTypes(null, false);
    }
  }

  public String getID() { return id; }
  public String getDescription() { return description; }
  public String getInfoUrl() { return info_url; }
  public Date getCreationDate() { return creation_date; }
  public Date getLastModifiedDate() { return modified_date; }
  public Das2Source getSource() { return source; }
  public String getRootUrl() { return vsource_root_url; }

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
      genome = gmodel.addSeqGroup(id);  // gets existing seq group if possible, otherwise adds new one
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

  /**
   *  assumes there is only one region for each seq
   *    may want to change this to return a list of regions instead
   **/
  public Das2Region getRegion(BioSeq seq) {
    // should probably make a region2seq hash, but for now can just iterate through regions
    Das2Region result = null;
    Iterator iter = getRegions().values().iterator();
    while (iter.hasNext()) {
      Das2Region region = (Das2Region)iter.next();
      BioSeq region_seq = region.getAnnotatedSeq();
      if (region_seq == seq) {
	result = region;
	break;
      }
    }
    return result;
  }

  public void addRegion(Das2Region region) {
    regions.put(region.getID(), region);
  }

  public void addType(Das2Type type) {
    types.put(type.getID(), type);
  }

  public Map getTypes() {
    if (! types_initialized || types_filter != null) {
      initTypes(null, false);
    }
    return types;
  }

  /*
  public Map getTypes(String filter) {
     if (! types_initialized || !filter.equals(types_filter)) {
       initTypes(filter, true);
    }
    return types;
  }
  */

  public void clearTypes() {
      this.types = new LinkedHashMap();
  }

  /** Get regions from das server. */
  protected void initRegions() {
    String region_request;
    if (DO_FILE_TEST)  {
      region_request = test_file;
    }
    else {
      //      region_request = getSource().getServerInfo().getRootUrl() + "/" +
      //          this.getID() + "/" + SEGMENTS_QUERY;
      region_request = this.getRootUrl() + "/" + SEGMENTS_QUERY;
    }
    try {
      System.out.println("Das Region Request: " + region_request);
      Document doc = DasLoader.getDocument(region_request);
      Element top_element = doc.getDocumentElement();
      //      NodeList regionlist = doc.getElementsByTagName("REGION");
      NodeList regionlist = doc.getElementsByTagName("SEGMENT");
      System.out.println("regions: " + regionlist.getLength());
      for (int i=0; i< regionlist.getLength(); i++)  {
	Element reg = (Element)regionlist.item(i);
        String region_id = reg.getAttribute("id");
	String lengthstr = reg.getAttribute("length");
	String region_name = reg.getAttribute("name");
	String region_info_url = reg.getAttribute("doc_href");

	String description = null;
	int length = Integer.parseInt(lengthstr);
	Das2Region region = new Das2Region(this, region_id, region_name, region_info_url, length);
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
  /**
   *  loading of parents disabled, getParents currently does nothing
   */
  protected void initTypes(String filter, boolean getParents) {
    this.types_filter = filter;
    this.clearTypes();

    // how should xml:base be handled?
    //example of type request:  http://das.biopackages.net/das/assay/mouse/6/type?ontology=MA
    String types_request = this.getRootUrl() + "/" + TYPES_QUERY;

    //    if (filter != null) { types_request = types_request+"?ontology="+filter; }
    try {
      System.out.println("Das Types Request: " + types_request);
      Document doc = DasLoader.getDocument(types_request);
      Element top_element = doc.getDocumentElement();
      NodeList typelist = doc.getElementsByTagName("TYPE");
      System.out.println("types: " + typelist.getLength());
      int typeCounter = 0;

      //      ontologyStuff1();
      for (int i=0; i< typelist.getLength(); i++)  {
	Element typenode = (Element)typelist.item(i);
        String typeid = typenode.getAttribute("id");                            // Gets the ID value
	//        String typeid = typenode.getAttribute("ontology");                            // Gets the ID value
        //FIXME: quick hack to get the type IDs to be kind of right (for now)

        // temporary workaround for getting type ending, rather than full URI
	if (typeid.startsWith("./")) { typeid = typeid.substring(2); }          //if these characters are one the beginning, take off the 1st 2 characters...
	//FIXME: quick hack to get the type IDs to be kind of right (for now)

        String ontid = typenode.getAttribute("ontology");
	String type_source = typenode.getAttribute("source");                   //PROBLEM!  This is missing too (no longer in this doc?)
	String href = typenode.getAttribute("doc_href");                        //ALSO MIA is this attribute  Are these needed?  I doubt they are needed YET

	NodeList flist = typenode.getElementsByTagName("FORMAT");               //FIXME: I don't even know if these are in the XML yet.
	LinkedHashMap formats = new LinkedHashMap();                            //I don't think that this has ever been used yet.
	HashMap props = new HashMap();
	for (int k=0; k<flist.getLength(); k++) {
	  Element fnode = (Element)flist.item(k);
	  String formatid = fnode.getAttribute("id");
	  String mimetype = fnode.getAttribute("mimetype");
	  if (mimetype == null || mimetype.equals("")) { mimetype = "unknown"; }
          //	  System.out.println("alternative format for annot type " + typeid +
          //": format = " + formatid + ", mimetype = " + mimetype);
          formats.put(formatid, mimetype);
	}

	NodeList plist = typenode.getElementsByTagName("PROP");                 //What IS this?  I am not sure if this is used either.
	for (int k=0; k<plist.getLength(); k++) {
	  Element pnode = (Element)plist.item(k);
	  String key = pnode.getAttribute("key");
	  String val = pnode.getAttribute("value");
	  props.put(key, val);
	}
	//	ontologyStuff2();
	Das2Type type = new Das2Type(this, typeid, ontid, type_source, href, formats, props, null);  // parents field is null for now -- remove at some point?
	//	Das2Type type = new Das2Type(this, typeid, ontid, type_source, href, formats, props);
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
