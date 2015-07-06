package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.*;
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

	public short bestMove(Board board) {
		short bestMove = PASS;
		int bestValue = Integer.MIN_VALUE;
		final ShortSet vacantPoints = board.getVacantPoints();
		// TODO Is it important to try these in random order?
		for (int i = 0; i < vacantPoints.size(); i++) {
			final short p = vacantPoints.get(i);
			// TODO isLegalFast?
			if (board.isLegal(p)) {
				int value = neurons[p].activity(inputs, coords);
				if (value > bestValue) {
					bestMove = p;
					bestValue = value;
				}
			}
		}
		return bestMove;
	}

	public int[] getOutputs() {
		return outputs;
	}

}
