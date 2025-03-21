package org.lorainelab.igb.das;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.symloader.SymLoader;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.github.kevinsawicki.http.HttpRequest;
import org.lorainelab.igb.das.parser.DASFeatureParser;
import org.lorainelab.igb.das.parser.DASSymmetry;
import org.lorainelab.igb.das.utils.DasServerUtils;
import static org.lorainelab.igb.das.utils.DasServerUtils.toExternalForm;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.lorainelab.igb.das.utils.DasServerUtils.checkValidAndSetUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class DasSymloader extends SymLoader {

    private static final Logger logger = LoggerFactory.getLogger(DasSymloader.class);
    private static final String DAS_EXT = "DAS";
    private final GenometryModel gmodel;
    private Set<String> chromosomes;
    private final String contextRoot;

    public DasSymloader(URI contextRoot, Optional<URI> indexUri, String typeName, GenomeVersion genomeVersion) {
        super(contextRoot, indexUri, typeName, genomeVersion);
        this.extension = DAS_EXT;
        this.contextRoot = contextRoot.toString().substring(0, contextRoot.toString().indexOf("/" + typeName));
        gmodel = GenometryModel.getInstance();
    }

    @Override
    public List<? extends SeqSymmetry> getRegion(SeqSpan overlapSpan) throws Exception {
        List<DASSymmetry> results;
        DASFeatureParser parser = new DASFeatureParser();
        parser.setAnnotateSeq(false);
        final int min = overlapSpan.getMin() == 0 ? 1 : overlapSpan.getMin();
        final int max = overlapSpan.getMax() - 1;
        String segmentParam = getChromosomeSynonym(overlapSpan) + ":" + min + "," + max + ";";
        //e.g. http://genome.cse.ucsc.edu/cgi-bin/das/hg38/features?type=altLocations;segment=chr1%3A1%2C14422303;
        final String request = toExternalForm(contextRoot) + "features";
        String url = checkValidAndSetUrl(request.trim());
        HttpRequest remoteHttpRequest = HttpRequest.get(url, true, "type", featureName, "segment", segmentParam)
                .acceptGzipEncoding()
                .uncompress(true)
                .trustAllCerts()
                .trustAllHosts()
                .followRedirects(true);
        logger.info(remoteHttpRequest.toString());
        try (InputStream inputStream = remoteHttpRequest.buffer()) {
            return parser.parse(inputStream, genomeVersion, toExternalForm(contextRoot));
        }
    }

    @Override
    public List<? extends SeqSymmetry> getChromosome(BioSeq seq) throws Exception {
        return super.getChromosome(seq); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<? extends SeqSymmetry> getGenome() throws Exception {
        return super.getGenome(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<BioSeq> getChromosomeList() throws Exception {
        return genomeVersion.getSeqList();
    }

    @Override
    protected void init() throws Exception {
        super.init(); //To change body of generated methods, choose Tools | Templates.
    }

    private String getChromosomeSynonym(SeqSpan span) {
        BioSeq currentSeq = span.getBioSeq();
        if (chromosomes == null) {
            chromosomes = DasServerUtils.retrieveAssemblyInfoByContextRoot(contextRoot).keySet();
        }
        return genomeVersion.getGenomeVersionSynonymLookup().findMatchingSynonym(chromosomes, currentSeq.getId());
    }

}
