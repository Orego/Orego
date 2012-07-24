package orego.policy;

import static org.junit.Assert.*;

import orego.core.Board;
import orego.mcts.SearchNode;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class TerritoryPolicyTest {

	private Board board;

	private TerritoryPolicy policy;
	
	private SearchNode node;

	private MersenneTwisterFast random;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		random = new MersenneTwisterFast();
		policy = new TerritoryPolicy();
		node = new SearchNode();
	}
	
	@Test
	public void testUpdatePriors() {
		String[] problem = new String[] { 
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"....#..#...........",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		policy.updatePriors(node, board, 2);
		assertEquals(0, node.getWins(at("f13")));
		assertEquals(14, node.getRuns(at("f13")));
		assertEquals(0, node.getWins(at("d12")));
		assertEquals(0, node.getRuns(at("d12")));
	}

	@Test
	public void testDilation() {
		int[] ourweights = new int[EXTENDED_BOARD_AREA];
		ourweights[at("e13")] = 64;
		ourweights[at("h13")] = 64;
		policy.dilation(ourweights);
		assertEquals(76, ourweights[at("e13")]);
		assertEquals(76, ourweights[at("h13")]);
		assertEquals(11, ourweights[at("f13")]);
		assertEquals(11, ourweights[at("g13")]);
		assertEquals(2, ourweights[at("e10")]);
		assertEquals(6, ourweights[at("e11")]);		
		assertEquals(10, ourweights[at("e12")]);
		assertEquals(10, ourweights[at("d13")]);
		assertEquals(8, ourweights[at("d12")]);
		assertEquals(4, ourweights[at("d11")]);
		assertEquals(2, ourweights[at("d10")]);
	}

	@Test
	public void testErosion() {
		int[] ourweights = new int[EXTENDED_BOARD_AREA];
		ourweights[at("e13")] = 64;
		ourweights[at("h13")] = 64;
		policy.dilation(ourweights);
		policy.erosion(ourweights);
		assertEquals(0, ourweights[at("e10")]);
		assertEquals(7, ourweights[at("e12")]);
		assertEquals(7, ourweights[at("e14")]);
		assertEquals(5, ourweights[at("d13")]);
		assertEquals(11, ourweights[at("f13")]);
		assertEquals(76, ourweights[at("e13")]);
	}

}
