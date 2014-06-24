package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.*;
import edu.lclark.orego.move.MoverFactory;
import edu.lclark.orego.score.*;

/** Static methods for creating some particular, widely-used CopiableStructures. */
public final class CopiableStructureFactory {

	public static CopiableStructure basicParts(int width) {
		Board board = new Board(width);
		return new CopiableStructure()
		.add(board)
		.add(new ChinesePlayoutScorer(board, 7.5))
		.add(new StoneCounter(board))
		.add(new HistoryObserver(board))
		.add(new ChineseFinalScorer(board, 7.5));
	}

	/** Plays randomly except for eyelike points. */
	public static CopiableStructure simpleRandom(int width) {
		CopiableStructure base = basicParts(width);
		Board board = base.get(Board.class);
		base.add(new NotEyeLike(board));
		return base.add(MoverFactory.simpleRandom(board));
	}

	/**
	 * Like simpleRandom, but only plays moves that are on the 3rd or 4th line
	 * or near another stone.
	 */
	public static CopiableStructure feasible(int width) {
		CopiableStructure base = basicParts(width);
		Board board = base.get(Board.class);
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(MoverFactory.feasible(board));
	}

	/** Like feasible, but captures when possible. */
	public static CopiableStructure capturer(int width) {
		CopiableStructure base = basicParts(width);
		Board board = base.get(Board.class);
		AtariObserver atariObserver = new AtariObserver(board);
		return base.add(MoverFactory.capturer(board,
				atariObserver));
	}

	/**
	 * Uses an EscapeSuggester first, with a CaptureSuggester as a fallback.
	 */
	public static CopiableStructure escapeCapturer(int width) {
		CopiableStructure base = basicParts(width);
		Board board = base.get(Board.class);
		AtariObserver atariObserver = new AtariObserver(board);
		return base.add(MoverFactory.escapeCapturer(board,
				atariObserver));
	}
	
	public static CopiableStructure escapePatternCapture(int width) {
		CopiableStructure base = basicParts(width);
		Board board = base.get(Board.class);
		AtariObserver atariObserver = new AtariObserver(board);
		HistoryObserver historyObserver = base.get(HistoryObserver.class);
		return base.add(MoverFactory.escapePatternCapture(board, atariObserver, historyObserver));
	}

}
