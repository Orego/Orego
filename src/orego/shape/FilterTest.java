package orego.shape;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FilterTest {

	private Filter filter;
	
	@Before
	public void setUp() throws Exception {
		filter = new Filter(4, 16);
	}

	@Test
	public void testGetLocalIndex() {
		filter = new Filter(4, 4);
		long hash = 0xBEEF;
		assertEquals(0xF, filter.getLocalIndex(hash, 0));
		assertEquals(0xE, filter.getLocalIndex(hash, 1));
		assertEquals(0xE, filter.getLocalIndex(hash, 2));
		assertEquals(0xB, filter.getLocalIndex(hash, 3));
	}

	@Test
	public void testFilter() {
		assertEquals(0,filter.getLowestCount(0x0));
		assertEquals(0,filter.getLowestCount(0xbeef0000));
		for (int i=-5; i<filter.getThreshold(); i++){
			filter.store(0xbeef0000);
		}
		assertEquals(filter.getThreshold()+5,filter.getLowestCount(0xbeef0000));
		assertEquals(0,filter.getLowestCount(0x0));
		assertTrue(filter.isReasonable(0xbeef0000));
		assertFalse(filter.isReasonable(0x0));
	}
}
