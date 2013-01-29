package orego.book;

import static org.junit.Assert.*;
import static orego.core.Coordinates.*;
import org.junit.Before;
import org.junit.Test;

public class HashMapTest {

	public SmallHashMap small;
	
	public BigHashMap<String> big;
	
	@Before
	public void setUp() throws Exception {
		small = new SmallHashMap();
		big = new BigHashMap<String>();
	}

	@Test
	public void testGetSmall() {
		assertEquals(NO_POINT, small.get(1L));
		small.put(1L, 20);
		assertEquals(20, small.get(1L));
		small.put(-4L, 30);
		assertEquals(30, small.get(-4L));
	}
	
	@Test
	public void testSmallContains() {
		assertFalse(small.containsKey(1L));
		small.put(1L, 10);
		assertTrue(small.containsKey(1L));
		small.put(-5L, 15);
		assertTrue(small.containsKey(-5L));
	}
	
	@Test
	public void testGetBig() {
		assertNull(big.get(1L));
		big.put(1L, "foo");
		assertEquals("foo", big.get(1L));
		big.put(-5L, "bar");
		assertEquals("bar", big.get(-5L));
	}

	@Test
	public void testBigContains() {
		assertFalse(big.containsKey(1L));
		big.put(1L, "foo");
		assertTrue(big.containsKey(1L));
		big.put(-5L, "bar");
		assertTrue(big.containsKey(-5L));
	}
	
	@Test
	public void testSmallGetKeys() {
		small.put(1L, 10);
		small.put(-5L, 80);
		assertArrayEquals(new long[] {0L, 1L, 0L, -5L}, small.getKeys());
	}

	@Test
	public void testBigGetKeys() {
		big.put(1L, "foo");
		assertArrayEquals(new long[] {0L, 1L}, big.getKeys());
		big.put(-5L, "bar");
		assertArrayEquals(new long[] {0L, 1L, 0L, -5L}, big.getKeys());
	}
	
	@Test
	public void testBigSize() {
		assertEquals(0, big.size());
		big.put(1L, "foo");
		assertEquals(1, big.size());
		big.put(-5L, "bar");		
		assertEquals(2, big.size());
	}
	
	@Test
	public void testSmallSize() {
		assertEquals(0, small.size());
		small.put(1L, -10);
		assertEquals(1, small.size());
		small.put(-5L, 224);		
		assertEquals(2, small.size());
	}

}
