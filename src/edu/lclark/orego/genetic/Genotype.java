package edu.lclark.orego.genetic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static edu.lclark.orego.core.StoneColor.BLACK;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;

public class Genotype {

	private double fitness;

	private int[] genes;
	
	/**
	 * @param length
	 *            The number of genes in this genotype.
	 */
	public Genotype(int length) {
		genes = new int[length];
	}

	public Genotype(int[] genes) {
		this.genes = genes;
	}

	public Genotype cross(Genotype that, MersenneTwisterFast random) {
		final int crossoverPoint = random.nextInt(genes.length);
		final int[] result = new int[genes.length];
		System.arraycopy(genes, 0, result, 0, crossoverPoint);
		System.arraycopy(that.genes, crossoverPoint, result, crossoverPoint, result.length - crossoverPoint);
		return new Genotype(result);
	}

	public void evaluateFitness() {
		fitness = 0;
		Board board = new Board(19);
		Phenotype phenotypeBlack = new Phenotype(board, this);
		File file = new File(SYSTEM.getExpertGamesDirectory());
		SgfParser parser = new SgfParser(board.getCoordinateSystem(), false);
		List<List<Short>> games = processFiles(file, parser);
		int totalEvaluated = 0;
		int hits = 0;
		for (final List<Short> game : games) {
			hits += phenotypeBlack.hits(game);
			totalEvaluated += (game.size() + 1) / 2; // Only count black moves
		}
		fitness = 1.0 * hits / totalEvaluated;
	}
	
	public double getFitness() {
		return fitness;
	}
	
	public int[] getGenes() {
		return genes;
	}

	public void mutate(MersenneTwisterFast random, short[] possiblePoints) {
		genes[random.nextInt(genes.length)] = randomGene(random, possiblePoints);
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

	@SuppressWarnings("static-method")
	public int randomGene(MersenneTwisterFast random, short[] possiblePoints) {
		return possiblePoints[random.nextInt(possiblePoints.length)]
				| (possiblePoints[random.nextInt(possiblePoints.length)] << 9)
				| (possiblePoints[random.nextInt(possiblePoints.length)] << 18);
	}

	public void randomize(MersenneTwisterFast random, short[] possiblePoints) {
		for (int i = 0; i < genes.length; i++) {
			genes[i] = randomGene(random, possiblePoints);
		}
	}

	public void setGenes(int[] genes) {
		this.genes = genes;
	}
}
