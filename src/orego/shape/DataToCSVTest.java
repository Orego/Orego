package orego.shape;

import static orego.core.Board.PATTERN_ZOBRIST_HASHES;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Test;

public class DataToCSVTest {
	
	@Test
	public void testFindPatternHash() {
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
		// Empty pattern
		assertEquals(0L,DataToCSV.keyToHash(".................................................................................",4));
		// Radius 1 pattern around d2
		long correct = 0L;
		correct ^= PATTERN_ZOBRIST_HASHES[1][WHITE][6];
		assertEquals(correct, DataToCSV.keyToHash("......O..", 1));
		// Radius 2 pattern around b2
		correct = 0L;
		correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][0];
		correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][5];
		correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][10];
		correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][15];
		correct ^= PATTERN_ZOBRIST_HASHES[2][WHITE][16];
		correct ^= PATTERN_ZOBRIST_HASHES[2][BLACK][17];
		correct ^= PATTERN_ZOBRIST_HASHES[2][WHITE][18];
		correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][20];
		correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][21];
		correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][22];
		correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][23];
		correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][24];
		assertEquals(correct, DataToCSV.keyToHash("*....*....*....*O#O.*****", 2));
		
		// Radius 2 pattern around t1
		correct = 0L;
		for (int p : new int[] {3, 4, 8, 9}) {			
			correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][p];
		}
		for (int p = 13; p < 25; p++) {
			correct ^= PATTERN_ZOBRIST_HASHES[2][OFF_BOARD_COLOR][p];
		}
		assertEquals(correct, DataToCSV.keyToHash("...**...**...************", 2));		
	}

}
