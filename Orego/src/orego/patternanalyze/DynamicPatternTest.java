package orego.patternanalyze;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import static org.junit.Assert.*;

import java.io.Serializable;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class DynamicPatternTest implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
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
					"....##O............",//8
					"...#OO##...........",//7
					"..###.###..........",//6
					"...O#O#O...........",//5
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
			assertEquals(WHITE, pattern.getColorFromPosition(5, 13));
			assertEquals(WHITE, pattern.getColorFromPosition(6, 16));
			assertEquals(WHITE, pattern.getColorFromPosition(7, 14));
			assertEquals(WHITE, pattern.getColorFromPosition(1, 7));
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
			assertTrue(pattern1.match(DynamicPattern.setupPattern(at("f6"), board, 24), 24));
			assertTrue(pattern2.match(DynamicPattern.setupPattern(at("p15"), board, 24), 24));
			assertTrue(pattern1.match(DynamicPattern.setupPattern(at("p15"), board, 24), 24));
		}
	}
	
	@Test
	public void testLongToPatternString() {
		assertEquals("O##OOOOO###########OO##O:#", DynamicPattern.longToPatternString(71743133734209L, 24));
		assertEquals("O***:O", DynamicPattern.longToPatternString(253L + (long)Math.pow(2, 62), 4));
		assertEquals("***O:O", DynamicPattern.longToPatternString(127L + (long)Math.pow(2, 62), 4));
	}
	
	@Test
	public void testRotateBlock90() {
		assertEquals(223L, DynamicPattern.rotateBlock90(127L, 2));
		assertEquals(127L, DynamicPattern.rotateBlock90(253L, 2));
		assertEquals(14L, DynamicPattern.rotateBlock90(13L, 1));
		assertEquals(230L, DynamicPattern.rotateBlock90(155L, 2));
		assertEquals(33060L, DynamicPattern.rotateBlock90(4680L, 4));
	}
	
	@Test
	public void testRotate90() {
		assertEquals("***O:#", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("O***:#").getPattern()[0]), 4));
		assertEquals("O***:O", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("*O**:O").getPattern()[0]), 4));
		assertEquals("*O**:#", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("**O*:#").getPattern()[0]), 4));
		assertEquals("**O*:O", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("***O:O").getPattern()[0]), 4));
		
		assertEquals("****O***:O", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("*****O**:O").getPattern()[0]), 8));
		assertEquals("**O***O*:O", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("***O***O:O").getPattern()[0]), 8));
		
		assertEquals("********O***:O", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("*********O**:O").getPattern()[0]), 12));

		assertEquals("O*******************:#", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("*O******************:#").getPattern()[0]), 20));
		assertEquals("***O****************:#", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("O*******************:#").getPattern()[0]), 20));
		assertEquals("*****************O**:O", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("*******************O:O").getPattern()[0]), 20));
		assertEquals("*******************O:O", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("*************O******:O").getPattern()[0]), 20));
		
		assertEquals("***********************O:O", DynamicPattern.longToPatternString(DynamicPattern.rotate90(new DynamicPattern("********************O***:O").getPattern()[0]), 24));
	}
	
	@Test
	public void testMirror() {
		assertEquals("*###:O", DynamicPattern.longToPatternString(DynamicPattern.mirror(new DynamicPattern("*###:O").getPattern()[0]), 4));
		assertEquals("#*##:O", DynamicPattern.longToPatternString(DynamicPattern.mirror(new DynamicPattern("###*:O").getPattern()[0]), 4));
		assertEquals("#*##*##*:O", DynamicPattern.longToPatternString(DynamicPattern.mirror(new DynamicPattern("###*#**#:O").getPattern()[0]), 8));
		assertEquals("#*##*##*#**#:O", DynamicPattern.longToPatternString(DynamicPattern.mirror(new DynamicPattern("###*#**#**##:O").getPattern()[0]), 12));
		assertEquals("#*##*##*#**#####****:O", DynamicPattern.longToPatternString(DynamicPattern.mirror(new DynamicPattern("###*#**#**##****####:O").getPattern()[0]), 20));
		assertEquals("#*##*##*#**#####******##:O", DynamicPattern.longToPatternString(DynamicPattern.mirror(new DynamicPattern("###*#**#**##****####*##*:O").getPattern()[0]), 24));
	}

}
