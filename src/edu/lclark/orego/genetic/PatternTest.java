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
		board.play("e5");
		board.play("a3");
		board.play("b5");		
//		board.play((short) 64);
		System.out.println(board.toString());
		int [] rules = Pattern.makeRule(
//				".?...",
//				"??.?.",
//				"..!..",
//				"..???",
//				"#.?..");
		
				".....",
				"O#.O.",
				"..!..",
				".....",
				"#.O..");
		int friendly = 0b010000000000010000000000001000000;
		int enemy =    0b000000000001000000000000100100000;
		System.out.println(Integer.toBinaryString(rules[0]) + "\n" + Integer.toBinaryString(rules[1]));
		assertEquals((short) 64, pattern.patternMatcher(friendly, enemy));
		System.out.println(board.toString());
//		assertEquals((short) 0, pattern.patternMatcher(friendly, enemy));
//		assertNotEquals((short) 64, pattern.patternMatcher(friendly, enemy));
//		assertNotEquals((short) 38, pattern.patternMatcher(friendly, enemy));
	}

	@Test
	public void testPatternMatcherTime() {
		Board board = new Board(9);
		Pattern pattern = new Pattern(board, new HistoryObserver(board));
		board.play("c5");
		board.play("d2");
		board.play("b2");
//		System.out.println(board.toString());
		int ultimate = 	0b11000000110000000000000001010010;
		assertEquals((short) 48, pattern.patternMatcher(ultimate, 0));
		assertNotEquals((short) 48, pattern.patternMatcher(ultimate, 0));
		assertEquals((short) 0, pattern.patternMatcher(ultimate));
		assertNotEquals((short) 65, pattern.patternMatcher(ultimate));
	}

	@Test
	public void testSelectAndPlayMove() {
		Board board = new Board(9);
		Pattern pattern = new Pattern(board, new HistoryObserver(board));
		board.play("c5");
		board.play("d2");
		board.play("b2");
//		System.out.println(board);
		int p1 = 0b11100000110000001010100001010010;
		assertEquals(pattern.selectAndPlayMove(p1, 0), (short) 48);
		assertNotEquals(pattern.selectAndPlayMove(p1, 0), (short) 48);
	}

}
