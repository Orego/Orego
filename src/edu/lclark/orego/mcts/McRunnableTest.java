package edu.lclark.orego.mcts;

import edu.lclark.orego.move.CopiableStructureFactory;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class McRunnableTest {

	private Player player;
	
	private McRunnable runnable;
	
	@Before
	public void setUp() throws Exception {
		player = new StubPlayer(1, CopiableStructureFactory.feasible(5));
		player.reset();
		runnable = player.getMcRunnable(0);
	}

	@Test
	public void testPlayoutAfterTwoPasses() {
		runnable.acceptMove(PASS);
		runnable.acceptMove(PASS);
		assertEquals(2, runnable.getTurn());
		assertEquals(WHITE, runnable.playout());
	}

	@Test
	public void testPlayoutMaxMoves() {
		for (int i = 0; i < runnable.getBoard().getCoordinateSystem().getMaxMovesPerGame(); i++) {
			runnable.acceptMove(PASS);
		}
		// Playing out from here should run up against the max game length
		assertEquals(VACANT, runnable.playout());
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

}
