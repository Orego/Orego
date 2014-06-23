package edu.lclark.orego.feature;

import static edu.lclark.orego.core.StoneColor.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class EscapeSuggesterTest {

	private Board board;
	
	private CoordinateSystem coords;

	private EscapeSuggester movesToEscape;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		movesToEscape = new EscapeSuggester(board, new AtariObserver(board));
	}
	
	@Test
	public void testEscape(){
		String[] diagram = {
				"O#...",
				"....#",
				"...#O",
				".....",
				".....",
		};
		board.setUpProblem(diagram, WHITE);
		assertTrue(movesToEscape.getMoves().contains(at("e2")));
		assertTrue(movesToEscape.getMoves().contains(at("a4")));
	}
	
	@Test
	public void testCaptureEscape(){
		String[] diagram = {
				"O#...",
				"...O#",
				"...#O",
				".....",
				".....",
		};
		board.setUpProblem(diagram, WHITE);
		assertTrue(movesToEscape.getMoves().contains(at("e2")));
		assertTrue(movesToEscape.getMoves().contains(at("a4")));
		assertTrue(movesToEscape.getMoves().contains(at("e5")));
		assertEquals(3, movesToEscape.getMoves().size());
	}
	
	@Test
	public void testConnectEscape(){
		String[] diagram = {
				"O###.",
				"...O#",
				"...#O",
				"...#.",
				"..O.O",
		};
		board.setUpProblem(diagram, WHITE);
		assertFalse(movesToEscape.getMoves().contains(at("e2")));
		board.play("d1");
		assertTrue(movesToEscape.getMoves().contains(at("e5")));
		board.play("a3");
		assertTrue(movesToEscape.getMoves().contains(at("e2")));
	}

	@Test
	public void testMultipleMerge() {
		String[] diagram = {
				"OO.OO",
				".#O#.",
				".#O#.",
				"..#..",
				".....",
		};
		board.setUpProblem(diagram, WHITE);
		assertTrue(movesToEscape.getMoves().contains(at("c5")));
		assertEquals(1, movesToEscape.getMoves().size());
	}

}
