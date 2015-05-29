package edu.lclark.orego.core;

import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.util.TestingTools.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.feature.NotEyeLike;
import edu.lclark.orego.feature.StoneCountObserver;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.move.PredicateMover;
import edu.lclark.orego.score.ChineseFinalScorer;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;

public class BoardTest {

	private Board board;

	private CoordinateSystem coords;
	
	/** Delegate method to call at on coords. */
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
	}

	@Test
	public void testSimplePlay() {
		assertEquals(OK, board.play("a2"));
		assertEquals(OK, board.play("b3"));
		assertEquals(BLACK, board.getColorAt(at("a2")));
		assertEquals(WHITE, board.getColorAt(at("b3")));
	}
	
	@Test(expected = AssertionError.class)
	public void testOffBoard() {
		// p is not on the board
		short p = coords.getNeighbors(at("e2"))[EAST_NEIGHBOR];
		board.play(p);
	}

	@Test
	public void testOccupied() {
		board.play("c1");
		assertEquals(OCCUPIED, board.play("c1"));
	}

	@Test
	public void testPass() {
		assertEquals(OK, board.play("pass"));
		assertEquals(WHITE, board.getColorToPlay());
	}

	@Test
	public void testSuicide() {
		String[] before = {
				".O.#.",
				".##..",
				".....",
				".##..",
				"#O.#.",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(SUICIDE, board.play("c1"));
		assertEquals(OK, board.play("c5"));
		String[] after = {
				".OO#.",
				".##..",
				".....",
				".##..",
				"#O.#.",
		};
		assertEquals(asOneString(after), board.toString());
	}

	@Test
	public void testCapture() {
		String[] before = {
				".....",
				".....",
				".....",
				"..#..",
				"#OO#.",
		};
		board.setUpProblem(before, BLACK);
		assertEquals(OK, board.play("b2"));
		String[] after = {
				".....",
				".....",
				".....",
				".##..",
				"#..#.",
		};
		assertEquals(asOneString(after), board.toString());
	}

	@Test
	public void testMultipleCapture() {
		String[] before = {
				".O#O.",
				"OO#O.",
				"##.##",
				"OO#OO",
				".O#O.",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(OK, board.play("c3"));
		String[] after = {
				".O.O.",
				"OO.O.",
				"..O##",
				"OO.OO",
				".O.O.",
		};
		assertEquals(asOneString(after), board.toString());
	}

	@Test
	public void testSimpleKo() {
		String[] before = {
				".....",
				".....",
				".....",
				"#O...",
				".#O..",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(OK, board.play("a1"));
		assertEquals(KO_VIOLATION, board.play("b1"));
		String[] after = {
				".....",
				".....",
				".....",
				"#O...",
				"O.O..",
		};
		assertEquals(asOneString(after), board.toString());
	}

	@Test
	public void testPositionalSuperKo() {
		String[] before = {
				".....",
				".....",
				".....",
				"O##..",
				".O.#.",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(OK, board.play("c1"));
		assertEquals(OK, board.play("a1"));
		assertEquals(KO_VIOLATION, board.play("b1"));
		String[] after = {
				".....",
				".....",
				".....",
				"O##..",
				"#..#.",
		};
		assertEquals(asOneString(after), board.toString());
	}
	
	@Test
	public void testMaxMovesPerGame() {
		short[] points = coords.getAllPointsOnBoard();
		int i;
		for (i = 0; i < points.length - 2; i++) {
			assertEquals(OK, board.play(points[i]));
			board.pass();
		}
		assertEquals(OK, board.play(points[i]));
		i++;
		assertEquals(OK, board.play(points[i]));
		board.pass();
		int n = coords.getArea() * 2 - 1;
		for (i = 0; i < points.length - 2; i++) {
			assertEquals(OK, board.play(points[i]));
			n++;
			if (n == coords.getMaxMovesPerGame() - 2) {
				break;
			}
			board.pass();
			n++;
			if (n == coords.getMaxMovesPerGame() - 2) {
				break;
			}
		}
		i++;
		assertEquals(GAME_TOO_LONG, board.play(points[i]));
		assertEquals(OK, board.play(PASS));
		i++;
		assertEquals(GAME_TOO_LONG, board.play(points[i]));
		assertEquals(OK, board.play(PASS));
		return;
	}

	@Test
	public void testPasses() {
		assertEquals(0, board.getPasses());
		board.pass();
		assertEquals(1, board.getPasses());
		board.play("c4");
		assertEquals(0, board.getPasses());
		board.pass();
		assertEquals(1, board.getPasses());
		board.pass();
		assertEquals(2, board.getPasses());
	}
	
	@Test
	public void testGetTurn() {
		assertEquals(0, board.getTurn());
		board.play("c1");
		assertEquals(1, board.getTurn());
		board.pass();
		assertEquals(2, board.getTurn());
	}

	@Test
	public void testBug1() {
		assertEquals(OK, board.play("D5"));
		assertEquals(OK, board.play("A5"));
		assertEquals(OK, board.play("A2"));
		assertEquals(OK, board.play("E4"));
		assertEquals(OK, board.play("D2"));
		assertEquals(OK, board.play("C4"));
		assertEquals(OK, board.play("B2"));
		assertEquals(OK, board.play("B5"));
		assertEquals(OK, board.play("E3"));
		assertEquals(OK, board.play("A1"));
		assertEquals(OK, board.play("C2"));
		assertEquals(OK, board.play("B1"));
		assertEquals(OK, board.play("D1"));
		assertEquals(OK, board.play("E2"));
		assertEquals(OK, board.play("D4"));
		assertEquals(OK, board.play("A4"));
		assertEquals(OK, board.play("E5"));
		assertEquals(OK, board.play("D3"));
		assertEquals(OK, board.play("C3"));
		assertEquals(OK, board.play("B4"));
		assertEquals(OK, board.play("C5"));
		assertEquals(OK, board.play("B3"));
		assertEquals(OK, board.play("C1"));
		assertEquals(OK, board.play("B1"));
		assertEquals(OK, board.play("D3"));
		assertEquals(SUICIDE, board.play("A1"));
	}

	@Test
	public void testLibertyUpdate() {
		String[] before = {
				".#O#.",
				".#O#.",
				"##.##",
				".OO..",
				".....",
		};
		board.setUpProblem(before, BLACK);
		assertEquals(1, board.getLiberties(at("c4")).size());
		assertEquals(4, board.getLiberties(at("b3")).size());
		assertEquals(5, board.getLiberties(at("d3")).size());
		assertEquals(5, board.getLiberties(at("c2")).size());
		assertEquals(OK, board.play("c3"));
		assertEquals(9, board.getLiberties(at("c3")).size());
	}

	@Test
	public void testPlayFast() {
		String[] before = {
				".#O.O",
				"#O.O#",
				"...#.",
				"O#..#",
				".O#..",
		};
		board.setUpProblem(before, WHITE);
		assertEquals(OK, board.playFast(at("a5")));
		assertEquals(OK, board.playFast(at("a1")));
		assertEquals(OK, board.playFast(at("e3")));
		assertEquals(OK, board.playFast(at("b5")));
		assertEquals(OK, board.playFast(at("b1")));
		assertEquals(OK, board.playFast(at("e4")));
		assertEquals(asOneString(before), board.toString());
	}

	@Test
	public void testIsLegal() {
		String[] before = {
				"..#..",
				".....",
				".....",
				".....",
				".....",
		};
		board.setUpProblem(before, BLACK);
		assertFalse(board.isLegal(at("c5")));
		assertTrue(board.isLegal(PASS));
		assertTrue(board.isLegal(at("c4")));
		assertEquals(asOneString(before), board.toString());
		assertEquals(BLACK, board.getColorToPlay());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBadDiagram() {
		String[] example = {
				"..#..",
				"...0.",
				".....",
				".....",
				".....",
		};
		board.setUpProblem(example, BLACK);		
	}

	@Test
	public void testCopyDataFrom() {
		board = new Board(19);
		Board copy = new Board(19);
		StoneCountObserver counter = new StoneCountObserver(board, new ChineseFinalScorer(board, 7.5));
		StoneCountObserver copyCounter = new StoneCountObserver(copy, new ChineseFinalScorer(copy, 7.5));
		Mover mover = new PredicateMover(board, new NotEyeLike(board));
		MersenneTwisterFast random = new MersenneTwisterFast();
		for (int i = 0; i < 50; i++) {
			mover.selectAndPlayOneMove(random, true);
		}
		copy.copyDataFrom(board);
		assertEquals(board.toString(), copy.toString());
		assertEquals(counter.getCount(BLACK), copyCounter.getCount(BLACK));
		assertEquals(counter.getCount(WHITE), copyCounter.getCount(WHITE));
		while (board.getPasses() < 2) {
			short p = mover.selectAndPlayOneMove(random, true);
			copy.play(p);
		}
		assertEquals(board.toString(), copy.toString());
		assertEquals(counter.getCount(BLACK), copyCounter.getCount(BLACK));
		assertEquals(counter.getCount(WHITE), copyCounter.getCount(WHITE));
	}
	
	@Test
	public void testHasMaxNeighborsForColor(){
		String[] diagram = {
				"..#.#",
				"...#.",
				"..O..",
				".O.O.",
				"..O..",
		};
		board.setUpProblem(diagram, WHITE);
		assertTrue(board.hasMaxNeighborsForColor(BLACK, at("d5")));
		assertTrue(board.hasMaxNeighborsForColor(WHITE, at("c2")));
		assertFalse(board.hasMaxNeighborsForColor(BLACK, at("b5")));
		assertFalse(board.hasMaxNeighborsForColor(WHITE, at("b2")));
	}

	@Test
	public void testGetChainNextPoint() {
		String[] diagram = {
				".....",
				".....",
				"#....",
				"#....",
				"#....",
		};
		board.setUpProblem(diagram, WHITE);
		ShortSet stones = new ShortSet(coords.getFirstPointBeyondBoard());
		short p = at("a1");
		short q = p;
		do {
			stones.add(q);
			q = board.getChainNextPoint(q);
		} while (q != p);
		assertEquals(3, stones.size());
		assertTrue(stones.contains(at("a1")));
		assertTrue(stones.contains(at("a2")));
		assertTrue(stones.contains(at("a3")));
	}

	@Test
	public void testFancyHashKo() {
		String[] diagram = {
				".....",
				".....",
				".#O..",
				"#O.O.",
				".#O..",
		};
		board.setUpProblem(diagram, BLACK);
		board.play("c2");
		long simple1 = board.getHash();
		long fancy1 = board.getFancyHash();
		diagram = new String[] {
				".....",
				".....",
				".#O..",
				"#.#O.",
				".#O..",
		};
		board.setUpProblem(diagram, WHITE);
		long simple2 = board.getHash();
		long fancy2 = board.getFancyHash();
		assertEquals(simple1, simple2);
		assertNotEquals(fancy1, fancy2);
	}
	
	@Test
	public void testFancyHashTurn() {
		String[] diagram = {
				".....",
				".....",
				".#O..",
				"#O.O.",
				".#O..",
		};
		board.setUpProblem(diagram, BLACK);
		long simpleBlack = board.getHash();
		long fancyBlack = board.getFancyHash();
		board.setUpProblem(diagram, WHITE);
		long simpleWhite = board.getHash();
		long fancyWhite = board.getFancyHash();
		assertEquals(simpleBlack, simpleWhite);
		assertNotEquals(fancyBlack, fancyWhite);
	}
	
	@Test
	public void testSetUpHandicap() {
		
		String[] problem = new String[] {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...#...........#...",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				".........#.........",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"...................",// 5
				"...#...........#...",// 4
				"...................",// 3
				"...................",// 2
				"..................." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		String[] problem2 = new String[] {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...#.....#.....#...",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...#...........#...",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"...................",// 5
				"...#.....#.....#...",// 4
				"...................",// 3
				"...................",// 2
				"..................." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board = new Board(19);
		board.setUpProblem(problem, WHITE);
		long hash = board.getFancyHash();
		board.setUpHandicap(5);
		assertEquals(hash, board.getFancyHash());
		board.setUpProblem(problem2, WHITE);
		hash = board.getFancyHash();
		board.setUpHandicap(8);
		assertEquals(hash, board.getFancyHash());
	}

	@Test
	public void testFancyHash9() {
		// Debugging test; this board appeared to be returning a fancy hash of 0L
		String[] diagram = new String[] {
				".#OO.O.OO",
				"##OOOOO.O",
				".#OO#OOO.",
				"##OO###OO",
				"#####OO.O",
				"######OOO",
				".##.#OOO.",
				"##.##O.OO",
				"#.#OOOOO."
				};
		board = new Board(9);
		board.setUpProblem(diagram, BLACK);
		assertFalse(0L == board.getFancyHash());
	}

	@Test
	public void testFancyHash5() {
		// Debugging test; this board appeared to be returning a fancy hash of 0L
		String[] moves = {"PASS", "C3", "B5", "D5", "E4", "C4", "B3", "C2", "D1", "E2", "E5", "C5", "B4", "D2", "D4", "D3", "B2", "A2", "A3", "A4", "A1", "E3", "D4", "E4", "A5", "B1", "C1", "E1", "B1", "PASS"};
		board = new Board(5);
		for (String move : moves) {
			board.play(move);
			assertFalse(0L == board.getFancyHash());
		}
	}

}
