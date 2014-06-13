package edu.lclark.orego.mcts;

import edu.lclark.orego.core.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.Legality.SUICIDE;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.util.TestingTools.asOneString;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class McRunnableTest {

	private Player player;
	
	private McRunnable runnable;
	
	@Before
	public void setUp() throws Exception {
		player = new StubPlayer(5, 1);
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
		Board board = runnable.getBoard();
		String[] before = {
				".##O.",
				"..#OO",
				"###O.",
				"..#OO",
				".####",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(BLACK, runnable.playout());
	}

}
