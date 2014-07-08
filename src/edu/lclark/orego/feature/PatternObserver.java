package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import static edu.lclark.orego.patterns.PatternFinder.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.patterns.PatternFinder;
import edu.lclark.orego.util.ShortList;

@SuppressWarnings("serial")
public class PatternObserver implements BoardObserver {

	public static final int[] PATTERN_SIZES = { 0, 4, 8, 12, 20, 24 };

	private long[][] patterns;

	private Board board;

	private CoordinateSystem coords;

	public PatternObserver(Board board) {
		this.board = board;
		board.addObserver(this);
		coords = board.getCoordinateSystem();
		patterns = new long[coords.getFirstPointBeyondBoard()][5];
	}

	@Override
	public void update(StoneColor color, short location, ShortList capturedStones) {
		updatePatterns(location);
		for (int i = 0; i < capturedStones.size(); i++) {
			updatePatterns(capturedStones.get(i));
		}
	}

	private void updatePatterns(short p) {
		int row = coords.row(p);
		int column = coords.column(p);
		for (int index = 0; index < PATTERN_SIZES.length - 1; index++) {
			for (int i = PATTERN_SIZES[index]; i < PATTERN_SIZES[index + 1]; i++) {
				int newRow = row + OFFSETS[i][0];
				int newColumn = column + OFFSETS[i][1];
				if ((newRow < 0 || newRow >= coords.getWidth())
						|| (newColumn < 0 || newColumn >= coords.getWidth())) {
					continue;
				} else if (board.getColorAt(coords.at(newRow, newColumn)) == VACANT) {
					updatePatterns(coords.at(newRow, newColumn), index + 1);
				}
			}
		}
	}

	private void updatePatterns(short p, int sizeIndex) {
		PatternFinder.getHash(board, p, sizeIndex);
		//TODO Do some storing
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void copyDataFrom(BoardObserver that) {
		// TODO Auto-generated method stub

	}

}
