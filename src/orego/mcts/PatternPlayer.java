package orego.mcts;

import static orego.core.Board.*;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.getAllPointsOnBoard;
import static orego.core.Coordinates.pointToString;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Board;
import orego.core.Colors;
import orego.patternanalyze.PatternInformation;

public class PatternPlayer extends MctsPlayer {

	HashMap<Character, PatternInformation> threePatterns;
	HashMap<Character, PatternInformation> fivePatterns;
	HashMap<Character, PatternInformation> sevenPatterns;
	HashMap<Character, PatternInformation> ninePatterns;
	@SuppressWarnings("unchecked")
	HashMap<Character, PatternInformation>[][] patterns = new HashMap[4][2];

	@SuppressWarnings("unchecked")
	public PatternPlayer() {
		for (int c = 0; c < 2; c++) {
			for (int i = 0; i < NINE_PATTERN + 1; i++) {
				// load from files
				try {
					ObjectInputStream ir = new ObjectInputStream(
							new FileInputStream(new File(
									"../testFiles/patternPlayed" + (i * 2 + 3)
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
		PatternInformation toReturn = patterns[patternType][getBoard().getColorToPlay()].get(hash);
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
				result += String.format("COLOR %s %s\nLABEL %s %d",
						colorCode(info.getRate()), pointToString(p),
						pointToString(p), info.getRuns());
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
				float totalRate = 0;
				int totalRuns = 0;
				for (int i = 0; i < NINE_PATTERN + 1; i++) {
					PatternInformation info = getInformation(i, getBoard()
							.getPatternHash(i, p));
					totalRate += info.getRate() * ((i + 1) * (i + 2) / 2);
					totalRuns += info.getRuns();
				}
				totalRate /= 20.0f;
				result += String.format("COLOR %s %s\nLABEL %s %d",
						colorCode(totalRate), pointToString(p),
						pointToString(p), totalRuns);
			}
		}
		return result;
	}

/*	private String goguiPatternRuns(int patternType) {
		String result = "";
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = getWinRate(p);
				if (winRate > 0) {
					if (result.length() > 0)
						result += '\n';
					result += String.format(
							"LABEL %s %.0f%%",
							pointToString(p),
							getInformation(patternType,
									getBoard().getPatternHash(patternType, p))
									.getRuns());
				}
			}
		}
		return result;
	}*/
}
