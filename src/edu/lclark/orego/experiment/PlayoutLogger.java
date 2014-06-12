package edu.lclark.orego.experiment;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.StoneCounter;
import edu.lclark.orego.move.Mover;
import edu.lclark.orego.move.MoverFactory;
import edu.lclark.orego.score.ChinesePlayoutScorer;
import edu.lclark.orego.score.Scorer;

public class PlayoutLogger {

	private final static Logger logger = Logger.getLogger(PlayoutSpeed.class
			.getName());

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Handler handler = new ConsoleHandler();
		try {
			handler = new FileHandler("%h/orego" + "0" + ".sgf");
			handler.setFormatter(new PlainTextFormatter());
			logger.addHandler(handler);
		} catch (IOException e) {
			logger.log(Level.INFO, e.toString());
		}
		logger.setLevel(Level.ALL);
		MersenneTwisterFast random = new MersenneTwisterFast();
		Board original = new Board(19);
		Board copy = new Board(19);
		CoordinateSystem copyCoords = copy.getCoordinateSystem();
		new StoneCounter(original); // This appears unused, but original knows
									// about its observers
		StoneCounter mercyObserver = new StoneCounter(copy);
		Scorer scorer = new ChinesePlayoutScorer(copy, 7.5);
		// The first mover is created only to make any BoardObservers
		MoverFactory.escapeCapturer(original);
		Mover mover = MoverFactory.escapeCapturer(copy);
		final int runs = 4;
		long total = 0;
		int[] wins = new int[3];
		int playoutLength = 0;
		for (int run = 0; run < runs; run++) {
			logger.log(Level.FINER, "(");
			long before = System.nanoTime();
			int turns = 0;
			copy.copyDataFrom(original);
			do {
				char colorSymbol = copy.getColorToPlay() == BLACK ? 'B' : 'W';
				short p = mover.selectAndPlayOneMove(random);
				if (p != CoordinateSystem.PASS) {
					logger.log(
							Level.FINER,
							";"
									+ colorSymbol
									+ "["
									+ CoordinateSystem
											.columnToStringSgf(copyCoords
													.column(p))

									+ CoordinateSystem
											.rowToStringSgf(copyCoords.row(p))
									+ "]");
				} else {
					logger.log(Level.FINER, ";" + colorSymbol + "[]");
				}
				turns++;
			} while (copy.getPasses() < 2
					&& mercyObserver.mercyWinner() == null);
			char colorWinner = scorer.winner() == BLACK ? 'B' : 'W';

			logger.log(Level.FINER,
					"RE[" + colorWinner + "+" + Math.abs(scorer.score()) + "]");
			logger.log(Level.FINER, ")");
			wins[scorer.winner().index()]++;
			playoutLength += turns;
			long after = System.nanoTime();
			total += (after - before);
			handler.close();
			logger.removeHandler(handler);
			if (run+1 < runs) {
				try {
					handler = new FileHandler("%h/orego" + (run + 1) + ".sgf");
					handler.setFormatter(new PlainTextFormatter());
					logger.addHandler(handler);
				} catch (IOException e) {
					logger.log(Level.INFO, e.toString());
				}
			}
		}
		System.out.println((runs / 1000.0) / (total / 1000000000.0) + " kpps");
		System.out.println("Black wins: " + wins[BLACK.index()]);
		System.out.println("White wins: " + wins[WHITE.index()]);
		System.out.println("Ties: " + wins[VACANT.index()]);
		System.out.println("Average playout length: " + playoutLength / runs);
	}
}
