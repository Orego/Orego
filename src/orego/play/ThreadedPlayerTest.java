package orego.play;

import static org.junit.Assert.*;
import static orego.core.Coordinates.*;
import orego.mcts.*;
import org.junit.Before;
import org.junit.Test;

public class ThreadedPlayerTest {

	private ThreadedPlayer player;

	@Before
	public void setUp() throws Exception {
		player = new MctsPlayer();
		player.setProperty("pool", "" + MctsPlayerTest.TABLE_SIZE);
		player.setProperty("threads", "2");
		player.setProperty("msec", "10");
		player.reset();
	}

	@Test
	public void testPondering() throws UnknownPropertyException {
		player.setProperty("ponder", "true");
		assertTrue(player.isPondering());
		assertTrue(player.getRunnable(1) instanceof McRunnable);
		player.acceptMove(at("c2"));
		assertTrue(player.threadsRunning());
		player.stopThreads();
	}

	@Test
	public void testExhaustBook() throws UnknownPropertyException {
		player.setProperty("book", "StarPointsBook");
		player.reset();
		for (int i = 0; i < 20; i++) {
			int move = player.bestMove();
			if (move == RESIGN) {
				return;
			}
			player.acceptMove(move);
		}
	}

	@Test
	public void testPlayoutLimit() throws UnknownPropertyException {
		player.setProperty("playouts", "1000");
		player.setProperty("threads", "4");
		player.reset();
		player.bestMove();
		assertTrue(player.getMillisecondsPerMove() < 0);
	}

	@Test
	public void testSetRemainingTime() {
		player.setRemainingTime(10);
		
			assertEquals(55, player.getMillisecondsPerMove());
		
	}

	@Test
	public void testUndo() throws UnknownPropertyException {
		player.setProperty("ponder", "true");
		player.acceptMove(at("d4"));
		player.undo();
		assertTrue(player.threadsRunning());
		player.stopThreads();
	}

}
