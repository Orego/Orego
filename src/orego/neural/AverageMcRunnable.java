package orego.neural;

import orego.heuristic.HeuristicList;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;

public class AverageMcRunnable extends McRunnable{

	private AverageClassifier classifier;
	
	private int playouts;
	
	protected AverageClassifier getClassifier() {
		return classifier;
	}

	public AverageMcRunnable(McPlayer player, HeuristicList heuristics, double learn, int history) {
		super(player, heuristics);
		classifier = new AverageClassifier(learn, history);
	}

	public void setPlayouts(int playouts) {
		this.playouts = playouts;
	}

	public int getPlayouts() {
		return playouts;
	}
}
