package orego.mcts;

import java.util.ArrayList;
import java.util.List;

import ec.util.MersenneTwisterFast;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.BOARD_AREA;
import static orego.core.Coordinates.pointToString;
import static orego.core.Coordinates.ON_BOARD;
import static orego.core.Coordinates.NO_POINT;

import orego.core.Board;
import orego.heuristic.Heuristic;
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
	
	private int selectAndPlayHeuristicMove(MersenneTwisterFast random, Board board) {
		for (Heuristic h : this.getHeuristics().getHeuristics()) {
			h.prepare(board);
			IntSet good = h.getGoodMoves();
			if (good.size() > 0) {
				int start = random.nextInt(good.size());
				int i = start;
				do {
					int p = good.get(i);
					if ((board.getColor(p) == VACANT) && (board.isFeasible(p)) && (board.playFast(p) == PLAY_OK)) {
						return p;
					}
					// Advancing by 457 skips "randomly" through the array,
					// in a manner analogous to double hashing.
					i = (i + 457) % good.size();
				} while (i != start);
			}
		}
		return NO_POINT;
	}
	
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int heuristicMove = this.selectAndPlayHeuristicMove(random, board);
		if(heuristicMove != NO_POINT) {
			return heuristicMove;
		}
		
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
