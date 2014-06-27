package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.util.ShortSet;

/** Suggests moves that capture enemy stones. */
@SuppressWarnings("serial")
public final class CaptureSuggester implements Suggester {

	private final Board board;
	
	private final AtariObserver atari;

	public CaptureSuggester(Board board, AtariObserver atari) {
		this.board = board;
		this.atari = atari;
		movesToCapture = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
	}
	
	/**
	 * A list of all of the moves for the current player to play that will
	 * capture stones
	 */
	private final ShortSet movesToCapture;

	@Override
	public ShortSet getMoves() {
		movesToCapture.clear();
		ShortSet chainsInAtari = atari.getChainsInAtari(board.getColorToPlay().opposite());
		for(int i = 0; i < chainsInAtari.size(); i++){
			movesToCapture.add(board.getLiberties(chainsInAtari.get(i)).get(0));
		}
		return movesToCapture;
	}

}
