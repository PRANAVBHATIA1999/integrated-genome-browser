/**
*   Copyright (c) 2001-2006 Affymetrix, Inc.
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

import java.util.*;

import com.affymetrix.igb.genometry.AnnotatedSeqGroup;

public class Das2Discovery {

  static Map name2server = new LinkedHashMap();
  static Map name2url = new LinkedHashMap();
  static boolean servers_initialized = false;

  static {
    name2url.put("localhost", "http://localhost:9092/das2/genome");
    name2url.put("NetAffx", "http://netaffxdas.affymetrix.com/das2/sources");
    name2url.put("biopackages", "http://das.biopackages.net/das/genome");
    name2url.put("Sanger registry", "http://www.spice-3d.org/dasregistry/das2/sources");
    //name2url.put("File based", "file:///C:/Documents%20and%20Settings/Ed%20Erwin/My%20Documents/genoviz/igb/test/test_files/sources.xml");
    //    name2url.put("NetAffx", "http://netaffxdas.affymetrix.com/das2/sequence");
    //    name2url.put("Affy Test Server, Nov 11 2006", "http://netaffxdas.affymetrix.com/das2/test/sources");
    //    name2url.put("Affy-test", "http://205.217.46.81:9091/das2/sequence");
    name2url.put("Affy-test", "http://unibrow.dmz2.ev.affymetrix.com:9091/das2/sequence");
    //    name2url.put("das.biopackages.net", "http://das.biopackages.net/das");
    name2url.put("riva",  "http://riva.ev.affymetrix.com:9092/das2/genome");
    //    name2url.put("bad test", "http://this.is.a.test/hmmm");
  }

  /**
   *  Gets a Map of DAS servers.
   *  Map is from Strings (server names) to Das2ServerInfo's.
   */
  public static Map getDas2Servers() {
    if (! servers_initialized)  {
      initServers();
    }
    return name2server;
  }

  protected static void initServers() {
    Iterator names = name2url.keySet().iterator();
    while (names.hasNext()) {
      String name = (String)names.next();
      String url = (String)name2url.get(name);
      try {
        Das2ServerInfo server = new Das2ServerInfo(url, name, false);
        name2server.put(name, server);
      } catch (Exception e) {
        System.out.println("WARNING: Could not initialize DAS/2 server with address: " + url);
        e.printStackTrace(System.out);
      }
    }
    servers_initialized = true;
  }

  /**
   *  Given an AnnotatedSeqGroup, return a list of Das2VersionedSources that
   *    provide annotations for the group [ versioned_source.getGenome() = group ]
   *  if (try_unloaded_servers) then force retrieval of versioned sources info for
   *       all known server, otherwise only check versioned sources whose info is already loaded??
   */
  public static List getVersionedSources(AnnotatedSeqGroup group, boolean try_unloaded_servers) {
    List matches = new ArrayList();
    Iterator servers = getDas2Servers().values().iterator();
    while (servers.hasNext()) {
      Das2ServerInfo server = (Das2ServerInfo)servers.next();
      boolean init = server.isInitialized();
      if ((! init) && try_unloaded_servers) {
	server.initialize();
	init = server.isInitialized();
      }
      if (init) {
	Iterator sources = server.getSources().values().iterator();
	while (sources.hasNext()) {
	  Das2Source source = (Das2Source)sources.next();
	  Iterator versioned_sources = source.getVersions().values().iterator();
	  while (versioned_sources.hasNext()) {
	    Das2VersionedSource version = (Das2VersionedSource)versioned_sources.next();
	    if (version.getGenome() == group) {
	      matches.add(version);
	    }
	  }
	}
      }
    }
    return matches;
  }

  /** NOT YET IMPLEMENTED
  public static void addDas2Server(String name, String url)  { }
  */

  /** NOT YET IMPLEMENTED
  public void removeDas2Server(String url) { }
  */


}
