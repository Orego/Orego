package edu.lclark.orego.score;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import static edu.lclark.orego.core.StoneColor.*;

public class ChineseFinalScorerTest {

	private ChineseFinalScorer scorer;
	
	private Board board;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		scorer = new ChineseFinalScorer(board, 0);
	}

	@Test
	public void testScoreCorner() {
		String[] diagram = {
				"#####",
				"#####",
				"#####",
				"#####",
				"####.",
		};
		board.setUpProblem(diagram, BLACK);
		assertEquals(25, scorer.score(), 0.01);
	}
	
	@Test
	public void testScore() {
		String[] diagram = {
				"###..",
				"#..#.",
				".##OO",
				".OO.O",
				".OOO.",
		};
		board.setUpProblem(diagram, BLACK);
		assertEquals(-1, scorer.score(), 0.01);
	}
	
	@Test
	public void testScoreOnePoint() {
		String[] diagram = {
				"..#..",
				".#.#.",
				"..#..",
				".....",
				"O....",
		};
		board.setUpProblem(diagram, BLACK);
		assertEquals(4, scorer.score(), 0.01);
	}

	@Test
	public void testBug1() {
		board = new Board(9);
		scorer = new ChineseFinalScorer(board, 7.5);
		String[] diagram = {
				"####.####",
				"##.######",
				"#.#.#.#.#",
				"#########",
				".#.#####O",
				"######O#O",
				"#OO#OOOOO",
				"#OOOO.OOO",
				"OOOO.OOO.",
		};
		board.setUpProblem(diagram, BLACK);
		assertEquals(BLACK, scorer.winner());
		assertEquals(19.5, scorer.score(), 0.01);		
	}

}
