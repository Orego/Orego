package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.at;
import static orego.patterns.Pattern.arrayToNeighborhood;
import static orego.patterns.Pattern.colorSpecificDiagramToArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import orego.core.Board;
import orego.patterns.Pattern;
import org.junit.Before;
import org.junit.Test;

public class PatternHeuristicTest {
	
	private PatternHeuristic heuristic;

	/**
	 * Good pattern tests
	 */
	@Before
	public void setUp() throws Exception {
//		board = new Board();
		heuristic = new PatternHeuristic(1);
	}

//	@Test
//	public void testLocalHane1and2() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"...#O#.............",// 16
//					"...................",// 15
//					"...#O..............",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("e15")));
//			assertFalse(heuristic.getGoodMoves().contains(at("g15")));
//	}
//	
//	@Test
//	public void testLocalHane3() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..#.#..............",// 16
//					"..#................",// 15
//					"..#O...............",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
//	}
//
//	@Test
//	public void testLocalHane4Black() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..#.#..............",// 16
//					"...................",// 15
//					"..#.O..............",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(WHITE, problem);
//			board.play("e15");
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
//			assertTrue(heuristic.getGoodMoves().contains(at("f15")));
//			assertTrue(heuristic.getGoodMoves().contains(at("f16")));
//	}
//
//	@Test
//	public void testLocalHane4White() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..O.O..............",// 16
//					"...................",// 15
//					"..O.#..............",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			board.play("e15");
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
//			assertTrue(heuristic.getGoodMoves().contains(at("f15")));
//			assertTrue(heuristic.getGoodMoves().contains(at("f16")));
//	}
//
//	@Test
//	public void testLocalCut1() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..#O#..............",// 16
//					"..O................",// 15
//					"....O..............",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			board.play("c14");
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("b14")));
//			assertTrue(heuristic.getGoodMoves().contains(at("b15")));
//			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
//	}
//
//	@Test
//	public void testLocalCut2() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..###..............",// 16
//					"..O.O..............",// 15
//					"..#................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(WHITE, problem);
//			board.play("c15");
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("d15")));
//	}
//
//	@Test
//	public void testLocalEdge1() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"...................",// 16
//					"...................",// 15
//					"...................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"....#..............",// 2
//					"....O.............."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			heuristic.prepare(board);
//			heuristic.prepare(board);
//			heuristic.prepare(board);
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("d1")));
//			assertTrue(heuristic.getGoodMoves().contains(at("d2")));
//			assertTrue(heuristic.getGoodMoves().contains(at("f1")));
//			assertTrue(heuristic.getGoodMoves().contains(at("f2")));
//	}
//
//	@Test
//	public void testLocalEdge2() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"...................",// 16
//					"...................",// 15
//					"...................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"....##.............",// 2
//					"......O............"// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(WHITE, problem);
//			board.play("e1");
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("d1")));
//			assertTrue(heuristic.getGoodMoves().contains(at("d2")));
//			assertTrue(heuristic.getGoodMoves().contains(at("f1")));
//	}
//
//	@Test
//	public void testLocalEdge3() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"...................",// 16
//					"...................",// 15
//					"...................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					".....#O............",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("f1")));
//			assertTrue(heuristic.getGoodMoves().contains(at("f3")));
//			assertTrue(heuristic.getGoodMoves().contains(at("g1")));
//			assertTrue(heuristic.getGoodMoves().contains(at("g3")));
//	}
//
//	@Test
//	public void testLocalEdge4() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"...................",// 16
//					"...................",// 15
//					"...................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"................O..",// 2
//					"................O.."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			board.play("q2");
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("q1")));
//	}
//
//	@Test
//	public void testLocalEdge5() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"...................",// 16
//					"...................",// 15
//					"...................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...............#O..",// 4
//					"...................",// 3
//					"...............#...",// 2
//					"..............O.#.."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(WHITE, problem);
//			board.play("r2");
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("q1")));
//			assertTrue(heuristic.getGoodMoves().contains(at("s1")));
//			assertTrue(heuristic.getGoodMoves().contains(at("s2")));
//	}
//
//	@Test
//	public void testPatternEdgeCases() {
//			String[] problem = { 
//					"...................",// 19
//					"#..................",// 18
//					"...................",// 17
//					"...................",// 16
//					"...................",// 15
//					"...................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(WHITE, problem);
//			board.play("b18");
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("a17")));
//			assertTrue(heuristic.getGoodMoves().contains(at("b17")));
//			assertTrue(heuristic.getGoodMoves().contains(at("b19")));
//			board.play("s2");
//			board.play("t2");
//			heuristic.prepare(board);
//			assertTrue(heuristic.getGoodMoves().contains(at("s3")));
//			assertTrue(heuristic.getGoodMoves().contains(at("s1")));
//			assertTrue(heuristic.getGoodMoves().contains(at("t3")));
//	}
//	
//	/**
//	 * Bad pattern tests
//	 */
//	@Test
//	public void testPonnukiBlack() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"...................",// 16
//					"...................",// 15
//					"...................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...OO..............",// 4
//					"....O..............",// 3
//					"..#O...............",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			heuristic.prepare(board);
//			assertTrue(heuristic.isBad(at("d3"), board));
//	}
//
//		@Test
//		public void testPonnuki2Black() {
//				String[] problem = { 
//						"...................",// 19
//						"...................",// 18
//						"...................",// 17
//						"...................",// 16
//						"...................",// 15
//						"...................",// 14
//						"...................",// 13
//						"...................",// 12
//						"...................",// 11
//						"...................",// 10
//						"...................",// 9
//						"...................",// 8
//						"...................",// 7
//						".O.................",// 6
//						".O.##..............",// 5
//						"..OO#..............",// 4
//						"....O..............",// 3
//						"...O...............",// 2
//						"..................."// 1
//				// 		 ABCDEFGHJKLMNOPQRST
//				};
//				board.setUpProblem(BLACK, problem);
//				heuristic.prepare(board);
//				assertTrue(heuristic.isBad(at("d3"), board));
//		}
//		
//	
//	@Test
//	public void testPonnukiWhite() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"...................",// 16
//					"...................",// 15
//					"...................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...##..............",// 4
//					"....#..............",// 3
//					"..O#................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(WHITE, problem);
//			heuristic.prepare(board);
//			assertTrue(heuristic.isBad(at("d3"), board));
//	}
//
//	
//	@Test
//	public void testEmptyTriangleVerticalBlack() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..#................",// 16
//					"..#................",// 15
//					"..O................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			heuristic.prepare(board);
//			assertTrue(heuristic.isBad(at("d15"), board));
//			assertTrue(heuristic.isBad(at("b15"), board));
//	}
//	
//	@Test
//	public void testEmptyTriangleVerticalWhite() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..O................",// 16
//					"..O................",// 15
//					"..#................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(WHITE, problem);
//			heuristic.prepare(board);
//			assertTrue(heuristic.isBad(at("d15"), board));
//			assertTrue(heuristic.isBad(at("b15"), board));
//	}
//	
//	@Test
//	public void testEmptyTriangleHorizontalBlack() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..##...............",// 16
//					"...................",// 15
//					"...................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(WHITE, problem);
//			board.play("e16");
//			heuristic.prepare(board);
//			assertTrue(heuristic.isBad(at("d17"), board));
//			assertTrue(heuristic.isBad(at("d15"), board));
//	}
//	
//	@Test
//	public void testEmptyTriangleHorizontalWhite() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..OO...............",// 16
//					"...................",// 15
//					"...................",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			board.play("e16");
//			heuristic.prepare(board);
//			assertTrue(heuristic.isBad(at("d17"), board));
//			assertTrue(heuristic.isBad(at("d15"), board));
//
//	}
//	
//	
//	@Test
//	public void testPushThroughBambooBlack() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..O.O..............",// 16
//					"....O..............",// 15
//					"..O#...............",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(WHITE, problem);
//			board.play("c15");
//			heuristic.prepare(board);
//			assertTrue(heuristic.isBad(at("d15"), board));
//			assertTrue(heuristic.isBad(at("d16"), board));
//
//	}
//		
//		@Test
//		public void testPushThroughBambooWhite() {
//				String[] problem = { 
//						"...................",// 19
//						"...................",// 18
//						"...................",// 17
//						"..#.#..............",// 16
//						"....#..............",// 15
//						"..#O...............",// 14
//						"...................",// 13
//						"...................",// 12
//						"...................",// 11
//						"...................",// 10
//						"...................",// 9
//						"...................",// 8
//						"...................",// 7
//						"...................",// 6
//						"...................",// 5
//						"...................",// 4
//						"...................",// 3
//						"...................",// 2
//						"..................."// 1
//				// 		 ABCDEFGHJKLMNOPQRST
//				};
//				board.setUpProblem(BLACK, problem);
//				board.play("c15");
//				heuristic.prepare(board);
//				assertTrue(heuristic.isBad(at("d15"), board));
//				assertTrue(heuristic.isBad(at("d16"), board));
//	}
	
	@Test
	public void testClone() throws Exception {
		PatternHeuristic copy = heuristic.clone();
		assertNotSame(heuristic, copy);
		assertTrue(copy instanceof PatternHeuristic);		
		assertSame(heuristic.goodNeighborhoods, copy.goodNeighborhoods);
//		assertEquals(heuristic.numberOfBadPatterns,  copy.numberOfBadPatterns);
		assertEquals(heuristic.numberOfGoodPatterns, copy.numberOfGoodPatterns);
		
	}
	
	private void setupGoodPatterns() {
		PatternHeuristicPatterns.ALL_GOOD_PATTERNS = new String[6];
		PatternHeuristicPatterns.ALL_GOOD_PATTERNS[0] = ".OO#O###";
		PatternHeuristicPatterns.ALL_GOOD_PATTERNS[1] = "####OOO#";
		PatternHeuristicPatterns.ALL_GOOD_PATTERNS[2] = "...#.OOO";
		PatternHeuristicPatterns.ALL_GOOD_PATTERNS[3] = "##..OOO#";
		PatternHeuristicPatterns.ALL_GOOD_PATTERNS[4] = ".O##...O";
		PatternHeuristicPatterns.ALL_GOOD_PATTERNS[5] = "#OO#..OO";
	}	

	private char diagramToChar(String diagram, int color) {
		return arrayToNeighborhood(colorSpecificDiagramToArray(diagram, color));
	}
	
	@Test
	public void testResetGoodPatterns() {
		setupGoodPatterns();
		
		// we need to now create a new pattern heuristic since we've changed the available pattern set
		heuristic = new PatternHeuristic(1);
		
		// pick some random samples to make certain we have the appropriate neighborhoods set
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("##..OOO#", BLACK)));
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar(".O##...O", BLACK)));
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("#OO#..OO", BLACK)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("####OOO#", WHITE)));
		
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("##..OOO#", WHITE)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar(".O##...O", WHITE)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("#OO#..OO", WHITE)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("####OOO#", WHITE)));
		
		heuristic.resetGoodPatterns();
		
		// again just a random sampling but quite reasonable
		// we want to ensure the patterns were removed from the goodNeighborhoods
		assertFalse(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("##..OOO#", BLACK)));
		assertFalse(heuristic.goodNeighborhoods[BLACK].get(diagramToChar(".O##...O", BLACK)));
		assertFalse(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("#OO#..OO", BLACK)));
		assertFalse(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("####OOO#", BLACK)));
		
		assertFalse(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("##..OOO#", WHITE)));
		assertFalse(heuristic.goodNeighborhoods[WHITE].get(diagramToChar(".O##...O", WHITE)));
		assertFalse(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("#OO#..OO", WHITE)));
		assertFalse(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("####OOO#", WHITE)));
	}
		
	@Test
	public void testResizeGoodPatterns() {
		
		setupGoodPatterns();
		// we need to now create a new pattern heuristic since we've changed the available pattern set
		heuristic = new PatternHeuristic(1);
				
		// make certain all the patterns are used (we always try for 250 but we'll take 6)
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("#OO#..OO", BLACK)));
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar(".O##...O", BLACK)));
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("##..OOO#", BLACK)));
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("...#.OOO", BLACK))); 
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar(".OO#O###", BLACK)));
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("####OOO#", BLACK)));
		
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("#OO#..OO", WHITE)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar(".O##...O", WHITE)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("##..OOO#", WHITE)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("...#.OOO", WHITE))); 
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar(".OO#O###", WHITE)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("####OOO#", WHITE)));
		
		// now resize to 4 and make sure all four *last* patterns are set
		heuristic.resizeNumberOfGoodPatterns(4);
		
		// make certain we have the bottom four patterns
		
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("#OO#..OO", BLACK)));
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar(".O##...O", BLACK)));
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("##..OOO#", BLACK)));
		assertTrue(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("...#.OOO", BLACK))); 
		
		// make certain white has the same
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("#OO#..OO", WHITE)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar(".O##...O", WHITE)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("##..OOO#", WHITE)));
		assertTrue(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("...#.OOO", WHITE)));
		
		// make sure we don't have the top two patterns
		assertFalse(heuristic.goodNeighborhoods[BLACK].get(diagramToChar(".OO#O###", BLACK)));
		assertFalse(heuristic.goodNeighborhoods[BLACK].get(diagramToChar("####OOO#", BLACK)));
		
		// make certain white doesn't either
		assertFalse(heuristic.goodNeighborhoods[WHITE].get(diagramToChar(".OO#O###", WHITE)));
		assertFalse(heuristic.goodNeighborhoods[WHITE].get(diagramToChar("####OOO#", WHITE)));
		
		// TODO: loop through all good neighborhoods and ensure only 4 have true values?
	}
	
	@Test
	public void testSetProperty() throws Exception {
		setupGoodPatterns();
		// we need to now create a new pattern heuristic since we've changed the available pattern set
		heuristic = new PatternHeuristic(1);
		
		assertEquals(6, heuristic.numberOfGoodPatterns);
		
		heuristic.setProperty("numberOfGoodPatterns", "3");
		
		assertEquals(3, heuristic.numberOfGoodPatterns);
		
	}
	
//	@Test
//	public void testPushThroughBambooWhiteFarFromLastMove() {
//			String[] problem = { 
//					"...................",// 19
//					"...................",// 18
//					"...................",// 17
//					"..#.#..............",// 16
//					"..#.#..............",// 15
//					"..#O...............",// 14
//					"...................",// 13
//					"...................",// 12
//					"...................",// 11
//					"...................",// 10
//					"...................",// 9
//					"...................",// 8
//					"...................",// 7
//					"...................",// 6
//					"...................",// 5
//					"...................",// 4
//					"...................",// 3
//					"...................",// 2
//					"..................."// 1
//			// 		 ABCDEFGHJKLMNOPQRST
//			};
//			board.setUpProblem(BLACK, problem);
//			board.play("a1");
//			heuristic.prepare(board);
//			assertTrue(heuristic.isBad(at("d15"), board));
//	}
		
}
