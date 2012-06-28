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
import orego.play.UnknownPropertyException;
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
	
	@Test
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
			//player.setTesting(true);
			/*
			ResponseList table = player.getResponseZeroBlack();
			System.out.println(table.getWins(table.getIndices()[365]));
			System.out.println(table.getRuns(table.getIndices()[365]));
			System.out.println(table.getWinRate(365));
			System.out.println(table.getWins(table.getIndices()[385]));
			System.out.println(table.getRuns(table.getIndices()[385]));
			System.out.println(table.getWinRate(385));
			System.out.println(table.getWins(table.getIndices()[Coordinates.PASS]));
			System.out.println(table.getRuns(table.getIndices()[Coordinates.PASS]));
			System.out.println(table.getWinRate(Coordinates.PASS));
			System.out.println("\n");
			*/
			for (int i = 0; i < 1000; i++) {
				runnable.performMcRun();
				//System.out.println(player.getResponseZeroBlack().getTotalRuns());
			}
			/*
			System.out.println(table.getWins(table.getIndices()[365]));
			System.out.println(table.getRuns(table.getIndices()[365]));
			System.out.println(table.getWinRate(365));
			System.out.println(table.getWins(table.getIndices()[385]));
			System.out.println(table.getRuns(table.getIndices()[385]));
			System.out.println(table.getWinRate(385));
			System.out.println(table.getWins(table.getIndices()[Coordinates.PASS]));
			System.out.println(table.getRuns(table.getIndices()[Coordinates.PASS]));
			System.out.println(table.getWinRate(Coordinates.PASS));
			System.out.println("Turn "+runnable.getBoard().getTurn());
			*/
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
	}
	
	@Test
	public void testKillDeadStonesToOvercomeLargeTerritory() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { "#..................",// 19
					"O..................",// 18
					"O..................",// 17
					"O..................",// 16
					"O..................",// 15
					"O..................",// 14
					"OOOOOOOOOOOOOOOOOOO",// 13
					"OOOOOOOOOOOOOOOOOOO",// 12
					"OOOOOOOOOOOOOOOOOOO",// 11
					"OOOOOOOOOOOOOOOOOOO",// 10
					"###################",// 9
					"#..................",// 8
					"#..................",// 7
					"#..................",// 6
					"#..................",// 5
					"#..................",// 4
					"#..................",// 3
					"####...............",// 2
					".#.#..............." // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.getBoard().setUpProblem(BLACK, problem);
			McRunnable runnable = new McRunnable(player, new RandomPolicy());
			player.acceptMove(PASS);
			for (int i = 0; i < 1000; i++) {
				runnable.performMcRun();
			}
			int move = player.bestMove();
			// White must capture the dead black stone to win
			assertFalse(PASS == move);
		} else {
			String[] problem = { "#........", "O........", "OOOOOOOOO",
					"OOOOOOOOO", "OOOOOOOOO", "#########", "#........",
					"####.....", ".#.#.....", };
			player.getBoard().setUpProblem(BLACK, problem);
			player.acceptMove(PASS);
			int move = player.bestMove();
			// White must capture the dead black stone to win
			assertFalse(PASS == move);
		}
	}
	
	@Test
	public void testKillDeadStonesToOvercomeLargeTerritory2() {
		if (BOARD_WIDTH == 19) {
			// Orego must realize that its own stone is dead.
			String[] problem = { "##.O.OOOOOOOOOOOOO.",// 19
					"OOOOOOOOOOOOOOOOOOO",// 18
					"OOOOOOOOOOOOOOOOOOO",// 17
					"OOOOOOOOOOOOOOOOOOO",// 16
					"OOOOOOOOOOOOOOOOOOO",// 15
					"OOOOOOOOOOOOOOOOOOO",// 14
					"OOOOOOOOOOOOOOOOOOO",// 13
					"OOOOOOOOOOOOOOOOOOO",// 12
					"OOOOOOOOOOOOOOOOOOO",// 11
					"OOOOOO#############",// 10
					"###################",// 9
					"###################",// 8
					"###################",// 7
					"###################",// 6
					"###################",// 5
					"###################",// 4
					"####.O#############",// 3
					"#######.###########",// 2
					".#.##..############" // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.getBoard().setUpProblem(BLACK, problem);
			McRunnable runnable = new McRunnable(player, new RandomPolicy());
			player.acceptMove(PASS);
			for (int i = 0; i < 1000; i++) {
				runnable.performMcRun();
			}
			int move = player.bestMove();
			// White must capture the dead black stones to win
			// assertEquals(at("c19"), move);
			assertFalse(PASS == move);
			player.getBoard().play(move);
		} else {
			// Orego must realize that its own stone is dead
			String[] problem = { "##.O.OOO.", "OOOOOOOOO", "OOOOOOOOO",
					"OOOOOOOOO", "########O", "#########", "##O.#####",
					"####.##.#", ".#.######", };
			player.getBoard().setUpProblem(BLACK, problem);
			player.acceptMove(PASS);
			int move = player.bestMove();
			// White must capture the dead black stone to win
			assertEquals(at("c9"), move);
			assertFalse(PASS == move);
		}
	}
	
	/*@Test
	public void testConnect() throws UnknownPropertyException {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"##########OOOOOOOOO",// 19
					"##.#######OOOOOO.OO",// 18
					"#...######OOOOO...O",// 17
					"##.#######OOOOOO.OO",// 16
					"##########OOOOOOOOO",// 15
					"##########OOOOOOOOO",// 14
					"##########OOOOOOOOO",// 13
					"##########OOOOOOOOO",// 12
					"##########OOOOOOOOO",// 11
					"OOOOOOOOO.OOOOOOOOO",// 10
					"OOOOOOOOO##########",// 9
					"OOOOOOOOO##########",// 8
					"OOOOOOOOO##########",// 7
					"OOOOOOOOO##########",// 6
					"OOOO.OOOO##########",// 5
					"OOO...OOO######.###",// 4
					"OOOO.OOOO#####...##",// 3
					"OOOOOOOOO######.###",// 2
					"OOOOOOOOO##########" // 1
			// ABCDEFGHJKLMNOPQRST
			};
			player.setUpProblem(BLACK, problem);
			McRunnable runnable = new McRunnable(player, new RandomPolicy());
			for (int i = 0; i < 10000; i++) {
				runnable.performMcRun();
			}
			ResponseList table = player.getResponseZeroBlack();
			System.out.println(table.getWins(table.getIndices()[305]));
			System.out.println(table.getRuns(table.getIndices()[305]));
			System.out.println(table.getWinRate(305));
			System.out.println(table.getWins(table.getIndices()[326]));
			System.out.println(table.getRuns(table.getIndices()[326]));
			System.out.println(table.getWinRate(326));
			System.out.println(table.getWins(table.getIndices()[210]));
			System.out.println(table.getRuns(table.getIndices()[210]));
			System.out.println(table.getWinRate(210));
			//System.out.println("\n");
			int move = player.bestMove();
			assertEquals(at("k10"), move);
		} else {
			// Broken for 9x9 -- no playouts
			//String[] problem = { "....#....", "....#....", "....#....",
			//		"....#....", "OOOO.OOOO", "....#....", "....#....",
			//		"....#....", "....#....", };
			//player.setUpProblem(BLACK, problem);
			//player.setProperty("playouts", "10000");
			//int move = player.bestMove();
			//assertEquals(at("e5"), move);
		}
	}*/
}