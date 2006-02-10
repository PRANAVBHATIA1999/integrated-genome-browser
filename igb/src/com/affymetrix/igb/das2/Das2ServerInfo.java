/**
*   Copyright (c) 2005 Affymetrix, Inc.
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
import org.w3c.dom.*;

import com.affymetrix.igb.das.DasLoader;
import com.affymetrix.genometry.MutableAnnotatedBioSeq;
import com.affymetrix.igb.genometry.AnnotatedSeqGroup;

public class Das2ServerInfo  {
  static boolean REPORT_SOURCES = true;
  protected static boolean DO_FILE_TEST = false;
  protected static String test_file = "file:/C:/data/das2_responses/alan_server/sources.xml";
  protected static String SOURCES_QUERY = "sequence";

  protected String root_url;
  String das_version;
  String name;
  protected Map sources = new LinkedHashMap();  // using LinkedHashMap for predictable iteration
  protected boolean initialized = false;

  /** Creates an instance of Das2ServerInfo for the given DAS server.
   *  @param init  whether or not to initialize the data right away.  If false
   *    will not contact the server to initialize data until needed.
   */
  public Das2ServerInfo(String url, String name, boolean init) {
    this.root_url = url;
    this.name = name;
    // all trailing "/" chars are stripped off the end if present
    while (root_url.endsWith("/")) {
      root_url = root_url.substring(0, root_url.length()-1);
    }
    if (init) {
      initialize();
    }
  }

  /** Returns the root URL String.  Will not have any trailing "/" at the end. */
  public String getRootUrl() {
    return root_url;
  }

  public String getName() {
    return name;
  }

  public String getDasVersion() {
    if (!initialized) { initialize(); }
    return das_version;
  }

  public Map getSources() {
    if (!initialized) { initialize(); }
    return sources;
  }

  protected void setDasVersion(String version) {
    das_version = version;
  }

  protected void addDataSource(Das2Source ds) {
    sources.put(ds.getID(), ds);
  }


  /**
   *  assumes there is only one versioned source for each AnnotatedSeqGroup
   *    may want to change this to return a list of versioned sources instead
   **/
  public Das2VersionedSource getVersionedSource(AnnotatedSeqGroup group) {
    // should probably make a vsource2seqgroup hash,
    //   but for now can just iterate through sources and versions
    Das2VersionedSource result = null;
    Iterator siter = getSources().values().iterator();
    while (siter.hasNext()) {
      Das2Source source = (Das2Source)siter.next();
      Iterator viter = source.getVersions().values().iterator();
      while (viter.hasNext()) {
	Das2VersionedSource version = (Das2VersionedSource)viter.next();
	AnnotatedSeqGroup version_group = version.getGenome();
	if (version_group == group) {
	  result = version;
	  break;
	}
      }
    }
    return result;
  }

//  public String getDescription() { return description; }
//  protected void setDescription(String desc)  { }

  /**
   * Return true if successfully initialized.
   */
  public boolean initialize() {
    //TODO: think about whether this needs synchronization.
    //TODO: clean-up streams in finally block
    try {
      //      System.out.println("in DasUtils.findDasSource()");
      //      SynonymLookup lookup = SynonymLookup.getDefaultLookup();
      URL das_request;
      if (DO_FILE_TEST)  {
	das_request = new URL(test_file);
      }
      else {
	// GAH 2006-02-10
	//  changed to assuming the root_url is the full URL for a sources (sequence?)
	//  done for compatibility with 

	//	das_request = new URL(root_url+"/" + SOURCES_QUERY);
	das_request = new URL(root_url);
      }
      System.out.println("Das Request: " + das_request);
      URLConnection request_con = das_request.openConnection();
      String das_version = request_con.getHeaderField("X-DAS-Version");
      String das_status = request_con.getHeaderField("X-DAS-Status");
      //  String das_capabilities = request_con.getHeaderField("X-DAS-Capabilities");

      setDasVersion(das_version);

      System.out.println("DAS server version: " + das_version + ", status: " + das_status);
      Document doc = DasLoader.getDocument(request_con);

      Element top_element = doc.getDocumentElement();
      NodeList sources= doc.getElementsByTagName("SOURCE");
      System.out.println("source count: " + sources.getLength());
      for (int i=0; i< sources.getLength(); i++)  {
        Element source = (Element)sources.item(i);
        //        System.out.println("source base URI: " + source.getBaseURI());
        String source_id = source.getAttribute("id");

        String source_info_url = source.getAttribute("doc_href");
        String source_description = source.getAttribute("description");
        String source_taxon = source.getAttribute("taxon");

	Das2Source dasSource = new Das2Source(this, source_id, false);
	//        setDasSource(this, source_id, false);

	dasSource.setID(source_id);
	dasSource.setInfoUrl(source_info_url);
	dasSource.setDescription(source_description);
	dasSource.setTaxon(source_taxon);
	this.addDataSource(dasSource);
	NodeList slist = source.getChildNodes();
	for (int k=0; k < slist.getLength(); k++) {
	  if (slist.item(k).getNodeName().equals("VERSION"))  {
	    Element version = (Element)slist.item(k);
	    String version_id = version.getAttribute("id");
	    String version_description = version.getAttribute("description");
	    String version_info_url = version.getAttribute("doc_href");
	    //	    setDasVersionedSource(dasSource, version_id, false);
	    Das2VersionedSource vsource = new Das2VersionedSource(dasSource, version_id, false);
	    dasSource.addVersion(vsource);
	    System.out.println("base URI for version element: " + getBaseURI(version));

	    NodeList vlist = version.getChildNodes();
	    for (int j=0; j<vlist.getLength(); j++) {
	      String nodename = vlist.item(j).getNodeName();
	      // was CATEGORY, renamed CAPABILITY
	      if (nodename.equals("CAPABILITY") || nodename.equals("CATEGORY")) {
		Element capel = (Element)vlist.item(j);
		String captype = capel.getAttribute("type");
		String query_id = capel.getAttribute("query_id");
		URI base_uri = getBaseURI(capel);
		URI cap_root = base_uri.resolve(query_id);
		System.out.println("Capability: " + captype + ", URI: " + cap_root);
		// for now don't worry about format subelements
		Das2Capability cap = new Das2Capability(captype, cap_root, null);
		vsource.addCapability(cap);
	      }
	    }
	  }
	}
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    initialized = true;
    return initialized;
  }

  /**
   * Attempt to retrieve base URI for an Element from a DOM-level2 model
   */
  public static URI getBaseURI(Node cnode) {
    Stack xml_bases = new Stack();
    Node pnode = cnode;
    while (pnode != null) {
      if (pnode instanceof Element) {
	Element el = (Element)pnode;
	String xbase = el.getAttribute("xml:base");
	if (xbase != null) { xml_bases.push(xbase); }
      }
      pnode = pnode.getParentNode();
    }

    URI base_uri = null;
    try  {
      if (! (xml_bases.empty())) {
        String xbase = (String) xml_bases.pop();
        base_uri = new URI(xbase);
        while (! (xml_bases.empty())) {
          xbase = (String) xml_bases.pop();
          base_uri = base_uri.resolve(xbase);
        }
      }
    }
    catch (Exception ex)  {
      System.out.println("*** problem figuring out base URI, setting to null");
      base_uri = null;
    }
    return base_uri;
  }


  /**
   *  For testing.
   */
  public static void main(String[] args) {
    // String test_url = "file:/C:/data/das2_responses/alan_server/sources.xml";
    String test_url = "http://das.ev.affymetrix.com/das/genome";

    // DEBUG: The first is a squid proxy that greatly increases response time
    // email me if you want access <boconnor@ucla.edu>
    //String test_url = "http://radius.genomics.ctrl.ucla.edu/das/genome";
    //String test_url = "http://das.biopackages.net/das/genome";

    Das2ServerInfo test = new Das2ServerInfo(test_url, "name unknown", true);
    System.out.println("***** DAS Server Info *****");
    System.out.println("  root URL: " + test.getRootUrl());
    System.out.println("  DAS version: " + test.getDasVersion());

    Iterator sources = test.getSources().values().iterator();
    System.out.println("  data sources: ");
    while (sources.hasNext()) {
      Das2Source source = (Das2Source)sources.next();
      System.out.println("     id = " + source.getID() +
                         ", description = " + source.getDescription() +
			 ", info_url = " + source.getInfoUrl() +
                         ", taxon = " + source.getTaxon());
      Iterator versions = source.getVersions().values().iterator();
      while (versions.hasNext()) {
	Das2VersionedSource version = (Das2VersionedSource)versions.next();
	System.out.println("          version id = " + version.getID());
	System.out.println("AnnotatedSeqGroup: " + version.getGenome().getID());
	Map regions = version.getRegions();
	Iterator riter = regions.values().iterator();
	while (riter.hasNext()) {
	  Das2Region region = (Das2Region)riter.next();
	  MutableAnnotatedBioSeq aseq = region.getAnnotatedSeq();
	}
      }
    }
    /*
       sources = test.getDataSources().values().iterator();
        DasSource first_source = (DasSource)sources.next();
    //    first_source.initialize();
    Map entryhash = first_source.getEntryPoints();
    Iterator entries = entryhash.values().iterator();
    while (entries.hasNext()) {
      DasEntryPoint entry_point = (DasEntryPoint)entries.next();
      System.out.println("entry point:  id = " + entry_point.getID());
      AnnotatedBioSeq seq = (AnnotatedBioSeq)entry_point.getAnnotatedSeq();
      System.out.println("seq: " + seq.getID() + ", length = " + seq.getLength());
    }
    Map typehash = first_source.getTypes();
    Iterator types = typehash.values().iterator();
    while (types.hasNext()) {
      DasType type = (DasType)types.next();
      System.out.println("type:  id = " + type.getID());
    }

    AnnotatedSeqGroup genome = first_source.getGenome();
    System.out.println("current genome: " + genome);

    Iterator iter = genome.getSeqs().values().iterator();
    while (iter.hasNext()) {
      AnnotatedBioSeq seq = (AnnotatedBioSeq)iter.next();
      System.out.println("seq: " + seq.getID() + ", length = " + seq.getLength());
    }
   */

   System.out.println("**************************");
  }
}


