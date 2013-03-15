package orego.core;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static org.junit.Assert.assertEquals;

import org.junit.*;

public class ParseSGFTest {

	private static ParseSGF parseSGF;
	
	@Before
	public void setUp() throws Exception {
		parseSGF =  new ParseSGF("testFiles/blunder.1.sgf.txt");
	}
	
	@Test
	public void testHandicap(){
		int[][] fooBlack = parseSGF.getAddBlack();
		int[][] barWhite = parseSGF.getAddWhite();
		
		assertEquals(fooBlack[0][0], 1);
		assertEquals(fooBlack[0][1], 0);
		assertEquals(fooBlack[1][0], 2);
		assertEquals(fooBlack[1][1], 0);
		assertEquals(fooBlack[2][0], 2);
		assertEquals(fooBlack[2][1], 1);
		assertEquals(barWhite[0][0], 2);
		assertEquals(barWhite[0][1], 5);
		assertEquals(barWhite[1][0], 3);
		assertEquals(barWhite[1][1], 3);
		assertEquals(barWhite[2][0], 3);
		assertEquals(barWhite[2][1], 8);
		
//		for( int i = 0; i < fooBlack.length; i++ ){
//			ourBoard[handicap[i][0]][handicap[i][1]] = '#';
//			ourBoard[problem[i][0]][problem[i][1]] = 'O';
//		}
	}
	
	@Test
	public void testKomi(){
		double foo = parseSGF.getKomi();
		assertEquals((int)foo,(int)7.0);
		
	}
	
	@Test
	public void testRules(){
		assertEquals(parseSGF.getRules(), "Chinese");
	}
	
	@Test
	public void testSize(){
		assertEquals(parseSGF.getSize(),19);
	}
	
	@Test
	public void testMoves(){
		assertEquals(parseSGF.getMove(0),105);
		assertEquals(parseSGF.getMove(1),126);
		assertEquals(parseSGF.getMove(2),125);
		assertEquals(parseSGF.getMove(3),106);
	}

}
