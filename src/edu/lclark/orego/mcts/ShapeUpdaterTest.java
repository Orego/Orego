package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.StoneColor.*;
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
		// TODO Make sure it works with more threads
		player = new PlayerBuilder().msecPerMove(1000).threads(1).boardWidth(5)
				.lgrf2(true).memorySize(10).rave(false).shape(true)
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
		runnable.acceptMove(coords.at("c1"));
		updater.updateTree(BLACK, runnable);
		runnable.copyDataFrom(player.getBoard());
		runnable.acceptMove(coords.at("a1"));
		runnable.acceptMove(coords.at("b1"));
		hash = PatternFinder
				.getHash(board, coords.at("c1"), 3, coords.at("b1"));
		double after = table.getWinRate(hash);
		assertTrue(after > before);
	}

	@Test
	public void testBadMoveDiscovered() {
		// Similar to above, but let the player run to discover the
		// values of various moves
		String[] diagram = {
				".##OO",
				"##OO.",
				"##O..",
				"##O..",
				".#O.O",
		};
		Board board = player.getBoard();
		board.setUpProblem(diagram, WHITE);
		board.play("d1");
		String[] moves = {"e1", "e2", "e3", "d2", "d3"};
		long[] hashes = new long[moves.length];
		double[] befores = new double[moves.length];
		for (int i = 0; i < moves.length; i++) {
			hashes[i] = PatternFinder
					.getHash(board, coords.at(moves[i]), 3, coords.at("d1"));
			befores[i] = table.getWinRate(hashes[i]);
		}
		System.out.println("Before search:");
		System.out.println(player.getRoot().toString(coords));
		player.bestMove();
		System.out.println("After search:");
		System.out.println(player.getRoot().toString(coords));
		double[] afters = new double[moves.length];
		for (int i = 0; i < moves.length; i++) {
			afters[i] = table.getWinRate(hashes[i]);
			System.out.println(hashes[i]);
			System.out.println(befores[i]);
			System.out.println(afters[i]);
		}
//		assertTrue(befores[0] > afters[0]); // e1 is a bad move
//		assertTrue(befores[1] > afters[1]); // e2 is bad
		assertTrue(befores[2] < afters[2]); // e3 is good
//		assertTrue(befores[3] > afters[3]); // d2 is bad
//		assertTrue(befores[4] > afters[4]); // d3 is bad
	}

}
