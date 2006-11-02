/*
 * WiggleParserTest.java
 * JUnit based test
 *
 * Created on October 18, 2006, 2:36 PM
 */

package com.affymetrix.igb.parsers;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.SeqSymmetry;
import com.affymetrix.genometry.util.SeqUtils;
import junit.framework.*;
import java.io.*;
import java.util.*;
import com.affymetrix.igb.genometry.*;
import com.affymetrix.igb.glyph.GraphState;

/**
 *
 * @author Ed Erwin
 */
public class WiggleParserTest extends TestCase {
  
  public WiggleParserTest(String testName) {
    super(testName);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(WiggleParserTest.class);
    
    return suite;
  }

  public void testParse() throws Exception {
    String filename = "test_files/wiggleExample.wig";
    InputStream istr = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

    AnnotatedSeqGroup seq_group = new AnnotatedSeqGroup("test");

    WiggleParser parser = new WiggleParser();
    
    List results = parser.parse(istr, seq_group, true, filename);
    
    assertEquals(3, results.size());
    
    GraphSym gr0 = (GraphSym) results.get(0);

    SmartAnnotBioSeq seq = (SmartAnnotBioSeq) gr0.getGraphSeq();
    
    assertTrue(gr0 instanceof GraphIntervalSym);
    assertEquals("chr19", gr0.getGraphSeq().getID());
    assertEquals(9, gr0.getPointCount());
    assertEquals(59302000, gr0.getSpan(seq).getMin());
    assertEquals(59304700, gr0.getSpan(seq).getMax());
    
    GraphSym gr1 = (GraphSym) results.get(1);
    assertTrue(gr1 instanceof GraphIntervalSym);
    assertEquals(9, gr1.getChildCount());
    assertTrue(gr1.getChild(0) instanceof Scored);
    assertEquals(59304701, gr1.getSpan(seq).getMin());
    assertEquals(59308021, gr1.getSpan(seq).getMax());
    
    GraphSym gr2 = (GraphSym) results.get(2);
    assertTrue(gr2 instanceof GraphIntervalSym);
    assertEquals(10, gr2.getChildCount());
    assertEquals(59307401, gr2.getSpan(seq).getMin());
    assertEquals(59310301, gr2.getSpan(seq).getMax());
    assertEquals(300.0f, ((Scored) gr2.getChild(7)).getScore(), 0.00000001);
    
    assertEquals("Bed Format", gr0.getID());
    assertEquals("variableStep", gr1.getID());
    assertEquals("fixedStep", gr2.getID());

    GraphState state = gr1.getGraphState();
    assertEquals(0.0, state.getVisibleMinY(), 0.00001);
    assertEquals(25.0, state.getVisibleMaxY(), 0.00001);
    
    assertEquals(59310301, seq.getLength());
    
  }
  
  public void testWriteGraphs() {
  }
}
