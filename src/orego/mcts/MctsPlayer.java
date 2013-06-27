package orego.mcts;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static orego.experiment.Debug.debug;
import static java.lang.Math.*;
import static java.lang.String.format;
import static java.lang.Double.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.StringTokenizer;
import orego.play.UnknownPropertyException;
import orego.util.*;
import orego.core.*;
import orego.heuristic.Heuristic;
import ec.util.MersenneTwisterFast;

/**
 * Monte-Carlo tree search player, using the UCT algorithm.
 * 
 * @see #searchValue(SearchNode, Board, int)
 */
public class MctsPlayer extends McPlayer {

	/** If the expected win rate exceeds this, emphasize capturing dead stones. */
	public static final double COUP_DE_GRACE_PARAMETER = 0.85;

	/**
	 * This player will resign if the win percentage is less than
	 * RESIGN_PARAMETER
	 */
	public static final double RESIGN_PARAMETER = 0.10;

	public static void main(String[] args) {
		MctsPlayer p = new MctsPlayer();
		try {
			p.setProperty("policy", "Random");
			p.setProperty("threads", "1");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		double[] benchMarkInfo = p.benchmark();
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
				+ benchMarkInfo[1]);
	}

	/**
	 * True if the "coup de grace" property has been set. This causes the player
	 * to try to end the game (by capturing enough enemy stones that it can
	 * safely pass) when it is very confident of winning.
	 */
	private boolean grace;

	/**
	 * True if coup de grace mode is active, i.e., the coup de grace property
	 * has been set and we have determined that we should emphasize capturing
	 * enemy stones on the next move. (This gets reset to false after each
	 * move.)
	 */
	private boolean isCoupDeGraceActive;

	/** The transposition table. */
	private TranspositionTable table;

	private boolean kgsCleanupMode = false;

	@Override
	public void beforeStartingThreads() {
		boolean shouldWeClean = (kgsCleanupMode || isCoupDeGraceActive)
				&& thereAreDeadEnemyStones();
		if (getBoard().getMove(getBoard().getTurn()) == PASS) {
			// Add wins to the moves that are liberties of dead stones (to
			// emphasize killing them).
			IntList deadStones = stonesNotUnconditionallyAlive();
			IntSet pointsToRecommend = new IntSet(
					Coordinates.getFirstPointBeyondBoard());

			for (int i = 0; i < deadStones.size(); i++) {
				if (getBoard().getColor(deadStones.get(i)) != getBoard()
						.getColorToPlay()) {
					IntSet libs = getBoard().getLiberties(deadStones.get(i));
					pointsToRecommend.addAll(libs);
				}
			}

			for (int i = 0; i < pointsToRecommend.size(); i++) {
				int recommendedMove = pointsToRecommend.get(i);
				int bias = (int) (getRoot().getWins(getRoot()
						.getMoveWithMostWins()));
				getRoot().addWins(recommendedMove, bias);
			}
		}
		// Don't emphasize capturing dead stones anymore (unless this gets reset
		// to true by bestStoredMove())
		isCoupDeGraceActive = false;
	}

	public void printAdditionalBenchmarkInfo(double kpps, int playouts,
			long time) {
		System.out.println(this);
		for (int i = 0; i < getNumberOfThreads(); i++) {
			long pp = ((McRunnable) getRunnable(i)).getPlayoutsCompleted();
			System.out.println("Thread " + i + ": " + pp + " playouts");
		}
		System.out.println("(" + getRoot().getTotalRuns()
				+ " playouts in tree)");
		System.out.println(playouts + " playouts in " + time + " msec");
		System.out.println(kpps + " kpps");
		System.out.println(table.dagSize(getRoot()) + " nodes in dag");
	}

	/**
	 * Returns the best move to make from here when actually playing (as opposed
	 * to during a playout). We choose the move with the most wins.
	 */
	protected int bestPlayMove(SearchNode node) {
		double best = 1;
		int result = PASS;
		IntSet vacantPoints = getBoard().getVacantPoints();
		do {
			best = node.getWins(PASS);
			// If the move chosen on the last pass was illegal (e.g., a superko
			// violation that was never actually tried in a playout),
			// throw it out
			if (result != PASS) {
				node.exclude(result);
				result = PASS;
			}
			if (kgsCleanupMode && thereAreDeadEnemyStones()) { // if
																// kgsCleanupMode
																// is true and
																// there are
																// enemy dead
																// stones, do
																// not consider
																// PASS
				result = vacantPoints.get(0);
			}
			for (int i = 0; i < vacantPoints.size(); i++) {
				int move = vacantPoints.get(i);
				if (node.getWins(move) > best) {
					best = node.getWins(move);
					result = move;
				}
			}
		} while ((result != PASS)
				&& !(getBoard().isFeasible(result) && (getBoard()
						.isLegal(result))));
		// Consider entering coup de grace mode
		if (grace && getRoot().bestWinRate() > COUP_DE_GRACE_PARAMETER) {
			isCoupDeGraceActive = true;
		}

		kgsCleanupMode = false;
		// Consider resigning
		if (node.getWinRate(result) < RESIGN_PARAMETER) {
			return RESIGN;
		}
		return result;
	}

	/** Returns the best move to make from here during a playout. */
	public int bestSearchMove(SearchNode node, Board board,
			MersenneTwisterFast random) {
		int result = node.getWinningMove();
		if ((result != NO_POINT) && board.isLegal(result)) {
			// The isLegal() check is necessary to avoid superko violations
			return result;
		}
		double best = searchValue(node, board, PASS);
		result = PASS;
		IntSet vacantPoints = board.getVacantPoints();
		int start;
		start = random.nextInt(vacantPoints.size());
		int i = start;
		do {
			int move = vacantPoints.get(i);
			double searchValue = searchValue(node, board, move);
			if (searchValue > best) {
				if (board.isFeasible(move) && board.isLegal(move)) {
					best = searchValue;
					result = move;
				} else {
					node.exclude(move);
				}
			}
			// The magic number 457 is prime and larger than
			// vacantPoints.size().
			// Advancing by 457 therefore skips "randomly" through the array,
			// in a manner analogous to double hashing.
			i = (i + 457) % vacantPoints.size();
		} while (i != start);
		return result;
	}

	@Override
	public int bestCleanupMove() {
		kgsCleanupMode = true;
		int bestMove = bestMove();
		kgsCleanupMode = false;
		return bestMove;
	}

	@Override
	public int bestStoredMove() {
		SearchNode root = getRoot();
		if (getBoard().getPasses() >= 1) {
			boolean shouldWeClean = kgsCleanupMode && thereAreDeadEnemyStones();
			if (secondPassWouldWinGame() && !shouldWeClean) {
				// Pass if we can win outright by doing so
				return PASS;
			}
			// Don't pass (if there's another legal move -- see exclude()).
			root.exclude(PASS);
		}
		return bestPlayMove(root);
	}

	/** Returns the number of search nodes in the search dag. */
	public int dagSize() {
		return table.dagSize(getRoot());
	}

	/**
	 * Similar to generateMovesToFrontierOfTree, but chooses the specified
	 * sequence of moves.
	 */
	public void fakeGenerateMovesToFrontierOfTree(McRunnable runnable,
			int... moves) {
		SearchNode node = getRoot();
		assert node != null : "Hash code: " + getBoard().getHash();
		runnable.getBoard().copyDataFrom(getBoard());
		for (int p : moves) {
			runnable.acceptMove(p);
			SearchNode child = table.findIfPresent(runnable.getBoard()
					.getHash());
			synchronized (table) {
				// A child will only be created on the second pass through the
				// node
				if (!node.hasChild(p) && (node.getRuns(p) > 2)) {
					child = table.findOrAllocate(runnable.getBoard().getHash());
					if (child == null) {
						return; // No nodes left in pool
					}
					node.setHasChild(p);
					table.addChild(node, child);
					if (child.isFresh()) {
						// child might not be fresh if it's a transposition
						runnable.updatePriors(child, runnable.getBoard());
					}
					return;
				}
			}
			if (child == null) {
				return; // No child
			}
			node = child;
		}
	}

	/** Used by handleCommand. */
	protected String finalStatusList(String status) {
		if (status.equals("seki")) {
			return "";
		}
		String result = "";
		IntList dead = stonesNotUnconditionallyAlive();
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) != VACANT) {
				if (status.equals("alive") != dead.contains(p)) {
					result += pointToString(p) + " ";
				}
			}
		}
		return result;
	}

	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		SearchNode node = getRoot();
		assert node != null : "Hash code: " + getBoard().getHash();
		runnable.getBoard().copyDataFrom(getBoard());
		while (runnable.getBoard().getPasses() < 2) {
			int p = selectAndPlayMove(node, runnable);
			SearchNode child = table.findIfPresent(runnable.getBoard()
					.getHash());
			synchronized (table) {
				// a child will only be created if we expect the node to be
				// visited again
				if (!node.hasChild(p) && (node.getWins(p) >= 2)) {
					child = table.findOrAllocate(runnable.getBoard().getHash());
					if (child == null) {
						return; // No nodes left in pool
					}
					node.setHasChild(p);
					table.addChild(node, child);
					if (child.isFresh()) {
						// child might not be fresh if it's a transposition
						runnable.updatePriors(child, runnable.getBoard());
					}
					return;
				}
			}
			if (child == null) {
				return; // No child
			}
			node = child;
		}
	}

	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("final_status_list");
		result.add("gogui-primary-variation");
		result.add("gogui-search-values");
		result.add("gogui-playouts");
		result.add("gogui-one-playout");
		result.add("gogui-total-wins");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("gfx/Primary variation/gogui-primary-variation");
		result.add("gfx/Search values/gogui-search-values");
		result.add("none/Playouts/gogui-playouts %s");
		result.add("gfx/One playout/gogui-one-playout");
		result.add("gfx/Total wins/gogui-total-wins");
		return result;
	}

	@Override
	public long getPlayouts(int p) {
		return getRoot().getRuns(p);
	}

	/** Returns a node of the type to be used to build the dag. */
	protected SearchNode getPrototypeNode() {
		return new SearchNode();
	}

	/** Returns the node at the root of the search tree. */
	public SearchNode getRoot() {
		return table.findOrAllocate(getBoard().getHash());
	}

	/** Returns the transposition table. For testing. */
	protected TranspositionTable getTable() {
		return table;
	}

	@Override
	public double getWinRate(int p) {
		return getRoot().getWinRate(p);
	}

	@Override
	public double getWins(int p) {
		return getRoot().getWins(p);
	}

	/**
	 * Runs one playout and returns a String that GoGui can use to display that
	 * playout.
	 */
	protected String goguiOnePlayout() {
		String result = "VAR";
		McRunnable runnable = ((McRunnable) getRunnable(0));
		runnable.performMcRun();
		int color = getBoard().getColorToPlay();
		for (int t = getBoard().getTurn(); t < runnable.getTurn(); t++) {
			result += format(" %s %s", color == BLACK ? "B" : "W",
					pointToString(runnable.getMove(t)));
			color = opposite(color);
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
		SearchNode node = getRoot();
		int depth = 0;
		while (node != null && depth < 15) {
			depth++;
			int best = bestPlayMove(node);
			int legality;
			if (best == RESIGN) {
				legality = -1;
			} else {
				legality = board.play(best);
			}
			if (legality != orego.core.Board.PLAY_OK) {
				debug("Illegal move after primary variation shown:\n"
						+ node.toString(best));
				break;
			}
			result += format(" %s %s", board.getColorToPlay() == BLACK ? "W"
					: "B", pointToString(best));
			node = table.findIfPresent(board.getHash());
		}
		return result;
	}

	/** Returns GoGui information showing search values. */
	protected String goguiSearchValues() {
		// TODO Encapsulate this normalization in a single place, called by all
		// the various gogui methods
		double min = 1.0;
		double max = 0.0;
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = getWinRate(p);
				if (winRate > 0) {
					double v = searchValue(getRoot(), getBoard(), p);
					min = Math.min(min, v);
					max = Math.max(max, v);
				}
			}
		}
		String result = "";
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = getWinRate(p);
				if (winRate > 0) {
					if (result.length() > 0) {
						result += "\n";
					}
					double v = searchValue(getRoot(), getBoard(), p);
					result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
							colorCode((v - min) / (max - min)),
							pointToString(p), pointToString(p), v * 100);
				}
			}
		}
		return result;
	}

	protected String goguiTotalWins() {
		double max = 0, min = 1;
		double maxWins = 0;
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double wins = getWins(p);
				// Excluded moves have negative win rates
				if (wins > 0) {
					max = Math.max(max, wins);
					min = Math.min(min, wins);
					maxWins = Math.max(maxWins, getWins(p));
				}
			}
		}
		String result = "INFLUENCE";
		for (int p : getAllPointsOnBoard()) {
			double wins = 0;
			if (getBoard().getColor(p) == VACANT) {
				wins = getWins(p);
				if (wins > 0) {
					result += format(" %s %.3f", pointToString(p), getWins(p)
							/ (double) maxWins);
				}
			}
		}
		// Display win rates as colors and percentages
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double wins = getWins(p);
				if (wins > 0) {
					if (result.length() > 0) {
						result += "\n";
					}
					result += String.format("COLOR %s %s\nLABEL %s %.0f",
							colorCode((double) wins / maxWins),
							pointToString(p), pointToString(p), (double) wins);
				}
			}
		}
		return result;
	}

	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		String result = null;
		if (command.equals("final_status_list")) {
			result = finalStatusList(arguments.nextToken());
		} else if (command.equals("gogui-primary-variation")) {
			result = goguiPrimaryVariation();
		} else if (command.equals("gogui-search-values")) {
			result = goguiSearchValues();
		} else if (command.equals("gogui-playouts")) {
			int n = Integer.parseInt(arguments.nextToken());
			for (int i = 0; i < n; i++) {
				((McRunnable) getRunnable(0)).performMcRun();
			}
			result = "";
		} else if (command.equals("gogui-one-playout")) {
			result = goguiOnePlayout();
		} else if (command.equals("gogui-total-wins")) {
			result = goguiTotalWins();
		} else {
			result = super.handleCommand(command, arguments);
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return result;
	}

	/** Returns true if there are enemy dead stones on the board. */
	protected boolean thereAreDeadEnemyStones() {
		IntList alreadyDeadStones = stonesNotUnconditionallyAlive();
		for (int i = 0; i < alreadyDeadStones.size(); i++) {
			// if there are enemy dead stones
			if (getBoard().getColor(alreadyDeadStones.get(i)) != getBoard()
					.getColorToPlay()) { // I'm assuming colorToPlay is orego's
											// color
				return true;
			}
		}
		return false;
	}

	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		int turn = runnable.getTurn();
		SearchNode node = getRoot();
		int[] moves = runnable.getMoves();
		long[] hashes = runnable.getHashes();
		float winProportion = winner == getBoard().getColorToPlay() ? 1 : 0;
		if (winner == VACANT) {
			winProportion = 0.5f;
		}
		for (int t = getBoard().getTurn(); t < turn; t++) {
			node.recordPlayout(winProportion, moves, t, turn,
					runnable.getPlayedPoints());
			long hash = hashes[t + 1];
			node = table.findIfPresent(hash);
			if (node == null) {
				return;
			}
			winProportion = 1 - winProportion;
		}
	}

	/**
	 * True if coup de grace mode is active for the next move. For testing.
	 */
	public boolean isCoupDeGraceActive() {
		return isCoupDeGraceActive;
	}

	/**
	 * Returns true if the coup de grace property (which encourages capturing
	 * enemy stones when the game is clearly won) is true.
	 */
	public boolean isGrace() {
		return grace;
	}

	@Override
	public void reset() {
		try {
			super.reset();
			if (table == null) {
				table = new TranspositionTable(getPrototypeNode());
			}
			table.sweep();
			table.findOrAllocate(getBoard().getHash());
			for (int i = 0; i < getNumberOfThreads(); i++) {
				setRunnable(i, new McRunnable(this, getHeuristics().clone()));
			}
			SearchNode root = getRoot();
			if (root.isFresh()) {
				((McRunnable) getRunnable(0)).updatePriors(root, getBoard());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Returns the UCT upper bound for node. This is the UCB1-TUNED policy,
	 * explained in the tech report by Gelly, et al, "Modification of UCT with
	 * Patterns in Monte-Carlo Go". The formula is at the bottom of p. 5 in that
	 * paper.
	 */
	public double searchValue(SearchNode node, Board board, int move) {
		// The variable names here are chosen for consistency with the tech
		// report
		double barX = node.getWinRate(move);
		if (barX < 0) { // if the move has been excluded
			return NEGATIVE_INFINITY;
		}
		double logParentRunCount = log(node.getTotalRuns());
		// In the paper, term1 is the mean of the SQUARES of the rewards; since
		// all rewards are 0 or 1 here, this is equivalent to the mean of the
		// rewards, i.e., the win rate.
		double term1 = barX;
		double term2 = -(barX * barX);
		double term3 = sqrt(2 * logParentRunCount / node.getRuns(move));
		double v = term1 + term2 + term3; // This equation is above Eq. 1
		assert v >= 0 : "Negative variability in UCT for move "
				+ pointToString(move) + ":\n" + node + "\nterm1: " + term1
				+ "\nterm2: " + term2 + "\nterm3: " + term3
				+ "\nPlayer's board:\n" + getBoard() + "\nVacant points: "
				+ getBoard().getVacantPoints().toStringAsPoints()
				+ "\nRunnable's board:\n" + board + "\nVacant points: "
				+ board.getVacantPoints().toStringAsPoints();
		double factor1 = logParentRunCount / node.getRuns(move);
		double factor2 = min(0.25, v);
		double uncertainty = 0.4 * sqrt(factor1 * factor2);
		return uncertainty + barX;
	}

	/** Selects and plays one move in the search tree. */
	protected int selectAndPlayMove(SearchNode node, McRunnable runnable) {
		int move = bestSearchMove(node, runnable.getBoard(),
				runnable.getRandom());
		runnable.acceptMove(move);
		return move;
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("pool")) {
			table = new TranspositionTable(Integer.parseInt(value),
					getPrototypeNode());
		} else if (property.equals("grace")) {
			assert value.equals("true");
			grace = true;
		} else {
			super.setProperty(property, value);
		}
	}

	protected void setTable(TranspositionTable table) {
		this.table = table;
	}

	@Override
	public void setUpProblem(int colorToPlay, String[] diagram) {
		super.setUpProblem(colorToPlay, diagram);
		getRoot();
	}

	/**
	 * Returns a string representing the search tree up to the specified depth.
	 */
	public String toString(int maxDepth) {
		Board board = new Board();
		board.copyDataFrom(getBoard());
		return toString(maxDepth, getRoot(), board, "");
	}

	/** Recursive helper method used by the one-argument version of toString(). */
	protected String toString(int maxDepth, SearchNode node, Board board,
			String indent) {
		if (maxDepth < 0) {
			return "";
		}
		String result = indent + "Hash: " + node.getHash() + " Total runs: "
				+ node.getTotalRuns() + "\n";
		Board childBoard = new Board();
		for (int p : getAllPointsOnBoard()) {
			if (node.getRuns(p) > 2) {
				result += indent + node.toString(p);
				childBoard.copyDataFrom(board);
				childBoard.play(p);
				SearchNode child = table.findIfPresent(childBoard.getHash());
				if (child != null) {
					result += toString(maxDepth - 1, child, childBoard, indent
							+ "  ");
				}
			}
		}
		if (node.getRuns(PASS) > 10) {
			result += indent + node.toString(PASS);
			childBoard.copyDataFrom(board);
			childBoard.play(PASS);
			SearchNode child = table.findIfPresent(childBoard.getHash());
			if (child != null) {
				result += toString(maxDepth - 1, child, childBoard, indent
						+ "  ");
			}
		}
		return result;
	}

	@Override
	public boolean undo() {
		debug("undoing");
		boolean result = super.undo();
		stopThreads();
		SearchNode root = getRoot();
		if (root != null) {
			table.markNodesReachableFrom(root);
		}
		table.sweep();
		root = getRoot();
		assert root != null;
		if (root.isFresh()) {
			((McRunnable) getRunnable(0)).updatePriors(root, getBoard());
		}
		if (isPondering()) {
			startThreads();
		}
		return result;
	}

	@Override
	public void updateForAcceptMove(int p) {
		debug("Accepted move " + pointToString(p));
		SearchNode root = getRoot();
		if (root != null) {
			table.markNodesReachableFrom(root);
		}
		table.sweep();
		root = getRoot();
		assert root != null;
		if (root.isFresh()) {
			((McRunnable) getRunnable(0)).updatePriors(root, getBoard());
		}
		debug(winRateReport());
	}

	@Override
	protected String winRateReport() {
		return "";
		// return colorToString(getBoard().getColorToPlay()) + " to play: "
		// + getRoot();
	}

}
