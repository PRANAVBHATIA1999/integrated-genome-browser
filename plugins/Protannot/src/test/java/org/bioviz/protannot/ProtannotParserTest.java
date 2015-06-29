/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bioviz.protannot;

import com.affymetrix.genometry.BioSeq;
import java.io.InputStream;
import junit.framework.Assert;
import org.bioviz.protannot.generated.ProtannotParser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tarun
 */
public class ProtannotParserTest {

    private static final Logger logger = LoggerFactory.getLogger(ProtannotParserTest.class);

    @Test
    public void testProtannotParser() {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("diff_motifs/STARD3.sample.paxml");
        ProtannotParser parser = new ProtannotParser();
        BioSeq chromosome = null;
        try {
            Assert.assertNotNull(resourceAsStream);
            chromosome = parser.parse(resourceAsStream);
        } catch (Exception ex) {
            logger.error(null, ex);
        }
        Assert.assertNotNull(chromosome);

    }
    
}
