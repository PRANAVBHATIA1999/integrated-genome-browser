package com.affymetrix.genometry.parsers.useq.apps;

import com.affymetrix.genometry.parsers.useq.ArchiveInfo;
import com.affymetrix.genometry.parsers.useq.SliceInfo;
import com.affymetrix.genometry.parsers.useq.USeqArchive;
import com.affymetrix.genometry.parsers.useq.USeqUtilities;
import com.affymetrix.genometry.parsers.useq.data.Position;
import com.affymetrix.genometry.parsers.useq.data.PositionData;
import com.affymetrix.genometry.parsers.useq.data.PositionScore;
import com.affymetrix.genometry.parsers.useq.data.PositionScoreData;
import com.affymetrix.genometry.parsers.useq.data.PositionScoreText;
import com.affymetrix.genometry.parsers.useq.data.PositionScoreTextData;
import com.affymetrix.genometry.parsers.useq.data.PositionText;
import com.affymetrix.genometry.parsers.useq.data.PositionTextData;
import com.affymetrix.genometry.parsers.useq.data.Region;
import com.affymetrix.genometry.parsers.useq.data.RegionData;
import com.affymetrix.genometry.parsers.useq.data.RegionScore;
import com.affymetrix.genometry.parsers.useq.data.RegionScoreData;
import com.affymetrix.genometry.parsers.useq.data.RegionScoreText;
import com.affymetrix.genometry.parsers.useq.data.RegionScoreTextData;
import com.affymetrix.genometry.parsers.useq.data.RegionText;
import com.affymetrix.genometry.parsers.useq.data.RegionTextData;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts text files to binary useq.
 */
public class Text2USeq {
    //fields
    //user defined

    private int chromosomeColumnIndex = -1;
    private int strandColumnIndex = -1;
    private int beginningColumnIndex = -1;
    private int endingColumnIndex = -1;
    private int textColumnIndexs[] = null;
    private int scoreColumnIndex = -1;
    private int rowChunkSize = 10000;
    private File[] inputFiles;
    private String versionedGenome = null;
    private boolean makeGraph = true;
    private int graphStyle = 0;
    private String color = null;
    private String description = null;

    //internal fields
    public static String[] GRAPH_STYLES = {ArchiveInfo.GRAPH_STYLE_VALUE_BAR, ArchiveInfo.GRAPH_STYLE_VALUE_STAIRSTEP, ArchiveInfo.GRAPH_STYLE_VALUE_HEATMAP, ArchiveInfo.GRAPH_STYLE_VALUE_LINE};
    private File tempSplitTextDirectory = null;
    private int maxIndex;
    private File[] outputDirectories;
    private File workingBinarySaveDirectory;
    private HashMap<String, File> chromStrandFileHash;
    private ArrayList<File> files2Zip = new ArrayList<>();
    public static final Pattern PATTERN_TAB = Pattern.compile("\\t");
    public static final Pattern PATTERN_STRAND = Pattern.compile(".*[+-\\.]$");

    //constructors
    //for use with main, contains System.exit calls!
    public Text2USeq(String[] args) {
        long startTime = System.currentTimeMillis();
        processArgs(args);

        //for each file
        for (int i = 0; i < inputFiles.length; i++) {
            System.out.println("Processing " + inputFiles[i]);

            //split text file by chromStrand and write to tempDirectory
            System.out.println("\tSplitting by chromosome and possibly strand...");

            tempSplitTextDirectory = new File(inputFiles[i].getParentFile(), "TempDir" + USeqArchive.createRandowWord(7));
            tempSplitTextDirectory.mkdir();

            chromStrandFileHash = splitFileByChromosomeAndStrand(inputFiles[i], tempSplitTextDirectory, chromosomeColumnIndex, strandColumnIndex, true);
            if (chromStrandFileHash == null || chromStrandFileHash.isEmpty()) {
                System.err.println("\nFailed to parse genomic data text file, aborting!\n");
                continue;
            }

            //check strand
            if (strandBad()) {
                USeqUtilities.deleteDirectory(tempSplitTextDirectory);
                System.err.println("\nError: convert your strand information to +, -, or .  Skipping useq conversion.");
                continue;
            }

            //Make directory to hold split binary files
            outputDirectories[i] = USeqUtilities.makeDirectory(inputFiles[i], ".TempDelMe");
            workingBinarySaveDirectory = outputDirectories[i];

            //clear files to zip
            files2Zip.clear();

            //write readme.txt
            writeReadMeTxt(inputFiles[i]);

            //split slice and write data to binary file
            System.out.println("\tParsing, slicing, and writing binary data...");
            if (!sliceWriteSplitData()) {
                USeqUtilities.deleteDirectory(tempSplitTextDirectory);
                USeqUtilities.deleteDirectory(workingBinarySaveDirectory);
                USeqUtilities.printErrAndExit("\nFailed to convert split data to binary, aborting!\n");
            }

            //zip compress directory
            System.out.println("\tZipping...");
            String zipName = USeqUtilities.removeExtension(workingBinarySaveDirectory.getName()) + USeqUtilities.USEQ_EXTENSION_WITH_PERIOD;
            File zipFile = new File(inputFiles[i].getParentFile(), zipName);
            File[] files = new File[files2Zip.size()];
            files2Zip.toArray(files);
            USeqUtilities.zip(files, zipFile);
            USeqUtilities.deleteDirectory(workingBinarySaveDirectory);
            USeqUtilities.deleteDirectory(tempSplitTextDirectory);

        }
        //finish and calc run time
        double diffTime = ((double) (System.currentTimeMillis() - startTime)) / 1000;
        System.out.println("\nDone! " + Math.round(diffTime) + " seconds\n");
    }

    //methods
    /**
     * Checks to see if the last character in the first chromStrand file is +, -, or .
     */
    private boolean strandBad() {
        if (strandColumnIndex == -1) {
            return false;
        }
        String name = chromStrandFileHash.keySet().iterator().next();
        if (PATTERN_STRAND.matcher(name).matches()) {
            return false;
        }
        return true;
    }

    private void writeReadMeTxt(File sourceFile) {
        try {
            ArchiveInfo ai = new ArchiveInfo(versionedGenome, null);
            //set data type, graph or region
            if (makeGraph) {
                ai.setDataType(ArchiveInfo.DATA_TYPE_VALUE_GRAPH);
                ai.setInitialGraphStyle(GRAPH_STYLES[graphStyle]);
            } else {
                ai.setDataType(ArchiveInfo.DATA_TYPE_VALUE_REGION);
            }
            //set text file source
            ai.setOriginatingDataSource(sourceFile.toString());
            //set color
            if (color != null) {
                ai.setInitialColor(color);
            }
            //set description?
            if (description != null) {
                ai.setDescription(description);
            }
            //write
            File readme = ai.writeReadMeFile(workingBinarySaveDirectory);
            files2Zip.add(readme);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calls the appropriate slice writer
     */
    private boolean sliceWriteSplitData() {
        try {
            //Region or Position data
            if (endingColumnIndex == -1) {
                //Position!
                if (scoreColumnIndex == -1) {
                    if (textColumnIndexs == null) {
                        sliceWritePositionData();
                    } else {
                        sliceWritePositionTextData();
                    }
                } else {
                    if (textColumnIndexs == null) {
                        sliceWritePositionScoreData();
                    } else {
                        sliceWritePositionScoreTextData();
                    }
                }
            } else {
                //Region
                if (scoreColumnIndex == -1) {
                    if (textColumnIndexs == null) {
                        sliceWriteRegionData();
                    } else {
                        sliceWriteRegionTextData();
                    }
                } else {
                    if (textColumnIndexs == null) {
                        sliceWriteRegionScoreData();
                    } else {
                        sliceWriteRegionScoreTextData();
                    }
                }

            }
        } catch (Exception e) {
            System.err.println("Error slicing and writing data!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Split chroms by the rowChunkSize and writes each to file using an appropriate binary file type.
     */
    private void sliceWriteRegionData() throws Exception {
        for (String chromStrand : chromStrandFileHash.keySet()) {
            String chromosome = chromStrand.substring(0, chromStrand.length() - 1);
            String strand = chromStrand.substring(chromStrand.length() - 1);
            SliceInfo sliceInfo = new SliceInfo(chromosome, strand, 0, 0, 0, null);
            int beginningIndex = 0;
            int endIndex = 0;
            Region[] reg = makeRegions(chromStrandFileHash.get(chromStrand));
            if (!Region.checkStartStops(reg)) {
                throw new Exception("\nError: one or more of your stop coordinates is less than your start coordinate.  Start must always be less than or equal to Stop.\n");
            }
            int numberReg = reg.length;
            while (true) {
                //find beginningIndex and endIndex(excluded) indexes
                Region[] slice;
                //don't slice?
                if (rowChunkSize == -1) {
                    beginningIndex = 0;
                    endIndex = numberReg;
                    slice = reg;
                } //slice!
                else {
                    beginningIndex = endIndex;
                    endIndex = beginningIndex + rowChunkSize;
                    if (endIndex > numberReg) {
                        endIndex = numberReg;
                    } else {
                        //advance until start changes
                        int endBP = reg[endIndex - 1].getStart();
                        for (int i = endIndex; i < numberReg; i++) {
                            if (reg[i].getStart() != endBP) {
                                break;
                            }
                            endIndex++;
                        }
                    }
                    int num = endIndex - beginningIndex;
                    slice = new Region[num];
                    System.arraycopy(reg, beginningIndex, slice, 0, num);
                }
                //update slice info
                RegionData.updateSliceInfo(slice, sliceInfo);
                RegionData rd = new RegionData(slice, sliceInfo);
                File savedFile = rd.write(workingBinarySaveDirectory, true);
                files2Zip.add(savedFile);
                //at the end of the data?
                if (endIndex == numberReg) {
                    break;
                }
            }
        }
    }

    /**
     * Split chroms by the rowChunkSize and writes each to file using an appropriate binary file type.
     */
    private void sliceWriteRegionScoreData() throws Exception {
        for (String chromStrand : chromStrandFileHash.keySet()) {
            String chromosome = chromStrand.substring(0, chromStrand.length() - 1);
            String strand = chromStrand.substring(chromStrand.length() - 1);
            SliceInfo sliceInfo = new SliceInfo(chromosome, strand, 0, 0, 0, null);
            int beginningIndex = 0;
            int endIndex = 0;
            RegionScore[] reg = makeRegionScores(chromStrandFileHash.get(chromStrand));
            if (!Region.checkStartStops(reg)) {
                throw new Exception("\nError: one or more of your stop coordinates is less than your start coordinate.  Start must always be less than or equal to Stop.\n");
            }
            int numberReg = reg.length;
            while (true) {
                //find beginningIndex and endIndex(excluded) indexes
                RegionScore[] slice;
                //don't slice?
                if (rowChunkSize == -1) {
                    beginningIndex = 0;
                    endIndex = numberReg;
                    slice = reg;
                } //slice!
                else {
                    beginningIndex = endIndex;
                    endIndex = beginningIndex + rowChunkSize;
                    if (endIndex > numberReg) {
                        endIndex = numberReg;
                    } else {
                        //advance until start changes
                        int endBP = reg[endIndex - 1].getStart();
                        for (int i = endIndex; i < numberReg; i++) {
                            if (reg[i].getStart() != endBP) {
                                break;
                            }
                            endIndex++;
                        }
                    }
                    int num = endIndex - beginningIndex;
                    slice = new RegionScore[num];
                    System.arraycopy(reg, beginningIndex, slice, 0, num);
                }
                //update slice info
                RegionScoreData.updateSliceInfo(slice, sliceInfo);
                RegionScoreData rd = new RegionScoreData(slice, sliceInfo);
                File savedFile = rd.write(workingBinarySaveDirectory, true);
                files2Zip.add(savedFile);
                //at the end of the data?
                if (endIndex == numberReg) {
                    break;
                }
            }
        }
    }

    /**
     * Split chroms by the rowChunkSize and writes each to file using an appropriate binary file type.
     */
    private void sliceWriteRegionScoreTextData() throws Exception {
        for (String chromStrand : chromStrandFileHash.keySet()) {
            String chromosome = chromStrand.substring(0, chromStrand.length() - 1);
            String strand = chromStrand.substring(chromStrand.length() - 1);
            SliceInfo sliceInfo = new SliceInfo(chromosome, strand, 0, 0, 0, null);
            int beginningIndex = 0;
            int endIndex = 0;
            RegionScoreText[] reg = makeRegionScoreTexts(chromStrandFileHash.get(chromStrand));
            if (!Region.checkStartStops(reg)) {
                throw new Exception("\nError: one or more of your stop coordinates is less than your start coordinate.  Start must always be less than or equal to Stop.\n");
            }
            int numberReg = reg.length;
            while (true) {
                //find beginningIndex and endIndex(excluded) indexes
                RegionScoreText[] slice;
                //don't slice?
                if (rowChunkSize == -1) {
                    beginningIndex = 0;
                    endIndex = numberReg;
                    slice = reg;
                } //slice!
                else {
                    beginningIndex = endIndex;
                    endIndex = beginningIndex + rowChunkSize;
                    if (endIndex > numberReg) {
                        endIndex = numberReg;
                    } else {
                        //advance until start changes
                        int endBP = reg[endIndex - 1].getStart();
                        for (int i = endIndex; i < numberReg; i++) {
                            if (reg[i].getStart() != endBP) {
                                break;
                            }
                            endIndex++;
                        }
                    }
                    int num = endIndex - beginningIndex;
                    slice = new RegionScoreText[num];
                    System.arraycopy(reg, beginningIndex, slice, 0, num);
                }
                //update slice info
                RegionScoreTextData.updateSliceInfo(slice, sliceInfo);
                RegionScoreTextData rd = new RegionScoreTextData(slice, sliceInfo);
                File savedFile = rd.write(workingBinarySaveDirectory, true);
                files2Zip.add(savedFile);
                //at the end of the data?
                if (endIndex == numberReg) {
                    break;
                }
            }
        }
    }

    /**
     * Split chroms by the rowChunkSize and writes each to file using an appropriate binary file type.
     */
    private void sliceWriteRegionTextData() throws Exception {
        for (String chromStrand : chromStrandFileHash.keySet()) {
            String chromosome = chromStrand.substring(0, chromStrand.length() - 1);
            String strand = chromStrand.substring(chromStrand.length() - 1);
            SliceInfo sliceInfo = new SliceInfo(chromosome, strand, 0, 0, 0, null);
            int beginningIndex = 0;
            int endIndex = 0;
            RegionText[] reg = makeRegionTexts(chromStrandFileHash.get(chromStrand));
            if (!Region.checkStartStops(reg)) {
                throw new Exception("\nError: one or more of your stop coordinates is less than your start coordinate.  Start must always be less than or equal to Stop.\n");
            }
            int numberReg = reg.length;
            while (true) {
                //find beginningIndex and endIndex(excluded) indexes
                RegionText[] slice;
                //don't slice?
                if (rowChunkSize == -1) {
                    beginningIndex = 0;
                    endIndex = numberReg;
                    slice = reg;
                } //slice!
                else {
                    beginningIndex = endIndex;
                    endIndex = beginningIndex + rowChunkSize;
                    if (endIndex > numberReg) {
                        endIndex = numberReg;
                    } else {
                        //advance until start changes
                        int endBP = reg[endIndex - 1].getStart();
                        for (int i = endIndex; i < numberReg; i++) {
                            if (reg[i].getStart() != endBP) {
                                break;
                            }
                            endIndex++;
                        }
                    }
                    int num = endIndex - beginningIndex;
                    slice = new RegionText[num];
                    System.arraycopy(reg, beginningIndex, slice, 0, num);
                }
                //update slice info
                RegionTextData.updateSliceInfo(slice, sliceInfo);
                RegionTextData rd = new RegionTextData(slice, sliceInfo);
                File savedFile = rd.write(workingBinarySaveDirectory, true);
                files2Zip.add(savedFile);
                //at the end of the data?
                if (endIndex == numberReg) {
                    break;
                }
            }
        }
    }

    /**
     * Split chroms by the rowChunkSize and writes each to file using an appropriate binary file type.
     */
    private void sliceWritePositionData() throws Exception {
        for (String chromStrand : chromStrandFileHash.keySet()) {
            String chromosome = chromStrand.substring(0, chromStrand.length() - 1);
            String strand = chromStrand.substring(chromStrand.length() - 1);
            SliceInfo sliceInfo = new SliceInfo(chromosome, strand, 0, 0, 0, null);
            int beginningIndex = 0;
            int endIndex = 0;

            Position[] positions = makePositions(chromStrandFileHash.get(chromStrand));
            int numberPositions = positions.length;
            while (true) {
                //find beginningIndex and endIndex(excluded) indexes
                Position[] slice;
                //don't slice?
                if (rowChunkSize == -1) {
                    beginningIndex = 0;
                    endIndex = numberPositions;
                    slice = positions;
                } //slice!
                else {
                    beginningIndex = endIndex;
                    endIndex = beginningIndex + rowChunkSize;
                    if (endIndex > numberPositions) {
                        endIndex = numberPositions;
                    } else {
                        //advance until position changes
                        int endBP = positions[endIndex - 1].getPosition();
                        for (int i = endIndex; i < numberPositions; i++) {
                            if (positions[i].getPosition() != endBP) {
                                break;
                            }
                            endIndex++;
                        }
                    }
                    int num = endIndex - beginningIndex;
                    slice = new Position[num];
                    System.arraycopy(positions, beginningIndex, slice, 0, num);
                }
                //update slice info
                PositionData.updateSliceInfo(slice, sliceInfo);
                PositionData pd = new PositionData(slice, sliceInfo);
                File savedFile = pd.write(workingBinarySaveDirectory, true);
                files2Zip.add(savedFile);
                //at the end of the data?
                if (endIndex == numberPositions) {
                    break;
                }
            }
        }
    }

    /**
     * Split chroms by the rowChunkSize and writes each to file using an appropriate binary file type.
     */
    private void sliceWritePositionTextData() throws Exception {
        for (String chromStrand : chromStrandFileHash.keySet()) {
            String chromosome = chromStrand.substring(0, chromStrand.length() - 1);
            String strand = chromStrand.substring(chromStrand.length() - 1);
            SliceInfo sliceInfo = new SliceInfo(chromosome, strand, 0, 0, 0, null);
            int beginningIndex = 0;
            int endIndex = 0;
            PositionText[] positions = makePositionTexts(chromStrandFileHash.get(chromStrand));
            int numberPositions = positions.length;
            while (true) {
                //find beginningIndex and endIndex(excluded) indexes
                PositionText[] slice;
                //don't slice?
                if (rowChunkSize == -1) {
                    beginningIndex = 0;
                    endIndex = numberPositions;
                    slice = positions;
                } //slice!
                else {
                    beginningIndex = endIndex;
                    endIndex = beginningIndex + rowChunkSize;
                    if (endIndex > numberPositions) {
                        endIndex = numberPositions;
                    } else {
                        //advance until position changes
                        int endBP = positions[endIndex - 1].getPosition();
                        for (int i = endIndex; i < numberPositions; i++) {
                            if (positions[i].getPosition() != endBP) {
                                break;
                            }
                            endIndex++;
                        }
                    }
                    int num = endIndex - beginningIndex;
                    slice = new PositionText[num];
                    System.arraycopy(positions, beginningIndex, slice, 0, num);
                }
                //update slice info
                PositionTextData.updateSliceInfo(slice, sliceInfo);
                PositionTextData pd = new PositionTextData(slice, sliceInfo);
                File savedFile = pd.write(workingBinarySaveDirectory, true);
                files2Zip.add(savedFile);
                //at the end of the data?
                if (endIndex == numberPositions) {
                    break;
                }
            }
        }
    }

    /**
     * Split chroms by the rowChunkSize and writes each to file using an appropriate binary file type.
     */
    private void sliceWritePositionScoreTextData() throws Exception {
        for (String chromStrand : chromStrandFileHash.keySet()) {
            String chromosome = chromStrand.substring(0, chromStrand.length() - 1);
            String strand = chromStrand.substring(chromStrand.length() - 1);
            SliceInfo sliceInfo = new SliceInfo(chromosome, strand, 0, 0, 0, null);
            int beginningIndex = 0;
            int endIndex = 0;
            PositionScoreText[] positions = makePositionScoreTexts(chromStrandFileHash.get(chromStrand));
            int numberPositions = positions.length;
            while (true) {
                //find beginningIndex and endIndex(excluded) indexes
                PositionScoreText[] slice;
                //don't slice?
                if (rowChunkSize == -1) {
                    beginningIndex = 0;
                    endIndex = numberPositions;
                    slice = positions;
                } //slice!
                else {
                    beginningIndex = endIndex;
                    endIndex = beginningIndex + rowChunkSize;
                    if (endIndex > numberPositions) {
                        endIndex = numberPositions;
                    } else {
                        //advance until position changes
                        int endBP = positions[endIndex - 1].getPosition();
                        for (int i = endIndex; i < numberPositions; i++) {
                            if (positions[i].getPosition() != endBP) {
                                break;
                            }
                            endIndex++;
                        }
                    }
                    int num = endIndex - beginningIndex;
                    slice = new PositionScoreText[num];
                    System.arraycopy(positions, beginningIndex, slice, 0, num);
                }
                //update slice info
                PositionScoreTextData.updateSliceInfo(slice, sliceInfo);
                PositionScoreTextData pd = new PositionScoreTextData(slice, sliceInfo);
                File savedFile = pd.write(workingBinarySaveDirectory, true);
                files2Zip.add(savedFile);
                //at the end of the data?
                if (endIndex == numberPositions) {
                    break;
                }
            }
        }
    }

    /**
     * Split chroms by the rowChunkSize and writes each to file using an appropriate binary file type.
     */
    private void sliceWritePositionScoreData() throws Exception {
        for (String chromStrand : chromStrandFileHash.keySet()) {
            String chromosome = chromStrand.substring(0, chromStrand.length() - 1);
            String strand = chromStrand.substring(chromStrand.length() - 1);
            SliceInfo sliceInfo = new SliceInfo(chromosome, strand, 0, 0, 0, null);
            PositionScore[] positions = makePositionScores(chromStrandFileHash.get(chromStrand));
            PositionScoreData psd = new PositionScoreData(positions, sliceInfo);
            psd.sliceWritePositionScoreData(rowChunkSize, workingBinarySaveDirectory, files2Zip);
        }

    }

    /**
     * Parses a Position[]
     */
    private Position[] makePositions(File file) {
        ArrayList<Position> al = new ArrayList<>();
        String[] tokens = null;
        String line = null;
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                while ((line = in.readLine()) != null) {
                    tokens = PATTERN_TAB.split(line);
                    al.add(new Position(Integer.parseInt(tokens[beginningColumnIndex])));
                }
            }
            Position[] d = new Position[al.size()];
            al.toArray(d);
            Arrays.sort(d);
            return d;
        } catch (Exception e) {
            System.out.println("Could not parse an int value from '" + tokens[endingColumnIndex] + "', malformed line -> " + line);
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Parses a PositionScore[]
     */
    private PositionScore[] makePositionScores(File file) {
        ArrayList<PositionScore> al = new ArrayList<>();
        String[] tokens = null;
        String line = null;
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                while ((line = in.readLine()) != null) {
                    tokens = PATTERN_TAB.split(line);
                    al.add(new PositionScore(Integer.parseInt(tokens[beginningColumnIndex]), Float.parseFloat(tokens[scoreColumnIndex])));
                }
            }
            PositionScore[] d = new PositionScore[al.size()];
            al.toArray(d);
            Arrays.sort(d);
            return d;
        } catch (Exception e) {
            System.out.println("Could not parse an int or float value from malformed line -> " + line);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a PositionText[]
     */
    private PositionText[] makePositionTexts(File file) {
        ArrayList<PositionText> al = new ArrayList<>();
        String[] tokens = null;
        String line = null;
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                while ((line = in.readLine()) != null) {
                    tokens = PATTERN_TAB.split(line);
                    al.add(new PositionText(Integer.parseInt(tokens[beginningColumnIndex]), concatinateTextColumns(tokens)));
                }
            }
            PositionText[] d = new PositionText[al.size()];
            al.toArray(d);
            Arrays.sort(d);
            return d;
        } catch (Exception e) {
            System.out.println("Could not parse an int or float value from malformed line -> " + line);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a PositionScoreText[]
     */
    private PositionScoreText[] makePositionScoreTexts(File file) {
        ArrayList<PositionScoreText> al = new ArrayList<>();
        String[] tokens = null;
        String line = null;
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                while ((line = in.readLine()) != null) {
                    tokens = PATTERN_TAB.split(line);
                    al.add(new PositionScoreText(Integer.parseInt(tokens[beginningColumnIndex]), Float.parseFloat(tokens[scoreColumnIndex]), concatinateTextColumns(tokens)));
                }
            }
            PositionScoreText[] d = new PositionScoreText[al.size()];
            al.toArray(d);
            Arrays.sort(d);
            return d;
        } catch (Exception e) {
            System.out.println("Could not parse an int or float value from malformed line -> " + line);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a Region[]
     */
    private Region[] makeRegions(File file) {
        ArrayList<Region> al = new ArrayList<>();
        String[] tokens = null;
        String line = null;
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                while ((line = in.readLine()) != null) {
                    tokens = PATTERN_TAB.split(line);
                    al.add(new Region(Integer.parseInt(tokens[beginningColumnIndex]), Integer.parseInt(tokens[endingColumnIndex])));
                }
            }
            Region[] d = new Region[al.size()];
            al.toArray(d);
            Arrays.sort(d);
            return d;
        } catch (Exception e) {
            System.out.println("Could not parse an int value from '" + tokens[endingColumnIndex] + "', malformed line -> " + line);
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Parses a RegionScore[]
     */
    private RegionScore[] makeRegionScores(File file) {
        ArrayList<RegionScore> al = new ArrayList<>();
        String[] tokens = null;
        String line = null;
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                while ((line = in.readLine()) != null) {
                    tokens = PATTERN_TAB.split(line);
                    al.add(new RegionScore(Integer.parseInt(tokens[beginningColumnIndex]), Integer.parseInt(tokens[endingColumnIndex]), Float.parseFloat(tokens[scoreColumnIndex])));
                }
            }
            RegionScore[] d = new RegionScore[al.size()];
            al.toArray(d);
            Arrays.sort(d);
            return d;
        } catch (Exception e) {
            System.out.println("Could not parse an int or float value from malformed line -> " + line);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a RegionText[]
     */
    private RegionText[] makeRegionTexts(File file) {
        ArrayList<RegionText> al = new ArrayList<>();
        String[] tokens = null;
        String line = null;
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                while ((line = in.readLine()) != null) {
                    tokens = PATTERN_TAB.split(line);
                    al.add(new RegionText(Integer.parseInt(tokens[beginningColumnIndex]), Integer.parseInt(tokens[endingColumnIndex]), concatinateTextColumns(tokens)));
                }
            }
            RegionText[] d = new RegionText[al.size()];
            al.toArray(d);
            Arrays.sort(d);
            return d;
        } catch (Exception e) {
            System.out.println("Could not parse an int or float value from malformed line -> " + line);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a RegionScoreText[]
     */
    private RegionScoreText[] makeRegionScoreTexts(File file) {
        ArrayList<RegionScoreText> al = new ArrayList<>();
        String[] tokens = null;
        String line = null;
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                while ((line = in.readLine()) != null) {
                    tokens = PATTERN_TAB.split(line);
                    al.add(new RegionScoreText(Integer.parseInt(tokens[beginningColumnIndex]), Integer.parseInt(tokens[endingColumnIndex]), Float.parseFloat(tokens[scoreColumnIndex]), concatinateTextColumns(tokens)));
                }
            }
            RegionScoreText[] d = new RegionScoreText[al.size()];
            al.toArray(d);
            Arrays.sort(d);
            return d;
        } catch (Exception e) {
            System.out.println("Could not parse an int or float value from malformed line -> " + line);
            e.printStackTrace();
            return null;
        }
    }

    private String concatinateTextColumns(String[] tokens) {
        //just one?
        if (textColumnIndexs.length == 1) {
            return tokens[textColumnIndexs[0]];
        }
        //nope so concatinate
        StringBuilder sb = new StringBuilder(tokens[textColumnIndexs[0]]);
        for (int i = 1; i < textColumnIndexs.length; i++) {
            sb.append("\t");
            sb.append(tokens[textColumnIndexs[i]]);
        }
        return sb.toString();
    }

    /**
     * Splits a text file by chromosome and strand writing the lines to the saveDirectory. Will skip chromosomes that
     * look like splice junctions upon request (ie chr5_234423_234899).
     * The files will be named chromosomeStrand (ie chr5+ or chr5F) as designated in the data file.
     * Set strandColumnIndex to -1 to ignore strand.
     */
    public static HashMap<String, File> splitFileByChromosomeAndStrand(File dataFile, File saveDirectory, int chromosomeColumnIndex, int strandColumnIndex, boolean skipSpliceJunctions) {
        Pattern tab = Pattern.compile("\\t");
        Pattern spliceJunction = Pattern.compile(".+_\\d+_\\d+");
        HashMap<String, PrintWriter> chromOut = new HashMap<>();
        HashMap<String, File> chromFile = new HashMap<>();
        try {
            try ( //get reader
                    BufferedReader in = USeqUtilities.fetchBufferedReader(dataFile)) {
                String line;
                String[] tokens = null;
                String currentChrom = "";
                PrintWriter out = null;
                String strand = ".";
                int counter = 0;
                while ((line = in.readLine()) != null) {
                    try {
                        line = line.trim();
                        if (line.length() == 0) {
                            continue;
                        }
                        if (line.startsWith("#")) {
                            continue;
                        }
                        if (line.contains("chrAdapter")) {
                            continue;
                        }
                        tokens = tab.split(line);
                        trim(tokens);

                        //parse chromosome
                        String chromosome = tokens[chromosomeColumnIndex];
                        //check for splice junction
                        if (skipSpliceJunctions && spliceJunction.matcher(chromosome).matches()) {
                            continue;
                        }
                        //parse strand
                        if (strandColumnIndex != -1) {
                            strand = tokens[strandColumnIndex];
                        }
                        String chromStrand = chromosome + strand;

                        //get PrintWriter
                        if (!currentChrom.equals(chromStrand)) {
                            currentChrom = chromStrand;
                            if (chromOut.containsKey(chromStrand)) {
                                out = chromOut.get(chromStrand);
                            } else {
                                File f = new File(saveDirectory, chromStrand);
                                out = new PrintWriter(new FileWriter(f));
                                chromOut.put(chromStrand, out);
                                chromFile.put(chromStrand, f);
                            }
                        }
                        //save data
                        out.println(line);
                    } catch (Exception e) {
                        System.out.println("\nProblem parsing line -> " + line + " Skipping!");
                        //e.printStackTrace();
                        if (counter++ == 1000) {
                            System.out.println("Too many malformed lines.  Aborting.");
                            return null;
                        }
                    }
                }
            }
            //close the print writers
            for (PrintWriter printWriter : chromOut.values()) {
                printWriter.close();
            }

            return chromFile;
        } catch (Exception e) {
            e.printStackTrace();
            return chromFile;
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printDocs();
            System.exit(0);
        }
        new Text2USeq(args);
    }

    /**
     * This method will process each argument and assign new variables
     */
    public void processArgs(String[] args) {
        Pattern pat = Pattern.compile("-[a-z]");
        System.out.println("\nArguments: " + USeqUtilities.stringArrayToString(args, " ") + "\n");
        for (int i = 0; i < args.length; i++) {
            String lcArg = args[i].toLowerCase();
            Matcher mat = pat.matcher(lcArg);
            if (mat.matches()) {
                char test = args[i].charAt(1);
                try {
                    switch (test) {
                        case 'f':
                            inputFiles = USeqUtilities.extractFiles(new File(args[++i]));
                            break;
                        case 'b':
                            beginningColumnIndex = Integer.parseInt(args[++i]);
                            break;
                        case 'e':
                            endingColumnIndex = Integer.parseInt(args[++i]);
                            break;
                        case 'v':
                            scoreColumnIndex = Integer.parseInt(args[++i]);
                            break;
                        case 't':
                            textColumnIndexs = USeqUtilities.stringArrayToInts(args[++i], ",");
                            break;
                        case 's':
                            strandColumnIndex = Integer.parseInt(args[++i]);
                            break;
                        case 'c':
                            chromosomeColumnIndex = Integer.parseInt(args[++i]);
                            break;
                        case 'i':
                            rowChunkSize = Integer.parseInt(args[++i]);
                            break;
                        case 'g':
                            versionedGenome = args[++i];
                            break;
                        case 'd':
                            description = args[++i];
                            break;
                        case 'h':
                            color = args[++i];
                            break;
                        case 'r':
                            graphStyle = Integer.parseInt(args[++i]);
                            break;
                        default:
                            USeqUtilities.printExit("\nProblem, unknown option! " + mat.group());
                    }
                } catch (Exception e) {
                    USeqUtilities.printExit("\nSorry, something doesn't look right with this parameter: -" + test + "\n");
                }
            }
        }
        //check params
        if (inputFiles == null || inputFiles.length == 0) {
            USeqUtilities.printErrAndExit("\nCannot find your input files?\n");
        }
        if (chromosomeColumnIndex == -1 || beginningColumnIndex == -1) {
            USeqUtilities.printErrAndExit("\nPlease enter a chromosome and or position column indexes\n");
        }
        if (versionedGenome == null) {
            USeqUtilities.printErrAndExit("\nPlease enter a genome version following DAS/2 notation (e.g. H_sapiens_Mar_2006, M_musculus_Jul_2007, C_elegans_May_2008).\n");
        }

        //instantiate outputFiles
        outputDirectories = new File[inputFiles.length];

        //find max index
        maxIndex = -1;
        if (beginningColumnIndex > maxIndex) {
            maxIndex = beginningColumnIndex;
        }
        if (endingColumnIndex > maxIndex) {
            maxIndex = endingColumnIndex;
        }
        if (scoreColumnIndex > maxIndex) {
            maxIndex = scoreColumnIndex;
        }
        if (textColumnIndexs != null) {
            for (int textColumnIndex : textColumnIndexs) {
                if (textColumnIndex > maxIndex) {
                    maxIndex = textColumnIndex;
                }
            }
        }
        if (strandColumnIndex > maxIndex) {
            maxIndex = strandColumnIndex;
        }
        if (chromosomeColumnIndex > maxIndex) {
            maxIndex = chromosomeColumnIndex;
        }
        if (beginningColumnIndex > maxIndex) {
            maxIndex = beginningColumnIndex;
        }
        //flip make graph boolean? if end position are provided
        if (endingColumnIndex != -1) {
            makeGraph = false;
        }

        //check color
        if (color != null) {
            if (!ArchiveInfo.COLOR_HEX_FORM.matcher(color).matches()) {
                USeqUtilities.printErrAndExit("\nCannot parse a hexidecimal color code (e.g. #CCFF33) from your color choice?! -> " + color);
            }
        }

    }

    /**
     * Trims all the strings in the array String.trim()
     */
    public static void trim(String[] s) {
        for (int i = 0; i < s.length; i++) {
            s[i] = s[i].trim();
        }
    }

    public static void printDocs() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < GRAPH_STYLES.length; i++) {
            sb.append("      ").append(i).append("\t").append(GRAPH_STYLES[i]).append("\n");
        }
        System.out.println("\n"
                + "**************************************************************************************\n"
                + "**                              Text 2 USeq: April 2011                             **\n"
                + "**************************************************************************************\n"
                + "Converts text genomic data files (e.g. xxx.bed, xxx.gff, xxx.sgr, etc.) to\n"
                + "binary USeq archives (xxx.useq).  Assumes interbase coordinates. Only select\n"
                + "the columns that contain relevant information.  For example, if your data isn't\n"
                + "stranded, or you want to ignore strands, then skip the -s option.  If your data\n"
                + "doesn't have a value/ score then skip the -v option. Etc. Use the USeq2Text app to\n"
                + "convert back to text xxx.bed format. \n\n"
                + "Options:\n"
                + "-f Full path file/directory containing tab delimited genomic data files.\n"
                + "-g Genome verison using DAS notation (e.g. H_sapiens_Mar_2006, M_musculus_Jul_2007),\n"
                + "      see http://genome.ucsc.edu/FAQ/FAQreleases#release1\n"
                + "-c Chromosome column index\n"
                + "-b Position/Beginning column index\n"
                + "-s (Optional) Strand column index (+, -, or .; NOT F, R)\n"
                + "-e (Optional) End column index\n"
                + "-v (Optional) Value column index\n"
                + "-t (Optional) Text column index(s), comma delimited, no spaces, defines which columns\n"
                + "      to join using a tab.\n"
                + "-i (Optional) Index size for slicing split chromosome data (e.g. # rows per slice),\n"
                + "      defaults to 10000.\n"
                + "-r (Optional) For graphs, select a style, defaults to 0\n" + sb
                + "-h (Optional) Color, hexadecimal (e.g. #6633FF), enclose in quotations\n"
                + "-d (Optional) Description, enclose in quotations \n"
                + "\nExample: java -Xmx4G -jar pathTo/USeq/Apps/Text2USeq -f\n"
                + "      /AnalysisResults/BedFiles/ -c 0 -b 1 -e 2 -i 5000 -h '#6633FF'\n"
                + "      -d 'Final processed chIP-Seq results for Bcd and Hunchback, 30M reads'\n"
                + "      -g H_sapiens_Feb_2009 \n\n"
                + "Indexes for common formats:\n"
                + "       bed3 -c 0 -b 1 -e 2\n"
                + "       bed5 -c 0 -b 1 -e 2 -t 3 -v 4 -s 5\n"
                + "       bed12 -c 0 -b 1 -e 2 -t 3,6,7,8,9,10,11 -v 4 -s 5\n"
                + "       gff w/scr,stnd,name -c 0 -b 3 -e 4 -v 5 -s 6 -t 8\n"
                + "\n"
                + "**************************************************************************************\n");

    }
}
