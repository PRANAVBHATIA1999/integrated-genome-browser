package com.affymetrix.genometry.symloader;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.comparator.BioSeqComparator;
import com.affymetrix.genometry.parsers.useq.ArchiveInfo;
import com.affymetrix.genometry.parsers.useq.USeqArchive;
import com.affymetrix.genometry.parsers.useq.USeqGraphParser;
import com.affymetrix.genometry.parsers.useq.USeqRegionParser;
import com.affymetrix.genometry.parsers.useq.data.USeqData;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.LoadUtils.LoadStrategy;
import com.affymetrix.genometry.util.LocalUrlCacher;
import java.io.BufferedInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Nix
 */
public class USeq extends SymLoader {

    private ArchiveInfo archiveInfo = null;
    private ZipInputStream zis = null;
    private BufferedInputStream bis = null;
    private final Map<BioSeq, String> chromosomeList = new HashMap<>();

    //for region parsing
    private USeqArchive useqArchive = null;

    private static final List<LoadStrategy> strategyList = new ArrayList<>();

    static {
        strategyList.add(LoadStrategy.NO_LOAD);
        strategyList.add(LoadStrategy.VISIBLE);
        strategyList.add(LoadStrategy.AUTOLOAD);
        strategyList.add(LoadStrategy.GENOME);
    }

    public USeq(URI uri, Optional<URI> indexUri, String featureName, GenomeVersion genomeVersion) {
        super(uri, indexUri, featureName, genomeVersion);
    }

    public List<LoadStrategy> getLoadChoices() {
        return strategyList;
    }

    public void init() throws Exception {
        if (this.isInitialized) {
            return;
        }
        try {
            //for getRegion()
            useqArchive = new USeqArchive(LocalUrlCacher.convertURIToFile(uri));

            //for getGenome()
            bis = LocalUrlCacher.convertURIToBufferedUnzippedStream(uri);
            zis = new ZipInputStream(bis);
            zis.getNextEntry();
            archiveInfo = new ArchiveInfo(zis, false);

            //build list of BioSeqs (one for each chromosome in the USeqArchive)
            HashMap<String, Integer> chromBase = useqArchive.fetchChromosomesAndLastBase();
            for (String chrom : chromBase.keySet()) {
                //fetch the BioSeq from the AnnotationGroup if it exists
                chromosomeList.put(genomeVersion.addSeq(chrom, chromBase.get(chrom), uri.toString()), chrom);
            }
            Collections.sort(new ArrayList<>(chromosomeList.keySet()), new BioSeqComparator());

        } catch (Exception ex) {
            throw ex;
        } finally {
            GeneralUtils.safeClose(bis);
            GeneralUtils.safeClose(zis);
        }

        super.init();
    }

    public List<? extends SeqSymmetry> getGenome() throws Exception {
        init();
        try {
            //is it a graph dataset?
            if (archiveInfo.getDataType().equals(ArchiveInfo.DATA_TYPE_VALUE_GRAPH)) {
                USeqGraphParser gp = new USeqGraphParser();
                return gp.parseGraphSyms(zis, GenometryModel.getInstance(), uri.toString(), archiveInfo);
                //must be a region dataset
            } else {
                USeqRegionParser rp = new USeqRegionParser();
                return rp.parse(zis, genomeVersion, uri.toString(), false, archiveInfo);
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            GeneralUtils.safeClose(bis);
            GeneralUtils.safeClose(zis);
        }
    }

    public List<? extends SeqSymmetry> getRegion(SeqSpan span) throws Exception {
        try {
            init();
            //fetch region, this may be stranded
            if (!chromosomeList.containsKey(span.getBioSeq())) {
                return null;
            }

            int start = span.getStart();
            int stop = span.getEnd();
            String chrom = chromosomeList.get(span.getBioSeq());
            USeqData[] useqData = useqArchive.fetch(chrom, start, stop);
            //any data?
            if (useqData == null) {
                return null;
            }
            //is it a graph dataset?
            if (useqArchive.getArchiveInfo().getDataType().equals(ArchiveInfo.DATA_TYPE_VALUE_GRAPH)) {
                USeqGraphParser gp = new USeqGraphParser();
                return gp.parseGraphSyms(useqArchive, useqData, GenometryModel.getInstance(), uri.toString());
                //must be a region dataset
            } else {
                USeqRegionParser rp = new USeqRegionParser();
                return rp.parse(useqArchive, useqData, genomeVersion, uri.toString());
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            GeneralUtils.safeClose(bis);
            GeneralUtils.safeClose(zis);
        }
    }

    public List<BioSeq> getChromosomeList() throws Exception {
        init();
        return new ArrayList<>(chromosomeList.keySet());
    }
}
