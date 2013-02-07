package orego.mcts;

import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.*;
import static orego.core.Board.*;
import orego.core.Board;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;
import orego.heuristic.*;

/**
 * Players use this class to perform multiple Monte Carlo runs in different
 * threads.
 */
public class McRunnable implements Runnable {

	/**
	 * If one player's number of stones on the board exceeds the opponent's by
	 * this much, the playout is considered a win for that player.
	 */
	public static final int MERCY_THRESHOLD = BOARD_AREA / 2;

	/** The board on which this McRunnable plays its moves. */
	private Board board;

	/** History of hash codes of dag nodes visited. */
	private long[] hashes;

	/** Used by MctsPlayer.incorporateRun(). */
	private IntSet playedPoints;

	/** The Player that launches the thread wrapped around this McRunnable. */
	private McPlayer player;

	/** Number of playouts completed. */
	private long playoutsCompleted;

	/** Array of heuristics. */
	private HeuristicList heuristics;

	/** Random number generator. */
	private final MersenneTwisterFast random;

	public McRunnable(McPlayer player, HeuristicList heuristics) {
		board = new Board();
		this.player = player;
		random = new MersenneTwisterFast();
		hashes = new long[MAX_MOVES_PER_GAME + 1];
		playedPoints = new IntSet(FIRST_POINT_BEYOND_BOARD);
		this.heuristics = heuristics;
	}

	/**
	 * Accepts (plays on on this McRunnable's own board) the given move.
	 * 
	 * @see orego.core.Board#play(int)
	 */
	public void acceptMove(int p) {
		int legality = board.play(p);
		assert legality == PLAY_OK : "Legality " + legality + " for move "
				+ pointToString(p) + "\n" + board;
		hashes[board.getTurn()] = board.getHash();
	}

	/** Copies data from that (the player's real board) to the local board. */
	public void copyDataFrom(Board that) {
		board.copyDataFrom(that);
	}

	/** Returns the board associated with this runnable. */
	public Board getBoard() {
		return board;
	}

	/**
	 * Return the array of hashes of board positions seen in this playout.
	 * 
	 * @see #getMoves()
	 */
	public long[] getHashes() {
		return hashes;
	}

	protected HeuristicList getHeuristics() {
		return heuristics;
	}

	/** Returns the move made at turn t, or NO_POINT if t < 0. */
	public int getMove(int t) {
		return board.getMove(t);
	}

	/**
	 * Returns the array of moves recorded for this playout. Indices correspond
	 * to turn numbers from the beginning of the game.
	 */
	public int[] getMoves() {
		return board.getMoves();
	}

	/** @see orego.mcts.MctsPlayer#incorporateRun(int, McRunnable) */
	public IntSet getPlayedPoints() {
		// TODO: is never actually updated and SearchNode#recordPlayout never
		// uses it
		return playedPoints;
	}

	/** @return the player associated with this runnable */
	public McPlayer getPlayer() {
		return player;
	}

	/** Returns the number of playouts completed by this runnable. */
	public long getPlayoutsCompleted() {
		return playoutsCompleted;
	}
	
	/** Sets the number of playouts completed (mostly for unit testing) */
	protected void setPlayoutsCompleted(long playoutsCompleted) {
		this.playoutsCompleted = playoutsCompleted;
	}

	/** Returns the random number generator associated with this runnable. */
	public MersenneTwisterFast getRandom() {
		return random;
	}

	/** Returns the current turn number on this runnable's board. */
	public int getTurn() {
		return board.getTurn();
	}

	/**
	 * Performs a single Monte Carlo run and incorporates it into player's
	 * search tree. The player should generate moves to the frontier of the
	 * known tree, add a 'node', and then return. We will handle performing the
	 * actual playout in the aptly named {@link playout}.
	 */
	public void performMcRun() {
		player.generateMovesToFrontier(this);
		playoutsCompleted++;
		int winner;
		if (board.getPasses() == 2) {
			winner = board.finalWinner();
		} else {
			winner = playout();
		}
		player.incorporateRun(winner, this);
	}

	/**
	 * Plays moves to the end of the game and returns the winner: BLACK, WHITE,
	 * or (in rare event where the playout is canceled because it hits the
	 * maximum number of moves) VACANT.
	 */
	public int playout() {
		do {
			if (board.getTurn() >= MAX_MOVES_PER_GAME) {
				// Playout ran out of moves, probably due to superko
				return VACANT;
			}
			if (board.getPasses() < 2) {
				selectAndPlayOneMove(random, board);
				hashes[board.getTurn()] = board.getHash();
			}
			if (board.getPasses() >= 2) {
				// Game ended
				return board.playoutWinner();
			}
			if (Math.abs(board.approximateScore()) > MERCY_THRESHOLD) {
				// One player has far more stones on the board
				return board.approximateWinner();
			}
		} while (true);
	}

	/**
	 * Performs runs and incorporate them into player's search tree until this
	 * thread is interrupted.
	 */
	@Override
	public void run() {
		boolean limitPlayouts = player.getMillisecondsPerMove() <= 0;
		int playouts = 0;
		int limit = player.getPlayoutLimit();
		while ((limitPlayouts && playouts < limit)
				|| (!limitPlayouts & getPlayer().shouldKeepRunning())) {
			performMcRun();
			playouts++;
		}
	}

	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		return heuristics.selectAndPlayOneMove(random, board);
	}

	// TODO This is only called on "fresh" nodes. This happens in several
	// places; could it be consolidated into the few places where fresh nodes
	// are produced?
	public void updatePriors(SearchNode node, Board board) {
		for (Heuristic h : heuristics.getHeuristics()) {
			h.prepare(board);
			IntSet good = h.getGoodMoves();
			for (int i = 0; i < good.size(); i++) {
				int p = good.get(i);
				node.addWins(p, h.getWeight());
			}
			// TODO The remaining code doesn't seem to do anything. Why is it here? Left over from when we had negative heuristics?
			IntSet vacant = board.getVacantPoints();
			for (int i = 0; i < vacant.size(); i++) {
				int p = vacant.get(i);
			}
		}
	}

}
