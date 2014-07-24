package edu.lclark.orego.patterns;

import static edu.lclark.orego.core.StoneColor.BLACK;
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
	public void test() {
		String[] diagram = {
				".....",
				"..O..",
				"...#.",
				".O.#.",
				".....",
		};
		board.setUpProblem(diagram, WHITE);
		long correctHash = 0;
		correctHash ^= PatternFinder.POINT_HASHES[1][0];
		correctHash ^= PatternFinder.POINT_HASHES[0][1];
		correctHash ^= PatternFinder.POINT_HASHES[0][6];
		correctHash ^= PatternFinder.POINT_HASHES[1][7];
		assertEquals(correctHash, PatternFinder.getHash(board, board.getCoordinateSystem().at("c3"), 4));
		correctHash = 0;
		correctHash ^= PatternFinder.POINT_HASHES[3][2];
		correctHash ^= PatternFinder.POINT_HASHES[3][3];
		correctHash ^= PatternFinder.POINT_HASHES[3][4];
		correctHash ^= PatternFinder.POINT_HASHES[1][5];
		correctHash ^= PatternFinder.POINT_HASHES[3][6];
		correctHash ^= PatternFinder.POINT_HASHES[3][7];
		assertEquals(correctHash, PatternFinder.getHash(board, board.getCoordinateSystem().at("a1"), 1));
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
		assertEquals(PatternFinder.getHash(board, board.getCoordinateSystem().at("R4"), 2),
				PatternFinder.getHash(board, board.getCoordinateSystem().at("F4"), 2));
		assertNotEquals(PatternFinder.getHash(board, board.getCoordinateSystem().at("R4"), 3),
				PatternFinder.getHash(board, board.getCoordinateSystem().at("F4"), 3));
	}

}
