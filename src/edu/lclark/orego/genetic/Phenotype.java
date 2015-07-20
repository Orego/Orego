package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.RESIGN;
import edu.lclark.orego.core.CoordinateSystem;

/** Instantiated Genotype. */
public class Phenotype {

	/** Indicates that one of the three points in a reply should be ignored. */
	public static final short IGNORE = RESIGN;
	
	/** Has only the 9 lowest-order bits on. */
	public static final int MASK9 = (1 << 9) - 1;

	/** replies[p][q] is the best reply to p followed by q. */
	private final short[][] replies;

	/** Number of wins this Phenotype has achieved. */
	private int winCount;

	public Phenotype(CoordinateSystem coords) {
		replies = new short[coords.getFirstPointBeyondBoard()][coords
				.getFirstPointBeyondBoard()];
	}

	short followUp(short penultimateMove) {
		return replies[penultimateMove][IGNORE];
	}

	public int getWinCount() {
		return winCount;
	}

	public void installGenes(Genotype genotype) {
		synchronized (genotype) {
			int[] words = genotype.getGenes();
			for (int i = 0; i < replies.length; i++) {
				java.util.Arrays.fill(replies[i], (short) 0);
			}
			// Extract replies
			for (int i = 0; i < words.length; i++) {
				setReply((short) (words[i] & MASK9),
						(short) ((words[i] >>> 9) & MASK9),
						(short) ((words[i] >>> 18) & MASK9));
			}
		}
	}

	short playBigPoint() {
		return replies[IGNORE][IGNORE];
	}

	short replyToOneMove(short previousMove) {
		return replies[IGNORE][previousMove];
	}

	short replyToTwoMoves(short penultimateMove, short previousMove) {
		return replies[penultimateMove][previousMove];
	}

	void setReply(short penultimateMove, short ultimateMove, short reply) {
		replies[penultimateMove][ultimateMove] = reply;
	}

	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

}
