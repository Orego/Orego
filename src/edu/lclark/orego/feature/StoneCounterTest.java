package edu.lclark.orego.feature;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class StoneCounterTest {

	private Board board;
	
	private CoordinateSystem coords;
	
	private StoneCounter counter;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		counter = new StoneCounter(board);
	}

	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
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
		board.play(at("c5"));
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
		board.play(at("c5"));
		assertEquals(2, counter.getCount(BLACK));
		assertEquals(6, counter.getCount(WHITE));
		board.clear();
		assertEquals(0, counter.getCount(BLACK));
		assertEquals(0, counter.getCount(WHITE));
	}
	
	@Test
	public void testMercy(){
		Board board11 = new Board(11);
		StoneCounter mercyCounter = new StoneCounter(board11);
		assertEquals(null, mercyCounter.mercyWinner());
		String[] diagram = {
				"#########..",
				"#########..",
				"########...",
				"#########..",
				"#####......",
				"#####......",
				"#####......",
				"#####......",
				"#####......",
				"#####...OO.",
				"#####......",
		};
		board11.setUpProblem(diagram, WHITE);
		board11.play(board11.getCoordinateSystem().at("f1"));
		assertEquals(BLACK, mercyCounter.mercyWinner());
	}

}
