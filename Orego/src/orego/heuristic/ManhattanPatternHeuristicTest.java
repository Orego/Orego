package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import orego.core.Board;
import org.junit.Before;
import org.junit.Test;

public class ManhattanPatternHeuristicTest {

	private Board board;
	
	private ManhattanPatternHeuristic heuristic;

	/**
	 * Good pattern tests
	 */
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new ManhattanPatternHeuristic(1);
	}

	@Test
	public void testLocalHane1and2() {
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
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("e15")));
			assertFalse(heuristic.getGoodMoves().contains(at("g15")));
	}
	
	@Test
	public void testLocalHane3() {
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
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
	}

	@Test
	public void testLocalHane4Black() {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..#.#..............",// 16
					"...................",// 15
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
			board.setUpProblem(WHITE, problem);
			board.play("e15");
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
			assertTrue(heuristic.getGoodMoves().contains(at("f15")));
			assertTrue(heuristic.getGoodMoves().contains(at("f16")));
	}

	@Test
	public void testLocalHane4White() {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..O.O..............",// 16
					"...................",// 15
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
			board.setUpProblem(BLACK, problem);
			board.play("e15");
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
			assertTrue(heuristic.getGoodMoves().contains(at("f15")));
			assertTrue(heuristic.getGoodMoves().contains(at("f16")));
	}

	@Test
	public void testLocalCut1() {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..#O#..............",// 16
					"..O................",// 15
					"....O..............",// 14
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
			board.play("c14");
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("b14")));
			assertTrue(heuristic.getGoodMoves().contains(at("b15")));
			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
	}

	@Test
	public void testLocalCut2() {
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
			board.setUpProblem(WHITE, problem);
			board.play("c15");
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
	}

	@Test
	public void testLocalEdge1() {
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
			heuristic.prepare(board);
//			System.out.println(heuristic.getGoodMoves().size());
			assertTrue(heuristic.getGoodMoves().contains(at("d1")));
			assertTrue(heuristic.getGoodMoves().contains(at("d2")));
			assertTrue(heuristic.getGoodMoves().contains(at("f1")));
			assertTrue(heuristic.getGoodMoves().contains(at("f2")));
	}

	@Test
	public void testLocalEdge2() {
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
					"......O............"// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			board.play("e1");
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("d1")));
			assertTrue(heuristic.getGoodMoves().contains(at("d2")));
			assertTrue(heuristic.getGoodMoves().contains(at("f1")));
	}

	@Test
	public void testLocalEdge3() {
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
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("f1")));
			assertTrue(heuristic.getGoodMoves().contains(at("f3")));
			assertTrue(heuristic.getGoodMoves().contains(at("g1")));
			assertTrue(heuristic.getGoodMoves().contains(at("g3")));
	}

	@Test
	public void testLocalEdge4() {
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
					"................O..",// 2
					"................O.."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(BLACK, problem);
			board.play("q2");
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("q1")));
	}

	@Test
	public void testLocalEdge5() {
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
					"...............#...",// 2
					"..............O.#.."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			board.play("r2");
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("q1")));
			assertTrue(heuristic.getGoodMoves().contains(at("s1")));
			assertTrue(heuristic.getGoodMoves().contains(at("s2")));
	}

	@Test
	public void testPatternEdgeCases() {
			String[] problem = { 
					"...................",// 19
					"#..................",// 18
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
					"...................",// 2
					"..................."// 1
			// 		 ABCDEFGHJKLMNOPQRST
			};
			board.setUpProblem(WHITE, problem);
			board.play("b18");
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("a17")));
			assertTrue(heuristic.getGoodMoves().contains(at("b17")));
			assertTrue(heuristic.getGoodMoves().contains(at("b19")));
			board.play("s2");
			board.play("t2");
			heuristic.prepare(board);
			assertTrue(heuristic.getGoodMoves().contains(at("s3")));
			assertTrue(heuristic.getGoodMoves().contains(at("s1")));
			assertTrue(heuristic.getGoodMoves().contains(at("t3")));
	}
	
	/**
	 * Bad pattern tests
	 */
	@Test
	public void testPonnukiBlack() {
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
			assertTrue(heuristic.getBadMoves().contains(at("d3")));
	}
	
	@Test
	public void testPonnukiWhite() {
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
			assertTrue(heuristic.getBadMoves().contains(at("d3")));
	}

	
	@Test
	public void testEmptyTriangleVerticalBlack() {
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
			assertTrue(heuristic.getBadMoves().contains(at("d15")));
			assertTrue(heuristic.getBadMoves().contains(at("b15")));
	}
	
	@Test
	public void testEmptyTriangleVerticalWhite() {
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
			assertTrue(heuristic.getBadMoves().contains(at("d15")));
			assertTrue(heuristic.getBadMoves().contains(at("b15")));
	}
	
	@Test
	public void testEmptyTriangleHorizontalBlack() {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..##...............",// 16
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
			board.play("e16");
			heuristic.prepare(board);
			assertTrue(heuristic.getBadMoves().contains(at("d17")));
			assertTrue(heuristic.getBadMoves().contains(at("d15")));
	}
	
	@Test
	public void testEmptyTriangleHorizontalWhite() {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..OO...............",// 16
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
			board.play("e16");
			heuristic.prepare(board);
			assertTrue(heuristic.getBadMoves().contains(at("d17")));
			assertTrue(heuristic.getBadMoves().contains(at("d15")));

	}
	
	
	@Test
	public void testPushThroughBambooBlack() {
			String[] problem = { 
					"...................",// 19
					"...................",// 18
					"...................",// 17
					"..O.O..............",// 16
					"....O..............",// 15
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
			board.setUpProblem(WHITE, problem);
			board.play("c15");
			heuristic.prepare(board);
			assertTrue(heuristic.getBadMoves().contains(at("d15")));
			assertTrue(heuristic.getBadMoves().contains(at("d16")));

	}
		
		@Test
		public void testPushThroughBambooWhite() {
				String[] problem = { 
						"...................",// 19
						"...................",// 18
						"...................",// 17
						"..#.#..............",// 16
						"....#..............",// 15
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
				board.play("c15");
				heuristic.prepare(board);
				assertTrue(heuristic.getBadMoves().contains(at("d15")));
				assertTrue(heuristic.getBadMoves().contains(at("d16")));
	}
	
}
