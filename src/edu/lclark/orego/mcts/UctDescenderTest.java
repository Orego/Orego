package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.Suggester;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class UctDescenderTest {

	private Player player;

	private SimpleTreeUpdater updater;

	private Board board;

	Mover mover;

	Suggester suggester;

	private UctDescender descender;

	/** Delegate method to call at on board. */
	private short at(String label) {
		return player.getBoard().getCoordinateSystem().at(label);
	}

	@Before
	public void setUp() throws Exception {
		CopiableStructure cp = CopiableStructureFactory.useWithBias(5, 7.5);
		cp = cp.copy();
		mover = cp.get(Mover.class);
		suggester = cp.get(Suggester[].class)[2];
		player = new Player(1, cp);
		board = cp.get(Board.class);
		CoordinateSystem coords = player.getBoard().getCoordinateSystem();
		TranspositionTable table = new TranspositionTable(100, new SimpleSearchNodeBuilder(coords),
				coords);
		descender = new UctDescender(player.getBoard(), table, 75);
		updater = new SimpleTreeUpdater(player.getBoard(), table, 12);
		player.setTreeDescender(descender);
		player.setTreeUpdater(updater);
	}

	@Test
	public void testDescend() {
		McRunnable runnable = player.getMcRunnable(0);
		runnable.acceptMove(at("b1"));
		runnable.acceptMove(at("c4"));
		runnable.acceptMove(at("a2"));
		updater.updateTree(BLACK, runnable);
		runnable.copyDataFrom(player.getBoard());
		descender.descend(runnable);
		assertEquals(at("b1"), runnable.getHistoryObserver().get(0));
		for (int i = 0; i < 2; i++) {
			runnable.copyDataFrom(player.getBoard());
			runnable.acceptMove(at("d2"));
			updater.updateTree(BLACK, runnable);
		}
		runnable.copyDataFrom(player.getBoard());
		descender.descend(runnable);
		assertEquals(at("d2"), runnable.getHistoryObserver().get(0));
	}

	@Test
	public void testUpdateBias() {
		assertEquals("Total runs: 60\n", updater.toString(5));
		McRunnable runnable = player.getMcRunnable(0);
		for (int i = 0; i < 100; i++) {
			descender.fakeDescend(runnable, at("b1"), at("a1"), at("c4"));
			updater.updateTree(BLACK, runnable);
		}
		assertEquals(
				"Total runs: 160\nB1:     101/    102 (0.9902)\n  Total runs: 150\n  A1:       0/     92 (0.0109)\n    Total runs: 200\n    C4:      81/     82 (0.9878)\n      Total runs: 60\n",
				updater.toString(5));
		descender.fakeDescend(runnable, at("b1"), at("a1"), at("a2"));
		updater.updateTree(BLACK, runnable);
		assertEquals(
				"Total runs: 161\nB1:     102/    103 (0.9903)\n  Total runs: 151\n  A1:       0/     93 (0.0108)\n    Total runs: 201\n    C4:      81/     82 (0.9878)\n      Total runs: 60\n    A2:      42/     43 (0.9767)\n      Total runs: 60\n",
				updater.toString(5));
	}

	@Test
	public void testSuggesters() {
		String[] diagram = {
				".....",
				".....",
				".....",
				".....",
				"O#...",
		};
		board.setUpProblem(diagram, BLACK);
		assertEquals(1, suggester.getMoves().size());
		assertTrue(suggester.getMoves().contains(at("a2")));
		assertEquals(
				"A2",
				board.getCoordinateSystem().toString(
						mover.selectAndPlayOneMove(new MersenneTwisterFast(), true)));
	}

}
