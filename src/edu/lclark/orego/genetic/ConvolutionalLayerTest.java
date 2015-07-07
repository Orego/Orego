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
		// For this neuron to fire, there must be 3 more friendly stones than
		// enemy stones in the receptive field
		ConvolutionalNeuron neuron1 = new ConvolutionalNeuron(3,
				new long[] { 0b01L, 0b01L, 0b01L, 0b01L, 0b01L, 0b01L, 0b01L,
						0b01L, 0b01L }, new long[] { 0b10L, 0b10L, 0b10L,
						0b10L, 0b10L, 0b10L, 0b10L, 0b10L, 0b10L });
		// This neuron fires if there are 3 off-board points in the receptive
		// field
		ConvolutionalNeuron neuron2 = new ConvolutionalNeuron(3, new long[] {
				0b1000L, 0b1000L, 0b1000L, 0b1000L, 0b1000L, 0b1000L, 0b1000L,
				0b1000L, 0b1000L }, new long[] { 0b0L, 0b0L, 0b0L, 0b0L, 0b0L,
				0b0L, 0b0L, 0b0L, 0b0L, });
		layer = new ConvolutionalLayer(coords, neuron1, neuron2);
		String[] diagram = {
				"..O..",
				"#O.O.",
				".#O..",
				"##...",
				".#...", };
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
		assertEquals(0b00L,
				outputs[coords.getNeighbors(at("a1"))[WEST_NEIGHBOR]]);
	}
	
	@Test
	public void testAsymmetry() {
		// This neuron only fires if there is a friendly stone to the right of it
		long[] excitatory = new long[9];
		excitatory[5] = 0b1L;
		long[] inhibitory = new long[9];
		ConvolutionalNeuron neuron = new ConvolutionalNeuron(1, excitatory, inhibitory);
		layer = new ConvolutionalLayer(coords, neuron);
		String[] diagram = {
				"..O..",
				"#O.O.",
				".#O..",
				"##...",
				".#...", };
		board.setUpProblem(diagram, BLACK);
		long[] outputs = layer.getOutputs();
		layer.extractFeatures(board);
		layer.update();
		assertEquals(0b1L, outputs[at("a1")]);
		assertEquals(0b1L, outputs[at("a2")]);
		assertEquals(0b1L, outputs[at("a3")]);
		assertEquals(0b0L, outputs[at("c2")]);
	}
	
	@Test 
	public void testMultipleLayers() {
		long[] excitatoryLeft = new long[9];
		excitatoryLeft[3] = 0b1L;
		long[] excitatoryRight = new long[9];
		excitatoryRight[5] = 0b1L;
		long[] inhibitory = new long[9];
		ConvolutionalNeuron friendlyLeft = new ConvolutionalNeuron(1, excitatoryLeft, inhibitory);
		ConvolutionalNeuron friendlyRight = new ConvolutionalNeuron(1, excitatoryRight, inhibitory);
		layer = new ConvolutionalLayer(coords, friendlyLeft, friendlyRight);
		long[] excitatoryWide = new long[9];
		excitatoryWide[3] = 0b1L;
		excitatoryWide[5] = 0b10L;
		ConvolutionalNeuron wide = new ConvolutionalNeuron(2, excitatoryWide, inhibitory);
		ConvolutionalLayer layer2 = new ConvolutionalLayer(layer, coords, wide);
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
		assertEquals(0b0L, outputs[at("d4")]);
	}

}
