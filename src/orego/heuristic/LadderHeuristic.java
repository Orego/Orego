package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.getFirstPointBeyondBoard;
import static orego.core.Coordinates.getFirstPointOnBoard;
import static orego.core.Coordinates.getNeighbors;
import static orego.core.Coordinates.isOnBoard;
import orego.core.Board;
import orego.core.Coordinates;
import orego.mcts.SearchNode;
import orego.util.IntSet;

public class LadderHeuristic extends Heuristic {
	public LadderHeuristic(int weight) {
		super(weight);
	}

	private int[] biased = new int[getFirstPointBeyondBoard()];

	@Override
	public void prepare(Board board, boolean local) {
		super.prepare(board, local);
		if (!local) {
			IntSet suggest = playOutLadders(board);
			for (int i = 0; i < suggest.size(); i++) {
				System.err.println("Size is " + suggest.size());
				System.err.println(i);
				recommend(suggest.get(i));
			}
		}
	}

	@Override
	public LadderHeuristic clone() {
		return (LadderHeuristic) super.clone();
	}

	public IntSet libertiesOfLadders(int color, Board board) {
		// our definition of a ladder is: a chain in atari whose
		// liberty has exactly two vacant neighbors
		IntSet chainsInAtari = board.getChainsInAtari(color);
		IntSet libertiesOfLadders = new IntSet(getFirstPointBeyondBoard());
		for (int i = 0; i < chainsInAtari.size(); i++) {
			libertiesOfLadders.add(chainsInAtari.get(i));
		}
		// for (int i = 0; i < chainsInAtari.size(); i++) {
		// int liberty = getBoard().getLibertyOfChainInAtari(
		// chainsInAtari.get(i));
		// if (getBoard().getVacantNeighborCount(liberty) == 2) {
		// libertiesOfLadders.add(liberty);
		// }
		// }
		// return libertiesOfLadders;

		return libertiesOfLadders;
	}

	public IntSet twoLibertyGroups(int color, Board board) {
		IntSet libertiesOfTwoLadders = new IntSet(getFirstPointBeyondBoard());
		for (int i = 0; i < Coordinates.getBoardArea(); i++) {
			if (board.getColor(i) == color) {
				if (board.getChainId(i) == i) {
					if (board.getLibertyCount(i) == 2) {
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
	public IntSet playOutLadders(Board board) {
		IntSet suggest = new IntSet(getFirstPointBeyondBoard());
		// Each ladder will be played out on runnable and then that entire
		// playout (run) will be incorporated into the search tree.
		// get the ladders
		for (int i = 0; i < biased.length; i++) {
			biased[i] = 0;
		}
		IntSet ladderLiberties = libertiesOfLadders(BLACK, board);
		int numberOfBlackLadders = ladderLiberties.size();
		ladderLiberties.addAll(libertiesOfLadders(WHITE, board));
		IntSet twoLibertyLadders = twoLibertyGroups(BLACK, board);
		twoLibertyLadders.addAll(twoLibertyGroups(WHITE, board));
		boolean twoLiberties = false;

		// play out each ladder separately
		Board ourCopy = new Board();
		for (int i = 0; i < ladderLiberties.size() + twoLibertyLadders.size(); i++) {

			ourCopy.copyDataFrom(board);

			int firstMove = 0;
			int insidePlaysHere = 0;
			int insideColor;
			if (i >= ladderLiberties.size()) {
				twoLiberties = true;

				IntSet twoLiberty = board.getLiberties(twoLibertyLadders.get(i
						- ladderLiberties.size()));
				int lib1 = board.getVacantNeighborCount(twoLiberty.get(0));
				int lib2 = board.getVacantNeighborCount(twoLiberty.get(1));
				if (lib1 > lib2) {
					firstMove = twoLiberty.get(0);
					if (ourCopy.isLegal(twoLiberty.get(0))) {
						ourCopy.play(twoLiberty.get(0));
					}
					insidePlaysHere = twoLiberty.get(1);
				} else {
					firstMove = twoLiberty.get(1);
					if (ourCopy.isLegal(twoLiberty.get(1))) {
						ourCopy.play(twoLiberty.get(1));
					}
					insidePlaysHere = twoLiberty.get(0);
				}
				insideColor = ourCopy.getColor(twoLibertyLadders.get(i
						- ladderLiberties.size()));

			} else {
				insidePlaysHere = board.getLiberties(ladderLiberties.get(i))
						.get(0);
				insideColor = (i < numberOfBlackLadders) ? BLACK : WHITE;
			}
			// recordRunnable.copyDataFrom(getBoard());

			int outsideColor = opposite(insideColor);
			int winner;
			int length = 0; // "length" of this ladder
			boolean neighborAtari = false; // test to see if neighboring stones
											// are in atari

			ourCopy.setColorToPlay(insideColor);
			int stone;
			int firstStone;
			if (i >= ladderLiberties.size()) {
				stone = twoLibertyLadders.get(i - ladderLiberties.size());
				firstStone = twoLibertyLadders.get(i - ladderLiberties.size());
			} else {
				stone = ladderLiberties.get(i);
				firstStone = ladderLiberties.get(i);
			}
			do {
				for (int j = 0; j < 4; j++) {
					int neighbor = getNeighbors(stone)[j];
					if (ourCopy.getColor(neighbor) != ourCopy.getColor(stone)) {
						if ((ourCopy.getColor(neighbor) == WHITE)
								|| ourCopy.getColor(neighbor) == BLACK) {
							if (ourCopy.isInAtari(ourCopy.getChainId(neighbor))) {
								neighborAtari = true;
								break;
							}
						}
					}
				}
				if (neighborAtari) {
					break;
				}
				stone = ourCopy.getChainNextPoint(stone);
			} while (stone != firstStone);
			// runnable.getBoard().setColorToPlay(insideColor);
			// recordRunnable.getBoard().setColorToPlay(insideColor);

			// keep applying the policy of the inside player and the outside
			// player until
			// the ladder is over (either the inside player is free or has been
			// captured)
			if (!neighborAtari) {
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
						// if
						// (runnable.getBoard().getLibertyCount(insidePlaysHere)
						// >= 3) {
						// inside player is free
						winner = insideColor;
						break;
					}

					// outside player policy: play in the inside player's
					// liberty with the most vacant neighbors
					// IntSet insideLiberties =
					// runnable.getBoard().getLiberties(insidePlaysHere);
					IntSet insideLiberties = ourCopy
							.getLiberties(insidePlaysHere);
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

					// for loop looks through each of the neighbors for the
					// newly
					// placed inside stone
					// checks each of these neighbors against the list of stones
					// in
					// atari and checks if they belong to the opposite color
					// if an outside stone is in atari it sets winner to inside
					// color and breaks.

					for (int x = 0; x < 4; x++) {
						int n = getNeighbors(insidePlaysHere)[x];
						if (isOnBoard(n)) {

							// if(runnable.getBoard().isInAtari(runnable.getBoard().getChainId(n))
							// &&
							// runnable.getBoard().getColor(n)==outsideColor){
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
					insidePlaysHere = ourCopy.getLiberties(insidePlaysHere)
							.get(0);
					length++;

				}
			} else {
				winner = insideColor;
			}
			// bias the search tree: call this playout for the winner.
			// 100 is used as the number of runs incorporated, this needs to be
			// tuned.

			if (twoLiberties) {
				if (winner == insideColor) {
					// In this case, we would negatively bias, but we don't want
					// to do that, so do nothing.

				} else if (winner == outsideColor
						&& outsideColor == board.getColorToPlay()) {
					// biased[firstMove] += ladderMult ? length * ladderBias
					// : ladderBias;
					// root.addWins(firstMove, ladderMult ? length * ladderBias
					// : ladderBias);
					if(!suggest.contains(firstMove)){
					suggest.add(firstMove);
					}
				}
			} else {
				if (winner == insideColor) {
					// biased[getBoard().getLiberties(ladderLiberties.get(i)).get(
					// 0)] += ladderMult ? length * ladderBias
					// : ladderBias;
					// root.addWins(getBoard()
					// .getLiberties(ladderLiberties.get(i)).get(0),
					// ladderMult ? length * ladderBias : ladderBias);
					if(!suggest.contains(board.getLiberties(ladderLiberties.get(i)).get(0))){
					suggest.add(board.getLiberties(ladderLiberties.get(i)).get(
							0));
					}
				} else if (winner == outsideColor
						&& insideColor == board.getColorToPlay()) {
					// Don't negatively bias, only positively.
					// biased[getBoard().getLiberties(ladderLiberties.get(i)).get(
					// 0)] += ladderMult ? -length * ladderBias
					// : -ladderBias;
					// root.addLosses(
					// getBoard().getLiberties(ladderLiberties.get(i))
					// .get(0), ladderMult ? length * ladderBias
					// : ladderBias);
				}
			}
		}
		return suggest;
	}

}
