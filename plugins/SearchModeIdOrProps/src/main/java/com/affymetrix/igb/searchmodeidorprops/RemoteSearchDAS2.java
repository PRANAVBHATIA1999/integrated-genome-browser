package com.affymetrix.igb.searchmodeidorprops;

import java.util.ArrayList;
import java.util.List;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.das2.Das2VersionedSource;
import com.affymetrix.genometryImpl.general.GenericVersion;
import com.affymetrix.genometryImpl.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometryImpl.util.ServerTypeI;

public class RemoteSearchDAS2 {

    public List<SeqSymmetry> searchFeatures(AnnotatedSeqGroup group, String name, BioSeq chrFilter) {
        List<SeqSymmetry> features = new ArrayList<>();

        if (name == null || name.isEmpty()) {
            return features;
        }

        group.getEnabledVersions().stream().filter(gVersion -> gVersion.gServer.serverType == ServerTypeI.DAS2).forEach(gVersion -> {
            Das2VersionedSource version = (Das2VersionedSource) gVersion.versionSourceObj;
            if (version != null) {
                List<SeqSymmetry> newFeatures = version.getFeaturesByName(name, group, chrFilter);
                if (newFeatures != null) {
                    features.addAll(newFeatures);
                }
            }
        });

        return features;
    }
}
