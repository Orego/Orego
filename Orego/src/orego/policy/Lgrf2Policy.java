package orego.policy;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.VACANT;
import orego.core.Board;
import ec.util.MersenneTwisterFast;

/** The last-good-reply (with forgetting) policy, responding to two moves. */
public class Lgrf2Policy extends Policy {

	public Lgrf2Policy() {
		this(new RandomPolicy());
	}

	public Lgrf2Policy(Policy fallback) {
		super(fallback);
	}

	/** replies2[c][p][q] is the last good reply to p then q for color c. */
	private int[][][] replies2;

	/** Stores the level-2 replies table. */
	public void setReplies2(int[][][] replies2) {
		this.replies2 = replies2;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int p = board.getMove(board.getTurn() - 2);
		int q = board.getMove(board.getTurn() - 1);
		int r = replies2[board.getColorToPlay()][p][q];
		if ((board.getColor(r) == VACANT) && board.isFeasible(r)
				&& (board.playFast(r) == PLAY_OK)) {
			return r;
		}
		return getFallback().selectAndPlayOneMove(random, board);
	}

}
