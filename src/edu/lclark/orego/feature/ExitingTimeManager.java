package edu.lclark.orego.feature;

import static java.lang.Math.max;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.mcts.Player;

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
		if(slices == 0 || confident){
			return 0;
		}
		slices--;
		return timePerSlice;
	}
}
