package bandit;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TableTest {

	private Table table;
	
	@Before
	public void setUp() throws Exception {
		table = new Table(16);
	}

	@Test
	public void testStore() {
		assertEquals(0, table.getRunCount(34));
		assertEquals(0.5, table.getWinRate(34), 0.001);
		table.store(34, true);
		assertEquals(1, table.getRunCount(34));
		assertEquals(1.0, table.getWinRate(34), 0.001);
		table.store(34, true);
		assertEquals(2, table.getRunCount(34));
		assertEquals(1.0, table.getWinRate(34), 0.001);
		table.store(34, false);
		assertEquals(3, table.getRunCount(34));
		assertEquals(2.0 / 3.0, table.getWinRate(34), 0.001);
	}

	@Test
	public void testTableSize() {
		table = new Table(4);
		table.store(3, true);
		table.store(19, true);
		assertEquals(2, table.getRunCount(3));
		assertEquals(1.0, table.getWinRate(3), 0.001);
	}

	@Test
	public void testTableSize2() {
		table = new Table(4);
		table.store(3, true);
		table.store(19, true);
		assertEquals(2, table.getRunCount(19));
		assertEquals(1.0, table.getWinRate(19), 0.001);
	}

	@Test
	public void testNegativeEntry() {
		table = new Table(4);
		table.store(-1, false);
		assertEquals(1, table.getRunCount(15));
	}

	@Test
	public void testMinValue() {
		table = new Table(4);
		// This should not throw an exception
		table.store(Integer.MIN_VALUE, true);
	}

}
