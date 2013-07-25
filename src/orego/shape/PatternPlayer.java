package orego.shape;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Math.log;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.lang.String.format;
import static orego.core.Board.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static orego.experiment.Debug.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Set;
import java.util.StringTokenizer;
import ec.util.MersenneTwisterFast;
import orego.core.Board;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.mcts.SearchNode;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;

public class PatternPlayer extends McPlayer {

	public static void main(String[] args) {
		PatternPlayer p = new PatternPlayer();
		try {
			p.setProperty("threads", "1");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		double[] benchMarkInfo = p.benchmark();
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
				+ benchMarkInfo[1]);
	}

	@SuppressWarnings("unchecked")
	private RichCluster patterns;

	private long numPlayouts;
//
	private int[] playoutCount; 
//
//	private boolean maintainHashes;
//
//	private double[] patternWeights;
//
//	public static final int NUM_HASH_TABLES = 4;

	@Override
	public void beforeStartingThreads() {
//		playoutCount = new int[getFirstPointBeyondBoard()];
		numPlayouts = 0;
	}

	/** Pass only if all moves have a win rate this low. */
	public static final float PASS_THRESHOLD = 0.25f;
	
	private float threshold;
	
	/** Returns the best move to make from here during a playout. */
	public int bestSearchMove(Board board, MersenneTwisterFast random) {
		double best = PASS_THRESHOLD;
		int result = PASS;
		IntSet vacantPoints = board.getVacantPoints();
		int start = random.nextInt(vacantPoints.size());
		int i = start;
		int skip = PRIMES[random.nextInt(PRIMES.length)];
//		int totalRuns = 0;
//		for(int j=0; j<vacantPoints.size(); j++){
//			totalRuns+= patterns.getCount(board, vacantPoints.get(j));
//		}
//		float threshold = 5 * (0.45f + (0.1f * vacantPoints.size()) / getBoardArea()); // 5 * because the win rates from five tables are being added, not averaged
		int count = 0;
		do {
			int move = vacantPoints.get(i);
//			double searchValue = searchValue(board, move, totalRuns);
			if (board.isFeasible(move)) {
				float searchValue = patterns.getWinRate(board, move) + 0.0f * random.nextFloat();
				count++;
				if (searchValue > best && board.isLegal(move)) {
					best = searchValue;
					result = move;
				}
			}
			// Advancing by a random prime skips through the array
			// in a manner analogous to double hashing.
			i = (i + skip) % vacantPoints.size();
		} while ((i != start) && best < threshold);
//		debug("Looked at "+count+" moves.");
		return result;
	}

	@Override
	public int bestStoredMove() {
		debug("Choosing a move for " + COLOR_NAMES[getBoard().getColorToPlay()] + " on:\n" + getBoard());
		debug(numPlayouts + " playouts");
		return bestStoredMove(getBoard());
	}
		
	protected int bestStoredMove(Board board) {
		MersenneTwisterFast random = ((McRunnable)(getRunnable(0))).getRandom();
		double best = PASS_THRESHOLD;
		int result = PASS;
		IntSet vacantPoints = board.getVacantPoints();
		int start = random.nextInt(vacantPoints.size());
		int i = start;
		int skip = PRIMES[random.nextInt(PRIMES.length)];
		do {
			int move = vacantPoints.get(i);
			float searchValue = patterns.getWinRate(board, move);
			if (searchValue > best) {
				if (board.isFeasible(move) && board.isLegal(move)) {
					best = searchValue;
					result = move;
				}
			}
			// Advancing by a random prime skips through the array
			// in a manner analogous to double hashing.
			i = (i + skip) % vacantPoints.size();
		} while (i != start);
		return result;
	}

	/** Hashes around move for later incorporating into pattern tables. */
	private long[][] hashes;
	
	/** Store pattern hashes around move for later incorporating into pattern tables. */
	protected void storeHashes(Board board, int move) {
//		if (move == at("n1")) {
//			System.err.println("Storing hashes for n1:\n" + board);
//		}
		int turn = board.getTurn();
		for (int r = 1; r <= MAX_PATTERN_RADIUS; r++) {
			hashes[turn][r] = board.getPatternHash(move, r);
		}
		hashes[turn][MAX_PATTERN_RADIUS + 1] = patterns.getGlobalHash(board, move, board.getColorToPlay());
	}

	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
//		debug("Starting new playout.");
		runnable.getBoard().copyDataFrom(getBoard());
		while (runnable.getBoard().getPasses() < 2) {
			int move = bestSearchMove(runnable.getBoard(), runnable.getRandom());
			if (move != PASS) {
				storeHashes(runnable.getBoard(), move);
			}
			runnable.acceptMove(move);
		}
	}

	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-radius-win-rates");
		result.add("gogui-combined-pattern-win-rates");
		result.add("gogui-playouts");
		result.add("gogui-playouts-through");
		result.add("gogui-run-counts");
		result.add("gogui-primary-variation");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("gfx/Radius win rates/gogui-radius-win-rates %s");
		result.add("gfx/Combined pattern win rates/gogui-combined-pattern-win-rates");
		result.add("none/Playouts/gogui-playouts %s");
		result.add("gfx/Playouts through/gogui-playouts-through");
		result.add("gfx/Run counts/gogui-run-counts");
		result.add("gfx/Primary variation/gogui-primary-variation");
		return result;
	}

	@Override
	public long getPlayouts(int p) {
		return playoutCount[p];
	}

//	public long getTotalNumPlayouts() {
//		return numPlayouts;
//	}

	protected float getWinRate(Board b, int point) {
		return patterns.getWinRate(b, point);
	}

	protected float getWinRate(Board b, int point, int radius) {
		return patterns.getWinRate(b, point, radius);
	}

	@Override
	public double getWinRate(int point) {
		return getWinRate(getBoard(), point);
	}

	public double getWinRate(int point, int radius) {
		return getWinRate(getBoard(), point, radius);
	}

	@Override
	public double getWins(int p) {
		float winRate = 0;
		double runs = 0;
//		for (int pattern = 0; pattern <= MAX_PATTERN_RADIUS; pattern++) {
//			PatternInformation[] info = getInformation(pattern, getBoard()
//					.getPatternHash(pattern, p), getBoard().getColorToPlay());
//			for (PatternInformation i : info) {
//				winRate += i.getRate() * patternWeight(pattern)
//						/ ((double) NUM_HASH_TABLES);
//				runs += i.getRuns() * patternWeight(pattern)
//						/ ((double) NUM_HASH_TABLES);
//			}
//		}
		return winRate * runs;
	}

	protected String goguiCombinedPatternWinRates() {
		String result = "";
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				if (result.length() > 0)
					result += '\n';

				float totalRate = (float) getWinRate(p);
				// RATE
				result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
						colorCode(totalRate), pointToString(p),
						pointToString(p), totalRate * 100);

				// RUNS
				// int totalRuns = 0;
				// for (int i = 0; i < NINE_PATTERN + 1; i++) {
				// PatternInformation info = getInformation(i, getBoard()
				// .getPatternHash(i, p));
				// totalRuns += info.getRuns();
				// }
				// result += String.format("COLOR %s %s\nLABEL %s %d",
				// colorCode(totalRate), pointToString(p),
				// pointToString(p), totalRuns%1000);
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
		MersenneTwisterFast random = new MersenneTwisterFast();
		board.copyDataFrom(getBoard());
		for (int depth = 0; depth < 15; depth++) {
//		while(board.getPasses() < 2) {
			int best = bestStoredMove(board);
			int legality;
			if (best == RESIGN) {
				legality = -1;
			} else {
				debug(pointToString(best) + " is legal on\n" + board);
				legality = board.play(best);
			}
			if (legality != orego.core.Board.PLAY_OK) {
				debug("Illegal move after primary variation shown: " + pointToString(best));
				break;
			}
			result += format(" %s %s", board.getColorToPlay() == BLACK ? "W"
					: "B", pointToString(best));
		}
		return result;
	}

	protected String goguiRadiusWinRates(int radius) {
		String result = "";
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				if (result.length() > 0)
					result += '\n';

				float totalRate = (float) getWinRate(p, radius);
				// RATE
				result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
						colorCode(totalRate), pointToString(p),
						pointToString(p), totalRate * 100);

				// RUNS
				// int totalRuns = 0;
				// for (int i = 0; i < NINE_PATTERN + 1; i++) {
				// PatternInformation info = getInformation(i, getBoard()
				// .getPatternHash(i, p));
				// totalRuns += info.getRuns();
				// }
				// result += String.format("COLOR %s %s\nLABEL %s %d",
				// colorCode(totalRate), pointToString(p),
				// pointToString(p), totalRuns%1000);
			}
		}
		return result;
		
	}

	// private String goguiFirstPlayoutMove() {
	// String result = "";
	// for (int p : getAllPointsOnBoard()) {
	// // if (getBoard().getColor(p) == VACANT) {
	// if (result.length() > 0)
	// result += '\n';
	// result += String.format("COLOR %s %s\nLABEL %s %d",
	// colorCode(getWinRate(p)), pointToString(p),
	// pointToString(p), playoutCount[p]);
	// // }
	// }
	// return result;
	// }

	// private String goguiPatternInfo(int patternType) {
	// String result = "";
	// for (int p : getAllPointsOnBoard()) {
	// if (getBoard().getColor(p) == VACANT) {
	// if (result.length() > 0)
	// result += '\n';
	// PatternInformation info[] = getInformation(patternType,
	// getBoard().getPatternHash(patternType, p), getBoard()
	// .getColorToPlay());
	// float rate = 0;
	// for (PatternInformation i : info) {
	// rate += i.getRate();
	// }
	// rate /= (double) NUM_HASH_TABLES;
	// result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
	// colorCode(rate), pointToString(p), pointToString(p),
	// rate * 100);
	// }
	// }
	// return result;
	// }

	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		String result = null;
		if (command.equals("gogui-radius-win-rates")) {
			result = goguiRadiusWinRates(Integer
					.parseInt(arguments.nextToken()));
		} else if (command.equals("gogui-combined-pattern-win-rates")) {
			result = goguiCombinedPatternWinRates();
		} else if (command.equals("gogui-run-counts")) {
			result = goguiRunCounts();
		} else if (command.equals("gogui-primary-variation")) {
			result = goguiPrimaryVariation();
		} else if (command.equals("gogui-playouts")) {
			int n = Integer.parseInt(arguments.nextToken());
			for (int i = 0; i < n; i++) {
				((McRunnable) getRunnable(0)).performMcRun();
			}
			result = "";
		} else if (command.equals("gogui-live-mc-playouts")) {
			long oldValue;
			boolean isMilliseconds = (getMillisecondsPerMove() != -1);
			if (isMilliseconds) {
				oldValue = getMillisecondsPerMove();
			} else {
				oldValue = getPlayoutLimit();
			}
			setMillisecondsPerMove(1000);
			for (int i = 0; i < 10; i++) {
				bestMove();
				System.err.println("gogui-gfx: \n"
						+ goguiCombinedPatternWinRates() + "\n");
			}
			if (isMilliseconds) {
				setMillisecondsPerMove((int) oldValue);
			} else {
				setPlayoutLimit(oldValue);
			}
			result = "";
			// } else if (command.equals("gogui-pattern-first-playout-move")) {
			// result = goguiFirstPlayoutMove();
		} else if( command.equals("gogui-playouts-through")){
			result=goguiPlayoutsThrough();
		} else {
			result = super.handleCommand(command, arguments);
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return result;
	}

	protected String goguiPlayoutsThrough() {
		String result="";
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				if (result.length() > 0)
					result += '\n';

				int playoutsThrough = playoutCount[p];
				// Number of playouts through each point
				result += String.format("LABEL %s %s",
						pointToString(p), playoutsThrough);
			}
		}
		return result;
	}

	protected String goguiRunCounts() {
		String result="";
		long max = -1;
		for (int p : getAllPointsOnBoard()) {
			long count = patterns.getCount(getBoard(), p);
			if (count > max) {
				max = count;
			}
		}		
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				if (result.length() > 0)
					result += '\n';
				long count = patterns.getCount(getBoard(), p);
				// Number of playouts through each point
				result += String.format("COLOR %s %s\nLABEL %s %s",
						colorCode(((double)count) / max), pointToString(p),
						pointToString(p), count);
			}
		}
		return result;
	}
	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		if (winner != VACANT) {
			int turn = runnable.getTurn();
			int color = getBoard().getColorToPlay();
			if (winner == color) {
				winner=1;
			} else{
				winner=0;
			}
//			System.err.println("Player's board's turn: " + getBoard().getTurn());
//			System.err.println("Runnable's board's turn: " + turn);
			for (int t = getBoard().getTurn(); t < turn; t++) {
				if (t == getBoard().getTurn()) {
//					System.err.println("Storing " + pointToString(runnable.getBoard().getMove(t)) + " at turn " + t);
				}
				patterns.store(hashes[t], color, winner);
				winner=1-winner;
				color=1-color;
			}
			playoutCount[runnable.getBoard().getMove(getBoard().getTurn())]++;
		}
		numPlayouts++;
//		threshold = 0.999f * threshold + 0.001f;
		debug("Threshold: " + threshold);
	}

	@SuppressWarnings("unchecked")
	protected void loadPatternHashMaps() {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					new File(orego.experiment.Debug.OREGO_ROOT_DIRECTORY
							+ "SgfFiles" + File.separator + "Patternsr4t4b16.data")));
			patterns = (RichCluster) (in.readObject());
			patterns.setCount(1000);
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

//	private double patternWeight(int pattern) {
//		return patternWeights[pattern];
//	}

	@Override
	public void reset() {
		super.reset();
		setBoard(new Board(true));
		playoutCount = new int[getFirstPointBeyondBoard()];
		loadPatternHashMaps();
		hashes = new long[MAX_MOVES_PER_GAME][MAX_PATTERN_RADIUS + 2];
//		threshold = 0.001f;
		threshold = 0.5f;
		setUpRunnables();
	}

	/** Selects and plays one move in the search tree. */
	protected int selectAndPlayMove(SearchNode node, McRunnable runnable) {
		int move = bestSearchMove(runnable.getBoard(),
				runnable.getRandom());
		runnable.acceptMove(move);
		return move;
	}
	
	/**
	 * Returns the UCT upper bound for node. This is the UCB1-TUNED policy,
	 * explained in the tech report by Gelly, et al, "Modification of UCT with
	 * Patterns in Monte-Carlo Go". The formula is at the bottom of p. 5 in that
	 * paper.
	 */
	public double searchValue(Board board, int move, int totalRuns) {
		// The variable names here are chosen for consistency with the tech
		// report
		double barX = getWinRate(board, move);
		if (barX < 0) { // if the move has been excluded
			return NEGATIVE_INFINITY;
		}
		double logParentRunCount = log(totalRuns);
		// In the paper, term1 is the mean of the SQUARES of the rewards; since
		// all rewards are 0 or 1 here, this is equivalent to the mean of the
		// rewards, i.e., the win rate.
		double term1 = barX;
		double term2 = -(barX * barX);
		double factor1 = logParentRunCount / patterns.getCount(board, move);
		double term3 = sqrt(2 * factor1);
		double v = term1 + term2 + term3; // This equation is above Eq. 1
		assert v >= 0 : "Negative variability in UCT for move "
				+ pointToString(move) + ":\n" + "ERROR" + "\nterm1: " + term1
				+ "\nterm2: " + term2 + "\nterm3: " + term3
				+ "\nPlayer's board:\n" + getBoard() + "\nVacant points: "
				+ getBoard().getVacantPoints().toStringAsPoints()
				+ "\nRunnable's board:\n" + board + "\nVacant points: "
				+ board.getVacantPoints().toStringAsPoints();
		double factor2 = min(0.25, v);
		double uncertainty = 0.4 * sqrt(factor1 * factor2);
		return uncertainty + barX;
	}

	protected void setUpRunnables() {
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new McRunnable(this, getHeuristics().clone()));
		}
	}

//	public void setWeights(double threePattern, double fivePattern,
//			double sevenPattern, double ninePattern) {
//		double sum = threePattern + fivePattern + sevenPattern + ninePattern;
//		patternWeights = new double[] { threePattern / sum, fivePattern / sum,
//				sevenPattern / sum, ninePattern / sum };
//	}

//	protected String topPlayoutCount() {
//		String result = "";
//		for (int row = 0; row < 19; row++) {
//			for (int col = 0; col < 19; col++) {
//				result += playoutCount[at(row, col)];
//			}
//			result += '\n';
//		}
//		return result;
//	}

	@Override
	public void updateForAcceptMove(int p) {
//		setWeights(9 * getBoard().getTurn() / 220.0 + 1, 3 * getBoard()
//				.getTurn() / 220.0 + 3,
//				3 * (1 - getBoard().getTurn() / 220.0) + 3, 9 * (1 - getBoard()
//						.getTurn() / 220.0) + 1);
	}

	@Override
	protected String winRateReport() {
		return "";
	}
}
