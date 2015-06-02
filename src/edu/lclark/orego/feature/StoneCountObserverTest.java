package edu.lclark.orego.feature;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.score.ChineseFinalScorer;

public class StoneCountObserverTest {

	private Board board;
	
	private StoneCountObserver counter;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		counter = new StoneCountObserver(board, new ChineseFinalScorer(board, 7.5));
	}

	@Test
	public void testCount() {
		String[] diagram = {
				"##.#O",
				"OO.O.",
				"....O",
				".#...",
				".#...",
		};
		board.setUpProblem(diagram, WHITE);
		board.play("c5");
		assertEquals(2, counter.getCount(BLACK));
		assertEquals(6, counter.getCount(WHITE));
	}

	@Test
	public void testClear() {
		String[] diagram = {
				"##.#O",
				"OO.O.",
				"....O",
				".#...",
				".#...",
		};
		board.setUpProblem(diagram, WHITE);
		board.play("c5");
		assertEquals(2, counter.getCount(BLACK));
		assertEquals(6, counter.getCount(WHITE));
		board.clear();
		assertEquals(0, counter.getCount(BLACK));
		assertEquals(0, counter.getCount(WHITE));
	}
	
	@Test
	public void testMercy(){
		assertNull(counter.mercyWinner());
		String[] diagram = {
				"#####",
				"#####",
				"#####",
				"#####",
				"#.#..",
		};
		board.setUpProblem(diagram, BLACK);
		assertNull(counter.mercyWinner());
		board.play(board.getCoordinateSystem().at("e1"));
		assertEquals(BLACK, counter.mercyWinner());
		diagram = new String[] {
				"###..",
				".....",
				".....",
				"OOOOO",
				"OOOOO",
		};
		board.setUpProblem(diagram, WHITE);
		assertNull(counter.mercyWinner());
		board.play(board.getCoordinateSystem().at("a3"));
		assertEquals(WHITE, counter.mercyWinner());	
	}

	@Test
	public void testMercyAfterCopying() {
		Board boardCopy = new Board(5);
		StoneCountObserver counterCopy = new StoneCountObserver(boardCopy, new ChineseFinalScorer(boardCopy, 7.5));
		String[] diagram = {
				"#####",
				"#####",
				"#####",
				"#####",
				"#.#.#",
		};
		board.setUpProblem(diagram, BLACK);
		assertNull(counterCopy.mercyWinner());
		counterCopy.copyDataFrom(counter);
		assertEquals(BLACK, counterCopy.mercyWinner());
	}

	@Test
	public void testMercyOnLargerBoard(){
		board = new Board(19);
		counter = new StoneCountObserver(board, new ChineseFinalScorer(board, 7.5));
		assertNull(counter.mercyWinner());
		String[] diagram = {
				"###################",
				"###################",
				"###################",
				"##########.........",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
		};
		board.setUpProblem(diagram, BLACK);
		assertNull(counter.mercyWinner());
		board.play(board.getCoordinateSystem().at("a1"));
		assertEquals(BLACK, counter.mercyWinner());
		diagram = new String[] {
				"#..................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"...................",
				"OOOOOOOOOOOOOOO....",
				"OOOOOOOOOOOOOOOOOOO",
				"OOOOOOOOOOOOOOOOOOO",
		};
		board.setUpProblem(diagram, WHITE);
		assertNull(counter.mercyWinner());
		board.play(board.getCoordinateSystem().at("e5"));
		assertEquals(WHITE, counter.mercyWinner());	
	}

}
