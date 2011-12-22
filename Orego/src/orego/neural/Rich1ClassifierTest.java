package orego.neural;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import orego.core.Board;
import org.junit.Before;
import org.junit.Test;

public class Rich1ClassifierTest {
	private Board board;

	private Rich1Classifier network;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		network = new Rich1Classifier(.01, 2);
	}

	@Test
	public void testLearn() {
		board.play("a2");
		for (int i = 0; i < 5000; i++) {
			network.learn(BLACK, board, board.getTurn() - 1, 0);
		}
		assertEquals(0,network.evaluate(BLACK, at("a2"), board, board.getTurn() - 1),
				 .001);
		board.clear();
		board.play("a2");
		for (int i = 0; i < 5000; i++) {
			network.learn(BLACK, board, board.getTurn() - 1, 1);
		}
		assertEquals(1,
				network.evaluate(BLACK, at("a2"), board, board.getTurn() - 1), .001);
	}
	
	@Test
	public void testLearn2(){
		board.play("a1");
		double eval = network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1);
		network.learn(BLACK, board, board.getTurn()-1, 0);
		assertTrue(eval > network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1));
		eval = network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1);
		network.learn(BLACK, board, board.getTurn()-1, 1);
		assertTrue(eval < network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1));

	}
	
//	@Test
//	public void testLargeKnightsMove() {
//		board.play("e5");
//		for (int i = 0; i < 5000; i++) {
//			network.learn(BLACK, board, board.getTurn() - 1, 1);
//		}
//		assertEquals(1,
//				network.evaluate(BLACK, at("e5"), board, board.getTurn() - 1), .001);
//		board.clear();
//		board.play("g6");
//		board.play("e5");
//		for (int i = 0; i < 5000; i++) {
//			network.learn(BLACK, board, board.getTurn() - 1, 0);
//		}
//		board.clear();
//		board.play("g6");
//		assertEquals(0,network.evaluate(BLACK, at("e5"), board, board.getTurn()),
//				 .001);
//	}
//	
//	@Test
//	public void testLargeKnightsMoveWeights() {
//		double[][][][] array = network.getNeighbors();
//		array[BLACK][at("e5")][20][VACANT] = .05;
//		array[BLACK][at("e5")][20][BLACK] = .5;
//		assertEquals(.9756, network.evaluate(BLACK, at("e5"), board, board.getTurn()), .001);
//		board.play("d8");
//		assertEquals(.9871, network.evaluate(BLACK, at("e5"), board, board.getTurn()), .001);
//	}
//
//	@Test
//	public void testHistoricalColors() {
//		double[][][][] array = network.getNeighbors();
//		array[BLACK][at("e5")][20][VACANT] = .05;
//		board.play("e5");
//		assertEquals(.9756, network.evaluate(BLACK, at("e5"), board, board.getTurn() - 1), .001);
//		board.clear();
//		board.play("e5");
//		board.play("d8");
//		assertEquals(.9756, network.evaluate(BLACK, at("e5"), board, board.getTurn() - 2), .001);
//	}

}
