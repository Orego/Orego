package orego.neural;

import static orego.core.Colors.BLACK;
import static orego.core.Coordinates.at;
import static org.junit.Assert.assertEquals;
import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class AverageClassifierTest {

	private Board board;

	private AverageClassifier network;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		board.play(at("c1"));
		board.play(at("b1"));
		network = new AverageClassifier(.01, 2);
	}

	@Test
	public void testLearn() {
		board.clear();
		board.play("a1");
		for (int i = 0; i < 5000; i++) {
			network.learn(BLACK, board, board.getTurn() - 1, 1);
		}
		board.clear();
		assertEquals(1,
				network.evaluate(BLACK, at("a1"), board, board.getTurn()), .001);
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
