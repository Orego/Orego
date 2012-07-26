package orego.mcts;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.VACANT;
import ec.util.MersenneTwisterFast;
import orego.core.Board;
import orego.heuristic.Heuristic;

/**
 * McRunnable that uses LGRF2 (Last Good Reply with Forgetting, level 2) to
 * generate moves.
 */
public class LgrfMcRunnable extends McRunnable {

	/**
	 * Level 1 reply table. replies1[c][p] is the last good reply for color c to
	 * move p, or NO_POINT if there is none.
	 */
	private int[][] replies1;

	/**
	 * Level 2 reply table. replies1[c][p][q] is the last good reply for color c
	 * to the move sequence pq, or NO_POINT if there is none.
	 */
	private int[][][] replies2;

	public LgrfMcRunnable(McPlayer player, 
			Heuristic[] heuristics, int[][] replies1, int[][][] replies2) {
		super(player, heuristics);
		this.replies1 = replies1;
		this.replies2 = replies2;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int p = board.getMove(board.getTurn() - 2);
		int q = board.getMove(board.getTurn() - 1);
		int r = replies2[board.getColorToPlay()][p][q];
		// Try a level 2 reply
		if ((board.getColor(r) == VACANT) && board.isFeasible(r)
				&& (board.playFast(r) == PLAY_OK)) {
			return r;
		}
		// Try a level 1 reply
		r = replies1[board.getColorToPlay()][q];
		if ((board.getColor(r) == VACANT) && board.isFeasible(r)
				&& (board.playFast(r) == PLAY_OK)) {
			return r;
		}
		// No good replies stored; proceed normally
		return super.selectAndPlayOneMove(random, board);
	}

}
