package edu.lclark.orego.genetic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;

public class Genotype {

//	public static void main(String[] args) {
//		// TODO This magic number should be parameterized
//		Genotype g = new Genotype(5 + 64*19+361*361*8 + 361);
//		g.randomize();
//		CoordinateSystem coords = CoordinateSystem.forWidth(19);
//		g.getWords()[0] = CoordinateSystem.NO_POINT | (CoordinateSystem.NO_POINT << 9) | (coords.at("q16") << 18);
//		g.evaluateFitness();
//		System.out.println(g.getFitness());
//	}

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
		fitness = 0;
		Board board = new Board(19);
		// TODO The magic number below, for number of replies, should be parameterized
		Phenotype phenotype = new Phenotype(board, Population.NUMBER_OF_REPLIES, this);
		File file = new File(SYSTEM.getExpertGamesDirectory());
		SgfParser parser = new SgfParser(board.getCoordinateSystem(), false);
		List<List<Short>> games = processFiles(file, parser);
		int totalEvaluated = 0;
		int hits = 0;
		for (final List<Short> game : games){
			Short[] g = game.toArray(new Short[0]);
			hits += phenotype.hits(g);
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
			games.addAll(parser.parseGamesFromFile(file, Integer.MAX_VALUE));
		}
		return games;
	}

	public double getFitness() {
		return fitness;
	}

	public long[] getWords() {
		return words;
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
