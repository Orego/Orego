package orego.neural;

import static orego.core.Coordinates.PASS;
import orego.core.Board;
import orego.mcts.McRunnable;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;

public class DeepeningPlayer extends LinearPlayer {

	private double growth;

	public DeepeningPlayer(){
		growth = .001;
	}
	
	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		super.incorporateRun(winner, runnable);
		((LinearMcRunnable) runnable).setPlayouts(((LinearMcRunnable) runnable)
				.getPlayouts() + 1);
	}

	@Override
	public void updateForAcceptMove(int p) {
		super.updateForAcceptMove(p);
		for (int i = 0; i < getNumberOfThreads(); i++) {
			((LinearMcRunnable) getRunnable(i)).setPlayouts(0);
		}
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("growth")) {
			setGrowth(Double.parseDouble(value));
		}else {
			super.setProperty(property, value);
		}
	}

	private void setGrowth(double parseDouble) {
		growth = parseDouble;
	}

	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		int cutoff = 1 + (int) (growth * ((LinearMcRunnable) runnable)
				.getPlayouts());
		Board board = runnable.getBoard();
		board.copyDataFrom(getBoard());
		int count = 0;
		LinearClassifier classifier = ((LinearMcRunnable) runnable)
				.getClassifier();
		while ((board.getPasses() < 2) && (count < cutoff)) {
			int bestMove = PASS;
			double bestEval = Integer.MIN_VALUE;
			IntSet vacantPoints = board.getVacantPoints();
			for (int i = 0; i < vacantPoints.size(); i++) {
				int p = vacantPoints.get(i);
				if (board.isFeasible(p)) {
					double eval = classifier.evaluate(board.getColorToPlay(),
							p, board, board.getTurn());
					if ((eval > bestEval) && (board.isLegal(p))) {
						bestMove = p;
						bestEval = eval;
					}
				}
			}
			runnable.acceptMove(bestMove);
			count++;
		}
	}

}
