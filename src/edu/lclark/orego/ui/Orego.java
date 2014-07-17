package edu.lclark.orego.ui;

import static edu.lclark.orego.core.CoordinateSystem.RESIGN;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.experiment.Git.getGitCommit;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import static java.io.File.separator;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

import static edu.lclark.orego.experiment.Logging.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.experiment.Logging;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.PlayerBuilder;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.util.ShortSet;

/**
 * Main class run by GTP front ends. Can also be run directly from the command
 * line. Responds to GTP commands like "showboard" and "genmove black".
 * <p>
 * Command-line arguments take the form <code>feature=value</code>. For
 * convenience, a boolean feature can be set to true simply as
 * <code>feature</code>.
 * <dl>
 * <dt>biasdelay</dt>
 * <dd>Number of runs required through a node before heuristic biases are added.
 * Because of initial wins and losses given to every move, any value less than
 * or equal to 732 results in immediate application of bias. Defaults to 800.</dd>
 * <dt>boardsize</dt>
 * <dd>Width of board. Defaults to 19.</dd>
 * <dt>book</dt>
 * <dd>Toggles whether Orego will play moves from a fuseki book at the beginning
 * of the game. Defaults to true.</dd>
 * <dt>grace</dt>
 * <dd>Toggles coup de grace mode. When the opposing player passes, Orego will
 * attempt to clear the board of enemy dead stones, or pass if it can win with
 * the current board state. Defaults to false.</dd>
 * <dt>gestation</dt>
 * <dd>The amount of runs required through a move before a child is created for
 * that move. Defaults to 4.</dd>
 * <dt>komi</dt>
 * <dd>Sets the komi for the game. Defaults to 7.5.</dd>
 * <dt>lgrf2</dt>
 * <dd>Toggles Last Good Reply with Forgetting (level 2). During playouts, Orego
 * tracks successful replies to a move or a chain of two moves, for use in
 * future playouts. Defaults to true.</dd>
 * <dt>memory</dt>
 * <dd>Megabytes of memory used by Orego. The transposition table is scaled
 * accordingly. Should match the memory allocated to the Java virtual machine
 * with a command-line argument like -Xmx1024M. Defaults to 1024.
 * <dt>msec</dt>
 * <dd>Sets the milliseconds that Orego takes to decide a move. Not relevant
 * when using time management. Defaults to 1000 milliseconds.</dd>
 * <dt>pondering</dt>
 * <dd>Toggles whether Orego thinks during the opponent's turn. Defaults to
 * false.</dd>
 * <dt>rave</dt>
 * <dd>Toggles Rapid Action Value Estimation. Defaults to true.</dd>
 * <dt>threads</dt>
 * <dd>The number of threads Orego uses to think. Defaults to 2.</dd>
 * <dt>time-management</dt>
 * <dd>Set the type of time manager to be used by Orego. If not specified, Orego
 * will rely on msec. Options are uniform (the default) and exiting.</dd>
 * </dl>
 */
public final class Orego {

	private static final String[] DEFAULT_GTP_COMMANDS = { "black",
			"boardsize", "clear_board", "final_score", "final_status_list", "fixed_handicap",
			"genmove", "genmove_black", "genmove_white", "known_command",
			"kgs-game_over", "kgs-genmove_cleanup", "komi", "list_commands",
			"loadsgf", "name", "play", "playout_count", "protocol_version",
			"quit", "reg_genmove", "showboard", "time_left", "time_settings",
			"undo", "version", "white", };

	public static void main(String[] args) throws IOException {
		new Orego(args).run();
	}

	/** The GTP id number of the current command. */
	private int commandId;

	/** Command line argument for reporting version. */
	private String commandLineArgs;

	/** Known GTP commands. */
	private final List<String> commands;

	/**
	 * The input stream.
	 */
	private final BufferedReader in;

	/** The output stream. */
	private final PrintStream out;

	/** The Player object that selects moves. */
	private Player player;

	/** Builds the player. */
	private PlayerBuilder playerBuilder;

	/**
	 * @param inStream
	 *            The input stream that drives the program (usually System.in)
	 * @param outStream
	 *            The output stream to print responses to (usually System.out)
	 */
	private Orego(InputStream inStream, OutputStream outStream, String[] args) {
		in = new BufferedReader(new InputStreamReader(inStream));
		out = new PrintStream(outStream);
		handleCommandLineArguments(args);
		commandLineArgs = "";
		for (final String arg : args) {
			commandLineArgs += arg + " ";
		}
		commands = new ArrayList<>();
		for (final String s : DEFAULT_GTP_COMMANDS) {
			commands.add(s);
		}
	}

	private Orego(String[] args) {
		this(System.in, System.out, args);
	}

	/** Acknowledges that the last command was handled correctly. */
	private void acknowledge() {
		acknowledge("");
	}

	/**
	 * Acknowledges that the last command was handled correctly, with the
	 * specified message.
	 */
	private void acknowledge(String message) {
		String response;
		if (commandId >= 0) {
			response = "=" + commandId + " " + message;
		} else {
			response = "= " + message;
		}
		log("Sent: " + response);
		out.println(response + "\n");
	}

	/** Indicates that the last command could not be handled. */
	private void error(String message) {
		String response;
		if (commandId >= 0) {
			response = "?" + commandId + " " + message;
		} else {
			response = "? " + message;
		}
		log("Sent: " + response);
		out.println(response + "\n");
	}

	/**
	 * Processes a GTP command.
	 * 
	 * @return true if command is anything but "quit".
	 */
	private boolean handleCommand(String command) {
		log("Received command: " + command);
		// Remove any comment
		final int commentStart = command.indexOf("#");
		if (commentStart >= 0) {
			command = command.substring(0, command.indexOf('#'));
		}
		// Parse the string into optional id number, command, and arguments
		final StringTokenizer arguments = new StringTokenizer(command);
		final String token1 = arguments.nextToken();
		try {
			commandId = Integer.parseInt(token1);
			command = arguments.nextToken().toLowerCase();
		} catch (final NumberFormatException exception) {
			commandId = -1;
			command = token1.toLowerCase();
		}
		// Call lengthier handleCommand method
		return handleCommand(command, arguments);
	}

	/**
	 * Helper method for handleCommand(String).
	 * 
	 * @return true if command is anything but "quit".
	 * @param arguments
	 *            contains arguments to command, e.g., color to play
	 */
	private boolean handleCommand(String command, StringTokenizer arguments) {
		final CoordinateSystem coords = player.getBoard().getCoordinateSystem();
		if (command.equals("black") || command.equals("b")
				|| command.equals("white") || command.equals("w")) {
			final short point = coords.at(arguments.nextToken());
			player.setColorToPlay(command.charAt(0) == 'b' ? BLACK : WHITE);
			if (player.acceptMove(point) == Legality.OK) {
				acknowledge();
			} else {
				error("Illegal move");
			}
		} else if (command.equals("boardsize")) {
			final int width = Integer.parseInt(arguments.nextToken());
			if (width == coords.getWidth()) {
				player.clear();
				acknowledge();
			} else if (width >= 2 && width <= 19) {
				playerBuilder = playerBuilder.boardWidth(width);
				player = playerBuilder.build();
				acknowledge();
			} else {
				error("unacceptable size");
			}
		} else if (command.equals("clear_board")) {
			player.clear();
			acknowledge();
		} else if (command.equals("final_score")) {
			final double score = player.finalScore();
			if (score > 0) {
				acknowledge("B+" + score);
			} else if (score < 0) {
				acknowledge("W+" + -score);
			} else {
				acknowledge("0");
			}
		} else if(command.equals("final_status_list")){
			String status = arguments.nextToken();
			if(status.equals("dead")){
				ShortSet deadStones = player.findDeadStones(0.75, WHITE);
				deadStones.addAll(player.findDeadStones(0.75, BLACK));
				acknowledge(produceVerticesString(deadStones));
			} else if(status.equals("alive")){
				acknowledge(produceVerticesString(player.getLiveStones(0.75)));
			}
		} else if (command.equals("fixed_handicap")) {
			final int handicapSize = parseInt(arguments.nextToken());
			if (handicapSize >= 2 && handicapSize <= 9) {
				player.setUpHandicap(handicapSize);
				acknowledge();
			} else {
				error("Invalid handicap size");
			}
		} else if (command.equals("genmove") || command.equals("genmove_black")
				|| command.equals("genmove_white")
				|| command.equals("kgs-genmove_cleanup")
				|| command.equals("reg_genmove")) {
			StoneColor color;
			if (command.equals("genmove")
					|| command.equals("kgs-genmove_cleanup")
					|| command.equals("reg_genmove")) {
				color = arguments.nextToken().toLowerCase().charAt(0) == 'b' ? BLACK
						: WHITE;
			} else {
				color = command.equals("genmove_black") ? BLACK : WHITE;
			}
			assert color == player.getBoard().getColorToPlay();
			if (command.equals("kgs-genmove_cleanup")) {
				player.setCleanupMode(true);
			}
			final short point = player.bestMove();
			if (point == RESIGN) {
				acknowledge("resign");
				player.clear(); // to stop threaded players
			} else {
				if (!command.equals("reg_genmove")) {
					player.acceptMove(point);
				}
				acknowledge(coords.toString(point));
			}
		} else if (command.equals("kgs-game_over")) {
			try (Scanner scanner = new Scanner(new File(OREGO_ROOT + separator
					+ "config" + separator + "quit.txt"))) {
				acknowledge();
				player.endGame(); // to stop threaded players
				if (scanner.nextLine().equals("true")) {
					return false;
				}
			} catch (final FileNotFoundException e) {
				// The file was not found, so we continue to play.
			}
		} else if (command.equals("known_command")) {
			acknowledge(commands.contains(arguments.nextToken()) ? "1" : "0");
		} else if (command.equals("komi")) {
			final double komi = parseDouble(arguments.nextToken());
			if (komi == player.getFinalScorer().getKomi()) {
				player.clear();
			} else {
				playerBuilder = playerBuilder.komi(komi);
				player = playerBuilder.build();
			}
			acknowledge();
		} else if (command.equals("list_commands")) {
			String response = "";
			for (final String s : commands) {
				response += s + "\n";
			}
			// Strip final newline
			response = response.substring(0, response.length() - 1);
			acknowledge(response);
		} else if (command.equals("loadsgf")) {
			final SgfParser parser = new SgfParser(player.getBoard()
					.getCoordinateSystem(), false);
			player.setUpSgfGame(parser.parseGameFromFile(new File(arguments
					.nextToken())));
			acknowledge();
		} else if (command.equals("name")) {
			acknowledge("Orego");
		} else if (command.equals("showboard")) {
			String s = player.getBoard().toString();
			s = "\n" + s.substring(0, s.length() - 1);
			acknowledge(s);
		} else if (command.equals("play")) {
			// Both ggo and Goban send "black f4" instead of "play black f4".
			// Accepts such commands.
			// We lower case the command string because GTP defines colors as
			// case insensitive.
			handleCommand(arguments.nextToken().toLowerCase(), arguments);
		} else if (command.equals("playout_count")) {
			acknowledge("playout count: " + player.getPlayoutCount());
		} else if (command.equals("protocol_version")) {
			acknowledge("2");
		} else if (command.equals("quit")) {
			acknowledge();
			player.endGame(); // to stop threaded players
			return false;
		} else if (command.equals("time_left")) {
			arguments.nextToken(); // Throw away color argument
			final int secondsLeft = parseInt(arguments.nextToken());
			player.setRemainingTime(secondsLeft);
			acknowledge();
		} else if (command.equals("time_settings")) {
			final int secondsLeft = parseInt(arguments.nextToken());
			player.setRemainingTime(secondsLeft);
			acknowledge();
		} else if (command.equals("undo")) {
			if (player.undo()) {
				acknowledge();
			} else {
				error("Cannot undo");
			}
		} else if (command.equals("version")) {
			String git;
			git = getGitCommit();
			if (git.isEmpty()) {
				git = "unknown";
			}
			final String version = "Orego 8 Git commit: " + git + " Args: "
					+ commandLineArgs;
			acknowledge(version);
		} else {
			error("unknown command: " + command);
		}
		return true;
	}

	private String produceVerticesString(ShortSet deadStones) {
		String vertices = "";
		for(int i = 0; i < deadStones.size(); i++){
			vertices += player.getBoard().getCoordinateSystem().toString(deadStones.get(i)) + " ";
		}
		return vertices;
	}

	/** Updates playerBuilder with command-line arguments. */
	private void handleCommandLineArguments(String[] args) {
		playerBuilder = new PlayerBuilder();
		for (final String argument : args) {
			final int j = argument.indexOf('=');
			String left, right;
			if (j > 0) {
				left = argument.substring(0, j);
				right = argument.substring(j + 1);
			} else {
				left = argument;
				right = "true";
			}
			// Handle properties
			if (left.equals("biasdelay")) {
				playerBuilder.biasDelay(parseInt(right));
			} else if (left.equals("boardsize")) {
				playerBuilder.boardWidth(parseInt(right));
			} else if (left.equals("book")) {
				playerBuilder.openingBook(parseBoolean(right));
			} else if (left.equals("grace")) {
				playerBuilder.coupDeGrace(parseBoolean(right));
			} else if (left.equals("gestation")) {
				playerBuilder.gestation(parseInt(right));
			} else if (left.equals("komi")) {
				playerBuilder.komi(parseDouble(right));
			} else if (left.equals("lgrf2")) {
				playerBuilder.lgrf2(parseBoolean(right));
			} else if (left.equals("logfile")){
				Logging.setFilePath(right);
			} else if (left.equals("memory")) {
				playerBuilder.memorySize(parseInt(right));
			} else if (left.equals("msec")) {
				playerBuilder.msecPerMove(parseInt(right));
			} else if (left.equals("ponder")) {
				playerBuilder.ponder(parseBoolean(right));
			} else if (left.equals("rave")) {
				playerBuilder.rave(parseBoolean(right));
			} else if (left.equals("threads")) {
				playerBuilder.threads(parseInt(right));
			} else if (left.equals("time-management")) {
				playerBuilder.timeManagement(right);
			} else {
				throw new IllegalArgumentException(
						"Unknown command line argument: " + left);
			}
		}
		player = playerBuilder.build();
	}

	/** Receives and handles GTP commands until told to quit. */
	private void run() throws IOException {
		String input;
		do {
			input = "";
			while (input.equals("")) {
				input = in.readLine();
				if (input == null) {
					return;
				}
			}
		} while (handleCommand(input));
	}

}
