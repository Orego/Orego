package edu.lclark.orego.experiment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.move.MoverFactory;
import edu.lclark.orego.score.*;

/** Tests the speed of playouts in one thread. */
public class MultithreadedPlayoutSpeed {

	public static final int THREADS = 4;
	
	public static final int MSEC = 10000;
	
	// This field is package-private so the inner class below can see it
	/** True while the treads should be running. */
	static boolean keepRunning;
	
	public static void main(String[] args) {
		Board original = new Board(19);
		// This mover is created just to create any observers
		MoverFactory.simpleRandom(original);
		PlayoutRunnable[] runnables = new PlayoutRunnable[THREADS];
		for (int i = 0; i < runnables.length; i++) {
			Board copy = new Board(19);
			runnables[i] = new PlayoutRunnable(copy, original, MoverFactory.simpleRandom(copy));
		}
		// Run all of the threads
		ExecutorService executor = Executors.newFixedThreadPool(THREADS);
		keepRunning = true;
		for (int i = 0; i < runnables.length; i++) {
			executor.execute(runnables[i]);
		}
		try {
			Thread.sleep(MSEC);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		keepRunning = false;
		int total = 0;
		for (int i = 0; i < runnables.length; i++) {
			total += runnables[i].playouts;
		}
		executor.shutdown();
		System.out.println("Total: " + total);
		System.out.println((total / (double)MSEC) + " kpps");
	}

	private static class PlayoutRunnable implements Runnable {

		MersenneTwisterFast random;

		Board board;
		
		Mover mover;
		
		Scorer scorer;
		
		Board original;
		
		int playouts;
		
		PlayoutRunnable(Board board, Board original, Mover mover) {
			random =  new MersenneTwisterFast();
			this.board = board;
			this.original = original;
			this.mover = mover;
			scorer = new ChinesePlayoutScorer(board, 7.5);
		}

		@Override
		public void run() {
			playouts = 0;
			while (keepRunning) {
				board.copyDataFrom(original);
				do {
					mover.selectAndPlayOneMove(random);
				} while (board.getPasses() < 2);
				scorer.winner();
				playouts++;
			}
			System.out.println(playouts);
		}
		
	}

}
