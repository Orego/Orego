package orego.ladder;

import orego.mcts.*;
import orego.util.IntSet;
import orego.core.*;

// CHANGE (2)

/**
 * This player plays out ladders and biases the point that kicks each one off,
 * either for play (if we win) or against play (if we lose).
 */
public class LadderPlayer extends Lgrf2Player {

	/** Copy of the board for playing out ladders. */
	private Board ladBoard;
	
	McRunnable runnable;

	public LadderPlayer() {
		 ladBoard = new Board();
	}
	
	@Override
	/** calls playOutLadders after stopping threads to bias stored moves*/	
	public int bestMove(){
		try {
			System.err.println(getRoot().getTotalRuns());
			System.err.println(getRoot().getTotalRuns());
			stopThreads();
			ladBoard = new Board();
			ladBoard.copyDataFrom(getBoard());
			playOutLadders();
			runThreads();
		} catch (InterruptedException shouldNotHappen) {
			shouldNotHappen.printStackTrace();
			System.exit(1);
		}
		return bestStoredMove();
	}

	/** 
	 * Returns the liberties of every ladder where color is on the inside.
	 */
	public IntSet libertiesOfLadders(int color) {
		// our definition of a ladder is: a chain in atari whose
		// liberty has exactly two vacant neighbors
		IntSet chainsInAtari = ladBoard.getChainsInAtari(color);
		IntSet libertiesOfLadders = new IntSet (Coordinates.FIRST_POINT_BEYOND_BOARD);
		for (int i = 0; i < chainsInAtari.size(); i++) {
			int liberty = ladBoard.getLibertyOfChainInAtari(chainsInAtari.get(i));
			if (ladBoard.getVacantNeighborCount(liberty) == 2) {
				libertiesOfLadders.add(liberty);
			}
		}
		return libertiesOfLadders;
	}
	
	/**
	 * Plays out every ladder and biases the search tree.
	 */
	public void playOutLadders() {

		runnable = new McRunnable(this, null);
		
		// get the ladders
		IntSet ladderLiberties = libertiesOfLadders(Colors.BLACK);
		int numberOfBlackLadders = ladderLiberties.size();
		ladderLiberties.addAll(libertiesOfLadders(Colors.WHITE));
		
		// play out each ladder separately
		for (int i = 0; i < ladderLiberties.size(); i++) {
			
			// we'll be moving, so we use a local copy of the board
			ladBoard.copyDataFrom(getBoard());
			runnable.copyDataFrom(getBoard());
			
			int liberty = ladderLiberties.get(i);
			int insideColor = (i < numberOfBlackLadders) ? Colors.BLACK : Colors.WHITE;
			boolean insideWon;
			
			// start as the inside color
			ladBoard.setColorToPlay(insideColor);
			
			// keep applying the policy of the inside player and the outside player until
			// the ladder is over (either the inside color is free or has been captured)
			while (true) {
				// inside player policy: play in my only liberty
				int insidePlaysHere = liberty;
				ladBoard.play(insidePlaysHere);
				runnable.acceptMove(insidePlaysHere);
				if (ladBoard.getLibertyCount(insidePlaysHere) >= 3)	{
					// inside player is free
					insideWon = true;
					break;
				}
				
				// outside player policy: play in the inside player's liberty
				// with the most vacant neighbors
				IntSet libsOfNewChain = ladBoard.getLiberties(insidePlaysHere);
				int mostVacantNeighbors = -1;
				int pointWithMostVacantNeighbors = Coordinates.FIRST_POINT_BEYOND_BOARD;
				
				for (int j = 0; j < libsOfNewChain.size(); j++)	{
					if (ladBoard.getVacantNeighborCount(libsOfNewChain.get(j)) > mostVacantNeighbors) {
						pointWithMostVacantNeighbors = libsOfNewChain.get(j);
						mostVacantNeighbors = ladBoard.getVacantNeighborCount(pointWithMostVacantNeighbors);
					}
				}
				ladBoard.play(pointWithMostVacantNeighbors);
				runnable.acceptMove(pointWithMostVacantNeighbors);
				if (ladBoard.getColor(insidePlaysHere) != insideColor) {
					// inside player was captured
					insideWon = false;
					break;
				}
				
				// add code here looking for outside stones in atari
				
			}
			
			// bias the search tree appropriately:
			// - if inside player wins, he wants to play here and
			//   *so does outside player* (to cut off the ladder)
			// - if inside player loses, he doesn't want to play here and 
			//   *neither does outside player* (to allow inside player to do so)
			if (insideWon) {
				for (int j = 0; j < 500; j++) {
					incorporateRun(insideColor, runnable);
				}
				//getRoot().addWins(liberty, 10);
				System.err.println("Biasing in favor of " + Coordinates.pointToString(liberty));
			} else {
				for (int j = 0; j < 500; j++) {
					incorporateRun(Colors.opposite(insideColor), runnable);
				}
				//getRoot().addLosses(liberty, 10);
				System.err.println("Biasing against " + Coordinates.pointToString(liberty));

			}
			
			System.err.println(ladBoard);
		}
	}		
}
