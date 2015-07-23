package edu.lclark.orego.move;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.*;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.move.MoverFactory.*;

public class SuggesterMoverTest {

	private Board board;

	private CoordinateSystem coords;

	private Mover mover;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		mover = capturer(board, new AtariObserver(board));
	}

	@Test
	public void testSuggestions() {
		String[] diagram = {
				".##..",
				"#OO#O",
				"..#OO",
				".#O##",
				".....",
		};
		MersenneTwisterFast random = new MersenneTwisterFast();
		int[] counts = new int[coords.getFirstPointBeyondBoard()];
		for(int i = 0; i < 1000; i++){
			board.setUpProblem(diagram, BLACK);
			short p = mover.selectAndPlayOneMove(random, true);
			counts[p]++;
		}
		assertTrue(counts[at("b3")] > 250);
		assertTrue(counts[at("c1")] > 250);
		assertTrue(counts[at("e5")] > 250);
	}
	
	@Test
	public void testFallback() {
		String[] diagram = {
				".#...",
				"#....",
				".....",
				".....",
				".....",
		};
		MersenneTwisterFast random = new MersenneTwisterFast();
		int[] counts = new int[coords.getFirstPointBeyondBoard()];
		for(int i = 0; i < 1000; i++){
			board.setUpProblem(diagram, BLACK);
			short p = mover.selectAndPlayOneMove(random, true);
			counts[p]++;
		}
		final ShortSet invalidPoints = new ShortSet(coords.getFirstPointBeyondBoard());
		for (String s : new String[] {"a4", "a5", "b5", "c1", "d1", "d2", "e1", "e2", "e3"}) {
			invalidPoints.add(at(s));
		}
		for(short p : coords.getAllPointsOnBoard()){
			if(!invalidPoints.contains(p)){
				assertTrue(counts[p] > 20);
			}
		}
		assertEquals(0, counts[at("a5")]);
		assertEquals(0, counts[at("a4")]);
		assertEquals(0, counts[at("b5")]);
	}

}
