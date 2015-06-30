package edu.lclark.orego.genetic;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.lclark.orego.core.Board;

public class PatternTest {

	@Test
	public void testPatternMatcher() {
		Pattern pattern = new Pattern();
		Board board = new Board(7);
		board.play("c5");
		board.play("d2");
		board.play("b2");
//		System.out.println(board.toString());
		int friendly = 0b11010000000000000000000000;
		int enemy = 0b11000100000000000001000000;
		int vacant = 0b1101011111110111110111111;
		assertTrue(pattern.patternMatcher((short) 36, board, friendly,
				enemy, vacant));
		assertFalse(pattern.patternMatcher((short) 39, board, friendly,
				enemy, vacant));
		assertFalse(pattern.patternMatcher((short) 36, board, enemy,
				enemy, vacant));
	}

}
