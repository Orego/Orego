package orego.response;

import ec.util.MersenneTwisterFast;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.util.IntSet;
import orego.core.Board;
import orego.core.Coordinates;

public class ResponsePlayer extends McPlayer {
	
	public static final int THRESHOLD = 100;
	
	ResponseList responseZero;
	ResponseList[] responseOne;
	ResponseList[][] responseTwo;
	
	public ResponsePlayer(){
		super();
		int arrayLength = Coordinates.FIRST_POINT_BEYOND_BOARD;
		responseZero = new ResponseList();
		responseOne = new ResponseList[arrayLength];
		responseTwo = new ResponseList[arrayLength][arrayLength];
		for (int i = 0; i < arrayLength; i++) {
			responseOne[i] = new ResponseList();
			responseTwo[i] = new ResponseList[arrayLength];
			for (int j = 0; j < arrayLength; j++) {
				responseTwo[i][j] = new ResponseList();
			}
		}
	}

	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		MersenneTwisterFast random = runnable.getRandom();
		runnable.getBoard().copyDataFrom(getBoard());
		Board board = runnable.getBoard();
		//play the first move in our list(s)
		if (board.getTurn() >= 2 && responseTwo[(board.getTurn()-2)][board.getTurn()-1].getTotalRuns() >= THRESHOLD) {
			int counter = 1;
			int move = responseTwo[(board.getTurn()-2)][board.getTurn()-1].getMoves()[0];
			while (! board.isLegal(responseTwo[(board.getTurn()-2)][board.getTurn()-1].getMoves()[move])) {
				move = responseTwo[(board.getTurn()-2)][board.getTurn()-1].getMoves()[counter];
				counter++;
			}
			board.play(move);
		}
		else if (board.getTurn() >= 1 && responseOne[board.getTurn()-1].getTotalRuns() >= THRESHOLD) {
			int counter = 1;
			int move = responseOne[board.getTurn()-1].getMoves()[0];
			while (! board.isLegal(responseOne[board.getTurn()-1].getMoves()[move])) {
				move = responseOne[board.getTurn()-1].getMoves()[counter];
				counter++;
			}
			board.play(move);
		}
		else {
			int counter = 1;
			int move = responseZero.getMoves()[0];
			while (! board.isLegal(responseZero.getMoves()[move])) {
				move = responseZero.getMoves()[counter];
				counter++;
			}
			board.play(move);
		}
		while (board.getPasses() < 2) {
			IntSet vacantPoints = runnable.getBoard().getVacantPoints();
			int move = random.nextInt(vacantPoints.size());
			if (board.isLegal(move) && board.isFeasible(move)){
				board.play(move);
			}
		}
	}
	
	@Override
	public int getPlayouts(int p) {
		return responseZero.getTotalRuns();
	}

	@Override
	public double getWinRate(int p) {
		return responseZero.getWinRate(p);
	}

	@Override
	public int getWins(int p) {
		return responseZero.getWins(p);
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
	public int bestStoredMove() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateForAcceptMove(int p) {
		// TODO Auto-generated method stub

	}

}
