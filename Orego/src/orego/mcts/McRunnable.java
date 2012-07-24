package orego.mcts;

import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.*;
import static orego.core.Board.*;
import orego.core.Board;
import orego.policy.*;
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
	private int playoutsCompleted;

	/** Playout policy. */
	private Policy policy;

	/** Array of heuristics. */
	private Heuristic[] heuristics;

	/** Random number generator. */
	private final MersenneTwisterFast random;

	/** Values of neighboring moves. Used by selectAndPlayOneMove(). */
	private int[] values;
	
	public McRunnable(McPlayer player, Policy policy) {
		board = new Board();
		this.player = player;
		random = new MersenneTwisterFast();
		hashes = new long[MAX_MOVES_PER_GAME + 1];
		this.policy = policy;
		playedPoints = new IntSet(FIRST_POINT_BEYOND_BOARD);
		heuristics = new Heuristic[] {
				new CaptureHeuristic() 
				};
		values = new int[8];
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
	public int getPlayoutsCompleted() {
		return playoutsCompleted;
	}

	/** Returns this runnable's policy. */
	public Policy getPolicy() {
		return policy;
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

	// TODO Is the return value necessary?
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		// TODO Is the bias from not randomizing the order a bad thing?
		// Compute heuristic values
		// TODO Should Board have a getLastMove() method?
		int lastMove = board.getMove(board.getTurn() - 1);
		for (int i = 0; i < 8; i++) {
			int p = NEIGHBORS[lastMove][i];
			if ((board.getColor(p) == VACANT) && (board.isFeasible(p))) {
				for (Heuristic h : heuristics) {
					values[i] += h.evaluate(p, board);
				}
			} else {
				values[i] = 0;
			}
		}
		// Find best suggested move
		while (true) {
			int bestIndex = -1;
			int bestValue = 0;
			for (int i = 0; i < 8; i++) {
				if (values[i] > bestValue) {
					bestIndex = i;
					bestValue = values[i];
				}
			}
			if (bestIndex == -1) {
				// No moves suggested -- play randomly
				return policy.selectAndPlayOneMove(random, board);
			}
			int bestMove = NEIGHBORS[lastMove][bestIndex];
			if (board.playFast(bestMove) == PLAY_OK) {
				return bestMove;
			} else {
				values[bestIndex] = 0;
			}
		}
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

	/** Sets the policy of this McRunnable. */
	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

}
