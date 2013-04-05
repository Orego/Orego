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
import static orego.core.Coordinates.*;

/**
 * Note: we do not override ArrayList since we need the raw speed of a native
 * array.
 * 
 * @author sstewart
 * 
 */
public class HeuristicList implements Cloneable {

	private Heuristic[] heuristics;
	
	// make it so playing randomly is 20% likely
	private static final double RANDOM_WEIGHT = 0.20;
	private int totalWeight;

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
	 * Returns the weighted sum of recommendations of all of the heuristics.
	 * (weight for heuristics that recommend move, -weight for heuristics that discourage it)
	 */
	public int moveRating(int move, Board board) {
		int value = 0;
		for (Heuristic h : heuristics) {
			h.prepare(board);
			if (h.getGoodMoves().contains(move)) {
				value += h.getWeight();
			}
		}
		return value;
	}

	public int size() {
		return heuristics.length;
	}

	@Override
	public HeuristicList clone() {
		HeuristicList copied = new HeuristicList(heuristics.length);
		try {
			// loop through heuristics and create *new* instances of each
			// underlying subclass
			for (int i = 0; i < heuristics.length; i++) {
				copied.getHeuristics()[i] = heuristics[i].clone();
			}
			copied.totalWeight = totalWeight;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return copied;
	}

	public Heuristic[] getHeuristics() {
		return heuristics;
	}

	// calculate the total weight so we can normalize later
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
		
		int sumOfHeuristicWeights = 0;
		for (int i = 0; i < heuristics.length; i++) {
			sumOfHeuristicWeights += heuristics[i].getWeight();
		}
		// extend the totalWeight so there is a 20% chance none of the heuristics gets chosen
		totalWeight = (int) (sumOfHeuristicWeights / (1 - RANDOM_WEIGHT));
//		System.err.println("totalWeight == " + totalWeight);
//		System.err.println("weights is " + weights);
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

	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		return selectAndPlayaWeightedMove(random, board);
	}
	
	/**
	 * Goes through the heuristics until one suggests a legal, feasible move; plays
	 * one of those moves randomly and returns it.
	 */
	public int selectAndPlayFirstRecommendedMove(MersenneTwisterFast random, Board board) {
		// Try to get good moves from heuristics
		for (Heuristic h : heuristics) {
			h.prepare(board);
			IntSet good = h.getGoodMoves();
			if (good.size() > 0) {
				int start = random.nextInt(good.size());
				int i = start;
				do {
					int p = good.get(i);
					if ((board.getColor(p) == VACANT) && (board.isFeasible(p)) && (board.playFast(p) == PLAY_OK)) {
						return p;
					}
					// Advancing by 457 skips "randomly" through the array,
					// in a manner analogous to double hashing.
					i = (i + 457) % good.size();
				} while (i != start);
			}
		}
		// Nothing recommended; play randomly
		return selectAndPlayUniformlyRandomMove(random, board);
	}
		
	public int selectAndPlayaWeightedMove(MersenneTwisterFast random, Board board) {
		// My modification: select a heuristic randomly (weighted by the weights) and then
		// randomly choose one of its recommended moves. If it didn't recommend
		// anything, play a random move.
		
		// take a spin at the wheel
		int spin = random.nextInt(75);
		for (int i = 0; i < heuristics.length; i++) {
			Heuristic h = heuristics[i];
			if (spin < h.getWeight()) {
				// (try to) use this heuristic
				IntSet good = h.getGoodMoves();
				if (good.size() > 0) {
					int start = random.nextInt(good.size());
					int j = start;
					do {
						int p = good.get(i);
						if (board.getColor(p) == VACANT && board.isFeasible(p) && board.playFast(p) == PLAY_OK) {
							return p;
						}
						j = (j + 457) % good.size();
					} while (j != start);
				}
				// didn't work, so just try a random move
				return selectAndPlayUniformlyRandomMove(random, board);
			} else {
				// not this heuristic: we'll try the next one
				spin -= h.getWeight();
			}
		}
		// random policy won the spin:
		return selectAndPlayUniformlyRandomMove(random, board);
	}
	

	/**
	 * Plays and returns a random feasible move.
	 */
	public static int selectAndPlayUniformlyRandomMove(MersenneTwisterFast random,
			Board board) {
		IntSet vacantPoints = board.getVacantPoints();
		int start = random.nextInt(vacantPoints.size());
		int i = start;
		do {
			int p = vacantPoints.get(i);
			if ((board.getColor(p) == VACANT) && (board.isFeasible(p))) {
				if (board.playFast(p) == PLAY_OK) {
					return p;
				}
			}
			// Advancing by 457 skips "randomly" through the array,
			// in a manner analogous to double hashing.
			i = (i + 457) % vacantPoints.size();
		} while (i != start);
		// Nothing left -- pass!
		board.pass();
		return PASS;
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
			String heuristicProperty = parser.nextToken();
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

