package edu.lclark.orego.mcts;

import edu.lclark.orego.book.FusekiBook;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.LgrfTable;
import edu.lclark.orego.patterns.ShapeTable;
import edu.lclark.orego.time.ExitingTimeManager;
import edu.lclark.orego.time.SimpleTimeManager;
import edu.lclark.orego.time.UniformTimeManager;

/** Builds a player. */
@SuppressWarnings("hiding")
public final class PlayerBuilder {

	private int biasDelay;

	private boolean book;

	private boolean coupDeGrace;

	private int gestation;

	private double komi;

	private boolean lgrf2;

	private boolean liveShape;
	
	private String managerType;

	/** Amount of memory allocated to Orego, in megabytes. The transposition table is scaled accordingly. */
	private int memorySize;

	private int msecPerMove;

	private boolean ponder;
	
	private boolean rave;
	
	private boolean shape;

	private int shapeBias;
	
	private int shapePatternSize;
	
	private float shapeScalingFactor;
	
	private int threads;

	private int width;

	public PlayerBuilder() {
		// Default values
		biasDelay = 800;
		gestation = 4;
		komi = 7.5;
		threads = 2;
		memorySize = 1024;
		msecPerMove = 1000;
		width = 19;
		ponder = false;
		book = true;
		managerType = "uniform";
		coupDeGrace = false;
		lgrf2 = true;
		rave = true;
		shapeScalingFactor = .95f;
		shapePatternSize = 5;
		shapeBias = 20;
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
		CopiableStructure copyStructure;
		if(shape){
			copyStructure = CopiableStructureFactory.shape(width, komi, shapeBias, shapePatternSize, shapeScalingFactor);
		} else if(lgrf2){
			copyStructure = CopiableStructureFactory.lgrfWithBias(width,
					komi);
		}else {
			copyStructure = CopiableStructureFactory.useWithBias(width, komi);
		}
		final Player result = new Player(threads, copyStructure);
		final Board board = result.getBoard();
		final CoordinateSystem coords = board.getCoordinateSystem();
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
		if (liveShape) {
			assert shape;
			ShapeTable shapeTable = copyStructure.get(ShapeTable.class);
			updater = new ShapeUpdater(updater, shapeTable);
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
		result.ponder(ponder);
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

	public PlayerBuilder liveShape(boolean liveShape) {
		this.liveShape = liveShape;
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

	public PlayerBuilder ponder(boolean ponder) {
		this.ponder = ponder;
		return this;
	}

	public PlayerBuilder rave(boolean rave) {
		this.rave = rave;
		return this;
	}

	public PlayerBuilder shape(boolean shape) {
		this.shape = shape;
		return this;
	}

	public PlayerBuilder shapeBias(int shapeBias) {
		this.shapeBias = shapeBias;
		return this;
	}

	public PlayerBuilder shapeMinStones(int shapePatternSize) {
		this.shapePatternSize = shapePatternSize;
		return this;
	}

	public PlayerBuilder shapeScalingFactor(float shapeScalingFactor) {
		this.shapeScalingFactor = shapeScalingFactor;
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
