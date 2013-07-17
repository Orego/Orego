package orego.heuristic;

import static orego.core.Board.NINE_PATTERN;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import orego.core.Board;
import orego.core.Colors;
import orego.patternanalyze.PatternInformation;
import orego.util.IntSet;

@SuppressWarnings("unchecked")
public class DeepPatternHeuristic extends Heuristic{
	
	// TODO Use a direct-addressing table instead of a hash table
	public static final HashMap<Character, PatternInformation>[][] PATTERNS = new HashMap[4][2];
	
	private double[] patternWeights;
	
	public static final double GOOD_PATTERN_THRESHOLD = .75;
		
	public DeepPatternHeuristic(int weight) {
		super(weight);
		patternWeights = new double[] { 1 / 20.0, 3 / 20.0, 6 / 20.0, 10 / 20.0 };
	}
	
	public void prepare(Board board, boolean local){
		super.prepare(board, local);
		if (!local) {
			// Update weights to pay more attention to smaller patterns later in game
			patternWeights[0] = 9 * board.getTurn() / 220.0 + 1;
			patternWeights[1] = 3 * board.getTurn() / 220.0 + 3;
			patternWeights[2] = 3 * (1 - board.getTurn() / 220.0) + 3;
			patternWeights[3] = 9 * (1 - board.getTurn() / 220.0) + 1;
			double sum = 0;
			for (int i = 0; i < patternWeights.length; i++) {
				sum += patternWeights[i];
			}
			for (int i = 0; i < patternWeights.length; i++) {
				patternWeights[i] /= sum;
			}
			IntSet moves = board.getVacantPoints();
			for (int i = 0; i < moves.size(); i++) {
				if (board.isFeasible(moves.get(i))) {
					float tempRate = (float) getWinRate(board, moves.get(i));
					if (tempRate > GOOD_PATTERN_THRESHOLD) {
						if(board.isLegal(moves.get(i))){
							recommend(moves.get(i));
						}
					}
				}
			}
		}
	}	
	
	public float getWinRate(Board b, int point) {
		float result = 0;
		for (int pattern = 0; pattern <= NINE_PATTERN; pattern++) {
			result += PATTERNS[pattern][b.getColorToPlay()].get(b.getPatternHash(pattern, point)).getRate() * patternWeights[pattern];
		}
		return result;
	}

	static {
		for (int c = 0; c < 2; c++) {
			for (int i = 0; i < NINE_PATTERN + 1; i++) {
				// load from files
				try {
					ObjectInputStream ir = new ObjectInputStream(
							new FileInputStream(new File(
									"./testFiles/patternPlayed" + (i * 2 + 3)
											+ Colors.colorToString(c) + ".dat")));
					PATTERNS[i][c] = (HashMap<Character, PatternInformation>) (ir
							.readObject());
					ir.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}
