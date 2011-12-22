package orego.policy;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import orego.core.*;
import orego.mcts.SearchNode;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class CapturePolicyTest {

	private Board board;
	
	private CapturePolicy policy;
	
	private MersenneTwisterFast random;
	
	@Before
	public void setUp() throws Exception {
		policy = new CapturePolicy();
		board = new Board();
		random = new MersenneTwisterFast();
	}

	@Test
	public void testSelectAndPlayOneMove() {
		String[] problem;
		if (BOARD_WIDTH == 19) {
			problem = new String[] {
					"...................",//19
					"...................",//18
					"...................",//17
					"...................",//16
					"...................",//15
					"...................",//14
					"...................",//13
					"...................",//12
					"...................",//11
					"...................",//10
					"...................",//9
					"...................",//8
					"...................",//7
					"..O................",//6
					".OOO...............",//5
					"OO#................",//4
					"#O#O...............",//3
					".#O#O..............",//2
					"#O................."//1
				  // ABCDEFGHJKLMNOPQRST
				};
		} else {
			problem = new String[] {
					".........", // 9
					".........", // 8
					".........", // 7
					"..O......", // 6
					".OOO.....", // 5
					"OO#......", // 4
					"#O#O.....", // 3
					".#O#O....", // 2
					"#O......." // 1
				  // ABCDEFGHJ
				};
		}
		board.setUpProblem(BLACK, problem);
		board.play("h8");
		assertEquals(at("a2"), policy.selectAndPlayOneMove(random, board));
	}
	
	@Test
	public void testUpdatePriors() {
		String[] problem;
		if (BOARD_WIDTH == 19) {
			problem = new String[] {
					"...................",//19
					"...................",//18
					"...................",//17
					"...................",//16
					"...................",//15
					"...................",//14
					"...................",//13
					"...................",//12
					"...................",//11
					"...................",//10
					"...................",//9
					"...................",//8
					"...................",//7
					"..O................",//6
					".OOO...............",//5
					"OO#................",//4
					"#O#O...............",//3
					".#O#O..............",//2
					"#O................."//1
				  // ABCDEFGHJKLMNOPQRST
				};
		} else {
			problem = new String[] {
					".........", // 9
					".........", // 8
					".........", // 7
					"..O......", // 6
					".OOO.....", // 5
					"OO#......", // 4
					"#O#O.....", // 3
					".#O#O....", // 2
					"#O......." // 1
				  // ABCDEFGHJ
				};
		}
		SearchNode node = new SearchNode();
		board.setUpProblem(WHITE, problem);
		node.reset(board.getHash());
		policy.updatePriors(node, board, 2);
		assertEquals(8, node.getRuns(at("a2")));
		assertEquals(6, node.getRuns(at("d4")));
		assertEquals(4, node.getRuns(at("d1")));
		assertEquals(7, node.getWins(at("a2")));
		assertEquals(5, node.getWins(at("d4")));
		assertEquals(3, node.getWins(at("d1")));
	}

}
