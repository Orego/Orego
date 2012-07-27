package orego.heuristic;

import java.lang.reflect.Constructor;
import java.util.StringTokenizer;

import orego.core.Board;
import orego.play.UnknownPropertyException;

/**
 * Note: we do not override ArrayList since we need the raw speed of a native array.
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
	
	/** Runs each of the heuristics on the given move and returns the result
	 * TODO: should we maintain a cache of these values to remove needless expensive computation
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
		// TODO: we could achieve the same goal by building a string then calling loadHeuristicList()
		
		HeuristicList copied = new HeuristicList(heuristics.length);
		
		try {
			// loop through heuristics and create *new* instances of each underlying subclass
			for (int i = 0; i < heuristics.length; i++) {
				Heuristic heur = heuristics[i];
				Constructor<?> constructor = heur.getClass().getConstructor(Integer.TYPE);
				Heuristic copy = (Heuristic) constructor.newInstance(heur.getWeight());	
				
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
		
		String[] heuristicClasses = heuristicList.split(":");
		
		heuristics = new Heuristic[heuristicClasses.length];
		
		for (int i = 0; i < heuristicClasses.length; i++) {
			String[] heuristicAndWeight = heuristicClasses[i].split("@");
			
			// if no weight was specified, default to 1
			double weight = heuristicAndWeight.length == 2 ? weight = Double.valueOf(heuristicAndWeight[1]) : 1;
			String genClass = heuristicAndWeight[0];
			
			if ( ! genClass.contains(".")) {
				// set default path to heuristic if it isn't given
				genClass = "orego.heuristic." + genClass;
			}
			if ( ! genClass.endsWith("Heuristic")) {
				// complete the class name if a shortened version is used
				genClass = genClass + "Heuristic";
			}
			
			try {
				Constructor<?> constructor = Class.forName(genClass).getConstructor(Integer.TYPE);
				Heuristic heur = (Heuristic) constructor.newInstance((int)(Math.round(weight)));
				heuristics[i] = heur;
				
			} catch (Exception e) {
				
				System.err.println("Cannot construct heuristic: " + heuristicList);
				e.printStackTrace();
				System.exit(1);
			}
		}
		
	}
	
	
	public void setProperty(String name, String value) throws UnknownPropertyException{
		if (name.equals("heuristics") && ! value.isEmpty()) {
			loadHeuristicList(value);
		} else if (name.startsWith("heuristic.") && ! value.isEmpty()) {
			
			// Command format: gogui-set-param heuristic.Escape.threshold 21
			if (heuristics.length == 0) {
				throw new UnsupportedOperationException("No heuristics exists when setting parameter '" + name + "'");
			}
			
			// parse the full property name out into its component parts
			StringTokenizer parser = new StringTokenizer(name);
			// skip the 'heuristic.' prefix
			parser.nextToken(".");
			String heuristicName  	 = parser.nextToken(".");
			
			String heuristicProperty = parser.nextToken(" ");
			// strip the prefix '.' off
			heuristicProperty = heuristicProperty.replace(".", "");
			
			// now we find the heuristic matching the heuristic name (we loop through all our heuristics)
			for (Heuristic heuristic : heuristics) {
				
				// strip off class suffix of Heuristic and do compare
				if (heuristic.getClass().getSimpleName().replace("Heuristic", "").equals(heuristicName)) {
					heuristic.setProperty(heuristicProperty, value);
					return;
				}
			}
			
			throw new UnsupportedOperationException("No heuristic exists for '" + heuristicName + "' when setting property '" + heuristicProperty + "'");
		} else {
			throw new UnknownPropertyException("No property exists for '" + name + "'");
		}
	}
	
	
}
