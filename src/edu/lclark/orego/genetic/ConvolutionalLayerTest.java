package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.WEST_NEIGHBOR;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class ConvolutionalLayerTest {

	
	// TODO Should board and coords be inside layer?
	
	private Board board;
	
	private CoordinateSystem coords;
	
	private ConvolutionalLayer layer;
	
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new Board(5);
		coords = board.getCoordinateSystem();
		layer = new ConvolutionalLayer(coords);
	}

	@Test
	public void testExtractFeatures() {
		String[] diagram = {
				".....",
				".....",
				".....",
				"#....",
				".O...",
		};
		board.setUpProblem(diagram, BLACK);
		long[] features = layer.getInputs();
		layer.extractFeatures(board);
		assertEquals(0b0001, features[at("a2")]); // Friendly
		assertEquals(0b0010, features[at("b1")]); // Enemy
		assertEquals(0b0100, features[at("a1")]); // Vacant
		assertEquals(0b1000, features[coords.getNeighbors(at("a1"))[WEST_NEIGHBOR]]); // Off board
		board.setColorToPlay(WHITE);
		layer.extractFeatures(board);
		assertEquals(0b0010, features[at("a2")]); // Enemy
		assertEquals(0b0001, features[at("b1")]); // Friendly
		assertEquals(0b0100, features[at("a1")]); // Vacant
		assertEquals(0b1000, features[coords.getNeighbors(at("a1"))[WEST_NEIGHBOR]]); // Off board
	}

	@Test
	public void testUpdate() {
		// For this neuron to fire, there must be 3 more friendly stones than enemy stones in the receptive field
		ConvolutionalNeuron neuron1 = new ConvolutionalNeuron(3, 0b01L, 0b10L);
		ConvolutionalNeuron neuron2 = new ConvolutionalNeuron(3, 0b1000L, 0b0L);
		layer = new ConvolutionalLayer(coords, neuron1, neuron2);
		// TODO Install neurons in layer
		String[] diagram = {
				"..O..",
				"#O.O.",
				".#O..",
				"##...",
				".#...",
		};
		board.setUpProblem(diagram, BLACK);
		long[] outputs = layer.getOutputs();
		layer.extractFeatures(board);
		layer.update();
		assertEquals(0b11L, outputs[at("a1")]);
		assertEquals(0b11L, outputs[at("a3")]);
		assertEquals(0b00L, outputs[at("c2")]);
		board.setUpProblem(diagram, WHITE);
		layer.extractFeatures(board);
		layer.update();
		assertEquals(0b01L, outputs[at("c4")]);
		assertEquals(0b00L, outputs[at("d3")]);
		assertEquals(0b10L, outputs[at("e1")]);
		assertEquals(0b00L, outputs[coords.getNeighbors(at("a1"))[WEST_NEIGHBOR]]);
	}
	
	@Test 
	public void testMultipleLayers() {
		ConvolutionalNeuron friendly = new ConvolutionalNeuron(1, 0b1L, 0b0L);
		layer = new ConvolutionalLayer(coords, friendly);
		ConvolutionalNeuron friendly2 = new ConvolutionalNeuron(2, 0b1L, 0b0L);
		ConvolutionalLayer layer2 = new ConvolutionalLayer(layer, coords, friendly2);
		String[] diagram = {
				".....",
				"#...#",
				".....",
				".....",
				"..O..",
		};
		board.setUpProblem(diagram, BLACK);
		layer.extractFeatures(board);
		layer.update();
		layer2.update();
		long[] outputs = layer2.getOutputs();
		assertEquals(0b1L, outputs[at("c4")]);
		board.setUpProblem(diagram, WHITE);	
	}

}
