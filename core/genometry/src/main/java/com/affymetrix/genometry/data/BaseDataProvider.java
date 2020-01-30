package com.affymetrix.genometry.data;

import com.affymetrix.common.PreferenceUtils;
import static com.affymetrix.genometry.general.DataProviderPrefKeys.*;
import com.affymetrix.genometry.util.LoadUtils.ResourceStatus;
import static com.affymetrix.genometry.util.LoadUtils.ResourceStatus.Disabled;
import static com.affymetrix.genometry.util.LoadUtils.ResourceStatus.Initialized;
import static com.affymetrix.genometry.util.LoadUtils.ResourceStatus.NotInitialized;
import com.affymetrix.genometry.util.StringEncrypter;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Strings;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public abstract class BaseDataProvider implements DataProvider {

    private static final Logger logger = LoggerFactory.getLogger(BaseDataProvider.class);
    private Preferences preferencesNode;
    protected final String url;
    protected String mirrorUrl;
    protected String name;
    protected String login;
    protected String password;
    protected int loadPriority;
    protected ResourceStatus status;
    private StringEncrypter encrypter;
    protected boolean useMirror;
    protected String defaultDataProviderId;

    public BaseDataProvider(String url, String name, int loadPriority) {
        this.url = checkNotNull(url);
        this.name = checkNotNull(name);
        this.loadPriority = loadPriority;
        encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME);
        preferencesNode = PreferenceUtils.getDataProviderNode(url);
        loadPersistedConfiguration();
        initializePreferences();
    }

    public BaseDataProvider(String url, String name, String mirrorUrl, int loadPriority) {
        this.url = checkNotNull(url);
        this.name = checkNotNull(name);
        this.loadPriority = loadPriority;
        this.mirrorUrl = checkNotNull(mirrorUrl);
        encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME);
        preferencesNode = PreferenceUtils.getDataProviderNode(url);
        loadPersistedConfiguration();
        initializePreferences();
    }
    
    public BaseDataProvider(String url, String name, int loadPriority, String id) {
        this.url = checkNotNull(url);
        this.name = checkNotNull(name);
        this.loadPriority = loadPriority;
        this.defaultDataProviderId = id;
        encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME);
        preferencesNode = PreferenceUtils.getDataProviderNode(url);
        loadPersistedConfiguration();
        initializePreferences();
    }
    
    public BaseDataProvider(String url, String name, String mirrorUrl, int loadPriority, String id) {
        this.url = checkNotNull(url);
        this.name = checkNotNull(name);
        this.loadPriority = loadPriority;
        this.defaultDataProviderId = id;
        this.mirrorUrl = checkNotNull(mirrorUrl);
        encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME);
        preferencesNode = PreferenceUtils.getDataProviderNode(url);
        loadPersistedConfiguration();
        initializePreferences();
    }

    private void loadPersistedConfiguration() {
        Optional.ofNullable(preferencesNode.get(PROVIDER_NAME, null)).ifPresent(preferenceValue -> name = preferenceValue);
        Optional.ofNullable(preferencesNode.get(LOAD_PRIORITY, null)).ifPresent(preferenceValue -> loadPriority = Integer.parseInt(preferenceValue));
        Optional.ofNullable(preferencesNode.get(MIRROR_URL, null)).ifPresent(preferenceValue -> mirrorUrl = preferenceValue);
        Optional.ofNullable(preferencesNode.get(LOGIN, null)).ifPresent(preferenceValue -> {
            if (!Strings.isNullOrEmpty(preferenceValue)) {
                login = preferenceValue;
            }
        });
        Optional.ofNullable(preferencesNode.get(PASSWORD, null)).ifPresent(preferenceValue -> {
            if (!Strings.isNullOrEmpty(preferenceValue)) {
                password = encrypter.decrypt(preferenceValue);
            }
        });
        Optional.ofNullable(preferencesNode.get(STATUS, null)).ifPresent(preferenceValue -> {
            ResourceStatus.fromName(preferenceValue).ifPresent(matchingStatus -> status = matchingStatus);
            if (status == Initialized) {
                status = NotInitialized;
            }
        });
    }

    private void initializePreferences() {
        preferencesNode.put(PRIMARY_URL, url);
        preferencesNode.put(PROVIDER_NAME, name);       
        preferencesNode.putInt(LOAD_PRIORITY, loadPriority);
        if (!Strings.isNullOrEmpty(mirrorUrl)) {
            preferencesNode.put(MIRROR_URL, mirrorUrl);
        }
        if (!Strings.isNullOrEmpty(defaultDataProviderId)){
            preferencesNode.put(DEFAULT_PROVIDER_ID, defaultDataProviderId);
        }
    }

    protected abstract void disable();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        preferencesNode.put(PROVIDER_NAME, name);
    }

    @Override
    public int getLoadPriority() {
        return loadPriority;
    }

    @Override
    public void setLoadPriority(int loadPriority) {
        this.loadPriority = loadPriority;
        preferencesNode.putInt(LOAD_PRIORITY, loadPriority);
    }

    @Override
    public String getUrl() {
        if (useMirror) {
            return mirrorUrl;
        } else {
            return url;
        }
    }

    @Override
    public Optional<String> getMirrorUrl() {
        return Optional.ofNullable(mirrorUrl);
    }

    @Override
    public void setMirrorUrl(String mirrorUrl) {
        this.mirrorUrl = mirrorUrl;
        preferencesNode.put(MIRROR_URL, mirrorUrl);
    }

    @Override
    public boolean useMirrorUrl() {
        return useMirror;
    }

    @Override
    public Optional<String> getLogin() {
        return Optional.ofNullable(login);
    }

    @Override
    public void setLogin(String login) {
        this.login = login;
        if (login == null) {
            preferencesNode.remove(LOGIN);
        } else if (preferencesNode.getBoolean(REMEMBER_CREDENTIALS, false)) {
            preferencesNode.put(LOGIN, login);
        }
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
        if (password == null) {
            preferencesNode.remove(PASSWORD);
        } else if (preferencesNode.getBoolean(REMEMBER_CREDENTIALS, false)) {
            if (password.isEmpty()) {
                preferencesNode.put(PASSWORD, "");
            } else {
                preferencesNode.put(PASSWORD, encrypter.encrypt(password));
            }
        }

    }

    @Override
    public ResourceStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(ResourceStatus status) {
        this.status = status;
        preferencesNode.put(STATUS, status.toString());
        if (status == Disabled) {
            useMirror = false;
            disable();
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BaseDataProvider other = (BaseDataProvider) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }

    @Override
    public void setId(String id){
        this.defaultDataProviderId = id;
    }

    @Override
    public Optional<String> getId(){
        return Optional.ofNullable(defaultDataProviderId);
    }
    
    

}
