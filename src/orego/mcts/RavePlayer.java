package orego.mcts;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.String.format;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.*;
import orego.core.*;
import java.util.Set;
import java.util.StringTokenizer;
import orego.play.UnknownPropertyException;

/**
 * A player using the RAVE improvement on UCT.
 * 
 * @see #searchValue(SearchNode, Board, int)
 */
public class RavePlayer extends MctsPlayer {

	public static void main(String[] args) {
		RavePlayer p = new RavePlayer();
		try {
			p.setProperty("policy", "Random");
			p.setProperty("threads", "1");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		double[] benchMarkInfo = p.benchmark();
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
				+ benchMarkInfo[1]);
	}

	/**
	 * This corresponds to b^2/(0.5*0.5) in Silver's formula. The higher this
	 * is, the less attention is paid to RAVE.
	 */
	private double raveBias;

	public RavePlayer() {
		raveBias = 0.0009;
	}

	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-rave-playouts");
		result.add("gogui-live-rave-playouts");
		result.add("gogui-rave-coefficients");
		result.add("gogui-rave-win-rates");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("gfx/RAVE playouts/gogui-rave-playouts");
		result.add("gfx/RAVE coefficients/gogui-rave-coefficients");
		result.add("gfx/RAVE win rates/gogui-rave-win-rates");
		result.add("none/Live RAVE playouts/gogui-live-rave-playouts");
		return result;
	}

	@Override
	protected SearchNode getPrototypeNode() {
		return new RaveNode();
	}

	/** Returns the number of RAVE playouts through the root via p. */
	protected int getRavePlayouts(int p) {
		return ((RaveNode) getRoot()).getRaveRuns(p);
	}

	/** Returns the RAVE win rate via p. */
	protected double getRaveWinRate(int p) {
		return ((RaveNode) getRoot()).getRaveWinRate(p);
	}

	/** Returns the number of RAVE wins through the root via p. */
	protected double getRaveWins(int p) {
		return ((RaveNode) getRoot()).getRaveWins(p);
	}

	/** Returns GoGui information showing RAVE coefficient distribution. */
	protected String goguiRaveCoefficients() {
		// Label all moves with coefficients
		String result = "";
		RaveNode raveNode = (RaveNode) getRoot();
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = getWinRate(p);
				if (winRate > 0) {
					if (result.length() > 0) {
						result += "\n";
					}
					double c = raveNode.getRuns(p);
					double rc = raveNode.getRaveRuns(p);
					double coeff = raveCoefficient(c, rc);
					result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
							colorCode(coeff), pointToString(p),
							pointToString(p), coeff * 100);
				}
			}
		}
		return result;
	}

	/** Returns GoGui information showing RAVE playout distribution. */
	protected String goguiRavePlayouts() {
		// Find the max playouts of any move
		int max = 0;
		for (int p : getAllPointsOnBoard()) {
			int playouts = ((RaveNode) getRoot()).getRaveRuns(p);
			if (playouts > max) {
				max = playouts;
			}
		}
		// Display proportional playouts through each move
		String result = "INFLUENCE";
		for (int p : getAllPointsOnBoard()) {
			result += format(" %s %.3f", pointToString(p), getRavePlayouts(p)
					/ (double) max);
		}
		// Label all moves with win rates
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = ((RaveNode) getRoot()).getRaveWinRate(p);
				int playouts = ((RaveNode) getRoot()).getRaveRuns(p);
				if (winRate > 0) {
					result += format("\nLABEL %s %d", pointToString(p),
							playouts);
				}
			}
		}
		// Highlight best move
		int best = bestStoredMove();
		if (getOnBoard()[best]) {
			result += "\nCOLOR green " + pointToString(best);
		}
		return result;
	}

	/** Returns GoGui information showing RAVE win rates. */
	protected String goguiRaveWinRates() {
		// Find the maximum and minimum win rates on the board, ignoring
		// occupied points
		double max = 0, min = 1;
		double maxWins = 0;
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = getRaveWinRate(p);
				// Excluded moves have negative win rates
				if (winRate > 0) {
					max = Math.max(max, winRate);
					min = Math.min(min, winRate);
					maxWins = Math.max(maxWins, getRaveWins(p));
				}
			}
		}
		// Display proportional wins through each move
		String result = "INFLUENCE";
		for (int p : getAllPointsOnBoard()) {
			if (getWinRate(p) > 0) {
				result += format(" %s %.3f", pointToString(p), getRaveWins(p)
						/ (double) maxWins);
			}
		}
		// Display win rates as colors and percentages
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = getRaveWinRate(p);
				if (winRate > 0) {
					if (result.length() > 0) {
						result += "\n";
					}
					result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
							colorCode(Math.max((winRate - min), 0.0)
									/ (max - min)), pointToString(p),
							pointToString(p), winRate * 100);
				}
			}
		}
		return result;
	}

	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		String result = null;
		if (command.equals("gogui-rave-playouts")) {
			result = goguiRavePlayouts();
		} else if (command.equals("gogui-live-rave-playouts")) {
			// Note: this will destroy the playout limit if that
			// (rather than a time limit) has been set
			int oldTime = getMillisecondsPerMove();
			setMillisecondsPerMove(1000);
			for (int i = 0; i < 10; i++) {
				bestMove();
				System.err
						.println("gogui-gfx: \n" + goguiRavePlayouts() + "\n");
			}
			setMillisecondsPerMove(oldTime);
			System.err.println("gogui-gfx: CLEAR");
			result = "";
		} else if (command.equals("gogui-rave-coefficients")) {
			result = goguiRaveCoefficients();
		} else if (command.equals("gogui-rave-win-rates")) {
			result = goguiRaveWinRates();
		} else {
			result = super.handleCommand(command, arguments);
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return result;
	}

	/**
	 * Returns the weight given to RAVE (as opposed to direct MC data) given c
	 * runs and rc RAVE runs.
	 */
	protected double raveCoefficient(double c, double rc) {
		return rc / (rc + c + rc * c * raveBias);
	}

	@Override
	/**
	 * Uses the formula from here:
	 * David Silver 
	 * Reinforcement Learning and Simulation-Based Search in Computer Go 
	 * A thesis submitted to the Faculty of Graduate Studies and Research 
	 *	in partial ful�llment of the requirements for the degree of 
	 *	Doctor of Philosophy 
	 *	Department of Computing Science 
	 * http://papersdb.cs.ualberta.ca/~papersdb/uploaded_files/1029/paper_thesis.pdf
	 * equation 8.40 page 107//Updated
	 */
	public double searchValue(SearchNode node, Board board, int move) {
		if (node.getWins(move) == Integer.MIN_VALUE) {
			return NEGATIVE_INFINITY;
		}
		if (move == PASS) {
			return node.getWinRate(move);
		}
		RaveNode raveNode = (RaveNode) node;
		double c = raveNode.getRuns(move);
		double r = raveNode.getWinRate(move);
		double rc = raveNode.getRaveRuns(move);
		double rr = raveNode.getRaveWinRate(move);
		double coef = raveCoefficient(c, rc);
		return r * (1 - coef) + rr * coef;
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("bias")) {
			raveBias = Double.parseDouble(value);
		} else {
			super.setProperty(property, value);
		}
	}

}
