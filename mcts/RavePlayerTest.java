package orego.mcts;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.at;
import static orego.mcts.MctsPlayerTest.TABLE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import orego.core.Board;
import orego.heuristic.HeuristicList;
import orego.play.UnknownPropertyException;
import org.junit.Before;
import org.junit.Test;

public class RavePlayerTest {
	
	private MctsPlayer player;
	
	private Board board;
	
	@Before
	public void setUp() throws Exception {
		player = new RavePlayer();
		player.setProperty("pool", "" + TABLE_SIZE);
		player.setProperty("threads", "1");
		player.setPlayoutLimit(1000);
		player.reset();
		board = new Board();
	}
	
	protected void incorporateRun(int winner, String... moves) {
		McRunnable runnable = new McRunnable(player, new HeuristicList());
		runnable.copyDataFrom(player.getBoard());
		for (String m : moves) {
			runnable.acceptMove(at(m));
		}
		runnable.acceptMove(PASS);
		runnable.acceptMove(PASS);
		player.incorporateRun(winner, runnable);
	}
	
	@Test
	public void testIncorporateRun() {
		RaveNode root = (RaveNode) player.getRoot();
		board.play(at("a1"));
		player.getTable().findOrAllocate(board.getHash());
		incorporateRun(BLACK, "a1", "a2", "a3", "a4");
		board.play(at("a2"));
		player.getTable().findOrAllocate(board.getHash());
		incorporateRun(WHITE, "a1", "a2", "a3", "a4");
		board.play(at("a3"));
		player.getTable().findOrAllocate(board.getHash());
		incorporateRun(BLACK, "a1", "a2", "a3", "a4");
		board.clear();
		board.play("a1");
		RaveNode child = (RaveNode) player.getTable().findIfPresent(
				board.getHash());
		assertNotNull(child);
		assertEquals(3.0 / 5, root.getWinRate(at("a1")), 0.01);
		assertEquals(1.0 / 2, root.getRaveWinRate(at("a2")), 0.01);
		assertEquals(3.0 / 5, root.getRaveWinRate(at("a3")), 0.01);
		assertEquals(2.0 / 5, child.getWinRate(at("a2")), 0.01);
		assertEquals(1.0 / 2, child.getRaveWinRate(at("a3")), 0.01);
		assertEquals(2.0 / 5, child.getRaveWinRate(at("a4")), 0.01);
	}

	@Test
	public void testIncorporateRun2() {
		board.copyDataFrom(player.getBoard());
		board.play("a1");
		player.getTable().findOrAllocate(board.getHash());
		incorporateRun(WHITE, "a1", "a2", "a3", "a4");
		board.play("a2");
		player.getTable().findOrAllocate(board.getHash());
		incorporateRun(BLACK, "a1", "a2", "a3", "a4");
		RaveNode root = (RaveNode) player.getRoot();
		board.clear();
		board.play(at("a1"));
		RaveNode child = (RaveNode) player.getTable().findIfPresent(
				board.getHash());
		player.setColorToPlay(WHITE);
		assertEquals(2.0 / 4, root.getWinRate(at("a1")), 0.01);
		assertEquals(1.0 / 2, root.getRaveWinRate(at("a2")), 0.01);
		assertEquals(2.0 / 4, root.getRaveWinRate(at("a3")), 0.01);
		assertNotNull(child);
		assertEquals(2.0 / 4, child.getWinRate(at("a2")), 0.01);
		assertEquals(1.0 / 2, child.getRaveWinRate(at("a3")), 0.01);
		assertEquals(2.0 / 4, child.getRaveWinRate(at("a4")), 0.01);
	}

	@Test
	public void testTreeGrowth() {
		RaveNode root = (RaveNode) player.getRoot();
		board.play(at("a1"));
		player.getTable().findOrAllocate(board.getHash());
		incorporateRun(WHITE, "a1", "a2", "a3", "a4");
		assertEquals(1.0 / 3, root.getWinRate(at("a1")), 0.01);
		assertEquals(1.0 / 2, root.getRaveWinRate(at("a2")), 0.01);
		assertEquals(1.0 / 3, root.getRaveWinRate(at("a3")), 0.01);
		RaveNode child = (RaveNode) player.getTable().findIfPresent(
				board.getHash());
		assertNotNull(child);
		assertEquals(2.0 / 3, child.getWinRate(at("a2")), 0.01);
		assertEquals(1.0 / 2, child.getRaveWinRate(at("a3")), 0.01);
		assertEquals(2.0 / 3, child.getRaveWinRate(at("a4")), 0.01);
	}
	
	@Test
	public void testMultipleMovesAtSamePointNotCounted2() {
	
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
					"OO.................",//2
					".#................." //1
			      // ABCDEFGHJKLMNOPQRST
			};
			player.setUpProblem(BLACK, problem);
			board.setUpProblem(BLACK, problem);
			board.play(at("c5"));
			player.getTable().findOrAllocate(board.getHash());
			incorporateRun(WHITE, "c5", "d7", "a1", "c1", "a5", "a1");
			RaveNode child = (RaveNode) player.getTable().findIfPresent(
					board.getHash());
			assertEquals(2.0 / 3, child.getWinRate(at("d7")), 0.01);
			assertEquals(2.0 / 3, child.getRaveWinRate(at("c1")), 0.01);
			assertEquals(1.0 / 2, child.getRaveWinRate(at("a1")), 0.01);
		
	}

	@Test
	public void testDebug1() {
		
			String[] problem = {
					"#########OOOOOOOOOO",//19
					"#########OOOOOOOOOO",//18
					"#########OOOOOOOOOO",//17
					"#########OOOOOOOOOO",//16
					"#########OOOOOOOOOO",//15
					"##########OOOOOOOOO",//14
					"##########OOOOOOOOO",//13
					"##########OOOOOOOOO",//12
					"##########OOOOOOOOO",//11
					"##########OOOOOOOOO",//10
					"..##.#..###########",//9
					"..#O.#.#.##########",//8
					"..#O##.##OOOOOOOOOO",//7
					"...#.##O########OOO",//6
					"..###OOOOOOOOO#####",//5
					"###OOOOOOOOO#######",//4
					"OOOO.O#############",//3
					"#OOOO##OOOOOOOOO###",//2
					".O###.#O.##########" //1
			       //ABCDEFGHJKLMNOPQRST
			};
			player.getBoard().setUpProblem(BLACK, problem);
			player.acceptMove(PASS);
			player.bestMove();
		
	}

	@Test
	public void testSetProperty() throws UnknownPropertyException {
		player.setProperty("bias", "0.01");
	}

	@Test
	public void testSuperko() {
		String[] problem;
		
			problem = new String[] {
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
					"......#............",//7
					"OOO................",//6
					"##O................",//5
					".#O................",//4
					"##OOO..............",//3
					"..##O..............",//2
					"##.O.O............." //1
			      // ABCDEFGHJKLMNOPQRST
			};
		
		player.setUpProblem(WHITE, problem);
		player.acceptMove(at("c1"));
		player.acceptMove(at("e1"));
		// D1 is now a superko violation
		// Add some wins so the move looks good
		player.getRoot().addWins(at("d1"), 1000);
		player.bestMove();
		assertEquals(Integer.MIN_VALUE, player.getWins(at("d1")));
	}
	
}
