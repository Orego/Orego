package edu.lclark.orego.genetic;

import edu.lclark.orego.core.CoordinateSystem;

public class LinearNeuron {

	private byte bias;
	
	private byte[][] weights;
	
	public LinearNeuron(byte bias, byte[][] weights) {
		this.bias = bias;
		this.weights = weights;
	}
	
	public int activity(long[] featureMap, CoordinateSystem coords) {
		// TODO If we have fewer than 64 features in the previous layer, this wastes some work
		int sum = bias;
		for (short p : coords.getAllPointsOnBoard()) {
			long features = featureMap[p];
			for (int i = 0; i < 64; i++) {
				if ((features & 1L) != 0) {
					sum += weights[p][i];
				}
				features >>>= 1;
			}
		}
		return sum;
	}

	void setBias(byte bias) {
		this.bias = bias;
	}

}
