package edu.lclark.orego.feature;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.Board;

public class OnThirdOrFourthLineTest {
	
	private Board board;
	
	private OnThirdOrFourthLine onThirdOrFourthLine;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return board.getCoordinateSystem().at(label);
	}
	
	@Before
	public void setUp() throws Exception {
		board = new Board(19);
		onThirdOrFourthLine = OnThirdOrFourthLine.forWidth(board.getCoordinateSystem().getWidth());
	}

	@Test
	public void atTest() {
		assertFalse(onThirdOrFourthLine.at(at("a1")));
		assertFalse(onThirdOrFourthLine.at(at("a3")));
		assertTrue(onThirdOrFourthLine.at(at("c3")));
		assertTrue(onThirdOrFourthLine.at(at("q16")));
	}

}
