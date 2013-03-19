package orego.ladder;

import orego.mcts.*;
import orego.util.IntSet;
import orego.core.*;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;

/**
 * This player plays out all the ladders on the board to find out who wins
 * and biases each position for the winner.
 */
public class LadderPlayer extends Lgrf2Player {

	/**
	 * The McRunnable used for incorporating ladder playouts into the search tree.
	 * (We play out ladders on runnable.getBoard()).
	 */
	McRunnable runnable;

//	/**this records total wins for the ladder this has most recently finished playing out (should be made into a list)**/
//	int wins[];
//	int lengths[];
	
	@Override
	/** Returns the best move, first biasing the search tree with the results of ladder playouts. */
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

//	/**returns wins for ladder #p. This is essentially the same as getWins() in mctsPlayer
//	 *  but allows us to test our ladder bias values once bestMove() has resumed other threads (which would change wins/runs etc.)**/
//	public int getWinsFor(int p){
//		return wins[p];
//	}
//	/**returns length of ladder p**/
//	public int getLadLengths(int p){
//		return lengths[p];
//	}
	
	/** 
	 * Returns the liberties of every ladder where color is on the inside.
	 * This is enough information to play out the ladder
	 * (see {@link #playOutLadders()}).
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
	 * Plays out every ladder and biases the search tree for each.
	 */
	public void playOutLadders() {
		// Each ladder will be played out on runnable and then that entire
		// playout (run) will be incorporated into the search tree.
		
		runnable = new McRunnable(this, null); // ladders are played out here
		
		// get the ladders
		IntSet ladderLiberties = libertiesOfLadders(BLACK);
		int numberOfBlackLadders = ladderLiberties.size();
		ladderLiberties.addAll(libertiesOfLadders(WHITE));
		
		// play out each ladder separately
		for (int i = 0; i < ladderLiberties.size(); i++) {
			runnable.copyDataFrom(getBoard());
			int insidePlaysHere = ladderLiberties.get(i);
			int insideColor = (i < numberOfBlackLadders) ? BLACK : WHITE;
			int outsideColor = opposite(insideColor);
			int winner;
			int length = 0; // "length" of this ladder
			boolean neighborAtari=false;   // test to see if neighboring stones are in atari
			runnable.getBoard().setColorToPlay(insideColor);
			
			// keep applying the policy of the inside player and the outside player until
			// the ladder is over (either the inside player is free or has been captured)
			while (true) {
				// inside player policy: play in my only liberty
				// Must check if move is legal first (not suicide)
				
				
				if(runnable.getBoard().isLegal(insidePlaysHere)){
					runnable.acceptMove(insidePlaysHere);
				}else{
					winner=outsideColor;
					break;
				}
						
				if (runnable.getBoard().getLibertyCount(insidePlaysHere) >= 3) {
					// inside player is free
					winner = insideColor;
					break;
				}
				
				// outside player policy: play in the inside player's
				// liberty with the most vacant neighbors
				IntSet insideLiberties = runnable.getBoard().getLiberties(insidePlaysHere);
				int mostVacantNeighbors = -1;
				int pointToPlay = FIRST_POINT_BEYOND_BOARD;
				for (int j = 0; j < insideLiberties.size(); j++) {
					int lib = insideLiberties.get(j);
					int vacantNeighborCount = runnable.getBoard().getVacantNeighborCount(lib);
					if (vacantNeighborCount > mostVacantNeighbors) {
						mostVacantNeighbors = vacantNeighborCount;
						pointToPlay = lib;
					}
				}
				if(runnable.getBoard().isLegal(pointToPlay)){
					runnable.acceptMove(pointToPlay);
				}else{
					winner=insideColor;
					break;
				}
				
				if (runnable.getBoard().getColor(insidePlaysHere) != insideColor) {
					// inside player was captured
					winner = outsideColor;
					break;
				}
				
				
				// for loop looks through each of the neighbors for the newly placed inside stone
				// checks each of these neighbors against the list of stones in atari and checks if they belong to the opposite color
				// if an outside stone is in atari it sets winner to inside color and breaks.
				
				for (int x = 0; x < 4; x++) {
				  	int n = Coordinates.NEIGHBORS[insidePlaysHere][x];
				  	if(Coordinates.ON_BOARD[n]){
				  		if(runnable.getBoard().isInAtari(runnable.getBoard().getChainId(n)) && runnable.getBoard().getColor(n)==outsideColor){
				  			neighborAtari=true;
				  		}
				  	}
				 }
				if(neighborAtari){
					winner=insideColor;
					break;
				}
				
				insidePlaysHere = runnable.getBoard().getLiberties(insidePlaysHere).get(0);
				length++;
			}
			
			// bias the search tree: call this playout for the winner.
			// 100 is used as the number of runs incorporated, this needs to be tuned.
			for (int j = 0; j < 1000; j++) {
				
				incorporateRun(winner, runnable);
			}
		}
	}
}

//		wins=new int[ladderLiberties.size()];
//		lengths=new int[ladderLiberties.size()];
//		int lNum=0;//keeps track of which ladder for index of array of bias #s
//		
//				
//				ladderLength++;
//
//			for (int j = 0; j < ladderLength*10; j++) {
//				incorporateRun(winner, runnable);
//				lengths[lNum]=ladderLength;
//			}
//			wins[lNum]=getRoot().getWins(Coordinates.at("F13"));
//			lNum++;
//
