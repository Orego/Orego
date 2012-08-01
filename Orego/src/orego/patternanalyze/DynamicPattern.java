package orego.patternanalyze;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;

import java.io.Serializable;

import orego.core.Board;

/**
 * A pattern that can be many different sizes. Acceptable sizes are 4, 8, 12,
 * 20, 24. The positions in the patterns are encoded as follows:
 *          22
 *       15  9 16
 *    14  4  0  5 17
 * 21  8  3  *  1 10 23
 *    13  7  2  6 18
 *       12 11 19
 *          20
 */
public class DynamicPattern implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int NUMBER_CHOICES = 8;

	private int patternSize;
	
	long[] pattern = new long[NUMBER_CHOICES];

	public int getPatternSize() {
		return patternSize;
	}
	
	public long[] getPattern() {
		return pattern;
	}

	public DynamicPattern(String diagram) {
		assert !diagram.equals("");
		patternSize = diagram.indexOf(':');
		for (int i = 0; i < patternSize; i++) {
			pattern[0] |= (long)("#O.*".indexOf(diagram.charAt(i))) << (i * 2);
		}
		pattern[0] |= (long)("#O".indexOf(diagram.charAt(patternSize + 1))) * (long) Math.pow(2, 62);
		createReflectionsAndRotations(pattern[0]);
	}
	
	public DynamicPattern(int p, Board board) {
		this(p, board, 8);
	} 
	
	public DynamicPattern(int p, Board board, int size) {
		patternSize = size;
		createReflectionsAndRotations(setupPattern(p, board, patternSize));
	}
	
	public DynamicPattern(long newPattern, int size) {
		patternSize = size;
		createReflectionsAndRotations(newPattern);
	}
	
	public boolean match(long incomingPattern, int size) {
		if (this.getPatternSize() == size) {
			for (int i = 0; i < NUMBER_CHOICES; i++) {
				if (incomingPattern == getPattern()[i]) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String patternToString(int choice) {
		String result = "";
		for (int i = 0; i < this.getPatternSize(); i++) {
			result += "#O.*".charAt((int)((this.getPattern()[choice] & ((long)3 << (i * 2))) >> (i* 2)));
		}
		return result;
	}
	
	public static String longToPatternString(long pattern, int patternLength) {
		String result = "";
		for (int i = 0; i < patternLength; i++) {
			result += "#O.*".charAt((int)((pattern & ((long)3 << (i * 2))) >> (i* 2)));
		}
		result += ":"+"#O".charAt(((int)(pattern >> 62) & 1));
		return result;
	}
	
	public int getColorFromPosition(int choice, int position) {
		return (int)((this.getPattern()[choice] & ((long)3 << (position * 2))) >> (position * 2));
	}
	
	public int getColorToPlay() {
		return (int)(this.getPattern()[0] >> 63);
	}
	
	/**
	 * Choice is:
	 * 0 = default
	 * 1 = rotate 90
	 * 2 = rotate 180
	 * 3 = rotate 270
	 * 4 = mirror 
	 * 5 = mirror 90
	 * 6 = mirror 180
	 * 7 = mirror 270
	 * @param currentPattern
	 * @param choice
	 */
	public static long setupPattern(int p, Board board, int size) {
		long currentPattern = ((long)board.getColorToPlay()) << 62;
		int north = -SOUTH;
		int east = EAST;
		int south = SOUTH;
		int west = -EAST;
		for (int i = 0; i < size; i++) {
			switch (i) {
			case 0:
				currentPattern = (long)(board.getColor(p + north)) << 0 | currentPattern;
				break;
			case 1:
				currentPattern = (long) (board.getColor(p + east)) << 2 | currentPattern;
				break;
			case 2:
				currentPattern = (long) (board.getColor(p + south)) << 4 | currentPattern;
				break;
			case 3:
				currentPattern = (long) (board.getColor(p + west)) << 6 | currentPattern;
				break;
			case 4:
				currentPattern = (long) (board.getColor(p + west + north)) << 8 | currentPattern;
				break;
			case 5:
				currentPattern = (long) (board.getColor(p + east + north)) << 10 | currentPattern;
				break;
			case 6:
				currentPattern = (long) (board.getColor(p + south + east)) << 12 | currentPattern;
				break;
			case 7:
				currentPattern = (long) (board.getColor(p + south + west)) << 14 | currentPattern;
				break;
			case 8:
				if ((currentPattern & (3L << 6)) >> 6 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + west + west) << 16) | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 16 | currentPattern;
				}
				break;
			case 9:
				if ((currentPattern & (3L)) != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + north + north)) << 18 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 18 | currentPattern;
				}
				break;
			case 10:
				if ((currentPattern & (3L << 2)) >> 2 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + east + east)) << 20 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 20 | currentPattern;
				}
				break;
			case 11:
				if ((currentPattern & (3L << 4)) >> 4 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + south + south)) << 22 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 22 | currentPattern;
				}
				break;
			case 12:
				if ((currentPattern & (3L << 4)) >> 4 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + south + south + west)) << 24 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 24 | currentPattern;
				}
				break;
			case 13:
				if ((currentPattern & (3L << 6)) >> 6 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + west + west + south)) << 26 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 26 | currentPattern;
				}
				break;
			case 14:
				if ((currentPattern & (3L << 6)) >> 6 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + west + west + north)) << 28 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 28 | currentPattern;
				}
				break;
			case 15:
				if ((currentPattern & (3L)) != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + north + north + west)) << 30 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 30 | currentPattern;
				}
				break;
			case 16:
				if ((currentPattern & (3L)) != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + north + north + east)) << 32| currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 32 | currentPattern;
				}
				break;
			case 17:
				if ((currentPattern & (3L << 2)) >> 2 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + east + east + north)) << 34 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 34 | currentPattern;
				}
				break;
			case 18:
				if ((currentPattern & (3L << 2)) >> 2 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + east + east + south)) << 36 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 36 | currentPattern;
				}
				break;
			case 19:
				if ((currentPattern & (3L << 4)) >> 4 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + south + south + east)) << 38 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 38 | currentPattern;
				}
				break;
			case 20:
				if ((currentPattern & (3L << 22)) >> 22 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + south + south + south)) << 40 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 40 | currentPattern;
				}
				break;
			case 21:
				if ((currentPattern & (3L << 16)) >> 16 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + west + west + west)) << 42 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 42 | currentPattern;
				}
				break;
			case 22:
				if ((currentPattern & (3L << 18)) >> 18 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + north + north + north)) << 44 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 44 | currentPattern;
				}
				break;
			case 23:
				if ((currentPattern & (3L << 20)) >> 20 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + east + east + east)) << 46 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 46 | currentPattern;
				}
				break;
			}
		}
		return currentPattern;
	}

	/**
	 * Create the eight possible patterns (reflections and rotations).
	 * @param Pattern we are looking at.
	 */
	private void createReflectionsAndRotations(long currentPattern) {
		pattern[0] = currentPattern;
		pattern[1] = rotate90(currentPattern);
		pattern[2] = rotate90(pattern[1]);
		pattern[3] = rotate90(pattern[2]);
		pattern[4] = mirror(pattern[0]);
		pattern[5] = rotate90(pattern[4]);
		pattern[6] = rotate90(pattern[5]);
		pattern[7] = rotate90(pattern[6]);
	}
	
	/**
	 * rotates the long representation of a pattern by 90 degrees Counter-Clockwise.
	 * @param pattern the pattern encoding to be rotated
	 * @return rotated pattern encoding
	 */
	protected static long rotate90(long patternWithColor) {
		long pattern = patternWithColor << 1 >> 1;
		long result = 0L;
		// blocks:   1    |    2    |     3     |            4            |      5
		//        0 1 2 3 | 4 5 6 7 | 8 9 10 11 | 12 13 14 15 16 17 18 19 | 20 21 22 23
		// block 5
		result |= rotateBlock90(pattern >> 40, 2);
		// block 4
		result <<= 16;
		result |= rotateBlock90(pattern >> 24, 4);
		// block 3
		result <<= 8;
		result |= rotateBlock90(pattern >> 16, 2);
		// block 2
		result <<= 8;
		result |= rotateBlock90(pattern >> 8, 2);
		// block 1
		result <<= 8;
		result |= rotateBlock90(pattern, 2);
		// get the color to play
		result |= patternWithColor & (long) (Math.pow(2, 62));
		return result;
	}
	
	/**
	 * Rotates a section of four characters in a pattern to complete a 90 degree rotation.
	 * @param charBitSize number of bits in each character.
	 * @param block the block to be rotated
	 * @return rotated block
	 */
	protected static long rotateBlock90(long block, int charBitSize) {
		long result = 0L;
		result |= block & (long) -(1 - Math.pow(2, charBitSize));
		result <<= 3 * charBitSize;
		result |= block >> charBitSize & (long) -(1 - Math.pow(2, charBitSize * 3));
		return result;
	}
	
	/**
	 * Mirror the selected pattern across the y-axis.
	 * @param The pattern selected.
	 * @return
	 */
	protected static long mirror(long block) {
		long result = 0L;
		result |= (block & 3L) | ((block & (3L << 6)) >> 4) | (block & (3L << 4)) | ((block & (3L << 2)) << 4);//0-4
		result |= ((block & (3L << 8)) << 2) | ((block & (3L << 10)) >> 2) | ((block & (3L << 12)) << 2) | ((block & (3L << 14)) >> 2);//4-7
		result |= ((block & (3L << 16)) << 4) | ((block & (3L << 18))) | ((block & (3L << 20)) >> 4) | ((block & (3L << 22)));//8-11
		result |= ((block & (3L << 24)) << 14) | ((block & (3L << 26)) << 10) | ((block & (3L << 28)) << 6) | ((block & (3L << 30)) << 2);//12-15
		result |= ((block & (3L << 32)) >> 2) | ((block & (3L << 34)) >> 6) | ((block & (3L << 36)) >> 10) | ((block & (3L << 38)) >> 14);//16-19
		result |= ((block & (3L << 40))) | ((block & (3L << 42)) << 4) | ((block & (3L << 44))) | ((block & (3L << 46)) >> 4);//20-23
		result |= block & (long) (Math.pow(2, 62));
		return result;
	}
}
