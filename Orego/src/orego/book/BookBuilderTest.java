package orego.book;


import static orego.core.Coordinates.at;
import static org.junit.Assert.assertEquals;

import org.junit.Before;

import org.junit.Test;

public class BookBuilderTest {

	private BookBuilder builder;

	@Before
	public void setup() {
		builder = new FusekiBookBuilder(2);
	}

	@Test
	public void testRotate90() {
			int p = builder.rotate90(at("d4"));
			assertEquals(at("q4"), p);
	}

	@Test
	public void testRotate902() {
			int p2 = builder.rotate90(at("e18"));
			assertEquals(at("b5"), p2);
	}

	@Test
	public void testRotate903() {
			int p3 = builder.rotate90(at("m11"));
			assertEquals(at("j12"), p3);
	}

	@Test
	public void testRotate904() {
			int p4 = builder.rotate90(at("r6"));
			assertEquals(at("o17"), p4);
	}

	@Test
	public void testRotate180() {
			int p = builder.rotate180(at("d4"));
			assertEquals(at("q16"), p);
	}

	@Test
	public void testRotate1802() {
			int p = builder.rotate180(at("e18"));
			assertEquals(at("p2"), p);
	}

	@Test
	public void testRotate1803() {
			int p = builder.rotate180(at("m11"));
			assertEquals(at("h9"), p);
	}

	@Test
	public void testRotate1804() {
			int p = builder.rotate180(at("r6"));
			assertEquals(at("c14"), p);
	}

	@Test
	public void testRotate270() {
			int p = builder.rotate270(at("d4"));
			assertEquals(at("d16"), p);
	}

	@Test
	public void testRotate2702() {
			int p = builder.rotate270(at("e18"));
			assertEquals(at("s15"), p);
	}

	@Test
	public void testRotate2703() {
			int p = builder.rotate270(at("m11"));
			assertEquals(at("l8"), p);
	}

	@Test
	public void testRotate2704() {
			int p = builder.rotate270(at("r6"));
			assertEquals(at("f3"), p);
	}

	@Test
	public void testReflectA() {
			int p = builder.reflectA(at("d4"));
			assertEquals(at("q16"), p);
	}

	@Test
	public void testReflectA2() {
			int p = builder.reflectA(at("e18"));
			assertEquals(at("b15"), p);
	}

	@Test
	public void testReflectA3() {
			int p = builder.reflectA(at("m11"));
			assertEquals(at("j8"), p);
	}

	@Test
	public void testReflectA4() {
			int p = builder.reflectA(at("r6"));
			assertEquals(at("o3"), p);
	}

	@Test
	public void testReflectB() {
			int p = builder.reflectB(at("d4"));
			assertEquals(at("q4"), p);
	}

	@Test
	public void testReflectB2() {
			int p = builder.reflectB(at("e18"));
			assertEquals(at("p18"), p);
	}

	@Test
	public void testReflectB3() {
			int p = builder.reflectB(at("m11"));
			assertEquals(at("h11"), p);
	}

	@Test
	public void testReflectB4() {
			int p = builder.reflectB(at("r6"));
			assertEquals(at("c6"), p);
	}

	@Test
	public void testReflectC() {
			int p = builder.reflectC(at("g4"));
			assertEquals(at("d7"), p);
	}

	@Test
	public void testReflectC2() {
			int p = builder.reflectC(at("e18"));
			assertEquals(at("s5"), p);
	}

	@Test
	public void testReflectC3() {
			int p = builder.reflectC(at("m11"));
			assertEquals(at("l12"), p);
	}

	@Test
	public void testReflectC4() {
			int p = builder.reflectC(at("r6"));
			assertEquals(at("f17"), p);
	}

	@Test
	public void testReflectD() {
			int p = builder.reflectD(at("g4"));
			assertEquals(at("g16"), p);
	}

	@Test
	public void testReflectD2() {
			int p = builder.reflectD(at("e18"));
			assertEquals(at("e2"), p);
	}

	@Test
	public void testReflectD3() {
			int p = builder.reflectD(at("m11"));
			assertEquals(at("m9"), p);
	}

	@Test
	public void testReflectD4() {
			int p = builder.reflectD(at("r6"));
			assertEquals(at("r14"), p);
	}

}
