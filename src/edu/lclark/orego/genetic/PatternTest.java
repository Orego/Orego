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
		board.play((short)65);
//		System.out.println(board.toString());
		int friendly = 0b01000000000100000000000001000000;
		int enemy = 0b10000000010000000000000;
		int vacant = 0b1101011111100111110111111;
		assertEquals(pattern.patternMatcher(friendly,
				enemy, vacant), (short)64);
		assertEquals(pattern.patternMatcher(friendly,
				enemy, vacant), (short)-1);
		assertNotEquals(pattern.patternMatcher(friendly,
				enemy, vacant), (short)64);
		assertNotEquals(pattern.patternMatcher(friendly,
				enemy, vacant), (short)38);
	}
	

	@Test
	public void testPatternMatcherTime() {
		Board board = new Board(9);
		Pattern pattern = new Pattern(board, new HistoryObserver(board));
		board.play("c5");
		board.play("d2");
		board.play("b2");
//		System.out.println(board.toString());
		int ultimate = 0b11000011010000000000000001010010;
		int penultimate = 0b1010100;
		int response = 0b1000000;
		assertEquals(pattern.patternMatcher(ultimate,
				penultimate, response), (short)64);
		assertEquals(pattern.patternMatcher(ultimate,
				penultimate, response), (short)-1);
		assertNotEquals(pattern.patternMatcher(ultimate,
				penultimate, response+1), (short)65);
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
		assertEquals(pattern.selectAndPlayMove(p1, p2, p3), (short)64);
	}

}
