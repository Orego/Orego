package edu.lclark.orego.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortList;
import static edu.lclark.orego.core.StoneColor.*;

public class HistoryObserverTest {

	private Board board;
	
	private CoordinateSystem coords;
	
	private HistoryObserver observer;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		observer = new HistoryObserver(board);
		board.setObservers(observer);
	}

	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}

	@Test
	public void testUpdate() {
		ShortList none = new ShortList(0);
		observer.update(BLACK, at("a2"), none);
		observer.update(WHITE, at("b3"), none);
		observer.update(BLACK, at("d1"), none);
		assertEquals(at("a2"), observer.get(0));
		assertEquals(at("b3"), observer.get(1));
		assertEquals(at("d1"), observer.get(2));
	}

	@Test
	public void testResponseToBoard() {
		board.play(at("c4"));
		board.play(at("e1"));
		board.play(at("b2"));
		assertEquals(at("c4"), observer.get(0));
		assertEquals(at("e1"), observer.get(1));
		assertEquals(at("b2"), observer.get(2));
	}

	@Test
	public void testIgnoreInitialStones() {
		String[] diagram = {
				".#...",
				".....",
				"....O",
				".....",
				".....",
		};
		board.setUpProblem(diagram, BLACK);
		board.play(at("c4"));
		board.play(at("e1"));
		board.play(at("b2"));
		assertEquals(at("c4"), observer.get(0));
		assertEquals(at("e1"), observer.get(1));
		assertEquals(at("b2"), observer.get(2));		
	}

}
