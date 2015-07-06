package edu.lclark.orego.genetic;

import static java.lang.Long.*;
import edu.lclark.orego.core.CoordinateSystem;

public class ConvolutionalNeuron {

	private int threshold;

	private long excitation;

	private long inhibition;

	public ConvolutionalNeuron(int threshold, long excitation, long inhibition) {
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
		for (short n : coords.getNeighbors(p)) {
			long input = featureMap[n];
			sum += bitCount(excitation & input);
			sum -= bitCount(inhibition & input);
		}
		return sum >= threshold;
	}

}
