package edu.lclark.orego.genetic;

import static java.lang.Long.*;
import edu.lclark.orego.core.CoordinateSystem;

public class ConvolutionalNeuron {

	private int threshold;

	private long[] excitation;

	private long[] inhibition;

	public ConvolutionalNeuron(int threshold, long[] excitation, long[] inhibition) {
		this.threshold = threshold;
		this.excitation = excitation;
		this.inhibition = inhibition;
	}

	public static void main(String[] args) {
		System.out.println(Long.toBinaryString(Long.MIN_VALUE));
		System.out.println(Integer.toBinaryString(-2));
	}

	/** Returns true if this neuron is active at point p. */
	public boolean isActiveAt(short p, long[] featureMap, CoordinateSystem coords) {
		int sum = 0;
		final short[] field = coords.getReceptiveField(p);
		for (int i = 0; i < excitation.length; i++) {
			final long features = featureMap[field[i]];
			sum += bitCount(excitation[i] & features);
			sum -= bitCount(inhibition[i] & features);
		}
		return sum >= threshold;
	}

}
