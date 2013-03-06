package orego.neural;

import orego.heuristic.HeuristicList;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;

public class LinearMcRunnable extends McRunnable {

	private LinearClassifier classifier;
	
	private int playouts;
	
	protected LinearClassifier getClassifier() {
		return classifier;
	}

	public LinearMcRunnable(McPlayer player, HeuristicList heuristics, double learn, int history) {
		super(player, heuristics);
		classifier = new LinearClassifier(learn, history);
	}

	public void setPlayouts(int playouts) {
		this.playouts = playouts;
	}

	public int getPlayouts() {
		return playouts;
	}

}
