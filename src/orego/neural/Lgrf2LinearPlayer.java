package orego.neural;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import orego.mcts.McRunnable;
import orego.policy.*;

/** Extends LinearPlayer with the LGRF-2 policy. */
public class Lgrf2LinearPlayer extends LinearPlayer {
	
	/** Indices: color to play, previous move. */
	private int[][] replies1;

	/** Indices are color, antepenultimate move, previous move. */
	private int[][][] replies2;

	/** Returns the level 1 reply table. */
	protected int[][] getReplies1() {
		return replies1;
	}

	/** Returns the level 2 replies table. */
	protected int[][][] getReplies2() {
		return replies2;
	}

	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		if (winner != VACANT) {
			super.incorporateRun(winner, runnable);
			int turn = runnable.getTurn();
			int[] moves = runnable.getMoves();
			boolean win = winner == getBoard().getColorToPlay();
			int antepenultimate = getMove(getTurn() - 2);
			int previous = getMove(getTurn() - 1);
			int color = getBoard().getColorToPlay();
			for (int t = getTurn(); t < turn; t++) {
				int move = moves[t];
				if (move != PASS) {
					if (win) {
						replies1[color][previous] = move;
						replies2[color][antepenultimate][previous] = move;
					} else {
						if (replies1[color][previous] == move) {
							replies1[color][previous] = NO_POINT;
						}
						if (replies2[color][antepenultimate][previous] == move) {
							replies2[color][antepenultimate][previous] = NO_POINT;
						}
					}
				}
				win = !win;
				antepenultimate = previous;
				previous = moves[t];
				color = opposite(color);
			}
		}
	}

	@Override
	public void reset() {
		super.reset();
		replies1 = new int[NUMBER_OF_PLAYER_COLORS][LAST_POINT_ON_BOARD + 1];
		replies2 = new int[NUMBER_OF_PLAYER_COLORS][LAST_POINT_ON_BOARD + 1][LAST_POINT_ON_BOARD + 1];
		for (int c = BLACK; c <= WHITE; c++) {
			for (int p : ALL_POINTS_ON_BOARD) {
				replies1[c][p] = NO_POINT;
				for (int q : ALL_POINTS_ON_BOARD) {
					replies2[c][p][q] = NO_POINT;
				}
				replies2[c][p][PASS] = NO_POINT;
				replies2[c][p][NO_POINT] = NO_POINT;
			}
			replies1[c][PASS] = NO_POINT;
			replies1[c][NO_POINT] = NO_POINT;
			replies2[c][NO_POINT][PASS] = NO_POINT;
			replies2[c][NO_POINT][NO_POINT] = NO_POINT;
		}
		for (int i = 0; i < getNumberOfThreads(); i++) {
			McRunnable runnable = ((McRunnable) getRunnable(i));
			Lgrf1Policy policy1 = new Lgrf1Policy(runnable.getPolicy());
			runnable.setPolicy(policy1);
			policy1.setReplies(replies1);
			Lgrf2Policy policy2 = new Lgrf2Policy(policy1);
			runnable.setPolicy(policy2);
			policy2.setReplies2(replies2);
		}
	}

}
