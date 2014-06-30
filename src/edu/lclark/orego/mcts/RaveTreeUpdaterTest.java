package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;

public class RaveTreeUpdaterTest {

	private Player player;
	
	private RaveTreeUpdater updater;

	private TreeDescender descender;
	
	private TranspositionTable table;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return player.getBoard().getCoordinateSystem().at(label);
	}

	@Before
	public void setUp() throws Exception {
		player = new Player(1, CopiableStructureFactory.feasible(5));
		CoordinateSystem coords = player.getBoard().getCoordinateSystem();
		table = new TranspositionTable(100, new RaveNodeBuilder(coords), coords);
		descender = new BestRateDescender(player.getBoard(), table, 0);
		updater = new RaveTreeUpdater(player.getBoard(), table, 0);
		player.setTreeDescender(descender);
		player.setTreeUpdater(updater);
	}

	@Test
	public void testClear() {
		McRunnable runnable = player.getMcRunnable(0);
		runnable.acceptMove(at("b1"));
		runnable.acceptMove(at("c4"));
		runnable.acceptMove(at("a2"));
		updater.updateTree(BLACK, runnable);
		updater.clear();
		assertEquals("Total runs: 60\n", updater.toString(5));
	}

	@Test
	public void testIncorporateRun() {
		assertEquals("Total runs: 60\n", updater.toString(5));
		McRunnable runnable = player.getMcRunnable(0);
		runnable.acceptMove(at("b1"));
		runnable.acceptMove(at("c4"));
		runnable.acceptMove(at("a2"));
		updater.updateTree(BLACK, runnable);
		assertEquals("Total runs: 61\nB1:       2/      3 (0.6667) RAVE 3 (0.6667)\n  Total runs: 60\n",
				updater.toString(5));
		runnable.copyDataFrom(player.getBoard());
		runnable.acceptMove(at("e5"));
		runnable.acceptMove(at("c4"));
		runnable.acceptMove(at("b1"));
		updater.updateTree(WHITE, runnable);
		assertEquals(
				"Total runs: 62\nE5:       1/      3 (0.3333) RAVE 3 (0.3333)\n  Total runs: 60\nB1:       2/      3 (0.6667) RAVE 4 (0.5000)\n  Total runs: 60\n",
				updater.toString(5));
	}

}
