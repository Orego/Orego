package edu.lclark.orego.patterns;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.CoordinateSystem.*;
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
		correctHash ^= PatternFinder.POINT_HASHES[3][0];
		correctHash ^= PatternFinder.POINT_HASHES[2][1];
		correctHash ^= PatternFinder.POINT_HASHES[0][4];
		correctHash ^= PatternFinder.POINT_HASHES[2][5];
		correctHash ^= PatternFinder.POINT_HASHES[2][6];
		correctHash ^= PatternFinder.POINT_HASHES[5][7];
		assertEquals(correctHash, PatternFinder.getHash(board, board.getCoordinateSystem().at("c3"), 6, NO_POINT));
		correctHash=0;
		correctHash ^= PatternFinder.POINT_HASHES[9][0];
		correctHash ^= PatternFinder.POINT_HASHES[3][1];
		correctHash ^= PatternFinder.POINT_HASHES[4][2];
		correctHash ^= PatternFinder.POINT_HASHES[9][3];
		correctHash ^= PatternFinder.POINT_HASHES[9][4];
		correctHash ^= PatternFinder.POINT_HASHES[9][5];
		correctHash ^= PatternFinder.POINT_HASHES[0][6];
		correctHash ^= PatternFinder.POINT_HASHES[9][7];
		assertEquals(correctHash, PatternFinder.getHash(board, board.getCoordinateSystem().at("a5"), 3, NO_POINT));
		
	}
	
	@Test
	public void testLastMove() {
		String[] diagram = {
				".O#..",
				"O#O#.",
				"...#.",
				".O.#.",
				".....",
		};
		board.setUpProblem(diagram, BLACK);
		long correctHash = 0;
		correctHash ^= PatternFinder.POINT_HASHES[6][0];
		correctHash ^= PatternFinder.POINT_HASHES[2][1];
		correctHash ^= PatternFinder.POINT_HASHES[0][4];
		correctHash ^= PatternFinder.POINT_HASHES[2][5];
		correctHash ^= PatternFinder.POINT_HASHES[2][6];
		correctHash ^= PatternFinder.POINT_HASHES[5][7];
		assertEquals(correctHash, PatternFinder.getHash(board, board.getCoordinateSystem().at("c3"), 6, board.getCoordinateSystem().at("c4")));
		correctHash=0;
		correctHash ^= PatternFinder.POINT_HASHES[9][0];
		correctHash ^= PatternFinder.POINT_HASHES[3][1];
		correctHash ^= PatternFinder.POINT_HASHES[4][2];
		correctHash ^= PatternFinder.POINT_HASHES[9][3];
		correctHash ^= PatternFinder.POINT_HASHES[9][4];
		correctHash ^= PatternFinder.POINT_HASHES[9][5];
		correctHash ^= PatternFinder.POINT_HASHES[0][6];
		correctHash ^= PatternFinder.POINT_HASHES[9][7];
		assertEquals(correctHash, PatternFinder.getHash(board, board.getCoordinateSystem().at("a5"), 3, board.getCoordinateSystem().at("c4")));
		
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
		assertEquals(PatternFinder.getHash(board, board.getCoordinateSystem().at("R4"), 2, NO_POINT),
				PatternFinder.getHash(board, board.getCoordinateSystem().at("F4"), 2, NO_POINT));
		assertNotEquals(PatternFinder.getHash(board, board.getCoordinateSystem().at("R4"), 3, NO_POINT),
				PatternFinder.getHash(board, board.getCoordinateSystem().at("F4"), 3, NO_POINT));
	}

}
