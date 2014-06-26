package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;

public class WideningTreeUpdaterTest {

	private Player player;
	
	private WideningTreeUpdater updater;

	private TreeDescender descender;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return player.getBoard().getCoordinateSystem().at(label);
	}

	@Before
	public void setUp() throws Exception {
		player = new Player(1, CopiableStructureFactory.feasible(5));
		CoordinateSystem coords = player.getBoard().getCoordinateSystem();
		TranspositionTable table = new TranspositionTable(100, new SimpleSearchNodeBuilder(coords), coords);
		descender = new BestRateDescender(player.getBoard(), table);
		updater = new WideningTreeUpdater(player.getBoard(), table);
		player.setTreeDescender(descender);
		player.setTreeUpdater(updater);
	}
	
	@Test
	public void testIncorporateRun() {
		assertEquals("Total runs: 60\n", updater.toString(5));
		McRunnable runnable = player.getMcRunnable(0);
		runnable.acceptMove(at("b1"));
		runnable.acceptMove(at("c4"));
		runnable.acceptMove(at("a2"));
		updater.updateTree(BLACK, runnable);
		assertEquals("Total runs: 61\n", updater.toString(5));
		for(int i = 0; i<9; i++){
			updater.updateTree(BLACK, runnable);
		}
		assertEquals("Total runs: 70\nB1:      11/     12 (0.9167)\n  Total runs: 60\n",
				updater.toString(5));
	}

}
