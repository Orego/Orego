package orego.heuristic;

import java.lang.reflect.Constructor;
import java.util.*;

import ec.util.MersenneTwisterFast;
import orego.core.Board;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;
import static java.lang.Math.*;
import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.NEIGHBORS;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.ON_BOARD;

/**
 * Note: we do not override ArrayList since we need the raw speed of a native
 * array.
 * 
 * @author sstewart
 * 
 */
public class HeuristicList implements Cloneable {

	private Heuristic[] heuristics;

	public HeuristicList(String heuristicList) {
		loadHeuristicList(heuristicList);
	}

	public HeuristicList() {
		this(0);
	}

	public HeuristicList(int size) {
		heuristics = new Heuristic[size];
	}

	/**
	 * Runs each of the heuristics on the given move and returns the result
	 * TODO: should we maintain a cache of these values to remove needless
	 * expensive computation
	 * */
	public int moveRating(int move, Board board) {
		int value = 0;
		for (Heuristic h : heuristics) {
			value += h.evaluate(move, board) * h.getWeight();
		}
		return value;
	}

	public int size() {
		return heuristics.length;
	}

	@Override
	public HeuristicList clone() {
		// TODO: we could achieve the same goal by building a string then
		// calling loadHeuristicList()
		HeuristicList copied = new HeuristicList(heuristics.length);
		try {
			// loop through heuristics and create *new* instances of each
			// underlying subclass
			for (int i = 0; i < heuristics.length; i++) {
				Heuristic heur = heuristics[i];
				Constructor<?> constructor = heur.getClass().getConstructor(
						Integer.TYPE);
				Heuristic copy = (Heuristic) constructor.newInstance(heur
						.getWeight());
				copied.getHeuristics()[i] = copy;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return copied;
	}

	public Heuristic[] getHeuristics() {
		return heuristics;
	}

	public void loadHeuristicList(String heuristicList) {
		List<Heuristic> list = new ArrayList<Heuristic>();
		String[] heuristicClasses = heuristicList.split(":");
		for (int i = 0; i < heuristicClasses.length; i++) {
			String[] heuristicAndWeight = heuristicClasses[i].split("@");
			// if no weight was specified, default to 1
			int weight = 1;
			if (heuristicAndWeight.length == 2) {
				weight = (int) (round(Double.valueOf(heuristicAndWeight[1])));
			}
			if (weight != 0) {
				String genClass = heuristicAndWeight[0];
				if (!genClass.contains(".")) {
					// set default path to heuristic if it isn't given
					genClass = "orego.heuristic." + genClass;
				}
				if (!genClass.endsWith("Heuristic")) {
					// complete the class name if a shortened version is used
					genClass = genClass + "Heuristic";
				}
				try {
					Constructor<?> constructor = Class.forName(genClass)
							.getConstructor(Integer.TYPE);
					Heuristic heur = (Heuristic) constructor
							.newInstance(weight);
					list.add(heur);
				} catch (Exception e) {
					System.err.println("Cannot construct heuristic: "
							+ heuristicList);
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		heuristics = list.toArray(new Heuristic[0]);
	}

	public void removeZeroWeightedHeuristics() {
		ArrayList<Heuristic> copied = new ArrayList<Heuristic>();
		
		for (Heuristic heur : heuristics) {
			if (heur.getWeight() != 0) {
				copied.add(heur);
			}
		}
		
		heuristics = copied.toArray(new Heuristic[0]);
	}
	
	public Heuristic appendNewHeuristic(String heuristicName) {
		Heuristic heuristic = null;
		
		try {
			if (!heuristicName.contains(".")) {
				// set default path to heuristic if it isn't given
				heuristicName = "orego.heuristic." + heuristicName;
			}
			if (!heuristicName.endsWith("Heuristic")) {
				// complete the class name if a shortened version is used
				heuristicName = heuristicName + "Heuristic";
			}
			
			Constructor<?> constructor = Class.forName(heuristicName).getConstructor(Integer.TYPE);
			heuristic = (Heuristic) constructor.newInstance(1);
			
			// copy into new array
			Heuristic[] cloned = Arrays.copyOf(heuristics, heuristics.length + 1);
			
			// copy into last index
			cloned[heuristics.length] = heuristic;
			
			heuristics = cloned;
			return heuristic;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}

	// TODO Go through the search area randomly
	/**
	 * Goes through the heuristics until one suggests a legal, feasible move; plays
	 * that move. In the interest of speed, the FIRST move given a positive value
	 * by a heuristic is chosen, not necessarily the highest-rated move.
	 */
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		// Compute best heuristic value
		int lastMove = board.getMove(board.getTurn() - 1);
		if (ON_BOARD[lastMove]) {
			for (Heuristic h : heuristics) {
				h.prepare(board);
				IntSet nonzeroPoints = h.getNonzeroPoints();
				if (nonzeroPoints.size() > 0) {
					int i = h.getBestIndex();
					int j = i;
					do {
						int p = nonzeroPoints.get(j);
						if ((board.getColor(p) == VACANT) && (board.isFeasible(p))) {
							if (board.playFast(p) == PLAY_OK) {
								return p;
							}
						}
						j = (j + 1) % nonzeroPoints.size();
					} while (j != i);
				}
			}
		}
		return NO_POINT;
	}

	public void setProperty(String name, String value)
			throws UnknownPropertyException {
		if (name.equals("heuristics") && !value.isEmpty()) {
			loadHeuristicList(value);
		} else if (name.startsWith("heuristic.") && !value.isEmpty()) {
			// Command format: gogui-set-param heuristic.Escape.threshold 21
			if (heuristics.length == 0) {
				throw new UnsupportedOperationException(
						"No heuristics exists when setting parameter '" + name
								+ "'");
			}
			// parse the full property name out into its component parts
			StringTokenizer parser = new StringTokenizer(name);
			// skip the 'heuristic.' prefix
			parser.nextToken(".");
			String heuristicName = parser.nextToken(".");
			String heuristicProperty = parser.nextToken(" ");
			// strip the prefix '.' off
			heuristicProperty = heuristicProperty.replace(".", "");
			// now we find the heuristic matching the heuristic name (we loop
			// through all our heuristics)
			for (Heuristic heuristic : heuristics) {
				// strip off class suffix of Heuristic and do compare
				if (heuristic.getClass().getSimpleName().replace("Heuristic", "").equals(heuristicName)) {
					heuristic.setProperty(heuristicProperty, value);
					return;
				}
			}
			
			// create heuristic if it doesn't exist
			Heuristic newHeuristic = appendNewHeuristic(heuristicName + "Heuristic");
			newHeuristic.setProperty(heuristicProperty, value);
		} else {
			throw new UnknownPropertyException("No property exists for '"
					+ name + "'");
		}
	}

}
