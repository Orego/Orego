package orego.ladder;

import orego.mcts.*;
import orego.play.UnknownPropertyException;
import orego.util.IntList;
import orego.util.IntSet;
import orego.core.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;

/**
 * This player plays out all the ladders on the board to find out who wins and
 * biases each position for the winner.
 */
public class LadderPlayer extends TimePlayer {

	/**
	 * The McRunnable used for incorporating ladder playouts into the search
	 * tree. (We play out ladders on runnable.getBoard()).
	 */
	private int ladderBias = 100;
	private boolean ladderMult = false;

	private int[] biased = new int[getFirstPointBeyondBoard()];

	// /**this records total wins for the ladder this has most recently finished
	// playing out (should be made into a list)**/
	// int wins[];
	// int lengths[];

	@Override
	public void beforeStartingThreads() {
		playOutLadders();
		super.beforeStartingThreads();
	}

	// Commit test

	// /**returns wins for ladder #p. This is essentially the same as getWins()
	// in mctsPlayer
	// * but allows us to test our ladder bias values once bestMove() has
	// resumed other threads (which would change wins/runs etc.)**/
	// public int getWinsFor(int p){
	// return wins[p];
	// }
	// /**returns length of ladder p**/
	// public int getLadLengths(int p){
	// return lengths[p];
	// }

	/**
	 * Returns the liberties of every ladder where color is on the inside. This
	 * is enough information to play out the ladder (see
	 * {@link #playOutLadders()}).
	 */
	public IntSet libertiesOfLadders(int color) {
		// our definition of a ladder is: a chain in atari whose
		// liberty has exactly two vacant neighbors
		IntSet chainsInAtari = getBoard().getChainsInAtari(color);
		IntSet libertiesOfLadders = new IntSet(getFirstPointBeyondBoard());
		for (int i = 0; i < chainsInAtari.size(); i++) {
			int liberty = getBoard().getLibertyOfChainInAtari(
					chainsInAtari.get(i));
			if (getBoard().getVacantNeighborCount(liberty) == 2) {
				libertiesOfLadders.add(liberty);
			}
		}
		return libertiesOfLadders;
	}
	
	public IntSet twoLibertyGroups(int color) {
		IntSet libertiesOfTwoLadders = new IntSet(getFirstPointBeyondBoard());
		for (int i = 0; i < Coordinates.getBoardArea(); i++) {
			if (getBoard().getColor(i)==color) {
				if (getBoard().getChainId(i) == i) {
					if (getBoard().getLibertyCount(i) == 2) {
						System.err.println("ADDING");
						libertiesOfTwoLadders.add(i);
					}
				}
			}
		}
		return libertiesOfTwoLadders;
	}

	/**
	 * Plays out every ladder and biases the search tree for each.
	 */
	public void playOutLadders() {
		// Each ladder will be played out on runnable and then that entire
		// playout (run) will be incorporated into the search tree.
		// get the ladders
		for (int i = 0; i < biased.length; i++) {
			biased[i] = 0;
		}
		IntSet ladderLiberties = libertiesOfLadders(BLACK);
		int numberOfBlackLadders = ladderLiberties.size();
		ladderLiberties.addAll(libertiesOfLadders(WHITE));
		IntSet twoLibertyLadders = twoLibertyGroups(BLACK);
		int numberOfBlackTwoLibertyLadders = twoLibertyLadders.size();
		twoLibertyLadders.addAll(twoLibertyGroups(WHITE));
		boolean twoLiberties=false;
		
		SearchNode root = getRoot();
		// play out each ladder separately
		Board ourCopy = new Board();
		for (int i = 0; i < ladderLiberties.size() + twoLibertyLadders.size(); i++) {
			
			ourCopy.copyDataFrom(getBoard());
			
			int firstMove = 0;
			int insidePlaysHere=0;
			int insideColor;
			if(i>=ladderLiberties.size()){
				twoLiberties=true;
				
				IntSet twoLiberty = getBoard().getLiberties(twoLibertyLadders.get(i-ladderLiberties.size()));
				int lib1= getBoard().getVacantNeighborCount(twoLiberty.get(0));
				int lib2= getBoard().getVacantNeighborCount(twoLiberty.get(1));
				if(lib1>lib2){
					firstMove=twoLiberty.get(0);
					if(ourCopy.isLegal(twoLiberty.get(0))){
						ourCopy.play(twoLiberty.get(0));
					}
					insidePlaysHere=twoLiberty.get(1);
				} else{
					firstMove=twoLiberty.get(1);
					if(ourCopy.isLegal(twoLiberty.get(1))){
						ourCopy.play(twoLiberty.get(1));
					}
					insidePlaysHere=twoLiberty.get(0);
				}
				insideColor=ourCopy.getColor(twoLibertyLadders.get(i-ladderLiberties.size()));
				
			} else{
				insidePlaysHere = ladderLiberties.get(i);
				insideColor = (i < numberOfBlackLadders) ? BLACK : WHITE;
			}
			// recordRunnable.copyDataFrom(getBoard());
			
			
			int outsideColor = opposite(insideColor);
			int winner;
			int length = 0; // "length" of this ladder
			boolean neighborAtari = false; // test to see if neighboring stones
											// are in atari
			
			ourCopy.setColorToPlay(insideColor);

			int stone = insidePlaysHere;
			do {
				for (int j = 0; j < 4; j++) {
					int neighbor = getNeighbors(stone)[j];
					if (ourCopy.getColor(neighbor) == ourCopy.getColor(stone)) {
						if ((ourCopy.getColor(neighbor) == WHITE)
								|| ourCopy.getColor(neighbor) == BLACK) {
							if (ourCopy.isInAtari(neighbor)) {
								neighborAtari = true;
							}
						}
					}
				}
				stone = ourCopy.getChainNextPoint(stone);
				System.err.println(stone);
			} while (stone != insidePlaysHere);
			// runnable.getBoard().setColorToPlay(insideColor);
			// recordRunnable.getBoard().setColorToPlay(insideColor);

			// keep applying the policy of the inside player and the outside
			// player until
			// the ladder is over (either the inside player is free or has been
			// captured)
			if(!neighborAtari){
			while (true) {

				// inside player policy: play in my only liberty
				// Must check if move is legal first (not suicide)

				// if(runnable.getBoard().isLegal(insidePlaysHere)){
				// runnable.acceptMove(insidePlaysHere);
				// }
				if (ourCopy.isLegal(insidePlaysHere)) {
					ourCopy.play(insidePlaysHere);
				} else {
					winner = outsideColor;
					break;
				}

				if (ourCopy.getLibertyCount(insidePlaysHere) >= 3) {
					// if (runnable.getBoard().getLibertyCount(insidePlaysHere)
					// >= 3) {
					// inside player is free
					winner = insideColor;
					break;
				}

				// outside player policy: play in the inside player's
				// liberty with the most vacant neighbors
				// IntSet insideLiberties =
				// runnable.getBoard().getLiberties(insidePlaysHere);
				IntSet insideLiberties = ourCopy.getLiberties(insidePlaysHere);
				int mostVacantNeighbors = -1;
				int pointToPlay = getFirstPointOnBoard();
				for (int j = 0; j < insideLiberties.size(); j++) {
					int lib = insideLiberties.get(j);
					// int vacantNeighborCount =
					// runnable.getBoard().getVacantNeighborCount(lib);
					int vacantNeighborCount = ourCopy
							.getVacantNeighborCount(lib);
					if (vacantNeighborCount > mostVacantNeighbors) {
						mostVacantNeighbors = vacantNeighborCount;
						pointToPlay = lib;
					}
				}
				// if(runnable.getBoard().isLegal(pointToPlay)){
				// runnable.acceptMove(pointToPlay);
				// }
				if (ourCopy.isLegal(pointToPlay)) {
					ourCopy.play(pointToPlay);
				} else {
					winner = insideColor;
					break;
				}

				// if (runnable.getBoard().getColor(insidePlaysHere) !=
				// insideColor) {
				if (ourCopy.getColor(insidePlaysHere) != insideColor) {
					// inside player was captured
					winner = outsideColor;
					break;
				}

				// for loop looks through each of the neighbors for the newly
				// placed inside stone
				// checks each of these neighbors against the list of stones in
				// atari and checks if they belong to the opposite color
				// if an outside stone is in atari it sets winner to inside
				// color and breaks.

				for (int x = 0; x < 4; x++) {
					int n = getNeighbors(insidePlaysHere)[x];
					if (isOnBoard(n)) {

						// if(runnable.getBoard().isInAtari(runnable.getBoard().getChainId(n))
						// && runnable.getBoard().getColor(n)==outsideColor){
						// neighborAtari=true;
						// }
						if (ourCopy.isInAtari(ourCopy.getChainId(n))
								&& ourCopy.getColor(n) == outsideColor) {
							neighborAtari = true;
						}
					}
				}
				if (neighborAtari) {
					winner = insideColor;
					break;
				}

				// insidePlaysHere =
				// runnable.getBoard().getLiberties(insidePlaysHere).get(0);
				insidePlaysHere = ourCopy.getLiberties(insidePlaysHere).get(0);
				length++;

			}
			}else{
				winner=insideColor;
			}
			// bias the search tree: call this playout for the winner.
			// 100 is used as the number of runs incorporated, this needs to be
			// tuned.
			
			if(twoLiberties){
				if (winner == insideColor) {
					
				} else if (winner == outsideColor
						&& outsideColor == getBoard().getColorToPlay()) {
					System.err.println("bye");
					biased[firstMove] += ladderMult ? length
							* ladderBias : ladderBias;
					root.addWins(firstMove, ladderMult ? length
							* ladderBias : ladderBias);
				}
			} else{
			if (winner == insideColor) {
				biased[ladderLiberties.get(i)] += ladderMult ? length
						* ladderBias : ladderBias;
				root.addWins(ladderLiberties.get(i), ladderMult ? length
						* ladderBias : ladderBias);
			} else if (winner == outsideColor
					&& insideColor == getBoard().getColorToPlay()) {
				biased[ladderLiberties.get(i)] += ladderMult ? -length
						* ladderBias : -ladderBias;
				root.addLosses(ladderLiberties.get(i), ladderMult ? length
						* ladderBias : ladderBias);
			}
			// for (int j = 0; j < (ladderMult? length*ladderBias: ladderBias);
			// j++) {
			// incorporateRun(winner, recordRunnable);
			// }
		}
		}
	}

	/**
	 * Returns the amount a node was biased. For testing purposes
	 */
	public int getBias(int i) {
		return biased[i];
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("ladderBias")) {
			setLadderBias(Integer.parseInt(value));
		} else if (property.equals("ladderMult")) {
			assert value.equals("true");
			ladderMult = true;
		} else {
			super.setProperty(property, value);
		}
	}

	private void setLadderBias(int bias) {
		assert bias >= 0 : "Cannot have a negative bias";
		ladderBias = bias;
	}
}

// wins=new int[ladderLiberties.size()];
// lengths=new int[ladderLiberties.size()];
// int lNum=0;//keeps track of which ladder for index of array of bias #s
//
//
// ladderLength++;
//
// for (int j = 0; j < ladderLength*10; j++) {
// incorporateRun(winner, runnable);
// lengths[lNum]=ladderLength;
// }
// wins[lNum]=getRoot().getWins(Coordinates.at("F13"));
// lNum++;
//
