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
		counts = FOUR_VACANT_NEIGHBORS;
	}

	@Test
	public void testIncrement() {
		// Test counts
		assertEquals(0, extractNeighborCount(counts, BLACK));
		assertEquals(0, extractNeighborCount(counts, WHITE));
		assertEquals(4, extractNeighborCount(counts, VACANT));
		assertFalse(hasMaxNeighborsForColor(counts, BLACK));
		assertFalse(hasMaxNeighborsForColor(counts, WHITE));
		// Add some neighbors
		for (int i = 1; i <= MAX_NEIGHBORS; i++) {
			counts += NEIGHBOR_INCREMENT[BLACK];
			assertEquals(i, extractNeighborCount(counts, BLACK));
			assertEquals(0, extractNeighborCount(counts, WHITE));
			assertEquals(4 - i, extractNeighborCount(counts, VACANT));
		}
		// Test revised counts
		assertTrue(hasMaxNeighborsForColor(counts, BLACK));
		assertFalse(hasMaxNeighborsForColor(counts, WHITE));
	}

}
