package edu.lclark.orego.util;

public class BitBoard {
	
	private int[] board;
	
	public BitBoard(int capacity){
		board = new int[capacity];
	}
	
	/**
	 * Sets the bit at row r and column c to 1
	 */
	public void set(int r, int c){
		board[r] |= (1 << c);
	}
	
	public void clear(){
		for(int i=0; i<board.length; i++){
			board[i]=0;
		}
	}
	
	public int getRow(int index){
		return board[index];
	}

}
