package orego.policy;

import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;
import orego.mcts.SearchNode;
import ec.util.MersenneTwisterFast;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import orego.core.NeighborCounts.*; 
public class NakadePolicy extends Policy {
	
	private int liberties[]; // TODO Make this a field for speed

	public NakadePolicy(Policy fallback) {
		super(fallback);
		liberties = new int[2];
	}

	/**
	 * Returns (if there is one) a point adjacent to the last move that is
	 * vacant and has two vacant neighbors, all surrounded by enemy stones (or
	 * board edges).
	 */
	public int findNakade(int lastMove, Board board) {
		for (int i = 0; i < 4; i++) {
			int neighbor = Coordinates.NEIGHBORS[lastMove][i];
			if (board.getColor(neighbor) == VACANT) {
				int center = soleVacantNeighbor(neighbor, board);
				if (center == NO_POINT) {
					// Neighbor may be at the center of a 3-point eyespace
					int[] eyeLiberties = twoVacantNeighbors(neighbor, board);
					if (eyeLiberties != null) {
						if ((soleVacantNeighbor(eyeLiberties[0], board) == neighbor)
								&& (soleVacantNeighbor(eyeLiberties[1], board) == neighbor)) {
							return neighbor;
						}
					}
				} else {
					// Center, adjacent to neighbor, may be at the center of a
					// 3-point eyespace
					int[] eyeLiberties = twoVacantNeighbors(center, board);
					if (eyeLiberties != null) {
						int other = (eyeLiberties[0] == neighbor) ? eyeLiberties[1]
								: eyeLiberties[0];
						if (soleVacantNeighbor(other, board) == center) {
							return center;
						}
					}
				}
			}
		}
		return NO_POINT;
	}

	/**
	 * If point (being vacant) has exactly one vacant neighbor, returns that
	 * neighbor. Otherwise, returns NO_POINT.
	 */
	protected int soleVacantNeighbor(int point, Board board) {
		int libertyCount = 0;
		int liberty = 0;
		assert board.getColor(point) != OFF_BOARD_COLOR : "\nTrying to get neighbors of off board point";
		// TODO We could use neighbor counts from board to count vacant and
		// enemy neighbors, then
		// only find the liberty if necessary
		if(board.getVacantNeighborCount(point)!=1) {
			return NO_POINT;
		}
		for (int i = 0; i < 4; i++) {
			int p = Coordinates.NEIGHBORS[point][i];
			if (board.getColor(p) == VACANT) {
				libertyCount++;
				liberty = p;
			} else if (board.getColor(p) == board.getColorToPlay()) {
				return NO_POINT;
			}
		}
		return liberty;
	}

	/**
	 * Returns (in an array) the two vacant points adjacent to point. Returns
	 * null if there are not exactly two such points.
	 */
	public int[] twoVacantNeighbors(int point, Board board) {
		int libertyCount = 0;
		if(board.getVacantNeighborCount(point)!=2) {
			return null;
		}
		for (int i = 0; i < 4; i++) {
			int p = Coordinates.NEIGHBORS[point][i];
			if (board.getColor(p) == VACANT) {
					liberties[libertyCount] = p;
					libertyCount++;
			} else if (board.getColor(p) == board.getColorToPlay()) {
				return null;
			}
		}
		return liberties;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int move = findNakade(board.getMove(board.getTurn() - 1), board);
		if (move != NO_POINT) {
			board.playFast(move); // The move MUST be legal (except possibly
									// superko)
			return move;
		}
		return getFallback().selectAndPlayOneMove(random, board);
	}

	public void updatePriors(SearchNode node, Board board, int weight) {
		int lastMove = board.getMove(board.getTurn() - 1);
		for (int i = 0; i < 4; i++) {
			int neighbor = Coordinates.NEIGHBORS[lastMove][i];
			if (board.getColor(neighbor) == VACANT) {
				int center = soleVacantNeighbor(neighbor, board);
				if (center == NO_POINT) {
					// Neighbor may be at the center of a 3-point eyespace
					int[] eyeLiberties = twoVacantNeighbors(neighbor, board);
					if (eyeLiberties != null) {
						if ((soleVacantNeighbor(eyeLiberties[0], board) == neighbor)
								&& (soleVacantNeighbor(eyeLiberties[1], board) == neighbor)) {
							node.addWins(neighbor, weight);
						}
					}
				} else {
					// Center, adjacent to neighbor, may be at the center of
					// a
					// 3-point eyespace
					int[] eyeLiberties = twoVacantNeighbors(center, board);
					if (eyeLiberties != null) {
						int other = (eyeLiberties[0] == neighbor) ? eyeLiberties[1]
								: eyeLiberties[0];
						if (soleVacantNeighbor(other, board) == center) {
							node.addWins(center, weight);
						}
					}
				}
			}
		}
		getFallback().updatePriors(node, board, weight);
	}

}
