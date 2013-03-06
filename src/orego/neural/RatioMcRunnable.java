package orego.neural;

import orego.heuristic.HeuristicList;
import orego.mcts.McRunnable;

public class RatioMcRunnable extends McRunnable {

	private RatioClassifier classifier;
	
	private int playouts;
	
	public RatioMcRunnable(RatioPlayer player, HeuristicList heuristics, int history) {
		super(player, heuristics);
		classifier = new RatioClassifier(history);
	}

	protected RatioClassifier getClassifier() {
		return classifier;
	}

	public int getPlayouts() {
		return playouts;
	}

	public void setPlayouts(int playouts) {
		this.playouts = playouts;
	}

}
