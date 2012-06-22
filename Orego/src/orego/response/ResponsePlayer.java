package orego.response;

import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.core.Coordinates;

public class ResponsePlayer extends McPlayer {
	
	ResponseList responseZero;
	ResponseList[] responseOne;
	ResponseList[][] responseTwo;
	
	public ResponsePlayer(){
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
		// TODO Auto-generated method stub

	}
	
	@Override
	public int getPlayouts(int p) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getWinRate(int p) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWins(int p) {
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
	public int bestStoredMove() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateForAcceptMove(int p) {
		// TODO Auto-generated method stub

	}

}
