package edu.lclark.orego.patterns;

import static edu.lclark.orego.patterns.PatternFinder.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

@SuppressWarnings("static-method")
public class PatternFinderTest {

	Board board;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
	}

	@Test
	public void testDistanceTo() {
		assertEquals(5.0, distanceTo(new short[] {3, 4}), 0.001);
	}

	@Test
	public void testSizes() {
		int[] sizes = getPatternSizes();
		assertEquals(0, sizes[0]);
		assertEquals(4, sizes[1]);
		assertEquals(8, sizes[2]);
		assertEquals(12, sizes[3]);
		assertEquals(20, sizes[4]);
		assertEquals(24, sizes[5]);
	}
	
	@Test
	public void testOffsets() {
		short[][] offsets = getOffsets();
		assertArrayEquals(new short[] {-1, 0}, offsets[0]);
		assertArrayEquals(new short[] {0, -1}, offsets[1]);
		assertArrayEquals(new short[] {0, 1}, offsets[2]);
		assertArrayEquals(new short[] {1, 0}, offsets[3]);
		assertArrayEquals(new short[] {-1, -1}, offsets[4]);
		assertNotNull(offsets[offsets.length - 1]);
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
		correctHash ^= POINT_HASHES[ENEMY_IN_ATARI][0];
		correctHash ^= POINT_HASHES[FRIENDLY_3_OR_MORE_LIBERTIES][2];
		assertEquals(correctHash, getHash(board, board.getCoordinateSystem().at("c3"), 2, NO_POINT));
		correctHash ^= POINT_HASHES[FRIENDLY_IN_ATARI][4];
		correctHash ^= POINT_HASHES[FRIENDLY_3_OR_MORE_LIBERTIES][5];
		correctHash ^= POINT_HASHES[ENEMY_3_OR_MORE_LIBERTIES][6];
		correctHash ^= POINT_HASHES[FRIENDLY_3_OR_MORE_LIBERTIES][7];
		assertEquals(correctHash, getHash(board, board.getCoordinateSystem().at("c3"), 5, NO_POINT));
		assertEquals(correctHash, getHash(board, board.getCoordinateSystem().at("c3"), 6, NO_POINT));
		correctHash=0;
		correctHash ^= POINT_HASHES[OFF_BOARD][0];
		correctHash ^= POINT_HASHES[OFF_BOARD][1];
		correctHash ^= POINT_HASHES[ENEMY_IN_ATARI][2];
		correctHash ^= POINT_HASHES[ENEMY_2_LIBERTIES][3];
		assertEquals(correctHash, getHash(board, board.getCoordinateSystem().at("a5"), 2, NO_POINT));
		correctHash ^= POINT_HASHES[OFF_BOARD][4];
		correctHash ^= POINT_HASHES[OFF_BOARD][5];
		correctHash ^= POINT_HASHES[OFF_BOARD][6];
		correctHash ^= POINT_HASHES[FRIENDLY_IN_ATARI][7];
		assertEquals(correctHash, getHash(board, board.getCoordinateSystem().at("a5"), 3, NO_POINT));
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
		correctHash ^= POINT_HASHES[ENEMY_IN_ATARI + LAST_MOVE_INCREASE][0];
		correctHash ^= POINT_HASHES[FRIENDLY_3_OR_MORE_LIBERTIES][2];
		assertEquals(correctHash, getHash(board, board.getCoordinateSystem().at("c3"), 2, board.getCoordinateSystem().at("c4")));		
		correctHash ^= POINT_HASHES[FRIENDLY_IN_ATARI][4];
		correctHash ^= POINT_HASHES[FRIENDLY_3_OR_MORE_LIBERTIES][5];
		correctHash ^= POINT_HASHES[ENEMY_3_OR_MORE_LIBERTIES][6];
		correctHash ^= POINT_HASHES[FRIENDLY_3_OR_MORE_LIBERTIES][7];
		assertEquals(correctHash, getHash(board, board.getCoordinateSystem().at("c3"), 6, board.getCoordinateSystem().at("c4")));		
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
		assertEquals(getHash(board, board.getCoordinateSystem().at("R4"), 2, NO_POINT),
				getHash(board, board.getCoordinateSystem().at("F4"), 2, NO_POINT));
		assertNotEquals(getHash(board, board.getCoordinateSystem().at("R4"), 3, NO_POINT),
				getHash(board, board.getCoordinateSystem().at("F4"), 3, NO_POINT));
	}

}
