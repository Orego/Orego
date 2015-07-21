package edu.lclark.orego.genetic;

import edu.lclark.orego.book.FusekiBook;
import edu.lclark.orego.mcts.CopiableStructure;
import edu.lclark.orego.mcts.CopiableStructureFactory;
import edu.lclark.orego.mcts.DoNothing;
import edu.lclark.orego.time.SimpleTimeManager;
import edu.lclark.orego.time.UniformTimeManager;

@SuppressWarnings("hiding")
public class PlayerBuilder {
	
	private boolean book;

	private boolean coupDeGrace;

	private double komi;
	
	private String managerType;

	private int populationSize;
	
	private int individualLength;

	private int msecPerMove;

	private boolean ponder;
		
	private int threads;

	private int width;

	private int contestants;
	
	public PlayerBuilder() {
		// Default values
		komi = 7.5;
		threads = 2;
		msecPerMove = 1000;
		width = 19;
		ponder = false;
		book = true;
		managerType = "uniform";
		coupDeGrace = false;
		populationSize = 10000;
		individualLength = 2000;
		contestants = 2;
	}

	public PlayerBuilder populationSize(int populationSize) {
		this.populationSize = populationSize;
		return this;
	}
	
	public PlayerBuilder contestants(int contestants) {
		this.contestants = contestants;
		return this;
	}
	
	public PlayerBuilder individualLength(int individualLength) {
		this.individualLength = individualLength;
		return this;
	}
	
	public PlayerBuilder boardWidth(int width) {
		this.width = width;
		return this;
	}

	/** Creates the Player. */
	public Player build() {
		CopiableStructure copyStructure;
		copyStructure = CopiableStructureFactory.escapePatternCapture(width, komi);
		final Player result = new Player(threads, copyStructure);
		if (managerType.equals("uniform")) {
			result.setTimeManager(new UniformTimeManager(result.getBoard()));
		} else {
			result.setTimeManager(new SimpleTimeManager(msecPerMove));
		}
		result.setCoupDeGrace(coupDeGrace);
		if (book && width == 19) {
			result.setOpeningBook(new FusekiBook());
		} else {
			result.setOpeningBook(new DoNothing());
		}
		result.setMsecPerMove(msecPerMove);
		result.ponder(ponder);
		result.setContestants(contestants);
		result.createPopulations(populationSize, individualLength);
		result.clear();
		return result;
	}

	public PlayerBuilder coupDeGrace(boolean grace) {
		this.coupDeGrace = grace;
		return this;
	}

	public PlayerBuilder komi(double komi) {
		this.komi = komi;
		return this;
	}

	public PlayerBuilder msecPerMove(int msec) {
		this.msecPerMove = msec;
		return this;
	}

	public PlayerBuilder openingBook(boolean book) {
		this.book = book;
		return this;
	}

	public PlayerBuilder ponder(boolean ponder) {
		this.ponder = ponder;
		return this;
	}

	public PlayerBuilder threads(int threads) {
		this.threads = threads;
		return this;
	}

	/** Sets the type of time manager to use, e.g., "exiting" or "uniform". */
	public PlayerBuilder timeManagement(String managerType) {
		this.managerType = managerType;
		return this;
	}

}
