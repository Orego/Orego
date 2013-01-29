package orego.decision;

import java.util.*;
import static java.lang.Math.log;
import static orego.core.Coordinates.*;

/** A collection of Datum objects. */
public class DataCollection extends ArrayList<Datum> {

	/** Indicates that no move is useful to split the data collection. */
	public static final int NO_SPLIT = -1;

	private static final long serialVersionUID = 1L;

	/**
	 * Returns the total number of data points having the specified previous move at the specified history depth.
	 */
	public int countAfter(int previous, int depth) {
		int count = 0;
		for (int i = 0; i < size(); i++) {
			if (get(i).getPrevious()[depth] == previous) {
				count++;
			}
		}
		return count;
	}

	/** Returns the number of wins in this collection. */
	public int countWins() {
		int count = 0;
		for (int i = 0; i < size(); i++) {
			if (get(i).isWin()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Returns the number of winning data points that have (if has is true) or
	 * don't have (if has is false) the specified previous move at the specified history depth.
	 */
	public int countWinsAfter(int previous, int depth, boolean has) {
		int count = 0;
		for (int i = 0; i < size(); i++) {
			Datum d = get(i);
			if (d.isWin() && ((d.getPrevious()[depth] == previous) == has)) {
				count++;
			}
		}
		return count;
	}

	/** Returns the entropy of a boolean attribute with probability p. */
	public double entropy(double p) {
		if (p == 0.0 || (1 - p) == 0.0) {
			return 0;
		}
		return -(p * (log(p) / log(2)) + (1 - p) * (log(1 - p) / log(2)));
	}

	/**
	 * Finds the best move on which to split this data collection. The move and history depth
	 * are stored in node.
	 */
	public void getBestSplitMove(Node node) {
		int move = NO_SPLIT;
		int depth = node.getDepth();
		if (depth >= get(0).getPrevious().length) {
			node.setPrevMove(NO_SPLIT);
			return;
		}
		double minRemain = Double.POSITIVE_INFINITY;
		int p = PASS;
		double remain = remainder(p, depth);
		if (remain < minRemain) {
			minRemain = remain;
			move = p;
		}
		p = NO_POINT;
		remain = remainder(p, depth);
		if (remain < minRemain) {
			minRemain = remain;
			move = p;
		}
		for (int q : ALL_POINTS_ON_BOARD) {
			remain = remainder(q, depth);
			if (remain < minRemain) {
				minRemain = remain;
				move = q;
			}
		}
		node.setPrevMove(move);
	}

	/** Returns the fraction of Datum objects in this collection that are wins. */
	public double getWinRate() {
		if (size() != 0) {
			return (double) countWins() / size();
		} else {
			return -1;
		}
	}

	/**
	 * Returns the remaining entropy after splitting on previous.
	 * 
	 * @return POSITIVE_INFINITY if this split is useless, e.g., either all or
	 *         no data have this previous move.
	 */
	public double remainder(int previous, int depth) {
		double yes = countAfter(previous, depth);
		double no = size() - yes;
		if (yes == 0 || no == 0) {
			return Double.POSITIVE_INFINITY;
		}
//		if (yes < 100) {
//			return Double.POSITIVE_INFINITY;			
//		}
		return (yes / size())
				* entropy(countWinsAfter(previous, depth, true) / yes)
				+ (no / size())
				* entropy(countWinsAfter(previous, depth, false) / no);
	}

	public String toString() {
		int wins = 0;
		int runs = 0;
		for (Datum d : this) {
			runs++;
			if (d.isWin()) {
				wins++;
			}
		}
		String result = "";
		result += "dataCollection win rate: " + (((double) wins) / runs) + ", "
				+ wins + "/" + runs;
		return result;
	}

}
