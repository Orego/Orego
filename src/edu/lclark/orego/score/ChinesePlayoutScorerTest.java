package edu.lclark.orego.score;

import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.core.NonStoneColor.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

public class ChinesePlayoutScorerTest {
	
	private ChinesePlayoutScorer scorer;
	
	private Board board;

	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		scorer = new ChinesePlayoutScorer(board, 7.5);
	}

	@Test
	public void testScore() {
		String[] before = {
				".#OO.",
				"###OO",
				".#.O.",
				"##OOO",
				"OOO.O",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(-13.5, scorer.score(), 0.1);
	}
	
	@Test
	public void testWinner() {
		String[] before = {
				".#OO.",
				"###OO",
				".#.O.",
				"##OOO",
				"OOO.O",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(WHITE, scorer.winner());
	}
	
	@Test
	public void testJigo() {
		scorer = new ChinesePlayoutScorer(board, 0);
		String[] before = {
				".##O.",
				"###OO",
				".#.O.",
				"##OOO",
				"##O.O",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(0, scorer.score(), 0.1);
		assertEquals(VACANT, scorer.winner());
	}

}
