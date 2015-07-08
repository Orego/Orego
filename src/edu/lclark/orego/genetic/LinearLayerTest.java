package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.StoneColor.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.NotEyeLike;

public class LinearLayerTest {

	private Board board;
	
	private CoordinateSystem coords;
	
	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
	}

	private short at(String label) {
		return coords.at(label);
	}

	@Test
	public void testUpdate() {
		// For this neuron to fire, there must be a friendly stone in the receptive field
		// For this neuron to fire, there must be 3 more friendly stones than
		// enemy stones in the receptive field
		ConvolutionalNeuron cNeuron = new ConvolutionalNeuron(3,
				new long[] { 0b01L, 0b01L, 0b01L, 0b01L, 0b01L, 0b01L, 0b01L,
						0b01L, 0b01L }, new long[] { 0b10L, 0b10L, 0b10L,
						0b10L, 0b10L, 0b10L, 0b10L, 0b10L, 0b10L });
		ConvolutionalLayer cLayer = new ConvolutionalLayer(coords, cNeuron);
		byte[][][] weights = new byte[coords.getFirstPointBeyondBoard()][coords.getFirstPointBeyondBoard()][64];
		byte[] biases = new byte[coords.getFirstPointBeyondBoard()];
		weights[at("a5")][at("a1")][0] = (byte)4;
		weights[at("a5")][at("a3")][0] = (byte)-1;
		weights[at("a5")][at("d2")][0] = (byte)70;
		biases[at("a5")] = 10;
		weights[at("e1")][at("a1")][0] = (byte)1;
		weights[at("e1")][at("a3")][0] = (byte)2;
		weights[at("e1")][at("d2")][0] = (byte)3;
		biases[at("e1")] = 4;
		weights[at("c4")][at("a1")][0] = (byte)4;
		weights[at("c4")][at("a3")][0] = (byte)-1;
		weights[at("c4")][at("d2")][0] = (byte)70;
		biases[at("c4")] = 10;
		LinearLayer lLayer = new LinearLayer(cLayer, coords, biases, weights);
		String[] diagram = {
				"..O..",
				"#O.O.",
				".#O..",
				"##...",
				".#...",
		};
		board.setUpProblem(diagram, BLACK);
		cLayer.extractFeatures(board);
		cLayer.update();
		assertEquals(at("a5"), lLayer.bestMove(board, new NotEyeLike(board)));
	}

	@Test
	public void testFilter() {
		ConvolutionalLayer cLayer = new ConvolutionalLayer(coords);
		byte[] biases = new byte[coords.getFirstPointBeyondBoard()];
		byte[][][] weights = new byte[coords.getFirstPointBeyondBoard()][coords.getFirstPointBeyondBoard()][64];
		biases[at("e1")] = 10;
		LinearLayer lLayer = new LinearLayer(cLayer, coords, biases, weights);
		String[] diagram = {
				"..O..",
				"#O.O.",
				".#O..",
				"##..O",
				".#.O.",
		};
		board.setUpProblem(diagram, WHITE);
		cLayer.extractFeatures(board);
		cLayer.update();
		assertNotEquals(at("e1"), lLayer.bestMove(board, new NotEyeLike(board)));
		
	}

}
