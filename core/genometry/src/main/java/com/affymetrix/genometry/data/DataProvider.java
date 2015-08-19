package com.affymetrix.genometry.data;

import com.affymetrix.genometry.general.DataContainer;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.util.LoadUtils.ResourceStatus;
import com.google.common.collect.SetMultimap;
import com.lorainelab.synonymlookup.services.SpeciesInfo;
import java.util.Optional;
import java.util.Set;

/**
 * TODO - add full description
 *
 * @author dcnorris
 */
public interface DataProvider {

    /**
     * @return name of the data provider which will populate name column of "Data Source Provider" panel
     */
    public String getName();

    /**
     * @param name which will populate name column of "Data Sources" panel
     */
    public void setName(String name);

    /**
     * @return required globally unique URL
     */
    public String getUrl();

    /**
     * It is expected this method will be called before any DataProvider content is loaded.
     * The expectation is implementors will wait for a call to this method before making remote request
     * to initialize content.
     */
    public void initialize();

    public Set<DataSet> getAvailableDataSets(DataContainer dataContainer);

    /**
     * @return set of genome versions for which data is available
     */
    public Set<String> getSupportedGenomeVersionNames();

    /**
     * If a DataProvider is providing data for a species which does not already exist,
     * providing SpeciesInfo is required, otherwise this is not a required file.
     */
    public default Optional<Set<SpeciesInfo>> getSpeciesInfo() {
        return Optional.empty();
    }

    public default Optional<SetMultimap<String, String>> getGenomeVersionSynonyms() {
        return Optional.empty();
    }

    public default Optional<String> getGenomeVersionDescription(String genomeVersionName) {
        return Optional.empty();
    }

    /**
     * Not all DataProviders will be generated by a Factory, so this is an optional method
     *
     * @return the name of the DataProviderFactory which generated this DataProvider
     */
    public default Optional<String> getFactoryName() {
        return Optional.empty();
    }

    /**
     * @return Returns the default load priority
     * this priority will be used to determine the order
     * in which to query DataProvider instances. Users will be able to override the value returned here.
     */
    public int getLoadPriority();

    /**
     * sets the load priority
     *
     * @param loadPriority
     */
    public void setLoadPriority(int loadPriority);

    /**
     * @return Optional mirror url for automatic failover
     */
    public default Optional<String> getMirrorUrl() {
        return Optional.empty();
    }

    public void setMirrorUrl(String mirrorUrl);

    public boolean useMirrorUrl();

    /**
     * @return Checks and returns current server status,
     * this status drives
     */
    public ResourceStatus getStatus();

    public void setStatus(ResourceStatus serverStatus);

    public Optional<String> getLogin();

    public void setLogin(String login);

    public Optional<String> getPassword();

    public void setPassword(String password);

    public default Optional<String> getPrimaryLinkoutUrl() {
        return Optional.empty();
    }

    public default Optional<String> getDataSetLinkoutUrl(DataSet dataSet) {
        return Optional.empty();
    }

}
