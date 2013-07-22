package orego.heuristic;

import static orego.core.Board.*;
import static orego.core.Coordinates.getBoardWidth;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import orego.core.Board;
import orego.mcts.Lgrf2Player;
import orego.play.UnknownPropertyException;
import orego.shape.Cluster;
import orego.util.IntSet;

@SuppressWarnings("unchecked")
public class DeepPatternHeuristic extends Heuristic{
	
	public static void main(String[] args) {
		try {
			Lgrf2Player p = new Lgrf2Player();
			p.setProperty("heuristics", "Escape@20:Pattern@20:Capture@20:DeepPattern@20");
			p.setProperty("heuristic.Pattern.numberOfGoodPatterns", "400");
			p.setProperty("threads", "1");
			double[] benchMarkInfo = p.benchmark();
			System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
					+ benchMarkInfo[1]);
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static Cluster patterns;
	
	public static final double GOOD_PATTERN_THRESHOLD = .65;
		
	public DeepPatternHeuristic(int weight) {
		super(weight);
//		patternWeights = new double[] { 1 / 20.0, 3 / 20.0, 6 / 20.0, 10 / 20.0 };
	}
	
	public void prepare(Board board, boolean local){
		super.prepare(board, local);
		if (!local) {
			// Update weights to pay more attention to smaller patterns later in game
//			patternWeights[0] = 9 * board.getTurn() / 220.0 + 1;
//			patternWeights[1] = 3 * board.getTurn() / 220.0 + 3;
//			patternWeights[2] = 3 * (1 - board.getTurn() / 220.0) + 3;
//			patternWeights[3] = 9 * (1 - board.getTurn() / 220.0) + 1;
//			double sum = 0;
//			for (int i = 0; i < patternWeights.length; i++) {
//				sum += patternWeights[i];
//			}
//			for (int i = 0; i < patternWeights.length; i++) {
//				patternWeights[i] /= sum;
//			}
			IntSet moves = board.getVacantPoints();
			for (int i = 0; i < moves.size(); i++) {
				//if (board.isFeasible(moves.get(i))) {
					float tempRate = (float) patterns.getWinRate(board, moves.get(i));
					if (tempRate > GOOD_PATTERN_THRESHOLD) {
						if(board.isLegal(moves.get(i))){
							recommend(moves.get(i));
						}
					}
				//}
			}
		}
	}

	static {
		for (int c = 0; c < 2; c++) {
			for (int i = 0; i < MAX_PATTERN_RADIUS + 1; i++) {
				// load from files
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
		}
		
	}

}
