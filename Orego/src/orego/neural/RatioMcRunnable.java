package orego.neural;

import orego.mcts.McRunnable;
import orego.policy.Policy;

public class RatioMcRunnable extends McRunnable {

	private RatioClassifier classifier;
	
	private int playouts;
	
	public RatioMcRunnable(RatioPlayer player, Policy policy, int history) {
		super(player, policy);
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
