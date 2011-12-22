package orego.policy;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import orego.core.Board;
import orego.mcts.SearchNode;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;


public class EscapePolicyTest {

	private Board board;
	
	private EscapePolicy policy;
	
	private MersenneTwisterFast random;
	

	@Before
	public void setUp() throws Exception {
		board = new Board();
		policy = new EscapePolicy(new SpecificPointPolicy());
		random = new MersenneTwisterFast();
		// escaper.reset();
	}
	
	@Test
	public void testRun() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					".OO................",// 19
					".##O.....O#O.......",// 18
					".O.........#.......",// 17
					"...........O.......",// 16
					"...................",// 15
					"...................",// 14
					"...................",// 13
					"...................",// 12
					"............O......",// 11
					"...........O#O..OO.",// 10
					"OO........O##..O#.#",// 9
					"##O.........O....#O",// 8
					"#.O..............OO",// 7
					".O.................",// 6
					"#..................",// 5
					"OOO.............OOO",// 4
					"##O.............O##",// 3
					"#.#............##.#",// 2
					"##.OO.........OO.##"// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			board.play(at("c17"));
			assertEquals(at("a18"), policy.selectAndPlayOneMove(random, board));
			board.setColorToPlay(WHITE);
			board.play(at("l17"));
			int move = policy.selectAndPlayOneMove(random, board);
			assertTrue(at("l19") == move || at("n17") == move);
			board.setColorToPlay(WHITE);
			board.play(at("m8"));
			assertEquals(at("o9"), policy.selectAndPlayOneMove(random, board));
			int count = 0;
			for (int i = 0; i < 100; i++) {
				board.setUpProblem(WHITE, problem);
				board.play(at("b5"));
				if (at("a6") == policy.selectAndPlayOneMove(random, board)) {
					count++;
				}
			}
			assertTrue(count <= 10);
			board.setColorToPlay(WHITE);
			board.play(at("r1"));
			assertEquals(at("s2"), policy.selectAndPlayOneMove(random, board));
			count = 0;
			for (int i = 0; i < 100; i++) {
				board.setUpProblem(WHITE, problem);
				board.play(at("c1"));
				if (at("b2") == policy.selectAndPlayOneMove(random, board)) {
					count++;
				}
			}
			assertTrue(count <= 10);
			count = 0;
			for (int i = 0; i < 100; i++) {
				board.setUpProblem(WHITE, problem);
				board.play(at("t10"));
				if (at("s9") == policy.selectAndPlayOneMove(random, board)) {
					count++;
				}
			}
			assertTrue(count <= 10);
		} else {
			String[] problem = { 
					"O##......", // 9
					"O.O..O#O.", // 8
					"OOOO...#.", // 7
					"####O.OO.", // 6
					".O.#OO#.#", // 5
					"#..O...#O", // 4
					"OOO...OOO", // 3
					"#.#..##.#", // 2
					"##.OOO.##" // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(WHITE, problem);
			board.play(at("b8"));
			assertEquals(at("d9"), policy.selectAndPlayOneMove(random, board));
			board.setColorToPlay(WHITE);
			board.play(at("g7"));
			int move = policy.selectAndPlayOneMove(random, board);
			assertTrue(at("g9") == move || at("j7") == move);
			int count = 0;
			for (int i = 0; i < 100; i++) {
				board.setUpProblem(WHITE, problem);
				board.play(at("b4"));
				if (at("a5") == policy.selectAndPlayOneMove(random, board)) {
					count++;
				}
			}
			assertTrue(count <= 10);
			board.setColorToPlay(WHITE);
			board.play(at("g1"));
			assertEquals(at("h2"), policy.selectAndPlayOneMove(random, board));
			count = 0;
			for (int i = 0; i < 100; i++) {
				board.setUpProblem(WHITE, problem);
				board.play(at("c1"));
				if (at("b2") == policy.selectAndPlayOneMove(random, board)) {
					count++;
				}
			}
			assertTrue(count <= 10);
			count = 0;
			for (int i = 0; i < 100; i++) {
				board.setUpProblem(WHITE, problem);
				board.play(at("j6"));
				if (at("h5") == policy.selectAndPlayOneMove(random, board)) {
					count++;
				}
			}
			assertTrue(count <= 10);
		}
	}
	
	@Test
	public void testOnlyTriesToCaptureIfInAtari() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"....OO.............",// 19
					"...O##.............",// 18
					"#O##.#O............",// 17
					".#.OOOO............",// 16
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
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"...................",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			board.play(at("e17"));
			// There's no need to defend D17, so the policy should suggest running
			// with the chain at F17.
			SearchNode node = new SearchNode();
			node.reset(board.getHash());
			policy.updatePriors(node, board, 2);
			assertEquals(2, node.getRuns(at("b18")));
			assertEquals(4, node.getRuns(at("g18")));
			assertEquals(1, node.getWins(at("b18")));
			assertEquals(3, node.getWins(at("g18")));
		}
	}
	
	@Test
	public void testEscapeByCapturing() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"....OO.............",// 19
					"...O##.............",// 18
					"#O##.#O............",// 17
					".#OOOOO............",// 16
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
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"...................",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			board.play(at("e17"));
			assertEquals(at("b18"), policy.selectAndPlayOneMove(random, board));
		} else {
			String[] problem = { 
					"....OO...", // 9
					"...O##...", // 8
					"#O##.#O..", // 7
					".#OOOOO..", // 6
					".........", // 5
					".........", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(WHITE, problem);
			board.play(at("e7"));
			assertEquals(at("b8"), policy.selectAndPlayOneMove(random, board));
		}
	}
	
	@Test
	public void testUpdatePriors() {
		SearchNode node = new SearchNode();
		if (BOARD_WIDTH == 19){
			String[] problem = { 
					"....OO.............",// 19
					"...O##.............",// 18
					"#O##.#O............",// 17
					".#OOOOO............",// 16
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
					"...................",// 5
					"...................",// 4
					"..O.O..............",// 3
					".O#.#O.............",// 2
					".O#.#O............."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			board.play("e17");
			node.reset(board.getHash());
			policy.updatePriors(node, board, 2);
			assertEquals(4, node.getRuns(at("b18")));
			assertEquals(4, node.getRuns(at("c18")));
			assertEquals(4, node.getRuns(at("g18")));
			assertEquals(3, node.getWins(at("b18")));
			assertEquals(3, node.getWins(at("c18")));
			assertEquals(3, node.getWins(at("g18")));
			board.setUpProblem(WHITE, problem);
			board.play("d1");
			node.reset(board.getHash());
			policy.updatePriors(node, board, 2);
			assertEquals(8, node.getRuns(at("d2")));
			assertEquals(7, node.getWins(at("d2")));
		}else{
			String[] problem = { 
					"....OO...", // 9
					"...O##...", // 8
					"#O##.#O..", // 7
					".#OOOOO..", // 6
					".........", // 5
					".........", // 4
					"..O.O....", // 3
					".O#.#O...", // 2
					".O#.#O..." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(WHITE, problem);
			board.play("e7");
			node.reset(board.getHash());
			policy.updatePriors(node, board, 2);
			assertEquals(4, node.getRuns(at("b8")));
			assertEquals(4, node.getRuns(at("c8")));
			assertEquals(4, node.getRuns(at("g8")));
			assertEquals(3, node.getWins(at("b8")));
			assertEquals(3, node.getWins(at("c8")));
			assertEquals(3, node.getWins(at("g8")));
			board.setUpProblem(WHITE, problem);
			board.play("d1");
			node.reset(board.getHash());
			policy.updatePriors(node, board, 2);
			assertEquals(8, node.getRuns(at("d2")));
			assertEquals(7, node.getWins(at("d2")));
		}
	}

	@Test
	public void testClone() {
		policy = new EscapePolicy(new CapturePolicy());
		Policy policy2 = policy.clone();
		assertTrue(policy2.getFallback() instanceof CapturePolicy);
	}
	
	@Test
	public void testConstructor() {
		policy = new EscapePolicy();
		assertTrue(policy.getFallback() instanceof RandomPolicy);
	}

}
