package edu.lclark.orego.feature;

import static java.lang.Math.max;
import static edu.lclark.orego.util.Gaussian.Phi;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.SearchNode;

public class ExitingTimeManager implements TimeManager{

	/** The constant C to use in the time management formula. */
	private double timeC = 0.20;

	private final Board board;
	
	private final Player player;
	
	private int slices;
	
	private int timePerSlice;
	
	private int timeRemaining;
	
	private static final int SLICE_COUNT = 10;
	
	public ExitingTimeManager(Player player){
		this.player = player;
		this.board = player.getBoard();
		setRemainingTime(60);
	}

	private int getMsecPerMove() {
		int movesLeft = max(10, (int) (board.getVacantPoints().size() * timeC));
		int msPerMove = max(1, (timeRemaining * 1000) / movesLeft);
		msPerMove = max(1, msPerMove);
		return msPerMove;
	}
	
	private void createSlices(){
		slices = SLICE_COUNT;
		timePerSlice = getMsecPerMove() / SLICE_COUNT;
		
	}
	
	@Override
	public void setRemainingTime(int seconds){
		timeRemaining = seconds;
		createSlices();
	}
	
	@Override
	public int getTime(){
		SearchNode root = player.getRoot();
		short best = root.getMoveWithMostWins(board.getCoordinateSystem());
		short nextBest = root.getMoveWithNextMostWins(board.getCoordinateSystem(), best);
		float winrateA = root.getWinRate(best);
		double runsA = root.getWins(best);
		float winrateB = root.getWinRate(nextBest);
		double runsB = root.getRuns(nextBest);
		if(slices == 0 || confidence(winrateA, runsA, winrateB, runsB) > .75){
			return 0;
		}
		slices--;
		return timePerSlice;
	}
	
	protected static double confidence(float winrateA, double runsA, float winrateB,
			double runsB) {
		double z = (winrateA - winrateB)
				/ Math.sqrt(winrateA * (1 - winrateA) / runsA + winrateB
						* (1 - winrateB) / runsB);
		return Phi(z);
	}
}
