package orego.response;

import static org.junit.Assert.*;

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
	public void testResponseChoice() {
		
		//assertEquals(Coordinates.PASS, player.getHistoryInfo()[player.getHistoryInfo().length-2].getMove());
		//assertEquals(ResponseList.PASS_RUNS_BIAS, player.getHistoryInfo()[player.getHistoryInfo().length-2].getRuns());
		//assertEquals(null, player.getHistoryInfo()[Coordinates.NO_POINT].getHistoryInfo(Coordinates.NO_POINT).getHistoryInfo(Coordinates.NO_POINT).getHistoryInfo(Coordinates.NO_POINT));
	}

	@Test
	public void testIncorporateRun() {
		McRunnable runnable = new McRunnable(player, null);
		runnable.acceptMove(28);
		runnable.acceptMove(25);
		runnable.acceptMove(47);
		runnable.acceptMove(52);
		player.incorporateRun(runnable.getBoard().getColorToPlay(),runnable);
		ResponseList respZero = player.getResponseZero();
		ResponseList[] respOne = player.getResponseOne();
		ResponseList[][] respTwo = player.getResponseTwo();
		assertEquals(1, respZero.getWins()[respZero.getIndices()[47]]);
		assertEquals(1, respOne[25].getWins()[respOne[25].getIndices()[47]]);
		assertEquals(1, respTwo[28][25].getWins()[respTwo[28][25].getIndices()[47]]);
	}
}
