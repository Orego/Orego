package edu.lclark.orego.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortList;
import static edu.lclark.orego.core.CoordinateSystem.*;
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
	}

	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}

	@Test
	public void testResponseToBoard() {
		board.play("c4");
		board.play("e1");
		board.play("b2");
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
		board.play("c4");
		board.play("e1");
		board.play("b2");
		assertEquals(at("c4"), observer.get(0));
		assertEquals(at("e1"), observer.get(1));
		assertEquals(at("b2"), observer.get(2));		
	}

	@Test
	public void testClear() {
		board.play("c4");
		board.play("e1");
		board.play("b2");
		board.clear();
		board.play("a3");
		assertEquals(at("a3"), observer.get(0));
	}

	@Test
	public void testPass() {
		board.pass();
		assertEquals(PASS, observer.get(0));
	}
	
	@Test
	public void testCopyDataFrom() {
		HistoryObserver copy = new HistoryObserver(board);
		board.play("b1");
		observer.update(BLACK, at("e2"), new ShortList(0));
		copy.copyDataFrom(observer);
		assertEquals(at("b1"), copy.get(0));
		assertEquals(at("e2"), copy.get(1));
		copy.update(WHITE, at("c1"), new ShortList(0));
		copy.copyDataFrom(observer);
		copy.update(WHITE, at("a2"), new ShortList(0));
		assertEquals(at("a2"), copy.get(2));
	}

	@Test
	public void testBeforeBeginningOfGame() {
		assertEquals(NO_POINT, observer.get(-1));
	}

}
