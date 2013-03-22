package orego.mcts;

import static orego.core.Coordinates.getFirstPointBeyondBoard;
import orego.core.Coordinates;
import orego.wls.State;
import orego.wls.WinLossStates;

/** 
 * Simple container object which manages a list of moves,
 * their associated WLS states, and a list of top K moves from this list.
 * 
 * Each list of moves will be used in response to a given two move pair in WLSPlayer.
 * 
 * You can examine Jacques' paper for further informatoin.
 * @author sstewart
 *
 */
public class WLSResponseMoveList {
	
	/** The win/loss states (indices) for every possible response move (indexed by move). Hence,
	 * this array serves a second level of indirection [move -> movesWLS -> WLS States]*/
	private short[] allMovesWLS;
	
	/** The list of top response moves (indexed by "goodness").
	 * The moves at the front are the best while the moves at the end
	 * are the worse. Hence, the various moves' rankings decrease
	 * as you move along the list.
	 * */
	private int[] topResponses;
	
	/** Tracks the number of times a given move in the top response list has
	 * been tried and declared illegal.
	 */
	private short[] topResponsesIllegality;
	
	public WLSResponseMoveList(int topResponseListLength) {
		
		// track all possible moves
		allMovesWLS     = new short[getFirstPointBeyondBoard()];
		
		// keep illegality counters for each move
		topResponsesIllegality = new short[topResponseListLength];
		
		allMovesWLS[Coordinates.NO_POINT] = 0;
		
		// track only the top k moves
		topResponses = new int[topResponseListLength];
		
		for (int i = 0; i < topResponses.length; i++) {
			topResponses[i] = Coordinates.NO_POINT;
		}
	}
	
	public short[] getMovesWLS() {
		return allMovesWLS;
	}
	
	public int[] getTopResponses() {
		return topResponses;
	}
	
	public short[] getTopResponsesIllegality() {
		return topResponsesIllegality;
	}
	
	/** Increments the illegality counter for a given index in the top list*/
	public void addIllegalPlay(int topIndex) {
		topResponsesIllegality[topIndex]++;
	}
	
	/** Returns the number of times the move at the specified index in the top response list
	 *  has been tried and deemed illegal. */
	public short getIllegality(int topIndex) {
		return topResponsesIllegality[topIndex];
	}
	
	/** Clears the illegality counter for the move at a given index in the top response list*/
	public void clearIllegality(int topIndex) {
		topResponsesIllegality[topIndex] = 0;
	}
	
	public int getTopResponsesLength() {
		return topResponses.length;
	}
	
	public void resizeTopResponses(int newLength) {
		
		topResponses = new int[newLength];
		
		for (int i = 0; i < topResponses.length; i++) {
			topResponses[i] = Coordinates.NO_POINT;
		}
	}
	public State getWLSState(int move) {
		return WinLossStates.getWLS().getState(allMovesWLS[move]);
	}
	
	/** Gets the index of the move in the top response list and returns -1 if
	 * the move does not exist.
	 */
	
	protected int moveIndexInTopResponses(int move) {
		for (int i = 0; i < topResponses.length; i++) {
			if (topResponses[i] == move) return i;
		}
		return -1;
	}
	/** Checks to see if a given move is already in the top responses list*/
	protected boolean inTopResponses(int move) {
		return (moveIndexInTopResponses(move) >= 0);
	}
	/** 
	 * Runs through the top responses list and removes any moves with illegality counters
	 * that exceed the threshold.
	 */
	protected void sweep() {
		for (int i = 0; i < topResponses.length; i++) {
			// TODO: should this be greater than or equal to?
			if (topResponsesIllegality[i] >= WLSPlayer.MAX_ILLEGALITY_CAP) {
				topResponsesIllegality[i] = 0;
				topResponses[i] = Coordinates.NO_POINT;
			}
		}
	}
	
	/** Finds the index of the move in the top k responses list with the lowest
	 * WLS. In the interest of space efficiency we do not cache this value. Hence,
	 * you should avoid calling this method too frequently.
	 *
	 * Also searches for the proposed move to determine if it is already in the list. 
	 * 
	 * TODO: we could combine this with the sweep method? We want to avoid running down
	 * the list.
	 * 
	 * @param proposedMove The move we are considering adding
	 * @return The lowest index (ranked by WLS confidence) or -1 if the move already exists in the list.
	 */
	protected int findLowestWLS(int proposedMove) {
		int minIndex = 0;
		
		for (int i = 0; i < topResponses.length; i++) {
			
			if (proposedMove == topResponses[i]) {
				return -1;
			}
			
			State cur = WinLossStates.getWLS().getState(allMovesWLS[topResponses[i]]);
			State min = WinLossStates.getWLS().getState(allMovesWLS[topResponses[minIndex]]);
			
			if (cur.getConfidence() < min.getConfidence()) {
				minIndex = i;
			}
		}
		
		return minIndex;
	}
	
	public String topResponsesToString() {
		StringBuilder builder = new StringBuilder();
		for (int move : topResponses) {
			double confidence = WinLossStates.getWLS().getState(allMovesWLS[move]).getConfidence();
			builder.append(Coordinates.pointToString(move) + "( " + confidence + " ) ");
		}
		
		return builder.toString();
	}
	
	
	public void addWin(int move) {
		// we increment to the new WLS state after a win (from old WLS state)
		short newStateIndex = (short) WinLossStates.getWLS().addWin((int) allMovesWLS[move]);
		
		// we remove the illegal moves
		sweep();
		
		int worstMoveIndexInTopList = findLowestWLS(move);
		
		if (worstMoveIndexInTopList != -1) {
			// we're not already in the top list so we continue
			int worstStateIndex = allMovesWLS[topResponses[worstMoveIndexInTopList]];
			
			// if we're better than the worst WLS state, replace the lowest element in the top list
			if (WinLossStates.getWLS().getState(newStateIndex).getConfidence() > WinLossStates.getWLS().getState(worstStateIndex).getConfidence()) {
				topResponses[worstMoveIndexInTopList] = move;
			}
		
		}
			
		allMovesWLS[move] = newStateIndex;
	}
	
	public void addLoss(int move) {
		// we increment to the new WLS state after a loss (from old WLS state)
		allMovesWLS[move] = (short) WinLossStates.getWLS().addLoss((int) allMovesWLS[move]);
				
		// remove illegal moves but do not add ourselves
		sweep();
	}
	
}
