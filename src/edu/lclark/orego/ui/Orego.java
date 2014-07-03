package edu.lclark.orego.ui;

import java.io.*;
import java.util.*;

import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.*;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.PlayerBuilder;
import static java.lang.Integer.parseInt;
import static java.lang.Double.parseDouble;

/**
 * Main class run by GTP front ends. Can also be run directly from the command
 * line. Responds to GTP commands like "showboard" and "genmove black".
 */
public final class Orego {

	private static final String[] DEFAULT_GTP_COMMANDS = { //
	"boardsize", // comments keep the commands on
			"clear_board", // separate lines in the event of a
			"final_score", // source -> format in Eclipse
			"genmove", //
			"genmove_black", //
			"genmove_white", //
			"black", "white", //
			"known_command", //
			"komi", //
			"list_commands", //
			// "loadsgf", //
			// "name", //
			"play", //
			// "playout_count", //
			// "protocol_version", //
			"reg_genmove", //
			"showboard", //
			// "time_left", //
			// "time_settings", //
			"quit", //
			// "undo", //
			"version", //
	// "kgs-genmove_cleanup", //
			// "gogui-analyze_commands", //
			// "kgs-game_over", //
	};

	/** The version of Go Text Protocol that Orego speaks. */
	private static final int GTP_VERSION = 2;

	/**
	 * @param args
	 *            Parameters for the player.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new Orego(args).run();
	}

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

	/** The id number of the current command. */
	private int commandId;

	/** Known GTP commands. */
	private List<String> commands;

	/** Known GoGui commands. */
	private List<String> goguiCommands;

	/**
	 * The input stream.
	 */
	private final BufferedReader in;

	/** The output stream. */
	private final PrintStream out;

	/** The Player object that selects moves. */
	private Player player;
	
	private String commandLineArgs;

	// /** The komi given on the command line. */
	// private double komiArgument = -1;

	/**
	 * @param inStream
	 *            The input stream that drives the program (usually System.in)
	 * @param outStream
	 *            The output stream to print responses to (usually System.out)
	 * @param args
	 * @see Orego#main(String[])
	 */
	private Orego(InputStream inStream, OutputStream outStream, String[] args) {
		in = new BufferedReader(new InputStreamReader(inStream));
		out = new PrintStream(outStream);
		handleCommandLineArguments(args);
		commandLineArgs = "";
		for(String arg : args){
			commandLineArgs += arg + " ";
		}
		commands = new ArrayList<>();
		for (String s : DEFAULT_GTP_COMMANDS) {
			commands.add(s);
		}
		// commands.addAll(player.getCommands());
		// goguiCommands = new ArrayList<>();
		// goguiCommands.addAll(player.getGoguiCommands());
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
		out.println(response + "\n");
	}

	/** @return list of commands in string form */
	private List<String> getCommands() {
		return commands;
	}

	/** @return list of gogui commands in string form */
	private List<String> getGoguiCommands() {
		return goguiCommands;
	}

	/** @return Orego's player */
	private Player getPlayer() {
		return player;
	}

	/**
	 * Processes a GTP command.
	 * 
	 * @return true if command is anything but "quit".
	 */
	private boolean handleCommand(String command) {
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
	private boolean handleCommand(String command, StringTokenizer arguments) {
		CoordinateSystem coords = player.getBoard().getCoordinateSystem();
		if (command.equals("boardsize")) {
			int width = Integer.parseInt(arguments.nextToken());
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
			double score = player.finalScore();
			if (score > 0) {
				acknowledge("B+" + score);
			} else {
				acknowledge("W+" + (-score));
			}
		} else if (command.equals("genmove") || command.equals("genmove_black")
				|| command.equals("genmove_white")
				|| command.equals("kgs-genmove_cleanup")
				|| command.equals("reg_genmove")) {
			StoneColor color;
			if (command.equals("genmove")
					|| command.equals("kgs-genmove_cleanup")
					|| command.equals("reg_genmove")) {
				color = (arguments.nextToken().toLowerCase().charAt(0) == 'b' ? BLACK
						: WHITE);
			} else {
				color = (command.equals("genmove_black") ? BLACK : WHITE);
			}
			// //this assertion to fails when running with CGTC, so skip it if
			// command line option set to true
			// if(cgtc){
			// player.getBoard().setColorToPlay(color);
			// } else {
			assert color == player.getBoard().getColorToPlay();
			// }
			// if (command.equals("kgs-genmove_cleanup")) {
			// player.setCleanUpMode(true);
			// }
			short point = player.bestMove();
			if (point == RESIGN) {
				acknowledge("resign");
				player.clear(); // to stop threaded players
			} else {
				if (!command.equals("reg_genmove")) {
					player.acceptMove(point);
				}
				acknowledge(coords.toString(point));
			}
		}
		// else
		// if (command.equals("gogui-analyze_commands")) {
		// String response = "";
		// for (String s : goguiCommands) {
		// response += s + "\n";
		// }
		// // strip final return
		// if (response.endsWith("\n")) {
		// response = response.substring(0, response.length() - 1);
		// }
		// acknowledge(response);
		// } else if (command.equals("known_command")) {
		// acknowledge(commands.contains(arguments.nextToken()) ? "1" : "0");
		// }
		else if (command.equals("komi")) {
			double komi = parseDouble(arguments.nextToken());
			if (komi == player.getFinalScorer().getKomi()) {
				player.clear();
				acknowledge();
			} else {
				playerBuilder = playerBuilder.komi(komi);
				player = playerBuilder.build();
				acknowledge();
			}
			acknowledge();
		} else if (command.equals("list_commands")) {
			String response = "";
			for (String s : commands) {
				response += s + "\n";
			}
			// Strip final return
			response = response.substring(0, response.length() - 1);
			acknowledge(response);
		}
		// else if (command.equals("loadsgf")) {
		// if (arguments.countTokens() > 1) {
		// System.err.println("the load sgf command recieved "+arguments.countTokens()+"arguments");
		// player.setUpSgf(arguments.nextToken(),
		// Integer.parseInt(arguments.nextToken()));
		// } else {
		// System.err.println("the load sgf command recieved "+arguments.countTokens()+"arguments");
		// player.setUpSgf(arguments.nextToken(), 0);
		// }
		// acknowledge();
		// } else if (command.equals("name")) {
		// acknowledge("Orego");
		// } else
		else if (command.equals("showboard")) {
			String s = player.getBoard().toString();
			s = "\n" + s.substring(0, s.length() - 1);
			acknowledge(s);
		} else if (command.equals("play")) {
			// Both ggo and Goban send "black f4" instead of "play black f4".
			// Accepts such commands.
			// We lower case the command string because GTP defines colors as
			// case insensitive.
			handleCommand(arguments.nextToken().toLowerCase(), arguments);
		}
		// else if (command.equals("playout_count")) {
		// if (player instanceof orego.mcts.McPlayer) {
		// orego.mcts.McPlayer mctsPlayer = (orego.mcts.McPlayer)player;
		// long playouts = 0;
		// for (int i = 0; i < mctsPlayer.getNumberOfThreads(); i++) {
		// orego.mcts.McRunnable mcRunnable =
		// (orego.mcts.McRunnable)mctsPlayer.getRunnable(i);
		// playouts += mcRunnable.getPlayoutsCompleted();
		// }
		// acknowledge("playout="+playouts);
		// }
		// else {
		// acknowledge("playout=null");
		// }
		// } else if (command.equals("protocol_version")) {
		// acknowledge("2");
		// }
		else if (command.equals("quit")) {
			acknowledge();
			player.clear(); // to stop threaded players
			System.exit(0);
		} else if (command.equals("time_left")) {
			// TODO Currently we're ignoring this
			// arguments.nextToken(); // Throw one argument away -- it's
			// irrelevant
			// int secondsLeft = parseInt(arguments.nextToken());
			// player.setRemainingTime(secondsLeft);
			acknowledge();
		}
		// else if (command.equals("kgs-game_over")) {
		// try {
		// Scanner scanner;
		// acknowledge();
		// player.endGame(); // to stop threaded players
		// scanner = new Scanner(new File("QuitAfterGameOver.txt"));
		// if (scanner.nextLine().equals("true")) {
		// scanner.close();
		// return false;
		// } else {
		// scanner.close();
		// }
		// } catch (FileNotFoundException e) {
		// // The file was not found, so we continue to play.
		// }
		// } else if (command.equals("time_settings")) {
		// int secondsLeft = parseInt(arguments.nextToken());
		// player.setRemainingTime(secondsLeft);
		// acknowledge();
		// } else if (command.equals("undo")) {
		// if (player.undo()) {
		// acknowledge();
		// } else {
		// error("Cannot undo");
		// }
		else if (command.equals("version")) {
			String git;
			try{
				verifyCleanGitState();
				git = getGitCommit();
			}catch(IllegalStateException e){
				git = "git state unknown";
			}
			String version = "Orego8  Args: " + commandLineArgs + " Git commit: " + git;
			acknowledge(version);
		}
		else if ((command.equals("black")) || (command.equals("b"))
				|| (command.equals("white")) || (command.equals("w"))) {
			char color = command.charAt(0);
			short point = coords.at(arguments.nextToken());
			player.setColorToPlay(color == 'b' ? BLACK : WHITE);
			if (player.acceptMove(point) == Legality.OK) {
				acknowledge();
			} else {
				error("illegal move");
			}
		}
		// else if (command.equals("fixed_handicap")) {
		// int handicapSize = parseInt(arguments.nextToken());
		// if (handicapSize >= 2 && handicapSize <= 9) {
		// player.getBoard().setUpHandicap(handicapSize);
		// acknowledge();
		// } else {
		// error("Invalid handicap size");
		// }
		// }
		else { // If Orego doesn't know how to handle this specific command,
				// maybe the player will
				// String result = player.handleCommand(command, arguments);
			// if (result == null) {
			error("unknown command: " + command);
			// } else {
			// acknowledge(result);
			// }
		}
		return true;
	}

	private PlayerBuilder playerBuilder;

	private void handleCommandLineArguments(String[] args) {
		playerBuilder = new PlayerBuilder();
		for (String argument : args) {
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
			if (left.equals("biasdelay")) {
				playerBuilder.biasDelay(parseInt(right));
			} else if (left.equals("boardsize")) {
				playerBuilder.boardWidth(parseInt(right));
			} else if (left.equals("gestation")) {
				playerBuilder.gestation(parseInt(right));
			} else if (left.equals("komi")) {
				playerBuilder.komi(parseDouble(right));
			} else if (left.equals("msec")) {
				playerBuilder.msecPerMove(parseInt(right));
			} else if (left.equals("threads")) {
				playerBuilder.threads(parseInt(right));
			} else if (left.equals("rave")) {
				playerBuilder.rave();
			} else if(left.equals("book")){
				playerBuilder.openingBook();
			}else {
				throw new IllegalArgumentException("Unknown command line argument: " + left);
			}
		}
		player = playerBuilder.build();
	}
	
	private static void verifyCleanGitState() {
		try (Scanner s = new Scanner(new ProcessBuilder("git", "status", "-s").start().getInputStream())) {
			if (s.hasNextLine()) {
				throw new IllegalStateException("Not in clean git state");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Returns the current git commit string.
	 */
	private static String getGitCommit() {
		try (Scanner s = new Scanner(new ProcessBuilder("git", "log", "--pretty=format:'%H'", "-n", "1").start().getInputStream())) {
			String commit = s.nextLine();
				// substring to remove single quotes that would otherwise appear
				return commit.substring(1, commit.length() - 1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return "";
	}

}
