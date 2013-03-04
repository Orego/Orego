package orego.neural;

import static orego.core.Colors.*;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import orego.core.Board;
import org.junit.Before;
import org.junit.Test;

public class LinearClassifierTest {

	private Board board;
	
	private LinearClassifier network;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		board.play(at("c1"));
		board.play(at("b1"));
		network = new LinearClassifier(.01, 2);
	}
	
	@Test
	public void testLearn() {
		board.clear();
		board.play("a1");
		for (int i = 0; i < 5000; i++) {
			network.learn(BLACK, board, board.getTurn() - 1, 1);
		}
		board.clear();
		assertEquals(network.evaluate(BLACK, at("a1"), board, board.getTurn()),
				1, .001);
		board = new Board();
		board.play(at("c1"));
		board.play(at("b1"));
		board.clear();
		board.play("a2");
		for (int i = 0; i < 5000; i++) {
			network.learn(BLACK, board, board.getTurn() - 1, 0);
		}
		board.clear();
		assertEquals(network.evaluate(BLACK, at("a2"), board, board.getTurn()),
				0, .001);
	}

}
