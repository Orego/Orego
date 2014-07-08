package edu.lclark.orego.time;

import static java.lang.Math.max;
import static edu.lclark.orego.util.Gaussian.Phi;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
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
	
	private int rollover;
	
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
		timePerSlice = (getMsecPerMove() + rollover) / SLICE_COUNT;
	}
	
	protected int getRollover(){
		return rollover;
	}
	
	@Override
	public void setRemainingTime(int seconds){
		timeRemaining = seconds;
		createSlices();
	}
	
	@Override
	public int getTime(){
		if(slices == 0 || confidenceBestVsRest() > .95){
			if(slices != 0){
				rollover = slices * timePerSlice;
			} else{
				rollover = 0;
			}
			return 0;
		}
		slices--;
		return timePerSlice;
	}
	
	/**
	 * Returns how confident we are (from 0.0 to 1.0) that the best move has a
	 * higher winrate than the rest of the legal moves.
	 */
	protected double confidenceBestVsRest() {
		SearchNode root = player.getRoot();
		CoordinateSystem coords = player.getBoard().getCoordinateSystem();
		// win rate and runs of the best move
		short bestMove = root.getMoveWithMostWins(coords);
		float bestWinRate = root.getWinRate(bestMove);
		int bestRuns = root.getRuns(bestMove);

		// runs and wins of the rest of the moves
		int restRuns = 0;
		int restWins = 0;
		for (short p : coords.getAllPointsOnBoard()) {
			if (p != bestMove && root.getWinRate(p) > 0.0) {
				float w = root.getWins(p);
				restWins += w;
				restRuns += root.getRuns(p);
			}
		}

		float restWinRate = restWins / (float) (restRuns);
		double c = confidence(bestWinRate, bestRuns, restWinRate, restRuns);
		return c;
	}
	
	protected static double confidence(float winrateA, double runsA, float winrateB,
			double runsB) {
		double z = (winrateA - winrateB)
				/ Math.sqrt(winrateA * (1 - winrateA) / runsA + winrateB
						* (1 - winrateB) / runsB);
		return Phi(z);
	}
}
