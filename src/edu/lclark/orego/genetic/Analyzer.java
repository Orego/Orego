package edu.lclark.orego.genetic;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import static edu.lclark.orego.genetic.Phenotype.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

/** Analyzes final population after evolution. */
public class Analyzer {

//	public static void main(String[] args) {
//		try (ObjectInputStream individuals = new ObjectInputStream(
//				new FileInputStream(args[0]));
//				ObjectInputStream champions = new ObjectInputStream(
//						new FileInputStream(args[1]))
//				) {
//			List<Genotype> genotypes = new ArrayList<>();
//			try {
//				while (true) {
//					int[] genes = (int[]) individuals.readObject();
//					genotypes.add(new Genotype(genes));
//				}
//			} catch (EOFException e) {
//				// Do nothing -- this is how readObject works!
//			}
//			Genotype champion = null;
//			try {
//				while (true) {
//					int[] genes = (int[]) champions.readObject();
//					champion = new Genotype(genes);
//				}
//			} catch (EOFException e) {
//				// Do nothing -- this is how readObject works!
//			}
//			Board board = new Board(19);
//			CoordinateSystem coords = board.getCoordinateSystem();
//			short penultimate = coords.at("c17");
//			short ultimate = coords.at("d17");
//			board.play(penultimate);
//			board.play(ultimate);
//			// Poll population
//			getVotes(genotypes.toArray(new Genotype[0]), board,
//					penultimate, ultimate
//					);
//			// Ask the champion
//			Phenotype champ = new Phenotype(Phenotype.makeRichBoard(board, 7.5), champion);
//			System.out.println(coords.toString(penultimate) + ", " + coords.toString(ultimate) + " -> " + coords.toString(champ.getRawReply(penultimate, ultimate)));
//			System.out.println(coords.toString(IGNORE) + ", " + coords.toString(ultimate) + " -> " + coords.toString(champ.getRawReply(IGNORE, ultimate)));
//			System.out.println(coords.toString(penultimate) + ", " + coords.toString(IGNORE) + " -> " + coords.toString(champ.getRawReply(penultimate, IGNORE)));
//			System.out.println(coords.toString(IGNORE) + ", " + coords.toString(IGNORE) + " -> " + coords.toString(champ.getRawReply(IGNORE, IGNORE)));
//			System.out.println("Champion's choice: " + coords.toString(champ.bestMoveVerbose(penultimate, ultimate)));
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
////	}
////
//	/**
//	 * Returns the number of votes for each point on the board, given the last
//	 * two moves.
//	 */
//	public static int[] getVotes(Genotype[] individuals, Board board,
//			short penultimate, short ultimate) {
//		final CoordinateSystem coords = board.getCoordinateSystem();
//		int[] result = new int[coords.getFirstPointBeyondBoard()];
//		for (Genotype g : individuals) {
//			result[new Phenotype(Phenotype.makeRichBoard(board, 7.5), g).bestMove(penultimate, ultimate)]++;
//		}
//		for (short p : coords.getAllPointsOnBoard()) {
//			if (result[p] > 0) {
//				System.out.println(coords.toString(p) + ": " + result[p]);
//			}
//		}
//		return result;
//	}
}
