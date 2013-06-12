package tictactoe;

import java.util.Scanner;

public class Game {

	public static void main(String[] args) {
		int ties=0;
		Board board = new Board();
		Scanner in = new Scanner(System.in);
		MctsPlayer cpu = new MctsPlayer(10000);
		MinmaxPlayer cpu2 = new MinmaxPlayer();
	for(int i=0; i<100; i++){
		while (!board.isOver()) {
//			if (board.getCurrentPlayer() == 1) {
//				System.out.println(board);
//				System.out.println("player " + board.getCurrentPlayer()
//						+ " please enter a row:");
//				int r = in.nextInt();
//				System.out.println("player " + board.getCurrentPlayer()
//						+ " please enter a column:");
//				int c = in.nextInt();
//				if (board.isVacant(r, c)) {
//					board.play(r, c, board.getCurrentPlayer());
//					board.switchPlayers();
//				} else {
//					System.out.println("The move is invalid.");
//				}
			if(board.getCurrentPlayer()==1){
				int best = cpu.move (board);
				int row = best/3;
				int column = best%3;
				board.play(row, column,board.getCurrentPlayer());
				board.switchPlayers();
			}
			else {
				int best = cpu2.minmax (board);
				int row = best/3;
				int column = best%3;
				board.play(row, column,board.getCurrentPlayer());
				board.switchPlayers();
				
			}
		}

		System.out.println("Game over!");
		System.out.print("Player " + board.getWinner() + " wins.");
		if (board.getWinner() == 0) {
			System.out.print("  It is a tie.");
			ties++;
		}
		System.out.println();
		System.out.println(board);
		
	}
	System.out.println(ties);
	}
}
