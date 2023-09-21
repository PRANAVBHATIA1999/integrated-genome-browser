package com.gene.bigbedhandler;

import org.osgi.service.component.annotations.Component;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.parsers.FileTypeHandler;
import com.affymetrix.genometry.parsers.IndexWriter;
import com.affymetrix.genometry.parsers.Parser;
import com.affymetrix.genometry.symloader.SymLoader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component(name = BigBedHandler.COMPONENT_NAME, immediate = true, service = FileTypeHandler.class)
public class BigBedHandler implements FileTypeHandler {

    public static final String COMPONENT_NAME = "BigBedHandler";
    private static final String[] EXTENSIONS = new String[]{"bb", "bigBed", "bigbed"};

    public BigBedHandler() {
        super();
    }

    @Override
    public String getName() {
        return "BED";
    }

    @Override
    public String[] getExtensions() {
        return EXTENSIONS;
    }

    public static List<String> getFormatPrefList() {
        return Arrays.asList(EXTENSIONS);
    }

    @Override
    public SymLoader createSymLoader(URI uri, Optional<URI> indexUri, String featureName,
            GenomeVersion genomeVersion) {
        return new BigBedSymLoader(uri, indexUri, featureName, genomeVersion);
    }

    @Override
    public Parser getParser() {
        return null;
    }

    @Override
    public IndexWriter getIndexWriter(String stream_name) {
        return null;
    }

    @Override
    public FileTypeCategory getFileTypeCategory() {
        return FileTypeCategory.Annotation;
    }
}
