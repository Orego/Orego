package orego.policy;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.assertEquals;
import orego.core.Board;
import orego.mcts.SearchNode;

import org.junit.Before;
import org.junit.Test;

public class CoupDeGracePolicyTest {

	private Board board;

	private CoupDeGracePolicy policy;

	@Before
	public void setUp() throws Exception {
		policy = new CoupDeGracePolicy();
		board = new Board();
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
					"...................",//6
					"###................",//5
					"#.##...............",//4
					"OO.#...............",//3
					".O##.O.............",//2
					"O#...O............."//1
				  // ABCDEFGHJKLMNOPQRST
				};
		} else {
			problem = new String[] {
					".........", // 9
					".........", // 8
					".........", // 7
					".........", // 6
					"###......", // 5
					"#.##.....", // 4
					"OO.#.....", // 3
					".O##.O...", // 2
					"O#...O..." // 1
				  // ABCDEFGHJ
				};
		}
		SearchNode node = new SearchNode();
		board.setUpProblem(BLACK, problem);
		node.reset(board.getHash());
		policy.updatePriors(node, board, 2);
		assertEquals(9, node.getWins(at("b4")));
		assertEquals(10, node.getRuns(at("b4")));
		// A2 attacks three adjacent dead stones
		assertEquals(41, node.getWins(at("a2")));
		assertEquals(42, node.getRuns(at("a2")));
		// F3 is next to a group that is probably not dead
		assertEquals(5, node.getWins(at("f3")));
		assertEquals(6, node.getRuns(at("f3")));
	}

}
