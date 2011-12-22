package orego.play;

import static orego.core.Colors.*;
import static org.junit.Assert.*;
import static orego.core.Coordinates.*;
import static orego.core.Board.*;
import java.io.IOException;
import java.io.PipedOutputStream;
import orego.core.Board;
import orego.core.Coordinates;
import orego.policy.*;
import orego.ui.Orego;
import org.junit.*;

public class PlayerTest {

	private Player player;
	
	@Before
	public void setUp() throws Exception {
		player = new Player();
		player.reset();
	}

	@Test
	public void testAcceptMoveSequence() {
		player.acceptMove(at("c1"));
		player.acceptMove(at("a2"));
		player.acceptMove(at("d6"));
		String sequence = player.getMoveSequence();
		String before = player.getBoard().toString();
		player.reset();
		player.acceptMoveSequence(sequence);
		assertEquals(before, player.getBoard().toString());
	}

	@Test
	public void testTurnZeroAfterReset() {
		player.acceptMove(at("c1"));
		player.acceptMove(at("a2"));
		player.acceptMove(at("d6"));
		player.reset();
		assertEquals(0, player.getTurn());
	}

	@Test
	public void testAcceptMultiStoneSuicide() {
		if(BOARD_WIDTH == 19) {
			String[] problem = {
					"...................",//19
					"...................",//18
					"...................",//17
					"...................",//16
					"...................",//15
					"...................",//14
					"...................",//13
					"...................",//12
					"...................",//11
					"OOOO#####..........",//10
					"####OOOO.#.........",//9
					"####OOOOO#.........",//8
					"####OOOOO#.........",//7
					"####OOOOO#.........",//6
					"####OOOOO#.........",//5
					"####OOOOO#.........",//4
					"####OOOOO#.........",//3
					"####OOOOO#.........",//2
					".###OOOO.#........." //1
	              // ABCDEFGHJKLMNOPQRST		
			};
			player.setUpProblem(BLACK, problem);
			player.acceptMove(at("a1"));
			assertEquals(BLACK, player.getBoard().getColor(at("b1")));
		} else {	
			String[] problem = {
				"####OOOO.",
				"####OOOOO",
				"####OOOOO",
				"####OOOOO",
				"####OOOOO",
				"####OOOOO",
				"####OOOOO",
				"####OOOOO",
				".###OOOO.",
			};
			player.setUpProblem(BLACK, problem);
			player.acceptMove(at("a1"));
			assertEquals(BLACK, player.getBoard().getColor(at("b1")));
		}
	}
	
	@Test
	public void testBestMoveIsLegal() {
		if(BOARD_WIDTH == 19) {
			String[] problem = {
					"#########OOOOOOOOO.",//19
					"#########OOOOOOOOOO",//18
					"#########OOOOOOOOOO",//17
					"#########OOOOOOOOOO",//16
					"#########OOOOOOOOOO",//15
					"#########OOOOOOOOOO",//14
					"#########OOOOOOOOOO",//13
					"#########OOOOOOOOOO",//12
					"#########OOOOOOOOOO",//11
					"#########OOOOOOOOOO",//10
					"#########OOOOOOOOOO",//9
					"#########OOOOOOOOOO",//8
					"#########OOOOOOOOOO",//7
					"#########OOOOOOOOOO",//6
					"#########OOOOOOOOOO",//5
					"#########OOOOOOOOOO",//4
					"#########OOOOOOOOOO",//3
					"#OOOOOOOOOOOOOOOOOO",//2
					".########OOOOOOOOO." //1
	              // ABCDEFGHJKLMNOPQRST		
			};
			player.setUpProblem(BLACK, problem);
			int move = player.bestMove();
			assertEquals(PASS, move);
			assertEquals(BLACK, player.getBoard().getColor(at("b1")));
		} else {	
			String[] problem = {
					"####OOOO.",
					"####OOOOO",
					"####OOOOO",
					"####OOOOO",
					"####OOOOO",
					"####OOOOO",
					"####OOOOO",
					"#OOOOOOOO",
					".###OOOO.",
			};
			player.setUpProblem(BLACK, problem);
			int move = player.bestMove();
			assertEquals(PASS, move);
			assertEquals(BLACK, player.getBoard().getColor(at("b1")));
		}
	}
	
	@Test
	public void testBoardLength() {
		for (int i = 0; i < orego.core.Board.MAX_MOVES_PER_GAME; i++) {
			assertEquals(player.acceptMove(PASS), Board.PLAY_OK);
		}
		assertEquals(Board.PLAY_GAME_TOO_LONG, player.getBoard().play("a1"));
		// Might cause array index out of bounds
	}
	
	@Test
	public void testCommandLineConstruction() {
		String[] args = { "player=orego.play.Player" };
		Orego orego = new Orego(System.in, new PipedOutputStream(), args);
		assertEquals(Player.class, orego.getPlayer().getClass());
	}
	
	@Test
	public void testDoNotGenerateSuperkoViolation() {
		if(BOARD_WIDTH == 19) {
			String[] problem = {
					".########OOOOOOOOO.",//19
					"#########OOOOOOOOOO",//18
					"#########OOOOOOOOOO",//17
					"#########OOOOOOOOOO",//16
					"#########OOOOOOOOOO",//15
					"#########OOOOOOOOOO",//14
					"#########OOOOOOOOOO",//13
					".########OOOOOOOOOO",//12
					"#########OOOOOOOOOO",//11
					"#########OOOOOOOOOO",//10
					"#########OOOOOOOOOO",//9
					"#########OOOOOOOOOO",//8
					"#########OOOOOOOOOO",//7
					"#########OOOOOOOOOO",//6
					"#########OOOOOOOOOO",//5
					"#########OOOOOOOOOO",//4
					"#########OOOOOOOOOO",//3
					"#OOOOOOOOOOOOOOOOOO",//2
					"..#OOOOOOOOOOOOOOO." //1
	              // ABCDEFGHJKLMNOPQRST		
			};
			player.setUpProblem(BLACK, problem);
			player.acceptMoveSequence("b1 a1 c1 PASS");
			int move = player.bestMove();
			assertEquals(PASS, move);
		} else {	
			String[] problem = {
					".###OOOO.",
					"####OOOOO",
					"####OOOOO",
					".###OOOOO",
					"####OOOOO",
					"####OOOOO",
					"####OOOOO",
					"#OOOOOOOO",
					"..#OOOOO.",
			};
			player.setUpProblem(BLACK, problem);
			player.acceptMoveSequence("b1 a1 c1 PASS");
			int move = player.bestMove();
			assertEquals(PASS, move);
		}
	}
	
	@Test
	public void testGetMoveSequence() {
		player.acceptMove(at("c1"));
		player.acceptMove(at("a2"));
		player.acceptMove(at("d6"));
		assertEquals("C1 A2 D6 ", player.getMoveSequence());
	}
	
	@Test
	public void testKomiSurvivesResetting() {
		player.setKomi(3.5);
		player.reset();
		assertEquals(3.5, player.getBoard().getKomi(), 0.01);
	}
	
	@Test
	public void testPositionalSuperko() {
		if(BOARD_WIDTH == 19) {
			String[] problem = {
					"...................",//19
					"...................",//18
					"...................",//17
					"...................",//16
					"...................",//15
					"...................",//14
					"...................",//13
					"...................",//12
					"...................",//11
					"...................",//10
					"...................",//9
					"...................",//8
					"...................",//7
					"...................",//6
					"...................",//5
					"...................",//4
					"...................",//3
					"O.#................",//2
					".O.#..............." //1
	              // ABCDEFGHJKLMNOPQRST		
			};
			player.setUpProblem(BLACK, problem);
			player.acceptMoveSequence("b2 c1 a1");
			assertEquals(PLAY_KO_VIOLATION, player.getBoard().play(at("b1")));
		} else {	
			String[] problem = {
					".........",
					".........",
					".........",
					".........",
					".........",
					".........",
					".........",
					"O.#......",
					".O.#.....",
			};
			player.setUpProblem(BLACK, problem);
			player.acceptMoveSequence("b2 c1 a1");
			assertEquals(PLAY_KO_VIOLATION, player.getBoard().play(at("b1")));
		}
	}

	@Test
	public void testSgf() {
		if (BOARD_WIDTH == 19) {
			player.setUpSgf("testFiles/blunder.1.sgf", 1);
			assertEquals(BLACK, player.getBoard().getColor(
					Coordinates.at("a18")));
			assertEquals(BLACK, player.getBoard().getColor(
					Coordinates.at("a17")));
			assertEquals(BLACK, player.getBoard().getColor(
					Coordinates.at("b17")));
			assertEquals(WHITE, player.getBoard().getColor(
					Coordinates.at("f17")));
			assertEquals(WHITE, player.getBoard().getColor(
					Coordinates.at("d16")));
			assertEquals(WHITE, player.getBoard().getColor(
					Coordinates.at("j16")));
		} else {
			player.setUpSgf("testFiles/blunder.1.sgf", 1);
			assertEquals(BLACK, player.getBoard()
					.getColor(Coordinates.at("a8")));
			assertEquals(BLACK, player.getBoard()
					.getColor(Coordinates.at("a7")));
			assertEquals(BLACK, player.getBoard()
					.getColor(Coordinates.at("b7")));
			assertEquals(WHITE, player.getBoard()
					.getColor(Coordinates.at("f7")));
			assertEquals(WHITE, player.getBoard()
					.getColor(Coordinates.at("d6")));
			assertEquals(WHITE, player.getBoard()
					.getColor(Coordinates.at("j6")));
		}
	}
	
	@Test
	public void testSetPropertyPolicy() throws UnknownPropertyException {
		player.setProperty("policy", "CapturePolicy:RandomPolicy");
		Policy gen = player.getPolicy();
		Policy fallback = ((CapturePolicy) gen).getFallback();
		assertEquals(gen.getClass(), CapturePolicy.class);
		assertEquals(fallback.getClass(), RandomPolicy.class);
		player.setProperty("policy", "Pattern:Random");
		gen = player.getPolicy();
		fallback = ((PatternPolicy) gen).getFallback();
		assertEquals(gen.getClass(), PatternPolicy.class);
		assertEquals(fallback.getClass(), RandomPolicy.class);
	}

	@Test
	public void testUnknownProperty() {
		try {
			player.setProperty("isSkyNet", "false");
			assertFalse(true); // Shouldn't get here
		} catch (UnknownPropertyException e) {
		}
	}

	@Test
	public void testUndo() {
		assertFalse(player.undo()); // can't undo at beginning of game
		player.acceptMove(at("c1"));
		player.acceptMove(at("a2"));
		String correct = player.getBoard().toString();
		player.acceptMove(at("d6"));
		player.undo();
		assertEquals(correct, player.getBoard().toString());
	}

	@Test
	public void testOpeningBook() throws UnknownPropertyException {
		player = new Player();
		player.setProperty("book", "StarPointsBook");
		player.reset();
		int p = player.bestMove();
		int correct;
		if (BOARD_WIDTH == 19) {
			correct = at("d4");
		} else {
			correct = at("e5");
		}
		assertEquals(correct, p);
		player.acceptMove(p);
		player.undo();
		p = player.bestMove();
		assertEquals(correct, p);
		assertEquals("orego.play.Player", player.toString());
	}
	
	@Test
	public void testFinalScore() {
		while (player.getBoard().getPasses() < 2) {
			player.acceptMove(player.bestMove());
		}
		assertEquals(player.getBoard().finalScore(), player.finalScore());
	}

	@Test
	public void testGetMillisecondsPerMove() {
		player.setRemainingTime(500);
		assertEquals(0, player.getMillisecondsPerMove());
	}

	@Test
	public void testSetColorToPlay() {
		player.setColorToPlay(WHITE);
		assertEquals(WHITE, player.getBoard().getColorToPlay());
	}

}
