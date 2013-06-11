package tictactoe;

import java.util.LinkedList;

public class Board {
	private int[][] board = new int[3][3];
	private int currentPlayer;
	
	private int winner;
	
	
	public Board(){
		currentPlayer=1;
	}

	
	public void play(int r, int c, int player) {
		board[r][c]=player;
		
		
	}

	public int get(int r, int c) {
		// TODO Auto-generated method stub
		return board[r][c];
		
	}

	public boolean isVacant(int r, int c) {
		if (board[r][c] == 0){
			return true;
		}
		return false;
	}
	
	
	public String toString() {
		String result = "";
		for(int r=0; r<3; r++){
			for(int c=0; c<3; c++){
				if(board[r][c]==0){
					result+= ".";
				}else if(board[r][c]==1){
					result+="X";
				}else{
					result+="O";
				}
			}
			result += "\n";
		}
		
		
		return result;
	}

	public void switchPlayers() {
		if(currentPlayer==1){
			currentPlayer=2;
		} else{
			currentPlayer=1;
		}
		
		
	}

	public int getCurrentPlayer() {
		return currentPlayer;
	}

	public boolean isOver() {
		for(int i=0;i<3; i++){
			if(board[i][0]==board[i][1] && board[i][0]==board[i][2] && board[i][0] != 0){
				winner = board[i][0];
				return true;
			}
			if(board[0][i]==board[1][i] && board[0][i]==board[2][i] && board[0][i] != 0){
				winner = board[0][i];
				return true;
			}
		}
		if(board[0][0]==board[1][1] && board[2][2] == board[0][0] && board[0][0] != 0){
			winner = board[2][2];
			return true;
		}
		if(board[2][0]==board[1][1] && board[0][2] == board[2][0] && board[2][0] != 0){
			winner = board[0][2];
			return true;
		}
		
		for(int i =0 ; i<3; i++){
			for(int j=0; j<3; j++){
				if(board[i][j]==0){
					return false;
				}
			}
		}
		
		
		return true;
	}
	
	public void setCurrentPlayer(int p){
		currentPlayer=p;
	}
	
	public Board copy(){
		Board copy = new Board();
		copy.setCurrentPlayer(getCurrentPlayer());
		for(int i=0; i<3; i++){
			for(int j=0; j<3; j++){
				copy.play(i, j, get(i,j));
			}
		}
		
		return copy;
	}


	public int getWinner() {
		// TODO Auto-generated method stub
		return winner;
	}
	public LinkedList<Integer> legalMoves(){
		for(int i=0; i<3; i++){
			for(int j=0; j<3; j++){
				
			}
	}
		return null;

}}
