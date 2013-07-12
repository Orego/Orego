package orego.mcts;

import static orego.core.Board.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;
import orego.patternanalyze.PatternInformation;
import orego.util.IntSet;

public class CopyOfPatternPlayer {

	public static void main(String[] args) {
		new CopyOfPatternPlayer(true);
	}

	@SuppressWarnings("unchecked")
	private HashMap<Character, PatternInformation>[][] patterns = new HashMap[4][2];

	private long numPlayouts;

	private int[] playoutCount;

	private boolean maintainHashes;

	private double[] patternWeights;
	
	public CopyOfPatternPlayer() {
		initializePlayer(true);
	}
	
	public CopyOfPatternPlayer(boolean maintainBoardHashes) {
		initializePlayer(maintainBoardHashes);
	}
	
	//TODO: took out override
	public void beforeStartingThreads() {
		playoutCount = new int[getFirstPointBeyondBoard()];
	}
	
	private int bestMove(Board curBoard, Board topBoard) {
		int result = 0;
		float rate = 0;
		IntSet moves = curBoard.getVacantPoints();
		for (int i = 0; i < moves.size(); i++) {
			if (curBoard.isLegal(moves.get(i)) && curBoard.isFeasible(moves.get(i))) {
				float tempRate = (float) getWinRate(curBoard, moves.get(i));
				if (tempRate > rate) {
					rate = tempRate;
					result = moves.get(i);
				}
			}
		}
		if (rate < .25)
			result = PASS;
		if (curBoard.getHash() == topBoard.getHash()) {
			playoutCount[result]++;
		}
		return result;
	}
	
	//TODO: took out override
	public int bestStoredMove(Board topBoard) {
		return bestMove(topBoard);
	}
	
	//TODO: took out override
	public void generateMovesToFrontier(McRunnable runnable, Board topBoard) {
		runnable.getBoard().copyDataFrom(topBoard);
		while (runnable.getBoard().getPasses() < 2) {
			int move = bestMove(runnable.getBoard(),topBoard);
			runnable.acceptMove(move);
		}
		numPlayouts++;
	}

	//TODO: took out override
	public Set<String> getCommands(Set<String> result) {
		result.add("gogui-pattern-info-3");
		result.add("gogui-pattern-info-5");
		result.add("gogui-pattern-info-7");
		result.add("gogui-pattern-info-9");
		result.add("gogui-combined-pattern-info");
		result.add("gogui-pattern-first-playout-move");
		return result;
	}
	
	//TODO: took out override
	public Set<String> getGoguiCommands(Set<String> result) {
		result.add("gfx/3x3 pattern info/gogui-pattern-info-3");
		result.add("gfx/5x5 pattern info/gogui-pattern-info-5");
		result.add("gfx/7x7 pattern info/gogui-pattern-info-7");
		result.add("gfx/9x9 pattern info/gogui-pattern-info-9");
		result.add("gfx/Combined pattern info/gogui-combined-pattern-info");
		result.add("gfx/Top playout move chosen/gogui-pattern-first-playout-move");
		return result;
	}

	public PatternInformation getInformation(int patternType, char hash,
			int color) {
		PatternInformation toReturn = patterns[patternType][color].get(hash);

		if (toReturn != null) {
			return toReturn;
		} else {
			return new PatternInformation();
		}
	}

	//TODO: took out override
	public long getPlayouts(int p) {
		return playoutCount[p];
	}

	public long getTotalNumPlayouts() {
		return numPlayouts;
	}
	
	private float getWinRate(Board b, int point) {
		float tempRate = 0;
		for (int pattern = 0; pattern <= NINE_PATTERN; pattern++) {
			PatternInformation info = getInformation(pattern,
					b.getPatternHash(pattern, point), b.getColorToPlay());
			tempRate += info.getRate() * patternWeight(pattern);
		}
		return tempRate;
	}

	//TODO: took out override
	public double getWinRate(int point, Board topBoard) {
		return getWinRate(topBoard, point);
	}

	//TODO: took out override
	public double getWins(int p, Board topBoard) {
		float winRate = 0;
		double runs = 0;
		for (int pattern = 0; pattern <= NINE_PATTERN; pattern++) {
			PatternInformation info = getInformation(pattern, topBoard
					.getPatternHash(pattern, p), topBoard.getColorToPlay());
			winRate += info.getRate() * patternWeight(pattern);
			runs += info.getRuns() * patternWeight(pattern);
		}
		return winRate * runs;
	}

	private String goguiCombinedPatternInfo(Board topBoard) {
		String result = "";
		for (int p : getAllPointsOnBoard()) {
			if (topBoard.getColor(p) == VACANT) {
				if (result.length() > 0)
					result += '\n';

				float totalRate = (float) getWinRate(p,topBoard);

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
	 * Returns a color (used by GoGui) corresponding to x. 1.0 maps to green,
	 * 0.0 to red.  Copy of a method in McPlayer.
	 */
	private String colorCode(double x) {
		int green = (int) (255 * x);
		return String.format("#%02x%02x00", 255 - green, green);
	}

	private String goguiFirstPlayoutMove(Board topBoard) {
		String result = "";
		for (int p : getAllPointsOnBoard()) {
			// if (getBoard().getColor(p) == VACANT) {
			if (result.length() > 0)
				result += '\n';
			result += String.format("COLOR %s %s\nLABEL %s %d",
					colorCode(getWinRate(p,topBoard)), pointToString(p),
					pointToString(p), playoutCount[p]);
			// }
		}
		return result;
	}

	private String goguiPatternInfo(int patternType, Board topBoard) {
		String result = "";
		for (int p : getAllPointsOnBoard()) {
			if (topBoard.getColor(p) == VACANT) {
				if (result.length() > 0)
					result += '\n';
				PatternInformation info = getInformation(patternType,
						topBoard.getPatternHash(patternType, p), topBoard
								.getColorToPlay());
				result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
						colorCode(info.getRate()), pointToString(p),
						pointToString(p), info.getRate() * 100);
			}
		}
		return result;
	}

	

	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		String result = null;
		if (command.contains("gogui-pattern-info")) {
			result = goguiPatternInfo((command.charAt(command.length() - 1) - '0') / 2 - 1);
		} else if (command.contains("gogui-combined-pattern-info")) {
			result = goguiCombinedPatternInfo();
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
				System.err.println("gogui-gfx: \n" + goguiCombinedPatternInfo()
						+ "\n");
			}
			if (isMilliseconds) {
				setMillisecondsPerMove((int) oldValue);
			} else {
				setPlayoutLimit(oldValue);
			}
			result = "";
		} else if (command.equals("gogui-pattern-first-playout-move")) {
			result = goguiFirstPlayoutMove();
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
		if (winner == VACANT) {
			System.out
					.println("Incorporate run was given VACANT winner--should not happen");
			return;
		}
		int turn = runnable.getTurn();
		int[] moves = runnable.getMoves();
		Board b = new Board(maintainHashes);
		b.copyDataFrom(getBoard());
		for (int t = getBoard().getTurn(); t < turn; t++) {
			for (int pattern = 0; pattern <= NINE_PATTERN; pattern++) {
				PatternInformation info = getInformation(pattern,
						b.getPatternHash(pattern, moves[t]), b.getColorToPlay());
				for (int i = 0; i < patternWeight(pattern) * 20; i++) {
					if (b.getColorToPlay() == winner)
						info.addWin();
					else
						info.addLoss();
				}
			}
			b.play(moves[t]);
		}
	}

	private void initializePlayer(boolean maintain) {
		maintainHashes = maintain;
		patternWeights = new double[] { 1 / 20.0, 3 / 20.0, 6 / 20.0, 10 / 20.0 };
		setBoard(new Board(maintainHashes));
		playoutCount = new int[getFirstPointBeyondBoard()];
		loadPatternHashMaps();
		numPlayouts = 0;
		this.setInOpeningBook(false);

	}

	@SuppressWarnings("unchecked")
	private void loadPatternHashMaps() {
		for (int c = 0; c < 2; c++) {
			for (int i = 0; i < NINE_PATTERN + 1; i++) {
				// load from files
				try {
					ObjectInputStream ir = new ObjectInputStream(
							new FileInputStream(new File(
									"./testFiles/patternPlayed" + (i * 2 + 3)
											+ Colors.colorToString(c) + ".dat")));
					patterns[i][c] = (HashMap<Character, PatternInformation>) (ir
							.readObject());
					ir.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private double patternWeight(int pattern) {
		return patternWeights[pattern];
	}

	@Override
	public void reset() {
		super.reset();
		setUpRunnables();
	}

	protected void setUpRunnables() {
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new McRunnable(this, getHeuristics().clone()));
		}
	}

	public void setWeights(double threePattern, double fivePattern,
			double sevenPattern, double ninePattern) {
		double sum = threePattern + fivePattern + sevenPattern + ninePattern;
		patternWeights = new double[] { threePattern / sum, fivePattern / sum,
				sevenPattern / sum, ninePattern / sum };
	}

	protected String topPlayoutCount() {
		String result = "";
		for (int row = 0; row < 19; row++) {
			for (int col = 0; col < 19; col++) {
				result += playoutCount[at(row, col)];
			}
			result += '\n';
		}
		return result;
	}

	@Override
	public void updateForAcceptMove(int p) {
		setWeights(9 * getBoard().getTurn() / 220.0 + 1, 
				3 * getBoard().getTurn() / 220.0 + 3,
				3 * (1 - getBoard().getTurn() / 220.0) + 3, 
				9 * (1 - getBoard().getTurn() / 220.0) + 1);
	}

	@Override
	protected String winRateReport() {
		return "";
	}
}
