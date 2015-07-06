package edu.lclark.orego.genetic;

import edu.lclark.orego.core.Board;
import static java.lang.Long.*;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.StoneColor.*;

public class Neuron {

	private int threshold;

	private long excitation;

	private long inhibition;

	/**
	 * Extracts features for each point on the board.
	 * 
	 * @param features
	 *            Results are written into this array, which has one element per
	 *            board (or board-adjacent) point.
	 */
	public static void extractFeatures(Board board, long[] features) {
		for (short p = 0; p < features.length; p++) {
			long temp = board.getColorAt(p).index();
			// Invert stone colors if it's white's turn
			if (board.getColorToPlay() == WHITE && temp < 2) {
				temp = 1 - temp;
			}
			features[p] = 1L << temp;
		}		
	}

	public Neuron(int threshold, long excitation, long inhibition) {
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
