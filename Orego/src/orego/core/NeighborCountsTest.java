package orego.core;

import static orego.core.NeighborCounts.*;
import static orego.core.Colors.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class NeighborCountsTest {

	private int counts;

	@Before
	public void setUp() {
		counts = initialNeighborCounts();
	}

	@Test
	public void testIncrement() {
		for (int i = 0; i < MAX_NEIGHBORS; i++) {
			counts += NEIGHBOR_INCREMENT[BLACK];
		}
		assertEquals(4, extractNeighborCount(counts, BLACK));
		assertEquals(0, extractNeighborCount(counts, WHITE));
		assertEquals(0, extractNeighborCount(counts, VACANT));
		assertTrue(hasMaxNeighborsForColor(counts, BLACK));
		assertFalse(hasMaxNeighborsForColor(counts, WHITE));
	}

	@Test
	public void testInitialNeighborCounts() {
		assertEquals(0, extractNeighborCount(counts, BLACK));
		assertEquals(0, extractNeighborCount(counts, WHITE));
		assertEquals(4, extractNeighborCount(counts, VACANT));
	}

}
