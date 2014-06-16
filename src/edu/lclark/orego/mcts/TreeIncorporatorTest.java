package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import edu.lclark.orego.core.CoordinateSystem;

public class TreeIncorporatorTest {

	private Player player;
	
	private TreeIncorporator treeIncorporator;

	/** Delegate method to call at on board. */
	private short at(String label) {
		return player.getBoard().getCoordinateSystem().at(label);
	}

	@Before
	public void setUp() throws Exception {
		player = new Player(1, CopiableStructureFactory.feasible(5));
		CoordinateSystem coords = player.getBoard().getCoordinateSystem();
		TranspositionTable table = new TranspositionTable(100, new SimpleSearchNodeBuilder(coords), coords);
		treeIncorporator = new TreeIncorporator(player.getBoard(), table);
		player.setRunIncorporator(treeIncorporator);
	}

	@Test
	public void testClear() {
		McRunnable runnable = player.getMcRunnable(0);
		runnable.acceptMove(at("b1"));
		runnable.acceptMove(at("c4"));
		runnable.acceptMove(at("a2"));
		treeIncorporator.incorporateRun(BLACK, runnable);
		treeIncorporator.clear();
		assertEquals("Total runs: 60\n", treeIncorporator.toString(5));
	}

	@Test
	public void testIncorporateRun() {
		assertEquals("Total runs: 60\n", treeIncorporator.toString(5));
		McRunnable runnable = player.getMcRunnable(0);
		runnable.acceptMove(at("b1"));
		runnable.acceptMove(at("c4"));
		runnable.acceptMove(at("a2"));
		treeIncorporator.incorporateRun(BLACK, runnable);
		assertEquals("Total runs: 61\nB1:       2/      3 (0.6667)\n  Total runs: 60\n",
				treeIncorporator.toString(5));
		runnable.copyDataFrom(player.getBoard());
		runnable.acceptMove(at("e5"));
		runnable.acceptMove(at("d2"));
		runnable.acceptMove(at("a4"));
		treeIncorporator.incorporateRun(WHITE, runnable);
		assertEquals(
				"Total runs: 62\nE5:       1/      3 (0.3333)\n  Total runs: 60\nB1:       2/      3 (0.6667)\n  Total runs: 60\n",
				treeIncorporator.toString(5));
		runnable.copyDataFrom(player.getBoard());
		runnable.acceptMove(at("e5"));
		runnable.acceptMove(at("c3"));
		runnable.acceptMove(at("d1"));
		treeIncorporator.incorporateRun(WHITE, runnable);
		assertEquals(
				"Total runs: 63\nE5:       1/      4 (0.2500)\n  Total runs: 61\n  C3:       2/      3 (0.6667)\n    Total runs: 60\nB1:       2/      3 (0.6667)\n  Total runs: 60\n",
				treeIncorporator.toString(5));
	}

}
