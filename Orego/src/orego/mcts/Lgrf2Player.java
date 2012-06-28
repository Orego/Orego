package orego.mcts;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.ALL_POINTS_ON_BOARD;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;
import orego.play.UnknownPropertyException;
import orego.policy.*;

/**
 * The last-good-reply (with forgetting) player, responding to two moves.
 */
public class Lgrf2Player extends RavePlayer {
	
	/** Indices are color to play, previous move. */
	private int[][] replies1;

	/** Returns the llevel 1 reply table. */
	protected int[][] getReplies1() {
		return replies1;
	}
	
	/** Indices are color, antepenultimate move, previous move. */
	private int[][][] replies2;

	/** Returns the level 2 replies table. */
	protected int[][][] getReplies2() {
		return replies2;
	}

	public static void main(String[] args) {
		Lgrf2Player p = new Lgrf2Player();
		try {
			p.setProperty("policy", "Escape:Pattern:Capture");
			p.setProperty("threads", "2");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		double[] benchMarkInfo = p.benchmark();
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
				+ benchMarkInfo[1]);
	}

	@Override
	public void reset() {
		super.reset();
		replies1 = new int[NUMBER_OF_PLAYER_COLORS][FIRST_POINT_BEYOND_BOARD];
		replies2 = new int[NUMBER_OF_PLAYER_COLORS][FIRST_POINT_BEYOND_BOARD][FIRST_POINT_BEYOND_BOARD];
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

}
