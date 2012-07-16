package orego.mcts;

import static orego.core.Coordinates.NO_POINT;

import java.util.HashMap;

import ec.util.MersenneTwisterFast;

import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;
import orego.play.UnknownPropertyException;
import orego.response.AbstractResponseList;
import orego.response.ResponsePlayer;
import orego.util.IntSet;

/**
 * Defines the distinction between our known "search tree"
 * and playout realm. We only store level two moves.
 * @author nsylvester
 *
 */
public class MctsResponsePlayer extends MctsPlayer {
	
	protected ResponseTable responses;
	
	public MctsResponsePlayer() {
		responses = new ResponseTable(0, null);
		
		// make the root nodes (possible responses for first root node play)
		responses.addTwoMoveSequence(Coordinates.NO_POINT, 
									 Coordinates.NO_POINT, 
									 Colors.WHITE, 0); // zero hash
		
		responses.addTwoMoveSequence(Coordinates.NO_POINT, 
									 Coordinates.NO_POINT, Colors.BLACK, 0); // zero hash
		
		// tune down those priors weight!
		try {
			setProperty("priors", "5");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public int bestSearchMove(SearchNode node, Board board,
			MersenneTwisterFast random) {
		int result = node.getWinningMove();
		if ((result != NO_POINT) && board.isLegal(result)) {
			// The isLegal() check is necessary to avoid superko violations
			return result;
		}
		IntSet vacantPoints = board.getVacantPoints();
		int start = random.nextInt(vacantPoints.size());
		int i = start;
		double bestValue = AbstractResponseList.PASS_WINS_PRIOR
				/ (double) AbstractResponseList.PASS_RUNS_PRIOR;
		int bestMove = Coordinates.PASS;
		do {
			int move = vacantPoints.get(i);
			double searchValue = getWinRate(move);
			if (searchValue > bestValue) {
				if (board.isFeasible(move) && board.isLegal(move)) {
					bestValue = searchValue;
					bestMove = move;
				} else {
					node.exclude(move);
				}
			}
			i = (i + 457) % vacantPoints.size();
		} while (i != start);
		return bestMove;

	}
	
	
	/**
	 * Pick our way gingerly down the tree until we reach
	 * the "frontier" then return and let the McRunnable
	 * finish the playout. This method is called from
	 * {@link McRunnable#performMcRun()}.
	 */
	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		SearchNode curResponseList = getRoot();
		
		
		runnable.getBoard().copyDataFrom(getBoard());
		while (runnable.getBoard().getPasses() < 2) {
			// pick a best node from list of root responses
			// and *play* it on the board copy
			int colorToPlay = runnable.getBoard().getColorToPlay(); // need to get *before* we play the move
			int p = selectAndPlayMove(curResponseList, runnable);
			
			int turn 		 = runnable.getBoard().getTurn();
			int prevMove 	 = runnable.getBoard().getMove(turn - 1);
			int prevPrevMove = runnable.getBoard().getMove(turn - 2);
			
			SearchNode responseList;
			synchronized (responses) {
				// see if we have an existing response list. if we do, simply descend
				responseList = responses.findIfPresent(ResponsePlayer.levelTwoEncodedIndex(
																				 prevPrevMove, 
																				 prevMove,
																				 colorToPlay));

				// A response list will only be created if we expect the node to be visited again.
				// We create the child only if it does not already exist 
				if ( ! curResponseList.hasChild(p)) {
					responseList = 		// add to the level two table
									  responses.findOrAllocate(ResponsePlayer.levelTwoEncodedIndex(prevPrevMove, 
											  													   prevMove, 
											  													   colorToPlay));
					
					// TODO: should this be .levelTwoEncodedIndex?
					responseList.reset(runnable.getBoard().getHash());
					
					// for quick lookup later
					curResponseList.setHasChild(p);
					
					
					// have we had more than default number of runs? (of course)
					if (curResponseList.isFresh()) { 
						runnable.getPolicy().updatePriors(responseList,
														  runnable.getBoard(), 
														  getPriors());
					}
					return;
				}
			}
			if (responseList == null) {
				return; // No child
			}
			
			// descend if we already have a response list
			curResponseList = responseList;
		}
	}
	
	/**
	 * Called from the {@link MctsPlayer#bestStoredMove()}. 
	 * We override this method instead of bestStoredMove since
	 * the logic is equivalent.
	 */
	@Override
	public SearchNode getRoot() {
		// return the SearchNode 
		// for the previous two moves
		// of the *ending* state of the game
		Board board = getBoard();
		int turn = board.getTurn();
		int prevPrevMove = board.getMove(turn - 2);
		int prevMove     = board.getMove(turn - 1);
		
		// return the best resposne to the previous two moves at the current
		// *real* board state
		SearchNode node =  responses.findIfPresent(ResponsePlayer.levelTwoEncodedIndex(prevPrevMove, prevMove, board.getColorToPlay()));
		
		if (node == null) throw new RuntimeException("Null root node (turn: " + turn + " prevPrevMove: " + prevPrevMove + " prevMove: " + prevMove);
		
		return node;
	}
	
	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		int toPlay = getBoard().getColorToPlay(); // current color
		SearchNode node = getRoot();
		boolean win = winner == getBoard().getColorToPlay();
		int turn = runnable.getTurn();
		int[] moves = runnable.getMoves();
		// move through all moves after we do a playout 
		// we start at our current state and move to the state of the runnable.
		// If we cannot find a matching entry for a two move sequence,
		// we have hit the bottom of the known tree so we break.
		for(int t = getBoard().getTurn(); t < runnable.getBoard().getTurn(); t++) {
			// record wins/losses for current state (at least once)
			node.recordPlayout(win, moves, t, turn, runnable.getPlayedPoints());
			
			// flip to other player (for next move)
			toPlay = 1 - toPlay;
					
			// last move played
			int curMove 		= runnable.getBoard().getMove(t);
			
			// previous to last move played
			int prevMove 		= runnable.getBoard().getMove(t - 1);
			
			// we now move ahead to the next node (t + 1)
			// do we have another node?
			node = responses.findIfPresent(ResponsePlayer.levelTwoEncodedIndex(prevMove, 
																			   curMove, 
																			   toPlay));
			if (node == null) {
				break; // we have reached the edge of the tree
			}
			
			
		}
	}
	
	@Override
	public TranspositionTable getTable() {
		return responses;
	}
}
