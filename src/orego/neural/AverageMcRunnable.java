package orego.neural;

import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.policy.Policy;

public class AverageMcRunnable extends McRunnable{

	private AverageClassifier classifier;
	
	private int playouts;
	
	protected AverageClassifier getClassifier() {
		return classifier;
	}

	public AverageMcRunnable(McPlayer player, Policy policy, double learn, int history) {
		super(player, policy);
		classifier = new AverageClassifier(learn, history);
	}

	public void setPlayouts(int playouts) {
		this.playouts = playouts;
	}

	public int getPlayouts() {
		return playouts;
	}
}
