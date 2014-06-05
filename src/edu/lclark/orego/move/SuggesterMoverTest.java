package edu.lclark.orego.move;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.*;
import edu.lclark.orego.feature.NotEyeLike;
import static edu.lclark.orego.core.StoneColor.*;

public class SuggesterMoverTest {

	private Board board;
	
	private CoordinateSystem coords;

	private SuggesterMover mover;
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		mover = new SuggesterMover(board, new CaptureSuggester(board, new AtariObserver(board)),
				new PredicateMover(board, new NotEyeLike(board)));
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
			short p = mover.selectAndPlayOneMove(random);
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
			short p = mover.selectAndPlayOneMove(random);
			counts[p]++;
		}
		for(short p : coords.getAllPointsOnBoard()){
			if(p != at("a5") && p != at("b5") && p != at("a4")){
				assertTrue(counts[p] > 25);
			}
		}
		assertEquals(0, counts[at("a5")]);
		assertEquals(0, counts[at("a4")]);
		assertEquals(0, counts[at("b5")]);
	}

}
