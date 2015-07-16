package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.RESIGN;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.Conjunction;
import edu.lclark.orego.feature.Disjunction;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.NearAnotherStone;
import edu.lclark.orego.feature.NotEyeLike;
import edu.lclark.orego.feature.OnThirdOrFourthLine;
import edu.lclark.orego.feature.Predicate;
import edu.lclark.orego.move.Mover;
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

	public Phenotype(Board board) {
		this.board = board;
		coords = board.getCoordinateSystem();
		replies = new short[coords.getFirstPointBeyondBoard()][coords
				.getFirstPointBeyondBoard()];
		history = new HistoryObserver(board);
		filter = new Conjunction(new NotEyeLike(board), new Disjunction(
				OnThirdOrFourthLine.forWidth(board.getCoordinateSystem()
						.getWidth()), new NearAnotherStone(board)));
	}

	public Phenotype(Board board, Genotype genotype) {
		this(board);
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
	
	@Override
	public short selectAndPlayOneMove(MersenneTwisterFast random, boolean fast) {
		short p = bestMove();
		board.play(p);
		// TODO Select a random move from a fallback suggester if p is NO_POINT
		return p;
	}

	void setReply(short penultimateMove, short ultimateMove, short reply) {
		replies[penultimateMove][ultimateMove] = reply;
	}
}

