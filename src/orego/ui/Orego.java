package orego.ui;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.RESIGN;
import static orego.core.Coordinates.at;
import static orego.core.Coordinates.pointToString;
import static orego.experiment.Debug.debug;
import static orego.experiment.Debug.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import orego.play.Playable;
import orego.play.Player;
import orego.play.UnknownPropertyException;

/**
 * Main class run by GTP front ends. Can also be run directly from the command
 * line. Responds to GTP commands like "showboard" and "genmove black".
 */
public class Orego {

	public static final String[] DEFAULT_GTP_COMMANDS = { //
		"boardsize", // comments keep the commands on
		"clear_board", // separate lines in the event of a
		"final_score", // source -> format
		"genmove", //
		"genmove_black", //
		"genmove_white", //
		"black", "white", //
		"known_command", //
		"komi", //
		"list_commands", //
		"loadsgf", //
		"name", //
		"play", //
		"playout_count", //
		"protocol_version", //
		"reg_genmove", //
		"showboard", //
		"time_left", //
		"time_settings", //
		"quit", //
		"undo", //
		"version", //
		"kgs-genmove_cleanup", //
		"gogui-analyze_commands", //
		"kgs-game_over", //
	};

	/** The version of Go Text Protocol that Orego speaks. */
	public static final int GTP_VERSION = 2;

	/**
	 * Packages to try finding players in. If no class is found in the first,
	 * the second is tried, and so on. The last, empty string in this array
	 * allows the user to specify a specific, non-Orego package.
	 */
	public static final String[] PLAYER_PACKAGES = { "orego.ladder", 
			"orego.mcts",
			"orego.play", "orego.response", "" };

	/** String to return in response to version command. */
	public static final String VERSION_STRING = "7.13";

	/**
	 * @param args
	 *            Parameters for the player.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Orego game = new Orego(args);
		String input;
		do {
			input = "";
			while (input.equals("")) {
				input = game.in.readLine();
				if (input == null) {
					return;
				}
			}
		} while (game.handleCommand(input));
		setDebugToStderr(false);
		setDebugFile(null);
	}

	/** The id number of the current command. */
	private int commandId;

	/** Known GTP commands. */
	private ArrayList<String> commands;

	/** Known GoGui commands. */
	private ArrayList<String> goguiCommands;

	/**
	 * The input stream.
	 */
	private BufferedReader in;

	/** The output stream. */
	private PrintStream out;

	/** The Player object that selects moves. */
	private Playable player;

	/**
	 * @param inStream
	 *            The input stream that drives the program (usually System.in)
	 * @param outStream
	 *            The output stream to print responses to (usually System.out)
	 * @param args
	 * @see Orego#main(String[])
	 */
	public Orego(InputStream inStream, OutputStream outStream, String[] args) {
		in = new BufferedReader(new InputStreamReader(inStream));
		out = new PrintStream(outStream);
		handleCommandLineArguments(args);
		player.reset();
		commands = new ArrayList<String>();
		for (String s : DEFAULT_GTP_COMMANDS) {
			commands.add(s);
		}
		commands.addAll(player.getCommands());
		goguiCommands = new ArrayList<String>();
		goguiCommands.addAll(player.getGoguiCommands());
	}

	public Orego(String[] args) {
		this(System.in, System.out, args);
	}

	/** Acknowledges that the last command was handled correctly. */
	protected void acknowledge() {
		acknowledge("");
	}

	/**
	 * Acknowledges that the last command was handled correctly, with the
	 * specified message.
	 */
	protected void acknowledge(String message) {
		String response;
		if (commandId >= 0) {
			response = "=" + commandId + " " + message;
		} else {
			response = "= " + message;
		}
		debug("Orego: " + response);
		out.println(response + "\n");
	}

	/** Indicates that the last command could not be handled. */
	protected void error(String message) {
		String response;
		if (commandId >= 0) {
			response = "?" + commandId + " " + message;
		} else {
			response = "? " + message;
		}
		debug("Orego (GTP error): " + response);
		out.println(response + "\n");
	}

	/** @return list of commands in string form */
	public ArrayList<String> getCommands() {
		return commands;
	}

	/** @return list of gogui commands in string form */
	public ArrayList<String> getGoguiCommands() {
		return goguiCommands;
	}

	/** @return Orego's player */
	public Player getPlayer() {
		return (Player) player;
	}

	/**
	 * Processes a GTP command.
	 * 
	 * @return true if command is anything but "quit".
	 */
	public boolean handleCommand(String command) {
		debug("GTP: " + command);
		// Remove any comment
		int commentStart = command.indexOf("#");
		if (commentStart >= 0) {
			command = command.substring(0, command.indexOf('#'));
		}
		// Parse the string into optional id number, command, and arguments
		StringTokenizer arguments = new StringTokenizer(command);
		String token1 = arguments.nextToken();
		try {
			commandId = Integer.parseInt(token1);
			command = arguments.nextToken().toLowerCase();
		} catch (NumberFormatException exception) {
			commandId = -1;
			command = token1.toLowerCase();
		}
		return handleCommand(command, arguments);
	}

	/**
	 * Helper method for handleCommand(String).
	 * 
	 * @return true if command is anything but "quit".
	 */
	protected boolean handleCommand(String command, StringTokenizer arguments) {
		if (command.equals("boardsize")) {
			if (arguments.countTokens() == 1) {
				int width = parseInt(arguments.nextToken());
				if (width == BOARD_WIDTH) {
					player.reset();
					acknowledge();
				} else {
					error("unacceptable size");
				}
			} else {
				error("unacceptable size");
			}
		} else if (command.equals("clear_board")) {
			player.reset();
			acknowledge();
		} else if (command.equals("final_score")) {
			double score = player.finalScore() - 0.5;
			if (score > 0) {
				acknowledge("B+" + score);
			} else {
				acknowledge("W+" + (-score));
			}
			debug(player.getBoard());
		} else if (command.equals("genmove") || command.equals("genmove_black")
				|| command.equals("genmove_white")
				|| command.equals("kgs-genmove_cleanup")
				|| command.equals("reg_genmove")) {
			int color;
			int point;
			if (command.equals("genmove")
					|| command.equals("kgs-genmove_cleanup")
					|| command.equals("reg_genmove")) {
				color = (arguments.nextToken().toLowerCase().charAt(0) == 'b' ? BLACK
						: WHITE);
			} else {
				color = (command.equals("genmove_black") ? BLACK : WHITE);
			}
			assert color == player.getBoard().getColorToPlay();
			point = player.bestMove();
			if (point == RESIGN) {
				acknowledge("resign");
			} else {
				if (!command.equals("reg_genmove")) {
					player.acceptMove(point);
				}
				acknowledge(pointToString(point));
			}
		} else if (command.equals("gogui-analyze_commands")) {
			String response = "";
			for (String s : goguiCommands) {
				response += s + "\n";
			}
			// strip final return
			if (response.endsWith("\n")) {
				response = response.substring(0, response.length() - 1);
			}
			acknowledge(response);
		} else if (command.equals("known_command")) {
			acknowledge(commands.contains(arguments.nextToken()) ? "1" : "0");
		} else if (command.equals("komi")) {
			player.setKomi(parseDouble(arguments.nextToken()));
			acknowledge();
		} else if (command.equals("list_commands")) {
			String response = "";
			for (String s : commands) {
				response += s + "\n";
			}
			// Strip final return
			response = response.substring(0, response.length() - 1);
			acknowledge(response);
		} else if (command.equals("loadsgf")) {
			if (arguments.countTokens() > 1) {
				player.setUpSgf(arguments.nextToken(),
						Integer.parseInt(arguments.nextToken()));
			} else {
				player.setUpSgf(arguments.nextToken(), 0);
			}
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
			if (player instanceof orego.mcts.McPlayer) {
				orego.mcts.McPlayer mctsPlayer = (orego.mcts.McPlayer)player;
				long playouts = 0;
				for (int i = 0; i < mctsPlayer.getNumberOfThreads(); i++) {
					orego.mcts.McRunnable mcRunnable = (orego.mcts.McRunnable)mctsPlayer.getRunnable(i);
					playouts += mcRunnable.getPlayoutsCompleted();
				}
				acknowledge("playout="+playouts);
			}
			else {
				acknowledge("playout=null");
			}			
		} else if (command.equals("protocol_version")) {
			acknowledge("2");
		} else if (command.equals("quit")) {
			acknowledge();
			player.reset(); // to stop threaded players
			return false;
		} else if (command.equals("time_left")) {
			arguments.nextToken(); // Throw one argument away -- it's irrelevant
			int secondsLeft = parseInt(arguments.nextToken());
			player.setRemainingTime(secondsLeft);
			acknowledge();
		} else if (command.equals("kgs-game_over")) {
			try {
				Scanner scanner;
				acknowledge();
				player.reset(); // to stop threaded players
				scanner = new Scanner(new File("QuitAfterGameOver.txt"));
				if (scanner.nextLine().equals("true")) {
					scanner.close();
					return false;
				} else {
					scanner.close();
					acknowledge();
				}
			} catch (FileNotFoundException e) {
				// The file was not found, so we continue to play.
				acknowledge();
			}
		} else if (command.equals("time_settings")) {
			int secondsLeft = parseInt(arguments.nextToken());
			player.setRemainingTime(secondsLeft);
			acknowledge();
		} else if (command.equals("undo")) {
			if (player.undo()) {
				acknowledge();
			} else {
				error("Cannot undo");
			}
		} else if (command.equals("version")) {
			acknowledge(VERSION_STRING + " " + player);
		} else if ((command.equals("black")) || (command.equals("b"))
				|| (command.equals("white")) || (command.equals("w"))) {
			char color = command.charAt(0);
			int point = at(arguments.nextToken());
			((Player) player).setColorToPlay(color == 'b' ? BLACK : WHITE);
			if (player.acceptMove(point) == PLAY_OK) {
				acknowledge();
			} else {
				error("illegal move");
			}
		} else if (command.equals("fixed_handicap")) {
			int handicapSize = parseInt(arguments.nextToken());
			if (handicapSize >= 2 && handicapSize <= 9) {
				player.getBoard().setUpHandicap(handicapSize);
				acknowledge();
			} else {
				error("Invalid handicap size");
			}
		} else { // If Orego doesn't know how to handle this specific command,
			// maybe the player will
			String result = player.handleCommand(command, arguments);
			if (result == null) {
				error("unknown command: " + command);
			} else {
				acknowledge(result);
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	protected void handleCommandLineArguments(String[] args) {
		HashMap<String, String> propertyMap = new HashMap<String, String>();
		// default settings
		String playerClass = "Lgrf2";
		propertyMap.put("heuristics", "Escape@20:Pattern@20:Capture@20");
		// Parse arguments
		for (int i = 0; i < args.length; i++) {
			String argument = args[i];
			// Split argument at the equals sign
			int j = argument.indexOf('=');
			String left, right;
			if (j > 0) {
				left = argument.substring(0, j);
				right = argument.substring(j + 1);
			} else {
				left = argument;
				right = "true";
			}
			// Handle properties
			debug("property: " + left);
			debug("property value: " + right);
			if (left.equals("debug")) {
				assert right.equals("true");
				setDebugToStderr(true);
			} else if (left.equals("debugfile")) {
				setDebugFile(right);
			} else if (left.equals("player")) {
				playerClass = right;
			} else { // Let the player set this property
				propertyMap.put(left, right);
			}
		}
		try { // Create player from string
			if (!playerClass.endsWith("Player")) {
				playerClass += "Player";
			}
			for (String pkg : PLAYER_PACKAGES) {
				String qualifiedPlayerClass = playerClass;
				if (!qualifiedPlayerClass.startsWith("orego.")
						&& pkg.length() > 0) {
					qualifiedPlayerClass = pkg + "." + qualifiedPlayerClass;
				}
				Class<Playable> c;
				try {
					c = (Class<Playable>) Class.forName(qualifiedPlayerClass);
				} catch (ClassNotFoundException e) {
					continue;
				}
				Constructor<Playable> constructor = (Constructor<Playable>) c
						.getConstructor();
				player = constructor.newInstance();
				break;
			}
		} catch (Exception e) {
			System.err
					.println("Does the player class have only a zero-argument constructor? It must!");
			e.printStackTrace();
			System.exit(1);
		}
		if (player == null) {
			// We didn't manage to find a class for our player
			throw new IllegalArgumentException(String.format(
					"Could not create a player for class %s.", playerClass));
		}
		// Let the player set all other properties
		for (String property : propertyMap.keySet()) {
			try {
				player.setProperty(property, propertyMap.get(property));
			} catch (UnknownPropertyException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}
