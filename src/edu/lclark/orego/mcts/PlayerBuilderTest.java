package edu.lclark.orego.mcts;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PlayerBuilderTest {

	private PlayerBuilder builder;
	
	@Before
	public void setUp() throws Exception {
		builder = new PlayerBuilder().openingBook(false);
	}

	@Test
	public void testBoardSize() {
		builder.boardWidth(9);
		assertEquals(9, builder.build().getBoard().getCoordinateSystem().getWidth());
		builder.boardWidth(19);
		assertEquals(19, builder.build().getBoard().getCoordinateSystem().getWidth());
	}

	@Test
	public void testKomi() {
		builder.komi(3.5);
		assertEquals(3.5, builder.build().getFinalScorer().getKomi(), 0.001);
	}

	@Test
	public void testThreads() {
		builder.threads(3);
		assertEquals(3, builder.build().getNumberOfThreads());
	}
	
	@Test
	public void testMsecPerMove() {
		builder.msecPerMove(8675309);
		assertEquals(8675309, builder.build().getMsecPerMove());
	}
	
	@Test
	public void testGestation() {
		builder.gestation(23);
		assertEquals(23, builder.build().getUpdater().getGestation());		
	}
	
	@Test
	public void testBiasDelay() {
		builder.biasDelay(173);
		assertEquals(173, builder.build().getDescender().getBiasDelay());				
	}

}
