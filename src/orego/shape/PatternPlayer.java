package orego.shape;

import static orego.core.Board.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ec.util.MersenneTwisterFast;

import orego.core.Board;
import orego.core.Colors;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.mcts.SearchNode;
import orego.util.IntSet;

public class PatternPlayer extends McPlayer {

//	public static void main(String[] args) {
//		new PatternPlayer(true);
//	}

	@SuppressWarnings("unchecked")
	private Cluster patterns;

//	private long numPlayouts;
//
//	private int[] playoutCount;
//
//	private boolean maintainHashes;
//
//	private double[] patternWeights;
//
//	public static final int NUM_HASH_TABLES = 4;

	public PatternPlayer() {
		initializePlayer(true);
	}

	public PatternPlayer(boolean maintainBoardHashes) {
		initializePlayer(maintainBoardHashes);
	}

	@Override
	public void beforeStartingThreads() {
//		playoutCount = new int[getFirstPointBeyondBoard()];
	}

//	private int bestMove(Board b) {
//		int result = 0;
//		float rate = 0;
//		IntSet moves = b.getVacantPoints();
//		for (int i = 0; i < moves.size(); i++) {
//			if (b.isLegal(moves.get(i)) && b.isFeasible(moves.get(i))) {
//				float tempRate = (float) getWinRate(b, moves.get(i));
//				if (tempRate > rate) {
//					rate = tempRate;
//					result = moves.get(i);
//				}
//			}
//		}
//		if (rate < .25)
//			result = PASS;
////		if (b.getHash() == getBoard().getHash()) {
////			playoutCount[result]++;
////		}
//		return result;
//	}

	/** Pass only if all moves have a win rate this low. */
	public static final float PASS_THRESHOLD = 0.25f;
	
	/** Returns the best move to make from here during a playout. */
	public int bestSearchMove(Board board, MersenneTwisterFast random) {
		double best = PASS_THRESHOLD;
		int result = PASS;
		IntSet vacantPoints = board.getVacantPoints();
		int start;
		start = random.nextInt(vacantPoints.size());
		int i = start;
		do {
			int move = vacantPoints.get(i);
			double searchValue = getWinRate(board, move);
			if (searchValue > best) {
				if (board.isFeasible(move) && board.isLegal(move)) {
					best = searchValue;
					result = move;
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
	public int bestStoredMove() {
		return bestSearchMove(getBoard(), ((McRunnable) getRunnable(0)).getRandom());
	}

	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		runnable.getBoard().copyDataFrom(getBoard());
		while (runnable.getBoard().getPasses() < 2) {
			int move = bestSearchMove(runnable.getBoard(), runnable.getRandom());
			runnable.acceptMove(move);
		}
//		numPlayouts++;
	}

	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-radius-win-rates");
		result.add("gogui-combined-pattern-win-rates");
		result.add("gogui-playouts");
//		result.add("gogui-pattern-first-playout-move");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("gfx/Radius win rates/gogui-radius-win-rates %s");
		result.add("gfx/Combined pattern win rates/gogui-combined-pattern-win-rates");
		result.add("none/Playouts/gogui-playouts %s");
//		result.add("gfx/Top playout move chosen/gogui-pattern-first-playout-move");
		return result;
	}

//	public PatternInformation[] getInformation(int patternType, long hash,
//			int color) {
//		PatternInformation[] toReturn = new PatternInformation[NUM_HASH_TABLES];
//		for (int table = 0; table < NUM_HASH_TABLES; table++) {
//			toReturn[table] = patterns[table][patternType][color]
//					.get(hashLongToChar(hash, table));
//			if (toReturn[table] == null)
//				toReturn[table] = new PatternInformation();
//		}
//		return toReturn;
//	}

//	/**
//	 * Given a long hash, returns the (64/NUM_HASH_TABLES)-bit (max is 16 bits)
//	 * hash associated with table 0 - NUM_HASH_TABLES.
//	 * 
//	 * @param hash
//	 *            The sixteen bits of the long associated with the given hash
//	 *            table
//	 * @param table
//	 *            Which hash table to extract the bits for
//	 * @return
//	 */
//	public static char hashLongToChar(long hash, int table) {
//		return (char) ((hash >>> ((table % NUM_HASH_TABLES) * (64 / NUM_HASH_TABLES))) & 0xffff);
//	}

	@Override
	public long getPlayouts(int p) {
//		return playoutCount[p];
		return -1L;
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
		if (command.contains("gogui-radius-win-rates")) {
			result = goguiRadiusWinRates(Integer
					.parseInt(arguments.nextToken()));
		} else if (command.contains("gogui-combined-pattern-win-rates")) {
			result = goguiCombinedPatternWinRates();
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
//		if (winner == VACANT) {
//			System.out
//					.println("Incorporate run was given VACANT winner--should not happen");
//			return;
//		}
//		int turn = runnable.getTurn();
//		int[] moves = runnable.getMoves();
//		Board b = new Board(maintainHashes);
//		b.copyDataFrom(getBoard());
//		for (int t = getBoard().getTurn(); t < turn; t++) {
//			for (int pattern = 0; pattern <= MAX_PATTERN_RADIUS; pattern++) {
//				PatternInformation[] info = getInformation(pattern,
//						b.getPatternHash(pattern, moves[t]), b.getColorToPlay());
//				for (int i = 0; i < patternWeight(pattern) * 20; i++) {
//					for (PatternInformation pi : info) {
//						if (b.getColorToPlay() == winner)
//							pi.addWin();
//						else
//							pi.addLoss();
//					}
//				}
//			}
//			b.play(moves[t]);
//		}
	}

	private void initializePlayer(boolean maintain) {
//		maintainHashes = maintain;
//		patternWeights = new double[] { 1 / 20.0, 3 / 20.0, 6 / 20.0, 10 / 20.0 };
		setBoard(new Board(maintain));
//		playoutCount = new int[getFirstPointBeyondBoard()];
		loadPatternHashMaps();
//		numPlayouts = 0;
//		this.setInOpeningBook(false);
	}

	@SuppressWarnings("unchecked")
	protected void loadPatternHashMaps() {
		try {
			// TODO Should use SgfFiles, not SgfTestFiles
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					new File(orego.experiment.Debug.OREGO_ROOT_DIRECTORY
							+ "SgfFiles" + File.separator + getBoardWidth()
							+ File.separator + "Patterns.data")));
			patterns = (Cluster) (in.readObject());
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
		setUpRunnables();
	}

	/** Selects and plays one move in the search tree. */
	protected int selectAndPlayMove(SearchNode node, McRunnable runnable) {
		int move = bestSearchMove(runnable.getBoard(),
				runnable.getRandom());
		runnable.acceptMove(move);
		return move;
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
