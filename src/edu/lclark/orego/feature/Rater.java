package edu.lclark.orego.feature;

import java.io.Serializable;

import edu.lclark.orego.mcts.SearchNode;

public interface Rater extends Serializable {

	/** Update all the children of the node with biases. */
	public void updateNode(SearchNode node);
}
