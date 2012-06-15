package orego.neural;

import static java.lang.String.format;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.ALL_POINTS_ON_BOARD;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.pointToString;
import static orego.experiment.Debug.debug;
import static orego.neural.LinearClassifier.BIAS;
import orego.core.Board;
import orego.mcts.McRunnable;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;

public class WeightedPlayer extends AveragePlayer {
	
	@Override
	public void reset() {
		super.reset();
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new WeightedMcRunnable(this, getPolicy().clone(),
					getLearn(), getHistory()));
		}
		setPlayouts(new int[FIRST_POINT_BEYOND_BOARD]);
	}

	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		if (winner != VACANT) {
			WeightedClassifier classifier = ((WeightedMcRunnable) runnable)
					.getClassifier();
			int turn = runnable.getTurn();
			int[] moves = runnable.getMoves();
			int win = 1 - Math.abs(winner - getBoard().getColorToPlay());
			int color = getBoard().getColorToPlay();
			getPlayouts()[moves[getTurn()]]++;
			for (int t = getTurn(); t < turn; t++) {
				classifier.learn(color, runnable.getBoard(), t, win);
				win = 1 - win;
				color = opposite(color);
			}
		}
	}

	/**
	 * Generates the string to be passed to GoGui representing the current best
	 * variation of moves found by this player.
	 */
	protected String goguiPrimaryVariation() {
		String result = "VAR";
		// To show the best tree, we need to manually traverse the tree
		Board board = new Board();
		board.copyDataFrom(getBoard());
		for (int depth = 0; depth < getCutoff(); depth++) {
			int best = bestSearchMove(board,
					((WeightedMcRunnable) getRunnable(0)).getClassifier());
			int legality = board.play(best);
			if (legality != orego.core.Board.PLAY_OK) {
				debug("Illegal in after primary variation");
				break;
			}
			result += format(" %s %s", board.getColorToPlay() == BLACK ? "W"
					: "B", pointToString(best));
		}
		return result;
	}

	public String goguiPreviousWeights() {
		int move = getMove(getTurn() - 1);
		double max = 0;
		double min = 9999;
		WeightedClassifier classifier = ((WeightedMcRunnable) getRunnable(0))
				.getClassifier();
		double[][][][] weights = classifier.getWeights();
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				double bias = weights[getBoard().getColorToPlay()][move][0][p];
				if (bias > max) {
					max = bias;
				}
				if (bias < min) {
					min = bias;
				}
			}
		}
		String result = "";
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				result += format(
						"\nCOLOR %s %s\nLABEL %s %.3f",
						colorCode((weights[getBoard().getColorToPlay()][move][0][p] - min)
								/ (max - min)), pointToString(p),
						pointToString(p),
						weights[getBoard().getColorToPlay()][move][0][p]);
			}
		}
		return result;
	}

	public String goguiPenultimateWeights() {
		int move = getMove(getTurn() - 2);
		double max = -999;
		double min = 9999;
		WeightedClassifier classifier = ((WeightedMcRunnable) getRunnable(0))
				.getClassifier();
		double[][][][] weights = classifier.getWeights();
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				double bias = weights[getBoard().getColorToPlay()][move][1][p];
				if (bias > max) {
					max = bias;
				}
				if (bias < min) {
					min = bias;
				}
			}
		}
		String result = "";
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				result += format(
						"\nCOLOR %s %s\nLABEL %s %.3f",
						colorCode((weights[getBoard().getColorToPlay()][move][1][p] - min)
								/ (max - min)), pointToString(p),
						pointToString(p),
						weights[getBoard().getColorToPlay()][move][1][p]);
			}
		}
		return result;
	}

	public String goguiBiasWeights() {
		double max = -999;
		double min = 9999;
		WeightedClassifier classifier = ((WeightedMcRunnable) getRunnable(0))
				.getClassifier();
		double[][][][] weights = classifier.getWeights();
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				double bias = weights[getBoard().getColorToPlay()][BIAS][0][p];
				if (bias > max) {
					max = bias;
				}
				if (bias < min) {
					min = bias;
				}
			}
		}
		String result = "";
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				result += format(
						"\nCOLOR %s %s\nLABEL %s %.3f",
						colorCode((weights[getBoard().getColorToPlay()][BIAS][0][p] - min)
								/ (max - min)), pointToString(p),
						pointToString(p),
						weights[getBoard().getColorToPlay()][BIAS][0][p]);
			}
		}
		return result;
	}

	@Override
	public double getWinRate(int p) {
		// TODO maybe be better to Weighted over all of the McRunnables
		WeightedClassifier classifier = ((WeightedMcRunnable) getRunnable(0))
				.getClassifier();
		return classifier.evaluate(getBoard().getColorToPlay(), p, getBoard(),
				getBoard().getTurn());
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		super.setProperty(property, value);
	}

	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		Board board = runnable.getBoard();
		board.copyDataFrom(getBoard());
		int count = 0;
		WeightedClassifier classifier = ((WeightedMcRunnable) runnable)
				.getClassifier();
		while ((board.getPasses() < 2) && (count < getCutoff())) {
			int bestMove = bestSearchMove(board, classifier);
			runnable.acceptMove(bestMove);
			count++;
		}
	}

	protected int bestSearchMove(Board board, WeightedClassifier classifier) {
		int bestMove = PASS;
		double bestEval = Integer.MIN_VALUE;
		IntSet vacantPoints = board.getVacantPoints();
		for (int i = 0; i < vacantPoints.size(); i++) {
			int p = vacantPoints.get(i);
			if (board.isFeasible(p)) {
				double eval = classifier.evaluate(board.getColorToPlay(), p,
						board, board.getTurn());
				if ((eval > bestEval) && (board.isLegal(p))) {
					bestMove = p;
					bestEval = eval;
				}
			}
		}
		return bestMove;
	}
}
