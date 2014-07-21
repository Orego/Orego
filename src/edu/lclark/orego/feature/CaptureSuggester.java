package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.util.ShortSet;

/** Suggests moves that capture enemy stones. */
@SuppressWarnings("serial")
public final class CaptureSuggester implements Suggester {

	private final AtariObserver atari;

	private final int bias;

	private final Board board;

	/**
	 * A list of all of the moves for the current player to play that will
	 * capture stones
	 */
	private final ShortSet movesToCapture;

	public CaptureSuggester(Board board, AtariObserver atari) {
		this(board, atari, 0);
	}

	public CaptureSuggester(Board board, AtariObserver atari, int bias) {
		this.bias = bias;
		this.board = board;
		this.atari = atari;
		movesToCapture = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
	}

	@Override
	public int getBias() {
		return bias;
	}

	@Override
	public ShortSet getMoves() {
		movesToCapture.clear();
		final ShortSet chainsInAtari = atari.getChainsInAtari(board.getColorToPlay()
				.opposite());
		for (int i = 0; i < chainsInAtari.size(); i++) {
			movesToCapture.add(board.getLiberties(chainsInAtari.get(i)).get(0));
		}
		return movesToCapture;
	}

}
