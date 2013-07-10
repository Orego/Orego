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
import orego.patternanalyze.PatternInformation;
import orego.util.IntSet;

public class PatternPlayer extends McPlayer {

	@SuppressWarnings("unchecked")
	private HashMap<Character, PatternInformation>[][] patterns = new HashMap[4][2];

	private long numPlayouts;

	public PatternPlayer() {
		loadPatternHashMaps();
		numPlayouts = 0;
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

	public static void main(String[] args) {
		new PatternPlayer();
	}

	public PatternInformation getInformation(int patternType, char hash) {
		PatternInformation toReturn = patterns[patternType][getBoard()
				.getColorToPlay()].get(hash);

		if (toReturn != null) {
			return toReturn;
		} else {
			return new PatternInformation();
		}
	}

	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-pattern-info-3");
		result.add("gogui-pattern-info-5");
		result.add("gogui-pattern-info-7");
		result.add("gogui-pattern-info-9");
		result.add("gogui-combined-pattern-info");
		// result.add("gogui-pattern-win-rates-3");
		// result.add("gogui-pattern-win-rates-5");
		// result.add("gogui-pattern-win-rates-7");
		// result.add("gogui-pattern-win-rates-9");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("gfx/3x3 pattern info/gogui-pattern-info-3");
		result.add("gfx/5x5 pattern info/gogui-pattern-info-5");
		result.add("gfx/7x7 pattern info/gogui-pattern-info-7");
		result.add("gfx/9x9 pattern info/gogui-pattern-info-9");
		result.add("gfx/Combined pattern info/gogui-combined-pattern-info");
		// result.add("gfx/Pattern win rates/gogui-pattern-win-rates-3");
		// result.add("gfx/Pattern win rates/gogui-pattern-win-rates-5");
		// result.add("gfx/Pattern win rates/gogui-pattern-win-rates-7");
		// result.add("gfx/Pattern win rates/gogui-pattern-win-rates-9");
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
			// } else if (command.contains("gogui-pattern-win-rates")) {
			// result =
			// goguiPatternWinRates((command.charAt(command.length()-1)-'0')/2-1);
		} else {
			result = super.handleCommand(command, arguments);
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return result;
	}

	private String goguiPatternInfo(int patternType) {
		String result = "";
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				if (result.length() > 0)
					result += '\n';
				PatternInformation info = getInformation(patternType,
						getBoard().getPatternHash(patternType, p));
				result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
						colorCode(info.getRate()), pointToString(p),
						pointToString(p), info.getRate() * 100);
			}
		}
		return result;
	}

	private String goguiCombinedPatternInfo() {
		String result = "";
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				if (result.length() > 0)
					result += '\n';
				float totalRate = (float) getWinRate(p);
				// int totalRuns = 0;
				// for (int i = 0; i < NINE_PATTERN + 1; i++) {
				// PatternInformation info = getInformation(i, getBoard()
				// .getPatternHash(i, p));
				// totalRuns += info.getRuns();
				// }
				result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
						colorCode(totalRate), pointToString(p),
						pointToString(p), totalRate * 100);
			}
		}
		return result;
	}

	@Override
	public int bestStoredMove() {
		return bestMove(getBoard());
	}

	private int bestMove(Board b) {
		int result = 0;
		float rate = 0;
		IntSet moves = getBoard().getVacantPoints();
		for (int i = 0; i < moves.size(); i++) {
			if (getBoard().isLegal(moves.get(i))) {
				float tempRate = (float) getWinRate(moves.get(i));
				if (tempRate > rate) {
					rate = tempRate;
					result = moves.get(i);
					// System.out.println("Current best: "+pointToString(result)+":"+rate);
				}
			}
		}
		// System.out.println("Best stored move: "+pointToString(result));
		if (rate < .25)
			result = PASS;
		return result;
	}

	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		runnable.getBoard().copyDataFrom(getBoard());
		while (runnable.getBoard().getPasses() < 2) {
			runnable.acceptMove(bestMove(runnable.getBoard()));
		}
		numPlayouts++;
	}

	@Override
	public long getPlayouts(int p) {
		return numPlayouts;
	}

	@Override
	public double getWinRate(int point) {
		float tempRate = 0;
		for (int pattern = 0; pattern <= NINE_PATTERN; pattern++) {
			PatternInformation info = getInformation(pattern, getBoard()
					.getPatternHash(pattern, point));
			tempRate += info.getRate() * patternWeight(pattern);
		}
		return tempRate;
	}

	private double patternWeight(int pattern) {
		return ((pattern + 1) * (pattern + 2) / 2)
				* ((pattern + 1) * (pattern + 2) / 2) / 146.0f;
	}

	@Override
	public double getWins(int p) {
		float winRate = 0;
		double runs = 0;
		for (int pattern = 0; pattern <= NINE_PATTERN; pattern++) {
			PatternInformation info = getInformation(pattern, getBoard()
					.getPatternHash(pattern, p));
			winRate += info.getRate() * patternWeight(pattern);
			runs += info.getRuns() * patternWeight(pattern);
		}
		return winRate * runs;
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
		Board b = new Board();
		for (int t = 0; t < turn; t++) {
			if (t >= getBoard().getTurn()) {
				for (int pattern = 0; pattern <= NINE_PATTERN; pattern++) {
					PatternInformation info = getInformation(pattern,
							getBoard().getPatternHash(pattern, moves[t]));
					if (t % 2 == winner)
						info.addWin();
					else
						info.addLoss();
				}
			}
			b.play(moves[t]);
		}
	}

	@Override
	protected String winRateReport() {
		return "";
	}

	@Override
	public void beforeStartingThreads() {
	}

	@Override
	public void updateForAcceptMove(int p) {
	}
}
