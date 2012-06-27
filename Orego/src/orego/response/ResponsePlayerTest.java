package orego.response;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.BOARD_AREA;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;
import orego.mcts.McRunnable;
import orego.policy.RandomPolicy;
import orego.response.ResponseList;

import org.junit.Before;
import org.junit.Test;

public class ResponsePlayerTest {
	
	ResponsePlayer player;

	@Before
	public void setUp() throws Exception {
		player = new ResponsePlayer();
		player.reset();
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
		ResponseList respZeroBlack = player.getResponseZeroBlack();
		ResponseList[] respOneBlack = player.getResponseOneBlack();
		ResponseList[][] respTwoBlack = player.getResponseTwoBlack();
		ResponseList respZeroWhite = player.getResponseZeroWhite();
		ResponseList[] respOneWhite = player.getResponseOneWhite();
		ResponseList[][] respTwoWhite = player.getResponseTwoWhite();
		assertEquals(2, respZeroBlack.getWins()[respZeroBlack.getIndices()[47]]);
		assertEquals(3, respZeroBlack.getRuns()[respZeroBlack.getIndices()[47]]);
		assertEquals(1, respZeroWhite.getWins()[respZeroWhite.getIndices()[25]]);
		assertEquals(3, respZeroWhite.getRuns()[respZeroWhite.getIndices()[25]]);
		assertEquals(2, respOneBlack[25].getWins()[respOneBlack[25].getIndices()[47]]);
		assertEquals(1, respOneWhite[28].getWins()[respOneWhite[28].getIndices()[25]]);
		assertEquals(2, respTwoBlack[28][25].getWins()[respTwoBlack[28][25].getIndices()[47]]);
		assertEquals(1, respTwoWhite[25][47].getWins()[respTwoWhite[25][47].getIndices()[52]]);
	}
	protected void fakeRun(int winner, String... labels) {
		int[] moves = new int[labels.length + 2];
		int i;
		for (i = 0; i < labels.length; i++) {
			moves[i] = at(labels[i]);
		}
		moves[i] = PASS;
		moves[i + 1] = PASS;
		McRunnable runnable = new McRunnable(player, new RandomPolicy());
		player.fakeGenerateMovesToFrontierOfTree(runnable, moves);
		runnable.copyDataFrom(player.getBoard());
		for (int p : moves) {
			runnable.acceptMove(p);
		}
		player.incorporateRun(winner, runnable);
	}
	
	@Test
	public void testBestStoredMove() {
		fakeRun(BLACK, "c4", "c5", "c6", "c7");
		fakeRun(BLACK, "c4", "c5", "c6", "c7");
		fakeRun(WHITE, "c5", "c4", "c6", "c7");
		assertEquals(at("c4"), player.bestStoredMove());
	}
	
	/*
	@Test
	public void testMcRunIncorporation() {
		McRunnable runnable = (McRunnable) player.getRunnable(0);
		int runs = 1;
		for (int i = 0; i < runs; i++) {
			runnable.performMcRun();
		}
		for(int run : player.getResponseZero().getRuns()) {
			//System.out.println(run);
		}
		assertEquals(runs + (2 * BOARD_AREA + 10), player.getResponseZero()
				.getTotalRuns());
	}
	*/
}
