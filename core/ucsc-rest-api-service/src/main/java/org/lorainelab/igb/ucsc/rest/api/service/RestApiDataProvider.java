package org.lorainelab.igb.ucsc.rest.api.service;

import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.data.BaseDataProvider;
import com.affymetrix.genometry.data.assembly.AssemblyProvider;
import com.affymetrix.genometry.data.sequence.ReferenceSequenceProvider;
import com.affymetrix.genometry.general.DataContainer;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.util.LoadUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.lorainelab.igb.ucsc.rest.api.service.model.GenomesData;
import org.lorainelab.igb.ucsc.rest.api.service.utils.UCSCRestServerUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.affymetrix.genometry.util.LoadUtils.ResourceStatus.*;

@Slf4j
public final class RestApiDataProvider extends BaseDataProvider implements AssemblyProvider, ReferenceSequenceProvider {

    private final Set<String> availableGenomesSet;

    public RestApiDataProvider(String ucscRestUrl, String name, int loadPriority) {
        super(ucscRestUrl, name, loadPriority);
        availableGenomesSet = Sets.newHashSet();
        try {
            URL ucscRestDsnUrl = new URL(url);
        } catch (MalformedURLException ex) {
            log.error(ex.getMessage(), ex);
            setStatus(Disabled);
        }
        if (status != Disabled) {
            initialize();
        }
    }

    public RestApiDataProvider(String ucscRestUrl, String name, String mirrorUrl, int loadPriority) {
        super(ucscRestUrl, name, mirrorUrl, loadPriority);
        availableGenomesSet = Sets.newHashSet();
        try {
            URL ucscRestDsnUrl = new URL(ucscRestUrl);
        } catch (MalformedURLException | IllegalArgumentException ex) {
            log.error(ex.getMessage(), ex);
            setStatus(Disabled);
        }
        if (status != Disabled) {
            initialize();
        }
    }

    public RestApiDataProvider(String ucscRestUrl, String name, int loadPriority, String id) {
        super(ucscRestUrl, name, loadPriority, id);
        availableGenomesSet = Sets.newHashSet();
        try {
            URL ucscRestDsnUrl = new URL(url);
        } catch (MalformedURLException ex) {
            log.error(ex.getMessage(), ex);
            setStatus(Disabled);
        }
        if (status != Disabled) {
            initialize();
        }
    }

    public RestApiDataProvider(String ucscRestUrl, String name, String mirrorUrl, int loadPriority, String id) {
        super(ucscRestUrl, name, mirrorUrl, loadPriority, id);
        availableGenomesSet = Sets.newHashSet();
        try {
            URL ucscRestDsnUrl = new URL(ucscRestUrl);
        } catch (MalformedURLException | IllegalArgumentException ex) {
            log.error(ex.getMessage(), ex);
            setStatus(Disabled);
        }
        if (status != Disabled) {
            initialize();
        }
    }

    @Override
    public void initialize() {
        if (status == LoadUtils.ResourceStatus.Disabled) {
            return;
        }
        try {
            Optional<GenomesData> genomoeApiResponse = UCSCRestServerUtils.retrieveDsnResponse(url);
            genomoeApiResponse.ifPresent(ds -> {
                ds.getUcscGenomes().forEach((genomoeName, genome) -> {
                   availableGenomesSet.add(genomoeName);
                });
            });
        } catch (IOException ex) {
            log.error("Could not initialize this UCSC Rest Server, setting status to unavailable for this session.", ex);
            setStatus(NotResponding);
            return;
        }
        setStatus(Initialized);
    }

    @Override
    protected void disable() {
        availableGenomesSet.clear();
    }

    @Override
    public Set<String> getSupportedGenomeVersionNames() {
        return availableGenomesSet;
    }

    @Override
    public Set<DataSet> getAvailableDataSets(DataContainer dataContainer) {
        return null;
    }

    @Override
    public Map<String, Integer> getAssemblyInfo(GenomeVersion genomeVersion) {
        return null;
    }

    @Override
    public String getSequence(DataContainer dataContainer, SeqSpan span) {
        return "";
    }

    @Override
    public Optional<String> getFactoryName() {
        return Optional.of(UCSCRestDataProviderFactory.FACTORY_NAME);
    }
}
