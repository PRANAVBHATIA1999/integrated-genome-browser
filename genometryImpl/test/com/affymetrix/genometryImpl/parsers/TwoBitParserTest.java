package com.affymetrix.genometryImpl.parsers;

import com.affymetrix.genometryImpl.AnnotatedSeqGroup;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author sgblanch
 * @version $Id$
 */
public class TwoBitParserTest {

    public TwoBitParserTest() {
    }

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

	/**
	 * Test of open method, of class TwoBitParser.
	 */
	@Test
	public void testOpen() throws Exception {
		System.out.println("open");
		File f = new File("/Users/sgblanch/Desktop/test.2bit");
		TwoBitParser instance = new TwoBitParser();
		instance.open(f, new AnnotatedSeqGroup("foo"));
		// TODO review the generated test code and remove the default call to fail.
		//fail("The test case is a prototype.");
	}

}