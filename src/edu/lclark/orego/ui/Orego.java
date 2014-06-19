package edu.lclark.orego.ui;

import java.io.*;
import java.util.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.CopiableStructureFactory;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.SimpleSearchNodeBuilder;
import edu.lclark.orego.mcts.SimpleTreeUpdater;
import edu.lclark.orego.mcts.TranspositionTable;
import edu.lclark.orego.mcts.UctDescender;

/**
 * Main class run by GTP front ends. Can also be run directly from the command
 * line. Responds to GTP commands like "showboard" and "genmove black".
 */
public final class Orego {

	private static final String[] DEFAULT_GTP_COMMANDS = { //
//		"boardsize", // comments keep the commands on
//		"clear_board", // separate lines in the event of a
//		"final_score", // source -> format in Eclipse
//		"genmove", //
//		"genmove_black", //
//		"genmove_white", //
//		"black", "white", //
//		"known_command", //
//		"komi", //
//		"list_commands", //
//		"loadsgf", //
//		"name", //
//		"play", //
//		"playout_count", //
//		"protocol_version", //
//		"reg_genmove", //
		"showboard", //
//		"time_left", //
//		"time_settings", //
//		"quit", //
//		"undo", //
//		"version", //
//		"kgs-genmove_cleanup", //
//		"gogui-analyze_commands", //
//		"kgs-game_over", //
	};

	/** The version of Go Text Protocol that Orego speaks. */
	private static final int GTP_VERSION = 2;

	// TODO Can this be updated automatically?
//	/** String to return in response to version command. */
//	public static final String VERSION_STRING = "7.15";
	
	private CoordinateSystem coords;

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
	
//	/** The komi given on the command line. */
//	private double komiArgument = -1;

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
		commands = new ArrayList<>();
		for (String s : DEFAULT_GTP_COMMANDS) {
			commands.add(s);
		}
//		commands.addAll(player.getCommands());
//		goguiCommands = new ArrayList<>();
//		goguiCommands.addAll(player.getGoguiCommands());
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
		if (command.equals("boardsize")) {
			if (arguments.countTokens() == 1) {
				int width = Integer.parseInt(arguments.nextToken());
				if (width == coords.getWidth()) {
					player.clear();
					acknowledge();
				} else if(width < 20){
					buildPlayer(width);
					acknowledge();
				} else {
					error("unacceptable size");
				}
			} else {
				error("unacceptable size");
			}
		} else if (command.equals("clear_board")) {
			player.clear();
			acknowledge();
		} else
//			if (command.equals("final_score")) {
//			double score = player.finalScore();
//			if (score > 0) {
//				acknowledge("B+" + score);
//			} else {
//				acknowledge("W+" + (-score));
//			}
//			debug(player.getBoard());
//		} else
		if (command.equals("genmove") || command.equals("genmove_black")
				|| command.equals("genmove_white")
				|| command.equals("kgs-genmove_cleanup")
				|| command.equals("reg_genmove")) {
			StoneColor color;
			short point;
			if (command.equals("genmove")
					|| command.equals("kgs-genmove_cleanup")
					|| command.equals("reg_genmove")) {
				color = (arguments.nextToken().toLowerCase().charAt(0) == 'b' ? BLACK
						: WHITE);
			} else {
				color = (command.equals("genmove_black") ? BLACK : WHITE);
			}
//			//this assertion to fails when running with CGTC, so skip it if command line option set to true
//			if(cgtc){
//				player.getBoard().setColorToPlay(color);
//			} else {
				assert color == player.getBoard().getColorToPlay();
//			}
//			if (command.equals("kgs-genmove_cleanup")) {
//				player.setCleanUpMode(true);
//			}
			point = player.bestMove();
			if (point == RESIGN) {
				acknowledge("resign");
			} else {
				if (!command.equals("reg_genmove")) {
					player.acceptMove(point);
				}
				// TODO Awkward
				acknowledge(player.getBoard().getCoordinateSystem().toString(point));
			}
		} else 
//		if (command.equals("gogui-analyze_commands")) {
//			String response = "";
//			for (String s : goguiCommands) {
//				response += s + "\n";
//			}
//			// strip final return
//			if (response.endsWith("\n")) {
//				response = response.substring(0, response.length() - 1);
//			}
//			acknowledge(response);
//		} else if (command.equals("known_command")) {
//			acknowledge(commands.contains(arguments.nextToken()) ? "1" : "0");
//		} else if (command.equals("komi")) {
//			player.setKomi(parseDouble(arguments.nextToken()));
//			acknowledge();
//		} else if (command.equals("list_commands")) {
//			String response = "";
//			for (String s : commands) {
//				response += s + "\n";
//			}
//			// Strip final return
//			response = response.substring(0, response.length() - 1);
//			acknowledge(response);
//		} else if (command.equals("loadsgf")) {
//			if (arguments.countTokens() > 1) {
//				System.err.println("the load sgf command recieved "+arguments.countTokens()+"arguments");
//				player.setUpSgf(arguments.nextToken(),
//						Integer.parseInt(arguments.nextToken()));
//			} else {
//				System.err.println("the load sgf command recieved "+arguments.countTokens()+"arguments");
//				player.setUpSgf(arguments.nextToken(), 0);
//			}
//			acknowledge();
//		} else if (command.equals("name")) {
//			acknowledge("Orego");
//		} else 
		if (command.equals("showboard")) {
			String s = player.getBoard().toString();
			s = "\n" + s.substring(0, s.length() - 1);
			acknowledge(s);
		}
		else if (command.equals("play")) {
			// Both ggo and Goban send "black f4" instead of "play black f4".
			// Accepts such commands.
			// We lower case the command string because GTP defines colors as
			// case insensitive.
			handleCommand(arguments.nextToken().toLowerCase(), arguments);
		}
//		 else if (command.equals("playout_count")) {
//			if (player instanceof orego.mcts.McPlayer) {
//				orego.mcts.McPlayer mctsPlayer = (orego.mcts.McPlayer)player;
//				long playouts = 0;
//				for (int i = 0; i < mctsPlayer.getNumberOfThreads(); i++) {
//					orego.mcts.McRunnable mcRunnable = (orego.mcts.McRunnable)mctsPlayer.getRunnable(i);
//					playouts += mcRunnable.getPlayoutsCompleted();
//				}
//				acknowledge("playout="+playouts);
//			}
//			else {
//				acknowledge("playout=null");
//			}			
//		} else if (command.equals("protocol_version")) {
//			acknowledge("2");
//		} else if (command.equals("quit")) {
//			acknowledge();
//			player.reset(); // to stop threaded players
//			return false;
//		} else if (command.equals("time_left")) {
//			arguments.nextToken(); // Throw one argument away -- it's irrelevant
//			int secondsLeft = parseInt(arguments.nextToken());
//			player.setRemainingTime(secondsLeft);
//			acknowledge();
//		} else if (command.equals("kgs-game_over")) {
//			try {
//				Scanner scanner;
//				acknowledge();
//				player.endGame(); // to stop threaded players
//				scanner = new Scanner(new File("QuitAfterGameOver.txt"));
//				if (scanner.nextLine().equals("true")) {
//					scanner.close();
//					return false;
//				} else {
//					scanner.close();
//				}
//			} catch (FileNotFoundException e) {
//				// The file was not found, so we continue to play.
//			}
//		} else if (command.equals("time_settings")) {
//			int secondsLeft = parseInt(arguments.nextToken());
//			player.setRemainingTime(secondsLeft);
//			acknowledge();
//		} else if (command.equals("undo")) {
//			if (player.undo()) {
//				acknowledge();
//			} else {
//				error("Cannot undo");
//			}
//		} else if (command.equals("version")) {
//			acknowledge(VERSION_STRING + " " + player);
//		} 
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
//			else if (command.equals("fixed_handicap")) {
//			int handicapSize = parseInt(arguments.nextToken());
//			if (handicapSize >= 2 && handicapSize <= 9) {
//				player.getBoard().setUpHandicap(handicapSize);
//				acknowledge();
//			} else {
//				error("Invalid handicap size");
//			}
//		} 
	else { // If Orego doesn't know how to handle this specific command,
			// maybe the player will
//			String result = player.handleCommand(command, arguments);
//			if (result == null) {
				error("unknown command: " + command);
//			} else {
//				acknowledge(result);
//			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private void handleCommandLineArguments(String[] args) {
//		ArrayList<String> properties = new ArrayList<String>();
//		ArrayList<String> values = new ArrayList<String>();
//		// Default settings
//		String playerClass = "Time";
//		boolean heuristicsSet = false;
//		// Parse arguments
//		for (int i = 0; i < args.length; i++) {
//			String argument = args[i];
//			// Split argument at the equals sign
//			int j = argument.indexOf('=');
//			String left, right;
//			if (j > 0) {
//				left = argument.substring(0, j);
//				right = argument.substring(j + 1);
//			} else {
//				left = argument;
//				right = "true";
//			}
//			// Handle properties
//			debug("property: " + left);
//			debug("property value: " + right);
//			if (left.equals("debug")) {
//				assert right.equals("true");
//				setDebugToStderr(true);
//			} else if (left.equals("debugfile")) {
//				setDebugFile(right);
//			} else if(left.equals("boardsize")){
//				StringTokenizer boardWidth = new StringTokenizer(right);
//				if (boardWidth.countTokens() == 1) {
//					int width = parseInt(boardWidth.nextToken());
//					if (width == getBoardWidth()) {
//						if(player != null) {							
//							player.reset();
//						}
//					} else if(width > 0){
//						try{
//							setBoardWidth(width);
//							if(player != null) {								
//								player.getBoard().clear();
//								player.reset();
//							}
//						}catch(IndexOutOfBoundsException e){
//							error("unacceptable size");
//						}
//					}else{
//						error("unacceptable size");
//					}
//				} else {
//					error("unacceptable size");
//				}
//					
//			} else if(left.equals("komi")) {
//				komiArgument = Double.parseDouble(right);
//			} else if(left.equals("cgtc")){//set to true if running with computer go test collection
//				cgtc=Boolean.parseBoolean(right);
//			}
//			else if (left.equals("player")) {
//				playerClass = right;
//			} else { // Let the player set this property
//				properties.add(left);
//				values.add(right);
//			}
//		}
//		try { // Create player from string
//			if (!playerClass.endsWith("Player")) {
//				playerClass += "Player";
//			}
//			for (String pkg : PLAYER_PACKAGES) {
//				String qualifiedPlayerClass = playerClass;
//				if (!qualifiedPlayerClass.startsWith("orego.")
//						&& pkg.length() > 0) {
//					qualifiedPlayerClass = pkg + "." + qualifiedPlayerClass;
//				}
//				Class<Playable> c;
//				try {
//					c = (Class<Playable>) Class.forName(qualifiedPlayerClass);
//				} catch (ClassNotFoundException e) {
//					continue;
//				}
//				Constructor<Playable> constructor = (Constructor<Playable>) c
//						.getConstructor();
//				player = constructor.newInstance();
//				break;
//			}
//		} catch (Exception e) {
//			System.err
//					.println("Does the player class have only a zero-argument constructor? It must!");
//			e.printStackTrace();
//			System.exit(1);
//		}
//		if (player == null) {
//			// We didn't manage to find a class for our player
//			throw new IllegalArgumentException(String.format(
//					"Could not create a player for class %s.", playerClass));
//		}
//		// Let the player set all other properties
//		try {
//			for (int i = 0; i < properties.size(); i++) {
//				player.setProperty(properties.get(i), values.get(i));
//				if (properties.get(i).equals("heuristics")) {
//					heuristicsSet = true;
//				}
//			}
//			// If the heuristics weren't set, use default values
//			if (!heuristicsSet) {
//				player.setProperty("heuristics",
//						"Escape@20:Pattern@20:Capture@20");
//			}
//		} catch (UnknownPropertyException e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
		buildPlayer(19);
	}
	
	private void buildPlayer(int width){
		final int milliseconds = 100;
		final int threads = 4;
		player = null;
		player = new Player(threads, CopiableStructureFactory.feasible(width));
		Board board = player.getBoard();
		coords = board.getCoordinateSystem();
		TranspositionTable table = new TranspositionTable(new SimpleSearchNodeBuilder(coords), coords);
		player.setTreeDescender(new UctDescender(board, table));
		SimpleTreeUpdater updater = new SimpleTreeUpdater(board, table);
		player.setTreeUpdater(updater);
		player.setMillisecondsPerMove(milliseconds);
	}

}
