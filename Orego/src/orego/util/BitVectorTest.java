package orego.util;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class BitVectorTest {

	/** Capacity of the bit vector used in these tests. */
	public static final int CAPACITY = 500;

	private BitVector vector;

	@Before
	public void setUp() throws Exception {
		vector = new BitVector(CAPACITY);
	}

	@Test
	public void testClear() {
		for (int i = 0; i < CAPACITY; i++) {
			vector.set(i, true);
		}
		vector.clear();
		for (int i = 0; i < CAPACITY; i++) {
			assertFalse(vector.get(i));
		}

	}

	@Test
	public void testSet() {
		for (int i = 0; i < CAPACITY; i++) {
			assertFalse(vector.get(i));
			vector.set(i, true);
			assertTrue(vector.get(i));
			vector.set(i, false);
			assertFalse(vector.get(i));
		}
	}

}
