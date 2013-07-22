package orego.mcts;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.at;
import static orego.core.Coordinates.getAllPointsOnBoard;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import orego.play.UnknownPropertyException;
import static java.lang.Math.min;

import org.junit.Before;
import org.junit.Test;

public class TimePlayerTest {

	protected TimePlayer player;
	
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
	}
	
	@Test
	public void testBehind() throws UnknownPropertyException {
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
		player.setRemainingTime(20);
		long start = System.currentTimeMillis();
		player.bestMove();
		long usedNotBehind = System.currentTimeMillis() - start;
		
		// test behind
		player.setUpProblem(WHITE, problemWhiteIsBehind);
		player.setRemainingTime(20);
		start = System.currentTimeMillis();
		player.bestMove();
		long usedBehind = System.currentTimeMillis() - start;
		
		assertEquals(2.0, (usedBehind + 0.0) / usedNotBehind, 0.1);		
	}
	
	@Test
	public void testUnstableEvaluation() throws UnknownPropertyException {
		player.setProperty("unstable-eval", "true");
		player.setProperty("unstable-mult", "1.0");
		player.setUpProblem(BLACK, randomProblem);
		player.bestMove();
		assertFalse(player.isEvaluationUnstable());
		
		player.setRemainingTime(20);
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
		player.setRemainingTime(20);
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

		player.setRemainingTime(20);
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

		player.setRemainingTime(20);
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
		
		player.setRemainingTime(20);
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
		
		player.setRemainingTime(20);
		start = System.currentTimeMillis();
		player.bestMove();
		long usedVeryConfident = System.currentTimeMillis() - start;
		
		assertTrue(usedVeryConfident * 2 < usedNotVeryConfident);
	}
	
	@Test
	public void testQuickMovesOutOfBook() throws UnknownPropertyException {
		player.setProperty("quick-moves-out-of-book", "0");
		player.setProperty("book", "LateOpeningBook");
		player.setRemainingTime(20);
		
		while (true) {
			int bestMove = player.bestMove();
			if (player.isInOpeningBook()) {
				player.acceptMove(bestMove);
			}
			else {
				break;
			}
		}
		
		assertEquals(0, player.movesOutOfOpeningBook());

		player.setRemainingTime(20);
		long startWithout = System.currentTimeMillis();
		player.acceptMove(player.bestMove());
		long timePerMoveWithoutHeuristic = System.currentTimeMillis() - startWithout;
		
		player.reset();
		
		player.setProperty("quick-moves-out-of-book", "8");
		player.setProperty("book", "LateOpeningBook");
		player.setRemainingTime(20);

		while (true) {
			int bestMove = player.bestMove();
			if (player.isInOpeningBook()) {
				player.acceptMove(bestMove);
			}
			else {
				break;
			}
		}
		assertEquals(0, player.movesOutOfOpeningBook());

		for (int i = 0; i < 16; i++) {		
			player.setRemainingTime(20);
			long startWith = System.currentTimeMillis();
			player.acceptMove(player.bestMove());
			long timePerMoveWithHeuristic = System.currentTimeMillis() - startWith;
			assertEquals(i + 1, player.movesOutOfOpeningBook());
			assertTrue(timePerMoveWithHeuristic * 2.25 < timePerMoveWithoutHeuristic);
		}
		
		player.setRemainingTime(20);
		long startWith = System.currentTimeMillis();
		player.acceptMove(player.bestMove());
		long timePerMoveWithHeuristic = System.currentTimeMillis() - startWith;
		assertFalse(timePerMoveWithHeuristic * 2.25 < timePerMoveWithoutHeuristic);
	}
	
	@Test
	public void testCompareRestConf() throws UnknownPropertyException {
		// PART 1
		// keep feature off
		// make us confident that one move is better than the rest
		player.setUpProblem(BLACK, randomProblem);
		player.getRoot().addWins(at("D4"), 1000);
		
		// record the time
		player.setRemainingTime(100);
		long start = System.currentTimeMillis();
		player.bestMove();

		long timeWithout = System.currentTimeMillis() - start;
		
		// PART 2
		player.reset();
		// turn feature on
		player.setProperty("compare-rest", "true");
		player.setProperty("compare-rest-conf", "0.99");
		// keep us not confident
		// record the time
		player.setRemainingTime(100);
		start = System.currentTimeMillis();
		player.bestMove();
		long timeWithAndUnconfident = System.currentTimeMillis() - start;

		// those times should be about the same
		assertEquals(timeWithout, timeWithAndUnconfident, 100);
		
		// PART 3		
		// keep the feature on
		// make us confident
		player.getRoot().addWins(at("D4"), 1000);
		// then record the time
		player.setRemainingTime(20);
		start = System.currentTimeMillis();
		player.bestMove();
		long timeWithAndConfident = System.currentTimeMillis() - start;
		
		// the timeWithAndConfident should be less than the timeWithAndUnconfident
		assertTrue(timeWithAndConfident * 2 < timeWithAndUnconfident);
	}
	
	@Test
	public void testCompareRestUnconf() throws UnknownPropertyException {
		
		// PART 1:
		// keep property off
		// make sure we aren't confident
		assertTrue(player.confidenceBestVsRest() < 0.99999);
		// make move and record the time
		player.setRemainingTime(100);
		long start = System.currentTimeMillis();
		player.bestMove();
		long timeWithout = System.currentTimeMillis() - start;

		// PART 2
		
		player.reset();
		// turn property on
		player.setProperty("compare-rest", "true");
		player.setProperty("compare-rest-unconf", "0.9");
		
		// make sure we are confident
		player.getRoot().addWins(at("D4"), 5000);
		assertTrue(player.confidenceBestVsRest() > 0.9);
		// make move and record the time
		player.setRemainingTime(100);
		start = System.currentTimeMillis();
		player.bestMove();
		long timeWithAndConfident = System.currentTimeMillis() - start;
		
		// make sure we these times are similar
		
		assertEquals(timeWithout, timeWithAndConfident, 50);
		
		// PART 3
		player.reset();

		// keep property on
		// make sure we aren't confident
		for (int p: getAllPointsOnBoard()) {
			player.getRoot().addWins(p, 500);
		}
		assertTrue(player.confidenceBestVsRest() < 0.9);
		// make move and record the time
		player.setRemainingTime(100);
		start = System.currentTimeMillis();
		player.bestMove();
		long timeWithAndUnconfident = System.currentTimeMillis() - start;

		// make sure we stayed longer
		assertTrue(timeWithAndUnconfident > timeWithAndConfident * 1.2);
		
	}
	
	@Test
	public void testBenefitFromPreviousWork() throws UnknownPropertyException {
		
		double pps = 1000.0;
		int initialPlayouts = 4000;
		int remainingTime = 500;
		
		player.setUpProblem(BLACK, randomProblem);
		
		// set prop
		player.setProperty("benefit-from-previous-work", "true");
		
		// update playouts per second
		player.setPlayoutsPerSecond(pps);
		
		// set remaining time
		player.setRemainingTime(remainingTime);
		
		int msPerMoveNormal = player.getMillisecondsPerMove();
		
		// add playouts
		player.getRoot().addWins(at("D10"), initialPlayouts);
		
		// set remaining time
		player.setRemainingTime(remainingTime);
		
		// check that time allocated went down enough
		int msPerMoveBenefit = player.getMillisecondsPerMove();
		
		// make sure we saved the right amount of time (at most, msPerMoveNormal - 1 ms)		
		assertEquals(msPerMoveNormal - msPerMoveBenefit, min(msPerMoveNormal - 1, initialPlayouts / pps * 1000), 10);
	}
}
