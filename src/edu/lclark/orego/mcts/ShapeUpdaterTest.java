package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.patterns.PatternFinder;
import edu.lclark.orego.patterns.ShapeTable;

public class ShapeUpdaterTest {

	private Player player;

	private CoordinateSystem coords;

	private ShapeUpdater updater;

	private ShapeTable table;

	@Before
	public void setUp() throws Exception {
		player = new PlayerBuilder().msecPerMove(100).threads(4).boardWidth(5)
				.lgrf2(true).memorySize(1).rave(false).shape(true)
				.shapeScalingFactor(0.999f).shapeBias(10).shapeMinStones(3)
				.liveShape(true).build();
		Board board = player.getBoard();
		coords = board.getCoordinateSystem();
		updater = (ShapeUpdater) player.getUpdater();
		table = updater.getTable();
	}

	@Test
	public void testUpdateTree() {
		McRunnable runnable = player.getMcRunnable(0);
		Board board = runnable.getBoard();
		runnable.acceptMove(coords.at("a1"));
		runnable.acceptMove(coords.at("b1"));
		long hash = PatternFinder.getHash(board, coords.at("c1"), 3,
				coords.at("b1"));
		double before = table.getWinRate(hash);
//		System.out.println("Before at " + hash + ": " + before);
		runnable.acceptMove(coords.at("c1"));
		updater.updateTree(BLACK, runnable);
		runnable.copyDataFrom(player.getBoard());
		runnable.acceptMove(coords.at("a1"));
		runnable.acceptMove(coords.at("b1"));
		hash = PatternFinder
				.getHash(board, coords.at("c1"), 3, coords.at("b1"));
		double after = table.getWinRate(hash);
//		System.out.println("After at " + hash + ": " + after);
		assertTrue(after > before);
	}

	@Test
	public void testBadMoveDiscovered() {
		// TODO
		// Similar to above, but let the player run until it discovers a local
		// bad move
		fail("Not yet implemented");
	}

}
