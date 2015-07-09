package edu.lclark.orego.genetic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

public class Genotype {

	public static void main(String[] args) {
		System.out.println(Long.toBinaryString(-1L));
	}

	private double fitness;

	private long[] words;

	/**
	 * @param length
	 *            The number of 64-bit longs in this genotype.
	 */
	public Genotype(int length) {
		words = new long[length];
	}

	public Genotype(long[] words) {
		this.words = words;
	}

	public Genotype cross(Genotype that, MersenneTwisterFast random) {
		int k = random.nextInt(64 * words.length + 1);
		int w = k / 64;
		int i = k % 64;
		long[] result = new long[words.length];
		for (int j = 0; j < result.length; j++) {
			if (j < w) {
				result[j] = words[j];
			} else if (j == w) {
				if (i == 0) {
					result[j] = that.words[j];
				} else {
					result[j] = (words[j] & (((1L << i) - 1L) << (64 - i)))
							| (that.words[j] & ((1L << (64 - i)) - 1L));
				}

			} else {
				result[j] = that.words[j];
			}
		}
		return new Genotype(result);
	}

	public void evaluateFitness() {
		Board board = new Board(19);
		Phenotype phenotype = new Phenotype(board, 20, this);
		File file = new File(SYSTEM.getExpertGamesDirectory());
		SgfParser parser = new SgfParser(board.getCoordinateSystem(), false);
		List<List<Short>> games = processFiles(file, parser);
		int totalEvaluated = 0;
		int hits = 0;
		for (final List<Short> game : games){
			Short[] g = game.toArray(new Short[0]);
			hits += hits(g, phenotype);
			totalEvaluated += game.size();
		}
		fitness = 1.0*hits/totalEvaluated;
			
	}
	
	List<List<Short>> processFiles(File file, SgfParser parser) {
		final List<List<Short>> games = new ArrayList<>();
		if (file.isDirectory()) {
			for (final File f : file.listFiles()) {
				games.addAll(processFiles(f, parser));
			}
		} else if (file.getPath().endsWith(".sgf")) {
			// System.out.println("Reading file " + file.getName());
			games.addAll(parser.parseGamesFromFile(file, 2));
		}
		return games;
	}

	public double getFitness() {
		return fitness;
	}

	public long[] getWords() {
		return words;
	}

	/** Returns the number of moves that phenotype correctly predicts from game. */
	int hits(short[] game, Phenotype phenotype) {
		return -1;
	}

	public void mutate(MersenneTwisterFast random) {
		int r = random.nextInt(64 * words.length);
		int w = r / 64;
		words[w] ^= (1L << r);
	}

	public void randomize() {
		MersenneTwisterFast random = new MersenneTwisterFast();
		for (int i = 0; i < words.length; i++) {
			words[i] = random.nextLong();
		}
	}

	public void setWords(long[] words) {
		this.words = words;
	}
}
