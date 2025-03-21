package org.lorainelab.igb.ucsc.rest.api.service;

import com.affymetrix.common.PreferenceUtils;
import com.affymetrix.genometry.data.DataProvider;
import com.affymetrix.genometry.data.DataProviderFactory;
import com.affymetrix.genometry.general.DataProviderPrefKeys;
import org.osgi.service.component.annotations.Component;

import static org.lorainelab.igb.ucsc.rest.api.service.utils.UCSCRestServerUtils.toExternalForm;


@Component(name = UCSCRestDataProviderFactory.COMPONENT_NAME, immediate = true)
public class UCSCRestDataProviderFactory implements DataProviderFactory {

    public static final String COMPONENT_NAME = "UCSCRestApiDataProviderFactory";
    public static final String FACTORY_NAME = "UCSC REST";
    private static final int WEIGHT = 3;

    @Override
    public String getFactoryName() {
        return FACTORY_NAME;
    }

    @Override
    public DataProvider createDataProvider(String url, String name, int loadPriority) {
        url = toExternalForm(url.trim());
        UCSCRestApiDataProvider ucscRestApiDataProvider = new UCSCRestApiDataProvider(url, name, loadPriority);
        PreferenceUtils.getDataProviderNode(url).put(DataProviderPrefKeys.FACTORY_NAME, FACTORY_NAME);
        return ucscRestApiDataProvider;
    }

    @Override
    public DataProvider createDataProvider(String url, String name, String mirrorUrl, int loadPriority) {
        url = toExternalForm(url.trim());
        UCSCRestApiDataProvider ucscRestApiDataProvider = new UCSCRestApiDataProvider(url, name, mirrorUrl, loadPriority);
        PreferenceUtils.getDataProviderNode(url).put(DataProviderPrefKeys.FACTORY_NAME, FACTORY_NAME);
        return ucscRestApiDataProvider;
    }

    @Override
    public DataProvider createDataProvider(int loadPriority, String url, String name, String id, String datasetLinkoutDomainUrl) {
        url = toExternalForm(url.trim());
        UCSCRestApiDataProvider ucscRestApiDataProvider = new UCSCRestApiDataProvider(loadPriority, url, name, id, datasetLinkoutDomainUrl);
        PreferenceUtils.getDataProviderNode(url).put(DataProviderPrefKeys.FACTORY_NAME, FACTORY_NAME);
        return ucscRestApiDataProvider;
    }

        @Override
    public DataProvider createDataProvider(String url, String name, int loadPriority, String id) {
        url = toExternalForm(url.trim());
        UCSCRestApiDataProvider ucscRestApiDataProvider = new UCSCRestApiDataProvider(url, name, loadPriority, id);
        PreferenceUtils.getDataProviderNode(url).put(DataProviderPrefKeys.FACTORY_NAME, FACTORY_NAME);
        return ucscRestApiDataProvider;
    }

    @Override
    public DataProvider createDataProvider(String url, String name, String mirrorUrl, int loadPriority, String id) {
        url = toExternalForm(url.trim());
        UCSCRestApiDataProvider ucscRestApiDataProvider = new UCSCRestApiDataProvider(url, name, mirrorUrl, loadPriority, id);
        PreferenceUtils.getDataProviderNode(url).put(DataProviderPrefKeys.FACTORY_NAME, FACTORY_NAME);
        return ucscRestApiDataProvider;
    }
    
    @Override
    public int getWeight() {
        return WEIGHT;
    }

}

