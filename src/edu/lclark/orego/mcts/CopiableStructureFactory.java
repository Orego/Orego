package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.AtariObserver;
import edu.lclark.orego.feature.CaptureSuggester;
import edu.lclark.orego.feature.Conjunction;
import edu.lclark.orego.feature.Disjunction;
import edu.lclark.orego.feature.EscapeSuggester;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.LgrfSuggester;
import edu.lclark.orego.feature.LgrfTable;
import edu.lclark.orego.feature.NearAnotherStone;
import edu.lclark.orego.feature.NotEyeLike;
import edu.lclark.orego.feature.OnThirdOrFourthLine;
import edu.lclark.orego.feature.PatternSuggester;
import edu.lclark.orego.feature.ShapeSuggester;
import edu.lclark.orego.feature.StoneCountObserver;
import edu.lclark.orego.feature.Suggester;
import edu.lclark.orego.move.MoverFactory;
import edu.lclark.orego.move.PredicateMover;
import edu.lclark.orego.move.SuggesterMover;
import edu.lclark.orego.patterns.ShapeTable;
import edu.lclark.orego.score.ChineseFinalScorer;
import edu.lclark.orego.score.ChinesePlayoutScorer;

/** Static methods for creating some particular, widely-used CopiableStructures. */
public final class CopiableStructureFactory {

	/** Returns a structure with a board, scorers, and a stone counter. */
	public static CopiableStructure basicParts(int width, double komi) {
		final Board board = new Board(width);
		return new CopiableStructure().add(board)
				.add(new ChinesePlayoutScorer(board, komi))
				.add(new StoneCountObserver(board)).add(new HistoryObserver(board))
				.add(new ChineseFinalScorer(board, komi));
	}

	/** Like feasible, but the returned structure captures when possible. */
	public static CopiableStructure capturer(int width) {
		final CopiableStructure base = basicParts(width, 7.5);
		final Board board = base.get(Board.class);
		final AtariObserver atariObserver = new AtariObserver(board);
		return base.add(MoverFactory.capturer(board, atariObserver));
	}

	/**
	 * The returned structure uses an EscapeSuggester first, with a CaptureSuggester as a fallback.
	 */
	public static CopiableStructure escapeCapturer(int width) {
		final CopiableStructure base = basicParts(width, 7.5);
		final Board board = base.get(Board.class);
		final AtariObserver atariObserver = new AtariObserver(board);
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(MoverFactory.escapeCapturer(board, atariObserver));
	}

	/**
	 * The returned structure tries escaping, then pattern matching, then capturing (like MoGo).
	 */
	public static CopiableStructure escapePatternCapture(int width) {
		final CopiableStructure base = basicParts(width, 7.5);
		final Board board = base.get(Board.class);
		final AtariObserver atariObserver = new AtariObserver(board);
		final HistoryObserver historyObserver = base.get(HistoryObserver.class);
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(MoverFactory.escapePatternCapturer(board, atariObserver,
				historyObserver));
	}

	/**
	 * Like simpleRandom, but the returned structure plays moves that are on the 3rd or 4th line
	 * or near another stone.
	 */
	public static CopiableStructure feasible(int width) {
		final CopiableStructure base = basicParts(width, 7.5);
		final Board board = base.get(Board.class);
		base.add(new Suggester[0]);
		base.add(new int[0]);
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(MoverFactory.feasible(board));
	}

	/** Similar to useWithBias, but incorporates LGRF2. */
	public static CopiableStructure lgrfWithBias(int width, double komi){
		final CopiableStructure base = basicParts(width, komi);
		final Board board = base.get(Board.class);		
		// Observers
		final AtariObserver atariObserver = new AtariObserver(board);
		final HistoryObserver historyObserver = base.get(HistoryObserver.class);
		// LGRF
		final LgrfTable table = new LgrfTable(board.getCoordinateSystem());
		base.add(table);
		final LgrfSuggester lgrf = new LgrfSuggester(board, historyObserver, table);
		// This is added to the structure to that every LgrfSuggester can point to
		// the same table. This is handled in the McRunnable constructor.
		base.add(lgrf);
		// Suggesters
		final EscapeSuggester escape = new EscapeSuggester(board, atariObserver);
		final PatternSuggester patterns = new PatternSuggester(board, historyObserver);
		final CaptureSuggester capture = new CaptureSuggester(board, atariObserver);
		// Bias
		base.add(new Suggester[] { escape, patterns, capture });
		base.add(new int[] { 20, 20, 20 });
		// Mover
		final SuggesterMover mover = new SuggesterMover(board, lgrf, new SuggesterMover(board, escape, new SuggesterMover(board,
				patterns, new SuggesterMover(board, capture, new PredicateMover(board,
						new Conjunction(new NotEyeLike(board), new Disjunction(
								OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
										.getWidth()), new NearAnotherStone(board))))))));
		// Filter
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(mover);
	}

	/** Returns a structure that plays randomly except for eyelike points. */
	public static CopiableStructure simpleRandom(int width) {
		final CopiableStructure base = basicParts(width, 7.5);
		final Board board = base.get(Board.class);
		base.add(new NotEyeLike(board));
		return base.add(MoverFactory.simpleRandom(board));
	}

	/**
	 * Similar to escapePatternCapture, but also updates bias.
	 */
	public static CopiableStructure useWithBias(int width, double komi) {
		final CopiableStructure base = basicParts(width, komi);
		final Board board = base.get(Board.class);
		// Observers
		final AtariObserver atariObserver = new AtariObserver(board);
		final HistoryObserver historyObserver = base.get(HistoryObserver.class);
		// Suggesters
		final EscapeSuggester escape = new EscapeSuggester(board, atariObserver);
		final PatternSuggester patterns = new PatternSuggester(board, historyObserver);
		final CaptureSuggester capture = new CaptureSuggester(board, atariObserver);
		// Bias
		base.add(new Suggester[] { escape, patterns, capture });
		base.add(new int[] { 20, 20, 20 });
		// Mover
		final SuggesterMover mover = new SuggesterMover(board, escape,
				new SuggesterMover(board, patterns, new SuggesterMover(board,
						capture, new PredicateMover(board, new Conjunction(
								new NotEyeLike(board), new Disjunction(
										OnThirdOrFourthLine.forWidth(board
												.getCoordinateSystem()
												.getWidth()),
										new NearAnotherStone(board)))))));
		// Filter
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(mover);
	}
	
	public static CopiableStructure shape5(int width, double komi){
		final CopiableStructure base = basicParts(width, komi);
		final Board board = base.get(Board.class);		
		// Observers
		final AtariObserver atariObserver = new AtariObserver(board);
		final HistoryObserver historyObserver = base.get(HistoryObserver.class);
		// LGRF
		final LgrfTable table = new LgrfTable(board.getCoordinateSystem());
		base.add(table);
		final LgrfSuggester lgrf = new LgrfSuggester(board, historyObserver, table);
		// This is added to the structure to that every LgrfSuggester can point to
		// the same table. This is handled in the McRunnable constructor.
		base.add(lgrf);
		// Suggesters
		final EscapeSuggester escape = new EscapeSuggester(board, atariObserver);
		final PatternSuggester patterns = new PatternSuggester(board, historyObserver);
		final CaptureSuggester capture = new CaptureSuggester(board, atariObserver);
		// Shape
		final ShapeTable shapeTable = new ShapeTable("patterns/patterns5x5.data");
		final ShapeSuggester shape = new ShapeSuggester(board, shapeTable);
		// Bias
		base.add(new Suggester[] {shape, escape, patterns, capture });
		base.add(new int[] {20, 20, 20, 20 });
		// Mover
		final SuggesterMover mover = new SuggesterMover(board, lgrf, new SuggesterMover(board, escape, new SuggesterMover(board,
				patterns, new SuggesterMover(board, capture, new PredicateMover(board,
						new Conjunction(new NotEyeLike(board), new Disjunction(
								OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
										.getWidth()), new NearAnotherStone(board))))))));
		// Filter
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(mover);
	}

}
