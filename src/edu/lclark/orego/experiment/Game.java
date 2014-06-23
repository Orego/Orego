package edu.lclark.orego.experiment;

import static edu.lclark.orego.core.NonStoneColor.OFF_BOARD;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.experiment.Game.State.QUITTING;
import static edu.lclark.orego.experiment.Game.State.REQUESTING_MOVE;
import static edu.lclark.orego.experiment.Game.State.SENDING_MOVE;
import static edu.lclark.orego.experiment.Game.State.SENDING_TIME_LEFT;
import static edu.lclark.orego.sgf.SgfWriter.toSgf;
import static edu.lclark.orego.ui.Orego.VERSION_STRING;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Scanner;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.score.ChineseFinalScorer;
import edu.lclark.orego.score.Scorer;

/** Allows two independent GTP programs to play a game. */
public final class Game {

	static enum State {
		QUITTING, REQUESTING_MOVE, SENDING_MOVE, SENDING_TIME_LEFT
	}

	// TODO Put this in a configuration file
	private static final int GAME_TIME_IN_SECONDS = 600;

	public static void main(String[] args) {
		final String black = "java -ea -server -Xmx3072M -cp /Network/Servers/maccsserver.lclark.edu/Users/drake/Documents/workspace/Orego/bin edu.lclark.orego.ui.Orego";
		final String white = black;
		new Game(
				"/Network/Servers/maccsserver.lclark.edu/Users/drake/test.sgf",
				black, white).play();
	}

	/** The board on which this game is played. */
	private final Board board;

	/** Shell commands to start the two contestants. */
	private final String[] contestants;

	/** File to which the results of this game are sent. */
	private final String filename;

	/** State of the program. @see #handleResponse */
	private State state;

	/** Prints to the file specified by filename. */
	private PrintWriter out;

	/** Processes running the competing programs. */
	private final Process[] programs;

	/** For scoring games. */
	private final Scorer scorer;

	/** System time (in milliseconds) when the game started. */
	private final long starttime;

	/**
	 * The system time (in milliseconds) the player was asked for a move. This
	 * is used to calculate how much of their time each player has used.
	 */
	private long timeLastMoveWasRequested;

	/** The amount of time (in milliseconds) each player has used so far. */
	private final long[] timeUsed;

	/** Prints to the two program processes. */
	private PrintWriter[] toPrograms;

	/** Color the winning player (BLACK or WHITE). */
	private Color winner;

	/**
	 * @param filename
	 *            File where output should be sent.
	 * @param black
	 *            Shell command to start black player.
	 * @param white
	 *            Shell command to start white player.
	 */
	public Game(String filename, String black, String white) {
		this.filename = filename;
		timeUsed = new long[] { 0, 0 };
		programs = new Process[2];
		toPrograms = new PrintWriter[2];
		contestants = new String[] { black, white };
		try {
			out = new PrintWriter(filename);
		} catch (final Throwable e) {
			out.println("In " + filename + ":");
			// out.println(board);
			e.printStackTrace(out);
			out.flush();
			out.close();
			System.exit(1);
		}
		// TODO Extract board size, komi from Orego command string
		final int boardSize = 9;
		final double komi = 7.5;
		out.println("(;FF[4]CA[UTF-8]AP[Orego" + VERSION_STRING + "]KM[" + komi
				+ "]GM[1]SZ[" + boardSize + "]");
		out.println("PB[" + black + "]");
		out.println("PW[" + white + "]");
		out.flush();
		board = new Board(boardSize);
		scorer = new ChineseFinalScorer(board, komi);
		starttime = System.currentTimeMillis();
	}

	/**
	 * Sends the quit command to both contestants, so that the processes will
	 * end. Also adds start and end time comments to the end of the output SGF
	 * file.
	 */
	private void endPrograms() {
		out.println(";C[starttime:" + new Date(starttime) + "]");
		out.println(";C[endtime:" + new Date(System.currentTimeMillis()) + "]");
		out.println(")");
		out.flush();
		state = QUITTING;
		for (final StoneColor color : StoneColor.values()) {
			toPrograms[color.index()].println("quit");
			toPrograms[color.index()].flush();
		}
	}

	/**
	 * Returns the current color to play.
	 */
	private StoneColor getColorToPlay() {
		return board.getColorToPlay();
	}

	// TODO This used to be synchronized. Why?
	/**
	 * Handles a response string (line) from the player of the given color.
	 *
	 * At the beginning of this method, mode is set to the action to which we
	 * are handling a response. For example, if mode is REQUESTING_MOVE, we are
	 * handling a move returned by a player.
	 *
	 * @return true if the game ended normally.
	 */
	public boolean handleResponse(StoneColor color, String line, Scanner s) {
		if (line.startsWith("=")) {
			if (state == REQUESTING_MOVE) {
				// Accumulate the time the player spent their total
				timeUsed[getColorToPlay().index()] += System
						.currentTimeMillis() - timeLastMoveWasRequested;
				final long timeLeftForThisPlayer = GAME_TIME_IN_SECONDS
						- timeUsed[getColorToPlay().index()] / 1000000;
				final String timeLeftIndicator = (getColorToPlay() == BLACK ? "BL"
						: "WL")
						+ "[" + timeLeftForThisPlayer + "]";
				final String coordinates = line
						.substring(line.indexOf(' ') + 1);
				// TODO Make this a field?
				final CoordinateSystem coords = board.getCoordinateSystem();
				// Begin SGF output
				if (coordinates.equals("PASS")) {
					out.println((getColorToPlay() == BLACK ? ";B" : ";W")
							+ "[]" + timeLeftIndicator);
					out.flush();
				} else if (coordinates.toLowerCase().equals("resign")) {
					winner = getColorToPlay().opposite();
					out.println(";RE[" + (winner == BLACK ? "B" : "W") + "+R]");
					out.println(";C[moves:" + board.getTurn() + "]");
					endPrograms();
					out.flush();
					return false;
				} else {
					out.println((getColorToPlay() == BLACK ? ";B" : ";W") + "["
							+ toSgf(coords.at(coordinates), coords) + "]"
							+ timeLeftIndicator);
					out.flush();
				}
				// End SGF output
				board.play(coordinates);
				if (board.getPasses() == 2) {
					winner = scorer.winner();
					out.println(";RE[" + (winner == BLACK ? "B" : "W")
							+ "+" + Math.abs(scorer.score()) + "]");
					out.println(";C[moves:" + board.getTurn() + "]");
					out.flush();
					endPrograms();
					return false;
				}
				// We are going to send the move now. After the response
				// received, we want to be in a mode indicating what we are
				// finishing. If we aren't tracking game time, pretend we sent
				// the time left to skip that step.
				if (GAME_TIME_IN_SECONDS > 0) {
					state = SENDING_MOVE;
				} else {
					state = SENDING_TIME_LEFT;
				}
				// Note the color reversal here, because the color to play has
				// already been switched
				toPrograms[getColorToPlay().index()].println(getColorToPlay()
						.opposite() + " " + coordinates);
				toPrograms[getColorToPlay().index()].flush();
				return false;
			} else if (state == SENDING_MOVE) {
				state = SENDING_TIME_LEFT;
				sendTime();
				return false;
			} else if (state == SENDING_TIME_LEFT) {
				// Ignore the player's response to the time_left command.
				state = REQUESTING_MOVE;
				sendMoveRequest();
				return false;
			} else { // Mode is QUITTING
				return true;
			}
		}
		// We got something other than an acknowledgment
		out.println("In " + filename + ":");
		out.println(board);
		out.println("Got something other than an acknowledgment: " + line);
		endPrograms();
		while (s.hasNextLine()) {
			out.println(s.nextLine());
		}
		out.flush();
		System.exit(1);
		return false;
	}

	/**
	 * Plays the game.
	 *
	 * @return the color of the winning player (BLACK or WHITE), as defined in
	 *         orego.core.Colors.
	 */
	private Color play() {
		winner = OFF_BOARD;
		try {
			for (final StoneColor color : StoneColor.values()) {
				final int c = color.index();
				final ProcessBuilder builder = new ProcessBuilder("nohup",
						"bash", "-c", contestants[c], "&");
				builder.redirectErrorStream(true);
				programs[c] = builder.start();
				toPrograms[c] = new PrintWriter(programs[c].getOutputStream());
				new Thread(new PlayerListener(color,
						programs[c].getInputStream(), this)).start();
			}
			board.clear();
			// Start by telling the first player how much time they have left,
			// which gets the game started (see handleResponse).
			if (GAME_TIME_IN_SECONDS > 0) {
				state = SENDING_TIME_LEFT;
				sendTime();
			} else {
				state = REQUESTING_MOVE;
				sendMoveRequest();
			}
			// Wait for programs to finish
			for (final StoneColor color : StoneColor.values()) {
				programs[color.index()].waitFor();
			}
			out.close();
		} catch (final Throwable e) {
			// Something when wrong; report the error, kill everything, and die
			out.println("In " + filename + ":");
			out.println(board);
			e.printStackTrace(out);
			out.flush();
			out.close();
			try {
				for (final StoneColor color : StoneColor.values()) {
					final int c = color.index();
					toPrograms[c].println("quit");
					toPrograms[c].flush();
					toPrograms[c].close();
					programs[c].getInputStream().close();
					programs[c].getOutputStream().close();
					programs[c].getErrorStream().close();
					programs[c].destroy();
				}
			} catch (final IOException ioe) {
				ioe.printStackTrace();
				System.exit(1);
			}
			System.exit(1);
		}
		return winner;
	}

	/** Sends a move request to the color to play. */
	private void sendMoveRequest() {
		final StoneColor c = getColorToPlay();
		toPrograms[c.index()].println("genmove " + c);
		toPrograms[c.index()].flush();
		timeLastMoveWasRequested = System.currentTimeMillis();
	}

	/** Sends a time left message to the color to play. */
	private void sendTime() {
		final StoneColor c = getColorToPlay();
		final long timeLeftForThisPlayer = GAME_TIME_IN_SECONDS
				- timeUsed[c.index()] / 1000000;
		toPrograms[c.index()].println("time_left " + c + " "
				+ timeLeftForThisPlayer + " 0");
		toPrograms[c.index()].flush();
	}

}
