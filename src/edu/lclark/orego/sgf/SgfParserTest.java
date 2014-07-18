package edu.lclark.orego.sgf;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.TestingTools;

public class SgfParserTest {

	private SgfParser parser;

	private Board board;

	private CoordinateSystem coords;

	@Before
	public void setUp() {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		parser = new SgfParser(coords, true);
	}

	/** Delegate method to call at on coords. */
	private short at(String label) {
		return coords.at(label);
	}

	@Test
	public void testSgfToPoint() {
		// Test conversion of sgf (and human-readable strings) to ints
		assertEquals(at("e15"), parser.sgfToPoint("ee"));
		assertEquals(at("t1"), parser.sgfToPoint("ss"));
		assertEquals(at("a19"), parser.sgfToPoint("aa"));
		assertEquals(at("t19"), parser.sgfToPoint("sa"));
		assertEquals(at("a1"), parser.sgfToPoint("as"));
	}

	@SuppressWarnings("boxing")
	@Test
	public void testSgfToMoves() {
		final List<List<Short>> games = parser.parseGamesFromFile(new File(
				"sgf-test-files/19/1977-02-27.sgf"), 500);
		assertEquals(1, games.size());
		final List<Short> game = games.get(0);
		assertEquals(180, game.size());
		assertEquals(coords.at("R16"), (short) game.get(0));
		assertEquals(coords.at("N11"), (short) game.get(179));
	}
	
	@Test
	public void testBreakOnPass(){
		List<Short> moves = parser.parseGameFromFile(new File("sgf-test-files/19/Orego4-Magisus.sgf"));
		assertEquals(174, moves.size());
		parser = new SgfParser(coords, false);
		moves = parser.parseGameFromFile(new File("sgf-test-files/19/Orego4-Magisus.sgf"));
		assertEquals(254, moves.size());
	}
	
	@Test
	public void testCgtc(){
		String[] diagram = {
				"...................",
				"..........#........",
				".....O......#.#....",
				"...O....O.O....#...",
				"...................",
				"...#............#..",
				"...............O#..",
				"..O.............O..",
				"...............#...",
				"..O................",
				"..............#.#..",
				"...O...............",
				"...................",
				"...................",
				"..O................",
				"......O.......#.#..",
				"...O....O..#.......",
				"...................",
				"...................",
		};
		parser.sgfToBoard("sgf-test-files/19/blunder.1.sgf", board);
		assertEquals(StoneColor.BLACK, board.getColorToPlay());
		assertEquals(TestingTools.asOneString(diagram), board.toString());
	}

}
