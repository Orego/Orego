package orego.neural;

import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.policy.Policy;

public class Rich1McRunnable extends McRunnable {

	private Rich1Classifier classifier;

	private int playouts;

	public Rich1McRunnable(McPlayer player, Policy policy, double learn,
			int history) {
		super(player, policy);
		classifier = new Rich1Classifier(learn, history);
	}

	protected Rich1Classifier getClassifier() {
		return classifier;
	}

	public int getPlayouts() {
		return playouts;
	}

	public void setPlayouts(int playouts) {
		this.playouts = playouts;
	}
}
