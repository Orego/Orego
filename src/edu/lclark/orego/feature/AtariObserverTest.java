package edu.lclark.orego.feature;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

public class AtariObserverTest {

	private Board board;
	
	private AtariObserver atari;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		atari = new AtariObserver(board);
	}
	
	/** Delegate method to call at on board. */
	private short at(String label) {
		return board.getCoordinateSystem().at(label);
	}

	@Test
	public void testAddNewAtaris() {
		String[] diagram = {
				".O#..",
				".##..",
				".....",
				".....",
				".....",
		};
		board.setUpProblem(diagram, WHITE);
		assertTrue(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("b5"))));
		assertFalse(atari.getChainsInAtari(BLACK).contains(board.getChainRoot(at("b4"))));
		board.play("a5");
		assertTrue(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("a5"))));
	}
	
	@Test
	public void testCapture() {
		String[] diagram = {
				".O#..",
				".##..",
				".....",
				".....",
				".....",
		};
		board.setUpProblem(diagram, BLACK);
		assertTrue(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("b5"))));
		assertFalse(atari.getChainsInAtari(BLACK).contains(board.getChainRoot(at("b4"))));
		board.play("a5");
		assertFalse(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("b5"))));
		board.play("a4");
		assertTrue(atari.getChainsInAtari(BLACK).contains(board.getChainRoot(at("a5"))));
		assertTrue(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("a4"))));
	}
	
	@Test
	public void testMultipleCaptures() {
		String[] diagram = {
				".#O#.",
				"##O##",
				"OO.OO",
				"##O##",
				".#O#.",
		};
		board.setUpProblem(diagram, BLACK);
		assertTrue(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("c5"))));
		assertTrue(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("d3"))));
		assertTrue(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("c1"))));
		board.play("c3");
		assertEquals(0, atari.getChainsInAtari(WHITE).size());
	}
	
	@Test
	public void testMerging() {
		String[] diagram = {
				".#O#.",
				".#O##",
				"OO.OO",
				".#O##",
				".#O#.",
		};
		board.setUpProblem(diagram, WHITE);
		assertTrue(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("c5"))));
		assertTrue(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("d3"))));
		assertTrue(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("c1"))));
		board.play("c3");
		assertFalse(atari.getChainsInAtari(WHITE).contains(board.getChainRoot(at("c5"))));
		assertEquals(0, atari.getChainsInAtari(WHITE).size());
	}
	
	@Test
	public void testPass() {
		board.pass();
		assertEquals(0, atari.getChainsInAtari(BLACK).size());
		assertEquals(0, atari.getChainsInAtari(WHITE).size());
	}

}
