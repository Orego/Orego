package orego.mcts;

import orego.core.Board;
import orego.response.ResponsePlayer;

import org.junit.Test;

public class MctsResponsePlayerTest extends MctsPlayerTest {
	
	@Override
	@Test
	public void setUp() throws Exception {
		player = new MctsResponsePlayer();
		player.setProperty("priors", "0");
		player.setProperty("threads", "1");
		player.setPlayoutLimit(1000);
		player.reset();
		board = new Board();
	}
	
	@Override
	public long indexOfBoard(Board board) {
		int turn 		 = board.getTurn();
		int prevMove 	 = board.getMove(turn - 1);
		int prevPrevMove = board.getMove(turn - 2);
		
		return ResponsePlayer.levelTwoEncodedIndex(prevPrevMove, prevMove, board.getColorToPlay());
	}
}
