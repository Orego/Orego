package edu.lclark.orego.neural;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.feature.HistoryObserver;

/**This is how I wrote the book*/
public class BookWritting {
	
	public static void main(String[] args) {
		Board board = new Board(19);
		DirectNetwork net = new DirectNetwork(board, new HistoryObserver(board));
		net.train(1);
		net.update();
		net.writeBook();
	}
}
