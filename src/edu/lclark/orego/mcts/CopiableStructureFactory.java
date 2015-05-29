package edu.lclark.orego.mcts;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
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
import edu.lclark.orego.feature.Rater;
import edu.lclark.orego.feature.ShapeRater;
import edu.lclark.orego.feature.Predicate;
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
		final ChinesePlayoutScorer scorer = new ChinesePlayoutScorer(board, komi);
		return new CopiableStructure().add(board)
				.add(scorer)
				.add(new StoneCountObserver(board, scorer))
				.add(new HistoryObserver(board))
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
	 * The returned structure uses an EscapeSuggester first, with a
	 * CaptureSuggester as a fallback.
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
	 * The returned structure tries escaping, then pattern matching, then
	 * capturing (like MoGo).
	 */
	public static CopiableStructure escapePatternCapture(int width) {
		final CopiableStructure base = basicParts(width, 7.5);
		final Board board = base.get(Board.class);
		final AtariObserver atariObserver = new AtariObserver(board);
		final HistoryObserver historyObserver = base.get(HistoryObserver.class);
		base.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		return base.add(MoverFactory.escapePatternCapturer(board,
				atariObserver, historyObserver));
	}

	/**
	 * Like simpleRandom, but the returned structure plays moves that are on the
	 * 3rd or 4th line or near another stone.
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
	public static CopiableStructure lgrfWithBias(int width, double komi) {
		final CopiableStructure base = basicParts(width, komi);
		final Board board = base.get(Board.class);
		// Observers
		final AtariObserver atariObserver = new AtariObserver(board);
		final HistoryObserver historyObserver = base.get(HistoryObserver.class);
		// Filter
		Predicate filter = new Conjunction(new NotEyeLike(board),
				new Disjunction(OnThirdOrFourthLine.forWidth(board
						.getCoordinateSystem().getWidth()),
						new NearAnotherStone(board)));
		base.add(filter);
		// LGRF
		final LgrfTable table = new LgrfTable(board.getCoordinateSystem());
		base.add(table);
		// This is added to the structure to that every LgrfSuggester can point
		// to
		final LgrfSuggester lgrf = new LgrfSuggester(board, historyObserver,
				table, filter);
		// This is added to the structure to that every LgrfSuggester can point
		// to
		// the same table. This is handled in the McRunnable constructor.
		base.add(lgrf);
		// Suggesters
		final EscapeSuggester escape = new EscapeSuggester(board,
				atariObserver, 20);
		final PatternSuggester patterns = new PatternSuggester(board,
				historyObserver, 20);
		final CaptureSuggester capture = new CaptureSuggester(board,
				atariObserver, 20);
		// Bias
		base.add(new Suggester[] { escape, patterns, capture });
		// Mover
		final SuggesterMover mover = new SuggesterMover(board, lgrf, new SuggesterMover(board, escape, new SuggesterMover(board,
				patterns, new SuggesterMover(board, capture, new PredicateMover(board,
						filter)))));
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
		final EscapeSuggester escape = new EscapeSuggester(board,
				atariObserver, 20);
		final PatternSuggester patterns = new PatternSuggester(board,
				historyObserver, 20);
		final CaptureSuggester capture = new CaptureSuggester(board,
				atariObserver, 20);
		// Bias
		base.add(new Suggester[] { escape, patterns, capture });
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

	public static CopiableStructure shape(int width, double komi,
			int shapeBias, int minStones,
			float shapeScalingFactor) {
		final CopiableStructure base = basicParts(width, komi);
		final Board board = base.get(Board.class);
		// Observers
		final AtariObserver atariObserver = new AtariObserver(board);
		final HistoryObserver historyObserver = base.get(HistoryObserver.class);
		// Filter
		Predicate filter = new Conjunction(new NotEyeLike(board),
				new Disjunction(OnThirdOrFourthLine.forWidth(board
						.getCoordinateSystem().getWidth()),
						new NearAnotherStone(board)));
		base.add(filter);
		// LGRF
		final LgrfTable table = new LgrfTable(board.getCoordinateSystem());
		base.add(table);
		final LgrfSuggester lgrf = new LgrfSuggester(board, historyObserver,
				table, filter);
		// This is added to the structure to that every LgrfSuggester can point
		// to
		// the same table. This is handled in the McRunnable constructor.
		base.add(lgrf);
		String sfString = Float.toString(shapeScalingFactor);
		sfString = sfString.substring(sfString.indexOf('.') + 1);
		// TODO The shape scaling factor (last parameter below) should not be hard-coded
		final ShapeTable shapeTable = new ShapeTable(OREGO_ROOT
				+ "patterns/patterns" + minStones + "stones-SHAPE-sf"
				+ sfString + ".data", 0.99f);
		// Suggesters
		final EscapeSuggester escape = new EscapeSuggester(board,
				atariObserver, 20);
		final PatternSuggester patterns = new PatternSuggester(board,
				historyObserver, 20);
		final CaptureSuggester capture = new CaptureSuggester(board,
				atariObserver, 20);
		// Shape
		final ShapeRater shape = new ShapeRater(board, historyObserver, shapeTable,
				shapeBias, minStones);
		base.add(shapeTable);
		base.add(shape);
		// Bias;
		base.add(new Suggester[] { escape, patterns, capture });
		// First argument is null because the ShapeTable needs to be
		// added to the ShapeRater on the outside, and this avoids resizing
		// the array; when using this copiable structure, add the ShapeRater
		// to the 0th slot of this array
		base.add(new Rater[] { null });
		// Mover
		final SuggesterMover mover = new SuggesterMover(board, lgrf,
				new SuggesterMover(board, escape, new SuggesterMover(board,
						patterns, new SuggesterMover(board, capture,
								new PredicateMover(board, filter)))));
		return base.add(mover);
	}

}
