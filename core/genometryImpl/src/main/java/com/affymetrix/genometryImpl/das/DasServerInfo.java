/**
 * Copyright (c) 2001-2006 Affymetrix, Inc.
 *
 * Licensed under the Common Public License, Version 1.0 (the "License"). A copy
 * of the license must be included with any distribution of this source code.
 * Distributions from Affymetrix, Inc., place this in the IGB_LICENSE.html file.
 *
 * The license is also available at http://www.opensource.org/licenses/cpl.php
 */
package com.affymetrix.genometryImpl.das;

import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LocalUrlCacher;
import com.affymetrix.genometryImpl.util.ServerTypeI;
import com.affymetrix.genometryImpl.util.XMLUtils;
import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerStatus;
import com.affymetrix.genometryImpl.util.ServerUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

/**
 *
 * @version $Id: DasServerInfo.java 9718 2012-01-09 23:01:44Z lfrohman $
 */
public final class DasServerInfo {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DasServerInfo.class);
    private static final boolean REPORT_SOURCES = false;
    private static final boolean REPORT_CAPS = true;
    private URL serverURL;
    private URL primaryURL = null;
    private final Map<String, DasSource> sources = new LinkedHashMap<String, DasSource>();  // using LinkedHashMap for predictable iteration
    private boolean initialized = false;
    private GenericServer primaryServer = null;

    /**
     * Creates an instance of DasServerInfo for the given DAS server url.
     *
     * @param url
     */
    public DasServerInfo(String url) {
        try {
            serverURL = new URL(url);
        } catch (MalformedURLException e) {
            logger.warn("Unable to convert URL '" + url + "' to URI", e);
        }

    }

    public Map<String, DasSource> getDataSources(URL primaryURL, GenericServer primaryServer) {

        if (this.primaryURL == null) {
            setPrimaryURL(primaryURL);
            this.primaryServer = primaryServer;
        }

        if (!initialized) {
            initialize();
        }
        return sources;
    }

    private void setPrimaryURL(URL primaryURL) {
        if (primaryURL != null) {
            try {
                this.primaryURL = new URL(ServerUtils.formatURL(primaryURL.toExternalForm(), ServerTypeI.QuickLoad));
            } catch (MalformedURLException ex) {
                logger.error("MalformedURLException", ex);
            }
        } else {
            this.primaryURL = null;
        }
    }

    public Map<String, DasSource> getDataSources() {
        return getDataSources(null, null);
    }

    /**
     * Return true if successfully initialized. see DAS specification for
     * returned XML format in response to "dsn" command:
     * http://biodas.org/documents/spec.html
     */
    private boolean initialize() {
        InputStream stream = null;
        try {
            Map<String, List<String>> headers = new HashMap<String, List<String>>();
            stream = getInputStream(headers, "Das Request");
            if (stream == null) {
                logger.error("Could not find URL {0}", serverURL);
                return false;
            }

            List<String> list;
            String das_version = "";
            String das_status = "";
            String das_capabilities = "";

            list = headers.get("X-DAS-Version");
            if (list != null) {
                das_version = list.toString();
            }
            list = headers.get("X-DAS-Status");
            if (list != null) {
                das_status = list.toString();
            }
            list = headers.get("X-DAS-Capabilities");
            if (list != null) {
                das_capabilities = list.toString();
            }

            logger.debug("DAS server version: " + das_version + ", status: " + das_status);
            logger.debug("DAS capabilities: " + das_capabilities);

            Document doc = XMLUtils.getDocument(stream);

            NodeList dsns = doc.getElementsByTagName("DSN");
            int dsnLength = dsns.getLength();
            logger.debug("dsn count: {0}", dsnLength);
            for (int i = 0; i < dsnLength; i++) {
                Element dsn = (Element) dsns.item(i);
                try {
                    parseDSNElement(dsn);
                } catch (Exception ex) {
                    logger.error("Error initializing DAS server info for\n" + serverURL, ex);
                }
            }
        } catch (Exception ex) {
            logger.error("Error initializing DAS server info for\n" + serverURL, ex);
            return false;
        } finally {
            GeneralUtils.safeClose(stream);
        }
        initialized = true;
        return true;
    }

    private void parseDSNElement(Element dsn) throws DOMException {
        NodeList sourcelist = dsn.getElementsByTagName("SOURCE");
        Element source = (Element) sourcelist.item(0);
        if (source == null) {
            // SOURCE tag is required.
            logger.error("Missing SOURCE element.  Ignoring.");
            return;
        }
        String sourceid = source.getAttribute("id");

        NodeList masterlist = dsn.getElementsByTagName("MAPMASTER");
        Element master = (Element) masterlist.item(0);
        if (master == null) {
            // MAPMASTER tag is required.
            logger.error("Missing MAPMASTER element.  Ignoring " + sourceid);
            return;
        }
        Text mastertext = (Text) master.getFirstChild();
        String master_url = null;
        if (mastertext != null) {
            master_url = mastertext.getData();
        }
        if (master_url == null || "null".equals(master_url)) {
            return;
        }
        try {
            URL masterURL = new URL(master_url);
            if (DasSource.getID(masterURL).isEmpty()) {
                Logger.getLogger(this.getClass().getName()).log(
                        Level.WARNING, "Skipping {0} as MAPMASTER could not be parsed", sourceid);
                return;
            }
            DasSource das_source = sources.get(DasSource.getID(masterURL));
            synchronized (this) {
                if (das_source == null) {
                    das_source = new DasSource(serverURL, masterURL, primaryURL, primaryServer);
                    sources.put(DasSource.getID(masterURL), das_source);
                }
                das_source.add(sourceid);
            }
        } catch (MalformedURLException ex) {
            logger.warn("MalformedURLException in DasServerInfo.parseDSNElement() {0}", ex.getMessage());
        }

        logger.debug("sourceid = " + sourceid + ", mapmaster = " + master_url);

    }

    public InputStream getInputStream(Map<String, List<String>> headers, String log_string) throws MalformedURLException, IOException {
        URL load_url = getLoadURL();
        InputStream istr = LocalUrlCacher.getInputStream(load_url, true, null, headers);

        /**
         * Check to see if trying to load from primary server but primary server
         * is not responding *
         */
        if (istr == null && isLoadingFromPrimary()) {

            Logger.getLogger(DasServerInfo.class.getName()).log(
                    Level.WARNING, "Primary Server :{0} is not responding. So disabling it for this session.", primaryServer.serverName);
            primaryServer.setServerStatus(ServerStatus.NotResponding);

            load_url = getLoadURL();
            istr = LocalUrlCacher.getInputStream(load_url, true, null, headers);
        }

        Logger.getLogger(DasServerInfo.class.getName()).log(
                Level.INFO, "{0} : {1}", new Object[]{log_string, load_url});
        return istr;
    }

    private boolean isLoadingFromPrimary() {
        return (primaryURL != null && primaryServer != null && !primaryServer.getServerStatus().equals(ServerStatus.NotResponding));
    }

    private URL getLoadURL() throws MalformedURLException {
        if (!isLoadingFromPrimary()) {
            return serverURL;
        }

        return new URL(primaryURL.toExternalForm() + "/dsn.xml");
    }
}
