package orego.mcts;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.getAllPointsOnBoard;

import java.util.HashMap;
import java.util.Hashtable;

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
	public static int TOP_RESPONSES_CAP = 8;
	
	/** The maximum number of times a move can be played and deemed illegal */
	public static int MAX_ILLEGALITY_CAP = 5;
	
	/** The minimum WLS threshold for a move to be considered "playable" */
	public static double MIN_WLS_THRESHOLD = .55;
	
	/** Indices are color, antepenultimate move, previous move. */
	private HashMap<Integer, WLSResponseMoveList> bestReplies; 

	
	public static void main(String[] args) {
		WLSPlayer p = new WLSPlayer();
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

	
	/** Converts a sequence of two moves into a hash key for our
	 * responses hashmap. The {@see responses} maintains a mapping
	 * between a "history" of a given number of moves and a "response list"
	 * of possible candidate responses. The response list includes all possible moves
	 * along with information pertaining to the "choiceness" of the move.
	 * 
	 * We maintain three different /conceptual/ tables:
	 * Level Zero
	 * Level One
	 * Level Two
	 * 
	 * The level zero has a single response list which maintains stats for every possible move.
	 * Entries from this table are used mostly at the start of the game.
	 * 
	 * The level one table has a response list for every single. In other words, for any given previous move,
	 * the table returns a list of possible new moves with given stats (response list).
	 * 
	 * The level two table has a response list for every combination of previous *two* moves. Hence, for the
	 * move sequence AB the level two table contains a response list of possible response.
	 * 
	 * We encode each of these types of tables as integers for keys in our responses hashmap. Level zero keys are encoded
	 * as integers with each group of nine bits set to a special sentinel value. 
	 * 
	 * Level one keys encode the previous move in the first nine lower order index.
	 * 
	 * Level two keys encode the previous two moves in the first nine lower order bits and then the second
	 * nine bits. Conceivably we can store up to three different "levels" of moves which would consume 24 bits.
	 * We need nine bits for each move since we have a maximum move index of 361.
	 * 
	 * We reserve the 28th bit to indicate the color of the player.
	 * 
	 * @param prevPrevMove Two moves ago
	 * @param prevMove Previous move
	 * @return An integer key for our {@link responses} hashmap.
	 */
	public static int levelTwoEncodedIndex(int prevPrevMove, int prevMove, int color) {
		// place the color in the upper 28th bit
		return (color << 27) | (prevPrevMove << 9) | prevMove;
	}
	
	/** Returns the level 2 best replies table. */
	protected HashMap<Integer, WLSResponseMoveList> getBestReplies() {
		return bestReplies;
	}
	
	@Override
	public void reset() {
		try {
			super.reset();
			// Create reply tables
			bestReplies = new HashMap<Integer, WLSResponseMoveList>();
			
			for (int c = BLACK; c <= WHITE; c++) {
				for (int p : getAllPointsOnBoard()) {
					for (int q : getAllPointsOnBoard()) {
						bestReplies.put(levelTwoEncodedIndex(p, q, c), new WLSResponseMoveList(TOP_RESPONSES_CAP));
					}
//					bestReplies.put(levelTwoEncodedIndex(p, pass, c), new WLSResponseMoveList(TOP_RESPONSES_CAP));
//					bestReplies[c][PASS][p]     	= new WLSResponseMoveList(TOP_RESPONSES_CAP);
//					bestReplies[c][p][NO_POINT] 	= new WLSResponseMoveList(TOP_RESPONSES_CAP);
//					bestReplies[c][NO_POINT][p] 	= new WLSResponseMoveList(TOP_RESPONSES_CAP);
				}
				bestReplies.put(levelTwoEncodedIndex(NO_POINT, NO_POINT, c), new WLSResponseMoveList(TOP_RESPONSES_CAP));
//				bestReplies[c][NO_POINT][PASS]      = new WLSResponseMoveList(TOP_RESPONSES_CAP);
//				bestReplies[c][NO_POINT][NO_POINT]  = new WLSResponseMoveList(TOP_RESPONSES_CAP);
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
					WLSResponseMoveList list = bestReplies.get(levelTwoEncodedIndex(antepenultimate, previous, color));
					
					if (list == null) {
						list = new WLSResponseMoveList(TOP_RESPONSES_CAP);
						bestReplies.put(levelTwoEncodedIndex(antepenultimate, previous, color), list);
					}
					
					if (win) {
						
						list.addWin(move);
					} else {
						// add a loss to this move
						list.addLoss(move);
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
	public void setProperty(String name, String value) throws UnknownPropertyException {
		if (name.equals("topResultsLength")) {
			int newLength = Integer.valueOf(value);
			TOP_RESPONSES_CAP = newLength;
			
			for (int c = BLACK; c <= WHITE; c++) {
				for (int p : getAllPointsOnBoard()) {
					for (int q : getAllPointsOnBoard()) {
						WLSResponseMoveList list = bestReplies.get(levelTwoEncodedIndex(p, q, c));
						if (list != null) {
							list.resizeTopResponses(newLength);
						}
					}
				}
			}
			return;
		} else if (name.equals("minWlsThreshold")) {
			MIN_WLS_THRESHOLD = Double.parseDouble(value);
			return;
		} else if (name.equals("maxIllegalityThreshold")) {
			MAX_ILLEGALITY_CAP = Integer.parseInt(value);
			return;
		}
		
		super.setProperty(name, value);
	}
}
