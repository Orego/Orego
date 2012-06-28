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
		// Get all of the black response lists
		ResponseList respZeroBlack = player.getResponseZeroBlack();
		ResponseList[] respOneBlack = player.getResponseOneBlack();
		ResponseList[][] respTwoBlack = player.getResponseTwoBlack();
		// Get all of the white response lists
		ResponseList respZeroWhite = player.getResponseZeroWhite();
		ResponseList[] respOneWhite = player.getResponseOneWhite();
		ResponseList[][] respTwoWhite = player.getResponseTwoWhite();
		// Make sure all of the Black lists are right
		assertEquals(2, respZeroBlack.getWins()[respZeroBlack.getIndices()[47]]);
		assertEquals(3, respZeroBlack.getRuns()[respZeroBlack.getIndices()[47]]);
		assertEquals(2, respOneBlack[25].getWins()[respOneBlack[25].getIndices()[47]]);
		assertEquals(2, respTwoBlack[28][25].getWins()[respTwoBlack[28][25].getIndices()[47]]);
		// Make sure all of the White lists are right
		assertEquals(1, respZeroWhite.getWins()[respZeroWhite.getIndices()[25]]);
		assertEquals(3, respZeroWhite.getRuns()[respZeroWhite.getIndices()[25]]);
		assertEquals(1, respOneWhite[28].getWins()[respOneWhite[28].getIndices()[25]]);
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
	
	/*@Test
	public void testPassInSeki() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"#.#########OOOOOOOO",// 19
					".##########OOOOOOO.",// 18
					"###########OOOOOOOO",// 17
					"###########OOOOOOO.",// 16
					"###########OOOOOOOO",// 15
					"#########OOOOOOOOOO",// 14
					"#########OOOOOOOOOO",// 13
					"#########OOOOOOOOOO",// 12
					"#########OOOOOOOOOO",// 11
					"#########OOOOOOOOOO",// 10
					"#########OOOOOOOOOO",// 9
					"#########OOOOOOOOOO",// 8
					"#########OOOOOOOOOO",// 7
					"#########OOOOOOOOOO",// 6
					"#########OOOOOOOOOO",// 5
					"##OO###OOOO######OO",// 4
					"OOO#OO#########OOOO",// 3
					".OO#.O#.######OOOOO",// 2
					"O.O#.O##.######OOOO" // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.setUpProblem(BLACK, problem);
			McRunnable runnable = new McRunnable(player, new RandomPolicy());
			player.setTesting(true);
			for (int i = 0; i < 100; i++) {
				runnable.performMcRun();
				//System.out.println(player.getResponseZeroBlack().getTotalRuns());
			}
			ResponseList table = player.getResponseZeroBlack();
			System.out.println(table.getWinRate(365));
			System.out.println(table.getWinRate(385));
			System.out.println(table.getWinRate(Coordinates.PASS));
			//System.out.println("Turn "+runnable.getBoard().getTurn());
			int move = player.bestMove();
			assertEquals(PASS, move);
		} else {
			String[] problem = { "#.####OOO", ".#####OO.", "######OOO",
					"#####OOO.", "####OOOOO", "##OO###OO", "OOO#OO###",
					".OO#.O#.#", "O.O#.O##." };
			player.setUpProblem(BLACK, problem);
			int move = player.bestMove();
			assertEquals(PASS, move);
		}
	}*/
}
