package edu.lclark.orego.move;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.AtariObserver;
import edu.lclark.orego.feature.CaptureSuggester;
import edu.lclark.orego.feature.Conjunction;
import edu.lclark.orego.feature.Disjunction;
import edu.lclark.orego.feature.EscapeSuggester;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.NearAnotherStone;
import edu.lclark.orego.feature.NotEyeLike;
import edu.lclark.orego.feature.OnThirdOrFourthLine;
import edu.lclark.orego.feature.PatternSuggester;
import edu.lclark.orego.feature.Predicate;

/** Static methods for creating some particular, widely-used Movers. */
public final class MoverFactory {

	/** Like feasible, but captures when possible. */
	public static Mover capturer(Board board, AtariObserver atariObserver) {
		return new SuggesterMover(board, new CaptureSuggester(board,
				atariObserver), feasible(board));
	}

	/**
	 * Uses an EscapeSuggester first, with a CaptureSuggester as a fallback.
	 */
	public static Mover escapeCapturer(Board board, AtariObserver atariObserver) {
		return new SuggesterMover(board, new EscapeSuggester(board,
				atariObserver), capturer(board, atariObserver));
	}

	/** Tries to escape, then play a pattern, then capture (similar to MoGo). */
	public static Mover escapePatternCapturer(Board board,
			AtariObserver atariObserver, HistoryObserver historyObserver) {
		return new SuggesterMover(board, new EscapeSuggester(board,
				atariObserver), new SuggesterMover(board, new PatternSuggester(
				board, historyObserver), capturer(board, atariObserver)));
	}

	/**
	 * Like simpleRandom, but only plays moves that are on the 3rd or 4th line
	 * or near another stone.
	 */
	public static Mover feasible(Board board) {
		final Predicate f = new Conjunction(new NotEyeLike(board),
				new Disjunction(OnThirdOrFourthLine.forWidth(board
						.getCoordinateSystem().getWidth()),
						new NearAnotherStone(board)));
		return new PredicateMover(board, f);
	}

	/** Plays randomly except for eyelike points. */
	public static Mover simpleRandom(Board board) {
		return new PredicateMover(board, new NotEyeLike(board));
	}

}
