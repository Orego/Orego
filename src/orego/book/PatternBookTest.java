package orego.book;

import static org.junit.Assert.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import java.io.File;
import orego.core.Board;
import orego.core.Coordinates;
import orego.shape.PatternExtractor;
import org.junit.Before;
import org.junit.Test;

public class PatternBookTest {
	
	private PatternBook book;

	@Before
	public void setUp() throws Exception {
		new PatternExtractor().run(orego.experiment.Debug.OREGO_ROOT_DIRECTORY+"SgfTestFiles"+File.separator+getBoardWidth()+File.separator, "SgfTestFiles"+File.separator+getBoardWidth(), 4, 16);
		book = new PatternBook("SgfTestFiles"+File.separator+getBoardWidth()+File.separator+"Patternsr4t4b16.data",0);
	}
	
	@Test 
	public void testLoadCluster() throws Exception {
		book.loadCluster("SgfTestFiles"+File.separator+getBoardWidth()+File.separator+"Patternsr4t4b16.data");
	}
	
	@Test
	public void testNextMove(){
		Board b = new Board();
		String[] problem = { 
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...#...........O...",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"..#................",// 5
				"...................",// 4
				"...O..........#....",// 3
				"...................",// 2
				"..................." // 1
		      // ABCDEFGHJKLMNOPQRST
		};
		b.setUpProblem(WHITE, problem);
		int move = book.nextMove(b);
		System.out.println(pointToString(move));
		assertEquals(Coordinates.at("c4"), move);
	}

}
