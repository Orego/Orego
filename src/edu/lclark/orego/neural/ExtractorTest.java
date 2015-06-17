package edu.lclark.orego.neural;

import static edu.lclark.orego.core.StoneColor.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;

public class ExtractorTest {

	private Board board;

	private Extractor extractor;

	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		extractor = new Extractor(board);
	}

	@Test
	public void testIsBlack() {
		String[] diagram = { "...#.", ".....", ".....", ".....", ".....", };
		board.setUpProblem(diagram, WHITE);
		assertEquals(0.0, extractor.isBlack(1, 7), 0.001);
		assertEquals(1.0, extractor.isBlack(0, 3), 0.001);
		assertEquals(0.0, extractor.isBlack(2, 3), 0.001);
	}

	@Test
	public void testIsWhite() {
		String[] diagram = { "...O.", ".....", "#....", ".....", ".....", };
		board.setUpProblem(diagram, WHITE);
		assertEquals(0.0, extractor.isWhite(1, 7), 0.001);
		assertEquals(1.0, extractor.isWhite(0, 3), 0.001);
		assertEquals(0.0, extractor.isWhite(2, 3), 0.001);
	}

	@Test
	public void testIsOffBoard() {
		String[] diagram = { ".....", ".....", ".....", ".....", ".....", };
		board.setUpProblem(diagram, WHITE);
		assertEquals(1.0, extractor.isOffBoard(1, 7), 0.001);
		assertEquals(0.0, extractor.isOffBoard(0, 3), 0.001);
		assertEquals(1.0, extractor.isOffBoard(9, 3), 0.001);
	}

	@Test
	/** Tests isUltimate and isPenultimate \. */
	public void testMoveTracker() {
		board.play("a2");
		board.play("b3");
		assertEquals(0.0, extractor.isUltimateMove(0, 0), 0.001);
		assertEquals(1.0, extractor.isUltimateMove(2, 1), 0.001);
		assertEquals(0.0, extractor.isPenultimateMove(2, 1), 0.001);
		assertEquals(0.0, extractor.isPenultimateMove(9, 2), 0.001);
		assertEquals(1.0, extractor.isPenultimateMove(3, 0), 0.001);
		assertEquals(0.0, extractor.isPenultimateMove(1, 1), 0.001);
	}
	
	@Test
	public void testToInputVector() {
		board.play("a2");
		board.play("b3");
		assertArrayEquals(new float[] {
				// Black
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
				// White
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
				// Ultimate move
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
				// Penultimate move
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
		}, extractor.toInputVector(), 0.01f);
	}

}
