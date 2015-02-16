package com.lorainelab.igb.plugins.repos;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.PreferenceUtils;
import com.affymetrix.igb.plugins.PluginsView;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.lorainelab.igb.plugins.repos.view.BundleRepositoryTableModel;
import com.lorainelab.igb.preferences.IgbPreferencesService;
import com.lorainelab.igb.preferences.model.IgbPreferences;
import com.lorainelab.igb.preferences.model.PluginRepository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(name = PluginRepositoryList.COMPONENT_NAME, immediate = true, provide = PluginRepositoryListProvider.class)
public class PluginRepositoryList implements PluginRepositoryListProvider {

    public static final String COMPONENT_NAME = "PluginRepositoryList";
    private static final Logger logger = LoggerFactory.getLogger(PluginRepositoryList.class);
    private IgbPreferencesService igbPreferencesService;
    private final List<PluginRepository> pluginRepositories;
    private PluginsView pluginsView;
    private BundleRepositoryTableModel bundleRepositoryTableModel;

    public PluginRepositoryList() {
        pluginRepositories = new ArrayList<>();
    }

    @Activate
    public void activate() {
        Optional<IgbPreferences> prefs = igbPreferencesService.fromDefaultPreferences();
        if (prefs.isPresent()) {
            prefs.get().getRepository().stream().forEach(repo -> {
                pluginRepositories.add(repo);
                if (!preferenceNodeExist(repo)) {
                    addRepositoryToPrefs(repo);
                }
            });
        }
        //Load persisted servers from java persistence api
        loadPersistedRepos();
        pluginRepositories.stream().forEach(repo -> {
            if (repo.isEnabled()) {
                pluginsView.addPluginRepository(repo);
            }
        });
        bundleRepositoryTableModel = new BundleRepositoryTableModel(this);
    }

    private void loadPersistedRepos() {
        try {
            for (String url : PreferenceUtils.getRepositoriesNode().childrenNames()) {
                Preferences node = PreferenceUtils.getRepositoriesNode().node(url);
                PluginRepository PluginRepository = getPluginRepositoryFromPreferencesNode(node);
                removeDuplicatesFromDefaultRepositories(PluginRepository);
                pluginRepositories.add(PluginRepository);
            }
        } catch (BackingStoreException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void removeDuplicatesFromDefaultRepositories(PluginRepository pr) {
        Iterator<PluginRepository> i = pluginRepositories.iterator();
        while (i.hasNext()) {
            PluginRepository pluginRepo = i.next();
            if (pluginRepo.getUrl().equals(pr.getUrl())) {
                i.remove();
            }
        }
    }

    @Reference(optional = false)
    public void setIgbPreferencesService(IgbPreferencesService igbPreferencesService) {
        this.igbPreferencesService = igbPreferencesService;
    }

    @Reference(optional = false)
    public void setPluginsView(PluginsView pluginsView) {
        this.pluginsView = pluginsView;
    }

    @Override
    public List<PluginRepository> getPluginRepositories() {
        return pluginRepositories;
    }

    @Override
    public void addPluginRepository(PluginRepository pluginRepository) {
        pluginRepositories.add(pluginRepository);
        if (pluginRepository.isEnabled()) {
            if (pluginsView.addPluginRepository(pluginRepository)) {
                addRepositoryToPrefs(pluginRepository);
            } else {
                pluginRepositories.remove(pluginRepository);
            }
        }
        bundleRepositoryTableModel.updateRepositories(pluginRepositories);
    }

    @Override
    public void removePluginRepository(PluginRepository pluginRepository) {
        pluginRepositories.remove(pluginRepository);
        removeRepositoryToPrefs(pluginRepository);
        pluginsView.removePluginRepository(pluginRepository);
        bundleRepositoryTableModel.updateRepositories(pluginRepositories);
    }

    private boolean preferenceNodeExist(PluginRepository pluginRepository) {
        try {
            String hashedUrlValue = getRepoUrlHash(pluginRepository);
            return PreferenceUtils.getRepositoriesNode().nodeExists(hashedUrlValue);
        } catch (BackingStoreException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return false;
    }

    private void addRepositoryToPrefs(PluginRepository pluginRepository) {
        String url = pluginRepository.getUrl();
        HashFunction hf = Hashing.md5();
        String hashedUrlValue = hf.hashString(url, Charsets.UTF_8).toString();
        Preferences node = PreferenceUtils.getRepositoriesNode().node(hashedUrlValue);
        node.put("name", pluginRepository.getName());
        node.put("url", GeneralUtils.URLEncode(url));
        node.put("enabled", pluginRepository.getEnabled());
    }

    @Override
    public void updatePluginRepoPrefs(PluginRepository pluginRepository) {
        addRepositoryToPrefs(pluginRepository);
    }

    private void removeRepositoryToPrefs(PluginRepository pluginRepository) {
        String hashedUrlValue = getRepoUrlHash(pluginRepository);
        try {
            PreferenceUtils.getRepositoriesNode().node(hashedUrlValue).removeNode();
        } catch (BackingStoreException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private String getRepoUrlHash(PluginRepository pluginRepository) {
        String url = pluginRepository.getUrl();
        HashFunction hf = Hashing.md5();
        String hashedUrlValue = hf.hashString(url, Charsets.UTF_8).toString();
        return hashedUrlValue;
    }

    private PluginRepository getPluginRepositoryFromPreferencesNode(Preferences node) {
        String reporUrl = GeneralUtils.URLDecode(node.get("url", ""));
        String repoName = node.get("name", "");
        Boolean enabled = node.getBoolean("enabled", false);
        PluginRepository pluginRepository = new PluginRepository();
        pluginRepository.setName(repoName);
        pluginRepository.setEnabled(enabled);
        pluginRepository.setUrl(reporUrl);
        return pluginRepository;
    }

    @Override
    public void pluginRepositoryRefreshed() {
        pluginsView.updateBundleTable();
    }

    @Override
    public void pluginRepoAvailabilityChanged(PluginRepository pluginRepository) {
        if (pluginRepository.isEnabled()) {
            pluginsView.addPluginRepository(pluginRepository);
        } else {
            pluginsView.removePluginRepository(pluginRepository);
        }
    }

    @Override
    public BundleRepositoryTableModel getBundleRepositoryTableModel() {
        return bundleRepositoryTableModel;
    }

}
