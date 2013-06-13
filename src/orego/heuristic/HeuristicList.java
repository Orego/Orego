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
 * A list of heuristics.
 */
public class HeuristicList implements Cloneable {

	/**
	 * Plays and returns a random feasible move.
	 */
	public static int selectAndPlayUniformlyRandomMove(
			MersenneTwisterFast random, Board board) {
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

	/** The individual heuristics in this list. */
	private Heuristic[] heuristics;

	/** Creates an empty list (which suggests random feasible moves). */
	public HeuristicList() {
		this(0);
	}

	/** Creates a list with room for the specified number of heuristics. */
	protected HeuristicList(int size) {
		heuristics = new Heuristic[size];
	}

	/**
	 * @see #loadHeuristics(String)
	 */
	public HeuristicList(String heuristicList) {
		loadHeuristics(heuristicList);
	}

	@Override
	public HeuristicList clone() {
		HeuristicList copied = null;
		try {
			copied = (HeuristicList) super.clone();
			// loop through heuristics and create *new* instances of each
			// underlying subclass
			for (int i = 0; i < heuristics.length; i++) {
				copied.heuristics[i] = heuristics[i].clone();
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return copied;
	}

	/**
	 * Returns the ith heuristic in this list.
	 */
	public Heuristic get(int i) {
		return heuristics[i];
	}

	/**
	 * Loads a specified list of heuristics. Example argument:
	 * <p>
	 * "EscapeHeuristics@20:orego.heuristic.Pattern@10:Capture"
	 * <p>
	 * This would produce a list with orego.heuristic.EscapeHeuristic at weight
	 * 20, orego.heuristic.PatternHeuristic at 10, and
	 * orego.heuristic.CaptureHeuristic at 1. Note that the prefix
	 * "orego.heuristic." and the suffix "Heuristic" can be omitted. If no
	 * weight is specified, 1 is used.
	 * <p>
	 * If specification is the empty String, this list is set to have no
	 * heuristics.
	 */
	public void loadHeuristics(String specification) {
		if (specification.isEmpty()) {
			heuristics = new Heuristic[0];
		} else {
			List<Heuristic> list = new ArrayList<Heuristic>();
			String[] heuristicClasses = specification.split(":");
			for (int i = 0; i < heuristicClasses.length; i++) {
				String[] heuristicAndWeight = heuristicClasses[i].split("@");
				// if no weight was specified, default to 1
				int weight = 1;
				if (heuristicAndWeight.length == 2) {
					weight = (int) (round(Double.valueOf(heuristicAndWeight[1])));
				}
				String genClass = heuristicAndWeight[0];
				if (!genClass.contains(".")) {
					// set default path to heuristic if it isn't given
					genClass = "orego.heuristic." + genClass;
				}
				if (!genClass.endsWith("Heuristic")) {
					// complete the class name if a shortened version is
					// used
					genClass = genClass + "Heuristic";
				}
				try {
					Constructor<?> constructor = Class.forName(genClass)
							.getConstructor(Integer.TYPE);
					Heuristic heur = (Heuristic) constructor
							.newInstance(weight);
					list.add(heur);
				} catch (Exception e) {
					System.err
							.println("Cannot construct heuristic " + genClass);
					e.printStackTrace();
					System.exit(1);
				}
			}
			heuristics = list.toArray(new Heuristic[0]);
		}
	}

	/**
	 * Returns the weighted sum of recommendations of all of the heuristics.
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

	/**
	 * Goes through the heuristics until one suggests a legal, feasible move;
	 * plays one of those moves randomly and returns it.
	 */
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		// Try to get good moves from heuristics
		for (Heuristic h : heuristics) {
			h.prepare(board);
			IntSet good = h.getGoodMoves();
			if (good.size() > 0) {
				int start = random.nextInt(good.size());
				int i = start;
				do {
					int p = good.get(i);
					if ((board.getColor(p) == VACANT) && (board.isFeasible(p))
							&& (board.playFast(p) == PLAY_OK)) {
						return p;
					}
					// Advancing by 457 skips "randomly" through the set of
					// suggested moves, in a manner analogous to double hashing.
					i = (i + 457) % good.size();
				} while (i != start);
			}
		}
		// Nothing recommended; play randomly
		return selectAndPlayUniformlyRandomMove(random, board);
	}

	/**
	 * @see orego.play.Playable#setProperty(String, String)
	 */
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("heuristics")) {
			loadHeuristics(value);
		} else if (property.startsWith("heuristic.") && !value.isEmpty()) {
			// Command format: gogui-set-param heuristic.Escape.threshold 21
			if (heuristics.length == 0) {
				throw new UnsupportedOperationException(
						"No heuristics exists when setting parameter '" + property
								+ "'");
			}
			// parse the full property name out into its component parts
			StringTokenizer parser = new StringTokenizer(property);
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
				if (heuristic.getClass().getSimpleName()
						.replace("Heuristic", "").equals(heuristicName)) {
					heuristic.setProperty(heuristicProperty, value);
					return;
				}
			}
			throw new UnknownPropertyException("Cannot set "
					+ heuristicProperty + " because there is no "
					+ heuristicName);
		} else {
			throw new UnknownPropertyException("No property exists for '"
					+ property + "'");
		}
	}

	/** Returns the number of heuristics in this list. */
	public int size() {
		return heuristics.length;
	}

}
