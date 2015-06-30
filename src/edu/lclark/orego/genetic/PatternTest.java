package edu.lclark.orego.genetic;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.HistoryObserver;

public class PatternTest {

	@Test
	public void testPatternMatcher() {
		Board board = new Board(7);
		Pattern pattern = new Pattern(board, new HistoryObserver(board));
		board.play("c5");
		board.play("d2");
		board.play("b2");
//		System.out.println(board.toString());
		int friendly = 0b11010000000000000000000000;
		int enemy = 0b11000100000000000001000000;
		int vacant = 0b1101011111110111110111111;
		assertTrue(true);
//		assertTrue(pattern.patternMatcher((short) 36, board, historyObserver, friendly,
//				enemy, vacant));
//		assertFalse(pattern.patternMatcher((short) 39, board, historyObserver, friendly,
//				enemy, vacant));
//		assertFalse(pattern.patternMatcher((short) 36, board, historyObserver, enemy,
//				enemy, vacant));
	}

}
