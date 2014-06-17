package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.CoordinateSystem;

public class BestRateDescenderTest {

	private TreeDescender descender;
	
	private TreeUpdater updater;
	
	private Player player;

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
		updater = new SimpleTreeUpdater(player.getBoard(), table);
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

}
