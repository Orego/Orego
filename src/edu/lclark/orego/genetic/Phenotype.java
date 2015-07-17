package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.RESIGN;
import edu.lclark.orego.core.CoordinateSystem;

public class Phenotype {

	/** Has only the 9 lowest-order bits on. */
	public static final int MASK9 = (1 << 9) - 1;

	/** Indicates that one of the three points in a reply should be ignored. */
	public static final short IGNORE = RESIGN;
	
	private final short[][] replies;
	
	public Phenotype(CoordinateSystem coords) {
		replies = new short[coords.getFirstPointBeyondBoard()][coords
		                                       				.getFirstPointBeyondBoard()];
	}

//	public Phenotype(CopiableStructure richBoard, Genotype genotype) {
//		this(richBoard);
//		int[] words = genotype.getGenes();
//		// Extract replies
//		for (int i = 0; i < words.length; i++) {
//			setReply((short) (words[i] & MASK9),
//					(short) ((words[i] >>> 9) & MASK9),
//					(short) ((words[i] >>> 18) & MASK9));
//		}
//	}

	public void installGenes(Genotype genotype) {
		
	}

	public short getRawReply(short penultimate, short ultimate) {
		return replies[penultimate][ultimate];
	}
	

	short followUp(short penultimateMove) {
		return replies[penultimateMove][RESIGN];
	}

	short playBigPoint() {
		return replies[RESIGN][RESIGN];
	}

	short replyToOneMove(short previousMove) {
		return replies[RESIGN][previousMove];
	}

	short replyToTwoMoves(short penultimateMove, short previousMove) {
		return replies[penultimateMove][previousMove];
	}
	
	void setReply(short penultimateMove, short ultimateMove, short reply) {
		replies[penultimateMove][ultimateMove] = reply;
	}
	
}

