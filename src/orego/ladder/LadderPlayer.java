package orego.ladder;

import orego.mcts.*;
import orego.util.IntSet;
import orego.core.*;

/**
 * This player plays out ladders and biases the point that kicks each one off,
 * either for play (if we win) or against play (if we lose).
 */
public class LadderPlayer extends Lgrf2Player {
	/**This runnable is for incorporating runs to the main tree. The board belonging to this is where we play out the ladders **/
	McRunnable runnable;
	/**this records total wins for the ladder this has most recently finished playing out (should be made into a list)**/
	int wins[];
	
	int lengths[];
	
	@Override
	/** Calls playOutLadders to bias the search tree. */	
	public int bestMove() {
		try {
			stopThreads();
			playOutLadders();
 			runThreads();
		} catch (InterruptedException shouldNotHappen) {
			shouldNotHappen.printStackTrace();
			System.exit(1);
		}
		return bestStoredMove();
	}
	/**returns wins for ladder #p. This is essentially the same as getWins() in mctsPlayer
	 *  but allows us to test our ladder bias values once bestMove() has resumed other threads (which would change wins/runs etc.)**/
	public int getWinsFor(int p){
		return wins[p];
	}
	/**returns length of ladder p**/
	public int getLadLengths(int p){
		return lengths[p];
	}
	/** 
	 * Returns the liberties of every ladder where color is on the inside.
	 * This is enough information to play out the ladder
	 * (see {@link #playOutLadders()}playOutLadders()).
	 */
	public IntSet libertiesOfLadders(int color) {
		// our definition of a ladder is: a chain in atari whose
		// liberty has exactly two vacant neighbors
		IntSet chainsInAtari = getBoard().getChainsInAtari(color);
		IntSet libertiesOfLadders = new IntSet (Coordinates.FIRST_POINT_BEYOND_BOARD);
		for (int i = 0; i < chainsInAtari.size(); i++) {
			int liberty = getBoard().getLibertyOfChainInAtari(chainsInAtari.get(i));
			if (getBoard().getVacantNeighborCount(liberty) == 2) {
				libertiesOfLadders.add(liberty);
			}
		}
		return libertiesOfLadders;
	}
	
	/**
	 * Plays out every ladder and biases the search tree.
	 */
	public void playOutLadders() {
		// Each ladder will be played out on runnable and then that whole
		// playout (run) will be incorporated into the search tree.

		// get the ladders
		IntSet ladderLiberties = libertiesOfLadders(Colors.BLACK);
		int numberOfBlackLadders = ladderLiberties.size();
		ladderLiberties.addAll(libertiesOfLadders(Colors.WHITE));
		wins=new int[ladderLiberties.size()];
		lengths=new int[ladderLiberties.size()];
		// each playout will play on a McRunnable, whose board is reset each time
		runnable = new McRunnable(this, null);
		int lNum=0;//keeps track of which ladder for index of array of bias #s
		
		// each playout will play on a McRunnable, whose board is reset each time
		runnable = new McRunnable(this, null);
		
		// play out each ladder (separately)
		for (int i = 0; i < ladderLiberties.size(); i++) {
			
			int ladderLength = 0;
			
			runnable.copyDataFrom(getBoard());
			
			int liberty = ladderLiberties.get(i);
			int insideColor = (i < numberOfBlackLadders) ? Colors.BLACK : Colors.WHITE;
			boolean insideWon;
			
			// start as the inside color
			runnable.getBoard().setColorToPlay(insideColor);
			
			// keep applying the policy of the inside player and the outside player until
			// the ladder is over (either the inside player is free or has been captured)
			while (true) {
				// inside player policy: play in my only liberty
				int insidePlaysHere = liberty;
				runnable.acceptMove(insidePlaysHere);
				if (runnable.getBoard().getLibertyCount(insidePlaysHere) >= 3) {
					// inside player is free
					insideWon = true;
					break;
				}
				
				// outside player policy: play in the inside player's liberty
				// with the most vacant neighbors
				IntSet insidesLiberties = runnable.getBoard().getLiberties(insidePlaysHere);
				int mostVacantNeighbors = -1;
				int pointWithMostVacantNeighbors = Coordinates.FIRST_POINT_BEYOND_BOARD;
				
				for (int j = 0; j < insidesLiberties.size(); j++) {
					if (runnable.getBoard().getVacantNeighborCount(insidesLiberties.get(j)) > mostVacantNeighbors) {
						pointWithMostVacantNeighbors = insidesLiberties.get(j);
						mostVacantNeighbors = runnable.getBoard().getVacantNeighborCount(pointWithMostVacantNeighbors);
					}
				}
				
				runnable.acceptMove(pointWithMostVacantNeighbors);
				if (runnable.getBoard().getColor(insidePlaysHere) != insideColor) {
					// inside player was captured
					insideWon = false;
					break;
				}
				
				ladderLength++;

				// TODO Add code here looking for outside stones in atari.
			}
			
			// bias the search tree: call this playout for the winner.
			// longer ladders are biased more because the stakes are higher.
			int winner = insideWon ? insideColor : Colors.opposite(insideColor); 

			//System.err.println("ladder "+lNum+" Length="+ladderLength);
			for (int j = 0; j < ladderLength*10; j++) {
				incorporateRun(winner, runnable);
				lengths[lNum]=ladderLength;
			}
			wins[lNum]=getRoot().getWins(Coordinates.at("F13"));
//			System.out.println("wins at F13:"+getRoot().getWins(Coordinates.at("F13")));
//			System.out.println("wins array: "+wins[lNum]);
//			System.out.println("lad length="+lengths[lNum]);
			lNum++;

			for (int j = 0; j < ladderLength*10; j++) {
				incorporateRun(winner, runnable);
			}

		}
	}		
}
