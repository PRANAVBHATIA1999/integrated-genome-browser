package org.lorainelab.igb.snp.convert;

import com.google.common.base.Splitter;
import org.lorainelab.igb.snp.convert.beans.Bed;
import org.lorainelab.igb.snp.convert.beans.Snp;
import org.lorainelab.igb.snp.convert.mapmaker.MapMaker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for the actual reading of the file, extraction of
 the relevant data, and converting it into .Bed format.
 *
 * @author Daniel
 */
public class SnpConverterAction {

    private static final Logger logger = LoggerFactory.getLogger(SnpConverterAction.class);
    /* Signifies that there is a problem with the input file (does not exist or missing). */
    private final int IN_FILE_ERROR = 0, DEST_ERROR = 1, OUT_FILE_ERROR = 2, BEGIN_CONVERSION = 3;

    private int snpCount = 0;
    /* REGEX pattern for lines that should be ignored */
    private final Pattern pattern;
    /* HasHSet containing the chromosome numbers the encounter has encountered. */
    private final HashSet<String> chroms = new HashSet<>(25);
    /* Reference to the TextArea where progress is displayed. */
    private final JTextArea progress;
    /* A String denoting where the .dat file generated by the ChronicleMapBuilder should go. */
    private final String DOT_DAT_PATH = "src/main/resources/GRCh38.dat";
    /* The ChronicleMap that contains the Snp positions for build GRCh38. */
//    private static ChronicleMap<String, String> grch38;
    private String[][] data;
    /* A fake Snp id for the IGB hack. */
    public static final String IGB_HACK_FAKE_SNP = "IgbHackFakeSNP";

    /**
     * This method is responsible for starting the conversion process. It takes
 three strings representing the input and output file paths, the name of
 the outputted file, and boolean which tells the method whether or not the
 user wants their Snp information updated to that of the latest build.
 Also,
 a string is returned that will tell the user that something is wrong with
 their inputs, or that the conversion is complete.
     *
     * @param fileIn the path/name of the file to be converted.
     * @param outDest the location where the output file is supposed to go.
     * @param fileOut the name of the file to be outputted.
     * @param ref tells the method which of the two conversion methods should be
     * called.
     * @return a string representing the state of the application.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public String convertStart(String fileIn, String outDest, String fileOut, Boolean ref) throws FileNotFoundException, IOException {

        switch (checkInputs(fileIn, outDest, fileOut)) {
            case IN_FILE_ERROR:
                return "Usage: Please select an input file.\n";
            case DEST_ERROR:
                return "Usage: Please select a destination for the output file. \n";
            case OUT_FILE_ERROR:
                return "Usage: Please provide a name for the output file. \n";
            case BEGIN_CONVERSION:
                if (!ref) {
                    conversionNoReference(fileIn, outDest, fileOut);
                } else {
                    conversionWithReference(fileIn, outDest, fileOut);
                }
                return "Conversion Complete!\n";
            default:
                return "Converter encountered a problem. \n";
        }

    }

    /**
     * This method checks to make sure that the user provided some kind of input
     * for the appropriate fields.
     *
     * @param fileIn the path/name of the file to be converted.
     * @param outDest the location where the output file is supposed to go.
     * @param fileOut the name of the file to be outputted.
     * @return an integer denoting if there is a problem with the inputs, or if
     * it
     * is safe to proceed.
     */
    private int checkInputs(String fileIn, String outDest, String fileOut) {

        if (fileIn.equals("")) {
            return IN_FILE_ERROR;
        } else if (outDest.equals("")) {
            return DEST_ERROR;
        } else if (fileOut.equals("")) {
            return OUT_FILE_ERROR;
        }

        return BEGIN_CONVERSION;
    }

    /**
     * If user decides that they do not want to update the positions of their
     * SNPs to that of a more recent build this is the method that manages the
     * conversion process.
     *
     * @param fileIn the path/name of the file to be converted.
     * @param outDest the location where the output file is supposed to go.
     * @param fileOut the name of the file to be outputted.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void conversionNoReference(String fileIn, String outDest, String fileOut) throws FileNotFoundException, IOException {
        String completeOutPath = outDest + File.separator + fileOut + ".bed";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(completeOutPath)));
                BufferedReader reader = new BufferedReader(new FileReader(new File(fileIn)))) {
            progress.append("Beginning conversion.\n");
            writer.write("track type=bedDetail\n");
            for (String nextLine = reader.readLine(); nextLine != null; nextLine = reader.readLine()) {
                snpCount += 1;
                List<Snp> snps = new LinkedList<>();
                Matcher matcher = pattern.matcher(nextLine);

                if (!matcher.find()) {
                    snps = getSNPs(nextLine);
                }
                if (snps.size() > 0) {
                    List<Bed> beds = convertSNPs(snps);
                    writeBEDs(beds, writer);
                    beds.clear();
                    snps.clear();
                }
                if (snpCount % 10000 == 0) {
                    progress.append("SNPs converted: " + snpCount + "\n");
                }

            }
        }
    }

    /**
     * If user decides that they do want to update the positions of their SNPs
     * to that of a more recent build this is the method that manages the
     * conversion process. The information related to the new positions of SNPs
     * is kept in a group of text files which have been zipped to conserve space
     * and can be found in the Resources directory.
     *
     * @param fileIn the path/name of the file to be converted.
     * @param outDest the location where the output file is supposed to go.
     * @param fileOut the name of the file to be outputted.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void conversionWithReference(String fileIn, String outDest, String fileOut) throws IOException {
        MapMaker mapMaker = new MapMaker();
//        grch38 = mapMaker.createChronicleMap(DOT_DAT_PATH);
        data = mapMaker.initializeData();
//        mapMaker.loadReferenceData(grch38);
        String completeOutPath = outDest + File.separator + fileOut + ".bed";
        List<Bed> beds;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(completeOutPath)));
                BufferedReader reader = new BufferedReader(new FileReader(new File(fileIn)))) {
            progress.append("Beginning conversion.\n");
            writer.write("track type=bedDetail\n");
            for (String nextLine = reader.readLine(); nextLine != null; nextLine = reader.readLine()) {
                snpCount += 1;
                List<Snp> snps = new LinkedList<>();
                Matcher matcher = pattern.matcher(nextLine);

                if (!matcher.find()) {
                    snps = getSNPs(nextLine);
                }
                if (snps.size() > 0) {
                    updateSNPLocation(snps);
                    beds = convertSNPs(snps);
                    writeBEDs(beds, writer);
                    beds.clear();
                    snps.clear();
                }
                if (snpCount % 10000 == 0) {
                    progress.append("SNPs converted: " + snpCount + "\n");
                }

            }
        }
    }

    /**
     * This method is responsible for taking a list of Snp objects and updating
 their positions with that found in the grch38 ChronicleMap. It first
 checks
 to make sure that the Snp id is a key to a position located within map.
 If
 so, it then proceeds to update the location in the Snp object with that
 in
 the map. If not, nothing happens.
     *
     * @param snps list of Snp objects to be updated.
     * @throws IOException
     */
    private void updateSNPLocation(List<Snp> snps) {
        int row = 0;
        try {
            for (Snp p : snps) {
                row = MapMaker.calculateRow(data, p.getRsid());
                if (row != -1) {
                    p.setPosition(Integer.parseInt(data[row][1]));
                }
            }
        } catch (Exception ex) {
            //logger.error("Error thrown while setting snp position " + row, ex);
        }
//        if (grch38.containsKey(snps.get(0).getRsid())) {
//            String s = new String();
//            for (Snp p : snps) {
//
//                p.setPosition(Integer.parseInt(grch38.getUsing(p.getRsid(), s)));
//            }
//        }
    }

    /**
     * This method is responsible for extracting the relevant information from
     * the 23&Me generated text file. This information is put into Snp objects
 which are then returned as part of a list.
     *
     * @param nextLine a line from the input file containing Snp information.
     * @return
     */
    private List<Snp> getSNPs(String nextLine) {
        List<Snp> snps = new LinkedList<>();
        List<String> tokens = Splitter.on("\t").omitEmptyStrings().trimResults().splitToList(nextLine);
        String chrom = "chr" + tokens.get(1);
        addIgbTrackHack(tokens, snps, chrom);
        return snps;
    }

    /**
     * This method takes a list of Strings and checks to see if the chromosome
 number for that Snp is present in the set of chromosome numbers (chroms)
 seen
 by the application thus far. If it is, then the method just adds the Snp
 to the list. If it isn't, the method adds five dummy SNPs in addition to
 the
 Snp from the file, and then adds to the chromosome number to the set.
 This
 hack is done to make sure the track will be visible in IGB.
     *
     * @param tokens a list of strings which contains the information relevant
 to each Snp.
     * @param snps a list of Snp objects.
     * @param chrom the chromosome number for the Snp that is about to be added
 to the list.
     * @throws NumberFormatException
     */
    private void addIgbTrackHack(List<String> tokens, List<Snp> snps, String chrom) throws NumberFormatException {
        if (chroms.contains(chrom)) {
            snps.add(new Snp(tokens.get(0), chrom, Integer.parseInt(tokens.get(2)), tokens.get(3)));
        } else {
            for (int i = 0; i < 5; i++) {
                snps.add(new Snp(IGB_HACK_FAKE_SNP, chrom, Integer.parseInt(tokens.get(2)) - 100, tokens.get(3)));
            }
            snps.add(new Snp(tokens.get(0), chrom, Integer.parseInt(tokens.get(2)), tokens.get(3)));
            chroms.add(chrom);
        }
    }

    /**
     * This method is responsible for taking a list of Snp objects and using the
 information contained within to create a list of equivalent Bed objects
     *
     * @param SNPs the list of Snp objects to be converted.
     * @return a list of Bed objects created from the Snp objects.
     */
    private List<Bed> convertSNPs(List<Snp> SNPs) {
        List<Bed> BEDs = new LinkedList<>();
        for (Iterator<Snp> s = SNPs.iterator(); s.hasNext();) {
            BEDs.add(new Bed(s.next()));
            s.remove();
        }

        return BEDs;
    }

    /**
     * This method is responsible for taking a list of Bed objects and writing
 the information it contains to a file specified by the user in .Bed
 format.
     *
     * @param beds a list of bed objects.
     * @param writer a file writer to employed in the writing of the file.
     * @throws IOException
     */
    private void writeBEDs(List<Bed> beds, BufferedWriter writer) throws IOException {
        for (Iterator<Bed> b = beds.iterator(); b.hasNext();) {
            String[] bedLines = b.next().getBED();
            for (String line : bedLines) {
                if (line != null) {
                    writer.write(line + "\n");
                }
            }
            b.remove();
        }
        writer.flush();
    }

    public SnpConverterAction(JTextArea progress) {
        this.pattern = Pattern.compile("#");
        this.progress = progress;
    }

}
