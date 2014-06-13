package edu.lclark.orego.mcts;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;

/** Mainly for testing McRunnable. */
public class StubPlayer implements Player {

	private Board board;
	
	private McRunnable[] runnables;
	
	private final ExecutorService executor;
	
	public StubPlayer(int threads, CopiableStructure stuff) {
		CopiableStructure copy = stuff.copy();
		board = copy.get(Board.class);
		runnables = new McRunnable[threads];
		for (int i = 0; i < runnables.length; i++) {
			runnables[i] = new McRunnable(this, stuff);
		}
		executor = Executors.newFixedThreadPool(threads);
	}

	@Override
	public Board getBoard() {
		return board;
	}

	@Override
	public void generateMovesToFrontier(McRunnable mcRunnable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void incorporateRun(Color winner, McRunnable mcRunnable) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean shouldKeepRunning() {
		return keepRunning;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public McRunnable getMcRunnable(int i) {
		return runnables[i];
	}

	private boolean keepRunning;
	
	@Override
	public short bestMove() {
		keepRunning = true;
		for (int i = 0; i < runnables.length; i++) {
			executor.execute(runnables[i]);
		}
		try {
			Thread.sleep(millisecondsPerMove);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		keepRunning = false;
		// TODO We need to tell the executor to shut down at some point
		return PASS;
	}

	private int millisecondsPerMove;
	
	@Override
	public void setMillisecondsPerMove(int milliseconds) {
		// TODO Auto-generated method stub
		millisecondsPerMove = milliseconds;
	}

}
