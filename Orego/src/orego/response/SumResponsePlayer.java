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
	
	/** Threshold for the first level table*/
	private int one_threshold = 100;
	
	/** Threshold for the second level table*/
	private int two_threshold = 100;
	
	private HashMap<Integer, AbstractResponseList> responses;
	
	/** Weight for updateResponses */
	private int priorsWeight;
	
	/** Default weight for updateResponses */
	private static final int DEFAULT_WEIGHT = 1;
	
	private final int HISTORY_DEPTH = 2; 
	
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
	
	public SumResponsePlayer(){
		super();
		responses = new HashMap<Integer, AbstractResponseList>();
		
		priorsWeight = DEFAULT_WEIGHT;
		
		int arrayLength = Coordinates.FIRST_POINT_BEYOND_BOARD;
		
		// black level zero table
		responses.put(levelZeroEncodedIndex(Colors.BLACK), new RawResponseList());
		
		// white level zero table
		responses.put(levelZeroEncodedIndex(Colors.WHITE), new RawResponseList());

	}
	
	@Override
	public int bestStoredMove() {
		Board board = getBoard();
		if (board.getPasses() == 1 && secondPassWouldWinGame()) {
			return PASS;
		}
		int history1 = board.getMove(board.getTurn() - 1);
		int history2 = board.getMove(board.getTurn() - 2);
		
		// we need a random generator
		// threads are stopped at this point so we are blithely unaware of synchronization concerns
		MersenneTwisterFast random = ((McRunnable)getRunnable(0)).getRandom();
		int move = findAppropriateMove(board, history1, history2, random);
		
		AbstractResponseList res = responses.get(levelTwoEncodedIndex(history2, history1, board.getColorToPlay()));
		if(res != null && res.getWinRate(move) < 0.1) {
			return Coordinates.RESIGN;
		}
		return move;
	}
	
	protected int findAppropriateMove(Board board, int history1, int history2, MersenneTwisterFast random) {
		int turn = board.getTurn();
		int colorToPlay = board.getColorToPlay();
		int sum = 0;
		int currMove;
		int bestMove = Coordinates.PASS;
		int bestSum = 0;
		
		// pick table based on threshold values
		RawResponseList twoList = (RawResponseList)responses.get(levelTwoEncodedIndex(history2, history1, colorToPlay));
		RawResponseList oneList = (RawResponseList)responses.get(levelOneEncodedIndex(history1, colorToPlay));
		RawResponseList zeroList = (RawResponseList)responses.get(levelZeroEncodedIndex(colorToPlay));
		
		IntSet vacantPoints = board.getVacantPoints();
		for(int i = 0; i < vacantPoints.size(); i++) {
			currMove = vacantPoints.get(i);
			sum = zeroList.getWins(currMove);
			sum += oneList.getWins(currMove);
			sum += twoList.getWins(i);
		}
		return bestMove;
		
	}
	
	public HashMap<Integer, AbstractResponseList> getResponses() {
		return responses;
	}
}
