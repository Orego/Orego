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
		correctHash ^= PatternFinder.POINT_HASHES[2][2];
		correctHash ^= PatternFinder.POINT_HASHES[2][3];
		assertEquals(correctHash, PatternFinder.getHash(board, board.getCoordinateSystem().at("c3"), 4));
		correctHash = 0;
		correctHash ^= PatternFinder.POINT_HASHES[2][0];
		correctHash ^= PatternFinder.POINT_HASHES[2][1];
		correctHash ^= PatternFinder.POINT_HASHES[3][2];
		correctHash ^= PatternFinder.POINT_HASHES[3][3];
		correctHash ^= PatternFinder.POINT_HASHES[3][4];
		correctHash ^= PatternFinder.POINT_HASHES[1][5];
		correctHash ^= PatternFinder.POINT_HASHES[3][6];
		correctHash ^= PatternFinder.POINT_HASHES[3][7];
		assertEquals(correctHash, PatternFinder.getHash(board, board.getCoordinateSystem().at("a1"), 8));
	}

}
