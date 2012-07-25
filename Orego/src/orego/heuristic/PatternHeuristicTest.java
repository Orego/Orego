package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;
import orego.mcts.SearchNode;
import orego.policy.PatternPolicy;
import orego.util.IntSet;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class PatternHeuristicTest {

	private Board board;
	
	private PatternHeuristic heuristic;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new PatternHeuristic();
	}

	@Test
	public void testLocalHane1and2() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"...#O#.............",// 16
					"...................",// 15
					"...#O..............",// 14
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
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("e15"),board));
			assertEquals(0, heuristic.evaluate(at("g15"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"..#O#....", // 6
					".........", // 5
					"..#O.....", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d5"),board));
		}
	}
	
	@Test
	public void testLocalHane3() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..#.#..............",// 16
					"..#................",// 15
					"..#O...............",// 14
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
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d15"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"..#.#....", // 6
					"..#......", // 5
					"..#O.....", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d5"),board));
		}
	}

	@Test
	public void testLocalHane4Black() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..#.#..............",// 16
					"....O..............",// 15
					"..#.O..............",// 14
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
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d15"),board));
			assertEquals(1, heuristic.evaluate(at("f15"),board));
			assertEquals(1, heuristic.evaluate(at("f16"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"..#.#....", // 6
					"....O....", // 5
					"..#.O....", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d5"),board));
			assertEquals(1, heuristic.evaluate(at("f5"),board));
			assertEquals(1, heuristic.evaluate(at("f6"),board));
		}
	}

	@Test
	public void testLocalHane4White() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..O.O..............",// 16
					"....#..............",// 15
					"..O.#..............",// 14
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
			assertEquals(1, heuristic.evaluate(at("d15"),board));
			assertEquals(1, heuristic.evaluate(at("f15"),board));
			assertEquals(1, heuristic.evaluate(at("f16"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"..O.O....", // 6
					"....#....", // 5
					"..O.#....", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(WHITE, problem);
			assertEquals(1, heuristic.evaluate(at("d5"),board));
			assertEquals(1, heuristic.evaluate(at("f5"),board));
			assertEquals(1, heuristic.evaluate(at("f6"),board));
		}
	}

	@Test
	public void testLocalCut1() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..#O#..............",// 16
					"..O................",// 15
					"..#.O..............",// 14
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
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("b14"),board));
			assertEquals(1, heuristic.evaluate(at("b15"),board));
			assertEquals(1, heuristic.evaluate(at("d15"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"..#O#....", // 6
					"..O......", // 5
					"..#.O....", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("b4"),board));
			assertEquals(1, heuristic.evaluate(at("b5"),board));
			assertEquals(1, heuristic.evaluate(at("d5"),board));
		}
	}

	@Test
	public void testLocalCut2() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..###..............",// 16
					"..O.O..............",// 15
					"..#................",// 14
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
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d15"),board));
		} else {
			String[] problem = { ".........", // 9
					".........", // 8
					".........", // 7
					"..###....", // 6
					"..O.O....", // 5
					"..#......", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d5"),board));
		}
	}

	@Test
	public void testLocalEdge1() {
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
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"...................",// 8
					"...................",// 7
					"...................",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"....#..............",// 2
					"....O.............."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d1"),board));
			assertEquals(1, heuristic.evaluate(at("d2"),board));
			assertEquals(1, heuristic.evaluate(at("f1"),board));
			assertEquals(1, heuristic.evaluate(at("f2"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					".........", // 6
					".........", // 5
					".........", // 4
					".........", // 3
					"....#....", // 2
					"....O...." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d1"),board));
			assertEquals(1, heuristic.evaluate(at("d2"),board));
			assertEquals(1, heuristic.evaluate(at("f1"),board));
			assertEquals(1, heuristic.evaluate(at("f2"),board));
		}
	}

	@Test
	public void testLocalEdge2() {
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
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"...................",// 8
					"...................",// 7
					"...................",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"....##.............",// 2
					"....O.O............"// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d1"),board));
			assertEquals(1, heuristic.evaluate(at("d2"),board));
			assertEquals(1, heuristic.evaluate(at("f1"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					".........", // 6
					".........", // 5
					".........", // 4
					".........", // 3
					"....##...", // 2
					"....O.O.." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("d1"),board));
			assertEquals(1, heuristic.evaluate(at("d2"),board));
			assertEquals(1, heuristic.evaluate(at("f1"),board));
		}
	}

	@Test
	public void testLocalEdge3() {
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
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"...................",// 8
					"...................",// 7
					"...................",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					".....#O............",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("f1"),board));
			assertEquals(1, heuristic.evaluate(at("f3"),board));
			assertEquals(1, heuristic.evaluate(at("g1"),board));
			assertEquals(1, heuristic.evaluate(at("g3"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					".........", // 6
					".........", // 5
					".........", // 4
					".........", // 3
					".....#O..", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("f1"),board));
			assertEquals(1, heuristic.evaluate(at("f3"),board));
			assertEquals(1, heuristic.evaluate(at("g1"),board));
			assertEquals(1, heuristic.evaluate(at("g3"),board));
		}
	}

	@Test
	public void testLocalEdge4() {
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
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"...................",// 8
					"...................",// 7
					"...................",// 6
					"...................",// 5
					"...................",// 4
					"...................",// 3
					"...............#O..",// 2
					"................O.."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			assertEquals(1, heuristic.evaluate(at("q1"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					".........", // 6
					".........", // 5
					".........", // 4
					".........", // 3
					".....#O..", // 2
					"......O.." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(WHITE, problem);
			assertEquals(1, heuristic.evaluate(at("f1"),board));
		}
	}

	@Test
	public void testLocalEdge5() {
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
					"...................",// 11
					"...................",// 10
					"...................",// 9
					"...................",// 8
					"...................",// 7
					"...................",// 6
					"...................",// 5
					"...............#O..",// 4
					"...................",// 3
					"...............#O..",// 2
					"..............O.#.."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("q1"),board));
			assertEquals(1, heuristic.evaluate(at("s1"),board));
			assertEquals(1, heuristic.evaluate(at("s2"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					".........", // 6
					".........", // 5
					".....#O..", // 4
					".........", // 3
					".....#O..", // 2
					"....O.#.." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("f1"),board));
			assertEquals(1, heuristic.evaluate(at("h1"),board));
			assertEquals(1, heuristic.evaluate(at("h2"),board));
			assertEquals(1, heuristic.evaluate(at("f3"),board));
			assertEquals(1, heuristic.evaluate(at("g3"),board));
		}
	}

	@Test
	public void testPatternEdgeCases() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"#O.................",// 18
					"...................",// 17
					"...................",// 16
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
					".................#O",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("a17"),board));
			assertEquals(1, heuristic.evaluate(at("b17"),board));
			assertEquals(1, heuristic.evaluate(at("b19"),board));
			assertEquals(1, heuristic.evaluate(at("s3"),board));
			assertEquals(1, heuristic.evaluate(at("s1"),board));
			assertEquals(1, heuristic.evaluate(at("t3"),board));
		} else {
			String[] problem = { 
					".........", // 9
					"#O.......", // 8
					".........", // 7
					".........", // 6
					".........", // 5
					".........", // 4
					".........", // 3
					".......#O", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(1, heuristic.evaluate(at("a7"),board));
			assertEquals(1, heuristic.evaluate(at("b7"),board));
			assertEquals(1, heuristic.evaluate(at("b9"),board));
			assertEquals(1, heuristic.evaluate(at("h3"),board));
			assertEquals(1, heuristic.evaluate(at("h1"),board));
			assertEquals(1, heuristic.evaluate(at("j3"),board));
		}
	}


}
