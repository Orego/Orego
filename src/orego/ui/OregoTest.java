package orego.ui;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import orego.core.Coordinates;
import orego.mcts.McPlayer;
import orego.play.ThreadedPlayer;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.ArrayList;

public class OregoTest {

	private Orego orego;

	private BufferedReader oregoOut;

	@Before
	public void setUp() throws IOException {
		PipedOutputStream out = new PipedOutputStream();
		oregoOut = new BufferedReader(new InputStreamReader(
				new PipedInputStream(out)));
		// TODO Is this expensive to do before every test?
		orego = new Orego(System.in, out, new String[] {"playouts=100", "threads=1"});
	}

	@Test
	public void testId() throws IOException {
		orego.handleCommand("53 known_command black");
		String output = oregoOut.readLine();
		assertEquals("53", output.substring(1, 3));
		assertEquals("1", output.substring(4, 5));
	}

	@Test
	public void testTrimComments() throws IOException {
		orego.handleCommand("known_command black #error");
		String output = oregoOut.readLine();
		assertEquals("= 1", output);
	}

	/** A test of the to verify the "boardsize" command. */
	@Test
	public void testBoardSize() throws IOException {
		// Any value not boardsize is invalid
		orego.handleCommand("boardsize " + (getBoardWidth() - 1));
		String output = oregoOut.readLine();
		assertEquals('?', output.charAt(0));
		oregoOut.readLine(); // read out the extra return
		// Check with the valid BOARD_WIDTH parameter
		orego.handleCommand("boardsize " + getBoardWidth());
		output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		oregoOut.readLine(); // read out the extra return
		// Check without a parameter. Check for error message.
		orego.handleCommand("boardsize");
		output = oregoOut.readLine();
		assertEquals('?', output.charAt(0));
		assertTrue(output.substring(2).equals("unacceptable size"));
		oregoOut.readLine(); // read out the extra return
		orego.handleCommand("boardsize " + 19);
	}

	/** Test the "clear_board" command. */
	@Test
	public void testClearBoard() throws IOException {
		// First play a piece, otherwise the board is already clear
		orego.handleCommand("play black a1");
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		oregoOut.readLine(); // read out the extra return
		// Next clear the board
		orego.handleCommand("clear_board");
		output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		oregoOut.readLine(); // read out the extra return
		// Verify the board is now empty
		orego.handleCommand("showboard");
		output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		for (int i = 0; i < getBoardWidth() + 2; i++) {
			output = oregoOut.readLine();
			assertFalse(output.contains("#"));
		}
		oregoOut.readLine(); // read out the extra return
	}

	/** Test the "final_score" command. */
	@Test
	public void testFinalScore() throws IOException {
		// Test the score of an empty board. White should win by default with
		// points = komi.
		orego.handleCommand("final_score");
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		String[] outcome = output.split("\\+"); // regex for split at the +
		assertTrue(outcome[0].endsWith("W")); // White wins
		double komi = orego.getPlayer().getBoard().getKomi();
		assertEquals("" + komi, outcome[1]); // by points = komi
		oregoOut.readLine(); // read out the extra return
		// Test the score when black wins. Play a single piece and black should
		// win by the max possible point minus komi.
		orego.handleCommand("play black a1");
		oregoOut.readLine(); // read out the extra returns
		oregoOut.readLine();

		orego.handleCommand("final_score");
		output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		outcome = output.split("\\+"); // regex for split at the +
		assertTrue(outcome[0].endsWith("B")); // Black wins
		double blackScore = getBoardArea() - komi; // With max territory
		assertEquals(outcome[1], "" + blackScore);
	}

	/** Verifies the command output of a genmove. */
	protected void verifyMove(String output) {
		assertEquals('=', output.charAt(0));
		// This converts the text output internal numerical format and verifies
		// the move is valid.
		assertTrue(at(output.substring(2)) < getFirstPointBeyondBoard());
	}

	@Test
	public void testGenMoveBlack() throws IOException {
		orego.handleCommand("genmove_black");
		verifyMove(oregoOut.readLine());
	}

	@Test
	public void testGenMoveWhite() throws IOException {
		orego.handleCommand("black pass");
		oregoOut.readLine();
		oregoOut.readLine();
		orego.handleCommand("genmove_white");
		verifyMove(oregoOut.readLine());
	}

	@Test
	public void testGenMove() throws IOException {
		orego.handleCommand("genmove black");
		verifyMove(oregoOut.readLine());
		oregoOut.readLine(); // read out the extra return
		orego.handleCommand("genmove white");
		verifyMove(oregoOut.readLine());
		oregoOut.readLine(); // read out the extra return
	}

	@Test
	public void testRegGenMove() throws IOException {
		orego.handleCommand("reg_genmove black");
		verifyMove(oregoOut.readLine());
		oregoOut.readLine(); // read out the extra return
		orego.handleCommand("black pass");
		oregoOut.readLine();
		oregoOut.readLine();
		orego.handleCommand("reg_genmove white");
		verifyMove(oregoOut.readLine());
		oregoOut.readLine(); // read out the extra return
	}

	@Test
	public void testKgsGenMove() throws IOException {
		orego.handleCommand("kgs-genmove_cleanup black");
		verifyMove(oregoOut.readLine());
		oregoOut.readLine(); // read out the extra return
		orego.handleCommand("kgs-genmove_cleanup white");
		verifyMove(oregoOut.readLine());
		oregoOut.readLine(); // read out the extra return
	}

	/** Tests the that genmove code resigns when appropriate. */
	@Test
	public void testResign() throws IOException {
		
			String[] problem = {
					"OOOOOOOOOOOOOOOOOOO",// 19
					"OOOOOOOOOOOOOOO.O.O",// 18
					"OOOOOOOOOOOOOOOOOOO",// 17
					"OOOOOOOOOOOOOOOOOOO",// 16
					"OOOOOOOOOOOOOOOOOOO",// 15
					"OOOOOOOOOOOOOOOOOOO",// 14
					"OOOOOOOOOOOOOOOOOOO",// 13
					"OOOOOOOOOOOOOOOOOOO",// 12
					"OOOOOOOOOOOOOOOOOOO",// 11
					"OOOOOOOOOOOOOOOOOOO",// 10
					"OOOOOOOOOOOOOOOOOOO",// 9
					"OOOOOOOOOOOOOOOOOOO",// 8
					"OOOOOOOOOOOOOOOOOOO",// 7
					"OOOOOOOOOOOOOOOOOOO",// 6
					"OOOOOOOOOOOOOOOOOOO",// 5
					"OOOOOOOOOOOOOOOOOOO",// 4
					"###OOOOOOOOOOOOOOOO",// 3
					"###..OOOOOOOOOOOOOO",// 2
					".....OOOOOOOOOOOOOO" // 1
				  // ABCDEFGHJKLMNOPQRST
			};
			orego.getPlayer().getBoard().setUpProblem(BLACK, problem);
			// Ask for a move by black
			orego.handleCommand("genmove_black");
			String output = oregoOut.readLine();
			assertEquals('=', output.charAt(0));
			// Ideally black would resign, but in practice they pass on
			// occasion.
			// Try removing the pass clause and see if things are better.
			assertTrue((at(output.substring(2)) == RESIGN)
					|| (at(output.substring(2)) == PASS));
			oregoOut.readLine(); // read out the extra return
	}

	/** Test that genmove passes when appropriate. */
	@Test
	public void testPass() throws IOException {
		// Use a default dumb player for this test, because a smarter one will
		// resign instead of passing
		PipedOutputStream out = new PipedOutputStream();
		oregoOut = new BufferedReader(new InputStreamReader(
				new PipedInputStream(out)));
		orego = new Orego(System.in, out, new String[] { "player=Player" });
			String[] problem = {
					"OOOOOOOOOOOOOOOOOOO",// 19
					"OOOOOOOOOOOOOOO.O.O",// 18
					"OOOOOOOOOOOOOOOOOOO",// 17
					"OOOOOOOOOOOOOOOOOOO",// 16
					"OOOOOOOOOOOOOOOOOOO",// 15
					"OOOOOOOOOOOOOOOOOOO",// 14
					"OOOOOOOOOOOOOOOOOOO",// 13
					"OOOOOOOOOOOOOOOOOOO",// 12
					"OOOOOOOOOOOOOOOOOOO",// 11
					"OOOOOOOOOOOOOOOOOOO",// 10
					"OOOOOOOOOOOOOOOOOOO",// 9
					"OOOOOOOOOOOOOOOOOOO",// 8
					"OOOOOOOOOOOOOOOOOOO",// 7
					"OOOOOOOOOOOOOOOOOOO",// 6
					"OOOOOOOOOOOOOOOOOOO",// 5
					"OOOOOOOOOOOOOOOOOOO",// 4
					"OOOOOOOOOOOOOOOOOOO",// 3
					"OOOOOOOOOOOOOOOOOOO",// 2
					"OOOOOOOOOOOOOOOOOOO" // 1
			      // ABCDEFGHJKLMNOPQRST
			};
			orego.getPlayer().getBoard().setUpProblem(BLACK, problem);
			// Make sure Black passes
			orego.handleCommand("genmove_black");
			String output = oregoOut.readLine();
			assertEquals('=', output.charAt(0));
			assertTrue(at(output.substring(2)) == PASS);
			oregoOut.readLine(); // read out the extra return
	}

	/** Test the "known_command' command. */
	@Test
	public void testKnownCommands() throws IOException {
		// Make sure an invalid command produces an error.
		orego.handleCommand("known_command blah");
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		assertEquals('0', output.charAt(2));
		oregoOut.readLine(); // read out the extra return
		// Make sure a valid command, well, validates
		orego.handleCommand("known_command known_command");
		output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		assertEquals('1', output.charAt(2));
		oregoOut.readLine(); // read out the extra return
	}

	/** Test the "komi" command. */
	@Test
	public void testKomi() throws IOException {
		// set the komi
		orego.handleCommand("komi 2.5");
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		// verify the komi is what it was set to
		double komi = orego.getPlayer().getBoard().getKomi();
		assertEquals(komi, 2.5, 0.00001);
		oregoOut.readLine(); // read out the extra return
	}

	/** Test the "playout-count" command. */
	@Test
	public void testPlayoutCount() throws IOException {
		orego.handleCommand("genmove black");
		oregoOut.readLine();
		oregoOut.readLine();
		orego.handleCommand("genmove white");
		oregoOut.readLine();
		oregoOut.readLine();
		orego.handleCommand("playout_count");
		String output = oregoOut.readLine();
		assertEquals("= playout=200", output);
	}

	/** Test the "list_commands" command. */
	@Test
	public void testListCommands() throws IOException {
		orego.handleCommand("list_commands");
		ArrayList<String> commands = orego.getCommands();
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		// check all commands against the command list
		assertEquals(commands.get(0), output.substring(2));
		for (int i = 1; i < commands.size(); i++) {
			assertEquals(commands.get(i), oregoOut.readLine());
		}
		oregoOut.readLine(); // read out the extra return
	}

	/** Test the "gogui-analyze_commands" command. */
	@Test
	public void testGoguiAnalyzeCommands() throws IOException {
		orego.handleCommand("gogui-analyze_commands");
		ArrayList<String> commands = orego.getGoguiCommands();
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		// check all commands against the command list
		assertEquals(commands.get(0), output.substring(2));
		for (int i = 1; i < commands.size(); i++) {
			assertEquals(commands.get(i), oregoOut.readLine());
		}
		oregoOut.readLine(); // read out the extra return
	}
	
	/**
	 * Test the "loadsgf" command. This test loads the file at
	 * testFiles/blunder.1.sgf and will fail if the file does not exist or has
	 * been modified.
	 */
	@Test
	public void testLoadSGF() {
			// test the white to move flag (the '1' at the end)
			orego.handleCommand("loadsgf testFiles/blunder.1.sgf 1");
			assertEquals(BLACK, orego.getPlayer().getBoard()
					.getColor(at("a18")));
			assertEquals(BLACK, orego.getPlayer().getBoard()
					.getColor(at("a17")));
			assertEquals(BLACK, orego.getPlayer().getBoard()
					.getColor(at("b17")));
			assertEquals(WHITE, orego.getPlayer().getBoard()
					.getColor(at("f17")));
			assertEquals(WHITE, orego.getPlayer().getBoard()
					.getColor(at("d16")));
			assertEquals(WHITE, orego.getPlayer().getBoard()
					.getColor(at("j16")));
			assertEquals(orego.getPlayer().getBoard().getColorToPlay(), WHITE);
			// test with out the flag, black to move by default
			orego.getPlayer().reset();
			orego.handleCommand("loadsgf testFiles/blunder.1.sgf");
			assertEquals(BLACK, orego.getPlayer().getBoard()
					.getColor(at("a18")));
			assertEquals(BLACK, orego.getPlayer().getBoard()
					.getColor(at("a17")));
			assertEquals(BLACK, orego.getPlayer().getBoard()
					.getColor(at("b17")));
			assertEquals(WHITE, orego.getPlayer().getBoard()
					.getColor(at("f17")));
			assertEquals(WHITE, orego.getPlayer().getBoard()
					.getColor(at("d16")));
			assertEquals(WHITE, orego.getPlayer().getBoard()
					.getColor(at("j16")));
	}

	/** Test that the "name" command actually returns Orego. */
	@Test
	public void testName() throws IOException {
		orego.handleCommand("name");
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		assertEquals("Orego", output.substring(2));
	}

	/** Test the "protocol_version" returns the correct protocol. */
	@Test
	public void testProtocolVersion() throws IOException {
		orego.handleCommand("protocol_version");
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		assertEquals(Orego.GTP_VERSION + "", output.substring(2));
	}

	/** Test the "quit" command. */
	@Test
	public void testQuit() throws IOException {
		assertTrue(orego.handleCommand("showboard"));
		assertFalse(orego.handleCommand("quit"));
	}

	/** Test the "kgs-game_over" command. */
	@Test
	public void testKgsGameOver() throws IOException {
		// This won't work if the QuitAfterGameOver.txt file contains true
		orego.handleCommand("kgs-game_over");
		assertEquals("= ", oregoOut.readLine());
	}

	/** Test the fixed_handicap command. */
	@Test
	public void testFixedHandicap() throws IOException {
		orego.handleCommand("fixed_handicap 5");
		assertEquals(BLACK, orego.getPlayer().getBoard().getColor(at("d4")));
		assertEquals(BLACK, orego.getPlayer().getBoard().getColor(at("q4")));
		assertEquals(BLACK, orego.getPlayer().getBoard().getColor(at("k10")));
		assertEquals(BLACK, orego.getPlayer().getBoard().getColor(at("d16")));
		assertEquals(BLACK, orego.getPlayer().getBoard().getColor(at("q16")));
		assertEquals(VACANT, orego.getPlayer().getBoard().getColor(at("d10")));
	}
	
	/** Test the "time_left command. */
	@Test
	public void testTimeLeftAndSettings() throws IOException {
		orego.handleCommand("time_left byoyomi 50");
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		// 6/19/2009 orego doesn't store time left anywhere, but it might at
		// some point. If it does, a test would go here
		oregoOut.readLine(); // read out the extra return
		orego.handleCommand("time_settings 50");
		output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		oregoOut.readLine(); // read out the extra return
	}

	/** Test the "undo" command. */
	@Test
	public void testUndo() throws IOException {
		// test undo on an empty board, should give an error
		orego.handleCommand("undo");
		String output = oregoOut.readLine();
		assertEquals('?', output.charAt(0));
		assertEquals("Cannot undo", output.substring(2));
		oregoOut.readLine(); // read out the extra return
		orego.handleCommand("play black a1");
		oregoOut.readLine(); // read out the extra returns
		oregoOut.readLine();
		// test undo, should pass
		orego.handleCommand("undo");
		output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		oregoOut.readLine(); // read out the extra return
	}

	/** Test the "version" command returns the correct identifier */
	@Test
	public void testVersion() throws IOException {
		orego.handleCommand("version");
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
		assertEquals(Orego.VERSION_STRING, output.substring(2,
				2 + Orego.VERSION_STRING.length()));
		assertEquals(orego.getPlayer().toString(), output
				.substring(3 + Orego.VERSION_STRING.length()));
		oregoOut.readLine(); // read out the extra return
	}

	/**
	 * Test that playing in an already occupied point returns an invalid move
	 * error.
	 */
	@Test
	public void testBogusPlay() throws IOException {
		orego.handleCommand("play black a1");
		oregoOut.readLine();
		oregoOut.readLine();
		orego.handleCommand("play white a1");
		String output = oregoOut.readLine();
		assertEquals('?', output.charAt(0));
		assertEquals("illegal move", output.substring(2));
		oregoOut.readLine(); // read out the extra return
	}

	/** Test that an invalid command generates an error message. */
	@Test
	public void testIllegalCommand() throws IOException {
		orego.handleCommand("nonsense");
		String output = oregoOut.readLine();
		assertEquals('?', output.charAt(0));
		assertEquals("unknown command: nonsense", output.substring(2));
		oregoOut.readLine(); // read out the extra return
	}

	@Test
	public void testCommandId() throws IOException {
		orego.handleCommand("23 play black a1");
		oregoOut.readLine();
		oregoOut.readLine();
		orego.handleCommand("42 play white a1");
		String output = oregoOut.readLine();
		assertEquals("?42 illegal move", output);
		oregoOut.readLine(); // read out the extra return
	}

	@Test
	public void testCommandPassedOnToPlayer() throws IOException {
		orego.handleCommand("final_status_list alive");
		String output = oregoOut.readLine();
		assertEquals('=', output.charAt(0));
	}

	@Test
	public void testCreatePlayer() {
		orego = new Orego(new String[0]);
		assertNotNull(orego.getPlayer());
	}

	@Test
	public void testCommandLineArguments() {
		orego = new Orego(new String[] { "player=MctsPlayer", "boardsize=9", "msec=100",
				"ponder" });
		McPlayer player = (McPlayer) orego.getPlayer();
		assertEquals(100, player.getMillisecondsPerMove());
		assertTrue(((ThreadedPlayer) orego.getPlayer()).isPondering());
		assertEquals(9, getBoardWidth());
		orego = new Orego(new String[] { "player=Mcts", "playouts=500" });
		player = (McPlayer) orego.getPlayer();
		assertEquals(-1, player.getMillisecondsPerMove());
		assertEquals(500, player.getPlayoutLimit());
		orego = new Orego(new String[] { "player=MctsPlayer", "boardsize=19", "msec=100",
		"ponder" });
		//sets boardsize back to 19 so tests will work?
	}
	
	@Test
	public void testCommandLineArgumentsOrder() {
		orego = new Orego(new String[] { "player=Mcts", "msec=100", "playouts=100"});
		assertEquals(-1, orego.getPlayer().getMillisecondsPerMove());
		assertEquals(100, ((McPlayer)(orego.getPlayer())).getPlayoutLimit());
		orego = new Orego(new String[] { "player=Mcts", "playouts=100", "msec=100"});
		assertEquals(100, orego.getPlayer().getMillisecondsPerMove());
		assertEquals(-1, ((McPlayer)(orego.getPlayer())).getPlayoutLimit());
	}

}
