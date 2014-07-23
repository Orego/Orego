package edu.lclark.orego.feature;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.BitVector;
import edu.lclark.orego.util.ShortSet;

/**
 * Suggests moves matching "good" patterns that are orthogonal or diagonal to the last move played.
 *
 * @see edu.lclark.orego.patterns.PatternExtractor
 */
@SuppressWarnings("serial")
public final class PatternSuggester implements Suggester {

	/** Patterns are considered good if its "win rate" is at least this high. */
	private static final float THRESHOLD = 0.8f;
	
	private final int bias;

	private final Board board;

	private final CoordinateSystem coords;

	private BitVector goodPatterns;

	private final HistoryObserver history;

	private final ShortSet moves;
	
	public PatternSuggester(Board board, HistoryObserver history) {
		this(board, history, 0);
	}

	public PatternSuggester(Board board, HistoryObserver history, int bias) {
		this.bias = bias;
		this.board = board;
		coords = board.getCoordinateSystem();
		this.history = history;
		moves = new ShortSet(coords.getFirstPointBeyondBoard());
		try (ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream(OREGO_ROOT
						+ "patterns/patterns3x3.data"));) {
			final int[] fileRuns = (int[]) objectInputStream.readObject();
			final int[] fileWins = (int[]) objectInputStream.readObject();
			goodPatterns = new BitVector(fileRuns.length);
			for (int i = 0; i < fileRuns.length; i++) {
				goodPatterns.set(i,
						(float) fileWins[i] / (float) fileRuns[i] > THRESHOLD);
			}
			objectInputStream.close();
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** Returns a 16-bit char indicating the colors of the 8 points around p. */
	private char calculatePattern(short p) {
		char pattern = 0;
		final short[] neighbors = coords.getNeighbors(p);
		for (int i = 0; i < neighbors.length; i++) {
			final Color color = board.getColorAt(neighbors[i]);
			if (color == board.getColorToPlay()) {
				// Friendly stone at this neighbor
				pattern |= 1 << i * 2;
			} else if (color != board.getColorToPlay().opposite()) {
				// neighbor is vacant or off board
				pattern |= color.index() << i * 2;
			} // else do nothing, no need to OR 0 with 0
		}
		return pattern;
	}

	@Override
	public int getBias() {
		return bias;
	}

	@Override
	public ShortSet getMoves() {
		moves.clear();
		final int turn = board.getTurn();
		if (turn == 0) {
			return moves;
		}
		final short p = history.get(turn - 1);
		if (p == CoordinateSystem.PASS) {
			return moves;
		}
		final short[] neighbors = coords.getNeighbors(p);
		for (final short n : neighbors) {
			if (board.getColorAt(n) == VACANT) {
				final char pattern = calculatePattern(n);
				if (goodPatterns.get(pattern)) {
					moves.add(n);
				}
			}
		}
		return moves;
	}
}
