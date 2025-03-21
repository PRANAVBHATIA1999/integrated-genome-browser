package com.affymetrix.genometry.util;

import com.affymetrix.common.PreferenceUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;

/**
 * Used to cache info on current directory.
 */
public final class FileTracker {

    public static final String CONTROL_GRAPH_DIRECTORY = "control graph directory";
    public static final String DATA_DIRECTORY = "data directory";
    public static final String OUTPUT_DIRECTORY = "output directory";
    public static final String GENOME_DIRECTORY = "genome directory";
    public static final String EXPORT_DIRECTORY = "export directory";

    private static List<String> FILENAMES;

    static {
        List<String> filenames = new ArrayList<>();
        filenames.add(CONTROL_GRAPH_DIRECTORY);
        filenames.add(DATA_DIRECTORY);
        filenames.add(OUTPUT_DIRECTORY);
        filenames.add(GENOME_DIRECTORY);
        filenames.add(EXPORT_DIRECTORY);
        FILENAMES = Collections.unmodifiableList(filenames);
    }

    private final String name;

    /**
     * The singleton FileTracker used to remember the user's most recent data
     * directory.
     */
    public final static FileTracker DATA_DIR_TRACKER
            = new FileTracker(DATA_DIRECTORY);

    /**
     * The singleton FileTracker used to remember the user's most recent output
     * directory.
     */
    public final static FileTracker OUTPUT_DIR_TRACKER
            = new FileTracker(OUTPUT_DIRECTORY);

    /**
     * The singleton FileTracker used to remember the user's most recent genome
     * directory.
     */
    public final static FileTracker GENOME_DIR_TRACKER
            = new FileTracker(GENOME_DIRECTORY);

    /**
     * The singleton FileTracker used to remember the user's most recent output
     * directory.
     */
    public final static FileTracker EXPORT_DIR_TRACKER
            = new FileTracker(EXPORT_DIRECTORY);

    private FileTracker(String name) {
        this.name = name;
    }

    public void setFile(File f) {
        if (!FILENAMES.contains(name)) {
            throw new IllegalArgumentException("'" + name + "' is not a known name for a file preference");
        }
        if (f == null || !f.exists()) {
            return;
        }
        try {
            String path = f.getCanonicalPath();
            PreferenceUtils.getTopNode().put(name, path);
        } catch (IOException ioe) {
            ErrorHandler.errorPanel("Can't resolve file path", ioe, Level.SEVERE);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        try {
            PreferenceUtils.getTopNode().flush();
        } catch (BackingStoreException bse) {
        }
    }

    public File getFile() {
        if (!FILENAMES.contains(name)) {
            throw new IllegalArgumentException("'" + name + "' is not a known name for a file preference");
        }
        try {
            PreferenceUtils.getTopNode().sync();
        } catch (BackingStoreException bse) {
        }
        String path = PreferenceUtils.getTopNode().get(name, System.getProperty("user.home"));
        File f = new File(path);
        if (!f.exists()) {
            f = new File(System.getProperty("user.dir"));
        }
        return f;
    }

}
