package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class BadPatternHeuristicTest {

	private Board board;
	
	private BadPatternHeuristic heuristic;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new BadPatternHeuristic(1);
	}

	@Test
	public void testPonnukiBlack() {
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
					"...OO..............",// 4
					"....O..............",// 3
					"..#O...............",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			heuristic.prepare(board);
			assertEquals(-1, heuristic.evaluate(at("d3"),board));
			assertEquals(0, heuristic.evaluate(at("c3"),board));
			board.clear();
			board.setUpProblem(WHITE, problem);
			assertEquals(0, heuristic.evaluate(at("c3"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"...OO....", // 6
					"....O....", // 5
					"..#O.....", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(-1, heuristic.evaluate(at("d5"),board));
		}
	}
	
	@Test
	public void testPonnukiWhite() {
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
					"...##..............",// 4
					"....#..............",// 3
					"..O#................",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			heuristic.prepare(board);
			assertEquals(-1, heuristic.evaluate(at("d3"),board));
			assertEquals(0, heuristic.evaluate(at("c3"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"...##....", // 6
					"....#....", // 5
					"..O#.....", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(WHITE, problem);
			assertEquals(-1, heuristic.evaluate(at("d5"),board));
		}
	}

	
	@Test
	public void testEmptyTriangleVerticalBlack() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..#................",// 16
					"..#................",// 15
					"..O................",// 14
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
			heuristic.prepare(board);
			assertEquals(-1, heuristic.evaluate(at("d15"),board));
			assertEquals(-1, heuristic.evaluate(at("b15"),board));
			board.clear();
			board.setUpProblem(WHITE, problem);
			assertEquals(0, heuristic.evaluate(at("d15"),board));
			assertEquals(0, heuristic.evaluate(at("b15"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"..#......", // 6
					"..#......", // 5
					"..O......", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(-1, heuristic.evaluate(at("d5"),board));
		}
	}
	
	@Test
	public void testEmptyTriangleVerticalWhite() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..O................",// 16
					"..O................",// 15
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
			board.setUpProblem(WHITE, problem);
			heuristic.prepare(board);
			assertEquals(-1, heuristic.evaluate(at("d15"),board));
			assertEquals(-1, heuristic.evaluate(at("b15"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"..#......", // 6
					"..#......", // 5
					"..O......", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(-1, heuristic.evaluate(at("d5"),board));
		}
	}
	
	@Test
	public void testEmptyTriangleHorizontalBlack() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..##O..............",// 16
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
			board.setUpProblem(BLACK, problem);
			heuristic.prepare(board);
			assertEquals(-1, heuristic.evaluate(at("c17"),board));
			assertEquals(-1, heuristic.evaluate(at("d17"),board));
			assertEquals(-1, heuristic.evaluate(at("c15"),board));
			assertEquals(-1, heuristic.evaluate(at("d15"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"..#......", // 6
					"..#......", // 5
					"..O......", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(-1, heuristic.evaluate(at("d5"),board));
		}
	}
	
	@Test
	public void testEmptyTriangleHorizontalWhite() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..OO#..............",// 16
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
			heuristic.prepare(board);
			assertEquals(-1, heuristic.evaluate(at("c17"),board));
			assertEquals(-1, heuristic.evaluate(at("d17"),board));
			assertEquals(-1, heuristic.evaluate(at("c15"),board));
			assertEquals(-1, heuristic.evaluate(at("d15"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"..#......", // 6
					"..#......", // 5
					"..O......", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(BLACK, problem);
			assertEquals(-1, heuristic.evaluate(at("d5"),board));
		}
	}
	
	
	@Test
	public void testPushThroughBambooBlack() {
		if (BOARD_WIDTH == 19) {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..O.O..............",// 16
					"..O.O..............",// 15
					"..O#...............",// 14
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
			heuristic.prepare(board);
			assertEquals(-1, heuristic.evaluate(at("d15"),board));
			assertEquals(-1, heuristic.evaluate(at("d16"),board));
		} else {
			String[] problem = { 
					".........", // 9
					".........", // 8
					".........", // 7
					"..#.#....", // 6
					"..#.#....", // 5
					"..#O.....", // 4
					".........", // 3
					".........", // 2
					"........." // 1
			// 		 ABCDEFGHJ
			};
			board.setUpProblem(WHITE, problem);
			assertEquals(-1, heuristic.evaluate(at("d5"),board));
		}
	}
		
		@Test
		public void testPushThroughBambooWhite() {
			if (BOARD_WIDTH == 19) {
				String[] problem = { 
						"...................",// 19
						"...................",// 18
						"...................",// 17
						"..#.#..............",// 16
						"..#.#..............",// 15
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
				board.setUpProblem(WHITE, problem);
				heuristic.prepare(board);
				assertEquals(-1, heuristic.evaluate(at("d15"),board));
				assertEquals(-1, heuristic.evaluate(at("d16"),board));
			} else {
				String[] problem = { 
						".........", // 9
						".........", // 8
						".........", // 7
						"..#.#....", // 6
						"..#.#....", // 5
						"..#O.....", // 4
						".........", // 3
						".........", // 2
						"........." // 1
				// 		 ABCDEFGHJ
				};
				board.setUpProblem(WHITE, problem);
				assertEquals(-1, heuristic.evaluate(at("d5"),board));
			}
	}
	

}
