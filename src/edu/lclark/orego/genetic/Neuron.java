package edu.lclark.orego.genetic;

import edu.lclark.orego.core.Board;
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
		final CoordinateSystem coords = board.getCoordinateSystem();
		for (short p = 0; p < features.length; p++) {
			long temp = board.getColorAt(p).index();
			// Invert stone colors if it's white's turn
			if (board.getColorToPlay() == WHITE && temp < 2) {
				temp = 1 - temp;
			}
			features[p] = 1L << temp;
		}
		
	}

	// TODO Separate extracting features per point and gathering them per
	// neighborhood
	// TODO Does this belong here or in ConvolutionalLayer?
	/**
	 * Extracts the features around point p into the long array features.
	 * 
	 * @param feature
	 *            An array of length 8, with one element for each neighbor of p.
	 *            Results are written into this array.
	 */
	public static void extractFeaturesAround(short p, Board board,
			long[] features) {
		final CoordinateSystem coords = board.getCoordinateSystem();
		final short[] neighbors = coords.getNeighbors(p);
		for (int i = 0; i < features.length; i++) {
			long temp = board.getColorAt(neighbors[i]).index();
			// Invert stone colors if it's white's turn
			if (board.getColorToPlay() == WHITE && temp < 2) {
				temp = 1 - temp;
			}
			features[i] = 1L << temp;
		}
	}

	public Neuron(int threshold, long excitation, long inhibition) {
		this.threshold = threshold;
		this.excitation = excitation;
		this.inhibition = inhibition;
	}

	/** Returns the number of bits set in bits. */
	static int countBits(long bits) {
		// Brian Kernighan's method, adapted from:
		// https://graphics.stanford.edu/~seander/bithacks.html#CountBitsSetKernighan
		int count;
		for (count = 0; bits != 0; count++) {
			bits &= bits - 1; // clear the least significant bit set
		}
		return count;
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
			sum += countBits(excitation & input);
			sum -= countBits(inhibition & input);
		}
		return sum >= threshold;
	}

}
