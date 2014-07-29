package edu.lclark.orego.patterns;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

public class PatternFinderTest {

	Board board;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
	}

	@Test
	public void testAtariDetection() {
		String[] diagram = {
				".O#..",
				"O#O#.",
				"...#.",
				".O.#.",
				".....",
		};
		board.setUpProblem(diagram, BLACK);
		long correctHash = 0;
		correctHash ^= PatternFinder.POINT_HASHES[0][2][0];
		correctHash ^= PatternFinder.POINT_HASHES[0][1][1];
		correctHash ^= PatternFinder.POINT_HASHES[0][0][4];
		correctHash ^= PatternFinder.POINT_HASHES[0][1][5];
		correctHash ^= PatternFinder.POINT_HASHES[0][1][6];
		correctHash ^= PatternFinder.POINT_HASHES[0][3][7];
		assertEquals(correctHash, PatternFinder.getHash(board, board.getCoordinateSystem().at("c3"), 6, 0));
		correctHash=0;
		correctHash ^= PatternFinder.POINT_HASHES[0][4][0];
		correctHash ^= PatternFinder.POINT_HASHES[0][2][1];
		correctHash ^= PatternFinder.POINT_HASHES[0][3][2];
		correctHash ^= PatternFinder.POINT_HASHES[0][4][3];
		correctHash ^= PatternFinder.POINT_HASHES[0][4][4];
		correctHash ^= PatternFinder.POINT_HASHES[0][4][5];
		correctHash ^= PatternFinder.POINT_HASHES[0][0][6];
		correctHash ^= PatternFinder.POINT_HASHES[0][4][7];
		assertEquals(correctHash, PatternFinder.getHash(board, board.getCoordinateSystem().at("a5"), 3, 0));
		
	}
	
	@Test
	public void bigTest(){
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
				"...#####...........",// 6
				"...O#..O.......#...",// 5
				"...O..OO.........O.",// 4
				"...O...O...........",// 3
				"...#####...........",// 2
				"..................."// 1
	          // ABCDEFGHJKLMNOPQRST
		};
		board = new Board(19);
		board.setUpProblem(problem, BLACK);
		assertEquals(PatternFinder.getHash(board, board.getCoordinateSystem().at("R4"), 2, 0),
				PatternFinder.getHash(board, board.getCoordinateSystem().at("F4"), 2, 0));
		assertNotEquals(PatternFinder.getHash(board, board.getCoordinateSystem().at("R4"), 3, 0),
				PatternFinder.getHash(board, board.getCoordinateSystem().at("F4"), 3, 0));
	}

}
