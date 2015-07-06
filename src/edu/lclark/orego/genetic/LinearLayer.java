package edu.lclark.orego.genetic;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

public class LinearLayer {

	private long[] inputs;
	
	private int[] outputs;
	
	private LinearNeuron[] neurons;
	
	private CoordinateSystem coords;
	
	public LinearLayer(ConvolutionalLayer previous, CoordinateSystem coords,
			byte[] biases, byte[][][] weights) {
		inputs = previous.getOutputs();
		this.coords = coords;
		final int n = coords.getFirstPointBeyondBoard();
		outputs = new int[n];
		neurons = new LinearNeuron[n];
		for (short p : coords.getAllPointsOnBoard()) {
			neurons[p] = new LinearNeuron(biases[p], weights[p]);
		}
	}

	public void update(Board board) {
		for (short p : coords.getAllPointsOnBoard()) {
			// TODO isLegalFast?
			if (board.isLegal(p)) {
				outputs[p] = neurons[p].activity(inputs, coords);
			}
		}
	}

	public int[] getOutputs() {
		return outputs;
	}

}
