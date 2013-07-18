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
		extractor.run("SgfTestFiles");
		Cluster cluster = extractor.getCluster();
		Board b = new Board();
		assertTrue(cluster.getWinRate(b, at("r16")) > 0.5);
		assertTrue(cluster.getWinRate(b, at("k16")) < 0.5);
	}

}
