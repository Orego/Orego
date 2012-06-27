package orego.response;

import static org.junit.Assert.*;

import orego.core.Colors;
import orego.core.Coordinates;
import orego.mcts.McRunnable;
import orego.response.ResponseList;

import org.junit.Before;
import org.junit.Test;

public class ResponsePlayerTest {
	
	ResponsePlayer player;

	@Before
	public void setUp() throws Exception {
		player = new ResponsePlayer();
	}

	@Test
	public void testIncorporateRun() {
		player.setTesting(true);
		// play a fake game
		McRunnable runnable = new McRunnable(player, null);
		runnable.acceptMove(28);
		runnable.acceptMove(25);
		runnable.acceptMove(47);
		runnable.acceptMove(52);
		player.incorporateRun(Colors.BLACK,runnable);
		ResponseList respZero = player.getResponseZero();
		ResponseList[] respOne = player.getResponseOne();
		ResponseList[][] respTwo = player.getResponseTwo();
		assertEquals(2, respZero.getWins()[respZero.getIndices()[47]]);
		assertEquals(3, respZero.getRuns()[respZero.getIndices()[47]]);
		assertEquals(2, respOne[25].getWins()[respOne[25].getIndices()[47]]);
		assertEquals(2, respTwo[28][25].getWins()[respTwo[28][25].getIndices()[47]]);
	}
}
