package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.RESIGN;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.AtariObserver;
import edu.lclark.orego.feature.BoardObserver;
import edu.lclark.orego.feature.Conjunction;
import edu.lclark.orego.feature.Disjunction;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.NearAnotherStone;
import edu.lclark.orego.feature.NotEyeLike;
import edu.lclark.orego.feature.OnThirdOrFourthLine;
import edu.lclark.orego.feature.Predicate;
import edu.lclark.orego.feature.StoneCountObserver;
import edu.lclark.orego.mcts.CopiableStructure;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.move.MoverFactory;
import edu.lclark.orego.score.ChinesePlayoutScorer;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

import java.util.*;

@SuppressWarnings("serial")
public class Phenotype implements Mover {

	/** Has only the 9 lowest-order bits on. */
	public static final int MASK9 = (1 << 9) - 1;

	/** Indicates that one of the three points in a reply should be ignored. */
	public static final short IGNORE = RESIGN;
	
	private final Board board;

	private final CoordinateSystem coords;

	private final Predicate filter;

	private final HistoryObserver history;

	private final short[][] replies;

	private final ChinesePlayoutScorer scorer;
	
	private final StoneCountObserver mercyObserver;
	
	public static CopiableStructure makeRichBoard(Board board, double komi) {
		CopiableStructure result = new CopiableStructure();
		result = new CopiableStructure();
		result.add(board);
		HistoryObserver history = new HistoryObserver(board);
		result.add(history);
		result.add(new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board))));
		result.add(MoverFactory.escapePatternCapturer(board,
				new AtariObserver(board), history));
		ChinesePlayoutScorer scorer = new ChinesePlayoutScorer(board, komi);
		result.add(scorer);
		result.add(new StoneCountObserver(board, scorer));
		return result;
	}

	public Phenotype(CopiableStructure richBoard) {
		this.board = richBoard.get(Board.class);
		coords = board.getCoordinateSystem();
		replies = new short[coords.getFirstPointBeyondBoard()][coords
				.getFirstPointBeyondBoard()];
		history = richBoard.get(HistoryObserver.class);
		filter = richBoard.get(Conjunction.class);
		fallbackMover = richBoard.get(Mover.class);	
		scorer = richBoard.get(ChinesePlayoutScorer.class);
		mercyObserver = richBoard.get(StoneCountObserver.class);
	}

	public Phenotype(CopiableStructure richBoard, Genotype genotype) {
		this(richBoard);
		int[] words = genotype.getGenes();
		// Extract replies
		for (int i = 0; i < words.length; i++) {
			setReply((short) (words[i] & MASK9),
					(short) ((words[i] >>> 9) & MASK9),
					(short) ((words[i] >>> 18) & MASK9));
		}
	}

	public short bestMove() {
		return bestMove(history.get(board.getTurn() - 2), history.get(board.getTurn() - 1));
	}
		
	public short getRawReply(short penultimate, short ultimate) {
		return replies[penultimate][ultimate];
	}

	public short bestMoveVerbose(short penultimate, short ultimate) {
		short reply = replyToTwoMoves(penultimate, ultimate);
		System.out.println("First reply:" + reply);
		if (isValidMove(reply)) {
			return reply;
		}
		reply = replyToOneMove(ultimate);
		System.out.println("Second reply:" + coords.toString(reply));
		if (isValidMove(reply)) {
			return reply;
		}
		reply = followUp(penultimate);
		System.out.println("Third reply:" + reply);
		if (isValidMove(reply)) {
			return reply;
		}
		reply = playBigPoint();
		System.out.println("Fourth reply:" + reply);
		if (isValidMove(reply)) {
			return reply;
		}
		return NO_POINT;
	}

	public short bestMove(short penultimate, short ultimate) {
		short reply = replyToTwoMoves(penultimate, ultimate);
		if (isValidMove(reply)) {
			return reply;
		}
		reply = replyToOneMove(ultimate);
		if (isValidMove(reply)) {
			return reply;
		}
		reply = followUp(penultimate);
		if (isValidMove(reply)) {
			return reply;
		}
		reply = playBigPoint();
		if (isValidMove(reply)) {
			return reply;
		}
		return NO_POINT;
	}

	short followUp(short penultimateMove) {
		return replies[penultimateMove][RESIGN];
	}

	/**
	 * Returns the number of moves that this phenotype correctly predicts from
	 * game.
	 */
	@SuppressWarnings("boxing")
	public int hits(List<Short> game) {
		board.clear();
		int result = 0;
		for (short p : game) {
			if (board.getColorToPlay() == BLACK) {
				if (bestMove() == p) {
					result++;
				}
			}
			board.play(p);
		}
		return result;
	}

	/** Returns true if p is move not excluded by a priori criteria. */
	boolean isValidMove(short p) {
		return coords.isOnBoard(p) && (board.getColorAt(p) == VACANT)
				&& filter.at(p) && board.isLegal(p);
	}

	short playBigPoint() {
		return replies[RESIGN][RESIGN];
	}

	short replyToOneMove(short previousMove) {
		return replies[RESIGN][previousMove];
	}

	short replyToTwoMoves(short penultimateMove, short previousMove) {
		return replies[penultimateMove][previousMove];
	}
	
	private Mover fallbackMover;
	
	@Override
	public short selectAndPlayOneMove(MersenneTwisterFast random, boolean fast) {
		short p = bestMove();
		if (p != NO_POINT) {
			board.play(p);
		} else {
			return fallbackMover.selectAndPlayOneMove(random, fast);
		}
		// TODO Select a random move from a fallback suggester if p is NO_POINT
		return p;
	}

	void setReply(short penultimateMove, short ultimateMove, short reply) {
		replies[penultimateMove][ultimateMove] = reply;
	}
	
	/**
	 * Plays a game against that. Assumes that this Phenotype is black, that is white.
	 * @param mercy True if we should abandon the playout when one color has many more stones than the other.
	 * @return The color of the winner, or VACANT if the game had no winner.
	 */
	public Color playAgainst(Phenotype that, MersenneTwisterFast random, boolean mercy) {
		do {
			if (board.getTurn() >= coords.getMaxMovesPerGame()) {
				// Playout ran out of moves, probably due to superko
				return VACANT;
			}
			if (board.getPasses() < 2) {
				if (board.getColorToPlay() == BLACK) {
					selectAndPlayOneMove(random, true);
				} else {
					that.selectAndPlayOneMove(random, true);
				}
			}
			if (board.getPasses() >= 2) {
				// Game ended
				return scorer.winner();
			}
			if (mercy) {
				final Color mercyWinner = mercyObserver.mercyWinner();
				if (mercyWinner != null) {
					// One player has far more stones on the board
					return mercyWinner;
				}
			}
		} while (true);
	}

}

