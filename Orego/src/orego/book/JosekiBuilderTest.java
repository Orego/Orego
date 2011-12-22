package orego.book;

import static orego.core.Coordinates.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class JosekiBuilderTest {

	@Test
	public void testIsInCorner() {
		if (BOARD_WIDTH == 19) {
			assertEquals(true, JosekiBookBuilder.isInCorner(at("s15"), 1));
			assertEquals(false, JosekiBookBuilder.isInCorner(at("b13"), 2));
			assertEquals(false, JosekiBookBuilder.isInCorner(at("b3"), 2));
			assertEquals(true, JosekiBookBuilder.isInCorner(at("s4"), 2));
			assertEquals(false, JosekiBookBuilder.isInCorner(PASS, 1));
			assertEquals(true, JosekiBookBuilder.isInCorner(at("b15"), 0));
			assertEquals(true, JosekiBookBuilder.isInCorner(at("o9"), 2));
			for (int c = 0; c < 4; c++) {
				assertEquals(false, JosekiBookBuilder.isInCorner(at("k2"), c));
				assertEquals(false, JosekiBookBuilder.isInCorner(at("k12"), c));
				assertEquals(false, JosekiBookBuilder.isInCorner(at("b10"), c));
				assertEquals(false, JosekiBookBuilder.isInCorner(at("s10"), c));
				assertEquals(false, JosekiBookBuilder.isInCorner(at("k10"), c));
			}
		}
	}

}
