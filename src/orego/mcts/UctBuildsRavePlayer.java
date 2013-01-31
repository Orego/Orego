package orego.mcts;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Math.log;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static orego.core.Coordinates.pointToString;
import static orego.core.Coordinates.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

import com.sun.corba.se.impl.orbutil.graph.Node;

import orego.core.Board;

/*
 * A player created to act as Mcts player but also update RAVE data. This should not be used to actually play, use Mcts. 
 */
public class UctBuildsRavePlayer extends RavePlayer {

	//UCT
	@Override
	public double searchValue(SearchNode node, Board board, int move) {
		// The variable names here are chosen for consistency with the tech
		// report
		double barX = node.getWinRate(move);
		if (barX < 0) { // if the move has been excluded
			return NEGATIVE_INFINITY;
		}
		double logParentRunCount = log(node.getTotalRuns());
		// In the paper, term1 is the mean of the SQUARES of the rewards; since
		// all rewards are 0 or 1 here, this is equivalent to the mean of the
		// rewards, i.e., the win rate.
		double term1 = barX;
		double term2 = -(barX * barX);
		double term3 = sqrt(2 * logParentRunCount / node.getRuns(move));
		double v = term1 + term2 + term3; // This equation is above Eq. 1
		assert v >= 0 : "Negative variability in UCT for move "
				+ pointToString(move) + ":\n" + node + "\nterm1: " + term1
				+ "\nterm2: " + term2 + "\nterm3: " + term3
				+ "\nPlayer's board:\n" + getBoard() + "\nVacant points: "
				+ getBoard().getVacantPoints().toStringAsPoints()
				+ "\nRunnable's board:\n" + board + "\nVacant points: "
				+ board.getVacantPoints().toStringAsPoints();
		double factor1 = logParentRunCount / node.getRuns(move);
		double factor2 = min(0.25, v);
		double uncertainty = 0.4 * sqrt(factor1 * factor2);
		return barX;
		//return uncertainty + barX;
	}

	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-rave-playouts");
		result.add("gogui-live-rave-playouts");
		result.add("gogui-rave-coefficients");
		result.add("gogui-rave-win-rates");
		result.add("final_status_list");
		result.add("gogui-primary-variation");
		result.add("gogui-search-values");
		result.add("gogui-playouts");
		result.add("gogui-one-playout");
		result.add("gogui-total-wins");
		result.add("gogui-uct-rave-data");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("string/Uct Rave Data/gogui-uct-rave-data");
		return result;
	}

	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		String result = null;
		if (command.equals("gogui-uct-rave-data")) {
			result = goguiUctRaveData();
		} else {
			result = super.handleCommand(command, arguments);
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return result;
	}

	//Getting the results to be printed out.
	private String goguiUctRaveData() {
		ArrayList<Integer> moves = new ArrayList<Integer>();
		int count1 = 0;
		int count2 = 0;
		String s = "";
		for (int p : ALL_POINTS_ON_BOARD) {
			double value = searchValue(getRoot(), getBoard(), p);
			if (value >= 0) {
				count1 ++;
				moves.add(p);
				s += value + ", ";
			}
		}
		s += "Stored " + count1 + "|||||||||||";
		s += "RAVE";
		for (int p : ALL_POINTS_ON_BOARD) {
			double value = searchValue2(getRoot(), getBoard(), p);
			if (moves.contains((Integer) p)) {
				count2 ++;
				s += value + ", ";
			}
		}
		s+= "Stored " +  count2;
		assert(count1 == count2);
		return s;
	}

	/**
	 * The RAVE formula.
	 */
	public double searchValue2(SearchNode node, Board board, int move) {
		if (node.getWins(move) == Integer.MIN_VALUE) {
			return NEGATIVE_INFINITY;
		}
		if (move == PASS) {
			return ((double) node.getWins(move)) / node.getRuns(move);
		}
		RaveNode raveNode = (RaveNode) node;
		double c = raveNode.getRuns(move);
		double w = raveNode.getWins(move);
		double rc = raveNode.getRaveRuns(move);
		double rw = raveNode.getRaveWins(move);
		double coef = raveCoefficient(c, rc);
		return rw/rc;
		//return (w / c) * (1 - coef) + (rw / rc) * coef;
	}

}
