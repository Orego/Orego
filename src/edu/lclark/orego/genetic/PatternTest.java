package edu.lclark.orego.genetic;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.HistoryObserver;

public class PatternTest {

	@Test
	public void testPatternMatcherSpace() {
		Board board = new Board(9);
		Pattern pattern = new Pattern(board, new HistoryObserver(board));
		board.play("c5");
		board.play("d2");
		board.play("b2");
		board.play((short) 65);
		// System.out.println(board.toString());
		int friendly =   0b01000000000100000000000001000000;
		int enemy =               0b10000000010000000000000;
		int vacant =            0b1101011111100111110111111;
		assertEquals((short) 64, pattern.patternMatcher(friendly, enemy));
		assertEquals((short) -1, pattern.patternMatcher(friendly, enemy));
		assertNotEquals((short) 64, pattern.patternMatcher(friendly, enemy));
		assertNotEquals((short) 38, pattern.patternMatcher(friendly, enemy));
	}

	@Test
	public void testPatternMatcherTime() {
		Board board = new Board(9);
		Pattern pattern = new Pattern(board, new HistoryObserver(board));
		board.play("c5");
		board.play("d2");
		board.play("b2");
		System.out.println(board.toString());
		int ultimate = 	0b11000011010000000000000001010010;
		int penultimate = 0b1010100;
		int response = 0b1000000;
		assertEquals((short) 64, pattern.patternMatcher(ultimate));
//		assertEquals((short) -1, pattern.patternMatcher(ultimate));
//		assertNotEquals((short) 65,pattern.patternMatcher(ultimate));
	}

	@Test
	public void testSelectAndPlayMove() {
		Board board = new Board(9);
		Pattern pattern = new Pattern(board, new HistoryObserver(board));
		board.play("c5");
		board.play("d2");
		board.play("b2");
		int p1 = 0b11000011010000000000000001010010;
		int p2 = 0b1010100;
		int p3 = 0b1000000;
		assertEquals(pattern.selectAndPlayMove(p1), (short) 64);
	}

}
