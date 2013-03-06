package orego.neural;

import static java.lang.String.format;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.*;
import static orego.experiment.Debug.debug;
import static orego.neural.RatioClassifier.*;

import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Board;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;

public class RatioPlayer extends McPlayer {

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
	 * Number of playouts starting at each point. The move with the most
	 * playouts is chosen.
	 */
	private int[] playouts;

	public RatioPlayer() {
		cutoff = 10;
		history = 2;
	}

	@Override
	public void beforeStartingThreads() {
		// TODO Auto-generated method stub

	}

	protected int bestPlayMove() {
		int best = -1;
		int result = PASS;
		IntSet vacantPoints = getBoard().getVacantPoints();
		// TODO We should consider passing; right now we will keep playing until
		// there are no legal moves, even in seki
		for (int i = 0; i < vacantPoints.size(); i++) {
			int move = vacantPoints.get(i);
			if (getBoard().isFeasible(move)) {
				if ((playouts[move] >= best) && getBoard().isLegal(move)) {
					best = playouts[move];
					result = move;
				}
			}
		}
		return result;
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
		RatioClassifier classifier = ((RatioMcRunnable) runnable)
				.getClassifier();
		while ((board.getPasses() < 2) && (count < cutoff)) {
			int bestMove = PASS;
			double bestEval = Integer.MIN_VALUE;
			IntSet vacantPoints = board.getVacantPoints();
			for (int i = 0; i < vacantPoints.size(); i++) {
				int p = vacantPoints.get(i);
				if (board.isFeasible(p)) {
					double eval = classifier.getUctValue(board.getColorToPlay(),
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

	@Override
	public int getPlayouts(int p) {
		return playouts[p];
	}

	@Override
	public double getWinRate(int p) {
		// TODO maybe be better to average over all of the McRunnables
		RatioClassifier classifier = ((RatioMcRunnable) getRunnable(0))
				.getClassifier();
		return classifier.evaluate(getBoard().getColorToPlay(), p, getBoard(),
				getBoard().getTurn());
	}

	@Override
	public int getWins(int p) {
		// TODO Is this meaningful here?
		return getPlayouts(p);
	}

	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		if (winner != VACANT) {
			RatioClassifier classifier = ((RatioMcRunnable) runnable)
					.getClassifier();
			// TODO turn may be a bad name for this
			int turn = runnable.getTurn();
			int[] moves = runnable.getMoves();
			int win = 1 - Math.abs(winner - getBoard().getColorToPlay());
			int color = getBoard().getColorToPlay();
			playouts[moves[getTurn()]]++;
			for (int t = getTurn(); t < turn; t++) {
				classifier.learn(color, runnable.getBoard(), t, win);
				win = 1 - win;
				color = opposite(color);
			}
//			System.out.println(classifier.evaluate(getBoard().getColorToPlay(), at("f7"), getBoard(), getTurn())
//			+ "\t" + classifier.evaluate(getBoard().getColorToPlay(), at("g6"), getBoard(), getTurn())
//			+ "\t" + classifier.evaluate(getBoard().getColorToPlay(), at("a2"), getBoard(), getTurn())
//			);				
		}
	}

	@Override
	public void reset() {
		super.reset();
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new RatioMcRunnable(this, getHeuristics().clone(),
					history));
		}
		playouts = new int[FIRST_POINT_BEYOND_BOARD];
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

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("cutoff")) {
			setCutoff(Integer.parseInt(value));
		} else if (property.equals("history")) {
			setHistory(Integer.parseInt(value));
		} else {
			super.setProperty(property, value);
		}
	}

	@Override
	public void updateForAcceptMove(int p) {
		java.util.Arrays.fill(playouts, 0);
	}

	@Override
	protected String winRateReport() {
		// TODO Auto-generated method stub
		return null;
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
		} else if (command.equals("gogui-bias-ratios")) {
			result = goguiBiasRatios();
		} else if (command.equals("gogui-previous-ratios")) {
			result = goguiPreviousRatios();
		} else if (command.equals("gogui-penultimate-ratios")) {
			result = goguiPenultimateRatios();
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
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-playouts");
		result.add("gogui-bias-ratios");
		result.add("gogui-previous-ratios");
		result.add("gogui-penultimate-ratios");
		result.add("gogui-primary-variation");
		return result;
	}

	protected String goguiPrimaryVariation() {
		String result = "VAR";
		// To show the best tree, we need to manually traverse the tree
		Board board = new Board();
		board.copyDataFrom(getBoard());
		for (int depth = 0; depth < cutoff; depth++) {
			int best = bestSearchMove(board,
					((RatioMcRunnable) getRunnable(0)).getClassifier());
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

	protected int bestSearchMove(Board board, RatioClassifier classifier) {
		int bestMove = PASS;
		double bestEval = Integer.MIN_VALUE;
		IntSet vacantPoints = board.getVacantPoints();
		for (int i = 0; i < vacantPoints.size(); i++) {
			int p = vacantPoints.get(i);
			if (board.isFeasible(p)) {
				double eval = classifier.getUctValue(board.getColorToPlay(), p,
						board, board.getTurn());
				if ((eval > bestEval) && (board.isLegal(p))) {
					bestMove = p;
					bestEval = eval;
				}
			}
		}
		return bestMove;
	}

	protected String goguiPenultimateRatios() {
		int move = getMove(getTurn() - 2);
		double max = -999;
		double min = 9999;
		RatioClassifier classifier = ((RatioMcRunnable) getRunnable(0))
				.getClassifier();
		int[][][][][] counts = classifier.getCounts();
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				double bias = (double) (counts[getBoard().getColorToPlay()][move][1][p][WINS])
						/ counts[getBoard().getColorToPlay()][move][1][p][RUNS];
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
						colorCode(((double) (counts[getBoard().getColorToPlay()][move][1][p][WINS])
								/ counts[getBoard().getColorToPlay()][move][1][p][RUNS] - min)
								/ (max - min)),
						pointToString(p),
						pointToString(p),
						(double) (counts[getBoard().getColorToPlay()][move][1][p][WINS])
								/ counts[getBoard().getColorToPlay()][move][1][p][RUNS]);
			}
		}
		return result;
	}

	protected String goguiPreviousRatios() {
		int move = getMove(getTurn() - 1);
		double max = 0;
		double min = 9999;
		RatioClassifier classifier = ((RatioMcRunnable) getRunnable(0))
				.getClassifier();
		int[][][][][] counts = classifier.getCounts();
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				double bias = (double) (counts[getBoard().getColorToPlay()][move][0][p][WINS])
						/ counts[getBoard().getColorToPlay()][move][0][p][RUNS];
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
						colorCode(((double) (counts[getBoard().getColorToPlay()][move][0][p][WINS])
								/ counts[getBoard().getColorToPlay()][move][0][p][RUNS] - min)
								/ (max - min)),
						pointToString(p),
						pointToString(p),
						(double) (counts[getBoard().getColorToPlay()][move][0][p][WINS])
								/ counts[getBoard().getColorToPlay()][move][0][p][RUNS]);
			}
		}
		return result;
	}

	protected String goguiBiasRatios() {
		double max = -999;
		double min = 9999;
		RatioClassifier classifier = ((RatioMcRunnable) getRunnable(0))
				.getClassifier();
		int[][][][][] counts = classifier.getCounts();
		for (int p : ALL_POINTS_ON_BOARD) {
			if (getBoard().getColor(p) == VACANT) {
				double bias = (double) (counts[getBoard().getColorToPlay()][BIAS][0][p][WINS])
						/ counts[getBoard().getColorToPlay()][BIAS][0][p][RUNS];
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
						colorCode(((double)(counts[getBoard().getColorToPlay()][BIAS][0][p][WINS])
								/ counts[getBoard().getColorToPlay()][BIAS][0][p][RUNS] - min)
								/ (max - min)),
						pointToString(p),
						pointToString(p),
						(double)(counts[getBoard().getColorToPlay()][BIAS][0][p][WINS])
								/ counts[getBoard().getColorToPlay()][BIAS][0][p][RUNS]);
			}
		}
		return result;
	}
	
	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("none/Playouts/gogui-playouts %s");
		result.add("gfx/Bias Ratios/gogui-bias-ratios");
		result.add("gfx/Previous Ratios/gogui-previous-ratios");
		result.add("gfx/Penultimate Ratios/gogui-penultimate-ratios");
		result.add("gfx/Primary variation/gogui-primary-variation");
		return result;
	}

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

	@Override
	public long[] getBoardWins() {
		// TODO: There may be a better way to estimate wins
		return getBoardPlayouts();
	}

	@Override
	public long[] getBoardPlayouts() {
		long[] longPlayouts = new long[FIRST_POINT_BEYOND_BOARD];
		for(int idx = 0; idx < FIRST_POINT_BEYOND_BOARD; idx++) {
			longPlayouts[idx] = (long) playouts[idx];
		}
		return longPlayouts;
	}

	@Override
	public long getTotalPlayoutCount() {
		long total = 0;
		for(int idx = 0; idx < FIRST_POINT_BEYOND_BOARD; idx++) {
			total += playouts[idx];
		}
		return total;
	}

}
