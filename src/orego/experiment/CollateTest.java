package orego.experiment;
import static org.junit.Assert.*;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

public class CollateTest {
	
	private Collate collater;
	
	private ByteArrayOutputStream sysOutSniffer;
	
	private static final String TEST_RESULTS = "SgfTestFiles/9/completed_games/";
	@Before
	public void setup() {
		sysOutSniffer = new ByteArrayOutputStream();
		
		System.setOut(new PrintStream(sysOutSniffer));
		System.setErr(new PrintStream(sysOutSniffer));
		
		collater = new Collate();
	}
	
	@Test
	public void testShouldCreateCSVFile() throws FileNotFoundException {
		collater.setResultsDir(TEST_RESULTS + "fixture 1/");
		collater.enableCSVSummary(true);
		collater.collate();
		
		// read in the file and see if the first line tallies with what we want
		File csv = new File(TEST_RESULTS + "fixture 1/" + Collate.CSV_OUTPUT_FILE_PREFIX + ".csv");
		
		
		assertTrue(csv.exists());
		
		Scanner scanner = new Scanner(csv);
		
		String parameters = "threads=2 msec=8000 book=FusekiBook";
		
		// these totals were assembled by hand
		String expected = String.format("%s,%d,%d,%d,%1.3f,%d,%d,%1.3f", parameters, 0, 2,3,2.0/3.0, 2806516 + 5766717 + 4676297, 
				  														261+390+376,
				  														(2806516.0 + 5766717.0 + 4676297.0)/(261.0+390.0+376.0));
		
		
		assertTrue(scanner.hasNextLine());
		assertEquals("Configuration,Index,Wins,Runs,Win Rate,Total Playouts,Total Moves,Average PPM", scanner.nextLine());
		assertTrue(scanner.hasNextLine());
		assertEquals(expected, scanner.nextLine());
		
		assertFalse(scanner.hasNextLine());
	}
	
	@Test
	public void testShouldWriteStatisticsToScreen() throws FileNotFoundException {
		collater.setResultsDir(TEST_RESULTS + "fixture 1/");
		collater.enableCSVSummary(false);
		collater.collate();
		
		// print out the wins/runs ratio
		String parameters = "threads=2 msec=8000 book=FusekiBook";
		
		String expected = String.format(parameters + ": %d/%d = %1.3f\n", 2,3,2.0/3.0);
				
		// these totalsl were assembled by hand
		expected += String.format("Total playouts:%d, Total moves:%d, average playouts per move:%1.3f\n", 2806516 + 5766717 + 4676297, 
																										  261+390+376,
																										  (2806516.0 + 5766717.0 + 4676297.0)/(261.0+390.0+376.0));
					
		// we make sure it properly agregated the stats from the four testing files
		assertEquals(expected, sysOutSniffer.toString());
	}
	
	@Test
	public void testPlayoutCountShouldNotOverflow() throws FileNotFoundException {
		collater.setResultsDir(TEST_RESULTS + "fixture 2/");
		collater.enableCSVSummary(false);
		collater.collate();
		
		// print out the wins/runs ratio
		String parameters = "threads=2 msec=8000 book=FusekiBook";
		
		String expected = String.format(parameters + ": %d/%d = %1.3f\n", 2,3,2.0/3.0);
				
		// these totalsl were assembled by hand
		expected += String.format("Total playouts:%d, Total moves:%d, average playouts per move:%1.3f\n", (long)Integer.MAX_VALUE + (long)Integer.MAX_VALUE + 3422L, 
																										  34111+390+1121,
																										  ((long)Integer.MAX_VALUE + (long)Integer.MAX_VALUE + 3422.0)/(34111.0+390.0+1121.0));
					
		// we make sure it properly agregated the stats from the four testing files
		assertEquals(expected, sysOutSniffer.toString());
	}


}
