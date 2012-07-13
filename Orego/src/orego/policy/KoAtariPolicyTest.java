package orego.policy;

import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import static orego.core.Colors.*;

import orego.core.Board;
import orego.mcts.SearchNode;
import orego.util.IntSet;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class KoAtariPolicyTest {

	private Board board;
	
	private KoAtariPolicy policy;
	
	private MersenneTwisterFast random;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		policy = new KoAtariPolicy();
		random = new MersenneTwisterFast();
	}

	@Test
	public void testSelectAndPlayOneMove() {
		
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"...................",// 16
					"...................",// 15
					"...................",// 14
					"...................",// 13
					"...................",// 12
					".....O#............",// 11
					"....O.O#...........",// 10
					".....O#............",// 9
					"...................",// 8
					"...........O.......",// 7
					"...........O#......",// 6
					"............O......",// 5
					"...................",// 4
					"...................",// 3
					"...................",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};

			board.setUpProblem(BLACK, problem);
			board.play(at("F10"));
			int move = policy.selectAndPlayOneMove(random, board);
			assertTrue(at("N7") == move || at("O6") == move);
			// Verify that the move was actually playerd
			assertTrue((board.getColor(at("n7")) == WHITE) || board.getColor(at("o6")) == WHITE);
		}
	}

	@Test
	public void testUpdatePriors() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"...................",// 16
					"...................",// 15
					"...................",// 14
					"...................",// 13
					"...................",// 12
					".....O#............",// 11
					"....O.O#...........",// 10
					".....O#............",// 9
					"...................",// 8
					"...........O.......",// 7
					"...........O#......",// 6
					"............O......",// 5
					"...................",// 4
					"...................",// 3
					"...................",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			board.play(at("F10"));
			SearchNode node = new SearchNode();
			policy.updatePriors(node, board, 1);
			assertEquals(7, node.getWins(at("N7")));
			assertEquals(7, node.getWins(at("O6")));
		}
	}

	@Test
	public void testAtari() {
		if (BOARD_WIDTH == 19) {	
			String[] problem = { 
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				".....O#............",// 11
				"....O.O#...........",// 10
				".....O#............",// 9
				"...................",// 8
				"...........O.......",// 7
				"...........O#......",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		// 		 ABCDEFGHJKLMNOPQRST
		};
			board.setUpProblem(BLACK, problem);
			board.play(at("F10"));
			assertEquals(at("G10"), board.getKoPoint());
			IntSet moves = new IntSet(FIRST_POINT_BEYOND_BOARD);
			assertEquals(moves, policy.atari(board));
			board.play(at("N5"));
			board.play(at("S18"));
			moves.add(at("O6"));
			moves.add(at("N7"));
			assertEquals(moves, policy.atari(board));
		
			board.setUpProblem(BLACK, problem);
			board.play(at("F10"));
			assertEquals(at("G10"), board.getKoPoint());
			moves.clear();
			assertEquals(moves, policy.atari(board));
			board.play(at("N7"));
			board.play(at("S18"));
			moves.add(at("O6"));
			moves.add(at("N5"));
			assertEquals(moves, policy.atari(board));
		}
	}

	@Test
	public void testFallsThroughIfThereIsNoAtari() {
		if (BOARD_WIDTH == 19) {	
			String[] problem = { 
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				".....O#............",// 11
				"....O.O#...........",// 10
				".....O#............",// 9
				"...................",// 8
				"...........O.......",// 7
				"...........O#......",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		// 		 ABCDEFGHJKLMNOPQRST
		};
			board.setUpProblem(BLACK, problem);
			board.play(at("F10"));
			assertEquals(at("G10"), board.getKoPoint());
			IntSet moves = new IntSet(FIRST_POINT_BEYOND_BOARD);
			assertEquals(moves, policy.atari(board));
			int before = board.getTurn();
			policy.selectAndPlayOneMove(random, board);
			assertEquals(before + 1, board.getTurn());
		}
	}
	
}
