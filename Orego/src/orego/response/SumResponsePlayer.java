package orego.response;

import static orego.core.Coordinates.PASS;

import java.util.HashMap;

import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;
import orego.mcts.McRunnable;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

public class SumResponsePlayer extends ResponsePlayer {
	
	
	public static void main(String[] args) {
		ResponsePlayer p = new ResponsePlayer();
		try {
			p.setProperty("policy", "Escape:Pattern:Capture");
			p.setProperty("threads", "2");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		double[] benchMarkInfo = p.benchmark();
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: " + benchMarkInfo[1]);
	}
	
	@Override
	protected int findAppropriateMove(Board board, int history1, int history2, MersenneTwisterFast random) {
		int turn = board.getTurn();
		int colorToPlay = board.getColorToPlay();
		double sum = 0;
		int currMove;
		int bestMove = Coordinates.PASS;
		double bestSum = 0;
		
		// pick table based on threshold values
		RawResponseList twoList = (RawResponseList)getResponses().get(levelTwoEncodedIndex(history2, history1, colorToPlay));
		RawResponseList oneList = (RawResponseList)getResponses().get(levelOneEncodedIndex(history1, colorToPlay));
		RawResponseList zeroList = (RawResponseList)getResponses().get(levelZeroEncodedIndex(colorToPlay));
		
		IntSet vacantPoints = board.getVacantPoints();
		for(int i = 0; i < vacantPoints.size(); i++) {
			currMove = vacantPoints.get(i);
			int wins = zeroList.getWins(currMove);
			wins += oneList.getWins(currMove);
			wins += twoList.getWins(currMove);
			int runs = zeroList.getRuns(currMove);
			runs += oneList.getRuns(currMove);
			runs += twoList.getRuns(currMove);
			sum = (double)wins/runs;
			if(sum > bestSum){
				bestSum = sum;
				bestMove = currMove;
			}
		}
		return bestMove;
		
	}

}
