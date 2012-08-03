package orego.mcts;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import orego.core.Coordinates;
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
	private int topResponseLength = Integer.MAX_VALUE;
	
	/** The win/loss states (indices) for every move (indexed by move). Hence,
	 * this array serves a second level of indirection [move -> movesWLS -> WLS States]*/
	private short[] movesWLS;
	
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
	private short[] responsesIllegal;
	
	public WLSResponseMoveList(int topResponseListLength) {
		this.topResponseLength = topResponseListLength;
		
		// track all possible moves
		movesWLS     = new short[FIRST_POINT_BEYOND_BOARD];
		
		movesWLS[Coordinates.NO_POINT] = 0;
		
		// track only the top k moves
		topResponses = new int[topResponseListLength];
		
		for (int i = 0; i < topResponseLength; i++) {
			topResponses[i] = Coordinates.NO_POINT;
		}
	}
	
	public short[] getMovesWLS() {
		return movesWLS;
	}
	
	public int[] getTopResponses() {
		return topResponses;
	}
		
	public int getTopResponsesLength() {
		return topResponseLength;
	}
	
	public void resizeTopResponses(int newLength) {
		topResponseLength = newLength;
		
		topResponses = new int[newLength];
		
		for (int i = 0; i < topResponseLength; i++) {
			topResponses[i] = Coordinates.NO_POINT;
		}
	}
	public State getWLSState(int move) {
		return WinLossStates.getWLS().getState(movesWLS[move]);
	}
	
	/** Gets the index of the move in the top response list and returns -1 if
	 * the move does not exist.
	 */
	
	protected int moveIndexInTopResponses(int move) {
		for (int i = 0; i < topResponseLength; i++) {
			if (topResponses[i] == move) return i;
		}
		return -1;
	}
	/** Checks to see if a given move is already in the top responses list*/
	protected boolean inTopResponses(int move) {
		return (moveIndexInTopResponses(move) >= 0);
	}
	/** 
	 * Simple method which checks to see if a given move should be included
	 * in the "top responses" list. If the move should be included, it makes the appropriate update.
	 * You must pass in the new proposed state index.
	 * @param move The proposed move
	 * @param newStateIndex the proposed WLS state
	 */
	protected void rankMove(int move, int newStateIndex) {
		
		// start at the worst moves and see if we can "break into" the top moves list.
		// We then work our way up the list until we find a move which is better than ours.
		// We place our move back one.
		WinLossStates wls = WinLossStates.getWLS();
		
		// we examine the confidence of the *new* WLS state (to compare against the possibly already existing state in topResponses)
		double moveConfidence = wls.getState(newStateIndex).getConfidence();
		
		int topResponseStartIndex = topResponseLength - 1;
		
		// do we break into the list at all?
		short responseMoveIndex = movesWLS[topResponses[topResponseStartIndex]];
		
		// if we are better than the smallest element, check to see if we already exist further up the list. Otherwise,
		// we will add ourselves.
		if (moveConfidence > wls.getState(responseMoveIndex).getConfidence()) {
			
			
			int start = moveIndexInTopResponses(move);
			
			if (start > 0) {
				// if we are already in the list, we simply swap ourselves up the list into the proper position.
				// We need to start from the position of our move in the list
				topResponseStartIndex = start;
			} else if (start == 0) {
				// already at the top of the list, don't move it
				return;
			} else {
				// (start < 0 so the element doesn't exist in the list)
				// if we're a new element, drop the lowest move off the end of the list
				topResponses[topResponseStartIndex] = move;
			}
			
		} else {
			return; // we don't even break in
		}
		
		// we start at the move position
		for (int i = (topResponseStartIndex) - 1; i >= 0; i--) {
			
			// find the WLS state for the current move
			responseMoveIndex = movesWLS[topResponses[i]];
			if (moveConfidence > wls.getState(responseMoveIndex).getConfidence()) {
				// swap ourselves into a better position down the list
				int oldMove = topResponses[i];
				topResponses[i] = move;
				topResponses[i + 1] = oldMove;

			} else {
				return; // we've swapped into proper place
			}
		}
	}
	
	public String topResponsesToString() {
		StringBuilder builder = new StringBuilder();
		for (int move : topResponses) {
			double confidence = WinLossStates.getWLS().getState(movesWLS[move]).getConfidence();
			builder.append(Coordinates.pointToString(move) + "( " + confidence + " ) ");
		}
		
		return builder.toString();
	}
	public void addWin(int move) {
		// we increment to the new WLS state after a win (from old WLS state)
		short newStateIndex = (short) WinLossStates.getWLS().addWin((int) movesWLS[move]);
		
		// we must rank the move before updating the movesWLS array since we use the
		// movesWLS array for comparison in rankMove
		rankMove(move, newStateIndex);
		
		movesWLS[move] = newStateIndex;
	}
	
	public void addLoss(int move) {
		// we increment to the new WLS state after a loss (from old WLS state)
		movesWLS[move] = (short) WinLossStates.getWLS().addLoss((int) movesWLS[move]);
				
		// Note: we don't bother to "de-rank" a move this will happen auto-magically when
		// we begin favoring a different moves.
	}
	
}
