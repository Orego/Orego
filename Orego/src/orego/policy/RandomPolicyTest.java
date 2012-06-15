package orego.policy;

import static org.junit.Assert.*;
import static orego.core.Coordinates.*;
import orego.core.Board;
import orego.mcts.SearchNode;

import org.junit.Before;
import org.junit.Test;
import ec.util.MersenneTwisterFast;

public class RandomPolicyTest {

	private RandomPolicy policy;

	private Board board;
	
	private MersenneTwisterFast random;
	
	@Before
	public void setUp() throws Exception {
		policy = new RandomPolicy();
	}

	@Test
	public void testSelectAndPlayOneMove() {
		board = new Board();
		random = new MersenneTwisterFast();
		int lastMove = NO_POINT;
		while (board.getPasses() < 2) {
			lastMove = policy.selectAndPlayOneMove(random, board);
		}
		assertEquals(PASS, lastMove);
	}
	
	// TODO Modify RandomPolicy (and equivalent code in other policies) so that this passes.
	// At present, C17 is far overrepresented on the empty board, because it comes at the end
	// of a long string of intersections that are vacant, but infeasible.
//	@Test
//	public void testDistribution() {
//		// Verify that no point makes up too large a fraction of the moves played
//		board = new Board();
//		random = new MersenneTwisterFast();
//		int[] counts = new int[FIRST_POINT_BEYOND_BOARD];
//		for (int i = 0; i < 10000; i++) {
//			board.clear();
//			counts[policy.selectAndPlayOneMove(random, board)]++;
//		}
//		for (int p : ALL_POINTS_ON_BOARD) {
//			if (counts[p] > 0) {
//				System.out.println(pointToString(p) + ": " + counts[p]);
//			}
//			assertTrue(counts[p] < 1000);
//		}
//	}

}
