package edu.lclark.orego.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

public class LastMoveObserverTest {
	
	private Board board;
	
	private LastMoveObserver lastMove;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		lastMove = new LastMoveObserver(board);
	}
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return board.getCoordinateSystem().at(label);
	}

	@Test
	public void testLastMove() {
		board.play(at("a1"));
		assertEquals(at("a1"), lastMove.getLastMove());
		board.play(at("b2"));
		assertEquals(at("b2"), lastMove.getLastMove());
		board.clear();
		assertEquals(board.getCoordinateSystem().NO_POINT, lastMove.getLastMove());
	}

}
