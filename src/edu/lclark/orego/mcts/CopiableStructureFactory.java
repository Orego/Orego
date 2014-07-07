package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.*;
import edu.lclark.orego.move.MoverFactory;
import edu.lclark.orego.move.*;
import edu.lclark.orego.score.*;

/** Static methods for creating some particular, widely-used CopiableStructures. */
public final class CopiableStructureFactory {

	public static CopiableStructure basicParts(int width, double komi) {
		Board board = new Board(width);
		return new CopiableStructure().add(board)
				.add(new ChinesePlayoutScorer(board, komi))
				.add(new StoneCounter(board)).add(new HistoryObserver(board))
				.add(new ChineseFinalScorer(board, komi));
	}

	/** Plays randomly except for eyelike points. */
	public static CopiableStructure simpleRandom(int width) {
		CopiableStructure base = basicParts(width, 7.5);
		Board board = base.get(Board.class);
		base.add(new NotEyeLike(board));
		return base.add(MoverFactory.simpleRandom(board));
	}

	/**
	 * Like simpleRandom, but only plays moves that are on the 3rd or 4th line
	 * or near another stone.
	 */
	public static CopiableStructure feasible(int width) {
		CopiableStructure base = basicParts(width, 7.5);
		Board board = base.get(Board.class);
		base.add(new Suggester[0]);
		base.add(new int[0]);
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(MoverFactory.feasible(board));
	}

	/** Like feasible, but captures when possible. */
	public static CopiableStructure capturer(int width) {
		CopiableStructure base = basicParts(width, 7.5);
		Board board = base.get(Board.class);
		AtariObserver atariObserver = new AtariObserver(board);
		return base.add(MoverFactory.capturer(board, atariObserver));
	}

	/**
	 * Uses an EscapeSuggester first, with a CaptureSuggester as a fallback.
	 */
	public static CopiableStructure escapeCapturer(int width) {
		CopiableStructure base = basicParts(width, 7.5);
		Board board = base.get(Board.class);
		AtariObserver atariObserver = new AtariObserver(board);
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(MoverFactory.escapeCapturer(board, atariObserver));
	}

	public static CopiableStructure escapePatternCapture(int width) {
		CopiableStructure base = basicParts(width, 7.5);
		Board board = base.get(Board.class);
		AtariObserver atariObserver = new AtariObserver(board);
		HistoryObserver historyObserver = base.get(HistoryObserver.class);
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(MoverFactory.escapePatternCapture(board, atariObserver,
				historyObserver));
	}

	public static CopiableStructure useWithPriors(int width, double komi) {
		CopiableStructure base = basicParts(width, komi);
		Board board = base.get(Board.class);

		AtariObserver atariObserver = new AtariObserver(board);
		HistoryObserver historyObserver = base.get(HistoryObserver.class);

		EscapeSuggester escape = new EscapeSuggester(board, atariObserver);
		PatternSuggester patterns = new PatternSuggester(board, historyObserver);
		CaptureSuggester capture = new CaptureSuggester(board, atariObserver);
		
		base.add(new Suggester[] { escape, patterns, capture });
		base.add(new int[] { 20, 20, 20 });

		SuggesterMover mover = new SuggesterMover(board, escape, new SuggesterMover(board,
				patterns, new SuggesterMover(board, capture, new PredicateMover(board,
						new Conjunction(new NotEyeLike(board), new Disjunction(
								OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
										.getWidth()), new NearAnotherStone(board)))))));

		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(mover);
	}
	
	public static CopiableStructure lgrfWithPriors(int width, double komi){
		CopiableStructure base = basicParts(width, komi);
		Board board = base.get(Board.class);
		
		LgrfTable table = new LgrfTable(board.getCoordinateSystem());
		base.add(table);

		AtariObserver atariObserver = new AtariObserver(board);
		HistoryObserver historyObserver = base.get(HistoryObserver.class);

		LgrfSuggester lgrf = new LgrfSuggester(board, historyObserver, table);
		// We'll need to modify this after a copy so that all LgrfSuggesters share the same table
		base.add(lgrf);
		EscapeSuggester escape = new EscapeSuggester(board, atariObserver);
		PatternSuggester patterns = new PatternSuggester(board, historyObserver);
		CaptureSuggester capture = new CaptureSuggester(board, atariObserver);
		
		base.add(new Suggester[] { escape, patterns, capture });
		base.add(new int[] { 20, 20, 20 });

		SuggesterMover mover = new SuggesterMover(board, lgrf, new SuggesterMover(board, escape, new SuggesterMover(board,
				patterns, new SuggesterMover(board, capture, new PredicateMover(board,
						new Conjunction(new NotEyeLike(board), new Disjunction(
								OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
										.getWidth()), new NearAnotherStone(board))))))));

		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(mover);
	}
}
