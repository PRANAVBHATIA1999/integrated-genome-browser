/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.genotyping;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.comparator.BioSeqComparator;
import com.affymetrix.genometry.symloader.SymLoader;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.GeneralUtils;
import com.affymetrix.genometry.util.LoadUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.function.Predicate;

import org.slf4j.LoggerFactory;

/**
 *
 * @author aloraine
 */
public class TwentyThreeAndMeSymLoader extends SymLoader{

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TwentyThreeAndMeSymLoader.class);

    private static final List<LoadUtils.LoadStrategy> SUPPORTED_LOAD_STRATEGIES = Lists.newArrayList(
            LoadUtils.LoadStrategy.NO_LOAD,
            LoadUtils.LoadStrategy.VISIBLE,
            LoadUtils.LoadStrategy.GENOME);

    private final String trackName;
    private final Map<String, BioSeq> chromosomeReference;
    private final List<BioSeq> chromosomes;

    private static final int BED_DETAIL_FIELD_COUNT = 4;
    private static final String GENOTYPE_MIME_TYPE = "text/23andme";
    private static final String GENOTYPE_FILE_EXTENSION = ".23andme";
    private static final Predicate<String> IS_NOT_COMMENT_LINE = line -> !line.startsWith("#");
    private static final Predicate<String> IS_PARSEABLE_LINE = IS_NOT_COMMENT_LINE;
    private final GenometryModel gmodel;

    TwentyThreeAndMeSymLoader(URI uri, Optional<URI> indexUri, String featureName, GenomeVersion genomeVersion) {
        super(uri, indexUri, featureName, genomeVersion);
        trackName = uri.toString();
        chromosomes = Lists.newArrayList();
        chromosomeReference = Maps.newHashMap();
        gmodel = GenometryModel.getInstance();
        initializeChromosomes();
    }

    @Override
    public List<BioSeq> getChromosomeList() throws Exception {
        return chromosomes;
    }

    @Override
    public List<LoadUtils.LoadStrategy> getLoadChoices() {
        return SUPPORTED_LOAD_STRATEGIES;
    }

    /**
     * Format:
     * # rsid  chromosome      position        genotype
     *   rs4477212       1       82154   AA
     */
    private void initializeChromosomes() {
        try (final InputStream openStream = uri.toURL().openStream();
             InputStream unzipedStream = GeneralUtils.unzipStream(openStream, uri.toString(), new StringBuffer());
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(unzipedStream))) {
            bufferedReader.lines()
                    .filter(IS_PARSEABLE_LINE)
                    .forEach(line -> {
                        Iterable<String> splitLine = Splitter.on("\t").trimResults().split(line);
                        final Iterator<String> iterator = splitLine.iterator();
                        if (iterator.hasNext()) {
                            String rsid = iterator.next();
                            String chromosome = iterator.next();
                            BioSeq seq = genomeVersion.getSeq(chromosome);
                            if (seq != null) {
                                if (!chromosomes.contains(seq)) {
                                    chromosomes.add(seq);
                                    chromosomeReference.put(chromosome, seq);
                                }
                            }
                        }

                    });
            Collections.sort(chromosomes, new BioSeqComparator());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    public List<SeqSymmetry> getGenome() throws Exception {
        List<SeqSymmetry> dataModel = new ArrayList<>();
        for (BioSeq chromosome : chromosomes) {
            dataModel.addAll(getChromosome(chromosome));
        }
        return dataModel;
    }

    @Override
    public List<SeqSymmetry> getChromosome(BioSeq seq) throws Exception {
        return parse(seq, seq.getMin(), seq.getMax());
    }

    @Override
    public List<SeqSymmetry> getRegion(SeqSpan span) throws Exception {
        return parse(span.getBioSeq(), span.getMin(), span.getMax());
    }



    public List<SeqSymmetry> parse(BioSeq seq, int requestMin, int requestMax) {
        List<SeqSymmetry> dataModelContent = Lists.newArrayList();
        try (final InputStream openStream = uri.toURL().openStream();
             InputStream unzipedStream = GeneralUtils.unzipStream(openStream, uri.toString(), new StringBuffer());
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(unzipedStream))) {
            Iterator<String> iterator = bufferedReader.lines().iterator();
            while (iterator.hasNext()) {
                String line = iterator.next().trim();
                if (IS_PARSEABLE_LINE.test(line)) {
                    List<String> fields = Splitter.on("\t").trimResults().splitToList(line);
                    String chrom = fields.get(1);
                    if (chromosomeReference.get(chrom) == seq) {
                        if (!parseLineToDataModel(fields, requestMin, requestMax, dataModelContent)) {
                            break;
                        }
                    }
                }
            }
            Collections.sort(chromosomes, new BioSeqComparator());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return dataModelContent;
    }

    public boolean parseLineToDataModel(List<String> fields, int requestMin, int requestMax, List<SeqSymmetry> dataModelContent) {
        if (fields.size() < 4) {
            LOGGER.error("line in 23andme file could not be visualized, missing required columns: {}", fields);
            return false;
        }
        String name = fields.get(0); // snp id
        String chrom = fields.get(1); // chromsome name
        int location = Integer.parseInt(fields.get(2)); // location of the polymorphism; check if it's one-based or not!
        // location of the polymorphism; check if it's one-based or not!
        String seq_name = fields.get(2);
        String genotype = fields.get(3);
        String strand = "+";
        String itemRgb = "";
        int chromStart = location - 2;
        int chromEnd = location;
        String id = name;
        int min = Math.min(chromStart, chromEnd);
        int max = Math.max(chromStart, chromEnd);
        int[] blockMins = new int[1];
        blockMins[0]  = min;
        int[] blockMaxs = new int[1];
        blockMaxs[0] = max;
        BioSeq seq = genomeVersion.getSeq(seq_name);

        TwentyThreeAndMeVariationSym twentyThreeAndMeVariationSym= new TwentyThreeAndMeVariationSym(uri.toString().toLowerCase(), chromosomeReference.get(chrom), min, max, name, true, blockMins, blockMaxs);

        twentyThreeAndMeVariationSym.setProperty("name",name);
        twentyThreeAndMeVariationSym.setProperty("chrom", chrom);
        twentyThreeAndMeVariationSym.setProperty("seq_name", seq_name);

        twentyThreeAndMeVariationSym.setProperty("chromStart", chromStart);
        twentyThreeAndMeVariationSym.setProperty("chromEnd", chromEnd);
        twentyThreeAndMeVariationSym.setProperty("genotype", genotype);

        dataModelContent.add(twentyThreeAndMeVariationSym);
        return true;
    }

    private static boolean inRange(int location, int requestMin, int requestMax) {
        return (location > requestMin && location < requestMax);
    }

    @Override
    public boolean isMultiThreadOK() {
        return true;
    }
}
