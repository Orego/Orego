package edu.lclark.orego.feature;

import static edu.lclark.orego.core.StoneColor.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class LastMoveEscapeSuggesterTest {

	private Board board;
	
	private CoordinateSystem coords;

	private LastMoveEscapeSuggester lastMoveEscapeSuggester;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		lastMoveEscapeSuggester = new LastMoveEscapeSuggester(board, new LastMoveObserver(board));
	}
	
	@Test
	public void testEscape(){
		String[] diagram = {
				"O#...",
				"....#",
				"....O",
				".....",
				".....",
		};
		board.setUpProblem(diagram, BLACK);
		board.play(at("d3"));
		assertTrue(lastMoveEscapeSuggester.getMoves().contains(at("e2")));
		assertFalse(lastMoveEscapeSuggester.getMoves().contains(at("a4")));
	}
	
	
	@Test
	public void testConnectEscape(){
		String[] diagram = {
				"O###.",
				"....#",
				"...#O",
				"...#.",
				"..O.O",
		};
		board.setUpProblem(diagram, WHITE);
		board.play(at("d4"));
		assertTrue(lastMoveEscapeSuggester.getMoves().contains(at("e5")));
		assertFalse(lastMoveEscapeSuggester.getMoves().contains(at("e2")));
	}

}
