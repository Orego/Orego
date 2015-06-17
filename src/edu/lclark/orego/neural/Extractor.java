package edu.lclark.orego.neural;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;
import static edu.lclark.orego.core.StoneColor.*;

/** Extracts board information for processing by neural network. */
public class Extractor {

	private Board board;

	private HistoryObserver historyObserver;

	public Extractor(Board board) {
		this.board = board;
		historyObserver = new HistoryObserver(board);
	}

	/**
	 * Returns 1 if there is a black stone at specified coordinates, otherwise
	 * 0.
	 */
	public float isBlack(int row, int col) {
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

	/**
	 * Returns 1 if the specified coordinate is off the board, otherwise 0.
	 */
	public float isOffBoard(int row, int col) {
		CoordinateSystem coords = board.getCoordinateSystem();
		if ((!coords.isValidOneDimensionalCoordinate(col))
				|| (!coords.isValidOneDimensionalCoordinate(row))) {
			return 1;
		}
		return 0;
	}

	public float isPenultimateMove(int row, int col) {
		CoordinateSystem coords = board.getCoordinateSystem();
		if (board.getTurn() < 2) {
			return 0;
		}
		if (coords.isValidOneDimensionalCoordinate(row)
				&& coords.isValidOneDimensionalCoordinate(col)
				&& historyObserver.get((board.getTurn() - 2)) == coords.at(row,
						col)) {
			return 1;
		}
		return 0;
	}

	public float isUltimateMove(int row, int col) {
		CoordinateSystem coords = board.getCoordinateSystem();
		if (board.getTurn() < 1) {
			return 0;
		}
		if (historyObserver.get(board.getTurn() - 1) == coords.at(row, col)) {
			return 1;
		}
		return 0;
	}

	/**
	 * Returns 1 if there is a white stone at specified coordinates, otherwise
	 * 0.
	 */
	public float isWhite(int row, int col) {
		CoordinateSystem coords = board.getCoordinateSystem();
		if (!coords.isValidOneDimensionalCoordinate(row)) {
			return 0;
		}
		if (!coords.isValidOneDimensionalCoordinate(col)) {
			return 0;
		}
		if (board.getColorAt(coords.at(row, col)) == WHITE) {
			return 1;
		}
		return 0;
	}

	/** Returns a neural network input vector based on the state of the game. */
	public float[] toInputVector() {
		final float[] result = new float[board.getCoordinateSystem().getArea() * 4]; // TODO 4 is a magic number
		final CoordinateSystem coords = board.getCoordinateSystem();
		final int width = coords.getWidth();
		final int area = coords.getArea();
		int p = 0; // place in training array
		for (int row = 0; row < width; row++) {
			for (int col = 0; col < width; col++) {
				result[p] = isBlack(row, col);
				result[p + area] = isWhite(row, col);
				result[p + area * 2] = isUltimateMove(row, col);
				result[p + area * 3] = isPenultimateMove(row, col);
				p++;
			}
		}
		return result;
	}

}
