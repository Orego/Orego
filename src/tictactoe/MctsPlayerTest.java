package tictactoe;

import static org.junit.Assert.*;

import org.junit.Test;

public class MctsPlayerTest {
	Board board= new Board();
	MctsPlayer player = new MctsPlayer(1000);
	private int seven;
	private int three;
	private int five;
	private int one;
	private int posZero;
	private int two;
	private int six;
	private int eight;
	@Test
	public void testTree() {
		board.play(1, 1, 1);
		board.play(0, 2, 2);
		board.play(1, 2, 1);
		board.setCurrentPlayer(2);
		System.out.println(board);
		System.out.println(player.move(board));
		
		
	}

	
	@Test
	public void testChoice(){
		board.play(1,1,1);
		board.setCurrentPlayer(2);
		System.out.println(board);
		int move = 0;
		for(int i=0; i<10000; i++){
			move=player.move(board);
			if(move==7){
				seven++;
			} else if(move==3){
				three++;
			} else if(move==5){
				five++;
			} else if(move ==1){
				one++;
			} else if(move==0){
				posZero++;
			} else if(move==2){
				two++;
			} else if(move ==6){
				six++;
			}
			else if(move ==8){
				eight++;
			}
		}
		
		System.out.println("The number of 0's is : " + posZero);
		System.out.println("The number of 1's is : " +one);
		System.out.println("The number of 2's is : " +two);
		System.out.println("The number of 3's is : " +three);
		System.out.println("The number of 5's is : " + five);
		System.out.println("The number of 6's is : " +six);
		System.out.println("The number of 7's is : " +seven);
		System.out.println("The number of 8's is : " +eight);
	}

}
