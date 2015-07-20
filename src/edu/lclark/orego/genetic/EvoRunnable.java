package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.experiment.Logging.log;
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
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

/**
 * Players use this class to perform multiple Monte Carlo coevolution in
 * different threads.
 */
public class EvoRunnable implements Runnable {

	private final Board board;

	private final CoordinateSystem coords;

	private Mover fallbackMover;

	private final Predicate filter;

	private final HistoryObserver history;

	/** Indices of the tournament losers. */
	private int[] loserIndices;

	private final StoneCountObserver mercyObserver;

	/** phenotypes[c][i] is phenotype number i of color c for the current tournament. */
	private Phenotype[][] phenotypes;

	private final Player player;
	
	private Population[] populations;

	private final MersenneTwisterFast random;

	private final ChinesePlayoutScorer scorer;

	public EvoRunnable(Player player, CopiableStructure stuff) {
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
		loserIndices = new int[2];
		this.player = player;
	}

	public short bestMove(Phenotype phenotype) {
		return bestMove(phenotype, history.get(board.getTurn() - 2),
				history.get(board.getTurn() - 1));
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

	public Board getBoard() {
		return board;
	}

	Phenotype getPhenotype(StoneColor color, int index) {
		return phenotypes[color.index()][index];
	}

	/** Returns the number of playouts completed by this runnable. */
	public int getPlayoutsCompleted() {
		// TODO Auto-generated method stub
		return 0;
	}

	/** Returns true if p is move not excluded by a priori criteria. */
	boolean isValidMove(short p) {
		return coords.isOnBoard(p) && (board.getColorAt(p) == VACANT)
				&& filter.at(p) && board.isLegal(p);
	}

	/**
	 * Chooses two random members of the population and has them play a game
	 * against each other.
	 *
	 * @param mercy
	 *            True if we should abandon the playout when one color has many
	 *            more stones than the other.
	 * @return The winning color, although this is only used in tests.
	 */
	public Color performPlayout(boolean mercy) {
		Phenotype black = phenotypes[BLACK.index()][0];
		black.installGenes(populations[BLACK.index()].randomGenotype(random));
		Phenotype white = phenotypes[WHITE.index()][0];
		white.installGenes(populations[WHITE.index()].randomGenotype(random));
		return playAgainst(black, white, mercy);
	}

	/**
	 * Plays a game against that. Assumes that this Phenotype is black, that is
	 * white.
	 * 
	 * @param mercy
	 *            True if we should abandon the playout when one color has many
	 *            more stones than the other.
	 * @return The color of the winner, or VACANT if the game had no winner.
	 */
	public Color playAgainst(Phenotype black, Phenotype white, boolean mercy) {
		playoutsCompleted++;
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

	/**
	 * Plays a tournament among two pairs of individuals -- one from each
	 * population. The indices of the individuals with the fewest wins are
	 * stored in loserIndices.
	 */
	public void playTournament(boolean mercy) {
		// Choose contestants and install their genes in phenotypes
		int[][] playIndices = new int[2][2];
		for (int color = 0; color < playIndices.length; color++) {
			for (int i = 0; i < playIndices[color].length; i++) {
				playIndices[color][i] = random.nextInt(populations[BLACK
						.index()].size());
				phenotypes[color][i].installGenes(populations[color]
						.getIndividuals()[playIndices[color][i]]);
			}
		}
		// Run the tournament
		for (int b = 0; b < playIndices[BLACK.index()].length; b++) {
			Phenotype black = getPhenotype(BLACK, b);
			for (int w = 0; w < playIndices[WHITE.index()].length; w++) {
				Phenotype white = getPhenotype(WHITE, w);
				Color winner = playAgainst(black, white, mercy);
				if (winner == WHITE) {
					getPhenotype(WHITE, w).setWinCount(
							getPhenotype(WHITE, w).getWinCount() + 1);
				} else if (winner == BLACK) {
					getPhenotype(BLACK, b).setWinCount(
							getPhenotype(BLACK, b).getWinCount() + 1);
				}
			}
		}
		// Determine losers
		loserIndices[BLACK.index()] = getPhenotype(BLACK, 0).getWinCount() < getPhenotype(
				BLACK, 1).getWinCount() ? playIndices[BLACK.index()][0]
				: playIndices[BLACK.index()][1];
		loserIndices[WHITE.index()] = getPhenotype(WHITE, 0).getWinCount() < getPhenotype(
				WHITE, 1).getWinCount() ? playIndices[WHITE.index()][0]
				: playIndices[WHITE.index()][1];
	}

	/** Replaces the losers indicated by loserIndices with new individuals. */
	public void replaceLosers() {
		for (int c = 0; c < 2; c++) {
			populations[c].replaceLoser(loserIndices[c], random);
		}
	}

	private long playoutsCompleted;
	
	@Override
	public void run() {
		playoutsCompleted = 0;
		while (player.shouldKeepRunning()) {
			playTournament(true); // Should that mercy value be read from somewhere?
			replaceLosers();
		}
		log("Playouts completed: " + playoutsCompleted);
		player.notifyMcRunnableDone();
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

	public void setPopulations(Population[] populations) {
		this.populations = populations;
	}

}
