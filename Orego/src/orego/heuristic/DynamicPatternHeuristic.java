package orego.heuristic;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import ec.util.MersenneTwisterFast;

import orego.patternanalyze.*;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.NEIGHBORS;
import static orego.experiment.Debug.*;

import orego.core.Board;

public class DynamicPatternHeuristic extends Heuristic {

	private static HashMap<Long, DynamicPattern> patternList;
	
	private static boolean test;
	
	private static int PATTERNS_TO_LOAD = 100;
	
	static {
		patternList = new HashMap<Long, DynamicPattern>();
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/patternPlayed8.dat", patternList);
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/patternPlayed12.dat", patternList);
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/patternPlayed20.dat", patternList);
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/patternPlayed24.dat", patternList);
	}
	
	public DynamicPatternHeuristic(int weight) {
		super(weight);
	}

	private static void extractPatternsFromFile(String fileName, HashMap<Long, DynamicPattern> patternList) {
		ObjectInputStream input;
		try {
			input = new ObjectInputStream(new FileInputStream(
					new File(fileName)));
			DynamicPattern pattern = null;
			try {
				int counter = 0;
				while ((pattern = (DynamicPattern) input.readObject()) != null && counter < PATTERNS_TO_LOAD) {
					for (int i = 0; i < 8; i++){
						patternList.put(pattern.getPattern()[i], pattern);
					}
					counter++;
				}
				input.close();
			} catch (EOFException ex) {
				input.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	public int evaluate(int p, Board board) {
		int returnValue = 0;
		long pattern24 = DynamicPattern.setupPattern(p, board, 24);
		long pattern20 = pattern24 & (~(255L << 40));
		long pattern12 = pattern20 & (~((255L << 24) | (255L << 32)));
		long pattern8 = pattern12 & (~(255L << 16));
		if (patternList.containsKey(pattern24)) {
			return 1;
		}
		if (patternList.containsKey(pattern20)) {
			return 1;
		}
		if (patternList.containsKey(pattern12)) {
			return 1;
		}
		if (patternList.containsKey(pattern8)) {
			return 1;
		}
		return returnValue;
	}
	
	public void prepare(Board board) {
		super.prepare(board);
		for (int p : NEIGHBORS[board.getMove(board.getTurn() - 1)]) {
			if (board.getColor(p) == VACANT) {
				int playValue = evaluate(p, board);
				if(playValue > 0) {
					recommend(p);
				}
				if(playValue < 0) {
					discourage(p); 
				}
			}
		}
	}

	public static void setTestMode(boolean value) {
		test = value;
	}
	
	@Override
	public DynamicPatternHeuristic clone() {
		return (DynamicPatternHeuristic) super.clone();
	}
}
