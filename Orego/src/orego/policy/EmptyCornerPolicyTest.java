package orego.policy;

import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.at;
import static orego.core.Colors.*;
import static org.junit.Assert.*;

import orego.core.Board;
import orego.mcts.SearchNode;
import orego.util.IntSet;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class EmptyCornerPolicyTest {

	private Board board;

	private EmptyCornerPolicy policy;
	
	private MersenneTwisterFast random;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		policy = new EmptyCornerPolicy();
		random = new MersenneTwisterFast();
	}

	@Test
	public void testUpdatePriors1() {
		SearchNode node = new SearchNode();
		String[] problem;
		problem = new String[] { 
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"..O................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"OOOOOOOOO..........",// 9
				"........O..........",// 8
				"........O..........",// 7
				"........O..........",// 6
				"........O..........",// 5
				"........O..........",// 4
				"........O..........",// 3
				"........O..........",// 2
				"........O.........O"// 1
				//ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		node.reset(board.getHash());
		policy.updatePriors(node, board, 1);
		assertEquals(11, node.getWins(at("q16")));
		assertEquals(12, node.getRuns(at("q16")));
		assertEquals(1, node.getWins(at("q4")));
		assertEquals(1, node.getWins(at("d4")));
		assertEquals(1, node.getWins(at("d16")));
		assertEquals(2, node.getRuns(at("q4")));
		assertEquals(2, node.getRuns(at("d4")));
		assertEquals(2, node.getRuns(at("d16")));
		
	}

}
