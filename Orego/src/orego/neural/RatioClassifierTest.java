package orego.neural;

import static orego.core.Colors.*;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import orego.core.Board;
import org.junit.Before;
import org.junit.Test;

public class RatioClassifierTest {

	private Board board;
	
	private RatioClassifier network;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		board.play(at("c1"));
		board.play(at("b1"));
		network = new RatioClassifier(2);
	}
	
	@Test
	public void testLearn() {
		board.clear();
		assertEquals(0.5, network.evaluate(BLACK, at("a1"), board, board.getTurn()),
				.001);
		board.play("a1");
		for (int i = 0; i < 10; i++) {
			network.learn(BLACK, board, board.getTurn() - 1, 1);
		}
		board.clear();
		assertEquals(11.0 / 12, network.evaluate(BLACK, at("a1"), board, board.getTurn()), .001);
		board = new Board();
		board.play(at("c1"));
		board.play(at("b1"));
		board.clear();
		board.play("a2");
		for (int i = 0; i < 10; i++) {
			network.learn(BLACK, board, board.getTurn() - 1, 0);
		}
		board.clear();
		assertEquals(1.0 / 12, network.evaluate(BLACK, at("a2"), board, board.getTurn()),
				.001);
	}

	@Test
	public void testUct() {
		assertEquals(0.927275, network.getUctValue(163, new int[] {2, 3}), 0.001);
	}

}
