package edu.lclark.orego.genetic;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.lclark.orego.core.Board;

public class PatternTest {

	@Test
	public void testPatternMatches() {
		Pattern pattern = new Pattern();
		Board board = new Board(5);
		board.play("a5");
		board.play("d2");
		// System.out.println(board.toString());
		int friendly = 1;
		int enemy = 0b1000000000000000000;
		int vacant = 0b1111110111110111111111110;
		assertTrue(pattern.patternMatcher((short) 21, board, friendly,
				enemy, vacant));
		assertFalse(pattern.patternMatcher((short) 21, board, enemy,
				enemy, vacant));
	}

}
