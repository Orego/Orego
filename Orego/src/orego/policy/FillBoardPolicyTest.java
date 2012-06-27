package orego.policy;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import static orego.policy.PatternPolicy.isPossibleNeighborhood;
import static orego.patterns.Pattern.diagramToNeighborhood;
import org.junit.*;

import orego.core.Board;
import orego.mcts.SearchNode;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

public class FillBoardPolicyTest {

	private Board board;
	
	private FillBoardPolicy policy;
	
	private IntSet moves;
	
	private MersenneTwisterFast random;
	

	@Before
	public void setUp() throws Exception {
		board = new Board();
		policy = new FillBoardPolicy();
		random = new MersenneTwisterFast();
		moves = new IntSet(FIRST_POINT_BEYOND_BOARD);
	}
	
	/** Kludgy way to set the last move, which the PatternPolicy uses to determine where to look for patterns. */
	protected void setLastMove(String p) {
		board.getMoves()[board.getTurn() - 1] = at(p);
	}

//	@Test
//	public void testHasAllVacantNeighbors() {
//		if (BOARD_WIDTH == 19) {
//			String[] problem = { 
//					".#.................",// 19
//					"...................",// 18
//					"...................",// 17
//					"...#O#.............",// 16
//					"...................",// 15
//					"...#O..............",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			assertTrue(policy.hasAllVacantNeighbors(at("m5"), board));
//			assertTrue(policy.hasAllVacantNeighbors(at("l4"), board));
//			assertTrue(policy.hasAllVacantNeighbors(at("r17"), board));
//			assertTrue(policy.hasAllVacantNeighbors(at("g18"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("a1"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("t1"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("t19"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("a19"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("f13"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("g15"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("d17"), board));
//			board.setUpProblem(WHITE, problem);
//			assertTrue(policy.hasAllVacantNeighbors(at("m5"), board));
//			assertTrue(policy.hasAllVacantNeighbors(at("l4"), board));
//			assertTrue(policy.hasAllVacantNeighbors(at("r17"), board));
//			assertTrue(policy.hasAllVacantNeighbors(at("g18"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("a1"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("t1"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("t19"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("a19"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("f13"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("g15"), board));
//			assertFalse(policy.hasAllVacantNeighbors(at("d17"), board));
//			policy.selectAndPlayOneMove(random, board);
//		} else {
//			String[] problem = { 
//					".........", // 9
//					".........", // 8
//					".........", // 7
//					"..#O#....", // 6
//					".........", // 5
//					"..#O.....", // 4
//					".........", // 3
//					".........", // 2
//					"........." // 1
//			// 		 ABCDEFGHJ
//			};
//			board.setUpProblem(BLACK, problem);
//			moves.add(at("c3"));
//			moves.add(at("d3"));
//			moves.add(at("d5"));
//			setLastMove("c4");
//			assertTrue(moves.contains(policy
//					.selectAndPlayOneMove(random, board)));
//		}
//	}
	
	@Test
	public void testSelectAndPlayOneMove() {
		// We do this so many times to ensure test coverage:
		// It's quite rare for there to be several good moves,
		// all of which are infeasible
		for (int i = 0; i < 100; i++) {
			board.clear();
			int lastMove = NO_POINT;
			while (board.getPasses() < 2) {
				lastMove = policy.selectAndPlayOneMove(random, board);
			}
			assertEquals(PASS, lastMove);
		}
	}
}