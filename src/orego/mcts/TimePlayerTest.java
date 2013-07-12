package orego.mcts;

import orego.core.Board;
import orego.play.UnknownPropertyException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import static orego.core.Colors.*;
import static orego.core.Coordinates.at;

public class TimePlayerTest {

	protected TimePlayer player;
	
	protected Board board;
	
	protected String[] randomProblem = new String[] {
			"...................",// 19
			"...................",// 18
			"...................",// 17
			"..#................",// 16
			"...................",// 15
			"..........O........",// 14
			"..............O....",// 13
			"...................",// 12
			"...................",// 11
			"..........#........",// 10
			"...................",// 9
			"...................",// 8
			"...................",// 7
			"..............O....",// 6
			"......#............",// 5
			"...................",// 4
			"...................",// 3
			"...................",// 2
			"..................." // 1
		  // ABCDEFGHJKLMNOPQRST
	};
	
	@Before
	public void setUp() throws Exception {
		player = new TimePlayer();
		player.reset();
		board = new Board();
	}
	
	@Test
	public void testThinkLonger() throws UnknownPropertyException {
		player.setProperty("behind", "true");
		player.setProperty("behind-mult", "1.0");
		String[] problemWhiteIsBehind = new String[] {
				"###################",// 19
				"###################",// 18
				"###################",// 17
				"###################",// 16
				"###################",// 15
				"###################",// 14
				"###################",// 13
				"###################",// 12
				"###################",// 11
				"###################",// 10
				"###################",// 9
				"###################",// 8
				"###################",// 7
				"###################",// 6
				"###################",// 5
				"###################",// 4
				"..#................",// 3
				"...................",// 2
				"..#................" // 1
			  // ABCDEFGHJKLMNOPQRST
		};
		// test not behind
		player.setUpProblem(BLACK, problemWhiteIsBehind);
		player.setRemainingTime(10);
		long start = System.currentTimeMillis();
		player.bestMove();
		long usedNotBehind = System.currentTimeMillis() - start;
		
		// test behind
		player.setUpProblem(WHITE, problemWhiteIsBehind);
		player.setRemainingTime(10);
		start = System.currentTimeMillis();
		player.bestMove();
		long usedBehind = System.currentTimeMillis() - start;
		
		assertEquals(2.0, (usedBehind + 0.0) / usedNotBehind, 0.1);		
	}
	
	@Test
	public void testIsEvaluationUnstable() throws UnknownPropertyException {
		player.setProperty("unstable-eval", "true");
		player.setProperty("unstable-mult", "1.0");
		player.setUpProblem(BLACK, randomProblem);
		player.bestMove();
		assertFalse(player.isEvaluationUnstable());
		
		player.setRemainingTime(10);
		long start = System.currentTimeMillis();
		player.bestMove();
		long usedStable = System.currentTimeMillis() - start;


		// make an unstable evaluation artificially
		player.getRoot().addWins(at("A16"), 10000);
		player.getRoot().addLosses(at("A16"), 10000);
		// now A1 is 10000/20000: most wins
		player.getRoot().addWins(at("E16"), 5000);
		player.getRoot().addLosses(at("E16"), 35000);
		// now T1 is 5000/40000: most runs

		assertTrue(player.isEvaluationUnstable());
		player.setRemainingTime(10);
		start = System.currentTimeMillis();
		player.bestMove();
		long usedUnstable = System.currentTimeMillis() - start;
		
		assertEquals(2.0, (usedUnstable + 0.0) / usedStable, 0.1);	
	}
	
	@Test
	public void testCompareSecondUnconf() throws UnknownPropertyException {
		player.setProperty("compare-second", "true");
		player.setProperty("compare-second-unconf-mult", "2.0");
		player.setProperty("compare-second-unconf", "0.2");
		player.setUpProblem(BLACK, randomProblem);
		player.bestMove();
		
		assertFalse(player.confidenceBestVsSecondBest() < 0.2);

		player.setRemainingTime(10);
		long start = System.currentTimeMillis();
		player.bestMove();
		long usedConfidentEnough = System.currentTimeMillis() - start;

		// make it unconfident artificially
		player.getRoot().addWins(at("A16"), 10000);
		player.getRoot().addLosses(at("A16"), 10000);

		// now A16 is 10000/20000: most wins = best move
		player.getRoot().addWins(at("E16"), 5000);
		player.getRoot().addLosses(at("E16"), 1);
		// now T1 is 9999/10000: 2nd most wins and much higher win rate = not confident that A16 better than E16
		
		assertTrue(player.confidenceBestVsSecondBest() < 0.2);

		player.setRemainingTime(10);
		start = System.currentTimeMillis();
		player.bestMove();
		long usedNotConfidentEnough = System.currentTimeMillis() - start;
		assertEquals(3.0, (usedNotConfidentEnough + 0.0) / usedConfidentEnough, 0.5);
	}
	
	@Test
	public void testCompareSecondConf() throws UnknownPropertyException {
		player.setProperty("compare-second", "true");
		player.setProperty("compare-second-conf", "0.9");
		player.setUpProblem(BLACK, randomProblem);
		assertFalse(player.confidenceBestVsSecondBest() > 0.9);
		
		player.setRemainingTime(10);
		long start = System.currentTimeMillis();
		player.bestMove();
		long usedNotVeryConfident = System.currentTimeMillis() - start;

		// make it very confident, artificially
		player.getRoot().addWins(at("A16"), 9999);
		player.getRoot().addLosses(at("A16"), 1);
		player.getRoot().addWins(at("E16"), 5000);
		player.getRoot().addLosses(at("E16"), 20000);
		player.bestMove();
		assertTrue(player.confidenceBestVsSecondBest() > 0.9);
		
		player.setRemainingTime(10);
		start = System.currentTimeMillis();
		player.bestMove();
		long usedVeryConfident = System.currentTimeMillis() - start;
		
		assertTrue(usedVeryConfident * 2 < usedNotVeryConfident);
	}
}
