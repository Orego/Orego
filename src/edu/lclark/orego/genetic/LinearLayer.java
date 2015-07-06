package edu.lclark.orego.genetic;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;

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
		final ShortSet vacantPoints = board.getVacantPoints();
		// TODO Is it important to try these in random order?
		for (int i = 0; i < vacantPoints.size(); i++) {
			final short p = vacantPoints.get(i);
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
