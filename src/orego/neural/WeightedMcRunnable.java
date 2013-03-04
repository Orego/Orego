package orego.neural;

import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.policy.Policy;

public class WeightedMcRunnable extends McRunnable{
	
	private WeightedClassifier classifier;
	
	private int playouts;
	
	protected WeightedClassifier getClassifier() {
		return classifier;
	}

	public WeightedMcRunnable(McPlayer player, Policy policy, double learn, int history) {
		super(player, policy);
		classifier = new WeightedClassifier(learn, history);
	}

	public void setPlayouts(int playouts) {
		this.playouts = playouts;
	}

	public int getPlayouts() {
		return playouts;
	}
}
