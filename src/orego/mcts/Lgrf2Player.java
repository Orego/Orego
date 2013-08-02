package orego.mcts;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.getAllPointsOnBoard;
import static orego.core.Coordinates.getFirstPointBeyondBoard;

import java.lang.reflect.Constructor;

import orego.heuristic.Heuristic;
import orego.play.UnknownPropertyException;

/**
 * The last-good-reply (with forgetting) player, responding to two moves.
 */
public class Lgrf2Player extends RavePlayer {

	/** Indices are color to play, previous move. */
	private int[][] replies1;

	/** Returns the level 1 reply table. */
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
		try {
			Lgrf2Player p = new Lgrf2Player();
			p.setProperty("heuristics", "Escape@20:Pattern@20:Capture@20:Ladder@20");
			p.setProperty("heuristic.Pattern.numberOfGoodPatterns", "400");
			p.setProperty("threads", "1");
			double[] benchMarkInfo = p.benchmark();
			System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
					+ benchMarkInfo[1]);
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void reset() {
		try {
			super.reset();
			// Create reply tables
			replies1 = new int[NUMBER_OF_PLAYER_COLORS][getFirstPointBeyondBoard()];
			replies2 = new int[NUMBER_OF_PLAYER_COLORS][getFirstPointBeyondBoard()][getFirstPointBeyondBoard()];
			for (int c = BLACK; c <= WHITE; c++) {
				for (int p : getAllPointsOnBoard()) {
					replies1[c][p] = NO_POINT;
					for (int q : getAllPointsOnBoard()) {
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
			// Replace McRunnables with LgrfMcRunnables
			for (int i = 0; i < getNumberOfThreads(); i++) {
				setRunnable(i, new LgrfMcRunnable(this, getHeuristics().clone(), replies1, replies2));

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		super.incorporateRun(winner, runnable);
		if (winner != VACANT) {
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
