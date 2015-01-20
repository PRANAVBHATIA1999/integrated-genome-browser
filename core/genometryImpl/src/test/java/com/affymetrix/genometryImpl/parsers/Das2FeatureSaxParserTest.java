/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.affymetrix.genometryImpl.parsers;

//import com.affymetrix.genometry.BioSeq;
//import com.affymetrix.genometry.SeqSpan;
//import com.affymetrix.genometry.SeqSymmetry;
//import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import com.affymetrix.genometryImpl.SeqSpan;
import com.affymetrix.genometryImpl.span.SimpleSeqSpan;
import com.affymetrix.genometryImpl.symmetry.impl.SeqSymmetry;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author auser
 */
public class Das2FeatureSaxParserTest {

    public static final String test_file_name_1 = "data/das2/test2.das2xml";

    /**
     * Tests the parsing of the <LINK> elements
     *
     * @throws org.xml.sax.SAXException
     */
    @Test
    public void testLinks() throws SAXException {
        AnnotatedSeqGroup group = new AnnotatedSeqGroup("Test Group");
        boolean annot_seq = true;
        Das2FeatureSaxParser ins = new Das2FeatureSaxParser();
        String uri = Das2FeatureSaxParser.TYPEURI;

        List<SeqSymmetry> results = null;
        try {
            InputStream istr = Das2FeatureSaxParserTest.class.getClassLoader().getResourceAsStream(test_file_name_1);
            assertNotNull(istr);
            InputSource isrc = new InputSource(istr);
            assertNotNull(isrc);
            results = ins.parse(isrc, uri, group, annot_seq);
            assertNotNull(results);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("Failed due to Exception: " + ioe.toString());
        }

    }

    @Test
    public void testgetRangeString() throws Exception {
        AnnotatedSeqGroup group = new AnnotatedSeqGroup("Test Group");
        BioSeq seq = group.addSeq("chr1", 30432563);
        SeqSpan span = new SimpleSeqSpan(500, 800, seq);
        boolean indicate_strand = true;
        String results = Das2FeatureSaxParser.getRangeString(span, indicate_strand);
        assertEquals("500:800:1", results);

    }

}
