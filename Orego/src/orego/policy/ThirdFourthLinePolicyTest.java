package orego.policy;

import static orego.core.Colors.BLACK;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;
import orego.mcts.SearchNode;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class ThirdFourthLinePolicyTest {

	private Board board;

	private ThirdFourthLinePolicy policy;
	
	private MersenneTwisterFast random;
	
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		policy = new ThirdFourthLinePolicy();
		random = new MersenneTwisterFast();
	}

	@Test
	public void testUpdatePriors() {
		SearchNode node = new SearchNode();
		String[] problem;
		problem = new String[] {      //
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...#........#...O..",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"..............OOOOO",// 5
				"...#..#....O..O...O",// 4
				"..............O...O",// 3
				"..............O...O",// 2
				"..............OOOOO"// 1
		/////////ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		node.reset(board.getHash());
		policy.updatePriors(node, board, 1);
		assertEquals(2, node.getWins(at("r3")));
		assertEquals(3, node.getRuns(at("r3")));
		assertEquals(2, node.getWins(at("p16")));
		assertEquals(3, node.getRuns(at("p16")));
		assertEquals(2, node.getWins(at("c8")));
		assertEquals(3, node.getRuns(at("c8")));
		assertEquals(2, node.getRuns(at("h4")));
		assertEquals(2, node.getRuns(at("c17")));
		assertEquals(2, node.getRuns(at("d3")));
	}
	
	@Test
	public void testSelectAndPlayOneMove(){
		SearchNode node = new SearchNode();
		String[] problem;
		problem = new String[] {      //
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...#..#..#..#..#O..",// 16
				"...................",// 15
				"..#............#...",// 14
				"...................",// 13
				"..#............#...",// 12
				"...................",// 11
				"..#............#...",// 10
				"...................",// 9
				"...O............#...",// 8
				"...................",// 7
				"...O...........#...",// 6
				"..............O.OO.",// 5
				"...#..#....O..O....",// 4
				"...O....O.O.O.O....",// 3
				"...................",// 2
				"..................."// 1
		/////////ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		int move = policy.selectAndPlayOneMove(random, board);
		assertEquals(at("r3"), move);
	}

}
