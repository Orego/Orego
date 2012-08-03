package orego.heuristic;

import java.io.*;
import java.util.ArrayList;

import ec.util.MersenneTwisterFast;

import orego.patternanalyze.*;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.NEIGHBORS;
import static orego.experiment.Debug.*;

import orego.core.Board;

public class DynamicPatternHeuristic extends Heuristic {

	private static ArrayList<DynamicPattern> pattern8List;
	private static ArrayList<DynamicPattern> pattern12List;
	private static ArrayList<DynamicPattern> pattern20List;
	private static ArrayList<DynamicPattern> pattern24List;
	
	private static boolean test;
	
	private static int PATTERNS_TO_LOAD = 100;
	
	static {
		pattern8List = new ArrayList<DynamicPattern>();
		pattern12List = new ArrayList<DynamicPattern>();
		pattern20List = new ArrayList<DynamicPattern>();
		pattern24List = new ArrayList<DynamicPattern>();			
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern8.dat", pattern8List);
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern12.dat", pattern12List);
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern20.dat", pattern20List);
		extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern24.dat", pattern24List);
	}
	
	public DynamicPatternHeuristic(int weight) {
		super(weight);
	}

	private static void extractPatternsFromFile(String fileName, ArrayList<DynamicPattern> patternList) {
		ObjectInputStream input;
		try {
			input = new ObjectInputStream(new FileInputStream(
					new File(fileName)));
			DynamicPattern pattern = null;
			try {
				int counter = 0;
				while ((pattern = (DynamicPattern) input.readObject()) != null && counter < PATTERNS_TO_LOAD) {
					patternList.add(pattern);
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

	@Override
	public void prepare(Board board) {
		int p = board.getMove(board.getTurn() - 1);
		long pattern1 = DynamicPattern.setupPattern(p, board, 8);
		long pattern2 = DynamicPattern.setupPattern(p, board, 12);
		long pattern3 = DynamicPattern.setupPattern(p, board, 20);
		long pattern4 = DynamicPattern.setupPattern(p, board, 24);
		for (DynamicPattern pattern : patternList) {
			if (pattern.match(pattern4, 24)) {
				return 4 * getWeight();
			}
			if (pattern.match(pattern3, 20)) {
				return 3 * getWeight();
			}
			if (pattern.match(pattern2, 12)) {
				return 2 * getWeight();
			}
			if (pattern.match(pattern1, 8)) {
				return 1 * getWeight();
		}
		for (DynamicPattern pattern : pattern20List) {
			if (pattern.match(pattern20, 20)) {
				returnValue += 3 * getWeight();
			}
		}
		for (DynamicPattern pattern : pattern12List) {
			if (pattern.match(pattern12, 12)) {
				returnValue += 2 * getWeight();
			}
		}
		for (DynamicPattern pattern : pattern8List) {
			if (pattern.match(pattern8, 8)) {
				returnValue += 1 * getWeight();
			}
		}
		return returnValue;
	}
	
	public void prepare(Board board, MersenneTwisterFast random) {
		super.prepare(board, random);
		int[] values = getValues();
		for (int p : NEIGHBORS[board.getMove(board.getTurn() - 1)]) {
			if (board.getColor(p) == VACANT) {
				values[p] = evaluate(p, board);
				if (values[p] > 0) {
					getNonzeroPoints().add(p);
				}
			}
		}
		if (getNonzeroPoints().size() > 0) {
			setBestMove(getNonzeroPoints().get(random.nextInt(getNonzeroPoints().size())));
		}
	}

	public static void setTestMode(boolean value) {
		test = value;
	}
}
