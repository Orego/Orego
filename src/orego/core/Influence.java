package orego.core;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;

/**
 * 
 * This class can be used to estimate the influence exerted on each point of the
 * board by the surrounding stones as well as estimating the territory of each
 * player.
 * 
 * To calculate influence:
 *     Influence inf = new Influence(board);
 *     inf.zobristDilate(4);
 *     int i = inf.get
 * 
 * To calculate territory:
 *     Influence inf = new Influence(board);
 *     inf.bouzyDilate(4);
 *     inf.bouzyErode(13);
 * 
 * This class estimates the territory of each player on the board, assigning a
 * numerical value to each point, from -64 to -1 for white and from 1 to 64 for
 * black.
 */
public class Influence {

	/**
	 * The current array of influence values (indexed by point) and a scratch
	 * array for writing the next influence values.
	 * 
	 * We have this scratch array because we perform dilation and erosion
	 * operations that need to be atomic: as we dilate or erode the influence
	 * values in the array, we don't want to refer to influence values that have
	 * been changed by this operation. So we read from the reading area and
	 * write into the writing area. Then we switch the two.
	 */
	private int[][] influence = new int[2][getFirstPointBeyondBoard()];

	/**
	 * The index of the most recently calculated influence values in
	 * influence[]. Reading the values should be done from here.
	 */
	private int readIndex = 0;

	/**
	 * The index of the next set of influence values in influence[]. Writing
	 * the next set of values should be done here (and the two indices should be
	 * switched when writing is complete).
	 */
	private int writeIndex = 1;

	/** The board we are using to determine influence values. */
	private Board board;

	/** Initialize the influence values of each point on the board. */
	public Influence(Board b) {
		readIndex = 0;
		writeIndex = 1;
		board = b;
		for (int p : getAllPointsOnBoard()) {
			switch (board.getColor(p)) {
			case BLACK:
				influence[readIndex][p] = 64;
				break;
			case WHITE:
				influence[readIndex][p] = -64;
				break;
			default:
				influence[readIndex][p] = 0;
			}
		}
	}

	/**
	 * Perform Bouzy's dilation operation a certain number of times.
	 * 
	 * @param n
	 *            The number of times to dilate.
	 */
	public void bouzyDilate(int n) {
		for (int i = 0; i < n; i++) {
			bouzyDilate();
		}
	}
	
	/**
	 * Perform Zobrist's dilation operation a certain number of times.
	 * 
	 * @param n
	 *           The number of times to dilate.
	 */
	public void zobristDilate(int n) {
		for (int i = 0; i < n; i++) {
			zobristDilate();
		}
	}
	/** Perform Zobrist's dilation operation once. */
	public void zobristDilate() {
		for (int p : getAllPointsOnBoard()) {
			// Learn about our neighbors
			int positives = 0;
			int negatives = 0;
			for (int i = 0; i < 4; i++) {
				if (board.getColor(getNeighbors(p)[i]) != OFF_BOARD_COLOR) {
					int influenceOfNeighbor = getValue(getNeighbors(p)[i]);
					if (influenceOfNeighbor > 0) {
						positives++;
					} else if (influenceOfNeighbor < 0) {
						negatives++;
					}
				}
			}
			// Update our territory value
			int currentInfluence = getValue(p);
			influence[writeIndex][p] = currentInfluence + positives - negatives;
		}

		int t = readIndex;
		readIndex = writeIndex;
		writeIndex = t;
	}

	/** Perform the dilation operation once. */
	public void bouzyDilate() {
		// If a point, p, already belongs to a certain color, c, add to p's
		// territory value the number of its neighbors who also belong to c, as
		// long as no neighbor belongs to the ~color.

		// If a point has no owner, and all of its neighbors have the same owner
		// as each other (or none at all), make the point have that owner, too,
		// and set its territory value to the number of those neighbors.
		for (int p : getAllPointsOnBoard()) {
			// Learn about our neighbors
			int positives = 0;
			int negatives = 0;
			for (int i = 0; i < 4; i++) {
				if (board.getColor(getNeighbors(p)[i]) != OFF_BOARD_COLOR) {
					int influenceOfNeighbor = getValue(getNeighbors(p)[i]);
					if (influenceOfNeighbor > 0) {
						positives++;
					} else if (influenceOfNeighbor < 0) {
						negatives++;
					}
				}
			}
			// Update our territory value
			int currentInfluence = getValue(p);
			if (currentInfluence == 0 && negatives == 0) {
				// add the number of positives
				influence[writeIndex][p] = currentInfluence + positives;
			} else if (currentInfluence == 0 && positives == 0) {
				// subtract the number of negatives
				influence[writeIndex][p] = currentInfluence - negatives;
			} else if (currentInfluence > 0 && negatives == 0) {
				// add the number of positives
				influence[writeIndex][p] = currentInfluence + positives;
			} else if (currentInfluence < 0 && positives == 0) {
				// subtract the number of negatives
				influence[writeIndex][p] = currentInfluence - negatives;
			} else {
				influence[writeIndex][p] = currentInfluence;
			}

			if (influence[writeIndex][p] > 64) {
				influence[writeIndex][p] = 64;
			} else if (influence[writeIndex][p] < -64) {
				influence[writeIndex][p] = -64;
			}
		}

		int t = readIndex;
		readIndex = writeIndex;
		writeIndex = t;
	}

	/**
	 * Perform Bouzy's erosion operation a certain number of times.
	 * 
	 * @param n
	 *            The number of times to erode.
	 */
	public void bouzyErode(int n) {
		for (int i = 0; i < n; i++) {
			bouzyErode();
		}
	}

	/** Perform Bouzy's dilation operation once. */
	public void bouzyErode() {

		// Subtract from the (absolute) territory value of a point with a given nature,
		// the number of its neighbors with no owner or opposite owner.
		
		// Also, make sure that if a point changes sign, it ends up as having no owner.

		for (int p : getAllPointsOnBoard()) {
			// Learn about our neighbors
			int positives = 0;
			int negatives = 0;
			int zeros = 0;
			for (int i = 0; i < 4; i++) {
				if (board.getColor(getNeighbors(p)[i]) != OFF_BOARD_COLOR) {
					int influenceOfNeighbor = getValue(getNeighbors(p)[i]);
					if (influenceOfNeighbor > 0) {
						positives++;
					} else if (influenceOfNeighbor < 0) {
						negatives++;
					} else if (influenceOfNeighbor == 0) {
						zeros++;
					}
				}
			}
			// Update our territory value
			if (getValue(p) > 0) {

				influence[writeIndex][p] = influence[readIndex][p]
						- negatives - zeros;
				if (influence[writeIndex][p] < 0) {
					influence[writeIndex][p] = 0;
				}
			} else if (getValue(p) < 0) {

				influence[writeIndex][p] = influence[readIndex][p]
						+ positives + zeros;
				if (influence[writeIndex][p] > 0) {
					influence[writeIndex][p] = 0;
				}
			} else {
				influence[writeIndex][p] = influence[readIndex][p];
			}
		}
		int t = readIndex;
		readIndex = writeIndex;
		writeIndex = t;
	}

	/** Return the color of the player who the point p belongs to. */
	public int getOwner(int p) {
		if (influence[readIndex][p] == 0) {
			return VACANT;
		} else if (influence[readIndex][p] > 0) {
			return BLACK;
		} else {
			return WHITE;
		}
	}

	/** Return the strength of the influence of the point p. */
	public int getValue(int p) {
		return influence[readIndex][p];
	}

	/**
	 * Return a human-readable representation of the territory value of each
	 * point.
	 */
	@Override
	public String toString() {
		String output = "";
		for (int i = 0; i < getBoardWidth(); i++) {
			for (int j = 0; j < getBoardWidth(); j++) {
				int p = Coordinates.at(i, j);
				int inf = getValue(p);
//				String displayInfluence;
//				if (inf > 0) {
//					displayInfluence = "#  ";
//				} else if (inf < 0) {
//					displayInfluence = "O  ";
//				} else {
//					displayInfluence = "-  ";
//				}
				output += String.format("%4d", inf);
			}
			output += "\n";
		}
		return output;
	}
}
