package orego.response;

import orego.core.Board;
import orego.core.Coordinates;
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
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
				+ benchMarkInfo[1]);
	}

	@Override
	protected int findAppropriateMove(Board board, int history1, int history2,
			MersenneTwisterFast random) {
		int colorToPlay = board.getColorToPlay();
		int bestMove = Coordinates.PASS;
		double bestSum = 0;
		boolean skipOne = false;
		boolean skipTwo = false;

		// pick table based on threshold values
		// TODO: these *might* be null, might want to check.
		// All tables have all moves *unless* there is a pass in which case only
		// the second level table has an entry. Luckily, only the vacantPoints array
		// should not have pass?
		RawResponseList twoList = (RawResponseList) getResponses().get(
				levelTwoEncodedIndex(history2, history1, colorToPlay));
		RawResponseList oneList = (RawResponseList) getResponses().get(
				levelOneEncodedIndex(history1, colorToPlay));
		RawResponseList zeroList = (RawResponseList) getResponses().get(
				levelZeroEncodedIndex(colorToPlay));

		skipOne = (oneList==null);
		skipTwo = (twoList==null);

		IntSet vacantPoints = board.getVacantPoints();
		for (int i = 0; i < vacantPoints.size(); i++) {
			int currMove = vacantPoints.get(i);
			int wins = zeroList.getWins(currMove);
			wins = skipOne ? wins : wins+oneList.getWins(currMove);
			wins = skipTwo ? wins : wins+twoList.getWins(currMove);
			int runs = zeroList.getRuns(currMove);
			runs = skipOne ? runs : runs+oneList.getRuns(currMove);
			runs = skipTwo ? runs : twoList.getRuns(currMove);
			double sum = (double) wins / runs;
			if (sum > bestSum) {
				if (board.isFeasible(currMove) && board.isLegal(currMove)) {
					bestSum = sum;
					bestMove = currMove;
				}
			}
		}
		return bestMove;

	}

}
