package orego.shape;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TableTest {

	private Table table;
	
	@Before
	public void setUp() throws Exception {
		table = new Table(4, 16);
	}

	@Test
	public void testGetLocalIndex() {
		table = new Table(4, 4);
		long hash = 0xBEEF;
		assertEquals(0xF, table.getLocalIndex(hash, 0));
		assertEquals(0xE, table.getLocalIndex(hash, 1));
		assertEquals(0xE, table.getLocalIndex(hash, 2));
		assertEquals(0xB, table.getLocalIndex(hash, 3));
	}
	
	@Test
	public void testStore() {
		long hash = 23L;
		assertEquals(0.5, table.getWinRate(hash), 0.001);
		table.store(hash, 1);
		assertEquals(1.0, table.getWinRate(hash), 0.001);
		table.store(hash, 1);
		assertEquals(1.0, table.getWinRate(hash), 0.001);
		table.store(hash, 0);
		assertEquals(2.0 / 3.0, table.getWinRate(hash), 0.001);
	}

}
