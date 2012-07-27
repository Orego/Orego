package orego.patternanalyze;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class DynamicPatternTest {

	Board board;
	DynamicPattern pattern;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
	}

	@Test
	public void test() {
		String[] problem;
		if (BOARD_WIDTH == 19) {
			problem = new String[] {
					"...................",//19
					"...................",//18
					"...................",//17
					"...................",//16
					"...................",//15
					"...................",//14
					"...................",//13
					"...................",//12
					"...................",//11
					"...................",//10
					".....O.............",//9
					"....###............",//8
					"...#OO##...........",//7
					"..###.###..........",//6
					"...##O##...........",//5
					"....##O............",//4
					".....O.............",//3
					"...................",//2
					"..................."//1
				  // ABCDEFGHJKLMNOPQRST
				};
			board.setUpProblem(BLACK, problem);
			pattern = new DynamicPattern(at("f6"), board, 24);
			assertEquals(WHITE, pattern.getColorFromPosition(0, 0));
			assertEquals(WHITE, pattern.getColorFromPosition(0, 19));
			assertEquals(WHITE, pattern.getColorFromPosition(1, 16));
			assertEquals(WHITE, pattern.getColorFromPosition(2, 12));
			assertEquals(WHITE, pattern.getColorFromPosition(3, 15));
			assertEquals(WHITE, pattern.getColorFromPosition(4, 18));
			assertEquals(WHITE, pattern.getColorFromPosition(5, 14));
			assertEquals(WHITE, pattern.getColorFromPosition(6, 13));
			assertEquals(WHITE, pattern.getColorFromPosition(7, 17));
			assertEquals(WHITE, pattern.getColorFromPosition(1, 5));
		}
	}
	
	@Test
	public void testMatch() {
		String[] problem;
		if (BOARD_WIDTH == 19) {
			problem = new String[] {
					"...................",//19
					"..............#....",//18
					".............###...",//17
					"............OO#O#..",//16
					"...........O#O.O#O.",//15
					"............#O#O#..",//14
					".............###...",//13
					"..............#....",//12
					"...................",//11
					"...................",//10
					".....O.............",//9
					"....###............",//8
					"...#OOO#...........",//7
					"..###.###..........",//6
					"...#OOO#...........",//5
					"....##O............",//4
					".....O.............",//3
					"...................",//2
					"..................."//1
				  // ABCDEFGHJKLMNOPQRST
				};
			board.setUpProblem(BLACK, problem);
			DynamicPattern pattern1 = new DynamicPattern(at("f6"), board, 24);
			DynamicPattern pattern2 = new DynamicPattern(at("p15"), board, 24);
			assertTrue(pattern1.match(pattern1));
			assertTrue(pattern1.match(pattern2));
		}
	}

}
