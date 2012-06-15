package orego.neural;

import java.util.Set;
import java.util.StringTokenizer;
import orego.mcts.*;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;
import orego.core.Board;
import static java.lang.String.format;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static orego.experiment.Debug.debug;
import static orego.neural.AverageClassifier.*;

/** Uses a linear classifier instead of a search tree. */
public class LinearPlayer extends McPlayer {

	public static void main(String[] args) {
		McPlayer p = new LinearPlayer();
		try {
			p.setProperty("learn", "0.01");
			p.setProperty("cutoff", "10");
			p.setProperty("history", "2");
			p.setProperty("threads", "1");
			p.setProperty("playouts", "10000"); // TODO Get rid of this line
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// System.out.println(p.benchmark());
		p.reset();
		p.bestMove();
		System.out.println(p.goguiWinRates());
	}

	/**
	 * How many moves the classifier plays before the policy finishes the
	 * playout.
	 */
	private int cutoff;

	/**
	 * How many previous moves we look back at to determine our next play.
	 */
	private int history;

	/**
	 * The learning rate.
	 */
	private double learn;

	/**
	 * Number of playouts starting at each point. The move with the most
	 * playouts is chosen.
	 */
	private int[] playouts;

	public LinearPlayer() {
		setLearn(0.01);
		cutoff = 10;
		setHistory(2);
	}

	@Override
	public void beforeStartingThreads() {
		// Do nothing
	}

	/**
	 * Returns the best move to make from here when actually playing (as opposed
	 * to during a playout). We choose the move with the most playouts.
	 */
	protected int bestPlayMove() {
		int best = -1;
		int result = PASS;
		IntSet vacantPoints = getBoard().getVacantPoints();
		// TODO We should consider passing; right now we will keep playing until
		// there are no legal moves, even in seki
		for (int i = 0; i < vacantPoints.size(); i++) {
			int move = vacantPoints.get(i);
			if (getBoard().isFeasible(move)) {
				if ((getPlayouts()[move] >= best) && getBoard().isLegal(move)) {
					best = getPlayouts()[move];
					result = move;
				}
			}
		}
		// Consider entering coup de grace mode
		// if (grace
		// && (node.getWinRate(result) > COUP_DE_GRACE_PARAMETER)
		// & (((McRunnable) getRunnable(0)).getPolicy().getClass() !=
		// CoupDeGracePolicy.class)) {
		// debug("Initiating coup de grace");
		// for (int i = 0; i < getNumberOfThreads(); i++) {
		// McRunnable m = ((McRunnable) getRunnable(i));
		// m.setPolicy(new CoupDeGracePolicy(m.getPolicy()));
		// }
		// }
		// Consider resigning
		// if (node.getWinRate(result) < RESIGN_PARAMETER) {
		// return RESIGN;
		// }
		return result;
	}

	protected int bestSearchMove(Board board, LinearClassifier classifier) {
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

	@Override
	public int bestStoredMove() {
		// Can we win outright by passing?
		if (getBoard().getPasses() == 1) {
			if (secondPassWouldWinGame()) {
				return PASS;
			}
			// If not, don't pass if there's a legal move!
			// root.exclude(PASS);
		}
		return bestPlayMove();
	}

	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		Board board = runnable.getBoard();
		board.copyDataFrom(getBoard());
		int count = 0;
		LinearClassifier classifier = ((LinearMcRunnable) runnable)
				.getClassifier();
		while ((board.getPasses() < 2) && (count < cutoff)) {
			int bestMove = bestSearchMove(board, classifier);
			runnable.acceptMove(bestMove);
			count++;
		}
	}

	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-playouts");
		result.add("gogui-bias-weights");
		result.add("gogui-previous-weights");
		result.add("gogui-penultimate-weights");
		result.add("gogui-primary-variation");
		return result;
	}

	/**
	 * Returns the number of moves the classifier plays before deferring to the
	 * policy.
	 */
	public int getCutoff() {
		return cutoff;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("none/Playouts/gogui-playouts %s");
		result.add("gfx/Bias Weights/gogui-bias-weights");
		result.add("gfx/Previous Weights/gogui-previous-weights");
		result.add("gfx/Penultimate Weights/gogui-penultimate-weights");
		result.add("gfx/Primary variation/gogui-primary-variation");
		return result;
	}

	/**
	 * Returns the learning rate.
	 */
	public double getLearningRate() {
		return getLearn();
	}

	@Override
	public int getPlayouts(int p) {
		return playouts[p];
	}

	@Override
	public double getWinRate(int p) {
		// TODO maybe be better to Linear over all of the McRunnables
		LinearClassifier classifier = ((LinearMcRunnable) getRunnable(0))
				.getClassifier();
		return classifier.evaluate(getBoard().getColorToPlay(), p, getBoard(),
				getBoard().getTurn());
	}

	@Override
	public int getWins(int p) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String goguiBiasWeights() {
		double max = -999;
		double min = 9999;
		LinearClassifier classifier = ((LinearMcRunnable) getRunnable(0))
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

	public String goguiPenultimateWeights() {
		int move = getMove(getTurn() - 2);
		double max = -999;
		double min = 9999;
		LinearClassifier classifier = ((LinearMcRunnable) getRunnable(0))
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

	/** Returns GoGui information showing playout distribution and win rates. */
	protected String goguiPlayouts() {
		// Find the max playouts of any move
		int max = 0;
		for (int p : ALL_POINTS_ON_BOARD) {
			int playouts = getPlayouts(p);
			if (playouts > max) {
				max = playouts;
			}
		}
		// Display proportional playouts through each move
		String result = "INFLUENCE";
		for (int p : ALL_POINTS_ON_BOARD) {
			result += format(" %s %.3f", pointToString(p), getPlayouts(p)
					/ (double) max);
		}
		// Label all moves with win rates
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				result += format("\nLABEL %s %d", pointToString(p),
						getPlayouts(p));
			}
		}
		// Highlight best move
		// TODO This causes some (but not all) infeasible moves to be excluded
		// -- why?
		int best = bestStoredMove();
		if (ON_BOARD[best]) {
			result += "\nCOLOR green " + pointToString(best);
		}
		return result;
	}

	public String goguiPreviousWeights() {
		int move = getMove(getTurn() - 1);
		double max = 0;
		double min = 9999;
		LinearClassifier classifier = ((LinearMcRunnable) getRunnable(0))
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

	/**
	 * Generates the string to be passed to GoGui representing the current best
	 * variation of moves found by this player.
	 */
	protected String goguiPrimaryVariation() {
		String result = "VAR";
		// To show the best tree, we need to manually traverse the tree
		Board board = new Board();
		board.copyDataFrom(getBoard());
		for (int depth = 0; depth < cutoff; depth++) {
			int best = bestSearchMove(board,
					((LinearMcRunnable) getRunnable(0)).getClassifier());
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

	@Override
	/** Returns GoGui information showing win rates as colors and percentages. */
	public String goguiWinRates() {
		// Find the maximum and minimum win rates on the board, ignoring
		// occupied points
		double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
		// int maxWins = 0;
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = getWinRate(p);
				max = Math.max(max, winRate);
				min = Math.min(min, winRate);
				// maxWins = Math.max(maxWins, getWins(p));
			}
		}
		// // Display proportional wins through each move
		// String result = "INFLUENCE";
		// for (int p : ALL_POINTS_ON_BOARD) {
		// if (getWinRate(p) > 0) {
		// result += format(" %s %.3f", pointToString(p), getWins(p)
		// / (double) maxWins);
		// }
		// }
		String result = "";
		// Display win rates as colors and percentages
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = getWinRate(p);
				if (result.length() > 0) {
					result += "\n";
				}
				result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
						colorCode((winRate - min) / (max - min)),
						pointToString(p), pointToString(p), winRate * 100);
			}
		}
		return result;
	}

	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		String result = null;
		if (command.equals("gogui-playouts")) {
			int n = Integer.parseInt(arguments.nextToken());
			for (int i = 0; i < n; i++) {
				((McRunnable) getRunnable(0)).performMcRun();
			}
			result = "";
		} else if (command.equals("gogui-bias-weights")) {
			result = goguiBiasWeights();
		} else if (command.equals("gogui-previous-weights")) {
			result = goguiPreviousWeights();
		} else if (command.equals("gogui-penultimate-weights")) {
			result = goguiPenultimateWeights();
		} else if (command.equals("gogui-primary-variation")) {
			result = goguiPrimaryVariation();
		} else {
			result = super.handleCommand(command, arguments);
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return result;
	}

	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		if (winner != VACANT) {
			LinearClassifier classifier = ((LinearMcRunnable) runnable)
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
			// double[][][][] weights = classifier.getWeights();
			// System.out
			// .println(weights[BLACK][LinearClassifier.BIAS][0][at("d5")]
			// + "\t"
			// + weights[BLACK][NO_POINT][0][at("d5")]
			// + "\t"
			// + weights[BLACK][NO_POINT][1][at("d5")]
			// + "\t"
			// + classifier.evaluate(BLACK, at("d5"), getBoard(),
			// 0)
			// + "\t"
			// + getWinRate(at("d5"))
			// );
//			System.out.println(classifier.evaluate(getBoard().getColorToPlay(), at("f7"), getBoard(), getTurn())
//					+ "\t" + classifier.evaluate(getBoard().getColorToPlay(), at("g6"), getBoard(), getTurn())
//					+ "\t" + classifier.evaluate(getBoard().getColorToPlay(), at("a9"), getBoard(), getTurn()));				
		}
	}

	@Override
	public void reset() {
		super.reset();
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new LinearMcRunnable(this, getPolicy().clone(),
					getLearn(), getHistory()));
		}
		setPlayouts(new int[FIRST_POINT_BEYOND_BOARD]);
	}

	/**
	 * Sets the cutoff.
	 */
	protected void setCutoff(int cutoff) {
		this.cutoff = cutoff;
	}

	/**
	 * Sets the history.
	 */
	protected void setHistory(int history) {
		this.history = history;
	}

	/**
	 * Sets the learning rate.
	 */
	protected void setLearn(double learn) {
		this.learn = learn;
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("learn")) {
			setLearn(Double.parseDouble(value));
		} else if (property.equals("cutoff")) {
			setCutoff(Integer.parseInt(value));
		} else if (property.equals("history")) {
			setHistory(Integer.parseInt(value));
		} else {
			super.setProperty(property, value);
		}
	}

	@Override
	public void updateForAcceptMove(int p) {
		java.util.Arrays.fill(getPlayouts(), 0);
	}

	@Override
	protected String winRateReport() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getLearn() {
		return learn;
	}

	public int getHistory() {
		return history;
	}

	public void setPlayouts(int[] playouts) {
		this.playouts = playouts;
	}

	public int[] getPlayouts() {
		return playouts;
	}

}
