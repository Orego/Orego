package orego.mcts;

import java.util.ArrayList;
import java.util.List;

import ec.util.MersenneTwisterFast;

import static orego.core.Coordinates.BOARD_AREA;
import static orego.core.Coordinates.pointToString;
import static orego.core.Coordinates.ON_BOARD;

import orego.core.Board;
import orego.play.Player;
import orego.util.IntSet;
import orego.util.Pair;

public class WeightTrainingPlayer extends Player {
	List<Pair<Character, Double>> logPiGradients;
	SoftmaxPolicy basePi;
	
	public WeightTrainingPlayer() {
		double weights[] = new double[Character.MAX_VALUE + 1];
		basePi = new SoftmaxPolicy(weights);
	}
	
	public void setWeight(int idx, double weight) {
		basePi.weights[idx] = weight;
	}
	
	public double getWeight(int idx) {
		return basePi.weights[idx];
	}
	
	public void setWeights(double[] weights) {
		basePi.weights = weights;
	}
	
	public double[] getWeights() {
		return basePi.weights;
	}
	
	public List<Pair<Character, Double>> getGradients() {
		return logPiGradients;
	}
	
	public void reset() {
		super.reset();
		logPiGradients = new ArrayList<Pair<Character,Double>>(BOARD_AREA);
	}
	
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		// Have the softmax policy select the move
		int selectedMove = basePi.selectAndPlayOneMove(random, board);
		if(!ON_BOARD[selectedMove]) return selectedMove;
		undo();
		
		// Normalize the scores
		double total = 0.0;
		for(int idx = 0; idx < basePi.scores.length; idx++) {
			total += basePi.scores[idx];
		}
		for(int idx = 0; idx < basePi.scores.length; idx++) {
			basePi.scores[idx] /= total;
		}
		
		// Now calculate grad log(pi(board, selectedMove))
		char feature = basePi.lowestTransforms[board.getNeighborhood(selectedMove)];
		double prob = 0.0;
		IntSet vacantPoints = board.getVacantPoints();
		for(int idx = 0; idx < vacantPoints.size(); idx++) {
			int pt = vacantPoints.get(idx);
			if(basePi.lowestTransforms[board.getNeighborhood(pt)] == feature) {
				prob += basePi.scores[pt];
			}
		}
		logPiGradients.add(Pair.fromValues(feature, 1 - prob));
		// System.out.println(logPiGradients.get(logPiGradients.size() - 1));

		// Play and return the selected move
		board.playFast(selectedMove);
		return selectedMove;
	}
}
