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

/**
 * A simple extension to RavePlayer which maintains a list of best replies
 * to a given move. We use WLS to track the "goodness" of a given move as a response.
 * 
 * WLS player only uses this last good reply in the actual playouts, not in the tree selection
 * component. WLSPlayer maintains a complementary WLSMcRunnable.
 * @author sstewart
 *
 */
public class WLSPlayer extends RavePlayer {

	/** Length of "top responses" list (4 - 16) */
	public final static int TOP_RESPONSES_CAP = 8;
	
	/** Indices are color, antepenultimate move, previous move. */
	private WLSResponseMoveList[][][] bestReplies;

	public static void main(String[] args) {
		Lgrf2Player p = new Lgrf2Player();
		try {
			p.setProperty("heuristics", "Escape@20:Pattern@20:Capture@20");
			p.setProperty("threads", "1");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		double[] benchMarkInfo = p.benchmark();
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: " + benchMarkInfo[1]);
	}

	/** Returns the level 2 best replies table. */
	protected WLSResponseMoveList[][][] getBestReplies() {
		return bestReplies;
	}
	
	@Override
	public void reset() {
		try {
			super.reset();
			// Create reply tables
			bestReplies = new WLSResponseMoveList[NUMBER_OF_PLAYER_COLORS][FIRST_POINT_BEYOND_BOARD][FIRST_POINT_BEYOND_BOARD];
			for (int c = BLACK; c <= WHITE; c++) {
				for (int p : ALL_POINTS_ON_BOARD) {
					for (int q : ALL_POINTS_ON_BOARD) {
						bestReplies[c][p][q] 		= new WLSResponseMoveList(TOP_RESPONSES_CAP);
					}
					bestReplies[c][p][PASS]     	= new WLSResponseMoveList(TOP_RESPONSES_CAP);
					bestReplies[c][p][NO_POINT] 	= new WLSResponseMoveList(TOP_RESPONSES_CAP);
				}
				bestReplies[c][NO_POINT][PASS]      = new WLSResponseMoveList(TOP_RESPONSES_CAP);
				bestReplies[c][NO_POINT][NO_POINT]  = new WLSResponseMoveList(TOP_RESPONSES_CAP);
			}
			
			// Replace McRunnables with WLSMcRunnables
			for (int i = 0; i < getNumberOfThreads(); i++) {
				setRunnable(i, new WLSMcRunnable(this, getHeuristics().clone(), bestReplies));

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
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
						bestReplies[color][antepenultimate][previous].addWin(move);
					} else {
						// add a loss to this move
						bestReplies[color][antepenultimate][previous].addLoss(move);
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
