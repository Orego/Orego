package edu.lclark.orego.mcts;

import edu.lclark.orego.book.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.LgrfTable;
import edu.lclark.orego.time.*;

/** Builds a player. */
@SuppressWarnings("hiding")
public final class PlayerBuilder {

	private int biasDelay;

	private boolean book;

	private int gestation;

	private double komi;

	private boolean lgrf2;

	private String managerType;

	private int msecPerMove;

	private boolean rave;

	private int threads;

	private boolean usePondering;

	private int width;

	private boolean coupDeGrace;

	/** Amount of memory allocated to Orego, in megabytes. The transposition table is scaled accordingly. */
	private int memorySize;

	public PlayerBuilder() {
		// Default values
		biasDelay = 800;
		gestation = 4;
		komi = 7.5;
		threads = 2;
		memorySize = 1024;
		msecPerMove = 1000;
		width = 19;
		usePondering = false;
		book = true;
		managerType = "";
		coupDeGrace = false;
		lgrf2 = false;
		rave = true;
	}

	public PlayerBuilder biasDelay(int biasDelay) {
		this.biasDelay = biasDelay;
		return this;
	}

	public PlayerBuilder boardWidth(int width) {
		this.width = width;
		return this;
	}

	/** Creates the Player. */
	public Player build() {
		CopiableStructure copyStructure = lgrf2 ? CopiableStructureFactory.lgrfWithPriors(width,
				komi) : CopiableStructureFactory.useWithBias(width, komi);
		Player result = new Player(threads, copyStructure);
		Board board = result.getBoard();
		CoordinateSystem coords = board.getCoordinateSystem();
		TranspositionTable table;
		if (rave) {
			table = new TranspositionTable(memorySize, new RaveNodeBuilder(coords),
					coords);
			result.setTreeDescender(new RaveDescender(board, table, biasDelay));
		} else {
			table = new TranspositionTable(memorySize, new SimpleSearchNodeBuilder(coords),
					coords);
			result.setTreeDescender(new UctDescender(board, table, biasDelay));
		}
		TreeUpdater updater;
		if (lgrf2) {
			updater = new LgrfUpdater(new SimpleTreeUpdater(board, table, gestation),
					copyStructure.get(LgrfTable.class));
		} else {
			updater = new SimpleTreeUpdater(board, table, gestation);
		}
		if (managerType.equals("exiting")) {
			result.setTimeManager(new ExitingTimeManager(result));
		} else if (managerType.equals("uniform")) {
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
		result.setTreeUpdater(updater);
		result.setMsecPerMove(msecPerMove);
		result.usePondering(usePondering);
		result.clear();
		return result;
	}

	public PlayerBuilder coupDeGrace(boolean grace) {
		this.coupDeGrace = grace;
		return this;
	}

	public PlayerBuilder gestation(int gestation) {
		this.gestation = gestation;
		return this;
	}

	public PlayerBuilder komi(double komi) {
		this.komi = komi;
		return this;
	}

	public PlayerBuilder lgrf2(boolean lgrf2) {
		this.lgrf2 = lgrf2;
		return this;
	}

	public PlayerBuilder memorySize(int megabytes) {
		memorySize = megabytes;
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

	public PlayerBuilder pondering(boolean ponder) {
		usePondering = ponder;
		return this;
	}

	public PlayerBuilder rave(boolean rave) {
		this.rave = rave;
		return this;
	}

	public PlayerBuilder threads(int threads) {
		this.threads = threads;
		return this;
	}

	public PlayerBuilder timeManagement(String managerType) {
		this.managerType = managerType;
		return this;
	}

}
