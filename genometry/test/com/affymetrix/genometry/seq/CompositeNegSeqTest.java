/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.affymetrix.genometry.seq;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sgblanch
 */
public class CompositeNegSeqTest {	
	static final CompositeNegSeq seq         = new CompositeNegSeq("seq", 0);
	static final CompositeNegSeq seq_len     = new CompositeNegSeq("len", 500);

	public CompositeNegSeqTest() {
	}

	@Before
		public void setUp() {
		}

	@After
		public void tearDown() {
		}

	/**
	 * Test if CompositeNegSeq(String) will accept a null string.
	 */
	@Test
		public void testConstructor1() {
			constructorTest(null, "Constructor accepted a null id");
		}

	/**
	 * Test if CompositeNegSeq(String, int) will accept a null string.
	 */
	@Test
		public void testConstructor2() {
			constructorTest(null, 42, "Constructor accepted a null id");
		}

	/**
	 * Test if CompositeNegSeq(String, len) will accept a negative length.
	 */
	@Test
		public void testConstructor3() {
			constructorTest("neg_len", -500, "Constructor accepted a negative length");
		}

	/**
	 * Test if CompositeNegSeq(String, int) will accept a zero length.
	 */
	@Test
		public void testConstructor4() {
			constructorTest("zero_len", 0, "Constructor accepted a zero length");
		}

	/**
	 * private function to aid in testing constructor
	 */
	private void constructorTest(String id, String err_msg) {
		CompositeNegSeq testseq;
		try {
			testseq = new CompositeNegSeq(id, 0);
			fail(err_msg);
		} catch (IllegalArgumentException e) { }
	}

	/**
	 * private function to aid in testing constructor
	 */
	private void constructorTest(String id, int len, String err_msg) {
		CompositeNegSeq testseq;
		try {
			testseq = new CompositeNegSeq(id, len);
			fail(err_msg);
		} catch (IllegalArgumentException e) { }
	}

	/**
	 * Simple test to verify that setBounds(int, int) is operating in
	 * the expected manner
	 */
	@Test
		public void testSetBounds1() {
			CompositeNegSeq testseq  = new CompositeNegSeq("testseq", 1000);

			assertEquals(   0, testseq.getMin());
			assertEquals(1000, testseq.getMax());

			testseq.setBounds(500, 1500);
			assertEquals( 500, testseq.getMin());
			assertEquals(1500, testseq.getMax());

			assertEquals(1000, testseq.getLength());
			assertEquals(1000, testseq.getMax() - testseq.getMin());
			assertEquals(1000d, testseq.getLengthDouble(), 0.00001d);

		}

	/**
	 * Test setBounds(int, int) to see what occurs if min == max
	 */
	@Test
		public void testSetBounds2() {
			CompositeNegSeq testseq  = new CompositeNegSeq("testseq", 1000);

			assertEquals(   0, testseq.getMin());
			assertEquals(1000, testseq.getMax());

			try {
				testseq.setBounds(314159,314159);
				fail("setBounds(int, int) allowed min == max");
			} catch (IllegalArgumentException e) { }
		}

	/**
	 * Test setBounds(int, int) to see what occurs if min &gt; max
	 */
	@Test
		public void testSetBounds3() {
			CompositeNegSeq testseq  = new CompositeNegSeq("testseq", 1000);

			assertEquals(   0, testseq.getMin());
			assertEquals(1000, testseq.getMax());

			try {
				testseq.setBounds(100, -1000);
				fail("setBounds(int, int) allowed min > max");
			} catch (IllegalArgumentException e) { }
		}

	/**
	 * Test setBounds(int, int) using a min and max whose values are
	 * between Integer.MIN_VALUE and Integer.MAX_VALUE but whose
	 * difference is greater than Integer.MAX_VALUE.
	 */
	@Test
		public void testSetBounds4() {
			CompositeNegSeq testseq = new CompositeNegSeq("testseq", 1000);

			assertEquals(   0, testseq.getMin());
			assertEquals(1000, testseq.getMax());

			try {
				testseq.setBounds(-2147483640, 2147483640);
			} catch (IllegalArgumentException e) {
				fail("setBoundsDouble(double, double) failed:" + e.getMessage());
			}

			assertEquals(-2147483640, testseq.getMin());
			assertEquals(2147483640, testseq.getMax());

			assertEquals(4294967280d, testseq.getLengthDouble(), 0.00001d);
			assertEquals(4294967280d, (double)testseq.getMax() - (double)testseq.getMin(), 0.00001d);
		}

	/**
	 * Test setBoundsDouble(double, double) using numbers above
	 * Integer.MAX_VALUE  Note that there is no way to directly verify
	 * that the values were actually set, as the only to access the
	 * information is via getLengthDouble().
	 *
	 * In fact, min and max are stored as int, so the bounds will be
	 * wrong
	 */
	@Test
		public void testSetBoundsDouble1() {
			CompositeNegSeq testseq = new CompositeNegSeq("testseq", 1000);

			assertEquals(   0, testseq.getMin());
			assertEquals(1000, testseq.getMax());

			try {
				testseq.setBoundsDouble(3.14159e42d, 3.14159e45d);
			} catch (IllegalArgumentException e) {
				fail("setBoundsDouble(double, double) failed:" + e.getMessage());
			}

			/* These will not work until CompositeNegSeq is fixed */
			/* assertEquals((double)testseq.getMin(), 3.14159e42d, 0.00001d); */
			/* assertEquals((double)testseq.getMax(), 3.14159e45d, 0.00001d); */

			assertEquals(3.13844841e45d, testseq.getLengthDouble(), 0.00001d);
			assertEquals(3.13844841e45d, (double)testseq.getMax() - (double)testseq.getMin(), 0.00001d);
		}

	/**
	 * Test setBoundsDouble(double, double) using a min and max whose
	 * values are between Integer.MIN_VALUE and Integer.MAX_VALUE but
	 * whose difference is greater than Integer.MAX_VALUE.
	 */
	@Test
		public void testSetBoundsDouble2() {
			CompositeNegSeq testseq = new CompositeNegSeq("testseq", 1000);

			assertEquals(   0, testseq.getMin());
			assertEquals(1000, testseq.getMax());

			try {
				testseq.setBoundsDouble(-2147483640d, 2147483640d);
			} catch (IllegalArgumentException e) {
				fail("setBoundsDouble(double, double) failed:" + e.getMessage());
			}

			assertEquals(-2147483640, testseq.getMin());
			assertEquals(2147483640, testseq.getMax());

			assertEquals(4294967280d, testseq.getLengthDouble(), 0.00001d);
			assertEquals(4294967280d, (double)testseq.getMax() - (double)testseq.getMin(), 0.00001d);
		}
}
