package edu.lclark.orego.experiment;

import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.OFF_BOARD;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.experiment.Game.State.QUITTING;
import static edu.lclark.orego.experiment.Game.State.REQUESTING_MOVE;
import static edu.lclark.orego.experiment.Game.State.SENDING_MOVE;
import static edu.lclark.orego.experiment.Game.State.SENDING_TIME_LEFT;
import static edu.lclark.orego.sgf.SgfWriter.toSgf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Scanner;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.score.ChineseFinalScorer;
import edu.lclark.orego.score.FinalScorer;

/** Allows two independent GTP programs to play a game. */
final class Game {

	static enum State {
		QUITTING, REQUESTING_MOVE, SENDING_MOVE, SENDING_TIME_LEFT
	}

	public static void main(String[] args) {
		final String black = "java -ea -server -Xmx3072M -cp /Network/Servers/maccsserver.lclark.edu/Users/drake/Documents/workspace/Orego/bin edu.lclark.orego.ui.Orego";
		final String white = black;
		final Rules rules = new Rules(9, 7.5, 600);
		new Game(
				"/Network/Servers/maccsserver.lclark.edu/Users/drake/test.sgf",
				rules, black, white).play();
	}

	/** The board on which this game is played. */
	private final Board board;

	/** File to which the results of this game are sent. */
	private final String filename;

	/** Prints to the file specified by filename. */
	private PrintWriter out;

	/** Shell commands to start the two players. */
	private final String[] players;

	/** Processes running the competing programs. */
	private final Process[] programs;

	/** Rules of this game. */
	private final Rules rules;

	/** For scoring games. */
	private final FinalScorer scorer;

	/** System time (in milliseconds) when the game started. */
	private long startTime;

	/** State of the program. @see #handleResponse */
	private State state;

	/**
	 * The system time (in milliseconds) when the player was asked for a move.
	 * This is used to calculate how much of their time each player has used.
	 */
	private long timeLastMoveWasRequested;

	/** The amount of time (in milliseconds) each player has used so far. */
	private final long[] timeUsed;

	/** Prints to the two program processes. */
	private final PrintWriter[] toPrograms;

	/** Color the winning player (BLACK or WHITE). */
	private Color winner;

	/**
	 * @param outputFilename
	 *            File where SGF output should be sent.
	 * @param black
	 *            Shell command to start black player.
	 * @param white
	 *            Shell command to start white player.
	 */
	Game(String outputFilename, Rules rules, String black, String white) {
		this.filename = outputFilename;
		this.rules = rules;
		timeUsed = new long[2];
		programs = new Process[2];
		toPrograms = new PrintWriter[2];
		players = new String[] { black, white };
		board = new Board(rules.boardWidth);
		scorer = new ChineseFinalScorer(board, rules.komi);
		try {
			out = new PrintWriter(filename);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			out.flush();
			out.close();
			System.exit(1);
		}
		out.println("(;FF[4]CA[UTF-8]AP[Orego8]KM[" + rules.komi
				+ "]GM[1]RU[Chinese]SZ[" + rules.boardWidth + "]");
		out.println("PB[" + players[BLACK.index()] + "]");
		out.println("PW[" + players[WHITE.index()] + "]");
		out.flush();
	}

	/**
	 * Something happened that prevents the game from continuing. Tells the
	 * players to shut down and then crashes.
	 */
	private void die(String line, Scanner s, String message) {
		System.err.println(hashCode() + " Game dying, line " + line + ", message " + message);
		endPrograms();
		out.println("In " + filename + ":");
		out.println(board);
		out.println(message);
		out.println(line);
		while (s.hasNextLine()) {
			out.println(s.nextLine());
		}
		out.flush();
		System.exit(1);
	}

	/**
	 * Sends the quit command to both players, so that the processes will end.
	 * Also adds start and end time comments to the end of the output SGF file.
	 */
	private void endPrograms() {
		out.println(";C[starttime:" + new Date(startTime) + "]");
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

	/**
	 * Handles a response string (line) from the player of the given color.
	 *
	 * At the beginning of this method, state is set to the action to which we
	 * are handling a response. For example, if state is REQUESTING_MOVE, we are
	 * handling a move returned by a player in response to our request.
	 *
	 * @param line
	 *            The response being handled.
	 * @param s
	 *            Scanner through which responses arrive. This is useful for
	 *            recovering a multi-line error message sent by a player.
	 * @return true if line is a response to a quit command.
	 */
	boolean handleResponse(String line, Scanner s) {
		System.err.println(hashCode() + " Game received line " + line);
		if (line.startsWith("=")) {
			if (state == REQUESTING_MOVE) {
				final String move = line.substring(line.indexOf(' ') + 1);
				System.err.println(hashCode() + " Game received move " + move);
				if (!writeMoveToSgf(move)) {
					return false;
				}
				final Legality legality = board.play(move);
				if (legality != OK) {
					die(line, s, "Illegal move: " + legality);
				}
				if (board.getPasses() == 2) {
					winner = scorer.winner();
					System.err.println(hashCode() + " Game set winner to " + winner);
					out.println(";RE[" + (winner == BLACK ? "B" : "W") + "+"
							+ Math.abs(scorer.score()) + "]");
					out.println(";C[moves:" + board.getTurn() + "]");
					out.flush();
					endPrograms();
					return false;
				}
				sendToOtherPlayer(move);
				return false;
			} else if (state == SENDING_MOVE) {
				state = SENDING_TIME_LEFT;
				sendTime();
				return false;
			} else if (state == SENDING_TIME_LEFT) {
				state = REQUESTING_MOVE;
				sendMoveRequest();
				return false;
			} else { // Mode is QUITTING
				return true;
			}
		}
		die(line, s, "Error from program");
		return false;
	}

	/**
	 * Plays the game and writes an SGF file.
	 *
	 * @return the winner of the game.
	 */
	Color play() {
		winner = OFF_BOARD;
		board.clear();
		try {
			startPlayers();
			startTime = System.currentTimeMillis();
			if (rules.time > 0) {
				state = SENDING_TIME_LEFT;
				sendTime();
			} else {
				state = REQUESTING_MOVE;
				sendMoveRequest();
			}
			for (final StoneColor color : StoneColor.values()) {
				programs[color.index()].waitFor();
			}
			out.close();
		} catch (final InterruptedException e) { // Should never happen
			e.printStackTrace();
			System.exit(1);
		}
		if(winner == OFF_BOARD){
			System.err.println("Winner was off board.\n" + board.toString());
		}
		return winner;
	}

	/** Sends a move request to the color to play. */
	private void sendMoveRequest() {
		final StoneColor c = getColorToPlay();
		System.err.println(hashCode() + " Sending move request to " + c);
		toPrograms[c.index()].println("genmove " + c);
		toPrograms[c.index()].flush();
		timeLastMoveWasRequested = System.currentTimeMillis();
	}

	/** Sends a time left message to the color to play. */
	private void sendTime() {
		final StoneColor c = getColorToPlay();
		final int timeLeftForThisPlayer = rules.time
				- (int) (timeUsed[c.index()] / 1000);
		System.err.println(hashCode() + " Sending time left (" + timeLeftForThisPlayer + " seconds) to " + c);
		toPrograms[c.index()].println("time_left " + c + " "
				+ timeLeftForThisPlayer + " 0");
		toPrograms[c.index()].flush();
	}

	/**
	 * Sends move to the other player. Depending on whether time is being kept
	 * for this game, state is set to either SENDING_MOVE (because we're going
	 * to send a move and then a time-left message) or SENDING_TIME_LEFT
	 * (because we're going to send just a move and treat the response as an
	 * acknowledgment of a time-left message).
	 */
	private void sendToOtherPlayer(final String move) {
		System.err.println(hashCode() + " Sending move to other player (" + getColorToPlay() + ")");
		if (rules.time > 0) {
			state = SENDING_MOVE;
		} else {
			state = SENDING_TIME_LEFT;
		}
		// Note the color reversal here, because the color to play has
		// already been switched
		toPrograms[getColorToPlay().index()].println((getColorToPlay()
				.opposite() + " " + move).toLowerCase());
		toPrograms[getColorToPlay().index()].flush();
	}

	/** Starts the players running (in different processes). */
	private void startPlayers() {
		try {
			for (final StoneColor color : StoneColor.values()) {
				final int c = color.index();
				final ProcessBuilder builder = new ProcessBuilder("nohup",
						"bash", "-c", players[c], "&");
				builder.redirectErrorStream(true);
				programs[c] = builder.start();
				toPrograms[c] = new PrintWriter(programs[c].getOutputStream());
				new Thread(new PlayerListener(programs[c].getInputStream(),
						this)).start();
			}
		} catch (final IOException e) {
			System.err
					.println("Failed to start one of the following two processes:");
			System.err.println(players[BLACK.index()]);
			System.err.println(players[WHITE.index()]);
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Write move to the output file. If this causes the game to end due to
	 * resignation or running out of time, also sets the winner, ends the
	 * programs, and finishes the SGF file.
	 *
	 * @return true if the game is still ongoing after this move.
	 */
	private boolean writeMoveToSgf(final String coordinates) {
		String timeLeftIndicator = "";
		int timeLeftForThisPlayer = 0;
		if (rules.time > 0) {
			timeUsed[getColorToPlay().index()] += System.currentTimeMillis()
					- timeLastMoveWasRequested;
			timeLeftForThisPlayer = rules.time
					- (int) (timeUsed[getColorToPlay().index()] / 1000);
			timeLeftIndicator = (getColorToPlay() == BLACK ? "BL" : "WL") + "["
					+ timeLeftForThisPlayer + "]";
		}
		if (!coordinates.toLowerCase().equals("resign")) {
			final CoordinateSystem coords = board.getCoordinateSystem();
			out.println((getColorToPlay() == BLACK ? ";B" : ";W") + "["
					+ toSgf(coords.at(coordinates), coords) + "]"
					+ timeLeftIndicator);
			out.flush();
		}
		if (coordinates.toLowerCase().equals("resign")
//				|| rules.time > 0
//				&& timeLeftForThisPlayer <= 0
				) {
			winner = getColorToPlay().opposite();
			out.print(";RE[" + (winner == BLACK ? "B" : "W") + "+");
//			out.print(rules.time > 0 && timeLeftForThisPlayer <= 0 ? "Time"
//					: "Resign");
			out.print("Resign");
			out.println("]");
			out.println(";C[moves:" + board.getTurn() + "]");
			endPrograms();
			out.flush();
			return false;
		}
		return true;
	}

}
