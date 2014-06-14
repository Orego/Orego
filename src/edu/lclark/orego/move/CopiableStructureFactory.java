package edu.lclark.orego.move;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.*;
import edu.lclark.orego.mcts.CopiableStructure;
import edu.lclark.orego.score.ChinesePlayoutScorer;

/** Static methods for creating some particular, widely-used CopiableStructures. */
public class CopiableStructureFactory {

	/** Plays randomly except for eyelike points. */
	public static CopiableStructure simpleRandom(int width) {
		Board board = new Board(width);
		return new CopiableStructure(board, MoverFactory.simpleRandom(board),
				new ChinesePlayoutScorer(board, 7.5), new StoneCounter(board));
	}

	/**
	 * Like simpleRandom, but only plays moves that are on the 3rd or 4th line
	 * or near another stone.
	 */
	public static CopiableStructure feasible(int width) {
		Board board = new Board(width);
		return new CopiableStructure(board, MoverFactory.feasible(board),
				new ChinesePlayoutScorer(board, 7.5), new StoneCounter(board));
	}

	/** Like feasible, but captures when possible. */
	public static CopiableStructure capturer(int width) {
		Board board = new Board(width);
		AtariObserver atariObserver = new AtariObserver(board);
		return new CopiableStructure(board, MoverFactory.capturer(board,
				atariObserver), new ChinesePlayoutScorer(board, 7.5),
				new StoneCounter(board));
	}

	/**
	 * Uses an EscapeSuggester first, with a CaptureSuggester as a fallback.
	 */
	public static CopiableStructure escapeCapturer(int width) {
		Board board = new Board(width);
		AtariObserver atariObserver = new AtariObserver(board);
		return new CopiableStructure(board, MoverFactory.escapeCapturer(board,
				atariObserver), new ChinesePlayoutScorer(board, 7.5),
				new StoneCounter(board));
	}

}
