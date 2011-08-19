package com.affymetrix.igb.general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;


import com.affymetrix.genometryImpl.event.RepositoryChangeListener;
import com.affymetrix.genometryImpl.general.GenericServer;
import com.affymetrix.igb.IGB;
import com.affymetrix.igb.osgi.service.RepositoryChangeHolderI;
import com.affymetrix.igb.prefs.PreferencesPanel;

public class RepositoryChangerHolder implements RepositoryChangeHolderI {
	private static RepositoryChangerHolder instance = new RepositoryChangerHolder();
	
	private RepositoryChangerHolder() {
		super();
	}
	
	public static RepositoryChangerHolder getInstance() {
		return instance;
	}

	private List<RepositoryChangeListener> repositoryChangeListeners = new ArrayList<RepositoryChangeListener>();


	/**
	 * add a RepositoryChangeListener, to be called when there
	 * is a change to the Bundle Repositories on the Bundle
	 * Repository tab of the Preferences page
	 * @param repositoryChangeListener the listener
	 */
	public void addRepositoryChangeListener(RepositoryChangeListener repositoryChangeListener) {
		repositoryChangeListeners.add(repositoryChangeListener);
	}

	/**
	 * remove a RepositoryChangeListener - so that is is no longer
	 * called for changes to the Bundle Repository tab of
	 * the Perferences page
	 * @param repositoryChangeListener the listener
	 */
	public void removeRepositoryChangeListener(RepositoryChangeListener repositoryChangeListener) {
		repositoryChangeListeners.remove(repositoryChangeListener);
	}


	public boolean repositoryAdded(String url) {
		boolean addedOK = true;
		for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
			addedOK &= repositoryChangeListener.repositoryAdded(url);
		}
		return addedOK;
	}

	public void repositoryRemoved(String url) {
		for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
			repositoryChangeListener.repositoryRemoved(url);
		}
	}

	/**
	 * get the list of all repositories from the 
	 * Bundle Repository Preferences tab
	 * @return the list of bundle repositories (URLs)
	 */
	public Map<String, String> getRepositories() {
		Map<String, String> repositories = new HashMap<String, String>();
		for (GenericServer repositoryServer : ServerList.getRepositoryInstance().getAllServers()) {
			if (repositoryServer.isEnabled()) {
				repositories.put(repositoryServer.serverName, repositoryServer.URL);
			}
		}
		return repositories;
	}

	/**
	 * mark a bundle repository as down / unavailable
	 * due to an error trying to connect
	 * @param url the URL to mark
	 */
	public void failRepository(String url) {
		ServerList.getRepositoryInstance().removeServer(url);
	}

	/**
	 * display the Bundle Repository tab of the Preferences page
	 */
	public void displayRepositoryPreferences() {
		if (IGB.TAB_PLUGIN_PREFS != -1) {
			PreferencesPanel pv = PreferencesPanel.getSingleton();
			pv.setTab(IGB.TAB_PLUGIN_PREFS);	// Repository preferences tab
			JFrame f = pv.getFrame();
			f.setVisible(true);
		} else {
			System.out.println("Plugin Repository Preferences not instantiated");
		}
	}
}
