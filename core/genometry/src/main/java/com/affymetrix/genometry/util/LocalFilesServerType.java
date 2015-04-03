package com.affymetrix.genometry.util;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.general.GenericFeature;
import com.affymetrix.genometry.general.GenericServer;
import com.affymetrix.genometry.general.GenericVersion;
import com.affymetrix.genometry.quickload.QuickLoadSymLoader;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalFilesServerType implements ServerTypeI {

    private static final String name = "Local Files";
    private static final LocalFilesServerType instance = new LocalFilesServerType();

    public static LocalFilesServerType getInstance() {
        return instance;
    }

    private LocalFilesServerType() {
        super();
    }

    @Override
    public String getServerName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void discoverFeatures(GenericVersion gVersion, boolean autoload) {
    }

    @Override
    public void discoverChromosomes(Object versionSourceObj) {
    }

    @Override
    public boolean getSpeciesAndVersions(GenericServer gServer, VersionDiscoverer versionDiscoverer) {
        return false;
    }

    @Override
    public Map<String, List<? extends SeqSymmetry>> loadFeatures(SeqSpan span, GenericFeature feature)
            throws Exception {
        if (((QuickLoadSymLoader) feature.getSymL()).getSymLoader() != null
                && (((QuickLoadSymLoader) feature.getSymL()).getSymLoader().isResidueLoader())) {
            return Collections.<String, List<? extends SeqSymmetry>>emptyMap();
        }
        return (((QuickLoadSymLoader) feature.getSymL()).loadFeatures(span, feature));
    }

    @Override
    public boolean loadResidues(GenericVersion version, String genomeVersionName,
            BioSeq aseq, int min, int max, SeqSpan span) {
        for (GenericFeature feature : version.getFeatures()) {
            if (feature.getSymL() == null || !feature.getSymL().isResidueLoader()) {
                continue;
            }
            try {
                String residues = feature.getSymL().getRegionResidues(span);
                if (residues != null) {
                    BioSeqUtils.addResiduesToComposition(aseq, residues, span);
                    return true;
                }
            } catch (Exception ex) {
                Logger.getLogger(LocalFilesServerType.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    @Override
    public boolean supportsUserAddedInstances() {
        return false;
    }
}
