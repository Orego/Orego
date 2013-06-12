package tictactoe;

import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;

import java.util.List;

import static java.lang.Math.*;

public class Node {

	/** Number of runs through this node. */
	private int runs;

	/** Number of wins through this node. This is a double because of ties. */
	private double wins;

	/** Children of this node, indexed by the moves (r * 3 + c) leading to them. */
	private TreeMap<Integer, Node> children;

	public Node() {
		children = new TreeMap<Integer, Node>();
	}

	/** Returns the stored move with the most wins. */
	public int actualMove() {
		double bestValue = Double.NEGATIVE_INFINITY;
		int bestMove = -1;
		for (int move : children.keySet()) {
			// TODO Is the child associated with this move better than the best
			// we've seen so far?
			if (bestValue < getChild(move).wins) {
				bestMove = move;
				bestValue = getChild(move).wins;
			}
		}
		
		return bestMove;
	}

	/** Adds a child for move to this Node. */
	public void addChild(int move) {
		children.put(move, new Node());
	}

	/** Returns the child corresponding to move. */
	public Node getChild(int move) {
		return children.get(move);
	}

	/**
	 * Returns the best move to make from here DURING PLAYOUTS. If there are any
	 * untried moves, a random one of them is chosen first. Otherwise, the
	 * UCB1-TUNED formula is used.
	 */
	public int playoutMove(Board board) {
		Random generator = new Random();
		LinkedList<Integer> legal = new LinkedList<Integer>();
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) {
				if (board.isVacant(r, c)) {
					legal.add(r * 3 + c);
				}
			}
		}
		while(legal.size()!=0){
			int temp = generator.nextInt(legal.size());
			if(getChild(legal.get(temp))==null){
				return legal.get(temp);
			}
			legal.remove(temp);
		}

		// No untried moves; use UCB1-TUNED
		double bestValue = Double.NEGATIVE_INFINITY;
		int bestMove = -1;
		for (int move : children.keySet()) {
			// Is the child associated with this move better than the best we've
			// seen so far?
			if (bestValue < getChild(move).getUcb1TunedValue(this)) {
				bestMove = move;
				bestValue = getChild(move).getUcb1TunedValue(this);
			}
		}
		return bestMove;
	}

	/** Returns the UCB1-TUNED value for this node. */
	public double getUcb1TunedValue(Node parent) {
		double barX = wins / runs;
		double logParentRunCount = log(parent.runs);
		double term1 = barX;
		double term2 = -(barX * barX);
		double term3 = sqrt(2 * logParentRunCount / runs);
		double v = term1 + term2 + term3;
		if (v < 0) {
			throw new RuntimeException("Negative variability in UCB");
		}
		double factor1 = logParentRunCount / runs;
		double factor2 = min(0.25, v);
		double uncertainty = sqrt(factor1 * factor2);
		return uncertainty + barX;
	}

	/**
	 * Updates the wins and runs statistics in the tree rooted at this Node. If
	 * the playout is long enough, adds a new leaf.
	 * 
	 * @param result
	 *            1.0 if X won, 0.0 if O won, 0.5 if the playout was a tie.
	 * @param colorToPlay
	 *            the color to play at root.
	 */
	public void recordPlayout(List<Integer> moves, double result,
			int colorToPlay) {
		Node node = this;
		runs++;
		boolean leafAdded = false;
		for (int move : moves) {
			// TODO If there is no child for move, add a leaf and remember that
			// we did so
			if (node.getChild(move) == null) {
				node.addChild(move);
				leafAdded = true;
			}

			node = node.getChild(move);
			node.runs++;
			if (colorToPlay == 1) {
				node.wins += result;
			} else {
				node.wins += 1.0 - result;
			}
			if (leafAdded) {
				return;
			}
			colorToPlay = opposite(colorToPlay);
		}
	}

	private int opposite(int colorToPlay) {
		if (colorToPlay == 1) {
			return 2;
		} else {
			return 1;
		}
	}
	@Override
	public String toString() {
		String result = "<root>\t(" + runs + " playouts)\n";
		for (Integer move : children.keySet()) {
			result += children.get(move).toString("\t" + move + ": ");
		}
		return result;
	}

	/** Recursive helper method. */
	protected String toString(String indent) {
		String result = indent + String.format("%1.3f", wins / runs) + "\t("
				+ runs + " playouts)\n";
		for (Integer move : children.keySet()) {
			result += children.get(move).toString(indent + "\t" + move + ": ");
		}
		return result;
	}

}
