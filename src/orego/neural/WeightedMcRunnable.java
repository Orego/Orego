package orego.neural;

import orego.heuristic.HeuristicList;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;

public class WeightedMcRunnable extends McRunnable{
	
	private WeightedClassifier classifier;
	
	private int playouts;
	
	protected WeightedClassifier getClassifier() {
		return classifier;
	}

	public WeightedMcRunnable(McPlayer player, HeuristicList heuristics, double learn, int history) {
		super(player, heuristics);
		classifier = new WeightedClassifier(learn, history);
	}

	public void setPlayouts(int playouts) {
		this.playouts = playouts;
	}

	public int getPlayouts() {
		return playouts;
	}
}
