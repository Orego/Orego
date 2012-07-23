package orego.policy;

import static orego.core.Colors.BLACK;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;

import orego.core.Board;
import orego.util.IntSet;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class NakadePolicyTest {

	private Board board;

	private NakadePolicy policy;
	
	private MersenneTwisterFast random;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		policy = new NakadePolicy(new RandomPolicy());
		random = new MersenneTwisterFast();
	}

	@Test
	public void testPlayOneMove() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { "...................",// 19
					"...................",// 18
					".................##",// 17
					".................#.",// 16
					".................#.",// 15
					".................#.",// 14
					".................#.",// 13
					"...................",// 12
					"...................",// 11
					".....###...........",// 10
					".......#...........",// 9
					".....#.#...........",// 8
					".....#.#...........",// 7
					".....###...........",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"...................",// 2
					"..................."// 1
			// ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			board.play(at("t13"));
			assertTrue(policy.selectAndPlayOneMove(random, board)== at("t15"));
			board.play(at("f9"));
			assertEquals(at("g8"),policy.findNakade(at("f9"), board));
		}
	}

	@Test
	public void testNakade() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { "...................",// 19
					"...................",// 18
					".................##",// 17
					".................#.",// 16
					".................#.",// 15
					".................#.",// 14
					".................#.",// 13
					"...................",// 12
					"...................",// 11
					".....###...........",// 10
					".......#...........",// 9
					".....#.............",// 8
					".....#.#...........",// 7
					".....###...........",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"...................",// 2
					"..................."// 1
			      // ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			board.play(at("t13"));
			//System.out.println(policy.findNakade(at("t13"), board));
			assertTrue(policy.findNakade(at("t13"), board)==at("t15"));
			board.play(at("t1"));
			board.play(at("f9"));
			assertEquals(NO_POINT,policy.findNakade(at("f9"), board));
			board.play(at("r1"));
			board.play(at("h8"));
			assertTrue(policy.findNakade(at("h8"), board)==at("g8"));
		}
	}
}
