package com.affymetrix.genometryImpl.parsers;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.affymetrix.genometryImpl.util.SynonymLookup;
import java.util.*;

/**
 * A class to test the SynonymLookup class.
 * Specifially tests the effect of setting the flags "case_sensitive" and "strip_random".
 *
 */
public class SynonymLookupTest {
    static SynonymLookup sl;


  public SynonymLookupTest() {
  }


  @Before
  public void setUp() {
    sl = new SynonymLookup();
    
    
    sl.addSynonyms(new String[] {"1", "chr1", "CHR1"}); // Add upper and lower case by hand in this test
    sl.addSynonyms(new String[] {"1", "one"});
    // NOTE that "one" and "chr1" are NOW currently considered synonyms 
    // even though they are listed on separate lines.  (This was not true prior to IGB 4.46)
    
    sl.addSynonyms(new String[] {"chr2", "", "2"});
    sl.addSynonyms(new String[] {"chr3", "", null, "3", "chrIII"});
    sl.addSynonyms(new String[] {"chr3_random", "3_random"});
    sl.addSynonyms(new String[] {"chrM", "M", "chrMT", "mitochondria", "mitochondrion"});
    sl.addSynonyms(new String[] {"R_norvegicus_Jan_2003", "Rat_Jan_2003", "Rat jan2003", "rn2", "rn:Jan_2003", "Rn:Jan_2003"});
    sl.addSynonyms(new String[] {"H_sapiens_May_2004", "Human_May_2004", "hs.NCBIv35", "hg17", "human_ncbi35", "ncbi.v35", "hs:May_2004", "Hs:May_2004", "hs:NCBIv35", "Hs:NCBIv35", "ensembl1834", "chado/chado-Hsa-17", "human/17", "Hs;NCBIv35", "Hs:NCBI35"});
  }
   
  
  /** Some tests that don't depend on whether case-sensitive or not. 
   *  This test is called as a helper in some other tests.
   */
  public static void helper1(SynonymLookup sl) {
    assertTrue(sl.isSynonym("chr1", "1"));
    assertTrue(sl.isSynonym("chr1", "CHR1"));
    assertTrue(sl.isSynonym("1", "chr1"));
    assertTrue(sl.isSynonym("1", "CHR1"));
    assertTrue(sl.isSynonym("CHR1", "chr1"));
    assertTrue(sl.isSynonym("CHR1", "1"));
        
    // This tests transitivity: that two things defined on separate lines ARE considered synonyms
    assertTrue(sl.isSynonym("CHR1", "one"));
    
    assertFalse(sl.isSynonym("1", "chr2"));

    assertTrue(sl.isSynonym("chr2", "2"));
    assertTrue(sl.isSynonym("2", "chr2"));
    
    List list1 = sl.getSynonyms("chrMT");
    List list2 = sl.getSynonyms("chrM");
    assertEquals(list1.size(), list2.size());    

    // This tests that the null and empty strings were ignored in the input
    // (If they were not ignored, then "chr2" == "" == "chr3" due to transitivity)
    assertFalse(sl.isSynonym("chr2", "chr3"));
    
    // The elements in list1 and list2 should be the same, other than ordering
    Set set1 = new HashSet(list1);
    Set set2 = new HashSet(list2);
    assertEquals(set1, set2);    
  }
  
  /** Run some tests with case-sensitive set to true. */
  @Test
  public void testCaseSensitive() {
    sl.setCaseSensitive(true);
    helper1(sl);

    // These tests are all false when case-sensitive is true
    assertFalse(sl.isSynonym("CHR2", "2"));
    assertFalse(sl.isSynonym("CHR2", "chr2"));
    assertFalse(sl.isSynonym("2", "CHR2"));
    assertFalse(sl.isSynonym("chr2", "CHR2"));
    
    // There is no list of synonyms for "chrm" if case sensitive is true.
    List list3 = sl.getSynonyms("chrm");
    assertTrue(list3 == null);
  }
  
  /** Run some tests with case-sensitive set to false. */
  @Test
  public void testCaseInsensitive() {
    sl.setCaseSensitive(false);
    helper1(sl);

    // These tests are all true when case-sensitive is true
    assertTrue(sl.isSynonym("CHR2", "2"));
    assertTrue(sl.isSynonym("CHR2", "chr2"));
    assertTrue(sl.isSynonym("2", "CHR2"));
    assertTrue(sl.isSynonym("chr2", "CHR2"));
    
    // There *is* a list of synonyms for "chrm" if case sensitive is false.
    // and it should match the list for "CHRmT"
    List list3 = sl.getSynonyms("chrm");
    List list4 = sl.getSynonyms("CHRmT");
    assertTrue(list3 != null);
    assertTrue(list4 != null);

    // The elements in list3 and list4 should be the same, other than ordering
    Set set3 = new HashSet(list3);
    Set set4 = new HashSet(list4);
    assertEquals(set3, set4);

    // even with case-sensitive set to false, the list will not actually contain "chrm", but rather "chrM"
    // since that is how we put it in the list.
    assertTrue(list3.contains("chrM"));
    assertFalse(list3.contains("chrm"));
  }
  
  /** Tests what happens when SynonymLookup.stripRandom is set to true.  */
  @Test
  public void testStripRandom() {
    sl.stripRandom = true;
    helper1(sl);
    
    sl.setCaseSensitive(true);
    assertTrue(sl.isSynonym("1_random", "chr1_random"));
    assertTrue(sl.isSynonym("1_random", "one_random"));
    assertTrue(sl.isSynonym("chrMT_random", "M_random"));
    
    // "random" chromosomes are NOT the same as non-random ones
    assertFalse(sl.isSynonym("1_random", "chr1"));
    assertFalse(sl.isSynonym("2", "chr2_random"));

    // True because "CHR1", "chr1" and "1" were all explicity given as input
    assertTrue(sl.isSynonym("1_random", "CHR1_random"));
    assertTrue(sl.isSynonym("1_random", "chr1_random"));
    assertTrue(sl.isSynonym("chr1_random", "CHR1_random"));

    // False due to case-sensitivity: we know nothing about "CHR2"
    assertFalse(sl.isSynonym("2_random", "CHR2_random"));
    assertFalse(sl.isSynonym("chr2_random", "CHR2_random"));
    

    // When case-sensitive is false, it should now know that 2_random == CHR2_random == chr2_random
    sl.setCaseSensitive(false);
    assertTrue(sl.isSynonym("2_random", "CHR2_random"));
    assertTrue(sl.isSynonym("chr2_random", "CHR2_random"));
    
    assertTrue(sl.isSynonym("CHRMT_RaNdOm", "M_random"));
    assertFalse(sl.isSynonym("CHRMT_RANDOM", "CHRM"));
    
    // This is false because we didn't teach it about "MT" (we taught it "chrMT" not "MT")
    assertFalse(sl.isSynonym("CHRMT_RANDOM", "MT_random"));
  }
}
