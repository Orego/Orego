package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class McRunnableTest {

	private Player player;
	
	private McRunnable runnable;

	/** Delegate method to call at on player's board. */
	private short at(String label) {
		return player.getBoard().getCoordinateSystem().at(label);
	}

	@Before
	public void setUp() throws Exception {
		player = new Player(1, CopiableStructureFactory.feasible(5));
		player.clear();
		runnable = player.getMcRunnable(0);
	}

	@Test
	public void testPlayoutAfterTwoPasses() {
		runnable.acceptMove(PASS);
		runnable.acceptMove(PASS);
		assertEquals(2, runnable.getTurn());
		assertEquals(WHITE, runnable.playout(true));
	}

	@Test
	public void testPlayoutMaxMoves() {
		for (int i = 0; i < runnable.getBoard().getCoordinateSystem().getMaxMovesPerGame(); i++) {
			runnable.acceptMove(PASS);
		}
		// Playing out from here should run up against the max game length
		assertEquals(VACANT, runnable.playout(true));
	}

	@Test
	public void testPlayout() {
		String[] blackWins = {
				".##O.",
				"..#OO",
				"###O.",
				"..#OO",
				".####",
		};
		player.getBoard().setUpProblem(blackWins, WHITE);
		String[] whiteWins = {
				".#O..",
				".#O.O",
				"##OOO",
				".#O.O",
				".#O..",
		};
		runnable.getBoard().setUpProblem(whiteWins, WHITE);
		assertEquals(BLACK, runnable.performMcRun());
	}

	@Test
	public void testCopyDataFrom() {
		player.acceptMove(at("c3"));
		player.acceptMove(at("d2"));
		runnable.performMcRun();
		runnable.copyDataFrom(player.getBoard());
		assertEquals(player.getBoard().toString(), runnable.getBoard().toString());
	}
	
	@Test
	public void testOneSlowMovePlayed() {
		runnable.playout(true);
		assertNotEquals(0L, runnable.getFancyHashes()[1]);
		assertEquals(0L, runnable.getFancyHashes()[2]);
	}

}
