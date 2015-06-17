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

	public void changeBoard(Board b) {
		board = b;
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

	/**
	 * Returns 1 if the specified coordinate is off the board, otherwise 0.
	 */
	public double isOffBoard(int row, int col) {
		CoordinateSystem coords = board.getCoordinateSystem();
		if ((!coords.isValidOneDimensionalCoordinate(col))
				|| (!coords.isValidOneDimensionalCoordinate(row))) {
			return 1;
		}
		return 0;
	}

	public double isPenultimateMove(int row, int col) {
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

	public double isUltimateMove(int row, int col) {
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
	public double isWhite(int row, int col) {
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

}
