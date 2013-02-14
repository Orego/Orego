package orego.ladder;

import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;
import ec.util.MersenneTwisterFast;
import orego.heuristic.HeuristicList;
import orego.mcts.*;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;
import orego.book.OpeningBook;
import orego.core.*;

/**
 * This player should look for patterns likely to play out into ladders, 
 * and play out those ladders to determine if it can reach a rescue stone
 */
public class LadderPlayer extends Lgrf2Player {

	/** The Board this player plays on. */
	private Board board;
	
	/** Copy of board for playing out ladders*/
	private Board ladBoard;//
	
	private int[][] replies1;

	/** Returns the level 1 reply table. */
	protected int[][] getReplies1() {
		return replies1;
	}

	/** Indices are color, antepenultimate move, previous move. */
	private int[][][] replies2;

	/** Returns the level 2 replies table. */
	protected int[][][] getReplies2() {
		return replies2;
	}



	public static void main(String[] args) {
		try {
			LadderPlayer lad=new LadderPlayer();
			//lad.playOutLadders();
			lad.setProperty("heuristics", "Escape@20:Pattern@20:Capture@20");
			lad.setProperty("heuristic.Pattern.numberOfGoodPatterns", "400");
			lad.setProperty("threads", "1");
			double[] benchMarkInfo = lad.benchmark();
			System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
					+ benchMarkInfo[1]);
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
			
		}

	}
	
	@Override
	/** calls playOutLadders after stopping threads to bias stored moves*/	
	public int bestMove(){
		try {
			stopThreads();
//			if (getOpeningBook() != null) {
//				int move = getOpeningBook().nextMove(getBoard());
//				if (move != NO_POINT) {
//					return move;
//				}
//			}
			ladBoard=new Board(); //do we need malloc here?
			ladBoard.copyDataFrom(getBoard());
			playOutLadders();
			runThreads();
		} catch (InterruptedException shouldNotHappen) {
			shouldNotHappen.printStackTrace();
			System.exit(1);
		}
		return bestStoredMove();
	}

	public int playOutLadders() {
		//threads should have been stopped by bestMove at this point
		findLadChains();
		
		return 0;
	}
	/** Identifies which chains in atari are likely to play out into ladders*/	
	public int findLadChains(){
		IntSet lads2play=new IntSet(365);
		IntSet ataris=ladBoard.getChainsInAtari(ladBoard.getColorToPlay());
		
		int lib=ladBoard.getLibertyOfChainInAtari(ataris.get(0));
		if(ladBoard.getLibertyCount(lib)>2){
			
		}
		else{
			//IntSet lad2play;
			lads2play.add(lib);
		}
String libString=Coordinates.pointToString(lib);
		System.err.println("E:Liberites of first Chain in Atari: "+libString);
		return 0;
	}
}
