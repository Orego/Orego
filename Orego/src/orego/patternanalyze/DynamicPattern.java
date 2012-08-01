package orego.patternanalyze;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
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
public class DynamicPattern {
	
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
	}
	
	public DynamicPattern(int p, Board board) {
		this(p, board, 8);
	} 
	
	public DynamicPattern(int p, Board board, int size) {
		patternSize = size;
		for (int i = 0; i < NUMBER_CHOICES; i++) {
			pattern[i] = setupPattern(i, p, board);
		}
	}
	
	public boolean match(DynamicPattern pattern) {
		if (this.getPatternSize() == pattern.getPatternSize()) {
			for (int i = 0; i < NUMBER_CHOICES; i++) {
				if (this.pattern[0] != pattern.getPattern()[i]) {
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
	public long setupPattern(int choice, int p, Board board) {
		long currentPattern = ((long)board.getColorToPlay()) << 63;
		int north = 0;
		int south = 0;
		int east = 0;
		int west = 0;
		if (choice == 0) {
			north = -SOUTH;
			east = EAST;
			south = SOUTH;
			west = -EAST;
		}
		if (choice == 1) {
			north = -EAST;
			east = -SOUTH;
			south = EAST;
			west = SOUTH;
		}
		if (choice == 2) {
			north = SOUTH;
			east = -EAST;
			south = -SOUTH;
			west = EAST;
		}
		if (choice == 3) {
			north = EAST;
			east = SOUTH;
			south = -EAST;
			west = -SOUTH;
		}
		if (choice == 4) {
			north = -SOUTH;
			east = -EAST;
			south = SOUTH;
			west = EAST;
		}
		if (choice == 5) {
			north = EAST;
			east = -SOUTH;
			south = -EAST;
			west = SOUTH;
		}
		if (choice == 6) {
			north = SOUTH;
			east = EAST;
			south = -SOUTH;
			west = -EAST;
		}
		if (choice == 7) {
			north = -EAST;
			east = SOUTH;
			south = EAST;
			west = -SOUTH;
		}
		for (int i = 0; i < patternSize; i++) {
			switch (i) {
			case 0:
				currentPattern = (long)(board.getColor(p + north)) << 0 | currentPattern;
				break;
			case 1:
				currentPattern = (long) (board.getColor(p + west)) << 2 | currentPattern;
				break;
			case 2:
				currentPattern = (long) (board.getColor(p + east)) << 4 | currentPattern;
				break;
			case 3:
				currentPattern = (long) (board.getColor(p + south)) << 6 | currentPattern;
				break;
			case 4:
				currentPattern = (long) (board.getColor(p + north + west)) << 8 | currentPattern;
				break;
			case 5:
				currentPattern = (long) (board.getColor(p + north + east)) << 10 | currentPattern;
				break;
			case 6:
				currentPattern = (long) (board.getColor(p + south + west)) << 12 | currentPattern;
				break;
			case 7:
				currentPattern = (long) (board.getColor(p + south + east)) << 14 | currentPattern;
				break;
			case 8:
				if ((currentPattern & 3) != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + north + north) << 16) | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 16 | currentPattern;
				}
				break;
			case 9:
				if ((currentPattern & 12) >> 2 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + west + west)) << 18 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 18 | currentPattern;
				}
				break;
			case 10:
				if ((currentPattern & 48) >> 4 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + east + east)) << 20 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 20 | currentPattern;
				}
				break;
			case 11:
				if ((currentPattern & 192) >> 6 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + south + south)) << 22 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 22 | currentPattern;
				}
				break;
			case 12:
				if ((currentPattern & 3) != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + north + north + west)) << 24 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 24 | currentPattern;
				}
				break;
			case 13:
				if ((currentPattern & 3) != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + north + north + east)) << 26 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 26 | currentPattern;
				}
				break;
			case 14:
				if ((currentPattern & 12) >> 2 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + west + west + north)) << 28 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 28 | currentPattern;
				}
				break;
			case 15:
				if ((currentPattern & 48) >> 4 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + east + east + north)) << 30 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 30 | currentPattern;
				}
				break;
			case 16:
				if ((currentPattern & 12) >> 2 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + west + west + south)) << 32| currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 32 | currentPattern;
				}
				break;
			case 17:
				if ((currentPattern & 48) >> 4 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + east + east + south)) << 34 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 34 | currentPattern;
				}
				break;
			case 18:
				if ((currentPattern & 192) >> 6 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + south + south + west)) << 36 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 36 | currentPattern;
				}
				break;
			case 19:
				if ((currentPattern & 192) >> 6 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + south + south + east)) << 38 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 38 | currentPattern;
				}
				break;
			case 20:
				if ((currentPattern & 3) != OFF_BOARD_COLOR && (currentPattern & 196608) >> 16 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + north + north + north)) << 40 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 40 | currentPattern;
				}
				break;
			case 21:
				if ((currentPattern & 12) >> 2 != OFF_BOARD_COLOR && (currentPattern & 786432) >> 18 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + west + west + west)) << 42 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 42 | currentPattern;
				}
				break;
			case 22:
				if ((currentPattern & 48) >> 4 != OFF_BOARD_COLOR && (currentPattern & 3145728) >> 20 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + east + east + east)) << 44 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 44 | currentPattern;
				}
				break;
			case 23:
				if ((currentPattern & 192) >> 6 != OFF_BOARD_COLOR && (currentPattern & 12582912) >> 22 != OFF_BOARD_COLOR) {
					currentPattern = (long) (board.getColor(p + south + south + south)) << 46 | currentPattern;
				} else {
					currentPattern = (long)OFF_BOARD_COLOR << 46 | currentPattern;
				}
				break;
			}
		}
		return currentPattern;
	}
	
	/**
	 * rotates the long representation of a pattern by 90 degrees
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
}
