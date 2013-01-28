package orego.decision;

import static orego.core.Coordinates.*;
import orego.core.Board;
import static orego.decision.DataCollection.*;

/** A decision tree node. */
public class Node {

	/** Nodes try to split once they have this many data. */
	public static final int SPLIT_SIZE = 1000;

	/** The collection of data associated with this node. */
	private DataCollection dataCollection;

	/** Number of turns back that the split move occurred (e.g., 0 for the previous move, 1 for the penultimate).*/
	private final int depth;

	/** The child for data not having previousMove. */
	private Node noChild;
	
	/** The previous move on which this node is split, or NO_SPLIT if it is a leaf. */
	private int splitMove;

	/** The child for data having previousMove. */
	private Node yesChild;

	public Node(DataCollection dataCollection, int depth) {
		this.dataCollection = dataCollection;
		splitMove = NO_SPLIT;
		this.depth = depth;
	}

	/** Adds a datum to this node. If this makes the size of this node >= SPLIT_SIZE, split this node if possible. */
	public void addDatum(Datum datum) {
		if (splitMove == NO_SPLIT) { // If already a leaf
			dataCollection.add(datum);
			if (dataCollection.size() >= SPLIT_SIZE) {
				dataCollection.getBestSplitMove(this);
				if (splitMove != NO_SPLIT) {
					split();
				}
			}
		} else if (splitMove == datum.getPrevious()[depth]) {
			yesChild.addDatum(datum);
		} else {
			noChild.addDatum(datum);
		}
	}

	protected DataCollection getDataCollection() {
		return dataCollection;
	}

	public int getDepth() {
		return depth;
	}

	protected Node getNoChild() {
		return noChild;
	}

	protected int getPrevMove() {
		return splitMove;
	}

	/**
	 * Returns the win rate at the leaf corresponding to the move history before board.getTurn().
	 */
	public double getWinRate(Board board) {
		if (splitMove == board.getMove(board.getTurn() - 1 - depth)) {
			return getYesChild().getWinRate(board);
		}
		if (getNoChild() != null) {
			return getNoChild().getWinRate(board);
		}
		return dataCollection.getWinRate();
	}

	protected Node getYesChild() {
		return yesChild;
	}
	
	protected void setChildren(Node yes, Node no) {
		yesChild = yes;
		noChild = no;
	}

	protected void setPrevMove(int prevMove) {
		this.splitMove = prevMove;
	}

	/** Splits this node on previousMove. */
	public void split() {
		DataCollection dataYes = new DataCollection();
		DataCollection dataNo = new DataCollection();
		for (int i = 0; i < dataCollection.size(); i++) {
			if (dataCollection.get(i).getPrevious()[depth] == splitMove) {
				dataYes.add(dataCollection.get(i));
			} else {
				dataNo.add(dataCollection.get(i));
			}
		}
		yesChild = new Node(dataYes, depth + 1);
		noChild = new Node(dataNo, depth);
	}

	public String toString() {
		String result = "";
		if (splitMove != NO_SPLIT) {
			result += "split on: " + pointToString(splitMove) + " at history depth: " + depth;
		} else {
			result += dataCollection;
		}
		return result;
	}

	public String toString(String indent) {
		String string = "";
		string += indent + toString();
		if (splitMove != NO_SPLIT) {
			string += "\n" + yesChild.toString(indent + "  ");
			string += "\n" + noChild.toString(indent + "  ");
		}
		return string;
	}
	
}
