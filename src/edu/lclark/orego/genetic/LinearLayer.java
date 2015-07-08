package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.Predicate;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;

public class LinearLayer {

	private CoordinateSystem coords;
	
	private long[] inputs;
	
	private LinearNeuron[] neurons;
	
	private int[] outputs;

	public LinearLayer(ConvolutionalLayer previous, CoordinateSystem coords) {
		this(previous, coords, new byte[coords.getFirstPointBeyondBoard()], new byte[coords.getFirstPointBeyondBoard()][coords.getFirstPointBeyondBoard()][64]);
	}

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

	public short bestMove(Board board, Predicate filter) {
		short bestMove = PASS;
		int bestValue = Integer.MIN_VALUE;
		final ShortSet vacantPoints = board.getVacantPoints();
		// TODO Is it important to try these in random order?
		for (int i = 0; i < vacantPoints.size(); i++) {
			final short p = vacantPoints.get(i);
			// TODO isLegalFast?
			if (filter.at(p) && board.isLegal(p)) {
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

	public void setBias(short p, byte bias) {
		neurons[p].setBias(bias);
	}
	
	byte getBias(short p) {
		return neurons[p].getBias();
	}

	public byte getWeight(short to, short from, int feature) {
		return neurons[to].getWeight(from, feature);
	}
	
	public void setWeight(short to, short from, int feature, byte weight) {
		neurons[to].setWeight(from, feature, weight);
	}

	public void randomizeBiases() {
		MersenneTwisterFast random = new MersenneTwisterFast();
		for (short p : coords.getAllPointsOnBoard()) {
			neurons[p].setBias(random.nextByte());
		}
	}

}
