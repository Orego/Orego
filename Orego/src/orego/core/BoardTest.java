package orego.core;

import static java.lang.String.format;
import static orego.core.Board.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static orego.patterns.Pattern.*;
import static orego.heuristic.PatternHeuristic.*;
import static orego.heuristic.HeuristicList.selectAndPlayUniformlyRandomMove;
import static org.junit.Assert.*;
import orego.heuristic.*;
import orego.play.Player;
import orego.util.IntList;
import orego.util.IntSet;
import org.junit.Before;
import org.junit.Test;
import ec.util.MersenneTwisterFast;

public class BoardTest {

	private Board board;

	private MersenneTwisterFast random;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		random = new MersenneTwisterFast();
	}

	/** Verifies that p has the indicated liberties. */
	protected void assertLiberties(Board board, String p, String... liberties) {
		IntSet libs = new IntSet(FIRST_POINT_BEYOND_BOARD);
		for (String s : liberties) {
			libs.add(at(s));
		}
		assertEquals(libs, board.getLiberties(at(p)));
	}

	/** Verifies that expected and actual contain the same points. */
	protected void assertIntListAndSetAreSame(IntList expected, IntSet actual) {
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < actual.size(); i++) {
			assertTrue(expected.contains(actual.get(i)));
		}
	}

	@Test
	public void testCalculateHash() {
		board.play("a1");
		Board b2 = new Board();
		b2.copyDataFrom(board);
		// A copy should have the same hash as its original
		assertEquals(board.getHash(), b2.getHash());
		// ...until another stone is played
		b2.play("a2");
		assertFalse(board.getHash() == b2.getHash());
	}

	@Test
	public void testCapture() {
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
				"..#................",// 2
				"#OO#................"// 1
	          // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		// This move should capture two white stones
		board.play("b2");
		assertEquals(VACANT, board.getColor(at("b1")));
		assertEquals(6, board.getLibertyCount(at("b2")));
	}

	@Test
	public void testCaptureByFillingLastEye() {
		String[] problem = {
				"OO##.##..##........",// 19
				"O##.......###.....#",// 18
				"O#.##......###OO...",// 17
				"O##.#......###.....",// 16
				"O###.###.....#O#...",// 15
				"OO###.###O.........",// 14
				"OO####.##..........",// 13
				"OO#####.##..O.##...",// 12
				"OOO#####.####....O.",// 11
				"OOO######.####.....",// 10
				"OOO#######.##..##OO",// 9
				"OO#########.##..#O#",// 8
				"OO##########.###OO.",// 7
				"OOOOO########.#####",// 6
				"OOOOOOO########.###",// 5
				"OOOOOOOOOOOO#OO####",// 4
				"OOOOOOOOOOOOOOOOOO#",// 3
				"OOOOOOOOOOOOOOOOOOO",// 2
				"OOOOOOOOOOOOOOO.OOO"// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		assertLiberties(board, "b1", "q1");
		assertEquals(1, board.getLibertyCount(at("b1")));
		assertEquals(WHITE, board.getColor(at("a1")));
		// This move should capture many white stones
		assertEquals(PLAY_OK, board.play("q1"));
		assertEquals(VACANT, board.getColor(at("a1")));
	}

	@Test
	public void testCopyDataFrom() {
		String[] problem;
		problem = new String[] {
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
				"O..................",// 3
				"O##................",// 2
				".O#................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		// Capture and set up a ko
		board.play("a1");
		assertEquals(5, board.getLibertyCount(at("b2")));
		assertLiberties(board, "b2", "b1", "b3", "c3", "d2", "d1");
		assertLiberties(board, "a2", "a4", "b3");
		// Make a copy
		Board b = new Board();
		b.copyDataFrom(board);
		// Verify that the copy is correct
		assertEquals(board, b);
		IntSet vacant = board.getVacantPoints();
		for (int i = 0; i < vacant.size(); i++) {
			assertEquals(board.getNeighborhood(vacant.get(i)),
					b.getNeighborhood(vacant.get(i)));
		}
		for (int t = 0; t < board.getTurn(); t++) {
			assertEquals(board.getMove(t), b.getMove(t));
		}
		assertEquals(5, b.getLibertyCount(at("b2")));
		assertLiberties(b, "b2", "b1", "b3", "c3", "d2", "d1");
		assertLiberties(b, "a2", "a4", "b3");
		// Playing a move on the copy should affect its hash
		long before = board.getHash();
		String beforeString = board.toString();
		assertEquals(before, b.getHash());
		b.play("a4");
		assertFalse(before == b.getHash());
		// The original should not be affected
		assertEquals(before, board.getHash());
		assertEquals(beforeString, board.toString());
	}

	@Test
	public void testCountPasses() {
		assertEquals(0, board.getPasses());
		board.play(PASS);
		assertEquals(1, board.getPasses());
		board.play(PASS);
		assertEquals(2, board.getPasses());
		board.play("d3");
		assertEquals(0, board.getPasses());
	}

	@Test
	public void testDebug1() {
		String[] problem = {
				"#########OOOOOOOOOO",// 19
				"#########OOOOOOOOOO",// 18
				"#########OOOOOOOOOO",// 17
				"#########OOOOOOOOOO",// 16
				"#########OOOOOOOOOO",// 15
				"##########OOOOOOOOO",// 14
				"##########OOOOOOOOO",// 13
				"##########OOOOOOOOO",// 12
				"##########OOOOOOOOO",// 11
				"##########OOOOOOOOO",// 10
				"..##.#..###########",// 9
				"..#O.#.#.##########",// 8
				"..#O##.##OOOOOOOOOO",// 7
				"...#.##O########OOO",// 6
				"..###OOOOOOOOO#####",// 5
				"###OOOOOOOOO#######",// 4
				"OOOO.O#############",// 3
				"#OOOO##OOOOOOOOO###",// 2
				".O###.#O.##########"// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		// The program was previously crashing in response to these moves
		board.play("a1");
		board.play("j1");
	}

	@Test
	public void testDebug2() {
		String[] problem = {
				"#########OOOOOOOOOO",// 19
				"#########OOOOOOOOOO",// 18
				"#########OOOOOOOOOO",// 17
				"#########OOOOOOOOOO",// 16
				"#########OOOOOOOOOO",// 15
				"##########OOOOOOOOO",// 14
				"##########OOOOOOOOO",// 13
				"##########OOOOOOOOO",// 12
				"##########OOOOOOOOO",// 11
				"##########OOOOOOOOO",// 10
				".O#.#OO..OOOOOOOOOO",// 9
				"OO####OOOOOOOOOOOOO",// 8
				".###OOO############",// 7
				"####OO#.###########",// 6
				"OO#OO##O###########",// 5
				"OOOOO##############",// 4
				".O###.#..OOOOOOOOOO",// 3
				"##O##..#OOOOOOOOOOO",// 2
				".#O.#.#OOOOOOOOOOOO"// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		// The program was previously crashing in response to these moves
		board.play("a5");
		board.play("b8");
		board.play("h9");
		board.play("e9");
		board.play("b7");
		board.play("b9");
		board.play("b6");
		board.play("a8");
		board.play("g7");
		board.play("j8");
		board.play("f1");
	}

	@Test
	public void testDistantLiberty() {
		board.play(PASS);
		board.play("a1");
		board.play("b2");
		board.play("b1");
		// The next move should not capture because white
		// still has a liberty at a2
		board.play("c1");
		assertEquals(WHITE, board.getColor(at("b1")));
	}

	@Test
	public void testFinalScore() {
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
				"OOOOOOOOO..........",// 5
				"########O..........",// 4
				"########O..........",// 3
				"########O..........",// 2
				".#.#####O.........."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		assertEquals(10 + (2 - (50 + (14 * 19))), board.finalScore());
	}

	@Test
	public void testForcedPassInLongPlayout() {
		for (int i = 0; i < MAX_MOVES_PER_GAME - 3; i++) {
			board.play(PASS);
		}
		assertEquals(PLAY_OK, board.play("a1"));
		// At this point, so many moves have been played that
		// only passes should be legal
		assertEquals(PLAY_GAME_TOO_LONG, board.play("b1"));
	}

	@Test
	public void testGetCapturePoint() {
		String[] problem = {
				".OO................",// 19
				"O##O...............",// 18
				".O.................",// 17
				"...................",// 16
				".##................",// 15
				"#OO#...............",// 14
				"#O.O#..............",// 13
				"#OOO#..............",// 12
				".###...............",// 11
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
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		assertEquals(at("c17"), board.getCapturePoint(at("c18")));
		assertEquals(at("c13"), board.getCapturePoint(at("c12")));
		assertEquals(NO_POINT, board.getCapturePoint(at("a12")));
	}

	@Test
	public void testGetChainIds() {
		board.play(at("a1"));
		board.pass();
		board.play(at("a2"));
		// These adjacent stones should be in the same chain
		assertEquals(board.getChainId(at("a1")), board.getChainId(at("a2")));
	}

	@Test
	public void testGetColorToPlay() {
		assertEquals(BLACK, board.getColorToPlay());
		board.play(at("d2"));
		assertEquals(WHITE, board.getColorToPlay());
		board.play(at("d3"));
		assertEquals(BLACK, board.getColorToPlay());
	}

	@Test
	public void testGetLibertiesOfChain() {
		String[] problem = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"............O......",// 16
				"...........O#O.....",// 15
				"............#O.....",// 14
				"............O......",// 13
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
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		IntList liberties = new IntList(3);
		board.getLibertiesByTraversal(at("n15"), liberties);
		assertIntListAndSetAreSame(liberties, board.getLiberties(at("n15")));
		assertEquals(at("m14"), liberties.get(0));
		assertLiberties(board, "n15", "m14");
	}

	@Test
	public void testGetTurn() {
		assertEquals(0, board.getTurn());
		for (int i = 0; i < 10; i++) {
			selectAndPlayUniformlyRandomMove(random, board);
		}
		assertEquals(10, board.getTurn());
		for (int i = 0; i < 10; i++) {
			board.play(PASS);
		}
		assertEquals(20, board.getTurn());
	}

	@Test
	public void testGetVacantPoints() {
		String[] problem = {
				"#########OOOOOOOOOO", // 19
				"#########OOOOOOOOOO", // 18
				"#########OOOOOOOOOO", // 17
				"#########OOOOOOOOOO", // 16
				"#########OOOOOOOOOO", // 15
				"#########OOOOOOOOOO", // 14
				"#########OOOOOOOOOO", // 13
				"#########OOOOOOOOOO", // 12
				"#########OOOOOOOOOO", // 11
				"#########OOOOOOOOOO", // 10
				"#########OOOOOOOOOO", // 9
				"#########OOOOOOOOOO", // 8
				"#########OOOOOOOOOO", // 7
				"#########OOOOOOOOOO", // 6
				"#########OOOOOOOOOO", // 5
				"#########OOOOOOOOOO", // 4
				"........#O.........", // 3
				".......#O.O........", // 2
				"........#O.........", // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		IntSet vacant = board.getVacantPoints();
		int hits = 0;
		assertEquals(50, vacant.size());
		// Two of the following points should be vacant
		for (int i = 0; i < vacant.size(); i++) {
			if (vacant.get(i) == at("h1")) {
				hits++;
			}
			if (vacant.get(i) == at("j2")) {
				hits++;
			}
			if (vacant.get(i) == at("k2")) {
				hits++;
			}
		}
		assertEquals(2, hits);
		// Take the ko
		board.play(at("k2"));
		vacant = board.getVacantPoints();
		hits = 0;
		assertEquals(50, vacant.size());
		// Two of the following points should be vacant
		for (int i = 0; i < vacant.size(); i++) {
			if (vacant.get(i) == at("h1")) {
				hits++;
			}
			if (vacant.get(i) == at("j2")) {
				hits++;
			}
			if (vacant.get(i) == at("k2")) {
				hits++;
			}
		}
		assertEquals(2, hits);
	}

	@Test
	public void testIgnoreChainIdsOfDeadPoints() {
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
				"OOO................",// 2
				"###................" // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		board.play("d1");
		board.play("c1");
		board.play(PASS);
		board.play("a1");
		// b1 is a liberty, not a friendly neighbor, of c1
		assertEquals(1, board.getLibertyCount(at("c1")));
	}

	@Test
	public void testIsEyelike() {
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
				".......O.O#........",// 6
				"......O.O.O........",// 5
				".......O.O#........",// 4
				"...................",// 3
				"O#..O...O#........O",// 2
				".O.O.O.O.O.......O." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		// The result of isEyelike() depends on the player to play
		board.setUpProblem(WHITE, problem);
		assertFalse(board.isEyelike(at("a1")));
		assertTrue(board.isEyelike(at("e1")));
		assertFalse(board.isEyelike(at("j1")));
		assertTrue(board.isEyelike(at("t1")));
		assertTrue(board.isEyelike(at("h5")));
		assertFalse(board.isEyelike(at("k5")));
		board.setUpProblem(BLACK, problem);
		assertFalse(board.isEyelike(at("a1")));
		assertFalse(board.isEyelike(at("e1")));
		assertFalse(board.isEyelike(at("j1")));
		assertFalse(board.isEyelike(at("t1")));
		assertFalse(board.isEyelike(at("h5")));
		assertFalse(board.isEyelike(at("k5")));
	}

	@Test
	public void testIsFeasible() {
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
				"..........#........",// 4
				".........#.#.......",// 3
				"..........#........",// 2
				"..................." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		assertFalse(board.isFeasible(at("a2")));
		assertTrue(board.isFeasible(at("n1")));
		assertFalse(board.isFeasible(at("l3")));
	}

	@Test
	public void testIsKnight() {
		String[] problem = { "...................",// 19
				"..............#....",// 18
				"...................",// 17
				"................O..",// 16
				"...#...............",// 15
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
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		// Moves should be legal if 3 or 4 from edge
		assertFalse(board.isWithinAKnightsMoveOfAnotherStone(at("a1")));
		assertFalse(board.isWithinAKnightsMoveOfAnotherStone(at("e2")));
		assertFalse(board.isWithinAKnightsMoveOfAnotherStone(at("g8")));
		assertFalse(board.isWithinAKnightsMoveOfAnotherStone(at("h14")));
		assertFalse(board.isWithinAKnightsMoveOfAnotherStone(at("j4")));
		assertTrue(board.isWithinAKnightsMoveOfAnotherStone(at("c17")));
		assertTrue(board.isWithinAKnightsMoveOfAnotherStone(at("e14")));
		assertTrue(board.isWithinAKnightsMoveOfAnotherStone(at("f15")));
		assertTrue(board.isWithinAKnightsMoveOfAnotherStone(at("b15")));
		assertTrue(board.isWithinAKnightsMoveOfAnotherStone(at("b16")));
		assertTrue(board.isWithinAKnightsMoveOfAnotherStone(at("c14")));
	}

	@Test
	public void testIsLargeKnight() {
		String[] problem = { "...................",// 19
				"..............#....",// 18
				"...................",// 17
				"................O..",// 16
				"...#...............",// 15
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
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		// Moves should be legal if 3 or 4 from edge
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("a1")));
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("e2")));
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("g8")));
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("h14")));
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("j4")));
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("d11")));
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("e11")));
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("f12")));
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("g13")));
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("h14")));
		assertFalse(board.isWithinALargeKnightsMoveOfAnotherStone(at("h15")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("c17")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("e14")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("f15")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("b15")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("b16")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("c14")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("d12")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("e12")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("f13")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("g14")));
		assertTrue(board.isWithinALargeKnightsMoveOfAnotherStone(at("g15")));

	}

	@Test
	public void testIsLegal() {
		String[] problem = { ".OO................",// 19
				"O##O...............",// 18
				".O.................",// 17
				"...................",// 16
				".##................",// 15
				"#OO#...............",// 14
				"#O.O#..............",// 13
				"#OOO#..............",// 12
				".###...............",// 11
				"...................",// 10
				"...................",// 9
				"...........####OOOO",// 8
				"...........#####OO.",// 7
				".........#######OOO",// 6
				"..........##OO###OO",// 5
				"..........OOO#.O###",// 4
				"...........OO####.#",// 3
				"..........O.O##.##.",// 2
				"..................."// 1
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		assertFalse(board.isLegal(at("p4")));
		assertFalse(board.isLegal(at("t3")));
	}

	@Test
	public void testIsLegalOnEdge() {
		String[] problem = { ".OO................",// 19
				"O##O...............",// 18
				".O.................",// 17
				"...................",// 16
				".##................",// 15
				"#OO#...............",// 14
				"#O.O#..............",// 13
				"#OOO#..............",// 12
				".###...............",// 11
				"...................",// 10
				"#.####OOO..........",// 9
				".#####OO...........",// 8
				".#####OOO..........",// 7
				"O####OOO...........",// 6
				".###OOOOO..........",// 5
				"##OO###OO..........",// 4
				"OOO#.####..........",// 3
				".OO####.#..........",// 2
				"O.O#.O##..........."// 1
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		assertFalse(board.isLegal(at("e1")));
		assertTrue(board.isLegal(at("a7")));
	}

	@Test
	public void testKoDoesNotExtendThroughPass() {
		String[] problem = { "...................",// 19
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
				"..OO#............#O",// 2
				".OO.O#.............."// 1
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.play("d1");
		assertEquals(PLAY_KO_VIOLATION, board.play("e1"));
		board.play(PASS);
		assertEquals(PLAY_OK, board.play("e1"));
	}

	@Test
	public void testNoFalseKoAlarm() {
		String[] problem = { "...................",// 19
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
				"....OO#..........#O",// 2
				"...O#.O#..........."// 1
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.play("f1");
		assertEquals(PLAY_OK, board.play("g1"));
	}

	@Test
	public void testPassingAffectsZobristHash() {
		long a = board.getHash();
		board.play(PASS);
		long b = board.getHash();
		board.play(PASS);
		long c = board.getHash();
		assertFalse(a == b);
		assertFalse(b == c);
		// Color to play, but not # of passes, is included in hash
		assertEquals(a, c);
	}

	@Test
	public void testPatterns() {
		board.play(at("a5"));
		board.play(at("a6"));
		board.play(at("b4"));
		board.play(at("c6"));
		board.play(at("a2"));
		board.play(at("b2"));
		IntSet vacant = board.getVacantPoints();
		for (int i = 0; i < vacant.size(); i++) {
			int p = vacant.get(i);
			assertTrue(
					format("Invalid pattern at %s:\n%s", pointToString(p),
							neighborhoodToDiagram(board.getNeighborhood(p))),
					isPossibleNeighborhood(board.getNeighborhood(p)));
		}
		assertEquals(diagramToNeighborhood("...\n. .\n..."),
				board.getNeighborhood(at("e5")));
		assertEquals(diagramToNeighborhood("*#O\n* .\n***"),
				board.getNeighborhood(at("a1")));
		assertEquals(diagramToNeighborhood("*#.\n* #\n*.."),
				board.getNeighborhood(at("a4")));
		assertEquals(diagramToNeighborhood("O.O\n# .\n.#."),
				board.getNeighborhood(at("b5")));
		assertEquals(diagramToNeighborhood("...\nO O\n#.."),
				board.getNeighborhood(at("b6")));
		board.playFast(at("b6"));
		assertEquals(diagramToNeighborhood("O#O\n# .\n.#."),
				board.getNeighborhood(at("b5")));
	}

	@Test
	public void testPatternsWithCapture() {
		String[] problem = { ".........",// 9
				".........",// 8
				".........",// 7
				".........",// 6
				".........",// 5
				".........",// 4
				".##......",// 3
				"#OO......",// 2
				".##......",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		board.play(at("D2"));
		assertEquals(diagramToNeighborhood(".##\n# .\n.##"),
				board.getNeighborhood(at("b2")));
		assertEquals(diagramToNeighborhood("##.\n. #\n##."),
				board.getNeighborhood(at("c2")));
	}

	@Test
	public void testGetNeighborhoodOppositeColor() {
		String[] problem = { ".........",// 9
				".........",// 8
				".........",// 7
				"...O#O...",// 6
				".....#...",// 5
				"....OO...",// 4
				".........",// 3
				".#O......",// 2
				"#........",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		assertEquals(diagramToNeighborhood("#O#\n. O\n.##"),
				board.getNeighborhoodColorsReversed(at("e5")));
		assertEquals(diagramToNeighborhood(".O#\nO .\n***"),
				board.getNeighborhoodColorsReversed(at("b1")));
	}

	@Test
	public void testPlayFast() {
		board.play("a2");
		board.play("b2");
		board.play("b1");
		board.play("c1");
		board.play(PASS);
		board.play("a1");
		assertEquals(PLAY_KO_VIOLATION, board.playFast(at("b1")));
	}

	@Test
	public void testPlayOnOccupiedPoint() {
		board.play("b2");
		assertEquals(PLAY_OCCUPIED, board.play("b2"));
	}

	@Test
	public void testPlayRich() {
		String[] problem = { ".OO................",// 19
				"O##O...............",// 18
				".O.................",// 17
				"...................",// 16
				".##................",// 15
				"#OO#...............",// 14
				"#O.O#..............",// 13
				"#OOO#..............",// 12
				".###...............",// 11
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
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		assertFalse(board.isLegal(at("c13")));
		assertEquals(PLAY_SUICIDE, board.play(at("c13")));
		assertEquals(WHITE, board.getColor(at("c12")));
	}

	@Test
	public void testPlayRichUpdatesZobristHash() {
		String[] problem = { ".OO................",// 19
				"O##O...............",// 18
				".O.................",// 17
				"...................",// 16
				".##................",// 15
				"#OO#...............",// 14
				"#O.O#..............",// 13
				"#OOO#..............",// 12
				".###...............",// 11
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
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		Board copy = new Board();
		copy.copyDataFrom(board);
		board.play(at("c17"));
		assertFalse(board.getHash() == copy.getHash());
		copy.play(at("c17"));
		assertEquals(board.getHash(), copy.getHash());
	}

	@Test
	public void testPositionalSuperko() {
		String[] problem = { "...................",// 19
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
				"O.#................",// 2
				".O.#..............." // 1
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.play("b2");
		board.play("c1");
		board.play("a1");
		assertFalse(board.isLegal(at("b1")));
		assertEquals(PLAY_KO_VIOLATION, board.play(at("b1")));
		board.play(at("b1"));
	}

	@Test
	public void testSameChainNeighborTwice() {
		board.play("b1");
		board.play(PASS);
		board.play("a1");
		board.play(PASS);
		board.play("a2");
		assertEquals(3, board.getLibertyCount(at("a1")));
		board.play(PASS);
		board.play("b2");
		assertEquals(4, board.getLibertyCount(at("b2")));
	}

	@Test
	public void testScore() {
		String[] problem = { ".##.#.#.#.#O.O.O.O.",// 19
				"##.#.#.#.##OO.O.O.O",// 18
				"#.#.#.#.#.#O.O.O.O.",// 17
				"##.#.#.#.##OO.O.O.O",// 16
				".##.#.#.#.#O.O.O.O.",// 15
				"##.#.#.#.##OO.O.O.O",// 14
				"#.#.#.#.#.#O.O.O.O.",// 13
				"##.#.#.#.##OO.O.O.O",// 12
				".##.#.#.#.#O.O.O.O.",// 11
				"##.#.#.#.##OO.O.O.O",// 10
				"#.#.#.#.#.#O.O.O.O.",// 9
				"##.#.#.#.##OO.O.O.O",// 8
				".##.#.#.#.#O.O.O.O.",// 7
				"##.#.#.#.##OO.O.O.O",// 6
				"#.#.#.#.#.#O.O.O.O.",// 5
				"##.#.#.#.##OO.O.O.O",// 4
				".##.#.#.#.#O.O.O.O.",// 3
				"##.#.#.#.##OO.O.O.O",// 2
				"#.#.#.#.#.#O.O.O.O.",// 1
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.setKomi(7);
		assertEquals(50, board.playoutScore());
		assertEquals(BLACK, board.playoutWinner());
	}

	@Test
	public void testScore2() {
		String[] problem = { ".OO###############.",// 19
				"O.OO##########.#.##",// 18
				"##OOOOO##.##.#.#.#.",// 17
				"##OOOOO##.###..####",// 16
				".###OOOOO##.#.##.#.",// 15
				"#.####OO##.#####.##",// 14
				"##.###OOO#######.#.",// 13
				".#.#.##OOOOO######.",// 12
				"####.###OOOO####.##",// 11
				".#.####OOO########.",// 10
				"#.#.#.#OO####.#.###",// 9
				"##.#.#.#OO##..##.#.",// 8
				".####.###O###..####",// 7
				"####.###OO#.#.##.#.",// 6
				"#.#####OO#.#####.##",// 5
				"##.###OOO#######.##",// 4
				".###.##OO#.#.#####.",// 3
				"###.###OO##.####.##",// 2
				".###.###OO##.##.##.",// 1
		};
		board.setUpProblem(BLACK, problem);
		board.setKomi(7);
		assertEquals(214, board.playoutScore());
	}

	@Test
	public void testScore3() {
		board.setKomi(7);
		String[] problem = { "OO##.##...OO##.##..", // 19
				"O#.##.#O#OO##.##OO.", // 18
				"OO##.####.OO####.#.", // 17
				"O.O#OO####OOOOO.OO#", // 16
				".OOOOOO.OOOOO.O.OO.", // 15
				"O#.##.#OO.O##.##OO.", // 14
				"OO##.####OOO####.#.", // 13
				"O.O#OO##O.OOOOO.OO#", // 12
				".OOOOOO.OOOOO.O.OO.", // 11
				"OO##.##...OO##.##..", // 10
				"O#.##.#O##O##.##OO.", // 9
				"OO##.####.OO####.#.", // 8
				"O.O#OO####OOOOO.OO#", // 7
				".OOOOOO.O.OOO.O.OO.", // 6
				"O#.##.#O#.O.OO##OO.", // 5
				"OO##.####.OO####.#.", // 4
				"O.O#OO###.OOOOOOOO#", // 3
				".OOO##..#.OOO.O.OO.", // 2
				"OOOOOOO.O.OOOOOOOO." // 1
		};
		board.setUpProblem(BLACK, problem);
		assertEquals(-56, board.playoutScore());
	}

	@Test
	public void testSimpleKoIncludedInZobristHash() {
		String[] problem = { "...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				".OO................",// 9
				"O##O...............",// 8
				".O.................",// 7
				"...................",// 6
				".##................",// 5
				"#OO#...............",// 4
				".O.O#..............",// 3
				"#OO#...............",// 2
				".###..............."// 1
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		board.play(at("c3"));
		String before = board.toString();
		int ko = board.getKoPoint();
		long hashWithKo = board.getHash();
		problem = new String[] { "...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				".OO................",// 9
				"O##O...............",// 8
				".O.................",// 7
				"...................",// 6
				".##................",// 5
				"#OO#...............",// 4
				".O..#..............",// 3
				"#OO#...............",// 2
				".###..............."// 1
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		assertFalse(board.isSuicidal(at("c3")));
		assertTrue(board.isLegal(at("c3")));
		board.play(at("c3"));
		assertEquals(before, board.toString());
		long hashWithoutKo = board.getHash();
		assertFalse(ko == board.getKoPoint());
		assertFalse(hashWithKo == hashWithoutKo);
	}

	@Test
	public void testSimpleKoViolation() {
		board.play("b2");
		board.play("a2");
		board.play("c1");
		board.play("b1");
		board.play("a1");
		assertFalse(board.isLegal(at("b1")));
		assertEquals(PLAY_KO_VIOLATION, board.play("b1"));
	}

	@Test
	public void testSingleStoneSuicide() {
		board.play("a2");
		board.play(PASS);
		board.play("b1");
		assertEquals(PLAY_SUICIDE, board.play("a1"));
	}

	@Test
	public void testSuicide() {
		String[] problem = { "...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"##.................",// 14
				"OO##..........##...",// 13
				"O#.##...#O#...OO...",// 12
				"O##...##OO.........",// 11
				"OO##...####........",// 10
				"OO#####......#.....",// 9
				"OOO#OO###...##.....",// 8
				"OOOOOOOOO#.........",// 7
				"OOOOOOO###.........",// 6
				"OOOOO##............",// 5
				"OOO###.............",// 4
				"OOOOO##............",// 3
				"OOOOOO##...........",// 2
				"OO.OOOOO##........."// 1
		// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		assertEquals(PLAY_SUICIDE, board.playFast(at("c1")));
	}

	@Test
	public void testChainsInAtariWithKo() {
		String[] problem = { ".....#...", // 9
				"....#O#..", // 8
				"....#O#..", // 7
				"....O.O..", // 6
				"...O.O...", // 5
				"..O#O....", // 4
				"..#.#....", // 3
				"...#.....", // 2
				".........", // 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		assertEquals(1, board.getLibertyCount(at("f7")));
		assertEquals(1, board.getLibertyCount(at("d4")));
		assertTrue(board.getChainsInAtari(WHITE).contains(
				board.getChainId(at("f7"))));
		assertTrue(board.getChainsInAtari(BLACK).contains(
				board.getChainId(at("d4"))));
		board.play(at("f6"));
		assertEquals(1, board.getLibertyCount(at("f6")));
		assertTrue(board.getChainsInAtari(BLACK).contains(
				board.getChainId(at("f6"))));
		board.play(at("d3"));
		assertEquals(1, board.getLibertyCount(at("d3")));
		assertTrue(board.getChainsInAtari(WHITE).contains(
				board.getChainId(at("d3"))));
		board.play(at("a1"));
		assertEquals(1, board.getLibertyCount(at("d3")));
		assertTrue(board.getChainsInAtari(WHITE).contains(
				board.getChainId(at("d3"))));
	}

	@Test
	public void testWinner() {
		board.setColorToPlay(WHITE);
		board.play(at("a1"));
		assertEquals(WHITE, board.approximateWinner());
		assertEquals(WHITE, board.finalWinner());
		for (int i = 1; i <= 9; i++) {
			board.setColorToPlay(BLACK);
			board.play(at("b" + i));
		}
		assertEquals(BLACK, board.approximateWinner());
		assertEquals(BLACK, board.finalWinner());
	}

	@Test
	public void testToProblemString() {
		MersenneTwisterFast random = new MersenneTwisterFast();
		for (int i = 0; i < 10; i++) {
			selectAndPlayUniformlyRandomMove(random, board);
		}
		String[] problem = board.toProblemString();
		Board copy = new Board();
		copy.setUpProblem(BLACK, problem);
		assertEquals(board.toString(), copy.toString());
	}

	@Test
	public void testSetPasses() {
		board.pass();
		board.pass();
		assertEquals(2, board.getPasses());
		board.setPasses(0);
		assertEquals(0, board.getPasses());
	}

	@Test
	public void testGetMove() {
		assertEquals(NO_POINT, board.getMove(-1));
		board.play("a1");
		board.play(PASS);
		board.play("e3");
		assertEquals(at("a1"), board.getMove(0));
		assertEquals(PASS, board.getMove(1));
		assertEquals(at("e3"), board.getMove(2));
	}

	@Test
	public void testEquals() {
		MersenneTwisterFast random = new MersenneTwisterFast();
		for (int i = 0; i < 30; i++) {
			selectAndPlayUniformlyRandomMove(random, board);
		}
		Board backup = new Board();
		backup.copyDataFrom(board);
		assertEquals(board, board);
		assertFalse(board.equals(null));
		assertFalse(board.equals(5));
		selectAndPlayUniformlyRandomMove(random, board);
		assertFalse(board.equals(backup));
		board.copyDataFrom(backup);
		for (int p : ALL_POINTS_ON_BOARD) {
			if (board.getColor(p) != VACANT) {
				IntSet liberties = board.getLiberties(p);
				if (liberties.size() > 0) {
					liberties.removeKnownPresent(liberties.get(0));
				}
			}
		}
		assertFalse(board.equals(backup));
		board.copyDataFrom(backup);
		board.getVacantPoints().add(NO_POINT);
		assertFalse(board.equals(backup));
		board.copyDataFrom(backup);
		board.getStoneCounts()[BLACK]++;
		assertFalse(board.equals(backup));
		board.copyDataFrom(backup);
		board.setKoPoint(FIRST_POINT_ON_BOARD - 1);
		assertFalse(board.equals(backup));
		board.copyDataFrom(backup);
		board.setHash(~board.getHash());
		assertFalse(board.equals(backup));
		board.copyDataFrom(backup);
		board.setColorToPlay(opposite(board.getColorToPlay()));
		board.setHash(~board.getHash());
		assertFalse(board.equals(backup));
		board.copyDataFrom(backup);
		board.setTurn(0);
		assertFalse(board.equals(backup));
		board.copyDataFrom(backup);
		board.setPasses(board.getPasses() + 1);
		assertFalse(board.equals(backup));
	}

	@Test
	public void testLocalPatterns() {
		String[] problem = { ".........", // 9
				".#.......", // 8
				".O.......", // 7
				"...O.....", // 6
				"..O.#....", // 5
				"....O....", // 4
				".........", // 3
				"#O..O#O..", // 2
				".O..O.#..", // 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		localPatternEqual("*#O\n* O\n***", at("a1"));
		localPatternEqual("O..\nO .\n***", at("c1"));
		localPatternEqual("O#O\nO #\n***", at("f1"));
		localPatternEqual(".O.\nO #\n..O", at("d5"));
		localPatternEqual("*..\n* #\n*.O", at("a8"));
		localPatternEqual("...\n. .\n...", at("h8"));
	}

	protected void localPatternEqual(String pattern, int p) {
		char pat = board.getNeighborhood(p);
		assertEquals(
				format("Expected\n%s, but was\n%s.", pattern,
						neighborhoodToDiagram(pat)),
				diagramToNeighborhood(pattern), pat);
	}

	@Test
	public void testAtariLiberties1() {
		String[] problem = { ".........", // 9
				".........", // 8
				".........", // 7
				".........", // 6
				"#O#......", // 5
				"#O#......", // 4
				"O#.......", // 3
				".........", // 2
				"O#.......", // 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(WHITE, problem);
		assertEquals(at("a2"),
				board.getLibertyOfChainInAtari(board.getChainId(at("a1"))));
		assertEquals(at("a6"),
				board.getLibertyOfChainInAtari(board.getChainId(at("a5"))));
		assertEquals(at("a2"),
				board.getLibertyOfChainInAtari(board.getChainId(at("a3"))));
		assertEquals(at("b6"),
				board.getLibertyOfChainInAtari(board.getChainId(at("b5"))));
		assertLiberties(board, "a1", "a2");
		assertLiberties(board, "a5", "a6");
		assertLiberties(board, "a3", "a2");
		assertLiberties(board, "b5", "b6");
		board.play(at("c3"));
		assertEquals(at("a6"),
				board.getLibertyOfChainInAtari(board.getChainId(at("a5"))));
		assertEquals(at("a2"),
				board.getLibertyOfChainInAtari(board.getChainId(at("a3"))));
		assertEquals(at("b6"),
				board.getLibertyOfChainInAtari(board.getChainId(at("b5"))));
		assertEquals(at("b2"),
				board.getLibertyOfChainInAtari(board.getChainId(at("b3"))));
		assertLiberties(board, "a5", "a6");
		assertLiberties(board, "a3", "a2");
		assertLiberties(board, "b5", "b6");
		assertLiberties(board, "b3", "b2");
		board.play(at("b6"));
		assertEquals(at("a2"),
				board.getLibertyOfChainInAtari(board.getChainId(at("a3"))));
		assertLiberties(board, "a3", "a2");
	}

	@Test
	public void testAtariLiberties2() {
		String[] problem = { ".....#...", // 9
				"....#O#..", // 8
				"....#.#..", // 7
				"......O..", // 6
				"#O#..O.O.", // 5
				"#O#..O#O.", // 4
				"O#.......", // 3
				".....O.O.", // 2
				"O#....O..", // 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(WHITE, problem);
		board.play(at("a2"));
		assertEquals(at("b2"),
				board.getLibertyOfChainInAtari(board.getChainId(at("a1"))));
		assertLiberties(board, "a1", "b2");
		board.play(at("g2"));
		assertEquals(at("g3"),
				board.getLibertyOfChainInAtari(board.getChainId(at("g2"))));
		assertLiberties(board, "g2", "g3");
		board.play(at("f7"));
		assertEquals(at("f6"),
				board.getLibertyOfChainInAtari(board.getChainId(at("f8"))));
		assertLiberties(board, "f8", "f6");
		board.play(at("g5"));
		assertEquals(at("g3"),
				board.getLibertyOfChainInAtari(board.getChainId(at("g4"))));
		assertLiberties(board, "g4", "g3");
	}

	@Test
	public void testLibertiesLots() {
		final int GAMES = 500;
		IntList liberties = new IntList(FIRST_POINT_BEYOND_BOARD);
		Player player = new Player();
		player.setHeuristics(new HeuristicList("Capture@1:Pattern@1"));
		player.reset();
		MersenneTwisterFast random = new MersenneTwisterFast();
		for (int game = 0; game < GAMES; game++) {
			do {
				IntSet vacant = board.getVacantPoints();
				for (int i = 0; i < vacant.size(); i++) {
					int p = vacant.get(i);
					if (board.getColor(p) < VACANT) {
						int libertyCount = board.getLibertyCount(p);
						final String pstr = pointToString(p);
						board.getLibertiesByTraversal(p, liberties);
						if (libertyCount == 1) {
							assertLiberties(board, pstr,
									pointToString(liberties.get(0)));
							assertEquals(
									format("Failed at %s on \n%sLast move was %s",
											pstr, board,
											pointToString(board.getMove(board
													.getTurn() - 1))),
									liberties.get(0), board.getCapturePoint(p));
							assertTrue(
									format("Failed at %s on \n%sLast move was %s",
											pstr, board,
											pointToString(board.getMove(board
													.getTurn() - 1))),
									board.getChainsInAtari(board.getColor(p))
											.contains(board.getChainId(p)));
						} else {
							assertEquals(
									format("Failed at %s on \n%sLast move was %s",
											pstr, board,
											pointToString(board.getMove(board
													.getTurn() - 1))),
									NO_POINT, board.getCapturePoint(p));
							assertIntListAndSetAreSame(liberties,
									board.getLiberties(p));
						}
					}
				}
			} while ((board.getTurn() < MAX_MOVES_PER_GAME)
					&& (player.selectAndPlayOneMove(random, board) != PASS));
			board.clear();
		}
	}

	@Test
	public void testSelfAtari() {
		String[] problem = { "OOOOOOOO.",// 9
				"O#.#O#.#.",// 8
				"OO.OOO.O.",// 7
				".........",// 6
				"......O.O",// 5
				".......O.",// 4
				"##....#..",// 3
				"OO#..#O#.",// 2
				".O#....O#",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		assertFalse(board.isSelfAtari(at("a1"), board.getColorToPlay()));
		assertFalse(board.isSelfAtari(at("g1"), board.getColorToPlay()));
		assertFalse(board.isSelfAtari(at("g8"), board.getColorToPlay()));
		assertFalse(board.isSelfAtari(at("j3"), board.getColorToPlay()));
		assertTrue(board.isSelfAtari(at("h5"), board.getColorToPlay()));
		assertTrue(board.isSelfAtari(at("c8"), board.getColorToPlay()));
		board.play(at("a1"));
		assertTrue(board.isSelfAtari(at("g1"), board.getColorToPlay()));
		assertFalse(board.isSelfAtari(at("c8"), board.getColorToPlay()));
		assertFalse(board.isSelfAtari(at("h5"), board.getColorToPlay()));
	}

	@Test
	public void testSelfAtarisSimple() {
		String[] problem = { ".###.....",// 9
				"#O.O#....",// 8
				"...#.....",// 7
				".........",// 6
				".........",// 5
				"...O.O...",// 4
				".........",// 3
				".........",// 2
				".........",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		assertFalse(board.isSelfAtari(at("e4"), board.getColorToPlay()));
		board.play(at("b7"));
		assertTrue(board.isSelfAtari(at("c8"), board.getColorToPlay()));
		assertFalse(board.isSelfAtari(at("c7"), board.getColorToPlay()));
		board.play(at("e3"));
		assertTrue(board.isSelfAtari(at("e4"), board.getColorToPlay()));
		board.pass();
		board.play(at("c7"));
		assertFalse(board.isSelfAtari(at("c8"), board.getColorToPlay()));
	}

	@Test
	public void testSelfAtariDistant() {
		String[] problem = { ".........",// 9
				"..#####..",// 8
				"..#OOO#..",// 7
				"..#.OO#..",// 6
				"..#OOOO..",// 5
				"..#OOO#..",// 4
				"..#####..",// 3
				".........",// 2
				".........",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(WHITE, problem);
		assertTrue(board.isSelfAtari(at("d6"), board.getColorToPlay()));
	}

	@Test
	public void testSelfAtariDistant2() {
		String[] problem = { ".........",// 9
				"..#####..",// 8
				"..#.OO#..",// 7
				"..#.OO#..",// 6
				"..#.OO#..",// 5
				"..#OOO#..",// 4
				"..#####..",// 3
				".........",// 2
				".........",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(WHITE, problem);
		assertFalse(board.isSelfAtari(at("d6"), board.getColorToPlay()));
		board.play(at("d6"));
		board.pass();
		assertTrue(board.isSelfAtari(at("d5"), board.getColorToPlay()));
		assertTrue(board.isSelfAtari(at("d7"), board.getColorToPlay()));
	}

	@Test
	public void testSelfAtariSharedDistantLiberty() {
		String[] problem = { ".........",// 9
				".........",// 8
				".........",// 7
				".........",// 6
				".OO......",// 5
				"#.#O.....",// 4
				"O#.......",// 3
				"OO.......",// 2
				".........",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		assertFalse(board.isSelfAtari(at("b4"), board.getColorToPlay()));
		board.pass();
		board.play(at("a5"));
		assertTrue(board.isSelfAtari(at("b4"), board.getColorToPlay()));
	}

	@Test
	public void testSelfAtariSharedReallyDistantLiberty() {
		String[] problem = { ".........",// 9
				".........",// 8
				".OOOOO...",// 7
				"#.####O..",// 6
				"O#OOO##O.",// 5
				"O#O.OO#O.",// 4
				"O##O.O#O.",// 3
				".O#OOO#O.",// 2
				".O####...",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		assertFalse(board.isSelfAtari(at("b6"), board.getColorToPlay()));
		board.pass();
		board.play(at("a7"));
		assertTrue(board.isSelfAtari(at("b6"), board.getColorToPlay()));
	}

	@Test
	public void testSelfAtariAfterMove() {
		String[] problem = { ".........",// 9
				"....OO...",// 8
				"...O##O..",// 7
				"...O#.O..",// 6
				"...O#.O..",// 5
				"...O.OO..",// 4
				"....O....",// 3
				".####....",// 2
				"..OO#....",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		board.play(at("e4"));
		// White's self atari
		assertTrue(board.isSelfAtari(at("b1"), board.getColorToPlay()));
		board.play(at("b1"));
		// Black's self ataris
		assertTrue(board.isSelfAtari(at("f5"), board.getColorToPlay()));
		assertTrue(board.isSelfAtari(at("f6"), board.getColorToPlay()));
		board.pass();
		// White's self atari
		assertTrue(board.isSelfAtari(at("a1"), board.getColorToPlay()));
	}

	@Test
	public void testSelfAtari2() {
		String[] problem = { ".........",// 9
				".........",// 8
				"..#O#....",// 7
				"..#O#....",// 6
				"...#.....",// 5
				".........",// 4
				".........",// 3
				".........",// 2
				".........",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		board.play(at("d8"));
		assertTrue(board.isSelfAtari(at("d6"), board.getColorToPlay()));
		assertTrue(board.isSelfAtari(at("d7"), board.getColorToPlay()));
	}

	@Test
	public void testSelfAtariCapture() {
		String[] problem = { ".........",// 9
				"...#.....",// 8
				"..#O#....",// 7
				".#O.#....",// 6
				".#O#O....",// 5
				".#O#O#...",// 4
				"..#O#.#..",// 3
				"....O#...",// 2
				".........",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(WHITE, problem);
		assertFalse(board.isSelfAtari(at("d6"), board.getColorToPlay()));
		assertTrue(board.isSelfAtari(at("f3"), board.getColorToPlay()));
	}

	@Test
	public void testLibertiesAfterMerge() {
		String[] problem = { ".........",// 9
				"....OO...",// 8
				"...O##...",// 7
				"##.O#.#O.",// 6
				".###.##..",// 5
				"..O##OO..",// 4
				"....#O...",// 3
				".........",// 2
				".........",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		assertLiberties(board, "d5", "e5", "e2", "d3", "b4", "a5", "a7", "b7",
				"c6");
		assertLiberties(board, "e6", "e5", "f6", "g7");
		assertLiberties(board, "f5", "e5", "f6", "g7", "h5");
		board.play(at("e5"));
		assertLiberties(board, "e5", "e2", "d3", "b4", "a5", "a7", "b7", "c6",
				"f6", "g7", "h5");
	}

	@Test
	public void testLibertiesAfterCapture() {
		String[] problem = { ".........",// 9
				"....OO...",// 8
				"...O#....",// 7
				"....O....",// 6
				".........",// 5
				".........",// 4
				".#O#.....",// 3
				"#OOO#....",// 2
				".###.....",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(BLACK, problem);
		board.play(at("c4"));
		assertLiberties(board, "a2", "a1", "a3", "b2");
		assertLiberties(board, "b1", "a1", "b2", "c2", "d2", "e1");
		assertLiberties(board, "e2", "e1", "e3", "d2", "f2");
		assertLiberties(board, "b3", "a3", "b2", "c3", "b4");
		assertLiberties(board, "d3", "c3", "d2", "e3", "d4");
		board.play(at("f7"));
		assertLiberties(board, "d7", "c7", "d8", "e7", "d6");
		assertLiberties(board, "e6", "d6", "e7", "f6", "e5");
		assertLiberties(board, "f8", "e7", "d8", "e9", "f9", "g8", "g7", "f6");
	}

	@Test
	public void testFillMoves() {
		board.play(at("a1")); // 0
		board.play(at("b2")); // 1
		board.play(at("c3")); // 2
		board.play(at("d4")); // 3
		board.play(at("e5")); // 4
		int[] fill = new int[5];
		board.fillMoves(fill, 0, board.getTurn() - 1);
		assertArrayEquals(new int[] { at("a1"), at("b2"), at("c3"), at("d4"),
				at("e5") }, fill);
		fill = new int[2];
		board.fillMoves(fill, 3, board.getTurn() - 1);
		assertArrayEquals(new int[] { at("d4"), at("e5") }, fill);
		fill = new int[3];
		board.fillMoves(fill, 1, 3);
		assertArrayEquals(new int[] { at("b2"), at("c3"), at("d4") }, fill);
	}

	@Test
	public void testTripleKo() {
		String[] problem = { ".........",// 9
				".........",// 8
				".........",// 7
				".........",// 6
				".........",// 5
				".........",// 4
				".........",// 3
				"O#.#O.O#.",// 2
				"#.#O.O..#",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(WHITE, problem);
		board.play(at("h1"));
		board.play(at("g1"));
		board.play(at("b1"));
		board.play(at("e1"));
		board.play(at("h1"));
		board.play(at("a1"));
		assertFalse(board.isLegal(at("d1")));
		assertEquals(PLAY_KO_VIOLATION, board.play(at("d1")));
	}

	@Test
	public void testTripleKo2() {
		String[] problem = { ".........",// 9
				"..#O.....",// 8
				".#.#O....",// 7
				"..#O.....",// 6
				".........",// 5
				"..#O.....",// 4
				".#O.O....",// 3
				"..#O.....",// 2
				".........",// 1
		// ABCDEFGHJ
		};
		String[] p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem[i - 10]
					+ "..........");
		}
		problem = p;
		board.setUpProblem(WHITE, problem);
		// System.out.println("Begin case 1");
		// System.out.println(board.getHash());
		board.play(at("c7"));
		// System.out.println(board.getHash());
		board.play(at("d3"));
		// System.out.println(board.getHash());
		// System.out.println("End case 1");
		long a = board.getHash();
		String[] problem2 = { ".........",// 9
				"..#O.....",// 8
				".#O.O....",// 7
				"..#O.....",// 6
				".........",// 5
				"..#O.....",// 4
				".#O.O....",// 3
				"..#O.....",// 2
				".........",// 1
		// ABCDEFGHJ
		};
		p = new String[19];
		for (int i = 0; i < p.length; i++) {
			p[i] = (i < 10 ? "..................." : problem2[i - 10]
					+ "..........");
		}
		problem2 = p;

		board.setUpProblem(BLACK, problem2);
		// board.play(at("c7"));
		// System.out.println("Begin case 2");
		// System.out.println(board.getHash());
		board.play(at("d3"));
		// System.out.println(board.getHash());
		// System.out.println("End case 2");
		assertEquals(a, board.getHash());
	}

	@Test
	public void testSetUpFixedHandicap() {
		board.setUpHandicap(2);
		assertEquals(BLACK, board.getColor(at("D4")));
		assertEquals(BLACK, board.getColor(at("Q16")));
		board.clear();
		board.setUpHandicap(7);
		assertEquals(BLACK, board.getColor(at("D4")));
		assertEquals(BLACK, board.getColor(at("Q16")));
		assertEquals(BLACK, board.getColor(at("Q4")));
		assertEquals(BLACK, board.getColor(at("D16")));
		assertEquals(BLACK, board.getColor(at("D10")));
		assertEquals(BLACK, board.getColor(at("Q10")));
		assertEquals(BLACK, board.getColor(at("K10")));
	}

}
