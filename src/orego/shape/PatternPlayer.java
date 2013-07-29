package orego.shape;

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

	/** Pass only if all moves have a win rate this low. */
	public static final float PASS_THRESHOLD = 0.25f;

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

	/** Counts of nearby stones for later incorporating into pattern tables. */
	private int[][] counts;
	
	/** Hashes around move for later incorporating into pattern tables. */
	private long[][] hashes;

	/** Noise added to the win rate of each move when choosing moves in playouts. */
	private float noise; 

	/** Pattern hash tables. */
	private RichCluster patterns;
	
	/** Number of moves through each point, for gogui display. */
	private int[] playoutCount;
	
	/** Used in bestSearchMove(). */
	private float threshold;
	
	/** Total number of playouts this turn. */
	private long totalPlayoutCount;

	/** Initial value for noise. */
	private float initialNoise;
	
	public PatternPlayer() {
		this(true);
	}

	public PatternPlayer(boolean maintainHashes) {
		setBoard(new Board(maintainHashes));
		hashes = new long[MAX_MOVES_PER_GAME][MAX_PATTERN_RADIUS + 2];
		counts = new int[MAX_MOVES_PER_GAME][MAX_PATTERN_RADIUS + 1];
		threshold = 0.51f;
		initialNoise = 1.0f;
	}
	
	@Override
	public void beforeStartingThreads() {
		totalPlayoutCount = 0;
		noise = initialNoise;
	}
	
	/** Returns the best move to make from here during a playout. */
	public int bestSearchMove(Board board, MersenneTwisterFast random) {
		double best = PASS_THRESHOLD;
		int result = PASS;
		IntSet vacantPoints = board.getVacantPoints();
		int start = random.nextInt(vacantPoints.size());
		int i = start;
		int skip = PRIMES[random.nextInt(PRIMES.length)];
		do {
			int move = vacantPoints.get(i);
			if (board.isFeasible(move)) {
				float searchValue = patterns.getWinRate(board, move) + noise
						* random.nextFloat();
				if (searchValue > best && board.isLegal(move)) {
					best = searchValue;
					result = move;
				}
			}
			// Advancing by a random prime skips through the array
			// in a manner analogous to double hashing.
			i = (i + skip) % vacantPoints.size();
		} while ((i != start) && best < threshold);
		return result;
	}
		
	@Override
	public int bestStoredMove() {
		debug("Choosing a move for " + COLOR_NAMES[getBoard().getColorToPlay()] + " on:\n" + getBoard());
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
	
	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
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
		result.add("gogui-radius-run-counts");
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
		result.add("gfx/Radius run counts/gogui-radius-run-counts %s");
		return result;
	}

	@Override
	public long getPlayouts(int p) {
		return playoutCount[p];
	}

	/** Returns the total number of playouts this turn. */
	protected long getTotalPlayoutCount() {
		return totalPlayoutCount;
	}

	protected float getWinRate(Board b, int point) {
		return patterns.getWinRate(b, point);
	}

	@Override
	public double getWinRate(int point) {
		return getWinRate(getBoard(), point);
	}

	public double getWinRate(int point, int radius) {
		return patterns.getWinRate(getBoard(), point, radius);
	}

	@Override
	public double getWins(int p) {
		float winRate = patterns.getWinRate(getBoard(), p);
		long runs = patterns.getCount(getBoard(), p);
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
			}
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

	/**
	 * Generates the string to be passed to GoGui representing the current best
	 * variation of moves found by this player.
	 */
	protected String goguiPrimaryVariation() {
		String result = "VAR";
		// To show the best tree, we need to manually traverse the tree
		Board board = new Board();
		board.copyDataFrom(getBoard());
		for (int depth = 0; depth < 15; depth++) {
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

	protected String goguiRadiusRunCounts(int radius) {
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
				long count = patterns.getPatternCount(getBoard(), p, radius);
				// Number of playouts through each point
				result += String.format("COLOR %s %s\nLABEL %s %s",
						colorCode(((double)count) / max), pointToString(p),
						pointToString(p), count);
			}
		}
		return result;
	}

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
		} else if (command.equals("gogui-radius-run-counts")) {
			result = goguiRadiusRunCounts(Integer.parseInt(arguments.nextToken()));
		} else if (command.equals("gogui-primary-variation")) {
			result = goguiPrimaryVariation();
		} else if (command.equals("gogui-playouts")) {
			beforeStartingThreads();
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
			for (int t = getBoard().getTurn(); t < turn; t++) {
				patterns.store(hashes[t], counts[t], color, winner);
				winner=1-winner;
				color=1-color;
			}
			playoutCount[runnable.getBoard().getMove(getBoard().getTurn())]++;
		}
		noise = 0.999f * noise;
		debug("Noise: " + noise);
		totalPlayoutCount++;
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

	@Override
	public void reset() {
		super.reset();
		playoutCount = new int[getFirstPointBeyondBoard()];
		loadPatternHashMaps();
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new McRunnable(this, getHeuristics().clone()));
		}
	}

	/** Selects and plays one move in the search tree. */
	protected int selectAndPlayMove(SearchNode node, McRunnable runnable) {
		int move = bestSearchMove(runnable.getBoard(),
				runnable.getRandom());
		runnable.acceptMove(move);
		return move;
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("initialnoise")) {
			initialNoise = Float.parseFloat(value);
		} else {
			super.setProperty(property, value);
		}
	}

	/** Store pattern hashes around move for later incorporating into pattern tables. */
	protected void storeHashes(Board board, int move) {
		int turn = board.getTurn();
		for (int r = 1; r <= MAX_PATTERN_RADIUS; r++) {
			hashes[turn][r] = board.getPatternHash(move, r);
			counts[turn][r] = board.getNumberOfStonesNear(move, r);
		}
		hashes[turn][MAX_PATTERN_RADIUS + 1] = patterns.getGlobalHash(board, move, board.getColorToPlay());
	}

	@Override
	public void updateForAcceptMove(int p) {
		// Does nothing
	}

	@Override
	protected String winRateReport() {
		return "";
	}

}
