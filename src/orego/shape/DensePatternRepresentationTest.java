package orego.shape;

import static orego.core.Board.PATTERN_ZOBRIST_HASHES;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.WHITE;
import static org.junit.Assert.*;

import org.junit.Test;

public class DensePatternRepresentationTest {
	
	@Test
	public void testToString() {
		String[] problem = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"O#O................" // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		// Pretend pattern (all off board except center)
		DensePatternRepresentation offBoard = new DensePatternRepresentation (new int[]{0xffff,0xffff,0xffff,0xffff,0xffff,0xffff,0xffff,0xffff,0xffff,0xffff});
		assertEquals("****************************************.****************************************",offBoard.toString());
		// Empty pattern
		DensePatternRepresentation emptyPat = new DensePatternRepresentation (new int[] {0xaaaa,0xaaaa,0xaaaa,0xaaaa,0xaaaa,0xaaaa,0xaaaa,0xaaaa,0xaaaa,0xaaaa});
		assertEquals(".................................................................................",emptyPat.toString());
		// Radius 1 pattern around d2
		DensePatternRepresentation aroundD2 = new DensePatternRepresentation( new int[] {0xaa9a});
		assertEquals("......O..",aroundD2.toString());
		// Radius 2 pattern around b2
		DensePatternRepresentation aroundB2 = new DensePatternRepresentation(new int[] {0xeaba,0xaead,0x1bff});
		assertEquals("*....*....*....*O#O.*****", aroundB2.toString());
		
		// Radius 2 pattern around t1
		DensePatternRepresentation aroundT1 = new DensePatternRepresentation (new int[] {0xabea,0xfaff,0xffff});
		assertEquals("...**...**...************", aroundT1.toString());		
	}

}
