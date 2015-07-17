package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.RESIGN;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.StoneColor.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.feature.Conjunction;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.Predicate;
import edu.lclark.orego.feature.StoneCountObserver;
import edu.lclark.orego.mcts.CopiableStructure;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.score.ChinesePlayoutScorer;
import edu.lclark.orego.score.Scorer;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

/**
 * Players use this class to perform multiple Monte Carlo coevolution in different
 * threads.
 */
public class EvoRunnable implements Runnable {

	private final Board board;

	private final CoordinateSystem coords;
	
	private Mover fallbackMover;

	private final Predicate filter;

	private final HistoryObserver history;

	private final ChinesePlayoutScorer scorer;
	
	private final StoneCountObserver mercyObserver;
	
	private final MersenneTwisterFast random;
			
	public EvoRunnable(CopiableStructure stuff) {
		random = new MersenneTwisterFast();
		final CopiableStructure copy = stuff.copy();
		this.board = copy.get(Board.class);
		coords = board.getCoordinateSystem();
		phenotypes = new Phenotype[2][3];
		for (int color = 0; color < phenotypes.length; color++) {
			for (int i = 0; i < phenotypes[color].length; i++) {
				phenotypes[color][i] = new Phenotype(coords);
			}
		}
		history = copy.get(HistoryObserver.class);
		filter = copy.get(Conjunction.class);
		fallbackMover = copy.get(Mover.class);
		scorer = copy.get(ChinesePlayoutScorer.class);
		mercyObserver = copy.get(StoneCountObserver.class);
	}

	public Board getBoard() {
		return board;
	}

	private Population[] populations;
	
	/**
	 * Chooses two random members of the population and has them play a game against each other.
	 *
	 * @param mercy True if we should abandon the playout when one color has many more stones than the other.
	 * @return The winning color, although this is only used in tests.
	 */	
	public Color performPlayout(boolean mercy) {
		Phenotype black = phenotypes[BLACK.index()][0];
		black.installGenes(populations[BLACK.index()].randomGenotype(random));
		Phenotype white = phenotypes[WHITE.index()][0];
		white.installGenes(populations[WHITE.index()].randomGenotype(random));
		return performPlayout(black, white, mercy);
	}

	/** Returns the number of playouts completed by this runnable. */
	public int getPlayoutsCompleted() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	private Phenotype[][] phenotypes;
	
	Phenotype getPhenotype(StoneColor color, int index) {
		return phenotypes[color.index()][index];
	}

	public Color performPlayout(Phenotype black, Phenotype white, boolean mercy) {
		return playAgainst(black, white, mercy);
	}

	/**
	 * Plays a game against that. Assumes that this Phenotype is black, that is white.
	 * @param mercy True if we should abandon the playout when one color has many more stones than the other.
	 * @return The color of the winner, or VACANT if the game had no winner.
	 */
	public Color playAgainst(Phenotype black, Phenotype white, boolean mercy) {
		final CoordinateSystem coords = board.getCoordinateSystem();
		do {
			if (board.getTurn() >= coords.getMaxMovesPerGame()) {
				// Playout ran out of moves, probably due to superko
				return VACANT;
			}
			if (board.getPasses() < 2) {
				if (board.getColorToPlay() == BLACK) {
					selectAndPlayOneMove(black, true);
				} else {
					selectAndPlayOneMove(white, true);
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

	public short selectAndPlayOneMove(Phenotype phenotype, boolean fast) {
		short p = bestMove(phenotype);
		if (p != NO_POINT) {
			board.play(p);
		} else {
			return fallbackMover.selectAndPlayOneMove(random, fast);
		}
		return p;
	}
	
	public short bestMove(Phenotype phenotype) {
		return bestMove(phenotype, history.get(board.getTurn() - 2), history.get(board.getTurn() - 1));
	}

	public short bestMove(Phenotype phenotype, short penultimate, short ultimate) {
		short reply = phenotype.replyToTwoMoves(penultimate, ultimate);
		if (isValidMove(reply)) {
			return reply;
		}
		reply = phenotype.replyToOneMove(ultimate);
		if (isValidMove(reply)) {
			return reply;
		}
		reply = phenotype.followUp(penultimate);
		if (isValidMove(reply)) {
			return reply;
		}
		reply = phenotype.playBigPoint();
		if (isValidMove(reply)) {
			return reply;
		}
		return NO_POINT;
	}

	/** Returns true if p is move not excluded by a priori criteria. */
	boolean isValidMove(short p) {
		return coords.isOnBoard(p) && (board.getColorAt(p) == VACANT)
				&& filter.at(p) && board.isLegal(p);
	}

	public void setPopulations(Population[] populations) {
		this.populations = populations;
	}

}
