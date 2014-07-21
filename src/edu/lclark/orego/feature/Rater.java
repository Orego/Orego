package edu.lclark.orego.feature;

import java.io.Serializable;

import edu.lclark.orego.mcts.SearchNode;

public interface Rater extends Serializable {

	public void updateNode(SearchNode node);
}
