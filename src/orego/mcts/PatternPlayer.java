package orego.mcts;

import static orego.core.Board.NINE_PATTERN;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.getAllPointsOnBoard;
import static orego.core.Coordinates.pointToString;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Colors;
import orego.core.Coordinates;
import orego.patternanalyze.PatternInformation;
import orego.util.IntSet;

public class PatternPlayer extends McPlayer {

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
						pointToString(p), info.getRate()*100);
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
				float totalRate = getCombinedRate(p);
//				int totalRuns = 0;
//				for (int i = 0; i < NINE_PATTERN + 1; i++) {
//					PatternInformation info = getInformation(i, getBoard()
//							.getPatternHash(i, p));
//					totalRuns += info.getRuns();
//				}
				result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
						colorCode(totalRate), pointToString(p),
						pointToString(p), totalRate*100);
			}
		}
		return result;
	}

	@Override
	public int bestStoredMove() {
		int result = 0;
		float rate = 0;
		IntSet moves = getBoard().getVacantPoints();
		for (int i = 0; i < moves.size(); i++) {
			if (//(getBoard().isFeasible(moves.get(i)) &&
					getBoard().isLegal(moves.get(i))) {
				float tempRate = getCombinedRate(moves.get(i));
				if (tempRate>rate){
					rate = tempRate;
					result = moves.get(i);
					//System.out.println("Current best: "+pointToString(result)+":"+rate);
				}
			}
		}
		//System.out.println("Best stored move: "+pointToString(result));
		return result;
	}
	
	private float getCombinedRate(int point){
		float tempRate = 0;
		for (int pattern = 0; pattern <= NINE_PATTERN; pattern++) {
			PatternInformation info = getInformation(pattern,
					getBoard().getPatternHash(pattern, point));
			tempRate+=info.getRate()* ((pattern + 1) * (pattern + 2) / 2)* ((pattern + 1) * (pattern + 2) / 2);
		}
		tempRate /= 146.0f;
		return tempRate;
	}

	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		// TODO Auto-generated method stub
	}

	@Override
	public long getPlayouts(int p) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getWinRate(int point) {
		float tempRate = 0;
		for (int pattern = 0; pattern <= NINE_PATTERN; pattern++) {
			PatternInformation info = getInformation(pattern,
					getBoard().getPatternHash(pattern, point));
			tempRate+=info.getRate()* ((pattern + 1) * (pattern + 2) / 2)* ((pattern + 1) * (pattern + 2) / 2);
		}
		tempRate /= 146.0f;
		return tempRate;
	}

	@Override
	public double getWins(int p) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String winRateReport() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeStartingThreads() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateForAcceptMove(int p) {
		// TODO Auto-generated method stub
		
	}
}
