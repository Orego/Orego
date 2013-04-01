package orego.mcts;

import static orego.core.Board.PLAY_OK;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.pointToString;
import static orego.patterns.Pattern.getLowestTransformation;

import java.util.Arrays;

import orego.core.Board;
import orego.heuristic.HeuristicList;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

public class SoftmaxPolicy {
	
	public static double exp(double val) {
		final long tmp = (long) (1512775 * val + 1072632447);
		return Double.longBitsToDouble(tmp << 32);
	}
	
	protected double[] weights;
	protected double[] scores = new double[FIRST_POINT_BEYOND_BOARD];
	protected char[] lowestTransforms = new char[Character.MAX_VALUE];
	
	public SoftmaxPolicy(double[] weights) {
		this.weights = weights;
		for(int idx = 0; idx < lowestTransforms.length; idx++) {
			lowestTransforms[idx] = getLowestTransformation((char)idx);
		}
	}
	
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		IntSet vacantPoints = board.getVacantPoints();
		double total = 0.0f;
		Arrays.fill(scores, 0.0f);
		for(int idx = 0; idx < vacantPoints.size(); idx++) {
			int pt = vacantPoints.get(idx);
			char neighborhood = board.getNeighborhood(pt);
			neighborhood = lowestTransforms[neighborhood];
			double weight = weights[neighborhood];
			double score = exp(weight);
			//System.out.println(pointToString(pt) + " : " + score);
			scores[idx] = score;
			total += score;
		}
		int passes = 0;
		while(total > 0 && passes < 100) {
			double sel = random.nextDouble() * total;
			double weightsSum = 0.0f;
			for(int idx = 0; idx < vacantPoints.size(); idx++) {
				weightsSum += scores[idx];
				if(weightsSum > sel) {
					int selected = vacantPoints.get(idx);
					if(board.isFeasible(selected) && board.playFast(selected) == PLAY_OK) {
						return selected;
					}
					else {
						total -= scores[idx];
						scores[idx] = 0;
						break;
					}
				}
			}
			passes++;
		}
		return HeuristicList.selectAndPlayUniformlyRandomMove(random, board);
	}
	
}
