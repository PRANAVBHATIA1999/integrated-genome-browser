package com.affymetrix.igb.general;

import com.affymetrix.genometry.GenometryConstants;
import com.affymetrix.genometry.event.GenericServerInitEvent;
import com.affymetrix.genometry.event.GenericServerInitListener;
import com.affymetrix.genometry.general.GenericServer;
import com.affymetrix.genometry.general.GenericServerPrefKeys;
import static com.affymetrix.genometry.general.GenericServerPrefKeys.ENABLE_IF_AVAILABLE;
import static com.affymetrix.genometry.general.GenericServerPrefKeys.IS_SERVER_ENABLED;
import static com.affymetrix.genometry.general.GenericServerPrefKeys.SERVER_LOGIN;
import static com.affymetrix.genometry.general.GenericServerPrefKeys.SERVER_NAME;
import static com.affymetrix.genometry.general.GenericServerPrefKeys.SERVER_ORDER;
import static com.affymetrix.genometry.general.GenericServerPrefKeys.SERVER_PASSWORD;
import static com.affymetrix.genometry.general.GenericServerPrefKeys.SERVER_TYPE;
import static com.affymetrix.genometry.general.GenericServerPrefKeys.SERVER_URL;
import com.affymetrix.genometry.quickload.QuickloadServerType;
import com.affymetrix.genometry.util.ErrorHandler;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.LoadUtils.ServerStatus;
import com.affymetrix.genometry.util.LocalFilesServerType;
import com.affymetrix.genometry.util.ModalUtils;
import com.affymetrix.genometry.util.PreferenceUtils;
import com.affymetrix.genometry.util.ServerTypeI;
import com.affymetrix.genometry.util.ServerUtils;
import com.affymetrix.igb.prefs.DataLoadPrefsView;
import com.lorainelab.igb.preferences.model.DataProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @version $Id: ServerList.java 11173 2012-04-19 13:52:00Z imnick $
 */
public final class ServerList {

    private static final Logger logger = LoggerFactory.getLogger(ServerList.class);
    private final Map<String, GenericServer> url2server = new LinkedHashMap<>();
    private final Set<GenericServerInitListener> server_init_listeners = new CopyOnWriteArraySet<>();
    private final GenericServer localFilesServer = new GenericServer("Local Files", "", LocalFilesServerType.getInstance(), true, null, false, null); //qlmirror
    private final GenericServer igbFilesServer = new GenericServer("IGB Tracks", "", LocalFilesServerType.getInstance(), true, null, false, null); //qlmirror
    private static ServerList serverInstance = new ServerList("server");
    private final String textName;
    private final Comparator<GenericServer> serverOrderComparator = (o1, o2) -> getServerOrder(o1) - getServerOrder(o2);

    private ServerList(String textName) {
        this.textName = textName;
    }

    public static ServerList getServerInstance() {
        return serverInstance;
    }

    public String getTextName() {
        return textName;
    }

    public boolean hasTypes() {
        return this == serverInstance;
    }

    public Set<GenericServer> getEnabledServers() {
        Set<GenericServer> serverList = new HashSet<>();
        for (GenericServer gServer : getAllServers()) {
            if (gServer.isEnabled() && gServer.getServerStatus() != ServerStatus.NotResponding) {
                serverList.add(gServer);
            }
        }
        return serverList;
    }

    public Set<GenericServer> getInitializedServers() {
        Set<GenericServer> serverList = new HashSet<>();
        for (GenericServer gServer : getEnabledServers()) {
            if (gServer.getServerStatus() == ServerStatus.Initialized) {
                serverList.add(gServer);
            }
        }
        return serverList;
    }

    public Comparator<GenericServer> getServerOrderComparator() {
        return serverOrderComparator;
    }

    public GenericServer getLocalFilesServer() {
        return localFilesServer;
    }

    public GenericServer getIGBFilesServer() {
        return igbFilesServer;
    }

    public boolean areAllServersInited() {
        for (GenericServer gServer : getAllServers()) {
            if (!gServer.isEnabled()) {
                continue;
            }
            if (gServer.getServerStatus() == ServerStatus.NotInitialized) {
                return false;
            }
        }
        return true;
    }

    public synchronized Collection<GenericServer> getAllServers() {
        List<GenericServer> allServers = new ArrayList<>(url2server.values());
        Collections.sort(allServers, serverOrderComparator);
        return allServers;
    }

    /**
     * Given an URLorName string which should be the resolvable root SERVER_URL
     * (but may optionally be the server name) Return the GenericServer object.
     * (This could be non-unique if passed a name.)
     *
     * @param URLorName
     * @return gserver or server
     */
    public GenericServer getServer(String URLorName) {
        GenericServer server = url2server.get(URLorName);
        if (server == null) {
            for (GenericServer gServer : getAllServers()) {
                if (gServer.getServerName().equals(URLorName)) {
                    return gServer;
                }
            }
        }
        return server;
    }

    //for now I must follow old conventions
    public void addServer(DataProvider dataProvider) {
        ServerTypeI serverType = getServerType(dataProvider.getType());

        addServer(serverType, dataProvider.getName(), dataProvider.getUrl(), Boolean.valueOf(dataProvider.getEnabled()),
                dataProvider.getOrder(), Boolean.valueOf(dataProvider.getDefault()), dataProvider.getMirror());
    }

    private static ServerTypeI getServerType(String type) {
        for (ServerTypeI t : ServerUtils.getServerTypes()) {
            if (type.equalsIgnoreCase(t.getName())) {
                return t;
            }
        }
        return LocalFilesServerType.getInstance();
    }

    public GenericServer addServer(ServerTypeI serverType, String name, String url,
            boolean enabled, int order, boolean isDefault, String mirrorURL) { //qlmirror
        url = ServerUtils.formatURL(url, serverType);
        GenericServer server = url2server.get(url);
        Object info;

        if (server == null) {
            info = serverType == null ? url : serverType.getServerInfo(url, name);

            if (info != null) {
                if (serverType == null || serverType.isSaveServersInPrefs()) {
                    Preferences node = getPreferencesNode().node(GenericServer.getHash(url));
                    if (node.get(SERVER_NAME, null) != null) {
                        name = node.get(SERVER_NAME, null); //Apply changes users may have made to server name
                    }
                }
                server = new GenericServer(name, url, serverType, enabled, info, isDefault, mirrorURL);
                url2server.put(url, server);
                addServerToPrefs(server, order, isDefault);
            }
        }

        return server;
    }

    public GenericServer addServer(Preferences node) {
        GenericServer server = url2server.get(GeneralUtils.URLDecode(node.get(SERVER_URL, "")));
        String url;
        String name;
        ServerTypeI serverType;
        Object info;

        if (server == null) {
            url = GeneralUtils.URLDecode(node.get(SERVER_URL, ""));
            name = node.get(SERVER_NAME, "Unknown");
            String type = node.get(SERVER_TYPE, hasTypes() ? LocalFilesServerType.getInstance().getName() : null);
            serverType = getServerType(type);
            url = ServerUtils.formatURL(url, serverType);
            info = (serverType == null) ? url : serverType.getServerInfo(url, name);

            if (info != null) {
                server = new GenericServer(node, info, serverType, false, null); //qlmirror

                if (server != null) {
                    url2server.put(url, server);
                }
            }
        }

        return server;
    }

    /**
     * Remove a server.
     *
     * @param url
     */
    public void removeServer(String url) {
        GenericServer server = url2server.get(url);
        url2server.remove(url);
        if (server != null) {
            server.clean();
            fireServerInitEvent(server, ServerStatus.NotResponding, true); // remove it from our lists.
        }
    }

    /**
     * Load server preferences from the Java preferences subsystem.
     */
    public void loadServerPrefs() {
        logger.info("Loading server preferences from the Java preferences subsystem");

        try {
            for (String nodeName : getPreferencesNode().childrenNames()) {
                Preferences node = getPreferencesNode().node(nodeName);
                processPreferenceNode(node);
            }
            logger.info("Completed loading server preferences from the Java preferences subsystem");
        } catch (BackingStoreException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void processPreferenceNode(Preferences node) {
        ServerTypeI serverType = null;
        if (node.get(SERVER_TYPE, null) != null) {
            serverType = getServerType(node.get(SERVER_TYPE, LocalFilesServerType.getInstance().getName()));
        }

        if (!(serverType == LocalFilesServerType.getInstance())) {
            addServer(node);
        }
    }

    /**
     * Update the old-style preference nodes to the newer format. This is now
     * called by the PrefsLoader when checking/updating the preferences version.
     */
    public void updateServerPrefs() {
        GenericServer server;

        for (ServerTypeI type : ServerUtils.getServerTypes()) {
            try {
                if (getPreferencesNode().nodeExists(type.toString())) {
                    Preferences prefServers = getPreferencesNode().node(type.toString());
                    String name, login, password, real_url;
                    boolean enabled;
                    //in here, again, the url is actually a hash of type long
                    for (String url : prefServers.keys()) {
                        name = prefServers.node(SERVER_NAME).get(url, "Unknown");
                        login = prefServers.node(SERVER_LOGIN).get(url, "");
                        password = prefServers.node(SERVER_PASSWORD).get(url, "");
                        enabled = prefServers.node(IS_SERVER_ENABLED).getBoolean(url, true);
                        real_url = prefServers.node(SERVER_URL).get(url, "");

                        server = addServerToPrefs(GeneralUtils.URLDecode(real_url), name, type, -1, false);
                        server.setLogin(login);
                        server.setEncryptedPassword(password);
                        server.setEnabled(enabled);
                    }
                    prefServers.removeNode();
                }
            } catch (BackingStoreException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    private Preferences getPreferencesNode() {
        return PreferenceUtils.getServersNode();
    }

    public void updateServerURLsInPrefs() {
        Preferences servers = getPreferencesNode();
        Preferences currentServer;
        String normalizedURL;
        String decodedURL;

        try {
            for (String encodedURL : servers.childrenNames()) {
                try {
                    currentServer = servers.node(encodedURL);
                    decodedURL = GeneralUtils.URLDecode(encodedURL);
                    String serverType = currentServer.get("type", "Unknown");
                    if (serverType.equals("Unknown")) {
                        logger.warn("server URL: {} could not be determined; ignoring.\nPreferences may be corrupted; clear preferences.", decodedURL);
                        continue;
                    }

                    normalizedURL = ServerUtils.formatURL(decodedURL, getServerType(serverType));

                    if (!decodedURL.equals(normalizedURL)) {
                        logger.debug("upgrading " + textName + " URL: ''{}'' in preferences", decodedURL);
                        Preferences normalizedServer = servers.node(GeneralUtils.URLEncode(normalizedURL));
                        for (String key : currentServer.keys()) {
                            normalizedServer.put(key, currentServer.get(key, ""));
                        }
                        currentServer.removeNode();
                    }
                } catch (Exception ex) {
                    // Allow preferences loading to continue if an exception is encountered.
                    logger.error(ex.getMessage(), ex);
                    continue;
                }
            }
        } catch (BackingStoreException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Add or update a server in the preferences subsystem. This only modifies
     * the preferences nodes, it does not affect any other part of the
     * application.
     *
     * @param url SERVER_URL of this server.
     * @param name name of this server.
     * @param type type of this server.
     * @return an anemic GenericServer object whose sole purpose is to aid in
     * setting of additional preferences
     */
    private GenericServer addServerToPrefs(String url, String name,
            ServerTypeI type, int order, boolean isDefault) {
        url = ServerUtils.formatURL(url, type);
        Preferences node = getPreferencesNode().node(GenericServer.getHash(url));
        if (node.get(SERVER_NAME, null) == null) {
            node.put(SERVER_NAME, name);
            node.put(SERVER_TYPE, type.getName());
            node.putInt(SERVER_ORDER, order);
            //Added url to preferences.
            //long url was bugging the node name since it only accepts 80 char names
            node.put(SERVER_URL, GeneralUtils.URLEncode(url));

        }
        return new GenericServer(node, null,
                getServerType(node.get(SERVER_TYPE, LocalFilesServerType.getInstance().getName())),
                isDefault, null); //qlmirror
    }

    /**
     * Add or update a server in the preferences subsystem. This only modifies
     * the preferences nodes, it does not affect any other part of the
     * application.
     *
     * @param server GenericServer object of the server to add or update.
     */
    public void addServerToPrefs(GenericServer server, int order, boolean isDefault) {
        if (server.getServerType().isSaveServersInPrefs()) {
            addServerToPrefs(server.getUrlString(), server.getServerName(), server.getServerType(), order, server.isDefault());

        }
    }

    /**
     * Remove a server from the preferences subsystem. This only modifies
     * the
     * preference nodes, it does not affect any other part of the
     * application.
     *
     * @param url SERVER_URL of the server to remove
     */
    public void removeServerFromPrefs(String url) {
        try {
            getPreferencesNode().node(GenericServer.getHash(url)).removeNode();
        } catch (BackingStoreException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void setServerOrder(String url, int order) {
        getPreferencesNode().node(GenericServer.getHash(url)).putInt(SERVER_ORDER, order);
    }

    private int getServerOrder(GenericServer server) {
        String url = ServerUtils.formatURL(server.getUrlString(), server.getServerType());
        return PreferenceUtils.getServersNode().node(GenericServer.getHash(url)).getInt(SERVER_ORDER, 0);
    }

    /**
     * Get server from ServerList that matches the SERVER_URL.
     *
     * @param u
     * @return server
     * @throws URISyntaxException
     */
    public GenericServer getServer(URL u) throws URISyntaxException {
        URI a = u.toURI();
        URI b;
        for (String url : url2server.keySet()) {
            try {
                b = new URI(url);
                if (!b.relativize(a).equals(a)) {
                    return url2server.get(url);
                }
            } catch (URISyntaxException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        throw new IllegalArgumentException("URL " + u.toString() + " is not a valid " + textName + ".");
    }

    public void addServerInitListener(GenericServerInitListener listener) {
        server_init_listeners.add(listener);
    }

    public void removeServerInitListener(GenericServerInitListener listener) {
        server_init_listeners.remove(listener);
    }

    public void fireServerInitEvent(GenericServer server, ServerStatus status, boolean removedManually) {
        Preferences node = getPreferencesNode().node(GenericServer.getHash(server.getUrlString()));
        if (status == ServerStatus.NotResponding) {
            if (server.getServerType() != null && !server.getServerType().isSaveServersInPrefs()) {
                removeServer(server.getUrlString());
            }

            if (!removedManually) {
                String errorText;
                if (server.getServerType() != null && server.getServerType() == QuickloadServerType.getInstance()) {

                    server.setEnabled(false);

                    //If the server was previously not available give the user the option to disable permanently
                    if (previouslyUnavailable(node)) {
                        if (ModalUtils.confirmPanel("The Quickload site named: " + server.getServerName() + " is still not responding. Would you like to ignore this site from now on?")) {
                            setEnableIfAvailable(node, false);
                        }
                    } else {
                        ErrorHandler.errorPanelWithReportBug(server.getServerName(), MessageFormat.format(GenometryConstants.BUNDLE.getString("quickloadConnectError"), server.getServerName()), Level.SEVERE);
                        DataLoadPrefsView.getSingleton().sourceTableModel.fireTableDataChanged();
                        //Ensure the server is checked for availibility on startup
                        setEnableIfAvailable(node, true);
                    }
                } else {
                    String superType = textName.substring(0, 1).toUpperCase() + textName.substring(1);
                    errorText = MessageFormat.format(GenometryConstants.BUNDLE.getString("connectError"), superType, server.getServerName());
                    if (server.getServerType() != null && server.getServerType().isSaveServersInPrefs()) {
                        ErrorHandler.errorPanel(server.getServerName(), errorText, Level.SEVERE);
                    } else {
                        logger.error(errorText);
                    }
                }
            }

//			if (server.serverType == null) {
//				IGB.getInstance().removeNotLockedUpMsg("Loading " + textName + " " + server);
//			} else if (server.serverType != LocalFilesServerType.getInstance()) {
//				IGB.getInstance().removeNotLockedUpMsg("Loading " + textName + " " + server + " (" + server.serverType.toString() + ")");
//			}
        }

        // Fire event whenever server status in set to initialized
        // or server status does not match previous status
        if (status == ServerStatus.Initialized || server.getServerStatus() != status) {

            //if is initialized after programmatic disabling then we should flip the enable_if_available flag
            if (status == ServerStatus.Initialized && node.getBoolean(ENABLE_IF_AVAILABLE, false)) {
                setEnableIfAvailable(node, false);
            }
            server.setServerStatus(status);
            GenericServerInitEvent evt = new GenericServerInitEvent(server);
            for (GenericServerInitListener listener : server_init_listeners) {
                listener.genericServerInit(evt);
            }
        }
    }

    private void setEnableIfAvailable(Preferences node, Boolean b) {
        //Sets the enableIfAvailable flag to true to ensure this server is checked on the next startup
        node.put(GenericServerPrefKeys.ENABLE_IF_AVAILABLE, b.toString());
    }

    private boolean previouslyUnavailable(Preferences node) {
        return node.getBoolean(GenericServerPrefKeys.ENABLE_IF_AVAILABLE, false);
    }

}
