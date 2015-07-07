package edu.lclark.orego.genetic;

import static edu.lclark.orego.genetic.Pattern.*;
import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class PatternTest {

	private Board board;
	
	private Pattern pattern;
	
	private CoordinateSystem coords;
	
	@Before
	public void setUp() {
		board = new Board(9);
		coords = board.getCoordinateSystem();
		pattern = new Pattern(board, new HistoryObserver(board));
	}

	private short at(String label) {
		return coords.at(label);
	}
	
//	@Test
//	public void testEdges(){
//		String[] diagram = {
//				".........",
//				".........",
//				".........",
//				".O.......",
//				"....#....",
//				".........",
//				".........",
//				"O..#.....",
//				".#O......",
//		};
//		board.setUpProblem(diagram, BLACK);
//		// Pattern is for lower left corner
//		int [] rule = Pattern.makeSpaceRule(coords.getWidth(), 1, YES,
//				".....",
//				"..O..",
//				"...#O",
//				".....",
//				".....");
//		assertEquals(at("a1"), pattern.patternMatcher(rule));
//		assertEquals(0, pattern.patternMatcher(rule));
//	}
//
//	@Test
//	public void testEdges2(){
//		String[] diagram = {
//				".O.#.....",
//				".........",
//				".........",
//				".........",
//				".........",
//				".........",
//				".........",
//				".........",
//				".........",
//		};
//		board.setUpProblem(diagram, BLACK);
//		// Pattern is on top edge, near corner
//		int [] rule = Pattern.makeSpaceRule(1, 3, YES,
//				".....",
//				".....",
//				".O.#.",
//				".....",
//				".....");
//		assertEquals(at("c9"), pattern.patternMatcher(rule));
//	}
//
//	
//	@Test
//	public void testPatternMatcherSpace() {
//		String[] diagram = {
//				".........",
//				".........",
//				".........",
//				".........",
//				".O#.O....",
//				".........",
//				"#........",
//				".#.O.....",
//				".........",
//		};
//		board.setUpProblem(diagram, BLACK);
//		// Pattern is not near an edge
//		int [] rule = Pattern.makeSpaceRule(0, 0, YES,
//				".....",
//				"O#.O.",
//				".....",
//				".....",
//				"#.O..");
//		assertEquals(at("d4"), pattern.patternMatcher(rule));
//		assertEquals(NO_POINT, pattern.patternMatcher(rule));
//		assertNotEquals(at("d4"), pattern.patternMatcher(rule));
//	}
//
//	@Test
//	public void testPatternMatcherTime() {
//		board.play("c5");
//		board.play("d2");
//		board.play("b2");
//		int ultimate = 	0b11000000110000000000000001010010;
//		assertEquals(at("h6"), pattern.patternMatcher(ultimate, 0));
//		assertNotEquals(at("h6"), pattern.patternMatcher(ultimate, 0));
//		assertEquals(NO_POINT, pattern.patternMatcher(ultimate));
//		assertEquals(NO_POINT, pattern.patternMatcher(ultimate));
//	}

	@Test
	public void testSelectAndPlayMove() {
		board.play("c5");
		board.play("d2");
		board.play("b2");
		int p1 = 0b11100000110000001010100001010010;
		pattern.setPattern(p1, 0);
		MersenneTwisterFast random = new MersenneTwisterFast();
		assertEquals(pattern.selectAndPlayOneMove(random, true), at("h6"));
		assertNotEquals(pattern.selectAndPlayOneMove(random, true), at("h6"));
	}

}
