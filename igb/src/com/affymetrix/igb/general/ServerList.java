package com.affymetrix.igb.general;

import com.affymetrix.genometryImpl.event.GenericServerInitEvent;
import com.affymetrix.genometryImpl.event.GenericServerInitListener;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerType;
import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.genometryImpl.util.GeneralUtils;
import com.affymetrix.genometryImpl.util.LoadUtils.ServerStatus;
import com.affymetrix.igb.das.DasServerInfo;
import com.affymetrix.igb.das2.Das2ServerInfo;
import com.affymetrix.igb.util.StringEncrypter;
import com.affymetrix.igb.util.StringEncrypter.EncryptionException;
import com.affymetrix.igb.util.UnibrowPrefsUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @version $Id$
 */
public final class ServerList {
	private final static Map<String, GenericServer> url2server = new LinkedHashMap<String, GenericServer>();
	private final static Set<GenericServerInitListener> server_init_listeners = new CopyOnWriteArraySet<GenericServerInitListener>();

	public static Set<GenericServer> getEnabledServers() {
		Set<GenericServer> serverList = new HashSet<GenericServer>();
		for (GenericServer gServer : getAllServers()) {
			if (gServer.enabled && gServer.getServerStatus() != ServerStatus.NotResponding) {
				serverList.add(gServer);
			}
		}
		return serverList;
	}

	public static Set<GenericServer> getInitializedServers() {
		Set<GenericServer> serverList = new HashSet<GenericServer>();
		for (GenericServer gServer : ServerList.getEnabledServers()) {
			if (gServer.getServerStatus() == ServerStatus.Initialized) {
				serverList.add(gServer);
			}
		}
		return serverList;
	}

	public static Collection<GenericServer> getAllServers() {
		return url2server.values();
	}

	/*public static Map<String, String> getUrls() {
	return url2Name;
	}*/
	/**
	 *  Given an URLorName string which should be the resolvable root URL
	 *  (but may optionally be the server name)
	 *  Return the GenericServer object.  (This could be non-unique if passed a name.)
	 *
	 * @param URLorName
	 * @return gserver or server
	 */
	public static GenericServer getServer(String URLorName) {
		GenericServer server = url2server.get(URLorName);
		if (server == null) {
			for (GenericServer gServer : getAllServers()) {
				if (gServer.serverName.equals(URLorName)) {
					return gServer;
				}
			}
		}
		return server;
	}

	/**
	 *
	 * @param serverType
	 * @param name
	 * @param url
	 * @return GenericServer
	 */
	public static GenericServer addServer(ServerType serverType, String name, String url) {
		if (url2server.containsKey(url)) {
			return url2server.get(url);
		}
		return initServer(serverType, url, name, true, false);
	}
	
	/**
	 * Remove a server.
	 * @param url
	 */
	public static void removeServer(String url) {
		GenericServer server = url2server.get(url);
		url2server.remove(url);
		server.enabled = false;
		fireServerInitEvent(server, ServerStatus.NotResponding);	// remove it from our lists.
	}

	/**
	 * Initialize the server.
	 * @param serverType
	 * @param url
	 * @param name
	 * @return initialized server
	 */
	private static GenericServer initServer(ServerType serverType, String url, String name, boolean enabled, boolean hardcodedPrefs) {
		GenericServer server = null;
		try {
			if (serverType == ServerType.Unknown) {
				return null;
			}
			if (serverType == ServerType.QuickLoad) {
				String root_url = url;
				if (!root_url.endsWith("/")) {
					root_url = root_url + "/";
				}
				server = new GenericServer(name, root_url, serverType, hardcodedPrefs, root_url);
			}
			if (serverType == ServerType.DAS) {
				DasServerInfo info = new DasServerInfo(url);
				server = new GenericServer(name, info.getURL().toString(), serverType, hardcodedPrefs, info);
			}
			if (serverType == ServerType.DAS2) {
				Das2ServerInfo info = new Das2ServerInfo(url, name, false);
				server = new GenericServer(name, info.getURI().toString(), serverType, hardcodedPrefs, info);
			}
			server.enabled = enabled;
			url2server.put(url, server);
			return server;

		} catch (Exception e) {
			System.out.println("WARNING: Could not initialize " + serverType + " server with address: " + url);
			e.printStackTrace(System.out);
		}
		return server;
	}

	/**
	 * Load server preferences from the Java preferences subsystem.
	 */
	public static void loadServerPrefs() {
		String server_name, login, password;
		ServerType serverType;
		Boolean enabled;
		try {
			for (String serverURL : UnibrowPrefsUtil.getServersNode().childrenNames()) {
				Preferences node = UnibrowPrefsUtil.getServersNode().node(serverURL);

				serverURL = GeneralUtils.URLDecode(serverURL);
				server_name = node.get("name", "Unknown");
				serverType = ServerType.valueOf(node.get("type", "Unknown"));

				login = node.get("login", "");
				password = decrypt(node.get("password", ""));

				enabled = node.getBoolean("enabled", true);

				System.out.println("Adding " + server_name + ":" + serverURL + " " + serverType);
				
				if (serverType == ServerType.Unknown) {
					System.out.println("WARNING: this server has an unknown type.  Skipping");
					continue;
				}

				// Add the server
				GenericServer server = addServer(serverType, server_name, serverURL);

				if (server != null) {
					server.login = login;
					server.password = password;
					server.enabled = enabled;
				}
			}
		} catch (BackingStoreException ex) {
			Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Update the old-style preference nodes to the newer format.  This is now
	 * called by the PrefsLoader when checking/updating the preferences version.
	 */
	public static void updateServerPrefs() {
		for (ServerType type : ServerType.values()) {
			try {
				if (UnibrowPrefsUtil.getServersNode().nodeExists(type.toString())) {
					Preferences prefServers = UnibrowPrefsUtil.getServersNode().node(type.toString());
					String name, login, password;
					boolean authEnabled, enabled;
					for (String url : prefServers.keys()) {
						name        = prefServers.get(url, "Unknown");
						login       = prefServers.node("login").get(url, "");
						password    = decrypt(prefServers.node("password").get(url, ""));
						authEnabled = !(login.isEmpty() || password.isEmpty());
						enabled     = Boolean.parseBoolean(prefServers.node("enabled").get(url, "true"));

						addServerToPrefs(GeneralUtils.URLDecode(url), name, type, authEnabled, login, password, enabled);
					}
					prefServers.removeNode();
				}
			} catch (BackingStoreException ex) {
				Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public static void updateServerURLsInPrefs() {
		Preferences servers = UnibrowPrefsUtil.getServersNode();
		Preferences currentServer;
		String normalizedURL;
		String decodedURL;
		
		try {
			for (String encodedURL : servers.childrenNames()) {
				currentServer = servers.node(encodedURL);
				decodedURL = GeneralUtils.URLDecode(encodedURL);
				normalizedURL = formatURL(decodedURL, ServerType.valueOf(currentServer.get("type", "Unknown")));

				if (!decodedURL.equals(normalizedURL)) {
					Logger.getLogger(ServerList.class.getName()).log(Level.FINE, "upgrading server URL: '" + decodedURL + "' in preferences");
					Preferences normalizedServer = servers.node(GeneralUtils.URLEncode(normalizedURL));
					for (String key : currentServer.keys()) {
						normalizedServer.put(key, currentServer.get(key, ""));
					}
					currentServer.removeNode();
				}
			}
		} catch (BackingStoreException ex) {
			Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Add or update a server in the preferences subsystem.  This only modifies
	 * the preferences nodes, it does not affect any other part of the application.
	 *
	 * @param url URL of this server.
	 * @param name name of this server.
	 * @param type type of this server.
	 * @param authEnabled boolean noting if client should authenticate to this server
	 * @param login account to use if attemting to authenticate to this server
	 * @param password password to use if attempting to authenticate to this server
	 * @param enabled boolean indicating whether this server is enabled.
	 */
	public static void addServerToPrefs(String url, String name, ServerType type, boolean authEnabled, String login, String password, boolean enabled) {
		url = formatURL(url, type);
		Preferences node = UnibrowPrefsUtil.getServersNode().node(GeneralUtils.URLEncode(formatURL(url, type)));

		node.put("name",  name);
		node.put("type", type.toString());

		if (authEnabled) {
			node.put("login", login);
			node.put("password", encrypt(password));
		} else {
			node.remove("login");
			node.remove("password");
		}

		node.putBoolean("enabled", enabled);
	}

	/**
	 * Add or update a server in the preferences subsystem.  This only modifies
	 * the preferences nodes, it does not affect any other part of the application.
	 *
	 * @param server GenericServer object of the server to add or update.
	 */
	public static void addServerToPrefs(GenericServer server) {
		boolean authEnabled = (server.login != null && server.password != null) && !(server.login.isEmpty() || server.password.isEmpty());
		addServerToPrefs(server.URL, server.serverName, server.serverType, authEnabled, server.login, server.password, server.enabled);
	}

	/**
	 * Remove a server from the preferences subsystem.  This only modifies the
	 * preference nodes, it does not affect any other part of the application.
	 *
	 * @param url  URL of the server to remove
	 */
	public static void removeServerFromPrefs(String url) {
		try {
			UnibrowPrefsUtil.getServersNode().node(GeneralUtils.URLEncode(url)).removeNode();
		} catch (BackingStoreException ex) {
			Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Check if a server exists in the preferences subsystem.  This method is
	 * used to determine if a server was added from the preferences subsystem
	 * or an external source.
	 *
	 * @param url URL of the server to check
	 * @return true if the url is in the preferences subsystem
	 */
	public static boolean inServerPrefs(String url) {
		try {
			return UnibrowPrefsUtil.getServersNode().nodeExists(GeneralUtils.URLEncode(url));
		} catch (BackingStoreException ex) {
			Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Decrypt the given password.
	 *
	 * @param encrypted encrypted representation of the password
	 * @return string representation of the password
	 */
	private static String decrypt(String encrypted) {
		if (!encrypted.isEmpty()) {
			try {
				StringEncrypter encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME);
				return encrypter.decrypt(encrypted);
			} catch (EncryptionException ex) {
				Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
				throw new IllegalArgumentException(ex);
			}
		}
		return "";
	}

	/**
	 * Encrypt the given password.
	 * 
	 * @param password unencrypted password string
	 * @return the encrypted representation of the password
	 */
	private static String encrypt(String password) {
		if (!password.isEmpty()) {
			try {
				StringEncrypter encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME);
				return encrypter.encrypt(password);
			} catch (Exception ex) {
				Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
				throw new IllegalArgumentException(ex);
			}
		}
		return "";
	}

	/**
	 * Format a URL based on the ServerType's requirements.
	 *
	 * @param url URL to format
	 * @param type type of server the URL represents
	 * @return formatted URL
	 */
	private static String formatURL(String url, ServerType type) {
		try {
			/* remove .. and // from URL */
			url = new URI(url).normalize().toASCIIString();
		} catch (URISyntaxException ex) {
			String message = "Unable to parse URL: '" + url + "'";
			Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, message, ex);
			throw new IllegalArgumentException(message, ex);
		}
		switch (type) {
			case DAS:
			case DAS2:
				return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
			case QuickLoad:
				return url.endsWith("/") ? url : url + "/";
			default:
				return url;
		}
	}

	/**
	 * Get server from ServerList that matches the URL.
	 * @param u
	 * @return server
	 * @throws URISyntaxException
	 */
	public static GenericServer getServer(URL u) throws URISyntaxException {
		URI a = u.toURI();
		URI b;
		for (String url : url2server.keySet()) {
			try {
				b = new URI(url);
				if (!b.relativize(a).equals(a)) {
					return url2server.get(url);
				}
			} catch (URISyntaxException ex) {
				Logger.getLogger(ServerList.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		throw new IllegalArgumentException("URL " + u.toString() + " is not a valid server.");
	}

	public static void addServerInitListener(GenericServerInitListener listener) {
		server_init_listeners.add(listener);
	}

	public static void fireServerInitEvent(GenericServer server, ServerStatus status) {
		if (server.getServerStatus() != status) {
			server.setServerStatus(status);
			GenericServerInitEvent evt = new GenericServerInitEvent(server);
			for (GenericServerInitListener listener : server_init_listeners) {
				listener.genericServerInit(evt);
			}
		}
	}

}
