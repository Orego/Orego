package orego.shape;

import static org.junit.Assert.*;
import static orego.core.Coordinates.*;
import orego.core.*;
import org.junit.Before;
import org.junit.Test;

public class PatternExtractorTest {

	private PatternExtractor extractor;
	
	@Before
	public void setUp() throws Exception {
		extractor = new PatternExtractor();
	}

	@Test
	public void testPatternExtractor() {
		extractor.run("SgfTestFiles", 4, 16);
		Cluster cluster = extractor.getCluster();
		Board b = new Board();
		/*System.out.println(cluster.getWinRate(b, at("r16"))+",r16");
		System.out.println(cluster.getWinRate(b, at("k16"))+",k16");*/
		assertTrue(cluster.getWinRate(b, at("r16")) > 0.3);
		assertTrue(cluster.getWinRate(b, at("k16")) < 0.3);
	}
	
	@Test
	public void testPatternExtractorParameters() {
		extractor.run("SgfTestFiles", 2, 17);
		Cluster cluster = extractor.getCluster();
		Board b = new Board();
		/*System.out.println(cluster.getWinRate(b, at("r16"))+",r16");
		System.out.println(cluster.getWinRate(b, at("k16"))+",k16");*/
		assertTrue(cluster.getWinRate(b, at("r16")) > 0.3);
		assertTrue(cluster.getWinRate(b, at("k16")) < 0.3);
		extractor.run("SgfTestFiles", 1, 18);
		cluster = extractor.getCluster();
		b = new Board();
		/*System.out.println(cluster.getWinRate(b, at("r16"))+",r16");
		System.out.println(cluster.getWinRate(b, at("k16"))+",k16");*/
		assertTrue(cluster.getWinRate(b, at("r16")) > 0.3);
		assertTrue(cluster.getWinRate(b, at("k16")) < 0.3);
	}

}

