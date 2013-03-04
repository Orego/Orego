package orego.neural;

import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.policy.Policy;

public class LinearMcRunnable extends McRunnable {

	private LinearClassifier classifier;
	
	private int playouts;
	
	protected LinearClassifier getClassifier() {
		return classifier;
	}

	public LinearMcRunnable(McPlayer player, Policy policy, double learn, int history) {
		super(player, policy);
		classifier = new LinearClassifier(learn, history);
	}

	public void setPlayouts(int playouts) {
		this.playouts = playouts;
	}

	public int getPlayouts() {
		return playouts;
	}

}
