package tictactoe;

import static org.junit.Assert.*;

import org.junit.Test;

public class BoardTest {
	Board board= new Board();
	

	@Test
	public void testPlay() {
		board.play(1,0,1);
		assertEquals(1,board.get(1,0));
		
		
		
	}
	@Test
	public void testVacancy() {
		System.out.println(board);
		board.play(1,0,1);
		assertEquals(false,board.isVacant(1,0));
		assertEquals(true,board.isVacant(1,1));
		
		
		board.play(2, 1, 1);
		board.play(2, 2, 2);
		board.play(0, 0, 1);
		System.out.println(board);
	}
	
	@Test
	public void testSwitchPlayer() {
		board.switchPlayers();
		assertEquals(2, board.getCurrentPlayer());
		
	}
	
	@Test
	public void testGameOver() {
		board.play(2, 1, 1);
		board.play(2, 2, 2);
		board.play(0, 0, 1);
		board.play(1, 1, 2);
		assertEquals(false, board.isOver());
		board.play(0, 1, 1);
		board.play(1, 2, 2);
		board.play(0, 2, 1);
		assertEquals(true, board.isOver());

		System.out.println(board);

		
	}
	
	@Test
	public void testTie() {
		board.play(1, 1, 1);
		board.play(0, 0, 2);
		board.play(0, 1, 1);
		board.play(1, 2, 2);
		board.play(0, 2, 1);
		board.play(2, 0, 2);
		board.play(1, 0, 1);
		board.play(2, 1, 2);
		
		
		assertEquals(false, board.isOver());
		board.play(2, 2, 1);
		assertEquals(true, board.isOver());
		System.out.println(board);

		
	}
	
	
	
	
}
