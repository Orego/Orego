package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.StoneColor.WHITE;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class ConvolutionalLayer {

	private CoordinateSystem coords;
	
	private long[] inputs;

	private Neuron[] neurons;

	private long[] outputs;
	
	public ConvolutionalLayer(CoordinateSystem coords, Neuron... neurons) {
		this.coords = coords;
		final int n = coords.getFirstPointBeyondExtendedBoard();
		outputs = new long[n];
		inputs = new long[n];
		this.neurons = neurons;
	}

	public ConvolutionalLayer(ConvolutionalLayer lowerLayer,
			CoordinateSystem coords, Neuron ... neurons) {
		this.coords = coords;
		outputs = new long[coords.getFirstPointBeyondExtendedBoard()];
		inputs = lowerLayer.getOutputs();
		this.neurons = neurons;
	}

	/**
	 * Extracts features for each point on the board. The results are written
	 * into this layer's inputs.
	 */
	public void extractFeatures(Board board) {
		for (short p = 0; p < inputs.length; p++) {
			long temp = board.getColorAt(p).index();
			// Invert stone colors if it's white's turn
			if (board.getColorToPlay() == WHITE && temp < 2) {
				temp = 1 - temp;
			}
			inputs[p] = 1L << temp;
		}
	}

	public long[] getInputs() {
		return inputs;
	}

	public long[] getOutputs() {
		return outputs;
	}

	/** Updates this layer's outputs. */
	public void update() {
		for (short p : coords.getAllPointsOnBoard()) {
			long result = 0L;
			long mask = 1L;
			for (int i = 0; i < neurons.length; i++) {
				if (neurons[i].isActiveAt(p, inputs, coords)) {
					result |= mask;
				}
				mask <<= 1;
			}
			outputs[p] = result;
		}
	}

}
