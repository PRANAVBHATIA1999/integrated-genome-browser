/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apollo.analysis;
import apollo.datamodel.Sequence;
import apollo.datamodel.StrandedFeatureSet;
import apollo.datamodel.StrandedFeatureSetI;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author tkanapar
 */
public class RemoteBlastNCBITest {
    private final RemoteBlastNCBI.BlastType blastType = RemoteBlastNCBI.BlastType.blastx;//can be resued for blastp
    StrandedFeatureSetI sf = new StrandedFeatureSet();
    String id ="null chr1:9,416-11,422 + A_thaliana_Jun_2009 [Arabidopsis thaliana]";
    String sequence = "GAAGTTTCCTTATCATCATTAATCATGCCTATGAAAACCCTAACCGGCCTACATATTTGCAGTCTCTTTTAGGTCAGGAATCTAATGAGAATGAATTATTTTATTTTATTTAACAATAATTGGAGCATGTCATTAATTCTCTTTATTCTTCTTACACAACTAATCATTTAGATGTGTTACAATATTATTTCCTTTAGTCATTTTCATAATTTTAATACCTCCGTACTTTTCACTAATACCTCCCCTTTTAATTTTCATTATTTCTTCTTTTCTATCAGTCTATGCATGCATTCTTTTGAATATTAAAATGCATTTTATATTCTTTTGACAACTATGCACAAGCCTTTTGAGACACATCTACACAATATAATAGCACAAGCCTTTATGAGACATATCTACACAATATCATTGCTACTTGTAGACTATTTGGAATACGTATTTACATATTCCATTGTATCATCCCTTTGCAAATGTTTTATACATATAACTAACATATACATTATTCGTAACTTTGTTCCCCTATATCGAAAAATGTGGGCTACACATATAACTAACATATATATATATATGTATGTTTATATGATATAGTCTCCATGTCTATATATCTTATATATTACATGTTTCATGTTTACGGTCAAGGGAGTATTTTTATACGCATACACAATCATACACACTTAACCCTACTTATAATGATGTAGGTTCATATATTTATCTTATTTTAGGATCATTCGATCACAAATTATACGGACCCTCATACTCTCTAAAGATATACAAAATCCGCTATGTCATATCCGATCCGAATTAGCAGCTAAAGAAAAACAAACACATGCATCTACTGAAGATTTGAGTCTCGAGTGCTTAGTTACATGAACTATCACAAAGGATATGGATAATATAAGGTGTACTGAAGTATGTCTATGCAATGGGAGGGAAATACATTCTGTTAAATGACTTGTCGATTTGATCTTTCATGCCAAAGATTAAAAATTTAACACTTAATTAACGCAATCTTACCATATATTCATGGACTACATGCAGAATAGTAATTCTCCCAACCTTTCTAGTTATTTACCTGAATGTGTTTATGTACATGGACCGGTAACCTCATGTATATATGCACATACTGACAATCTGACATACATATATATAGTAGATATGACAACAACAACAAAAAAAAAAAAAATTCCTTGTTCGTGAAGCATGATCTGAGAGTTCCTAGTTAGCATGTTGTGTGGGATCATACTTTTAATATGCTGCAAGTACCAGTCAATTTTAGTATGGGAAACTATAAACATGTATAATCAACCAATGAACACGTCAATAACCTATTGAACAGCTTAGGGTGAAAATTATGATCCGTAGAGACAGCATTTAAAAGTTCCTTACGTCCACGTAAAATAATATATCAATTTATACATATACATGTGTAAACTGTGTATATATAGGGTAGGTATATGTGTATATATATAGTAATTGACAAATGATTTAGGTTCTAACATATATTCTAAAAGTACTCATGAGTTTGTGAGATCTACACAAGATACCTGATTTGATAAAAATGGCTTCAACTTGCAATCCAAACCAAACCAAACAAAGTTAATAACCAAGGGTTAATAACAAAAACAAGAATCTAGAATTAGTAAAAAAATGAGAAATTAATGAACCTGTGATCATAAAAAAAGTCAAACAATGTGAAAACATATCATACCTTTTGTTCTTTTTAATATAATAATTGAATTACTAAATGGATGGATCAGTCCTTCTTCCATAGCTAGCTTCTCTTTATTTTCTCTGCCCATAACCTGCAGAAAATCTCTTTAACCGAGCAAAATTACAAGAGTAACCAAACAAACAAAATAGGACCATCAGAGAGAGAGAAAAGAGTGCCTTTTTTTGGACCTGCCCATTAGCTTAGAATGTACCATGAAACACTTTGTCAGTGTAGGGAGATAGGCACAGAGAGTACAATTCATGAAATTTATAAGCTTTTTTCCCACTCATCAATT";
    int length = 50;
    
    @Test
    public void checkRunAnalysis() throws Exception{
        RemoteBlastNCBI blast = new RemoteBlastNCBI(blastType, new RemoteBlastNCBI.BlastOptions());
        String residue = sequence.substring(0,length);
        Sequence seq = new Sequence(id, residue);
        String url = blast.runAnalysis(sf, seq, 1);
        System.out.println(residue);//can be used for direct input in NCBI
        System.out.println(url);//To analyse the url
        String[] urlSplits = url.split("\\?");//Splitting on the url ?
        String ridValue = urlSplits[1].split("\\&")[0];//The first parameter is rid
        String rid = ridValue.split("\\=")[1];//obtaining the rid value
        if(rid.equalsIgnoreCase("null")||rid.equalsIgnoreCase("")){
            rid = null;
        }
        Assert.assertNotNull(rid);
    }
    
}
