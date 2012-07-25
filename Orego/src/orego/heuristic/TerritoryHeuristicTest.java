package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Coordinates.EXTENDED_BOARD_AREA;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;
import orego.mcts.SearchNode;
import orego.policy.TerritoryPolicy;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class TerritoryHeuristicTest {

	private Board board;

	private TerritoryHeuristic heuristic;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new TerritoryHeuristic();
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
				".O.O...............",// 2
				"..................." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		assertEquals(-1,heuristic.evaluate(at("f13"), board));
		assertEquals(-1,heuristic.evaluate(at("c1"), board));
		assertEquals(0,heuristic.evaluate(at("t6"), board));
	}

	@Test
	public void testDilation() {
		int[] ourweights = new int[EXTENDED_BOARD_AREA];
		ourweights[at("e13")] = 64;
		ourweights[at("h13")] = 64;
		heuristic.dilation(ourweights);
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
		heuristic.dilation(ourweights);
		heuristic.erosion(ourweights);
		assertEquals(0, ourweights[at("e10")]);
		assertEquals(7, ourweights[at("e12")]);
		assertEquals(7, ourweights[at("e14")]);
		assertEquals(5, ourweights[at("d13")]);
		assertEquals(11, ourweights[at("f13")]);
		assertEquals(76, ourweights[at("e13")]);
	}
}
