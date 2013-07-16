package orego.heuristic;

import static orego.core.Board.NINE_PATTERN;
import static orego.core.Coordinates.getFirstPointBeyondBoard;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import orego.core.Board;
import orego.core.Colors;
import orego.patternanalyze.PatternInformation;
import orego.util.IntSet;

public class DeepPatternHeuristic extends Heuristic{
	
	@SuppressWarnings("unchecked")
	private HashMap<Character, PatternInformation>[][] patterns = new HashMap[4][2];
	
	private double[] patternWeights;
	
	private double goodPatternThreshold = .75;
	
	
	public DeepPatternHeuristic(int weight) {
		super(weight);
		initializeHeuristic();
	}
	
	public void prepare(Board board, boolean local){
		super.prepare(board, local);
		updateForAcceptMove(board);
		IntSet moves = board.getVacantPoints();
		for (int i = 0; i < moves.size(); i++) {
			if (board.isFeasible(moves.get(i))) {
				float tempRate = (float) getWinRate(board, moves.get(i));
				if (tempRate > goodPatternThreshold) {
					if(board.isLegal(moves.get(i))){
						recommend(moves.get(i));
					}
				}
			}
		}
	}
	
	private double patternWeight(int pattern) {
		return patternWeights[pattern];
	}
	
	public void setWeights(double threePattern, double fivePattern,
			double sevenPattern, double ninePattern) {
		double sum = threePattern + fivePattern + sevenPattern + ninePattern;
		patternWeights = new double[] { threePattern / sum, fivePattern / sum,
				sevenPattern / sum, ninePattern / sum };
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
	
	public PatternInformation getInformation(int patternType, char hash,
			int color) {
		PatternInformation toReturn = patterns[patternType][color].get(hash);

		if (toReturn != null) {
			return toReturn;
		} else {
			return new PatternInformation();
		}
	}
	
	private void initializeHeuristic() {
		patternWeights = new double[] { 1 / 20.0, 3 / 20.0, 6 / 20.0, 10 / 20.0 };
		loadPatternHashMaps();
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
	
	public void updateForAcceptMove(Board board) {
		setWeights(9 * board.getTurn() / 220.0 + 1, 
				3 * board.getTurn() / 220.0 + 3,
				3 * (1 - board.getTurn() / 220.0) + 3, 
				9 * (1 - board.getTurn() / 220.0) + 1);
	}

}
