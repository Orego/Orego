package edu.lclark.orego.feature;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.StoneColor.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class LgrfSuggesterTest {

	private CoordinateSystem coords;

	private Board board;

	private LgrfTable lgrfTable;

	private LgrfSuggester suggester;

	private HistoryObserver history;

	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		history = new HistoryObserver(board);
		lgrfTable = new LgrfTable(coords);
		suggester = new LgrfSuggester(board, history, lgrfTable, new NotEyeLike(board));
	}

	@Test
	public void testLgrfSuggester() {
		lgrfTable.update(BLACK, true, coords.at("a1"), coords.at("b1"),
				coords.at("c1"));
		board.play(coords.at("a1"));
		board.play(coords.at("b1"));
		assertEquals(coords.at("c1"), suggester.getMoves().get(0));
	}

	@Test
	public void testLevel1() {
		lgrfTable.update(BLACK, true, coords.at("a1"), coords.at("b1"),
				coords.at("c1"));
		board.play(coords.at("d1"));
		board.play(coords.at("b1"));
		// There is no 2nd level reply, but there is a 1st level reply
		assertEquals(coords.at("c1"), suggester.getMoves().get(0));
	}

	@Test
	public void testOccupiedPoint() {
		lgrfTable.update(BLACK, true, coords.at("a1"), coords.at("b1"),
				coords.at("c1"));
		board.play(coords.at("c1"));
		board.play(coords.at("b1"));
		// The tables say c1, but it's occupied
		assertEquals(NO_POINT, suggester.getMoves().get(0));
	}
	
	@Test
	public void testFilter() {
		lgrfTable.update(BLACK, true, coords.at("a1"), coords.at("b1"), coords.at("c3"));
		String[] before = {
				".....",
				"..#..",
				".#.#.",
				"..#..",
				".....",
		};
		board.setUpProblem(before, WHITE);
		board.play(coords.at("b1"));
		// The tables say c1, but it's infeasible
		assertEquals(0, suggester.getMoves().size());		
	}

}
