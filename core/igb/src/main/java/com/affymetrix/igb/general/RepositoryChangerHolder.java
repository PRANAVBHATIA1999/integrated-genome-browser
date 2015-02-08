package com.affymetrix.igb.general;

import com.affymetrix.genometry.event.GenericServerInitListener;
import com.affymetrix.genometry.event.RepositoryChangeListener;
import com.affymetrix.genometry.general.GenericServer;
import com.affymetrix.genometry.util.LoadUtils.ServerStatus;
import com.affymetrix.igb.prefs.PreferencesPanel;
import com.affymetrix.igb.service.api.RepositoryChangeHolderI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;

public class RepositoryChangerHolder implements RepositoryChangeHolderI {

    private static final RepositoryChangerHolder instance = new RepositoryChangerHolder();

    public static RepositoryChangerHolder getInstance() {
        return instance;
    }

    private final GenericServerInitListener genericServerListener = evt -> {
        GenericServer gServer = (GenericServer) evt.getSource();
        if (gServer.getServerStatus() == ServerStatus.Initialized) {
            repositoryAdded(gServer.URL);
        } else if (gServer.getServerStatus() == ServerStatus.NotResponding) {
            repositoryRemoved(gServer.URL);
        }
    };

    private final List<RepositoryChangeListener> repositoryChangeListeners = new ArrayList<>();

    private RepositoryChangerHolder() {
        super();
        ServerList.getRepositoryInstance().addServerInitListener(genericServerListener);
    }

    /**
     * add a RepositoryChangeListener, to be called when there
     * is a change to the Bundle Repositories on the Bundle
     * Repository tab of the Preferences page
     *
     * @param repositoryChangeListener the listener
     */
    @Override
    public void addRepositoryChangeListener(RepositoryChangeListener repositoryChangeListener) {
        repositoryChangeListeners.add(repositoryChangeListener);
    }

    /**
     * remove a RepositoryChangeListener - so that is is no longer
     * called for changes to the Bundle Repository tab of
     * the Perferences page
     *
     * @param repositoryChangeListener the listener
     */
    @Override
    public void removeRepositoryChangeListener(RepositoryChangeListener repositoryChangeListener) {
        repositoryChangeListeners.remove(repositoryChangeListener);
    }

    @Override
    public boolean repositoryAdded(String url) {
        boolean addedOK = true;
        for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
            addedOK &= repositoryChangeListener.repositoryAdded(url);
        }
        return addedOK;
    }

    @Override
    public void repositoryRemoved(String url) {
        for (RepositoryChangeListener repositoryChangeListener : repositoryChangeListeners) {
            repositoryChangeListener.repositoryRemoved(url);
        }
    }

    /**
     * get the list of all repositories from the
     * Bundle Repository Preferences tab
     *
     * @return the list of bundle repositories (URLs)
     */
    @Override
    public Map<String, String> getRepositories() {
        Map<String, String> repositories = new HashMap<>();
        ServerList.getRepositoryInstance().getAllServers().stream().filter(repositoryServer -> repositoryServer.isEnabled()).forEach(repositoryServer -> {
            repositories.put(repositoryServer.serverName, repositoryServer.URL);
        });
        return repositories;
    }

    /**
     * mark a bundle repository as down / unavailable
     * due to an error trying to connect
     *
     * @param url the URL to mark
     */
    @Override
    public void failRepository(String url) {
        ServerList.getRepositoryInstance().getServer(url).setEnabled(false);
    }

    /**
     * display the Bundle Repository tab of the Preferences page
     */
    @Override
    public void displayRepositoryPreferences() {
        if (PreferencesPanel.TAB_PLUGIN_PREFS != -1) {
            PreferencesPanel pv = PreferencesPanel.getSingleton();
            pv.setTab(PreferencesPanel.TAB_PLUGIN_PREFS);	// Repository preferences tab
            JFrame f = pv.getFrame();
            f.setVisible(true);
        } else {
            System.out.println("Plugin Repository Preferences not instantiated");
        }
    }
}
