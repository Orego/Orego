package orego.policy;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.*;
import orego.core.Board;
import ec.util.MersenneTwisterFast;

/** The last-good-reply (with forgetting) policy, responding to one move. */
public class Lgrf1Policy extends Policy {

	public Lgrf1Policy() {
		this(new RandomPolicy());
	}

	public Lgrf1Policy(Policy fallback) {
		super(fallback);
	}

	/** replies[c][p] is the last good reply to p for color c. */
	private int[][] replies;

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int lastPlay = board.getMove(board.getTurn() - 1);
		int p = replies[board.getColorToPlay()][lastPlay];
		if ((board.getColor(p) == VACANT) && board.isFeasible(p)
				&& (board.playFast(p) == PLAY_OK)) {
			return p;
		}
		return getFallback().selectAndPlayOneMove(random, board);
	}

	/** Stores the replies table. When running multithreaded, all threads use the same table. */
	public void setReplies(int[][] replies) {
		this.replies = replies;
	}

}
