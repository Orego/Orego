package orego.neural;

import orego.mcts.*;
//import orego.mcts.SearchNode;
import orego.play.UnknownPropertyException;
//import orego.policy.CoupDeGracePolicy;
import orego.util.IntSet;
import orego.core.Board;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;

/** Uses a neural network instead of a search tree. */
public class NeuralPlayer extends McPlayer {

	/** Number of moves played by the network before turning to the policy. */
	private int cutoff;

	/** Number of hidden units in the network. */
	private int hidden;

	/** Number of previous moves considered by the network. */
	private int history;

	/** Learning rate. */
	private double learn;

	/** The neural network. */
	private Network network;

	/** Number of playouts starting at each point. */
	private int[] playouts;

	public NeuralPlayer() {
		learn = 0.5;
		cutoff = 10;
		history = 2;
		hidden = 0;
	}

	@Override
	public void beforeStartingThreads() {
		// Do nothing
	}

	/**
	 * Returns the best move to make from here when actually playing (as opposed
	 * to during a playout). We choose the move with the most playouts.
	 */
	protected int bestPlayMove() {
		int best = -1;
		int result = PASS;
		IntSet vacantPoints = getBoard().getVacantPoints();
		network.computeHiddenActivations(getBoard().getColorToPlay(),
				getBoard(), getBoard().getTurn());
		// TODO We should consider passing; right now we will keep playing until
		// there are no legal moves, even in seki
		for (int i = 0; i < vacantPoints.size(); i++) {
			int move = vacantPoints.get(i);
			if (getBoard().isFeasible(move)) {
				if ((playouts[move] >= best) && getBoard().isLegal(move)) {
					best = playouts[move];
					result = move;
				}
			}
		}
		// Consider entering coup de grace mode
		// if (grace
		// && (node.getWinRate(result) > COUP_DE_GRACE_PARAMETER)
		// & (((McRunnable) getRunnable(0)).getPolicy().getClass() !=
		// CoupDeGracePolicy.class)) {
		// debug("Initiating coup de grace");
		// for (int i = 0; i < getNumberOfThreads(); i++) {
		// McRunnable m = ((McRunnable) getRunnable(i));
		// m.setPolicy(new CoupDeGracePolicy(m.getPolicy()));
		// }
		// }
		// Consider resigning
		// if (node.getWinRate(result) < RESIGN_PARAMETER) {
		// return RESIGN;
		// }
		return result;
	}

	@Override
	public int bestStoredMove() {
		// Can we win outright by passing?
		if (getBoard().getPasses() == 1) {
			if (secondPassWouldWinGame()) {
				return PASS;
			}
			// If not, don't pass if there's a legal move!
			// root.exclude(PASS);
		}
		return bestPlayMove();
	}

	// TODO Rename in McPlayer so that it doesn't refer to "tree"?
	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		Board board = runnable.getBoard();
		board.copyDataFrom(getBoard());
		int count = 0;
		while ((board.getPasses() < 2) && (count < getCutoff())) {
			int bestMove = PASS;
			double bestEval = -1;
			IntSet vacantPoints = board.getVacantPoints();
			getNetwork().computeHiddenActivations(board.getColorToPlay(),
					board, board.getTurn());
			for (int i = 0; i < vacantPoints.size(); i++) {
				int p = vacantPoints.get(i);
				if (board.isFeasible(p)) {
					double eval = getNetwork().evaluateFast(
							board.getColorToPlay(), p, board, board.getTurn());
					if ((eval > bestEval) && (board.isLegal(p))) {
						bestMove = p;
						bestEval = eval;
					}
				}
			}
			runnable.acceptMove(bestMove);
			count++;
		}
	}

	/**
	 * @return the cutoff.
	 */
	public int getCutoff() {
		return cutoff;
	}

	/**
	 * @return the learning rate.
	 */
	public double getLearningRate() {
		return learn;
	}

	/** Returns the network. For testing only. */
	protected Network getNetwork() {
		return network;
	}

	@Override
	public int getPlayouts(int p) {
		return playouts[p];
	}

	@Override
	public double getWinRate(int p) {
		return network.evaluate(getBoard().getColorToPlay(), p, getBoard(),
				getBoard().getTurn());
	}

	@Override
	public int getWins(int p) {
		return 0;
	}

	/**
	 * Incorporates a run in the network
	 */
	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		if (winner != VACANT) {
			int turn = runnable.getTurn();
			int[] moves = runnable.getMoves();
			int win = 1 - Math.abs(winner - getBoard().getColorToPlay());
			int color = getBoard().getColorToPlay();
			playouts[moves[getTurn()]]++;
			for (int t = getTurn(); t < turn; t++) {
				network.learn(color, runnable.getBoard(), t, win);
				win = 1 - win;
				color = opposite(color);
			}
//			System.out.println(network.evaluate(getBoard().getColorToPlay(), at("f7"), getBoard(), getTurn())
//					+ "\t" + network.evaluate(getBoard().getColorToPlay(), at("g6"), getBoard(), getTurn())
//					+ "\t" + network.evaluate(getBoard().getColorToPlay(), at("a2"), getBoard(), getTurn())
//					);				
		}
	}

	@Override
	public void reset() {
		super.reset();
		network = new Network(learn, hidden, history);
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new McRunnable(this, getPolicy().clone()));
		}
		playouts = new int[FIRST_POINT_BEYOND_BOARD];
	}

	/**
	 * Sets the cutoff.
	 */
	protected void setCutoff(int cutoff) {
		this.cutoff = cutoff;
	}

	/**
	 * Sets the number of hidden uints.
	 */
	protected void setHidden(int hidden) {
		this.hidden = hidden;
	}

	/**
	 * Sets the history.
	 */
	protected void setHistory(int history) {
		this.history = history;
	}

	/**
	 * Sets the learning rate.
	 */
	protected void setLearn(double learn) {
		this.learn = learn;
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("hidden")) {
			setHidden(Integer.parseInt(value));
		} else if (property.equals("learn")) {
			setLearn(Double.parseDouble(value));
		} else if (property.equals("cutoff")) {
			setCutoff(Integer.parseInt(value));
		} else if (property.equals("history")) {
			setHistory(Integer.parseInt(value));
		} else {
			super.setProperty(property, value);
		}
	}

	@Override
	public void updateForAcceptMove(int p) {
		java.util.Arrays.fill(playouts, 0);
	}

	@Override
	protected String winRateReport() {
		return null;
	}

}
