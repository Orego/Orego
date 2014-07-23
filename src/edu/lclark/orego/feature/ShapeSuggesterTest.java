package edu.lclark.orego.feature;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.patterns.PatternFinder;
import edu.lclark.orego.patterns.ShapeTable;

public class ShapeSuggesterTest {

	private Board board;
	
	private ShapeSuggester suggester;
	
	private ShapeTable shapeTable;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(9);
		shapeTable = new ShapeTable();
		suggester = new ShapeSuggester(board, shapeTable, 0.0, 24);
	}

	@Test
	public void testGetMoves() {
		String[] diagram = {
				".........",
				"OO.OO....",
				".........",
				".........",
				".........",
				"OO.......",
				".........",
				"OO.##....",
				"OO.OO....",
		};
		board.setUpProblem(diagram, BLACK);
		long hash = PatternFinder.getHash(board, board.getCoordinateSystem().at("c8"), 24);
		for(int i = 0; i < 500; i++){
			shapeTable.update(hash, true);
		}
		assertEquals(board.getCoordinateSystem().at("c8"), suggester.getMoves().get(0));
	}

}
