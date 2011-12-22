package orego.book;

import static orego.core.Coordinates.BOARD_WIDTH;
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
		if (BOARD_WIDTH == 19) {
			int p = builder.rotate90(at("d4"));
			assertEquals(at("q4"), p);
		} else {
			int p = builder.rotate90(at("b3"));
			assertEquals(at("g2"), p);
		}

	}

	@Test
	public void testRotate902() {
		if (BOARD_WIDTH == 19) {
			int p2 = builder.rotate90(at("e18"));
			assertEquals(at("b5"), p2);
		} else {
			int p = builder.rotate90(at("g2"));
			assertEquals(at("h7"), p);
		}
	}

	@Test
	public void testRotate903() {
		if (BOARD_WIDTH == 19) {
			int p3 = builder.rotate90(at("m11"));
			assertEquals(at("j12"), p3);
		} else {
			int p = builder.rotate90(at("g8"));
			assertEquals(at("b7"), p);
		}
	}

	@Test
	public void testRotate904() {
		if (BOARD_WIDTH == 19) {
			int p4 = builder.rotate90(at("r6"));
			assertEquals(at("o17"), p4);
		} else {
			int p = builder.rotate90(at("c6"));
			assertEquals(at("d3"), p);
		}
	}

	@Test
	public void testRotate180() {
		if (BOARD_WIDTH == 19) {
			int p = builder.rotate180(at("d4"));
			assertEquals(at("q16"), p);
		} else {
			int p = builder.rotate180(at("b3"));
			assertEquals(at("h7"), p);
		}
	}

	@Test
	public void testRotate1802() {
		if (BOARD_WIDTH == 19) {
			int p = builder.rotate180(at("e18"));
			assertEquals(at("p2"), p);
		} else {
			int p = builder.rotate180(at("g2"));
			assertEquals(at("c8"), p);
		}
	}

	@Test
	public void testRotate1803() {
		if (BOARD_WIDTH == 19) {
			int p = builder.rotate180(at("m11"));
			assertEquals(at("h9"), p);
		} else {
			int p = builder.rotate180(at("g8"));
			assertEquals(at("c2"), p);
		}
	}

	@Test
	public void testRotate1804() {
		if (BOARD_WIDTH == 19) {
			int p = builder.rotate180(at("r6"));
			assertEquals(at("c14"), p);
		} else {
			int p = builder.rotate180(at("c6"));
			assertEquals(at("g4"), p);
		}
	}

	@Test
	public void testRotate270() {
		if (BOARD_WIDTH == 19) {
			int p = builder.rotate270(at("d4"));
			assertEquals(at("d16"), p);
		} else {
			int p = builder.rotate270(at("b3"));
			assertEquals(at("c8"), p);
		}
	}

	@Test
	public void testRotate2702() {
		if (BOARD_WIDTH == 19) {
			int p = builder.rotate270(at("e18"));
			assertEquals(at("s15"), p);
		} else {
			int p = builder.rotate270(at("g2"));
			assertEquals(at("b3"), p);
		}
	}

	@Test
	public void testRotate2703() {
		if (BOARD_WIDTH == 19) {
			int p = builder.rotate270(at("m11"));
			assertEquals(at("l8"), p);
		} else {
			int p = builder.rotate270(at("g8"));
			assertEquals(at("h3"), p);
		}
	}

	@Test
	public void testRotate2704() {
		if (BOARD_WIDTH == 19) {
			int p = builder.rotate270(at("r6"));
			assertEquals(at("f3"), p);
		} else {
			int p = builder.rotate270(at("c6"));
			assertEquals(at("f7"), p);
		}
	}

	@Test
	public void testReflectA() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectA(at("d4"));
			assertEquals(at("q16"), p);
		} else {
			int p = builder.reflectA(at("b3"));
			assertEquals(at("g8"), p);
		}
	}

	@Test
	public void testReflectA2() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectA(at("e18"));
			assertEquals(at("b15"), p);
		} else {
			int p = builder.reflectA(at("g2"));
			assertEquals(at("h3"), p);
		}
	}

	@Test
	public void testReflectA3() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectA(at("m11"));
			assertEquals(at("j8"), p);
		} else {
			int p = builder.reflectA(at("g8"));
			assertEquals(at("b3"), p);
		}
	}

	@Test
	public void testReflectA4() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectA(at("r6"));
			assertEquals(at("o3"), p);
		} else {
			int p = builder.reflectA(at("c6"));
			assertEquals(at("d7"), p);
		}
	}

	@Test
	public void testReflectB() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectB(at("d4"));
			assertEquals(at("q4"), p);
		} else {
			int p = builder.reflectB(at("b3"));
			assertEquals(at("h3"), p);
		}
	}

	@Test
	public void testReflectB2() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectB(at("e18"));
			assertEquals(at("p18"), p);
		} else {
			int p = builder.reflectB(at("g2"));
			assertEquals(at("c2"), p);
		}
	}

	@Test
	public void testReflectB3() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectB(at("m11"));
			assertEquals(at("h11"), p);
		} else {
			int p = builder.reflectB(at("g8"));
			assertEquals(at("c8"), p);
		}
	}

	@Test
	public void testReflectB4() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectB(at("r6"));
			assertEquals(at("c6"), p);
		} else {
			int p = builder.reflectB(at("c6"));
			assertEquals(at("g6"), p);
		}
	}

	@Test
	public void testReflectC() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectC(at("g4"));
			assertEquals(at("d7"), p);
		} else {
			int p = builder.reflectC(at("b3"));
			assertEquals(at("c2"), p);
		}
	}

	@Test
	public void testReflectC2() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectC(at("e18"));
			assertEquals(at("s5"), p);
		} else {
			int p = builder.reflectC(at("g2"));
			assertEquals(at("b7"), p);
		}
	}

	@Test
	public void testReflectC3() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectC(at("m11"));
			assertEquals(at("l12"), p);
		} else {
			int p = builder.reflectC(at("g8"));
			assertEquals(at("h7"), p);
		}
	}

	@Test
	public void testReflectC4() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectC(at("r6"));
			assertEquals(at("f17"), p);
		} else {
			int p = builder.reflectC(at("c6"));
			assertEquals(at("f3"), p);
		}
	}

	@Test
	public void testReflectD() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectD(at("g4"));
			assertEquals(at("g16"), p);
		} else {
			int p = builder.reflectD(at("b3"));
			assertEquals(at("b7"), p);
		}
	}

	@Test
	public void testReflectD2() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectD(at("e18"));
			assertEquals(at("e2"), p);
		} else {
			int p = builder.reflectD(at("g2"));
			assertEquals(at("g8"), p);
		}
	}

	@Test
	public void testReflectD3() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectD(at("m11"));
			assertEquals(at("m9"), p);
		} else {
			int p = builder.reflectD(at("g8"));
			assertEquals(at("g2"), p);
		}
	}

	@Test
	public void testReflectD4() {
		if (BOARD_WIDTH == 19) {
			int p = builder.reflectD(at("r6"));
			assertEquals(at("r14"), p);
		} else {
			int p = builder.reflectD(at("c6"));
			assertEquals(at("c4"), p);
		}
	}

}
