package orego.mcts;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import orego.wls.State;
import orego.wls.WinLossStates;

/** 
 * Simple container object which manages a list of moves,
 * their associated WLS states, and a list of top K moves from this list.
 * 
 * Each list of moves will be used in response to a given two move pair in WLSPlayer.
 * @author sstewart
 *
 */
public class WLSResponseMoveList {
	
	/** The size of our "top" response list" */
	private int topResponseLength;
	
	/** The win/loss states (indices) for every move (indexed by move).*/
	private byte[] movesWLS;
	
	/** The list of top response moves (indexed by "goodness").
	 * The moves at the front are the best while the moves at the end
	 * are the worse. Hence, the various moves' rankings decrease
	 * as you move along the list.
	 * */
	private int[] topResponses;
	
	/** Tracks the number of times a given move in the top response list has
	 * been tried and declared illegal.
	 * TODO: actually use this
	 */
	private short[] responseIllegal;
	
	public WLSResponseMoveList(int topResponseListLength) {
		this.topResponseLength = topResponseListLength;
		
		// track all possible moves
		movesWLS     = new byte[FIRST_POINT_BEYOND_BOARD];
		
		// track only the top k moves
		topResponses = new int[topResponseListLength];
	}
	
	public byte[] getMovesWLS() {
		return movesWLS;
	}
	
	public int[] getTopResponses() {
		return topResponses;
	}
		
	public int getTopResponsePoint() {
		return topResponseLength;
	}
	
	public State getWLSState(int move) {
		return WinLossStates.getWLS().getState(movesWLS[move]);
	}
	
	/** Checks to see if a given move is already in the top responses list*/
	protected boolean inTopResponses(int move) {
		for (int topResponseMove : topResponses) {
			if (topResponseMove == move) return true;
		}
		
		return false;
	}
	/** 
	 * Simple method which checks to see if a given move should be included
	 * in the "top responses" list. 
	 * @param move
	 */
	protected void rankMove(int move) {
		// if already in the top response list, simply skip
		if (inTopResponses(move)) return;
		
		// start at the worst moves and see if we can "break into" the top moves list.
		// We then work our way up the list until we find a move which is better than ours.
		// We place our move back one.
		WinLossStates wls = WinLossStates.getWLS();
		
		// do we break into the list at all?
		byte responseMoveIndex = movesWLS[topResponseLength - 1];
		if (wls.getState(movesWLS[move]).getConfidence() > wls.getState(responseMoveIndex).getConfidence()) {
			
			// simply replace the lowest element in preparation for swappage
			topResponses[topResponseLength - 1] = move;
			
		} else {
			return; // we don't even break in
		}
		
		
		// we start at one less than the end
		for (int i = (topResponseLength - 1) - 1; i >= 0; i--) {
			responseMoveIndex = movesWLS[i];
			if (wls.getState(movesWLS[move]).getConfidence() > wls.getState(responseMoveIndex).getConfidence()) {
				
				// swap ourselves into a better position
				int oldMove = topResponses[i];
				topResponses[i] = move;
				topResponses[i + 1] = oldMove;
				
				return;
			} else {
				return; // we've swapped into proper place
			}
		}
	}
	public void addWin(int move) {
		// we increment to the new WLS state after a win (from old WLS state)
		movesWLS[move] = (byte) WinLossStates.getWLS().addWin((int) movesWLS[move]);
		
		// decide if we break into the top list or not
		rankMove(move);
	}
	
	public void addLoss(int move) {
		// we increment to the new WLS state after a loss (from old WLS state)
		movesWLS[move] = (byte) WinLossStates.getWLS().addLoss((int) movesWLS[move]);
				
		// Note: we don't bother to "derank" a moveas this will happen automagically when
		// we begin favoring a different moves.
	}
	
}
