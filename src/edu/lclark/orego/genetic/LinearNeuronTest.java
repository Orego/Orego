package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class LinearNeuronTest {

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
	public void testActivity() {
		// For this neuron to fire, there must be 3 more friendly stones than enemy stones in the receptive field
		// For this neuron to fire, there must be 3 more friendly stones than
		// enemy stones in the receptive field
		ConvolutionalNeuron cNeuron1 = new ConvolutionalNeuron(3,
				new long[] { 0b01L, 0b01L, 0b01L, 0b01L, 0b01L, 0b01L, 0b01L,
						0b01L, 0b01L }, new long[] { 0b10L, 0b10L, 0b10L,
						0b10L, 0b10L, 0b10L, 0b10L, 0b10L, 0b10L });
		// This neuron fires if there are 3 off-board points in the receptive
		// field
		ConvolutionalNeuron cNeuron2 = new ConvolutionalNeuron(3, new long[] {
				0b1000L, 0b1000L, 0b1000L, 0b1000L, 0b1000L, 0b1000L, 0b1000L,
				0b1000L, 0b1000L }, new long[] { 0b0L, 0b0L, 0b0L, 0b0L, 0b0L,
				0b0L, 0b0L, 0b0L, 0b0L, });
		ConvolutionalLayer cLayer = new ConvolutionalLayer(coords, cNeuron1, cNeuron2);
		byte[][] weights = new byte[coords.getFirstPointBeyondBoard()][64];
		weights[at("a1")][0] = (byte)4;
		weights[at("a3")][0] = (byte)-1;
		weights[at("d2")][0] = (byte)70;
		weights[at("d1")][1] = (byte)-5;
		LinearNeuron lNeuron = new LinearNeuron((byte)10, weights);
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
		assertEquals(8, lNeuron.activity(cLayer.getOutputs(), coords));
	}

}
