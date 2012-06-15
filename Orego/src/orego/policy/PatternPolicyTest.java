package orego.policy;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import static orego.policy.PatternPolicy.isPossibleNeighborhood;
import static orego.patterns.Pattern.diagramToNeighborhood;
import org.junit.*;

import orego.core.Board;
import orego.mcts.SearchNode;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

public class PatternPolicyTest {

	private Board board;
	
	private PatternPolicy policy;
	
	private IntSet moves;
	
	private MersenneTwisterFast random;
	

	@Before
	public void setUp() throws Exception {
		board = new Board();
		policy = new PatternPolicy();
		random = new MersenneTwisterFast();
		moves = new IntSet(FIRST_POINT_BEYOND_BOARD);
	}
	
	/** Kludgy way to set the last move, which the PatternPolicy uses to determine where to look for patterns. */
	protected void setLastMove(String p) {
		board.getMoves()[board.getTurn() - 1] = at(p);
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
			moves.add(at("d13"));
			moves.add(at("e13"));
			moves.add(at("e15"));
			setLastMove("d14");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));

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
			moves.add(at("c3"));
			moves.add(at("d3"));
			moves.add(at("d5"));
			setLastMove("c4");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
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
			setLastMove("e16");
			assertEquals(at("d15"), policy.selectAndPlayOneMove(random, board));
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
			setLastMove("e6");
			assertEquals(at("D5"), policy.selectAndPlayOneMove(random, board));
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
			moves.add(at("d15"));
			moves.add(at("f15"));
			moves.add(at("f16"));
			setLastMove("e15");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));

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
			moves.add(at("d5"));
			moves.add(at("f5"));
			moves.add(at("f6"));
			setLastMove("e5");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
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
			moves.add(at("d15"));
			moves.add(at("f15"));
			moves.add(at("f16"));
			setLastMove("e15");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
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
			moves.add(at("d5"));
			moves.add(at("f5"));
			moves.add(at("f6"));
			setLastMove("e5");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
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
			moves.add(at("b14"));
			moves.add(at("b15"));
			moves.add(at("d15"));
			setLastMove("c14");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));

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
			moves.add(at("b4"));
			moves.add(at("b5"));
			moves.add(at("d5"));
			setLastMove("c4");
			assertTrue(moves.contains(policy.selectAndPlayOneMove(random, board)));
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
			setLastMove("d16");
			assertEquals(at("d15"), policy.selectAndPlayOneMove(random, board));

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
			setLastMove("d6");
			assertEquals(at("D5"), policy.selectAndPlayOneMove(random, board));
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
			moves.add(at("d1"));
			moves.add(at("d2"));
			moves.add(at("f1"));
			moves.add(at("f2"));
			setLastMove("e1");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
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
			moves.add(at("d1"));
			moves.add(at("d2"));
			moves.add(at("f1"));
			moves.add(at("f2"));
			setLastMove("e1");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
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
			moves.add(at("d1"));
			moves.add(at("d2"));
			moves.add(at("f1"));
			setLastMove("e1");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
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
			moves.add(at("d1"));
			moves.add(at("d2"));
			moves.add(at("f1"));
			setLastMove("e1");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
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
			moves.add(at("f1"));
			moves.add(at("f3"));
			moves.add(at("g1"));
			moves.add(at("g3"));
			setLastMove("g2");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));

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
			moves.add(at("f1"));
			moves.add(at("f3"));
			moves.add(at("g1"));
			moves.add(at("g3"));
			setLastMove("g2");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
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
			setLastMove("r1");
			assertEquals(at("q1"), policy.selectAndPlayOneMove(random, board));

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
			setLastMove("g1");
			assertEquals(at("f1"), policy.selectAndPlayOneMove(random, board));
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
			moves.add(at("q1"));
			moves.add(at("s1"));
			moves.add(at("s2"));
			setLastMove("r2");
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
			moves.add(at("f1"));
			moves.add(at("h1"));
			moves.add(at("h2"));
			moves.add(at("f3"));
			moves.add(at("g3"));
			setLastMove("g2");
		}
		assertTrue(moves.contains(policy.selectAndPlayOneMove(random, board)));
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
			moves.add(at("a17"));
			moves.add(at("b17"));
			moves.add(at("b19"));
			setLastMove("a18");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
			moves.clear();
			moves.add(at("s3"));
			moves.add(at("s1"));
			moves.add(at("t3"));
			setLastMove("t2");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
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
			moves.add(at("a7"));
			moves.add(at("b7"));
			moves.add(at("b9"));
			setLastMove("a8");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
			moves.clear();
			moves.add(at("h3"));
			moves.add(at("h1"));
			moves.add(at("j3"));
			setLastMove("j2");
			assertTrue(moves.contains(policy
					.selectAndPlayOneMove(random, board)));
		}
	}

	@Test
	public void testUpdatePriors() {
		SearchNode node = new SearchNode();
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
			node.reset(board.getHash());
			setLastMove("d14");
			policy.updatePriors(node, board, 1);
			moves.clear();
			moves.add(at("d13"));
			moves.add(at("e13"));
			moves.add(at("e15"));
			for (int i = 0; i < moves.size(); i++) {
				assertEquals(3, node.getRuns(moves.get(i)));
				assertEquals(2, node.getWins(moves.get(i)));
			}
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
			node.reset(board.getHash());
			setLastMove("c4");
			policy.updatePriors(node, board, 1);
			moves.clear();
			moves.add(at("c3"));
			moves.add(at("d3"));
			moves.add(at("d5"));
			for (int i = 0; i < moves.size(); i++) {
				assertEquals(3, node.getRuns(moves.get(i)));
				assertEquals(2, node.getWins(moves.get(i)));
			}
		}
	}

	@Test
	public void testClone() {
		policy = new PatternPolicy(new CapturePolicy());
		Policy policy2 = policy.clone();
		assertTrue(policy2.getFallback() instanceof CapturePolicy);
	}

	@Test
	public void testSelectAndPlayOneMove() {
		// We do this so many times to ensure test coverage:
		// It's quite rare for there to be several good moves,
		// all of which are infeasible
		for (int i = 0; i < 100; i++) {
			board.clear();
			int lastMove = NO_POINT;
			while (board.getPasses() < 2) {
				lastMove = policy.selectAndPlayOneMove(random, board);
			}
			assertEquals(PASS, lastMove);
		}
	}

	@Test
	public void testIsPossiblePattern() {
		assertTrue(isPossibleNeighborhood(diagramToNeighborhood("...\n. .\n.#.")));
		assertFalse(isPossibleNeighborhood(diagramToNeighborhood("...\nO #\n.*.")));
		assertFalse(isPossibleNeighborhood(diagramToNeighborhood("...\n* O\n.O.")));
		assertFalse(isPossibleNeighborhood(diagramToNeighborhood(".#.\n. *\n.#.")));
		assertTrue(isPossibleNeighborhood(diagramToNeighborhood("***\n* O\n*O#")));
		assertTrue(isPossibleNeighborhood(diagramToNeighborhood("*O#\n* O\n*OO")));
		assertTrue(isPossibleNeighborhood(diagramToNeighborhood(".O*\nO *\n***")));
		assertFalse(isPossibleNeighborhood(diagramToNeighborhood("***\n* O\n*#*")));
		assertFalse(isPossibleNeighborhood(diagramToNeighborhood("***\nO .\n*O*")));
		assertFalse(isPossibleNeighborhood(diagramToNeighborhood("*O*\n. O\n*.*")));
	}

}
