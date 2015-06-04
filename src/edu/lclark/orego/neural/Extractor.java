package edu.lclark.orego.neural;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.StoneColor.*;

/** Extracts board information for processing by neural network. */
public class Extractor {

	private Board board;

	public Extractor(Board board) {
		this.board = board;
	}

	/**
	 * Returns 1 if there is a black stone at specified coordinates, otherwise
	 * 0.
	 */
	public double isBlack(int row, int col) {
		CoordinateSystem coords = board.getCoordinateSystem();
		if (!coords.isValidOneDimensionalCoordinate(row)) {
			return 0;
		}
		if (!coords.isValidOneDimensionalCoordinate(col)) {
			return 0;
		}
		if (board.getColorAt(coords.at(row, col)) == BLACK) {
			return 1;
		}
		return 0;
	}

}
