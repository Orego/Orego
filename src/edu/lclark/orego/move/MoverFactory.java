package edu.lclark.orego.move;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.*;

/** Static methods for creating some particular, widely-used movers. */
public class MoverFactory {

	/** Plays randomly except for eyelike points. */
	public static PredicateMover simpleRandom(Board board) {
		return new PredicateMover(board, new NotEyeLike(board));
	}

	/**
	 * Like simpleRandom, but only plays moves that are on the 3rd or 4th line
	 * or near another stone.
	 */
	public static PredicateMover feasible(Board board) {
		Predicate f = new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board)));
		return new PredicateMover(board, f);
	}

	/** Like feasible, but captures when possible. */
	public static SuggesterMover capturer(Board board) {
		return capturer(board, new AtariObserver(board));
	}

	/**
	 * Like feasible, but captures when possible. Uses an AtariObserver passed
	 * in rather than making its own.
	 */
	private static SuggesterMover capturer(Board board, AtariObserver atariObserver) {
		Suggester s = new CaptureSuggester(board, atariObserver);
		return new SuggesterMover(board, s, feasible(board));
	}

	/**
	 * Uses the EscapeSuggester first, and the greedy (CaptureSuggester) as a
	 * fallback.
	 */
	public static SuggesterMover escapeCapturer(Board board) {
		AtariObserver atariObserver = new AtariObserver(board);
		Suggester s = new EscapeSuggester(board, atariObserver);
		return new SuggesterMover(board, s, capturer(board, atariObserver));
	}

}
