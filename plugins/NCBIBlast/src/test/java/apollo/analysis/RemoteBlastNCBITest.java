/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apollo.analysis;

import apollo.datamodel.Sequence;
import apollo.datamodel.StrandedFeatureSet;
import apollo.datamodel.StrandedFeatureSetI;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/**
 *
 * @author tkanapar
 */
public class RemoteBlastNCBITest {

    private final RemoteBlastNCBI.BlastType blastType = RemoteBlastNCBI.BlastType.blastx;//can be resued for blastp
    StrandedFeatureSetI sf = new StrandedFeatureSet();
    String id = "null chr1:9,416-11,422 + A_thaliana_Jun_2009 [Arabidopsis thaliana]";
    String sequence = "GAAGTTTCCTTATCATCATTAATCATGCCTATGAAAACCCTAACCGGCCTACATATTTGCAGTCTCTTTTAGGTCAGGAATCTAATGAGAATGAATTATTTTATTTTATTTAACAATAATTGGAGCATGTCATTAATTCTCTTTATTCTTCTTACACAACTAATCATTTAGATGTGTTACAATATTATTTCCTTTAGTCATTTTCATAATTTTAATACCTCCGTACTTTTCACTAATACCTCCCCTTTTAATTTTCATTATTTCTTCTTTTCTATCAGTCTATGCATGCATTCTTTTGAATATTAAAATGCATTTTATATTCTTTTGACAACTATGCACAAGCCTTTTGAGACACATCTACACAATATAATAGCACAAGCCTTTATGAGACATATCTACACAATATCATTGCTACTTGTAGACTATTTGGAATACGTATTTACATATTCCATTGTATCATCCCTTTGCAAATGTTTTATACATATAACTAACATATACATTATTCGTAACTTTGTTCCCCTATATCGAAAAATGTGGGCTACACATATAACTAACATATATATATATATGTATGTTTATATGATATAGTCTCCATGTCTATATATCTTATATATTACATGTTTCATGTTTACGGTCAAGGGAGTATTTTTATACGCATACACAATCATACACACTTAACCCTACTTATAATGATGTAGGTTCATATATTTATCTTATTTTAGGATCATTCGATCACAAATTATACGGACCCTCATACTCTCTAAAGATATACAAAATCCGCTATGTCATATCCGATCCGAATTAGCAGCTAAAGAAAAACAAACACATGCATCTACTGAAGATTTGAGTCTCGAGTGCTTAGTTACATGAACTATCACAAAGGATATGGATAATATAAGGTGTACTGAAGTATGTCTATGCAATGGGAGGGAAATACATTCTGTTAAATGACTTGTCGATTTGATCTTTCATGCCAAAGATTAAAAATTTAACACTTAATTAACGCAATCTTACCATATATTCATGGACTACATGCAGAATAGTAATTCTCCCAACCTTTCTAGTTATTTACCTGAATGTGTTTATGTACATGGACCGGTAACCTCATGTATATATGCACATACTGACAATCTGACATACATATATATAGTAGATATGACAACAACAACAAAAAAAAAAAAAATTCCTTGTTCGTGAAGCATGATCTGAGAGTTCCTAGTTAGCATGTTGTGTGGGATCATACTTTTAATATGCTGCAAGTACCAGTCAATTTTAGTATGGGAAACTATAAACATGTATAATCAACCAATGAACACGTCAATAACCTATTGAACAGCTTAGGGTGAAAATTATGATCCGTAGAGACAGCATTTAAAAGTTCCTTACGTCCACGTAAAATAATATATCAATTTATACATATACATGTGTAAACTGTGTATATATAGGGTAGGTATATGTGTATATATATAGTAATTGACAAATGATTTAGGTTCTAACATATATTCTAAAAGTACTCATGAGTTTGTGAGATCTACACAAGATACCTGATTTGATAAAAATGGCTTCAACTTGCAATCCAAACCAAACCAAACAAAGTTAATAACCAAGGGTTAATAACAAAAACAAGAATCTAGAATTAGTAAAAAAATGAGAAATTAATGAACCTGTGATCATAAAAAAAGTCAAACAATGTGAAAACATATCATACCTTTTGTTCTTTTTAATATAATAATTGAATTACTAAATGGATGGATCAGTCCTTCTTCCATAGCTAGCTTCTCTTTATTTTCTCTGCCCATAACCTGCAGAAAATCTCTTTAACCGAGCAAAATTACAAGAGTAACCAAACAAACAAAATAGGACCATCAGAGAGAGAGAAAAGAGTGCCTTTTTTTGGACCTGCCCATTAGCTTAGAATGTACCATGAAACACTTTGTCAGTGTAGGGAGATAGGCACAGAGAGTACAATTCATGAAATTTATAAGCTTTTTTCCCACTCATCAATT";
    int length = 50;

    public void checkRunAnalysis(String residue) throws Exception {
        RemoteBlastNCBI blast = new RemoteBlastNCBI(blastType, new RemoteBlastNCBI.BlastOptions());
        Sequence seq = new Sequence(id, residue);
        String url = blast.runAnalysis(sf, seq, 1);
        

        int parameterIndex = url.indexOf("?");
        int firstParamenterEndIndex = url.indexOf("&");
        String firstParameter = url.substring(parameterIndex + 1, firstParamenterEndIndex);
        String[] parameterValue = firstParameter.split("\\=");
        String rid = "";
        if (parameterValue[0].equalsIgnoreCase("rid")) {
            rid = parameterValue[1];
        }
        if (rid.equalsIgnoreCase("null") || rid.equalsIgnoreCase("")) {
            rid = null;
        }

        assertNotNull(rid);
    }

    @Test
    public void residueLengthLessThanLength() throws Exception {

        String residue = sequence.substring(0, length);
        checkRunAnalysis(residue);

    }

    @Test
    public void residueLengthGreaterThanLength() throws Exception {
        String residue = sequence;
        checkRunAnalysis(residue);

    }

}
