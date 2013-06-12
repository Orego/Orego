package tictactoe;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import java.util.List;
import java.math.*;

public class MctsPlayer {
	private int playoutLimit;
	private Node root;
	private int move;
	
	public MctsPlayer(int playoutLimit) {
		this.playoutLimit = playoutLimit;
	}
	
	
	public int move(Board board) {
		root = new Node();
		for (int i = 0; i < playoutLimit; i++) {
			playout(board.copy());
		}
//		System.out.println(root.toString());
		
		return root.actualMove();
	}
	
	/** Performs one complete playout. This modifies state. */
	public void playout(Board board) {
		int colorToPlay = board.getCurrentPlayer();
		Node node = root;
		List<Integer> moves = new ArrayList<Integer>();
		while (node != null && !board.isOver()) {
			
			move=node.playoutMove(board);
			board.play(move/3, move%3, colorToPlay);
			moves.add(move);
			node=node.getChild(move);
			colorToPlay=opposite(colorToPlay);
			
			// TODO Find the best move to make using playoutMove()
			// TODO Play it
			// TODO Add it to the list of moves
			// TODO Advance to the next node down the tree
		}
		colorToPlay=board.getCurrentPlayer();
		double result = finishPlayout(board, moves);
		root.recordPlayout(moves, result, colorToPlay);
	}
	
	/**
	 * Plays random moves from state to the end of the game. Returns 1 if X
	 * wins, 0 if O wins, and 0.5 in case of a tie. The moves played are added
	 * to the end of moves. This modifies both state and moves.
	 */
	protected double finishPlayout(Board copy, List<Integer> moves) {
		Random generator = new Random();
		// TODO While the game is not over, pick a random legal move, play it,
		// and add it to moves
		int currentPlayer= copy.getCurrentPlayer();
		while(!copy.isOver()){
			LinkedList<Integer> legal = new LinkedList<Integer>();
			legal=copy.legalMoves();
			
			
			
			
			
			move=legal.get(generator.nextInt( legal.size() ));
			copy.play(move/3, move%3, currentPlayer);
			moves.add(move);	
			currentPlayer=opposite(currentPlayer);
		}
		int winner = copy.getWinner();
		if (winner ==1) {
			return 1;
		} else if (winner == 2) {
			return 0;
		} else {
			return 0.5;
		}
	}


	private int opposite(int currentPlayer) {
		if (currentPlayer == 1) {
			return 2;
		} else {
			return 1;
		}
	}

	

}
