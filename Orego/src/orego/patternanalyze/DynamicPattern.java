package orego.patternanalyze;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.core.Board;

public class DynamicPattern {

	private int patternSize;
	byte[][] pattern = new byte[8][patternSize];

	public int getPatternSize() {
		return patternSize;
	}
	
	public byte[][] getPattern() {
		return pattern;
	}

	public DynamicPattern(int p, Board board) {
		this(p, board, 8);
	} 
	
	public DynamicPattern(int p, Board board, int size) {
		patternSize = size;
		for (int i = 0; i < 8; i++) {
			pattern[i] = new byte[patternSize];
			setupPattern(pattern[i], i, p, board);	
		}
	}
	
	public boolean match(DynamicPattern pattern) {
		if (this.getPatternSize() == pattern.getPatternSize()) {
			for (int i = 0; i < 8; i++) {
				boolean allValuesSame = true;
				for (int j = 0; j < pattern.getPatternSize(); j++) {
					if (this.pattern[0][j] != pattern.getPattern()[i][j]) {
						allValuesSame = false;
						break;
					}
				}
				if (allValuesSame) {
					return true;
				}
			}
		}
		return false;
	}
	
	public String patternToString(int choice) {
		String result = "";
		for (int i = 0; i < this.getPatternSize(); i++) {
			result += "#O.*".charAt(this.getPattern()[choice][i]);
		}
		return result;
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
	public void setupPattern(byte[] currentPattern, int choice, int p, Board board) {
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
				currentPattern[i] = (byte) (board.getColor(p + north));
				break;
			case 1:
				currentPattern[i] = (byte) (board.getColor(p + west));
				break;
			case 2:
				currentPattern[i] = (byte) (board.getColor(p + east));
				break;
			case 3:
				currentPattern[i] = (byte) (board.getColor(p + south));
				break;
			case 4:
				currentPattern[i] = (byte) (board.getColor(p + north + west));
				break;
			case 5:
				currentPattern[i] = (byte) (board.getColor(p + north + east));
				break;
			case 6:
				currentPattern[i] = (byte) (board.getColor(p + south + west));
				break;
			case 7:
				currentPattern[i] = (byte) (board.getColor(p + south + east));
				break;
			case 8:
				if (currentPattern[0] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + north + north));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 9:
				if (currentPattern[1] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + west + west));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 10:
				if (currentPattern[2] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + east + east));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 11:
				if (currentPattern[3] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + south + south));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 12:
				if (currentPattern[0] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + north + north + west));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 13:
				if (currentPattern[0] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + north + north + east));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 14:
				if (currentPattern[1] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + west + west + north));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 15:
				if (currentPattern[2] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + east + east + north));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 16:
				if (currentPattern[1] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + west + west + south));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 17:
				if (currentPattern[2] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + east + east + south));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 18:
				if (currentPattern[3] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + south + south + west));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 19:
				if (currentPattern[3] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + south + south + east));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 20:
				if (currentPattern[0] != OFF_BOARD_COLOR && currentPattern[8] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + north + north + north));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 21:
				if (currentPattern[1] != OFF_BOARD_COLOR && currentPattern[9] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + west + west + west));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 22:
				if (currentPattern[2] != OFF_BOARD_COLOR && currentPattern[10] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + east + east + east));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			case 23:
				if (currentPattern[3] != OFF_BOARD_COLOR && currentPattern[11] != OFF_BOARD_COLOR) {
					currentPattern[i] = (byte) (board.getColor(p + south + south + south));
				} else {
					currentPattern[i] = OFF_BOARD_COLOR;
				}
				break;
			}
		}
	}

}
