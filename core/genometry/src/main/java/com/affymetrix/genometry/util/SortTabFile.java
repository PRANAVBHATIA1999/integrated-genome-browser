package com.affymetrix.genometry.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;

/**
 * This class sorts tab delimited files such as bed, psl, wiggle etc.
 *
 * @author hiralv
 */
public class SortTabFile {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SortTabFile.class);
    private static final Pattern line_regex = Pattern.compile("\\s+");
    private static final Pattern tab_regex = Pattern.compile("\\t");
    private static final String BED_FILE_EXT = ".bed";
    private static final String PSL_FILE_EXT = ".psl";
    private static final String LINK_PSL_EXT = ".link.psl";

    public static boolean sort(File file) {

        BufferedReader br = null;
        String line = null;
        List<String> list = new ArrayList<>(1000);
        List<String> templist = new ArrayList<>(1000);
        String unzippedStreamName = GeneralUtils.stripEndings(file.getName());
        String extension = GeneralUtils.getExtension(unzippedStreamName);
        Comparator<String> comparator = new LineComparator(extension);

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            while ((line = br.readLine()) != null) {
                if (line.startsWith("track")) {
                    Collections.sort(templist, comparator);
                    list.addAll(templist);
                    templist.clear();
                }
                templist.add(line);
            }
            Collections.sort(templist, comparator);
            list.addAll(templist);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SortTabFile.class.getName()).log(Level.SEVERE, "Could not find file " + file, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(SortTabFile.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            GeneralUtils.safeClose(br);
        }

        return writeFile(file, list);
    }

    private static boolean writeFile(File file, List<String> lines) {
        BufferedWriter bw = null;
        try {

            if (!file.canWrite()) {
                Logger.getLogger(SortTabFile.class.getName()).log(Level.SEVERE, "Cannot write to file {0}", file);
                return false;
            }

            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

            for (String line : lines) {
                bw.write(line + "\n");
            }

            bw.flush();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SortTabFile.class.getName()).log(Level.SEVERE, "Could not find file " + file, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(SortTabFile.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            GeneralUtils.safeClose(bw);
        }
        return true;
    }

    private static final class LineComparator implements Comparator<String> {

        private final int column;
        private final int or_column;
        private final String ext;
        private final Pattern regex;

        public LineComparator(String ext) {
            int[] columns = determineColumns(ext);
            this.column = columns[0] - 1;
            this.or_column = columns[1] - 1;
            this.ext = ext;
            this.regex = determineRegex(ext);
        }

        public int compare(String o1, String o2) {

            if (o1.startsWith("track") || o2.startsWith("track")) {
                return 0;
            }

            int[] mins = minimum(o1, o2);

            return Integer.valueOf(mins[0]).compareTo(mins[1]);
        }

        private int[] minimum(String o1, String o2) {
            int[] mins = new int[2];
            int col = column;
            int or_col = or_column;

            String[] o1Fields = regex.split(o1);
            if (o1Fields.length == 1) {
                o1Fields = line_regex.split(o1);
            }

            String[] o2Fields = regex.split(o2);
            if (o2Fields.length == 1) {
                o2Fields = line_regex.split(o2);
            }

            if (ext.endsWith(BED_FILE_EXT)) {
                if (isIncludesBinField(o1Fields)) {
                    col += 1;
                    or_col += 1;
                }
            }

            mins[0] = Integer.valueOf(o1Fields[col]);
            mins[1] = Integer.valueOf(o2Fields[col]);

            if (or_col > 0) {
                mins[0] = Math.min(mins[0], Integer.valueOf(o1Fields[or_col]));
                mins[1] = Math.min(mins[1], Integer.valueOf(o2Fields[or_col]));
            }

            return mins;
        }

        private static boolean isIncludesBinField(String[] o1Fields) {
            return o1Fields.length > 6
                    && (o1Fields[6].equals("+") || o1Fields[6].equals("-")
                    || o1Fields[6].equals("."));
        }

        private static Pattern determineRegex(String ext) {
            if (ext.equals(PSL_FILE_EXT) || ext.endsWith(LINK_PSL_EXT)) {
//				return Pattern.compile("\t");
                return tab_regex;
            } else if (ext.equals(BED_FILE_EXT)) {
//				return Pattern.compile("\\s+");
                return tab_regex;
            }

            return null;
        }

        private static int[] determineColumns(String ext) {

            if (ext.equals(PSL_FILE_EXT) || ext.endsWith(LINK_PSL_EXT)) {
                return new int[]{16, -1};
            } else if (ext.equals(BED_FILE_EXT)) {
                return new int[]{2, 3};
            }

            return new int[]{-1, -1};
        }

    }

}
