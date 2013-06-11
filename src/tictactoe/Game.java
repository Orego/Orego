package tictactoe;

import java.util.Scanner;

public class Game {

	public static void main(String[] args) {
		Board board = new Board();
		Scanner in = new Scanner(System.in);
		MinmaxPlayer cpu = new MinmaxPlayer();

		while (!board.isOver()) {
			if (board.getCurrentPlayer() == 1) {
				System.out.println(board);
				System.out.println("player " + board.getCurrentPlayer()
						+ " please enter a row:");
				int r = in.nextInt();
				System.out.println("player " + board.getCurrentPlayer()
						+ " please enter a column:");
				int c = in.nextInt();
				if (board.isVacant(r, c)) {
					board.play(r, c, board.getCurrentPlayer());
					board.switchPlayers();
				} else {
					System.out.println("The move is invalid.");
				}
			}
			else {
				int best = cpu.minmax(board);
				int row = best/3;
				int column = best%3;
				board.play(row, column,board.getCurrentPlayer());
				board.switchPlayers();
				
			}
		}

		System.out.println("Game over!");
		System.out.println("Player " + board.getWinner() + " wins");
		System.out.println(board);
	}

}
