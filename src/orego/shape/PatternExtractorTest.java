package orego.shape;

import static org.junit.Assert.*;

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
	}

}
