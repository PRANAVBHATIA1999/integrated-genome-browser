package com.affymetrix.genometry.parsers;

import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.symloader.SymLoader;
import java.net.URI;
import java.util.Optional;

public interface FileTypeHandler {

    /**
     * get the name of the Parser, displayed in the FileChooser popup
     *
     * @return the name of the parser
     */
    public String getName();

    /**
     * get the file extensions (there may be more than one) for this
     * file type
     *
     * @return the array of possible file extensions
     */
    public String[] getExtensions();

    /**
     * get an appropriate SymLoader for this file type
     *
     * @param uri the URI for the symloader
     * @param indexUri
     * @param featureName the feature name for the symloader
     * @param genomeVersion the GenomeVersion for the symloader
     * @return the SymLoader to use
     */
    public SymLoader createSymLoader(URI uri, Optional<URI> indexUri, String featureName, GenomeVersion genomeVersion);

    /**
     * get a Parser for the file type
     *
     * @return the Parser
     */
    public Parser getParser();

    /**
     * get an IndexWriter for the file type
     *
     * @return the IndexWriter
     */
    public IndexWriter getIndexWriter(String stream_name);

    /**
     * @return the category of this file type
     */
    public FileTypeCategory getFileTypeCategory();
}
