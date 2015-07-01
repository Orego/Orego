package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.genetic.Pattern.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.HistoryObserver;

public class PatternTest {

	private Board board;
	
	private Pattern pattern;
	
	private short at(String label) {
		return board.getCoordinateSystem().at(label);
	}

	@Before
	public void setUp() {
		board = new Board(9);
		pattern = new Pattern(board, new HistoryObserver(board));		
	}
	
	@Test
	public void testMakeRule() {
		int[] rule = Pattern.makeRule(
				".....",
				".#...",
				"..!O.",
				".....",
				"#.O.."
		);
		assertEquals(0b0000100000000000001000000, rule[0]);
		assertEquals(0b0010000000010000000000000, rule[1]);
		assertEquals(0b1101011111100111110111111, rule[2]);
	}

	// TODO Beautify these tests
	@Test
	public void testPatternMatcherSpace() {
		board.play("c5");
		board.play("d2");
		board.play("b2");
		board.play("e4");
		System.out.println(board);
		int friendly = 0b0000100000000000001000000 | YES;
		int enemy =    0b0010000000010000000000000;
		int vacant =   0b1101011111100111110111111;
		assertEquals(at("d4"), pattern.patternMatcher(friendly, enemy, vacant));
		System.out.println(board);
		assertEquals(NO_POINT, pattern.patternMatcher(friendly, enemy, vacant));
		System.out.println(board);
		assertNotEquals(at("d4"), pattern.patternMatcher(friendly, enemy, vacant));
	}

	@Test
	public void testPatternMatcherTime() {
		board.play("c5");
		board.play("d2");
		board.play("b2");
		// System.out.println(board.toString());
		int ultimate = 0b11000011010000000000000001010010;
		int penultimate = 0b1010100;
		int response = 0b1000000;
		assertEquals(pattern.patternMatcher(ultimate, penultimate, response),
				(short) 64);
		assertEquals(pattern.patternMatcher(ultimate, penultimate, response),
				(short) NO_POINT);
		assertNotEquals(
				pattern.patternMatcher(ultimate, penultimate, response + 1),
				(short) 65);
	}

	@Test
	public void testSelectAndPlayMove() {
		board.play("c5");
		board.play("d2");
		board.play("b2");
		int p1 = 0b11000011010000000000000001010010;
		int p2 = 0b1010100;
		int p3 = 0b1000000;
		assertEquals(pattern.selectAndPlayMove(p1, p2, p3), (short) 64);
	}

	// TODO More tests!
	// Wild cards?
	// Edges?
	// 1 vs 2 move history in time patterns?
	// No (avoid) rules?
	// Friendly vs enemy (turn to play)
	
}
