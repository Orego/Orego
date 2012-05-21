package orego.neural;

import static orego.core.Colors.BLACK;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import orego.core.Board;
import org.junit.Before;
import org.junit.Test;

public class NetworkTest {

	private Network network;

	private double[][][][] weightsInput;

	private double[][][] weightsOutput;

	private double[][][][] weightsSkip;

	private Board board;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		board.play(at("c1"));
		board.play(at("b1"));
		network = new Network(1, 2, 2);
		weightsInput = network.getWeightsInputHidden();
		weightsOutput = network.getWeightsHiddenOutput();
		weightsSkip = network.getWeightsInputOutput();
		weightsSkip[BLACK][2][0][at("a1")] = .6;
		weightsSkip[BLACK][at("b1")][0][at("a1")] = .15;
		weightsSkip[BLACK][at("c1")][1][at("a1")] = .5;
		weightsInput[BLACK][2][0][0] = .7;
		weightsInput[BLACK][2][0][1] = .4;
		weightsInput[BLACK][at("b1")][0][0] = .2;
		weightsInput[BLACK][at("b1")][0][1] = .4;
		weightsInput[BLACK][at("c1")][1][0] = .3;
		weightsInput[BLACK][at("c1")][1][1] = -.5;
		weightsOutput[BLACK][0][at("a1")] = .2;
		weightsOutput[BLACK][1][at("a1")] = .1;
	}

	@Test
	public void testEvaluate() {
		assertEquals(.8117, network.evaluate(BLACK, at("a1"), board, board.getTurn()), .01);
	}

	@Test
	public void testLearn() {
		double eval = 0;
		board.clear();
		eval = network.evaluate(BLACK, at("a1"), board, board.getTurn());
		board.play("a1");
		network.learn(BLACK, board, board.getTurn() - 1, 1);
		assertTrue(eval < network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1));
		eval = network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1);
		network.learn(BLACK, board, board.getTurn() - 1, 1);
		assertTrue(eval < network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1));
		eval = network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1);
		network.learn(BLACK, board, board.getTurn() - 1, 0);
		assertTrue(eval > network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1));
		eval = network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1);
		network.learn(BLACK, board, board.getTurn() - 1, 0);
		board.clear();
		assertTrue(eval > network.evaluate(BLACK, at("a1"), board, board.getTurn() - 1));
	}

	@Test
	public void testLearn2() {
		board = new Board();
		board.play(at("c1"));
		board.play(at("b1"));
		network = new Network(0.5, 1, 2);
		weightsInput = network.getWeightsInputHidden();
		weightsOutput = network.getWeightsHiddenOutput();
		weightsSkip = network.getWeightsInputOutput();
		weightsSkip[BLACK][2][0][at("a1")] = .1;
		weightsSkip[BLACK][at("b1")][0][at("a1")] = 0;
		weightsSkip[BLACK][at("c1")][1][at("a1")] = 0;
		weightsInput[BLACK][2][0][0] = .2;
		weightsInput[BLACK][at("b1")][0][0] = .3;
		weightsInput[BLACK][at("c1")][1][0] = .4;
		weightsOutput[BLACK][0][at("a1")] = .5;
		assertEquals(.6119, network.evaluate(BLACK, at("a1"), board, board.getTurn()), .001);
		board.play("a1");
		network.learn(BLACK, board, board.getTurn() - 1, 1);
		assertEquals(.5 + 0.5 * .61194 * .0921,
				weightsOutput[BLACK][0][at("a1")], .01);
		assertEquals(.3 + 0.5 * .009471, weightsInput[BLACK][at("b1")][0][0],
				.01);
		assertEquals(.2 + 0.5 * .009471, weightsInput[BLACK][2][0][0], .01);
		assertEquals(.4 + 0.5 * .009471, weightsInput[BLACK][at("c1")][1][0],
				.01);
		assertTrue(weightsSkip[BLACK][at("b1")][0][at("a1")] > 0);
		assertTrue(weightsSkip[BLACK][at("c1")][1][at("a1")] > 0);
	}

	// TODO This test occasionally fails
	/**
	 * Makes sure that the network with hidden units can learn correctly.
	 */
	@Test
	public void testXor() {
		board = new Board();
		network = new Network(.2, 2, 2);
		weightsInput = network.getWeightsInputHidden();
		weightsOutput = network.getWeightsHiddenOutput();
		weightsSkip = network.getWeightsInputOutput();
		for (int i = 0; i < 5000; i++) {
			board.clear();
			board.play(at("d1"));
			board.play(at("b1"));
			board.play("a1");
			network.learn(BLACK, board, board.getTurn() - 1, 0);
			board.clear();
			board.play(at("e1"));
			board.play(at("b1"));
			board.play("a1");
			network.learn(BLACK, board, board.getTurn() - 1, 1);
			board.clear();
			board.play(at("d1"));
			board.play(at("c1"));
			board.play("a1");
			network.learn(BLACK, board, board.getTurn() - 1, 1);
			board.clear();
			board.play(at("e1"));
			board.play(at("c1"));
			board.play("a1");
			network.learn(BLACK, board, board.getTurn() - 1, 0);
		}
		board.clear();
		board.play(at("d1"));
		board.play(at("b1"));
		assertTrue(network.evaluate(BLACK, at("a1"), board, board.getTurn()) < .3);
		board.clear();
		board.play(at("e1"));
		board.play(at("b1"));
		assertTrue(network.evaluate(BLACK, at("a1"), board, board.getTurn()) > .7);
		board.clear();
		board.play(at("d1"));
		board.play(at("c1"));
		assertTrue(network.evaluate(BLACK, at("a1"), board, board.getTurn()) > .7);
		board.clear();
		board.play(at("e1"));
		board.play(at("c1"));
		assertTrue(network.evaluate(BLACK, at("a1"), board, board.getTurn()) < .3);
	}

	/**
	 * Tests a history of 3
	 */
	@Test
	public void testHistoryOf3() {
		board = new Board();
		network = new Network(.2, 2, 3);
		weightsInput = network.getWeightsInputHidden();
		weightsOutput = network.getWeightsHiddenOutput();
		weightsSkip = network.getWeightsInputOutput();
		for (int i = 0; i < 5000; i++) {
			board.clear();
			board.play(at("b1"));
			board.play(at("c1"));
			board.play(at("d1"));
			board.play("a1");
			network.learn(BLACK, board, board.getTurn() - 1, 1);
			board.clear();
			board.play(at("b1"));
			board.play(at("c1"));
			board.play(at("e1"));
			board.play("a1");
			network.learn(BLACK, board, board.getTurn() - 1, 0);
		}
		board.clear();
		board.play(at("b1"));
		board.play(at("c1"));
		board.play(at("d1"));
		assertTrue(network.evaluate(BLACK, at("a1"), board, board.getTurn()) > .7);
		board.clear();
		board.play(at("b1"));
		board.play(at("c1"));
		board.play(at("e1"));
		assertTrue(network.evaluate(BLACK, at("a1"), board, board.getTurn()) < .3);
	}

}
