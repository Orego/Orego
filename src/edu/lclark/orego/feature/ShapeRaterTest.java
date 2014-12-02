package edu.lclark.orego.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.SearchNode;
import edu.lclark.orego.mcts.SimpleSearchNode;
import edu.lclark.orego.patterns.PatternFinder;
import edu.lclark.orego.patterns.ShapeTable;

public class ShapeRaterTest {

	private ShapeRater rater;
	
	private SearchNode node;
	
	private Board board;
	
	private CoordinateSystem coords;
	
	private ShapeTable shapeTable;
	
	private static final int MIN_STONES = 3;
	
	private static final int BIAS = 100;
	
	private HistoryObserver history;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		node = new SimpleSearchNode(coords);
		node.clear(board.getFancyHash(), coords);
		history = new HistoryObserver(board);
		shapeTable = new ShapeTable(0.9f);
		rater = new ShapeRater(board, history, shapeTable, BIAS, MIN_STONES);
	}

	@Test
	public void testUpdateNode() {
		short p = coords.at("a1");
		assertEquals(1, node.getWins(p), 0.01);
		assertEquals(0.5, node.getWinRate(p), 0.01);
		rater.updateNode(node);
		assertEquals(1.0 + (BIAS * 0.5), node.getWins(p), 0.01);
		assertEquals((1.0 + (BIAS * 0.5)) / (2.0 + BIAS), node.getWinRate(p), 0.01);
	}
	
	@Test
	public void testSpecificUpdate() {
		short p = coords.at("c2");
		long hash = PatternFinder.getHash(board, p, MIN_STONES, history.get(board.getTurn()-1));
		shapeTable.update(hash, true);
		assertEquals(0.55f, shapeTable.getWinRate(hash), 0.01);
		rater.updateNode(node);
		assertEquals((1.0 + (BIAS * 0.5)) / (2.0 + BIAS), node.getWinRate(coords.at("a1")), 0.01);
		assertEquals((1.0 + (BIAS * 0.55)) / (2.0 + BIAS), node.getWinRate(p), 0.01);
	}

}
